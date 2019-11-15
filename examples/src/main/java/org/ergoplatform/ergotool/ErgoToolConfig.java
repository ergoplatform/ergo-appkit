package org.ergoplatform.ergotool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;

/**
 * Configuration parameters of ErgoTool utility
 */
public class ErgoToolConfig {
    private ErgoNodeConfig node;
    private ToolParameters parameters;

    /**
     * Returns Ergo node configuration
     */
    public ErgoNodeConfig getNode() {
        return node;
    }

    /**
    * Config section with Named parameters
     * @return {@code HashMap<String,String>} of Name-Value pairs
     */
    public ToolParameters getParameters() {
        return parameters;
    }

    public static ErgoToolConfig load(String fileName) throws FileNotFoundException {
        Gson gson = new GsonBuilder().create();
        File file = Paths.get(fileName).toAbsolutePath().toFile();
        FileReader reader = new FileReader(file);
        return gson.fromJson(reader, ErgoToolConfig.class);
    }
}
