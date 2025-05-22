package mors.museumguide.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import mors.museumguide.entity.guideEntity;
import mors.museumguide.logic.guideInteractionTracker;
import mors.museumguide.logic.moveToCoord;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static mors.museumguide.logic.guideInteractionTracker.getLastInteractedGuide;

public class guideTools {

    signTools sign = new signTools();

    // Methode zum dynamischen Abrufen des letzten interagierenden Spielers
    private static ServerPlayerEntity getLastInteractedPlayer() {
        return guideInteractionTracker.getPlayer();
    }

    public static void setLastInteraction(ServerPlayerEntity player) {
        guideInteractionTracker.trackInteraction(player, getLastInteractedGuide(player));
    }

    @Tool("Follow the player")
    public String followWrapper() {
        System.out.println("followWrapper() was called");

        ServerPlayerEntity player = getLastInteractedPlayer();
        if (player == null) {
            System.out.println("No interacted player available");
            return "No player available to follow";
        }
        makeLastInteractedGuideFollow(player);
        return "Following the player!";
    }

    public String makeLastInteractedGuideFollow(ServerPlayerEntity player) {
        if (player == null) {
            return "No player specified";
        }

        guideEntity guide = guideInteractionTracker.getLastInteractedGuide(player);

        if (guide == null) {
            return "Please interact with a guide entity first!";
        }

        if (guide instanceof guideEntity) {
            guide.setFollowPlayer(player);
            return "The guide you last interacted with is now following you.";
        } else {
            return "Couldn't find the guide you interacted with.";
        }
    }
    @Tool("Calculate the square root of a number")
    public Double squareRoot(Double number) {
        return Math.sqrt(number);
    }



    @Tool("Stop following the player")
    public String stopFollowingWrapper()  {
        System.out.println("stopFollowingWrapper() was called");

        ServerPlayerEntity player = getLastInteractedPlayer();
        if (player == null) {
            System.out.println("No interacted player available");
            return "No player available to stop following";
        }
        stopFollowing(player);
        return "Stopped following the player!";
    }


    public void stopFollowing(ServerPlayerEntity player) {
        if (player == null) {
            System.out.println("No player specified");
        }

        guideEntity guide = getLastInteractedGuide(player);

        if (guide == null) {
            System.out.println("Please interact with a guide entity first!");
        }
        guide.removeFollowPlayer(player);
        System.out.println("The guide is no longer following you.");
    }
    @Tool("Move to coordinates")
    public static String move(
            @P("x Coordinate") int x,
            @P("y Coordinate") int y,
            @P("z Coordinate") int z) {
        ServerPlayerEntity player = getLastInteractedPlayer();
        if (player == null) {
            return "No player available";
        }

        World world = player.getWorld();
        if (world instanceof ServerWorld serverWorld) {
            // Schedule the movement logic to run on the server thread
            serverWorld.getServer().execute(() -> {
                guideEntity guide = getLastInteractedGuide(player);
                if (guide != null) {
                    moveToCoord move = new moveToCoord(guide, world);
                    move.moveTo(new BlockPos(x, y, z), guide);
                }
            });
            return "Moving to coordinates: " + x + ", " + y + ", " + z;
        }

        return "Could not access server world";
    }
    @Tool("Move to the nearest sign")
    public static String moveToNearestSign() {
        System.out.println("moveToNearestSign() was called");
        ServerPlayerEntity player = getLastInteractedPlayer();
        if (player == null) {
            return "No interacted player available";
        }

        World world = player.getWorld();
        if (world instanceof ServerWorld serverWorld) {
            BlockPos signPos = signTools.findNearestSignPos(world, player.getBlockPos(), 50);
            if (signPos != null) {
                serverWorld.getServer().execute(() -> {
                    move(signPos.getX(), signPos.getY(), signPos.getZ());
                });
                return "Moving to nearest sign with coordinates: " + signPos;
            }
            return "No sign found nearby";
        }

        return "Could not access server world";
    }
}
