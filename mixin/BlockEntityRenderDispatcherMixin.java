package me.Gui.gui.mixin;

import me.Gui.gui.modules.XrayUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BlockEntityRenderDispatcher.class})
public class BlockEntityRenderDispatcherMixin {
    @Inject(method={"render"}, at={@At(value="HEAD")}, cancellable=true)
    private <E extends BlockEntity> void gui$xrayBlockEntityRender(E blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (blockEntity == null) {
            return;
        }
        BlockState state = blockEntity.getCachedState();
        if (state == null) {
            return;
        }
        if (!XrayUtil.shouldRender(state)) {
            ci.cancel();
        }
    }
}

