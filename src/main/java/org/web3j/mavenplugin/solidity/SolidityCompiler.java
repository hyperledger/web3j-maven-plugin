package org.web3j.mavenplugin.solidity;

import org.apache.maven.plugin.logging.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Compiles the given Solidity Contracts into binary code.
 * <p>
 * Inspired by https://github.com/ethereum/ethereumj/tree/develop/ethereumj-core/src/main/java/org/ethereum/solidity
 */
public class SolidityCompiler {

    private SolC solc;

    private Log LOG;

    private String usedSolCVersion;

    private static SolidityCompiler INSTANCE;

    private SolidityCompiler(Log log) {
        this.LOG = log;
        Optional<String> solCVersion = getSolCVersionFromSystemPath();
        if (solCVersion.isPresent()) {
            LOG.info("Solidity Compiler from library is used");
            usedSolCVersion = solCVersion.get();
        } else {
            solc = new SolC();
            usedSolCVersion = solc.getVersion();
        }
    }

    public static SolidityCompiler getInstance(Log log) {
        if (INSTANCE == null) {
            INSTANCE = new SolidityCompiler(log);
        }
        return INSTANCE;
    }

    public CompilerResult compileSrc(
            String rootDirectory, Collection<String> sources,
            String[] pathPrefixes,
            SolidityCompiler.Options... options) {


        boolean success = false;
        String error;
        String output;
        Process process;

        try {
            process = (solc != null)
                    ? getSolCProcessFromLibrary(rootDirectory, sources, pathPrefixes, options)
                    : getSolCProcessFromSystem(rootDirectory, sources, pathPrefixes, options);

            ParallelReader errorReader = new ParallelReader(process.getErrorStream());
            ParallelReader outputReader = new ParallelReader(process.getInputStream());
            errorReader.start();
            outputReader.start();

            success = process.waitFor() == 0;
            error = errorReader.getContent();
            output = outputReader.getContent();

        } catch (IOException | InterruptedException e) {
            StringWriter errorWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(errorWriter));
            error = errorWriter.toString();
            output = "";
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }

        return new CompilerResult(error, output, success);
    }

    private Process getSolCProcessFromLibrary(String rootDirectory, Collection<String> sources, String[] pathPrefixes, Options[] options) throws IOException {
        assert solc != null;

        Process process;
        String canonicalSolCPath = solc.getCanonicalPath();

        List<String> commandParts = prepareCommandOptions(canonicalSolCPath, rootDirectory, sources, pathPrefixes, options);

        ProcessBuilder processBuilder = new ProcessBuilder(commandParts)
                .directory(solc.getWorkingDirectory());
        processBuilder
                .environment()
                .put("LD_LIBRARY_PATH", solc.getCanonicalWorkingDirectory());
        process = processBuilder.start();
        return process;
    }

    private Process getSolCProcessFromSystem(String rootDirectory, Collection<String> sources, String[] pathPrefixes, Options[] options) throws IOException {
        Process process;
        List<String> commandParts = prepareCommandOptions("solc", rootDirectory, sources, pathPrefixes, options);
        process = Runtime.getRuntime().exec(commandParts.toArray(new String[commandParts.size()]));
        return process;
    }

    private Map<String, String> getAbsolutePathPrefixes(String rootDirectory, String[] pathPrefixes) {
        return Stream.of(pathPrefixes)
                .map(pathPrefix -> replaceMakePathPrefixAbsolute(rootDirectory, pathPrefix))
                .collect(Collectors.toMap(p -> p[0], p -> p[1]));
    }

    public Optional<String> getSolCVersionFromSystemPath() {
        try {
            Process p = Runtime.getRuntime().exec("solc --version");

            String output;
            try (java.util.Scanner s = new java.util.Scanner(p.getInputStream())) {
                output = s.useDelimiter("\\A").hasNext() ? s.next() : "";
            }
            if (p.waitFor() == 0) {
                LOG.info("Solidity Compiler found");
                LOG.debug(output);

                Matcher matcher = Constant.SOLC_VERSION_PATTERN.matcher(output);
                if (matcher.find()) {
                    return Optional.ofNullable(matcher.group(1));
                }
            } else {
                LOG.error(output);
            }
        } catch (InterruptedException e) {
            LOG.error("Could not read from solc process.");
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            LOG.info("Solidity Compiler not installed.");
        }
        return Optional.empty();
    }

    private String prepareAllowPath(String rootDirectory, String[] pathPrefixes) {
        return Stream.concat(
                Stream.of(rootDirectory).map(this::toAbsolutePath),
                getAbsolutePathPrefixes(rootDirectory, pathPrefixes).values().stream()
        ).collect(Collectors.joining(","));
    }

    private List<String> prepareCommandOptions(String canonicalSolCPath, String rootDirectory, Collection<String> sources, String[] pathPrefixes, SolidityCompiler.Options... options) {
        String outputFormats = Arrays.stream(options).map(Options::toString).collect(Collectors.joining(","));
        String allowedPaths = prepareAllowPath(rootDirectory, pathPrefixes);
        List<String> dependencyPath = getAbsolutePathPrefixes(rootDirectory, pathPrefixes).entrySet().stream().map(entry1 -> entry1.getKey() + "=" + entry1.getValue()).collect(Collectors.toList());
        List<String> sourceFiles = sources.stream().map(source -> toAbsolutePath(rootDirectory, source)).collect(Collectors.toList());

        List<String> commandParts = new ArrayList<>();
        commandParts.add(canonicalSolCPath);
        commandParts.add("--optimize");
        commandParts.add("--combined-json");
        commandParts.add(outputFormats);
        commandParts.add("--allow-paths");
        commandParts.add(allowedPaths);
        commandParts.addAll(dependencyPath);
        commandParts.addAll(sourceFiles);
        return commandParts;
    }

    private String toAbsolutePath(String baseDirectory, String... subDirectories) {
        return Paths.get(baseDirectory, subDirectories).normalize().toFile().getAbsolutePath();
    }

    String[] replaceMakePathPrefixAbsolute(String baseDirectory, String pathPrefix) {
        String[] prefixAndPath = pathPrefix.split("=", 2);
        prefixAndPath[1] = toAbsolutePath(baseDirectory, prefixAndPath[1]);
        return prefixAndPath;
    }

    public String getUsedSolCVersion() {
        return usedSolCVersion;
    }

    public enum Options {
        BIN("bin"),
        INTERFACE("interface"),
        ABI("abi"),
        METADATA("metadata"),
        ;

        private String name;

        Options(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class ParallelReader extends Thread {

        private InputStream stream;
        private String content;

        ParallelReader(InputStream stream) {
            this.stream = stream;
        }

        public String getContent() {
            return getContent(true);
        }

        public synchronized String getContent(boolean waitForComplete) {
            if (waitForComplete) {
                while (stream != null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        // we are being interrupted so we should stop running
                        return null;
                    }
                }
            }
            return content;
        }

        @Override
        public void run() {

            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(stream))) {
                content = buffer.lines().collect(Collectors.joining(System.lineSeparator()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                synchronized (this) {
                    stream = null;
                    notifyAll();
                }
            }
        }
    }
}
