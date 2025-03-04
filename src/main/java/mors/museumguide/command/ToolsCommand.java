package mors.museumguide.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import mors.museumguide.entity.guideEntity;
import mors.museumguide.logic.guideInteractionTracker;
import mors.museumguide.tools.functionTools;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ToolsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess access,
                                CommandManager.RegistrationEnvironment environment) {

        dispatcher.register(
                CommandManager.literal("guide")
                        .then(CommandManager.literal("findNearestSign")
                                .then(CommandManager.argument("radius", IntegerArgumentType.integer(1, 50))
                                        .executes(context -> {
                                            ServerCommandSource source = context.getSource();
                                            ServerPlayerEntity player = source.getPlayerOrThrow();
                                            int radius = IntegerArgumentType.getInteger(context, "radius");

                                            functionTools tools = new functionTools();
                                            tools.setLastInteraction(player);

                                            String result = tools.findNearestSignToPlayer();
                                            source.sendFeedback(() -> Text.literal(result), false);
                                            return 1;
                                        })
                                )
                        )
                        .then(CommandManager.literal("getSignText")
                                .then(CommandManager.argument("radius", IntegerArgumentType.integer(1, 50))
                                        .executes(context -> {
                                            ServerCommandSource source = context.getSource();
                                            ServerPlayerEntity player = source.getPlayerOrThrow();
                                            int radius = IntegerArgumentType.getInteger(context, "radius");

                                            functionTools tools = new functionTools();
                                            tools.setLastInteraction(player);

                                            int x = player.getBlockPos().getX();
                                            int y = player.getBlockPos().getY();
                                            int z = player.getBlockPos().getZ();

                                            String result = tools.getNearestSignText(x, y, z, radius);
                                            source.sendFeedback(() -> Text.literal("Sign text: " + result), false);
                                            return 1;
                                        })
                                )
                        )
                        .then(CommandManager.literal("nearestSignPlayer")
                                .then(CommandManager.argument("radius", IntegerArgumentType.integer(1, 50))
                                        .executes(context -> {
                                            ServerCommandSource source = context.getSource();
                                            ServerPlayerEntity player = source.getPlayerOrThrow();
                                            int radius = IntegerArgumentType.getInteger(context, "radius");

                                            functionTools tools = new functionTools();
                                            tools.setLastInteraction(player);

                                            String result = tools.findNearestSignToPlayer();
                                            source.sendFeedback(() -> Text.literal(result), false);
                                            return 1;
                                        })
                                )
                        )
                        .then(CommandManager.literal("testFunction")
                                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            ServerCommandSource source = context.getSource();
                                            String message = StringArgumentType.getString(context, "message");

                                            functionTools tools = new functionTools();
                                            String result = tools.testFunctionCalling(message);
                                            source.sendFeedback(() -> Text.literal(result), false);
                                            return 1;
                                        })
                                )
                        )
                        .then(CommandManager.literal("follow")
                                .executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    ServerPlayerEntity player = source.getPlayerOrThrow();

                                    functionTools tools = new functionTools();
                                    tools.setLastInteraction(player);

                                    // Find nearby guide entity or spawn one if none exists
                                    guideEntity guide = guideInteractionTracker.getLastInteractedGuide(player);
                                    if (guide != null) {
                                        guide.setFollowPlayer(player);
                                        source.sendFeedback(() -> Text.literal("The guide is now following you!"), false);
                                    } else {
                                        source.sendFeedback(() -> Text.literal("Failed to find or spawn a guide entity."), false);
                                    }

                                    return 1;
                                })
                        )
        );
    }
}