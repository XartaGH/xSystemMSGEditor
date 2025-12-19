package me.xarta.xsystemmsgeditor.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ConfigHandler {

    public static final ModConfigSpec SPEC; // Config object used during registration
    public static final ModConfigSpec.ConfigValue<String> SERVER_CLOSED; // Value of server-closed from config

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder(); // Create config builder

        // Add server-closed value to config
        SERVER_CLOSED = builder
                .comment("Message shown to players when server shuts down (Supports Bukkit color codes)")
                .define("server-closed", "&cServer is closed.");

        SPEC = builder.build(); // Build config
    }
}
