package org.web3j.mavenplugin;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.web3j.mavenplugin.solidity.CompilerResult;
import org.web3j.mavenplugin.solidity.SolidityCompiler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class IssueITest {

    @Rule
    public MojoRule mojoRule = new MojoRule();

    @Rule
    public TestResources resources = new TestResources();

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void issue13_bigInteger() throws Exception {
        File pom = new File(resources.getBasedir("issue"), "issue13.pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupMojo("generate-sources", pom);
        assertNotNull(mojo);

        mojo.sourceDestination = testFolder.getRoot().getPath();
        mojo.outputFormat = "java";
        mojo.execute();

        Path path = Paths.get(mojo.sourceDestination);

        List<Path> files = Files
                .find(path, 99, (p, bfa) -> bfa.isRegularFile())
                .filter(file -> file.toString().endsWith("java"))
                .collect(Collectors.toList());
        assertThat("Predictor is created", files.size(), is(1));
        assertThat(files.get(0).getFileName().toString(), is("Predictor.java"));
    }

    @Test
    public void issue17() throws Exception {
        File pom = new File(resources.getBasedir("issue/17"), "pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupConfiguredMojo(resources.getBasedir("issue/17"), "generate-sources");
        assertNotNull(mojo);

        mojo.sourceDestination = testFolder.getRoot().getPath();
        mojo.execute();

        Path path = Paths.get(mojo.sourceDestination);

        List<String> files = Files
                .find(path, 99, (p, bfa) -> bfa.isRegularFile())
                .filter(file -> file.toString().endsWith("java"))
                .map(p -> p.toFile().getName())
                .collect(Collectors.toList());
        assertThat("All java class files are generated", files.size(), is(5));
        assertTrue(files.contains("Issue17import1.java"));
        assertTrue(files.contains("Issue17import2.java"));
        assertTrue(files.contains("Issue17relative1.java"));
        assertTrue(files.contains("Issue17relative2.java"));
        assertTrue(files.contains("Issue17main.java"));
    }

    @Test
    public void issue09() {
        SolidityCompiler solidityCompiler = SolidityCompiler.getInstance(new SystemStreamLog());
        Set<String> sources = Collections.singleton("issue-09.sol");

        CompilerResult compilerResult = solidityCompiler.compileSrc("src/test/resources/", sources, new String[0], SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);

        assertFalse(compilerResult.isFailed());
    }

    @Test
    public void issue17_1() throws Exception {
        File pom = new File(resources.getBasedir("issue/17.1"), "pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupConfiguredMojo(resources.getBasedir("issue/17.1"), "generate-sources");
        assertNotNull(mojo);

        mojo.sourceDestination = testFolder.getRoot().getPath();
        mojo.execute();

        Path path = Paths.get(mojo.sourceDestination);

        List<String> files = Files
                .find(path, 99, (p, bfa) -> bfa.isRegularFile())
                .filter(file -> file.toString().endsWith("java"))
                .map(p -> p.toFile().getName())
                .collect(Collectors.toList());
        assertThat("All java class files are generated", files.size(), is(5));
        assertTrue(files.contains("Issue17import1.java"));
        assertTrue(files.contains("Issue17import2.java"));
        assertTrue(files.contains("Issue17relative1.java"));
        assertTrue(files.contains("Issue17relative2.java"));
        assertTrue(files.contains("Issue17main.java"));
    }

    @Test
    public void issue23() throws Exception {
        File pom = new File(resources.getBasedir("issue/23"), "pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());
        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupConfiguredMojo(resources.getBasedir("issue/23"), "generate-sources");
        assertNotNull(mojo);
        mojo.sourceDestination = testFolder.getRoot().getPath();
        mojo.execute();

        Path path = Paths.get(mojo.sourceDestination);

        List<String> files = Files
                .find(path, 99, (p, bfa) -> bfa.isRegularFile())
                .filter(file -> file.toString().endsWith("java"))
                .map(p -> p.toFile().getName()).collect(Collectors.toList());
        assertThat("Interface and java classes are generated", files.size(), is(3));
        assertTrue(files.contains("CheckImpl.java"));
    }

    @Test
    public void issue63() throws Exception {
        File pom = new File(resources.getBasedir("issue/63"), "pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());
        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupConfiguredMojo(resources.getBasedir("issue/63"), "generate-sources");
        assertNotNull(mojo);
        mojo.sourceDestination = testFolder.getRoot().getPath();
        mojo.execute();

        Path path = Paths.get(mojo.sourceDestination);

        List<File> files = Files
                .find(path, 99, (p, bfa) -> bfa.isRegularFile())
                .filter(file -> file.toString().endsWith("java"))
                .map(Path::toFile).collect(Collectors.toList());
        assertThat("Interface and java classes are generated", files.size(), is(1));
        File file = files.get(0);
        assertThat(file.getName(), is("Greeter.java"));
        String content = FileUtils.readFileToString(file);
        assertThat(content, containsString("greet()"));
        assertThat(content, containsString("kill()"));
    }

    @Test
    public void issue63CustomParentContract() throws Exception {
        File pom = new File(resources.getBasedir("issue/63"), "pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());
        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupConfiguredMojo(resources.getBasedir("issue/63"), "generate-sources");
        assertNotNull(mojo);
        mojo.sourceDestination = testFolder.getRoot().getPath();
        mojo.outputJavaParentContractClassName = "org.web3j.mavenplugin.SampleCustomContract";
        mojo.execute();

        Path path = Paths.get(mojo.sourceDestination);

        List<File> files = Files
                .find(path, 99, (p, bfa) -> bfa.isRegularFile())
                .filter(file -> file.toString().endsWith("java"))
                .map(Path::toFile).collect(Collectors.toList());
        assertThat("Interface and java classes are generated", files.size(), is(1));
        File file = files.get(0);
        assertThat(file.getName(), is("Greeter.java"));
        String content = FileUtils.readFileToString(file);
        assertThat(content, containsString("extends SampleCustomContract"));
        assertThat(content, containsString("greet()"));
        assertThat(content, containsString("kill()"));
    }

    @Test
    public void issue63Error() throws Exception {
        File pom = new File(resources.getBasedir("issue/63"), "pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());
        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupConfiguredMojo(resources.getBasedir("issue/63"), "generate-sources");
        assertNotNull(mojo);
        mojo.sourceDestination = testFolder.getRoot().getPath();
        mojo.abiSourceFiles.getIncludes().clear();
        mojo.abiSourceFiles.getIncludes().add("**/*.nonexistent");
        mojo.execute();

        Path path = Paths.get(mojo.sourceDestination);

        List<File> files = Files
                .find(path, 99, (p, bfa) -> bfa.isRegularFile())
                .filter(file -> file.toString().endsWith("java"))
                .map(Path::toFile).collect(Collectors.toList());
        assertThat("Interface and java classes are generated", files.size(), is(0));
    }

    @Test
    public void issue9_library() throws Exception {
        File pom = new File(resources.getBasedir("issue"), "issue9.pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupMojo("generate-sources", pom);
        assertNotNull(mojo);

        mojo.sourceDestination = testFolder.getRoot().getPath();
        mojo.outputFormat = "java";
        mojo.execute();

        Path path = Paths.get(mojo.sourceDestination);

        List<Path> files = Files
                .find(path, 99, (p, bfa) -> bfa.isRegularFile())
                .filter(file -> file.toString().endsWith("java"))
                .collect(Collectors.toList());
        assertThat("ConvertLib is created", files.size(), is(1));
        assertThat(files.get(0).getFileName().toString(), is("ConvertLib.java"));
    }

    @Test
    public void issue83_solidityVersion() throws Exception {
        File pom = new File(resources.getBasedir("issue/83"), "pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupMojo("generate-sources", pom);
        assertNotNull(mojo);

        mojo.sourceDestination = testFolder.getRoot().getPath();
        mojo.outputFormat = "java";
        mojo.execute();

        Path path = Paths.get(mojo.sourceDestination);

        List<Path> files = Files.find(path, 99, (p, bfa) -> bfa.isRegularFile())
                .filter(file -> file.toString().endsWith(".java"))
                .collect(Collectors.toList());
        assertEquals("EtherWallet.java", files.get(0).getFileName().toString());
    }

    @Test
    public void issue83_solidityVersion_withABI() throws Exception {
        File pom = new File(resources.getBasedir("issue/83"), "abi.pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupMojo("generate-sources", pom);
        assertNotNull(mojo);

        mojo.sourceDestination = testFolder.getRoot().getPath();
        mojo.outputFormat = "java";
        mojo.execute();

        Path path = Paths.get(mojo.sourceDestination);

        List<Path> files = Files.find(path, 99, (p, bfa) -> bfa.isRegularFile())
                .filter(file -> file.toString().endsWith(".java"))
                .collect(Collectors.toList());
        assertEquals("EtherWallet.java", files.get(0).getFileName().toString());
    }

    @Test
    public void issue106_ABI_and_BIN_destination() throws Exception {
        File pom = new File(resources.getBasedir("issue/106"), "pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupMojo("generate-sources", pom);
        assertNotNull(mojo);

        mojo.sourceDestination = testFolder.getRoot().getPath();
        mojo.outputDirectory.setAbi(testFolder.getRoot().getPath() + "/abi");
        mojo.outputDirectory.setBin(testFolder.getRoot().getPath() + "/bin");
        mojo.execute();

        assertTrue(Paths.get(testFolder.getRoot().getPath(), "abi", "com", "sample", "generated", "Sample.json").toFile().exists());
        assertTrue(Paths.get(testFolder.getRoot().getPath(), "bin", "com", "sample", "generated", "Sample.bin").toFile().exists());
    }
}