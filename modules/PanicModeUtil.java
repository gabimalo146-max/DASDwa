package me.Gui.gui.modules;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;

public final class PanicModeUtil {
    private PanicModeUtil() {
    }

    public static boolean allowArrayList() {
        Boolean show = GuiClient.CONFIG.showArrayList;
        if (show != null && !show.booleanValue()) {
            return false;
        }
        if (!PanicModeUtil.isEnabled()) {
            return true;
        }
        return !PanicModeUtil.hideFlag(GuiClient.CONFIG.panicHideArrayList);
    }

    public static boolean allowNotifications() {
        Boolean show = GuiClient.CONFIG.showNotifications;
        if (show != null && !show.booleanValue()) {
            return false;
        }
        if (!PanicModeUtil.isEnabled()) {
            return true;
        }
        return !PanicModeUtil.hideFlag(GuiClient.CONFIG.panicHideNotifications);
    }

    public static boolean allowKeybinds() {
        Boolean show = GuiClient.CONFIG.showKeybindsHud;
        if (show != null && !show.booleanValue()) {
            return false;
        }
        if (!PanicModeUtil.isEnabled()) {
            return true;
        }
        return !PanicModeUtil.hideFlag(GuiClient.CONFIG.panicHideKeybinds);
    }

    private static boolean hideFlag(Boolean flag) {
        return flag == null || flag != false;
    }

    private static boolean isEnabled() {
        HackModule mod = GuiClient.MODULES.byId(ModuleId.PANIC_MODE);
        return mod != null && mod.isEnabled();
    }
}

