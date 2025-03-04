package mors.museumguide.entity;

import mors.museumguide.client.MuseumGuideClient;
import mors.museumguide.llm.initLLM;
import mors.museumguide.logic.followPlayer;
import mors.museumguide.logic.guideInteractionTracker;
import mors.museumguide.tools.functionTools;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
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
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            guideInteractionTracker.trackInteraction(serverPlayer, this);
            functionTools.setLastInteraction(serverPlayer); // Store the last interacted player
        }

        if (!player.getWorld().isClient && hand == Hand.MAIN_HAND) {
            // Track this interaction
            if (player instanceof ServerPlayerEntity serverPlayer) {
                guideInteractionTracker.trackInteraction(serverPlayer, this);
            }
            player.sendMessage(Text.literal("Ask me anything!"), false);
        }

        if (player.getWorld().isClient && hand == Hand.MAIN_HAND) {
            net.minecraft.client.MinecraftClient.getInstance().setScreen(
                    new net.minecraft.client.gui.screen.ChatScreen("@GuideBot ")
            );
        }

        return ActionResult.SUCCESS;
    }
    public void setFollowPlayer(ServerPlayerEntity player) {
        // Remove any existing follow goals first
        this.goalSelector.getGoals().stream()
                .filter(prioritizedGoal -> prioritizedGoal.getGoal() instanceof followPlayer)
                .forEach(prioritizedGoal -> this.goalSelector.remove(prioritizedGoal.getGoal()));

        // Add the new follow goal
        followPlayer followGoal = new followPlayer(player, this, 1.0);
        this.goalSelector.add(1, followGoal);
    }
    public guideEntity getGuide()   {
        return this;
    }
    public void removeFollowPlayer(ServerPlayerEntity player) {
        this.goalSelector.getGoals().stream()
                .filter(prioritizedGoal -> prioritizedGoal.getGoal() instanceof followPlayer)
                .forEach(prioritizedGoal -> this.goalSelector.remove(prioritizedGoal.getGoal()));
    }

}