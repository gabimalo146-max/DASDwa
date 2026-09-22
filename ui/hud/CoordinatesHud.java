package me.Gui.gui.ui.hud;

import java.util.Objects;
import me.Gui.gui.client.GuiClient;
import me.Gui.gui.ui.GuiFonts;
import me.Gui.gui.ui.HudEditScreen;
import me.Gui.gui.ui.Theme;
import me.Gui.gui.ui.render.HudGlass;
import me.Gui.gui.ui.render.Rounded;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public final class CoordinatesHud {
    private static boolean dragging = false;
    private static boolean lastDown = false;
    private static double dragOffX;
    private static double dragOffY;

    private CoordinatesHud() {
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        int padY;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        boolean editActive = HudEditScreen.isActiveFor(HudEditScreen.HudItem.COORDINATES);
        if (HudEditScreen.isActive() && !editActive) {
            return;
        }
        if (!editActive && !Boolean.TRUE.equals(GuiClient.CONFIG.showCoordinatesHud)) {
            return;
        }
        TextRenderer tr = GuiFonts.textRendererOrDefault(client.textRenderer);
        String sx = "61";
        String sy = "130";
        String sz = "229";
        if (client.player != null) {
            BlockPos pos = client.player.getBlockPos();
            sx = Integer.toString(pos.getX());
            sy = Integer.toString(pos.getY());
            sz = Integer.toString(pos.getZ());
        }
        int dotSize = 4;
        int sepGap = 10;
        int padX = 10;
        int textY = padY = 4;
        int wX = tr.getWidth(sx);
        int wY = tr.getWidth(sy);
        int wZ = tr.getWidth(sz);
        Objects.requireNonNull(tr);
        int rawH = 9 + padY * 2;
        int rawW = padX * 2 + wX + wY + wZ + sepGap * 2;
        float scale = GuiClient.CONFIG.coordinatesScale;
        if (Float.isNaN(scale) || Float.isInfinite(scale)) {
            scale = 0.9f;
        }
        scale = CoordinatesHud.clamp(scale, 0.5f, 1.4f);
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        int boxW = Math.round((float)rawW * scale);
        int boxH = Math.round((float)rawH * scale);
        int x = GuiClient.CONFIG.coordinatesX;
        int y = GuiClient.CONFIG.coordinatesY;
        int maxX = Math.max(4, sw - boxW - 4);
        int maxY = Math.max(4, sh - boxH - 4);
        x = CoordinatesHud.clamp(x, 4, maxX);
        y = CoordinatesHud.clamp(y, 4, maxY);
        GuiClient.CONFIG.coordinatesX = x;
        GuiClient.CONFIG.coordinatesY = y;
        if (editActive) {
            Window win = client.getWindow();
            long handle = win.getHandle();
            if (handle != 0L) {
                boolean down;
                int mx = CoordinatesHud.scaledMouseX(client);
                int my = CoordinatesHud.scaledMouseY(client);
                boolean bl = down = GLFW.glfwGetMouseButton((long)handle, (int)0) == 1;
                if (down && !lastDown && CoordinatesHud.inside(mx, my, x, y, boxW, boxH)) {
                    dragging = true;
                    dragOffX = mx - x;
                    dragOffY = my - y;
                }
                if (!down && lastDown && dragging) {
                    dragging = false;
                    CoordinatesHud.savePosition();
                }
                if (dragging) {
                    x = CoordinatesHud.clamp((int)((double)mx - dragOffX), 4, maxX);
                    y = CoordinatesHud.clamp((int)((double)my - dragOffY), 4, maxY);
                    GuiClient.CONFIG.coordinatesX = x;
                    GuiClient.CONFIG.coordinatesY = y;
                }
                lastDown = down;
            }
        } else {
            dragging = false;
            lastDown = false;
        }
        if (editActive) {
            HudEditScreen.setBounds(HudEditScreen.HudItem.COORDINATES, x, y, boxW, boxH);
        } else {
            HudEditScreen.clearBounds(HudEditScreen.HudItem.COORDINATES);
        }
        int accent = Theme.accentColor(System.currentTimeMillis());
        boolean useGuiColor = Boolean.TRUE.equals(GuiClient.CONFIG.coordinatesUseGuiColor);
        float frameAlpha = HudEditScreen.getFrameAlpha(HudEditScreen.HudItem.COORDINATES);
        int textColor = Theme.withAlpha(accent, 255);
        int dotColor = Theme.withAlpha(accent, 230);
        float radius = Math.min(10.0f, (float)rawH * 0.5f);
        ctx.getMatrices().push();
        ctx.getMatrices().translate((float)x, (float)y, 0.0f);
        ctx.getMatrices().scale(scale, scale, 1.0f);
        HudGlass.panelNoBorder(ctx, 0, 0, rawW, rawH, radius, accent, frameAlpha, useGuiColor);
        int centerY = rawH / 2;
        int cx = padX;
        ctx.drawText(tr, sx, cx, textY, textColor, false);
        int dotX = (cx += wX) + (sepGap - dotSize) / 2;
        Rounded.rect(ctx, dotX, centerY - dotSize / 2, dotSize, dotSize, (float)dotSize * 0.5f, dotColor);
        ctx.drawText(tr, sy, cx += sepGap, textY, textColor, false);
        dotX = (cx += wY) + (sepGap - dotSize) / 2;
        Rounded.rect(ctx, dotX, centerY - dotSize / 2, dotSize, dotSize, (float)dotSize * 0.5f, dotColor);
        ctx.drawText(tr, sz, cx += sepGap, textY, textColor, false);
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

