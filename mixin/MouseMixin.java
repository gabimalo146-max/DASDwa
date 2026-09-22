package me.Gui.gui.mixin;

import me.Gui.gui.modules.FreecamUtil;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={Mouse.class})
public class MouseMixin {
    @ModifyArgs(method={"updateMouse"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"), require=0)
    private void gui$freecamModifyLookArgs(Args args) {
        if (!FreecamUtil.isActive() || FreecamUtil.followBody()) {
            return;
        }
        double dx = (Double)args.get(0);
        double dy = (Double)args.get(1);
        FreecamUtil.applyLookDelta(dx, dy);
        args.set(0, (Object)0.0);
        args.set(1, (Object)0.0);
    }
}

