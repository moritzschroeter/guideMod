/*
package mors.museumguide.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import mors.museumguide.tools.functionTools;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class findSignCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("findsign")
                .executes(findSignCommand::execute));
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        try {
            ServerCommandSource source = context.getSource();
            if (source.getPlayer() == null) {
                source.sendFeedback(() -> Text.literal("This command must be executed by a player"), false);
                return 0;
            }

            // Set the executing player as the last interacted player
            functionTools.setLastInteraction(source.getPlayer());

            // Create an instance of functionTools and call findNearestSign
            functionTools tools = new functionTools();
            String result = tools.findNearestSign();

            // Send the result to the player
            source.sendFeedback(() -> Text.literal(result), false);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFeedback(() -> Text.literal("Error: " + e.getMessage()), false);
            return 0;
        }
    }
}
*/
