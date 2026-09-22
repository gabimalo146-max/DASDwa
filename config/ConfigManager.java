package me.Gui.gui.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import me.Gui.gui.config.GuiConfig;
import me.Gui.gui.config.JsonUtil;
import me.Gui.gui.friends.FriendManager;
import me.Gui.gui.modules.ModuleManager;
import net.fabricmc.loader.api.FabricLoader;

public class ConfigManager {
    private final GuiConfig config;
    private final ModuleManager modules;
    private final FriendManager friends;
    private final Path baseDir;
    private final Path configsDir;
    private final Path activeFile;
    private final Path friendsDir;
    private final Path friendsFile;

    public ConfigManager(GuiConfig config, ModuleManager modules, FriendManager friends) {
        this.config = config;
        this.modules = modules;
        this.friends = friends;
        this.baseDir = FabricLoader.getInstance().getConfigDir().resolve("gui");
        this.configsDir = this.baseDir.resolve("configs");
        this.activeFile = this.baseDir.resolve("active.txt");
        this.friendsDir = this.baseDir.resolve("friends");
        this.friendsFile = this.friendsDir.resolve("friends.json");
        this.ensureDirs();
    }

    private void ensureDirs() {
        try {
            Files.createDirectories(this.configsDir, new FileAttribute[0]);
            Files.createDirectories(this.friendsDir, new FileAttribute[0]);
        }
        catch (IOException e) {
            ConfigManager.logIo("ConfigManager.ensureDirs", e);
        }
    }

    public List<String> listConfigs() {
        this.ensureDirs();
        ArrayList<String> out = new ArrayList<String>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(this.configsDir, "*.json");){
            for (Path p : ds) {
                String name = p.getFileName().toString();
                if (name.endsWith(".json")) {
                    name = name.substring(0, name.length() - 5);
                }
                out.add(name);
            }
        }
        catch (IOException e) {
            ConfigManager.logIo("ConfigManager.listConfigs", e);
        }
        out.sort(String::compareToIgnoreCase);
        return out;
    }

    public void save(String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        this.ensureDirs();
        Snapshot s = new Snapshot();
        s.config = this.copyConfig(this.config);
        s.modules = this.modules.exportState();
        s.friends = null;
        Path f = this.configsDir.resolve(name + ".json");
        this.writeJson(f, s);
        this.saveFriends();
        this.setActive(name);
    }

    public boolean load(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        this.ensureDirs();
        Path f = this.configsDir.resolve(name + ".json");
        if (!Files.exists(f, new LinkOption[0])) {
            return false;
        }
        Snapshot s = this.readJson(f, Snapshot.class);
        if (s == null) {
            return false;
        }
        this.applyConfig(s.config);
        this.modules.importState(s.modules);
        boolean loadedFriends = this.loadFriends();
        if (!loadedFriends && s.friends != null) {
            this.friends.importState(s.friends);
            this.saveFriends();
        }
        this.setActive(name);
        return true;
    }

    public void delete(String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        Path f = this.configsDir.resolve(name + ".json");
        try {
            Files.deleteIfExists(f);
        }
        catch (IOException e) {
            ConfigManager.logIo("ConfigManager.delete", e);
        }
        if (name.equalsIgnoreCase(this.config.activeConfig)) {
            this.setActive("default");
            this.save("default");
        }
    }

    public void loadActiveOrDefault() {
        Path def;
        this.ensureDirs();
        String active = this.readActive();
        if (active == null || active.isBlank()) {
            active = "default";
        }
        if (!Files.exists(def = this.configsDir.resolve("default.json"), new LinkOption[0])) {
            this.save("default");
        }
        if (!this.load(active)) {
            this.load("default");
        }
    }

    private void saveFriends() {
        this.ensureDirs();
        FriendManager.FriendState state = this.friends.exportState();
        this.writeJson(this.friendsFile, state);
    }

    private boolean loadFriends() {
        this.ensureDirs();
        if (!Files.exists(this.friendsFile, new LinkOption[0])) {
            return false;
        }
        FriendManager.FriendState state = this.readJson(this.friendsFile, FriendManager.FriendState.class);
        if (state == null) {
            return false;
        }
        this.friends.importState(state);
        return true;
    }

    private void setActive(String name) {
        this.config.activeConfig = name;
        try {
            Files.createDirectories(this.baseDir, new FileAttribute[0]);
            Files.writeString(this.activeFile, (CharSequence)name, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (IOException e) {
            ConfigManager.logIo("ConfigManager.setActive", e);
        }
    }

    private String readActive() {
        try {
            if (!Files.exists(this.activeFile, new LinkOption[0])) {
                return null;
            }
            return Files.readString(this.activeFile, StandardCharsets.UTF_8).trim();
        }
        catch (IOException e) {
            ConfigManager.logIo("ConfigManager.readActive", e);
            return null;
        }
    }

    private void writeJson(Path file, Object obj) {
        try {
            String json = JsonUtil.GSON.toJson(obj);
            Files.writeString(file, (CharSequence)json, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (IOException e) {
            ConfigManager.logIo("ConfigManager.writeJson", e);
        }
    }

    private <T> T readJson(Path file, Class<T> cls) {
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            return (T)JsonUtil.GSON.fromJson(json, cls);
        }
        catch (Exception e) {
            ConfigManager.logIo("ConfigManager.readJson", e);
            return null;
        }
    }

    private static void logIo(String where, Exception e) {
        System.err.println("[Gui] " + where + " failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
    }

    private GuiConfig copyConfig(GuiConfig src) {
        String json = JsonUtil.GSON.toJson((Object)src);
        return (GuiConfig)JsonUtil.GSON.fromJson(json, GuiConfig.class);
    }

    private void applyConfig(GuiConfig loaded) {
        float kScale;
        float fcX;
        float bhx;
        float aaRange;
        float allScale;
        String npd;
        String npk;
        String npm;
        String npt;
        String np;
        int sDelay;
        String sm;
        int ekDelay;
        boolean allZero;
        if (loaded == null) {
            return;
        }
        this.config.guiScale = ConfigManager.clamp(loaded.guiScale, 0.75f, 1.5f);
        this.config.transparency = ConfigManager.clamp(loaded.transparency, 0.18f, 0.6f);
        this.config.uiVolume = ConfigManager.clamp(loaded.uiVolume, 0.0f, 1.0f);
        Float tpaVol = loaded.tpaAcceptSoundVolume;
        if (tpaVol == null || Float.isNaN(tpaVol.floatValue()) || Float.isInfinite(tpaVol.floatValue())) {
            tpaVol = Float.valueOf(1.0f);
        }
        this.config.tpaAcceptSoundVolume = Float.valueOf(ConfigManager.clamp(tpaVol.floatValue(), 0.0f, 1.0f));
        this.config.showKeybindsHud = loaded.showKeybindsHud;
        if (this.config.showKeybindsHud == null) {
            this.config.showKeybindsHud = true;
        }
        this.config.keybindsOnlyEnabled = loaded.keybindsOnlyEnabled;
        this.config.showCoordinatesHud = loaded.showCoordinatesHud;
        if (this.config.showCoordinatesHud == null) {
            this.config.showCoordinatesHud = false;
        }
        this.config.showArmorHud = loaded.showArmorHud;
        if (this.config.showArmorHud == null) {
            this.config.showArmorHud = false;
        }
        this.config.armorHudDurabilityPercent = loaded.armorHudDurabilityPercent;
        if (this.config.armorHudDurabilityPercent == null) {
            this.config.armorHudDurabilityPercent = true;
        }
        this.config.armorHudVertical = loaded.armorHudVertical;
        if (this.config.armorHudVertical == null) {
            this.config.armorHudVertical = false;
        }
        this.config.showPotionsHud = loaded.showPotionsHud;
        if (this.config.showPotionsHud == null) {
            this.config.showPotionsHud = false;
        }
        this.config.showCooldownsHud = loaded.showCooldownsHud;
        if (this.config.showCooldownsHud == null) {
            this.config.showCooldownsHud = false;
        }
        this.config.showInventoryHud = loaded.showInventoryHud;
        if (this.config.showInventoryHud == null) {
            this.config.showInventoryHud = false;
        }
        this.config.showPingHud = loaded.showPingHud;
        if (this.config.showPingHud == null) {
            this.config.showPingHud = false;
        }
        this.config.showFpsHud = loaded.showFpsHud;
        if (this.config.showFpsHud == null) {
            this.config.showFpsHud = false;
        }
        this.config.keybindsUseGuiColor = loaded.keybindsUseGuiColor;
        if (this.config.keybindsUseGuiColor == null) {
            this.config.keybindsUseGuiColor = false;
        }
        this.config.arrayListUseGuiColor = loaded.arrayListUseGuiColor;
        if (this.config.arrayListUseGuiColor == null) {
            this.config.arrayListUseGuiColor = false;
        }
        this.config.notificationsUseGuiColor = loaded.notificationsUseGuiColor;
        if (this.config.notificationsUseGuiColor == null) {
            this.config.notificationsUseGuiColor = false;
        }
        this.config.coordinatesUseGuiColor = loaded.coordinatesUseGuiColor;
        if (this.config.coordinatesUseGuiColor == null) {
            this.config.coordinatesUseGuiColor = false;
        }
        this.config.armorUseGuiColor = loaded.armorUseGuiColor;
        if (this.config.armorUseGuiColor == null) {
            this.config.armorUseGuiColor = false;
        }
        this.config.potionsUseGuiColor = loaded.potionsUseGuiColor;
        if (this.config.potionsUseGuiColor == null) {
            this.config.potionsUseGuiColor = false;
        }
        this.config.cooldownsUseGuiColor = loaded.cooldownsUseGuiColor;
        if (this.config.cooldownsUseGuiColor == null) {
            this.config.cooldownsUseGuiColor = false;
        }
        this.config.inventoryUseGuiColor = loaded.inventoryUseGuiColor;
        if (this.config.inventoryUseGuiColor == null) {
            this.config.inventoryUseGuiColor = false;
        }
        this.config.pingUseGuiColor = loaded.pingUseGuiColor;
        if (this.config.pingUseGuiColor == null) {
            this.config.pingUseGuiColor = false;
        }
        this.config.fpsUseGuiColor = loaded.fpsUseGuiColor;
        if (this.config.fpsUseGuiColor == null) {
            this.config.fpsUseGuiColor = false;
        }
        Float kbAlpha = loaded.keybindsHudAlpha;
        Float notifAlpha = loaded.notificationsHudAlpha;
        Float coordAlpha = loaded.coordinatesHudAlpha;
        Float armorAlpha = loaded.armorHudAlpha;
        Float potAlpha = loaded.potionsHudAlpha;
        Float cdAlpha = loaded.cooldownsHudAlpha;
        Float invAlpha = loaded.inventoryHudAlpha;
        Float pingAlpha = loaded.pingHudAlpha;
        Float fpsAlpha = loaded.fpsHudAlpha;
        boolean bl = allZero = ConfigManager.isAlphaUnset(kbAlpha) && ConfigManager.isAlphaUnset(notifAlpha) && ConfigManager.isAlphaUnset(coordAlpha) && ConfigManager.isAlphaUnset(armorAlpha) && ConfigManager.isAlphaUnset(potAlpha) && ConfigManager.isAlphaUnset(cdAlpha) && ConfigManager.isAlphaUnset(invAlpha) && ConfigManager.isAlphaUnset(pingAlpha) && ConfigManager.isAlphaUnset(fpsAlpha);
        if (kbAlpha == null || Float.isNaN(kbAlpha.floatValue()) || Float.isInfinite(kbAlpha.floatValue())) {
            kbAlpha = Float.valueOf(1.0f);
        }
        if (notifAlpha == null || Float.isNaN(notifAlpha.floatValue()) || Float.isInfinite(notifAlpha.floatValue())) {
            notifAlpha = Float.valueOf(1.0f);
        }
        if (coordAlpha == null || Float.isNaN(coordAlpha.floatValue()) || Float.isInfinite(coordAlpha.floatValue())) {
            coordAlpha = Float.valueOf(1.0f);
        }
        if (armorAlpha == null || Float.isNaN(armorAlpha.floatValue()) || Float.isInfinite(armorAlpha.floatValue())) {
            armorAlpha = Float.valueOf(1.0f);
        }
        if (potAlpha == null || Float.isNaN(potAlpha.floatValue()) || Float.isInfinite(potAlpha.floatValue())) {
            potAlpha = Float.valueOf(1.0f);
        }
        if (cdAlpha == null || Float.isNaN(cdAlpha.floatValue()) || Float.isInfinite(cdAlpha.floatValue())) {
            cdAlpha = Float.valueOf(1.0f);
        }
        if (invAlpha == null || Float.isNaN(invAlpha.floatValue()) || Float.isInfinite(invAlpha.floatValue())) {
            invAlpha = Float.valueOf(1.0f);
        }
        if (pingAlpha == null || Float.isNaN(pingAlpha.floatValue()) || Float.isInfinite(pingAlpha.floatValue())) {
            pingAlpha = Float.valueOf(1.0f);
        }
        if (fpsAlpha == null || Float.isNaN(fpsAlpha.floatValue()) || Float.isInfinite(fpsAlpha.floatValue())) {
            fpsAlpha = Float.valueOf(1.0f);
        }
        if (allZero) {
            kbAlpha = Float.valueOf(1.0f);
            notifAlpha = Float.valueOf(1.0f);
            coordAlpha = Float.valueOf(1.0f);
            armorAlpha = Float.valueOf(1.0f);
            potAlpha = Float.valueOf(1.0f);
            cdAlpha = Float.valueOf(1.0f);
            invAlpha = Float.valueOf(1.0f);
            pingAlpha = Float.valueOf(1.0f);
            fpsAlpha = Float.valueOf(1.0f);
        }
        this.config.keybindsHudAlpha = Float.valueOf(ConfigManager.clamp(kbAlpha.floatValue(), 0.0f, 1.0f));
        this.config.notificationsHudAlpha = Float.valueOf(ConfigManager.clamp(notifAlpha.floatValue(), 0.0f, 1.0f));
        this.config.coordinatesHudAlpha = Float.valueOf(ConfigManager.clamp(coordAlpha.floatValue(), 0.0f, 1.0f));
        this.config.armorHudAlpha = Float.valueOf(ConfigManager.clamp(armorAlpha.floatValue(), 0.0f, 1.0f));
        this.config.potionsHudAlpha = Float.valueOf(ConfigManager.clamp(potAlpha.floatValue(), 0.0f, 1.0f));
        this.config.cooldownsHudAlpha = Float.valueOf(ConfigManager.clamp(cdAlpha.floatValue(), 0.0f, 1.0f));
        this.config.inventoryHudAlpha = Float.valueOf(ConfigManager.clamp(invAlpha.floatValue(), 0.0f, 1.0f));
        this.config.pingHudAlpha = Float.valueOf(ConfigManager.clamp(pingAlpha.floatValue(), 0.0f, 1.0f));
        this.config.fpsHudAlpha = Float.valueOf(ConfigManager.clamp(fpsAlpha.floatValue(), 0.0f, 1.0f));
        float fb = loaded.fullbrightStrength;
        if (Float.isNaN(fb) || Float.isInfinite(fb)) {
            fb = 0.5f;
        }
        this.config.fullbrightStrength = ConfigManager.clamp(fb, 0.0f, 1.0f);
        boolean pearlTraj = loaded.pearlShowTrajectory == null ? true : loaded.pearlShowTrajectory;
        boolean pearlLanding = loaded.pearlShowLanding == null ? true : loaded.pearlShowLanding;
        boolean pearlNick = loaded.pearlShowNickname == null ? true : loaded.pearlShowNickname;
        boolean pearlCount = loaded.pearlShowCountdown == null ? true : loaded.pearlShowCountdown;
        boolean pearlOwn = loaded.pearlShowOwn == null ? true : loaded.pearlShowOwn;
        float pearlWidth = loaded.pearlLineWidth;
        if (Float.isNaN(pearlWidth) || Float.isInfinite(pearlWidth)) {
            pearlWidth = 2.0f;
        }
        if (!(pearlTraj || pearlLanding || pearlNick || pearlCount)) {
            pearlTraj = true;
            pearlLanding = true;
            pearlNick = true;
            pearlCount = true;
        }
        this.config.pearlShowTrajectory = pearlTraj;
        this.config.pearlShowLanding = pearlLanding;
        this.config.pearlShowNickname = pearlNick;
        this.config.pearlShowCountdown = pearlCount;
        this.config.pearlShowOwn = pearlOwn;
        this.config.pearlLineWidth = ConfigManager.clamp(pearlWidth, 1.0f, 6.0f);
        this.config.logoutShowNick = loaded.logoutShowNick;
        if (this.config.logoutShowNick == null) {
            this.config.logoutShowNick = true;
        }
        this.config.logoutShowTime = loaded.logoutShowTime;
        if (this.config.logoutShowTime == null) {
            this.config.logoutShowTime = true;
        }
        if ((ekDelay = loaded.errorKillerDelayMs) < 1 || ekDelay > 100) {
            ekDelay = 1;
        }
        this.config.errorKillerDelayMs = ekDelay;
        int ekObBind = loaded.errorKillerBindObsidian;
        if (ekObBind <= 0) {
            ekObBind = -1;
        }
        this.config.errorKillerBindObsidian = ekObBind;
        int ekObDelay = loaded.errorKillerDelayMsObsidian;
        if (ekObDelay < 1 || ekObDelay > 100) {
            ekObDelay = ekDelay;
        }
        this.config.errorKillerDelayMsObsidian = ekObDelay;
        this.config.errorKillerShowMissing = loaded.errorKillerShowMissing;
        if (this.config.errorKillerShowMissing == null) {
            this.config.errorKillerShowMissing = false;
        }
        this.config.errorKillerShowMissingObsidian = loaded.errorKillerShowMissingObsidian;
        if (this.config.errorKillerShowMissingObsidian == null) {
            this.config.errorKillerShowMissingObsidian = false;
        }
        this.config.errorKillerHudX = ConfigManager.clampInt(loaded.errorKillerHudX, 0, 10000);
        this.config.errorKillerHudY = ConfigManager.clampInt(loaded.errorKillerHudY, 0, 10000);
        String string = sm = loaded.spammerMessage == null ? "" : loaded.spammerMessage.trim();
        if (sm.length() > 200) {
            sm = sm.substring(0, 200);
        }
        this.config.spammerMessage = sm;
        this.config.spammerAntiSpam = loaded.spammerAntiSpam;
        if (this.config.spammerAntiSpam == null) {
            this.config.spammerAntiSpam = false;
        }
        if ((sDelay = loaded.spammerDelaySec) < 1 || sDelay > 60) {
            sDelay = 3;
        }
        this.config.spammerDelaySec = sDelay;
        this.config.nameProtectMode = loaded.nameProtectMode == null ? GuiConfig.NameProtectMode.SELF : loaded.nameProtectMode;
        String string2 = np = loaded.nameProtectName == null ? "" : loaded.nameProtectName.trim();
        if (np.length() > 32) {
            np = np.substring(0, 32);
        }
        this.config.nameProtectName = np;
        this.config.nameProtectChangeStats = loaded.nameProtectChangeStats;
        if (this.config.nameProtectChangeStats == null) {
            this.config.nameProtectChangeStats = false;
        }
        String string3 = npt = loaded.nameProtectStatTime == null ? "" : loaded.nameProtectStatTime.trim();
        if (npt.length() > 64) {
            npt = npt.substring(0, 64);
        }
        this.config.nameProtectStatTime = npt;
        String string4 = npm = loaded.nameProtectStatMoney == null ? "" : loaded.nameProtectStatMoney.trim();
        if (npm.length() > 64) {
            npm = npm.substring(0, 64);
        }
        this.config.nameProtectStatMoney = npm;
        String string5 = npk = loaded.nameProtectStatKills == null ? "" : loaded.nameProtectStatKills.trim();
        if (npk.length() > 64) {
            npk = npk.substring(0, 64);
        }
        this.config.nameProtectStatKills = npk;
        String string6 = npd = loaded.nameProtectStatDeaths == null ? "" : loaded.nameProtectStatDeaths.trim();
        if (npd.length() > 64) {
            npd = npd.substring(0, 64);
        }
        this.config.nameProtectStatDeaths = npd;
        this.config.tracerPlayers = loaded.tracerPlayers;
        if (this.config.tracerPlayers == null) {
            this.config.tracerPlayers = true;
        }
        this.config.tracerMobs = loaded.tracerMobs;
        if (this.config.tracerMobs == null) {
            this.config.tracerMobs = true;
        }
        this.config.tracerFriends = loaded.tracerFriends;
        if (this.config.tracerFriends == null) {
            this.config.tracerFriends = true;
        }
        this.config.friendsChangeNormalNametag = loaded.friendsChangeNormalNametag;
        if (this.config.friendsChangeNormalNametag == null) {
            this.config.friendsChangeNormalNametag = true;
        }
        this.config.espBox = loaded.espBox;
        if (this.config.espBox == null) {
            this.config.espBox = true;
        }
        this.config.espPlayerColor = ConfigManager.clampInt(loaded.espPlayerColor, 0, 0xFFFFFF);
        this.config.espFriendColor = ConfigManager.clampInt(loaded.espFriendColor, 0, 0xFFFFFF);
        float espLine = loaded.espLineWidth;
        if (Float.isNaN(espLine) || Float.isInfinite(espLine)) {
            espLine = 4.0f;
        }
        this.config.espLineWidth = ConfigManager.clamp(espLine, 1.0f, 6.0f);
        this.config.tracerPlayerHue = ConfigManager.clamp01Inclusive(loaded.tracerPlayerHue);
        this.config.tracerMobHue = ConfigManager.clamp01Inclusive(loaded.tracerMobHue);
        this.config.tracerFriendHue = ConfigManager.clamp01Inclusive(loaded.tracerFriendHue);
        float tracerDist = loaded.tracerDistance;
        if (Float.isNaN(tracerDist) || Float.isInfinite(tracerDist)) {
            tracerDist = 64.0f;
        }
        this.config.tracerDistance = ConfigManager.clamp(tracerDist, 8.0f, 256.0f);
        this.config.swordInfoShowName = loaded.swordInfoShowName;
        if (this.config.swordInfoShowName == null) {
            this.config.swordInfoShowName = true;
        }
        this.config.swordInfoShowEnchant = loaded.swordInfoShowEnchant;
        if (this.config.swordInfoShowEnchant == null) {
            this.config.swordInfoShowEnchant = true;
        }
        this.config.swordInfoShowExtra = loaded.swordInfoShowExtra;
        if (this.config.swordInfoShowExtra == null) {
            this.config.swordInfoShowExtra = true;
        }
        this.config.swordInfoFrame = loaded.swordInfoFrame;
        if (this.config.swordInfoFrame == null) {
            this.config.swordInfoFrame = true;
        }
        this.config.swordInfoExtraColor = loaded.swordInfoExtraColor == null ? Integer.valueOf(5611775) : Integer.valueOf(ConfigManager.clampInt(loaded.swordInfoExtraColor, 0, 0xFFFFFF));
        float siScale = loaded.swordInfoScale;
        if (Float.isNaN(siScale) || Float.isInfinite(siScale)) {
            siScale = 0.9f;
        }
        this.config.swordInfoScale = ConfigManager.clamp(siScale, 0.6f, 1.4f);
        this.config.swordInfoX = ConfigManager.clampInt(loaded.swordInfoX, -1, 10000);
        this.config.swordInfoY = ConfigManager.clampInt(loaded.swordInfoY, -1, 10000);
        this.config.swordInfoShowAllPlayers = loaded.swordInfoShowAllPlayers;
        if (this.config.swordInfoShowAllPlayers == null) {
            this.config.swordInfoShowAllPlayers = false;
        }
        if (Float.isNaN(allScale = loaded.swordInfoAllScale) || Float.isInfinite(allScale)) {
            allScale = 0.8f;
        }
        this.config.swordInfoAllScale = ConfigManager.clamp(allScale, 0.6f, 1.2f);
        this.config.swordInfoAllX = ConfigManager.clampInt(loaded.swordInfoAllX, -1, 10000);
        this.config.swordInfoAllY = ConfigManager.clampInt(loaded.swordInfoAllY, -1, 10000);
        float cdScale = loaded.cooldownListScale;
        if (Float.isNaN(cdScale) || Float.isInfinite(cdScale)) {
            cdScale = 0.9f;
        }
        this.config.cooldownListScale = ConfigManager.clamp(cdScale, 0.6f, 1.4f);
        this.config.cooldownListX = ConfigManager.clampInt(loaded.cooldownListX, 0, 10000);
        this.config.cooldownListY = ConfigManager.clampInt(loaded.cooldownListY, 0, 10000);
        float coordScale = loaded.coordinatesScale;
        if (Float.isNaN(coordScale) || Float.isInfinite(coordScale)) {
            coordScale = 0.9f;
        }
        this.config.coordinatesScale = ConfigManager.clamp(coordScale, 0.5f, 1.4f);
        this.config.coordinatesX = ConfigManager.clampInt(loaded.coordinatesX, 0, 10000);
        this.config.coordinatesY = ConfigManager.clampInt(loaded.coordinatesY, 0, 10000);
        float armorScale = loaded.armorHudScale;
        if (Float.isNaN(armorScale) || Float.isInfinite(armorScale)) {
            armorScale = 0.9f;
        }
        this.config.armorHudScale = ConfigManager.clamp(armorScale, 0.5f, 1.4f);
        this.config.armorHudX = ConfigManager.clampInt(loaded.armorHudX, 0, 10000);
        this.config.armorHudY = ConfigManager.clampInt(loaded.armorHudY, 0, 10000);
        float potScale = loaded.potionsHudScale;
        if (Float.isNaN(potScale) || Float.isInfinite(potScale)) {
            potScale = 0.9f;
        }
        this.config.potionsHudScale = ConfigManager.clamp(potScale, 0.5f, 1.4f);
        this.config.potionsHudX = ConfigManager.clampInt(loaded.potionsHudX, 0, 10000);
        this.config.potionsHudY = ConfigManager.clampInt(loaded.potionsHudY, 0, 10000);
        float invScale = loaded.inventoryHudScale;
        if (Float.isNaN(invScale) || Float.isInfinite(invScale)) {
            invScale = 0.9f;
        }
        this.config.inventoryHudScale = ConfigManager.clamp(invScale, 0.5f, 1.4f);
        this.config.inventoryHudX = ConfigManager.clampInt(loaded.inventoryHudX, 0, 10000);
        this.config.inventoryHudY = ConfigManager.clampInt(loaded.inventoryHudY, 0, 10000);
        float pingScale = loaded.pingHudScale;
        if (Float.isNaN(pingScale) || Float.isInfinite(pingScale)) {
            pingScale = 0.9f;
        }
        this.config.pingHudScale = ConfigManager.clamp(pingScale, 0.5f, 1.4f);
        this.config.pingHudX = ConfigManager.clampInt(loaded.pingHudX, 0, 10000);
        this.config.pingHudY = ConfigManager.clampInt(loaded.pingHudY, 0, 10000);
        float fpsScale = loaded.fpsHudScale;
        if (Float.isNaN(fpsScale) || Float.isInfinite(fpsScale)) {
            fpsScale = 0.9f;
        }
        this.config.fpsHudScale = ConfigManager.clamp(fpsScale, 0.5f, 1.4f);
        this.config.fpsHudX = ConfigManager.clampInt(loaded.fpsHudX, 0, 10000);
        this.config.fpsHudY = ConfigManager.clampInt(loaded.fpsHudY, 0, 10000);
        this.config.blockEspIds = new ArrayList<String>();
        if (loaded.blockEspIds != null) {
            for (String id : loaded.blockEspIds) {
                if (id == null || id.isBlank()) continue;
                this.config.blockEspIds.add(id.trim());
            }
        }
        this.config.itemEspIds = new ArrayList<String>();
        if (loaded.itemEspIds != null) {
            for (String id : loaded.itemEspIds) {
                if (id == null || id.isBlank()) continue;
                this.config.itemEspIds.add(id.trim());
            }
        }
        this.config.itemEspAddAll = loaded.itemEspAddAll;
        if (this.config.itemEspAddAll == null) {
            this.config.itemEspAddAll = false;
        }
        this.config.itemEspMode = GuiConfig.ItemEspMode.SQUARE;
        this.config.itemEspOverlay = false;
        this.config.xrayIds = new ArrayList<String>();
        if (loaded.xrayIds != null) {
            for (String id : loaded.xrayIds) {
                if (id == null || id.isBlank()) continue;
                this.config.xrayIds.add(id.trim());
            }
        }
        this.config.antiTrapItemFrame = loaded.antiTrapItemFrame;
        if (this.config.antiTrapItemFrame == null) {
            this.config.antiTrapItemFrame = true;
        }
        this.config.antiTrapItemFrameAutoBreak = loaded.antiTrapItemFrameAutoBreak;
        if (this.config.antiTrapItemFrameAutoBreak == null) {
            this.config.antiTrapItemFrameAutoBreak = true;
        }
        this.config.antiTrapItemFrameVisual = loaded.antiTrapItemFrameVisual;
        if (this.config.antiTrapItemFrameVisual == null) {
            this.config.antiTrapItemFrameVisual = true;
        }
        this.config.antiTrapPainting = loaded.antiTrapPainting;
        if (this.config.antiTrapPainting == null) {
            this.config.antiTrapPainting = true;
        }
        this.config.antiTrapPaintingAutoBreak = loaded.antiTrapPaintingAutoBreak;
        if (this.config.antiTrapPaintingAutoBreak == null) {
            this.config.antiTrapPaintingAutoBreak = true;
        }
        this.config.antiTrapPaintingVisual = loaded.antiTrapPaintingVisual;
        if (this.config.antiTrapPaintingVisual == null) {
            this.config.antiTrapPaintingVisual = true;
        }
        this.config.antiTrapMinecart = loaded.antiTrapMinecart;
        if (this.config.antiTrapMinecart == null) {
            this.config.antiTrapMinecart = true;
        }
        this.config.antiTrapArmorStand = loaded.antiTrapArmorStand;
        if (this.config.antiTrapArmorStand == null) {
            this.config.antiTrapArmorStand = true;
        }
        this.config.antiTrapMobs = loaded.antiTrapMobs;
        if (this.config.antiTrapMobs == null) {
            this.config.antiTrapMobs = false;
        }
        this.config.autoParawanDontAttackFriends = loaded.autoParawanDontAttackFriends;
        if (this.config.autoParawanDontAttackFriends == null) {
            this.config.autoParawanDontAttackFriends = true;
        }
        if (Float.isNaN(aaRange = loaded.aimAssistRange) || Float.isInfinite(aaRange)) {
            aaRange = 6.0f;
        }
        this.config.aimAssistRange = ConfigManager.clamp(aaRange, 2.0f, 10.0f);
        this.config.aimAssistTargetLock = loaded.aimAssistTargetLock;
        if (this.config.aimAssistTargetLock == null) {
            this.config.aimAssistTargetLock = false;
        }
        this.config.aimAssistMobs = loaded.aimAssistMobs;
        if (this.config.aimAssistMobs == null) {
            this.config.aimAssistMobs = false;
        }
        if (Float.isNaN(bhx = loaded.betterHitboxSizeX) || Float.isInfinite(bhx)) {
            bhx = 100.0f;
        }
        if (bhx <= 0.0f) {
            bhx = 100.0f;
        }
        if (bhx <= 3.5f) {
            bhx *= 100.0f;
        }
        this.config.betterHitboxSizeX = ConfigManager.clamp(bhx, 100.0f, 300.0f);
        float bhy = loaded.betterHitboxSizeY;
        if (Float.isNaN(bhy) || Float.isInfinite(bhy)) {
            bhy = 100.0f;
        }
        if (bhy <= 0.0f) {
            bhy = 100.0f;
        }
        if (bhy <= 3.5f) {
            bhy *= 100.0f;
        }
        this.config.betterHitboxSizeY = ConfigManager.clamp(bhy, 100.0f, 300.0f);
        int fpDelay = loaded.fastPlaceDelay;
        if (fpDelay < 0 || fpDelay > 3) {
            fpDelay = 0;
        }
        this.config.fastPlaceDelay = fpDelay;
        int aeDelay = loaded.armorEquipperDelayTicks;
        if (aeDelay < 0 || aeDelay > 20) {
            aeDelay = 1;
        }
        this.config.armorEquipperDelayTicks = aeDelay;
        float flSpeed = loaded.fastLeverSpeed;
        if (Float.isNaN(flSpeed) || Float.isInfinite(flSpeed)) {
            flSpeed = 100.0f;
        }
        this.config.fastLeverSpeed = ConfigManager.clamp(flSpeed, 0.0f, 100.0f);
        float akSpeed = loaded.antyKostkaSpeed;
        if (Float.isNaN(akSpeed) || Float.isInfinite(akSpeed)) {
            akSpeed = 0.0f;
        }
        this.config.antyKostkaSpeed = ConfigManager.clamp(akSpeed, 0.0f, 1.0f);
        this.config.noPushPlayers = loaded.noPushPlayers;
        if (this.config.noPushPlayers == null) {
            this.config.noPushPlayers = true;
        }
        this.config.noPushBlocks = loaded.noPushBlocks;
        if (this.config.noPushBlocks == null) {
            this.config.noPushBlocks = true;
        }
        this.config.noPushLiquids = loaded.noPushLiquids;
        if (this.config.noPushLiquids == null) {
            this.config.noPushLiquids = true;
        }
        this.config.freecamCancelMove = loaded.freecamCancelMove;
        if (this.config.freecamCancelMove == null) {
            this.config.freecamCancelMove = true;
        }
        if (Float.isNaN(fcX = loaded.freecamSpeedX) || Float.isInfinite(fcX)) {
            fcX = 0.45f;
        }
        this.config.freecamSpeedX = ConfigManager.clamp(fcX, 0.05f, 1.0f);
        float fcY = loaded.freecamSpeedY;
        if (Float.isNaN(fcY) || Float.isInfinite(fcY)) {
            fcY = 0.3f;
        }
        this.config.freecamSpeedY = ConfigManager.clamp(fcY, 0.05f, 1.0f);
        this.config.refillerObsidian = loaded.refillerObsidian;
        if (this.config.refillerObsidian == null) {
            this.config.refillerObsidian = true;
        }
        this.config.refillerCobwebs = loaded.refillerCobwebs;
        if (this.config.refillerCobwebs == null) {
            this.config.refillerCobwebs = true;
        }
        this.config.refillerPearls = loaded.refillerPearls;
        if (this.config.refillerPearls == null) {
            this.config.refillerPearls = true;
        }
        this.config.nametagsShowSelf = loaded.nametagsShowSelf;
        if (this.config.nametagsShowSelf == null) {
            this.config.nametagsShowSelf = true;
        }
        this.config.nametagsShowDistance = loaded.nametagsShowDistance;
        if (this.config.nametagsShowDistance == null) {
            this.config.nametagsShowDistance = true;
        }
        this.config.nametagsShowName = loaded.nametagsShowName;
        if (this.config.nametagsShowName == null) {
            this.config.nametagsShowName = true;
        }
        this.config.nametagsShowHealth = loaded.nametagsShowHealth;
        if (this.config.nametagsShowHealth == null) {
            this.config.nametagsShowHealth = true;
        }
        this.config.nametagsShowEnchantments = loaded.nametagsShowEnchantments;
        if (this.config.nametagsShowEnchantments == null) {
            this.config.nametagsShowEnchantments = true;
        }
        this.config.nametagsShowSetBonus = loaded.nametagsShowSetBonus;
        if (this.config.nametagsShowSetBonus == null) {
            this.config.nametagsShowSetBonus = true;
        }
        this.config.nametagsShowEffects = loaded.nametagsShowEffects;
        if (this.config.nametagsShowEffects == null) {
            this.config.nametagsShowEffects = true;
        }
        this.config.nametagsShowEffectDuration = loaded.nametagsShowEffectDuration;
        if (this.config.nametagsShowEffectDuration == null) {
            this.config.nametagsShowEffectDuration = true;
        }
        this.config.nametagsShowDurability = loaded.nametagsShowDurability;
        if (this.config.nametagsShowDurability == null) {
            this.config.nametagsShowDurability = true;
        }
        this.config.nametagsShowArmorSet = loaded.nametagsShowArmorSet;
        if (this.config.nametagsShowArmorSet == null) {
            this.config.nametagsShowArmorSet = true;
        }
        this.config.nametagsShowHandItems = loaded.nametagsShowHandItems;
        if (this.config.nametagsShowHandItems == null) {
            this.config.nametagsShowHandItems = true;
        }
        this.config.nametagsShowItemCount = loaded.nametagsShowItemCount;
        if (this.config.nametagsShowItemCount == null) {
            this.config.nametagsShowItemCount = true;
        }
        this.config.nametagsShortenDistance = loaded.nametagsShortenDistance;
        if (this.config.nametagsShortenDistance == null) {
            this.config.nametagsShortenDistance = false;
        }
        this.config.nametagsTextColor = ConfigManager.clampInt(loaded.nametagsTextColor, 0, 0xFFFFFF);
        this.config.nametagsBackgroundColor = ConfigManager.clampInt(loaded.nametagsBackgroundColor, 0, 0xFFFFFF);
        this.config.nametagsBorderColor = ConfigManager.clampInt(loaded.nametagsBorderColor, 0, 0xFFFFFF);
        this.config.nametagsTextAlpha = ConfigManager.clampInt(loaded.nametagsTextAlpha, 0, 255);
        this.config.nametagsBackgroundAlpha = ConfigManager.clampInt(loaded.nametagsBackgroundAlpha, 0, 255);
        this.config.nametagsScalePercent = ConfigManager.clampInt(loaded.nametagsScalePercent, 50, 200);
        this.config.showArrayList = loaded.showArrayList;
        if (this.config.showArrayList == null) {
            this.config.showArrayList = true;
        }
        this.config.showNotifications = loaded.showNotifications;
        if (this.config.showNotifications == null) {
            this.config.showNotifications = true;
        }
        Integer notifX = loaded.notificationsX;
        Integer notifY = loaded.notificationsY;
        this.config.notificationsX = notifX == null ? null : Integer.valueOf(ConfigManager.clampInt(notifX, 0, 10000));
        this.config.notificationsY = notifY == null ? null : Integer.valueOf(ConfigManager.clampInt(notifY, 0, 10000));
        float nScale = loaded.notificationsScale;
        if (Float.isNaN(nScale) || Float.isInfinite(nScale) || nScale <= 0.0f) {
            nScale = 0.5f;
        }
        this.config.notificationsScale = ConfigManager.clamp(nScale, 0.35f, 1.2f);
        this.config.panicHideArrayList = loaded.panicHideArrayList;
        if (this.config.panicHideArrayList == null) {
            this.config.panicHideArrayList = true;
        }
        this.config.panicHideNotifications = loaded.panicHideNotifications;
        if (this.config.panicHideNotifications == null) {
            this.config.panicHideNotifications = true;
        }
        this.config.panicHideKeybinds = loaded.panicHideKeybinds;
        if (this.config.panicHideKeybinds == null) {
            this.config.panicHideKeybinds = true;
        }
        if (Float.isNaN(kScale = loaded.bindListScale) || Float.isInfinite(kScale)) {
            kScale = 0.9f;
        }
        this.config.bindListScale = ConfigManager.clamp(kScale, 0.5f, 1.4f);
        float aScale = loaded.arrayListScale;
        if (Float.isNaN(aScale) || Float.isInfinite(aScale)) {
            aScale = 1.0f;
        }
        this.config.arrayListScale = ConfigManager.clamp(aScale, 0.5f, 1.4f);
        this.config.arrayListX = ConfigManager.clampInt(loaded.arrayListX, 0, 10000);
        this.config.arrayListY = ConfigManager.clampInt(loaded.arrayListY, 0, 10000);
        this.config.arrayListRight = loaded.arrayListRight;
        if (this.config.arrayListRight == null) {
            this.config.arrayListRight = true;
        }
        this.config.colorMode = loaded.colorMode == null ? GuiConfig.ColorMode.SINGLE : loaded.colorMode;
        this.config.singleHue = ConfigManager.wrap01(loaded.singleHue);
        this.config.gradHueA = ConfigManager.wrap01(loaded.gradHueA);
        this.config.gradHueB = ConfigManager.wrap01(loaded.gradHueB);
        this.config.gradSpeed = ConfigManager.clamp(loaded.gradSpeed, 0.0f, 1.0f);
        this.config.rainbowSpeed = ConfigManager.clamp(loaded.rainbowSpeed, 0.0f, 1.0f);
        this.config.activeConfig = loaded.activeConfig == null ? "default" : loaded.activeConfig;
        this.config.bindListX = ConfigManager.clampInt(loaded.bindListX, 0, 10000);
        this.config.bindListY = ConfigManager.clampInt(loaded.bindListY, 0, 10000);
    }

    private static float clamp(float v, float a, float b) {
        return Math.max(a, Math.min(b, v));
    }

    private static boolean isAlphaUnset(Float v) {
        if (v == null) {
            return true;
        }
        if (Float.isNaN(v.floatValue()) || Float.isInfinite(v.floatValue())) {
            return true;
        }
        return v.floatValue() <= 1.0E-4f;
    }

    private static int clampInt(int v, int a, int b) {
        return Math.max(a, Math.min(b, v));
    }

    private static float wrap01(float v) {
        if (Float.isNaN(v) || Float.isInfinite(v)) {
            return 0.0f;
        }
        if ((v %= 1.0f) < 0.0f) {
            v += 1.0f;
        }
        return v;
    }

    private static float clamp01Inclusive(float v) {
        if (Float.isNaN(v) || Float.isInfinite(v)) {
            return 0.0f;
        }
        return ConfigManager.clamp(v, 0.0f, 1.0f);
    }

    public static class Snapshot {
        public GuiConfig config;
        public ModuleManager.ModuleState modules;
        public FriendManager.FriendState friends;
    }
}

