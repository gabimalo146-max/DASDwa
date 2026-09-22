package me.Gui.gui.mixin;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.config.GuiConfig;
import me.Gui.gui.modules.NameProtectUtil;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={PlayerEntityRenderer.class})
public abstract class NameProtectPlayerEntityRendererMixin {
    @ModifyVariable(method={"renderLabelIfPresent"}, at=@At(value="HEAD"), argsOnly=true)
    private Text gui$displayNameLabel(Text text, PlayerEntityRenderState state) {
        String display;
        boolean changeNormal;
        GuiConfig cfg = GuiClient.CONFIG;
        boolean bl = changeNormal = cfg == null || cfg.friendsChangeNormalNametag == null || cfg.friendsChangeNormalNametag != false;
        if (changeNormal && !(display = NameProtectPlayerEntityRendererMixin.resolveDisplayName(state)).isBlank()) {
            return Text.literal((String)display).setStyle(text.getStyle());
        }
        return NameProtectUtil.replaceText(text);
    }

    private static String resolveDisplayName(PlayerEntityRenderState state) {
        if (state == null || state.name == null || state.name.isBlank()) {
            return "";
        }
        String display = GuiClient.FRIENDS.getDisplayName(state.name);
        return display == null ? "" : display.trim();
    }
}

