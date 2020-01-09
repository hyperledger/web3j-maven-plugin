package org.web3j.mavenplugin.solidity;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.regex.Matcher;

/**
 * Wrapper class to the native solc execution on different platforms.
 * <p>
 * Inspired by https://github.com/ethereum/ethereumj/tree/develop/ethereumj-core/src/main/java/org/ethereum/solidity
 */
public class SolC {

    private File solc;

    private String canonicalPath;
    private String canonicalWorkingDirectory;
    private File workingDirectory;
    private String version;

    SolC() {
        try {
            solc = initBundled();

            canonicalPath = solc.getCanonicalPath();
            canonicalWorkingDirectory = solc.getParentFile().getCanonicalPath();
            workingDirectory = solc.getParentFile();

        } catch (IOException e) {
            throw new RuntimeException("Can't init solc compiler: ", e);
        }
    }

    private String evaluateSolCVersion() {
        try {
            Process p = Runtime.getRuntime().exec(getCanonicalPath() + " --version");

            try (java.util.Scanner s = new java.util.Scanner(p.getInputStream())) {
                String output = s.useDelimiter("\\A").hasNext() ? s.next() : "";

                if (p.waitFor() == 0) {
                    Matcher matcher = SolCConstant.SOLC_VERSION_PATTERN.matcher(output);
                    if (matcher.find()) {
                        return matcher.group(1);
                    }
                }
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException("Could not evaluate SolC Version from '" + getCanonicalPath() + "'", e);
        }
        throw new RuntimeException("Could not evaluate SolC Version from '" + getCanonicalPath() + "'");
    }

    public String getCanonicalPath() {
        return canonicalPath;
    }

    public String getCanonicalWorkingDirectory() {
        return canonicalWorkingDirectory;
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

    /**
     * Evaluate (lazy) the version of the solC Library.
     * <p>
     * The first time this getter is called, a version call to solC is executed. (<code>solc
     * --version</code>)
     *
     * @return
     */
    public String getVersion() {
        if (StringUtils.isEmpty(version)) {
            version = evaluateSolCVersion();
        }
        return version;
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    private File initBundled() throws IOException {
        File solC = null;
        File tmpDir = new File(System.getProperty("java.io.tmpdir"), "solc");
        tmpDir.setReadable(true);
        tmpDir.setWritable(true);
        tmpDir.setExecutable(true);
        tmpDir.mkdirs();

        String solcPath = "/native/" + getOS() + "/solc/";
        InputStream is = getClass().getResourceAsStream(solcPath + "file.list");
        Scanner scanner = new Scanner(is);
        if (scanner.hasNext()) {
            // first file in the list denotes executable
            String s = scanner.next();
            File targetFile = new File(tmpDir, s);
            InputStream fis = getClass().getResourceAsStream(solcPath + s);
            Files.copy(fis, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            solC = targetFile;
            solC.setExecutable(true);
            targetFile.deleteOnExit();
        }
        tmpDir.deleteOnExit();
        return solC;
    }
}
