package me.xarta.xsystemmsgeditor.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ConfigHandler {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC; // Config object used during registration
    public static final ModConfigSpec.ConfigValue<String> SERVER_CLOSED; // Value of server-closed from config
    public static final ModConfigSpec.ConfigValue<String> UNKNOWN_COMMAND; // Message shown when player types unknown command

    static {
        BUILDER.push("Custom Messages");
        BUILDER.comment("These messages support Bukkit color codes");

        // Add server-closed value to config
        SERVER_CLOSED = BUILDER
                .comment("Message shown to players when server shuts down")
                .define("server-closed", "&cServer is closed");

        UNKNOWN_COMMAND = BUILDER
                .comment("Message shown when player types an unknown command (Leave empty to disable)")
                .define("unknown-command", "&cUnknown command!");

        BUILDER.pop();
        SPEC = BUILDER.build(); // Build config
    }
}
