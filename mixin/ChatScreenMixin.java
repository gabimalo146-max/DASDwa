package me.Gui.gui.mixin;

import me.Gui.gui.client.command.BindCommand;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ChatScreen.class})
public class ChatScreenMixin {
    @Inject(method={"sendMessage"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$bindCommand(String chatText, boolean addToHistory, CallbackInfo ci) {
        if (BindCommand.handleMessage(chatText, addToHistory)) {
            ci.cancel();
        }
    }
}

