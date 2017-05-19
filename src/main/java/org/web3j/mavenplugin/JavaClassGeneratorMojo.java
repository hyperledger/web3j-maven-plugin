package org.web3j.mavenplugin;


import org.web3j.mavenplugin.solidity.CompilerResult;
import org.web3j.mavenplugin.solidity.SolidityCompiler;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.web3j.codegen.SolidityFunctionWrapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Maven Plugin to generate the java classes out of the solidity contract files.
 *
 */
@Mojo(name = "generate-sources",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class JavaClassGeneratorMojo extends AbstractMojo {

    private static final String DEFAULT_INCLUDE = "**/*.sol";
    private static final String DEFAULT_PACKAGE = "org.web3j.model";
    private static final String DEFAULT_SOURCE_DESTINATION = "org.web3j.model";

    @Parameter(property = "packageName", defaultValue = DEFAULT_PACKAGE)
    protected String packageName;

    @Parameter(property = "sourceDestination", defaultValue = DEFAULT_SOURCE_DESTINATION)
    protected String sourceDestination;

    @Parameter(property = "soliditySourceFiles", required = true)
    protected FileSet soliditySourceFiles;

    public void execute() throws MojoExecutionException {
        if (soliditySourceFiles.getDirectory() == null) {
            String absolutePath = new File(Paths.get(".").toUri()).getAbsolutePath();
            getLog().info("No solidity directory specified, using current working directory [" + absolutePath + "]");
            soliditySourceFiles.setDirectory(absolutePath);
        }
        if (soliditySourceFiles.getIncludes().size() == 0) {
            getLog().info("No solidity contracts specified, using the default [" + DEFAULT_INCLUDE + "]");
            soliditySourceFiles.setIncludes(Collections.singletonList(DEFAULT_INCLUDE));
        }

        for (String includedFile : new FileSetManager().getIncludedFiles(soliditySourceFiles)) {
            getLog().debug("process '" + includedFile + "'");
            processContractFile(includedFile);
            getLog().debug("processed '" + includedFile + "'");
        }
    }

    private void processContractFile(String includedFile) throws MojoExecutionException {
        getLog().debug("\tCompile '" + soliditySourceFiles.getDirectory() + File.separator + includedFile + "'");
        String result = parseSoliditySource(includedFile);
        getLog().debug("\tCompiled '" + includedFile + "'");

        Map<String, Map<String, String>> contracts = extractContracts(result);
        for (String contractName : contracts.keySet()) {
            try {
                generatedJavaClass(contracts, contractName);
                getLog().debug("\tBuilt Class for contract '" + contractName + "'");
            } catch (ClassNotFoundException | IOException ioException) {
                getLog().error("Could not build java class for contract '" + contractName + "'", ioException);
            }
        }
    }

    private Map<String, Map<String, String>> extractContracts(String result) throws MojoExecutionException {
        try {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            String script = "Java.asJSONCompatible(" + result + ")";
            Map<String, Object> json = (Map<String, Object>) engine.eval(script);
            return (Map<String, Map<String, String>>) json.get("contracts");
        } catch (ScriptException e) {
            throw new MojoExecutionException("Could not parse SolC result", e);
        }
    }

    private String parseSoliditySource(String includedFile) throws MojoExecutionException {
        try {
            byte[] contract = Files.readAllBytes(Paths.get(soliditySourceFiles.getDirectory(), includedFile));
            CompilerResult result = SolidityCompiler.getInstance().compileSrc(
                    contract,
                    SolidityCompiler.Options.ABI,
                    SolidityCompiler.Options.BIN,
                    SolidityCompiler.Options.INTERFACE,
                    SolidityCompiler.Options.METADATA
                    );
            if (result.isFailed()) {
                throw new MojoExecutionException("Could not compile solidity files\n" + result.errors);
            }
            getLog().debug("\t\tResult:\t" + result.output);
            getLog().debug("\t\tError: \t" + result.errors);
            return result.output;
        } catch (IOException ioException) {
            throw new MojoExecutionException("Could not compile files", ioException);
        }
    }

    private void generatedJavaClass(Map<String, Map<String, String>> result, String contractName) throws IOException, ClassNotFoundException {
        new SolidityFunctionWrapper().generateJavaFiles(
                contractName,
                result.get(contractName).get(SolidityCompiler.Options.BIN.getName()),
                result.get(contractName).get(SolidityCompiler.Options.ABI.getName()),
                sourceDestination,
                packageName);
    }
}
