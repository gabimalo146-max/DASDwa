package me.Gui.gui.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import me.Gui.gui.client.command.BindCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ChatInputSuggestor.class})
public class ChatInputSuggestorMixin {
    @Shadow
    @Final
    private TextFieldWidget textField;
    @Shadow
    private boolean completingSuggestions;
    @Shadow
    private List<OrderedText> messages;
    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow
    private boolean windowActive;
    @Shadow
    private MinecraftClient client;
    @Shadow
    private ParseResults<CommandSource> parse;

    @Inject(method={"keyPressed"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$bindTab(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (((ChatInputSuggestor)(Object)this).isOpen()) {
            return;
        }
        if (BindCommand.handleTabCompletion(this.textField, keyCode, modifiers)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method={"refresh"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$bindRefresh(CallbackInfo ci) {
        int cursor;
        String text = this.textField.getText();
        BindCommand.SuggestionResult res = BindCommand.getSuggestions(text, cursor = this.textField.getCursor());
        if (res == null) {
            return;
        }
        if (!this.completingSuggestions) {
            this.textField.setSuggestion(null);
            ((ChatInputSuggestor)(Object)this).clearWindow();
        }
        this.messages.clear();
        this.parse = null;
        int safeCursor = Math.max(0, Math.min(cursor, text.length()));
        String sub = text.substring(0, safeCursor);
        SuggestionsBuilder builder = new SuggestionsBuilder(sub, res.start);
        this.pendingSuggestions = CommandSource.suggestMatching(res.suggestions, (SuggestionsBuilder)builder);
        this.pendingSuggestions.thenRun(this::gui$showBindSuggestions);
        ci.cancel();
    }

    @Unique
    private void gui$showBindSuggestions() {
        ((ChatInputSuggestor)(Object)this).clearWindow();
        if (this.windowActive && this.client != null && ((Boolean)this.client.options.getAutoSuggestions().getValue()).booleanValue()) {
            ((ChatInputSuggestor)(Object)this).show(false);
        }
    }
}

