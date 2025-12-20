
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
        // После парсинга, до выполнения
        CommandContextBuilder<CommandSourceStack> ctx = event.getParseResults().getContext();
        CommandSourceStack source = ctx.getSource();

        // Игнорируем не-игроков (консоль и командные блоки)
        if (!source.isPlayer()) {
            return;
        }

        // Если парсер уже собрал узлы — команда распознана, не вмешиваемся
        if (!ctx.getNodes().isEmpty()) {
            return;
        }

        // Исходная строка ввода команды
        String input = event.getParseResults().getReader().getString();
        String firstToken = extractFirstToken(input);
        if (firstToken.isEmpty()) {
            return;
        }

        // Корневой узел дерева команд
        CommandNode<CommandSourceStack> root = ctx.getRootNode();
        LiteralCommandNode<CommandSourceStack> literal = findLiteral(root, firstToken);

        if (literal != null) {
            // Команда существует, но может быть недоступна по requires(...)
            boolean allowed = literal.getRequirement().test(source);
            if (!allowed) {
                // Недостаточно прав → custom сообщение "no-permission"
                String rawPerm = ConfigHandler.NO_PERMISSION.get();
                if (!rawPerm.isBlank()) {
                    source.sendSystemMessage(Component.literal(colorize(rawPerm)));
                    event.setCanceled(true);
                }
                return;
            }
            // Если узел существует и разрешён, но парсер не собрал узлы — оставим стандартное поведение
            // (например, ошибка синтаксиса в аргументах — это не наш кейс)
        }

        // Литерал не найден → действительно неизвестная команда
        String rawUnknown = ConfigHandler.UNKNOWN_COMMAND.get();
        if (!rawUnknown.isBlank()) {
            source.sendSystemMessage(Component.literal(colorize(rawUnknown)));
            event.setCanceled(true);
        }
    }

    private static String extractFirstToken(String input) {
        String s = input.startsWith("/") ? input.substring(1) : input;
        s = s.trim();
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

    private static String colorize(String raw) {
        // В конфиге используем & — здесь преобразуем в §
        return raw.replace("&", "§");
    }

}
