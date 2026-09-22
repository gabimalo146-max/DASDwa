package me.Gui.gui.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import me.Gui.gui.client.GuiClient;
import me.Gui.gui.client.sound.SoundUtil;
import me.Gui.gui.modules.AimAssistUtil;
import me.Gui.gui.modules.AntiTrapUtil;
import me.Gui.gui.modules.AntyKostkaUtil;
import me.Gui.gui.modules.ArmorEquipperUtil;
import me.Gui.gui.modules.AutoParawanUtil;
import me.Gui.gui.modules.AutoTotemUtil;
import me.Gui.gui.modules.BindUtil;
import me.Gui.gui.modules.ErrorKillerUtil;
import me.Gui.gui.modules.FastLeverUtil;
import me.Gui.gui.modules.FreecamUtil;
import me.Gui.gui.modules.GearRenderUtil;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.LogoutSpots;
import me.Gui.gui.modules.ModuleId;
import me.Gui.gui.modules.NoJumpDelayUtil;
import me.Gui.gui.modules.RefillerUtil;
import me.Gui.gui.modules.SpammerUtil;
import me.Gui.gui.modules.TracersUtil;
import me.Gui.gui.modules.XrayUtil;
import me.Gui.gui.ui.hud.NotificationHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.lwjgl.glfw.GLFW;

public class ModuleManager {
    private final List<HackModule> modules = new ArrayList<HackModule>();
    private final Map<ModuleId, HackModule> byId = new HashMap<ModuleId, HackModule>();
    private final Set<Integer> downKeys = new HashSet<Integer>();
    private static final int GUI_DEFAULT_BIND = 80;
    private boolean fullbrightApplied = false;
    private boolean panicWasEnabled = false;
    private final Map<ModuleId, Boolean> panicSnapshot = new HashMap<ModuleId, Boolean>();
    private boolean panicSnapshotValid = false;

    public ModuleManager() {
        for (ModuleId id : ModuleId.values()) {
            HackModule m = new HackModule(id);
            if (id.forceEnabled || id == ModuleId.ERROR_KILLER) {
                m.setEnabled(true);
            }
            if (id == ModuleId.GUI) {
                m.setBindKey(80);
            }
            this.modules.add(m);
            this.byId.put(id, m);
        }
    }

    public List<HackModule> all() {
        return this.modules;
    }

    public HackModule byId(ModuleId id) {
        return this.byId.get((Object)id);
    }

    private boolean isEnabled(ModuleId id) {
        HackModule m = this.byId(id);
        return m != null && m.isEnabled();
    }

    public void tickBinds(MinecraftClient client) {
        int key;
        if (client == null || client.getWindow() == null) {
            return;
        }
        if (client.inGameHud != null && client.inGameHud.getChatHud().isChatFocused()) {
            this.downKeys.clear();
            return;
        }
        if (client.currentScreen != null) {
            this.downKeys.clear();
            return;
        }
        long handle = client.getWindow().getHandle();
        if (handle == 0L) {
            return;
        }
        HashSet<Integer> pressedNow = new HashSet<Integer>();
        for (HackModule m : this.modules) {
            if (m.id.hidden || m.id == ModuleId.ERROR_KILLER || (key = m.getBindKey()) <= 0) continue;
            if (BindUtil.isMouseBind(key)) {
                int btn = BindUtil.mouseButton(key);
                if (GLFW.glfwGetMouseButton((long)handle, (int)btn) != 1) continue;
                pressedNow.add(key);
                continue;
            }
            if (GLFW.glfwGetKey((long)handle, (int)key) != 1) continue;
            pressedNow.add(key);
        }
        for (HackModule m : this.modules) {
            if (m.id.hidden || m.id == ModuleId.ERROR_KILLER || (key = m.getBindKey()) <= 0 || !pressedNow.contains(key) || this.downKeys.contains(key)) continue;
            if (m.id == ModuleId.GUI) {
                GuiClient.openGui();
                continue;
            }
            if (m.id == ModuleId.ARMOR_EQUIPPER) {
                ArmorEquipperUtil.requestSwap(client);
                continue;
            }
            if (m.id == ModuleId.ANTYKOSTKA) {
                AntyKostkaUtil.triggerManual(client);
                continue;
            }
            this.toggleWithSound(m);
        }
        this.downKeys.clear();
        this.downKeys.addAll(pressedNow);
    }

    public void tick(MinecraftClient client) {
        HackModule fb;
        boolean panicEnabled = this.isPanicEnabled();
        if (panicEnabled) {
            if (!this.panicWasEnabled) {
                this.capturePanicSnapshotIfNeeded();
            }
            if (this.enforcePanicMode()) {
                this.saveActiveConfig();
            }
        } else if (this.panicWasEnabled && this.restoreFromPanicSnapshot()) {
            this.saveActiveConfig();
        }
        this.panicWasEnabled = panicEnabled;
        if (this.isEnabled(ModuleId.LOGOUT_SPOTS)) {
            LogoutSpots.tick(client);
        }
        if (this.isEnabled(ModuleId.TRACERS)) {
            TracersUtil.syncViewBobbing(client);
        }
        if (this.isEnabled(ModuleId.ANTYKOSTKA)) {
            AntyKostkaUtil.tick(client);
        }
        AntiTrapUtil.tick(client);
        if (this.isEnabled(ModuleId.NOJUMP_DELAY)) {
            NoJumpDelayUtil.tick(client);
        }
        if (this.isEnabled(ModuleId.XRAY)) {
            XrayUtil.tick(client);
        }
        if (this.isEnabled(ModuleId.FAST_LEVER)) {
            FastLeverUtil.tick(client);
        }
        if (this.isEnabled(ModuleId.AUTO_TOTEM)) {
            AutoTotemUtil.tick(client);
        }
        if (this.isEnabled(ModuleId.AUTO_PARAWAN)) {
            AutoParawanUtil.tick(client);
        }
        if (this.isEnabled(ModuleId.SPAMMER)) {
            SpammerUtil.tick(client);
        }
        if (this.isEnabled(ModuleId.ARMOR_EQUIPPER)) {
            ArmorEquipperUtil.tick(client);
        }
        FreecamUtil.tick(client);
        if (this.isEnabled(ModuleId.REFILLER)) {
            RefillerUtil.tick(client);
        }
        if (this.isEnabled(ModuleId.ERROR_KILLER)) {
            ErrorKillerUtil.tick(client);
        }
        GearRenderUtil.tick(client);
        AimAssistUtil.tick(client);
        if (client == null || client.player == null) {
            return;
        }
        if (this.isEnabled(ModuleId.NO_BAD_EFFECTS)) {
            this.applyNoBadEffects(client);
        }
        if ((fb = this.byId(ModuleId.FULLBRIGHT)) == null || !fb.isEnabled()) {
            this.clearFullbright(client);
        } else {
            StatusEffectInstance current = client.player.getStatusEffect(StatusEffects.NIGHT_VISION);
            if (current == null || this.isFullbrightEffect(current) || current.getDuration() < 220) {
                client.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 260, 0, true, false, false));
                this.fullbrightApplied = true;
            }
        }
    }

    private void applyNoBadEffects(MinecraftClient client) {
        HackModule mod = this.byId(ModuleId.NO_BAD_EFFECTS);
        if (mod == null || !mod.isEnabled()) {
            return;
        }
        if (client == null || client.player == null) {
            return;
        }
        client.player.removeStatusEffect(StatusEffects.BLINDNESS);
        client.player.removeStatusEffect(StatusEffects.DARKNESS);
        client.player.removeStatusEffect(StatusEffects.NAUSEA);
    }

    private void clearFullbright(MinecraftClient client) {
        if (client.player == null) {
            return;
        }
        StatusEffectInstance current = client.player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (current != null && this.isFullbrightEffect(current)) {
            client.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
        this.fullbrightApplied = false;
    }

    private boolean isFullbrightEffect(StatusEffectInstance effect) {
        if (!this.fullbrightApplied) {
            return false;
        }
        return effect.isAmbient() && !effect.shouldShowParticles() && !effect.shouldShowIcon();
    }

    public void toggleWithSound(HackModule m) {
        if (m == null) {
            return;
        }
        if (m.id == ModuleId.GUI) {
            return;
        }
        if (m.id != ModuleId.PANIC_MODE && this.isPanicEnabled()) {
            return;
        }
        if (m.id.forceEnabled) {
            return;
        }
        m.toggle();
        if (m.id == ModuleId.LOGOUT_SPOTS && !m.isEnabled()) {
            LogoutSpots.clearAll();
        }
        SoundUtil.playToggle(m.isEnabled());
        String name = m.id.display == null ? m.id.name() : m.id.display;
        NotificationHud.post(name, m.isEnabled());
        if (m.id == ModuleId.PANIC_MODE && m.isEnabled()) {
            this.capturePanicSnapshotIfNeeded();
            this.enforcePanicMode();
        }
        this.saveActiveConfig();
    }

    public ModuleState exportState() {
        ModuleState s = new ModuleState();
        for (HackModule m : this.modules) {
            s.enabled.put(m.id.name(), m.isEnabled());
            s.binds.put(m.id.name(), m.getBindKey());
        }
        return s;
    }

    public void importState(ModuleState state) {
        if (state == null) {
            return;
        }
        for (HackModule m : this.modules) {
            Boolean en = state.enabled.get(m.id.name());
            Integer bk = state.binds.get(m.id.name());
            if (m.id.forceEnabled) {
                m.setEnabled(true);
            } else {
                boolean enabled = m.id == ModuleId.ERROR_KILLER && en == null ? m.isEnabled() : en != null && en != false;
                m.setEnabled(enabled);
                if (m.id == ModuleId.LOGOUT_SPOTS && !enabled) {
                    LogoutSpots.clearAll();
                }
            }
            if (m.id == ModuleId.ERROR_KILLER) {
                m.setBindKey(-1);
                continue;
            }
            if (m.id == ModuleId.GUI && bk == null) {
                m.setBindKey(80);
                continue;
            }
            m.setBindKey(bk == null ? -1 : bk);
        }
    }

    private void saveActiveConfig() {
        String active = GuiClient.CONFIG.activeConfig;
        if (active == null || active.isBlank()) {
            active = "default";
        }
        GuiClient.CONFIGS.save(active);
    }

    private boolean isPanicEnabled() {
        HackModule panic = this.byId(ModuleId.PANIC_MODE);
        return panic != null && panic.isEnabled();
    }

    private boolean enforcePanicMode() {
        if (!this.isPanicEnabled()) {
            return false;
        }
        boolean changed = false;
        for (HackModule m : this.modules) {
            if (m.id == ModuleId.PANIC_MODE || !m.isEnabled()) continue;
            m.setEnabled(false);
            if (m.id == ModuleId.LOGOUT_SPOTS) {
                LogoutSpots.clearAll();
            }
            changed = true;
        }
        return changed;
    }

    private void capturePanicSnapshotIfNeeded() {
        if (this.panicSnapshotValid) {
            return;
        }
        this.panicSnapshot.clear();
        for (HackModule m : this.modules) {
            this.panicSnapshot.put(m.id, m.isEnabled());
        }
        this.panicSnapshotValid = true;
    }

    private boolean restoreFromPanicSnapshot() {
        boolean changed = false;
        if (this.panicSnapshotValid) {
            for (HackModule m : this.modules) {
                Boolean prev;
                if (m.id == ModuleId.PANIC_MODE || (prev = this.panicSnapshot.get((Object)m.id)) == null || m.isEnabled() == prev.booleanValue()) continue;
                m.setEnabled(prev);
                if (m.id == ModuleId.LOGOUT_SPOTS && !prev.booleanValue()) {
                    LogoutSpots.clearAll();
                }
                changed = true;
            }
            this.panicSnapshot.clear();
            this.panicSnapshotValid = false;
        }
        for (HackModule m : this.modules) {
            if (!m.id.forceEnabled || m.id == ModuleId.PANIC_MODE || m.isEnabled()) continue;
            m.setEnabled(true);
            changed = true;
        }
        return changed;
    }

    public static class ModuleState {
        public Map<String, Boolean> enabled = new HashMap<String, Boolean>();
        public Map<String, Integer> binds = new HashMap<String, Integer>();
    }
}

