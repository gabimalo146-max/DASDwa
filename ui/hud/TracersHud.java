package me.Gui.gui.ui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import me.Gui.gui.mixin.GameRendererAccessor;
import me.Gui.gui.modules.TracersUtil;
import me.Gui.gui.ui.HudEditScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class TracersHud {
    private TracersHud() {
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null || client.player == null) {
            return;
        }
        if (HudEditScreen.isActive()) {
            return;
        }
        float tickDelta = tickCounter.getTickDelta(true);
        List<TracersUtil.Target> targets = TracersUtil.collect(client, tickDelta);
        if (targets.isEmpty()) {
            return;
        }
        Camera camera = client.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        float fov = ((GameRendererAccessor)client.gameRenderer).gui$getFov(camera, tickDelta, true);
        Matrix4f projection = client.gameRenderer.getBasicProjectionMatrix(fov);
        Quaternionf camRot = new Quaternionf((Quaternionfc)camera.getRotation()).conjugate();
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        float cx = (float)sw / 2.0f;
        float cy = (float)sh / 2.0f;
        boolean firstPerson = client.options.getPerspective().isFirstPerson();
        float originX = cx;
        float originY = cy;
        if (!firstPerson) {
            Vec3d head = client.player.getLerpedPos(tickDelta).add(0.0, (double)client.player.getEyeHeight(client.player.getPose()), 0.0);
            Vec3d relHead = head.subtract(camPos);
            Vector3f camHead = new Vector3f((float)relHead.x, (float)relHead.y, (float)relHead.z).rotate((Quaternionfc)camRot);
            if (camHead.z <= -0.01f) {
                Vector4f clipHead = new Vector4f(camHead.x, camHead.y, camHead.z, 1.0f).mul((Matrix4fc)projection);
                if (clipHead.w > 0.0f) {
                    float ndcX = clipHead.x / clipHead.w;
                    float ndcY = clipHead.y / clipHead.w;
                    float hx = cx + ndcX * cx;
                    float hy = cy - ndcY * cy;
                    if (Float.isFinite(hx) && Float.isFinite(hy)) {
                        originX = hx;
                        originY = hy;
                    }
                }
            }
        }
        float[] prevColor = RenderSystem.getShaderColor();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        Matrix4f mat = ctx.getMatrices().peek().getPositionMatrix();
        float margin = 1.0f;
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        float thickness = firstPerson ? 0.35f : 0.22f;
        float softExtra = firstPerson ? 0.55f : 0.35f;
        boolean any = false;
        for (TracersUtil.Target t : targets) {
            Vec3d pos = t.pos();
            Vec3d rel = pos.subtract(camPos);
            if (rel.lengthSquared() < 1.0E-4) continue;
            Vector3f cam = new Vector3f((float)rel.x, (float)rel.y, (float)rel.z).rotate((Quaternionfc)camRot);
            boolean behind = cam.z > -0.01f;
            float ex = cx;
            float ey = cy;
            boolean ok = false;
            if (!behind) {
                Vector4f clip = new Vector4f(cam.x, cam.y, cam.z, 1.0f).mul((Matrix4fc)projection);
                if (clip.w <= 0.0f) {
                    behind = true;
                } else {
                    boolean inside;
                    float ndcX = clip.x / clip.w;
                    float ndcY = clip.y / clip.w;
                    ex = cx + ndcX * cx;
                    ey = cy - ndcY * cy;
                    boolean bl = inside = ex >= margin && ex <= (float)sw - margin && ey >= margin && ey <= (float)sh - margin;
                    if (!inside) {
                        float dx = ex - originX;
                        float dy = ey - originY;
                        float edge = TracersHud.edgeScale(dx, dy, originX, originY, sw, sh, margin);
                        if (edge <= 0.0f) continue;
                        ex = originX + dx * edge;
                        ey = originY + dy * edge;
                    }
                    ok = true;
                }
            }
            if (behind) {
                float edge;
                float dx = cam.x;
                float dy = -cam.y;
                float len = (float)Math.sqrt(dx * dx + dy * dy);
                if (len < 1.0E-4f) {
                    dx = 0.0f;
                    dy = 1.0f;
                    len = 1.0f;
                }
                if ((edge = TracersHud.edgeScale(dx /= len, dy /= len, originX, originY, sw, sh, margin)) <= 0.0f) continue;
                ex = originX + dx * edge;
                ey = originY + dy * edge;
                ok = true;
            }
            if (!ok) continue;
            int baseColor = t.color();
            int softColor = TracersHud.scaleAlpha(baseColor, 0.35f);
            TracersHud.addLineQuad(buf, mat, originX, originY, ex, ey, thickness + softExtra, softColor);
            TracersHud.addLineQuad(buf, mat, originX, originY, ex, ey, thickness, baseColor);
            any = true;
        }
        if (!any) {
            RenderSystem.setShaderColor((float)prevColor[0], (float)prevColor[1], (float)prevColor[2], (float)prevColor[3]);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            return;
        }
        BuiltBuffer built = buf.end();
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)built);
        built.close();
        RenderSystem.setShaderColor((float)prevColor[0], (float)prevColor[1], (float)prevColor[2], (float)prevColor[3]);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private static void addLineQuad(BufferBuilder buf, Matrix4f mat, float x1, float y1, float x2, float y2, float thickness, int color) {
        float r = (float)(color >>> 16 & 0xFF) / 255.0f;
        float g = (float)(color >>> 8 & 0xFF) / 255.0f;
        float b = (float)(color & 0xFF) / 255.0f;
        float a = (float)(color >>> 24 & 0xFF) / 255.0f;
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        float half = thickness * 0.5f;
        if (len < 0.001f) {
            TracersHud.addQuad(buf, mat, x1 - half, y1 - half, x1 + half, y1 + half, r, g, b, a);
            return;
        }
        float ox = -dy / len * half;
        float oy = dx / len * half;
        float x1a = x1 + ox;
        float y1a = y1 + oy;
        float x1b = x1 - ox;
        float y1b = y1 - oy;
        float x2a = x2 + ox;
        float y2a = y2 + oy;
        float x2b = x2 - ox;
        float y2b = y2 - oy;
        TracersHud.v(buf, mat, x1a, y1a, r, g, b, a);
        TracersHud.v(buf, mat, x1b, y1b, r, g, b, a);
        TracersHud.v(buf, mat, x2b, y2b, r, g, b, a);
        TracersHud.v(buf, mat, x1a, y1a, r, g, b, a);
        TracersHud.v(buf, mat, x2b, y2b, r, g, b, a);
        TracersHud.v(buf, mat, x2a, y2a, r, g, b, a);
    }

    private static void addQuad(BufferBuilder buf, Matrix4f mat, float x1, float y1, float x2, float y2, float r, float g, float b, float a) {
        TracersHud.v(buf, mat, x1, y1, r, g, b, a);
        TracersHud.v(buf, mat, x2, y1, r, g, b, a);
        TracersHud.v(buf, mat, x2, y2, r, g, b, a);
        TracersHud.v(buf, mat, x1, y1, r, g, b, a);
        TracersHud.v(buf, mat, x2, y2, r, g, b, a);
        TracersHud.v(buf, mat, x1, y2, r, g, b, a);
    }

    private static void v(BufferBuilder buf, Matrix4f mat, float x, float y, float r, float g, float b, float a) {
        buf.vertex(mat, x, y, 0.0f).color(r, g, b, a);
    }

    private static int scaleAlpha(int color, float mul) {
        float clamped = Math.max(0.0f, Math.min(1.0f, mul));
        int a = color >>> 24 & 0xFF;
        int na = Math.max(0, Math.min(255, Math.round((float)a * clamped)));
        return na << 24 | color & 0xFFFFFF;
    }

    private static float edgeScale(float dx, float dy, float cx, float cy, float sw, float sh, float margin) {
        float left = margin - cx;
        float right = sw - margin - cx;
        float top = margin - cy;
        float bottom = sh - margin - cy;
        float t = Float.POSITIVE_INFINITY;
        if (dx > 0.0f) {
            t = Math.min(t, right / dx);
        } else if (dx < 0.0f) {
            t = Math.min(t, left / dx);
        }
        if (dy > 0.0f) {
            t = Math.min(t, bottom / dy);
        } else if (dy < 0.0f) {
            t = Math.min(t, top / dy);
        }
        if (!Float.isFinite(t)) {
            return -1.0f;
        }
        return t;
    }
}

