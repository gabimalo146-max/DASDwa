package me.Gui.gui.modules;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;

public final class NoGuiSignUtil {
    private NoGuiSignUtil() {
    }

    public static boolean isEnabled() {
        HackModule m = GuiClient.MODULES.byId(ModuleId.NO_GUI_SIGN);
        return m != null && m.isEnabled();
    }
}

