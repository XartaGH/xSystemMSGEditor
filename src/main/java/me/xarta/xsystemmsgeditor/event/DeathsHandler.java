
package me.xarta.xsystemmsgeditor.event;

import me.xarta.xsystemmsgeditor.config.ConfigHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.List;
import java.util.Map;

public class DeathsHandler {

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        String baseFormat = ConfigHandler.DEATH_MESSAGE.get();
        if (baseFormat.isBlank()) return;

        Component vanilla = player.getCombatTracker().getDeathMessage();
        Component reason  = extractReasonWithoutVictim(vanilla);

        final Map<String, Component> replBase = Map.of(
                "%player%", Component.literal(player.getName().getString()),
                "%reason%", reason.copy()
        );

        Component tooltip = null;
        if (ConfigHandler.DEATH_TOOLTIP.get()) {
            String tooltipFormat = ConfigHandler.DEATH_TOOLTIP_CONTENTS.get();
            if (!tooltipFormat.isBlank()) {
                tooltip = parseFormatted(tooltipFormat, replBase, Style.EMPTY.withColor(ChatFormatting.WHITE));
            }
        }

        Component msg = parseFormatted(baseFormat, replBase, Style.EMPTY.withColor(ChatFormatting.WHITE));
        msg = stripLeadingSpacesFromFirstPlainText(msg);
        MutableComponent out = msg.copy();
        if (tooltip != null) {
            out.setStyle(out.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip)));
        }

        for (ServerPlayer target : player.server.getPlayerList().getPlayers()) {
            target.sendSystemMessage(out);
        }
    }

    private static Component extractReasonWithoutVictim(Component vanilla) {
        var contents = vanilla.getContents();
        if (contents instanceof TranslatableContents tc) {
            String   key  = tc.getKey();
            Object[] args = tc.getArgs();
            if (args.length >= 1) {
                Object[] patched = args.clone();
                patched[0] = Component.empty();
                try {
                    // ВАЖНО: возвращаем переводимый компонент, не превращаем в literal
                    return Component.translatable(key, patched);
                } catch (Exception ignored) {}
            }
        }
        // Фолбэк: оставить как есть (переводимый), без getString()
        return vanilla;
    }

    private static String getPlainText(PlainTextContents pt) {
        return pt.text();
    }

    private static MutableComponent copyHeadOnly(Component in) {
        var contents = in.getContents();
        MutableComponent head;
        if (contents instanceof PlainTextContents pt) {
            head = Component.literal(getPlainText(pt));
        } else if (contents instanceof TranslatableContents tr) {
            head = Component.translatable(tr.getKey(), tr.getArgs());
        } else {
            head = Component.literal(in.getString());
        }
        head.setStyle(in.getStyle());
        return head;
    }

    private static String trimLeadingSpaces(String s) {
        int i = 0;
        while (i < s.length() && s.charAt(i) == ' ') i++;
        return (i == 0) ? s : s.substring(i);
    }

    private static Component parseFormatted(String format,
                                            Map<String, Component> replacements,
                                            Style baseStyle) {
        final String fmt = (format != null) ? format : "";
        if (fmt.isBlank()) return Component.empty();

        MutableComponent out = Component.empty();
        Style current = baseStyle;
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < fmt.length(); i++) {
            char ch = fmt.charAt(i);

            if (ch == '%' && i + 1 < fmt.length()) {
                int end = fmt.indexOf('%', i + 1);
                if (end != -1) {
                    flush(out, buf, current);
                    String key = fmt.substring(i, end + 1);
                    Component rep = replacements.get(key);

                    if (rep == null) {
                        rep = Component.literal(key);
                    } else if ("%reason%".equals(key)) {
                        // Убираем ТОЛЬКО ведущие пробелы из первого PlainTextContents причины,
                        // не трогая переводимость и стили остальных частей.
                        rep = stripLeadingSpacesFromFirstPlainText(rep);
                    }

                    append(out, rep, current);
                    i = end;
                    continue;
                }
            }

            if (ch == '&' && i + 1 < fmt.length()) {
                flush(out, buf, current);
                char code = Character.toLowerCase(fmt.charAt(i + 1));
                Style next = applyLegacyCode(current, code);
                current = (next != null) ? next : current;
                i++;
                continue;
            }

            buf.append(ch);
        }

        flush(out, buf, current);
        return out;
    }

    private static Component stripLeadingSpacesFromFirstPlainText(Component in) {
        var headContents = in.getContents();
        if (headContents instanceof PlainTextContents pt) {
            String txt = getPlainText(pt);
            String trimmed = trimLeadingSpaces(txt);
            if (!trimmed.equals(txt)) {
                MutableComponent rebuiltHead = Component.literal(trimmed).setStyle(in.getStyle());
                for (Component sib : in.getSiblings()) {
                    rebuiltHead.append(sib.copy());
                }
                return rebuiltHead;
            }
        }

        List<Component> siblings = in.getSiblings();
        if (!siblings.isEmpty()) {
            Component first = siblings.getFirst();
            var c0 = first.getContents();
            if (c0 instanceof PlainTextContents pt0) {
                String txt0 = getPlainText(pt0);
                String trimmed0 = trimLeadingSpaces(txt0);
                if (!trimmed0.equals(txt0)) {
                    MutableComponent rebuilt = copyHeadOnly(in);

                    MutableComponent fixedFirst = Component.literal(trimmed0).setStyle(first.getStyle());
                    for (Component s : first.getSiblings()) fixedFirst.append(s.copy());
                    rebuilt.append(fixedFirst);

                    for (int i = 1; i < siblings.size(); i++) {
                        rebuilt.append(siblings.get(i).copy());
                    }
                    return rebuilt;
                }
            }
        }

        return in;
    }

    private static Style applyLegacyCode(Style in, char code) {
        return switch (code) {
            case '0' -> in.withColor(ChatFormatting.BLACK);
            case '1' -> in.withColor(ChatFormatting.DARK_BLUE);
            case '2' -> in.withColor(ChatFormatting.DARK_GREEN);
            case '3' -> in.withColor(ChatFormatting.DARK_AQUA);
            case '4' -> in.withColor(ChatFormatting.DARK_RED);
            case '5' -> in.withColor(ChatFormatting.DARK_PURPLE);
            case '6' -> in.withColor(ChatFormatting.GOLD);
            case '7' -> in.withColor(ChatFormatting.GRAY);
            case '8' -> in.withColor(ChatFormatting.DARK_GRAY);
            case '9' -> in.withColor(ChatFormatting.BLUE);
            case 'a' -> in.withColor(ChatFormatting.GREEN);
            case 'b' -> in.withColor(ChatFormatting.AQUA);
            case 'c' -> in.withColor(ChatFormatting.RED);
            case 'd' -> in.withColor(ChatFormatting.LIGHT_PURPLE);
            case 'e' -> in.withColor(ChatFormatting.YELLOW);
            case 'f' -> in.withColor(ChatFormatting.WHITE);
            case 'l' -> in.withBold(true);
            case 'o' -> in.withItalic(true);
            case 'n' -> in.withUnderlined(true);
            case 'm' -> in.withStrikethrough(true);
            case 'k' -> in.withObfuscated(true);
            case 'r' -> Style.EMPTY.withColor(ChatFormatting.WHITE);
            default  -> null;
        };
    }

    private static void flush(MutableComponent out, StringBuilder buf, Style style) {
        if (!buf.isEmpty()) {
            MutableComponent lit = Component.literal(buf.toString()).setStyle(style);
            out.append(lit);
            buf.setLength(0);
        }
    }

    private static void append(MutableComponent out, Component part, Style style) {
        MutableComponent comp = part.copy().setStyle(mergeBase(part.getStyle(), style));
        out.append(comp);
    }

    private static Style mergeBase(Style a, Style b) {
        TextColor c = b.getColor();
        Style s = a;
        if (c != null) s = s.withColor(c);
        if (b.isBold()) s = s.withBold(true);
        if (b.isItalic()) s = s.withItalic(true);
        if (b.isUnderlined()) s = s.withUnderlined(true);
        if (b.isStrikethrough()) s = s.withStrikethrough(true);
        if (b.isObfuscated()) s = s.withObfuscated(true);
        return s;
    }
}
