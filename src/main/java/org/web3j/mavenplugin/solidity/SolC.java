package org.web3j.mavenplugin.solidity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

/**
 * Wrapper class to the native solc execution on different platforms.
 *
 * Inspired by https://github.com/ethereum/ethereumj/tree/develop/ethereumj-core/src/main/java/org/ethereum/solidity
 */
public class SolC {

    private File solc = null;

    private String canonicalPath;
    private String canonicalWorkingDirectory;
    private File workingDirectory;

    SolC() {
        try {
            initBundled();

            canonicalPath = solc.getCanonicalPath();
            canonicalWorkingDirectory = solc.getParentFile().getCanonicalPath();
            workingDirectory = solc.getParentFile();

        } catch (IOException e) {
            throw new RuntimeException("Can't init solc compiler: ", e);
        }
    }

    private void initBundled() throws IOException {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"), "solc");
        tmpDir.setReadable(true);
        tmpDir.setWritable(true);
        tmpDir.setExecutable(true);
        tmpDir.mkdirs();

        String solcPath = "/native/" + getOS() + "/solc/";
        InputStream is = getClass().getResourceAsStream(solcPath + "file.list");
        Scanner scanner = new Scanner(is);
        while (scanner.hasNext()) {
            String s = scanner.next();
            File targetFile = new File(tmpDir, s);
            InputStream fis = getClass().getResourceAsStream(solcPath + s);
            Files.copy(fis, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            if (solc == null) {
                // first file in the list denotes executable
                solc = targetFile;
                solc.setExecutable(true);
            }
            targetFile.deleteOnExit();
        }
        tmpDir.deleteOnExit();
    }


    private String getOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return "win";
        } else if (osName.contains("linux")) {
            return "linux";
        } else if (osName.contains("mac")) {
            return "mac";
        } else {
            throw new RuntimeException("Can't find solc compiler: unrecognized OS: " + osName);
        }
    }

    public String getCanonicalPath() {
        return canonicalPath;
    }

    public String getCanonicalWorkingDirectory() {
        return canonicalWorkingDirectory;
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }


}
