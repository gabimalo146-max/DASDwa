package me.Gui.gui.modules;

import java.util.HashMap;
import java.util.Map;
import me.Gui.gui.client.GuiClient;
import me.Gui.gui.config.GuiConfig;
import me.Gui.gui.modules.AutoPlotekUtil;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import me.Gui.gui.modules.SilentRotationUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.CobwebBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class AntiTrapUtil {
    private static final double AUTO_BREAK_RANGE_SQ = 9.0;
    private static final long AUTO_BREAK_COOLDOWN_MS = 350L;
    private static final int ROTATE_DEFAULT_MS = 80;
    private static final int ROTATE_MIN_MS = 1;
    private static final int ROTATE_MAX_MS = 500;
    private static final Map<Integer, Long> RECENT_BREAKS = new HashMap<Integer, Long>();
    private static boolean allowAttack = false;

    private AntiTrapUtil() {
    }

    public static boolean isEnabled() {
        HackModule m = GuiClient.MODULES.byId(ModuleId.ANTI_TRAP);
        return m != null && m.isEnabled();
    }

    public static boolean allowAttack() {
        return allowAttack;
    }

    public static boolean shouldIgnore(Entity entity) {
        boolean mobs;
        if (entity == null || !AntiTrapUtil.isEnabled()) {
            return false;
        }
        GuiConfig cfg = GuiClient.CONFIG;
        boolean itemFrame = cfg.antiTrapItemFrame == null || cfg.antiTrapItemFrame != false;
        boolean itemFrameVisual = cfg.antiTrapItemFrameVisual == null || cfg.antiTrapItemFrameVisual != false;
        boolean painting = cfg.antiTrapPainting == null || cfg.antiTrapPainting != false;
        boolean paintingVisual = cfg.antiTrapPaintingVisual == null || cfg.antiTrapPaintingVisual != false;
        boolean minecart = cfg.antiTrapMinecart == null || cfg.antiTrapMinecart != false;
        boolean armorStand = cfg.antiTrapArmorStand == null || cfg.antiTrapArmorStand != false;
        boolean bl = mobs = cfg.antiTrapMobs != null && cfg.antiTrapMobs != false;
        if (itemFrame && itemFrameVisual && entity instanceof ItemFrameEntity) {
            return true;
        }
        if (painting && paintingVisual && entity instanceof PaintingEntity) {
            return true;
        }
        if (minecart && entity instanceof AbstractMinecartEntity) {
            return true;
        }
        if (armorStand && entity instanceof ArmorStandEntity) {
            return true;
        }
        if (mobs && entity instanceof MobEntity) {
            return true;
        }
        if (mobs && entity instanceof VillagerEntity) {
            return true;
        }
        return mobs && entity instanceof WanderingTraderEntity;
    }

    public static void tick(MinecraftClient client) {
        boolean handled;
        if (client == null || client.world == null || client.player == null) {
            AutoPlotekUtil.tick(client);
            return;
        }
        if (!AntiTrapUtil.isEnabled()) {
            RECENT_BREAKS.clear();
            AutoPlotekUtil.tick(client);
            return;
        }
        GuiConfig cfg = GuiClient.CONFIG;
        boolean itemFrame = cfg.antiTrapItemFrame == null || cfg.antiTrapItemFrame != false;
        boolean itemFrameBreak = itemFrame && (cfg.antiTrapItemFrameAutoBreak == null || cfg.antiTrapItemFrameAutoBreak != false);
        boolean painting = cfg.antiTrapPainting == null || cfg.antiTrapPainting != false;
        boolean paintingBreak = painting && (cfg.antiTrapPaintingAutoBreak == null || cfg.antiTrapPaintingAutoBreak != false);
        boolean autoPlotek = cfg.antiTrapAutoPlotek != null && cfg.antiTrapAutoPlotek != false;
        boolean plotekHandled = false;
        if (autoPlotek) {
            plotekHandled = AutoPlotekUtil.tick(client);
        }
        if (!(handled = plotekHandled) && (itemFrameBreak || paintingBreak)) {
            handled = AntiTrapUtil.tryBreakNearby(client, itemFrameBreak, paintingBreak);
        }
        if (!autoPlotek && !handled) {
            AutoPlotekUtil.tick(client);
        }
    }

    private static void autoAttack(MinecraftClient client, Entity target) {
        if (client.interactionManager == null || client.player == null) {
            return;
        }
        allowAttack = true;
        try {
            client.interactionManager.attackEntity((PlayerEntity)client.player, target);
            client.player.swingHand(Hand.MAIN_HAND);
        }
        finally {
            allowAttack = false;
        }
        RECENT_BREAKS.put(target.getId(), System.currentTimeMillis());
    }

    private static boolean tryBreakNearby(MinecraftClient client, boolean itemFrame, boolean painting) {
        Vec3d center;
        long now = System.currentTimeMillis();
        RECENT_BREAKS.entrySet().removeIf(e -> now - (Long)e.getValue() > 350L);
        Entity target = AntiTrapUtil.findBreakTarget(client, itemFrame, painting);
        if (target == null) {
            return false;
        }
        Vec3d eye = client.player.getEyePos();
        float[] rot = AntiTrapUtil.calcYawPitch(eye, center = target.getBoundingBox().getCenter());
        boolean aligned = SilentRotationUtil.update(client, rot[0], rot[1], AntiTrapUtil.readRotateMs());
        if (aligned) {
            AntiTrapUtil.autoAttack(client, target);
        }
        return true;
    }

    private static Entity findBreakTarget(MinecraftClient client, boolean itemFrame, boolean painting) {
        if (client == null || client.world == null || client.player == null) {
            return null;
        }
        Entity best = null;
        double bestDist = Double.MAX_VALUE;
        Vec3d eye = client.player.getEyePos();
        for (Entity e : client.world.getEntities()) {
            Vec3d center;
            double dist;
            boolean isPainting;
            if (e == null || e.isRemoved()) continue;
            boolean isFrame = itemFrame && e instanceof ItemFrameEntity;
            boolean bl = isPainting = painting && e instanceof PaintingEntity;
            if (!isFrame && !isPainting || RECENT_BREAKS.containsKey(e.getId()) || (dist = client.player.squaredDistanceTo(e)) > 9.0 || !AntiTrapUtil.canSeeThroughCobwebs(client, eye, center = e.getBoundingBox().getCenter()) || !(dist < bestDist)) continue;
            bestDist = dist;
            best = e;
        }
        return best;
    }

    private static boolean canSeeThroughCobwebs(MinecraftClient client, Vec3d start, Vec3d end) {
        if (client == null || client.world == null || client.player == null) {
            return false;
        }
        Vec3d dir = end.subtract(start);
        if (dir.lengthSquared() < 1.0E-6) {
            return true;
        }
        Vec3d curStart = start;
        Vec3d stepDir = dir.normalize();
        for (int i = 0; i < 8; ++i) {
            BlockHitResult hit = client.world.raycast(new RaycastContext(curStart, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)client.player));
            if (hit == null || hit.getType() == HitResult.Type.MISS) {
                return true;
            }
            BlockPos pos = hit.getBlockPos();
            BlockState state = client.world.getBlockState(pos);
            if (state.getBlock() instanceof CobwebBlock) {
                curStart = AntiTrapUtil.advancePastHit(hit, stepDir);
                if (curStart != null) continue;
                return false;
            }
            return false;
        }
        return false;
    }

    private static Vec3d advancePastHit(BlockHitResult hit, Vec3d dirNorm) {
        if (hit == null || dirNorm == null) {
            return null;
        }
        BlockPos hitPos = hit.getBlockPos();
        Vec3d step = dirNorm.multiply(0.2);
        Vec3d p = hit.getPos();
        for (int i = 0; i < 10; ++i) {
            if (BlockPos.ofFloored((Position)(p = p.add(step))).equals((Object)hitPos)) continue;
            return p;
        }
        return p;
    }

    private static int readRotateMs() {
        int cd = GuiClient.CONFIG.autoPlotekCooldownMs;
        if (cd < 1 || cd > 500) {
            cd = 80;
        }
        return cd;
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
}

