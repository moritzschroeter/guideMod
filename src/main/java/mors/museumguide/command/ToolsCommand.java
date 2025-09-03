package mors.museumguide.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import mors.museumguide.entity.guideEntity;
import mors.museumguide.llm.initLLM;
import mors.museumguide.llm.ollamaHandler;
import mors.museumguide.logic.guideInteractionTracker;
import mors.museumguide.prompts.promptsTypology;
import mors.museumguide.tools.ApiTools;
import mors.museumguide.tools.functionTools;
import mors.museumguide.tools.guideTools;
import mors.museumguide.tools.signTools;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.IOException;

import static mors.museumguide.tools.guideTools.move;

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
                                            signTools sign = new signTools();
                                            functionTools.setLastInteraction(player);

                                            String result = sign.findNearestSignToPlayer();
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
                                            functionTools.setLastInteraction(player);

                                            int x = player.getBlockPos().getX();
                                            int y = player.getBlockPos().getY();
                                            int z = player.getBlockPos().getZ();

                                            String result = signTools.getNearestSignText(x, y, z, radius);
                                            source.sendFeedback(() -> Text.literal("Sign text: " + result), false);
                                            return 1;
                                        })
                                )
                        )
                        .then(CommandManager.literal("getSignTextPos")
                                .then(CommandManager.argument("x", IntegerArgumentType.integer())
                                        .then(CommandManager.argument("y", IntegerArgumentType.integer())
                                                .then(CommandManager.argument("z", IntegerArgumentType.integer())
                                                        .executes(context -> {
                                                            ServerCommandSource source = context.getSource();
                                                            ServerPlayerEntity player = source.getPlayerOrThrow();
                                                            int x = IntegerArgumentType.getInteger(context, "x");
                                                            int y = IntegerArgumentType.getInteger(context, "y");
                                                            int z = IntegerArgumentType.getInteger(context, "z");

                                                            signTools sign = new signTools();
                                                            sign.setLastInteraction(player);

                                                            String signText = signTools.getTextPos(x, y, z);
                                                            source.sendFeedback(() -> Text.literal(signText), false);
                                                            return 1;
                                                        })
                                                )
                                        )
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

                                            String result = signTools.findNearestSignToPlayer();
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
                        .then(CommandManager.literal("move")
                                .then(CommandManager.argument("x", IntegerArgumentType.integer())
                                        .then(CommandManager.argument("y", IntegerArgumentType.integer())
                                                .then(CommandManager.argument("z", IntegerArgumentType.integer())
                                                        .executes(context -> {
                                                            ServerCommandSource source = context.getSource();
                                                            ServerPlayerEntity player = source.getPlayerOrThrow();

                                                            functionTools tools = new functionTools();
                                                            tools.setLastInteraction(player);
                                                            int x = IntegerArgumentType.getInteger(context, "x");
                                                            int y = IntegerArgumentType.getInteger(context, "y");
                                                            int z = IntegerArgumentType.getInteger(context, "z");

                                                            // Find nearby guide entity or spawn one if none exists
                                                            guideEntity guide = guideInteractionTracker.getLastInteractedGuide(player);
                                                            if (guide != null) {
                                                                move(x, y , z);
                                                                source.sendFeedback(() -> Text.literal("The guide is now moving to" + x + ", " + y + ", " + z), false);
                                                            } else {
                                                                source.sendFeedback(() -> Text.literal("Failed to find or spawn a guide entity."), false);
                                                            }

                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(CommandManager.literal("moveToNearestSign")
                                .executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    ServerPlayerEntity player = source.getPlayerOrThrow();
                                    guideTools gTools = new guideTools();

                                    functionTools tools = new functionTools();
                                    tools.setLastInteraction(player);

                                    guideEntity guide = guideInteractionTracker.getLastInteractedGuide(player);
                                    if (guide != null) {
                                        guideTools.moveToNearestSign();
                                        source.sendFeedback(() -> Text.literal("Moving guide to nearest sign"), false);
                                    }
                                    return 0;
                                }))
                        .then(CommandManager.literal("searchArtwork")
                                .then(CommandManager.argument("query", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            ServerCommandSource source = context.getSource();
                                            ServerPlayerEntity player = source.getPlayerOrThrow();
                                            String query = StringArgumentType.getString(context, "query");

                                            functionTools.setLastInteraction(player);

                                            ApiTools apiTools = new ApiTools();
                                            ApiTools.ArtworkInfo artworkInfo = apiTools.getArtwork(query);

                                            if (artworkInfo != null) {
                                                source.sendFeedback(() -> Text.literal("Ergebnis der Kunstwerksuche:"), false);
                                                source.sendFeedback(() -> Text.literal(artworkInfo.toString()), false);
                                            } else {
                                                source.sendFeedback(() -> Text.literal("Keine Kunstwerke für \"" + query + "\" gefunden."), false);
                                            }

                                            return 1;
                                        })
                                )
                        )
                        .then(CommandManager.literal("setPromptLevel")
                                .then(CommandManager.argument("level", IntegerArgumentType.integer(1, 3))
                                        .executes(context -> {
                                            ServerCommandSource source = context.getSource();
                                            ServerPlayerEntity player = source.getPlayerOrThrow();
                                            int level = IntegerArgumentType.getInteger(context, "level");

                                            // Alten Prompt für Vergleich speichern
                                            String oldPrompt = promptsTypology.getCurrentPrompt().substring(0, Math.min(20, promptsTypology.getCurrentPrompt().length())) + "...";

                                            // Prompt-Level setzen
                                            promptsTypology.setPromptLevel(level);
                                            System.out.println("Prompt set level to " + level);

                                            // LLM neu initialisieren
                                            ollamaHandler handler = new ollamaHandler();
                                            handler.resetModel();

                                            // Neuen Prompt zur Verifikation ausgeben
                                            String newPrompt = promptsTypology.getCurrentPrompt().substring(0, Math.min(20, promptsTypology.getCurrentPrompt().length())) + "...";
                                            System.out.println("Alter Prompt: " + oldPrompt);
                                            System.out.println("Neuer Prompt: " + newPrompt);

                                            source.sendFeedback(() -> Text.literal("Prompt-Level auf " + level + " gesetzt und LLM neu initialisiert"), false);
                                            source.sendFeedback(() -> Text.literal("Alt: " + oldPrompt + " → Neu: " + newPrompt), false);
                                            return 1;
                                        })
                                )
                        )
                        .then(CommandManager.literal("printPrompt")
                                .executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    ServerPlayerEntity player = source.getPlayerOrThrow();
                                    source.sendFeedback(() -> Text.literal("Printing prompt: " + promptsTypology.getCurrentPrompt()), false);
                                    return 0;
                                }))
        );
        dispatcher.register(
                CommandManager.literal("llm")
                        .then(CommandManager.literal("getModels")
                                .executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    ServerPlayerEntity player = source.getPlayerOrThrow();

                                    ollamaHandler handler = new ollamaHandler();
                                    try {
                                        handler.getModels();
                                    } catch (IOException | InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    source.sendFeedback(() -> Text.literal("executed"), false);
                                    return 1;
                                })
                        )
                        .then(CommandManager.literal("modelList")
                                .then(CommandManager.argument("name", StringArgumentType.greedyString()))
                                .executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    ServerPlayerEntity player = source.getPlayerOrThrow();

                                    ollamaHandler handler = new ollamaHandler();
                                    try {
                                        handler.modelList();
                                    } catch (IOException | InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    source.sendFeedback(() -> Text.literal("Model list fetched"), false);
                                    return 1;
                                })
                        )
                        .then(CommandManager.literal("setModelName")
                                .then(CommandManager.argument("modelName", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> {
                                            String[] models = initLLM.getModelNames();
                                            if (models != null) {
                                                for (String model : models) {
                                                    builder.suggest(model);
                                                }
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            ServerCommandSource source = context.getSource();
                                            ServerPlayerEntity player = source.getPlayerOrThrow();
                                            String modelName = StringArgumentType.getString(context, "modelName");

                                            ollamaHandler handler = new ollamaHandler();
                                            try {
                                                handler.setModelName(modelName);
                                            } catch (IOException | InterruptedException e) {
                                                throw new RuntimeException(e);
                                            }
                                            source.sendFeedback(() -> Text.literal("Model set to " + modelName), false);
                                            return 1;
                                        })
                                )
                        )
                        .then(CommandManager.literal("printModel")
                                .executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    ServerPlayerEntity player = source.getPlayerOrThrow();

                                    ollamaHandler handler = new ollamaHandler();

                                    handler.printModel();

                                    return 1;
                                }))
                        .then(CommandManager.literal("resetModel")
                                .executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    ServerPlayerEntity player = source.getPlayerOrThrow();

                                    ollamaHandler handler = new ollamaHandler();
                                    handler.resetModel();
                                    return 1;
                                }))
        );
    }
}

