package mors.museumguide.model;

import mors.museumguide.MuseumGuide;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class guideEntityModelLayers {
    public static final EntityModelLayer GUIDE = createMain("guide");

    private static EntityModelLayer createMain(String name) {
        return new EntityModelLayer(Identifier.of(MuseumGuide.MOD_ID, name), "main");
    }

    public static void registerModelLayers() {
        EntityModelLayerRegistry.registerModelLayer(guideEntityModelLayers.GUIDE, guideEntityModel::getTexturedModelData);
    }
}

