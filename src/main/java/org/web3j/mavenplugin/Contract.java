package org.web3j.mavenplugin;

import java.util.List;

/**
 * Created by hem on 01.06.2017.
 */
public class Contract {

    private List<String> includes;
    private List<String> excludes;


    public List<String> getIncludes() {
        return includes;
    }

    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }
}
