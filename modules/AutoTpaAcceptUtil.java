package me.Gui.gui.modules;

import java.util.Locale;
import me.Gui.gui.client.sound.SoundUtil;
import me.Gui.gui.modules.TpAcceptSoundsUtil;
import net.minecraft.text.Text;

public final class AutoTpaAcceptUtil {
    private static final String PREFIX = "gracz ";
    private static final String ACCEPT_MARKER = " zaakceptowal twoja prosbe o telepor";

    private AutoTpaAcceptUtil() {
    }

    public static void onChatMessage(Text text) {
        if (text == null) {
            return;
        }
        String raw = text.getString();
        if (raw == null || raw.isBlank()) {
            return;
        }
        if (!TpAcceptSoundsUtil.isEnabled()) {
            return;
        }
        String normalized = AutoTpaAcceptUtil.normalizeAscii(raw).toLowerCase(Locale.ROOT).trim();
        if (AutoTpaAcceptUtil.isAcceptMessage(normalized)) {
            SoundUtil.playAutoTpaAccept();
        }
    }

    private static boolean isAcceptMessage(String normalized) {
        if (normalized == null || normalized.isBlank()) {
            return false;
        }
        if (!normalized.startsWith(PREFIX)) {
            return false;
        }
        if (AutoTpaAcceptUtil.matchesExactPattern(normalized)) {
            return true;
        }
        if (AutoTpaAcceptUtil.containsReject(normalized)) {
            return false;
        }
        if (!AutoTpaAcceptUtil.containsAccept(normalized)) {
            return false;
        }
        return AutoTpaAcceptUtil.containsTeleport(normalized);
    }

    private static boolean matchesExactPattern(String normalized) {
        if (!normalized.startsWith(PREFIX)) {
            return false;
        }
        int marker = normalized.indexOf(ACCEPT_MARKER, PREFIX.length());
        return marker >= 0;
    }

    private static boolean containsAccept(String normalized) {
        return normalized.contains("zaakceptow");
    }

    private static boolean containsTeleport(String normalized) {
        return normalized.contains("telepor") || normalized.contains("tpa");
    }

    private static boolean containsReject(String normalized) {
        return normalized.contains("nie zaakcept") || normalized.contains("niezaakcept") || normalized.contains("odrzu") || normalized.contains("odmow") || normalized.contains("anulow");
    }

    private static String normalizeAscii(String s) {
        if (s == null) {
            return "";
        }
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
}

