package me.Gui.gui.modules;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class AutoDripUtil {
    private static final ThreadLocal<Boolean> IN_INTERNAL_INTERACT = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private AutoDripUtil() {
    }

    public static boolean isEnabled() {
        HackModule m = GuiClient.MODULES.byId(ModuleId.AUTO_DRIP);
        return m != null && m.isEnabled();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (world == null || !world.isClient) {
            return ActionResult.PASS;
        }
        if (!(player instanceof ClientPlayerEntity)) {
            return ActionResult.PASS;
        }
        if (IN_INTERNAL_INTERACT.get().booleanValue()) {
            return ActionResult.PASS;
        }
        if (!AutoDripUtil.isEnabled()) {
            return ActionResult.PASS;
        }
        if (hand != Hand.MAIN_HAND) {
            return ActionResult.PASS;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || mc.world == null || mc.interactionManager == null) {
            return ActionResult.PASS;
        }
        if (mc.getNetworkHandler() == null) {
            return ActionResult.PASS;
        }
        ClientPlayerEntity clientPlayer = mc.player;
        if (!clientPlayer.getMainHandStack().isOf(Items.POINTED_DRIPSTONE)) {
            return ActionResult.PASS;
        }
        BlockPos blockPos = hitResult.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        if (!(blockState.getBlock() instanceof TrapdoorBlock)) {
            return ActionResult.PASS;
        }
        if (((Boolean)blockState.get((Property)TrapdoorBlock.OPEN)).booleanValue()) {
            return ActionResult.PASS;
        }
        BlockPos down = blockPos.down();
        if (!world.getBlockState(down).isAir()) {
            return ActionResult.PASS;
        }
        IN_INTERNAL_INTERACT.set(Boolean.TRUE);
        try {
            boolean pressedSneak = false;
            if (!clientPlayer.isSneaking()) {
                mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)clientPlayer, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                clientPlayer.setSneaking(true);
                pressedSneak = true;
            }
            double hitY = blockState.get((Property)TrapdoorBlock.HALF) == BlockHalf.TOP ? (double)blockPos.getY() + 1.0 - 0.001 : (double)blockPos.getY() + 0.001;
            Vec3d downHitPos = new Vec3d((double)blockPos.getX() + 0.5, hitY, (double)blockPos.getZ() + 0.5);
            BlockHitResult downHit = new BlockHitResult(downHitPos, Direction.DOWN, blockPos, false);
            ActionResult placeResult = mc.interactionManager.interactBlock(clientPlayer, hand, downHit);
            if (pressedSneak) {
                mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)clientPlayer, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                clientPlayer.setSneaking(false);
            }
            ActionResult originalResult = mc.interactionManager.interactBlock(clientPlayer, hand, hitResult);
            if (placeResult.isAccepted() || originalResult.isAccepted()) {
                clientPlayer.swingHand(hand);
            }
            ActionResult.Success success = ActionResult.SUCCESS;
            return success;
        }
        finally {
            IN_INTERNAL_INTERACT.set(Boolean.FALSE);
        }
    }
}

