package org.web3j.mavenplugin.solidity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

}