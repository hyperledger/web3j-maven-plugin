package org.web3j.mavenplugin.solidity;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;

//TODO
public class SolidityCompilerTest {

    @Test
    public void compileSources() throws Exception {
        SolidityCompiler solidityCompiler = SolidityCompiler.getInstance();

        Path path = Paths.get("src/test/resources/Greeter.sol");
        byte[] source = Files.readAllBytes(path);

        CompilerResult compilerResult = solidityCompiler.compileSrc(source, SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);

        assertNotNull(compilerResult.output);
    }

}