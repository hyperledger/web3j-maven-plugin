package org.web3j.mavenplugin.solidity;

import java.util.regex.Pattern;

public interface Constant {

    Pattern SOLC_VERSION_PATTERN = Pattern.compile("Version: (.*)", Pattern.MULTILINE);
    Pattern SOLIDITY_VERSION_EXTRACT = Pattern.compile("(pragma solidity.*;)", Pattern.MULTILINE);

}
