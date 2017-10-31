package org.web3j.mavenplugin.solidity;

import org.apache.maven.plugin.logging.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compiles the given Solidity Contracts into binary code.
 *
 * Inspired by https://github.com/ethereum/ethereumj/tree/develop/ethereumj-core/src/main/java/org/ethereum/solidity
 */
public class SolidityCompiler {

    private SolC solc;

    private Log LOG;

    private static SolidityCompiler INSTANCE;

    private SolidityCompiler(Log log) {
        this.LOG = log;
        if (!solidityCompilerExists()) {
            LOG.info("Solidity Compiler from library is used");
            solc = new SolC();
        }
    }

    public static SolidityCompiler getInstance(Log log) {
        if (INSTANCE == null) {
            INSTANCE = new SolidityCompiler(log);
        }
        return INSTANCE;
    }

    public CompilerResult compileSrc(
            byte[] source, SolidityCompiler.Options... options) {


        boolean success = false;
        String error;
        String output;
        Process process;

        try {
            process = (solc != null)
                    ? getSolCProcessFromLibrary(options)
                    : getSolCProcessFromSystem(options);

            try (BufferedOutputStream stream = new BufferedOutputStream(process.getOutputStream())) {
                stream.write(source);
            }
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
        }

        return new CompilerResult(error, output, success);
    }

    private Process getSolCProcessFromLibrary(Options[] options) throws IOException {
        assert solc != null;

        Process process;
        String canonicalSolCPath = solc.getCanonicalPath();
        List<String> commandParts = prepareCommandOptions(canonicalSolCPath, options);
        ProcessBuilder processBuilder = new ProcessBuilder(commandParts)
                .directory(solc.getWorkingDirectory());
        processBuilder
                .environment()
                .put("LD_LIBRARY_PATH", solc.getCanonicalWorkingDirectory());
        process = processBuilder.start();
        return process;
    }

    private Process getSolCProcessFromSystem(Options[] options) throws IOException {
        Process process;
        List<String> commandParts = prepareCommandOptions("solc", options);
        process = Runtime.getRuntime().exec(commandParts.toArray(new String[commandParts.size()]));
        return process;
    }

    private List<String> prepareCommandOptions(String canonicalSolCPath, SolidityCompiler.Options... options) {
        List<String> commandParts = new ArrayList<>();
        commandParts.add(canonicalSolCPath);
        commandParts.add("--optimize");
        commandParts.add("--combined-json");
        commandParts.add(Arrays.stream(options).map(option -> option.toString()).collect(Collectors.joining(",")));
        return commandParts;
    }

    private boolean solidityCompilerExists() {
        try {
            Process p = Runtime.getRuntime().exec("solc --version");

            String output;
            try (java.util.Scanner s = new java.util.Scanner(p.getInputStream())) {
                output = s.useDelimiter("\\A").hasNext() ? s.next() : "";
            }
            if (p.waitFor() == 0) {
                LOG.info("Solidity Compiler found");
                LOG.debug(output);
                return true;
            } else {
                LOG.error(output);
            }
        } catch (InterruptedException | IOException e) {
            LOG.info("Solidity Compiler not installed.");
        }
        return false;
    }

    public enum Options {
        BIN("bin"),
        INTERFACE("interface"),
        ABI("abi"),
        METADATA("metadata"),;

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

        //private StringBuilder content = new StringBuilder();

        ParallelReader(InputStream stream) {
            this.stream = stream;
        }

        public String getContent() throws InterruptedException {
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
