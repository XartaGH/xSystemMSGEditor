package me.xarta.xsystemmsgeditor.event;

import me.xarta.xsystemmsgeditor.config.ConfigHandler;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

public class ServerShutdownHandler {

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        var server = event.getServer(); // Get server instance

        String raw = ConfigHandler.SERVER_CLOSED.get(); // Get message from config as raw text
        String colored = raw.replace("&", "§"); // Replace Bukkit-style coloring with default style

        Component message = Component.literal(colored); // Create a component

        server.getPlayerList().getPlayers().forEach(
                player -> player.connection.disconnect(message) // Send component to each player
        );
    }
}
