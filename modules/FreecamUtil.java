package me.Gui.gui.modules;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.config.GuiConfig;
import me.Gui.gui.mixin.EntityAccessor;
import me.Gui.gui.mixin.LivingEntityAccessor;
import me.Gui.gui.mixin.MinecraftClientAccessor;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class FreecamUtil {
    private static final float MIN_SPEED = 0.05f;
    private static final float MAX_SPEED = 1.0f;
    private static final boolean FOLLOW_BODY = false;
    private static boolean active = false;
    private static Vec3d originPos = Vec3d.ZERO;
    private static float originYaw = 0.0f;
    private static float originPitch = 0.0f;
    private static float originBodyYaw = 0.0f;
    private static float originHeadYaw = 0.0f;
    private static OtherClientPlayerEntity ghost = null;
    private static int ghostId = -7300;
    private static Vec3d freecamPos = Vec3d.ZERO;
    private static Vec3d prevFreecamPos = Vec3d.ZERO;
    private static float freecamYaw = 0.0f;
    private static float freecamPitch = 0.0f;
    private static float prevFreecamYaw = 0.0f;
    private static float prevFreecamPitch = 0.0f;
    private static boolean lookDirty = false;

    private FreecamUtil() {
    }

    public static boolean isEnabled() {
        HackModule mod = GuiClient.MODULES.byId(ModuleId.FREECAM);
        return mod != null && mod.isEnabled();
    }

    public static boolean isActive() {
        return active;
    }

    public static OtherClientPlayerEntity ghost() {
        return ghost;
    }

    public static Vec3d originPos() {
        return originPos;
    }

    public static Vec3d freecamPos() {
        return freecamPos;
    }

    public static float freecamYaw() {
        return freecamYaw;
    }

    public static float freecamPitch() {
        return freecamPitch;
    }

    public static Vec3d getCameraPos(float tickDelta) {
        return new Vec3d(MathHelper.lerp((double)tickDelta, (double)FreecamUtil.prevFreecamPos.x, (double)FreecamUtil.freecamPos.x), MathHelper.lerp((double)tickDelta, (double)FreecamUtil.prevFreecamPos.y, (double)FreecamUtil.freecamPos.y), MathHelper.lerp((double)tickDelta, (double)FreecamUtil.prevFreecamPos.z, (double)FreecamUtil.freecamPos.z));
    }

    public static float getCameraYaw(float tickDelta) {
        return MathHelper.lerpAngleDegrees((float)tickDelta, (float)prevFreecamYaw, (float)freecamYaw);
    }

    public static float getCameraPitch(float tickDelta) {
        return MathHelper.lerp((float)tickDelta, (float)prevFreecamPitch, (float)freecamPitch);
    }

    public static void applyLookDelta(double deltaX, double deltaY) {
        prevFreecamYaw = freecamYaw;
        prevFreecamPitch = freecamPitch;
        freecamYaw += (float)deltaX * 0.15f;
        freecamPitch += (float)deltaY * 0.15f;
        freecamPitch = MathHelper.clamp((float)freecamPitch, (float)-90.0f, (float)90.0f);
        freecamYaw = MathHelper.wrapDegrees((float)freecamYaw);
        lookDirty = true;
    }

    public static float originYaw() {
        return originYaw;
    }

    public static float originPitch() {
        return originPitch;
    }

    public static void applyServerPosition(Vec3d pos, float yaw, float pitch) {
        if (pos == null) {
            return;
        }
        originPos = pos;
    }

    public static boolean shouldCancelPackets() {
        return FreecamUtil.isActive() && FreecamUtil.cancelMoveEnabled();
    }

    public static boolean cancelMoveEnabled() {
        GuiConfig cfg = GuiClient.CONFIG;
        return cfg.freecamCancelMove == null || cfg.freecamCancelMove != false;
    }

    public static boolean followBody() {
        return false;
    }

    public static void tick(MinecraftClient client) {
        boolean enabled = FreecamUtil.isEnabled();
        if (!enabled) {
            if (active) {
                FreecamUtil.disable(client);
            }
            return;
        }
        if (client == null || client.player == null || client.world == null) {
            if (active) {
                active = false;
            }
            return;
        }
        if (!active) {
            FreecamUtil.enable(client);
        }
        originPos = client.player.getPos();
        if (ghost != null) {
            FreecamUtil.syncGhost((PlayerEntity)client.player);
        }
    }

    public static boolean handleMovement(ClientPlayerEntity player) {
        boolean cancelMove;
        float len;
        if (player == null || !FreecamUtil.isEnabled()) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) {
            return false;
        }
        if (!active) {
            FreecamUtil.enable(client);
        }
        if (!active) {
            return false;
        }
        prevFreecamPos = freecamPos;
        if (lookDirty) {
            lookDirty = false;
        } else {
            prevFreecamYaw = freecamYaw;
            prevFreecamPitch = freecamPitch;
        }
        if (player.input != null) {
            player.input.movementForward = 0.0f;
            player.input.movementSideways = 0.0f;
            player.input.playerInput = PlayerInput.DEFAULT;
        }
        GuiConfig cfg = GuiClient.CONFIG;
        float speedX = FreecamUtil.clampSpeed(cfg.freecamSpeedX);
        float speedY = FreecamUtil.clampSpeed(cfg.freecamSpeedY);
        float forward = 0.0f;
        if (client.options.forwardKey.isPressed()) {
            forward += 1.0f;
        }
        if (client.options.backKey.isPressed()) {
            forward -= 1.0f;
        }
        float strafe = 0.0f;
        if (client.options.leftKey.isPressed()) {
            strafe += 1.0f;
        }
        if (client.options.rightKey.isPressed()) {
            strafe -= 1.0f;
        }
        if ((len = (float)Math.sqrt(forward * forward + strafe * strafe)) > 0.001f) {
            forward /= len;
            strafe /= len;
        }
        double rad = Math.toRadians(freecamYaw);
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);
        double dx = ((double)forward * -sin + (double)strafe * cos) * (double)speedX;
        double dz = ((double)forward * cos + (double)strafe * sin) * (double)speedX;
        double dy = 0.0;
        if (client.options.jumpKey.isPressed()) {
            dy += (double)speedY;
        }
        if (client.options.sneakKey.isPressed()) {
            dy -= (double)speedY;
        }
        if (!(cancelMove = FreecamUtil.cancelMoveEnabled())) {
            Vec3d vel = player.getVelocity();
            if (vel.lengthSquared() > 1.0E-5) {
                player.move(MovementType.SELF, vel);
                player.setVelocity(vel.multiply(0.85));
            }
        } else {
            player.setVelocity(Vec3d.ZERO);
        }
        if (dx != 0.0 || dy != 0.0 || dz != 0.0) {
            freecamPos = freecamPos.add(dx, dy, dz);
        }
        return true;
    }

    public static void updateCrosshair(MinecraftClient client, float tickDelta) {
        double blockDistSq;
        Vec3d dir;
        Vec3d start;
        if (client == null || !FreecamUtil.isEnabled()) {
            return;
        }
        if (client.world == null) {
            return;
        }
        Entity camEntity = client.getCameraEntity();
        double blockRange = 4.0;
        if (FreecamUtil.isActive()) {
            start = FreecamUtil.getCameraPos(tickDelta);
            dir = Vec3d.fromPolar((float)FreecamUtil.getCameraPitch(tickDelta), (float)FreecamUtil.getCameraYaw(tickDelta));
        } else {
            if (camEntity == null) {
                return;
            }
            start = new Vec3d(camEntity.getX(), camEntity.getEyeY(), camEntity.getZ());
            dir = camEntity.getRotationVec(tickDelta);
        }
        Vec3d end = start.add(dir.multiply(blockRange));
        EntityHitResult ehr = FreecamUtil.raycastEntities(client, start, dir, 3.0);
        BlockHitResult blockHit = client.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)(camEntity == null ? client.player : camEntity)));
        double entityDistSq = Double.POSITIVE_INFINITY;
        if (ehr != null) {
            entityDistSq = start.squaredDistanceTo(ehr.getPos());
        }
        double d = blockDistSq = blockHit == null ? Double.POSITIVE_INFINITY : start.squaredDistanceTo(blockHit.getPos());
        if (ehr != null && entityDistSq <= blockDistSq) {
            ((MinecraftClientAccessor)client).setCrosshairTarget((HitResult)ehr);
            ((MinecraftClientAccessor)client).setTargetedEntity(ehr.getEntity());
        } else if (blockHit != null && blockHit.getType() != HitResult.Type.MISS) {
            ((MinecraftClientAccessor)client).setCrosshairTarget((HitResult)blockHit);
            ((MinecraftClientAccessor)client).setTargetedEntity(null);
        }
    }

    private static EntityHitResult raycastEntities(MinecraftClient client, Vec3d start, Vec3d dir, double range) {
        if (client == null || client.player == null) {
            return null;
        }
        Vec3d end = start.add(dir.x * range, dir.y * range, dir.z * range);
        Box box = new Box(start, start).stretch(dir.multiply(range)).expand(1.0);
        return ProjectileUtil.raycast((Entity)client.player, (Vec3d)start, (Vec3d)end, (Box)box, entity -> !entity.isSpectator() && entity.canHit(), (double)(range * range));
    }

    private static void enable(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) {
            return;
        }
        ClientPlayerEntity player = client.player;
        active = true;
        originPos = player.getPos();
        originYaw = player.getYaw();
        originPitch = player.getPitch();
        originBodyYaw = player.getBodyYaw();
        originHeadYaw = player.getHeadYaw();
        prevFreecamPos = freecamPos = player.getEyePos();
        freecamYaw = player.getYaw();
        freecamPitch = player.getPitch();
        prevFreecamYaw = freecamYaw;
        prevFreecamPitch = freecamPitch;
        ghost = FreecamUtil.buildGhost(client.world, (PlayerEntity)player);
    }

    private static void disable(MinecraftClient client) {
        active = false;
        if (client != null && client.player != null) {
            ClientPlayerEntity player = client.player;
            player.setVelocity(Vec3d.ZERO);
        }
        ghost = null;
    }

    private static OtherClientPlayerEntity buildGhost(ClientWorld world, PlayerEntity player) {
        if (world == null || player == null) {
            return null;
        }
        OtherClientPlayerEntity g = new OtherClientPlayerEntity(world, player.getGameProfile());
        g.copyPositionAndRotation((Entity)player);
        g.setYaw(originYaw);
        g.setPitch(originPitch);
        g.setBodyYaw(originBodyYaw);
        g.setHeadYaw(originHeadYaw);
        g.setPose(player.getPose());
        g.setOnGround(player.isOnGround());
        g.setVelocity(Vec3d.ZERO);
        g.noClip = true;
        g.setNoGravity(true);
        g.setId(ghostId--);
        FreecamUtil.copyEquipment(player, g);
        return g;
    }

    private static void syncGhost(PlayerEntity from) {
        if (ghost == null) {
            return;
        }
        ghost.setPos(FreecamUtil.originPos.x, FreecamUtil.originPos.y, FreecamUtil.originPos.z);
        ghost.setYaw(originYaw);
        ghost.setPitch(originPitch);
        ghost.setBodyYaw(originBodyYaw);
        ghost.setHeadYaw(originHeadYaw);
        EntityAccessor ea = (EntityAccessor)ghost;
        ea.setPrevX(FreecamUtil.originPos.x);
        ea.setPrevY(FreecamUtil.originPos.y);
        ea.setPrevZ(FreecamUtil.originPos.z);
        ea.setLastRenderX(FreecamUtil.originPos.x);
        ea.setLastRenderY(FreecamUtil.originPos.y);
        ea.setLastRenderZ(FreecamUtil.originPos.z);
        ea.setPrevYaw(originYaw);
        ea.setPrevPitch(originPitch);
        if (ghost instanceof LivingEntityAccessor) {
            LivingEntityAccessor lea = (LivingEntityAccessor)ghost;
            lea.setPrevBodyYaw(originBodyYaw);
            lea.setPrevHeadYaw(originHeadYaw);
        }
        FreecamUtil.copyEquipment(from, ghost);
    }

    private static void copyEquipment(PlayerEntity from, OtherClientPlayerEntity to) {
        EquipmentSlot[] equipmentSlotArray = EquipmentSlot.values();
        int n = equipmentSlotArray.length;
        for (int i = 0; i < n; ++i) {
            EquipmentSlot slot;
            ItemStack stack = from.getEquippedStack(slot = equipmentSlotArray[i]);
            to.equipStack(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        }
    }

    private static float clampSpeed(float v) {
        if (Float.isNaN(v) || Float.isInfinite(v)) {
            return 0.35f;
        }
        return MathHelper.clamp((float)v, (float)0.05f, (float)1.0f);
    }
}

