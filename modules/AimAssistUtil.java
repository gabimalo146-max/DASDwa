package me.Gui.gui.modules;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.config.GuiConfig;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class AimAssistUtil {
    private static final float MIN_RANGE = 2.0f;
    private static final float MAX_RANGE = 10.0f;
    private static final float DEFAULT_RANGE = 6.0f;
    private static int lockedId = -1;

    private AimAssistUtil() {
    }

    public static void tick(MinecraftClient client) {
        double distSq;
        LivingEntity target;
        if (client == null || client.world == null || client.player == null) {
            AimAssistUtil.clearLock();
            return;
        }
        HackModule mod = GuiClient.MODULES.byId(ModuleId.AIM_ASSIST);
        if (mod == null || !mod.isEnabled()) {
            AimAssistUtil.clearLock();
            return;
        }
        if (client.currentScreen != null) {
            return;
        }
        GuiConfig cfg = GuiClient.CONFIG;
        boolean targetLock = cfg.aimAssistTargetLock != null && cfg.aimAssistTargetLock != false;
        boolean allowMobs = cfg.aimAssistMobs != null && cfg.aimAssistMobs != false;
        float range = AimAssistUtil.readRange(cfg);
        if (targetLock) {
            target = AimAssistUtil.resolveLocked(client, allowMobs);
            if (target == null && (target = AimAssistUtil.findNearestTarget(client, -1.0, allowMobs)) != null) {
                lockedId = target.getId();
            }
        } else {
            AimAssistUtil.clearLock();
            target = AimAssistUtil.findNearestTarget(client, range * range, allowMobs);
        }
        if (target == null) {
            return;
        }
        if (targetLock && (distSq = client.player.squaredDistanceTo((Entity)target)) > (double)(range * range)) {
            return;
        }
        AimAssistUtil.aimAt(client.player, target);
    }

    private static void clearLock() {
        lockedId = -1;
    }

    public static boolean isTargetLockEnabled() {
        HackModule mod = GuiClient.MODULES.byId(ModuleId.AIM_ASSIST);
        if (mod == null || !mod.isEnabled()) {
            return false;
        }
        return GuiClient.CONFIG.aimAssistTargetLock != null && GuiClient.CONFIG.aimAssistTargetLock != false;
    }

    public static Entity getLockedEntity(MinecraftClient client) {
        if (client == null || client.world == null || lockedId < 0) {
            return null;
        }
        return client.world.getEntityById(lockedId);
    }

    private static float readRange(GuiConfig cfg) {
        float range = cfg.aimAssistRange;
        if (Float.isNaN(range) || Float.isInfinite(range)) {
            range = 6.0f;
        }
        return Math.max(2.0f, Math.min(10.0f, range));
    }

    private static LivingEntity resolveLocked(MinecraftClient client, boolean allowMobs) {
        if (lockedId < 0 || client.world == null) {
            return null;
        }
        Entity e = client.world.getEntityById(lockedId);
        if (!(e instanceof LivingEntity)) {
            AimAssistUtil.clearLock();
            return null;
        }
        LivingEntity le = (LivingEntity)e;
        if (!AimAssistUtil.isValidTarget(client, le, allowMobs)) {
            AimAssistUtil.clearLock();
            return null;
        }
        return le;
    }

    private static LivingEntity findNearestTarget(MinecraftClient client, double maxDistSq, boolean allowMobs) {
        ClientPlayerEntity self = client.player;
        if (self == null || client.world == null) {
            return null;
        }
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (PlayerEntity p : client.world.getPlayers()) {
            if (!AimAssistUtil.isValidTarget(client, (LivingEntity)p, allowMobs)) continue;
            double dist = self.squaredDistanceTo((Entity)p);
            if (maxDistSq >= 0.0 && dist > maxDistSq || !(dist < bestDist)) continue;
            bestDist = dist;
            best = p;
        }
        if (allowMobs) {
            for (Entity e : client.world.getEntities()) {
                MobEntity mob;
                if (!(e instanceof MobEntity) || !AimAssistUtil.isValidTarget(client, (LivingEntity)(mob = (MobEntity)e), true)) continue;
                double dist = self.squaredDistanceTo((Entity)mob);
                if (maxDistSq >= 0.0 && dist > maxDistSq || !(dist < bestDist)) continue;
                bestDist = dist;
                best = mob;
            }
        }
        return best;
    }

    private static boolean isValidTarget(MinecraftClient client, LivingEntity e, boolean allowMobs) {
        if (e == null || e.isRemoved()) {
            return false;
        }
        ClientPlayerEntity self = client.player;
        if (self == null || e == self) {
            return false;
        }
        if (!e.isAlive()) {
            return false;
        }
        if (e instanceof PlayerEntity) {
            PlayerEntity p = (PlayerEntity)e;
            if (p.isSpectator()) {
                return false;
            }
            return !GuiClient.FRIENDS.isFriend(p.getName().getString());
        }
        return allowMobs && e instanceof MobEntity;
    }

    private static void aimAt(ClientPlayerEntity self, LivingEntity target) {
        if (self == null || target == null) {
            return;
        }
        Vec3d eye = self.getEyePos();
        Vec3d targetPos = AimAssistUtil.closestPointOnBox(target.getBoundingBox(), eye);
        float[] rot = AimAssistUtil.calcYawPitch(eye, targetPos);
        self.setYaw(rot[0]);
        self.setPitch(rot[1]);
        self.setHeadYaw(rot[0]);
        self.setBodyYaw(rot[0]);
    }

    private static float[] calcYawPitch(Vec3d eye, Vec3d target) {
        double dx = target.x - eye.x;
        double dy = target.y - eye.y;
        double dz = target.z - eye.z;
        float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
        yaw = MathHelper.wrapDegrees((float)yaw);
        pitch = MathHelper.clamp((float)pitch, (float)-90.0f, (float)90.0f);
        return new float[]{yaw, pitch};
    }

    private static Vec3d closestPointOnBox(Box box, Vec3d point) {
        if (box == null || point == null) {
            return point;
        }
        double x = MathHelper.clamp((double)point.x, (double)box.minX, (double)box.maxX);
        double y = MathHelper.clamp((double)point.y, (double)box.minY, (double)box.maxY);
        double z = MathHelper.clamp((double)point.z, (double)box.minZ, (double)box.maxZ);
        return new Vec3d(x, y, z);
    }
}

