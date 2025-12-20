
package me.xarta.xsystemmsgeditor.event;

import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.xarta.xsystemmsgeditor.config.ConfigHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.CommandEvent;

public class CommandsHandler {

    @SubscribeEvent
    public void onCommandEvent(CommandEvent event) {
        CommandContextBuilder<CommandSourceStack> ctx = event.getParseResults().getContext();
        CommandSourceStack source = ctx.getSource();

        if (!source.isPlayer()) return; // ignore console and command blocks

        if (!ctx.getNodes().isEmpty()) return; // do nothing if command is valid

        String input = event.getParseResults().getReader().getString();
        String firstToken = extractFirstToken(input);
        if (firstToken.isEmpty()) return;

        CommandNode<CommandSourceStack> root = ctx.getRootNode();
        LiteralCommandNode<CommandSourceStack> literal = findLiteral(root, firstToken);

        /*
        No permission logic
         */
        if (literal != null) {
            boolean allowed = literal.getRequirement().test(source);
            if (!allowed) {
                String rawPerm = ConfigHandler.NO_PERMISSION.get();
                boolean addSlash = ConfigHandler.NO_PERMISSION_ADD_SLASH.get();
                if (!rawPerm.isBlank()) {
                    source.sendSystemMessage(Component.literal(applyFormatting(rawPerm, firstToken, addSlash)));
                    event.setCanceled(true);
                }
                return;
            }
        }

        /*
        Unknown command logic
         */
        String rawUnknown = ConfigHandler.UNKNOWN_COMMAND.get();
        if (!rawUnknown.isBlank()) {
            boolean addSlash = ConfigHandler.UNKNOWN_COMMAND_ADD_SLASH.get();
            source.sendSystemMessage(Component.literal(applyFormatting(rawUnknown, firstToken, addSlash)));
            event.setCanceled(true);
        }
    }

    private static String extractFirstToken(String input) {
        if (input == null) return "";
        String s = input.startsWith("/") ? input.substring(1) : input;
        s = s.trim();
        if (s.isEmpty()) return "";
        int space = s.indexOf(' ');
        return space >= 0 ? s.substring(0, space) : s;
    }

    @SuppressWarnings("unchecked")
    private static LiteralCommandNode<CommandSourceStack> findLiteral(CommandNode<CommandSourceStack> root, String name) {
        for (CommandNode<CommandSourceStack> child : root.getChildren()) {
            if (child instanceof LiteralCommandNode<?> lit && name.equals(child.getName())) {
                return (LiteralCommandNode<CommandSourceStack>) lit;
            }
        }
        return null;
    }

    /*
    Method, that replaces bukkit style ampersands into vanilla style paragraph symbols
     */
    private static String applyFormatting(String raw, String commandToken, boolean addSlash) {
        String token = addSlash ? "/" + commandToken : commandToken;
        String withPlaceholders = raw.replace("%command%", token);
        return withPlaceholders.replace("&", "§");
    }
}
