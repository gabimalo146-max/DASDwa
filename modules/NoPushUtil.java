package me.Gui.gui.modules;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.config.GuiConfig;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;

public final class NoPushUtil {
    private NoPushUtil() {
    }

    public static boolean isEnabled() {
        HackModule m = GuiClient.MODULES.byId(ModuleId.NO_PUSH);
        return m != null && m.isEnabled();
    }

    public static boolean playersEnabled() {
        if (!NoPushUtil.isEnabled()) {
            return false;
        }
        GuiConfig cfg = GuiClient.CONFIG;
        return cfg.noPushPlayers == null || cfg.noPushPlayers != false;
    }

    public static boolean blocksEnabled() {
        if (!NoPushUtil.isEnabled()) {
            return false;
        }
        GuiConfig cfg = GuiClient.CONFIG;
        return cfg.noPushBlocks == null || cfg.noPushBlocks != false;
    }

    public static boolean liquidsEnabled() {
        if (!NoPushUtil.isEnabled()) {
            return false;
        }
        GuiConfig cfg = GuiClient.CONFIG;
        return cfg.noPushLiquids == null || cfg.noPushLiquids != false;
    }
}

