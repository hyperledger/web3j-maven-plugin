package org.web3j.mavenplugin;

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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class JavaClassGeneratorFilterITest {

    @Rule
    public MojoRule mojoRule = new MojoRule();

    @Rule
    public TestResources resources = new TestResources();

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void filteredContractExclude() throws Exception {
        File pom = new File(resources.getBasedir("filtered"), "exclude.pom.xml");
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
        assertThat(files.get(0).getFileName().toString(), is("Mortal.java"));
    }

    @Test
    public void filteredContractInclude() throws Exception {
        File pom = new File(resources.getBasedir("filtered"), "include.pom.xml");
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
        assertThat(files.get(0).getFileName().toString(), is("Greeter.java"));
    }

    @Test
    public void filteredContractMixed() throws Exception {
        File pom = new File(resources.getBasedir("filtered"), "mixed.pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupMojo("generate-sources", pom);
        assertNotNull(mojo);

        mojo.sourceDestination = testFolder.getRoot().getPath();
        mojo.execute();

        Path path = Paths.get(mojo.sourceDestination);

        List<Path> files = Files.find(path, 99, (p, bfa) -> bfa.isRegularFile())
                .filter(file -> file.toString().endsWith(".java"))
                .collect(Collectors.toList());
        assertThat(files.get(0).getFileName().toString(), is("Greeter.java"));
    }


}