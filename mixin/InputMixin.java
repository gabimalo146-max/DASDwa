package me.Gui.gui.mixin;

import net.minecraft.client.input.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Input.class})
public class InputMixin {
    @Inject(method={"tick"}, at={@At(value="TAIL")})
    private void gui$betterHitboxesMoveFix(CallbackInfo ci) {
    }
}

