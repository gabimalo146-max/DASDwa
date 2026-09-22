package me.Gui.gui.client;

import java.lang.reflect.Method;
import me.Gui.gui.client.sound.SoundUtil;
import me.Gui.gui.config.ConfigManager;
import me.Gui.gui.config.GuiConfig;
import me.Gui.gui.friends.FriendManager;
import me.Gui.gui.license.LicenseManager;
import me.Gui.gui.modules.AutoDripUtil;
import me.Gui.gui.modules.ModuleManager;
import me.Gui.gui.ui.GuiScreen;
import me.Gui.gui.ui.hud.ArmorHud;
import me.Gui.gui.ui.hud.ArrayListHud;
import me.Gui.gui.ui.hud.BindListHud;
import me.Gui.gui.ui.hud.CooldownListHud;
import me.Gui.gui.ui.hud.CoordinatesHud;
import me.Gui.gui.ui.hud.ErrorKillerHud;
import me.Gui.gui.ui.hud.FpsHud;
import me.Gui.gui.ui.hud.InventoryHud;
import me.Gui.gui.ui.hud.NotificationHud;
import me.Gui.gui.ui.hud.PingHud;
import me.Gui.gui.ui.hud.PotionsHud;
import me.Gui.gui.ui.hud.SwordInfoHud;
import me.Gui.gui.ui.hud.TracersHud;
import me.Gui.gui.ui.world.AimAssistHighlightRenderer;
import me.Gui.gui.ui.world.BetterHitboxesRenderer;
import me.Gui.gui.ui.world.BlockEspRenderer;
import me.Gui.gui.ui.world.FreecamGhostRenderer;
import me.Gui.gui.ui.world.ItemEspRenderer;
import me.Gui.gui.ui.world.LogoutSpotsRenderer;
import me.Gui.gui.ui.world.NameTagsRenderer;
import me.Gui.gui.ui.world.PearlTrajectoryRenderer;
import me.Gui.gui.ui.world.PlayerEspRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class GuiClient
implements ClientModInitializer {
    public static final GuiConfig CONFIG = new GuiConfig();
    public static final ModuleManager MODULES = new ModuleManager();
    public static final FriendManager FRIENDS = new FriendManager();
    public static final ConfigManager CONFIGS = new ConfigManager(CONFIG, MODULES, FRIENDS);
    private static final long AUTO_SAVE_MS = 10000L;
    private static long lastAutoSaveAt = 0L;
    private static boolean licenseNotified = false;
    private static boolean booted = false;
    private static boolean guiBroken = false;
    private static boolean guiBrokenNotified = false;
    private static String guiBrokenReason = null;

    public void onInitializeClient() {
        if (LicenseManager.initAndVerify()) {
            GuiClient.boot();
            return;
        }
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (booted) {
                return;
            }
            if (client == null) {
                return;
            }
            if (!licenseNotified && client.player != null) {
                licenseNotified = true;
                String hwid = LicenseManager.currentHwid();
                String msg = "License invalid: " + LicenseManager.lastError() + " | HWID: " + (hwid == null ? "" : hwid);
                client.player.sendMessage((Text)Text.literal((String)msg), true);
            }
        });
    }

    public static boolean tryBootAfterLicense() {
        if (booted) {
            return true;
        }
        if (!LicenseManager.initAndVerify()) {
            return false;
        }
        GuiClient.boot();
        return true;
    }

    private static void boot() {
        if (booted) {
            return;
        }
        booted = true;
        CONFIGS.loadActiveOrDefault();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            MODULES.tickBinds(client);
            MODULES.tick(client);
            GuiClient.autoSave();
        });
        HudRenderCallback.EVENT.register(BindListHud::render);
        HudRenderCallback.EVENT.register(ArrayListHud::render);
        HudRenderCallback.EVENT.register(NotificationHud::render);
        HudRenderCallback.EVENT.register(CoordinatesHud::render);
        HudRenderCallback.EVENT.register(ArmorHud::render);
        HudRenderCallback.EVENT.register(PotionsHud::render);
        HudRenderCallback.EVENT.register(CooldownListHud::render);
        HudRenderCallback.EVENT.register(InventoryHud::render);
        HudRenderCallback.EVENT.register(PingHud::render);
        HudRenderCallback.EVENT.register(FpsHud::render);
        HudRenderCallback.EVENT.register(TracersHud::render);
        HudRenderCallback.EVENT.register(SwordInfoHud::render);
        HudRenderCallback.EVENT.register(ErrorKillerHud::render);
        NameTagsRenderer.register();
        UseBlockCallback.EVENT.register(AutoDripUtil::onUseBlock);
        WorldRenderEvents.LAST.register(PearlTrajectoryRenderer::render);
        WorldRenderEvents.LAST.register(BlockEspRenderer::render);
        WorldRenderEvents.LAST.register(ItemEspRenderer::render);
        WorldRenderEvents.LAST.register(LogoutSpotsRenderer::render);
        WorldRenderEvents.LAST.register(FreecamGhostRenderer::render);
        WorldRenderEvents.LAST.register(PlayerEspRenderer::render);
        WorldRenderEvents.LAST.register(BetterHitboxesRenderer::render);
        WorldRenderEvents.LAST.register(AimAssistHighlightRenderer::render);
    }

    public static void openGui() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        if (guiBroken) {
            GuiClient.notifyGuiBroken(client);
            return;
        }
        try {
            SoundUtil.playGuiOpen();
            client.setScreen((Screen)new GuiScreen());
        }
        catch (Throwable t) {
            guiBroken = true;
            guiBrokenReason = t.getClass().getSimpleName();
            guiBrokenNotified = false;
            System.err.println("[Gui] Failed to open GUI");
            t.printStackTrace();
            GuiClient.notifyGuiBroken(client);
        }
    }

    private static void notifyGuiBroken(MinecraftClient client) {
        if (guiBrokenNotified) {
            return;
        }
        if (client == null || client.player == null) {
            return;
        }
        String reason = guiBrokenReason == null ? "unknown" : guiBrokenReason;
        String msg = "GUI failed to load (" + reason + "). Reinstall the mod or remove duplicate jars.";
        client.player.sendMessage((Text)Text.literal((String)msg), true);
        guiBrokenNotified = true;
    }

    private static void autoSave() {
        long now = System.currentTimeMillis();
        if (now - lastAutoSaveAt < 10000L) {
            return;
        }
        lastAutoSaveAt = now;
        String active = GuiClient.CONFIG.activeConfig;
        if (active == null || active.isBlank()) {
            active = "default";
        }
        CONFIGS.save(active);
    }

    private static void forceShutdown(MinecraftClient client) {
        if (client == null) {
            System.exit(0);
            return;
        }
        try {
            Method m = client.getClass().getMethod("scheduleStop", new Class[0]);
            m.invoke((Object)client, new Object[0]);
            return;
        }
        catch (Throwable m) {
            try {
                Method m2 = client.getClass().getMethod("stop", new Class[0]);
                m2.invoke((Object)client, new Object[0]);
                return;
            }
            catch (Throwable throwable) {
                System.exit(0);
                return;
            }
        }
    }
}

