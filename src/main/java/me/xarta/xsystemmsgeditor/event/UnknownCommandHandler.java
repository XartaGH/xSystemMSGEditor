package me.xarta.xsystemmsgeditor.event;

import com.mojang.brigadier.context.CommandContextBuilder;
import me.xarta.xsystemmsgeditor.config.ConfigHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.CommandEvent;

public class UnknownCommandHandler {

    @SubscribeEvent
    public void onCommandEvent(CommandEvent event) {
        // событие: после парсинга, до исполнения команды
        CommandContextBuilder<CommandSourceStack> ctx = event.getParseResults().getContext();
        CommandSourceStack source = ctx.getSource();

        // не игрок -> не вмешиваемся (консоль, командный блок)
        if (!source.isPlayer()) {
            return;
        }

        // "неизвестная команда" — парсер не собрал ни одного узла
        if (!ctx.getNodes().isEmpty()) {
            return; // валидные команды не трогаем
        }

        // get() никогда не null; пустая строка = выключено
        String raw = ConfigHandler.UNKNOWN_COMMAND.get();
        if (raw.isBlank()) {
            return;
        }

        // в конфиге используй '&', а не '&amp;'
        String colored = raw.replace("&", "§");
        Component message = Component.literal(colored);

        // отправить своё и отменить стандартную ошибку Brigadier
        source.sendSystemMessage(message);
        event.setCanceled(true);
    }
}
