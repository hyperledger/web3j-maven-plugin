package org.web3j.mavenplugin.solidity;

import org.web3j.sokt.SolcInstance;
import org.web3j.sokt.SolcRelease;
import org.web3j.sokt.SolidityFile;
import org.web3j.sokt.VersionResolver;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Selects a Solidity compiler release that can compile all source files in one Solc invocation.
 */
final class SolcVersionSelector {

    SolcInstance getCompilerInstance(String cacheDirectory, boolean download, List<SolidityFile> solidityFiles) {
        VersionResolver versionResolver = new VersionResolver(cacheDirectory);
        SolcRelease release = selectLatestCompatibleRelease(solidityFiles, versionResolver.getSolcReleases(), versionResolver);
        return new SolcInstance(release, cacheDirectory, download, solidityFiles.toArray(new SolidityFile[0]));
    }

    SolcRelease selectLatestCompatibleRelease(Collection<SolidityFile> solidityFiles, List<SolcRelease> releases) {
        return selectLatestCompatibleRelease(solidityFiles, releases, new VersionResolver(".web3j"));
    }

    private SolcRelease selectLatestCompatibleRelease(
            Collection<SolidityFile> solidityFiles,
            List<SolcRelease> releases,
            VersionResolver versionResolver) {

        if (solidityFiles == null || solidityFiles.isEmpty()) {
            throw new IllegalArgumentException("No Solidity source files provided for compiler selection.");
        }
        if (releases == null || releases.isEmpty()) {
            throw new IllegalStateException("No Solc releases are available for compiler selection.");
        }

        List<SolcRelease> compatibleReleases = releases;
        boolean foundPragma = false;

        for (SolidityFile solidityFile : solidityFiles) {
            String versionPragma = solidityFile.getVersionPragma();
            if (versionPragma == null || versionPragma.isBlank()) {
                continue;
            }

            foundPragma = true;
            compatibleReleases = versionResolver.getCompatibleVersions(versionPragma, compatibleReleases);
            if (compatibleReleases.isEmpty()) {
                throw noCompatibleReleaseException(solidityFiles);
            }
        }

        if (!foundPragma) {
            compatibleReleases = releases.stream()
                    .filter(SolcRelease::isCompatibleWithOs)
                    .collect(Collectors.toList());
        }

        if (compatibleReleases.isEmpty()) {
            throw noCompatibleReleaseException(solidityFiles);
        }

        return compatibleReleases.getLast();
    }

    private IllegalStateException noCompatibleReleaseException(Collection<SolidityFile> solidityFiles) {
        String sourceDescription = solidityFiles.stream()
                .map(this::describeSource)
                .collect(Collectors.joining(", "));
        String sourceNoun = solidityFiles.size() == 1 ? "file" : "files";
        return new IllegalStateException(
                "No compatible solc release could be found for the " + sourceNoun + ": " + sourceDescription);
    }

    private String describeSource(SolidityFile solidityFile) {
        String pragma = solidityFile.getVersionPragma();
        String versionRequirement = pragma == null || pragma.isBlank() ? "no pragma" : pragma;
        return solidityFile.getSourceFile() + " (" + versionRequirement + ")";
    }
}
