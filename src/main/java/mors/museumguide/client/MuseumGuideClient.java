package mors.museumguide.client;

import mors.museumguide.entity.ModEntityTypes;
import mors.museumguide.model.guideEntityModelLayers;
import mors.museumguide.entity.renderer.guideEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class MuseumGuideClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This code runs on the client side when Minecraft is loaded.
        guideEntityModelLayers.registerModelLayers();
        EntityRendererRegistry.register(ModEntityTypes.GUIDE, guideEntityRenderer::new);
    }
}
