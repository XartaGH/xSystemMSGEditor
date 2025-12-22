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
    public static final ModConfigSpec.ConfigValue<String> SYNTAX_ERROR;
    public static final ModConfigSpec.ConfigValue<Boolean> SYNTAX_ADD_SLASH;
    public static final ModConfigSpec.ConfigValue<String> ADVANCEMENT_FORMAT;
    public static final ModConfigSpec.ConfigValue<Boolean> ADVANCEMENT_TOOLTIP;
    public static final ModConfigSpec.ConfigValue<String> CHALLENGE_FORMAT;
    public static final ModConfigSpec.ConfigValue<Boolean> CHALLENGE_TOOLTIP;
    public static final ModConfigSpec.ConfigValue<String> GOAL_FORMAT;
    public static final ModConfigSpec.ConfigValue<Boolean> GOAL_TOOLTIP;
    public static final ModConfigSpec.ConfigValue<String> TOOLTIP_HEADING_COLOR;
    public static final ModConfigSpec.ConfigValue<String> TOOLTIP_BODY_COLOR;
    public static final ModConfigSpec.ConfigValue<String> DEATH_MESSAGE;
    public static final ModConfigSpec.ConfigValue<Boolean> DEATH_TOOLTIP;
    public static final ModConfigSpec.ConfigValue<String> DEATH_TOOLTIP_CONTENTS;

    static {
        BUILDER.push("xSystemMSGEditor Configuration");
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

        SYNTAX_ERROR = BUILDER
                .comment("Message when command syntax is incorrect (leave empty to disable). Use %syntax% for usage.")
                .define("syntax-error", "&cInvalid syntax. Correct: %syntax%");

        SYNTAX_ADD_SLASH = BUILDER
                .comment("Add leading slash to %syntax% when possible")
                .define("syntax-add-slash", true);

        BUILDER.comment("Use %player% for player name and %advancement%/%challenge%/%goal% for advancement title",
                "Color codes: &0-9, &a-f for colors, &l for bold, &o for italic, &r for reset");

        ADVANCEMENT_FORMAT = BUILDER
                .comment("Format for regular task advancements")
                .define("advancement-format", "&a%player% &7achieved advancement &a%advancement%");

        ADVANCEMENT_TOOLTIP = BUILDER
                .comment("Show tooltip when hovering on advancement")
                .define("advancement-tooltip", true);

        CHALLENGE_FORMAT = BUILDER
                .comment("Format for challenge advancements")
                .define("challenge-format", "&5%player% &7completed challenge &5%challenge%");

        CHALLENGE_TOOLTIP = BUILDER
                .comment("Show tooltip when hovering on challenge")
                .define("challenge-tooltip", true);

        GOAL_FORMAT = BUILDER
                .comment("Format for goal advancements")
                .define("goal-format", "&e%player% &7reached goal &e%goal%");

        GOAL_TOOLTIP = BUILDER
                .comment("Show tooltip when hovering on goal")
                .define("goal-tooltip", true);

        TOOLTIP_HEADING_COLOR = BUILDER
                .comment("Tooltip's heading text color, %parent-color% is the same as in advancement's message")
                .define("tooltip-heading-color", "%parent-color%");

        TOOLTIP_BODY_COLOR = BUILDER
                .comment("Tooltip's body text color, %parent-color% is the same as in advancement's message")
                .define("tooltip-body-color", "%parent-color%");

        DEATH_MESSAGE = BUILDER
                .comment("Message shown when player dies (%reason% has a leading space)")
                .define("death-message", "%player%&c%reason%");

        DEATH_TOOLTIP = BUILDER
                .comment("Show tooltip when hovering on death message (i.e. %player% died and when player hovers they can see a full reason)")
                .define("death-tooltip", true);

        DEATH_TOOLTIP_CONTENTS = BUILDER
                .comment("Death message tooltip's content (%reason% has a leading space)")
                .define("death-tooltip-contents", "&7Reason:&c%reason%");

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
