package me.Gui.gui.client.keybind;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.modules.BindUtil;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public final class Keybinds {
    private static KeyBinding OPEN_GUI;
    private static boolean registered;
    private static int lastSyncedKey;

    private Keybinds() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        OPEN_GUI = KeyBindingHelper.registerKeyBinding((KeyBinding)new KeyBinding("key.gui.open", 80, "category.gui"));
        registered = true;
    }

    public static void tick(MinecraftClient client) {
        if (OPEN_GUI == null) {
            return;
        }
        Keybinds.syncGuiBind();
        while (OPEN_GUI.wasPressed()) {
            GuiClient.openGui();
        }
    }

    private static void syncGuiBind() {
        HackModule gui = GuiClient.MODULES.byId(ModuleId.GUI);
        if (gui == null) {
            return;
        }
        int moduleKey = gui.getBindKey();
        int keybindKey = Keybinds.keyBindingToModuleKey();
        if (lastSyncedKey == Integer.MIN_VALUE) {
            lastSyncedKey = moduleKey;
            Keybinds.setKeyBindingFromModule(moduleKey);
            return;
        }
        if (moduleKey == keybindKey) {
            lastSyncedKey = moduleKey;
            return;
        }
        if (moduleKey == lastSyncedKey) {
            gui.setBindKey(keybindKey);
            Keybinds.saveActiveConfig();
            lastSyncedKey = keybindKey;
            return;
        }
        if (keybindKey == lastSyncedKey) {
            Keybinds.setKeyBindingFromModule(moduleKey);
            lastSyncedKey = moduleKey;
            return;
        }
        Keybinds.setKeyBindingFromModule(moduleKey);
        lastSyncedKey = moduleKey;
    }

    private static int keyBindingToModuleKey() {
        if (OPEN_GUI == null || OPEN_GUI.isUnbound()) {
            return -1;
        }
        InputUtil.Key key = InputUtil.fromTranslationKey((String)OPEN_GUI.getBoundKeyTranslationKey());
        if (key == null) {
            return -1;
        }
        InputUtil.Type type = key.getCategory();
        if (type == InputUtil.Type.MOUSE) {
            return BindUtil.toMouseBind(key.getCode());
        }
        if (type == InputUtil.Type.KEYSYM) {
            return key.getCode();
        }
        return -1;
    }

    private static void setKeyBindingFromModule(int key) {
        if (OPEN_GUI == null) {
            return;
        }
        if (key <= 0) {
            OPEN_GUI.setBoundKey(InputUtil.UNKNOWN_KEY);
        } else if (BindUtil.isMouseBind(key)) {
            OPEN_GUI.setBoundKey(InputUtil.Type.MOUSE.createFromCode(BindUtil.mouseButton(key)));
        } else {
            OPEN_GUI.setBoundKey(InputUtil.Type.KEYSYM.createFromCode(key));
        }
        KeyBinding.updateKeysByCode();
    }

    private static void saveActiveConfig() {
        String active = GuiClient.CONFIG.activeConfig;
        if (active == null || active.isBlank()) {
            active = "default";
        }
        GuiClient.CONFIGS.save(active);
    }

    static {
        registered = false;
        lastSyncedKey = Integer.MIN_VALUE;
    }
}

