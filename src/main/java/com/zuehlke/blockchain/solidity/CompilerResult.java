package com.zuehlke.blockchain.solidity;

/**
 * Created by hem on 18.05.2017.
 */
public class CompilerResult {
    public String errors;
    public String output;
    private boolean success = false;

    public CompilerResult(String errors, String output, boolean success) {
        this.errors = errors;
        this.output = output;
        this.success = success;
    }

    public boolean isFailed() {
        return !success;
    }
}
