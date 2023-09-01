package org.web3j.mavenplugin;

import org.apache.maven.plugins.annotations.Parameter;

public class SourceDestination {

    @Parameter(property = "java")
    private String java;
    @Parameter(property = "bin")
    private String bin;
    @Parameter(property = "abi")
    private String abi;
    @Parameter(property = "metadata")
    private String metadata;

    public String getAbi() {
        return abi;
    }

    public void setAbi(String abi) {
        this.abi = abi;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getJava() {
        return java;
    }

    public void setJava(String java) {
        this.java = java;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
