package mors.museumguide.llm;

import mors.museumguide.client.MuseumGuideClient;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class chatHandler {

    public static void getChatMessage() {
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            String messageText = message.getString();
            if (messageText.contains("@GuideBot")) {
                messageText = convMessage(messageText).replaceAll("@GuideBot","").trim();
                if (!messageText.isEmpty()) {
                    processGuideQuery(messageText);
                }
            }
        });
    }

    public static String convMessage(String message) {
        return message.replaceAll("<[^>]*>", "").trim();
    }

    private static void processGuideQuery(String query) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Show thinking message
        client.player.sendMessage(Text.literal("[Guide] Thinking..."), false);

        try {
            // Get LLM instance from client
            var llm = MuseumGuideClient.getLlmInstance();
            if (llm == null) {
                client.player.sendMessage(Text.literal("[Guide] LLM not initialized"), false);
                return;
            }

            // Process with LLM asynchronously
            llm.processMessageAsync(query).thenAccept(response -> {
                // Display response to player on the main thread
                client.execute(() -> {
                    client.player.sendMessage(Text.literal("[Guide] " + response), false);
                });
            }).exceptionally(ex -> {
                // Handle exceptions
                client.execute(() -> {
                    client.player.sendMessage(Text.literal("[Guide] Error: " + ex.getMessage()), false);
                });
                return null;
            });
        } catch (Exception e) {
            client.player.sendMessage(Text.literal("[Guide] Error: " + e.getMessage()), false);
        }
    }
}