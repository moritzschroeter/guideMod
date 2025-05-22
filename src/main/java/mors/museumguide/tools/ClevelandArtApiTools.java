package mors.museumguide.tools;

import com.google.gson.Gson;
import dev.langchain4j.agent.tool.Tool;
import okhttp3.HttpUrl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class ClevelandArtApiTools {

    private final String baseURL = "https://openaccess-api.clevelandart.org/api/artworks";

    @Tool("Get information about a painting ")
    public ArtworkInfo searchArtworksCompact(String paintingName) {
        System.out.println(" with painting " + paintingName);
        try {
            HttpUrl url = HttpUrl.parse(baseURL).newBuilder()
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

    @Tool("Get information about an artist and other paintings they have done. ")
    public List<SimpleArtworkInfo> searchArtistInfo(String artistName) {
        System.out.println("Calling searchArtistInfo() with artistName: " + artistName);
        try {
            HttpUrl url = HttpUrl.parse(baseURL).newBuilder()
                    .addQueryParameter("q", "")
                    .addQueryParameter("artists", artistName)
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
                    List<SimpleArtworkInfo> result = new ArrayList<>();
                    if (artResponse != null && artResponse.data != null && !artResponse.data.isEmpty()) {
                        for (ArtworkItem artwork : artResponse.data) {
                            String artist = (artwork.creators != null && !artwork.creators.isEmpty())
                                    ? artwork.creators.get(0).description
                                    : "Unbekannt";
                            String description = artwork.description != null ? artwork.description : "Keine Beschreibung verfügbar";
                            result.add(new SimpleArtworkInfo(artwork.title, description, artist));
                        }
                    }
                    return result;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Tool("Get information about the artist of the nearest painting")
    public List<SimpleArtworkInfo> getNearestSignAuthorInfo() {
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

    public static void main(String[] args) {
        ClevelandArtApiTools tools = new ClevelandArtApiTools();
        ArtworkInfo info = tools.searchArtworksCompact("View of Schroon Mountain, Essex County, New York, After a Storm");
        if (info != null) {
            System.out.println(info);
        } else {
            System.out.println("Keine Daten gefunden.");
        }

        // Beispiel für die Künstlersuche (nur kompakte Infos)
        List<SimpleArtworkInfo> artworks = tools.searchArtistInfo("Thomas Cole");
        if (artworks != null && !artworks.isEmpty()) {
            for (SimpleArtworkInfo infoItem : artworks) {
                System.out.println(infoItem);
                System.out.println("---");
            }
        } else {
            System.out.println("Keine Werke gefunden.");
        }
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

class Creator {
    String name;
    String description; // Hinzugefügt für Künstlerbeschreibung
}

// Hilfsklassen für die Künstler-Deserialisierung
class ClevelandArtistResponse {
    List<ArtistItem> data;
}

class ArtistItem {
    String name;
    String description;
    String birth;
    String death;
}
