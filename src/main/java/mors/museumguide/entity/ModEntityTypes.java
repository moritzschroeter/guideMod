package mors.museumguide.entity;

import mors.museumguide.MuseumGuide;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEntityTypes {
    public static final EntityType<guideEntity> GUIDE = register(
            "guide",
            EntityType.Builder.<guideEntity>create(guideEntity::new, SpawnGroup.MISC)
                    .dimensions(0.75f, 1.75f)
    );

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MuseumGuide.MOD_ID, name));
        return Registry.register(Registries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void registerModEntityTypes(){
        MuseumGuide.LOGGER.info("Registering EntityTypes for " + MuseumGuide.MOD_ID);
    }

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(GUIDE, guideEntity.createCubeAttributes());
    }
}