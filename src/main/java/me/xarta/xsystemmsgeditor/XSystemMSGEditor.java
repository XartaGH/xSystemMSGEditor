package me.xarta.xsystemmsgeditor;

import com.mojang.logging.LogUtils;
import me.xarta.xsystemmsgeditor.config.ConfigHandler;
import me.xarta.xsystemmsgeditor.event.ServerShutdownHandler;
import me.xarta.xsystemmsgeditor.event.CommandsHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(XSystemMSGEditor.MODID) // Declare this class as mod's main class
public class XSystemMSGEditor {

    public static final String MODID = "xsystemmsgeditor"; // Define modification's ID
    public static final Logger LOGGER = LogUtils.getLogger(); // Create logger


    public XSystemMSGEditor(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("xSystemMSGEditor initializing..."); // Print initialization message

        // Register config for the mod
        modContainer.registerConfig(
                ModConfig.Type.SERVER,
                ConfigHandler.SPEC,
                "xsystemmsgeditor.toml"
        );

        NeoForge.EVENT_BUS.register(new ServerShutdownHandler()); // Register server closed event handler
        NeoForge.EVENT_BUS.register(new CommandsHandler()); // Register commands events handler
        LOGGER.info("xSystemMSGEditor is on."); // Print success message
    }

}
