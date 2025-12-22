package me.xarta.xsystemmsgeditor.event;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.xarta.xsystemmsgeditor.config.ConfigHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.CommandEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class CommandsHandler {

    @SubscribeEvent
    public void onCommandEvent(CommandEvent event) {
        final ParseResults<CommandSourceStack> parse = event.getParseResults();
        final CommandContextBuilder<CommandSourceStack> ctx = parse.getContext();
        final CommandSourceStack source = ctx.getSource();
        if (!source.isPlayer()) return;

        final String input = parse.getReader().getString();
        final String[] tokens = tokenize(input);
        if (tokens.length == 0) return;

        final CommandNode<CommandSourceStack> root = ctx.getRootNode();

        final TraversalResult tr = traverseLiterals(root, source, tokens);

        final String matchedPrefix = joinPrefix(tokens, tr.matchedCount());

        if (ctx.getNodes().isEmpty()) {
            if (tr.deniedIndex() >= 0) {
                final String deniedPrefix = joinPrefix(tokens, tr.deniedIndex() + 1);
                handleNoPermission(source, event, deniedPrefix);
                return;
            }
            if (tr.matchedCount() == 0) {
                handleUnknown(input, source, event);
                return;
            }
            handleSyntaxErrorWithAnchor(source, event, matchedPrefix, tr.lastMatchedLiteral(), ctx);
            return;
        }

        final boolean hasExceptions = hasParsingExceptions(parse);
        final boolean hasTrailing = parse.getReader().canRead();
        final boolean notExecutable = (ctx.getCommand() == null);
        if (hasExceptions || hasTrailing || notExecutable) {
            final LiteralCommandNode<CommandSourceStack> anchor =
                    (tr.lastMatchedLiteral() != null) ? tr.lastMatchedLiteral() : findLiteral(root, tokens[0]);
            handleSyntaxErrorWithAnchor(source, event, matchedPrefix, anchor, ctx);
        }
    }

    private record TraversalResult(
            LiteralCommandNode<CommandSourceStack> lastMatchedLiteral,
            int matchedCount,
            int deniedIndex
    ) {}

    private static TraversalResult traverseLiterals(CommandNode<CommandSourceStack> root,
                                                    CommandSourceStack source,
                                                    String[] tokens) {
        CommandNode<CommandSourceStack> current = root;
        LiteralCommandNode<CommandSourceStack> lastMatched = null;
        int matched = 0;
        int deniedAt = -1;

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];

            LiteralCommandNode<CommandSourceStack> lit = findLiteral(current, token);
            if (lit == null) {
                break;
            }

            if (!lit.getRequirement().test(source)) {
                deniedAt = i;
                break;
            }

            matched++;
            lastMatched = lit;

            CommandNode<CommandSourceStack> redirectTarget = lit.getRedirect();
            current = (redirectTarget != null) ? redirectTarget : lit;
        }

        return new TraversalResult(lastMatched, matched, deniedAt);
    }

    private static void handleUnknown(String input,
                                      CommandSourceStack source,
                                      CommandEvent event) {
        final String raw = ConfigHandler.UNKNOWN_COMMAND.get();
        if (raw.isBlank()) return;

        final boolean addSlash = ConfigHandler.UNKNOWN_COMMAND_ADD_SLASH.get();
        final String first = extractFirstToken(input);
        final String text = formatWithCommand(raw, first, addSlash);
        source.sendSystemMessage(Component.literal(text));
        event.setCanceled(true);
    }

    private static void handleNoPermission(CommandSourceStack source,
                                           CommandEvent event,
                                           String commandPrefix) {
        final String raw = ConfigHandler.NO_PERMISSION.get();
        if (raw.isBlank()) return;

        final boolean addSlash = ConfigHandler.NO_PERMISSION_ADD_SLASH.get();
        final String text = formatWithCommand(raw, commandPrefix, addSlash);
        source.sendSystemMessage(Component.literal(text));
        event.setCanceled(true);
    }

    private static void handleSyntaxErrorWithAnchor(CommandSourceStack source,
                                                    CommandEvent event,
                                                    String commandPrefix,
                                                    LiteralCommandNode<CommandSourceStack> anchor,
                                                    CommandContextBuilder<CommandSourceStack> ctx) {
        final String raw = ConfigHandler.SYNTAX_ERROR.get();
        if (raw.isBlank()) return;

        String usageTail = "";
        if (anchor != null) {
            final Collection<String> tails = ctx.getDispatcher().getSmartUsage(anchor, source).values();
            if (!tails.isEmpty()) {
                var it = tails.iterator();
                usageTail = it.hasNext() ? it.next() : "";
            }
        }

        String usage = commandPrefix;
        if (!usageTail.isBlank()) {
            usage = usage + " " + usageTail;
        }

        final boolean addSlashToSyntax = ConfigHandler.SYNTAX_ADD_SLASH.get();
        if (addSlashToSyntax && !usage.startsWith("/")) {
            usage = "/" + usage;
        }

        String text = raw.replace("%syntax%", usage)
                .replace("%command%", commandPrefix);
        text = colorize(text);

        source.sendSystemMessage(Component.literal(text));
        event.setCanceled(true);
    }

    private static boolean hasParsingExceptions(ParseResults<CommandSourceStack> parse) {
        final Map<CommandNode<CommandSourceStack>, com.mojang.brigadier.exceptions.CommandSyntaxException> ex = parse.getExceptions();
        return ex != null && !ex.isEmpty();
    }

    private static String[] tokenize(String input) {
        if (input == null) return new String[0];
        String s = input.startsWith("/") ? input.substring(1) : input;
        s = s.trim();
        if (s.isEmpty()) return new String[0];
        return s.split("\\s+");
    }

    private static String extractFirstToken(String input) {
        String[] t = tokenize(input);
        return (t.length == 0) ? "" : t[0];
    }

    private static String joinPrefix(String[] tokens, int count) {
        int n = Math.min(count, tokens.length);
        if (n <= 0) return (tokens.length > 0 ? tokens[0] : "");
        return String.join(" ", Arrays.copyOfRange(tokens, 0, n));
    }

    @SuppressWarnings("unchecked")
    private static LiteralCommandNode<CommandSourceStack> findLiteral(CommandNode<CommandSourceStack> parent, String name) {
        for (CommandNode<CommandSourceStack> child : parent.getChildren()) {
            if (child instanceof LiteralCommandNode<?> lit && name.equals(child.getName())) {
                return (LiteralCommandNode<CommandSourceStack>) lit;
            }
        }
        return null;
    }

    private static String formatWithCommand(String raw, String commandTokenOrPrefix, boolean addSlash) {
        final String token = addSlash ? "/" + commandTokenOrPrefix : commandTokenOrPrefix;
        String s = raw.replace("%command%", token);
        return colorize(s);
    }

    private static String colorize(String s) {
        if (s.contains("&amp;")) s = s.replace("&amp;", "§");
        if (s.indexOf('&') >= 0) s = s.replace("&", "§");
        return s;
    }
}
