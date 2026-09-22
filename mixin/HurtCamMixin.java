package me.Gui.gui.mixin;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GameRenderer.class})
public class HurtCamMixin {
    @Inject(method={"tiltViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void gui$noHurtCamTilt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        HackModule m = GuiClient.MODULES.byId(ModuleId.NO_HURT_CAM);
        if (m != null && m.isEnabled()) {
            ci.cancel();
        }
    }
}

