package me.Gui.gui.mixin;

import me.Gui.gui.modules.NameProtectUtil;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.text.StringVisitable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={ChatMessages.class})
public class ChatMessagesMixin {
    @ModifyVariable(method={"breakRenderedChatMessageLines"}, at=@At(value="HEAD"), argsOnly=true)
    private static StringVisitable gui$nameProtectChatLines(StringVisitable message) {
        return NameProtectUtil.replaceVisitable(message);
    }
}

