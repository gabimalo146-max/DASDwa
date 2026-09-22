package me.Gui.gui.modules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import me.Gui.gui.client.GuiClient;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;

public final class FastLeverUtil {
    private static int cooldown = 0;
    private static boolean isRotating = false;
    private static float rotYaw = 0.0f;
    private static float rotPitch = 0.0f;
    private static long lastRotateMs = 0L;
    private static final int ROTATE_MS = 80;
    private static final Map<BlockPos, Long> FRIENDLY_FLUID = new HashMap<BlockPos, Long>();
    private static final Set<BlockPos> OWNED_FLUID = new HashSet<BlockPos>();
    private static final Set<BlockPos> LAST_SOURCES = new HashSet<BlockPos>();
    private static long lastSelfPlaceMs = 0L;
    private static BlockPos lastSelfPlacePos = null;
    private static final long SELF_WINDOW_MS = 2000L;
    private static final int SELF_RADIUS = 2;
    private static final long FRIENDLY_PROPAGATE_MS = 10000L;
    private static final long FRIENDLY_GRACE_MS = 2500L;

    private FastLeverUtil() {
    }

    public static void tick(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) {
            FastLeverUtil.reset(client);
            return;
        }
        if (!FastLeverUtil.isEnabled()) {
            FastLeverUtil.reset(client);
            return;
        }
        if (cooldown > 0 && --cooldown == 0) {
            FastLeverUtil.stopRotation();
        }
        FastLeverUtil.updateFriendlySources(client);
        if (FastLeverUtil.isHoldingAnyBucket((PlayerEntity)client.player)) {
            if (cooldown == 0) {
                FastLeverUtil.stopRotation();
            }
            return;
        }
        if (client.currentScreen != null) {
            if (cooldown == 0) {
                FastLeverUtil.stopRotation();
            }
            return;
        }
        if (cooldown > 0) {
            return;
        }
        if (!FastLeverUtil.hasLever((PlayerEntity)client.player)) {
            FastLeverUtil.stopRotation();
            return;
        }
        if (FastLeverUtil.shouldYieldToFriendlyWater(client)) {
            FastLeverUtil.stopRotation();
            return;
        }
        if (!FastLeverUtil.isOpponentWithBucketNearby(client)) {
            FastLeverUtil.stopRotation();
            return;
        }
        BlockPos targetPos = FastLeverUtil.findFluidTarget(client);
        if (targetPos != null) {
            FastLeverUtil.executeAction(client, targetPos);
        } else {
            FastLeverUtil.stopRotation();
        }
    }

    private static boolean isEnabled() {
        HackModule m = GuiClient.MODULES.byId(ModuleId.FAST_LEVER);
        return m != null && m.isEnabled();
    }

    private static void reset(MinecraftClient client) {
        FastLeverUtil.stopRotation();
        cooldown = 0;
    }

    private static void stopRotation() {
        if (!isRotating) {
            return;
        }
        isRotating = false;
        lastRotateMs = 0L;
    }

    private static boolean hasLever(PlayerEntity player) {
        if (player.getOffHandStack().isOf(Items.LEVER)) {
            return true;
        }
        for (int i = 0; i < 9; ++i) {
            if (!player.getInventory().getStack(i).isOf(Items.LEVER)) continue;
            return true;
        }
        return false;
    }

    private static boolean isOpponentWithBucketNearby(MinecraftClient client) {
        for (PlayerEntity player : client.world.getPlayers()) {
            String name;
            if (player == client.player || GuiClient.FRIENDS.isFriend(name = player.getName().getString().toLowerCase()) || !(client.player.squaredDistanceTo((Entity)player) <= 25.0) || !FastLeverUtil.isHoldingAnyBucket(player)) continue;
            return true;
        }
        return false;
    }

    private static boolean shouldYieldToFriendlyWater(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) {
            return false;
        }
        for (PlayerEntity player : client.world.getPlayers()) {
            String name;
            if (player == null || player == client.player || !GuiClient.FRIENDS.isFriend(name = player.getName().getString().toLowerCase()) || client.player.squaredDistanceTo((Entity)player) > 25.0 || !FastLeverUtil.isHoldingAnyBucket(player)) continue;
            return true;
        }
        return false;
    }

    private static boolean isHoldingAnyBucket(PlayerEntity player) {
        return player.getMainHandStack().isOf(Items.BUCKET) || player.getMainHandStack().isOf(Items.WATER_BUCKET) || player.getMainHandStack().isOf(Items.LAVA_BUCKET) || player.getOffHandStack().isOf(Items.BUCKET) || player.getOffHandStack().isOf(Items.WATER_BUCKET) || player.getOffHandStack().isOf(Items.LAVA_BUCKET);
    }

    private static BlockPos findFluidTarget(MinecraftClient client) {
        BlockPos pPos = client.player.getBlockPos();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.iterate((BlockPos)pPos.add(-5, -3, -5), (BlockPos)pPos.add(5, 3, 5))) {
            double dist;
            if (!FastLeverUtil.isFreeFluidSource(client, pos) || FastLeverUtil.isFriendlyFluid(pos) || (dist = client.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter((Vec3i)pos))) > 20.25 || !FastLeverUtil.canSee(client, pos) || !(dist < bestDist)) continue;
            bestDist = dist;
            best = pos.toImmutable();
        }
        return best;
    }

    private static boolean canSee(MinecraftClient client, BlockPos pos) {
        Vec3d targetPos;
        Vec3d eyePos = client.player.getEyePos();
        BlockHitResult result = client.world.raycast(new RaycastContext(eyePos, targetPos = Vec3d.ofCenter((Vec3i)pos), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)client.player));
        return result.getType() == HitResult.Type.MISS || result.getBlockPos().equals((Object)pos);
    }

    private static void executeAction(MinecraftClient client, BlockPos fluidPos) {
        BlockPos solidPos = FastLeverUtil.findAdjacentSolidBlock(client, fluidPos);
        if (solidPos == null) {
            return;
        }
        if (client.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter((Vec3i)solidPos)) > 20.25) {
            return;
        }
        if (!FastLeverUtil.canSee(client, solidPos)) {
            return;
        }
        if (FastLeverUtil.updateRotation(client, solidPos)) {
            BlockHitResult hit = FastLeverUtil.raycastToBlock(client, solidPos);
            if (hit == null) {
                FastLeverUtil.stopRotation();
                return;
            }
            FastLeverUtil.performAction(client, hit);
        }
    }

    private static void performAction(MinecraftClient client, BlockHitResult bhr) {
        Hand hand;
        if (client.interactionManager == null) {
            return;
        }
        Hand hand2 = hand = client.player.getOffHandStack().isOf(Items.LEVER) ? Hand.OFF_HAND : Hand.MAIN_HAND;
        if (hand == Hand.MAIN_HAND) {
            int slot = FastLeverUtil.findLeverSlot(client);
            if (slot == -1) {
                return;
            }
            client.player.getInventory().selectedSlot = slot;
        }
        client.interactionManager.interactBlock(client.player, hand, bhr);
        client.player.swingHand(hand);
        cooldown = FastLeverUtil.computeCooldown();
    }

    private static int findLeverSlot(MinecraftClient client) {
        for (int i = 0; i < 9; ++i) {
            if (!client.player.getInventory().getStack(i).isOf(Items.LEVER)) continue;
            return i;
        }
        return -1;
    }

    private static BlockPos findAdjacentSolidBlock(MinecraftClient client, BlockPos pos) {
        if (client == null || client.world == null || client.player == null) {
            return null;
        }
        if (FastLeverUtil.hasBlockAboveHead(client)) {
            BlockPos down = FastLeverUtil.findSolidNeighbor(client, pos, new Direction[]{Direction.DOWN});
            if (down != null) {
                return down;
            }
            BlockPos side = FastLeverUtil.findSolidNeighbor(client, pos, new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST});
            if (side != null) {
                return side;
            }
            BlockPos up = FastLeverUtil.findSolidNeighbor(client, pos, new Direction[]{Direction.UP});
            if (up != null) {
                return up;
            }
            return null;
        }
        for (Direction d : Direction.values()) {
            BlockPos neighbor = pos.offset(d);
            if (!client.world.getBlockState(neighbor).isSolidBlock((BlockView)client.world, neighbor)) continue;
            return neighbor;
        }
        return null;
    }

    private static BlockPos findSolidNeighbor(MinecraftClient client, BlockPos pos, Direction[] dirs) {
        for (Direction d : dirs) {
            BlockPos neighbor = pos.offset(d);
            if (!client.world.getBlockState(neighbor).isSolidBlock((BlockView)client.world, neighbor)) continue;
            return neighbor;
        }
        return null;
    }

    private static boolean hasBlockAboveHead(MinecraftClient client) {
        BlockPos above = client.player.getBlockPos().up(2);
        return client.world.getBlockState(above).isSolidBlock((BlockView)client.world, above);
    }

    private static boolean isFriendlyFluid(BlockPos pos) {
        return OWNED_FLUID.contains(pos) || FRIENDLY_FLUID.containsKey(pos);
    }

    public static void noteFriendlyFluidPlace(MinecraftClient client, Hand hand, BlockHitResult hit) {
        if (client == null || client.player == null || client.world == null) {
            return;
        }
        if (hand == null || hit == null) {
            return;
        }
        ItemStack stack = client.player.getStackInHand(hand);
        if (stack == null || stack.isEmpty()) {
            return;
        }
        if (!stack.isOf(Items.WATER_BUCKET) && !stack.isOf(Items.LAVA_BUCKET)) {
            return;
        }
        BlockPos placed = hit.getBlockPos().offset(hit.getSide()).toImmutable();
        long now = System.currentTimeMillis();
        FRIENDLY_FLUID.put(placed, now);
        OWNED_FLUID.add(placed);
        FRIENDLY_FLUID.put(hit.getBlockPos().toImmutable(), now);
        OWNED_FLUID.add(hit.getBlockPos().toImmutable());
        lastSelfPlaceMs = now;
        lastSelfPlacePos = placed;
    }

    private static void updateFriendlySources(MinecraftClient client) {
        if (client == null || client.world == null || client.player == null) {
            FRIENDLY_FLUID.clear();
            OWNED_FLUID.clear();
            LAST_SOURCES.clear();
            return;
        }
        long now = System.currentTimeMillis();
        BlockPos pPos = client.player.getBlockPos();
        HashSet<BlockPos> current = new HashSet<BlockPos>();
        for (BlockPos pos : BlockPos.iterate((BlockPos)pPos.add(-5, -3, -5), (BlockPos)pPos.add(5, 3, 5))) {
            if (!FastLeverUtil.isFreeFluidSource(client, pos)) continue;
            BlockPos bp = pos.toImmutable();
            current.add(bp);
            if (!LAST_SOURCES.contains(bp) && FastLeverUtil.isLikelyFriendPlaced(client, bp)) {
                FRIENDLY_FLUID.put(bp, now);
                OWNED_FLUID.add(bp);
                continue;
            }
            if (!LAST_SOURCES.contains(bp) && FastLeverUtil.isLikelySelfPlaced(bp, now)) {
                FRIENDLY_FLUID.put(bp, now);
                OWNED_FLUID.add(bp);
                continue;
            }
            if (LAST_SOURCES.contains(bp) || !FastLeverUtil.isAdjacentToRecentFriendly(bp, now)) continue;
            FRIENDLY_FLUID.put(bp, now);
        }
        LAST_SOURCES.clear();
        LAST_SOURCES.addAll(current);
        FastLeverUtil.pruneFriendlyFluid(client);
    }

    private static boolean isLikelySelfPlaced(BlockPos pos, long now) {
        if (lastSelfPlacePos == null) {
            return false;
        }
        if (now - lastSelfPlaceMs > 2000L) {
            return false;
        }
        return lastSelfPlacePos.getSquaredDistance((Vec3i)pos) <= 4.0;
    }

    private static boolean isAdjacentToRecentFriendly(BlockPos pos, long now) {
        for (Direction d : Direction.values()) {
            BlockPos n = pos.offset(d);
            if (OWNED_FLUID.contains(n)) {
                return true;
            }
            Long t = FRIENDLY_FLUID.get(n);
            if (t == null || now - t > 10000L) continue;
            return true;
        }
        return false;
    }

    private static boolean isLikelyFriendPlaced(MinecraftClient client, BlockPos pos) {
        Vec3d center = Vec3d.ofCenter((Vec3i)pos);
        for (PlayerEntity player : client.world.getPlayers()) {
            String name;
            if (player == null || player == client.player || !GuiClient.FRIENDS.isFriend(name = player.getName().getString().toLowerCase()) || player.squaredDistanceTo(center) > 49.0 || !FastLeverUtil.isHoldingAnyBucket(player)) continue;
            return true;
        }
        return false;
    }

    private static void pruneFriendlyFluid(MinecraftClient client) {
        if (client == null || client.world == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<BlockPos, Long>> it = FRIENDLY_FLUID.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, Long> e = it.next();
            if (FastLeverUtil.isFreeFluidSource(client, e.getKey()) || now - e.getValue() <= 2500L) continue;
            it.remove();
        }
        Iterator<BlockPos> itOwned = OWNED_FLUID.iterator();
        while (itOwned.hasNext()) {
            Long t;
            BlockPos pos = itOwned.next();
            if (FastLeverUtil.isFreeFluidSource(client, pos) || (t = FRIENDLY_FLUID.get(pos)) != null && now - t <= 2500L) continue;
            itOwned.remove();
        }
    }

    private static boolean isFreeFluidSource(MinecraftClient client, BlockPos pos) {
        if (client == null || client.world == null || pos == null) {
            return false;
        }
        FluidState fluid = client.world.getFluidState(pos);
        if (fluid.getFluid() != Fluids.WATER && fluid.getFluid() != Fluids.LAVA || !fluid.isStill()) {
            return false;
        }
        return !FastLeverUtil.isWaterlogged(client.world.getBlockState(pos));
    }

    private static boolean isWaterlogged(BlockState state) {
        return state != null && state.contains((Property)Properties.WATERLOGGED) && (Boolean)state.get((Property)Properties.WATERLOGGED) != false;
    }

    private static BlockHitResult raycastToBlock(MinecraftClient client, BlockPos pos) {
        BlockHitResult bhr;
        Vec3d targetPos;
        Vec3d eyePos = client.player.getEyePos();
        BlockHitResult result = client.world.raycast(new RaycastContext(eyePos, targetPos = Vec3d.ofCenter((Vec3i)pos), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)client.player));
        if (result instanceof BlockHitResult && (bhr = result).getBlockPos().equals((Object)pos)) {
            return bhr;
        }
        return null;
    }

    private static boolean updateRotation(MinecraftClient client, BlockPos pos) {
        long dtMs;
        Vec3d target = Vec3d.ofCenter((Vec3i)pos);
        Vec3d eye = client.player.getEyePos();
        double dx = target.x - eye.x;
        double dy = target.y - eye.y;
        double dz = target.z - eye.z;
        float targetYaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
        long now = System.currentTimeMillis();
        if (!isRotating) {
            rotYaw = client.player.getYaw();
            rotPitch = client.player.getPitch();
            lastRotateMs = now;
            isRotating = true;
        }
        if ((dtMs = now - lastRotateMs) < 1L) {
            dtMs = 1L;
        }
        lastRotateMs = now;
        float maxStep = FastLeverUtil.rotationSpeedDegPerMs(80) * (float)dtMs;
        rotYaw = FastLeverUtil.approachAngle(rotYaw, targetYaw, maxStep);
        rotPitch = FastLeverUtil.approach(rotPitch, targetPitch, maxStep);
        rotPitch = MathHelper.clamp((float)rotPitch, (float)-90.0f, (float)90.0f);
        client.player.setYaw(rotYaw);
        client.player.setPitch(rotPitch);
        client.player.setHeadYaw(rotYaw);
        client.player.setBodyYaw(rotYaw);
        float yawDiff = Math.abs(MathHelper.wrapDegrees((float)(targetYaw - rotYaw)));
        float pitchDiff = Math.abs(targetPitch - rotPitch);
        return yawDiff <= 1.0f && pitchDiff <= 1.0f;
    }

    private static int computeCooldown() {
        int cd;
        float speed = GuiClient.CONFIG.fastLeverSpeed;
        if (Float.isNaN(speed) || Float.isInfinite(speed)) {
            speed = 100.0f;
        }
        if ((cd = (int)(9.0f - (speed = FastLeverUtil.clamp(speed, 0.0f, 100.0f)) / 12.0f)) < 1) {
            cd = 1;
        }
        return cd;
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

    private static float clamp(float v, float a, float b) {
        return Math.max(a, Math.min(b, v));
    }
}

