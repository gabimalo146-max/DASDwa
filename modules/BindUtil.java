package me.Gui.gui.modules;

public final class BindUtil {
    public static final int MOUSE_BIND_OFFSET = 1000;

    private BindUtil() {
    }

    public static int toMouseBind(int button) {
        return 1000 + Math.max(0, button);
    }

    public static boolean isMouseBind(int key) {
        return key >= 1000;
    }

    public static int mouseButton(int key) {
        return key - 1000;
    }
}

