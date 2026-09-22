package me.Gui.gui.ui.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import me.Gui.gui.client.GuiClient;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.PanicModeUtil;
import me.Gui.gui.ui.GuiFonts;
import me.Gui.gui.ui.HudEditScreen;
import me.Gui.gui.ui.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;

public final class ArrayListHud {
    private static boolean dragging = false;
    private static boolean lastDown = false;
    private static double dragOffX;
    private static double dragOffY;

    private ArrayListHud() {
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }
        boolean editActive = HudEditScreen.isActiveFor(HudEditScreen.HudItem.ARRAY_LIST);
        if (HudEditScreen.isActive() && !editActive) {
            return;
        }
        if (!editActive && !PanicModeUtil.allowArrayList()) {
            return;
        }
        ArrayList<String> names = new ArrayList<String>();
        for (HackModule m : GuiClient.MODULES.all()) {
            String name;
            if (m.id.hidden || !m.isEnabled() || (name = m.id.display == null ? "" : m.id.display).isBlank()) continue;
            names.add(name);
        }
        if (names.isEmpty()) {
            return;
        }
        TextRenderer tr = GuiFonts.textRendererOrDefault(client.textRenderer);
        names.sort(Comparator.comparingInt((String s) -> tr.getWidth(s)).reversed());
        int maxW = 0;
        for (String string : names) {
            maxW = Math.max(maxW, tr.getWidth(string));
        }
        float scale = GuiClient.CONFIG.arrayListScale;
        if (Float.isNaN(scale) || Float.isInfinite(scale)) {
            scale = 1.0f;
        }
        scale = Math.max(0.5f, Math.min(1.4f, scale));
        int n = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        Objects.requireNonNull(tr);
        int lineH = 9 + 3;
        int listH = lineH * names.size();
        int scaledW = Math.round((float)maxW * scale);
        int scaledH = Math.round((float)listH * scale);
        boolean right = GuiClient.CONFIG.arrayListRight == null || GuiClient.CONFIG.arrayListRight != false;
        int baseX = GuiClient.CONFIG.arrayListX;
        int baseY = GuiClient.CONFIG.arrayListY;
        int maxX = Math.max(4, n - scaledW - 4);
        int maxY = Math.max(4, sh - scaledH - 4);
        baseX = ArrayListHud.clamp(baseX, 4, maxX);
        baseY = ArrayListHud.clamp(baseY, 4, maxY);
        int left = right ? n - baseX - scaledW : baseX;
        int top = baseY;
        if (editActive) {
            Window win = client.getWindow();
            long handle = win.getHandle();
            if (handle != 0L) {
                boolean down;
                int mx = ArrayListHud.scaledMouseX(client);
                int n2 = ArrayListHud.scaledMouseY(client);
                boolean bl = down = GLFW.glfwGetMouseButton((long)handle, (int)0) == 1;
                if (down && !lastDown && ArrayListHud.inside(mx, n2, left, top, scaledW, scaledH)) {
                    dragging = true;
                    dragOffX = mx - left;
                    dragOffY = n2 - top;
                }
                if (!down && lastDown && dragging) {
                    dragging = false;
                    ArrayListHud.savePosition();
                }
                if (dragging) {
                    boolean snapRight;
                    int newLeft = ArrayListHud.clamp((int)Math.round((double)mx - dragOffX), 4, maxX);
                    int newTop = ArrayListHud.clamp((int)Math.round((double)n2 - dragOffY), 4, maxY);
                    boolean snapLeft = newLeft < 20;
                    boolean bl2 = snapRight = newLeft + scaledW > n - 20;
                    if (snapLeft) {
                        right = false;
                    } else if (snapRight) {
                        right = true;
                    }
                    GuiClient.CONFIG.arrayListRight = right;
                    baseX = right ? n - (newLeft + scaledW) : newLeft;
                    baseX = ArrayListHud.clamp(baseX, 4, maxX);
                    baseY = newTop;
                    GuiClient.CONFIG.arrayListX = baseX;
                    GuiClient.CONFIG.arrayListY = baseY;
                }
                lastDown = down;
            }
        } else {
            dragging = false;
            lastDown = false;
        }
        left = right ? n - baseX - scaledW : baseX;
        top = baseY;
        if (editActive) {
            HudEditScreen.setBounds(HudEditScreen.HudItem.ARRAY_LIST, left, top, scaledW, scaledH);
        } else {
            HudEditScreen.clearBounds(HudEditScreen.HudItem.ARRAY_LIST);
        }
        int accent = Theme.accentColor(System.currentTimeMillis());
        int color = Theme.withAlpha(accent, 230);
        ctx.getMatrices().push();
        ctx.getMatrices().translate((float)left, (float)top, 0.0f);
        ctx.getMatrices().scale(scale, scale, 1.0f);
        int y = 0;
        for (String string : names) {
            int w = tr.getWidth(string);
            int x = right ? maxW - w : 0;
            ctx.drawText(tr, string, x, y, color, false);
            y += lineH;
        }
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
}

