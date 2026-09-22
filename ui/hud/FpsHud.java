package me.Gui.gui.ui.hud;

import java.util.Objects;
import me.Gui.gui.client.GuiClient;
import me.Gui.gui.ui.GuiFonts;
import me.Gui.gui.ui.HudEditScreen;
import me.Gui.gui.ui.Theme;
import me.Gui.gui.ui.render.HudGlass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;

public final class FpsHud {
    private static boolean dragging = false;
    private static boolean lastDown = false;
    private static double dragOffX;
    private static double dragOffY;

    private FpsHud() {
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        boolean editActive = HudEditScreen.isActiveFor(HudEditScreen.HudItem.FPS);
        if (HudEditScreen.isActive() && !editActive) {
            return;
        }
        if (!editActive && !Boolean.TRUE.equals(GuiClient.CONFIG.showFpsHud)) {
            return;
        }
        int fps = MinecraftClient.getInstance().getCurrentFps();
        String text = "FPS " + fps;
        TextRenderer tr = GuiFonts.textRendererOrDefault(client.textRenderer);
        int padX = 8;
        int padY = 4;
        int rawW = padX * 2 + tr.getWidth(text);
        Objects.requireNonNull(tr);
        int rawH = 9 + padY * 2;
        float scale = GuiClient.CONFIG.fpsHudScale;
        if (Float.isNaN(scale) || Float.isInfinite(scale)) {
            scale = 0.9f;
        }
        scale = FpsHud.clamp(scale, 0.5f, 1.4f);
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        int boxW = Math.round((float)rawW * scale);
        int boxH = Math.round((float)rawH * scale);
        int x = GuiClient.CONFIG.fpsHudX;
        int y = GuiClient.CONFIG.fpsHudY;
        int maxX = Math.max(4, sw - boxW - 4);
        int maxY = Math.max(4, sh - boxH - 4);
        x = FpsHud.clamp(x, 4, maxX);
        y = FpsHud.clamp(y, 4, maxY);
        GuiClient.CONFIG.fpsHudX = x;
        GuiClient.CONFIG.fpsHudY = y;
        if (editActive) {
            Window win = client.getWindow();
            long handle = win.getHandle();
            if (handle != 0L) {
                boolean down;
                int mx = FpsHud.scaledMouseX(client);
                int my = FpsHud.scaledMouseY(client);
                boolean bl = down = GLFW.glfwGetMouseButton((long)handle, (int)0) == 1;
                if (down && !lastDown && FpsHud.inside(mx, my, x, y, boxW, boxH)) {
                    dragging = true;
                    dragOffX = mx - x;
                    dragOffY = my - y;
                }
                if (!down && lastDown && dragging) {
                    dragging = false;
                    FpsHud.savePosition();
                }
                if (dragging) {
                    x = FpsHud.clamp((int)((double)mx - dragOffX), 4, maxX);
                    y = FpsHud.clamp((int)((double)my - dragOffY), 4, maxY);
                    GuiClient.CONFIG.fpsHudX = x;
                    GuiClient.CONFIG.fpsHudY = y;
                }
                lastDown = down;
            }
        } else {
            dragging = false;
            lastDown = false;
        }
        if (editActive) {
            HudEditScreen.setBounds(HudEditScreen.HudItem.FPS, x, y, boxW, boxH);
        } else {
            HudEditScreen.clearBounds(HudEditScreen.HudItem.FPS);
        }
        int accent = Theme.accentColor(System.currentTimeMillis());
        boolean useGuiColor = Boolean.TRUE.equals(GuiClient.CONFIG.fpsUseGuiColor);
        float frameAlpha = HudEditScreen.getFrameAlpha(HudEditScreen.HudItem.FPS);
        int textColor = Theme.withAlpha(accent, 255);
        float radius = Math.min(8.0f, (float)rawH * 0.5f);
        ctx.getMatrices().push();
        ctx.getMatrices().translate((float)x, (float)y, 0.0f);
        ctx.getMatrices().scale(scale, scale, 1.0f);
        HudGlass.panelNoBorder(ctx, 0, 0, rawW, rawH, radius, accent, frameAlpha, useGuiColor);
        int textX = padX;
        Objects.requireNonNull(tr);
        int textY = (rawH - 9) / 2;
        ctx.drawText(tr, text, textX, textY, textColor, false);
        ctx.getMatrices().pop();
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

