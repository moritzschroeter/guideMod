package mors.museumguide.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;

public class guideEntity extends PathAwareEntity {


    public guideEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createCubeAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 5)
                .add(EntityAttributes.TEMPT_RANGE, 10)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.3);
    }
    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new WanderAroundGoal(this, 1));
    }
}
