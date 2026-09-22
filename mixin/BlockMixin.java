package me.Gui.gui.mixin;

import me.Gui.gui.modules.XrayUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Block.class})
public class BlockMixin {
    @Inject(method={"shouldDrawSide"}, at={@At(value="HEAD")}, cancellable=true)
    private static void gui$xrayDrawSide(BlockState state, BlockState neighbor, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (!XrayUtil.isEnabled()) {
            return;
        }
        boolean cur = XrayUtil.isAllowed(state);
        boolean adj = XrayUtil.isAllowed(neighbor);
        if (!cur) {
            cir.setReturnValue(false);
            return;
        }
        if (!adj) {
            cir.setReturnValue(true);
        }
    }
}

