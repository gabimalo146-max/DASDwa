package me.Gui.gui.ui.hud;

import java.util.Objects;
import java.util.UUID;
import me.Gui.gui.client.GuiClient;
import me.Gui.gui.ui.GuiFonts;
import me.Gui.gui.ui.HudEditScreen;
import me.Gui.gui.ui.Theme;
import me.Gui.gui.ui.render.HudGlass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;

public final class PingHud {
    private static boolean dragging = false;
    private static boolean lastDown = false;
    private static double dragOffX;
    private static double dragOffY;

    private PingHud() {
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        boolean editActive = HudEditScreen.isActiveFor(HudEditScreen.HudItem.PING);
        if (HudEditScreen.isActive() && !editActive) {
            return;
        }
        if (!editActive && !Boolean.TRUE.equals(GuiClient.CONFIG.showPingHud)) {
            return;
        }
        int ping = -1;
        if (client.getNetworkHandler() != null) {
            UUID sessionId;
            PlayerListEntry entry = null;
            if (client.player != null) {
                entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            }
            if (entry == null && client.getSession() != null && (sessionId = client.getSession().getUuidOrNull()) != null) {
                entry = client.getNetworkHandler().getPlayerListEntry(sessionId);
            }
            if (entry == null && client.player != null) {
                entry = client.getNetworkHandler().getPlayerListEntry(client.player.getName().getString());
            }
            if (entry == null && client.getSession() != null) {
                entry = client.getNetworkHandler().getPlayerListEntry(client.getSession().getUsername());
            }
            if (entry != null) {
                ping = entry.getLatency();
            }
        }
        Object text = ping >= 0 ? "Ping " + ping + "ms" : "Ping -";
        TextRenderer tr = GuiFonts.textRendererOrDefault(client.textRenderer);
        int iconSize = 10;
        int gap = 6;
        int padX = 8;
        int padY = 4;
        int rawW = padX * 2 + iconSize + gap + tr.getWidth((String)text);
        Objects.requireNonNull(tr);
        int rawH = Math.max(iconSize, 9) + padY * 2;
        float scale = GuiClient.CONFIG.pingHudScale;
        if (Float.isNaN(scale) || Float.isInfinite(scale)) {
            scale = 0.9f;
        }
        scale = PingHud.clamp(scale, 0.5f, 1.4f);
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        int boxW = Math.round((float)rawW * scale);
        int boxH = Math.round((float)rawH * scale);
        int x = GuiClient.CONFIG.pingHudX;
        int y = GuiClient.CONFIG.pingHudY;
        int maxX = Math.max(4, sw - boxW - 4);
        int maxY = Math.max(4, sh - boxH - 4);
        x = PingHud.clamp(x, 4, maxX);
        y = PingHud.clamp(y, 4, maxY);
        GuiClient.CONFIG.pingHudX = x;
        GuiClient.CONFIG.pingHudY = y;
        if (editActive) {
            Window win = client.getWindow();
            long handle = win.getHandle();
            if (handle != 0L) {
                boolean down;
                int mx = PingHud.scaledMouseX(client);
                int my = PingHud.scaledMouseY(client);
                boolean bl = down = GLFW.glfwGetMouseButton((long)handle, (int)0) == 1;
                if (down && !lastDown && PingHud.inside(mx, my, x, y, boxW, boxH)) {
                    dragging = true;
                    dragOffX = mx - x;
                    dragOffY = my - y;
                }
                if (!down && lastDown && dragging) {
                    dragging = false;
                    PingHud.savePosition();
                }
                if (dragging) {
                    x = PingHud.clamp((int)((double)mx - dragOffX), 4, maxX);
                    y = PingHud.clamp((int)((double)my - dragOffY), 4, maxY);
                    GuiClient.CONFIG.pingHudX = x;
                    GuiClient.CONFIG.pingHudY = y;
                }
                lastDown = down;
            }
        } else {
            dragging = false;
            lastDown = false;
        }
        if (editActive) {
            HudEditScreen.setBounds(HudEditScreen.HudItem.PING, x, y, boxW, boxH);
        } else {
            HudEditScreen.clearBounds(HudEditScreen.HudItem.PING);
        }
        int accent = Theme.accentColor(System.currentTimeMillis());
        boolean useGuiColor = Boolean.TRUE.equals(GuiClient.CONFIG.pingUseGuiColor);
        float frameAlpha = HudEditScreen.getFrameAlpha(HudEditScreen.HudItem.PING);
        int textColor = Theme.withAlpha(accent, 255);
        float radius = Math.min(8.0f, (float)rawH * 0.5f);
        ctx.getMatrices().push();
        ctx.getMatrices().translate((float)x, (float)y, 0.0f);
        ctx.getMatrices().scale(scale, scale, 1.0f);
        HudGlass.panelNoBorder(ctx, 0, 0, rawW, rawH, radius, accent, frameAlpha, useGuiColor);
        int iconX = padX;
        int iconY = (rawH - iconSize) / 2;
        PingHud.drawSignalIcon(ctx, iconX, iconY, iconSize, textColor);
        int textX = iconX + iconSize + gap;
        Objects.requireNonNull(tr);
        int textY = (rawH - 9) / 2;
        ctx.drawText(tr, (String)text, textX, textY, textColor, false);
        ctx.getMatrices().pop();
    }

    private static void drawSignalIcon(DrawContext ctx, int x, int y, int size, int color) {
        int barW = Math.max(1, size / 4);
        int gap = Math.max(1, size / 10);
        int baseY = y + size - 1;
        int h1 = Math.max(2, size / 3);
        int h2 = Math.max(3, size / 2);
        int h3 = Math.max(4, size - 1);
        ctx.fill(x, baseY - h1, x + barW, baseY, color);
        ctx.fill(x + barW + gap, baseY - h2, x + barW * 2 + gap, baseY, color);
        ctx.fill(x + barW * 2 + gap * 2, baseY - h3, x + barW * 3 + gap * 2, baseY, color);
    }

    private static void savePosition() {
        String active = GuiClient.CONFIG.activeConfig;
        if (active == null || active.isBlank()) {
            active = "default";
        }
        GuiClient.CONFIGS.save(active);
    }

    private static int scaledMouseX(MinecraftClient client) {
        Window win = client.getWindow();
        return (int)(client.mouse.getX() * (double)win.getScaledWidth() / (double)win.getWidth());
    }

    private static int scaledMouseY(MinecraftClient client) {
        Window win = client.getWindow();
        return (int)(client.mouse.getY() * (double)win.getScaledHeight() / (double)win.getHeight());
    }

    private static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }

    private static int clamp(int v, int a, int b) {
        return Math.max(a, Math.min(b, v));
    }

    private static float clamp(float v, float a, float b) {
        return Math.max(a, Math.min(b, v));
    }
}

