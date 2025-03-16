package mors.museumguide.tools;

import com.google.gson.Gson;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import mors.museumguide.logic.guideInteractionTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

public class paintingTools {

    static paintingData.image currentPainting = new paintingData.image();
    private static String getWorldCustomPaintingsPath() {
        if (guideInteractionTracker.getPlayer() != null) {
            ServerPlayerEntity player = guideInteractionTracker.getPlayer();
            ServerWorld world = player.getServerWorld();
            String worldDirectory = world.getServer().getSavePath(WorldSavePath.ROOT).toString();
            // Fix the path construction - remove any relative path components
            System.out.println(worldDirectory + "/custompaintings/baMod - paintings/custompaintings.json");

            return worldDirectory + "/custompaintings/baMod - paintings/custompaintings.json";
        }
        System.out.println("custompaintings/baMod - paintings/custompaintings.json");

        return "custompaintings/baMod - paintings/custompaintings.json";
    }
    public static paintingData getCustompaintings() throws FileNotFoundException {
        String filePath = getWorldCustomPaintingsPath();
        System.out.println("Loading custom paintings from: " + filePath);

        Gson gson = new Gson();
        paintingData paintings = null;

        try (Reader reader = new FileReader(filePath)) {
            // Convert the JSON data to a Java object
            paintings = gson.fromJson(reader, paintingData.class);

            // Debug output
            if (paintings == null) {
                System.out.println("Warning: Gson parsed null object from JSON");
            } else if (paintings.paintings == null) {
                System.out.println("Warning: images array is null in parsed JSON");
            } else {
                System.out.println("Successfully loaded " + paintings.paintings.size() + " paintings");
            }
        } catch (IOException e) {
            System.out.println("IO error reading paintings file: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return paintings;
    }
    @Tool("Get the description of the painting with the provided id")
    public static String getPaintingDescription(@P("ID of the painting") String paintingID) {
        System.out.println("getPaintingDescription() called for ID: " + paintingID);

        try {
            paintingData paintings = getCustompaintings();

            if (paintings == null) {
                return "Error: Failed to load paintings data";
            }

            // Use paintings.paintings instead of paintings.images
            if (paintings.paintings == null) {
                System.out.println("Error: paintings array is null in parsed JSON");
                return "Error: Paintings data has no paintings array";
            }

            // Iterate through paintings.paintings instead of paintings.images
            for (paintingData.image img : paintings.paintings) {
                if (Objects.equals(img.id, paintingID)) {
                    currentPainting = img;
                    return img.description != null ? img.description :
                            "Painting: " + img.name + " by " + img.artist + " (No detailed description available)";
                }
            }

            return "No painting with ID: " + paintingID + " found";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error retrieving painting description: " + e.getMessage();
        }
    }
    //@Tool("Return information about the current painting")
    public static String getPaintingInfo()  {
        System.out.println("getPaintingInfo() called");
        return currentPainting.toString();
    }
}
