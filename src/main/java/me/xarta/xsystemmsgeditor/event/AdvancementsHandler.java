package me.xarta.xsystemmsgeditor.event;

import me.xarta.xsystemmsgeditor.config.ConfigHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
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

        String headingColorCfg = ConfigHandler.TOOLTIP_HEADING_COLOR.get();
        String bodyColorCfg    = ConfigHandler.TOOLTIP_BODY_COLOR.get();

        Component msg = buildMessage(format, playerName, title, desc, tooltipEnabled, headingColorCfg, bodyColorCfg);

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
            String headingColorCfg,
            String bodyColorCfg
    ) {
        MutableComponent out = Component.empty();

        ChatFormatting color = ChatFormatting.WHITE;
        boolean bold = false, italic = false;

        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < format.length(); i++) {
            char ch = format.charAt(i);

            if (ch == '%' && i + 1 < format.length()) {
                int end = format.indexOf('%', i + 1);
                if (end != -1) {
                    flush(out, buf, color, bold, italic);

                    String key = format.substring(i + 1, end);
                    switch (key) {
                        case "player" -> flushLiteral(out, playerName, color, bold, italic);

                        case "advancement", "challenge", "goal" -> {
                            Style nameStyle = Style.EMPTY.withColor(color);
                            if (bold)   nameStyle = nameStyle.withBold(true);
                            if (italic) nameStyle = nameStyle.withItalic(true);

                            MutableComponent nameCopy = advancementTitle.copy().setStyle(nameStyle);

                            if (tooltipEnabled && advancementDesc != null) {
                                ChatFormatting headingColor = resolveTooltipColor(
                                        headingColorCfg, color, ChatFormatting.WHITE   // Fallback: WHITE
                                );
                                ChatFormatting bodyColor    = resolveTooltipColor(
                                        bodyColorCfg,    color, ChatFormatting.GRAY    // Fallback: GRAY
                                );

                                MutableComponent hover = Component.empty()
                                        .append(advancementTitle.copy().withStyle(Style.EMPTY.withColor(headingColor)))
                                        .append(Component.literal("\n"))
                                        .append(advancementDesc.copy().withStyle(Style.EMPTY.withColor(bodyColor)));

                                nameCopy = nameCopy.setStyle(
                                        nameCopy.getStyle().withHoverEvent(
                                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)
                                        )
                                );
                            }

                            out.append(nameCopy);
                        }

                        default -> flushLiteral(out, "%" + key + "%", color, bold, italic);
                    }

                    i = end;
                    continue;
                }
            }

            if (ch == '&' && i + 1 < format.length()) {
                flush(out, buf, color, bold, italic);

                char code = Character.toLowerCase(format.charAt(i + 1));
                switch (code) {
                    case '0' -> color = ChatFormatting.BLACK;
                    case '1' -> color = ChatFormatting.DARK_BLUE;
                    case '2' -> color = ChatFormatting.DARK_GREEN;
                    case '3' -> color = ChatFormatting.DARK_AQUA;
                    case '4' -> color = ChatFormatting.DARK_RED;
                    case '5' -> color = ChatFormatting.DARK_PURPLE;
                    case '6' -> color = ChatFormatting.GOLD;
                    case '7' -> color = ChatFormatting.GRAY;
                    case '8' -> color = ChatFormatting.DARK_GRAY;
                    case '9' -> color = ChatFormatting.BLUE;
                    case 'a' -> color = ChatFormatting.GREEN;
                    case 'b' -> color = ChatFormatting.AQUA;
                    case 'c' -> color = ChatFormatting.RED;
                    case 'd' -> color = ChatFormatting.LIGHT_PURPLE;
                    case 'e' -> color = ChatFormatting.YELLOW;
                    case 'f' -> color = ChatFormatting.WHITE;

                    case 'l' -> bold = true;
                    case 'o' -> italic = true;
                    case 'r' -> { color = ChatFormatting.WHITE; bold = false; italic = false; }

                    default -> buf.append('&').append(format.charAt(i + 1));
                }
                i++;
                continue;
            }

            buf.append(ch);
        }

        flush(out, buf, color, bold, italic);
        return out;
    }

    private static void flush(
            MutableComponent out, StringBuilder buf,
            ChatFormatting color, boolean bold, boolean italic
    ) {
        if (!buf.isEmpty()) {
            flushLiteral(out, buf.toString(), color, bold, italic);
            buf.setLength(0);
        }
    }

    private static void flushLiteral(
            MutableComponent out, String text,
            ChatFormatting color, boolean bold, boolean italic
    ) {
        Style style = Style.EMPTY.withColor(color);
        if (bold)   style = style.withBold(true);
        if (italic) style = style.withItalic(true);
        out.append(Component.literal(text).setStyle(style));
    }

    private static ChatFormatting resolveTooltipColor(String cfg,
                                                      ChatFormatting effectiveColorBeforePlaceholder,
                                                      ChatFormatting fallback) {
        if (cfg == null || cfg.isBlank()) return fallback;

        if ("DEPENDS-ON-TYPE".equalsIgnoreCase(cfg)) {
            return effectiveColorBeforePlaceholder != null ? effectiveColorBeforePlaceholder : fallback;
        }

        if (cfg.length() >= 2 && cfg.charAt(0) == '&') {
            char code = Character.toLowerCase(cfg.charAt(1));
            return switch (code) {
                case '0' -> ChatFormatting.BLACK;
                case '1' -> ChatFormatting.DARK_BLUE;
                case '2' -> ChatFormatting.DARK_GREEN;
                case '3' -> ChatFormatting.DARK_AQUA;
                case '4' -> ChatFormatting.DARK_RED;
                case '5' -> ChatFormatting.DARK_PURPLE;
                case '6' -> ChatFormatting.GOLD;
                case '7' -> ChatFormatting.GRAY;
                case '8' -> ChatFormatting.DARK_GRAY;
                case '9' -> ChatFormatting.BLUE;
                case 'a' -> ChatFormatting.GREEN;
                case 'b' -> ChatFormatting.AQUA;
                case 'c' -> ChatFormatting.RED;
                case 'd' -> ChatFormatting.LIGHT_PURPLE;
                case 'e' -> ChatFormatting.YELLOW;
                case 'f' -> ChatFormatting.WHITE;
                default  -> fallback;
            };
        }

        try {
            return ChatFormatting.valueOf(cfg.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}
