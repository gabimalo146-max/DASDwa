package me.Gui.gui.mixin;

import me.Gui.gui.modules.XrayUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BlockRenderManager.class})
public class BlockRenderManagerMixin {
    @Inject(method={"renderBlock"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$xrayRenderBlock(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, Random random, CallbackInfo ci) {
        if (!XrayUtil.shouldRender(state)) {
            ci.cancel();
        }
    }

    @Inject(method={"renderFluid"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$xrayRenderFluid(BlockPos pos, BlockRenderView world, VertexConsumer vertexConsumer, BlockState state, FluidState fluidState, CallbackInfo ci) {
        if (!XrayUtil.shouldRender(state)) {
            ci.cancel();
        }
    }

    @Inject(method={"renderBlockAsEntity"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$xrayRenderBlockAsEntity(BlockState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
        if (!XrayUtil.shouldRender(state)) {
            ci.cancel();
        }
    }
}

