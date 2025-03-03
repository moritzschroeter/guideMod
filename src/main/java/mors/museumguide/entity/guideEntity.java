package mors.museumguide.entity;

import mors.museumguide.client.MuseumGuideClient;
import mors.museumguide.llm.initLLM;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
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

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!player.getWorld().isClient && hand == Hand.MAIN_HAND) {
            player.sendMessage(Text.literal("Ask me anything!"), false);
        }

        if (player.getWorld().isClient && hand == Hand.MAIN_HAND) {
            net.minecraft.client.MinecraftClient.getInstance().setScreen(
                    new net.minecraft.client.gui.screen.ChatScreen("@GuideBot ")
            );
        }

        return ActionResult.SUCCESS;
    }
}