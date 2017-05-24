package org.web3j.mavenplugin.solidity;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SolCTest {

    @Test
    public void loadExecutable(){
        SolC solC = new SolC();

        assertTrue(solC.getWorkingDirectory().exists());
        assertFalse(solC.getCanonicalPath().isEmpty());
        assertFalse(solC.getCanonicalWorkingDirectory().isEmpty());
        assertTrue(solC.getCanonicalPath().contains(solC.getCanonicalWorkingDirectory()));
    }

}