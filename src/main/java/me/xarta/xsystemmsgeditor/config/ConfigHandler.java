package me.xarta.xsystemmsgeditor.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ConfigHandler {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<String> SERVER_CLOSED;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        SERVER_CLOSED = builder
                .comment("Message shown to players when server shuts down")
                .define("server-closed", "&cServer is closed.");

        SPEC = builder.build();
    }
}
