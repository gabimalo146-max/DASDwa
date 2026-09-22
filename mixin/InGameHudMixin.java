package me.Gui.gui.mixin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import me.Gui.gui.modules.NameProtectUtil;
import me.Gui.gui.ui.GuiFonts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={InGameHud.class})
public abstract class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;
    private static final Comparator<ScoreboardEntry> SCOREBOARD_ENTRY_COMPARATOR = Comparator.comparingInt(ScoreboardEntry::value).reversed().thenComparing(ScoreboardEntry::owner, String.CASE_INSENSITIVE_ORDER);

    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Inject(method={"renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$renderCustomScoreboard(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
        if (!NameProtectUtil.shouldUseCustomScoreboard()) {
            return;
        }
        this.renderCustomScoreboard(context, objective);
        ci.cancel();
    }

    private void renderCustomScoreboard(DrawContext context, ScoreboardObjective objective) {
        int titleWidth;
        if (objective == null || this.client == null || this.client.world == null) {
            return;
        }
        Scoreboard scoreboard = objective.getScoreboard();
        List<ScoreboardEntry> entries = new ArrayList<>(scoreboard.getScoreboardEntries(objective));
        entries.removeIf(ScoreboardEntry::hidden);
        entries.sort(SCOREBOARD_ENTRY_COMPARATOR);
        if (entries.size() > 15) {
            entries = entries.subList(0, 15);
        }
        TextRenderer textRenderer = GuiFonts.textRendererOrDefault(this.getTextRenderer());
        ArrayList<SidebarLine> lines = new ArrayList<SidebarLine>(entries.size());
        int lineCount = entries.size();
        for (int i = 0; i < entries.size(); ++i) {
            ScoreboardEntry entry = (ScoreboardEntry)entries.get(i);
            Team team = scoreboard.getScoreHolderTeam(entry.owner());
            Text baseName = entry.name();
            MutableText decorated = Team.decorateName((AbstractTeam)team, (Text)baseName);
            int lineFromBottom = lineCount - i;
            Text name = InGameHudMixin.safeText(NameProtectUtil.replaceScoreboardTextByLine((Text)decorated, lineFromBottom), (Text)decorated);
            lines.add(new SidebarLine(name));
        }
        Text titleText = InGameHudMixin.safeText(NameProtectUtil.replaceText(objective.getDisplayName()), objective.getDisplayName());
        int maxWidth = titleWidth = textRenderer.getWidth((StringVisitable)titleText);
        for (SidebarLine line : lines) {
            int lineWidth = textRenderer.getWidth((StringVisitable)line.name());
            if (lineWidth <= maxWidth) continue;
            maxWidth = lineWidth;
        }
        lineCount = lines.size();
        int totalHeight = lineCount * 9;
        int y = context.getScaledWindowHeight() / 2 + totalHeight / 3;
        int xLeft = context.getScaledWindowWidth() - maxWidth - 3;
        int xRight = context.getScaledWindowWidth() - 3 + 2;
        int backgroundLines = this.client.options.getTextBackgroundColor(0.3f);
        int backgroundHeader = this.client.options.getTextBackgroundColor(0.4f);
        int yStart = y - lineCount * 9;
        context.fill(xLeft - 2, yStart - 10, xRight, yStart - 1, backgroundHeader);
        context.fill(xLeft - 2, yStart - 1, xRight, y, backgroundLines);
        context.drawText(textRenderer, titleText, xLeft + maxWidth / 2 - titleWidth / 2, yStart - 9, -1, false);
        for (int i = 0; i < lineCount; ++i) {
            SidebarLine line = (SidebarLine)lines.get(i);
            int lineY = y - (lineCount - i) * 9;
            context.drawText(textRenderer, line.name(), xLeft, lineY, -1, false);
        }
    }

    private static Text safeText(Text candidate, Text fallback) {
        return candidate != null ? candidate : fallback;
    }

    private record SidebarLine(Text name) {
    }
}

