package me.xarta.xsystemmsgeditor.event;

import me.xarta.xsystemmsgeditor.config.ConfigHandler;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

public class ServerShutdownHandler {

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        var server = event.getServer();

        String raw = ConfigHandler.SERVER_CLOSED.get();
        String colored = raw.replace("&", "§");

        Component message = Component.literal(colored);

        server.getPlayerList().getPlayers().forEach(player ->
                player.connection.disconnect(message)
        );
    }
}
