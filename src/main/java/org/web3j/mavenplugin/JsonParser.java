package org.web3j.mavenplugin;

import org.apache.maven.plugin.MojoExecutionException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;

public class JsonParser {

    private final ScriptEngine engine;

    public JsonParser() {
        engine = new ScriptEngineManager(null).getEngineByName("nashorn");
    }

    public Map<String, Object> parseJson(String jsonString) throws MojoExecutionException {
        try {
            String script = "JSON.parse(JSON.stringify(" + jsonString + "))";
            return  (Map<String, Object>) engine.eval(script);
        } catch (
                ScriptException e) {
            throw new MojoExecutionException("Could not parse SolC result", e);
        }
    }
}
