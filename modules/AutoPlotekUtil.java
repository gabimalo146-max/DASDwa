package me.Gui.gui.modules;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import me.Gui.gui.modules.SilentRotationUtil;
import net.minecraft.block.Block;
import net.minecraft.block.CobwebBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public final class AutoPlotekUtil {
    private static final int DEFAULT_COOLDOWN_MS = 80;
    private static final int MIN_COOLDOWN_MS = 0;
    private static final int MAX_COOLDOWN_MS = 500;
    private static final long BREAK_COOLDOWN_MS = 80L;
    private static final double MAX_RANGE_SQ = 16.0;
    private static BlockPos currentTarget = null;
    private static int originalSlot = -1;
    private static long lastActionMs = 0L;

    private AutoPlotekUtil() {
    }

    public static boolean tick(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) {
            AutoPlotekUtil.reset(client);
            return false;
        }
        if (!AutoPlotekUtil.isEnabled()) {
            AutoPlotekUtil.reset(client);
            return false;
        }
        if (client.currentScreen != null) {
            AutoPlotekUtil.reset(client);
            return false;
        }
        ClientPlayerEntity player = client.player;
        BlockPos feetPos = player.getBlockPos();
        if (!AutoPlotekUtil.isCobweb(client.world.getBlockState(feetPos).getBlock())) {
            AutoPlotekUtil.reset(client);
            return false;
        }
        BlockPos underPos = feetPos.down();
        Block underBlock = client.world.getBlockState(underPos).getBlock();
        if (!AutoPlotekUtil.isFenceOrWall(underBlock)) {
            AutoPlotekUtil.reset(client);
            return false;
        }
        if (player.squaredDistanceTo(Vec3d.ofCenter((Vec3i)underPos)) > 16.0) {
            AutoPlotekUtil.reset(client);
            return false;
        }
        if (client.interactionManager == null) {
            return true;
        }
        AutoPlotekUtil.equipToolIfPossible((PlayerEntity)player);
        float[] rot = AutoPlotekUtil.calcYawPitch(player.getEyePos(), Vec3d.ofCenter((Vec3i)underPos));
        boolean aligned = SilentRotationUtil.update(client, rot[0], rot[1], AutoPlotekUtil.readCooldownMs());
        if (!aligned) {
            currentTarget = null;
            lastActionMs = 0L;
            return true;
        }
        AutoPlotekUtil.breakTarget(client, (PlayerEntity)player, underPos);
        return true;
    }

    private static boolean isEnabled() {
        HackModule mod = GuiClient.MODULES.byId(ModuleId.ANTI_TRAP);
        if (mod == null || !mod.isEnabled()) {
            return false;
        }
        return GuiClient.CONFIG.antiTrapAutoPlotek != null && GuiClient.CONFIG.antiTrapAutoPlotek != false;
    }

    private static void breakTarget(MinecraftClient client, PlayerEntity player, BlockPos target) {
        long now = System.currentTimeMillis();
        if (currentTarget == null || !currentTarget.equals((Object)target)) {
            currentTarget = target.toImmutable();
            client.interactionManager.attackBlock(currentTarget, Direction.UP);
            player.swingHand(Hand.MAIN_HAND);
            lastActionMs = now;
            return;
        }
        if (now - lastActionMs < 80L) {
            return;
        }
        client.interactionManager.updateBlockBreakingProgress(currentTarget, Direction.UP);
        player.swingHand(Hand.MAIN_HAND);
        lastActionMs = now;
    }

    private static void equipToolIfPossible(PlayerEntity player) {
        int slotToUse;
        int axeSlot = AutoPlotekUtil.findAxeSlot(player);
        int n = slotToUse = axeSlot != -1 ? axeSlot : AutoPlotekUtil.findEmptyHotbarSlot(player);
        if (slotToUse == -1) {
            return;
        }
        if (originalSlot == -1) {
            originalSlot = player.getInventory().selectedSlot;
        }
        player.getInventory().selectedSlot = slotToUse;
    }

    private static int findAxeSlot(PlayerEntity player) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof AxeItem)) continue;
            return i;
        }
        return -1;
    }

    private static int findEmptyHotbarSlot(PlayerEntity player) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack != null && !stack.isEmpty()) continue;
            return i;
        }
        return -1;
    }

    private static void reset(MinecraftClient client) {
        currentTarget = null;
        SilentRotationUtil.clear();
        AutoPlotekUtil.restoreSlot(client);
        lastActionMs = 0L;
    }

    private static void restoreSlot(MinecraftClient client) {
        if (originalSlot == -1) {
            return;
        }
        if (client != null && client.player != null) {
            client.player.getInventory().selectedSlot = originalSlot;
        }
        originalSlot = -1;
    }

    private static int readCooldownMs() {
        int cd = GuiClient.CONFIG.autoPlotekCooldownMs;
        if (cd < 0 || cd > 500) {
            cd = 80;
        }
        if (cd <= 0) {
            cd = 1;
        }
        return cd;
    }

    private static boolean isCobweb(Block block) {
        return block instanceof CobwebBlock;
    }

    private static boolean isFenceOrWall(Block block) {
        return block instanceof FenceBlock || block instanceof WallBlock;
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

