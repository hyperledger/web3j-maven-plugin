package org.web3j.mavenplugin.solidity;

import java.util.regex.Pattern;

public interface SolCConstant {

    Pattern SOLC_VERSION_PATTERN = Pattern.compile("Version: (.*)", Pattern.MULTILINE);

}
