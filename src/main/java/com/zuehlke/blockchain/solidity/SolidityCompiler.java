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
 * Created by hem on 18.05.2017.
 * Inspired by https://github.com/ethereum/ethereumj/tree/develop/ethereumj-core/src/main/java/org/ethereum/solidity
 */
public class SolidityCompiler {

    private SolC solc;

    private static SolidityCompiler INSTANCE;

    private SolidityCompiler() {
        solc = new SolC();
    }

    public CompilerResult compileSrc(byte[] source, boolean optimize, boolean combinedJson, SolidityCompiler.Options... options) throws IOException {
        List<String> commandParts = prepareCommandOptions(optimize, combinedJson, options);

        ProcessBuilder processBuilder = new ProcessBuilder(commandParts)
                .inheritIO()
                .directory(solc.getExecutable().getParentFile());
        processBuilder.environment().put("LD_LIBRARY_PATH", solc.getExecutable().getParentFile().getCanonicalPath());

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
            throw new RuntimeException(e);
        }
        return new CompilerResult(error, output, success);
    }

    private String getInputStreamAsString(InputStream is) {
        try (BufferedReader outReader = new BufferedReader(new InputStreamReader(is))) {
            return String.join(System.lineSeparator(), outReader.lines().collect(toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private List<String> prepareCommandOptions(boolean optimize, boolean combinedJson, SolidityCompiler.Options... options) throws IOException {
        List<String> commandParts = new ArrayList<>();
        commandParts.add(solc.getExecutable().getCanonicalPath());
        if (optimize) {
            commandParts.add("--optimize");
        }
        if (combinedJson) {
            commandParts.add("--combined-json");
            commandParts.add(Arrays.stream(options).map(option1 -> option1.toString() ).collect(Collectors.joining(",")));
        } else {
            for (SolidityCompiler.Options option : options) {
                commandParts.add("--" + option.getName());
            }
        }
        return commandParts;
    }


    public static SolidityCompiler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SolidityCompiler();
        }
        return INSTANCE;
    }

    public enum Options {
        AST("ast"),
        BIN("bin"),
        INTERFACE("interface"),
        ABI("abi"),
        METADATA("metadata"),
        ASTJSON("ast-json");

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
