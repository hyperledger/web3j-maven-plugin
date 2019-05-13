package org.web3j.mavenplugin;

import org.apache.maven.plugin.testing.ConfigurationException;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JavaClassGeneratorITest {

    @Rule
    public MojoRule mojoRule = new MojoRule();

    @Rule
    public TestResources resources = new TestResources();

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void pomStandard() throws Exception {
        File pom = new File(resources.getBasedir("valid"), "pom.xml");
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
        assertEquals("Greeter and Mortal Class", 2l, files.size());
    }

    @Test
    public void pomWithEmptyContract() throws Exception {
        File pom = new File(resources.getBasedir("valid"), "empty.pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupMojo("generate-sources", pom);
        assertNotNull(mojo);

        mojo.sourceDestination = testFolder.getRoot().getPath();
        mojo.execute();

        Path path = Paths.get(mojo.sourceDestination);

        List<Path> files = Files
                .find(path, 99, (p, bfa) -> bfa.isRegularFile())
                .filter(file -> file.toString().endsWith("java"))
                .collect(Collectors.toList());
        assertEquals("no files in default value", 0l, files.size());
    }

    @Test
    public void pomWithImport() throws Exception {
        File pom = new File(resources.getBasedir("import"), "pom.xml");
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
        assertEquals("Main, Upper and Util Class", 3, files.size());
    }


    @Test
    public void pomWithImportWithPath() throws Exception {
        File pom = new File(resources.getBasedir("import-with-path"), "pom.xml");
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
        assertEquals("Main and Dependency Class", 2, files.size());
    }



    @Test
    public void pomWithImportWithPathWithSpaces() throws Exception {
        File pom = new File(resources.getBasedir("import-with-path-with-spaces"), "pom.xml");
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
        assertEquals("Main and Dependency Class", 2, files.size());
    }

    @Test
    public void pomWithoutConfiguration() throws Exception {
        File pom = new File(resources.getBasedir("valid"), "default.pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupMojo("generate-sources", pom);
        assertNotNull(mojo);

        mojo.sourceDestination = testFolder.getRoot().getPath();
        mojo.execute();

        Path path = Paths.get(mojo.sourceDestination);

        List<Path> files = Files
                .find(path, 99, (p, bfa) -> bfa.isRegularFile())
                .filter(file -> file.toString().endsWith("java"))
                .collect(Collectors.toList());
        assertEquals("no files in default value", 0l, files.size());
    }

    @Test(expected = ConfigurationException.class)
    public void invalidPom() throws Exception {
        File pom = new File(resources.getBasedir("invalid"), "pom.xml");

        assertNotNull(pom);
        assertTrue(pom.exists());

        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupMojo("generate-sources", pom);
        assertNotNull(mojo);

        mojo.execute();
        // soliditySourceFiles is missing
    }

    @Test
    public void pomWithABI() throws Exception {
        File pom = new File(resources.getBasedir("valid"), "abi.pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupMojo("generate-sources", pom);
        assertNotNull(mojo);

        mojo.sourceDestination = testFolder.getRoot().getPath();
        mojo.execute();

        Path path = Paths.get(mojo.sourceDestination);

        List<Path> json = Files
                .find(path, 99, (p, bfa) -> bfa.isRegularFile())
                .filter(file -> file.toString().endsWith("json"))
                .collect(Collectors.toList());
        assertEquals("no files in default value", 2, json.size());

        List<Path> bin = Files
                .find(path, 99, (p, bfa) -> bfa.isRegularFile())
                .filter(file -> file.toString().endsWith("bin"))
                .collect(Collectors.toList());
        assertEquals("no files in default value", 0, bin.size());
    }

    @Test
    public void pomWithAbiBin() throws Exception {
        File pom = new File(resources.getBasedir("valid"), "abi.bin.pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupMojo("generate-sources", pom);
        assertNotNull(mojo);

        mojo.sourceDestination = testFolder.getRoot().getPath();
        mojo.execute();

        Path path = Paths.get(mojo.sourceDestination);

        List<Path> json = Files
                .find(path, 99, (p, bfa) -> bfa.isRegularFile())
                .filter(file -> file.toString().endsWith("json"))
                .collect(Collectors.toList());
        assertEquals("no files in default value", 2, json.size());

        List<Path> bin = Files
                .find(path, 99, (p, bfa) -> bfa.isRegularFile())
                .filter(file -> file.toString().endsWith("bin"))
                .collect(Collectors.toList());
        assertEquals("no files in default value", 2, bin.size());
    }


    @Test
    public void pomWithAbiBin_differentFolder() throws Exception {
        File pom = new File(resources.getBasedir("valid"), "different.destination.pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupMojo("generate-sources", pom);
        assertNotNull(mojo);

        String tempPath = testFolder.getRoot().getPath();
        mojo.outputDirectory.setJava(
                tempPath + File.separator + mojo.outputDirectory.getJava()
        );

        mojo.outputDirectory.setAbi(
                tempPath + File.separator + mojo.outputDirectory.getAbi()
        );

        mojo.outputDirectory.setBin(
                tempPath + File.separator + mojo.outputDirectory.getBin()
        );

        mojo.execute();

        Path path = Paths.get(tempPath);

        List<Path> json = Files
                .find(path, 99, (p, bfa) -> bfa.isRegularFile())
                .filter(file -> file.toString().endsWith("json"))
                .collect(Collectors.toList());
        assertEquals("no files in default value", 2, json.size());

        List<Path> bin = Files
                .find(path, 99, (p, bfa) -> bfa.isRegularFile())
                .filter(file -> file.toString().endsWith("bin"))
                .collect(Collectors.toList());
        assertEquals("no files in default value", 2, bin.size());

        List<Path> java = Files
                .find(path, 99, (p, bfa) -> bfa.isRegularFile())
                .filter(file -> file.toString().endsWith("java"))
                .collect(Collectors.toList());
        assertEquals("no files in default value", 2, java.size());

    }
}