package mors.museumguide.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import mors.museumguide.entity.guideEntity;
import mors.museumguide.logic.followPlayer;
import mors.museumguide.logic.guideInteractionTracker;
import mors.museumguide.logic.moveToCoord;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

import static mors.museumguide.logic.guideInteractionTracker.getLastInteractedGuide;

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


    @Tool("Find the nearest sign to the player")
    public String findNearestSignToPlayer() {
        System.out.println("Find the nearest sign to the player");
        if (lastInteractedPlayer == null) {
            System.out.println("No interacted player available");
            return "No player available";
        }

        BlockPos playerPos = lastInteractedPlayer.getBlockPos();
        return findNearestSign(playerPos);
    }

    //@Tool("Get the position of the nearest sign block entity to specified coordinates")
    public String findNearestSign(BlockPos pos) {
        World world = getPlayerWorld();
        if (world == null) {
            System.out.println("No interacted player available");
            return "No world available";
        }

        System.out.println("Searching for signs at origin: " + pos.getX() + "," + pos.getY() + "," + pos.getZ());

        BlockPos origin = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
        BlockPos nearestSign = null;
        double closestDistanceSq = Double.MAX_VALUE;
        int blocksChecked = 0;
        int signsFound = 0;
        int radius = 50;

        // Loop over all block positions within a cube of side 2*radius+1 around origin
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos currentPos = origin.add(dx, dy, dz);
                    blocksChecked++;

                    // Only check loaded chunks
                    if (!world.isChunkLoaded(currentPos)) {

                        continue;
                    }

                    BlockState blockState = world.getBlockState(currentPos);
                    Block block = blockState.getBlock();

                    // Check if the block is a sign
                    boolean isSign = block instanceof SignBlock || blockState.isIn(BlockTags.SIGNS);

                    if (isSign) {
                        signsFound++;
                        double distSq = currentPos.getSquaredDistance(origin);
                        if (distSq < closestDistanceSq) {
                            closestDistanceSq = distSq;
                            nearestSign = currentPos;

                            // Get sign text if possible
                            BlockEntity blockEntity = world.getBlockEntity(currentPos);
                            if (blockEntity instanceof SignBlockEntity) {
                                SignBlockEntity signEntity = (SignBlockEntity) blockEntity;
                                String signText = extractSimpleText(signEntity);
                                System.out.println("Found sign at " + currentPos + " with text: " + signText);
                            } else {
                                System.out.println("Found sign at " + currentPos + " (no text available)");
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Search complete. Checked " + blocksChecked + " blocks, found " + signsFound + " signs");

        if (nearestSign != null) {
            BlockEntity blockEntity = world.getBlockEntity(nearestSign);
            if (blockEntity instanceof SignBlockEntity) {
                SignBlockEntity signEntity = (SignBlockEntity) blockEntity;
                String signText = extractSimpleText(signEntity);

                return String.format("Found sign at %d,%d,%d with text: %s",
                        nearestSign.getX(), nearestSign.getY(), nearestSign.getZ(), signText);
            } else {
                return String.format("Found sign at %d,%d,%d (no text available)",
                        nearestSign.getX(), nearestSign.getY(), nearestSign.getZ());
            }
        } else {
            return "No sign found within " + radius + " blocks after checking " + blocksChecked + " positions";
        }
    }

    /**
     * Extracts text from a sign as a simple, LLM-friendly string
     **/
    private String extractSimpleText(SignBlockEntity sign) {
        if (sign == null) return "";

        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            Text lineText = sign.getFrontText().getMessage(i, false);
            String line = lineText.getString().trim();
            if (!line.isEmpty()) {
                if (text.length() > 0) text.append(" | ");
                text.append(line);
            }
        }
        return text.toString();
    }

    /**
     * Gets the raw text content from a sign for LLM processing
     */
    //@Tool("Get the text content from the nearest sign to specified coordinates")
    public String getNearestSignText(
            @P("X coordinate of the origin") int x,
            @P("Y coordinate of the origin") int y,
            @P("Z coordinate of the origin") int z,
            @P("Search radius in blocks") int radius) {

        World world = getPlayerWorld();
        if (world == null) return "No world available";

        BlockPos origin = new BlockPos(x, y, z);
        BlockPos nearestSign = findNearestSignPos(world, origin, radius);

        if (nearestSign == null) {
            return "";
        }

        BlockEntity blockEntity = world.getBlockEntity(nearestSign);
        if (blockEntity instanceof SignBlockEntity) {
            return extractSimpleText((SignBlockEntity) blockEntity);
        }

        return "";
    }

    /**
     * Helper method to find the nearest sign position
     */
    private BlockPos findNearestSignPos(World world, BlockPos origin, int radius) {
        BlockPos nearestSign = null;
        double closestDistanceSq = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos currentPos = origin.add(dx, dy, dz);

                    BlockState blockState = world.getBlockState(currentPos);
                    Block block = blockState.getBlock();

                    boolean isSign = block instanceof SignBlock || blockState.isIn(BlockTags.SIGNS);

                    if (isSign) {
                        double distSq = currentPos.getSquaredDistance(origin);
                        if (distSq < closestDistanceSq) {
                            closestDistanceSq = distSq;
                            nearestSign = currentPos;
                        }
                    }
                }
            }
        }

        return nearestSign;
    }

    //@Tool("Test if function calling is working properly")
    public String testFunctionCalling(@P("Test message") String message) {
        System.out.println("Function calling test received: " + message);
        return "Function calling is working! Received: " + message;
    }

    @Tool("Follow the player")
    public String followWrapper() {
        System.out.println("followWrapper() was called");

        if (lastInteractedPlayer == null) {
            System.out.println("No interacted player available");
            return "No player available to follow";
        }
        makeLastInteractedGuideFollow(lastInteractedPlayer);
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

        if (lastInteractedPlayer == null) {
            System.out.println("No interacted player available");
            return "No player available to stop following";
        }
        stopFollowing(lastInteractedPlayer);
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
    public String move(
            @P("x Coordinate") int x,
            @P("y Coordinate") int y,
            @P("z Coordinate") int z) {
        if (lastInteractedPlayer == null) {
            return "No player available";
        }

        World world = lastInteractedPlayer.getWorld();
        if (world instanceof ServerWorld serverWorld) {
            // Schedule the movement logic to run on the server thread
            serverWorld.getServer().execute(() -> {
                guideEntity guide = getLastInteractedGuide(lastInteractedPlayer);
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
    public String moveToNearestSign() {
        System.out.println("moveToNearestSign() was called");
        if (lastInteractedPlayer == null) {
            return "No interacted player available";
        }

        World world = lastInteractedPlayer.getWorld();
        if (world instanceof ServerWorld serverWorld) {
            BlockPos signPos = findNearestSignPos(world, lastInteractedPlayer.getBlockPos(), 50);
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