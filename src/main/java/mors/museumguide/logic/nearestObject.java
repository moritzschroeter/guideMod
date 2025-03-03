package mors.museumguide.logic;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class nearestObject {

    /**
     * Finds the nearest sign block entity to the given entity within the search radius
     * @param entity The entity to search from
     * @param radius The search radius
     * @return Optional containing the nearest SignBlockEntity if found
     */
    public static Optional<SignBlockEntity> findNearestSign(Entity entity, int radius) {
        World world = entity.getWorld();
        BlockPos entityPos = entity.getBlockPos();

        List<SignBlockEntity> signs = new ArrayList<>();

        // Search in a cubic area around the entity
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = entityPos.add(x, y, z);
                    BlockEntity blockEntity = world.getBlockEntity(pos);

                    if (blockEntity instanceof SignBlockEntity) {
                        signs.add((SignBlockEntity) blockEntity);
                    }
                }
            }
        }

        // Find the closest sign by distance
        return signs.stream()
                .min(Comparator.comparingDouble(sign ->
                        sign.getPos().getSquaredDistance(entityPos)));
    }

    /**
     * Generic method to find the nearest block entity of a specific type
     * @param entity The entity to search from
     * @param radius The search radius
     * @param blockEntityClass The class of block entity to search for
     * @param <T> The type of block entity
     * @return Optional containing the nearest block entity if found
     */
    public static <T extends BlockEntity> Optional<T> findNearestBlockEntity(
            Entity entity, int radius, Class<T> blockEntityClass) {
        World world = entity.getWorld();
        BlockPos entityPos = entity.getBlockPos();

        List<T> blockEntities = new ArrayList<>();

        // Search in a cubic area around the entity
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = entityPos.add(x, y, z);
                    BlockEntity blockEntity = world.getBlockEntity(pos);

                    if (blockEntityClass.isInstance(blockEntity)) {
                        blockEntities.add(blockEntityClass.cast(blockEntity));
                    }
                }
            }
        }

        // Find the closest block entity by distance
        return blockEntities.stream()
                .min(Comparator.comparingDouble(be ->
                        be.getPos().getSquaredDistance(entityPos)));
    }

    /**
     * Find nearby entities of a specific type within a box around the source entity
     * @param sourceEntity The entity to search from
     * @param radius The search radius
     * @param entityClass The class of entities to search for
     * @param <T> The type of entity
     * @return The nearest entity if found
     */
    public static <T extends Entity> Optional<T> findNearestEntity(
            Entity sourceEntity, double radius, Class<T> entityClass) {
        World world = sourceEntity.getWorld();
        Box box = new Box(
                sourceEntity.getX() - radius,
                sourceEntity.getY() - radius,
                sourceEntity.getZ() - radius,
                sourceEntity.getX() + radius,
                sourceEntity.getY() + radius,
                sourceEntity.getZ() + radius);

        List<T> nearbyEntities = world.getEntitiesByClass(entityClass, box, entity -> entity != sourceEntity);

        return nearbyEntities.stream()
                .min(Comparator.comparingDouble(
                        entity -> entity.squaredDistanceTo(sourceEntity)));
    }
}