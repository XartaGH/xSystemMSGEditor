package me.xarta.xsystemmsgeditor;

import com.mojang.logging.LogUtils;
import me.xarta.xsystemmsgeditor.config.ConfigHandler;
import me.xarta.xsystemmsgeditor.event.ServerShutdownHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(XSystemMSGEditor.MODID)
public class XSystemMSGEditor {

    public static final String MODID = "xsystemmsgeditor";
    public static final Logger LOGGER = LogUtils.getLogger();

    public XSystemMSGEditor(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("xSystemMSGEditor initializing...");

        // Регистрируем SERVER-конфиг (как в твоём другом моде)
        modContainer.registerConfig(
                ModConfig.Type.SERVER,
                ConfigHandler.SPEC,
                "xsystemmsgeditor.toml"
        );

        // Регистрируем обработчик серверных событий
        NeoForge.EVENT_BUS.register(new ServerShutdownHandler());
    }
}
