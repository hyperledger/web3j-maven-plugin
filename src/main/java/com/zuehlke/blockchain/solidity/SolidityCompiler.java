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

import static java.util.stream.Collectors.toList;

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

        String error = getInputStreamAsString(process.getErrorStream());
        String output = getInputStreamAsString(process.getInputStream());

        boolean success;
        try {
            success = process.waitFor() == 0;
        } catch (InterruptedException e) {
            //TODO
            throw new RuntimeException(e);
        }
        return new CompilerResult(error, output, success);
    }

    private String getInputStreamAsString(InputStream is) {
        try (BufferedReader outReader = new BufferedReader(new InputStreamReader(is))) {
            return String.join(System.lineSeparator(), outReader.lines().collect(toList()));
        } catch (IOException e) {
            //TODO
            throw new RuntimeException(e);
        }
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

}
