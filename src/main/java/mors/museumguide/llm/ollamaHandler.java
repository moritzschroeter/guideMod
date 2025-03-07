package mors.museumguide.llm;

import mors.museumguide.client.MuseumGuideClient;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class ollamaHandler {
    private String[] modelNames;
    public ollamaHandler() {
        var llm = MuseumGuideClient.getLlmInstance();

    }

    public static String getJsonFromUrl(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * Saves the given JSON data to a file with the specified name in the current working directory.
     *
     * @param jsonData the JSON data to save
     * @param fileName the name of the file to write to
     * @throws IOException if an I/O error occurs writing to or creating the file
     */
    public static void saveJsonToFile(String jsonData, String fileName) throws IOException {
        Path filePath = Paths.get(fileName);
        Files.writeString(filePath, jsonData);
    }

    public void getModels() throws IOException, InterruptedException {
        String modelURL = initLLM.getBASE_URL().concat("/api/tags");
        String response = getJsonFromUrl(modelURL);
        saveJsonToFile(response, "models.json");
    }

    public String[] modelList() throws IOException, InterruptedException {
        String modelURL = initLLM.getBASE_URL().concat("/api/tags");
        JSONObject response = (JSONObject) JSONValue.parse(getJsonFromUrl(modelURL));
        JSONArray data = (JSONArray) response.get("models");
        String[] modelNames = new String[0];
        if (data != null) {
            modelNames = new String[data.size()];
            for(int i = 0; i < data.size(); i++) {
                JSONObject modelObj = (JSONObject) data.get(i);
                modelNames[i] = (String) modelObj.get("model");
            }
        }
        System.out.println(Arrays.toString(modelNames));
        return modelNames;
    }
    public void setModelName(String modelName) throws IOException, InterruptedException {
        initLLM.setModelName(modelName);
    }
    public void printModel()  {
        System.out.println("[GUIDE] Printing LLM model...");
        System.out.println(initLLM.getModelName() + ": " + initLLM.getBASE_URL());
    };
    public void resetModel()    {

    }
}