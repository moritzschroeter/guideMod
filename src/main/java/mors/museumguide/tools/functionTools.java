package mors.museumguide.tools;

import dev.langchain4j.agent.tool.P;
import mors.museumguide.logic.guideInteractionTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class functionTools {

    private static ServerPlayerEntity lastInteractedPlayer = guideInteractionTracker.getPlayer();

    public static void setLastInteraction(ServerPlayerEntity player) {
        lastInteractedPlayer = player;
    }

    private World getPlayerWorld() {
        if (lastInteractedPlayer != null) {
            return lastInteractedPlayer.getWorld();
        }
        return null;
    }

    //@Tool("Test if function calling is working properly")
    public String testFunctionCalling(@P("Test message") String message) {
        System.out.println("Function calling test received: " + message);
        return "Function calling is working! Received: " + message;
    }





    }
