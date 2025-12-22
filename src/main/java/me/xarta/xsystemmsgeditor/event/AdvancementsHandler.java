
package me.xarta.xsystemmsgeditor.event;

import me.xarta.xsystemmsgeditor.config.ConfigHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;

public class AdvancementsHandler {

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        AdvancementHolder holder = event.getAdvancement();
        DisplayInfo display = holder.value().display().orElse(null);
        if (display == null) return;

        String format;
        boolean tooltipEnabled;
        switch (display.getType()) {
            case TASK -> {
                format = ConfigHandler.ADVANCEMENT_FORMAT.get();
                tooltipEnabled = ConfigHandler.ADVANCEMENT_TOOLTIP.get();
            }
            case GOAL -> {
                format = ConfigHandler.GOAL_FORMAT.get();
                tooltipEnabled = ConfigHandler.GOAL_TOOLTIP.get();
            }
            case CHALLENGE -> {
                format = ConfigHandler.CHALLENGE_FORMAT.get();
                tooltipEnabled = ConfigHandler.CHALLENGE_TOOLTIP.get();
            }
            default -> {
                format = "";
                tooltipEnabled = false;
            }
        }
        if (format.isBlank()) return;

        String playerName = player.getName().getString();
        Component title   = display.getTitle();
        Component desc    = display.getDescription();

        String headingCfg = ConfigHandler.TOOLTIP_HEADING_COLOR.get();
        String bodyCfg    = ConfigHandler.TOOLTIP_BODY_COLOR.get();

        Component msg = buildMessage(format, playerName, title, desc, tooltipEnabled, headingCfg, bodyCfg);

        for (ServerPlayer target : player.server.getPlayerList().getPlayers()) {
            target.sendSystemMessage(msg);
        }
    }

    private static Component buildMessage(
            String format,
            String playerName,
            Component advancementTitle,
            Component advancementDesc,
            boolean tooltipEnabled,
            String headingCfg,
            String bodyCfg
    ) {
        MutableComponent out = Component.empty();

        Style current = Style.EMPTY.withColor(ChatFormatting.WHITE);
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < format.length(); i++) {
            char ch = format.charAt(i);

            if (ch == '%' && i + 1 < format.length()) {
                int end = format.indexOf('%', i + 1);
                if (end != -1) {
                    flush(out, buf, current);

                    String key = format.substring(i + 1, end);
                    switch (key) {
                        case "player" -> appendLiteral(out, playerName, current);

                        case "advancement", "challenge", "goal" -> {
                            MutableComponent nameCopy = advancementTitle.copy().setStyle(current);

                            if (tooltipEnabled && advancementDesc != null) {
                                Style heading = parseHoverStyle(headingCfg, current, Style.EMPTY.withColor(ChatFormatting.WHITE));
                                Style body    = parseHoverStyle(bodyCfg,    current, Style.EMPTY.withColor(ChatFormatting.GRAY));

                                MutableComponent hover = Component.empty()
                                        .append(advancementTitle.copy().setStyle(heading))
                                        .append(Component.literal("\n"))
                                        .append(advancementDesc.copy().setStyle(body));

                                nameCopy = nameCopy.setStyle(
                                        nameCopy.getStyle().withHoverEvent(
                                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)
                                        )
                                );
                            }

                            out.append(nameCopy);
                        }

                        default -> appendLiteral(out, "%" + key + "%", current);
                    }

                    i = end;
                    continue;
                }
            }

            if (ch == '&' && i + 1 < format.length()) {
                flush(out, buf, current);

                char code = Character.toLowerCase(format.charAt(i + 1));
                switch (code) {
                    case '0' -> current = current.withColor(ChatFormatting.BLACK);
                    case '1' -> current = current.withColor(ChatFormatting.DARK_BLUE);
                    case '2' -> current = current.withColor(ChatFormatting.DARK_GREEN);
                    case '3' -> current = current.withColor(ChatFormatting.DARK_AQUA);
                    case '4' -> current = current.withColor(ChatFormatting.DARK_RED);
                    case '5' -> current = current.withColor(ChatFormatting.DARK_PURPLE);
                    case '6' -> current = current.withColor(ChatFormatting.GOLD);
                    case '7' -> current = current.withColor(ChatFormatting.GRAY);
                    case '8' -> current = current.withColor(ChatFormatting.DARK_GRAY);
                    case '9' -> current = current.withColor(ChatFormatting.BLUE);
                    case 'a' -> current = current.withColor(ChatFormatting.GREEN);
                    case 'b' -> current = current.withColor(ChatFormatting.AQUA);
                    case 'c' -> current = current.withColor(ChatFormatting.RED);
                    case 'd' -> current = current.withColor(ChatFormatting.LIGHT_PURPLE);
                    case 'e' -> current = current.withColor(ChatFormatting.YELLOW);
                    case 'f' -> current = current.withColor(ChatFormatting.WHITE);

                    case 'l' -> current = current.withBold(true);
                    case 'o' -> current = current.withItalic(true);
                    case 'n' -> current = current.withUnderlined(true);
                    case 'm' -> current = current.withStrikethrough(true);
                    case 'k' -> current = current.withObfuscated(true);

                    case 'r' -> current = Style.EMPTY.withColor(ChatFormatting.WHITE);

                    default -> buf.append('&').append(format.charAt(i + 1));
                }
                i++;
                continue;
            }

            buf.append(ch);
        }

        flush(out, buf, current);
        return out;
    }

    private static void flush(MutableComponent out, StringBuilder buf, Style style) {
        if (!buf.isEmpty()) {
            appendLiteral(out, buf.toString(), style);
            buf.setLength(0);
        }
    }

    private static void appendLiteral(MutableComponent out, String text, Style style) {
        out.append(Component.literal(text).setStyle(style));
    }

    private static Style parseHoverStyle(String cfg, Style parentStyle, Style fallback) {
        if (cfg == null || cfg.isBlank()) return fallback;

        Style out = fallback;
        String s = cfg.trim();
        final String PARENT = "%parent-color%";

        if (s.toLowerCase().contains(PARENT)) {
            TextColor parentColor = parentStyle.getColor();
            out = (parentColor != null ? Style.EMPTY.withColor(parentColor) : fallback);
            s = s.replace(PARENT, "");
        }

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '&' && i + 1 < s.length()) {
                char code = Character.toLowerCase(s.charAt(i + 1));
                switch (code) {
                    case '0' -> out = out.withColor(ChatFormatting.BLACK);
                    case '1' -> out = out.withColor(ChatFormatting.DARK_BLUE);
                    case '2' -> out = out.withColor(ChatFormatting.DARK_GREEN);
                    case '3' -> out = out.withColor(ChatFormatting.DARK_AQUA);
                    case '4' -> out = out.withColor(ChatFormatting.DARK_RED);
                    case '5' -> out = out.withColor(ChatFormatting.DARK_PURPLE);
                    case '6' -> out = out.withColor(ChatFormatting.GOLD);
                    case '7' -> out = out.withColor(ChatFormatting.GRAY);
                    case '8' -> out = out.withColor(ChatFormatting.DARK_GRAY);
                    case '9' -> out = out.withColor(ChatFormatting.BLUE);
                    case 'a' -> out = out.withColor(ChatFormatting.GREEN);
                    case 'b' -> out = out.withColor(ChatFormatting.AQUA);
                    case 'c' -> out = out.withColor(ChatFormatting.RED);
                    case 'd' -> out = out.withColor(ChatFormatting.LIGHT_PURPLE);
                    case 'e' -> out = out.withColor(ChatFormatting.YELLOW);
                    case 'f' -> out = out.withColor(ChatFormatting.WHITE);

                    case 'l' -> out = out.withBold(true);
                    case 'o' -> out = out.withItalic(true);
                    case 'n' -> out = out.withUnderlined(true);
                    case 'm' -> out = out.withStrikethrough(true);
                    case 'k' -> out = out.withObfuscated(true);

                    case 'r' -> out = fallback;

                    default -> {}
                }
                i++;
            }
        }

        String word = s.replace("&", "").trim();
        if (!word.isEmpty() && looksLikeSingleWordColorName(word)) {
            try {
                out = out.withColor(ChatFormatting.valueOf(word.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }

        return out;
    }

    private static boolean looksLikeSingleWordColorName(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!Character.isLetter(c) && c != '_') return false;
        }
        return true;
    }
}
