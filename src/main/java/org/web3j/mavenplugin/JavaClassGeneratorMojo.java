package org.web3j.mavenplugin;


import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.web3j.abi.datatypes.Address;
import org.web3j.codegen.SolidityFunctionWrapper;
import org.web3j.mavenplugin.solidity.CompilerResult;
import org.web3j.mavenplugin.solidity.SolidityCompiler;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.response.AbiDefinition;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Maven Plugin to generate the java classes out of the solidity contract files.
 */
@Mojo(name = "generate-sources",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class JavaClassGeneratorMojo extends AbstractMojo {

    private static final String DEFAULT_SOLIDITY_INCLUDE = "**/*.sol";
    private static final String DEFAULT_ABI_INCLUDE = "**/*.json";
    private static final String DEFAULT_PACKAGE = "org.web3j.model";
    private static final String DEFAULT_SOURCE_DESTINATION = "src/main/java";
    private static final String DEFAULT_SOLIDITY_SOURCES = "src/main/resources";
    private static final String DEFAULT_ABI_SOURCES = "src/main/resources";
    private static final String DEFAULT_OUTPUT_FORMAT = "java";

    @Parameter(property = "packageName", defaultValue = DEFAULT_PACKAGE)
    protected String packageName;

    @Parameter(property = "sourceDestination", defaultValue = DEFAULT_SOURCE_DESTINATION)
    protected String sourceDestination;

    @Parameter(property = "outputDirectory")
    protected SourceDestination outputDirectory = new SourceDestination();

    @Parameter(property = "soliditySourceFiles")
    protected FileSet soliditySourceFiles = new FileSet();

    @Parameter(property = "abiSourceFiles")
    protected FileSet abiSourceFiles = new FileSet();

    @Parameter(property = "contract")
    protected Contract contract;

    @Parameter(property = "nativeJavaType", defaultValue = "true")
    protected boolean nativeJavaType;

    @Parameter(property = "pathPrefixes")
    protected String[] pathPrefixes = new String[0];

    @Parameter(property = "outputFormat", defaultValue = DEFAULT_OUTPUT_FORMAT)
    protected String outputFormat;

    @Parameter(property = "outputJavaParentContractClassName")
    protected String outputJavaParentContractClassName;

    private Path createPath(String destinationPath) throws IOException {
        Path path = Paths.get(destinationPath, packageName);

        if (!path.toFile().exists()) {
            Files.createDirectories(path);
        }
        return path;
    }

    private Map<String, Map<String, String>> extractContracts(String result) throws MojoExecutionException {
        JsonParser jsonParser = new JsonParser();
        Map<String, Object> json = jsonParser.parseJson(result);
        Map<String, Map<String, String>> contracts = (Map<String, Map<String, String>>) json.get("contracts");
        if (contracts == null) {
            getLog().warn("no contracts found");
            return null;
        }
        Map<String, String> contractRemap = new HashMap<>();

        HashSet<String> contractsKeys = new HashSet<>(contracts.keySet());
        for (String contractFilename : contractsKeys) {
            Map<String, String> contractMetadata = contracts.get(contractFilename);
            String metadata = contractMetadata.get("metadata");
            if (metadata == null || metadata.length() == 0) {
                contracts.remove(contractFilename);
                continue;
            }
            getLog().debug("metadata:" + metadata);
            Map<String, Object> metadataJson = jsonParser.parseJson(metadata);
            Object settingsMap = metadataJson.get("settings");
            // FIXME this generates java files for interfaces with >org.ethereum:solcJ-all:0.5.2 , because the compiler generates now metadata.
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
    }

    private void generatedJavaClass(Map<String, String> results, String contractName) throws IOException, ClassNotFoundException {
        if (!StringUtils.containsIgnoreCase(outputFormat, "java")) {
            return;
        }
        List<AbiDefinition> functionDefinitions = loadContractDefinition(results.get(SolidityCompiler.Options.ABI.getName()));
        generatedJavaClass(contractName, functionDefinitions, results.get(SolidityCompiler.Options.BIN.getName()));
    }

    private void generatedJavaClass(String contractName, List<AbiDefinition> functionDefinitions, String bin) throws IOException, ClassNotFoundException {
        int addressLength = Address.DEFAULT_LENGTH / Byte.SIZE;
        boolean primitiveTypes = false;

        if (functionDefinitions.isEmpty()) {
            getLog().warn("Unable to parse input ABI file");
            return;
        }

        Class<? extends org.web3j.tx.Contract> contractClass = outputJavaParentContractClassName == null
                ? org.web3j.tx.Contract.class
                : (Class<? extends org.web3j.tx.Contract>) Class.forName(outputJavaParentContractClassName);
        new SolidityFunctionWrapper(
                nativeJavaType,
                primitiveTypes,
                false, //generateSendTxForCalls
                addressLength)
                .generateJavaFiles(
                        contractClass,
                        contractName,
                        bin,
                        functionDefinitions,
                        StringUtils.defaultString(outputDirectory.getJava(), sourceDestination),
                        packageName,
                        null

                );
    }

    private void processSolidityFile(Collection<String> files) throws MojoExecutionException {
        String result = parseSoliditySources(files);
        processResult(result, "\tNo Solidity сontract found in files '" + files + "'");
    }

    public void processAbiFile(Collection<String> fileNames) throws IOException, ClassNotFoundException {
        for (String fileName : fileNames) {
            File file = Paths.get(abiSourceFiles.getDirectory(), fileName).toFile();
            generatedJavaClass(FilenameUtils.removeExtension(file.getName()), FileUtils.readFileToString(file));
        }
    }

    private void generatedJavaClass(String contractName, String fileContent) throws IOException, ClassNotFoundException {
        generatedJavaClass(contractName, loadContractDefinition(fileContent), "Bin file was not provided");
    }

    public void execute() throws MojoExecutionException {
        processSolidityFiles();
        processAbiFiles();
    }

    private void processSolidityFiles() throws MojoExecutionException {
        if (soliditySourceFiles.getDirectory() == null) {
            getLog().info("No Solidity directory specified, using default directory [" + DEFAULT_SOLIDITY_SOURCES + "]");
            soliditySourceFiles.setDirectory(DEFAULT_SOLIDITY_SOURCES);
        }
        if (soliditySourceFiles.getIncludes().isEmpty()) {
            getLog().info("No Solidity contracts specified, using the default [" + DEFAULT_SOLIDITY_INCLUDE + "]");
            soliditySourceFiles.setIncludes(Collections.singletonList(DEFAULT_SOLIDITY_INCLUDE));
        }

        String[] files = new FileSetManager().getIncludedFiles(soliditySourceFiles);
        if (files != null) {
            processSolidityFile(Stream.of(files)
                    .filter(f -> {
                        getLog().info("Solidity: adding to process '" + f + "'");
                        return true;
                    })
                    .collect(Collectors.toList()));
        }
    }

    private void processAbiFiles() throws MojoExecutionException {
        if (abiSourceFiles.getDirectory() == null) {
            getLog().info("No abiSourceFiles directory specified, using default directory [" + DEFAULT_ABI_SOURCES + "]");
            abiSourceFiles.setDirectory(DEFAULT_ABI_SOURCES);
        }
        if (abiSourceFiles.getIncludes().isEmpty()) {
            getLog().info("No abiSourceFiles contracts specified, using the default [" + DEFAULT_ABI_INCLUDE + "]");
            abiSourceFiles.setIncludes(Collections.singletonList(DEFAULT_ABI_INCLUDE));
        }
        String[] abiFiles = new FileSetManager().getIncludedFiles(abiSourceFiles);
        if (abiFiles != null) {
            try {
                processAbiFile(Stream.of(abiFiles)
                        .peek(it -> getLog().info("Abi: adding to process '" + it + "'"))
                        .collect(Collectors.toList()));
            } catch (IOException | ClassNotFoundException e) {
                getLog().error(e);
                throw new MojoExecutionException("Error during abi source files processing", e);
            }
        }
    }

    private void generatedAbi(Map<String, String> contractResult, String contractName) {
        if (!StringUtils.containsIgnoreCase(outputFormat, "abi")) {
            return;
        }

        String abiJson = contractResult.get(SolidityCompiler.Options.ABI.getName());
        try {
            String filename = contractName + ".json";
            Path path = createPath(StringUtils.defaultString(outputDirectory.getAbi(), sourceDestination));
            Files.write(Paths.get(path.toString(), filename), abiJson.getBytes());
        } catch (IOException e) {
            getLog().error("Could not build abi file for contract '" + contractName + "'", e);
        }
    }

    private void generatedBin(Map<String, String> contractResult, String contractName) {
        if (!StringUtils.containsIgnoreCase(outputFormat, "bin")) {
            return;
        }

        String binJson = contractResult.get(SolidityCompiler.Options.BIN.getName());
        try {
            String filename = contractName + ".bin";
            Path path = createPath(StringUtils.defaultString(outputDirectory.getBin(), sourceDestination));

            Files.write(Paths.get(path.toString(), filename), binJson.getBytes());
        } catch (IOException e) {
            getLog().error("Could not build bin file for contract '" + contractName + "'", e);
        }
    }

    protected List<AbiDefinition> loadContractDefinition(String abiFile) throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        AbiDefinition[] abiDefinition = objectMapper.readValue(abiFile, AbiDefinition[].class);
        return Arrays.asList(abiDefinition);
    }

    private String parseSoliditySources(Collection<String> includedFiles) throws MojoExecutionException {
        if (includedFiles.isEmpty()) {
            return "{}";
        }
        CompilerResult result = SolidityCompiler.getInstance(getLog()).compileSrc(
                soliditySourceFiles.getDirectory(),
                includedFiles,
                pathPrefixes,
                SolidityCompiler.Options.ABI,
                SolidityCompiler.Options.BIN,
                SolidityCompiler.Options.INTERFACE,
                SolidityCompiler.Options.METADATA
        );
        if (result.isFailed()) {
            throw new MojoExecutionException("Could not compile Solidity files\n" + result.errors);
        }

        getLog().debug("\t\tResult:\t" + result.output);
        if (result.errors.contains("Warning:")) {
            getLog().info("\tCompile Warning:\n" + result.errors);
        } else {
            getLog().debug("\t\tError: \t" + result.errors);
        }
        return result.output;
    }

    private void processResult(String result, String warnMsg) throws MojoExecutionException {
        Map<String, Map<String, String>> contracts = extractContracts(result);
        if (contracts == null) {
            getLog().warn(warnMsg);
            return;
        }
        for (Map.Entry<String, Map<String, String>> entry : contracts.entrySet()) {
            String contractName = entry.getKey();
            if (isFiltered(contractName)) {
                getLog().debug("\tContract '" + contractName + "' is filtered");
                continue;
            }
            try {
                Map<String, String> contractResult = entry.getValue();
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
