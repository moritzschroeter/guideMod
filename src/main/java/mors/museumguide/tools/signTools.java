package mors.museumguide.tools;

import dev.langchain4j.agent.tool.Tool;
import mors.museumguide.logic.guideInteractionTracker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class signTools {
    private static ServerPlayerEntity lastInteractedPlayer = guideInteractionTracker.getPlayer();

    public static void setLastInteraction(ServerPlayerEntity player) {
        lastInteractedPlayer = player;
    }

    private static World getPlayerWorld() {
        if (lastInteractedPlayer != null) {
            return lastInteractedPlayer.getWorld();
        }
        return null;
    }

    @Tool("Find the nearest sign to the player")
    public static String findNearestSignToPlayer() {
        System.out.println("Find the nearest sign to the player");
        if (lastInteractedPlayer == null) {
            System.out.println("No interacted player available");
            return "No player available";
        }

        BlockPos playerPos = lastInteractedPlayer.getBlockPos();
        return findNearestSign(playerPos);
    }

    public static String findNearestSign(BlockPos pos) {
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

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos currentPos = origin.add(dx, dy, dz);
                    blocksChecked++;

                    if (!world.isChunkLoaded(currentPos)) {
                        continue;
                    }

                    BlockState blockState = world.getBlockState(currentPos);
                    Block block = blockState.getBlock();

                    boolean isSign = block instanceof SignBlock || blockState.isIn(BlockTags.SIGNS);

                    if (isSign) {
                        signsFound++;
                        double distSq = currentPos.getSquaredDistance(origin);
                        if (distSq < closestDistanceSq) {
                            closestDistanceSq = distSq;
                            nearestSign = currentPos;

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

    private static String extractSimpleText(SignBlockEntity sign) {
        System.out.println("extractSimpleText() called");
        if (sign == null) return "";

        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            Text lineText = sign.getFrontText().getMessage(i, false);
            String line = lineText.getString().trim();
            System.out.println("Line " + i + ": " + line);
            if (!line.isEmpty()) {
                if (text.length() > 0) text.append(" | ");
                text.append(line);
            }
        }
        return text.toString();
    }

    public static String getNearestSignText(int x, int y, int z, int radius) {
        System.out.println("getNearestSignText called");

        World world = getPlayerWorld();
        if (world == null) return "No world available";

        BlockPos origin = new BlockPos(x, y, z);
        BlockPos nearestSign = findNearestSignPos(world, origin, radius);

        if (nearestSign == null) {
            return "No sign found";
        }

        BlockEntity blockEntity = world.getBlockEntity(nearestSign);
        if (blockEntity instanceof SignBlockEntity) {
            String signText = extractSimpleText((SignBlockEntity) blockEntity);
            System.out.println(signText);
            return signText;
        }

        return "";
    }

    static BlockPos findNearestSignPos(World world, BlockPos origin, int radius) {
        BlockPos nearestSign = null;
        double closestDistanceSq = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos currentPos = origin.add(dx, dy, dz);

                    if (!world.isChunkLoaded(currentPos)) {
                        continue;
                    }

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

    @Tool("Get the text of the nearest sign to the player")
    public static String signWrapper()  {
        System.out.println("signWrapper() was called");
        lastInteractedPlayer = guideInteractionTracker.getPlayer();
        if (lastInteractedPlayer == null) {
            System.out.println("No interacted player available");
            return "No interacted player available";
        }
        BlockPos playerpos = lastInteractedPlayer.getBlockPos();
        World world = lastInteractedPlayer.getWorld();
        if (world instanceof ServerWorld serverWorld) {
            BlockPos signPos = findNearestSignPos(serverWorld, playerpos, 50);
            CompletableFuture<String> future = new CompletableFuture<>();
            serverWorld.getServer().execute(() -> {
                String signText = getTextPos(signPos.getX(), signPos.getY(), signPos.getZ());
                future.complete(signText);
            });
            try {
                return future.get(15, TimeUnit.SECONDS);
            } catch (Exception e) {
                return "Error getting sign text: " + e.getMessage();
            }
        }
        return "Could not access server world";
    }

    public static String getTextPos(int x, int y, int z) {
        lastInteractedPlayer = guideInteractionTracker.getPlayer();
        System.out.println("getTextPos() called");
        BlockPos signPos = new BlockPos(x, y, z);
        if (lastInteractedPlayer == null) {
            System.out.println("No interacted player available");
            return "No interacted player available";
        }
        World world = lastInteractedPlayer.getWorld();
        if (world instanceof ServerWorld serverWorld) {
            return extractSimpleText((SignBlockEntity) serverWorld.getBlockEntity(signPos));
        }
        return "No sign at position";
    }
}