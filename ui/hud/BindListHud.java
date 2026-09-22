package me.Gui.gui.ui.hud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import me.Gui.gui.client.GuiClient;
import me.Gui.gui.modules.BindUtil;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
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
import org.lwjgl.glfw.GLFW;

public final class BindListHud {
    private static boolean dragging = false;
    private static boolean lastDown = false;
    private static double dragOffX;
    private static double dragOffY;
    private static final Map<ModuleId, Float> bindAnim;
    private static final float BIND_ANIM_SPEED = 0.35f;
    private static final float BIND_SLIDE_PX = 10.0f;
    private static final float BIND_ANIM_EPS = 0.01f;

    private BindListHud() {
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        int headerW;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }
        boolean editActive = HudEditScreen.isActiveFor(HudEditScreen.HudItem.KEYBINDS);
        if (HudEditScreen.isActive() && !editActive) {
            return;
        }
        if (!editActive && !PanicModeUtil.allowKeybinds()) {
            return;
        }
        boolean onlyEnabled = GuiClient.CONFIG.keybindsOnlyEnabled;
        ArrayList<BindEntry> lines = new ArrayList<BindEntry>();
        for (HackModule m : GuiClient.MODULES.all()) {
            if (m.id.hidden) {
                bindAnim.remove((Object)m.id);
                continue;
            }
            if (m.id == ModuleId.ARMOR_EQUIPPER || m.id == ModuleId.ANTYKOSTKA || m.id == ModuleId.ERROR_KILLER) {
                bindAnim.remove((Object)m.id);
                continue;
            }
            int key = m.getBindKey();
            if (key <= 0) {
                bindAnim.remove((Object)m.id);
                continue;
            }
            boolean show = !onlyEnabled || m.isEnabled();
            float anim = BindListHud.animateBind(m.id, show ? 1.0f : 0.0f);
            if (anim <= 0.01f && !show) continue;
            String name = m.id.display == null ? "" : m.id.display;
            lines.add(new BindEntry(name, BindListHud.formatKey(key), m.isEnabled(), anim));
        }
        if (lines.isEmpty() && editActive) {
            lines.add(new BindEntry("KillAura", "RMB", true, 1.0f));
            lines.add(new BindEntry("Fly", "G", true, 1.0f));
            if (!onlyEnabled) {
                lines.add(new BindEntry("AutoTotem", "V", false, 1.0f));
            }
        }
        if (lines.isEmpty()) {
            return;
        }
        TextRenderer tr = GuiFonts.textRendererOrDefault(client.textRenderer);
        int pad = 4;
        float headerScale = 1.25f;
        int keyGap = 6;
        int keyBoxPadX = 5;
        int keyBoxPadY = 3;
        int namePadX = 6;
        int namePadY = 3;
        Objects.requireNonNull(tr);
        Objects.requireNonNull(tr);
        int lineH = Math.max(9 + keyBoxPadY * 2 + 4, 9 + namePadY * 2 + 4);
        String title = "Keybinds";
        int headerTextW = Math.round((float)tr.getWidth(title) * headerScale);
        Objects.requireNonNull(tr);
        int headerTextH = Math.round(9.0f * headerScale);
        int headerH = headerTextH + 8;
        int maxW = headerW = headerTextW + pad * 2 + 4;
        for (BindEntry e : lines) {
            int nameW;
            int keyW = tr.getWidth(e.key);
            int keyBoxW = keyW + keyBoxPadX * 2;
            int rowW = keyBoxW + keyGap + (nameW = tr.getWidth(e.module) + namePadX * 2);
            if (rowW <= maxW) continue;
            maxW = rowW;
        }
        int boxW = maxW + pad * 2;
        int boxH = headerH + pad + lines.size() * lineH + pad;
        float scale = GuiClient.CONFIG.bindListScale;
        if (Float.isNaN(scale) || Float.isInfinite(scale)) {
            scale = 0.9f;
        }
        scale = Math.max(0.5f, Math.min(1.4f, scale));
        int x = GuiClient.CONFIG.bindListX;
        int y = GuiClient.CONFIG.bindListY;
        int scaledW = Math.round((float)boxW * scale);
        int scaledBoxH = Math.round((float)boxH * scale);
        int scaledHeaderH = Math.round((float)headerH * scale);
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        int maxX = Math.max(4, sw - scaledW - 4);
        int maxHeaderY = Math.max(4, sh - scaledHeaderH - 4);
        x = BindListHud.clamp(x, 4, maxX);
        y = BindListHud.clamp(y, 4, maxHeaderY);
        GuiClient.CONFIG.bindListX = x;
        GuiClient.CONFIG.bindListY = y;
        if (editActive) {
            Window win = client.getWindow();
            long handle = win.getHandle();
            if (handle != 0L) {
                boolean down;
                int mx = BindListHud.scaledMouseX(client);
                int my = BindListHud.scaledMouseY(client);
                boolean bl = down = GLFW.glfwGetMouseButton((long)handle, (int)0) == 1;
                if (down && !lastDown && BindListHud.inside(mx, my, x, y, scaledW, scaledHeaderH)) {
                    dragging = true;
                    dragOffX = mx - x;
                    dragOffY = my - y;
                }
                if (!down && lastDown && dragging) {
                    dragging = false;
                    BindListHud.savePosition();
                }
                if (dragging) {
                    x = BindListHud.clamp((int)((double)mx - dragOffX), 4, maxX);
                    y = BindListHud.clamp((int)((double)my - dragOffY), 4, maxHeaderY);
                    GuiClient.CONFIG.bindListX = x;
                    GuiClient.CONFIG.bindListY = y;
                }
                lastDown = down;
            }
        } else {
            dragging = false;
            lastDown = false;
        }
        if (editActive) {
            HudEditScreen.setBounds(HudEditScreen.HudItem.KEYBINDS, x, y, scaledW, scaledBoxH);
        } else {
            HudEditScreen.clearBounds(HudEditScreen.HudItem.KEYBINDS);
        }
        int accentRgb = Theme.accentColor(System.currentTimeMillis());
        int accent = Theme.withAlpha(accentRgb, 170);
        boolean useGuiColor = Boolean.TRUE.equals(GuiClient.CONFIG.keybindsUseGuiColor);
        float frameAlpha = HudEditScreen.getFrameAlpha(HudEditScreen.HudItem.KEYBINDS);
        float panelR = 6.0f;
        ctx.getMatrices().push();
        ctx.getMatrices().translate((float)x, (float)y, 0.0f);
        ctx.getMatrices().scale(scale, scale, 1.0f);
        int shadow1 = BindListHud.scaleAlpha(Theme.argb(45, 0, 0, 0), frameAlpha);
        int shadow2 = BindListHud.scaleAlpha(Theme.argb(25, 0, 0, 0), frameAlpha);
        Rounded.rect(ctx, -2, -2, headerW + 4, headerH + 4, panelR + 2.0f, shadow2);
        Rounded.rect(ctx, -1, -1, headerW + 2, headerH + 2, panelR + 1.0f, shadow1);
        HudGlass.panelNoBorder(ctx, 0, 0, headerW, headerH, panelR, accentRgb, frameAlpha, useGuiColor);
        int ty = (headerH - headerTextH) / 2;
        ctx.getMatrices().push();
        ctx.getMatrices().translate((float)(pad + 2), (float)ty, 0.0f);
        ctx.getMatrices().scale(headerScale, headerScale, 1.0f);
        ctx.drawText(tr, title, 0, 0, -1, false);
        ctx.getMatrices().pop();
        int yy = headerH + pad;
        for (BindEntry e : lines) {
            float anim = BindListHud.clamp01(e.anim);
            if (anim <= 0.01f) {
                yy += lineH;
                continue;
            }
            int activeColor = onlyEnabled ? accent : -11151510;
            int col = e.enabled ? activeColor : -6643544;
            col = BindListHud.scaleAlpha(col, anim);
            int keyW = tr.getWidth(e.key);
            int keyBoxW = keyW + keyBoxPadX * 2;
            Objects.requireNonNull(tr);
            int keyBoxH = 9 + keyBoxPadY * 2;
            int rowOffset = Math.round(-10.0f * (1.0f - anim));
            int keyX = pad + rowOffset;
            int keyY = yy + (lineH - keyBoxH) / 2;
            int keyBg = BindListHud.scaleAlpha(HudGlass.glassFill(accentRgb, 110, useGuiColor), anim * frameAlpha);
            Rounded.rect(ctx, keyX, keyY, keyBoxW, keyBoxH, 6.0f, keyBg);
            ctx.drawText(tr, e.key, keyX + keyBoxPadX, keyY + keyBoxPadY, col, false);
            int mx = keyX + keyBoxW + keyGap;
            int labelW = tr.getWidth(e.module) + namePadX * 2;
            Objects.requireNonNull(tr);
            int labelH = 9 + namePadY * 2;
            int labelY = yy + (lineH - labelH) / 2;
            int labelBg = BindListHud.scaleAlpha(HudGlass.glassFill(accentRgb, 110, useGuiColor), anim * frameAlpha);
            Rounded.rect(ctx, mx, labelY, labelW, labelH, 6.0f, labelBg);
            String string = e.module;
            Objects.requireNonNull(tr);
            ctx.drawText(tr, string, mx + namePadX, yy + (lineH - 9) / 2, col, false);
            yy += lineH;
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

    private static String formatKey(int key) {
        if (key <= 0) {
            return "None";
        }
        if (BindUtil.isMouseBind(key)) {
            int btn = BindUtil.mouseButton(key);
            return "MB" + (btn + 1);
        }
        switch (key) {
            case 290: {
                return "F1";
            }
            case 291: {
                return "F2";
            }
            case 292: {
                return "F3";
            }
            case 293: {
                return "F4";
            }
            case 294: {
                return "F5";
            }
            case 295: {
                return "F6";
            }
            case 296: {
                return "F7";
            }
            case 297: {
                return "F8";
            }
            case 298: {
                return "F9";
            }
            case 299: {
                return "F10";
            }
            case 300: {
                return "F11";
            }
            case 301: {
                return "F12";
            }
            case 302: {
                return "F13";
            }
            case 303: {
                return "F14";
            }
            case 304: {
                return "F15";
            }
            case 305: {
                return "F16";
            }
            case 306: {
                return "F17";
            }
            case 307: {
                return "F18";
            }
            case 308: {
                return "F19";
            }
            case 309: {
                return "F20";
            }
            case 310: {
                return "F21";
            }
            case 311: {
                return "F22";
            }
            case 312: {
                return "F23";
            }
            case 313: {
                return "F24";
            }
            case 314: {
                return "F25";
            }
            case 258: {
                return "TAB";
            }
            case 32: {
                return "SPACE";
            }
            case 340: {
                return "LSHIFT";
            }
            case 344: {
                return "RSHIFT";
            }
            case 341: {
                return "LCTRL";
            }
            case 345: {
                return "RCTRL";
            }
            case 342: {
                return "LALT";
            }
            case 346: {
                return "RALT";
            }
            case 256: {
                return "ESC";
            }
            case 257: {
                return "ENTER";
            }
            case 259: {
                return "BACKSPACE";
            }
            case 261: {
                return "DEL";
            }
            case 260: {
                return "INS";
            }
            case 268: {
                return "HOME";
            }
            case 269: {
                return "END";
            }
            case 266: {
                return "PGUP";
            }
            case 267: {
                return "PGDN";
            }
            case 265: {
                return "UP";
            }
            case 264: {
                return "DOWN";
            }
            case 263: {
                return "LEFT";
            }
            case 262: {
                return "RIGHT";
            }
        }
        String n = GLFW.glfwGetKeyName((int)key, (int)0);
        if (n != null && !n.isBlank()) {
            return n.toUpperCase();
        }
        return "KEY_" + key;
    }

    private static float animateBind(ModuleId id, float target) {
        float cur = bindAnim.getOrDefault((Object)id, Float.valueOf(target)).floatValue();
        if (Math.abs((cur = BindListHud.lerp(cur, target, 0.35f)) - target) < 0.01f) {
            cur = target;
        }
        bindAnim.put(id, Float.valueOf(cur));
        return cur;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float clamp01(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    private static int scaleAlpha(int argb, float t) {
        int a = argb >>> 24 & 0xFF;
        int outA = Math.round((float)a * BindListHud.clamp01(t));
        return Theme.withAlpha(argb, outA);
    }

    static {
        bindAnim = new HashMap<ModuleId, Float>();
    }

    private record BindEntry(String module, String key, boolean enabled, float anim) {
    }
}

