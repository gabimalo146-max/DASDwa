package me.Gui.gui.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

public final class SilentRotationUtil {
    private static boolean active = false;
    private static float yaw = 0.0f;
    private static float pitch = 0.0f;
    private static long lastUpdateMs = 0L;

    private SilentRotationUtil() {
    }

    public static boolean isActive() {
        return active;
    }

    public static float yaw() {
        return yaw;
    }

    public static float pitch() {
        return pitch;
    }

    public static void clear() {
        active = false;
        lastUpdateMs = 0L;
    }

    public static boolean update(MinecraftClient client, float targetYaw, float targetPitch, int durationMs) {
        long dtMs;
        if (client == null || client.player == null) {
            SilentRotationUtil.clear();
            return false;
        }
        long now = System.currentTimeMillis();
        if (!active) {
            active = true;
            yaw = client.player.getYaw();
            pitch = client.player.getPitch();
            lastUpdateMs = now;
        }
        if ((dtMs = now - lastUpdateMs) < 1L) {
            dtMs = 1L;
        }
        lastUpdateMs = now;
        float speed = SilentRotationUtil.rotationSpeedDegPerMs(durationMs);
        float maxStep = speed * (float)dtMs;
        yaw = SilentRotationUtil.approachAngle(yaw, targetYaw, maxStep);
        pitch = SilentRotationUtil.approach(pitch, targetPitch, maxStep);
        pitch = MathHelper.clamp((float)pitch, (float)-90.0f, (float)90.0f);
        float yawDiff = Math.abs(MathHelper.wrapDegrees((float)(targetYaw - yaw)));
        float pitchDiff = Math.abs(targetPitch - pitch);
        return yawDiff <= 1.0f && pitchDiff <= 1.0f;
    }

    private static float rotationSpeedDegPerMs(int durationMs) {
        int d = Math.max(1, durationMs);
        return 90.0f / (float)d;
    }

    private static float approach(float current, float target, float maxStep) {
        float diff = target - current;
        if (Math.abs(diff) <= maxStep) {
            return target;
        }
        return current + Math.copySign(maxStep, diff);
    }

    private static float approachAngle(float current, float target, float maxStep) {
        float diff = MathHelper.wrapDegrees((float)(target - current));
        if (Math.abs(diff) <= maxStep) {
            return MathHelper.wrapDegrees((float)(current + diff));
        }
        return MathHelper.wrapDegrees((float)(current + Math.copySign(maxStep, diff)));
    }
}

