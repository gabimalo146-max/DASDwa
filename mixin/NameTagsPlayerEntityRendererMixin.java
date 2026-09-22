package me.Gui.gui.mixin;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={PlayerEntityRenderer.class})
public abstract class NameTagsPlayerEntityRendererMixin {
    @Inject(method={"renderLabelIfPresent"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$cancelVanillaPlayerLabel(PlayerEntityRenderState state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (NameTagsPlayerEntityRendererMixin.isNametagsEnabled()) {
            ci.cancel();
        }
    }

    private static boolean isNametagsEnabled() {
        HackModule m = GuiClient.MODULES.byId(ModuleId.NAMETAGS);
        return m != null && m.isEnabled();
    }
}

