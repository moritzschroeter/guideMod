package mors.museumguide.tools;

import com.google.gson.Gson;
import dev.langchain4j.agent.tool.Tool;
import okhttp3.HttpUrl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class ClevelandArtApiTools {

    private final String baseURL = "https://collectionapi.metmuseum.org/public/collection/v1/";
    @Tool("Get information about the artist of the nearest painting")
    public ArtistItem getNearestSignAuthorInfo() {
        // Hole den Text des nächsten Schilds
        String signText = signTools.signWrapper();
        if (signText == null || signText.isEmpty()) {
            System.out.println("Kein Schildtext gefunden.");
            return null;
        }
        // Suche nach "by:" und extrahiere den Autor
        String lower = signText.toLowerCase();
        int idx = lower.indexOf("by:");
        if (idx == -1) {
            System.out.println("Kein 'by:' im Schildtext gefunden.");
            return null;
        }
        String afterBy = signText.substring(idx + 3).trim();
        // Falls noch weitere Infos nach dem Namen stehen, nur den Namen nehmen (bis zum nächsten Trennzeichen)
        String author = afterBy.split("[|\\n\\r]")[0].trim();
        if (author.isEmpty()) {
            System.out.println("Kein Autor nach 'by:' gefunden.");
            return null;
        }
        // Suche nach Künstlerinfos
        return searchArtistInfo(author);
    }
    @Tool("Get information about a painting")
    public ArtworkInfo getArtwork(String paintingName) {
        System.out.println("Calling getArtwork() with painting " + paintingName);
        try {
            // Step 1: search for the painting by keyword
            HttpUrl searchUrl = HttpUrl.parse(baseURL + "search").newBuilder()
                    .addQueryParameter("q", paintingName)
                    .build();

            HttpURLConnection searchCon = (HttpURLConnection) searchUrl.url().openConnection();
            searchCon.setRequestMethod("GET");

            int searchResponseCode = searchCon.getResponseCode();
            if (searchResponseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(searchCon.getInputStream()))) {
                    StringBuilder searchResponse = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        searchResponse.append(line);
                    }
                    Gson gson = new Gson();
                    MetSearchResponse searchResult = gson.fromJson(searchResponse.toString(), MetSearchResponse.class);
                    if (searchResult != null && searchResult.objectIDs != null && !searchResult.objectIDs.isEmpty()) {
                        int objectId = searchResult.objectIDs.get(0);
                        // Step 2: fetch details for the first result
                        HttpUrl objectUrl = HttpUrl.parse(baseURL + "objects/" + objectId).newBuilder().build();
                        HttpURLConnection objectCon = (HttpURLConnection) objectUrl.url().openConnection();
                        objectCon.setRequestMethod("GET");
                        int objectResponseCode = objectCon.getResponseCode();
                        if (objectResponseCode == HttpURLConnection.HTTP_OK) {
                            try (BufferedReader objIn = new BufferedReader(new InputStreamReader(objectCon.getInputStream()))) {
                                StringBuilder objResponse = new StringBuilder();
                                String objLine;
                                while ((objLine = objIn.readLine()) != null) {
                                    objResponse.append(objLine);
                                }
                                MetObjectResponse objectDetails = gson.fromJson(objResponse.toString(), MetObjectResponse.class);
                                if (objectDetails != null) {
                                    String artist = objectDetails.artistDisplayName != null && !objectDetails.artistDisplayName.isEmpty()
                                            ? objectDetails.artistDisplayName : "Unbekannt";
                                    String description = objectDetails.creditLine != null ? objectDetails.creditLine : "Keine Beschreibung verfügbar";
                                    String technique = objectDetails.medium != null ? objectDetails.medium : "Unbekannt";
                                    String tombstone = objectDetails.department != null ? objectDetails.department : "Keine Details verfügbar";
                                    String creationDate = objectDetails.objectDate != null ? objectDetails.objectDate : "Unbekannt";
                                    return new ArtworkInfo(objectDetails.title, artist, tombstone, technique, creationDate, description);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArtworkInfo(paintingName, "Unbekannt", "Fehler bei der Anfrage", "Unbekannt", "Unbekannt", "Keine Beschreibung verfügbar");
        }
        return new ArtworkInfo(paintingName, "Unbekannt", "Keine Details verfügbar", "Unbekannt", "Unbekannt", "Keine Beschreibung verfügbar");
    }

    @Tool("Get information about an artist.")
    public ArtistItem searchArtistInfo(String artistName) {
        System.out.println("Calling searchArtistInfo() with artistName: " + artistName);
        try {
            // The Met API does not provide direct artist search.
            // We search for the artist name and take the first object to extract artist info.
            HttpUrl searchUrl = HttpUrl.parse(baseURL + "search").newBuilder()
                    .addQueryParameter("q", artistName)
                    .build();

            HttpURLConnection con = (HttpURLConnection) searchUrl.url().openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    Gson gson = new Gson();
                    MetSearchResponse searchResponse = gson.fromJson(response.toString(), MetSearchResponse.class);
                    if (searchResponse != null && searchResponse.objectIDs != null && !searchResponse.objectIDs.isEmpty()) {
                        int objectId = searchResponse.objectIDs.get(0);
                        HttpUrl objectUrl = HttpUrl.parse(baseURL + "objects/" + objectId).newBuilder().build();
                        HttpURLConnection objCon = (HttpURLConnection) objectUrl.url().openConnection();
                        objCon.setRequestMethod("GET");
                        if (objCon.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            try (BufferedReader objIn = new BufferedReader(new InputStreamReader(objCon.getInputStream()))) {
                                StringBuilder objResponse = new StringBuilder();
                                String objLine;
                                while ((objLine = objIn.readLine()) != null) {
                                    objResponse.append(objLine);
                                }
                                MetObjectResponse objectDetails = gson.fromJson(objResponse.toString(), MetObjectResponse.class);
                                if (objectDetails != null) {
                                    ArtistItem artistItem = new ArtistItem();
                                    artistItem.name = objectDetails.artistDisplayName;
                                    artistItem.nationality = objectDetails.artistNationality;
                                    artistItem.birth_year = objectDetails.artistBeginDate;
                                    artistItem.death_year = objectDetails.artistEndDate;
                                    artistItem.description = objectDetails.creditLine;
                                    return artistItem;
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Tool("Get all paintings by an artist")
    public ArrayList<ArtworkInfo> searchArtworks(String artistName) {
        System.out.println("Calling searchArtworks() with artistName " + artistName);
        ArrayList<ArtworkInfo> result = new ArrayList<>();

        try {
            HttpUrl searchUrl = HttpUrl.parse(baseURL + "search").newBuilder()
                    .addQueryParameter("q", artistName)
                    .build();

            HttpURLConnection con = (HttpURLConnection) searchUrl.url().openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    Gson gson = new Gson();
                    MetSearchResponse searchResponse = gson.fromJson(response.toString(), MetSearchResponse.class);
                    if (searchResponse != null && searchResponse.objectIDs != null) {
                        int count = 0;
                        for (Integer objectId : searchResponse.objectIDs) {
                            HttpUrl objectUrl = HttpUrl.parse(baseURL + "objects/" + objectId).newBuilder().build();
                            HttpURLConnection objCon = (HttpURLConnection) objectUrl.url().openConnection();
                            objCon.setRequestMethod("GET");
                            if (objCon.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                try (BufferedReader objIn = new BufferedReader(new InputStreamReader(objCon.getInputStream()))) {
                                    StringBuilder objResponse = new StringBuilder();
                                    String objLine;
                                    while ((objLine = objIn.readLine()) != null) {
                                        objResponse.append(objLine);
                                    }
                                    MetObjectResponse objectDetails = gson.fromJson(objResponse.toString(), MetObjectResponse.class);
                                    if (objectDetails != null && objectDetails.title != null) {
                                        String artist = objectDetails.artistDisplayName != null ? objectDetails.artistDisplayName : "Unbekannt";
                                        String description = objectDetails.creditLine != null ? objectDetails.creditLine : "Keine Beschreibung verfügbar";
                                        String technique = objectDetails.medium != null ? objectDetails.medium : "Unbekannt";
                                        String tombstone = objectDetails.department != null ? objectDetails.department : "Keine Details verfügbar";
                                        String creationDate = objectDetails.objectDate != null ? objectDetails.objectDate : "Unbekannt";
                                        result.add(new ArtworkInfo(objectDetails.title, artist, tombstone, technique, creationDate, description));
                                        count++;
                                        if (count >= 10) break; // limit results for performance
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static class ArtworkInfo {
        public String title;
        public String artist;
        public String tombstone;
        public String technique;
        public String creationDate;
        public String description;

        public ArtworkInfo(String title, String artist, String tombstone, String technique, String creationDate, String description) {
            this.title = title;
            this.artist = artist;
            this.tombstone = tombstone;
            this.technique = technique;
            this.creationDate = creationDate;
            this.description = description;
        }

        @Override
        public String toString() {
            return String.format("Titel: %s\nKünstler: %s\nBeschreibung: %s\nTechnik: %s", title, artist, tombstone, technique);
        }
    }

    public static class ArtistItem {
        public String name;
        public String description;
        public String nationality;
        public String birth_year;
        public String death_year;
    }

    // Helper classes for Met API JSON responses
    class MetSearchResponse {
        List<Integer> objectIDs;
    }

    class MetObjectResponse {
        int objectID;
        String title;
        String artistDisplayName;
        String artistNationality;
        String artistBeginDate;
        String artistEndDate;
        String objectDate;
        String medium;
        String department;
        String creditLine;
    }
}
