package me.Gui.gui.mixin;

import me.Gui.gui.modules.NameProtectUtil;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ScoreboardEntry.class})
public class ScoreboardEntryMixin {
    @Shadow
    @Final
    private int value;

    @Inject(method={"name"}, at={@At(value="RETURN")}, cancellable=true)
    private void gui$nameProtectScoreboard(CallbackInfoReturnable<Text> cir) {
        Text replaced = NameProtectUtil.replaceScoreboardText((Text)cir.getReturnValue(), this.value);
        cir.setReturnValue(replaced);
    }
}

