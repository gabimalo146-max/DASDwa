package me.Gui.gui.mixin;

import me.Gui.gui.modules.FreecamUtil;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={PlayerEntity.class})
public class PlayerEntityMixin {
    @Inject(method={"getBlockInteractionRange"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$freecamBlockRange(CallbackInfoReturnable<Double> cir) {
        if (FreecamUtil.isEnabled()) {
            cir.setReturnValue(4.0);
        }
    }

    @Inject(method={"getEntityInteractionRange"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$freecamEntityRange(CallbackInfoReturnable<Double> cir) {
        if (FreecamUtil.isEnabled()) {
            cir.setReturnValue(3.0);
        }
    }
}

