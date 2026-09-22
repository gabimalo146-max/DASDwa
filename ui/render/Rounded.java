package me.Gui.gui.ui.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;

public final class Rounded {
    private static float globalAlpha = 1.0f;

    private Rounded() {
    }

    public static void setGlobalAlpha(float alpha) {
        if (Float.isNaN(alpha) || Float.isInfinite(alpha)) {
            globalAlpha = 1.0f;
            return;
        }
        if (alpha < 0.0f) {
            alpha = 0.0f;
        }
        if (alpha > 1.0f) {
            alpha = 1.0f;
        }
        globalAlpha = alpha;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void rect(DrawContext ctx, int x, int y, int w, int h, float radius, int argb) {
        argb = Rounded.applyGlobalAlpha(argb);
        if (w <= 0 || h <= 0) {
            return;
        }
        if ((argb >>> 24 & 0xFF) == 0) {
            return;
        }
        float r = Math.max(0.0f, radius);
        if (r <= 0.5f) {
            ctx.fill(x, y, x + w, y + h, argb);
            return;
        }
        float rad = Math.min(r, (float)Math.min(w, h) * 0.5f);
        float x1 = x;
        float y1 = y;
        float x2 = x + w;
        float y2 = y + h;
        float cx1 = x1 + rad;
        float cy1 = y1 + rad;
        float cx2 = x2 - rad;
        float cy2 = y2 - rad;
        int a = argb >>> 24 & 0xFF;
        int rr = argb >>> 16 & 0xFF;
        int gg = argb >>> 8 & 0xFF;
        int bb = argb & 0xFF;
        Matrix4f m = ctx.getMatrices().peek().getPositionMatrix();
        float[] sc = RenderSystem.getShaderColor();
        float scR = sc[0];
        float scG = sc[1];
        float scB = sc[2];
        float scA = sc[3];
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        try {
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
            Rounded.addQuad(buf, m, cx1, y1, cx2, y2, rr, gg, bb, a);
            Rounded.addQuad(buf, m, x1, cy1, cx1, cy2, rr, gg, bb, a);
            Rounded.addQuad(buf, m, cx2, cy1, x2, cy2, rr, gg, bb, a);
            int seg = Rounded.segments(rad);
            Rounded.addCorner(buf, m, cx1, cy1, rad, 180.0f, 270.0f, seg, rr, gg, bb, a);
            Rounded.addCorner(buf, m, cx2, cy1, rad, 270.0f, 360.0f, seg, rr, gg, bb, a);
            Rounded.addCorner(buf, m, cx2, cy2, rad, 0.0f, 90.0f, seg, rr, gg, bb, a);
            Rounded.addCorner(buf, m, cx1, cy2, rad, 90.0f, 180.0f, seg, rr, gg, bb, a);
            BuiltBuffer built = buf.end();
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)built);
            built.close();
        }
        catch (Throwable t) {
            Rounded.rectFallback(ctx, x, y, w, h, r, argb);
        }
        finally {
            RenderSystem.setShaderColor((float)scR, (float)scG, (float)scB, (float)scA);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void rectRightRounded(DrawContext ctx, int x, int y, int w, int h, float radius, int argb) {
        argb = Rounded.applyGlobalAlpha(argb);
        if (w <= 0 || h <= 0) {
            return;
        }
        if ((argb >>> 24 & 0xFF) == 0) {
            return;
        }
        float r = Math.max(0.0f, radius);
        if (r <= 0.5f) {
            ctx.fill(x, y, x + w, y + h, argb);
            return;
        }
        float rad = Math.min(r, (float)Math.min(w, h) * 0.5f);
        float x1 = x;
        float y1 = y;
        float x2 = x + w;
        float y2 = y + h;
        float cx2 = x2 - rad;
        float cy1 = y1 + rad;
        float cy2 = y2 - rad;
        int a = argb >>> 24 & 0xFF;
        int rr = argb >>> 16 & 0xFF;
        int gg = argb >>> 8 & 0xFF;
        int bb = argb & 0xFF;
        Matrix4f m = ctx.getMatrices().peek().getPositionMatrix();
        float[] sc = RenderSystem.getShaderColor();
        float scR = sc[0];
        float scG = sc[1];
        float scB = sc[2];
        float scA = sc[3];
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        try {
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
            Rounded.addQuad(buf, m, x1, y1, x2 - rad, y2, rr, gg, bb, a);
            Rounded.addQuad(buf, m, x2 - rad, y1 + rad, x2, y2 - rad, rr, gg, bb, a);
            int seg = Rounded.segments(rad);
            Rounded.addCorner(buf, m, cx2, cy1, rad, 270.0f, 360.0f, seg, rr, gg, bb, a);
            Rounded.addCorner(buf, m, cx2, cy2, rad, 0.0f, 90.0f, seg, rr, gg, bb, a);
            BuiltBuffer built = buf.end();
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)built);
            built.close();
        }
        catch (Throwable t) {
            Rounded.rectRightFallback(ctx, x, y, w, h, rad, argb);
        }
        finally {
            RenderSystem.setShaderColor((float)scR, (float)scG, (float)scB, (float)scA);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void outline(DrawContext ctx, int x, int y, int w, int h, float radius, int thickness, int argb) {
        argb = Rounded.applyGlobalAlpha(argb);
        if (thickness <= 0 || w <= 0 || h <= 0) {
            return;
        }
        if ((argb >>> 24 & 0xFF) == 0) {
            return;
        }
        float r = Math.max(0.0f, radius);
        float ro = Math.min(r, (float)Math.min(w, h) * 0.5f);
        if (ro <= 0.5f) {
            Rounded.outlineFallback(ctx, x, y, w, h, radius, thickness, argb);
            return;
        }
        if (thickness * 2 >= w || thickness * 2 >= h) {
            Rounded.rect(ctx, x, y, w, h, ro, argb);
            return;
        }
        float ri = Math.max(0.0f, ro - (float)thickness);
        float x1 = x;
        float y1 = y;
        float x2 = x + w;
        float y2 = y + h;
        float cx1 = x1 + ro;
        float cy1 = y1 + ro;
        float cx2 = x2 - ro;
        float cy2 = y2 - ro;
        int a = argb >>> 24 & 0xFF;
        int rr = argb >>> 16 & 0xFF;
        int gg = argb >>> 8 & 0xFF;
        int bb = argb & 0xFF;
        Matrix4f m = ctx.getMatrices().peek().getPositionMatrix();
        float[] sc = RenderSystem.getShaderColor();
        float scR = sc[0];
        float scG = sc[1];
        float scB = sc[2];
        float scA = sc[3];
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        try {
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
            float t = thickness;
            Rounded.addQuad(buf, m, cx1, y1, cx2, y1 + t, rr, gg, bb, a);
            Rounded.addQuad(buf, m, cx1, y2 - t, cx2, y2, rr, gg, bb, a);
            Rounded.addQuad(buf, m, x1, cy1, x1 + t, cy2, rr, gg, bb, a);
            Rounded.addQuad(buf, m, x2 - t, cy1, x2, cy2, rr, gg, bb, a);
            int seg = Rounded.segments(ro);
            Rounded.addCornerRing(buf, m, cx1, cy1, ro, ri, 180.0f, 270.0f, seg, rr, gg, bb, a);
            Rounded.addCornerRing(buf, m, cx2, cy1, ro, ri, 270.0f, 360.0f, seg, rr, gg, bb, a);
            Rounded.addCornerRing(buf, m, cx2, cy2, ro, ri, 0.0f, 90.0f, seg, rr, gg, bb, a);
            Rounded.addCornerRing(buf, m, cx1, cy2, ro, ri, 90.0f, 180.0f, seg, rr, gg, bb, a);
            BuiltBuffer built = buf.end();
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)built);
            built.close();
        }
        catch (Throwable t) {
            Rounded.outlineFallback(ctx, x, y, w, h, radius, thickness, argb);
        }
        finally {
            RenderSystem.setShaderColor((float)scR, (float)scG, (float)scB, (float)scA);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
    }

    private static int segments(float radius) {
        int seg = Math.max(16, (int)(radius * 2.2f));
        return Math.min(64, seg);
    }

    private static int applyGlobalAlpha(int argb) {
        if (globalAlpha >= 0.999f) {
            return argb;
        }
        int a = argb >>> 24 & 0xFF;
        if ((a = Math.round((float)a * globalAlpha)) <= 0) {
            return 0;
        }
        return argb & 0xFFFFFF | a << 24;
    }

    private static void v(BufferBuilder buf, Matrix4f m, float x, float y, int r, int g, int b, int a) {
        buf.vertex(m, x, y, 0.0f).color(r, g, b, a);
    }

    private static void addQuad(BufferBuilder buf, Matrix4f m, float x1, float y1, float x2, float y2, int r, int g, int b, int a) {
        if (x2 <= x1 || y2 <= y1) {
            return;
        }
        Rounded.v(buf, m, x1, y1, r, g, b, a);
        Rounded.v(buf, m, x2, y1, r, g, b, a);
        Rounded.v(buf, m, x2, y2, r, g, b, a);
        Rounded.v(buf, m, x1, y1, r, g, b, a);
        Rounded.v(buf, m, x2, y2, r, g, b, a);
        Rounded.v(buf, m, x1, y2, r, g, b, a);
    }

    private static void addCorner(BufferBuilder buf, Matrix4f m, float cx, float cy, float rad, float degStart, float degEnd, int seg, int r, int g, int b, int a) {
        float prevX = cx + (float)Math.cos(Math.toRadians(degStart)) * rad;
        float prevY = cy + (float)Math.sin(Math.toRadians(degStart)) * rad;
        for (int i = 1; i <= seg; ++i) {
            float t = (float)i / (float)seg;
            float deg = degStart + (degEnd - degStart) * t;
            float x = cx + (float)Math.cos(Math.toRadians(deg)) * rad;
            float y = cy + (float)Math.sin(Math.toRadians(deg)) * rad;
            Rounded.v(buf, m, cx, cy, r, g, b, a);
            Rounded.v(buf, m, prevX, prevY, r, g, b, a);
            Rounded.v(buf, m, x, y, r, g, b, a);
            prevX = x;
            prevY = y;
        }
    }

    private static void addCornerRing(BufferBuilder buf, Matrix4f m, float cx, float cy, float outerR, float innerR, float degStart, float degEnd, int seg, int r, int g, int b, int a) {
        float prevOuterX = cx + (float)Math.cos(Math.toRadians(degStart)) * outerR;
        float prevOuterY = cy + (float)Math.sin(Math.toRadians(degStart)) * outerR;
        float prevInnerX = cx + (float)Math.cos(Math.toRadians(degStart)) * innerR;
        float prevInnerY = cy + (float)Math.sin(Math.toRadians(degStart)) * innerR;
        for (int i = 1; i <= seg; ++i) {
            float t = (float)i / (float)seg;
            float deg = degStart + (degEnd - degStart) * t;
            float outerX = cx + (float)Math.cos(Math.toRadians(deg)) * outerR;
            float outerY = cy + (float)Math.sin(Math.toRadians(deg)) * outerR;
            float innerX = cx + (float)Math.cos(Math.toRadians(deg)) * innerR;
            float innerY = cy + (float)Math.sin(Math.toRadians(deg)) * innerR;
            Rounded.v(buf, m, prevOuterX, prevOuterY, r, g, b, a);
            Rounded.v(buf, m, outerX, outerY, r, g, b, a);
            Rounded.v(buf, m, innerX, innerY, r, g, b, a);
            Rounded.v(buf, m, prevOuterX, prevOuterY, r, g, b, a);
            Rounded.v(buf, m, innerX, innerY, r, g, b, a);
            Rounded.v(buf, m, prevInnerX, prevInnerY, r, g, b, a);
            prevOuterX = outerX;
            prevOuterY = outerY;
            prevInnerX = innerX;
            prevInnerY = innerY;
        }
    }

    private static void rectFallback(DrawContext ctx, int x, int y, int w, int h, float radius, int argb) {
        argb = Rounded.applyGlobalAlpha(argb);
        if (w <= 0 || h <= 0) {
            return;
        }
        if ((argb >>> 24 & 0xFF) == 0) {
            return;
        }
        int r = Math.max(0, Math.round(radius));
        if (r <= 0) {
            ctx.fill(x, y, x + w, y + h, argb);
            return;
        }
        r = Math.min(r, Math.min(w, h) / 2);
        ctx.fill(x + r, y, x + w - r, y + h, argb);
        ctx.fill(x, y + r, x + r, y + h - r, argb);
        ctx.fill(x + w - r, y + r, x + w, y + h - r, argb);
        Rounded.fillCorner(ctx, x + r, y + r, r, Corner.TL, argb);
        Rounded.fillCorner(ctx, x + w - r - 1, y + r, r, Corner.TR, argb);
        Rounded.fillCorner(ctx, x + w - r - 1, y + h - r - 1, r, Corner.BR, argb);
        Rounded.fillCorner(ctx, x + r, y + h - r - 1, r, Corner.BL, argb);
    }

    private static void rectRightFallback(DrawContext ctx, int x, int y, int w, int h, float radius, int argb) {
        argb = Rounded.applyGlobalAlpha(argb);
        if (w <= 0 || h <= 0) {
            return;
        }
        if ((argb >>> 24 & 0xFF) == 0) {
            return;
        }
        int r = Math.max(0, Math.round(radius));
        if (r <= 0) {
            ctx.fill(x, y, x + w, y + h, argb);
            return;
        }
        r = Math.min(r, Math.min(w, h) / 2);
        ctx.fill(x, y, x + w - r, y + h, argb);
        ctx.fill(x + w - r, y + r, x + w, y + h - r, argb);
        Rounded.fillCorner(ctx, x + w - r - 1, y + r, r, Corner.TR, argb);
        Rounded.fillCorner(ctx, x + w - r - 1, y + h - r - 1, r, Corner.BR, argb);
    }

    private static void outlineFallback(DrawContext ctx, int x, int y, int w, int h, float radius, int thickness, int argb) {
        argb = Rounded.applyGlobalAlpha(argb);
        if (thickness <= 0 || w <= 0 || h <= 0) {
            return;
        }
        if ((argb >>> 24 & 0xFF) == 0) {
            return;
        }
        int r = Math.max(0, Math.round(radius));
        r = Math.min(r, Math.min(w, h) / 2);
        int t = Math.min(thickness, Math.min(w, h));
        Rounded.rectFallback(ctx, x + r, y, w - 2 * r, t, 0.0f, argb);
        Rounded.rectFallback(ctx, x + r, y + h - t, w - 2 * r, t, 0.0f, argb);
        Rounded.rectFallback(ctx, x, y + r, t, h - 2 * r, 0.0f, argb);
        Rounded.rectFallback(ctx, x + w - t, y + r, t, h - 2 * r, 0.0f, argb);
        Rounded.outlineCorner(ctx, x + r, y + r, r, t, Corner.TL, argb);
        Rounded.outlineCorner(ctx, x + w - r - 1, y + r, r, t, Corner.TR, argb);
        Rounded.outlineCorner(ctx, x + w - r - 1, y + h - r - 1, r, t, Corner.BR, argb);
        Rounded.outlineCorner(ctx, x + r, y + h - r - 1, r, t, Corner.BL, argb);
    }

    private static void fillCorner(DrawContext ctx, int cx, int cy, int r, Corner corner, int color) {
        int rr = r * r;
        for (int dx = 0; dx <= r; ++dx) {
            for (int dy = 0; dy <= r; ++dy) {
                int px;
                if (dx * dx + dy * dy > rr) continue;
                int py = switch (corner.ordinal()) {
                    case 0 -> {
                        px = cx - dx;
                        yield cy - dy;
                    }
                    case 1 -> {
                        px = cx + dx;
                        yield cy - dy;
                    }
                    case 2 -> {
                        px = cx + dx;
                        yield cy + dy;
                    }
                    case 3 -> {
                        px = cx - dx;
                        yield cy + dy;
                    }
                    default -> {
                        px = cx;
                        yield cy;
                    }
                };
                ctx.fill(px, py, px + 1, py + 1, color);
            }
        }
    }

    private static void outlineCorner(DrawContext ctx, int cx, int cy, int r, int thickness, Corner corner, int color) {
        if (r <= 0) {
            return;
        }
        int outer2 = r * r;
        int inner = Math.max(0, r - thickness);
        int inner2 = inner * inner;
        for (int dx = 0; dx <= r; ++dx) {
            for (int dy = 0; dy <= r; ++dy) {
                int px;
                int d2 = dx * dx + dy * dy;
                if (d2 > outer2 || d2 < inner2) continue;
                int py = switch (corner.ordinal()) {
                    case 0 -> {
                        px = cx - dx;
                        yield cy - dy;
                    }
                    case 1 -> {
                        px = cx + dx;
                        yield cy - dy;
                    }
                    case 2 -> {
                        px = cx + dx;
                        yield cy + dy;
                    }
                    case 3 -> {
                        px = cx - dx;
                        yield cy + dy;
                    }
                    default -> {
                        px = cx;
                        yield cy;
                    }
                };
                ctx.fill(px, py, px + 1, py + 1, color);
            }
        }
    }

    private static enum Corner {
        TL,
        TR,
        BR,
        BL;

    }
}

