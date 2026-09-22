package me.Gui.gui.modules;

import me.Gui.gui.modules.ModuleId;

public class HackModule {
    public final ModuleId id;
    private boolean enabled = false;
    private int bindKey = -1;

    public HackModule(ModuleId id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
    }

    public void toggle() {
        this.enabled = !this.enabled;
    }

    public int getBindKey() {
        return this.bindKey;
    }

    public void setBindKey(int bindKey) {
        this.bindKey = bindKey;
    }
}

