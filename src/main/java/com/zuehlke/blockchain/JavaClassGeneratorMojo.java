package com.zuehlke.blockchain;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.web3j.codegen.SolidityFunctionWrapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

@Mojo(name = "generate-sources", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class JavaClassGeneratorMojo extends AbstractMojo {

    @Parameter(property = "packageName", defaultValue = "com.zuehlke.blockchain.model")
    private String packageName;

    @Parameter(property = "sourceDestination", defaultValue = "src/main/java")
    private String sourceDestination;

    @Parameter(property = "soliditySourceFiles", required = true)
    private FileSet soliditySourceFiles;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    public JavaClassGeneratorMojo() {
    }

    JavaClassGeneratorMojo(String packageName, String sourceDestination, FileSet soliditySourceFiles, MavenProject project) {
        this.packageName = packageName;
        this.sourceDestination = sourceDestination;
        this.soliditySourceFiles = soliditySourceFiles;
        this.project = project;
    }

    public void execute() throws MojoExecutionException {
        if (soliditySourceFiles.getDirectory() == null) {
            getLog().info("No solidity directory specified, using project base directory.");
            soliditySourceFiles.setDirectory(project.getBasedir().getAbsolutePath());
        }
        if (soliditySourceFiles.getIncludes().size() == 0) {
            getLog().info("No solidity includes specified, using the default (**/*.sol)");
            soliditySourceFiles.setIncludes(Collections.singletonList("**/*.sol"));
        }

        for (String includedFile : new FileSetManager().getIncludedFiles(soliditySourceFiles)) {
            getLog().debug("process '" + includedFile + "'");
            processContractFile(includedFile);
            getLog().debug("processed '" + includedFile + "'");
        }
    }

    private void processContractFile(String includedFile) throws MojoExecutionException {
        CompilationResult parse;
        try {
            getLog().debug("Compile '" + soliditySourceFiles.getDirectory() + File.separator + includedFile + "'");
            parse = parseSoliditySource(includedFile);
            getLog().debug("Compiled '" + includedFile + "'");

        } catch (IOException ioException) {
            throw new MojoExecutionException("Could not compile files", ioException);
        }

        Map<String, CompilationResult.ContractMetadata> parsedContracts = parse.contracts;
        for (String contractName : parsedContracts.keySet()) {

            try {
                getLog().debug("Build contract '" + contractName + "'");
                generatedJavaClass(parse, contractName);
                getLog().debug("java class for contract '" + contractName + "' generated");
            } catch (ClassNotFoundException | IOException ioException) {
                getLog().error("Could not build java class for contract '" + contractName + "'", ioException);
            }
        }
    }

    private void generatedJavaClass(CompilationResult parse, String contractName) throws IOException, ClassNotFoundException {
        new SolidityFunctionWrapper().generateJavaFiles(
                contractName,
                parse.contracts.get(contractName).bin,
                parse.contracts.get(contractName).abi,
                sourceDestination,
                packageName);
    }

    private CompilationResult parseSoliditySource(String includedFile) throws IOException {
        Path path = Paths.get(soliditySourceFiles.getDirectory(), includedFile);
        byte[] contract = Files.readAllBytes(path);
        SolidityCompiler.Result result = SolidityCompiler.getInstance().compileSrc(
                contract,
                true,
                true,
                SolidityCompiler.Options.ABI,
                SolidityCompiler.Options.BIN,
                SolidityCompiler.Options.METADATA);
        return CompilationResult.parse(result.output);
    }
}
