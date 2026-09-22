package me.Gui.gui.mixin;

import me.Gui.gui.modules.XrayUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BlockModelRenderer.class})
public class BlockModelRendererMixin {
    @Inject(method={"render(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLnet/minecraft/util/math/random/Random;JI)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$xrayRenderBlock(BlockRenderView world, BakedModel model, BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, Random random, long seed, int overlay, CallbackInfo ci) {
        if (!XrayUtil.shouldRender(state)) {
            ci.cancel();
        }
    }

    @Inject(method={"render(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/block/BlockState;Lnet/minecraft/client/render/model/BakedModel;FFFII)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$xrayRenderBlockAsEntity(MatrixStack.Entry entry, VertexConsumer vertexConsumer, BlockState state, BakedModel model, float red, float green, float blue, int light, int overlay, CallbackInfo ci) {
        if (!XrayUtil.shouldRender(state)) {
            ci.cancel();
        }
    }
}

