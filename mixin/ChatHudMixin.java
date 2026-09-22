package me.Gui.gui.mixin;

import me.Gui.gui.modules.AutoTpaAcceptUtil;
import me.Gui.gui.modules.NameProtectUtil;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={ChatHud.class})
public class ChatHudMixin {
    @ModifyVariable(method={"addMessage(Lnet/minecraft/text/Text;)V"}, at=@At(value="HEAD"), argsOnly=true)
    private Text gui$nameProtectChat(Text text) {
        AutoTpaAcceptUtil.onChatMessage(text);
        return NameProtectUtil.replaceText(text);
    }

    @ModifyVariable(method={"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V"}, at=@At(value="HEAD"), argsOnly=true, require=0)
    private Text gui$nameProtectChatSigned(Text text) {
        AutoTpaAcceptUtil.onChatMessage(text);
        return NameProtectUtil.replaceText(text);
    }

    @ModifyVariable(method={"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V"}, at=@At(value="HEAD"), argsOnly=true, require=0)
    private Text gui$nameProtectChatSignedLegacy(Text text) {
        AutoTpaAcceptUtil.onChatMessage(text);
        return NameProtectUtil.replaceText(text);
    }
}

