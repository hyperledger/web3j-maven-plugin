package org.web3j.mavenplugin.solidity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.web3j.sokt.SolcRelease;
import org.web3j.sokt.SolidityFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class SolcVersionSelectorTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private final SolcVersionSelector selector = new SolcVersionSelector();

    @Test
    public void selectsLatestReleaseCompatibleWithAllSourcePragmas() throws Exception {
        SolidityFile legacy = solidityFile(
                "Legacy.sol",
                "pragma solidity >=0.4.19 <0.7.0;\ncontract Legacy {}\n");
        SolidityFile midRange = solidityFile(
                "MidRange.sol",
                "pragma solidity >=0.5.0 <0.6.0;\ncontract MidRange {}\n");

        SolcRelease selectedRelease = selector.selectLatestCompatibleRelease(
                Arrays.asList(legacy, midRange), releases());

        assertEquals("0.5.17", selectedRelease.getVersion());
    }

    @Test
    public void selectionIsIndependentOfSourceIterationOrder() throws Exception {
        SolidityFile legacy = solidityFile(
                "Legacy.sol",
                "pragma solidity >=0.4.19 <0.7.0;\ncontract Legacy {}\n");
        SolidityFile midRange = solidityFile(
                "MidRange.sol",
                "pragma solidity >=0.5.0 <0.6.0;\ncontract MidRange {}\n");

        SolcRelease forwardOrder = selector.selectLatestCompatibleRelease(
                Arrays.asList(legacy, midRange), releases());
        SolcRelease reverseOrder = selector.selectLatestCompatibleRelease(
                Arrays.asList(midRange, legacy), releases());

        assertEquals("0.5.17", forwardOrder.getVersion());
        assertEquals("0.5.17", reverseOrder.getVersion());
    }

    @Test
    public void throwsClearErrorWhenSourcePragmasHaveNoCompatibleRelease() throws Exception {
        SolidityFile legacy = solidityFile(
                "Legacy.sol",
                "pragma solidity <0.5.0;\ncontract Legacy {}\n");
        SolidityFile modern = solidityFile(
                "Modern.sol",
                "pragma solidity >=0.8.0;\ncontract Modern {}\n");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> selector.selectLatestCompatibleRelease(Arrays.asList(legacy, modern), releases()));

        assertTrue(exception.getMessage().contains("No compatible solc release could be found for the files"));
        assertTrue(exception.getMessage().contains("Legacy.sol"));
        assertTrue(exception.getMessage().contains("Modern.sol"));
        assertTrue(exception.getMessage().contains("<0.5.0"));
        assertTrue(exception.getMessage().contains(">=0.8.0"));
    }

    @Test
    public void usesSingularErrorMessageForSingleSourceWithoutCompatibleRelease() throws Exception {
        SolidityFile tooNew = solidityFile(
                "TooNew.sol",
                "pragma solidity >=1.0.0;\ncontract TooNew {}\n");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> selector.selectLatestCompatibleRelease(Collections.singletonList(tooNew), releases()));

        assertTrue(exception.getMessage().contains("No compatible solc release could be found for the file"));
        assertTrue(exception.getMessage().contains("TooNew.sol"));
        assertTrue(exception.getMessage().contains(">=1.0.0"));
    }

    @Test
    public void sourceWithoutPragmaDoesNotNarrowCompatibleReleaseSelection() throws Exception {
        SolidityFile unconstrained = solidityFile(
                "Unconstrained.sol",
                "contract Unconstrained {}\n");
        SolidityFile constrained = solidityFile(
                "Constrained.sol",
                "pragma solidity >=0.4.19 <0.6.0;\ncontract Constrained {}\n");

        SolcRelease selectedRelease = selector.selectLatestCompatibleRelease(
                Arrays.asList(unconstrained, constrained), releases());

        assertEquals("0.5.17", selectedRelease.getVersion());
    }

    @Test
    public void selectsLatestOsCompatibleReleaseWhenNoSourceDeclaresPragma() throws Exception {
        SolidityFile first = solidityFile("First.sol", "contract First {}\n");
        SolidityFile second = solidityFile("Second.sol", "contract Second {}\n");

        SolcRelease selectedRelease = selector.selectLatestCompatibleRelease(
                Arrays.asList(first, second), releases());

        assertEquals("0.8.30", selectedRelease.getVersion());
    }

    @Test
    public void rejectsEmptySourceCollection() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> selector.selectLatestCompatibleRelease(Collections.emptyList(), releases()));

        assertTrue(exception.getMessage().contains("No Solidity source files provided"));
    }

    private SolidityFile solidityFile(String fileName, String content) throws IOException {
        Path source = testFolder.newFile(fileName).toPath();
        Files.writeString(source, content, StandardCharsets.UTF_8);
        return new SolidityFile(source.toString());
    }

    private List<SolcRelease> releases() {
        return Arrays.asList(
                release("0.4.26"),
                release("0.5.17"),
                release("0.6.12"),
                release("0.8.30"));
    }

    private SolcRelease release(String version) {
        return new SolcRelease(version, "windows-url", "linux-url", "mac-url");
    }
}
