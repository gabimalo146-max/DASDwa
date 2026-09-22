package me.Gui.gui.modules;

import java.util.Optional;
import me.Gui.gui.client.GuiClient;
import me.Gui.gui.mixin.MinecraftClientAccessor;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class BetterHitboxesUtil {
    private static final float MIN_PERCENT = 100.0f;
    private static final float MAX_PERCENT = 300.0f;
    private static boolean debugRender = false;

    private BetterHitboxesUtil() {
    }

    public static void updateCrosshair(MinecraftClient client, float tickDelta) {
        double entityRange;
        Vec3d dir;
        Vec3d entityEnd;
        if (!BetterHitboxesUtil.isEnabled()) {
            BetterHitboxesUtil.clearTarget();
            return;
        }
        if (client == null || client.world == null || client.player == null) {
            BetterHitboxesUtil.clearTarget();
            return;
        }
        float scaleX = BetterHitboxesUtil.readScaleX();
        float scaleY = BetterHitboxesUtil.readScaleY();
        if (scaleX <= 0.0f && scaleY <= 0.0f) {
            BetterHitboxesUtil.clearTarget();
            return;
        }
        Entity cam = client.getCameraEntity();
        if (cam == null) {
            BetterHitboxesUtil.clearTarget();
            return;
        }
        Vec3d start = cam.getCameraPosVec(tickDelta);
        RaycastResult rr = BetterHitboxesUtil.raycastLiving(client, start, entityEnd = start.add((dir = cam.getRotationVec(tickDelta)).multiply(entityRange = client.player.getEntityInteractionRange())), scaleX, scaleY);
        if (rr == null) {
            BetterHitboxesUtil.clearTarget();
            return;
        }
        EntityHitResult ehr = new EntityHitResult((Entity)rr.entity, rr.hitPos);
        ((MinecraftClientAccessor)client).setCrosshairTarget((HitResult)ehr);
        ((MinecraftClientAccessor)client).setTargetedEntity((Entity)rr.entity);
    }

    public static void clearTarget() {
    }

    public static void toggleDebugRender() {
        debugRender = !debugRender;
    }

    public static boolean shouldRenderDebug() {
        if (!debugRender) {
            return false;
        }
        if (!BetterHitboxesUtil.isEnabled()) {
            return false;
        }
        return BetterHitboxesUtil.readScaleX() > 0.0f || BetterHitboxesUtil.readScaleY() > 0.0f;
    }

    public static float readScaleX() {
        return BetterHitboxesUtil.clampPercent(GuiClient.CONFIG.betterHitboxSizeX) / 100.0f;
    }

    public static float readScaleY() {
        return BetterHitboxesUtil.clampPercent(GuiClient.CONFIG.betterHitboxSizeY) / 100.0f;
    }

    public static Box getScaledDebugHitbox(MinecraftClient client, Entity entity, Box base) {
        if (base == null || entity == null) {
            return base;
        }
        if (!BetterHitboxesUtil.isEnabled()) {
            return base;
        }
        if (!(entity instanceof LivingEntity)) {
            return base;
        }
        LivingEntity le = (LivingEntity)entity;
        if (le.isRemoved() || !le.isAlive()) {
            return base;
        }
        if (le.isSpectator()) {
            return base;
        }
        float scaleX = BetterHitboxesUtil.readScaleX();
        float scaleY = BetterHitboxesUtil.readScaleY();
        if (scaleX <= 1.0f && scaleY <= 1.0f) {
            return base;
        }
        Box scaled = BetterHitboxesUtil.scaleBox(base, scaleX, scaleY);
        return scaled != null ? scaled : base;
    }

    private static boolean isEnabled() {
        HackModule mod = GuiClient.MODULES.byId(ModuleId.BETTER_HITBOXES);
        return mod != null && mod.isEnabled();
    }

    private static RaycastResult raycastLiving(MinecraftClient client, Vec3d start, Vec3d end, float scaleX, float scaleY) {
        if (client == null || client.world == null || client.player == null) {
            return null;
        }
        double maxDistSq = start.squaredDistanceTo(end);
        ClientPlayerEntity self = client.player;
        LivingEntity best = null;
        Vec3d bestPos = null;
        for (Entity entity : client.world.getEntities()) {
            Vec3d hitPos;
            double distSq;
            double margin;
            Box base;
            Box expanded;
            Optional hit;
            Box bb;
            LivingEntity le;
            if (!BetterHitboxesUtil.isTargetCandidate(client, entity) || (le = (LivingEntity)entity) == self || (bb = le.getBoundingBox()) == null || (hit = (expanded = BetterHitboxesUtil.scaleBox(base = bb.expand(margin = (double)le.getTargetingMargin()), scaleX, scaleY)).raycast(start, end)).isEmpty() || (distSq = start.squaredDistanceTo(hitPos = (Vec3d)hit.get())) > maxDistSq) continue;
            maxDistSq = distSq;
            best = le;
            bestPos = hitPos;
        }
        if (best == null || bestPos == null) {
            return null;
        }
        return new RaycastResult(best, bestPos);
    }

    private static float clampPercent(float v) {
        if (Float.isNaN(v) || Float.isInfinite(v)) {
            return 100.0f;
        }
        return MathHelper.clamp((float)v, (float)100.0f, (float)300.0f);
    }

    private static Box scaleBox(Box box, float scaleX, float scaleY) {
        if (box == null) {
            return null;
        }
        double cx = (box.minX + box.maxX) * 0.5;
        double cy = (box.minY + box.maxY) * 0.5;
        double cz = (box.minZ + box.maxZ) * 0.5;
        double hx = (box.maxX - box.minX) * 0.5;
        double hy = (box.maxY - box.minY) * 0.5;
        double hz = (box.maxZ - box.minZ) * 0.5;
        double sx = Math.max(0.0, hx * (double)Math.max(1.0f, scaleX));
        double sy = Math.max(0.0, hy * (double)Math.max(1.0f, scaleY));
        double sz = Math.max(0.0, hz * (double)Math.max(1.0f, scaleX));
        return new Box(cx - sx, cy - sy, cz - sz, cx + sx, cy + sy, cz + sz);
    }

    public static boolean isTargetCandidate(MinecraftClient client, Entity entity) {
        if (client == null || client.player == null) {
            return false;
        }
        if (entity == null) {
            return false;
        }
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        LivingEntity le = (LivingEntity)entity;
        if (le == client.player) {
            return false;
        }
        if (!(le instanceof PlayerEntity) && !(le instanceof MobEntity)) {
            return false;
        }
        if (le.isRemoved() || !le.isAlive()) {
            return false;
        }
        if (le.isSpectator()) {
            return false;
        }
        return le.canHit();
    }

    private record RaycastResult(LivingEntity entity, Vec3d hitPos) {
    }
}

