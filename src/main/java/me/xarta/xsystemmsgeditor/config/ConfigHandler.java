
package me.xarta.xsystemmsgeditor.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ConfigHandler {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<String> SERVER_CLOSED;
    public static final ModConfigSpec.ConfigValue<String> UNKNOWN_COMMAND;
    public static final ModConfigSpec.ConfigValue<Boolean> UNKNOWN_COMMAND_ADD_SLASH;
    public static final ModConfigSpec.ConfigValue<String> NO_PERMISSION;
    public static final ModConfigSpec.ConfigValue<Boolean> NO_PERMISSION_ADD_SLASH;

    static {
        BUILDER.push("Custom Messages");
        BUILDER.comment("These messages support Bukkit color codes (& -> §) and the %command% placeholder.");

        SERVER_CLOSED = BUILDER
                .comment("Message shown to players when server shuts down")
                .define("server-closed", "&cServer is closed");

        UNKNOWN_COMMAND = BUILDER
                .comment("Message shown when player types an unknown command (Leave empty to disable)")
                .define("unknown-command", "&cUnknown command: %command%");

        UNKNOWN_COMMAND_ADD_SLASH = BUILDER
                .comment("Whether slash should be added to %command% placeholder in unknown-command message")
                .define("unknown-command-add-slash", true);

        NO_PERMISSION = BUILDER
                .comment("Message shown when player has no permission (Leave empty to disable)")
                .define("no-permission", "&4You have no permission to execute %command%");

        NO_PERMISSION_ADD_SLASH = BUILDER
                .comment("Whether slash should be added to %command% placeholder in no-permission message")
                .define("no-permission-add-slash", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
