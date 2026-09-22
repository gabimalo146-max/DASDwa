package me.Gui.gui.ui.render;

import me.Gui.gui.ui.Theme;
import me.Gui.gui.ui.render.Rounded;
import net.minecraft.client.gui.DrawContext;

public final class HudGlass {
    private HudGlass() {
    }

    public static void panel(DrawContext ctx, int x, int y, int w, int h, float radius, int accent) {
        HudGlass.panel(ctx, x, y, w, h, radius, accent, 1.0f, false);
    }

    public static void panel(DrawContext ctx, int x, int y, int w, int h, float radius, int accent, float alpha) {
        HudGlass.panel(ctx, x, y, w, h, radius, accent, alpha, false);
    }

    public static void panel(DrawContext ctx, int x, int y, int w, int h, float radius, int accent, float alpha, boolean useGuiColor) {
        if (w <= 0 || h <= 0) {
            return;
        }
        float a = HudGlass.clamp01(alpha);
        int base = HudGlass.scaleAlpha(Theme.argb(125, 22, 22, 26), a);
        int border = HudGlass.scaleAlpha(Theme.argb(140, 0, 0, 0), a);
        int outerShadow = HudGlass.scaleAlpha(Theme.argb(45, 0, 0, 0), a);
        int tint = useGuiColor ? HudGlass.scaleAlpha(Theme.withAlpha(accent, 35), a) : 0;
        int glow = useGuiColor ? HudGlass.scaleAlpha(Theme.withAlpha(accent, 70), a) : 0;
        Rounded.rect(ctx, x - 1, y - 1, w + 2, h + 2, radius + 1.0f, outerShadow);
        Rounded.rect(ctx, x, y, w, h, radius, base);
        if (useGuiColor && w > 2 && h > 2) {
            Rounded.rect(ctx, x + 1, y + 1, w - 2, h - 2, Math.max(0.0f, radius - 1.0f), tint);
        }
        Rounded.outline(ctx, x, y, w, h, radius, 1, border);
        if (useGuiColor && w > 2 && h > 2) {
            Rounded.outline(ctx, x + 1, y + 1, w - 2, h - 2, Math.max(0.0f, radius - 1.0f), 1, glow);
        }
    }

    public static void panelNoBorder(DrawContext ctx, int x, int y, int w, int h, float radius, int accent, float alpha, boolean useGuiColor) {
        if (w <= 0 || h <= 0) {
            return;
        }
        float a = HudGlass.clamp01(alpha);
        int base = HudGlass.scaleAlpha(Theme.argb(125, 22, 22, 26), a);
        int tint = useGuiColor ? HudGlass.scaleAlpha(Theme.withAlpha(accent, 35), a) : 0;
        Rounded.rect(ctx, x, y, w, h, radius, base);
        if (useGuiColor && w > 2 && h > 2) {
            Rounded.rect(ctx, x + 1, y + 1, w - 2, h - 2, Math.max(0.0f, radius - 1.0f), tint);
        }
    }

    public static int glassFill(int alpha) {
        return Theme.argb(alpha, 22, 22, 26);
    }

    public static int glassFill(int accent, int alpha, boolean useGuiColor) {
        if (!useGuiColor) {
            return HudGlass.glassFill(alpha);
        }
        int rgb = HudGlass.mixRgb(1973796, accent, 0.18f);
        return Theme.argb(alpha, rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
    }

    private static int mixRgb(int a, int b, float t) {
        float tt = HudGlass.clamp01(t);
        int ar = a >> 16 & 0xFF;
        int ag = a >> 8 & 0xFF;
        int ab = a & 0xFF;
        int br = b >> 16 & 0xFF;
        int bg = b >> 8 & 0xFF;
        int bb = b & 0xFF;
        int rr = HudGlass.mixChannel(ar, br, tt);
        int gg = HudGlass.mixChannel(ag, bg, tt);
        int bb2 = HudGlass.mixChannel(ab, bb, tt);
        return rr << 16 | gg << 8 | bb2;
    }

    private static int mixChannel(int a, int b, float t) {
        return HudGlass.clamp255(Math.round((float)a + (float)(b - a) * t));
    }

    private static int scaleAlpha(int argb, float mul) {
        if (mul <= 0.0f) {
            return 0;
        }
        int a = argb >>> 24 & 0xFF;
        int outA = Math.round((float)a * mul);
        return Theme.withAlpha(argb, outA);
    }

    private static float clamp01(float v) {
        if (v < 0.0f) {
            return 0.0f;
        }
        if (v > 1.0f) {
            return 1.0f;
        }
        return v;
    }

    private static int clamp255(int v) {
        if (v < 0) {
            return 0;
        }
        if (v > 255) {
            return 255;
        }
        return v;
    }
}

