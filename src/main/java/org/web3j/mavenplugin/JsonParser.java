package org.web3j.mavenplugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Map;

public class JsonParser {

    JSONParser jsonParser;


    public JsonParser() {
        jsonParser = new JSONParser();
    }

    public Map<String, Object> parseJson(String jsonString) throws MojoExecutionException {

        try {
            return (Map<String, Object>) jsonParser.parse(jsonString);
        } catch (ParseException e) {
            throw new MojoExecutionException("Could not parse SolC result", e);
        }
    }
}
