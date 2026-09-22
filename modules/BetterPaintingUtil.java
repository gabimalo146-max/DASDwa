package me.Gui.gui.modules;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CobwebBlock;
import net.minecraft.block.WallBannerBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;

public final class BetterPaintingUtil {
    private BetterPaintingUtil() {
    }

    public static boolean shouldHandlePainting(MinecraftClient client) {
        if (client == null || client.player == null) {
            return false;
        }
        if (!BetterPaintingUtil.isEnabled()) {
            return false;
        }
        return client.player.getMainHandStack().isOf(Items.PAINTING);
    }

    public static boolean isPassThroughBlock(BlockState state) {
        if (state == null) {
            return false;
        }
        Block block = state.getBlock();
        return block instanceof CobwebBlock || block instanceof BannerBlock || block instanceof WallBannerBlock;
    }

    public static boolean isCobweb(BlockState state) {
        if (state == null) {
            return false;
        }
        return state.getBlock() instanceof CobwebBlock;
    }

    public static BlockHitResult raycastFromCamera(MinecraftClient client, float tickDelta, Vec3d startOverride) {
        Vec3d end;
        BlockHitResult hit;
        if (client == null || client.world == null) {
            return null;
        }
        Entity cam = client.getCameraEntity();
        if (cam == null) {
            return null;
        }
        Vec3d start = startOverride != null ? startOverride : cam.getCameraPosVec(tickDelta);
        Vec3d dir = cam.getRotationVec(tickDelta);
        double range = 4.5;
        if (client.player != null) {
            range = client.player.getBlockInteractionRange();
        }
        if ((hit = client.world.raycast(new RaycastContext(start, end = start.add(dir.x * range, dir.y * range, dir.z * range), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, cam))) instanceof BlockHitResult) {
            BlockHitResult bhr = hit;
            if (hit.getType() == HitResult.Type.BLOCK) {
                return bhr;
            }
        }
        return null;
    }

    public static BlockHitResult skipPassThrough(MinecraftClient client, float tickDelta, BlockHitResult initial) {
        if (client == null || client.world == null || initial == null) {
            return null;
        }
        if (!BetterPaintingUtil.isPassThroughBlock(client.world.getBlockState(initial.getBlockPos()))) {
            return initial;
        }
        Vec3d start = BetterPaintingUtil.offsetPastHit(initial, client, tickDelta);
        for (int i = 0; i < 8 && start != null; ++i) {
            BlockHitResult hit = BetterPaintingUtil.raycastFromCamera(client, tickDelta, start);
            if (hit == null) {
                return null;
            }
            if (!BetterPaintingUtil.isPassThroughBlock(client.world.getBlockState(hit.getBlockPos()))) {
                return hit;
            }
            start = BetterPaintingUtil.offsetPastHit(hit, client, tickDelta);
        }
        return null;
    }

    public static BlockHitResult skipCobwebs(MinecraftClient client, float tickDelta, BlockHitResult initial) {
        if (client == null || client.world == null || initial == null) {
            return null;
        }
        if (!BetterPaintingUtil.isCobweb(client.world.getBlockState(initial.getBlockPos()))) {
            return initial;
        }
        Vec3d start = BetterPaintingUtil.advancePastHit(initial, client, tickDelta);
        for (int i = 0; i < 8 && start != null; ++i) {
            BlockHitResult hit = BetterPaintingUtil.raycastFromCamera(client, tickDelta, start);
            if (hit == null) {
                return null;
            }
            if (!BetterPaintingUtil.isCobweb(client.world.getBlockState(hit.getBlockPos()))) {
                return hit;
            }
            start = BetterPaintingUtil.advancePastHit(hit, client, tickDelta);
        }
        return null;
    }

    private static Vec3d advancePastHit(BlockHitResult hit, MinecraftClient client, float tickDelta) {
        if (hit == null || client == null) {
            return null;
        }
        Entity cam = client.getCameraEntity();
        if (cam == null) {
            return null;
        }
        Vec3d dir = cam.getRotationVec(tickDelta);
        if (dir == null) {
            return null;
        }
        Vec3d start = hit.getPos();
        BlockPos hitPos = hit.getBlockPos();
        Vec3d step = dir.normalize().multiply(0.2);
        for (int i = 0; i < 10 && BlockPos.ofFloored((Position)(start = start.add(step))).equals((Object)hitPos); ++i) {
        }
        return start;
    }

    public static EntityHitResult raycastPainting(MinecraftClient client, Vec3d start, Vec3d dir, double range) {
        if (client == null || client.player == null) {
            return null;
        }
        if (start == null || dir == null) {
            return null;
        }
        Vec3d end = start.add(dir.multiply(range));
        Box box = new Box(start, start).stretch(dir.multiply(range)).expand(1.0);
        return ProjectileUtil.raycast((Entity)client.player, (Vec3d)start, (Vec3d)end, (Box)box, entity -> entity instanceof PaintingEntity && !entity.isSpectator() && entity.canHit(), (double)(range * range));
    }

    public static Direction horizontalFaceTowardCamera(Vec3d blockCenter, Vec3d cameraPos) {
        double az;
        if (blockCenter == null || cameraPos == null) {
            return Direction.NORTH;
        }
        Vec3d toCam = cameraPos.subtract(blockCenter);
        double ax = Math.abs(toCam.x);
        if (ax >= (az = Math.abs(toCam.z))) {
            return toCam.x >= 0.0 ? Direction.EAST : Direction.WEST;
        }
        return toCam.z >= 0.0 ? Direction.SOUTH : Direction.NORTH;
    }

    public static Vec3d offsetPastHit(BlockHitResult hit, MinecraftClient client, float tickDelta) {
        Entity cam;
        if (hit == null) {
            return null;
        }
        Entity entity = cam = client == null ? null : client.getCameraEntity();
        if (cam == null) {
            return null;
        }
        Direction travel = hit.getSide().getOpposite();
        Vec3d center = Vec3d.ofCenter((Vec3i)hit.getBlockPos());
        return center.add((double)travel.getOffsetX() * 0.51, (double)travel.getOffsetY() * 0.51, (double)travel.getOffsetZ() * 0.51);
    }

    private static boolean isEnabled() {
        HackModule m = GuiClient.MODULES.byId(ModuleId.BETTER_PAINTING);
        return m != null && m.isEnabled();
    }
}

