package mors.museumguide.tools;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.langchain4j.agent.tool.Tool;
import okhttp3.HttpUrl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class ClevelandArtApiTools {

    private final String baseURL = "https://openaccess-api.clevelandart.org/api/";

    @Tool("Get information about a painting ")
    public ArtworkInfo getArtwork(String paintingName) {
        System.out.println(" calling with painting " + paintingName);
        try {
            HttpUrl url = HttpUrl.parse(baseURL + "artworks/").newBuilder()
                    .addQueryParameter("q", paintingName)
                    .build();

            HttpURLConnection con = (HttpURLConnection) url.url().openConnection();
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
                    ClevelandArtResponse artResponse = gson.fromJson(response.toString(), ClevelandArtResponse.class);
                    if (artResponse != null && artResponse.data != null && !artResponse.data.isEmpty()) {
                        ArtworkItem artwork = artResponse.data.get(0);
                        String artist = (artwork.creators != null && !artwork.creators.isEmpty())
                                ? artwork.creators.get(0).name
                                : "Unbekannt";
                        String description = artwork.description != null ? artwork.description : "Keine Beschreibung verfügbar";
                        String technique = artwork.technique != null ? artwork.technique : "Unbekannt";
                        return new ArtworkInfo(artwork.title, artist, description, technique);
                    } else {
                        return new ArtworkInfo(null, "", "", "");
                    }
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }




    @Tool("Get information about an artist.")
    public ArtistItem searchArtistInfo(String artistName) {
        System.out.println("Calling searchArtistInfo() with artistName: " + artistName);
        try {
            HttpUrl url = HttpUrl.parse(baseURL + "creators/").newBuilder()
                    .addQueryParameter("name", artistName)
                    .build();

            HttpURLConnection con = (HttpURLConnection) url.url().openConnection();
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
                    com.google.gson.JsonObject jsonObject = gson.fromJson(response.toString(), com.google.gson.JsonObject.class);
                    if (jsonObject.has("data")) {
                        Type listType = new TypeToken<List<ArtistItem>>() {}.getType();
                        List<ArtistItem> artists = gson.fromJson(jsonObject.get("data"), listType);

                        if (artists != null && !artists.isEmpty()) {
                            ArtistItem artistItem = artists.get(0);
                            System.out.println(String.format("Name: %s, Description: %s, Nationality: %s, Birth Year: %s, Death Year: %s",
                                    artistItem.name, artistItem.description, artistItem.biography,  artistItem.nationality, artistItem.birth_year, artistItem.death_year));
                            return artistItem;
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
    //@P("artistName", "Name of the artist")
    public ArrayList<ArtworkItem> searchArtworks(String artistName) {
        System.out.println("Calling searchArtworks() with artistName " + artistName);
        ArrayList<ArtworkItem> result = new ArrayList<>();

        try {
            // Request artist data including artworks
            HttpUrl url = HttpUrl.parse(baseURL + "creators/").newBuilder()
                    .addQueryParameter("name", artistName)
                    .build();

            HttpURLConnection con = (HttpURLConnection) url.url().openConnection();
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
                    ClevelandArtistResponse artistResponse = gson.fromJson(response.toString(), ClevelandArtistResponse.class);

                    if (artistResponse != null && artistResponse.data != null && !artistResponse.data.isEmpty()) {
                        ArtistItem artist = artistResponse.data.get(0);
                        if (artist.artworks != null) {
                            result.addAll(artist.artworks);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }



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


    public static class ArtworkInfo {
        public String title;
        public String artist;
        public String description;
        public String technique;

        public ArtworkInfo(String title, String artist, String description, String technique) {
            this.title = title;
            this.artist = artist;
            this.description = description;
            this.technique = technique;
        }

        @Override
        public String toString() {
            return String.format("Titel: %s\nKünstler: %s\nBeschreibung: %s\nTechnik: %s", title, artist, description, technique);
        }
    }

    public static class SimpleArtworkInfo {
        public String title;
        public String description;
        public String artist;

        public SimpleArtworkInfo(String title, String description, String artist) {
            this.title = title;
            this.description = description;
            this.artist = artist;
        }

        @Override
        public String toString() {
            return String.format("Titel: %s\nBeschreibung: %s\nKünstler: %s", title, description, artist);
        }
    }

    // Hilfsklassen für die JSON-Deserialisierung
    class ClevelandArtResponse {
        List<ArtworkItem> data;
    }

    class ArtworkItem {
        String title;
        String description;
        String technique;
        List<Creator> creators;
    }
    public class ArtworkResponse {
        public int id;
        public String accession_number;
        public String title;
        public String tombstone;
        public String url;
    }


    class Creator {
        String name;
        String description; // Hinzugefügt für Künstlerbeschreibung
    }

    // Hilfsklassen für die Künstler-Deserialisierung
    class ClevelandArtistResponse {
        List<ArtistItem> data;
    }

    public static class ArtistItem {
        String name;
        String description;
        String biography;
        String nationality;
        String birth_year;
        String death_year;
        List<ArtworkItem> artworks; // Hinzugefügt
    }
}