package org.web3j.mavenplugin.solidity;

/**
 * Container for the compile result.
 *
 */
public class CompilerResult {
    public String errors;
    public String output;
    private boolean success = false;

    public CompilerResult(String errors, String output, boolean success) {
        this.errors = errors;
        // https://ethereum.stackexchange.com/questions/11912/unable-to-define-greetercontract-in-the-greeter-tutorial-breaking-change-in-sol
        this.output = output.replaceAll("<stdin>:", "");
        this.success = success;
    }

    public boolean isFailed() {
        return !success;
    }
}
