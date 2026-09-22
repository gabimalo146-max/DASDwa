package me.Gui.gui.modules;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.function.UnaryOperator;
import me.Gui.gui.client.GuiClient;
import me.Gui.gui.config.GuiConfig;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;

public final class NameProtectUtil {
    private static long lastTick = Long.MIN_VALUE;
    private static GuiConfig.NameProtectMode lastMode = null;
    private static int lastListSize = -1;
    private static UUID lastSelfId = null;
    private static List<String> lastTargets = List.of();
    private static final String ANARCHIA_HOST = "anarchia.gg";
    private static String lastServerAddress = null;
    private static boolean lastServerMatch = false;
    private static final String LABEL_TIME = "Czas gry:";
    private static final String LABEL_MONEY = "Pieni\u0105dze:";
    private static final String LABEL_KILLS = "Zab\u00f3jstwa:";
    private static final String LABEL_DEATHS = "\u015amierci:";
    private static final int LINE_KILLS = 3;
    private static final int LINE_DEATHS = 4;
    private static final int LINE_MONEY = 5;
    private static final int LINE_TIME = 6;
    private static final String LABEL_TIME_N = NameProtectUtil.normalizeAscii("Czas gry:").toLowerCase(Locale.ROOT);
    private static final String LABEL_MONEY_N = NameProtectUtil.normalizeAscii("Pieni\u0105dze:").toLowerCase(Locale.ROOT);
    private static final String LABEL_KILLS_N = NameProtectUtil.normalizeAscii("Zab\u00f3jstwa:").toLowerCase(Locale.ROOT);
    private static final String LABEL_DEATHS_N = NameProtectUtil.normalizeAscii("\u015amierci:").toLowerCase(Locale.ROOT);

    private NameProtectUtil() {
    }

    public static boolean isEnabled() {
        HackModule mod = GuiClient.MODULES.byId(ModuleId.NAME_PROTECT);
        return mod != null && mod.isEnabled();
    }

    public static String replaceString(String text) {
        if (text == null || text.isEmpty() || !NameProtectUtil.isEnabled()) {
            return text;
        }
        String alias = NameProtectUtil.getAlias();
        if (alias.isEmpty()) {
            return text;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        List<String> targets = NameProtectUtil.getTargets(client, NameProtectUtil.getMode());
        if (targets.isEmpty()) {
            return text;
        }
        return NameProtectUtil.replaceAll(text, targets, alias);
    }

    public static StringVisitable replaceVisitable(StringVisitable text) {
        String replaced;
        if (text == null || !NameProtectUtil.isEnabled()) {
            return text;
        }
        if (text instanceof Text) {
            Text t = (Text)text;
            return NameProtectUtil.replaceText(t);
        }
        String alias = NameProtectUtil.getAlias();
        if (alias.isEmpty()) {
            return text;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        List<String> targets = NameProtectUtil.getTargets(client, NameProtectUtil.getMode());
        if (targets.isEmpty()) {
            return text;
        }
        String original = text.getString();
        if (original.equals(replaced = NameProtectUtil.replaceAll(original, targets, alias))) {
            return text;
        }
        return StringVisitable.plain((String)replaced);
    }

    public static String getDisplayName(PlayerEntity player) {
        if (player == null || !NameProtectUtil.isEnabled()) {
            return null;
        }
        String alias = NameProtectUtil.getAlias();
        if (alias.isEmpty()) {
            return null;
        }
        GuiConfig.NameProtectMode mode = NameProtectUtil.getMode();
        if (mode == GuiConfig.NameProtectMode.EVERYONE) {
            return alias;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null && player.getUuid().equals(client.player.getUuid())) {
            return alias;
        }
        return null;
    }

    public static Text replaceText(Text text) {
        if (text == null || !NameProtectUtil.isEnabled()) {
            return text;
        }
        String alias = NameProtectUtil.getAlias();
        if (alias.isEmpty()) {
            return text;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        List<String> targets = NameProtectUtil.getTargets(client, NameProtectUtil.getMode());
        if (targets.isEmpty()) {
            return text;
        }
        return NameProtectUtil.replaceTextInternal(text, targets, alias);
    }

    public static Text replaceScoreboardText(Text text, int score) {
        if (text == null) {
            return null;
        }
        if (!NameProtectUtil.isOnAnarchiaServer()) {
            return text;
        }
        Text base = NameProtectUtil.replaceText(text);
        if (NameProtectUtil.shouldUseCustomScoreboard()) {
            return base;
        }
        GuiConfig cfg = GuiClient.CONFIG;
        if (cfg == null || cfg.nameProtectChangeStats == null || !cfg.nameProtectChangeStats.booleanValue()) {
            return base;
        }
        String time = NameProtectUtil.cleanStat(cfg.nameProtectStatTime);
        String money = NameProtectUtil.cleanStat(cfg.nameProtectStatMoney);
        String kills = NameProtectUtil.cleanStat(cfg.nameProtectStatKills);
        String deaths = NameProtectUtil.cleanStat(cfg.nameProtectStatDeaths);
        if (time.isEmpty() && money.isEmpty() && kills.isEmpty() && deaths.isEmpty()) {
            return base;
        }
        return NameProtectUtil.replaceTextBy(base, s -> NameProtectUtil.replaceStatsLine(s, time, money, kills, deaths));
    }

    public static Text replaceScoreboardTextByLine(Text text, int lineFromBottom) {
        if (text == null) {
            return null;
        }
        if (!NameProtectUtil.isOnAnarchiaServer()) {
            return text;
        }
        String rawOriginal = text.getString();
        if (NameProtectUtil.isLikelySeparatorLine(rawOriginal)) {
            return Text.empty();
        }
        Text base = NameProtectUtil.replaceText(text);
        GuiConfig cfg = GuiClient.CONFIG;
        if (cfg == null || cfg.nameProtectChangeStats == null || !cfg.nameProtectChangeStats.booleanValue()) {
            return base;
        }
        String time = NameProtectUtil.cleanStat(cfg.nameProtectStatTime);
        String money = NameProtectUtil.cleanStat(cfg.nameProtectStatMoney);
        String kills = NameProtectUtil.cleanStat(cfg.nameProtectStatKills);
        String deaths = NameProtectUtil.cleanStat(cfg.nameProtectStatDeaths);
        if (time.isEmpty() && money.isEmpty() && kills.isEmpty() && deaths.isEmpty()) {
            return base;
        }
        String value = null;
        String raw = base.getString();
        String normalized = NameProtectUtil.normalizeAscii(raw).toLowerCase(Locale.ROOT);
        if (!time.isEmpty() && normalized.contains(LABEL_TIME_N)) {
            value = time;
        } else if (!money.isEmpty() && normalized.contains(LABEL_MONEY_N)) {
            value = money;
        } else if (!kills.isEmpty() && normalized.contains(LABEL_KILLS_N)) {
            value = kills;
        } else if (!(deaths.isEmpty() && kills.isEmpty() || !normalized.contains(LABEL_DEATHS_N))) {
            value = deaths.isEmpty() ? kills : deaths;
        } else if (NameProtectUtil.shouldUseFallbackLine(raw, normalized) && lineFromBottom == 3) {
            value = kills;
        } else if (NameProtectUtil.shouldUseFallbackLine(raw, normalized) && lineFromBottom == 4) {
            value = deaths.isEmpty() ? kills : deaths;
        } else if (NameProtectUtil.shouldUseFallbackLine(raw, normalized) && lineFromBottom == 5) {
            value = money;
        } else if (NameProtectUtil.shouldUseFallbackLine(raw, normalized) && lineFromBottom == 6) {
            value = time;
        }
        if (value == null || value.isEmpty()) {
            return base;
        }
        return NameProtectUtil.replaceLineValuePreservingStyle(base, value);
    }

    private static boolean isLikelySeparatorLine(String raw) {
        if (raw == null) {
            return false;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return true;
        }
        if (trimmed.length() > 2) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasColon = false;
        for (int i = 0; i < trimmed.length(); ++i) {
            char c = trimmed.charAt(i);
            if (Character.isLetter(c)) {
                hasLetter = true;
            }
            if (c != ':') continue;
            hasColon = true;
        }
        return !hasLetter && !hasColon;
    }

    private static boolean shouldUseFallbackLine(String raw, String normalized) {
        if (raw == null || raw.isBlank()) {
            return false;
        }
        if (normalized.contains(LABEL_TIME_N) || normalized.contains(LABEL_MONEY_N) || normalized.contains(LABEL_KILLS_N) || normalized.contains(LABEL_DEATHS_N)) {
            return false;
        }
        if (raw.indexOf(58) >= 0) {
            return true;
        }
        for (int i = 0; i < normalized.length(); ++i) {
            if (!Character.isLetter(normalized.charAt(i))) continue;
            return true;
        }
        return false;
    }

    public static boolean shouldUseCustomScoreboard() {
        return NameProtectUtil.isOnAnarchiaServer();
    }

    public static boolean isOnAnarchiaServer() {
        String address;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return false;
        }
        ServerInfo info = client.getCurrentServerEntry();
        String string = address = info != null ? info.address : null;
        if (address == null || address.isBlank()) {
            return false;
        }
        if (address.equals(lastServerAddress)) {
            return lastServerMatch;
        }
        lastServerAddress = address;
        lastServerMatch = NameProtectUtil.isAddressMatch(address, ANARCHIA_HOST);
        return lastServerMatch;
    }

    private static boolean isAddressMatch(String address, String host) {
        String normalized = address.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return false;
        }
        String hostPart = normalized;
        if (hostPart.startsWith("[")) {
            int end = hostPart.indexOf(93);
            if (end > 0) {
                hostPart = hostPart.substring(1, end);
            }
        } else {
            int colon = hostPart.indexOf(58);
            if (colon > 0) {
                hostPart = hostPart.substring(0, colon);
            }
        }
        while (hostPart.endsWith(".")) {
            hostPart = hostPart.substring(0, hostPart.length() - 1);
        }
        return hostPart.equals(host) || hostPart.endsWith("." + host);
    }

    public static OrderedText replaceOrderedText(OrderedText text) {
        if (text == null || !NameProtectUtil.isEnabled()) {
            return text;
        }
        String alias = NameProtectUtil.getAlias();
        if (alias.isEmpty()) {
            return text;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        List<String> targets = NameProtectUtil.getTargets(client, NameProtectUtil.getMode());
        if (targets.isEmpty()) {
            return text;
        }
        ArrayList<Integer> codePoints = new ArrayList<Integer>();
        ArrayList<Style> styles = new ArrayList<Style>();
        StringBuilder sb = new StringBuilder();
        text.accept((index, style, codePoint) -> {
            codePoints.add(codePoint);
            styles.add(style);
            sb.appendCodePoint(codePoint);
            return true;
        });
        String original = sb.toString();
        if (original.isEmpty()) {
            return text;
        }
        Style[] stylesByChar = NameProtectUtil.buildStylesByChar(original, codePoints, styles);
        List<StyledChar> out = NameProtectUtil.replaceWithStyles(original, stylesByChar, targets, alias);
        if (out == null) {
            return text;
        }
        return visitor -> {
            int charIndex = 0;
            for (StyledChar sc : out) {
                if (!visitor.accept(charIndex, sc.style, sc.codePoint)) {
                    return false;
                }
                charIndex += Character.charCount(sc.codePoint);
            }
            return true;
        };
    }

    private static Text replaceTextInternal(Text text, List<String> targets, String alias) {
        TextContent content;
        TextContent newContent = content = text.getContent();
        boolean changed = false;
        if (content instanceof PlainTextContent) {
            String replaced;
            PlainTextContent plain = (PlainTextContent)content;
            String original = plain.string();
            if (!original.equals(replaced = NameProtectUtil.replaceAll(original, targets, alias))) {
                newContent = PlainTextContent.of((String)replaced);
                changed = true;
            }
        } else if (content instanceof TranslatableTextContent) {
            TranslatableTextContent translatable = (TranslatableTextContent)content;
            Object[] args = translatable.getArgs();
            Object[] newArgs = null;
            for (int i = 0; i < args.length; ++i) {
                String s;
                Object replaced;
                Object arg;
                Object next = arg = args[i];
                if (arg instanceof Text) {
                    Text t = (Text)arg;
                    replaced = NameProtectUtil.replaceTextInternal(t, targets, alias);
                    if (replaced != t) {
                        next = replaced;
                    }
                } else if (arg instanceof String && !(s = (String)arg).equals(replaced = NameProtectUtil.replaceAll(s, targets, alias))) {
                    next = replaced;
                }
                if (next == arg) continue;
                if (newArgs == null) {
                    newArgs = (Object[])args.clone();
                }
                newArgs[i] = next;
            }
            if (newArgs != null) {
                newContent = new TranslatableTextContent(translatable.getKey(), translatable.getFallback(), newArgs);
                changed = true;
            }
        }
        MutableText out = MutableText.of((TextContent)newContent).setStyle(text.getStyle());
        if (!text.getSiblings().isEmpty()) {
            for (Text sibling : text.getSiblings()) {
                Text replacedSibling = NameProtectUtil.replaceTextInternal(sibling, targets, alias);
                out.append(replacedSibling);
                if (replacedSibling == sibling) continue;
                changed = true;
            }
        }
        if (!changed && newContent == content && text.getSiblings().isEmpty()) {
            return text;
        }
        return out;
    }

    private static Text replaceTextBy(Text text, UnaryOperator<String> replacer) {
        TextContent content;
        TextContent newContent = content = text.getContent();
        boolean changed = false;
        if (content instanceof PlainTextContent) {
            String replaced;
            PlainTextContent plain = (PlainTextContent)content;
            String original = plain.string();
            if (!Objects.equals(original, replaced = (String)replacer.apply(original))) {
                newContent = PlainTextContent.of((String)replaced);
                changed = true;
            }
        } else if (content instanceof TranslatableTextContent) {
            TranslatableTextContent translatable = (TranslatableTextContent)content;
            Object[] args = translatable.getArgs();
            Object[] newArgs = null;
            for (int i = 0; i < args.length; ++i) {
                String s;
                Object replaced;
                Object arg;
                Object next = arg = args[i];
                if (arg instanceof Text) {
                    Text t = (Text)arg;
                    replaced = NameProtectUtil.replaceTextBy(t, replacer);
                    if (replaced != t) {
                        next = replaced;
                    }
                } else if (arg instanceof String && !Objects.equals(s = (String)arg, replaced = (String)replacer.apply(s))) {
                    next = replaced;
                }
                if (next == arg) continue;
                if (newArgs == null) {
                    newArgs = (Object[])args.clone();
                }
                newArgs[i] = next;
            }
            if (newArgs != null) {
                newContent = new TranslatableTextContent(translatable.getKey(), translatable.getFallback(), newArgs);
                changed = true;
            }
        }
        MutableText out = MutableText.of((TextContent)newContent).setStyle(text.getStyle());
        if (!text.getSiblings().isEmpty()) {
            for (Text sibling : text.getSiblings()) {
                Text replacedSibling = NameProtectUtil.replaceTextBy(sibling, replacer);
                out.append(replacedSibling);
                if (replacedSibling == sibling) continue;
                changed = true;
            }
        }
        if (!changed && newContent == content && text.getSiblings().isEmpty()) {
            return text;
        }
        return out;
    }

    private static List<String> getTargets(MinecraftClient client, GuiConfig.NameProtectMode mode) {
        UUID selfId;
        if (client == null) {
            return List.of();
        }
        long tick = client.world != null ? client.world.getTime() : Long.MIN_VALUE;
        int listSize = client.getNetworkHandler() != null ? client.getNetworkHandler().getPlayerList().size() : -1;
        UUID uUID = selfId = client.player != null ? client.player.getUuid() : null;
        if (tick == lastTick && mode == lastMode && listSize == lastListSize && Objects.equals(selfId, lastSelfId)) {
            return lastTargets;
        }
        lastTick = tick;
        lastMode = mode;
        lastListSize = listSize;
        lastSelfId = selfId;
        List<String> built = NameProtectUtil.collectTargets(client, mode);
        lastTargets = built.isEmpty() ? List.of() : built;
        return lastTargets;
    }

    private static List<String> collectTargets(MinecraftClient client, GuiConfig.NameProtectMode mode) {
        String name;
        ArrayList<String> out = new ArrayList<String>();
        if (client == null) {
            return out;
        }
        if (mode == GuiConfig.NameProtectMode.SELF) {
            String name2;
            if (client.player != null && !(name2 = client.player.getName().getString()).isBlank()) {
                out.add(name2);
            }
            return out;
        }
        LinkedHashSet<String> names = new LinkedHashSet<String>();
        if (client.getNetworkHandler() != null) {
            for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
                name = entry.getProfile().getName();
                if (name == null || name.isBlank()) continue;
                names.add(name);
            }
        }
        if (client.world != null) {
            for (PlayerEntity p : client.world.getPlayers()) {
                if (p == null || (name = p.getName().getString()).isBlank()) continue;
                names.add(name);
            }
        }
        out.addAll(names);
        return out;
    }

    private static String getAlias() {
        String name = GuiClient.CONFIG.nameProtectName;
        if (name == null) {
            return "";
        }
        return name.trim();
    }

    private static GuiConfig.NameProtectMode getMode() {
        GuiConfig.NameProtectMode mode = GuiClient.CONFIG.nameProtectMode;
        return mode == null ? GuiConfig.NameProtectMode.SELF : mode;
    }

    private static String replaceAll(String text, List<String> targets, String alias) {
        String out = text;
        for (String target : targets) {
            if (target == null || target.isBlank()) continue;
            out = NameProtectUtil.replaceWholeWord(out, target, alias);
        }
        return out;
    }

    private static String replaceWholeWord(String text, String target, String replacement) {
        if (text == null || text.isEmpty() || target.isEmpty()) {
            return text;
        }
        int idx = 0;
        int last = 0;
        StringBuilder sb = null;
        while ((idx = text.indexOf(target, idx)) >= 0) {
            int start = idx;
            int end = idx + target.length();
            if (NameProtectUtil.isBoundary(text, start - 1) && NameProtectUtil.isBoundary(text, end)) {
                if (sb == null) {
                    sb = new StringBuilder(text.length());
                }
                sb.append(text, last, start);
                sb.append(replacement);
                last = end;
            }
            idx = end;
        }
        if (sb == null) {
            return text;
        }
        sb.append(text, last, text.length());
        return sb.toString();
    }

    private static Text replaceLineValuePreservingStyle(Text text, String value) {
        if (text == null || value == null) {
            return text;
        }
        ArrayList<Integer> codePoints = new ArrayList<Integer>();
        ArrayList<Style> styles = new ArrayList<Style>();
        StringBuilder sb = new StringBuilder();
        text.asOrderedText().accept((index, style, codePoint) -> {
            codePoints.add(codePoint);
            styles.add(style);
            sb.appendCodePoint(codePoint);
            return true;
        });
        String original = sb.toString();
        if (original.isEmpty()) {
            return text;
        }
        int digitIndex = -1;
        for (int i = 0; i < original.length(); ++i) {
            if (!Character.isDigit(original.charAt(i))) continue;
            digitIndex = i;
            break;
        }
        if (digitIndex < 0) {
            return text;
        }
        Style[] stylesByChar = NameProtectUtil.buildStylesByChar(original, codePoints, styles);
        MutableText out = Text.empty();
        int idx = 0;
        while (idx < digitIndex) {
            int runEnd;
            Style style2 = stylesByChar[idx];
            if (style2 == null) {
                style2 = Style.EMPTY;
            }
            for (runEnd = idx + 1; runEnd < digitIndex && Objects.equals(stylesByChar[runEnd], style2); ++runEnd) {
            }
            String run = original.substring(idx, runEnd);
            if (!run.isEmpty()) {
                out.append((Text)Text.literal((String)run).setStyle(style2));
            }
            idx = runEnd;
        }
        Style digitStyle = stylesByChar[digitIndex];
        if (digitStyle == null) {
            digitStyle = Style.EMPTY;
        }
        out.append((Text)Text.literal((String)value).setStyle(digitStyle));
        return out;
    }

    private static String cleanStat(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() > 64) {
            trimmed = trimmed.substring(0, 64);
        }
        return trimmed;
    }

    private static String replaceStatsLine(String input, String time, String money, String kills, String deaths) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String normalized = NameProtectUtil.normalizeAscii(input).toLowerCase(Locale.ROOT);
        if (!time.isEmpty() && normalized.contains(LABEL_TIME_N)) {
            return "Czas gry: " + time;
        }
        if (!money.isEmpty() && normalized.contains(LABEL_MONEY_N)) {
            return "Pieni\u0105dze: " + money;
        }
        if (!kills.isEmpty() && normalized.contains(LABEL_KILLS_N)) {
            return "Zab\u00f3jstwa: " + kills;
        }
        if (!deaths.isEmpty() && normalized.contains(LABEL_DEATHS_N)) {
            return "\u015amierci: " + deaths;
        }
        return input;
    }

    private static String normalizeAscii(String s) {
        StringBuilder out = new StringBuilder(s.length());
        block11: for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            switch (c) {
                case '\u0104': 
                case '\u0105': {
                    out.append('a');
                    continue block11;
                }
                case '\u0106': 
                case '\u0107': {
                    out.append('c');
                    continue block11;
                }
                case '\u0118': 
                case '\u0119': {
                    out.append('e');
                    continue block11;
                }
                case '\u0141': 
                case '\u0142': {
                    out.append('l');
                    continue block11;
                }
                case '\u0143': 
                case '\u0144': {
                    out.append('n');
                    continue block11;
                }
                case '\u00d3': 
                case '\u00f3': {
                    out.append('o');
                    continue block11;
                }
                case '\u015a': 
                case '\u015b': {
                    out.append('s');
                    continue block11;
                }
                case '\u0179': 
                case '\u017a': {
                    out.append('z');
                    continue block11;
                }
                case '\u017b': 
                case '\u017c': {
                    out.append('z');
                    continue block11;
                }
                default: {
                    out.append(c);
                }
            }
        }
        return out.toString();
    }

    private static boolean isBoundary(String text, int index) {
        if (index < 0 || index >= text.length()) {
            return true;
        }
        char c = text.charAt(index);
        return !NameProtectUtil.isNameChar(c);
    }

    private static boolean isNameChar(char c) {
        return c >= '0' && c <= '9' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c == '_';
    }

    private static Style[] buildStylesByChar(String original, List<Integer> codePoints, List<Style> styles) {
        Style[] stylesByChar = new Style[original.length()];
        int charIndex = 0;
        for (int i = 0; i < codePoints.size() && i < styles.size(); ++i) {
            int cp = codePoints.get(i);
            Style style = styles.get(i);
            int len = Character.charCount(cp);
            for (int j = 0; j < len && charIndex + j < stylesByChar.length; ++j) {
                stylesByChar[charIndex + j] = style;
            }
            if ((charIndex += len) >= stylesByChar.length) break;
        }
        return stylesByChar;
    }

    private static List<StyledChar> replaceWithStyles(String original, Style[] stylesByChar, List<String> targets, String alias) {
        int len = original.length();
        int idx = 0;
        boolean changed = false;
        ArrayList<StyledChar> out = new ArrayList<StyledChar>(len);
        while (idx < len) {
            Style style;
            Match match = NameProtectUtil.findNextMatch(original, targets, idx);
            if (match == null) {
                NameProtectUtil.appendSegment(original, stylesByChar, idx, len, out);
                break;
            }
            if (match.start > idx) {
                NameProtectUtil.appendSegment(original, stylesByChar, idx, match.start, out);
            }
            if ((style = stylesByChar[match.start]) == null) {
                style = Style.EMPTY;
            }
            NameProtectUtil.appendAlias(alias, style, out);
            idx = match.start + match.target.length();
            changed = true;
        }
        if (!changed) {
            return null;
        }
        return out;
    }

    private static void appendSegment(String original, Style[] stylesByChar, int start, int end, List<StyledChar> out) {
        int cp;
        for (int i = start; i < end; i += Character.charCount(cp)) {
            cp = original.codePointAt(i);
            Style style = stylesByChar[i];
            if (style == null) {
                style = Style.EMPTY;
            }
            out.add(new StyledChar(cp, style));
        }
    }

    private static void appendAlias(String alias, Style style, List<StyledChar> out) {
        int cp;
        for (int i = 0; i < alias.length(); i += Character.charCount(cp)) {
            cp = alias.codePointAt(i);
            out.add(new StyledChar(cp, style));
        }
    }

    private static Match findNextMatch(String text, List<String> targets, int fromIndex) {
        int bestStart = -1;
        String bestTarget = null;
        int bestLen = -1;
        block0: for (String target : targets) {
            if (target == null || target.isBlank()) continue;
            int idx = text.indexOf(target, fromIndex);
            while (idx >= 0) {
                int end = idx + target.length();
                if (NameProtectUtil.isBoundary(text, idx - 1) && NameProtectUtil.isBoundary(text, end)) {
                    if (bestStart != -1 && idx >= bestStart && (idx != bestStart || target.length() <= bestLen)) continue block0;
                    bestStart = idx;
                    bestTarget = target;
                    bestLen = target.length();
                    continue block0;
                }
                idx = text.indexOf(target, end);
            }
        }
        if (bestStart < 0) {
            return null;
        }
        return new Match(bestStart, bestTarget);
    }

    private static final class Match {
        final int start;
        final String target;

        private Match(int start, String target) {
            this.start = start;
            this.target = target;
        }
    }

    private static final class StyledChar {
        final int codePoint;
        final Style style;

        private StyledChar(int codePoint, Style style) {
            this.codePoint = codePoint;
            this.style = style;
        }
    }
}

