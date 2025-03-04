package mors.museumguide.logic;

import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

public class followPlayer extends Goal {
    private final MobEntity entity;
    private final EntityNavigation navigation;
    private final double speed;
    private final ServerPlayerEntity targetEntity;

    public followPlayer(ServerPlayerEntity player, MobEntity entity, double speed) {
        super();
        this.entity = entity;
        this.speed = speed;
        this.navigation = entity.getNavigation();
        this.targetEntity = player;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        // Start only if the target player is more than 8 blocks away
        return this.entity.squaredDistanceTo(this.targetEntity) > 64;
    }

    @Override
    public boolean shouldContinue() {
        // Continue unless the entity gets within 3 blocks of the player
        return this.entity.squaredDistanceTo(this.targetEntity) > 9;
    }

    @Override
    public void stop() {
        // Stop the entity temporarily
        this.navigation.stop();
    }

    @Override
    public void tick() {
        if (this.entity instanceof EndermanEntity || this.entity instanceof EndermiteEntity || this.entity instanceof ShulkerEntity) {
            // Certain entities should teleport to the player if they get too far
            if (this.entity.squaredDistanceTo(this.targetEntity) > 256) {
                Vec3d targetPos = findTeleportPosition(12);
                if (targetPos != null) {
                    this.entity.refreshPositionAndAngles(targetPos.x, targetPos.y, targetPos.z, this.entity.getYaw(), this.entity.getPitch());
                }
            }
        } else {
            // Look at the player and start moving towards them
            this.entity.getLookControl().lookAt(this.targetEntity, 10.0F, this.entity.getMaxLookPitchChange());
            this.navigation.startMovingTo(this.targetEntity, this.speed);
        }
    }

    private Vec3d findTeleportPosition(int distance) {
        if (this.entity instanceof PathAwareEntity) {
            return FuzzyTargeting.findTo((PathAwareEntity) this.entity, distance, distance, this.targetEntity.getPos());
        }
        return null;
    }
}