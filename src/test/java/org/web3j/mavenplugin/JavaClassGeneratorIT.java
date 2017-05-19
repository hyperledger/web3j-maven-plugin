package org.web3j.mavenplugin;

import org.apache.maven.plugin.testing.ConfigurationException;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JavaClassGeneratorIT {

    @Rule
    public MojoRule mojoRule = new MojoRule();

    @Rule
    public TestResources resources = new TestResources();

    private String sourceDestination;

    @Test
    public void validPom() throws Exception {
        File pom = new File(resources.getBasedir("valid"), "pom.xml");

        assertNotNull(pom);
        assertTrue(pom.exists());

        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupMojo("generate-sources", pom);
        assertNotNull(mojo);

        mojo.execute();

        this.sourceDestination = mojo.sourceDestination;
        Path path = Paths.get(mojo.sourceDestination);
        long generatedJavaFiles = Files.walk(path)
                .filter(Files::isRegularFile)
                .count();

        assertEquals("Greeter and Mortal Class", 2l, generatedJavaFiles);
    }


    @Test(expected = ConfigurationException.class)
    public void inValidPom() throws Exception {
        File pom = new File(resources.getBasedir("invalid"), "pom.xml");

        assertNotNull(pom);
        assertTrue(pom.exists());

        JavaClassGeneratorMojo mojo = (JavaClassGeneratorMojo) mojoRule.lookupMojo("generate-sources", pom);
        assertNotNull(mojo);

        mojo.execute();
        // soliditySourceFiles is missing

    }

    @After
    public void removeTestFiles() throws IOException {
        if (sourceDestination != null && !sourceDestination.isEmpty()) {
            File file = new File(sourceDestination);
            Files.walk(file.toPath())
                    .map(Path::toFile)
                    .sorted((o1, o2) -> -o1.compareTo(o2)) //reversed order
                    .forEach(File::delete);
        }
    }

}