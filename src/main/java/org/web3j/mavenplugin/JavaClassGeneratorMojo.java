package org.web3j.mavenplugin;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.web3j.codegen.SolidityFunctionWrapper;
import org.web3j.mavenplugin.solidity.CompilerResult;
import org.web3j.mavenplugin.solidity.SolidityCompiler;

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
 */
@Mojo(name = "generate-sources",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class JavaClassGeneratorMojo extends AbstractMojo {

    private static final String DEFAULT_INCLUDE = "**/*.sol";
    private static final String DEFAULT_PACKAGE = "org.web3j.model";
    private static final String DEFAULT_SOURCE_DESTINATION = "src/main/java";
    private static final String DEFAULT_SOLIDITY_SOURCES = "src/main/resources";

    @Parameter(property = "packageName", defaultValue = DEFAULT_PACKAGE)
    protected String packageName;

    @Parameter(property = "sourceDestination", defaultValue = DEFAULT_SOURCE_DESTINATION)
    protected String sourceDestination;

    @Parameter(property = "soliditySourceFiles")
    protected FileSet soliditySourceFiles = new FileSet();

    @Parameter(property = "contract")
    protected Contract contract;

    public void execute() throws MojoExecutionException {

        if (soliditySourceFiles.getDirectory() == null) {
            getLog().info("No solidity directory specified, using default directory [" + DEFAULT_SOLIDITY_SOURCES + "]");
            soliditySourceFiles.setDirectory(DEFAULT_SOLIDITY_SOURCES);
        }
        if (soliditySourceFiles.getIncludes().size() == 0) {
            getLog().info("No solidity contracts specified, using the default [" + DEFAULT_INCLUDE + "]");
            soliditySourceFiles.setIncludes(Collections.singletonList(DEFAULT_INCLUDE));
        }

        for (String includedFile : new FileSetManager().getIncludedFiles(soliditySourceFiles)) {
            getLog().info("process '" + includedFile + "'");
            processContractFile(includedFile);
            getLog().debug("processed '" + includedFile + "'");
        }
    }

    private void processContractFile(String includedFile) throws MojoExecutionException {
        getLog().debug("\tCompile '" + soliditySourceFiles.getDirectory() + File.separator + includedFile + "'");
        String result = parseSoliditySource(includedFile);
        getLog().debug("\tCompiled '" + includedFile + "'");

        Map<String, Map<String, String>> contracts = extractContracts(result);
        if (contracts == null) {
            getLog().warn("\tNo Contract found for file '" + includedFile + "'");
            return;
        }
        for (String contractName : contracts.keySet()) {
            if (isFiltered(contractName)) {
                getLog().debug("\tContract '" + contractName + "' is filtered");
                continue;
            }
            try {
                generatedJavaClass(contracts, contractName);
                getLog().info("\tBuilt Class for contract '" + contractName + "'");
            } catch (ClassNotFoundException | IOException ioException) {
                getLog().error("Could not build java class for contract '" + contractName + "'", ioException);
            }
        }
    }

    private Map<String, Map<String, String>> extractContracts(String result) throws MojoExecutionException {
        try {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            String script = "JSON.parse(JSON.stringify(" + result + "))";
//            String script = "Java.asJSONCompatible('" + result + "')"; //Java 8, Update 60 is needed for that. travis ci has jdk1.8_b31 installed
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
        new SolidityFunctionWrapper(true).generateJavaFiles(
                contractName,
                result.get(contractName).get(SolidityCompiler.Options.BIN.getName()),
                result.get(contractName).get(SolidityCompiler.Options.ABI.getName()),
                sourceDestination,
                packageName);
    }

    private boolean isFiltered(String contractName) {
        if (contract == null) {
            return false;
        }

        if (contract.getExcludes() != null && !contract.getExcludes().isEmpty()) {
            return contract.getExcludes().contains(contractName);
        }

        if (contract.getIncludes() == null || contract.getIncludes().isEmpty()) {
            return false;
        } else {
            return !contract.getIncludes().contains(contractName);
        }
    }
}
