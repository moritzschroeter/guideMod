package mors.museumguide.logic;

import mors.museumguide.entity.guideEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class guideInteractionTracker {
    // Maps player UUID to last interacted guide entity ID
    private static final Map<UUID, guideEntity> lastInteractedGuide = new HashMap<>();

    public static void trackInteraction(ServerPlayerEntity player, guideEntity guide) {
        lastInteractedGuide.put(player.getUuid(), guide.getGuide());
    }

    public static guideEntity getLastInteractedGuide(ServerPlayerEntity player) {
        return lastInteractedGuide.get(player.getUuid());
    }
}