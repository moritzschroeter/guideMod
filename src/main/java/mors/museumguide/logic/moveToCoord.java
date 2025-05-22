package mors.museumguide.logic;

import mors.museumguide.entity.guideEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class moveToCoord {
    private final World world;

    public moveToCoord(guideEntity guide, World world) {
        this.world = world;
    }

    public String moveTo(BlockPos pos, guideEntity guide) {
        if (guide == null) {
            System.out.println("No guide to move");
            return "No guide to move";
        }
        guide.setMoving(true);

        BlockPos guidePos = guide.getBlockPos();
        System.out.println("Guide position: " + guidePos);
        System.out.println("Target position: " + pos);

        double distance = Math.sqrt(pos.getSquaredDistance(guidePos));
        System.out.println("Distance to target: " + distance);

        // Check if guide has navigation capability
        if (guide.getNavigation() == null) {
            System.out.println("Navigation controller is null");
            guide.setMoving(false);
            return "Guide navigation system not available";
        }
        // Use the entity's built-in navigation with a higher priority
        boolean navigationStarted = guide.getNavigation().startMovingTo(pos.getX() - 1 , pos.getY(), pos.getZ() - 1, 1.0);
        System.out.println("Navigation started: " + navigationStarted);


        return "Moving guide to " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

}

