package me.Gui.gui.mixin;

import me.Gui.gui.modules.FreecamUtil;
import me.Gui.gui.modules.NoPushUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPlayerEntity.class})
public class ClientPlayerEntityMixin {
    @Inject(method={"pushOutOfBlocks"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$noPushBlocks(double x, double z, CallbackInfo ci) {
        if (NoPushUtil.blocksEnabled() || FreecamUtil.isActive()) {
            ci.cancel();
        }
    }

    @Inject(method={"sendMovementPackets"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$freecamCancelPackets(CallbackInfo ci) {
        if (FreecamUtil.shouldCancelPackets() && !FreecamUtil.followBody()) {
            ci.cancel();
        }
    }

    @Inject(method={"tickMovement"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$freecamMovement(CallbackInfo ci) {
        ClientPlayerEntity self = ((ClientPlayerEntity)(Object)this);
        if (FreecamUtil.handleMovement(self)) {
            ci.cancel();
        }
    }
}

