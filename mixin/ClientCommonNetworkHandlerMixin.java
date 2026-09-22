package me.Gui.gui.mixin;

import me.Gui.gui.modules.FreecamUtil;
import me.Gui.gui.modules.SilentRotationUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientCommonNetworkHandler.class})
public class ClientCommonNetworkHandlerMixin {
    @Inject(method={"sendPacket"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$freecamCancelMovePackets(Packet<?> packet, CallbackInfo ci) {
        if (!FreecamUtil.shouldCancelPackets() || FreecamUtil.followBody()) {
            return;
        }
        if (packet instanceof PlayerMoveC2SPacket) {
            ci.cancel();
        }
    }

    @ModifyVariable(method={"sendPacket"}, at=@At(value="HEAD"), argsOnly=true)
    private Packet<?> gui$freecamPearlFromStanding(Packet<?> packet) {
        if (!FreecamUtil.isActive() || FreecamUtil.followBody()) {
            return packet;
        }
        if (!(packet instanceof PlayerInteractItemC2SPacket)) {
            return packet;
        }
        PlayerInteractItemC2SPacket itemPacket = (PlayerInteractItemC2SPacket)packet;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return packet;
        }
        ItemStack stack = client.player.getStackInHand(itemPacket.getHand());
        if (stack == null || stack.isEmpty() || stack.getItem() != Items.ENDER_PEARL) {
            return packet;
        }
        return new PlayerInteractItemC2SPacket(itemPacket.getHand(), itemPacket.getSequence(), FreecamUtil.originYaw(), FreecamUtil.originPitch());
    }

    @ModifyVariable(method={"sendPacket"}, at=@At(value="HEAD"), argsOnly=true)
    private Packet<?> gui$silentRotation(Packet<?> packet) {
        if (!(packet instanceof PlayerMoveC2SPacket)) {
            return packet;
        }
        PlayerMoveC2SPacket move = (PlayerMoveC2SPacket)packet;
        if (!SilentRotationUtil.isActive()) {
            return packet;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return packet;
        }
        float yaw = SilentRotationUtil.yaw();
        float pitch = SilentRotationUtil.pitch();
        boolean onGround = move.isOnGround();
        boolean horiz = move.horizontalCollision();
        if (move.changesPosition()) {
            double x = move.getX(client.player.getX());
            double y = move.getY(client.player.getY());
            double z = move.getZ(client.player.getZ());
            return new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, onGround, horiz);
        }
        return new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, onGround, horiz);
    }
}

