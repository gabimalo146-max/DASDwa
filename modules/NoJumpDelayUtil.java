package me.Gui.gui.modules;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.mixin.LivingEntityAccessor;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import net.minecraft.client.MinecraftClient;

public final class NoJumpDelayUtil {
    private NoJumpDelayUtil() {
    }

    public static boolean isEnabled() {
        HackModule m = GuiClient.MODULES.byId(ModuleId.NOJUMP_DELAY);
        return m != null && m.isEnabled();
    }

    public static void tick(MinecraftClient client) {
        if (client == null || client.player == null || client.options == null) {
            return;
        }
        if (!NoJumpDelayUtil.isEnabled()) {
            return;
        }
        if (!client.options.jumpKey.isPressed()) {
            return;
        }
        ((LivingEntityAccessor)client.player).setJumpingCooldown(0);
    }
}

