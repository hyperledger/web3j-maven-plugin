package org.web3j.mavenplugin.solidity;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;

public class SolidityCompilerTest {

    private SolidityCompiler solidityCompiler;

    @Test
    public void compileContract() {
        Set<String> source = Collections.singleton("Greeter.sol");

        CompilerResult compilerResult = solidityCompiler.compileSrc("src/test/resources/", source, new String[0], SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);

        assertFalse(compilerResult.errors, compilerResult.isFailed());
        // We don't need this assertion, newer versions of solc may always print warnings to stderr about our source files
        // assertTrue(compilerResult.errors, compilerResult.errors.isEmpty());
        assertFalse(compilerResult.output.isEmpty());

        assertTrue(compilerResult.output.contains("Greeter.sol:greeter\""));
    }

    @Test
    public void invalidContractSyntax() {
        Set<String> source = Collections.singleton("Greeter-invalid-syntax.sol");

        CompilerResult compilerResult = solidityCompiler.compileSrc("src/test/resources/", source, new String[0], SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);

        assertTrue(compilerResult.isFailed());
        assertFalse(compilerResult.errors.isEmpty());
        assertTrue(compilerResult.output.isEmpty());
    }

    @Test
    public void invalidContractVersion() {
        Set<String> source = Collections.singleton("Greeter-invalid-version.sol");

        try {
            solidityCompiler.compileSrc("src/test/resources/", source, new String[0], SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);
            fail("Should throw a version mismatch exception.");
        } catch (Exception v) {
            assertThat(v.getMessage(), containsString("No compatible solc release could be found for the file"));
        }
    }

    @Test
    public void pragmaVersionTooHigh() {
        Set<String> source = Collections.singleton("TooHighPragmaVersion.sol");

        try {
            solidityCompiler.compileSrc("src/test/resources/", source, new String[0], SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);
            fail("Should throw a version mismatch exception.");
        } catch (Exception v) {
            assertThat(v.getMessage(), containsString("No compatible solc release could be found for the file"));
        }
    }

    @Before
    public void loadCompiler() throws MojoExecutionException {
        solidityCompiler = SolidityCompiler.getInstance(new SystemStreamLog());
    }
}