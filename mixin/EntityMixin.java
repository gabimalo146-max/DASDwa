package me.Gui.gui.mixin;

import me.Gui.gui.modules.AntiTrapUtil;
import me.Gui.gui.modules.FreecamUtil;
import me.Gui.gui.modules.NoPushUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Entity.class})
public abstract class EntityMixin {
    @Inject(method={"canHit()Z"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$antiTrapCanHit(CallbackInfoReturnable<Boolean> cir) {
        Entity self = ((Entity)(Object)this);
        if (AntiTrapUtil.shouldIgnore(self) && !AntiTrapUtil.allowAttack()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method={"isPushedByFluids"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$noPushFluids(CallbackInfoReturnable<Boolean> cir) {
        if (!NoPushUtil.liquidsEnabled()) {
            return;
        }
        Entity self = ((Entity)(Object)this);
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && self == client.player) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method={"pushAwayFrom"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$noPushPlayers(Entity other, CallbackInfo ci) {
        if (!NoPushUtil.playersEnabled()) {
            return;
        }
        Entity self = ((Entity)(Object)this);
        if (!(self instanceof PlayerEntity) || !(other instanceof PlayerEntity)) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && self == client.player) {
            ci.cancel();
        }
    }

    @Inject(method={"changeLookDirection"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$freecamRedirectLook(double deltaX, double deltaY, CallbackInfo ci) {
        if (!FreecamUtil.isActive() || FreecamUtil.followBody()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }
        Entity self = ((Entity)(Object)this);
        if (self != client.player) {
            return;
        }
        FreecamUtil.applyLookDelta(deltaX, deltaY);
        ci.cancel();
    }
}

