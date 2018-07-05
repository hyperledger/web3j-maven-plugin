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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Parameter(property = "nativeJavaType", defaultValue = "true")
    protected boolean nativeJavaType;

    @Parameter(property = "bin", defaultValue = "false")
    protected boolean generateBIN;

    @Parameter(property = "abi", defaultValue = "false")
    protected boolean generateABI;


    public void execute() throws MojoExecutionException {

        if (soliditySourceFiles.getDirectory() == null) {
            getLog().info("No solidity directory specified, using default directory [" + DEFAULT_SOLIDITY_SOURCES + "]");
            soliditySourceFiles.setDirectory(DEFAULT_SOLIDITY_SOURCES);
        }
        if (soliditySourceFiles.getIncludes().size() == 0) {
            getLog().info("No solidity contracts specified, using the default [" + DEFAULT_INCLUDE + "]");
            soliditySourceFiles.setIncludes(Collections.singletonList(DEFAULT_INCLUDE));
        }

        String[] files = new FileSetManager().getIncludedFiles(soliditySourceFiles);
        if (files != null) {
            processContractFile(Stream.of(files)
                    .filter(f -> {
                        getLog().info("Adding to process '" + f + "'");
                        return true;
                    })
                    .collect(Collectors.toList()));
        }
		/*
        for (String includedFile : new FileSetManager().getIncludedFiles(soliditySourceFiles)) {
            getLog().info("process '" + includedFile + "'");
            processContractFile(includedFile);
            getLog().debug("processed '" + includedFile + "'");
        }
		*/
    }

    private Map<String, Map<String, String>> extractContracts(String result) throws MojoExecutionException {
        try {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            String script = "JSON.parse(JSON.stringify(" + result + "))";
            Map<String, Object> json = (Map<String, Object>) engine.eval(script);
            Map<String, Map<String, String>> contracts = (Map<String, Map<String, String>>) json.get("contracts");
            if (contracts == null) {
                getLog().warn("no contracts found");
                return null;
            }
            Map<String, String> contractRemap = new HashMap<>();
            for (String contractFilename : contracts.keySet()) {
                Map<String, String> contractMetadata = contracts.get(contractFilename);
                String metadata = contractMetadata.get("metadata");
                if (metadata == null || metadata.length() == 0) {
                    contracts.remove(contractFilename);
                    continue;
                }
                getLog().debug("metadata:" + metadata);
                String metadataScript = "JSON.parse(JSON.stringify(" + metadata + "))";
                Map<String, Object> metadataJson = (Map<String, Object>) engine.eval(metadataScript);
                Object settingsMap = metadataJson.get("settings");
                if (settingsMap != null) {
                    Map<String, String> compilationTarget = ((Map<String, Map<String, String>>) settingsMap).get("compilationTarget");
                    if (compilationTarget != null) {
                        for (Map.Entry<String, String> entry : compilationTarget.entrySet()) {
                            String value = entry.getValue();
                            contractRemap.put(contractFilename, value);
                        }
                    }
                }
                Map<String, String> compiledContract = contracts.remove(contractFilename);
                String contractName = contractRemap.get(contractFilename);
                contracts.put(contractName, compiledContract);
            }
            return contracts;
        } catch (ScriptException e) {
            throw new MojoExecutionException("Could not parse SolC result", e);
        }
    }

    private String parseSoliditySources(Collection<String> includedFiles) throws MojoExecutionException {
        if (includedFiles == null || includedFiles.isEmpty())
            return "{}";
        CompilerResult result = SolidityCompiler.getInstance(getLog()).compileSrc(
                soliditySourceFiles.getDirectory(),
                includedFiles,
                SolidityCompiler.Options.ABI,
                SolidityCompiler.Options.BIN,
                SolidityCompiler.Options.INTERFACE,
                SolidityCompiler.Options.METADATA
        );
        if (result.isFailed()) {
            throw new MojoExecutionException("Could not compile solidity files\n" + result.errors);
        }

        getLog().debug("\t\tResult:\t" + result.output);
        if (result.errors.contains("Warning:")) {
            getLog().info("\tCompile Warning:\n" + result.errors);
        } else {
            getLog().debug("\t\tError: \t" + result.errors);
        }
        return result.output;
    }

    private void processContractFile(Collection<String> files) throws MojoExecutionException {
        String result = parseSoliditySources(files);
        processResult(result, "\tNo Contract found in files '" + files + "'");
    }

    private void generatedAbi(Map<String, String> contractResult, String contractName) {
        if (!generateABI) {
            return;
        }
        String abiJson = contractResult.get(SolidityCompiler.Options.ABI.getName());

        try {
            Files.write(Paths.get(sourceDestination, packageName, contractName + ".json"), abiJson.getBytes());
        } catch (IOException e) {
            getLog().error("Could not build abi file for contract '" + contractName + "'", e);
        }
    }

    private void generatedBin(Map<String, String> contractResult, String contractName) {
        if (!generateBIN) {
            return;
        }

        String binJson = contractResult.get(SolidityCompiler.Options.BIN.getName());
        try {
            Files.write(Paths.get(sourceDestination, packageName, contractName + ".bin"), binJson.getBytes());
        } catch (IOException e) {
            getLog().error("Could not build bin file for contract '" + contractName + "'", e);
        }
    }

    private void generatedJavaClass(Map<String, String> results, String contractName) throws IOException, ClassNotFoundException {
        new SolidityFunctionWrapper(nativeJavaType).generateJavaFiles(
                contractName,
                results.get(SolidityCompiler.Options.BIN.getName()),
                results.get(SolidityCompiler.Options.ABI.getName()),
                sourceDestination,
                packageName);
    }

    private void processResult(String result, String warnMsg) throws MojoExecutionException {
        Map<String, Map<String, String>> contracts = extractContracts(result);
        if (contracts == null) {
            getLog().warn(warnMsg);
            return;
        }
        for (String contractName : contracts.keySet()) {
            if (isFiltered(contractName)) {
                getLog().debug("\tContract '" + contractName + "' is filtered");
                continue;
            }
            try {
                Map<String, String> contractResult = contracts.get(contractName);
                generatedJavaClass(contractResult, contractName);
                generatedAbi(contractResult, contractName);
                generatedBin(contractResult, contractName);
                getLog().info("\tBuilt Class for contract '" + contractName + "'");
            } catch (ClassNotFoundException | IOException ioException) {
                getLog().error("Could not build java class for contract '" + contractName + "'", ioException);
            }
        }
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
