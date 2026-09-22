package me.Gui.gui.client.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import me.Gui.gui.client.GuiClient;
import me.Gui.gui.modules.BindUtil;
import me.Gui.gui.modules.HackModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class BindCommand {
    private static final String COMMAND_NAME = "bind";
    private static final List<String> KEY_SUGGESTIONS = BindCommand.buildKeySuggestions();
    private static String lastContext = null;
    private static List<String> lastSuggestions = Collections.emptyList();
    private static int lastIndex = 0;

    private BindCommand() {
    }

    public static boolean handleMessage(String chatText, boolean addToHistory) {
        if (chatText == null) {
            return false;
        }
        ParsedCommand parsed = BindCommand.parseBindCommand(chatText);
        if (parsed == null) {
            if (!BindCommand.isBindCommandPrefix(chatText)) {
                return false;
            }
            if (addToHistory) {
                BindCommand.addToHistory(chatText);
            }
            BindCommand.sendClientMessage("Usage: !bind <module> <key>");
            return true;
        }
        if (addToHistory) {
            BindCommand.addToHistory(chatText);
        }
        String keyToken = parsed.keyToken;
        String moduleName = parsed.moduleName;
        HackModule module = BindCommand.findModuleExact(moduleName);
        if (module == null) {
            BindCommand.sendClientMessage("Module not found: " + moduleName);
            return true;
        }
        int key = BindCommand.parseKey(keyToken);
        if (key == Integer.MIN_VALUE) {
            BindCommand.sendClientMessage("Unknown key: " + keyToken);
            return true;
        }
        module.setBindKey(key);
        BindCommand.saveActiveConfig();
        String display = module.id.display == null ? module.id.name() : module.id.display;
        String keyName = BindCommand.formatKey(key);
        BindCommand.sendClientMessage("Succesfully binded " + display + " to " + keyName);
        return true;
    }

    public static boolean handleTabCompletion(TextFieldWidget field, int keyCode, int modifiers) {
        if (field == null) {
            return false;
        }
        if (keyCode != 258) {
            return false;
        }
        String text = field.getText();
        if (text == null) {
            return false;
        }
        int cursor = field.getCursor();
        SuggestionResult res = BindCommand.getSuggestions(text, cursor);
        if (res == null) {
            return false;
        }
        boolean shift = (modifiers & 1) != 0;
        List<String> suggestions = res.suggestions;
        if (suggestions.isEmpty()) {
            lastContext = null;
            lastSuggestions = Collections.emptyList();
            lastIndex = 0;
            return true;
        }
        String contextKey = res.stage.name() + "|" + res.start + "|" + BindCommand.normalize(res.moduleName);
        if (!contextKey.equals(lastContext)) {
            lastContext = contextKey;
            lastSuggestions = suggestions;
            lastIndex = shift ? suggestions.size() - 1 : 0;
        } else {
            int size = lastSuggestions.size();
            if (size == 0) {
                return true;
            }
            lastIndex = shift ? (lastIndex - 1 + size) % size : (lastIndex + 1) % size;
        }
        String pick = lastSuggestions.get(lastIndex);
        String before = text.substring(0, res.start);
        String after = cursor < text.length() ? text.substring(cursor) : "";
        Object insert = pick;
        if (res.stage == Stage.COMMAND) {
            insert = pick + " ";
        } else if (res.stage == Stage.MODULE) {
            insert = pick + " ";
        }
        String newText = before + (String)insert + after;
        field.setText(newText);
        field.setCursor(before.length() + ((String)insert).length(), false);
        return true;
    }

    public static SuggestionResult getSuggestions(String text, int cursor) {
        int moduleStart;
        int cmdEnd;
        int pos;
        String sub;
        int bang;
        if (text == null) {
            return null;
        }
        int len = text.length();
        if (cursor < 0) {
            cursor = 0;
        }
        if (cursor > len) {
            cursor = len;
        }
        if ((bang = BindCommand.firstNonSpaceIndex(sub = text.substring(0, cursor))) < 0 || sub.charAt(bang) != '!') {
            return null;
        }
        for (pos = bang + 1; pos < sub.length() && Character.isWhitespace(sub.charAt(pos)); ++pos) {
        }
        int cmdStart = pos;
        if (cmdStart >= sub.length()) {
            return BindCommand.commandSuggestions(cmdStart, "");
        }
        for (cmdEnd = cmdStart; cmdEnd < sub.length() && !Character.isWhitespace(sub.charAt(cmdEnd)); ++cmdEnd) {
        }
        String cmdPrefix = sub.substring(cmdStart, cmdEnd);
        if (!BindCommand.startsWithIgnoreCase(COMMAND_NAME, cmdPrefix)) {
            return BindCommand.commandSuggestions(cmdStart, cmdPrefix);
        }
        boolean commandComplete = cmdPrefix.equalsIgnoreCase(COMMAND_NAME);
        if (!commandComplete || cmdEnd == sub.length()) {
            return BindCommand.commandSuggestions(cmdStart, cmdPrefix);
        }
        for (moduleStart = cmdEnd; moduleStart < sub.length() && Character.isWhitespace(sub.charAt(moduleStart)); ++moduleStart) {
        }
        if (moduleStart >= sub.length()) {
            return BindCommand.moduleSuggestionsResult(moduleStart, "");
        }
        String moduleAndKey = sub.substring(moduleStart);
        int lastSpace = BindCommand.lastWhitespaceIndex(moduleAndKey);
        if (lastSpace < 0) {
            return BindCommand.moduleSuggestionsResult(moduleStart, moduleAndKey);
        }
        String moduleCandidate = moduleAndKey.substring(0, lastSpace).trim();
        String keyPrefix = moduleAndKey.substring(lastSpace + 1);
        if (!moduleCandidate.isEmpty() && BindCommand.findModuleExact(moduleCandidate) != null) {
            return BindCommand.keySuggestionsResult(moduleStart + lastSpace + 1, keyPrefix, moduleCandidate);
        }
        return BindCommand.moduleSuggestionsResult(moduleStart, moduleAndKey);
    }

    private static List<String> moduleSuggestions(String rawPrefix) {
        String norm = BindCommand.normalize(rawPrefix);
        ArrayList<String> out = new ArrayList<String>();
        for (HackModule m : GuiClient.MODULES.all()) {
            String display;
            if (m.id.hidden) continue;
            String string = display = m.id.display == null ? m.id.name() : m.id.display;
            if (!norm.isEmpty() && !BindCommand.normalize(display).startsWith(norm) && !BindCommand.normalize(m.id.name()).startsWith(norm)) continue;
            out.add(display);
        }
        out.sort(String::compareToIgnoreCase);
        return out;
    }

    private static SuggestionResult commandSuggestions(int start, String prefix) {
        if (!BindCommand.startsWithIgnoreCase(COMMAND_NAME, prefix)) {
            return new SuggestionResult(Stage.COMMAND, start, Collections.emptyList(), "");
        }
        return new SuggestionResult(Stage.COMMAND, start, Collections.singletonList(COMMAND_NAME), "");
    }

    private static SuggestionResult moduleSuggestionsResult(int start, String prefix) {
        List<String> suggestions = BindCommand.moduleSuggestions(prefix);
        return new SuggestionResult(Stage.MODULE, start, suggestions, "");
    }

    private static SuggestionResult keySuggestionsResult(int start, String prefix, String moduleName) {
        List<String> suggestions = BindCommand.keySuggestions(prefix);
        return new SuggestionResult(Stage.KEY, start, suggestions, moduleName);
    }

    private static List<String> keySuggestions(String rawPrefix) {
        String norm = BindCommand.normalize(rawPrefix);
        if (norm.isEmpty()) {
            return KEY_SUGGESTIONS;
        }
        ArrayList<String> out = new ArrayList<String>();
        for (String key : KEY_SUGGESTIONS) {
            if (!BindCommand.normalize(key).startsWith(norm)) continue;
            out.add(key);
        }
        out.sort(String::compareToIgnoreCase);
        return out;
    }

    private static HackModule findModuleExact(String rawName) {
        if (rawName == null) {
            return null;
        }
        String norm = BindCommand.normalize(rawName);
        if (norm.isEmpty()) {
            return null;
        }
        for (HackModule m : GuiClient.MODULES.all()) {
            if (m.id.hidden) continue;
            if (norm.equals(BindCommand.normalize(m.id.name()))) {
                return m;
            }
            String display = m.id.display == null ? m.id.name() : m.id.display;
            if (!norm.equals(BindCommand.normalize(display))) continue;
            return m;
        }
        return null;
    }

    private static ParsedCommand parseBindCommand(String text) {
        int argsStart;
        int pos;
        if (text == null) {
            return null;
        }
        int bang = BindCommand.firstNonSpaceIndex(text);
        if (bang < 0 || text.charAt(bang) != '!') {
            return null;
        }
        for (pos = bang + 1; pos < text.length() && Character.isWhitespace(text.charAt(pos)); ++pos) {
        }
        if (!BindCommand.startsWithIgnoreCase(text, pos, COMMAND_NAME)) {
            return null;
        }
        int cmdEnd = pos + COMMAND_NAME.length();
        if (cmdEnd < text.length() && !Character.isWhitespace(text.charAt(cmdEnd))) {
            return null;
        }
        for (argsStart = cmdEnd; argsStart < text.length() && Character.isWhitespace(text.charAt(argsStart)); ++argsStart) {
        }
        if (argsStart >= text.length()) {
            return null;
        }
        String rest = text.substring(argsStart).trim();
        int lastSpace = rest.lastIndexOf(32);
        if (lastSpace <= 0 || lastSpace == rest.length() - 1) {
            return null;
        }
        String moduleName = rest.substring(0, lastSpace).trim();
        String keyToken = rest.substring(lastSpace + 1).trim();
        if (moduleName.isEmpty() || keyToken.isEmpty()) {
            return null;
        }
        return new ParsedCommand(moduleName, keyToken);
    }

    private static boolean isBindCommandPrefix(String text) {
        int pos;
        if (text == null) {
            return false;
        }
        int bang = BindCommand.firstNonSpaceIndex(text);
        if (bang < 0 || text.charAt(bang) != '!') {
            return false;
        }
        for (pos = bang + 1; pos < text.length() && Character.isWhitespace(text.charAt(pos)); ++pos) {
        }
        if (!BindCommand.startsWithIgnoreCase(text, pos, COMMAND_NAME)) {
            return false;
        }
        int cmdEnd = pos + COMMAND_NAME.length();
        return cmdEnd >= text.length() || Character.isWhitespace(text.charAt(cmdEnd));
    }

    private static int parseKey(String raw) {
        int fn;
        int btn;
        if (raw == null) {
            return Integer.MIN_VALUE;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return Integer.MIN_VALUE;
        }
        String up = s.toUpperCase(Locale.ROOT);
        if (up.equals("NONE") || up.equals("UNBIND")) {
            return -1;
        }
        String cleaned = up.replace("_", "");
        if (cleaned.startsWith("MOUSE")) {
            cleaned = cleaned.substring(5);
        }
        if (cleaned.startsWith("MB")) {
            cleaned = cleaned.substring(2);
        }
        if (cleaned.startsWith("M") && cleaned.length() > 1 && (btn = BindCommand.parseInt(cleaned.substring(1))) >= 1 && btn <= 16) {
            return BindUtil.toMouseBind(btn - 1);
        }
        if (cleaned.startsWith("F") && cleaned.length() > 1 && (fn = BindCommand.parseInt(cleaned.substring(1))) >= 1 && fn <= 25) {
            return 290 + (fn - 1);
        }
        switch (cleaned) {
            case "SPACE": 
            case "SP": {
                return 32;
            }
            case "TAB": {
                return 258;
            }
            case "LSHIFT": 
            case "SHIFT": {
                return 340;
            }
            case "RSHIFT": {
                return 344;
            }
            case "LCTRL": 
            case "CTRL": {
                return 341;
            }
            case "RCTRL": {
                return 345;
            }
            case "LALT": 
            case "ALT": {
                return 342;
            }
            case "RALT": {
                return 346;
            }
            case "ESC": 
            case "ESCAPE": {
                return 256;
            }
            case "ENTER": 
            case "RETURN": {
                return 257;
            }
            case "BACKSPACE": 
            case "BK": 
            case "BACK": {
                return 259;
            }
            case "DEL": 
            case "DELETE": {
                return 261;
            }
            case "INS": 
            case "INSERT": {
                return 260;
            }
            case "HOME": {
                return 268;
            }
            case "END": {
                return 269;
            }
            case "PGUP": 
            case "PAGEUP": {
                return 266;
            }
            case "PGDN": 
            case "PAGEDOWN": {
                return 267;
            }
            case "UP": {
                return 265;
            }
            case "DOWN": {
                return 264;
            }
            case "LEFT": {
                return 263;
            }
            case "RIGHT": {
                return 262;
            }
        }
        if (cleaned.length() == 1) {
            char c = cleaned.charAt(0);
            if (c >= 'A' && c <= 'Z') {
                return 65 + (c - 65);
            }
            if (c >= '0' && c <= '9') {
                return 48 + (c - 48);
            }
        }
        return Integer.MIN_VALUE;
    }

    private static String formatKey(int key) {
        if (key <= 0) {
            return "None";
        }
        if (BindUtil.isMouseBind(key)) {
            int btn = BindUtil.mouseButton(key);
            return "MB" + (btn + 1);
        }
        switch (key) {
            case 290: {
                return "F1";
            }
            case 291: {
                return "F2";
            }
            case 292: {
                return "F3";
            }
            case 293: {
                return "F4";
            }
            case 294: {
                return "F5";
            }
            case 295: {
                return "F6";
            }
            case 296: {
                return "F7";
            }
            case 297: {
                return "F8";
            }
            case 298: {
                return "F9";
            }
            case 299: {
                return "F10";
            }
            case 300: {
                return "F11";
            }
            case 301: {
                return "F12";
            }
            case 302: {
                return "F13";
            }
            case 303: {
                return "F14";
            }
            case 304: {
                return "F15";
            }
            case 305: {
                return "F16";
            }
            case 306: {
                return "F17";
            }
            case 307: {
                return "F18";
            }
            case 308: {
                return "F19";
            }
            case 309: {
                return "F20";
            }
            case 310: {
                return "F21";
            }
            case 311: {
                return "F22";
            }
            case 312: {
                return "F23";
            }
            case 313: {
                return "F24";
            }
            case 314: {
                return "F25";
            }
            case 258: {
                return "TAB";
            }
            case 32: {
                return "SPACE";
            }
            case 340: {
                return "LSHIFT";
            }
            case 344: {
                return "RSHIFT";
            }
            case 341: {
                return "LCTRL";
            }
            case 345: {
                return "RCTRL";
            }
            case 342: {
                return "LALT";
            }
            case 346: {
                return "RALT";
            }
            case 256: {
                return "ESC";
            }
            case 257: {
                return "ENTER";
            }
            case 259: {
                return "BACKSPACE";
            }
            case 261: {
                return "DEL";
            }
            case 260: {
                return "INS";
            }
            case 268: {
                return "HOME";
            }
            case 269: {
                return "END";
            }
            case 266: {
                return "PGUP";
            }
            case 267: {
                return "PGDN";
            }
            case 265: {
                return "UP";
            }
            case 264: {
                return "DOWN";
            }
            case 263: {
                return "LEFT";
            }
            case 262: {
                return "RIGHT";
            }
        }
        String n = GLFW.glfwGetKeyName((int)key, (int)0);
        if (n != null && !n.isBlank()) {
            return n.toUpperCase(Locale.ROOT);
        }
        return "KEY_" + key;
    }

    private static void sendClientMessage(String msg) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }
        client.player.sendMessage((Text)Text.literal((String)msg), false);
    }

    private static void addToHistory(String msg) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.inGameHud == null) {
            return;
        }
        client.inGameHud.getChatHud().addToMessageHistory(msg);
    }

    private static void saveActiveConfig() {
        String active = GuiClient.CONFIG.activeConfig;
        if (active == null || active.isBlank()) {
            active = "default";
        }
        GuiClient.CONFIGS.save(active);
    }

    private static String join(String[] parts, int start, int end) {
        if (parts == null || parts.length == 0) {
            return "";
        }
        if (start < 0) {
            start = 0;
        }
        if (end > parts.length) {
            end = parts.length;
        }
        if (start >= end) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; ++i) {
            if (i > start) {
                sb.append(' ');
            }
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    private static String normalize(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (!Character.isLetterOrDigit(c)) continue;
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    private static boolean startsWithIgnoreCase(String text, int start, String prefix) {
        if (text == null || prefix == null) {
            return false;
        }
        if (start < 0) {
            start = 0;
        }
        if (start + prefix.length() > text.length()) {
            return false;
        }
        return text.regionMatches(true, start, prefix, 0, prefix.length());
    }

    private static boolean startsWithIgnoreCase(String full, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return true;
        }
        if (full == null) {
            return false;
        }
        if (prefix.length() > full.length()) {
            return false;
        }
        return full.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private static int firstNonSpaceIndex(String s) {
        if (s == null) {
            return -1;
        }
        for (int i = 0; i < s.length(); ++i) {
            if (Character.isWhitespace(s.charAt(i))) continue;
            return i;
        }
        return -1;
    }

    private static int lastWhitespaceIndex(String s) {
        if (s == null) {
            return -1;
        }
        for (int i = s.length() - 1; i >= 0; --i) {
            if (!Character.isWhitespace(s.charAt(i))) continue;
            return i;
        }
        return -1;
    }

    private static int parseInt(String s) {
        if (s == null || s.isEmpty()) {
            return -1;
        }
        int v = 0;
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return -1;
            }
            v = v * 10 + (c - 48);
        }
        return v;
    }

    private static List<String> buildKeySuggestions() {
        int i;
        char c;
        ArrayList<String> out = new ArrayList<String>();
        for (c = 'A'; c <= 'Z'; c = (char)(c + '\u0001')) {
            out.add(String.valueOf(c));
        }
        for (c = '0'; c <= '9'; c = (char)(c + '\u0001')) {
            out.add(String.valueOf(c));
        }
        for (i = 1; i <= 12; ++i) {
            out.add("F" + i);
        }
        for (i = 13; i <= 25; ++i) {
            out.add("F" + i);
        }
        Collections.addAll(out, "SPACE", "TAB", "LSHIFT", "RSHIFT", "SHIFT", "LCTRL", "RCTRL", "CTRL", "LALT", "RALT", "ALT", "ESC", "ENTER", "BACKSPACE", "DEL", "INS", "HOME", "END", "PGUP", "PGDN", "UP", "DOWN", "LEFT", "RIGHT");
        for (i = 1; i <= 5; ++i) {
            out.add("MB" + i);
        }
        out.sort(String::compareToIgnoreCase);
        return Collections.unmodifiableList(out);
    }

    private record ParsedCommand(String moduleName, String keyToken) {
    }

    public static final class SuggestionResult {
        public final Stage stage;
        public final int start;
        public final List<String> suggestions;
        public final String moduleName;

        private SuggestionResult(Stage stage, int start, List<String> suggestions, String moduleName) {
            this.stage = stage;
            this.start = start;
            this.suggestions = suggestions;
            this.moduleName = moduleName == null ? "" : moduleName;
        }
    }

    public static enum Stage {
        COMMAND,
        MODULE,
        KEY;

    }
}

