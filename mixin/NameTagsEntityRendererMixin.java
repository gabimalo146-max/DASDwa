package me.Gui.gui.mixin;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={EntityRenderer.class})
public abstract class NameTagsEntityRendererMixin<T extends Entity> {
    @Inject(method={"hasLabel"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$hideVanillaPlayerLabels(T entity, double squaredDistanceToCamera, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity player = (PlayerEntity)entity;
        MinecraftClient client = MinecraftClient.getInstance();
        boolean enabled = NameTagsEntityRendererMixin.isNametagsEnabled();
        if (client.player != null && player == client.player) {
            cir.setReturnValue(!enabled);
            return;
        }
        if (enabled) {
            cir.setReturnValue(false);
        }
    }

    private static boolean isNametagsEnabled() {
        HackModule m = GuiClient.MODULES.byId(ModuleId.NAMETAGS);
        return m != null && m.isEnabled();
    }
}

