package mors.museumguide.client;

//import mors.museumguide.command.findSignCommand;
import mors.museumguide.command.ToolsCommand;
import mors.museumguide.entity.ModEntityTypes;
import mors.museumguide.llm.chatHandler;
import mors.museumguide.model.guideEntityModelLayers;
import mors.museumguide.entity.renderer.guideEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import mors.museumguide.llm.initLLM;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class MuseumGuideClient implements ClientModInitializer {

    private static initLLM llmInstance;

    @Override
    public void onInitializeClient() {
        // This code runs on the client side when Minecraft is loaded.
        guideEntityModelLayers.registerModelLayers();
        EntityRendererRegistry.register(ModEntityTypes.GUIDE, guideEntityRenderer::new);

        // Initialize LLM
        initializeLLM();
        chatHandler.getChatMessage();
        // In MuseumGuideClient.java
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                ToolsCommand.register(dispatcher, registryAccess, environment));

    }

    private void initializeLLM() {
        try {
            System.out.println("Initializing LLM service...");
            llmInstance = new initLLM();
        } catch (Exception e) {
            System.err.println("Failed to initialize LLM service: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static initLLM getLlmInstance() {
        return llmInstance;
    }
}
