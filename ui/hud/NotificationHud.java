package me.Gui.gui.ui.hud;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import me.Gui.gui.client.GuiClient;
import me.Gui.gui.modules.PanicModeUtil;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public final class NotificationHud {
    private static final List<Note> NOTES = new ArrayList<Note>();
    private static final long FADE_IN_MS = 180L;
    private static final long HOLD_MS = 1200L;
    private static final long FADE_OUT_MS = 260L;
    private static final long LONG_HOLD_MS = 2000L;
    private static final int MAX_NOTES = 6;
    private static final int DRAG_MARGIN = 4;
    private static boolean dragging = false;
    private static boolean lastDown = false;
    private static double dragOffX;
    private static double dragOffY;

    private NotificationHud() {
    }

    public static void post(String moduleName, boolean enabled) {
        if (moduleName == null || moduleName.isBlank()) {
            return;
        }
        long now = System.currentTimeMillis();
        NOTES.add(new Note(moduleName.trim(), enabled, now, null, null, 1200L));
        if (NOTES.size() > 6) {
            NOTES.remove(0);
        }
    }

    public static void postCustomItem(String text, Item iconItem) {
        if (text == null || text.isBlank()) {
            return;
        }
        long now = System.currentTimeMillis();
        NOTES.add(new Note("", true, now, text.trim(), iconItem, 2000L));
        if (NOTES.size() > 6) {
            NOTES.remove(0);
        }
    }

    public static void postCustomText(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        long now = System.currentTimeMillis();
        NOTES.add(new Note("", true, now, text.trim(), null, 2000L));
        if (NOTES.size() > 6) {
            NOTES.remove(0);
        }
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }
        boolean editActive = HudEditScreen.isActiveFor(HudEditScreen.HudItem.NOTIFICATIONS);
        if (HudEditScreen.isActive() && !editActive) {
            return;
        }
        if (!editActive && !PanicModeUtil.allowNotifications()) {
            return;
        }
        if (!editActive) {
            dragging = false;
            lastDown = false;
            HudEditScreen.clearBounds(HudEditScreen.HudItem.NOTIFICATIONS);
        }
        long now = System.currentTimeMillis();
        if (editActive) {
            NotificationHud.renderPreview(ctx, now);
            return;
        }
        NotificationHud.pruneExpired(now);
        if (NOTES.isEmpty()) {
            return;
        }
        TextRenderer tr = GuiFonts.textRendererOrDefault(client.textRenderer);
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        int padX = 6;
        int padY = 4;
        int gapY = 4;
        int iconGap = 6;
        float scale = GuiClient.CONFIG.notificationsScale;
        if (Float.isNaN(scale) || Float.isInfinite(scale)) {
            scale = 0.5f;
        }
        scale = Math.max(0.35f, Math.min(1.2f, scale));
        ArrayList<DrawNote> draw = new ArrayList<DrawNote>();
        for (Note n : NOTES) {
            float alpha = NotificationHud.alphaFor(n, now - n.startMs);
            if (alpha <= 0.0f) continue;
            String text = n.text();
            int textW = tr.getWidth(text);
            boolean itemIcon = n.iconItem != null;
            int iconW = itemIcon ? 16 : 14;
            int iconH = itemIcon ? 16 : 8;
            Objects.requireNonNull(tr);
            int boxH = Math.max(9, iconH) + padY * 2;
            int boxW = padX * 2 + iconW + iconGap + textW;
            draw.add(new DrawNote(n, text, alpha, boxW, boxH, iconW, iconH));
        }
        if (draw.isEmpty()) {
            return;
        }
        int maxScaledW = 0;
        int totalScaledH = 0;
        for (int i = 0; i < draw.size(); ++i) {
            DrawNote dn = (DrawNote)draw.get(i);
            int scaledW = Math.round((float)dn.boxW * scale);
            int scaledH = Math.round((float)dn.boxH * scale);
            maxScaledW = Math.max(maxScaledW, scaledW);
            totalScaledH += scaledH;
            if (i + 1 >= draw.size()) continue;
            totalScaledH += Math.round((float)gapY * scale);
        }
        int centerX = NotificationHud.resolveCenterX(sw);
        int topY = NotificationHud.resolveTopY(sh);
        centerX = NotificationHud.clamp(centerX, 4 + maxScaledW / 2, sw - 4 - maxScaledW / 2);
        int y = topY = NotificationHud.clamp(topY, 4, sh - totalScaledH - 4);
        int accent = Theme.accentColor(now);
        boolean useGuiColor = Boolean.TRUE.equals(GuiClient.CONFIG.notificationsUseGuiColor);
        float frameAlpha = HudEditScreen.getFrameAlpha(HudEditScreen.HudItem.NOTIFICATIONS);
        for (DrawNote dn : draw) {
            int scaledW = Math.round((float)dn.boxW * scale);
            int scaledH = Math.round((float)dn.boxH * scale);
            int x = centerX - scaledW / 2;
            ctx.getMatrices().push();
            ctx.getMatrices().translate((float)x, (float)y, 0.0f);
            ctx.getMatrices().scale(scale, scale, 1.0f);
            float r = Math.min(6.0f, (float)dn.boxH * 0.5f);
            float panelAlpha = dn.alpha * frameAlpha;
            HudGlass.panelNoBorder(ctx, 0, 0, dn.boxW, dn.boxH, r, accent, panelAlpha, useGuiColor);
            int iconX = padX;
            int iconY = (dn.boxH - dn.iconH) / 2;
            if (dn.note.iconItem != null) {
                ctx.drawItemWithoutEntity(new ItemStack((ItemConvertible)dn.note.iconItem), iconX, iconY);
            } else {
                NotificationHud.drawToggleIcon(ctx, iconX, iconY, dn.iconW, dn.iconH, dn.note.enabled, dn.alpha, accent);
            }
            int textX = iconX + dn.iconW + iconGap;
            int n = dn.boxH;
            Objects.requireNonNull(tr);
            int textY = (n - 9) / 2;
            int textA = (int)(dn.alpha * 255.0f);
            int textCol = dn.note.enabled ? Theme.withAlpha(accent, textA) : Theme.argb(textA, 210, 210, 218);
            ctx.drawText(tr, dn.text, textX, textY, textCol, false);
            ctx.getMatrices().pop();
            y += Math.round((float)(dn.boxH + gapY) * scale);
        }
    }

    private static void renderPreview(DrawContext ctx, long now) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        TextRenderer tr = GuiFonts.textRendererOrDefault(client.textRenderer);
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        int padX = 6;
        int padY = 4;
        int gapY = 4;
        int iconGap = 6;
        float scale = GuiClient.CONFIG.notificationsScale;
        if (Float.isNaN(scale) || Float.isInfinite(scale)) {
            scale = 0.5f;
        }
        scale = Math.max(0.35f, Math.min(1.2f, scale));
        String text = "Notifications";
        int textW = tr.getWidth(text);
        int iconW = 14;
        int iconH = 8;
        Objects.requireNonNull(tr);
        int boxH = Math.max(9, iconH) + padY * 2;
        int boxW = padX * 2 + iconW + iconGap + textW;
        int centerX = NotificationHud.resolveCenterX(sw);
        int topY = NotificationHud.resolveTopY(sh);
        int accent = Theme.accentColor(now);
        boolean useGuiColor = Boolean.TRUE.equals(GuiClient.CONFIG.notificationsUseGuiColor);
        float frameAlpha = HudEditScreen.getFrameAlpha(HudEditScreen.HudItem.NOTIFICATIONS);
        int scaledW = Math.round((float)boxW * scale);
        int scaledH = Math.round((float)boxH * scale);
        centerX = NotificationHud.clamp(centerX, 4 + scaledW / 2, sw - 4 - scaledW / 2);
        topY = NotificationHud.clamp(topY, 4, sh - scaledH - 4);
        int x = centerX - scaledW / 2;
        int y = topY;
        int[] dragPos = NotificationHud.handleDrag(client, x, y, scaledW, scaledH, sw, sh);
        x = dragPos[0];
        y = dragPos[1];
        GuiClient.CONFIG.notificationsX = x + scaledW / 2;
        GuiClient.CONFIG.notificationsY = y;
        HudEditScreen.setBounds(HudEditScreen.HudItem.NOTIFICATIONS, x, y, scaledW, scaledH);
        ctx.getMatrices().push();
        ctx.getMatrices().translate((float)x, (float)y, 0.0f);
        ctx.getMatrices().scale(scale, scale, 1.0f);
        float r = Math.min(6.0f, (float)boxH * 0.5f);
        HudGlass.panelNoBorder(ctx, 0, 0, boxW, boxH, r, accent, frameAlpha, useGuiColor);
        int iconX = padX;
        int iconY = (boxH - iconH) / 2;
        NotificationHud.drawToggleIcon(ctx, iconX, iconY, iconW, iconH, true, 1.0f, accent);
        int textX = iconX + iconW + iconGap;
        Objects.requireNonNull(tr);
        int textY = (boxH - 9) / 2;
        ctx.drawText(tr, text, textX, textY, Theme.withAlpha(accent, 255), false);
        ctx.getMatrices().pop();
    }

    private static void drawToggleIcon(DrawContext ctx, int x, int y, int w, int h, boolean enabled, float alpha, int accent) {
        int a1 = (int)(alpha * (float)(enabled ? 190 : 150));
        int a2 = (int)(alpha * 220.0f);
        int pill = enabled ? Theme.withAlpha(accent, a1) : Theme.argb(a1, 90, 90, 100);
        int knob = Theme.argb(a2, 230, 230, 236);
        float r = (float)h * 0.5f;
        Rounded.rect(ctx, x, y, w, h, r, pill);
        int knobSize = Math.max(2, h - 2);
        int knobX = enabled ? x + w - knobSize - 1 : x + 1;
        int knobY = y + 1;
        Rounded.rect(ctx, knobX, knobY, knobSize, knobSize, (float)knobSize * 0.5f, knob);
    }

    private static void pruneExpired(long now) {
        Iterator<Note> it = NOTES.iterator();
        while (it.hasNext()) {
            Note n = it.next();
            long total = 180L + n.holdMs + 260L;
            if (now - n.startMs < total) continue;
            it.remove();
        }
    }

    private static float alphaFor(Note note, long ageMs) {
        if (ageMs <= 0L) {
            return 0.0f;
        }
        if (ageMs < 180L) {
            return (float)ageMs / 180.0f;
        }
        long mid = 180L + note.holdMs;
        if (ageMs < mid) {
            return 1.0f;
        }
        long end = mid + 260L;
        if (ageMs >= end) {
            return 0.0f;
        }
        return 1.0f - (float)(ageMs - mid) / 260.0f;
    }

    private static int resolveCenterX(int sw) {
        Integer cfg = GuiClient.CONFIG.notificationsX;
        if (cfg == null) {
            return sw / 2;
        }
        return NotificationHud.clamp(cfg, 4, sw - 4);
    }

    private static int resolveTopY(int sh) {
        Integer cfg = GuiClient.CONFIG.notificationsY;
        if (cfg == null) {
            return (int)((float)sh * 0.6f);
        }
        return NotificationHud.clamp(cfg, 4, sh - 4);
    }

    private static int[] handleDrag(MinecraftClient client, int x, int y, int w, int h, int sw, int sh) {
        Window win = client.getWindow();
        long handle = win.getHandle();
        if (handle != 0L) {
            boolean down;
            int mx = NotificationHud.scaledMouseX(client);
            int my = NotificationHud.scaledMouseY(client);
            boolean bl = down = GLFW.glfwGetMouseButton((long)handle, (int)0) == 1;
            if (down && !lastDown && NotificationHud.inside(mx, my, x, y, w, h)) {
                dragging = true;
                dragOffX = mx - x;
                dragOffY = my - y;
            }
            if (!down && lastDown && dragging) {
                dragging = false;
                NotificationHud.savePosition();
            }
            if (dragging) {
                int maxX = Math.max(4, sw - w - 4);
                int maxY = Math.max(4, sh - h - 4);
                x = NotificationHud.clamp((int)Math.round((double)mx - dragOffX), 4, maxX);
                y = NotificationHud.clamp((int)Math.round((double)my - dragOffY), 4, maxY);
            }
            lastDown = down;
        }
        return new int[]{x, y};
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

    private static void savePosition() {
        String active = GuiClient.CONFIG.activeConfig;
        if (active == null || active.isBlank()) {
            active = "default";
        }
        GuiClient.CONFIGS.save(active);
    }

    private record Note(String moduleName, boolean enabled, long startMs, String customText, Item iconItem, long holdMs) {
        String text() {
            return this.customText != null && !this.customText.isBlank() ? this.customText : this.moduleName + (this.enabled ? " has been enabled" : " has been disabled");
        }
    }

    private record DrawNote(Note note, String text, float alpha, int boxW, int boxH, int iconW, int iconH) {
    }
}

