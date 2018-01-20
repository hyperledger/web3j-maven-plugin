package org.web3j.mavenplugin;

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.web3j.mavenplugin.solidity.CompilerResult;
import org.web3j.mavenplugin.solidity.SolidityCompiler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
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
    @Ignore("works with solc version > 0.4.18")
    public void issue9_library() throws Exception {
        File pom = new File(resources.getBasedir("issue"), "issue9.pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupMojo("generate-sources", pom);
        assertNotNull(mojo);

        mojo.sourceDestination = testFolder.getRoot().getPath();
        mojo.execute();

        Path path = Paths.get(mojo.sourceDestination);

        List<Path> files = Files.find(path, 99, (p, bfa) -> bfa.isRegularFile()).collect(Collectors.toList());
        assertThat("ConvertLib is created", files.size(), is(1));
        assertThat(files.get(0).getFileName().toString(), is("ConvertLib.java"));
    }

    @Test
    public void issue13_bigInteger() throws Exception {
        File pom = new File(resources.getBasedir("issue"), "issue13.pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupMojo("generate-sources", pom);
        assertNotNull(mojo);

        mojo.sourceDestination = testFolder.getRoot().getPath();
        mojo.execute();

        Path path = Paths.get(mojo.sourceDestination);

        List<Path> files = Files.find(path, 99, (p, bfa) -> bfa.isRegularFile()).collect(Collectors.toList());
        assertThat("Predictor is created", files.size(), is(1));
        assertThat(files.get(0).getFileName().toString(), is("Predictor.java"));
    }

}