package org.web3j.mavenplugin.solidity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SolCTest {

    static String osProperty;

    @Before
    public void storeSystemProperty() {
        osProperty = System.getProperty("os.name");
    }

    @After
    public void resetSystemProperty() {
        System.setProperty("os.name", osProperty);
    }

    @Test
    public void initExecutable() {
        SolC solC = new SolC();

        assertTrue(solC.getWorkingDirectory().exists());
        assertFalse(solC.getCanonicalWorkingDirectory().isEmpty());
        assertTrue(solC.getCanonicalPath().contains(solC.getCanonicalWorkingDirectory()));
    }


    @Test
    public void loadExecutableOnWindows() {
        System.setProperty("os.name", "Windows 10");

        SolC solC = new SolC();

        assertThat(solC.getCanonicalPath(), endsWith(".exe"));
    }


    @Test
    public void loadExecutableOnMac() {
        System.setProperty("os.name", "mac 10.3");

        SolC solC = new SolC();

        assertThat(solC.getCanonicalPath(), endsWith("solc"));
    }


    @Test
    public void loadExecutableOnLinux() {
        System.setProperty("os.name", "af linux asdf");

        SolC solC = new SolC();

        assertThat(solC.getCanonicalPath(), endsWith("solc"));
    }


    @Test
    public void takeExecutableFromSystemPath() throws IOException, InterruptedException {

        try {
            Process p = Runtime.getRuntime().exec("solc --version");

            String output = "";
            try (java.util.Scanner s = new java.util.Scanner(p.getInputStream())) {
                output = s.useDelimiter("\\A").hasNext() ? s.next() : "";
            }


            if (p.waitFor() == 0) {
                System.out.println("found and works");
            }
            System.out.println(output);
        } catch (IOException io) {
            System.out.println("solc is NOT in systmpath");
        }
    }

}