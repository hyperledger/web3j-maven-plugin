package com.zuehlke.blockchain.solidity;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    private static SolidityCompiler INSTANCE;

    private SolidityCompiler() {
        solc = new SolC();
    }

    public CompilerResult compileSrc(
            byte[] source, SolidityCompiler.Options... options)
            throws IOException {
        List<String> commandParts = prepareCommandOptions(options);

        ProcessBuilder processBuilder = new ProcessBuilder(commandParts)
                .directory(solc.getExecutable().getParentFile());
        processBuilder
                .environment()
                .put("LD_LIBRARY_PATH", solc.getExecutable().getParentFile().getCanonicalPath());

        Process process = processBuilder.start();

        // Write Data into SolC
        try (BufferedOutputStream stream = new BufferedOutputStream(process.getOutputStream())) {
            stream.write(source);
        }

        ParallelReader errorReader = new ParallelReader(process.getErrorStream());
        ParallelReader outputReader = new ParallelReader(process.getInputStream());
        errorReader.start();
        outputReader.start();


        boolean success;
        try {
            success = process.waitFor() == 0;
        } catch (InterruptedException e) {
            //TODO
            throw new RuntimeException(e);
        }

        String error = errorReader.getContent();
        String output = outputReader.getContent();

        return new CompilerResult(error, output, success);
    }

    private List<String> prepareCommandOptions(SolidityCompiler.Options... options) throws IOException {
        List<String> commandParts = new ArrayList<>();
        commandParts.add(solc.getExecutable().getCanonicalPath());
        commandParts.add("--optimize");
        commandParts.add("--combined-json");
        commandParts.add(Arrays.stream(options).map(option -> option.toString()).collect(Collectors.joining(",")));
        return commandParts;
    }


    public static SolidityCompiler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SolidityCompiler();
        }
        return INSTANCE;
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

        public String getContent() {
            return getContent(true);
        }

        public synchronized String getContent(boolean waitForComplete) {
            if (waitForComplete) {
                while (stream != null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return content;
        }

        public void run() {

            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(stream))) {
                content = buffer.lines().collect(Collectors.joining(System.lineSeparator()));
            /*
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {

                List<String> collect = reader.lines().collect(StringBuilder);
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                */
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                synchronized (this) {
                    stream = null;
                    notifyAll();
                }
            }
        }
    }
}
