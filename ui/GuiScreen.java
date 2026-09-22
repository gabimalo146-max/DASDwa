package me.Gui.gui.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import me.Gui.gui.client.GuiClient;
import me.Gui.gui.config.GuiConfig;
import me.Gui.gui.modules.BindUtil;
import me.Gui.gui.modules.GearRenderUtil;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import me.Gui.gui.modules.NameProtectUtil;
import me.Gui.gui.modules.XrayUtil;
import me.Gui.gui.ui.GuiFonts;
import me.Gui.gui.ui.HudEditScreen;
import me.Gui.gui.ui.Theme;
import me.Gui.gui.ui.render.Rounded;
import me.Gui.gui.ui.widget.UiAlphaSlider;
import me.Gui.gui.ui.widget.UiColorSlider;
import me.Gui.gui.ui.widget.UiHueSlider;
import me.Gui.gui.ui.widget.UiSlider;
import me.Gui.gui.ui.widget.UiTextField;
import me.Gui.gui.ui.world.BlockEspRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

public class GuiScreen
extends Screen {
    private static Tab savedTab = Tab.MODULES;
    private static SettingsSub savedSettingsSub = SettingsSub.COLORS;
    private static ModulesView savedModulesView = ModulesView.LIST;
    private static ModuleId savedSelectedModuleId = null;
    private static float savedModulesScroll = 0.0f;
    private static float savedModuleSettingsScroll = 0.0f;
    private Tab tab = Tab.MODULES;
    private SettingsSub settingsSub = SettingsSub.COLORS;
    private ModulesView modulesView = ModulesView.LIST;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private final int sidebarW = 96;
    private final int topH = 44;
    private static final float R_PANEL = 18.0f;
    private static final float R_TILE = 14.0f;
    private static final float R_BTN = 10.0f;
    private static final int FIELD_H = 16;
    private static final int SEARCH_FIELD_H = 14;
    private static final int SEARCH_FIELD_MIN_W = 120;
    private static final int SEARCH_FIELD_MAX_W = 220;
    private static final int FRIEND_ADD_MIN_W = 64;
    private static final int FRIEND_ADD_MAX_W = 220;
    private static final int TAB_H = 22;
    private static final int TAB_GAP = 8;
    private static final int TAB_START_Y = 38;
    private static final int TILE_H = 30;
    private static final int TILE_GAP = 8;
    private static final int LIST_ROW_H = 24;
    private static final int LIST_GAP = 8;
    private static final int SETTINGS_TAB_H = 18;
    private static final int SETTINGS_GAP = 8;
    private static final int SLIDER_H = 26;
    private static final int TOGGLE_H = 18;
    private static final int BACK_H = 18;
    private static final int REVEAL_SLIDE_PX = 8;
    private static final int GEAR_ROW_H = 26;
    private static final int GEAR_ICON = 18;
    private static final int GEAR_BTN_W = 50;
    private static final int GEAR_BTN_H = 18;
    private static final long OPEN_DUR_MS = 520L;
    private static final long CLOSE_DUR_MS = 360L;
    private static final long TAB_SWITCH_DUR_MS = 260L;
    private static final float TAB_SLIDE_PX = 18.0f;
    private static final float VIEW_SLIDE_PX = 22.0f;
    private float modulesScroll = 0.0f;
    private float configsScroll = 0.0f;
    private float friendsScroll = 0.0f;
    private float displayNamesScroll = 0.0f;
    private float gearBlacklistScroll = 0.0f;
    private float moduleSettingsScroll = 0.0f;
    private float hudScroll = 0.0f;
    private float blockListScroll = 0.0f;
    private float blockSelectedScroll = 0.0f;
    private float xrayListScroll = 0.0f;
    private float xraySelectedScroll = 0.0f;
    private float itemListScroll = 0.0f;
    private float itemSelectedScroll = 0.0f;
    private float modulesScrollTarget = 0.0f;
    private float configsScrollTarget = 0.0f;
    private float friendsScrollTarget = 0.0f;
    private float displayNamesScrollTarget = 0.0f;
    private float gearBlacklistScrollTarget = 0.0f;
    private float moduleSettingsScrollTarget = 0.0f;
    private float hudScrollTarget = 0.0f;
    private float blockListScrollTarget = 0.0f;
    private float blockSelectedScrollTarget = 0.0f;
    private float xrayListScrollTarget = 0.0f;
    private float xraySelectedScrollTarget = 0.0f;
    private float itemListScrollTarget = 0.0f;
    private float itemSelectedScrollTarget = 0.0f;
    private UiTextField searchField;
    private UiTextField configNameField;
    private UiTextField friendNameField;
    private UiTextField friendSearchField;
    private UiTextField nameProtectField;
    private UiTextField nameProtectTimeField;
    private UiTextField nameProtectMoneyField;
    private UiTextField nameProtectKillsField;
    private UiTextField nameProtectDeathsField;
    private UiTextField spammerMessageField;
    private UiTextField gearBlacklistField;
    private UiTextField blockSearchField;
    private UiTextField xraySearchField;
    private UiTextField itemSearchField;
    private List<String> configNames = new ArrayList<String>();
    private HackModule bindingTarget = null;
    private HackModule selectedModule = null;
    private ModuleId hoverModuleId = null;
    private long hoverStartMs = 0L;
    private boolean blockPickerOpen = false;
    private boolean xrayPickerOpen = false;
    private boolean itemPickerOpen = false;
    private boolean displayNamesOpen = false;
    private boolean gearBlacklistOpen = false;
    private boolean pickerShowAdded = false;
    private List<BlockEntry> blockEntries = null;
    private Map<String, BlockEntry> blockEntryById = null;
    private List<BlockEntry> itemEntries = null;
    private Map<String, BlockEntry> itemEntryById = null;
    private final Map<String, UiTextField> displayNameFields = new HashMap<String, UiTextField>();
    private final List<GearRenderButton> gearRenderButtons = new ArrayList<GearRenderButton>();
    private long openStartMs = System.currentTimeMillis();
    private boolean closing = false;
    private long closeStartMs = 0L;
    private long tabSwitchMs = 0L;
    private int tabSwitchDir = 1;
    private float tabHighlightY = -1.0f;
    private float modulesViewAnim = 0.0f;
    private float searchExpandAnim = 0.0f;
    private float friendAddExpandAnim = 0.0f;
    private boolean friendAddExpanded = false;
    private int friendAddX = 0;
    private int friendAddY = 0;
    private int friendAddW = 0;
    private int friendAddH = 0;
    private UiHueSlider hueSingle;
    private UiHueSlider hueA;
    private UiHueSlider hueB;
    private UiSlider gradSpeed;
    private UiSlider rainbowSpeed;
    private UiSlider transparency;
    private UiSlider uiVolume;
    private UiSlider guiScale;
    private UiSlider tpaAcceptSoundVolumeSlider;
    private UiSlider bindListScaleSlider;
    private UiSlider arrayListScaleSlider;
    private UiSlider fullbrightSlider;
    private UiSlider fastPlaceDelaySlider;
    private UiSlider fastLeverSpeedSlider;
    private UiSlider autoPlotekCooldownSlider;
    private UiSlider aimAssistRangeSlider;
    private UiSlider betterHitboxSizeXSlider;
    private UiSlider betterHitboxSizeYSlider;
    private UiSlider antyKostkaSpeedSlider;
    private UiSlider spammerDelaySlider;
    private UiSlider errorKillerDelaySlider;
    private UiSlider errorKillerDelayObsidianSlider;
    private UiSlider armorEquipperDelaySlider;
    private UiSlider pearlLineWidthSlider;
    private UiHueSlider tracerPlayersColorSlider;
    private UiHueSlider tracerFriendsColorSlider;
    private UiHueSlider tracerMobsColorSlider;
    private UiColorSlider espPlayerColorSlider;
    private UiColorSlider espFriendColorSlider;
    private UiSlider espLineWidthSlider;
    private UiSlider tracerDistanceSlider;
    private UiSlider freecamSpeedXSlider;
    private UiSlider freecamSpeedYSlider;
    private UiColorSlider swordInfoExtraColorSlider;
    private UiSlider swordInfoScaleSlider;
    private UiSlider swordInfoAllScaleSlider;
    private float swordInfoFrameAnim;
    private float swordInfoShowAllAnim;
    private float itemEspAddAllAnim;
    private UiColorSlider nametagsTextColorSlider;
    private UiColorSlider nametagsBackgroundColorSlider;
    private UiColorSlider nametagsBorderColorSlider;
    private UiAlphaSlider nametagsTextAlphaSlider;
    private UiAlphaSlider nametagsBackgroundAlphaSlider;
    private UiSlider nametagsScaleSlider;
    private float pearlTrajAnim;
    private float pearlLandingAnim;
    private float pearlNicknameAnim;
    private float pearlCountdownAnim;
    private float pearlOwnAnim;
    private float tracerPlayersAnim;
    private float tracerFriendsAnim;
    private float friendsChangeNametagAnim;
    private float tracerMobsAnim;
    private float espBoxAnim;
    private float logoutNickAnim;
    private float logoutTimeAnim;
    private float antiTrapFrameAnim;
    private float antiTrapFrameAutoBreakAnim;
    private float antiTrapFrameVisualAnim;
    private float antiTrapPaintingAnim;
    private float antiTrapPaintingAutoBreakAnim;
    private float antiTrapPaintingVisualAnim;
    private float antiTrapMinecartAnim;
    private float antiTrapArmorAnim;
    private float antiTrapMobsAnim;
    private float antiTrapAutoPlotekAnim;
    private float autoParawanDontAttackFriendsAnim;
    private float aimAssistTargetLockAnim;
    private float aimAssistMobsAnim;
    private float noPushPlayersAnim;
    private float noPushBlocksAnim;
    private float noPushLiquidsAnim;
    private float freecamCancelMoveAnim;
    private float nametagsDistanceAnim;
    private float nametagsNameAnim;
    private float nametagsHealthAnim;
    private float nametagsEnchantAnim;
    private float nametagsSetBonusAnim;
    private float nametagsEffectsAnim;
    private float nametagsDurabilityAnim;
    private float nametagsArmorAnim;
    private float nametagsHandAnim;
    private float nametagsItemCountAnim;
    private float refillerObsidianAnim;
    private float refillerCobwebsAnim;
    private float refillerPearlsAnim;
    private float panicArrayListAnim;
    private float panicNotificationsAnim;
    private float panicKeybindsAnim;
    private float errorKillerShowMissingAnim;
    private float errorKillerShowMissingObsidianAnim;
    private float spammerAntiSpamAnim;
    private float nameProtectChangeStatsAnim;
    private float gearAutoTpaAnim;
    private float gearAutoLogoutAnim;
    private final Map<ModuleId, Float> moduleHoverAnim = new HashMap<ModuleId, Float>();
    private final Map<ModuleId, Float> moduleEnableAnim = new HashMap<ModuleId, Float>();
    private OverlayType overlayType = OverlayType.NONE;
    private float overlayAnim = 0.0f;

    public GuiScreen() {
        super((Text)Text.literal((String)"FastClient"));
    }

    protected void init() {
        super.init();
        this.rebuildLayout();
        this.buildWidgets();
        this.restoreGuiState();
        this.refreshConfigs();
        this.layoutFields(0, 0);
    }

    private void rebuildLayout() {
        int sw = this.width;
        int sh = this.height;
        this.panelW = Math.min(980, sw - 60);
        this.panelH = Math.min(580, sh - 60);
        this.panelX = (sw - this.panelW) / 2;
        this.panelY = (sh - this.panelH) / 2;
    }

    private void buildWidgets() {
        this.searchField = new UiTextField(0, 0, 120, 14, "Search...");
        this.configNameField = new UiTextField(0, 0, 200, 16, "Config name (Enter)");
        this.friendNameField = new UiTextField(0, 0, 200, 16, "Friend name (Enter)");
        this.friendSearchField = new UiTextField(0, 0, 200, 16, "Search friends...");
        this.nameProtectField = new UiTextField(0, 0, 200, 16, "Nick...");
        this.nameProtectTimeField = new UiTextField(0, 0, 200, 16, "Czas...");
        this.nameProtectMoneyField = new UiTextField(0, 0, 200, 16, "Kasa...");
        this.nameProtectKillsField = new UiTextField(0, 0, 200, 16, "Kille...");
        this.nameProtectDeathsField = new UiTextField(0, 0, 200, 16, "\u015amierci...");
        this.spammerMessageField = new UiTextField(0, 0, 200, 16, "Message...");
        this.gearBlacklistField = new UiTextField(0, 0, 200, 16, "Nick...");
        this.spammerMessageField.field.setMaxLength(200);
        this.gearBlacklistField.field.setMaxLength(32);
        this.friendSearchField.field.setMaxLength(64);
        this.nameProtectTimeField.field.setMaxLength(64);
        this.nameProtectMoneyField.field.setMaxLength(64);
        this.nameProtectKillsField.field.setMaxLength(64);
        this.nameProtectDeathsField.field.setMaxLength(64);
        this.blockSearchField = new UiTextField(0, 0, 200, 16, "Search blocks...");
        this.xraySearchField = new UiTextField(0, 0, 200, 16, "Search blocks...");
        this.itemSearchField = new UiTextField(0, 0, 200, 16, "Search items...");
        this.addSelectableChild(this.searchField.field);
        this.addSelectableChild(this.configNameField.field);
        this.addSelectableChild(this.friendNameField.field);
        this.addSelectableChild(this.friendSearchField.field);
        this.addSelectableChild(this.nameProtectField.field);
        this.addSelectableChild(this.nameProtectTimeField.field);
        this.addSelectableChild(this.nameProtectMoneyField.field);
        this.addSelectableChild(this.nameProtectKillsField.field);
        this.addSelectableChild(this.nameProtectDeathsField.field);
        this.addSelectableChild(this.spammerMessageField.field);
        this.addSelectableChild(this.blockSearchField.field);
        this.addSelectableChild(this.xraySearchField.field);
        this.addSelectableChild(this.itemSearchField.field);
        GuiConfig cfg = GuiClient.CONFIG;
        this.hueSingle = new UiHueSlider(0, 0, 0, 0, cfg.singleHue, v -> {
            cfg.singleHue = v;
        });
        this.hueA = new UiHueSlider(0, 0, 0, 0, cfg.gradHueA, v -> {
            cfg.gradHueA = v;
        });
        this.hueB = new UiHueSlider(0, 0, 0, 0, cfg.gradHueB, v -> {
            cfg.gradHueB = v;
        });
        this.gradSpeed = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)"Speed"), cfg.gradSpeed, v -> {
            cfg.gradSpeed = v;
        });
        this.rainbowSpeed = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)"Speed"), cfg.rainbowSpeed, v -> {
            cfg.rainbowSpeed = v;
        });
        this.transparency = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)"Transparency"), cfg.transparency, v -> {
            cfg.transparency = v;
        });
        this.uiVolume = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)"UI Volume"), cfg.uiVolume, v -> {
            cfg.uiVolume = v;
        });
        Float tpaVolObj = cfg.tpaAcceptSoundVolume;
        float tpaVol = tpaVolObj == null || Float.isNaN(tpaVolObj.floatValue()) || Float.isInfinite(tpaVolObj.floatValue()) ? 1.0f : tpaVolObj.floatValue();
        tpaVol = GuiScreen.clamp(tpaVol, 0.0f, 1.0f);
        cfg.tpaAcceptSoundVolume = Float.valueOf(tpaVol);
        this.tpaAcceptSoundVolumeSlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)"Glosnosc"), tpaVol, v -> {
            cfg.tpaAcceptSoundVolume = Float.valueOf(GuiScreen.clamp(v, 0.0f, 1.0f));
        });
        this.guiScale = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)"GUI Scale"), (cfg.guiScale - 0.75f) / 0.6f, v -> {
            cfg.guiScale = 0.75f + v * 0.6f;
        });
        float minHud = 0.5f;
        float maxHud = 1.4f;
        this.bindListScaleSlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)"Keybinds Size"), (cfg.bindListScale - minHud) / (maxHud - minHud), v -> {
            cfg.bindListScale = minHud + v * (maxHud - minHud);
        });
        this.arrayListScaleSlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)"ArrayList Size"), (cfg.arrayListScale - minHud) / (maxHud - minHud), v -> {
            cfg.arrayListScale = minHud + v * (maxHud - minHud);
        });
        this.fullbrightSlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)"Brightness"), cfg.fullbrightStrength, v -> {
            cfg.fullbrightStrength = v;
        });
        this.fastPlaceDelaySlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)"Delay"), Math.max(0.0f, Math.min(1.0f, (float)cfg.fastPlaceDelay / 3.0f)), v -> {
            int mode = Math.round(v * 3.0f);
            if (mode < 0) {
                mode = 0;
            }
            if (mode > 3) {
                mode = 3;
            }
            cfg.fastPlaceDelay = mode;
            this.fastPlaceDelaySlider.value = (float)mode / 3.0f;
        });
        int aeDelay = cfg.armorEquipperDelayTicks;
        if (aeDelay < 0 || aeDelay > 20) {
            aeDelay = 1;
        }
        this.armorEquipperDelaySlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)("Delay: " + aeDelay + " ticks")), (float)aeDelay / 20.0f, v -> {
            cfg.armorEquipperDelayTicks = Math.max(0, Math.min(20, Math.round(v * 20.0f)));
        });
        float flSpeed = cfg.fastLeverSpeed;
        if (Float.isNaN(flSpeed) || Float.isInfinite(flSpeed)) {
            flSpeed = 100.0f;
        }
        flSpeed = Math.max(0.0f, Math.min(100.0f, flSpeed));
        this.fastLeverSpeedSlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)("Speed: " + Math.round(flSpeed) + "%")), flSpeed / 100.0f, v -> {
            cfg.fastLeverSpeed = Math.max(0.0f, Math.min(100.0f, v * 100.0f));
        });
        int apCdMin = 0;
        int apCdMax = 500;
        int apCd = cfg.autoPlotekCooldownMs;
        if (apCd < apCdMin || apCd > apCdMax) {
            apCd = 80;
        }
        this.autoPlotekCooldownSlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)("Cooldown: " + apCd + "ms")), (float)(apCd - apCdMin) / (float)(apCdMax - apCdMin), v -> {
            cfg.autoPlotekCooldownMs = apCdMin + Math.round(v * (float)(apCdMax - apCdMin));
        });
        float aaMin = 2.0f;
        float aaMax = 10.0f;
        float aaRange = cfg.aimAssistRange;
        if (Float.isNaN(aaRange) || Float.isInfinite(aaRange)) {
            aaRange = 6.0f;
        }
        aaRange = Math.max(aaMin, Math.min(aaMax, aaRange));
        this.aimAssistRangeSlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)("Range: " + String.format(Locale.US, "%.1f", Float.valueOf(aaRange)))), (aaRange - aaMin) / (aaMax - aaMin), v -> {
            cfg.aimAssistRange = aaMin + v * (aaMax - aaMin);
        });
        float bhMin = 100.0f;
        float bhMax = 300.0f;
        float bhX = cfg.betterHitboxSizeX;
        if (Float.isNaN(bhX) || Float.isInfinite(bhX)) {
            bhX = 100.0f;
        }
        bhX = GuiScreen.clamp(bhX, bhMin, bhMax);
        this.betterHitboxSizeXSlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)("Size X: " + Math.round(bhX) + "%")), (bhX - bhMin) / (bhMax - bhMin), v -> {
            cfg.betterHitboxSizeX = bhMin + v * (bhMax - bhMin);
        });
        float bhY = cfg.betterHitboxSizeY;
        if (Float.isNaN(bhY) || Float.isInfinite(bhY)) {
            bhY = 100.0f;
        }
        bhY = GuiScreen.clamp(bhY, bhMin, bhMax);
        this.betterHitboxSizeYSlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)("Size Y: " + Math.round(bhY) + "%")), (bhY - bhMin) / (bhMax - bhMin), v -> {
            cfg.betterHitboxSizeY = bhMin + v * (bhMax - bhMin);
        });
        float akSpeed = cfg.antyKostkaSpeed;
        if (Float.isNaN(akSpeed) || Float.isInfinite(akSpeed)) {
            akSpeed = 0.0f;
        }
        akSpeed = Math.max(0.0f, Math.min(1.0f, akSpeed));
        this.antyKostkaSpeedSlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)"Speed: 1.0x"), akSpeed, v -> {
            cfg.antyKostkaSpeed = Math.max(0.0f, Math.min(1.0f, v));
        });
        int sDelay = cfg.spammerDelaySec;
        if (sDelay < 1 || sDelay > 60) {
            sDelay = 3;
        }
        this.spammerDelaySlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)("Delay: " + sDelay + "s")), (float)(sDelay - 1) / 59.0f, v -> {
            cfg.spammerDelaySec = Math.max(1, Math.min(60, 1 + Math.round(v * 59.0f)));
        });
        int ekDelay = cfg.errorKillerDelayMs;
        if (ekDelay < 1 || ekDelay > 100) {
            ekDelay = 1;
        }
        this.errorKillerDelaySlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)"Delay"), Math.max(0.0f, Math.min(1.0f, (float)(ekDelay - 1) / 99.0f)), v -> {
            int ms = 1 + Math.round(v * 99.0f);
            if (ms < 1) {
                ms = 1;
            }
            cfg.errorKillerDelayMs = ms;
        });
        int ekObDelay = cfg.errorKillerDelayMsObsidian;
        if (ekObDelay < 1 || ekObDelay > 100) {
            ekObDelay = ekDelay;
        }
        this.errorKillerDelayObsidianSlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)"Delay"), Math.max(0.0f, Math.min(1.0f, (float)(ekObDelay - 1) / 99.0f)), v -> {
            int ms = 1 + Math.round(v * 99.0f);
            if (ms < 1) {
                ms = 1;
            }
            cfg.errorKillerDelayMsObsidian = ms;
        });
        float minLine = 1.0f;
        float maxLine = 6.0f;
        this.pearlLineWidthSlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)"Line Width"), (cfg.pearlLineWidth - minLine) / (maxLine - minLine), v -> {
            cfg.pearlLineWidth = minLine + v * (maxLine - minLine);
        });
        this.tracerPlayersColorSlider = new UiHueSlider(0, 0, 0, 0, cfg.tracerPlayerHue, v -> {
            cfg.tracerPlayerHue = v;
        });
        this.tracerFriendsColorSlider = new UiHueSlider(0, 0, 0, 0, cfg.tracerFriendHue, v -> {
            cfg.tracerFriendHue = v;
        });
        this.tracerMobsColorSlider = new UiHueSlider(0, 0, 0, 0, cfg.tracerMobHue, v -> {
            cfg.tracerMobHue = v;
        });
        this.espPlayerColorSlider = new UiColorSlider(0, 0, 0, 0, "Player Color", cfg.espPlayerColor, v -> {
            cfg.espPlayerColor = v;
        });
        this.espFriendColorSlider = new UiColorSlider(0, 0, 0, 0, "Friend Color", cfg.espFriendColor, v -> {
            cfg.espFriendColor = v;
        });
        float minEspLine = 1.0f;
        float maxEspLine = 6.0f;
        float espLine = cfg.espLineWidth;
        if (Float.isNaN(espLine) || Float.isInfinite(espLine)) {
            espLine = 4.0f;
        }
        espLine = Math.max(minEspLine, Math.min(maxEspLine, espLine));
        this.espLineWidthSlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)"Line Width"), (espLine - minEspLine) / (maxEspLine - minEspLine), v -> {
            cfg.espLineWidth = minEspLine + v * (maxEspLine - minEspLine);
        });
        float minTrace = 8.0f;
        float maxTrace = 256.0f;
        this.tracerDistanceSlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)"Distance"), (cfg.tracerDistance - minTrace) / (maxTrace - minTrace), v -> {
            cfg.tracerDistance = minTrace + v * (maxTrace - minTrace);
        });
        float minFreecam = 0.05f;
        float maxFreecam = 1.0f;
        this.freecamSpeedXSlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)"Speed X"), (cfg.freecamSpeedX - minFreecam) / (maxFreecam - minFreecam), v -> {
            cfg.freecamSpeedX = minFreecam + v * (maxFreecam - minFreecam);
        });
        this.freecamSpeedYSlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)"Speed Y"), (cfg.freecamSpeedY - minFreecam) / (maxFreecam - minFreecam), v -> {
            cfg.freecamSpeedY = minFreecam + v * (maxFreecam - minFreecam);
        });
        int swordColor = cfg.swordInfoExtraColor == null ? 5611775 : cfg.swordInfoExtraColor;
        this.swordInfoExtraColorSlider = new UiColorSlider(0, 0, 0, 0, "Kolor dodatkowych obrazen", swordColor, v -> {
            cfg.swordInfoExtraColor = v;
        });
        float minSwordScale = 0.6f;
        float maxSwordScale = 1.4f;
        float siScale = cfg.swordInfoScale;
        if (Float.isNaN(siScale) || Float.isInfinite(siScale)) {
            siScale = 0.9f;
        }
        siScale = Math.max(minSwordScale, Math.min(maxSwordScale, siScale));
        this.swordInfoScaleSlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)"Size"), (siScale - minSwordScale) / (maxSwordScale - minSwordScale), v -> {
            cfg.swordInfoScale = minSwordScale + v * (maxSwordScale - minSwordScale);
        });
        float minSwordAll = 0.6f;
        float maxSwordAll = 1.2f;
        float allScale = cfg.swordInfoAllScale;
        if (Float.isNaN(allScale) || Float.isInfinite(allScale)) {
            allScale = 0.8f;
        }
        allScale = Math.max(minSwordAll, Math.min(maxSwordAll, allScale));
        this.swordInfoAllScaleSlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)"Tabelka size"), (allScale - minSwordAll) / (maxSwordAll - minSwordAll), v -> {
            cfg.swordInfoAllScale = minSwordAll + v * (maxSwordAll - minSwordAll);
        });
        this.nametagsTextColorSlider = new UiColorSlider(0, 0, 0, 0, "Kolor tekstu", cfg.nametagsTextColor, v -> {
            cfg.nametagsTextColor = v;
        });
        this.nametagsBackgroundColorSlider = new UiColorSlider(0, 0, 0, 0, "Kolor tla", cfg.nametagsBackgroundColor, v -> {
            cfg.nametagsBackgroundColor = v;
        });
        this.nametagsBorderColorSlider = new UiColorSlider(0, 0, 0, 0, "Kolor obramowki tla", cfg.nametagsBorderColor, v -> {
            cfg.nametagsBorderColor = v;
        });
        this.nametagsTextAlphaSlider = new UiAlphaSlider(0, 0, 0, 0, "Przezroczystosc tekstu", cfg.nametagsTextAlpha, v -> {
            cfg.nametagsTextAlpha = v;
        });
        this.nametagsBackgroundAlphaSlider = new UiAlphaSlider(0, 0, 0, 0, "Przezroczystosc tla", cfg.nametagsBackgroundAlpha, v -> {
            cfg.nametagsBackgroundAlpha = v;
        });
        this.nametagsScaleSlider = new UiSlider(0, 0, 0, 0, (Text)Text.literal((String)"Skala"), (float)(cfg.nametagsScalePercent - 50) / 150.0f, v -> {
            cfg.nametagsScalePercent = 50 + Math.round(v * 150.0f);
        });
        this.pearlTrajAnim = cfg.pearlShowTrajectory == null || cfg.pearlShowTrajectory != false ? 1.0f : 0.0f;
        this.pearlLandingAnim = cfg.pearlShowLanding == null || cfg.pearlShowLanding != false ? 1.0f : 0.0f;
        this.pearlNicknameAnim = cfg.pearlShowNickname == null || cfg.pearlShowNickname != false ? 1.0f : 0.0f;
        this.pearlCountdownAnim = cfg.pearlShowCountdown == null || cfg.pearlShowCountdown != false ? 1.0f : 0.0f;
        this.pearlOwnAnim = cfg.pearlShowOwn == null || cfg.pearlShowOwn != false ? 1.0f : 0.0f;
        this.tracerPlayersAnim = cfg.tracerPlayers == null || cfg.tracerPlayers != false ? 1.0f : 0.0f;
        this.tracerFriendsAnim = cfg.tracerFriends == null || cfg.tracerFriends != false ? 1.0f : 0.0f;
        this.friendsChangeNametagAnim = cfg.friendsChangeNormalNametag == null || cfg.friendsChangeNormalNametag != false ? 1.0f : 0.0f;
        this.tracerMobsAnim = cfg.tracerMobs == null || cfg.tracerMobs != false ? 1.0f : 0.0f;
        this.espBoxAnim = cfg.espBox == null || cfg.espBox != false ? 1.0f : 0.0f;
        this.logoutNickAnim = cfg.logoutShowNick == null || cfg.logoutShowNick != false ? 1.0f : 0.0f;
        this.logoutTimeAnim = cfg.logoutShowTime == null || cfg.logoutShowTime != false ? 1.0f : 0.0f;
        this.antiTrapFrameAnim = cfg.antiTrapItemFrame == null || cfg.antiTrapItemFrame != false ? 1.0f : 0.0f;
        this.antiTrapFrameAutoBreakAnim = cfg.antiTrapItemFrameAutoBreak == null || cfg.antiTrapItemFrameAutoBreak != false ? 1.0f : 0.0f;
        this.antiTrapFrameVisualAnim = cfg.antiTrapItemFrameVisual == null || cfg.antiTrapItemFrameVisual != false ? 1.0f : 0.0f;
        this.antiTrapPaintingAnim = cfg.antiTrapPainting == null || cfg.antiTrapPainting != false ? 1.0f : 0.0f;
        this.antiTrapPaintingAutoBreakAnim = cfg.antiTrapPaintingAutoBreak == null || cfg.antiTrapPaintingAutoBreak != false ? 1.0f : 0.0f;
        this.antiTrapPaintingVisualAnim = cfg.antiTrapPaintingVisual == null || cfg.antiTrapPaintingVisual != false ? 1.0f : 0.0f;
        this.antiTrapMinecartAnim = cfg.antiTrapMinecart == null || cfg.antiTrapMinecart != false ? 1.0f : 0.0f;
        this.antiTrapArmorAnim = cfg.antiTrapArmorStand == null || cfg.antiTrapArmorStand != false ? 1.0f : 0.0f;
        this.antiTrapMobsAnim = cfg.antiTrapMobs != null && cfg.antiTrapMobs != false ? 1.0f : 0.0f;
        this.antiTrapAutoPlotekAnim = cfg.antiTrapAutoPlotek != null && cfg.antiTrapAutoPlotek != false ? 1.0f : 0.0f;
        this.autoParawanDontAttackFriendsAnim = cfg.autoParawanDontAttackFriends == null || cfg.autoParawanDontAttackFriends != false ? 1.0f : 0.0f;
        this.noPushPlayersAnim = cfg.noPushPlayers == null || cfg.noPushPlayers != false ? 1.0f : 0.0f;
        this.noPushBlocksAnim = cfg.noPushBlocks == null || cfg.noPushBlocks != false ? 1.0f : 0.0f;
        this.noPushLiquidsAnim = cfg.noPushLiquids == null || cfg.noPushLiquids != false ? 1.0f : 0.0f;
        this.freecamCancelMoveAnim = cfg.freecamCancelMove == null || cfg.freecamCancelMove != false ? 1.0f : 0.0f;
        this.refillerObsidianAnim = cfg.refillerObsidian == null || cfg.refillerObsidian != false ? 1.0f : 0.0f;
        this.refillerCobwebsAnim = cfg.refillerCobwebs == null || cfg.refillerCobwebs != false ? 1.0f : 0.0f;
        this.refillerPearlsAnim = cfg.refillerPearls == null || cfg.refillerPearls != false ? 1.0f : 0.0f;
        this.errorKillerShowMissingAnim = cfg.errorKillerShowMissing != null && cfg.errorKillerShowMissing != false ? 1.0f : 0.0f;
        this.errorKillerShowMissingObsidianAnim = cfg.errorKillerShowMissingObsidian != null && cfg.errorKillerShowMissingObsidian != false ? 1.0f : 0.0f;
        this.spammerAntiSpamAnim = cfg.spammerAntiSpam != null && cfg.spammerAntiSpam != false ? 1.0f : 0.0f;
        this.nameProtectChangeStatsAnim = cfg.nameProtectChangeStats != null && cfg.nameProtectChangeStats != false ? 1.0f : 0.0f;
        this.gearAutoTpaAnim = cfg.gearRenderAutoTpa != null && cfg.gearRenderAutoTpa != false ? 1.0f : 0.0f;
        this.gearAutoLogoutAnim = cfg.gearRenderAutoLogout == null || cfg.gearRenderAutoLogout != false ? 1.0f : 0.0f;
        this.nametagsDistanceAnim = cfg.nametagsShowDistance == null || cfg.nametagsShowDistance != false ? 1.0f : 0.0f;
        this.nametagsNameAnim = cfg.nametagsShowName == null || cfg.nametagsShowName != false ? 1.0f : 0.0f;
        this.nametagsHealthAnim = cfg.nametagsShowHealth == null || cfg.nametagsShowHealth != false ? 1.0f : 0.0f;
        this.nametagsEnchantAnim = cfg.nametagsShowEnchantments == null || cfg.nametagsShowEnchantments != false ? 1.0f : 0.0f;
        this.nametagsSetBonusAnim = cfg.nametagsShowSetBonus == null || cfg.nametagsShowSetBonus != false ? 1.0f : 0.0f;
        this.nametagsEffectsAnim = cfg.nametagsShowEffects == null || cfg.nametagsShowEffects != false ? 1.0f : 0.0f;
        this.nametagsDurabilityAnim = cfg.nametagsShowDurability == null || cfg.nametagsShowDurability != false ? 1.0f : 0.0f;
        this.nametagsArmorAnim = cfg.nametagsShowArmorSet == null || cfg.nametagsShowArmorSet != false ? 1.0f : 0.0f;
        this.nametagsHandAnim = cfg.nametagsShowHandItems == null || cfg.nametagsShowHandItems != false ? 1.0f : 0.0f;
        this.nametagsItemCountAnim = cfg.nametagsShowItemCount == null || cfg.nametagsShowItemCount != false ? 1.0f : 0.0f;
        this.swordInfoFrameAnim = cfg.swordInfoFrame == null || cfg.swordInfoFrame != false ? 1.0f : 0.0f;
        this.swordInfoShowAllAnim = cfg.swordInfoShowAllPlayers != null && cfg.swordInfoShowAllPlayers != false ? 1.0f : 0.0f;
    }

    private void refreshConfigs() {
        this.configNames = GuiClient.CONFIGS.listConfigs();
    }

    private void openConfigsFolder() {
        try {
            Path dir = FabricLoader.getInstance().getConfigDir().resolve("gui").resolve("configs");
            Files.createDirectories(dir, new FileAttribute[0]);
            Util.getOperatingSystem().open(dir.toFile());
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public boolean shouldPause() {
        return false;
    }

    private float openAnim() {
        long now = System.currentTimeMillis();
        if (this.closing) {
            float t = (float)(now - this.closeStartMs) / 360.0f;
            t = GuiScreen.clamp(t, 0.0f, 1.0f);
            return 1.0f - GuiScreen.easeOutCubic(t);
        }
        float t = (float)(now - this.openStartMs) / 520.0f;
        t = GuiScreen.clamp(t, 0.0f, 1.0f);
        return GuiScreen.easeOutCubic(t);
    }

    private float finalScale() {
        float openScale;
        if (this.closing) {
            openScale = 1.0f;
        } else {
            float t = this.openAnim();
            openScale = 0.88f + 0.12f * t;
        }
        return openScale * GuiClient.CONFIG.guiScale;
    }

    private int scaledMouseX(int rawX) {
        float s = this.finalScale();
        int cx = this.panelX + this.panelW / 2;
        return (int)((float)(rawX - cx) / s + (float)cx);
    }

    private int scaledMouseY(int rawY) {
        float s = this.finalScale();
        int cy = this.panelY + this.panelH / 2;
        return (int)((float)(rawY - cy) / s + (float)cy);
    }

    private void layoutFields(int mouseX, int mouseY) {
        int w;
        int y;
        int x;
        Object layout;
        int contentX = this.panelX + 96;
        int pad = (int)Math.ceil(18.0) + 8;
        int innerRight = this.panelX + this.panelW - pad;
        int innerTop = this.panelY + pad;
        boolean pickerOpen = this.blockPickerOpen || this.xrayPickerOpen || this.itemPickerOpen || this.displayNamesOpen || this.gearBlacklistOpen;
        boolean showSearch = !pickerOpen && this.tab == Tab.MODULES && this.modulesView == ModulesView.LIST;
        boolean showConfig = !pickerOpen && this.tab == Tab.CONFIGS;
        boolean showFriend = !pickerOpen && this.tab == Tab.FRIENDS;
        boolean showBlockSearch = this.blockPickerOpen;
        boolean showXraySearch = this.xrayPickerOpen;
        boolean showItemSearch = this.itemPickerOpen;
        boolean showGearBlacklist = this.gearBlacklistOpen;
        this.searchField.field.setVisible(showSearch);
        this.configNameField.field.setVisible(showConfig);
        this.friendNameField.field.setVisible(false);
        this.friendSearchField.field.setVisible(false);
        this.nameProtectField.field.setVisible(false);
        this.nameProtectTimeField.field.setVisible(false);
        this.nameProtectMoneyField.field.setVisible(false);
        this.nameProtectKillsField.field.setVisible(false);
        this.nameProtectDeathsField.field.setVisible(false);
        this.spammerMessageField.field.setVisible(false);
        this.friendAddExpanded = false;
        this.friendAddX = 0;
        this.friendAddY = 0;
        this.friendAddW = 0;
        this.friendAddH = 0;
        this.blockSearchField.field.setVisible(showBlockSearch);
        this.xraySearchField.field.setVisible(showXraySearch);
        this.itemSearchField.field.setVisible(showItemSearch);
        this.gearBlacklistField.field.setVisible(showGearBlacklist);
        if (showSearch) {
            int searchY = innerTop;
            int minX = contentX + pad;
            int maxW = Math.max(20, innerRight - minX);
            int expandedW = Math.max(20, Math.min(maxW, 220));
            int collapsedW = Math.max(20, Math.min(maxW, 120));
            boolean expanded = this.searchField.field.isFocused() || !this.searchField.field.getText().isBlank();
            float expandT = this.updateSearchExpandAnim(expanded);
            int fieldW = Math.round(GuiScreen.lerp(collapsedW, expandedW, GuiScreen.easeOutCubic(expandT)));
            int searchX = innerRight - fieldW;
            this.searchField.setPos(searchX, searchY);
            this.searchField.setSize(fieldW, 14);
            return;
        }
        this.searchExpandAnim = 0.0f;
        if (showConfig) {
            int x2 = contentX + pad;
            int y2 = innerTop;
            int w2 = Math.max(20, innerRight - x2);
            this.configNameField.setPos(x2, y2);
            this.configNameField.setSize(w2, 16);
            return;
        }
        if (showFriend) {
            int fieldX;
            int x3 = contentX + pad;
            int y3 = innerTop;
            int maxW = Math.max(20, innerRight - x3);
            int expandedW = Math.max(20, Math.min(maxW, 220));
            int collapsedW = Math.max(20, Math.min(maxW, 64));
            boolean expanded = this.friendNameField.field.isFocused() || !this.friendNameField.field.getText().isBlank();
            float expandT = this.updateFriendAddExpandAnim(expanded);
            int fieldW = Math.round(GuiScreen.lerp(collapsedW, expandedW, GuiScreen.easeOutCubic(expandT)));
            this.friendAddX = fieldX = innerRight - fieldW;
            this.friendAddY = y3;
            this.friendAddW = fieldW;
            this.friendAddH = 16;
            this.friendAddExpanded = expanded;
            this.friendNameField.setPos(fieldX, y3);
            this.friendNameField.setSize(fieldW, 16);
            this.friendNameField.field.setVisible(expanded);
            int gap = 8;
            int searchW = fieldX - gap - x3;
            if (searchW < 40) {
                this.friendSearchField.field.setVisible(false);
                this.friendSearchField.field.setFocused(false);
            } else {
                this.friendSearchField.setPos(x3, y3);
                this.friendSearchField.setSize(searchW, 16);
                this.friendSearchField.field.setVisible(true);
            }
            return;
        }
        this.friendAddExpandAnim = 0.0f;
        if (showBlockSearch && (layout = this.blockPickerLayout(true)) != null && ((BlockPickerLayout)layout).searchY >= 0) {
            x = ((BlockPickerLayout)layout).x;
            y = ((BlockPickerLayout)layout).searchY;
            w = Math.max(20, ((BlockPickerLayout)layout).w);
            this.blockSearchField.setPos(x, y);
            this.blockSearchField.setSize(w, 16);
        }
        if (showXraySearch && (layout = this.blockPickerLayout(true)) != null && ((BlockPickerLayout)layout).searchY >= 0) {
            x = ((BlockPickerLayout)layout).x;
            y = ((BlockPickerLayout)layout).searchY;
            w = Math.max(20, ((BlockPickerLayout)layout).w);
            this.xraySearchField.setPos(x, y);
            this.xraySearchField.setSize(w, 16);
        }
        if (showItemSearch && (layout = this.blockPickerLayout(true)) != null && ((BlockPickerLayout)layout).searchY >= 0) {
            x = ((BlockPickerLayout)layout).x;
            y = ((BlockPickerLayout)layout).searchY;
            w = Math.max(20, ((BlockPickerLayout)layout).w);
            this.itemSearchField.setPos(x, y);
            this.itemSearchField.setSize(w, 16);
        }
        if (showGearBlacklist && (layout = this.blacklistLayout()) != null) {
            int gap = 8;
            int addW = this.blacklistAddWidth();
            int fieldW = Math.max(80, ((BlacklistLayout)layout).inputW - addW - gap);
            int fieldX = ((BlacklistLayout)layout).inputX;
            int fieldY = ((BlacklistLayout)layout).inputY;
            this.gearBlacklistField.setPos(fieldX, fieldY);
            this.gearBlacklistField.setSize(fieldW, ((BlacklistLayout)layout).inputH);
        }
    }

    private void clearFieldFocus() {
        UiTextField.clearAllFocus();
    }

    private UiTextField displayNameField(String friend) {
        String key = GuiScreen.normalizeName(friend);
        UiTextField field = this.displayNameFields.get(key);
        if (field == null) {
            field = new UiTextField(0, 0, 200, 16, "");
            this.displayNameFields.put(key, field);
            this.addSelectableChild(field.field);
        }
        return field;
    }

    private static String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().toLowerCase();
    }

    private boolean isBlockPickerOpen() {
        return this.blockPickerOpen || this.xrayPickerOpen || this.itemPickerOpen;
    }

    private boolean isOverlayOpen() {
        return this.overlayAnim > 0.01f || this.isBlockPickerOpen() || this.displayNamesOpen || this.gearBlacklistOpen;
    }

    private OverlayType resolveOverlayType() {
        if (this.displayNamesOpen) {
            return OverlayType.DISPLAY_NAMES;
        }
        if (this.gearBlacklistOpen) {
            return OverlayType.GEAR_BLACKLIST;
        }
        if (this.isBlockPickerOpen()) {
            return OverlayType.BLOCK_PICKER;
        }
        return OverlayType.NONE;
    }

    private void closeBlockPicker() {
        if (this.blockPickerOpen) {
            this.blockPickerOpen = false;
            this.blockSearchField.field.setFocused(false);
            this.blockListScroll = 0.0f;
            this.blockSelectedScroll = 0.0f;
            this.blockListScrollTarget = 0.0f;
            this.blockSelectedScrollTarget = 0.0f;
        }
        if (this.xrayPickerOpen) {
            this.xrayPickerOpen = false;
            this.xraySearchField.field.setFocused(false);
            this.xrayListScroll = 0.0f;
            this.xraySelectedScroll = 0.0f;
            this.xrayListScrollTarget = 0.0f;
            this.xraySelectedScrollTarget = 0.0f;
        }
        if (this.itemPickerOpen) {
            this.itemPickerOpen = false;
            this.itemSearchField.field.setFocused(false);
            this.itemListScroll = 0.0f;
            this.itemSelectedScroll = 0.0f;
            this.itemListScrollTarget = 0.0f;
            this.itemSelectedScrollTarget = 0.0f;
        }
        this.pickerShowAdded = false;
    }

    private void openDisplayNamesOverlay() {
        this.closeBlockPicker();
        this.closeGearBlacklistOverlay();
        this.displayNamesOpen = true;
        this.displayNamesScroll = 0.0f;
        this.displayNamesScrollTarget = 0.0f;
        this.clearFieldFocus();
    }

    private void closeDisplayNamesOverlay() {
        if (!this.displayNamesOpen) {
            return;
        }
        this.displayNamesOpen = false;
        this.displayNamesScroll = 0.0f;
        this.displayNamesScrollTarget = 0.0f;
        for (UiTextField field : this.displayNameFields.values()) {
            field.field.setFocused(false);
            field.field.setVisible(false);
        }
    }

    private void openGearBlacklistOverlay() {
        this.closeBlockPicker();
        this.closeDisplayNamesOverlay();
        this.gearBlacklistOpen = true;
        this.gearBlacklistScroll = 0.0f;
        this.gearBlacklistScrollTarget = 0.0f;
        this.clearFieldFocus();
        this.gearBlacklistField.requestFocus();
    }

    private void closeGearBlacklistOverlay() {
        if (!this.gearBlacklistOpen) {
            return;
        }
        this.gearBlacklistOpen = false;
        this.gearBlacklistScroll = 0.0f;
        this.gearBlacklistScrollTarget = 0.0f;
        this.gearBlacklistField.field.setFocused(false);
        this.gearBlacklistField.field.setVisible(false);
        this.gearBlacklistField.field.setText("");
    }

    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        long now = System.currentTimeMillis();
        if (this.closing && now - this.closeStartMs >= 360L) {
            this.finishClose();
            return;
        }
        int accent = Theme.accentColor(now);
        float openT = this.openAnim();
        this.renderGuiBackground(ctx, delta);
        float[] sc = RenderSystem.getShaderColor();
        float prevR = sc[0];
        float prevG = sc[1];
        float prevB = sc[2];
        float prevA = sc[3];
        if (prevA < 0.01f) {
            prevR = 1.0f;
            prevG = 1.0f;
            prevB = 1.0f;
            prevA = 1.0f;
        }
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)openT);
        Rounded.setGlobalAlpha(openT);
        OverlayType targetOverlay = this.resolveOverlayType();
        float overlayTarget = targetOverlay == OverlayType.NONE ? 0.0f : 1.0f;
        this.overlayAnim = GuiScreen.lerp(this.overlayAnim, overlayTarget, 0.22f);
        if (Math.abs(this.overlayAnim - overlayTarget) < 0.001f) {
            this.overlayAnim = overlayTarget;
        }
        if (targetOverlay != OverlayType.NONE) {
            this.overlayType = targetOverlay;
        } else if (this.overlayAnim <= 0.01f) {
            this.overlayType = OverlayType.NONE;
        }
        if (this.overlayAnim > 0.01f) {
            int smx = mouseX;
            int smy = mouseY;
            this.layoutFields(smx, smy);
            float scale = 0.97f + 0.03f * this.overlayAnim;
            ctx.getMatrices().push();
            ctx.getMatrices().translate((float)this.width / 2.0f, (float)this.height / 2.0f, 0.0f);
            ctx.getMatrices().scale(scale, scale, 1.0f);
            ctx.getMatrices().translate((float)(-this.width) / 2.0f, (float)(-this.height) / 2.0f, 0.0f);
            this.withAlpha(this.overlayAnim, () -> {
                if (this.overlayType == OverlayType.DISPLAY_NAMES) {
                    this.renderDisplayNamesOverlay(ctx, smx, smy, delta, accent);
                } else if (this.overlayType == OverlayType.GEAR_BLACKLIST) {
                    this.renderGearBlacklistOverlay(ctx, smx, smy, delta, accent);
                } else {
                    this.renderBlockPickerOverlay(ctx, smx, smy, delta, accent);
                }
            });
            ctx.getMatrices().pop();
            Rounded.setGlobalAlpha(1.0f);
            RenderSystem.setShaderColor((float)prevR, (float)prevG, (float)prevB, (float)prevA);
            super.render(ctx, mouseX, mouseY, delta);
            return;
        }
        float s = this.finalScale();
        int cx = this.panelX + this.panelW / 2;
        int cy = this.panelY + this.panelH / 2;
        int smx = this.scaledMouseX(mouseX);
        int smy = this.scaledMouseY(mouseY);
        float tabSlide = this.currentTabSlide(now);
        int smxContent = (int)((float)smx - tabSlide);
        int smyContent = smy;
        ctx.getMatrices().push();
        ctx.getMatrices().translate((float)cx, (float)cy, 0.0f);
        ctx.getMatrices().scale(s, s, 1.0f);
        ctx.getMatrices().translate((float)(-cx), (float)(-cy), 0.0f);
        this.layoutFields(smxContent, smyContent);
        if (this.tab != Tab.FRIENDS) {
            for (UiTextField field : this.displayNameFields.values()) {
                field.field.setVisible(false);
            }
        }
        float alphaF = GuiScreen.clamp(GuiClient.CONFIG.transparency, 0.18f, 1.0f);
        int panelA = (int)(alphaF * 240.0f * openT);
        float viewT = this.updateModulesViewAnim();
        int panelBg = Theme.argb(panelA, 14, 14, 18);
        int sbBg = Theme.argb((int)((float)panelA * 0.92f), 9, 9, 12);
        Rounded.rect(ctx, this.panelX, this.panelY, this.panelW, this.panelH, 18.0f, panelBg);
        this.renderPanelGradient(ctx, this.panelX, this.panelY, this.panelW, this.panelH, 18.0f, accent, openT);
        Rounded.rect(ctx, this.panelX, this.panelY, 96, this.panelH, 18.0f, sbBg);
        Rounded.outline(ctx, this.panelX, this.panelY, this.panelW, this.panelH, 18.0f, 1, Theme.withAlpha(accent, (int)(110.0f * openT)));
        TextRenderer tr = GuiFonts.textRenderer();
        String title = "FastClient";
        float titleScale = 1.15f;
        int titlePad = (int)Math.ceil(18.0) + 4;
        ctx.getMatrices().push();
        ctx.getMatrices().translate((float)(this.panelX + titlePad), (float)(this.panelY + titlePad), 0.0f);
        ctx.getMatrices().scale(titleScale, titleScale, 1.0f);
        ctx.drawText(tr, (Text)Text.literal((String)title), 0, 0, Theme.withAlpha(accent, 220), false);
        ctx.getMatrices().pop();
        int tabY = this.panelY + 38;
        int tabPad = (int)Math.ceil(10.0) + 2;
        int tabX = this.panelX + 10;
        int tabW = 76;
        int tabStep = 30;
        float targetTabY = tabY + this.tabIndex(this.tab) * tabStep;
        if (this.tabHighlightY < 0.0f) {
            this.tabHighlightY = targetTabY;
        }
        this.tabHighlightY = GuiScreen.lerp(this.tabHighlightY, targetTabY, 0.22f);
        int hiY = Math.round(this.tabHighlightY);
        int hiBg = Theme.withAlpha(accent, (int)(38.0f * openT));
        Rounded.rect(ctx, tabX, hiY, tabW, 22, 10.0f, hiBg);
        this.renderTabUnderline(ctx, tabX, hiY, tabW, 22, accent, openT);
        for (Tab tb : Tab.values()) {
            int bg;
            int x = this.panelX + 10;
            int y = tabY;
            int w = 76;
            int h = 22;
            boolean sel = tb == this.tab;
            boolean hover = GuiScreen.inside(smx, smy, x, y, w, h);
            int n = bg = hover ? Theme.argb(40, 30, 30, 38) : Theme.argb(0, 0, 0, 0);
            if (!sel) {
                Rounded.rect(ctx, x, y, w, h, 10.0f, bg);
            }
            int col = sel ? -1 : -4605498;
            Objects.requireNonNull(tr);
            int ty = y + (h - 9) / 2;
            ctx.drawText(tr, (Text)Text.literal((String)tb.label), x + tabPad, ty, col, false);
            tabY += h + 8;
        }
        ctx.getMatrices().push();
        ctx.getMatrices().translate(tabSlide, 0.0f, 0.0f);
        float tabFade = this.currentTabFade(now);
        this.withAlpha(tabFade, () -> {
            this.searchField.render(ctx, smxContent, smyContent, delta);
            this.configNameField.render(ctx, smxContent, smyContent, delta);
            this.friendNameField.render(ctx, smxContent, smyContent, delta);
            this.friendSearchField.render(ctx, smxContent, smyContent, delta);
            this.blockSearchField.render(ctx, smxContent, smyContent, delta);
            this.xraySearchField.render(ctx, smxContent, smyContent, delta);
            if (this.tab != Tab.MODULES || this.modulesView != ModulesView.LIST) {
                this.hoverModuleId = null;
                this.hoverStartMs = 0L;
            }
            if (this.tab == Tab.MODULES) {
                float viewSlide = this.modulesView == ModulesView.LIST ? -22.0f * viewT : 22.0f * (1.0f - viewT);
                int smxView = (int)((float)smxContent - viewSlide);
                ctx.getMatrices().push();
                ctx.getMatrices().translate(viewSlide, 0.0f, 0.0f);
                if (this.modulesView == ModulesView.LIST) {
                    this.renderModulesList(ctx, smxView, smyContent, delta, accent, 14.0f);
                } else {
                    this.renderModuleSettingsPage(ctx, smxView, smyContent, accent, 14.0f, 10.0f, delta);
                }
                ctx.getMatrices().pop();
            } else if (this.tab == Tab.CONFIGS) {
                this.renderConfigs(ctx, smxContent, smyContent, delta, accent, 14.0f);
            } else if (this.tab == Tab.FRIENDS) {
                this.renderFriends(ctx, smxContent, smyContent, delta, accent, 14.0f);
            } else if (this.tab == Tab.SETTINGS) {
                this.renderSettings(ctx, smxContent, smyContent, delta, accent, 14.0f, 10.0f);
            } else {
                this.renderGearRender(ctx, smxContent, smyContent, delta, accent, 14.0f, 10.0f);
            }
        });
        ctx.getMatrices().pop();
        ctx.getMatrices().pop();
        this.renderGuiHints(ctx, accent, openT);
        Rounded.setGlobalAlpha(1.0f);
        RenderSystem.setShaderColor((float)prevR, (float)prevG, (float)prevB, (float)prevA);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderGuiBackground(DrawContext ctx, float delta) {
        int blur;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        if (client.world == null) {
            this.renderPanoramaBackground(ctx, delta);
        }
        if ((blur = client.options.getMenuBackgroundBlurrinessValue()) > 0) {
            client.gameRenderer.renderBlur();
            client.getFramebuffer().beginWrite(false);
        }
        this.renderDarkening(ctx);
    }

    private void renderGuiHints(DrawContext ctx, int accent, float openT) {
        int pad;
        if (openT <= 0.0f) {
            return;
        }
        TextRenderer tr = GuiFonts.textRenderer();
        float scale = 0.8f;
        Objects.requireNonNull(tr);
        int lineH = 9 + 2;
        int lines = 4;
        int extra = 1;
        int x = pad = 6;
        int y = (int)((float)(this.height - pad) - (float)(lineH * (lines + extra)) * scale);
        int col = Theme.withAlpha(accent, (int)(220.0f * openT));
        ctx.getMatrices().push();
        ctx.getMatrices().translate((float)x, (float)y, 0.0f);
        ctx.getMatrices().scale(scale, scale, 1.0f);
        ctx.drawText(tr, (Text)Text.literal((String)"!=wykrywalne"), 0, 0, col, false);
        ctx.drawText(tr, (Text)Text.literal((String)"LMB to turn on"), 0, lineH, col, false);
        ctx.drawText(tr, (Text)Text.literal((String)"RMB to see settings"), 0, lineH * 2, col, false);
        ctx.drawText(tr, (Text)Text.literal((String)"Scroll to bind module"), 0, lineH * 3, col, false);
        ctx.drawText(tr, (Text)Text.literal((String)"ESC/BKSP to clear bind"), 0, lineH * 4, col, false);
        ctx.getMatrices().pop();
    }

    private void renderBlockPickerOverlay(DrawContext ctx, int mouseX, int mouseY, float delta, int accent) {
        List<BlockEntry> entries;
        BlockPickerLayout layout = this.blockPickerLayout(true);
        if (layout == null) {
            return;
        }
        boolean xray = this.xrayPickerOpen;
        boolean item = this.itemPickerOpen;
        TextRenderer tr = GuiFonts.textRenderer();
        int panelBg = Theme.argb(210, 14, 14, 18);
        int headerBg = Theme.argb(150, 20, 20, 26);
        int pad = (int)Math.ceil(10.0) + 2;
        Rounded.rect(ctx, layout.panelX, layout.panelY, layout.panelW, layout.panelH, 18.0f, panelBg);
        Rounded.outline(ctx, layout.panelX, layout.panelY, layout.panelW, layout.panelH, 18.0f, 1, Theme.withAlpha(accent, 180));
        int headerX = layout.panelX + 6;
        int headerW = layout.panelW - 12;
        Rounded.rect(ctx, headerX, layout.headerY, headerW, layout.headerH, 10.0f, headerBg);
        int n = layout.headerY;
        int n2 = layout.headerH;
        Objects.requireNonNull(tr);
        int headerTy = n + (n2 - 9) / 2;
        String title = item ? "Item ESP Items" : (xray ? "Xray Blocks" : "Block ESP Blocks");
        ctx.drawText(tr, (Text)Text.literal((String)title), headerX + pad, headerTy, -1, false);
        String close = "CLOSE";
        int closeW = tr.getWidth(close);
        int closeX = headerX + headerW - pad - closeW;
        boolean closeHover = GuiScreen.inside(mouseX, mouseY, closeX - 4, layout.headerY, closeW + 8, layout.headerH);
        int closeCol = closeHover ? Theme.withAlpha(accent, 255) : -4605498;
        ctx.drawText(tr, (Text)Text.literal((String)close), closeX, headerTy, closeCol, false);
        String allLabel = "ALL";
        String addedLabel = "ADDED";
        int toggleH = Math.max(10, layout.headerH - 4);
        int toggleY = layout.headerY + (layout.headerH - toggleH) / 2;
        int allW = tr.getWidth(allLabel) + 10;
        int addedW = tr.getWidth(addedLabel) + 10;
        int addedX = closeX - 8 - addedW;
        int allX = addedX - 6 - allW;
        boolean showingAdded = this.pickerShowAdded;
        int allBg = !showingAdded ? Theme.withAlpha(accent, 55) : Theme.argb(120, 20, 20, 26);
        int addedBg = showingAdded ? Theme.withAlpha(accent, 55) : Theme.argb(120, 20, 20, 26);
        Rounded.rect(ctx, allX, toggleY, allW, toggleH, 10.0f, allBg);
        Rounded.rect(ctx, addedX, toggleY, addedW, toggleH, 10.0f, addedBg);
        int allTextCol = !showingAdded ? -1 : -4605498;
        int addedTextCol = showingAdded ? -1 : -4605498;
        Objects.requireNonNull(tr);
        int toggleTextY = toggleY + (toggleH - 9) / 2;
        ctx.drawText(tr, (Text)Text.literal((String)allLabel), allX + (allW - tr.getWidth(allLabel)) / 2, toggleTextY, allTextCol, false);
        ctx.drawText(tr, (Text)Text.literal((String)addedLabel), addedX + (addedW - tr.getWidth(addedLabel)) / 2, toggleTextY, addedTextCol, false);
        if (item) {
            this.itemSearchField.render(ctx, mouseX, mouseY, delta);
        } else if (xray) {
            this.xraySearchField.render(ctx, mouseX, mouseY, delta);
        } else {
            this.blockSearchField.render(ctx, mouseX, mouseY, delta);
        }
        String query = xray ? this.xraySearchField.field.getText().trim().toLowerCase() : this.blockSearchField.field.getText().trim().toLowerCase();
        String string = item ? this.itemSearchField.field.getText().trim().toLowerCase() : query;
        if (showingAdded) {
            List<BlockEntry> selectedEntries = item ? this.selectedItemEntries() : (xray ? this.selectedXrayEntries() : this.selectedBlockEntries());
            entries = this.filterEntriesByQuery(selectedEntries, query);
        } else {
            entries = item ? this.availableItemEntries(query) : this.availableBlockEntries(xray, query);
        }
        int rowH = 24;
        int rowGap = 8;
        int rowPad = (int)Math.ceil(14.0) + 4;
        int iconSize = Math.min(16, rowH - 6);
        if (layout.listH > 0) {
            float listScroll;
            ctx.enableScissor(layout.x, layout.listTop, layout.x + layout.w, layout.listTop + layout.listH);
            int totalH = entries.size() * (rowH + rowGap);
            float maxListScroll = Math.max(0.0f, (float)(totalH - layout.listH));
            if (showingAdded) {
                if (item) {
                    this.itemSelectedScrollTarget = GuiScreen.clamp(this.itemSelectedScrollTarget, 0.0f, maxListScroll);
                    this.itemSelectedScroll = GuiScreen.lerp(this.itemSelectedScroll, this.itemSelectedScrollTarget, 0.25f);
                    if (Math.abs(this.itemSelectedScroll - this.itemSelectedScrollTarget) < 0.01f) {
                        this.itemSelectedScroll = this.itemSelectedScrollTarget;
                    }
                    listScroll = this.itemSelectedScroll;
                } else if (xray) {
                    this.xraySelectedScrollTarget = GuiScreen.clamp(this.xraySelectedScrollTarget, 0.0f, maxListScroll);
                    this.xraySelectedScroll = GuiScreen.lerp(this.xraySelectedScroll, this.xraySelectedScrollTarget, 0.25f);
                    if (Math.abs(this.xraySelectedScroll - this.xraySelectedScrollTarget) < 0.01f) {
                        this.xraySelectedScroll = this.xraySelectedScrollTarget;
                    }
                    listScroll = this.xraySelectedScroll;
                } else {
                    this.blockSelectedScrollTarget = GuiScreen.clamp(this.blockSelectedScrollTarget, 0.0f, maxListScroll);
                    this.blockSelectedScroll = GuiScreen.lerp(this.blockSelectedScroll, this.blockSelectedScrollTarget, 0.25f);
                    if (Math.abs(this.blockSelectedScroll - this.blockSelectedScrollTarget) < 0.01f) {
                        this.blockSelectedScroll = this.blockSelectedScrollTarget;
                    }
                    listScroll = this.blockSelectedScroll;
                }
            } else if (item) {
                this.itemListScrollTarget = GuiScreen.clamp(this.itemListScrollTarget, 0.0f, maxListScroll);
                this.itemListScroll = GuiScreen.lerp(this.itemListScroll, this.itemListScrollTarget, 0.25f);
                if (Math.abs(this.itemListScroll - this.itemListScrollTarget) < 0.01f) {
                    this.itemListScroll = this.itemListScrollTarget;
                }
                listScroll = this.itemListScroll;
            } else if (xray) {
                this.xrayListScrollTarget = GuiScreen.clamp(this.xrayListScrollTarget, 0.0f, maxListScroll);
                this.xrayListScroll = GuiScreen.lerp(this.xrayListScroll, this.xrayListScrollTarget, 0.25f);
                if (Math.abs(this.xrayListScroll - this.xrayListScrollTarget) < 0.01f) {
                    this.xrayListScroll = this.xrayListScrollTarget;
                }
                listScroll = this.xrayListScroll;
            } else {
                this.blockListScrollTarget = GuiScreen.clamp(this.blockListScrollTarget, 0.0f, maxListScroll);
                this.blockListScroll = GuiScreen.lerp(this.blockListScroll, this.blockListScrollTarget, 0.25f);
                if (Math.abs(this.blockListScroll - this.blockListScrollTarget) < 0.01f) {
                    this.blockListScroll = this.blockListScrollTarget;
                }
                listScroll = this.blockListScroll;
            }
            int yy = layout.listTop - (int)listScroll;
            for (BlockEntry e : entries) {
                int bg;
                boolean hover = GuiScreen.inside(mouseX, mouseY, layout.x, yy, layout.w, rowH);
                int n3 = bg = showingAdded ? Theme.withAlpha(accent, 40) : Theme.argb(150, 20, 20, 26);
                if (hover) {
                    bg = showingAdded ? Theme.withAlpha(accent, 60) : Theme.argb(180, 22, 22, 30);
                }
                Rounded.rect(ctx, layout.x, yy, layout.w, rowH, 14.0f, bg);
                Objects.requireNonNull(tr);
                int textY = yy + (rowH - 9) / 2;
                int iconX = layout.x + rowPad;
                int iconY = yy + (rowH - iconSize) / 2;
                if (!e.stack.isEmpty()) {
                    ctx.drawItemWithoutEntity(e.stack, iconX, iconY);
                }
                int textX = iconX + iconSize + 6;
                ctx.drawText(tr, (Text)Text.literal((String)e.name), textX, textY, -1, false);
                String action = showingAdded ? "Remove" : "Add";
                int actionW = tr.getWidth(action);
                int actionCol = showingAdded ? -38037 : Theme.withAlpha(accent, 255);
                ctx.drawText(tr, (Text)Text.literal((String)action), layout.x + layout.w - rowPad - actionW, textY, actionCol, false);
                yy += rowH + rowGap;
            }
            if (entries.isEmpty()) {
                String empty = showingAdded ? "No added blocks" : "No results";
                int ew = tr.getWidth(empty);
                int ex = layout.x + (layout.w - ew) / 2;
                int n4 = layout.listTop;
                int n5 = layout.listH;
                Objects.requireNonNull(tr);
                int ey = n4 + (n5 - 9) / 2;
                ctx.drawText(tr, (Text)Text.literal((String)empty), ex, ey, -6643544, false);
            }
            ctx.disableScissor();
        }
    }

    private void renderDisplayNamesOverlay(DrawContext ctx, int mouseX, int mouseY, float delta, int accent) {
        DisplayNamesLayout layout = this.displayNamesLayout();
        if (layout == null) {
            return;
        }
        TextRenderer tr = GuiFonts.textRenderer();
        int panelBg = Theme.argb(210, 14, 14, 18);
        int headerBg = Theme.argb(150, 20, 20, 26);
        int pad = (int)Math.ceil(14.0) + 4;
        Rounded.rect(ctx, layout.panelX, layout.panelY, layout.panelW, layout.panelH, 18.0f, panelBg);
        Rounded.outline(ctx, layout.panelX, layout.panelY, layout.panelW, layout.panelH, 18.0f, 1, Theme.withAlpha(accent, 180));
        Rounded.rect(ctx, layout.headerX, layout.headerY, layout.headerW, layout.headerH, 10.0f, headerBg);
        int n = layout.headerY;
        int n2 = layout.headerH;
        Objects.requireNonNull(tr);
        int headerTy = n + (n2 - 9) / 2;
        ctx.drawText(tr, (Text)Text.literal((String)"Display Names"), layout.headerX + pad, headerTy, -1, false);
        String close = "CLOSE";
        int closeW = tr.getWidth(close);
        int closeX = layout.headerX + layout.headerW - pad - closeW;
        boolean closeHover = GuiScreen.inside(mouseX, mouseY, closeX - 4, layout.headerY, closeW + 8, layout.headerH);
        int closeCol = closeHover ? Theme.withAlpha(accent, 255) : -4605498;
        ctx.drawText(tr, (Text)Text.literal((String)close), closeX, headerTy, closeCol, false);
        Rounded.rect(ctx, layout.leftX, layout.columnHeaderY, layout.colW, layout.columnHeaderH, 10.0f, headerBg);
        Rounded.rect(ctx, layout.rightX, layout.columnHeaderY, layout.colW, layout.columnHeaderH, 10.0f, headerBg);
        int n3 = layout.columnHeaderY;
        int n4 = layout.columnHeaderH;
        Objects.requireNonNull(tr);
        int colTy = n3 + Math.round((float)(n4 - 9) / 2.0f);
        ctx.drawText(tr, (Text)Text.literal((String)"friends"), layout.leftX + pad, colTy, -1, false);
        ctx.drawText(tr, (Text)Text.literal((String)"Display Names"), layout.rightX + pad, colTy, -1, false);
        List<String> friends = GuiClient.FRIENDS.list();
        int rowH = 24;
        int rowGap = 8;
        int rowPad = pad;
        int totalH = friends.size() * (rowH + rowGap);
        float maxScroll = Math.max(0.0f, (float)(totalH - layout.listH));
        this.displayNamesScrollTarget = GuiScreen.clamp(this.displayNamesScrollTarget, 0.0f, maxScroll);
        this.displayNamesScroll = GuiScreen.lerp(this.displayNamesScroll, this.displayNamesScrollTarget, 0.25f);
        if (Math.abs(this.displayNamesScroll - this.displayNamesScrollTarget) < 0.01f) {
            this.displayNamesScroll = this.displayNamesScrollTarget;
        }
        for (UiTextField uiTextField : this.displayNameFields.values()) {
            uiTextField.field.setVisible(false);
        }
        ctx.enableScissor(layout.leftX, layout.listTop, layout.rightX + layout.colW, layout.listTop + layout.listH);
        int yy = layout.listTop - (int)this.displayNamesScroll;
        for (String f : friends) {
            Rounded.rect(ctx, layout.leftX, yy, layout.colW, rowH, 14.0f, Theme.argb(150, 20, 20, 26));
            Objects.requireNonNull(tr);
            int textY = yy + (rowH - 9) / 2;
            ctx.drawText(tr, (Text)Text.literal((String)f), layout.leftX + rowPad, textY, -1, false);
            UiTextField field = this.displayNameField(f);
            field.setPos(layout.rightX, yy);
            field.setSize(layout.colW, rowH);
            field.field.setVisible(true);
            if (!field.field.isFocused()) {
                String desired = GuiClient.FRIENDS.getDisplayName(f);
                if (!field.field.getText().equals(desired)) {
                    field.field.setText(desired);
                }
            }
            field.render(ctx, mouseX, mouseY, delta);
            GuiClient.FRIENDS.setDisplayName(f, field.field.getText());
            yy += rowH + rowGap;
        }
        if (friends.isEmpty()) {
            String string = "No friends";
            int ew = tr.getWidth(string);
            int ex = layout.leftX + (layout.colW * 2 + layout.colGap - ew) / 2;
            int n5 = layout.listTop;
            int n6 = layout.listH;
            Objects.requireNonNull(tr);
            int ey = n5 + (n6 - 9) / 2;
            ctx.drawText(tr, (Text)Text.literal((String)string), ex, ey, -6643544, false);
        }
        ctx.disableScissor();
    }

    private void renderGearBlacklistOverlay(DrawContext ctx, int mouseX, int mouseY, float delta, int accent) {
        BlacklistLayout layout = this.blacklistLayout();
        if (layout == null) {
            return;
        }
        TextRenderer tr = GuiFonts.textRenderer();
        int panelBg = Theme.argb(210, 14, 14, 18);
        int headerBg = Theme.argb(150, 20, 20, 26);
        int pad = (int)Math.ceil(14.0) + 4;
        Rounded.rect(ctx, layout.panelX, layout.panelY, layout.panelW, layout.panelH, 18.0f, panelBg);
        Rounded.outline(ctx, layout.panelX, layout.panelY, layout.panelW, layout.panelH, 18.0f, 1, Theme.withAlpha(accent, 180));
        Rounded.rect(ctx, layout.headerX, layout.headerY, layout.headerW, layout.headerH, 10.0f, headerBg);
        int n = layout.headerY;
        int n2 = layout.headerH;
        Objects.requireNonNull(tr);
        int headerTy = n + (n2 - 9) / 2;
        ctx.drawText(tr, (Text)Text.literal((String)"Auto TP Blacklist"), layout.headerX + pad, headerTy, -1, false);
        String close = "CLOSE";
        int closeW = tr.getWidth(close);
        int closeX = layout.headerX + layout.headerW - pad - closeW;
        boolean closeHover = GuiScreen.inside(mouseX, mouseY, closeX - 4, layout.headerY, closeW + 8, layout.headerH);
        int closeCol = closeHover ? Theme.withAlpha(accent, 255) : -4605498;
        ctx.drawText(tr, (Text)Text.literal((String)close), closeX, headerTy, closeCol, false);
        int gap = 8;
        int addW = this.blacklistAddWidth();
        int fieldW = Math.max(80, layout.inputW - addW - gap);
        int addX = layout.inputX + fieldW + gap;
        int addY = layout.inputY;
        this.gearBlacklistField.render(ctx, mouseX, mouseY, delta);
        this.drawGearButton(ctx, addX, addY, addW, layout.inputH, "ADD", accent, mouseX, mouseY);
        List<String> entries = this.gearBlacklistEntries();
        int rowH = 24;
        int rowGap = 8;
        int rowPad = pad;
        int totalH = entries.size() * (rowH + rowGap);
        float maxScroll = Math.max(0.0f, (float)(totalH - layout.listH));
        this.gearBlacklistScrollTarget = GuiScreen.clamp(this.gearBlacklistScrollTarget, 0.0f, maxScroll);
        this.gearBlacklistScroll = GuiScreen.lerp(this.gearBlacklistScroll, this.gearBlacklistScrollTarget, 0.25f);
        if (Math.abs(this.gearBlacklistScroll - this.gearBlacklistScrollTarget) < 0.01f) {
            this.gearBlacklistScroll = this.gearBlacklistScrollTarget;
        }
        ctx.enableScissor(layout.listX, layout.listY, layout.listX + layout.listW, layout.listY + layout.listH);
        int yy = layout.listY - (int)this.gearBlacklistScroll;
        for (String name : entries) {
            boolean hover = GuiScreen.inside(mouseX, mouseY, layout.listX, yy, layout.listW, rowH);
            int bg = Theme.argb(150, 20, 20, 26);
            if (hover) {
                bg = Theme.argb(180, 22, 22, 30);
            }
            Rounded.rect(ctx, layout.listX, yy, layout.listW, rowH, 14.0f, bg);
            Objects.requireNonNull(tr);
            int textY = yy + (rowH - 9) / 2;
            ctx.drawText(tr, (Text)Text.literal((String)name), layout.listX + rowPad, textY, -1, false);
            String action = "Remove";
            int actionW = tr.getWidth(action);
            ctx.drawText(tr, (Text)Text.literal((String)action), layout.listX + layout.listW - rowPad - actionW, textY, -38037, false);
            yy += rowH + rowGap;
        }
        if (entries.isEmpty()) {
            String empty = "Lista pusta";
            int ew = tr.getWidth(empty);
            int ex = layout.listX + (layout.listW - ew) / 2;
            int n3 = layout.listY;
            int n4 = layout.listH;
            Objects.requireNonNull(tr);
            int ey = n3 + (n4 - 9) / 2;
            ctx.drawText(tr, (Text)Text.literal((String)empty), ex, ey, -6643544, false);
        }
        ctx.disableScissor();
    }

    private List<HackModule> filteredModulesForTab(String query) {
        ArrayList<HackModule> out = new ArrayList<HackModule>();
        List<HackModule> all = GuiClient.MODULES.all();
        for (HackModule m : all) {
            String name;
            if (m.id.hidden) continue;
            String string = name = m.id.display == null ? "" : m.id.display;
            if (!query.isEmpty() && !name.toLowerCase().contains(query)) continue;
            out.add(m);
        }
        return out;
    }

    private void renderModulesList(DrawContext ctx, int mouseX, int mouseY, float delta, int accent, float R_TILE) {
        int contentX = this.panelX + 96;
        int areaX = contentX + 12;
        int areaY = this.panelY + 44 + 10;
        int areaW = this.panelW - 96 - 24;
        int areaH = this.panelH - 44 - 16;
        ctx.enableScissor(areaX, areaY, areaX + areaW, areaY + areaH);
        String q = this.searchField.field.isVisible() ? this.searchField.field.getText().trim().toLowerCase() : "";
        List<HackModule> filtered = this.filteredModulesForTab(q);
        int gap = 8;
        int tileW = (areaW - gap) / 2;
        int tileH = 30;
        int rows = (int)Math.ceil((double)filtered.size() / 2.0);
        int totalH = rows * (tileH + gap);
        float maxScroll = Math.max(0.0f, (float)(totalH - areaH));
        this.modulesScrollTarget = GuiScreen.clamp(this.modulesScrollTarget, 0.0f, maxScroll);
        this.modulesScroll = GuiScreen.lerp(this.modulesScroll, this.modulesScrollTarget, 0.25f);
        if (Math.abs(this.modulesScroll - this.modulesScrollTarget) < 0.01f) {
            this.modulesScroll = this.modulesScrollTarget;
        }
        int startY = areaY - (int)this.modulesScroll;
        TextRenderer tr = GuiFonts.textRenderer();
        int tileBg = Theme.argb(120, 20, 20, 26);
        int tileHoverBg = Theme.argb(170, 22, 22, 30);
        int tilePad = (int)Math.ceil(R_TILE) + 4;
        HackModule hovered = null;
        for (int i = 0; i < filtered.size(); ++i) {
            HackModule m = filtered.get(i);
            int col = i % 2;
            int row = i / 2;
            int x = areaX + col * (tileW + gap);
            int y = startY + row * (tileH + gap);
            boolean hover = GuiScreen.inside(mouseX, mouseY, x, y, tileW, tileH);
            float hoverT = this.animateMap(this.moduleHoverAnim, m.id, hover ? 1.0f : 0.0f, 0.22f);
            float enableT = this.animateMap(this.moduleEnableAnim, m.id, m.isEnabled() ? 1.0f : 0.0f, 0.2f);
            int bg = GuiScreen.lerpColor(tileBg, tileHoverBg, hoverT);
            if (hover) {
                hovered = m;
            }
            float scale = 1.0f + 0.02f * hoverT;
            ctx.getMatrices().push();
            ctx.getMatrices().translate((float)x + (float)tileW / 2.0f, (float)y + (float)tileH / 2.0f, 0.0f);
            ctx.getMatrices().scale(scale, scale, 1.0f);
            ctx.getMatrices().translate(-((float)x + (float)tileW / 2.0f), -((float)y + (float)tileH / 2.0f), 0.0f);
            Rounded.rect(ctx, x, y, tileW, tileH, R_TILE, bg);
            if (hoverT > 0.01f) {
                int outline = Theme.withAlpha(accent, (int)(60.0f + 90.0f * hoverT));
                Rounded.outline(ctx, x, y, tileW, tileH, R_TILE, 1, outline);
            }
            if (enableT > 0.01f) {
                int lineW = 2;
                int lineH = Math.max(4, Math.round((float)(tileH - 10) * (0.3f + 0.7f * enableT)));
                int lineX = x + tileW - (int)Math.ceil(R_TILE) - lineW - 6;
                int lineY = y + (tileH - lineH) / 2;
                int lineCol = Theme.withAlpha(accent, (int)(80.0f + 140.0f * enableT));
                Rounded.rect(ctx, lineX, lineY, lineW, lineH, (float)lineW * 0.5f, lineCol);
            }
            String label = m.id.display;
            Objects.requireNonNull(tr);
            int textY = y + (tileH - 9) / 2;
            ctx.drawText(tr, (Text)Text.literal((String)label), x + tilePad, textY, -1, false);
            String bindLine = GuiScreen.formatKeyShort(m.getBindKey());
            if (m.getBindKey() > 0 && !bindLine.isBlank()) {
                Objects.requireNonNull(tr);
                int keySize = Math.min(tileH - 6, 9 + 4);
                Objects.requireNonNull(tr);
                if (keySize < 9 + 2) {
                    Objects.requireNonNull(tr);
                    keySize = 9 + 2;
                }
                int bindRightPad = Math.max(tilePad, (int)Math.ceil(R_TILE) + 12);
                int keyX = x + tileW - bindRightPad - keySize;
                int keyY = y + (tileH - keySize) / 2;
                float keyR = (float)keySize * 0.5f;
                int outer = Theme.argb(30, 40, 40, 48);
                int inner = Theme.argb(50, 22, 22, 28);
                Rounded.rect(ctx, keyX, keyY, keySize, keySize, keyR, outer);
                if (keySize > 2) {
                    Rounded.rect(ctx, keyX + 1, keyY + 1, keySize - 2, keySize - 2, Math.max(0.0f, keyR - 1.0f), inner);
                }
                int keyTextX = keyX + (keySize - tr.getWidth(bindLine)) / 2;
                Objects.requireNonNull(tr);
                int keyTextY = keyY + (keySize - 9) / 2;
                ctx.drawText(tr, (Text)Text.literal((String)bindLine), keyTextX, keyTextY, -4605498, false);
            }
            ctx.getMatrices().pop();
        }
        ctx.disableScissor();
        if (hovered == null) {
            this.hoverModuleId = null;
            this.hoverStartMs = 0L;
            return;
        }
        long now = System.currentTimeMillis();
        if (this.hoverModuleId != hovered.id) {
            this.hoverModuleId = hovered.id;
            this.hoverStartMs = now;
            return;
        }
        if (now - this.hoverStartMs < 1000L) {
            return;
        }
        String desc = hovered.id.description;
        if (desc == null || desc.isBlank()) {
            return;
        }
        int pad = 6;
        int tw = tr.getWidth(desc);
        int boxW = tw + pad * 2;
        Objects.requireNonNull(tr);
        int boxH = 9 + pad * 2;
        int bx = mouseX + 12;
        int by = mouseY + 10;
        int minX = areaX;
        int minY = areaY;
        int maxX = areaX + areaW - boxW;
        int maxY = areaY + areaH - boxH;
        bx = GuiScreen.clamp(bx, minX, maxX);
        by = GuiScreen.clamp(by, minY, maxY);
        int bg = Theme.argb(210, 14, 14, 18);
        Rounded.rect(ctx, bx, by, boxW, boxH, 10.0f, bg);
        Rounded.outline(ctx, bx, by, boxW, boxH, 10.0f, 1, Theme.withAlpha(accent, 180));
        ctx.drawText(tr, (Text)Text.literal((String)desc), bx + pad, by + pad, -1, false);
    }

    private void renderModuleSettingsPage(DrawContext ctx, int mouseX, int mouseY, int accent, float R_TILE, float R_BTN, float delta) {
        this.searchField.field.setVisible(false);
        TextRenderer tr = GuiFonts.textRenderer();
        int contentX = this.panelX + 96;
        int areaX = contentX + 12;
        int areaY = this.panelY + 12;
        int areaW = this.panelW - 96 - 24;
        int areaH = this.panelH - 24;
        String backText = "\u2190 Back";
        int backW = tr.getWidth(backText);
        Objects.requireNonNull(tr);
        int backH = Math.max(18, 9 + 6);
        boolean backHover = GuiScreen.inside(mouseX, mouseY, areaX, areaY, backW + 16, backH);
        int backBg = backHover ? Theme.withAlpha(accent, 55) : Theme.argb(150, 20, 20, 26);
        Rounded.rect(ctx, areaX, areaY, backW + 16, backH, R_BTN, backBg);
        int backPad = (int)Math.ceil(R_BTN) + 2;
        Objects.requireNonNull(tr);
        int backTy = areaY + (backH - 9) / 2;
        ctx.drawText(tr, (Text)Text.literal((String)backText), areaX + backPad, backTy, -1, false);
        String title = this.selectedModule != null ? this.selectedModule.id.display : "Module";
        ctx.drawText(tr, (Text)Text.literal((String)title), areaX, areaY + 28, Theme.withAlpha(accent, 255), false);
        int lineW = Math.round((float)(areaW - 12) * (0.25f + 0.75f * this.modulesViewAnim));
        Objects.requireNonNull(tr);
        int lineY = areaY + 28 + 9 + 3;
        int lineCol = Theme.withAlpha(accent, (int)(120.0f + 100.0f * this.modulesViewAnim));
        Rounded.rect(ctx, areaX, lineY, Math.max(2, lineW), 2, 2.0f, lineCol);
        int px = areaX;
        int py = areaY + 44;
        int pw = areaW;
        int ph = areaH - 52;
        Rounded.rect(ctx, px, py, pw, ph, R_TILE, Theme.argb(130, 18, 18, 22));
        int innerX = px + 12;
        int innerY = py + 12;
        int innerW = pw - 24;
        int innerH = ph - 24;
        if (innerH <= 0) {
            return;
        }
        int contentH = 0;
        int gap = 8;
        int toggleH = 18;
        if (this.selectedModule != null && this.selectedModule.id == ModuleId.FULLBRIGHT) {
            contentH = 26;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.TPACCEPT_SOUNDS) {
            contentH = 26;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.ARMOR_EQUIPPER) {
            gap = 8;
            contentH = 44 + gap;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.ANTYKOSTKA) {
            gap = 8;
            contentH = 44 + gap;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.AUTO_TOTEM) {
            Objects.requireNonNull(tr);
            contentH = 9 + 4;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.SPAMMER) {
            gap = 8;
            contentH = 62 + gap * 2;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.ERROR_KILLER) {
            gap = 8;
            contentH = 124 + gap * 5;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.FAST_LEVER) {
            contentH = 26;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.FAST_PLACE) {
            Objects.requireNonNull(tr);
            int labelH = 9 + 4;
            contentH = 26 + labelH;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.PEARL_TRAJECTORY) {
            toggleH = 18;
            gap = 8;
            contentH = toggleH * 5 + gap * 4 + 26 + gap;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.TRACERS) {
            toggleH = 18;
            gap = 8;
            contentH = toggleH * 3 + 104 + gap * 6;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.ESP) {
            contentH = toggleH = 18;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.NAME_PROTECT) {
            GuiConfig cfg = GuiClient.CONFIG;
            toggleH = 18;
            gap = 8;
            Objects.requireNonNull(tr);
            int labelH = 9 + 4;
            int baseH = labelH + toggleH * 3 + gap * 3;
            float statsT = this.nameProtectChangeStatsAnim;
            int extraH = Math.round((float)(toggleH * 4 + gap * 3) * statsT);
            contentH = baseH + extraH;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.LOGOUT_SPOTS) {
            toggleH = 18;
            gap = 8;
            contentH = toggleH * 2 + gap;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.SWORD_INFO) {
            toggleH = 18;
            gap = 8;
            contentH = toggleH + 52 + gap * 2;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.ANTI_TRAP) {
            GuiConfig cfg = GuiClient.CONFIG;
            toggleH = 18;
            gap = 8;
            float frameT = this.antiTrapFrameAnim;
            float paintingT = this.antiTrapPaintingAnim;
            float autoPlotekT = this.antiTrapAutoPlotekAnim;
            float h = 0.0f;
            h += (float)(toggleH + gap);
            h += (float)(toggleH + gap) * 3.0f * frameT;
            h += (float)(toggleH + gap);
            h += (float)(toggleH + gap) * 3.0f * paintingT;
            h += (float)(toggleH + gap);
            h += (float)(toggleH + gap);
            h += (float)(toggleH + gap);
            h += (float)(toggleH + gap);
            contentH = Math.round(h += (float)(26 + gap) * autoPlotekT);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.AUTO_PARAWAN) {
            contentH = 18;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.AIM_ASSIST) {
            gap = 8;
            contentH = 62 + gap * 2;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.BETTER_HITBOXES) {
            gap = 8;
            contentH = 52 + gap;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.NO_PUSH) {
            toggleH = 18;
            gap = 8;
            contentH = toggleH * 3 + gap * 2;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.FREECAM) {
            toggleH = 18;
            gap = 8;
            contentH = toggleH + 52 + gap * 2;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.NAMETAGS) {
            toggleH = 18;
            gap = 8;
            int toggleCount = 10;
            int sliderCount = 6;
            int buttonCount = 1;
            int rows = toggleCount + sliderCount + buttonCount;
            contentH = toggleCount * toggleH + sliderCount * 26 + buttonCount * toggleH + gap * Math.max(0, rows - 1);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.REFILLER) {
            toggleH = 18;
            gap = 8;
            contentH = toggleH * 3 + gap * 2;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.PANIC_MODE) {
            toggleH = 18;
            gap = 8;
            contentH = toggleH * 3 + gap * 2;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.XRAY) {
            contentH = 18;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.ITEM_ESP) {
            gap = 8;
            contentH = 36 + gap;
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.BLOCK_ESP) {
            contentH = 18;
        }
        float maxScroll = Math.max(0.0f, (float)(contentH - innerH));
        this.moduleSettingsScrollTarget = GuiScreen.clamp(this.moduleSettingsScrollTarget, 0.0f, maxScroll);
        this.moduleSettingsScroll = GuiScreen.lerp(this.moduleSettingsScroll, this.moduleSettingsScrollTarget, 0.25f);
        if (Math.abs(this.moduleSettingsScroll - this.moduleSettingsScrollTarget) < 0.01f) {
            this.moduleSettingsScroll = this.moduleSettingsScrollTarget;
        }
        int sx;
        int sy;
        int sw;
        int sh;
        int fill;
        int bg;
        int btnBg;
        int tx;
        ctx.enableScissor(px, py, px + pw, py + ph);
        if (this.selectedModule != null && this.selectedModule.id == ModuleId.FULLBRIGHT) {
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            sh = 26;
            bg = Theme.argb(150, 20, 20, 26);
            fill = Theme.withAlpha(accent, 255);
            this.fullbrightSlider.setBounds(sx, sy, sw, sh);
            this.fullbrightSlider.render(ctx, mouseX, mouseY, bg, fill, -1);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.TPACCEPT_SOUNDS) {
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            sh = 26;
            bg = Theme.argb(150, 20, 20, 26);
            fill = Theme.withAlpha(accent, 255);
            Float volObj = GuiClient.CONFIG.tpaAcceptSoundVolume;
            float vol = volObj == null || Float.isNaN(volObj.floatValue()) || Float.isInfinite(volObj.floatValue()) ? 1.0f : volObj.floatValue();
            vol = GuiScreen.clamp(vol, 0.0f, 1.0f);
            if (!this.tpaAcceptSoundVolumeSlider.dragging) {
                this.tpaAcceptSoundVolumeSlider.value = vol;
            }
            this.tpaAcceptSoundVolumeSlider.label = Text.literal((String)("Glosnosc: " + Math.round(vol * 100.0f) + "%"));
            this.tpaAcceptSoundVolumeSlider.setBounds(sx, sy, sw, sh);
            this.tpaAcceptSoundVolumeSlider.render(ctx, mouseX, mouseY, bg, fill, -1);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.ARMOR_EQUIPPER) {
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            int rowH = 18;
            gap = 8;
            String bind = this.bindingTarget == this.selectedModule ? "..." : GuiScreen.formatKeyShort(this.selectedModule.getBindKey());
            this.drawBindRow(ctx, sx, sy, sw, rowH, "Bind", bind, accent, this.bindingTarget == this.selectedModule);
            sy += rowH + gap;
            bg = Theme.argb(150, 20, 20, 26);
            fill = Theme.withAlpha(accent, 255);
            int delay = GuiClient.CONFIG.armorEquipperDelayTicks;
            if (delay < 0 || delay > 20) {
                delay = 1;
            }
            this.armorEquipperDelaySlider.label = Text.literal((String)("Delay: " + delay + " ticks"));
            this.armorEquipperDelaySlider.setBounds(sx, sy, sw, 26);
            this.armorEquipperDelaySlider.render(ctx, mouseX, mouseY, bg, fill, -1);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.ANTYKOSTKA) {
            sx = innerX;
            sw = innerW;
            int rowH = 18;
            sy = innerY - (int)this.moduleSettingsScroll;
            String bind = this.bindingTarget == this.selectedModule ? "..." : GuiScreen.formatKeyShort(this.selectedModule.getBindKey());
            this.drawBindRow(ctx, sx, sy, sw, rowH, "Bind", bind, accent, this.bindingTarget == this.selectedModule);
            sy += rowH + 8;
            bg = Theme.argb(150, 20, 20, 26);
            fill = Theme.withAlpha(accent, 255);
            float v = GuiClient.CONFIG.antyKostkaSpeed;
            if (Float.isNaN(v) || Float.isInfinite(v)) {
                v = 0.0f;
            }
            if (v < 0.0f) {
                v = 0.0f;
            }
            if (v > 1.0f) {
                v = 1.0f;
            }
            float mult = 1.0f + v;
            this.antyKostkaSpeedSlider.label = Text.literal((String)String.format(Locale.US, "Speed: %.1fx", Float.valueOf(mult)));
            this.antyKostkaSpeedSlider.setBounds(sx, sy, sw, 26);
            this.antyKostkaSpeedSlider.render(ctx, mouseX, mouseY, bg, fill, -1);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.ERROR_KILLER) {
            GuiConfig cfg = GuiClient.CONFIG;
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            int rowH = 18;
            gap = 8;
            bg = Theme.argb(150, 20, 20, 26);
            fill = Theme.withAlpha(accent, 255);
            int delay = cfg.errorKillerDelayMs;
            if (delay < 1 || delay > 100) {
                delay = 1;
            }
            Object val = delay <= 1 ? "INSTANT" : delay + "ms";
            this.errorKillerDelaySlider.label = Text.literal((String)("Delay For Cobweb: " + (String)val));
            this.errorKillerDelaySlider.setBounds(sx, sy, sw, 26);
            this.errorKillerDelaySlider.render(ctx, mouseX, mouseY, bg, fill, -1);
            boolean showMissing = cfg.errorKillerShowMissing != null && cfg.errorKillerShowMissing != false;
            this.errorKillerShowMissingAnim = this.drawToggleRow(ctx, sx, sy += 26 + gap, sw, rowH, "Show Missing For Cobweb", showMissing, this.errorKillerShowMissingAnim, accent);
            sy += rowH + gap;
            int obsDelay = cfg.errorKillerDelayMsObsidian;
            if (obsDelay < 1 || obsDelay > 100) {
                obsDelay = delay;
            }
            Object obsVal = obsDelay <= 1 ? "INSTANT" : obsDelay + "ms";
            this.errorKillerDelayObsidianSlider.label = Text.literal((String)("Delay For Obsidian: " + (String)obsVal));
            this.errorKillerDelayObsidianSlider.setBounds(sx, sy, sw, 26);
            this.errorKillerDelayObsidianSlider.render(ctx, mouseX, mouseY, bg, fill, -1);
            boolean showMissingObs = cfg.errorKillerShowMissingObsidian != null && cfg.errorKillerShowMissingObsidian != false;
            this.errorKillerShowMissingObsidianAnim = this.drawToggleRow(ctx, sx, sy += 26 + gap, sw, rowH, "Show Missing For Obsidian", showMissingObs, this.errorKillerShowMissingObsidianAnim, accent);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.FAST_LEVER) {
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            sh = 26;
            bg = Theme.argb(150, 20, 20, 26);
            fill = Theme.withAlpha(accent, 255);
            float speed = GuiClient.CONFIG.fastLeverSpeed;
            if (Float.isNaN(speed) || Float.isInfinite(speed)) {
                speed = 100.0f;
            }
            speed = GuiScreen.clamp(speed, 0.0f, 100.0f);
            this.fastLeverSpeedSlider.label = Text.literal((String)("Speed: " + Math.round(speed) + "%"));
            this.fastLeverSpeedSlider.setBounds(sx, sy, sw, sh);
            this.fastLeverSpeedSlider.render(ctx, mouseX, mouseY, bg, fill, -1);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.FAST_PLACE) {
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            sh = 26;
            bg = Theme.argb(150, 20, 20, 26);
            fill = Theme.withAlpha(accent, 255);
            int labelY = sy;
            Objects.requireNonNull(tr);
            int sliderY = sy + 9 + 4;
            int pad = 8;
            int barW = Math.max(1, sw - pad * 2);
            for (int i = 0; i <= 3; ++i) {
                String txt = Integer.toString(i);
                int tw = tr.getWidth(txt);
                int tickX = sx + pad + Math.round((float)barW * ((float)i / 3.0f)) - tw / 2;
                ctx.drawText(tr, (Text)Text.literal((String)txt), tickX, labelY, -4605498, false);
            }
            this.fastPlaceDelaySlider.setBounds(sx, sliderY, sw, sh);
            this.fastPlaceDelaySlider.render(ctx, mouseX, mouseY, bg, fill, -1);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.PEARL_TRAJECTORY) {
            GuiConfig cfg = GuiClient.CONFIG;
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            toggleH = 18;
            gap = 8;
            boolean showTrajectory = cfg.pearlShowTrajectory == null || cfg.pearlShowTrajectory != false;
            this.pearlTrajAnim = this.drawToggleRow(ctx, sx, sy, sw, toggleH, "Trajectory", showTrajectory, this.pearlTrajAnim, accent);
            boolean showLanding = cfg.pearlShowLanding == null || cfg.pearlShowLanding != false;
            this.pearlLandingAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Landing Spot", showLanding, this.pearlLandingAnim, accent);
            boolean showNickname = cfg.pearlShowNickname == null || cfg.pearlShowNickname != false;
            this.pearlNicknameAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Nickname", showNickname, this.pearlNicknameAnim, accent);
            boolean showCountdown = cfg.pearlShowCountdown == null || cfg.pearlShowCountdown != false;
            this.pearlCountdownAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Countdown", showCountdown, this.pearlCountdownAnim, accent);
            boolean showOwn = cfg.pearlShowOwn == null || cfg.pearlShowOwn != false;
            this.pearlOwnAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "See Own Pearls", showOwn, this.pearlOwnAnim, accent);
            this.pearlLineWidthSlider.setBounds(sx, sy += toggleH + gap, sw, 26);
            bg = Theme.argb(150, 20, 20, 26);
            fill = Theme.withAlpha(accent, 255);
            this.pearlLineWidthSlider.render(ctx, mouseX, mouseY, bg, fill, -1);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.TRACERS) {
            GuiConfig cfg = GuiClient.CONFIG;
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            toggleH = 18;
            gap = 8;
            bg = Theme.argb(150, 20, 20, 26);
            fill = Theme.withAlpha(accent, 255);
            boolean showPlayers = cfg.tracerPlayers == null || cfg.tracerPlayers != false;
            this.tracerPlayersAnim = this.drawToggleRow(ctx, sx, sy, sw, toggleH, "Players", showPlayers, this.tracerPlayersAnim, accent);
            this.tracerPlayersColorSlider.setBounds(sx, sy += toggleH + gap, sw, 26);
            this.tracerPlayersColorSlider.render(ctx, mouseX, mouseY, bg, fill, -1);
            boolean showFriends = cfg.tracerFriends == null || cfg.tracerFriends != false;
            this.tracerFriendsAnim = this.drawToggleRow(ctx, sx, sy += 26 + gap, sw, toggleH, "Friends", showFriends, this.tracerFriendsAnim, accent);
            this.tracerFriendsColorSlider.setBounds(sx, sy += toggleH + gap, sw, 26);
            this.tracerFriendsColorSlider.render(ctx, mouseX, mouseY, bg, fill, -1);
            boolean showMobs = cfg.tracerMobs == null || cfg.tracerMobs != false;
            this.tracerMobsAnim = this.drawToggleRow(ctx, sx, sy += 26 + gap, sw, toggleH, "Mobs", showMobs, this.tracerMobsAnim, accent);
            this.tracerMobsColorSlider.setBounds(sx, sy += toggleH + gap, sw, 26);
            this.tracerMobsColorSlider.render(ctx, mouseX, mouseY, bg, fill, -1);
            this.tracerDistanceSlider.setBounds(sx, sy += 26 + gap, sw, 26);
            this.tracerDistanceSlider.render(ctx, mouseX, mouseY, bg, fill, -1);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.ESP) {
            GuiConfig cfg = GuiClient.CONFIG;
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            toggleH = 18;
            gap = 8;
            bg = Theme.argb(150, 20, 20, 26);
            fill = Theme.withAlpha(accent, 255);
            boolean box = cfg.espBox == null || cfg.espBox != false;
            this.espBoxAnim = this.drawToggleRow(ctx, sx, sy, sw, toggleH, "Box", box, this.espBoxAnim, accent);
            this.espPlayerColorSlider.setBounds(sx, sy += toggleH + gap, sw, 26);
            this.espPlayerColorSlider.syncRgb(cfg.espPlayerColor);
            this.espPlayerColorSlider.render(ctx, mouseX, mouseY, bg, -1, fill);
            this.espFriendColorSlider.setBounds(sx, sy += 26 + gap, sw, 26);
            this.espFriendColorSlider.syncRgb(cfg.espFriendColor);
            this.espFriendColorSlider.render(ctx, mouseX, mouseY, bg, -1, fill);
            sy += 26 + gap;
            float minEspLine = 1.0f;
            float maxEspLine = 6.0f;
            float espLine = cfg.espLineWidth;
            if (Float.isNaN(espLine) || Float.isInfinite(espLine)) {
                espLine = 4.0f;
            }
            espLine = Math.max(minEspLine, Math.min(maxEspLine, espLine));
            if (!this.espLineWidthSlider.dragging) {
                this.espLineWidthSlider.value = (espLine - minEspLine) / (maxEspLine - minEspLine);
            }
            this.espLineWidthSlider.label = Text.literal((String)String.format(Locale.US, "Line Width: %.1f", Float.valueOf(espLine)));
            this.espLineWidthSlider.setBounds(sx, sy, sw, 26);
            this.espLineWidthSlider.render(ctx, mouseX, mouseY, bg, fill, -1);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.NAME_PROTECT) {
            String desired;
            GuiConfig cfg = GuiClient.CONFIG;
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            toggleH = 18;
            gap = 8;
            int modeGap = 6;
            int modeW = (sw - modeGap) / 2;
            Objects.requireNonNull(tr);
            int labelH = 9 + 4;
            ctx.drawText(tr, (Text)Text.literal((String)"Mode:"), sx, sy, -4605498, false);
            boolean self = cfg.nameProtectMode == null || cfg.nameProtectMode == GuiConfig.NameProtectMode.SELF;
            this.drawModeButton(ctx, sx, sy += labelH, modeW, toggleH, "Self", self, accent, R_BTN);
            this.drawModeButton(ctx, sx + modeW + modeGap, sy, modeW, toggleH, "Everyone", !self, accent, R_BTN);
            sy += toggleH + gap;
            String label = "Display Name:";
            int labelW = tr.getWidth(label);
            int fieldX = sx + labelW + gap;
            int fieldW = Math.max(80, sw - labelW - gap);
            MutableText mutableText = Text.literal((String)label);
            Objects.requireNonNull(tr);
            ctx.drawText(tr, (Text)mutableText, sx, sy + (toggleH - 9) / 2, -4605498, false);
            this.nameProtectField.setPos(fieldX, sy);
            this.nameProtectField.setSize(fieldW, toggleH);
            this.nameProtectField.field.setVisible(true);
            String string = desired = cfg.nameProtectName == null ? "" : cfg.nameProtectName;
            if (!this.nameProtectField.field.isFocused() && !this.nameProtectField.field.getText().equals(desired)) {
                this.nameProtectField.field.setText(desired);
            }
            this.nameProtectField.render(ctx, mouseX, mouseY, delta);
            cfg.nameProtectName = this.nameProtectField.field.getText();
            boolean changeStats = cfg.nameProtectChangeStats != null && cfg.nameProtectChangeStats != false;
            this.nameProtectChangeStatsAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Change Stats", changeStats, this.nameProtectChangeStatsAnim, accent);
            sy += toggleH + gap;
            float statsT = this.nameProtectChangeStatsAnim;
            if (statsT > 0.01f) {
                int timeY = sy + this.revealOffset(statsT);
                final int cap$gap$1 = gap; final int cap$sw$2 = sw; final int cap$sx$3 = sx; final int cap$toggleH$4 = toggleH;
                this.withAlpha(statsT, () -> {
                    String timeDesired;
                    String timeLabel = "Czas";
                    int timeLabelW = tr.getWidth(timeLabel);
                    int timeFieldX = cap$sx$3 + timeLabelW + cap$gap$1;
                    int timeFieldW = Math.max(80, cap$sw$2 - timeLabelW - cap$gap$1);
                    MutableText mtTime = Text.literal((String)timeLabel);
                    Objects.requireNonNull(tr);
                    ctx.drawText(tr, (Text)mtTime, cap$sx$3, timeY + (cap$toggleH$4 - 9) / 2, -4605498, false);
                    this.nameProtectTimeField.setPos(timeFieldX, timeY);
                    this.nameProtectTimeField.setSize(timeFieldW, cap$toggleH$4);
                    this.nameProtectTimeField.field.setVisible(true);
                    timeDesired = cfg.nameProtectStatTime == null ? "" : cfg.nameProtectStatTime;
                    if (!this.nameProtectTimeField.field.isFocused() && !this.nameProtectTimeField.field.getText().equals(timeDesired)) {
                        this.nameProtectTimeField.field.setText(timeDesired);
                    }
                    this.nameProtectTimeField.render(ctx, mouseX, mouseY, delta);
                    cfg.nameProtectStatTime = this.nameProtectTimeField.field.getText();
                });
                int moneyY = (sy += Math.round((float)(toggleH + gap) * statsT)) + this.revealOffset(statsT);
                final int cap$gap$5 = gap; final int cap$sw$6 = sw; final int cap$sx$7 = sx; final int cap$toggleH$8 = toggleH;
                this.withAlpha(statsT, () -> {
                    String moneyDesired;
                    String moneyLabel = "Kasa";
                    int moneyLabelW = tr.getWidth(moneyLabel);
                    int moneyFieldX = cap$sx$7 + moneyLabelW + cap$gap$5;
                    int moneyFieldW = Math.max(80, cap$sw$6 - moneyLabelW - cap$gap$5);
                    MutableText mtMoney = Text.literal((String)moneyLabel);
                    Objects.requireNonNull(tr);
                    ctx.drawText(tr, (Text)mtMoney, cap$sx$7, moneyY + (cap$toggleH$8 - 9) / 2, -4605498, false);
                    this.nameProtectMoneyField.setPos(moneyFieldX, moneyY);
                    this.nameProtectMoneyField.setSize(moneyFieldW, cap$toggleH$8);
                    this.nameProtectMoneyField.field.setVisible(true);
                    moneyDesired = cfg.nameProtectStatMoney == null ? "" : cfg.nameProtectStatMoney;
                    if (!this.nameProtectMoneyField.field.isFocused() && !this.nameProtectMoneyField.field.getText().equals(moneyDesired)) {
                        this.nameProtectMoneyField.field.setText(moneyDesired);
                    }
                    this.nameProtectMoneyField.render(ctx, mouseX, mouseY, delta);
                    cfg.nameProtectStatMoney = this.nameProtectMoneyField.field.getText();
                });
                int killsY = (sy += Math.round((float)(toggleH + gap) * statsT)) + this.revealOffset(statsT);
                final int cap$gap$9 = gap; final int cap$sw$10 = sw; final int cap$sx$11 = sx; final int cap$toggleH$12 = toggleH;
                this.withAlpha(statsT, () -> {
                    String killsDesired;
                    String killsLabel = "Kille";
                    int killsLabelW = tr.getWidth(killsLabel);
                    int killsFieldX = cap$sx$11 + killsLabelW + cap$gap$9;
                    int killsFieldW = Math.max(80, cap$sw$10 - killsLabelW - cap$gap$9);
                    MutableText mtKills = Text.literal((String)killsLabel);
                    Objects.requireNonNull(tr);
                    ctx.drawText(tr, (Text)mtKills, cap$sx$11, killsY + (cap$toggleH$12 - 9) / 2, -4605498, false);
                    this.nameProtectKillsField.setPos(killsFieldX, killsY);
                    this.nameProtectKillsField.setSize(killsFieldW, cap$toggleH$12);
                    this.nameProtectKillsField.field.setVisible(true);
                    killsDesired = cfg.nameProtectStatKills == null ? "" : cfg.nameProtectStatKills;
                    if (!this.nameProtectKillsField.field.isFocused() && !this.nameProtectKillsField.field.getText().equals(killsDesired)) {
                        this.nameProtectKillsField.field.setText(killsDesired);
                    }
                    this.nameProtectKillsField.render(ctx, mouseX, mouseY, delta);
                    cfg.nameProtectStatKills = this.nameProtectKillsField.field.getText();
                });
                int deathsY = (sy += Math.round((float)(toggleH + gap) * statsT)) + this.revealOffset(statsT);
                final int cap$gap$13 = gap; final int cap$sw$14 = sw; final int cap$sx$15 = sx; final int cap$toggleH$16 = toggleH;
                this.withAlpha(statsT, () -> {
                    String deathsDesired;
                    String deathsLabel = "\u015amierci";
                    int deathsLabelW = tr.getWidth(deathsLabel);
                    int deathsFieldX = cap$sx$15 + deathsLabelW + cap$gap$13;
                    int deathsFieldW = Math.max(80, cap$sw$14 - deathsLabelW - cap$gap$13);
                    MutableText mtDeaths = Text.literal((String)deathsLabel);
                    Objects.requireNonNull(tr);
                    ctx.drawText(tr, (Text)mtDeaths, cap$sx$15, deathsY + (cap$toggleH$16 - 9) / 2, -4605498, false);
                    this.nameProtectDeathsField.setPos(deathsFieldX, deathsY);
                    this.nameProtectDeathsField.setSize(deathsFieldW, cap$toggleH$16);
                    this.nameProtectDeathsField.field.setVisible(true);
                    deathsDesired = cfg.nameProtectStatDeaths == null ? "" : cfg.nameProtectStatDeaths;
                    if (!this.nameProtectDeathsField.field.isFocused() && !this.nameProtectDeathsField.field.getText().equals(deathsDesired)) {
                        this.nameProtectDeathsField.field.setText(deathsDesired);
                    }
                    this.nameProtectDeathsField.render(ctx, mouseX, mouseY, delta);
                    cfg.nameProtectStatDeaths = this.nameProtectDeathsField.field.getText();
                });
            } else {
                this.nameProtectTimeField.field.setVisible(false);
                this.nameProtectMoneyField.field.setVisible(false);
                this.nameProtectKillsField.field.setVisible(false);
                this.nameProtectDeathsField.field.setVisible(false);
            }
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.SPAMMER) {
            String desired;
            GuiConfig cfg = GuiClient.CONFIG;
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            int rowH = 18;
            gap = 8;
            String label = "Message:";
            int labelW = tr.getWidth(label);
            int fieldX = sx + labelW + gap;
            int fieldW = Math.max(80, sw - labelW - gap);
            MutableText mutableText = Text.literal((String)label);
            Objects.requireNonNull(tr);
            ctx.drawText(tr, (Text)mutableText, sx, sy + (rowH - 9) / 2, -4605498, false);
            this.spammerMessageField.setPos(fieldX, sy);
            this.spammerMessageField.setSize(fieldW, rowH);
            this.spammerMessageField.field.setVisible(true);
            String string = desired = cfg.spammerMessage == null ? "" : cfg.spammerMessage;
            if (!this.spammerMessageField.field.isFocused() && !this.spammerMessageField.field.getText().equals(desired)) {
                this.spammerMessageField.field.setText(desired);
            }
            this.spammerMessageField.render(ctx, mouseX, mouseY, delta);
            cfg.spammerMessage = this.spammerMessageField.field.getText();
            boolean anti = cfg.spammerAntiSpam != null && cfg.spammerAntiSpam != false;
            this.spammerAntiSpamAnim = this.drawToggleRow(ctx, sx, sy += rowH + gap, sw, rowH, "Anti-spam", anti, this.spammerAntiSpamAnim, accent);
            sy += rowH + gap;
            int delay = cfg.spammerDelaySec;
            if (delay < 1 || delay > 60) {
                delay = 3;
            }
            cfg.spammerDelaySec = delay;
            this.spammerDelaySlider.label = Text.literal((String)("Delay: " + delay + "s"));
            this.spammerDelaySlider.setBounds(sx, sy, sw, 26);
            bg = Theme.argb(150, 20, 20, 26);
            fill = Theme.withAlpha(accent, 255);
            this.spammerDelaySlider.render(ctx, mouseX, mouseY, bg, fill, -1);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.LOGOUT_SPOTS) {
            GuiConfig cfg = GuiClient.CONFIG;
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            toggleH = 18;
            gap = 8;
            boolean showNick = cfg.logoutShowNick == null || cfg.logoutShowNick != false;
            this.logoutNickAnim = this.drawToggleRow(ctx, sx, sy, sw, toggleH, "Nick", showNick, this.logoutNickAnim, accent);
            boolean showTime = cfg.logoutShowTime == null || cfg.logoutShowTime != false;
            this.logoutTimeAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Time", showTime, this.logoutTimeAnim, accent);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.SWORD_INFO) {
            GuiConfig cfg = GuiClient.CONFIG;
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            toggleH = 18;
            gap = 8;
            bg = Theme.argb(150, 20, 20, 26);
            fill = Theme.withAlpha(accent, 255);
            boolean frame = cfg.swordInfoFrame == null || cfg.swordInfoFrame != false;
            this.swordInfoFrameAnim = this.drawToggleRow(ctx, sx, sy, sw, toggleH, "Frame", frame, this.swordInfoFrameAnim, accent);
            boolean showAll = cfg.swordInfoShowAllPlayers != null && cfg.swordInfoShowAllPlayers != false;
            this.swordInfoShowAllAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Tabelka", showAll, this.swordInfoShowAllAnim, accent);
            int extraColor = cfg.swordInfoExtraColor == null ? 5611775 : cfg.swordInfoExtraColor;
            this.swordInfoExtraColorSlider.setBounds(sx, sy += toggleH + gap, sw, 26);
            this.swordInfoExtraColorSlider.syncRgb(extraColor);
            this.swordInfoExtraColorSlider.render(ctx, mouseX, mouseY, bg, -1, fill);
            sy += 26 + gap;
            float scale = cfg.swordInfoScale;
            if (Float.isNaN(scale) || Float.isInfinite(scale)) {
                scale = 0.9f;
            }
            scale = GuiScreen.clamp(scale, 0.6f, 1.4f);
            if (!this.swordInfoScaleSlider.dragging) {
                this.swordInfoScaleSlider.value = (scale - 0.6f) / 0.79999995f;
            }
            this.swordInfoScaleSlider.label = Text.literal((String)"Size");
            this.swordInfoScaleSlider.setBounds(sx, sy, sw, 26);
            this.swordInfoScaleSlider.render(ctx, mouseX, mouseY, bg, fill, -1);
            sy += 26 + gap;
            float allScale = cfg.swordInfoAllScale;
            if (Float.isNaN(allScale) || Float.isInfinite(allScale)) {
                allScale = 0.8f;
            }
            allScale = GuiScreen.clamp(allScale, 0.6f, 1.2f);
            if (!this.swordInfoAllScaleSlider.dragging) {
                this.swordInfoAllScaleSlider.value = (allScale - 0.6f) / 0.6f;
            }
            this.swordInfoAllScaleSlider.label = Text.literal((String)"Tabelka size");
            this.swordInfoAllScaleSlider.setBounds(sx, sy, sw, 26);
            this.swordInfoAllScaleSlider.render(ctx, mouseX, mouseY, bg, fill, -1);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.ANTI_TRAP) {
            GuiConfig cfg = GuiClient.CONFIG;
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            toggleH = 18;
            gap = 8;
            int subIndent = 12;
            int subX = sx + subIndent;
            int subW = sw - subIndent;
            bg = Theme.argb(150, 20, 20, 26);
            fill = Theme.withAlpha(accent, 255);
            boolean itemFrame = cfg.antiTrapItemFrame == null || cfg.antiTrapItemFrame != false;
            this.antiTrapFrameAnim = this.drawToggleRow(ctx, sx, sy, sw, toggleH, "Item Frame", itemFrame, this.antiTrapFrameAnim, accent);
            sy += toggleH + gap;
            float itemFrameT = this.antiTrapFrameAnim;
            if (itemFrameT > 0.01f) {
                int headerY = sy + this.revealOffset(itemFrameT);
                final int cap$sw$17 = sw; final int cap$sx$18 = sx;
                this.withAlpha(itemFrameT, () -> this.drawSubHeaderRow(ctx, sx, headerY, sw, "Item Frame", accent));
                boolean itemFrameAutoBreak = cfg.antiTrapItemFrameAutoBreak == null || cfg.antiTrapItemFrameAutoBreak != false;
                int autoBreakY = (sy += Math.round((float)(toggleH + gap) * itemFrameT)) + this.revealOffset(itemFrameT);
                final int cap$toggleH$19 = toggleH;
                this.withAlpha(itemFrameT, () -> {
                    this.antiTrapFrameAutoBreakAnim = this.drawToggleRow(ctx, subX, autoBreakY, subW, cap$toggleH$19, "AutoBreak", itemFrameAutoBreak, this.antiTrapFrameAutoBreakAnim, accent);
                });
                boolean itemFrameVisual = cfg.antiTrapItemFrameVisual == null || cfg.antiTrapItemFrameVisual != false;
                int visualY = (sy += Math.round((float)(toggleH + gap) * itemFrameT)) + this.revealOffset(itemFrameT);
                final int cap$toggleH$20 = toggleH;
                this.withAlpha(itemFrameT, () -> {
                    this.antiTrapFrameVisualAnim = this.drawToggleRow(ctx, subX, visualY, subW, cap$toggleH$20, "Visual", itemFrameVisual, this.antiTrapFrameVisualAnim, accent);
                });
                sy += Math.round((float)(toggleH + gap) * itemFrameT);
            }
            boolean painting = cfg.antiTrapPainting == null || cfg.antiTrapPainting != false;
            this.antiTrapPaintingAnim = this.drawToggleRow(ctx, sx, sy, sw, toggleH, "Painting", painting, this.antiTrapPaintingAnim, accent);
            sy += toggleH + gap;
            float paintingT = this.antiTrapPaintingAnim;
            if (paintingT > 0.01f) {
                int headerY = sy + this.revealOffset(paintingT);
                final int cap$sw$21 = sw; final int cap$sx$22 = sx;
                this.withAlpha(paintingT, () -> this.drawSubHeaderRow(ctx, sx, headerY, sw, "Painting", accent));
                boolean paintingAutoBreak = cfg.antiTrapPaintingAutoBreak == null || cfg.antiTrapPaintingAutoBreak != false;
                int autoBreakY = (sy += Math.round((float)(toggleH + gap) * paintingT)) + this.revealOffset(paintingT);
                final int cap$toggleH$23 = toggleH;
                this.withAlpha(paintingT, () -> {
                    this.antiTrapPaintingAutoBreakAnim = this.drawToggleRow(ctx, subX, autoBreakY, subW, cap$toggleH$23, "AutoBreak", paintingAutoBreak, this.antiTrapPaintingAutoBreakAnim, accent);
                });
                boolean paintingVisual = cfg.antiTrapPaintingVisual == null || cfg.antiTrapPaintingVisual != false;
                int visualY = (sy += Math.round((float)(toggleH + gap) * paintingT)) + this.revealOffset(paintingT);
                final int cap$toggleH$24 = toggleH;
                this.withAlpha(paintingT, () -> {
                    this.antiTrapPaintingVisualAnim = this.drawToggleRow(ctx, subX, visualY, subW, cap$toggleH$24, "Visual", paintingVisual, this.antiTrapPaintingVisualAnim, accent);
                });
                sy += Math.round((float)(toggleH + gap) * paintingT);
            }
            boolean minecart = cfg.antiTrapMinecart == null || cfg.antiTrapMinecart != false;
            this.antiTrapMinecartAnim = this.drawToggleRow(ctx, sx, sy, sw, toggleH, "Minecart", minecart, this.antiTrapMinecartAnim, accent);
            boolean armor = cfg.antiTrapArmorStand == null || cfg.antiTrapArmorStand != false;
            this.antiTrapArmorAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Armor Stand", armor, this.antiTrapArmorAnim, accent);
            boolean mobs = cfg.antiTrapMobs != null && cfg.antiTrapMobs != false;
            this.antiTrapMobsAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Mobs/NPC", mobs, this.antiTrapMobsAnim, accent);
            boolean autoPlotek = cfg.antiTrapAutoPlotek != null && cfg.antiTrapAutoPlotek != false;
            this.antiTrapAutoPlotekAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Auto Plotek", autoPlotek, this.antiTrapAutoPlotekAnim, accent);
            sy += toggleH + gap;
            float autoPlotekT = this.antiTrapAutoPlotekAnim;
            if (autoPlotekT > 0.01f) {
                int sliderY = sy + this.revealOffset(autoPlotekT);
                final int cap$bg$25 = bg; final int cap$fill$26 = fill; final int cap$sw$27 = sw; final int cap$sx$28 = sx;
                this.withAlpha(autoPlotekT, () -> {
                    int minCd = 0;
                    int maxCd = 500;
                    int cd = cfg.autoPlotekCooldownMs;
                    if (cd < minCd || cd > maxCd) {
                        cd = 80;
                    }
                    this.autoPlotekCooldownSlider.label = Text.literal((String)("Cooldown: " + cd + "ms"));
                    this.autoPlotekCooldownSlider.setBounds(cap$sx$28, sliderY, cap$sw$27, 26);
                    this.autoPlotekCooldownSlider.render(ctx, mouseX, mouseY, cap$bg$25, cap$fill$26, -1);
                });
                sy += Math.round((float)(26 + gap) * autoPlotekT);
            }
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.AUTO_PARAWAN) {
            GuiConfig cfg = GuiClient.CONFIG;
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            toggleH = 18;
            boolean dontAttack = cfg.autoParawanDontAttackFriends == null || cfg.autoParawanDontAttackFriends != false;
            this.autoParawanDontAttackFriendsAnim = this.drawToggleRow(ctx, sx, sy, sw, toggleH, "Dont attack friends", dontAttack, this.autoParawanDontAttackFriendsAnim, accent);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.AIM_ASSIST) {
            GuiConfig cfg = GuiClient.CONFIG;
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            toggleH = 18;
            gap = 8;
            bg = Theme.argb(150, 20, 20, 26);
            fill = Theme.withAlpha(accent, 255);
            boolean targetLock = cfg.aimAssistTargetLock != null && cfg.aimAssistTargetLock != false;
            this.aimAssistTargetLockAnim = this.drawToggleRow(ctx, sx, sy, sw, toggleH, "Target Lock", targetLock, this.aimAssistTargetLockAnim, accent);
            boolean mobs = cfg.aimAssistMobs != null && cfg.aimAssistMobs != false;
            this.aimAssistMobsAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Mobs", mobs, this.aimAssistMobsAnim, accent);
            sy += toggleH + gap;
            float range = cfg.aimAssistRange;
            if (Float.isNaN(range) || Float.isInfinite(range)) {
                range = 6.0f;
            }
            range = GuiScreen.clamp(range, 2.0f, 10.0f);
            if (!this.aimAssistRangeSlider.dragging) {
                this.aimAssistRangeSlider.value = (range - 2.0f) / 8.0f;
            }
            this.aimAssistRangeSlider.label = Text.literal((String)("Range: " + String.format(Locale.US, "%.1f", Float.valueOf(range))));
            this.aimAssistRangeSlider.setBounds(sx, sy, sw, 26);
            this.aimAssistRangeSlider.render(ctx, mouseX, mouseY, bg, fill, -1);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.BETTER_HITBOXES) {
            GuiConfig cfg = GuiClient.CONFIG;
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            gap = 8;
            bg = Theme.argb(150, 20, 20, 26);
            fill = Theme.withAlpha(accent, 255);
            float min = 100.0f;
            float max = 300.0f;
            float sizeX = cfg.betterHitboxSizeX;
            if (Float.isNaN(sizeX) || Float.isInfinite(sizeX)) {
                sizeX = 100.0f;
            }
            sizeX = GuiScreen.clamp(sizeX, min, max);
            if (!this.betterHitboxSizeXSlider.dragging) {
                this.betterHitboxSizeXSlider.value = (sizeX - min) / (max - min);
            }
            this.betterHitboxSizeXSlider.label = Text.literal((String)("Size X: " + Math.round(sizeX) + "%"));
            this.betterHitboxSizeXSlider.setBounds(sx, sy, sw, 26);
            this.betterHitboxSizeXSlider.render(ctx, mouseX, mouseY, bg, fill, -1);
            sy += 26 + gap;
            float sizeY = cfg.betterHitboxSizeY;
            if (Float.isNaN(sizeY) || Float.isInfinite(sizeY)) {
                sizeY = 100.0f;
            }
            sizeY = GuiScreen.clamp(sizeY, min, max);
            if (!this.betterHitboxSizeYSlider.dragging) {
                this.betterHitboxSizeYSlider.value = (sizeY - min) / (max - min);
            }
            this.betterHitboxSizeYSlider.label = Text.literal((String)("Size Y: " + Math.round(sizeY) + "%"));
            this.betterHitboxSizeYSlider.setBounds(sx, sy, sw, 26);
            this.betterHitboxSizeYSlider.render(ctx, mouseX, mouseY, bg, fill, -1);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.NO_PUSH) {
            GuiConfig cfg = GuiClient.CONFIG;
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            toggleH = 18;
            gap = 8;
            boolean players = cfg.noPushPlayers == null || cfg.noPushPlayers != false;
            this.noPushPlayersAnim = this.drawToggleRow(ctx, sx, sy, sw, toggleH, "Players", players, this.noPushPlayersAnim, accent);
            boolean blocks = cfg.noPushBlocks == null || cfg.noPushBlocks != false;
            this.noPushBlocksAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Blocks", blocks, this.noPushBlocksAnim, accent);
            boolean liquids = cfg.noPushLiquids == null || cfg.noPushLiquids != false;
            this.noPushLiquidsAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Liquids", liquids, this.noPushLiquidsAnim, accent);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.NOJUMP_DELAY) {
            String msg = "Nothing To Show";
            int tw = tr.getWidth(msg);
            tx = innerX + Math.max(0, (innerW - tw) / 2);
            int ty = innerY + 4;
            ctx.drawText(tr, (Text)Text.literal((String)msg), tx, ty, -4605498, false);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.FREECAM) {
            GuiConfig cfg = GuiClient.CONFIG;
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            toggleH = 18;
            gap = 8;
            bg = Theme.argb(150, 20, 20, 26);
            fill = Theme.withAlpha(accent, 255);
            boolean cancelMove = cfg.freecamCancelMove == null || cfg.freecamCancelMove != false;
            this.freecamCancelMoveAnim = this.drawToggleRow(ctx, sx, sy, sw, toggleH, "Cancel Move", cancelMove, this.freecamCancelMoveAnim, accent);
            this.freecamSpeedXSlider.setBounds(sx, sy += toggleH + gap, sw, 26);
            this.freecamSpeedXSlider.render(ctx, mouseX, mouseY, bg, fill, -1);
            this.freecamSpeedYSlider.setBounds(sx, sy += 26 + gap, sw, 26);
            this.freecamSpeedYSlider.render(ctx, mouseX, mouseY, bg, fill, -1);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.NAMETAGS) {
            GuiConfig cfg = GuiClient.CONFIG;
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            toggleH = 18;
            gap = 8;
            bg = Theme.argb(150, 20, 20, 26);
            fill = Theme.withAlpha(accent, 255);
            boolean showDistance = cfg.nametagsShowDistance == null || cfg.nametagsShowDistance != false;
            this.nametagsDistanceAnim = this.drawToggleRow(ctx, sx, sy, sw, toggleH, "Dystans", showDistance, this.nametagsDistanceAnim, accent);
            boolean showName = cfg.nametagsShowName == null || cfg.nametagsShowName != false;
            this.nametagsNameAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Nick", showName, this.nametagsNameAnim, accent);
            boolean showHealth = cfg.nametagsShowHealth == null || cfg.nametagsShowHealth != false;
            this.nametagsHealthAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "HP", showHealth, this.nametagsHealthAnim, accent);
            boolean showEnchant = cfg.nametagsShowEnchantments == null || cfg.nametagsShowEnchantments != false;
            this.nametagsEnchantAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Enchanty", showEnchant, this.nametagsEnchantAnim, accent);
            boolean showSetBonus = cfg.nametagsShowSetBonus == null || cfg.nametagsShowSetBonus != false;
            this.nametagsSetBonusAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Bonusy seta", showSetBonus, this.nametagsSetBonusAnim, accent);
            boolean showEffects = cfg.nametagsShowEffects == null || cfg.nametagsShowEffects != false;
            this.nametagsEffectsAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Efekty", showEffects, this.nametagsEffectsAnim, accent);
            boolean showDurability = cfg.nametagsShowDurability == null || cfg.nametagsShowDurability != false;
            this.nametagsDurabilityAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Paski durability", showDurability, this.nametagsDurabilityAnim, accent);
            boolean showArmor = cfg.nametagsShowArmorSet == null || cfg.nametagsShowArmorSet != false;
            this.nametagsArmorAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Set", showArmor, this.nametagsArmorAnim, accent);
            boolean showHand = cfg.nametagsShowHandItems == null || cfg.nametagsShowHandItems != false;
            this.nametagsHandAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Itemy w rekach", showHand, this.nametagsHandAnim, accent);
            boolean showCount = cfg.nametagsShowItemCount == null || cfg.nametagsShowItemCount != false;
            this.nametagsItemCountAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Ilosc przedmiotow", showCount, this.nametagsItemCountAnim, accent);
            this.nametagsTextColorSlider.setBounds(sx, sy += toggleH + gap, sw, 26);
            this.nametagsTextColorSlider.syncRgb(cfg.nametagsTextColor);
            this.nametagsTextColorSlider.render(ctx, mouseX, mouseY, bg, -1, fill);
            this.nametagsBackgroundColorSlider.setBounds(sx, sy += 26 + gap, sw, 26);
            this.nametagsBackgroundColorSlider.syncRgb(cfg.nametagsBackgroundColor);
            this.nametagsBackgroundColorSlider.render(ctx, mouseX, mouseY, bg, -1, fill);
            this.nametagsBorderColorSlider.setBounds(sx, sy += 26 + gap, sw, 26);
            this.nametagsBorderColorSlider.syncRgb(cfg.nametagsBorderColor);
            this.nametagsBorderColorSlider.render(ctx, mouseX, mouseY, bg, -1, fill);
            this.nametagsTextAlphaSlider.setBounds(sx, sy += 26 + gap, sw, 26);
            this.nametagsTextAlphaSlider.setBaseColor(cfg.nametagsTextColor);
            this.nametagsTextAlphaSlider.syncAlpha(cfg.nametagsTextAlpha);
            this.nametagsTextAlphaSlider.render(ctx, mouseX, mouseY, bg, -1, fill);
            this.nametagsBackgroundAlphaSlider.setBounds(sx, sy += 26 + gap, sw, 26);
            this.nametagsBackgroundAlphaSlider.setBaseColor(cfg.nametagsBackgroundColor);
            this.nametagsBackgroundAlphaSlider.syncAlpha(cfg.nametagsBackgroundAlpha);
            this.nametagsBackgroundAlphaSlider.render(ctx, mouseX, mouseY, bg, -1, fill);
            this.nametagsScaleSlider.setBounds(sx, sy += 26 + gap, sw, 26);
            if (!this.nametagsScaleSlider.dragging) {
                this.nametagsScaleSlider.value = (float)(cfg.nametagsScalePercent - 50) / 150.0f;
            }
            this.nametagsScaleSlider.label = Text.literal((String)("Skala: " + cfg.nametagsScalePercent + "%"));
            this.nametagsScaleSlider.render(ctx, mouseX, mouseY, bg, fill, -1);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.REFILLER) {
            GuiConfig cfg = GuiClient.CONFIG;
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            toggleH = 18;
            gap = 8;
            boolean obsidian = cfg.refillerObsidian == null || cfg.refillerObsidian != false;
            this.refillerObsidianAnim = this.drawToggleRow(ctx, sx, sy, sw, toggleH, "Obsidian", obsidian, this.refillerObsidianAnim, accent);
            boolean cobwebs = cfg.refillerCobwebs == null || cfg.refillerCobwebs != false;
            this.refillerCobwebsAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Cobwebs", cobwebs, this.refillerCobwebsAnim, accent);
            boolean pearls = cfg.refillerPearls == null || cfg.refillerPearls != false;
            this.refillerPearlsAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Pearls", pearls, this.refillerPearlsAnim, accent);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.PANIC_MODE) {
            GuiConfig cfg = GuiClient.CONFIG;
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            toggleH = 18;
            gap = 8;
            boolean arrayList = cfg.panicHideArrayList == null || cfg.panicHideArrayList != false;
            this.panicArrayListAnim = this.drawToggleRow(ctx, sx, sy, sw, toggleH, "ArrayList", arrayList, this.panicArrayListAnim, accent);
            boolean notifications = cfg.panicHideNotifications == null || cfg.panicHideNotifications != false;
            this.panicNotificationsAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Notifications", notifications, this.panicNotificationsAnim, accent);
            boolean keybinds = cfg.panicHideKeybinds == null || cfg.panicHideKeybinds != false;
            this.panicKeybindsAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Keybinds", keybinds, this.panicKeybindsAnim, accent);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.XRAY) {
            sx = innerX;
            sy = innerY;
            sw = innerW;
            toggleH = 18;
            int pad = (int)Math.ceil(R_BTN) + 2;
            btnBg = this.xrayPickerOpen ? Theme.withAlpha(accent, 45) : Theme.argb(150, 20, 20, 26);
            Rounded.rect(ctx, sx, sy, sw, toggleH, R_BTN, btnBg);
            Objects.requireNonNull(tr);
            int ty = sy + (toggleH - 9) / 2;
            ctx.drawText(tr, (Text)Text.literal((String)"Add Blocks+"), sx + pad, ty, -1, false);
            String state = this.xrayPickerOpen ? "CLOSE" : "OPEN";
            int stateW = tr.getWidth(state);
            int stateCol = this.xrayPickerOpen ? -11151510 : Theme.withAlpha(accent, 255);
            ctx.drawText(tr, (Text)Text.literal((String)state), sx + sw - pad - stateW, ty, stateCol, false);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.ITEM_ESP) {
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            toggleH = 18;
            gap = 8;
            int pad = (int)Math.ceil(R_BTN) + 2;
            GuiConfig cfg = GuiClient.CONFIG;
            btnBg = this.itemPickerOpen ? Theme.withAlpha(accent, 45) : Theme.argb(150, 20, 20, 26);
            Rounded.rect(ctx, sx, sy, sw, toggleH, R_BTN, btnBg);
            Objects.requireNonNull(tr);
            int ty = sy + (toggleH - 9) / 2;
            ctx.drawText(tr, (Text)Text.literal((String)"Add Items+"), sx + pad, ty, -1, false);
            String state = this.itemPickerOpen ? "CLOSE" : "OPEN";
            int stateW = tr.getWidth(state);
            int stateCol = this.itemPickerOpen ? -11151510 : Theme.withAlpha(accent, 255);
            ctx.drawText(tr, (Text)Text.literal((String)state), sx + sw - pad - stateW, ty, stateCol, false);
            boolean addAll = cfg.itemEspAddAll != null && cfg.itemEspAddAll != false;
            this.itemEspAddAllAnim = this.drawToggleRow(ctx, sx, sy += toggleH + gap, sw, toggleH, "Add all", addAll, this.itemEspAddAllAnim, accent);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.BLOCK_ESP) {
            sx = innerX;
            sy = innerY;
            sw = innerW;
            toggleH = 18;
            int pad = (int)Math.ceil(R_BTN) + 2;
            btnBg = this.blockPickerOpen ? Theme.withAlpha(accent, 45) : Theme.argb(150, 20, 20, 26);
            Rounded.rect(ctx, sx, sy, sw, toggleH, R_BTN, btnBg);
            Objects.requireNonNull(tr);
            int ty = sy + (toggleH - 9) / 2;
            ctx.drawText(tr, (Text)Text.literal((String)"Add Blocks+"), sx + pad, ty, -1, false);
            String state = this.blockPickerOpen ? "CLOSE" : "OPEN";
            int stateW = tr.getWidth(state);
            int stateCol = this.blockPickerOpen ? -11151510 : Theme.withAlpha(accent, 255);
            ctx.drawText(tr, (Text)Text.literal((String)state), sx + sw - pad - stateW, ty, stateCol, false);
        } else if (this.selectedModule != null && this.selectedModule.id == ModuleId.AUTO_TOTEM) {
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            String msg = "Nothing To Show";
            int tw = tr.getWidth(msg);
            tx = sx + Math.max(0, (sw - tw) / 2);
            int ty = sy + 4;
            ctx.drawText(tr, (Text)Text.literal((String)msg), tx, ty, -4605498, false);
        } else if (this.selectedModule != null) {
            sx = innerX;
            sy = innerY - (int)this.moduleSettingsScroll;
            sw = innerW;
            String msg = "Nothing To Show";
            int tw = tr.getWidth(msg);
            tx = sx + Math.max(0, (sw - tw) / 2);
            int ty = sy + 4;
            ctx.drawText(tr, (Text)Text.literal((String)msg), tx, ty, -4605498, false);
        }
        ctx.disableScissor();
    }

    private void renderConfigs(DrawContext ctx, int mouseX, int mouseY, float delta, int accent, float R_TILE) {
        int listX = this.panelX + 96 + 12;
        int listY = this.panelY + 44 + 10;
        int listW = this.panelW - 96 - 24;
        int listH = this.panelH - 44 - 16;
        ctx.enableScissor(listX, listY, listX + listW, listY + listH);
        int rowH = 24;
        int gap = 8;
        int totalH = this.configNames.size() * (rowH + gap);
        float maxScroll = Math.max(0.0f, (float)(totalH - listH));
        this.configsScrollTarget = GuiScreen.clamp(this.configsScrollTarget, 0.0f, maxScroll);
        this.configsScroll = GuiScreen.lerp(this.configsScroll, this.configsScrollTarget, 0.25f);
        if (Math.abs(this.configsScroll - this.configsScrollTarget) < 0.01f) {
            this.configsScroll = this.configsScrollTarget;
        }
        int yy = listY - (int)this.configsScroll;
        TextRenderer tr = GuiFonts.textRenderer();
        int rowPad = (int)Math.ceil(R_TILE) + 4;
        for (String name : this.configNames) {
            boolean active = name.equalsIgnoreCase(GuiClient.CONFIG.activeConfig);
            int bg = Theme.argb(150, 20, 20, 26);
            if (active) {
                bg = Theme.withAlpha(accent, 40);
            }
            Rounded.rect(ctx, listX, yy, listW, rowH, R_TILE, bg);
            Objects.requireNonNull(tr);
            int textY = yy + (rowH - 9) / 2;
            ctx.drawText(tr, (Text)Text.literal((String)name), listX + rowPad, textY, -1, false);
            String load = "Load";
            String del = "Del";
            int delW = tr.getWidth(del);
            int loadW = tr.getWidth(load);
            int delX = listX + listW - rowPad - delW;
            int loadX = delX - 16 - loadW;
            ctx.drawText(tr, (Text)Text.literal((String)load), loadX, textY, Theme.withAlpha(accent, 255), false);
            ctx.drawText(tr, (Text)Text.literal((String)del), delX, textY, -38037, false);
            yy += rowH + gap;
        }
        ctx.disableScissor();
        String openText = "Open folder";
        int pad = (int)Math.ceil(10.0) + 2;
        int btnH = 18;
        int btnW = Math.max(80, tr.getWidth(openText) + pad * 2);
        int btnX = listX + listW - btnW;
        int btnY = listY + listH - btnH;
        boolean hover = GuiScreen.inside(mouseX, mouseY, btnX, btnY, btnW, btnH);
        int btnBg = hover ? Theme.withAlpha(accent, 55) : Theme.argb(150, 20, 20, 26);
        Rounded.rect(ctx, btnX, btnY, btnW, btnH, 10.0f, btnBg);
        int tx = btnX + (btnW - tr.getWidth(openText)) / 2;
        Objects.requireNonNull(tr);
        int ty = btnY + (btnH - 9) / 2;
        ctx.drawText(tr, (Text)Text.literal((String)openText), tx, ty, Theme.withAlpha(accent, 255), false);
    }

    private List<String> filteredFriends(String query) {
        List<String> all = GuiClient.FRIENDS.list();
        if (query == null || query.isBlank()) {
            return all;
        }
        String q = query.toLowerCase();
        ArrayList<String> out = new ArrayList<String>();
        for (String f : all) {
            boolean matchDisplay;
            if (f == null) continue;
            String display = GuiClient.FRIENDS.getDisplayName(f);
            boolean matchName = f.toLowerCase().contains(q);
            boolean bl = matchDisplay = display != null && display.toLowerCase().contains(q);
            if (!matchName && !matchDisplay) continue;
            out.add(f);
        }
        return out;
    }

    private void renderFriends(DrawContext ctx, int mouseX, int mouseY, float delta, int accent, float R_TILE) {
        String query = this.friendSearchField.field.isVisible() ? this.friendSearchField.field.getText().trim().toLowerCase() : "";
        List<String> friends = this.filteredFriends(query);
        int listX = this.panelX + 96 + 12;
        int listY = this.panelY + 44 + 10;
        int listW = this.panelW - 96 - 24;
        int listH = this.panelH - 44 - 16;
        if (listH < 0) {
            listH = 0;
        }
        GuiConfig cfg = GuiClient.CONFIG;
        TextRenderer tr = GuiFonts.textRenderer();
        int headerH = 24;
        int headerGap = 8;
        int colGap = 10;
        int colW = (listW - colGap) / 2;
        if (colW < 0) {
            colW = 0;
        }
        int rowPad = (int)Math.ceil(R_TILE) + 4;
        int toggleH = 18;
        int togglePad = 6;
        String toggleLabel = "Change Normal Nametag";
        int toggleTextW = tr.getWidth(toggleLabel);
        int toggleMinW = toggleTextW + (int)Math.ceil(10.0) * 2 + 26 + 8;
        int toggleW = Math.min(listW - togglePad * 2, Math.max(90, toggleMinW));
        if (toggleW < 0) {
            toggleW = listW;
        }
        int toggleX = listX + listW - toggleW - togglePad;
        int toggleY = listY + listH - toggleH - togglePad;
        int listTop = listY + headerH + headerGap;
        int reservedBottom = toggleH + togglePad;
        int listViewH = listH - (headerH + headerGap + reservedBottom);
        if (listViewH < 0) {
            listViewH = 0;
        }
        int headerBg = Theme.argb(150, 20, 20, 26);
        if (!this.friendAddExpanded && this.friendAddW > 0) {
            int addBg = Theme.argb(150, 20, 20, 26);
            Rounded.rect(ctx, this.friendAddX, this.friendAddY, this.friendAddW, this.friendAddH, 10.0f, addBg);
            String addText = "Add";
            int addW = tr.getWidth(addText);
            int addTx = this.friendAddX + (this.friendAddW - addW) / 2;
            Objects.requireNonNull(tr);
            int addTy = this.friendAddY + (this.friendAddH - 9) / 2;
            ctx.drawText(tr, (Text)Text.literal((String)addText), addTx, addTy, Theme.withAlpha(accent, 255), false);
        }
        float frameR = 2.0f;
        int frameBg = Theme.argb(110, 16, 16, 22);
        Rounded.rect(ctx, listX, listY, listW, listH, frameR, frameBg);
        Rounded.outline(ctx, listX, listY, listW, listH, frameR, 1, Theme.withAlpha(accent, 140));
        int dividerW = 2;
        int dividerX = listX + colW + (colGap - dividerW) / 2;
        Rounded.rect(ctx, dividerX, listY + 2, dividerW, Math.max(0, listH - 4), 0.0f, Theme.argb(120, 50, 50, 58));
        Rounded.rect(ctx, listX, listY, colW, headerH, 10.0f, headerBg);
        Rounded.rect(ctx, listX + colW + colGap, listY, colW, headerH, 10.0f, headerBg);
        Objects.requireNonNull(tr);
        int headerTy = listY + (headerH - 9) / 2;
        ctx.drawText(tr, (Text)Text.literal((String)"Friends"), listX + rowPad, headerTy, -1, false);
        ctx.drawText(tr, (Text)Text.literal((String)"Display Name"), listX + colW + colGap + rowPad, headerTy, -1, false);
        for (UiTextField field : this.displayNameFields.values()) {
            field.field.setVisible(false);
        }
        if (listViewH > 0) {
            ctx.enableScissor(listX, listTop, listX + listW, listTop + listViewH);
            int rowH = 24;
            int gap = 8;
            int totalH = friends.size() * (rowH + gap);
            float maxScroll = Math.max(0.0f, (float)(totalH - listViewH));
            this.friendsScrollTarget = GuiScreen.clamp(this.friendsScrollTarget, 0.0f, maxScroll);
            this.friendsScroll = GuiScreen.lerp(this.friendsScroll, this.friendsScrollTarget, 0.25f);
            if (Math.abs(this.friendsScroll - this.friendsScrollTarget) < 0.01f) {
                this.friendsScroll = this.friendsScrollTarget;
            }
            int yy = listTop - (int)this.friendsScroll;
            int rightX = listX + colW + colGap;
            for (String f : friends) {
                Rounded.rect(ctx, listX, yy, colW, rowH, R_TILE, Theme.argb(150, 20, 20, 26));
                Objects.requireNonNull(tr);
                int textY = yy + (rowH - 9) / 2;
                ctx.drawText(tr, (Text)Text.literal((String)f), listX + rowPad, textY, -1, false);
                String del = "Remove";
                int delW = tr.getWidth(del);
                int delX = listX + colW - rowPad - delW;
                ctx.drawText(tr, (Text)Text.literal((String)del), delX, textY, -38037, false);
                UiTextField field = this.displayNameField(f);
                field.setPos(rightX, yy);
                field.setSize(colW, rowH);
                field.field.setVisible(true);
                if (!field.field.isFocused()) {
                    String desired = GuiClient.FRIENDS.getDisplayName(f);
                    if (!field.field.getText().equals(desired)) {
                        field.field.setText(desired);
                    }
                }
                field.render(ctx, mouseX, mouseY, delta);
                GuiClient.FRIENDS.setDisplayName(f, field.field.getText());
                yy += rowH + gap;
            }
            if (friends.isEmpty()) {
                String empty = query.isBlank() ? "No friends" : "No matches";
                int ew = tr.getWidth(empty);
                int ex = listX + (listW - ew) / 2;
                Objects.requireNonNull(tr);
                int ey = listTop + (listViewH - 9) / 2;
                ctx.drawText(tr, (Text)Text.literal((String)empty), ex, ey, -6643544, false);
            }
            ctx.disableScissor();
        }
        boolean changeNormal = cfg.friendsChangeNormalNametag == null || cfg.friendsChangeNormalNametag != false;
        this.friendsChangeNametagAnim = this.drawToggleRow(ctx, toggleX, toggleY, toggleW, toggleH, toggleLabel, changeNormal, this.friendsChangeNametagAnim, accent);
    }

    private void renderSettings(DrawContext ctx, int mouseX, int mouseY, float delta, int accent, float R_TILE, float R_BTN) {
        this.searchField.field.setVisible(false);
        TextRenderer tr = GuiFonts.textRenderer();
        int sx = this.panelX + 96 + 12;
        int sy = this.panelY + 12;
        int bw = 96;
        int bh = 18;
        int gapTabs = 8;
        int bg1 = this.settingsSub == SettingsSub.COLORS ? Theme.withAlpha(accent, 45) : Theme.argb(150, 20, 20, 26);
        int bg2 = this.settingsSub == SettingsSub.GUI ? Theme.withAlpha(accent, 45) : Theme.argb(150, 20, 20, 26);
        int bg3 = this.settingsSub == SettingsSub.HUD ? Theme.withAlpha(accent, 45) : Theme.argb(150, 20, 20, 26);
        Rounded.rect(ctx, sx, sy, bw, bh, R_BTN, bg1);
        Rounded.rect(ctx, sx + bw + gapTabs, sy, bw, bh, R_BTN, bg2);
        Rounded.rect(ctx, sx + 2 * (bw + gapTabs), sy, bw, bh, R_BTN, bg3);
        int topPad = (int)Math.ceil(R_BTN) + 2;
        Objects.requireNonNull(tr);
        int topTy = sy + (bh - 9) / 2;
        ctx.drawText(tr, (Text)Text.literal((String)"Colors"), sx + topPad, topTy, -1, false);
        ctx.drawText(tr, (Text)Text.literal((String)"Gui"), sx + bw + gapTabs + topPad, topTy, -1, false);
        ctx.drawText(tr, (Text)Text.literal((String)"Hud"), sx + 2 * (bw + gapTabs) + topPad, topTy, -1, false);
        int contentX = this.panelX + 96;
        int x = contentX + 12;
        int y = this.panelY + 44 + 10;
        int w = this.panelW - 96 - 24;
        int sh = 26;
        int gap = 8;
        int bg = Theme.argb(150, 20, 20, 26);
        int fill = Theme.withAlpha(accent, 255);
        if (this.settingsSub == SettingsSub.COLORS) {
            GuiConfig cfg = GuiClient.CONFIG;
            int modeW = 110;
            int modeH = 18;
            this.drawModeButton(ctx, x, y, modeW, modeH, "Single", cfg.colorMode == GuiConfig.ColorMode.SINGLE, accent, R_BTN);
            this.drawModeButton(ctx, x, y + (modeH + gap), modeW, modeH, "Gradient", cfg.colorMode == GuiConfig.ColorMode.GRADIENT, accent, R_BTN);
            this.drawModeButton(ctx, x, y + 2 * (modeH + gap), modeW, modeH, "Rainbow", cfg.colorMode == GuiConfig.ColorMode.RAINBOW, accent, R_BTN);
            int sx2 = x + modeW + 14;
            int sw2 = w - modeW - 14;
            int yy = y;
            if (cfg.colorMode == GuiConfig.ColorMode.SINGLE) {
                this.hueSingle.setBounds(sx2, yy, sw2, sh);
                this.hueSingle.render(ctx, mouseX, mouseY, bg, fill, -1);
            } else if (cfg.colorMode == GuiConfig.ColorMode.GRADIENT) {
                this.hueA.setBounds(sx2, yy, sw2, sh);
                this.hueA.render(ctx, mouseX, mouseY, bg, fill, -1);
                this.hueB.setBounds(sx2, yy += sh + gap, sw2, sh);
                this.hueB.render(ctx, mouseX, mouseY, bg, fill, -1);
                this.gradSpeed.setBounds(sx2, yy += sh + gap, sw2, sh);
                this.gradSpeed.render(ctx, mouseX, mouseY, bg, fill, -1);
            } else {
                this.rainbowSpeed.setBounds(sx2, yy, sw2, sh);
                this.rainbowSpeed.render(ctx, mouseX, mouseY, bg, fill, -1);
            }
        } else if (this.settingsSub == SettingsSub.GUI) {
            int yy = y;
            this.transparency.setBounds(x, yy, w, sh);
            this.transparency.render(ctx, mouseX, mouseY, bg, fill, -1);
            this.uiVolume.setBounds(x, yy += sh + gap, w, sh);
            this.uiVolume.render(ctx, mouseX, mouseY, bg, fill, -1);
            this.guiScale.setBounds(x, yy += sh + gap, w, sh);
            this.guiScale.render(ctx, mouseX, mouseY, bg, fill, -1);
        } else {
            this.hudScroll = 0.0f;
            this.hudScrollTarget = 0.0f;
            int btnH = 18;
            int btnX = x;
            int btnY = y;
            int btnW = w;
            int btnBg = Theme.withAlpha(accent, 55);
            Rounded.rect(ctx, btnX, btnY, btnW, btnH, R_BTN, btnBg);
            String label = "Edit Hud";
            int tw = tr.getWidth(label);
            Objects.requireNonNull(tr);
            int ty = btnY + (btnH - 9) / 2;
            ctx.drawText(tr, (Text)Text.literal((String)label), btnX + (btnW - tw) / 2, ty, -1, false);
        }
    }

    private void renderGearRender(DrawContext ctx, int mouseX, int mouseY, float delta, int accent, float R_TILE, float R_BTN) {
        int unknownSwordW;
        this.searchField.field.setVisible(false);
        this.gearRenderButtons.clear();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        TextRenderer tr = GuiFonts.textRenderer();
        int areaX = this.panelX + 96 + 12;
        int areaY = this.panelY + 12;
        int areaW = this.panelW - 96 - 24;
        int areaH = this.panelH - 24;
        if (areaW <= 0 || areaH <= 0) {
            return;
        }
        int bg = Theme.argb(150, 18, 18, 22);
        int outline = Theme.withAlpha(accent, 140);
        Rounded.rect(ctx, areaX, areaY, areaW, areaH, R_TILE, bg);
        Rounded.outline(ctx, areaX, areaY, areaW, areaH, R_TILE, 1, outline);
        int titleX = areaX + 12;
        int titleY = areaY + 8;
        ctx.drawText(tr, (Text)Text.literal((String)"GEAR RENDER"), titleX, titleY, Theme.withAlpha(accent, 255), false);
        if (!NameProtectUtil.isOnAnarchiaServer()) {
            String msg = "Doesn't work on this server.";
            int mw = tr.getWidth(msg);
            int mx = areaX + (areaW - mw) / 2;
            int n = areaY + areaH / 2;
            Objects.requireNonNull(tr);
            int my = n - 9 / 2;
            ctx.drawText(tr, (Text)Text.literal((String)msg), mx, my, -4605498, false);
            return;
        }
        GearRenderUtil.ensureRefreshed(client);
        List<GearRenderUtil.GearEntry> inRange = GearRenderUtil.getInRange();
        List<GearRenderUtil.MissingEntry> missing = GearRenderUtil.getMissing();
        int innerX = areaX + 12;
        int innerY = areaY + 28;
        int innerW = areaW - 24;
        Objects.requireNonNull(tr);
        int reservedBottom = 18 + 9 + 12;
        int innerH = areaH - 52 - reservedBottom;
        if (innerH < 0) {
            innerH = 0;
        }
        int colGap = 12;
        int colW = (innerW - colGap) / 2;
        int leftX = innerX;
        int rightX = innerX + colW + colGap;
        int headerY = innerY;
        String leftTitle = "W ZASIEGU (" + inRange.size() + ")";
        String rightTitle = "ZNIKNIECI (" + missing.size() + ")";
        ctx.drawText(tr, (Text)Text.literal((String)leftTitle), leftX + 4, headerY, -1, false);
        ctx.drawText(tr, (Text)Text.literal((String)rightTitle), rightX + 4, headerY, -1, false);
        String unknownSword = "?";
        int leftSwordTextW = unknownSwordW = tr.getWidth(unknownSword);
        for (GearRenderUtil.GearEntry gearEntry : inRange) {
            String s = this.swordExtraLabel(gearEntry.sawSword, gearEntry.swordExtra);
            leftSwordTextW = Math.max(leftSwordTextW, tr.getWidth(s));
        }
        int rightSwordTextW = unknownSwordW;
        for (GearRenderUtil.MissingEntry e : missing) {
            String s = this.swordExtraLabel(e.sawSword, e.swordExtra);
            rightSwordTextW = Math.max(rightSwordTextW, tr.getWidth(s));
        }
        ItemStack itemStack = new ItemStack((ItemConvertible)Items.NETHERITE_SWORD);
        Objects.requireNonNull(tr);
        int rowY = headerY + 9 + 6;
        int rowH = 26;
        int maxRows = Math.max(0, (innerY + innerH - rowY) / rowH);
        int rowBg = Theme.argb(90, 18, 18, 22);
        int iconSize = 18;
        int iconGap = 3;
        int iconCount = 5;
        int iconsTotal = iconCount * iconSize + (iconCount - 1) * iconGap;
        int leftIconsWithText = iconsTotal + 2 + leftSwordTextW;
        int leftIconsX = leftX + colW - leftIconsWithText - 4;
        int leftTextX = leftIconsX + iconsTotal + 2;
        int leftNameX = leftX + 4;
        for (int i = 0; i < inRange.size() && i < maxRows; ++i) {
            GearRenderUtil.GearEntry e = inRange.get(i);
            int y = rowY + i * rowH;
            Rounded.rect(ctx, leftX, y, colW, rowH - 2, 4.0f, rowBg);
            Objects.requireNonNull(tr);
            int ty = y + (rowH - 9) / 2;
            ctx.drawText(tr, (Text)Text.literal((String)e.name), leftNameX, ty, -1, false);
            int iconY = y + (rowH - iconSize) / 2;
            this.drawGearSlot(ctx, leftIconsX, iconY, iconSize, e.helmet);
            this.drawGearSlot(ctx, leftIconsX + (iconSize + iconGap), iconY, iconSize, e.chest);
            this.drawGearSlot(ctx, leftIconsX + 2 * (iconSize + iconGap), iconY, iconSize, e.legs);
            this.drawGearSlot(ctx, leftIconsX + 3 * (iconSize + iconGap), iconY, iconSize, e.boots);
            int swordSlotX = leftIconsX + 4 * (iconSize + iconGap);
            this.drawGearSlot(ctx, swordSlotX, iconY, iconSize, e.sawSword ? itemStack : ItemStack.EMPTY);
            if (!e.sawSword) {
                String q = "?";
                int qw = tr.getWidth(q);
                MutableText mutableText = Text.literal((String)q);
                int n = swordSlotX + (iconSize - qw) / 2;
                Objects.requireNonNull(tr);
                ctx.drawText(tr, (Text)mutableText, n, iconY + (iconSize - 9) / 2, -4605498, false);
            }
            String swordText = this.swordExtraLabel(e.sawSword, e.swordExtra);
            ctx.drawText(tr, (Text)Text.literal((String)swordText), leftTextX, ty, -4605498, false);
        }
        int rightNameX = rightX + 4;
        int btnGap = 6;
        int buttonsW = 150 + btnGap * 2;
        int buttonsX = rightX + colW - buttonsW - 4;
        int rightIconsEnd = buttonsX - 6;
        int rightIconsWithText = iconsTotal + 2 + rightSwordTextW;
        int rightIconsX = rightIconsEnd - rightIconsWithText;
        if (rightIconsX < rightX + 4) {
            rightIconsX = rightX + 4;
        }
        int rightTextX = rightIconsX + iconsTotal + 2;
        for (int i = 0; i < missing.size() && i < maxRows; ++i) {
            GearRenderUtil.MissingEntry e = missing.get(i);
            int y = rowY + i * rowH;
            Rounded.rect(ctx, rightX, y, colW, rowH - 2, 4.0f, rowBg);
            Objects.requireNonNull(tr);
            int ty = y + (rowH - 9) / 2;
            ctx.drawText(tr, (Text)Text.literal((String)e.name), rightNameX, ty, -1, false);
            int btnY = y + (rowH - 18) / 2;
            int tpaX = buttonsX;
            int blockX = tpaX + 50 + btnGap;
            int statsX = blockX + 50 + btnGap;
            this.drawGearButton(ctx, tpaX, btnY, 50, 18, "TPA", accent, mouseX, mouseY);
            this.drawGearButton(ctx, blockX, btnY, 50, 18, "BLOCK", accent, mouseX, mouseY);
            this.drawGearButton(ctx, statsX, btnY, 50, 18, "STATS", accent, mouseX, mouseY);
            this.gearRenderButtons.add(new GearRenderButton(GearRenderButtonType.TPA, e.name, tpaX, btnY, 50, 18));
            this.gearRenderButtons.add(new GearRenderButton(GearRenderButtonType.BLOCK, e.name, blockX, btnY, 50, 18));
            this.gearRenderButtons.add(new GearRenderButton(GearRenderButtonType.STATS, e.name, statsX, btnY, 50, 18));
            int iconY = y + (rowH - iconSize) / 2;
            this.drawGearSlot(ctx, rightIconsX, iconY, iconSize, e.helmet);
            this.drawGearSlot(ctx, rightIconsX + (iconSize + iconGap), iconY, iconSize, e.chest);
            this.drawGearSlot(ctx, rightIconsX + 2 * (iconSize + iconGap), iconY, iconSize, e.legs);
            this.drawGearSlot(ctx, rightIconsX + 3 * (iconSize + iconGap), iconY, iconSize, e.boots);
            int swordSlotX = rightIconsX + 4 * (iconSize + iconGap);
            this.drawGearSlot(ctx, swordSlotX, iconY, iconSize, e.sawSword ? itemStack : ItemStack.EMPTY);
            if (!e.sawSword) {
                String q = "?";
                int qw = tr.getWidth(q);
                MutableText mutableText = Text.literal((String)q);
                int n = swordSlotX + (iconSize - qw) / 2;
                Objects.requireNonNull(tr);
                ctx.drawText(tr, (Text)mutableText, n, iconY + (iconSize - 9) / 2, -4605498, false);
            }
            String swordText = this.swordExtraLabel(e.sawSword, e.swordExtra);
            ctx.drawText(tr, (Text)Text.literal((String)swordText), rightTextX, ty, -4605498, false);
        }
        String footer = "Auto refresh co 2s";
        Objects.requireNonNull(tr);
        int footerY = areaY + areaH - 9 - 6;
        boolean autoTpa = GuiClient.CONFIG.gearRenderAutoTpa != null && GuiClient.CONFIG.gearRenderAutoTpa != false;
        int toggleW = colW;
        int toggleX = leftX;
        int toggleY = footerY - 18 - 6;
        this.gearAutoTpaAnim = this.drawToggleRow(ctx, toggleX, toggleY, toggleW, 18, "Auto TP", autoTpa, this.gearAutoTpaAnim, accent);
        this.gearRenderButtons.add(new GearRenderButton(GearRenderButtonType.AUTO_TP, null, toggleX, toggleY, toggleW, 18));
        int resetW = 70;
        int resetH = 18;
        int resetX = areaX + areaW - resetW - 10;
        int resetY = areaY + areaH - resetH - 6;
        String chLabel = "CHANGE CH";
        int chW = Math.max(90, tr.getWidth(chLabel) + 16);
        int chX = resetX - chW - 8;
        int chY = resetY;
        if (chX < areaX + 10) {
            chX = areaX + 10;
        }
        String blLabel = "BLACKLIST";
        int blW = Math.max(90, tr.getWidth(blLabel) + 16);
        int blX = chX - blW - 8;
        int blY = resetY;
        if (blX < areaX + 10) {
            blX = areaX + 10;
        }
        this.drawGearButton(ctx, blX, blY, blW, resetH, blLabel, accent, mouseX, mouseY);
        this.gearRenderButtons.add(new GearRenderButton(GearRenderButtonType.BLACKLIST, null, blX, blY, blW, resetH));
        this.drawGearButton(ctx, chX, chY, chW, resetH, chLabel, accent, mouseX, mouseY);
        this.gearRenderButtons.add(new GearRenderButton(GearRenderButtonType.CHANGE_CH, null, chX, chY, chW, resetH));
        this.drawGearButton(ctx, resetX, resetY, resetW, resetH, "RESET", accent, mouseX, mouseY);
        this.gearRenderButtons.add(new GearRenderButton(GearRenderButtonType.RESET, null, resetX, resetY, resetW, resetH));
        ctx.drawText(tr, (Text)Text.literal((String)footer), leftX + 4, footerY, -6643544, false);
    }

    private void drawGearSlot(DrawContext ctx, int x, int y, int size, ItemStack stack) {
        int bg = Theme.argb(130, 20, 20, 26);
        Rounded.rect(ctx, x, y, size, size, 3.0f, bg);
        if (stack != null && !stack.isEmpty()) {
            int iconX = x + (size - 16) / 2;
            int iconY = y + (size - 16) / 2;
            ctx.drawItemWithoutEntity(stack, iconX, iconY);
        }
    }

    private String swordExtraLabel(boolean sawSword, String swordExtra) {
        if (!sawSword) {
            return "?";
        }
        if (swordExtra == null || swordExtra.isBlank()) {
            return "?";
        }
        return swordExtra;
    }

    private void drawGearButton(DrawContext ctx, int x, int y, int w, int h, String label, int accent, int mouseX, int mouseY) {
        boolean hover = GuiScreen.inside(mouseX, mouseY, x, y, w, h);
        int bg = hover ? Theme.withAlpha(accent, 90) : Theme.argb(140, 20, 20, 26);
        Rounded.rect(ctx, x, y, w, h, 4.0f, bg);
        TextRenderer tr = GuiFonts.textRenderer();
        int tw = tr.getWidth(label);
        int tx = x + (w - tw) / 2;
        Objects.requireNonNull(tr);
        int ty = y + (h - 9) / 2;
        ctx.drawText(tr, (Text)Text.literal((String)label), tx, ty, -1, false);
    }

    private void drawModeButton(DrawContext ctx, int x, int y, int w, int h, String text, boolean selected, int accent, float radius) {
        int bg = selected ? Theme.withAlpha(accent, 45) : Theme.argb(150, 20, 20, 26);
        Rounded.rect(ctx, x, y, w, h, radius, bg);
        TextRenderer tr = GuiFonts.textRenderer();
        int pad = (int)Math.ceil(radius) + 2;
        Objects.requireNonNull(tr);
        int ty = y + (h - 9) / 2;
        ctx.drawText(tr, (Text)Text.literal((String)text), x + pad, ty, -1, false);
    }

    private void drawBindRow(DrawContext ctx, int x, int y, int w, int h, String label, String value, int accent, boolean listening) {
        int bg = Theme.argb(150, 20, 20, 26);
        Rounded.rect(ctx, x, y, w, h, 10.0f, bg);
        TextRenderer tr = GuiFonts.textRenderer();
        int pad = (int)Math.ceil(10.0) + 2;
        Objects.requireNonNull(tr);
        int ty = y + (h - 9) / 2;
        ctx.drawText(tr, (Text)Text.literal((String)label), x + pad, ty, -1, false);
        String v = value == null || value.isBlank() ? "None" : value;
        int col = listening ? Theme.withAlpha(accent, 255) : -4605498;
        int tw = tr.getWidth(v);
        int tx = x + w - pad - tw;
        ctx.drawText(tr, (Text)Text.literal((String)v), tx, ty, col, false);
    }

    private float drawToggleRow(DrawContext ctx, int x, int y, int w, int h, String text, boolean enabled, float anim, int accent) {
        int bg = Theme.argb(150, 20, 20, 26);
        Rounded.rect(ctx, x, y, w, h, 10.0f, bg);
        TextRenderer tr = GuiFonts.textRenderer();
        int pad = (int)Math.ceil(10.0) + 2;
        Objects.requireNonNull(tr);
        int ty = y + (h - 9) / 2;
        ctx.drawText(tr, (Text)Text.literal((String)text), x + pad, ty, -1, false);
        int sw = 26;
        int sh = 12;
        int sx = x + w - pad - sw;
        int sy = y + (h - sh) / 2;
        float target = enabled ? 1.0f : 0.0f;
        anim = GuiScreen.lerp(anim, target, 0.25f);
        if (anim > 0.01f) {
            int barW = 1;
            int barH = Math.round((float)(h - 6) * anim);
            int barX = x + 4;
            int barY = y + (h - barH) / 2;
            int barCol = Theme.withAlpha(accent, (int)(120.0f + 80.0f * anim));
            ctx.fill(barX, barY, barX + barW, barY + barH, barCol);
        }
        this.drawToggleIcon(ctx, sx, sy, sw, sh, anim, accent);
        return anim;
    }

    private void withAlpha(float alpha, Runnable draw) {
        float a = Math.max(0.0f, Math.min(1.0f, alpha));
        float[] sc = RenderSystem.getShaderColor();
        RenderSystem.setShaderColor((float)sc[0], (float)sc[1], (float)sc[2], (float)(sc[3] * a));
        draw.run();
        RenderSystem.setShaderColor((float)sc[0], (float)sc[1], (float)sc[2], (float)sc[3]);
    }

    private int revealOffset(float t) {
        float clamped = Math.max(0.0f, Math.min(1.0f, t));
        return Math.round((1.0f - clamped) * 8.0f);
    }

    private void drawSubHeaderRow(DrawContext ctx, int x, int y, int w, String text, int accent) {
        TextRenderer tr = GuiFonts.textRenderer();
        int pad = (int)Math.ceil(10.0) + 2;
        Objects.requireNonNull(tr);
        int ty = y + (18 - 9) / 2;
        int barW = 3;
        Objects.requireNonNull(tr);
        int barH = 9 + 2;
        int barY = y + (18 - barH) / 2;
        ctx.fill(x, barY, x + barW, barY + barH, Theme.withAlpha(accent, 190));
        ctx.drawText(tr, (Text)Text.literal((String)text), x + barW + pad, ty, -4605498, false);
    }

    private void drawToggleIcon(DrawContext ctx, int x, int y, int w, int h, float t, int accent) {
        int off = Theme.argb(150, 90, 90, 100);
        int on = Theme.withAlpha(accent, 190);
        int pill = GuiScreen.lerpColor(off, on, t);
        int knob = Theme.argb(220, 230, 230, 236);
        float r = (float)h * 0.5f;
        Rounded.rect(ctx, x, y, w, h, r, pill);
        int knobSize = Math.max(2, h - 2);
        int travel = Math.max(0, w - knobSize - 2);
        int knobX = x + 1 + Math.round((float)travel * t);
        int knobY = y + 1;
        Rounded.rect(ctx, knobX, knobY, knobSize, knobSize, (float)knobSize * 0.5f, knob);
    }

    private List<BlockEntry> filterBlockEntries(String query) {
        List<BlockEntry> all = this.allBlockEntries();
        if (query == null || query.isBlank()) {
            return all;
        }
        String q = query.toLowerCase();
        ArrayList<BlockEntry> out = new ArrayList<BlockEntry>();
        for (BlockEntry e : all) {
            if (!e.nameLower.contains(q) && !e.id.contains(q)) continue;
            out.add(e);
        }
        return out;
    }

    private List<BlockEntry> filterEntriesByQuery(List<BlockEntry> entries, String query) {
        if (entries == null) {
            return new ArrayList<BlockEntry>();
        }
        if (query == null || query.isBlank()) {
            return entries;
        }
        String q = query.toLowerCase();
        ArrayList<BlockEntry> out = new ArrayList<BlockEntry>();
        for (BlockEntry e : entries) {
            if (!e.nameLower.contains(q) && !e.id.contains(q)) continue;
            out.add(e);
        }
        return out;
    }

    private List<BlockEntry> availableBlockEntries(boolean xray, String query) {
        List<String> ids;
        List<BlockEntry> base = this.filterBlockEntries(query);
        ArrayList<BlockEntry> out = new ArrayList<BlockEntry>(base);
        List<String> list = ids = xray ? GuiClient.CONFIG.xrayIds : GuiClient.CONFIG.blockEspIds;
        if (ids != null && !ids.isEmpty()) {
            HashSet<String> selected = new HashSet<String>();
            for (String id : ids) {
                selected.add(GuiScreen.canonicalBlockId(id));
            }
            out.removeIf(e -> selected.contains(GuiScreen.canonicalBlockId(e.id)));
        }
        return out;
    }

    private List<BlockEntry> filterItemEntries(String query) {
        List<BlockEntry> all = this.allItemEntries();
        if (query == null || query.isBlank()) {
            return all;
        }
        String q = query.toLowerCase();
        ArrayList<BlockEntry> out = new ArrayList<BlockEntry>();
        for (BlockEntry e : all) {
            if (!e.nameLower.contains(q) && !e.id.contains(q)) continue;
            out.add(e);
        }
        return out;
    }

    private List<BlockEntry> availableItemEntries(String query) {
        List<BlockEntry> base = this.filterItemEntries(query);
        ArrayList<BlockEntry> out = new ArrayList<BlockEntry>(base);
        List<String> ids = GuiClient.CONFIG.itemEspIds;
        if (ids != null && !ids.isEmpty()) {
            out.removeIf(e -> ids.contains(e.id));
        }
        return out;
    }

    private List<BlockEntry> allBlockEntries() {
        String canonicalId;
        String rawId;
        if (this.blockEntries != null) {
            return this.blockEntries;
        }
        this.blockEntries = new ArrayList<BlockEntry>();
        this.blockEntryById = new HashMap<String, BlockEntry>();
        ArrayList<Identifier> ids = new ArrayList<>(Registries.BLOCK.getIds());
        for (Identifier id : ids) {
            rawId = id.toString();
            if (!rawId.equals(canonicalId = GuiScreen.canonicalBlockId(rawId))) continue;
            Block block = (Block)Registries.BLOCK.get(id);
            String name = block.getName().getString();
            Item item = block.asItem();
            ItemStack stack = item == Items.AIR ? ItemStack.EMPTY : new ItemStack((ItemConvertible)item);
            BlockEntry entry = new BlockEntry(rawId, name, stack);
            this.blockEntries.add(entry);
            this.blockEntryById.put(rawId, entry);
        }
        for (Identifier id : ids) {
            rawId = id.toString();
            if (rawId.equals(canonicalId = GuiScreen.canonicalBlockId(rawId))) continue;
            BlockEntry canonical = this.blockEntryById.get(canonicalId);
            if (canonical != null) {
                this.blockEntryById.put(rawId, canonical);
                continue;
            }
            Block block = (Block)Registries.BLOCK.get(id);
            String name = block.getName().getString();
            Item item = block.asItem();
            ItemStack stack = item == Items.AIR ? ItemStack.EMPTY : new ItemStack((ItemConvertible)item);
            BlockEntry entry = new BlockEntry(rawId, name, stack);
            this.blockEntries.add(entry);
            this.blockEntryById.put(rawId, entry);
        }
        this.appendPlaceableItemAliases(this.blockEntries, this.blockEntryById);
        this.blockEntries.sort(Comparator.comparing(a -> a.name, String.CASE_INSENSITIVE_ORDER));
        return this.blockEntries;
    }

    private List<BlockEntry> allItemEntries() {
        if (this.itemEntries != null) {
            return this.itemEntries;
        }
        this.itemEntries = new ArrayList<BlockEntry>();
        this.itemEntryById = new HashMap<String, BlockEntry>();
        for (Identifier id : Registries.ITEM.getIds()) {
            Item item = (Item)Registries.ITEM.get(id);
            if (item == Items.AIR) continue;
            String name = item.getName().getString();
            ItemStack stack = new ItemStack((ItemConvertible)item);
            BlockEntry entry = new BlockEntry(id.toString(), name, stack);
            this.itemEntries.add(entry);
            this.itemEntryById.put(entry.id, entry);
        }
        this.itemEntries.sort(Comparator.comparing(a -> a.name, String.CASE_INSENSITIVE_ORDER));
        return this.itemEntries;
    }

    private void appendPlaceableItemAliases(List<BlockEntry> list, Map<String, BlockEntry> byId) {
        this.addAliasEntry(list, byId, Items.STRING, Blocks.TRIPWIRE);
        this.addAliasEntry(list, byId, Items.REDSTONE, Blocks.REDSTONE_WIRE);
        this.addAliasEntry(list, byId, Items.WHEAT_SEEDS, Blocks.WHEAT);
        this.addAliasEntry(list, byId, Items.BEETROOT_SEEDS, Blocks.BEETROOTS);
        this.addAliasEntry(list, byId, Items.PUMPKIN_SEEDS, Blocks.PUMPKIN_STEM);
        this.addAliasEntry(list, byId, Items.MELON_SEEDS, Blocks.MELON_STEM);
        this.addAliasEntry(list, byId, Items.CARROT, Blocks.CARROTS);
        this.addAliasEntry(list, byId, Items.POTATO, Blocks.POTATOES);
        this.addAliasEntry(list, byId, Items.NETHER_WART, Blocks.NETHER_WART);
        this.addAliasEntry(list, byId, Items.COCOA_BEANS, Blocks.COCOA);
        this.addAliasEntry(list, byId, Items.SWEET_BERRIES, Blocks.SWEET_BERRY_BUSH);
        this.addAliasEntry(list, byId, Items.GLOW_BERRIES, Blocks.CAVE_VINES);
    }

    private void addAliasEntry(List<BlockEntry> list, Map<String, BlockEntry> byId, Item item, Block block) {
        if (item == null || block == null) {
            return;
        }
        String id = Registries.BLOCK.getId(block).toString();
        if (id == null || id.isBlank()) {
            return;
        }
        String name = item.getName().getString();
        ItemStack stack = new ItemStack((ItemConvertible)item);
        BlockEntry entry = new BlockEntry(id, name, stack);
        list.add(entry);
        if (byId != null) {
            byId.put(id, entry);
        }
    }

    private boolean isBlockSelected(String id) {
        List<String> ids = GuiClient.CONFIG.blockEspIds;
        if (ids == null || id == null) {
            return false;
        }
        String canonical = GuiScreen.canonicalBlockId(id);
        for (String v : ids) {
            if (!canonical.equals(GuiScreen.canonicalBlockId(v))) continue;
            return true;
        }
        return false;
    }

    private void toggleBlockSelection(String id) {
        boolean removed;
        if (id == null || id.isBlank()) {
            return;
        }
        List<String> ids = GuiClient.CONFIG.blockEspIds;
        if (ids == null) {
            GuiClient.CONFIG.blockEspIds = ids = new ArrayList<String>();
        }
        String canonical = GuiScreen.canonicalBlockId(id);
        if (!(removed = ids.removeIf(v -> canonical.equals(GuiScreen.canonicalBlockId(v))))) {
            ids.add(canonical);
        }
        BlockEspRenderer.invalidate();
    }

    private List<BlockEntry> selectedBlockEntries() {
        ArrayList<BlockEntry> out = new ArrayList<BlockEntry>();
        List<String> ids = GuiClient.CONFIG.blockEspIds;
        if (ids == null || ids.isEmpty()) {
            return out;
        }
        this.allBlockEntries();
        HashSet<String> seen = new HashSet<String>();
        for (String id : ids) {
            BlockEntry entry;
            String canonical;
            if (id == null || id.isBlank() || !seen.add(canonical = GuiScreen.canonicalBlockId(id))) continue;
            BlockEntry blockEntry = entry = this.blockEntryById == null ? null : this.blockEntryById.get(canonical);
            if (entry == null && this.blockEntryById != null) {
                entry = this.blockEntryById.get(id);
            }
            if (entry == null) continue;
            out.add(entry);
        }
        out.sort(Comparator.comparing(a -> a.name, String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    private static String canonicalBlockId(String id) {
        String path;
        if (id == null) {
            return "";
        }
        int split = id.indexOf(58);
        String ns = split >= 0 ? id.substring(0, split + 1) : "";
        String canonicalPath = path = split >= 0 ? id.substring(split + 1) : id;
        if (path.startsWith("wall_")) {
            canonicalPath = path.substring("wall_".length());
        } else if (path.contains("_wall_")) {
            canonicalPath = path.replace("_wall_", "_");
        }
        return ns + canonicalPath;
    }

    private void toggleItemSelection(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        List<String> ids = GuiClient.CONFIG.itemEspIds;
        if (ids == null) {
            GuiClient.CONFIG.itemEspIds = ids = new ArrayList<String>();
        }
        if (ids.contains(id)) {
            ids.remove(id);
        } else {
            ids.add(id);
        }
    }

    private List<BlockEntry> selectedItemEntries() {
        ArrayList<BlockEntry> out = new ArrayList<BlockEntry>();
        List<String> ids = GuiClient.CONFIG.itemEspIds;
        if (ids == null || ids.isEmpty()) {
            return out;
        }
        this.allItemEntries();
        for (String id : ids) {
            BlockEntry entry;
            if (id == null || id.isBlank() || (entry = this.itemEntryById == null ? null : this.itemEntryById.get(id)) == null) continue;
            out.add(entry);
        }
        out.sort(Comparator.comparing(a -> a.name, String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    private boolean isXraySelected(String id) {
        List<String> ids = GuiClient.CONFIG.xrayIds;
        if (ids == null || id == null) {
            return false;
        }
        return ids.contains(id);
    }

    private void toggleXraySelection(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        List<String> ids = GuiClient.CONFIG.xrayIds;
        if (ids == null) {
            GuiClient.CONFIG.xrayIds = ids = new ArrayList<String>();
        }
        if (ids.contains(id)) {
            ids.removeIf(v -> v.equals(id));
        } else {
            ids.add(id);
        }
        XrayUtil.markDirty();
    }

    private List<BlockEntry> selectedXrayEntries() {
        ArrayList<BlockEntry> out = new ArrayList<BlockEntry>();
        List<String> ids = GuiClient.CONFIG.xrayIds;
        if (ids == null || ids.isEmpty()) {
            return out;
        }
        this.allBlockEntries();
        for (String id : ids) {
            BlockEntry entry;
            if (id == null || id.isBlank() || (entry = this.blockEntryById == null ? null : this.blockEntryById.get(id)) == null) continue;
            out.add(entry);
        }
        out.sort(Comparator.comparing(a -> a.name, String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    private BlockPickerLayout blockPickerLayout(boolean showPicker) {
        int listAreaH;
        int innerH;
        int innerW;
        int innerY;
        int innerX;
        int panelH;
        int panelW;
        int panelY;
        int panelX;
        int contentX = this.panelX + 96;
        int areaX = contentX + 12;
        int areaY = this.panelY + 12;
        int areaW = this.panelW - 96 - 24;
        int areaH = this.panelH - 24;
        if (showPicker) {
            int sw = this.width;
            int sh = this.height;
            int overlayW = Math.min(sw - 80, Math.max(360, Math.round((float)sw * 0.72f)));
            int overlayH = Math.min(sh - 80, Math.max(240, Math.round((float)sh * 0.62f)));
            panelX = (sw - overlayW) / 2;
            panelY = (sh - overlayH) / 2;
            panelW = overlayW;
            panelH = overlayH;
            innerX = panelX + 10;
            innerY = panelY + 10;
            innerW = panelW - 20;
            innerH = panelH - 20;
        } else {
            int px = areaX;
            int py = areaY + 44;
            int pw = areaW;
            int ph = areaH - 52;
            panelX = px;
            panelY = py;
            panelW = pw;
            panelH = ph;
            innerX = px + 12;
            innerY = py + 12;
            innerW = pw - 24;
            innerH = ph - 24;
        }
        if (innerH <= 0 || innerW <= 0) {
            return null;
        }
        int gap = 8;
        int headerH = 18;
        int headerY = innerY;
        int searchY = -1;
        int listTop = innerY + headerH + gap;
        if (showPicker) {
            searchY = listTop;
            listTop += 16 + gap;
        }
        if ((listAreaH = innerH - (listTop - innerY)) <= 0) {
            return null;
        }
        int listH = listAreaH;
        if (listH <= 0) {
            return null;
        }
        int selectedHeaderY = listTop + listH;
        int selectedHeaderH = 0;
        int selectedListY = selectedHeaderY;
        int selectedListH = 0;
        return new BlockPickerLayout(panelX, panelY, panelW, panelH, innerX, innerW, headerY, headerH, searchY, listTop, listH, selectedHeaderY, selectedHeaderH, selectedListY, selectedListH);
    }

    private DisplayNamesLayout displayNamesLayout() {
        int sw = this.width;
        int sh = this.height;
        int overlayW = Math.min(sw - 80, Math.max(420, Math.round((float)sw * 0.72f)));
        int overlayH = Math.min(sh - 80, Math.max(240, Math.round((float)sh * 0.62f)));
        int panelX = (sw - overlayW) / 2;
        int panelY = (sh - overlayH) / 2;
        int panelW = overlayW;
        int panelH = overlayH;
        int innerX = panelX + 10;
        int innerY = panelY + 10;
        int innerW = panelW - 20;
        int innerH = panelH - 20;
        if (innerW <= 0 || innerH <= 0) {
            return null;
        }
        int gap = 8;
        int headerH = 18;
        int headerY = innerY;
        int headerX = innerX;
        int headerW = innerW;
        int columnHeaderY = headerY + headerH + gap;
        int columnHeaderH = 24;
        int listTop = columnHeaderY + columnHeaderH + gap;
        int listH = innerY + innerH - listTop;
        if (listH <= 0) {
            return null;
        }
        int colGap = 10;
        int colW = (innerW - colGap) / 2;
        if (colW <= 0) {
            return null;
        }
        int leftX = innerX;
        int rightX = innerX + colW + colGap;
        return new DisplayNamesLayout(panelX, panelY, panelW, panelH, headerX, headerY, headerW, headerH, leftX, rightX, colW, colGap, columnHeaderY, columnHeaderH, listTop, listH);
    }

    private BlacklistLayout blacklistLayout() {
        int sw = this.width;
        int sh = this.height;
        int overlayW = Math.min(sw - 80, Math.max(360, Math.round((float)sw * 0.56f)));
        int overlayH = Math.min(sh - 80, Math.max(220, Math.round((float)sh * 0.55f)));
        int panelX = (sw - overlayW) / 2;
        int panelY = (sh - overlayH) / 2;
        int panelW = overlayW;
        int panelH = overlayH;
        int innerX = panelX + 10;
        int innerY = panelY + 10;
        int innerW = panelW - 20;
        int innerH = panelH - 20;
        if (innerW <= 0 || innerH <= 0) {
            return null;
        }
        int gap = 8;
        int headerH = 18;
        int headerY = innerY;
        int headerX = innerX;
        int headerW = innerW;
        int inputY = headerY + headerH + gap;
        int inputH = 16;
        int listY = inputY + inputH + gap;
        int listH = innerY + innerH - listY;
        if (listH <= 0) {
            return null;
        }
        int inputX = innerX;
        int inputW = innerW;
        int listX = innerX;
        int listW = innerW;
        return new BlacklistLayout(panelX, panelY, panelW, panelH, headerX, headerY, headerW, headerH, inputX, inputY, inputW, inputH, listX, listY, listW, listH);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.closing) {
            return true;
        }
        boolean overlayOpen = this.isOverlayOpen();
        int smx = overlayOpen ? (int)mouseX : this.scaledMouseX((int)mouseX);
        int smy = overlayOpen ? (int)mouseY : this.scaledMouseY((int)mouseY);
        float tabSlide = overlayOpen ? 0.0f : this.currentTabSlide(System.currentTimeMillis());
        int smxContent = (int)((float)smx - tabSlide);
        float viewSlide = overlayOpen ? 0.0f : this.currentModulesViewSlide();
        int smxView = (int)((float)smxContent - viewSlide);
        this.layoutFields(smxContent, smy);
        if (this.bindingTarget != null) {
            this.bindingTarget.setBindKey(BindUtil.toMouseBind(button));
            this.bindingTarget = null;
            this.saveActiveConfig();
            return true;
        }
        if (this.searchField.mouseClicked(smxContent, smy, button)) {
            return true;
        }
        if (this.configNameField.mouseClicked(smxContent, smy, button)) {
            return true;
        }
        if (this.friendNameField.mouseClicked(smxContent, smy, button)) {
            return true;
        }
        if (this.friendSearchField.mouseClicked(smxContent, smy, button)) {
            return true;
        }
        if (this.gearBlacklistField.mouseClicked(smxContent, smy, button)) {
            return true;
        }
        if (this.blockSearchField.mouseClicked(smxContent, smy, button)) {
            return true;
        }
        if (this.xraySearchField.mouseClicked(smxContent, smy, button)) {
            return true;
        }
        if (this.itemSearchField.mouseClicked(smxContent, smy, button)) {
            return true;
        }
        if (overlayOpen) {
            if (this.displayNamesOpen) {
                if (this.clickDisplayNamesOverlay(smx, smy, button)) {
                    return true;
                }
                return true;
            }
            if (this.gearBlacklistOpen) {
                if (this.clickGearBlacklistOverlay(smx, smy, button)) {
                    return true;
                }
                return true;
            }
            if (this.clickBlockPickerOverlay(smx, smy, button)) {
                return true;
            }
            return true;
        }
        if (this.clickLeftTabs(smx, smy, button)) {
            return true;
        }
        if (this.tab == Tab.MODULES && this.modulesView == ModulesView.SETTINGS && button == 0) {
            if (this.clickModuleSettingsBack(smxView, smy)) {
                return true;
            }
            if (this.clickModuleSettings(smxView, smy, button)) {
                return true;
            }
        }
        if (this.tab == Tab.MODULES && this.modulesView == ModulesView.LIST && (button == 0 || button == 1 || button == 2) && this.clickModulesList(smxView, smy, button)) {
            return true;
        }
        if (this.tab == Tab.CONFIGS && button == 0 && this.clickConfigs(smxContent, smy)) {
            return true;
        }
        if (this.tab == Tab.FRIENDS && button == 0 && this.clickFriends(smxContent, smy)) {
            return true;
        }
        if (this.tab == Tab.GEAR_RENDER && button == 0 && this.clickGearRender(smxContent, smy)) {
            return true;
        }
        if (this.tab == Tab.SETTINGS && button == 0) {
            int sx = this.panelX + 96 + 12;
            int sy = this.panelY + 12;
            int bw = 96;
            int bh = 18;
            int gapTabs = 8;
            if (GuiScreen.inside(smxContent, smy, sx, sy, bw, bh)) {
                this.settingsSub = SettingsSub.COLORS;
                return true;
            }
            if (GuiScreen.inside(smxContent, smy, sx + bw + gapTabs, sy, bw, bh)) {
                this.settingsSub = SettingsSub.GUI;
                return true;
            }
            if (GuiScreen.inside(smxContent, smy, sx + 2 * (bw + gapTabs), sy, bw, bh)) {
                this.settingsSub = SettingsSub.HUD;
                return true;
            }
            int contentX = this.panelX + 96;
            int x = contentX + 12;
            int y = this.panelY + 44 + 10;
            int w = this.panelW - 96 - 24;
            if (this.settingsSub == SettingsSub.COLORS) {
                GuiConfig cfg = GuiClient.CONFIG;
                int modeW = 110;
                int modeH = 18;
                int modeGap = 8;
                if (GuiScreen.inside(smxContent, smy, x, y, modeW, modeH)) {
                    cfg.colorMode = GuiConfig.ColorMode.SINGLE;
                    return true;
                }
                if (GuiScreen.inside(smxContent, smy, x, y + (modeH + modeGap), modeW, modeH)) {
                    cfg.colorMode = GuiConfig.ColorMode.GRADIENT;
                    return true;
                }
                if (GuiScreen.inside(smxContent, smy, x, y + 2 * (modeH + modeGap), modeW, modeH)) {
                    cfg.colorMode = GuiConfig.ColorMode.RAINBOW;
                    return true;
                }
                if (cfg.colorMode == GuiConfig.ColorMode.SINGLE) {
                    if (this.hueSingle.mouseClicked(smxContent, smy, button)) {
                        return true;
                    }
                } else if (cfg.colorMode == GuiConfig.ColorMode.GRADIENT) {
                    if (this.hueA.mouseClicked(smxContent, smy, button)) {
                        return true;
                    }
                    if (this.hueB.mouseClicked(smxContent, smy, button)) {
                        return true;
                    }
                    if (this.gradSpeed.mouseClicked(smxContent, smy, button)) {
                        return true;
                    }
                } else if (this.rainbowSpeed.mouseClicked(smxContent, smy, button)) {
                    return true;
                }
            } else if (this.settingsSub == SettingsSub.GUI) {
                if (this.transparency.mouseClicked(smxContent, smy, button)) {
                    return true;
                }
                if (this.uiVolume.mouseClicked(smxContent, smy, button)) {
                    return true;
                }
                if (this.guiScale.mouseClicked(smxContent, smy, button)) {
                    return true;
                }
            } else {
                int btnH = 18;
                if (GuiScreen.inside(smxContent, smy, x, y, w, btnH)) {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client != null) {
                        client.setScreen((Screen)new HudEditScreen());
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (this.closing) {
            return true;
        }
        if (this.displayNamesOpen) {
            return super.mouseDragged(mouseX, mouseY, button, dx, dy);
        }
        if (this.gearBlacklistOpen) {
            return true;
        }
        if (this.isBlockPickerOpen()) {
            return true;
        }
        int smx = this.scaledMouseX((int)mouseX);
        int smy = this.scaledMouseY((int)mouseY);
        float tabSlide = this.currentTabSlide(System.currentTimeMillis());
        int smxContent = (int)((float)smx - tabSlide);
        float viewSlide = this.currentModulesViewSlide();
        int smxView = (int)((float)smxContent - viewSlide);
        if (this.tab == Tab.SETTINGS && button == 0) {
            if (this.settingsSub == SettingsSub.COLORS) {
                if (this.hueSingle.mouseDragged(smxContent, smy, button, dx, dy)) {
                    return true;
                }
                if (this.hueA.mouseDragged(smxContent, smy, button, dx, dy)) {
                    return true;
                }
                if (this.hueB.mouseDragged(smxContent, smy, button, dx, dy)) {
                    return true;
                }
                if (this.gradSpeed.mouseDragged(smxContent, smy, button, dx, dy)) {
                    return true;
                }
                if (this.rainbowSpeed.mouseDragged(smxContent, smy, button, dx, dy)) {
                    return true;
                }
            } else if (this.settingsSub == SettingsSub.GUI) {
                if (this.transparency.mouseDragged(smxContent, smy, button, dx, dy)) {
                    return true;
                }
                if (this.uiVolume.mouseDragged(smxContent, smy, button, dx, dy)) {
                    return true;
                }
                if (this.guiScale.mouseDragged(smxContent, smy, button, dx, dy)) {
                    return true;
                }
            }
        }
        if (this.tab == Tab.MODULES && this.modulesView == ModulesView.SETTINGS && button == 0) {
            if (this.selectedModule != null && this.selectedModule.id == ModuleId.FULLBRIGHT && this.fullbrightSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                return true;
            }
            if (this.selectedModule != null && this.selectedModule.id == ModuleId.TPACCEPT_SOUNDS && this.tpaAcceptSoundVolumeSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                return true;
            }
            if (this.selectedModule != null && this.selectedModule.id == ModuleId.ARMOR_EQUIPPER && this.armorEquipperDelaySlider.mouseDragged(smxView, smy, button, dx, dy)) {
                return true;
            }
            if (this.selectedModule != null && this.selectedModule.id == ModuleId.ANTYKOSTKA && this.antyKostkaSpeedSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                return true;
            }
            if (this.selectedModule != null && this.selectedModule.id == ModuleId.ERROR_KILLER) {
                if (this.errorKillerDelaySlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
                if (this.errorKillerDelayObsidianSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
            }
            if (this.selectedModule != null && this.selectedModule.id == ModuleId.SPAMMER && this.spammerDelaySlider.mouseDragged(smxView, smy, button, dx, dy)) {
                return true;
            }
            if (this.selectedModule != null && this.selectedModule.id == ModuleId.FAST_LEVER && this.fastLeverSpeedSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                return true;
            }
            if (this.selectedModule != null && this.selectedModule.id == ModuleId.FAST_PLACE && this.fastPlaceDelaySlider.mouseDragged(smxView, smy, button, dx, dy)) {
                return true;
            }
            if (this.selectedModule != null && this.selectedModule.id == ModuleId.AIM_ASSIST && this.aimAssistRangeSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                return true;
            }
            if (this.selectedModule != null && this.selectedModule.id == ModuleId.BETTER_HITBOXES) {
                if (this.betterHitboxSizeXSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
                if (this.betterHitboxSizeYSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
            }
            if (this.selectedModule != null && this.selectedModule.id == ModuleId.ANTI_TRAP) {
                GuiConfig cfg = GuiClient.CONFIG;
                if (cfg.antiTrapAutoPlotek != null && cfg.antiTrapAutoPlotek.booleanValue() && this.autoPlotekCooldownSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
            }
            if (this.selectedModule != null && this.selectedModule.id == ModuleId.PEARL_TRAJECTORY && this.pearlLineWidthSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                return true;
            }
            if (this.selectedModule != null && this.selectedModule.id == ModuleId.TRACERS) {
                if (this.tracerPlayersColorSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
                if (this.tracerFriendsColorSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
                if (this.tracerMobsColorSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
                if (this.tracerDistanceSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
            }
            if (this.selectedModule != null && this.selectedModule.id == ModuleId.ESP) {
                if (this.espPlayerColorSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
                if (this.espFriendColorSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
                if (this.espLineWidthSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
            }
            if (this.selectedModule != null && this.selectedModule.id == ModuleId.FREECAM) {
                if (this.freecamSpeedXSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
                if (this.freecamSpeedYSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
            }
            if (this.selectedModule != null && this.selectedModule.id == ModuleId.SWORD_INFO) {
                if (this.swordInfoExtraColorSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
                if (this.swordInfoScaleSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
                if (this.swordInfoAllScaleSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
            }
            if (this.selectedModule != null && this.selectedModule.id == ModuleId.NAMETAGS) {
                if (this.nametagsTextColorSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
                if (this.nametagsBackgroundColorSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
                if (this.nametagsBorderColorSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
                if (this.nametagsTextAlphaSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
                if (this.nametagsBackgroundAlphaSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
                if (this.nametagsScaleSlider.mouseDragged(smxView, smy, button, dx, dy)) {
                    return true;
                }
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.closing) {
            return true;
        }
        if (this.displayNamesOpen) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        if (this.gearBlacklistOpen) {
            return true;
        }
        if (this.isBlockPickerOpen()) {
            return true;
        }
        int smx = this.scaledMouseX((int)mouseX);
        int smy = this.scaledMouseY((int)mouseY);
        float tabSlide = this.currentTabSlide(System.currentTimeMillis());
        int smxContent = (int)((float)smx - tabSlide);
        float viewSlide = this.currentModulesViewSlide();
        int smxView = (int)((float)smxContent - viewSlide);
        boolean colorDragging = this.hueSingle.dragging || this.hueA.dragging || this.hueB.dragging || this.gradSpeed.dragging || this.rainbowSpeed.dragging;
        this.hueSingle.mouseReleased(smxContent, smy, button);
        this.hueA.mouseReleased(smxContent, smy, button);
        this.hueB.mouseReleased(smxContent, smy, button);
        this.gradSpeed.mouseReleased(smxContent, smy, button);
        this.rainbowSpeed.mouseReleased(smxContent, smy, button);
        boolean settingsDragging = this.transparency.dragging || this.uiVolume.dragging || this.guiScale.dragging;
        this.transparency.mouseReleased(smxContent, smy, button);
        this.uiVolume.mouseReleased(smxContent, smy, button);
        this.guiScale.mouseReleased(smxContent, smy, button);
        boolean hudDragging = this.bindListScaleSlider.dragging || this.arrayListScaleSlider.dragging;
        this.bindListScaleSlider.mouseReleased(smxContent, smy, button);
        this.arrayListScaleSlider.mouseReleased(smxContent, smy, button);
        boolean fbDragging = this.fullbrightSlider.dragging;
        boolean tpaDragging = this.tpaAcceptSoundVolumeSlider.dragging;
        boolean aeDragging = this.armorEquipperDelaySlider.dragging;
        boolean aimAssistDragging = this.aimAssistRangeSlider.dragging;
        boolean hitboxDragging = this.betterHitboxSizeXSlider.dragging || this.betterHitboxSizeYSlider.dragging;
        this.fullbrightSlider.mouseReleased(smxView, smy, button);
        this.tpaAcceptSoundVolumeSlider.mouseReleased(smxView, smy, button);
        this.armorEquipperDelaySlider.mouseReleased(smxView, smy, button);
        this.antyKostkaSpeedSlider.mouseReleased(smxView, smy, button);
        this.errorKillerDelaySlider.mouseReleased(smxView, smy, button);
        this.errorKillerDelayObsidianSlider.mouseReleased(smxView, smy, button);
        this.spammerDelaySlider.mouseReleased(smxView, smy, button);
        this.fastLeverSpeedSlider.mouseReleased(smxView, smy, button);
        this.autoPlotekCooldownSlider.mouseReleased(smxView, smy, button);
        this.fastPlaceDelaySlider.mouseReleased(smxView, smy, button);
        this.aimAssistRangeSlider.mouseReleased(smxView, smy, button);
        this.betterHitboxSizeXSlider.mouseReleased(smxView, smy, button);
        this.betterHitboxSizeYSlider.mouseReleased(smxView, smy, button);
        this.pearlLineWidthSlider.mouseReleased(smxView, smy, button);
        this.tracerPlayersColorSlider.mouseReleased(smxView, smy, button);
        this.tracerFriendsColorSlider.mouseReleased(smxView, smy, button);
        this.tracerMobsColorSlider.mouseReleased(smxView, smy, button);
        this.tracerDistanceSlider.mouseReleased(smxView, smy, button);
        boolean espDragging = this.espPlayerColorSlider.dragging || this.espFriendColorSlider.dragging || this.espLineWidthSlider.dragging;
        this.espPlayerColorSlider.mouseReleased(smxView, smy, button);
        this.espFriendColorSlider.mouseReleased(smxView, smy, button);
        this.espLineWidthSlider.mouseReleased(smxView, smy, button);
        this.freecamSpeedXSlider.mouseReleased(smxView, smy, button);
        this.freecamSpeedYSlider.mouseReleased(smxView, smy, button);
        this.swordInfoExtraColorSlider.mouseReleased(smxView, smy, button);
        this.swordInfoScaleSlider.mouseReleased(smxView, smy, button);
        this.swordInfoAllScaleSlider.mouseReleased(smxView, smy, button);
        this.nametagsTextColorSlider.mouseReleased(smxView, smy, button);
        this.nametagsBackgroundColorSlider.mouseReleased(smxView, smy, button);
        this.nametagsBorderColorSlider.mouseReleased(smxView, smy, button);
        this.nametagsTextAlphaSlider.mouseReleased(smxView, smy, button);
        this.nametagsBackgroundAlphaSlider.mouseReleased(smxView, smy, button);
        this.nametagsScaleSlider.mouseReleased(smxView, smy, button);
        if (colorDragging || settingsDragging || hudDragging || fbDragging || tpaDragging || aeDragging || espDragging || aimAssistDragging || hitboxDragging) {
            this.saveActiveConfig();
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean charTyped(char chr, int modifiers) {
        if (this.closing) {
            return true;
        }
        if (this.gearBlacklistOpen) {
            if (this.gearBlacklistField.field.isVisible() && this.gearBlacklistField.field.charTyped(chr, modifiers)) {
                return true;
            }
            return true;
        }
        if (this.displayNamesOpen) {
            if (this.handleDisplayNameCharTyped(chr, modifiers)) {
                return true;
            }
            return true;
        }
        if (this.searchField.field.isVisible() && this.searchField.field.charTyped(chr, modifiers)) {
            return true;
        }
        if (this.configNameField.field.isVisible() && this.configNameField.field.charTyped(chr, modifiers)) {
            return true;
        }
        if (this.friendNameField.field.isVisible() && this.friendNameField.field.charTyped(chr, modifiers)) {
            return true;
        }
        if (this.friendSearchField.field.isVisible() && this.friendSearchField.field.charTyped(chr, modifiers)) {
            return true;
        }
        if (this.nameProtectField.field.isVisible() && this.nameProtectField.field.charTyped(chr, modifiers)) {
            return true;
        }
        if (this.nameProtectTimeField.field.isVisible() && this.nameProtectTimeField.field.charTyped(chr, modifiers)) {
            return true;
        }
        if (this.nameProtectMoneyField.field.isVisible() && this.nameProtectMoneyField.field.charTyped(chr, modifiers)) {
            return true;
        }
        if (this.nameProtectKillsField.field.isVisible() && this.nameProtectKillsField.field.charTyped(chr, modifiers)) {
            return true;
        }
        if (this.nameProtectDeathsField.field.isVisible() && this.nameProtectDeathsField.field.charTyped(chr, modifiers)) {
            return true;
        }
        if (this.spammerMessageField.field.isVisible() && this.spammerMessageField.field.charTyped(chr, modifiers)) {
            return true;
        }
        if (this.tab == Tab.FRIENDS && this.handleDisplayNameCharTyped(chr, modifiers)) {
            return true;
        }
        if (this.blockSearchField.field.isVisible() && this.blockSearchField.field.charTyped(chr, modifiers)) {
            return true;
        }
        if (this.xraySearchField.field.isVisible() && this.xraySearchField.field.charTyped(chr, modifiers)) {
            return true;
        }
        if (this.itemSearchField.field.isVisible() && this.itemSearchField.field.charTyped(chr, modifiers)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.closing) {
            return true;
        }
        if (this.gearBlacklistOpen) {
            if (keyCode == 256) {
                this.closeGearBlacklistOverlay();
                return true;
            }
            if (keyCode == 257 || keyCode == 335) {
                this.addGearBlacklistFromField();
                return true;
            }
            if (this.gearBlacklistField.field.isVisible() && this.gearBlacklistField.field.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            return true;
        }
        if (this.displayNamesOpen) {
            if (keyCode == 256) {
                this.closeDisplayNamesOverlay();
                return true;
            }
            if (this.handleDisplayNameKeyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            return true;
        }
        if (this.isBlockPickerOpen() && keyCode == 256) {
            this.closeBlockPicker();
            return true;
        }
        if (keyCode == 257 || keyCode == 335) {
            String name;
            if (this.tab == Tab.CONFIGS && this.configNameField.field.isVisible() && this.configNameField.field.isFocused() && !(name = this.configNameField.field.getText().trim()).isEmpty()) {
                GuiClient.CONFIGS.save(name);
                this.configNameField.field.setText("");
                this.refreshConfigs();
                return true;
            }
            if (this.tab == Tab.FRIENDS && this.friendNameField.field.isVisible() && this.friendNameField.field.isFocused() && !(name = this.friendNameField.field.getText().trim()).isEmpty()) {
                GuiClient.FRIENDS.add(name);
                this.friendNameField.field.setText("");
                this.friendNameField.field.setFocused(false);
                return true;
            }
        }
        if (this.bindingTarget != null) {
            if (keyCode == 256 || keyCode == 259) {
                this.bindingTarget.setBindKey(-1);
                this.bindingTarget = null;
                this.saveActiveConfig();
                return true;
            }
            this.bindingTarget.setBindKey(keyCode);
            this.bindingTarget = null;
            this.saveActiveConfig();
            return true;
        }
        if (this.searchField.field.isVisible() && this.searchField.field.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.configNameField.field.isVisible() && this.configNameField.field.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.friendNameField.field.isVisible() && this.friendNameField.field.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.friendSearchField.field.isVisible() && this.friendSearchField.field.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.nameProtectField.field.isVisible() && this.nameProtectField.field.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.nameProtectTimeField.field.isVisible() && this.nameProtectTimeField.field.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.nameProtectMoneyField.field.isVisible() && this.nameProtectMoneyField.field.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.nameProtectKillsField.field.isVisible() && this.nameProtectKillsField.field.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.nameProtectDeathsField.field.isVisible() && this.nameProtectDeathsField.field.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.spammerMessageField.field.isVisible() && this.spammerMessageField.field.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.tab == Tab.FRIENDS && this.handleDisplayNameKeyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.blockSearchField.field.isVisible() && this.blockSearchField.field.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.xraySearchField.field.isVisible() && this.xraySearchField.field.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.itemSearchField.field.isVisible() && this.itemSearchField.field.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double h, double v) {
        if (this.closing) {
            return true;
        }
        double dv = -v * 18.0;
        if (this.gearBlacklistOpen) {
            int smx = (int)mouseX;
            int smy = (int)mouseY;
            BlacklistLayout layout = this.blacklistLayout();
            if (layout != null && GuiScreen.inside(smx, smy, layout.listX, layout.listY, layout.listW, layout.listH)) {
                this.gearBlacklistScrollTarget = (float)((double)this.gearBlacklistScrollTarget + dv);
            }
            return true;
        }
        if (this.displayNamesOpen) {
            int listW;
            int smx = (int)mouseX;
            int smy = (int)mouseY;
            DisplayNamesLayout layout = this.displayNamesLayout();
            if (layout != null && GuiScreen.inside(smx, smy, layout.leftX, layout.listTop, listW = layout.colW * 2 + layout.colGap, layout.listH)) {
                this.displayNamesScrollTarget = (float)((double)this.displayNamesScrollTarget + dv);
            }
            return true;
        }
        if (this.isBlockPickerOpen()) {
            int smx = (int)mouseX;
            int smy = (int)mouseY;
            BlockPickerLayout layout = this.blockPickerLayout(true);
            if (layout != null) {
                boolean xray = this.xrayPickerOpen;
                boolean item = this.itemPickerOpen;
                if (GuiScreen.inside(smx, smy, layout.x, layout.listTop, layout.w, layout.listH)) {
                    if (this.pickerShowAdded) {
                        if (item) {
                            this.itemSelectedScrollTarget = (float)((double)this.itemSelectedScrollTarget + dv);
                        } else if (xray) {
                            this.xraySelectedScrollTarget = (float)((double)this.xraySelectedScrollTarget + dv);
                        } else {
                            this.blockSelectedScrollTarget = (float)((double)this.blockSelectedScrollTarget + dv);
                        }
                    } else if (item) {
                        this.itemListScrollTarget = (float)((double)this.itemListScrollTarget + dv);
                    } else if (xray) {
                        this.xrayListScrollTarget = (float)((double)this.xrayListScrollTarget + dv);
                    } else {
                        this.blockListScrollTarget = (float)((double)this.blockListScrollTarget + dv);
                    }
                    return true;
                }
                if (GuiScreen.inside(smx, smy, layout.x, layout.selectedListY, layout.w, layout.selectedListH)) {
                    if (item) {
                        this.itemSelectedScrollTarget = (float)((double)this.itemSelectedScrollTarget + dv);
                    } else if (xray) {
                        this.xraySelectedScrollTarget = (float)((double)this.xraySelectedScrollTarget + dv);
                    } else {
                        this.blockSelectedScrollTarget = (float)((double)this.blockSelectedScrollTarget + dv);
                    }
                    return true;
                }
            }
            return true;
        }
        if (this.tab == Tab.MODULES && this.modulesView == ModulesView.LIST) {
            this.modulesScrollTarget = (float)((double)this.modulesScrollTarget + dv);
        }
        if (this.tab == Tab.MODULES && this.modulesView == ModulesView.SETTINGS) {
            this.moduleSettingsScrollTarget = (float)((double)this.moduleSettingsScrollTarget + dv);
        }
        if (this.tab == Tab.CONFIGS) {
            this.configsScrollTarget = (float)((double)this.configsScrollTarget + dv);
        }
        if (this.tab == Tab.FRIENDS) {
            this.friendsScrollTarget = (float)((double)this.friendsScrollTarget + dv);
        }
        return true;
    }

    public void close() {
        if (!this.closing) {
            this.closing = true;
            this.closeStartMs = System.currentTimeMillis();
            return;
        }
        this.finishClose();
    }

    private boolean clickLeftTabs(int mx, int my, int button) {
        if (button != 0) {
            return false;
        }
        int tabY = this.panelY + 38;
        for (Tab tb : Tab.values()) {
            int x = this.panelX + 10;
            int y = tabY;
            int w = 76;
            int h = 22;
            if (GuiScreen.inside(mx, my, x, y, w, h)) {
                Tab prev = this.tab;
                boolean changed = tb != this.tab;
                this.tab = tb;
                if (changed) {
                    this.tabSwitchMs = System.currentTimeMillis();
                    this.tabSwitchDir = Integer.compare(this.tabIndex(this.tab), this.tabIndex(prev));
                    if (this.tabSwitchDir == 0) {
                        this.tabSwitchDir = 1;
                    }
                }
                this.bindingTarget = null;
                this.clearFieldFocus();
                this.blockPickerOpen = false;
                this.blockSearchField.field.setText("");
                this.blockListScroll = 0.0f;
                this.blockSelectedScroll = 0.0f;
                this.blockListScrollTarget = 0.0f;
                this.blockSelectedScrollTarget = 0.0f;
                this.xrayPickerOpen = false;
                this.xraySearchField.field.setText("");
                this.xrayListScroll = 0.0f;
                this.xraySelectedScroll = 0.0f;
                this.xrayListScrollTarget = 0.0f;
                this.xraySelectedScrollTarget = 0.0f;
                this.itemPickerOpen = false;
                this.itemSearchField.field.setText("");
                this.itemListScroll = 0.0f;
                this.itemSelectedScroll = 0.0f;
                this.itemListScrollTarget = 0.0f;
                this.itemSelectedScrollTarget = 0.0f;
                this.pickerShowAdded = false;
                this.closeDisplayNamesOverlay();
                this.closeGearBlacklistOverlay();
                if (changed) {
                    this.modulesView = ModulesView.LIST;
                    this.selectedModule = null;
                    this.moduleSettingsScroll = 0.0f;
                    this.moduleSettingsScrollTarget = 0.0f;
                }
                if (tb == Tab.CONFIGS) {
                    this.configNameField.requestFocus();
                } else if (tb == Tab.FRIENDS) {
                    this.friendNameField.field.setFocused(false);
                    this.friendSearchField.field.setFocused(false);
                }
                return true;
            }
            tabY += h + 8;
        }
        return false;
    }

    private boolean clickModuleSettingsBack(int mx, int my) {
        TextRenderer tr = GuiFonts.textRenderer();
        String backText = "\u2190 Back";
        int backW = tr.getWidth(backText);
        Objects.requireNonNull(tr);
        int backH = Math.max(18, 9 + 6);
        int areaX = this.panelX + 96 + 12;
        int areaY = this.panelY + 12;
        if (GuiScreen.inside(mx, my, areaX, areaY, backW + 16, backH)) {
            this.bindingTarget = null;
            if (this.blockPickerOpen && this.selectedModule != null && this.selectedModule.id == ModuleId.BLOCK_ESP) {
                this.blockPickerOpen = false;
                this.blockSearchField.field.setFocused(false);
                this.blockListScroll = 0.0f;
                this.blockSelectedScroll = 0.0f;
                this.blockListScrollTarget = 0.0f;
                this.blockSelectedScrollTarget = 0.0f;
                this.pickerShowAdded = false;
                return true;
            }
            if (this.xrayPickerOpen && this.selectedModule != null && this.selectedModule.id == ModuleId.XRAY) {
                this.xrayPickerOpen = false;
                this.xraySearchField.field.setFocused(false);
                this.xrayListScroll = 0.0f;
                this.xraySelectedScroll = 0.0f;
                this.xrayListScrollTarget = 0.0f;
                this.xraySelectedScrollTarget = 0.0f;
                this.pickerShowAdded = false;
                return true;
            }
            if (this.itemPickerOpen && this.selectedModule != null && this.selectedModule.id == ModuleId.ITEM_ESP) {
                this.itemPickerOpen = false;
                this.itemSearchField.field.setFocused(false);
                this.itemListScroll = 0.0f;
                this.itemSelectedScroll = 0.0f;
                this.itemListScrollTarget = 0.0f;
                this.itemSelectedScrollTarget = 0.0f;
                this.pickerShowAdded = false;
                return true;
            }
            this.modulesView = ModulesView.LIST;
            this.selectedModule = null;
            this.moduleSettingsScroll = 0.0f;
            this.moduleSettingsScrollTarget = 0.0f;
            return true;
        }
        return false;
    }

    private boolean clickBlockPickerOverlay(int mx, int my, int button) {
        List<BlockEntry> entries;
        int closeW;
        if (button != 0) {
            return false;
        }
        BlockPickerLayout layout = this.blockPickerLayout(true);
        if (layout == null) {
            return false;
        }
        boolean xray = this.xrayPickerOpen;
        boolean item = this.itemPickerOpen;
        TextRenderer tr = GuiFonts.textRenderer();
        int headerX = layout.panelX + 6;
        int headerW = layout.panelW - 12;
        int pad = (int)Math.ceil(10.0) + 2;
        int closeX = headerX + headerW - pad - (closeW = tr.getWidth("CLOSE"));
        if (GuiScreen.inside(mx, my, closeX - 4, layout.headerY, closeW + 8, layout.headerH)) {
            this.closeBlockPicker();
            return true;
        }
        String allLabel = "ALL";
        String addedLabel = "ADDED";
        int toggleH = Math.max(10, layout.headerH - 4);
        int toggleY = layout.headerY + (layout.headerH - toggleH) / 2;
        int allW = tr.getWidth(allLabel) + 10;
        int addedW = tr.getWidth(addedLabel) + 10;
        int addedX = closeX - 8 - addedW;
        int allX = addedX - 6 - allW;
        if (GuiScreen.inside(mx, my, allX, toggleY, allW, toggleH)) {
            this.pickerShowAdded = false;
            return true;
        }
        if (GuiScreen.inside(mx, my, addedX, toggleY, addedW, toggleH)) {
            this.pickerShowAdded = true;
            return true;
        }
        String query = item ? this.itemSearchField.field.getText().trim().toLowerCase() : (xray ? this.xraySearchField.field.getText().trim().toLowerCase() : this.blockSearchField.field.getText().trim().toLowerCase());
        boolean showingAdded = this.pickerShowAdded;
        if (showingAdded) {
            List<BlockEntry> selectedEntries = item ? this.selectedItemEntries() : (xray ? this.selectedXrayEntries() : this.selectedBlockEntries());
            entries = this.filterEntriesByQuery(selectedEntries, query);
        } else {
            List<BlockEntry> list = entries = item ? this.availableItemEntries(query) : this.availableBlockEntries(xray, query);
        }
        if (GuiScreen.inside(mx, my, layout.x, layout.listTop, layout.w, layout.listH)) {
            float listScroll = showingAdded ? (item ? this.itemSelectedScroll : (xray ? this.xraySelectedScroll : this.blockSelectedScroll)) : (item ? this.itemListScroll : (xray ? this.xrayListScroll : this.blockListScroll));
            int yy = layout.listTop - (int)listScroll;
            for (BlockEntry e : entries) {
                if (GuiScreen.inside(mx, my, layout.x, yy, layout.w, 24)) {
                    if (item) {
                        this.toggleItemSelection(e.id);
                    } else if (xray) {
                        this.toggleXraySelection(e.id);
                    } else {
                        this.toggleBlockSelection(e.id);
                    }
                    return true;
                }
                yy += 32;
            }
            return true;
        }
        return false;
    }

    private boolean clickDisplayNamesOverlay(int mx, int my, int button) {
        String close;
        int closeW;
        if (button != 0) {
            return false;
        }
        DisplayNamesLayout layout = this.displayNamesLayout();
        if (layout == null) {
            return false;
        }
        TextRenderer tr = GuiFonts.textRenderer();
        int pad = (int)Math.ceil(14.0) + 4;
        int closeX = layout.headerX + layout.headerW - pad - (closeW = tr.getWidth(close = "CLOSE"));
        if (GuiScreen.inside(mx, my, closeX - 4, layout.headerY, closeW + 8, layout.headerH)) {
            this.closeDisplayNamesOverlay();
            return true;
        }
        List<String> friends = GuiClient.FRIENDS.list();
        int rowH = 24;
        int rowGap = 8;
        float maxScroll = Math.max(0.0f, (float)(friends.size() * (rowH + rowGap) - layout.listH));
        this.displayNamesScrollTarget = GuiScreen.clamp(this.displayNamesScrollTarget, 0.0f, maxScroll);
        this.displayNamesScroll = GuiScreen.lerp(this.displayNamesScroll, this.displayNamesScrollTarget, 0.25f);
        if (Math.abs(this.displayNamesScroll - this.displayNamesScrollTarget) < 0.01f) {
            this.displayNamesScroll = this.displayNamesScrollTarget;
        }
        int yy = layout.listTop - (int)this.displayNamesScroll;
        boolean clickedField = false;
        for (String f : friends) {
            UiTextField field = this.displayNameField(f);
            field.setPos(layout.rightX, yy);
            field.setSize(layout.colW, rowH);
            field.field.setVisible(true);
            if (field.mouseClicked(mx, my, button)) {
                clickedField = true;
                break;
            }
            yy += rowH + rowGap;
        }
        if (!clickedField) {
            for (UiTextField field : this.displayNameFields.values()) {
                field.field.setFocused(false);
            }
        }
        return true;
    }

    private boolean clickGearBlacklistOverlay(int mx, int my, int button) {
        int addY;
        String close;
        int closeW;
        if (button != 0) {
            return false;
        }
        BlacklistLayout layout = this.blacklistLayout();
        if (layout == null) {
            return false;
        }
        TextRenderer tr = GuiFonts.textRenderer();
        int pad = (int)Math.ceil(10.0) + 2;
        int closeX = layout.headerX + layout.headerW - pad - (closeW = tr.getWidth(close = "CLOSE"));
        if (GuiScreen.inside(mx, my, closeX - 4, layout.headerY, closeW + 8, layout.headerH)) {
            this.closeGearBlacklistOverlay();
            return true;
        }
        int gap = 8;
        int addW = this.blacklistAddWidth();
        int fieldW = Math.max(80, layout.inputW - addW - gap);
        int addX = layout.inputX + fieldW + gap;
        if (GuiScreen.inside(mx, my, addX, addY = layout.inputY, addW, layout.inputH)) {
            this.addGearBlacklistFromField();
            return true;
        }
        List<String> entries = this.gearBlacklistEntries();
        int rowH = 24;
        int rowGap = 8;
        float maxScroll = Math.max(0.0f, (float)(entries.size() * (rowH + rowGap) - layout.listH));
        this.gearBlacklistScrollTarget = GuiScreen.clamp(this.gearBlacklistScrollTarget, 0.0f, maxScroll);
        this.gearBlacklistScroll = GuiScreen.lerp(this.gearBlacklistScroll, this.gearBlacklistScrollTarget, 0.25f);
        if (Math.abs(this.gearBlacklistScroll - this.gearBlacklistScrollTarget) < 0.01f) {
            this.gearBlacklistScroll = this.gearBlacklistScrollTarget;
        }
        int yy = layout.listY - (int)this.gearBlacklistScroll;
        for (String name : entries) {
            if (GuiScreen.inside(mx, my, layout.listX, yy, layout.listW, rowH)) {
                this.removeGearBlacklistEntry(name);
                return true;
            }
            yy += rowH + rowGap;
        }
        return true;
    }

    private boolean handleDisplayNameCharTyped(char chr, int modifiers) {
        for (UiTextField field : this.displayNameFields.values()) {
            if (!field.field.isVisible() || !field.field.charTyped(chr, modifiers)) continue;
            return true;
        }
        return false;
    }

    private boolean handleDisplayNameKeyPressed(int keyCode, int scanCode, int modifiers) {
        for (UiTextField field : this.displayNameFields.values()) {
            if (!field.field.isVisible() || !field.field.keyPressed(keyCode, scanCode, modifiers)) continue;
            return true;
        }
        return false;
    }

    private int blacklistAddWidth() {
        TextRenderer tr = GuiFonts.textRenderer();
        return Math.max(50, tr.getWidth("ADD") + 14);
    }

    private List<String> gearBlacklistEntries() {
        List<String> list = GuiClient.CONFIG.gearRenderBlacklist;
        if (list == null) {
            GuiClient.CONFIG.gearRenderBlacklist = list = new ArrayList<String>();
        }
        ArrayList<String> out = new ArrayList<String>(list);
        out.sort(String::compareToIgnoreCase);
        return out;
    }

    private void addGearBlacklistFromField() {
        if (!this.gearBlacklistField.field.isVisible()) {
            return;
        }
        String raw = this.gearBlacklistField.field.getText();
        if (raw == null) {
            return;
        }
        this.addGearBlacklistEntry(raw);
        this.gearBlacklistField.field.setText("");
    }

    private void addGearBlacklistEntry(String raw) {
        List<String> list;
        if (raw == null) {
            return;
        }
        String name = raw.trim();
        if (name.isEmpty()) {
            return;
        }
        if (name.length() > 32) {
            name = name.substring(0, 32);
        }
        if ((list = GuiClient.CONFIG.gearRenderBlacklist) == null) {
            GuiClient.CONFIG.gearRenderBlacklist = list = new ArrayList<String>();
        }
        String key = GuiScreen.normalizeName(name);
        for (String existing : list) {
            if (!GuiScreen.normalizeName(existing).equals(key)) continue;
            return;
        }
        list.add(name);
        this.saveActiveConfig();
    }

    private void removeGearBlacklistEntry(String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        List<String> list = GuiClient.CONFIG.gearRenderBlacklist;
        if (list == null || list.isEmpty()) {
            return;
        }
        String key = GuiScreen.normalizeName(name);
        list.removeIf(n -> GuiScreen.normalizeName(n).equals(key));
        this.saveActiveConfig();
    }

    private boolean clickModuleSettings(int mx, int my, int button) {
        if (button != 0) {
            return false;
        }
        if (this.selectedModule == null) {
            return false;
        }
        int contentX = this.panelX + 96;
        int areaX = contentX + 12;
        int areaY = this.panelY + 12;
        int areaW = this.panelW - 96 - 24;
        int areaH = this.panelH - 24;
        int px = areaX;
        int py = areaY + 44;
        int pw = areaW;
        int ph = areaH - 52;
        if (ph <= 0) {
            return false;
        }
        if (this.selectedModule.id == ModuleId.FULLBRIGHT) {
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int sh = 26;
            this.fullbrightSlider.setBounds(sx, sy, sw, sh);
            return this.fullbrightSlider.mouseClicked(mx, my, button);
        }
        if (this.selectedModule.id == ModuleId.TPACCEPT_SOUNDS) {
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int sh = 26;
            this.tpaAcceptSoundVolumeSlider.setBounds(sx, sy, sw, sh);
            return this.tpaAcceptSoundVolumeSlider.mouseClicked(mx, my, button);
        }
        if (this.selectedModule.id == ModuleId.ARMOR_EQUIPPER) {
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int rowH = 18;
            int gap = 8;
            if (GuiScreen.inside(mx, my, sx, sy, sw, rowH)) {
                this.bindingTarget = this.selectedModule;
                this.clearFieldFocus();
                return true;
            }
            this.armorEquipperDelaySlider.setBounds(sx, sy += rowH + gap, sw, 26);
            return this.armorEquipperDelaySlider.mouseClicked(mx, my, button);
        }
        if (this.selectedModule.id == ModuleId.ANTYKOSTKA) {
            int sx = px + 12;
            int sw = pw - 24;
            int rowH = 18;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int gap = 8;
            if (GuiScreen.inside(mx, my, sx, sy, sw, rowH)) {
                this.bindingTarget = this.selectedModule;
                this.clearFieldFocus();
                return true;
            }
            this.antyKostkaSpeedSlider.setBounds(sx, sy += rowH + gap, sw, 26);
            return this.antyKostkaSpeedSlider.mouseClicked(mx, my, button);
        }
        if (this.selectedModule.id == ModuleId.ERROR_KILLER) {
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int rowH = 18;
            int gap = 8;
            this.errorKillerDelaySlider.setBounds(sx, sy, sw, 26);
            if (this.errorKillerDelaySlider.mouseClicked(mx, my, button)) {
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += 26 + gap, sw, rowH)) {
                boolean show = GuiClient.CONFIG.errorKillerShowMissing != null && GuiClient.CONFIG.errorKillerShowMissing != false;
                GuiClient.CONFIG.errorKillerShowMissing = !show;
                return true;
            }
            this.errorKillerDelayObsidianSlider.setBounds(sx, sy += rowH + gap, sw, 26);
            if (this.errorKillerDelayObsidianSlider.mouseClicked(mx, my, button)) {
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += 26 + gap, sw, rowH)) {
                boolean show = GuiClient.CONFIG.errorKillerShowMissingObsidian != null && GuiClient.CONFIG.errorKillerShowMissingObsidian != false;
                GuiClient.CONFIG.errorKillerShowMissingObsidian = !show;
                return true;
            }
            return false;
        }
        if (this.selectedModule.id == ModuleId.SPAMMER) {
            GuiConfig cfg = GuiClient.CONFIG;
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int rowH = 18;
            int gap = 8;
            int labelW = GuiFonts.textRenderer().getWidth("Message:");
            int fieldX = sx + labelW + gap;
            int fieldW = Math.max(80, sw - labelW - gap);
            this.spammerMessageField.setPos(fieldX, sy);
            this.spammerMessageField.setSize(fieldW, rowH);
            this.spammerMessageField.field.setVisible(true);
            if (this.spammerMessageField.mouseClicked(mx, my, button)) {
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += rowH + gap, sw, rowH)) {
                boolean anti = cfg.spammerAntiSpam != null && cfg.spammerAntiSpam != false;
                cfg.spammerAntiSpam = !anti;
                return true;
            }
            this.spammerDelaySlider.setBounds(sx, sy += rowH + gap, sw, 26);
            return this.spammerDelaySlider.mouseClicked(mx, my, button);
        }
        if (this.selectedModule.id == ModuleId.FAST_LEVER) {
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int sh = 26;
            this.fastLeverSpeedSlider.setBounds(sx, sy, sw, sh);
            return this.fastLeverSpeedSlider.mouseClicked(mx, my, button);
        }
        if (this.selectedModule.id == ModuleId.FAST_PLACE) {
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int sh = 26;
            Objects.requireNonNull(GuiFonts.textRenderer());
            int sliderY = sy + 9 + 4;
            this.fastPlaceDelaySlider.setBounds(sx, sliderY, sw, sh);
            return this.fastPlaceDelaySlider.mouseClicked(mx, my, button);
        }
        if (this.selectedModule.id == ModuleId.XRAY) {
            int sx = px + 12;
            int sy = py + 12;
            int sw = pw - 24;
            int toggleH = 18;
            if (GuiScreen.inside(mx, my, sx, sy, sw, toggleH)) {
                boolean bl = this.xrayPickerOpen = !this.xrayPickerOpen;
                if (this.xrayPickerOpen) {
                    this.xrayListScroll = 0.0f;
                    this.xraySelectedScroll = 0.0f;
                    this.xrayListScrollTarget = 0.0f;
                    this.xraySelectedScrollTarget = 0.0f;
                    this.pickerShowAdded = false;
                    this.xraySearchField.requestFocus();
                } else {
                    this.xraySearchField.field.setFocused(false);
                }
                return true;
            }
            return false;
        }
        if (this.selectedModule.id == ModuleId.ITEM_ESP) {
            GuiConfig cfg = GuiClient.CONFIG;
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int toggleH = 18;
            int gap = 8;
            if (GuiScreen.inside(mx, my, sx, sy, sw, toggleH)) {
                boolean bl = this.itemPickerOpen = !this.itemPickerOpen;
                if (this.itemPickerOpen) {
                    this.itemListScroll = 0.0f;
                    this.itemSelectedScroll = 0.0f;
                    this.itemListScrollTarget = 0.0f;
                    this.itemSelectedScrollTarget = 0.0f;
                    this.pickerShowAdded = false;
                    this.itemSearchField.requestFocus();
                } else {
                    this.itemSearchField.field.setFocused(false);
                }
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean addAll = cfg.itemEspAddAll != null && cfg.itemEspAddAll != false;
                cfg.itemEspAddAll = !addAll;
                return true;
            }
            return false;
        }
        if (this.selectedModule.id == ModuleId.BLOCK_ESP) {
            int sx = px + 12;
            int sy = py + 12;
            int sw = pw - 24;
            int toggleH = 18;
            if (GuiScreen.inside(mx, my, sx, sy, sw, toggleH)) {
                boolean bl = this.blockPickerOpen = !this.blockPickerOpen;
                if (this.blockPickerOpen) {
                    this.blockListScroll = 0.0f;
                    this.blockSelectedScroll = 0.0f;
                    this.blockListScrollTarget = 0.0f;
                    this.blockSelectedScrollTarget = 0.0f;
                    this.pickerShowAdded = false;
                    this.blockSearchField.requestFocus();
                } else {
                    this.blockSearchField.field.setFocused(false);
                }
                return true;
            }
            return false;
        }
        if (this.selectedModule.id == ModuleId.PEARL_TRAJECTORY) {
            GuiConfig cfg = GuiClient.CONFIG;
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int toggleH = 18;
            int gap = 8;
            if (GuiScreen.inside(mx, my, sx, sy, sw, toggleH)) {
                boolean show = cfg.pearlShowTrajectory == null || cfg.pearlShowTrajectory != false;
                cfg.pearlShowTrajectory = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean show = cfg.pearlShowLanding == null || cfg.pearlShowLanding != false;
                cfg.pearlShowLanding = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean show = cfg.pearlShowNickname == null || cfg.pearlShowNickname != false;
                cfg.pearlShowNickname = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean show = cfg.pearlShowCountdown == null || cfg.pearlShowCountdown != false;
                cfg.pearlShowCountdown = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean show = cfg.pearlShowOwn == null || cfg.pearlShowOwn != false;
                cfg.pearlShowOwn = !show;
                return true;
            }
            this.pearlLineWidthSlider.setBounds(sx, sy += toggleH + gap, sw, 26);
            return this.pearlLineWidthSlider.mouseClicked(mx, my, button);
        }
        if (this.selectedModule.id == ModuleId.TRACERS) {
            GuiConfig cfg = GuiClient.CONFIG;
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int toggleH = 18;
            int gap = 8;
            if (GuiScreen.inside(mx, my, sx, sy, sw, toggleH)) {
                boolean show = cfg.tracerPlayers == null || cfg.tracerPlayers != false;
                cfg.tracerPlayers = !show;
                return true;
            }
            this.tracerPlayersColorSlider.setBounds(sx, sy += toggleH + gap, sw, 26);
            if (this.tracerPlayersColorSlider.mouseClicked(mx, my, button)) {
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += 26 + gap, sw, toggleH)) {
                boolean show = cfg.tracerFriends == null || cfg.tracerFriends != false;
                cfg.tracerFriends = !show;
                return true;
            }
            this.tracerFriendsColorSlider.setBounds(sx, sy += toggleH + gap, sw, 26);
            if (this.tracerFriendsColorSlider.mouseClicked(mx, my, button)) {
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += 26 + gap, sw, toggleH)) {
                boolean show = cfg.tracerMobs == null || cfg.tracerMobs != false;
                cfg.tracerMobs = !show;
                return true;
            }
            this.tracerMobsColorSlider.setBounds(sx, sy += toggleH + gap, sw, 26);
            if (this.tracerMobsColorSlider.mouseClicked(mx, my, button)) {
                return true;
            }
            this.tracerDistanceSlider.setBounds(sx, sy += 26 + gap, sw, 26);
            return this.tracerDistanceSlider.mouseClicked(mx, my, button);
        }
        if (this.selectedModule.id == ModuleId.ESP) {
            GuiConfig cfg = GuiClient.CONFIG;
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int toggleH = 18;
            int gap = 8;
            if (GuiScreen.inside(mx, my, sx, sy, sw, toggleH)) {
                boolean show = cfg.espBox == null || cfg.espBox != false;
                cfg.espBox = !show;
                return true;
            }
            this.espPlayerColorSlider.setBounds(sx, sy += toggleH + gap, sw, 26);
            if (this.espPlayerColorSlider.mouseClicked(mx, my, button)) {
                return true;
            }
            this.espFriendColorSlider.setBounds(sx, sy += 26 + gap, sw, 26);
            if (this.espFriendColorSlider.mouseClicked(mx, my, button)) {
                return true;
            }
            this.espLineWidthSlider.setBounds(sx, sy += 26 + gap, sw, 26);
            return this.espLineWidthSlider.mouseClicked(mx, my, button);
        }
        if (this.selectedModule.id == ModuleId.NAME_PROTECT) {
            boolean changeStats;
            GuiConfig cfg = GuiClient.CONFIG;
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int toggleH = 18;
            int gap = 8;
            int modeGap = 6;
            int modeW = (sw - modeGap) / 2;
            TextRenderer tr = GuiFonts.textRenderer();
            Objects.requireNonNull(tr);
            int labelH = 9 + 4;
            if (button == 0) {
                this.clearFieldFocus();
            }
            if (GuiScreen.inside(mx, my, sx, sy += labelH, modeW, toggleH)) {
                cfg.nameProtectMode = GuiConfig.NameProtectMode.SELF;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx + modeW + modeGap, sy, modeW, toggleH)) {
                cfg.nameProtectMode = GuiConfig.NameProtectMode.EVERYONE;
                return true;
            }
            int labelW = tr.getWidth("Display Name:");
            int fieldX = sx + labelW + gap;
            int fieldW = Math.max(80, sw - labelW - gap);
            this.nameProtectField.setPos(fieldX, sy += toggleH + gap);
            this.nameProtectField.setSize(fieldW, toggleH);
            this.nameProtectField.field.setVisible(true);
            if (this.nameProtectField.mouseClicked(mx, my, button)) {
                return true;
            }
            boolean bl = changeStats = cfg.nameProtectChangeStats != null && cfg.nameProtectChangeStats != false;
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                cfg.nameProtectChangeStats = !changeStats;
                return true;
            }
            sy += toggleH + gap;
            float statsT = this.nameProtectChangeStatsAnim;
            if (statsT > 0.01f) {
                int timeY = sy + this.revealOffset(statsT);
                if (changeStats) {
                    int timeLabelW = tr.getWidth("Czas");
                    int timeFieldX = sx + timeLabelW + gap;
                    int timeFieldW = Math.max(80, sw - timeLabelW - gap);
                    this.nameProtectTimeField.setPos(timeFieldX, timeY);
                    this.nameProtectTimeField.setSize(timeFieldW, toggleH);
                    this.nameProtectTimeField.field.setVisible(true);
                    if (this.nameProtectTimeField.mouseClicked(mx, my, button)) {
                        return true;
                    }
                }
                int moneyY = (sy += Math.round((float)(toggleH + gap) * statsT)) + this.revealOffset(statsT);
                if (changeStats) {
                    int moneyLabelW = tr.getWidth("Kasa");
                    int moneyFieldX = sx + moneyLabelW + gap;
                    int moneyFieldW = Math.max(80, sw - moneyLabelW - gap);
                    this.nameProtectMoneyField.setPos(moneyFieldX, moneyY);
                    this.nameProtectMoneyField.setSize(moneyFieldW, toggleH);
                    this.nameProtectMoneyField.field.setVisible(true);
                    if (this.nameProtectMoneyField.mouseClicked(mx, my, button)) {
                        return true;
                    }
                }
                int killsY = (sy += Math.round((float)(toggleH + gap) * statsT)) + this.revealOffset(statsT);
                if (changeStats) {
                    int killsLabelW = tr.getWidth("Kille");
                    int killsFieldX = sx + killsLabelW + gap;
                    int killsFieldW = Math.max(80, sw - killsLabelW - gap);
                    this.nameProtectKillsField.setPos(killsFieldX, killsY);
                    this.nameProtectKillsField.setSize(killsFieldW, toggleH);
                    this.nameProtectKillsField.field.setVisible(true);
                    if (this.nameProtectKillsField.mouseClicked(mx, my, button)) {
                        return true;
                    }
                }
                int deathsY = (sy += Math.round((float)(toggleH + gap) * statsT)) + this.revealOffset(statsT);
                if (changeStats) {
                    int deathsLabelW = tr.getWidth("\u015amierci");
                    int deathsFieldX = sx + deathsLabelW + gap;
                    int deathsFieldW = Math.max(80, sw - deathsLabelW - gap);
                    this.nameProtectDeathsField.setPos(deathsFieldX, deathsY);
                    this.nameProtectDeathsField.setSize(deathsFieldW, toggleH);
                    this.nameProtectDeathsField.field.setVisible(true);
                    if (this.nameProtectDeathsField.mouseClicked(mx, my, button)) {
                        return true;
                    }
                }
            }
            return false;
        }
        if (this.selectedModule.id == ModuleId.LOGOUT_SPOTS) {
            GuiConfig cfg = GuiClient.CONFIG;
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int toggleH = 18;
            int gap = 8;
            if (GuiScreen.inside(mx, my, sx, sy, sw, toggleH)) {
                boolean show = cfg.logoutShowNick == null || cfg.logoutShowNick != false;
                cfg.logoutShowNick = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean show = cfg.logoutShowTime == null || cfg.logoutShowTime != false;
                cfg.logoutShowTime = !show;
                return true;
            }
            return false;
        }
        if (this.selectedModule.id == ModuleId.SWORD_INFO) {
            GuiConfig cfg = GuiClient.CONFIG;
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int toggleH = 18;
            int gap = 8;
            if (GuiScreen.inside(mx, my, sx, sy, sw, toggleH)) {
                boolean show = cfg.swordInfoFrame == null || cfg.swordInfoFrame != false;
                cfg.swordInfoFrame = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean show = cfg.swordInfoShowAllPlayers != null && cfg.swordInfoShowAllPlayers != false;
                cfg.swordInfoShowAllPlayers = !show;
                return true;
            }
            this.swordInfoExtraColorSlider.setBounds(sx, sy += toggleH + gap, sw, 26);
            if (this.swordInfoExtraColorSlider.mouseClicked(mx, my, button)) {
                return true;
            }
            this.swordInfoScaleSlider.setBounds(sx, sy += 26 + gap, sw, 26);
            if (this.swordInfoScaleSlider.mouseClicked(mx, my, button)) {
                return true;
            }
            this.swordInfoAllScaleSlider.setBounds(sx, sy += 26 + gap, sw, 26);
            return this.swordInfoAllScaleSlider.mouseClicked(mx, my, button);
        }
        if (this.selectedModule.id == ModuleId.ANTI_TRAP) {
            boolean show;
            boolean painting;
            boolean itemFrame;
            GuiConfig cfg = GuiClient.CONFIG;
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int toggleH = 18;
            int gap = 8;
            int subIndent = 12;
            int subX = sx + subIndent;
            int subW = sw - subIndent;
            float frameT = this.antiTrapFrameAnim;
            float paintingT = this.antiTrapPaintingAnim;
            float autoPlotekT = this.antiTrapAutoPlotekAnim;
            boolean bl = itemFrame = cfg.antiTrapItemFrame == null || cfg.antiTrapItemFrame != false;
            if (GuiScreen.inside(mx, my, sx, sy, sw, toggleH)) {
                cfg.antiTrapItemFrame = !itemFrame;
                return true;
            }
            sy += toggleH + gap;
            if (frameT > 0.01f) {
                int autoBreakY = (sy += Math.round((float)(toggleH + gap) * frameT)) + this.revealOffset(frameT);
                if (itemFrame && GuiScreen.inside(mx, my, subX, autoBreakY, subW, toggleH)) {
                    boolean show2 = cfg.antiTrapItemFrameAutoBreak == null || cfg.antiTrapItemFrameAutoBreak != false;
                    cfg.antiTrapItemFrameAutoBreak = !show2;
                    return true;
                }
                int visualY = (sy += Math.round((float)(toggleH + gap) * frameT)) + this.revealOffset(frameT);
                if (itemFrame && GuiScreen.inside(mx, my, subX, visualY, subW, toggleH)) {
                    boolean show3 = cfg.antiTrapItemFrameVisual == null || cfg.antiTrapItemFrameVisual != false;
                    cfg.antiTrapItemFrameVisual = !show3;
                    return true;
                }
                sy += Math.round((float)(toggleH + gap) * frameT);
            }
            boolean bl2 = painting = cfg.antiTrapPainting == null || cfg.antiTrapPainting != false;
            if (GuiScreen.inside(mx, my, sx, sy, sw, toggleH)) {
                cfg.antiTrapPainting = !painting;
                return true;
            }
            sy += toggleH + gap;
            if (paintingT > 0.01f) {
                int autoBreakY = (sy += Math.round((float)(toggleH + gap) * paintingT)) + this.revealOffset(paintingT);
                if (painting && GuiScreen.inside(mx, my, subX, autoBreakY, subW, toggleH)) {
                    boolean show4 = cfg.antiTrapPaintingAutoBreak == null || cfg.antiTrapPaintingAutoBreak != false;
                    cfg.antiTrapPaintingAutoBreak = !show4;
                    return true;
                }
                int visualY = (sy += Math.round((float)(toggleH + gap) * paintingT)) + this.revealOffset(paintingT);
                if (painting && GuiScreen.inside(mx, my, subX, visualY, subW, toggleH)) {
                    boolean show5 = cfg.antiTrapPaintingVisual == null || cfg.antiTrapPaintingVisual != false;
                    cfg.antiTrapPaintingVisual = !show5;
                    return true;
                }
                sy += Math.round((float)(toggleH + gap) * paintingT);
            }
            if (GuiScreen.inside(mx, my, sx, sy, sw, toggleH)) {
                show = cfg.antiTrapMinecart == null || cfg.antiTrapMinecart != false;
                cfg.antiTrapMinecart = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                show = cfg.antiTrapArmorStand == null || cfg.antiTrapArmorStand != false;
                cfg.antiTrapArmorStand = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                show = cfg.antiTrapMobs != null && cfg.antiTrapMobs != false;
                cfg.antiTrapMobs = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                show = cfg.antiTrapAutoPlotek != null && cfg.antiTrapAutoPlotek != false;
                cfg.antiTrapAutoPlotek = !show;
                return true;
            }
            sy += toggleH + gap;
            if (autoPlotekT > 0.01f) {
                int sliderY = sy + this.revealOffset(autoPlotekT);
                if (cfg.antiTrapAutoPlotek != null && cfg.antiTrapAutoPlotek.booleanValue()) {
                    this.autoPlotekCooldownSlider.setBounds(sx, sliderY, sw, 26);
                    if (this.autoPlotekCooldownSlider.mouseClicked(mx, my, button)) {
                        return true;
                    }
                }
                sy += Math.round((float)(26 + gap) * autoPlotekT);
            }
            return false;
        }
        if (this.selectedModule.id == ModuleId.AUTO_PARAWAN) {
            GuiConfig cfg = GuiClient.CONFIG;
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int toggleH = 18;
            if (GuiScreen.inside(mx, my, sx, sy, sw, toggleH)) {
                boolean show = cfg.autoParawanDontAttackFriends == null || cfg.autoParawanDontAttackFriends != false;
                cfg.autoParawanDontAttackFriends = !show;
                return true;
            }
            return false;
        }
        if (this.selectedModule.id == ModuleId.AIM_ASSIST) {
            GuiConfig cfg = GuiClient.CONFIG;
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int toggleH = 18;
            int gap = 8;
            if (GuiScreen.inside(mx, my, sx, sy, sw, toggleH)) {
                boolean enabled = cfg.aimAssistTargetLock != null && cfg.aimAssistTargetLock != false;
                cfg.aimAssistTargetLock = !enabled;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean enabled = cfg.aimAssistMobs != null && cfg.aimAssistMobs != false;
                cfg.aimAssistMobs = !enabled;
                return true;
            }
            this.aimAssistRangeSlider.setBounds(sx, sy += toggleH + gap, sw, 26);
            return this.aimAssistRangeSlider.mouseClicked(mx, my, button);
        }
        if (this.selectedModule.id == ModuleId.BETTER_HITBOXES) {
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int gap = 8;
            this.betterHitboxSizeXSlider.setBounds(sx, sy, sw, 26);
            if (this.betterHitboxSizeXSlider.mouseClicked(mx, my, button)) {
                return true;
            }
            this.betterHitboxSizeYSlider.setBounds(sx, sy += 26 + gap, sw, 26);
            return this.betterHitboxSizeYSlider.mouseClicked(mx, my, button);
        }
        if (this.selectedModule.id == ModuleId.NO_PUSH) {
            GuiConfig cfg = GuiClient.CONFIG;
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int toggleH = 18;
            int gap = 8;
            if (GuiScreen.inside(mx, my, sx, sy, sw, toggleH)) {
                boolean show = cfg.noPushPlayers == null || cfg.noPushPlayers != false;
                cfg.noPushPlayers = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean show = cfg.noPushBlocks == null || cfg.noPushBlocks != false;
                cfg.noPushBlocks = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean show = cfg.noPushLiquids == null || cfg.noPushLiquids != false;
                cfg.noPushLiquids = !show;
                return true;
            }
            return false;
        }
        if (this.selectedModule.id == ModuleId.FREECAM) {
            GuiConfig cfg = GuiClient.CONFIG;
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int toggleH = 18;
            int gap = 8;
            if (GuiScreen.inside(mx, my, sx, sy, sw, toggleH)) {
                boolean show = cfg.freecamCancelMove == null || cfg.freecamCancelMove != false;
                cfg.freecamCancelMove = !show;
                return true;
            }
            this.freecamSpeedXSlider.setBounds(sx, sy += toggleH + gap, sw, 26);
            if (this.freecamSpeedXSlider.mouseClicked(mx, my, button)) {
                return true;
            }
            this.freecamSpeedYSlider.setBounds(sx, sy += 26 + gap, sw, 26);
            return this.freecamSpeedYSlider.mouseClicked(mx, my, button);
        }
        if (this.selectedModule.id == ModuleId.NAMETAGS) {
            GuiConfig cfg = GuiClient.CONFIG;
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int toggleH = 18;
            int gap = 8;
            if (GuiScreen.inside(mx, my, sx, sy, sw, toggleH)) {
                boolean show = cfg.nametagsShowDistance == null || cfg.nametagsShowDistance != false;
                cfg.nametagsShowDistance = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean show = cfg.nametagsShowName == null || cfg.nametagsShowName != false;
                cfg.nametagsShowName = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean show = cfg.nametagsShowHealth == null || cfg.nametagsShowHealth != false;
                cfg.nametagsShowHealth = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean show = cfg.nametagsShowEnchantments == null || cfg.nametagsShowEnchantments != false;
                cfg.nametagsShowEnchantments = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean show = cfg.nametagsShowSetBonus == null || cfg.nametagsShowSetBonus != false;
                cfg.nametagsShowSetBonus = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean show = cfg.nametagsShowEffects == null || cfg.nametagsShowEffects != false;
                cfg.nametagsShowEffects = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean show = cfg.nametagsShowDurability == null || cfg.nametagsShowDurability != false;
                cfg.nametagsShowDurability = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean show = cfg.nametagsShowArmorSet == null || cfg.nametagsShowArmorSet != false;
                cfg.nametagsShowArmorSet = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean show = cfg.nametagsShowHandItems == null || cfg.nametagsShowHandItems != false;
                cfg.nametagsShowHandItems = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean show = cfg.nametagsShowItemCount == null || cfg.nametagsShowItemCount != false;
                cfg.nametagsShowItemCount = !show;
                return true;
            }
            this.nametagsTextColorSlider.setBounds(sx, sy += toggleH + gap, sw, 26);
            if (this.nametagsTextColorSlider.mouseClicked(mx, my, button)) {
                return true;
            }
            this.nametagsBackgroundColorSlider.setBounds(sx, sy += 26 + gap, sw, 26);
            if (this.nametagsBackgroundColorSlider.mouseClicked(mx, my, button)) {
                return true;
            }
            this.nametagsBorderColorSlider.setBounds(sx, sy += 26 + gap, sw, 26);
            if (this.nametagsBorderColorSlider.mouseClicked(mx, my, button)) {
                return true;
            }
            this.nametagsTextAlphaSlider.setBounds(sx, sy += 26 + gap, sw, 26);
            if (this.nametagsTextAlphaSlider.mouseClicked(mx, my, button)) {
                return true;
            }
            this.nametagsBackgroundAlphaSlider.setBounds(sx, sy += 26 + gap, sw, 26);
            if (this.nametagsBackgroundAlphaSlider.mouseClicked(mx, my, button)) {
                return true;
            }
            this.nametagsScaleSlider.setBounds(sx, sy += 26 + gap, sw, 26);
            return this.nametagsScaleSlider.mouseClicked(mx, my, button);
        }
        if (this.selectedModule.id == ModuleId.REFILLER) {
            GuiConfig cfg = GuiClient.CONFIG;
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int toggleH = 18;
            int gap = 8;
            if (GuiScreen.inside(mx, my, sx, sy, sw, toggleH)) {
                boolean show = cfg.refillerObsidian == null || cfg.refillerObsidian != false;
                cfg.refillerObsidian = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean show = cfg.refillerCobwebs == null || cfg.refillerCobwebs != false;
                cfg.refillerCobwebs = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean show = cfg.refillerPearls == null || cfg.refillerPearls != false;
                cfg.refillerPearls = !show;
                return true;
            }
            return false;
        }
        if (this.selectedModule.id == ModuleId.PANIC_MODE) {
            GuiConfig cfg = GuiClient.CONFIG;
            int sx = px + 12;
            int sy = py + 12 - (int)this.moduleSettingsScroll;
            int sw = pw - 24;
            int toggleH = 18;
            int gap = 8;
            if (GuiScreen.inside(mx, my, sx, sy, sw, toggleH)) {
                boolean show = cfg.panicHideArrayList == null || cfg.panicHideArrayList != false;
                cfg.panicHideArrayList = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean show = cfg.panicHideNotifications == null || cfg.panicHideNotifications != false;
                cfg.panicHideNotifications = !show;
                return true;
            }
            if (GuiScreen.inside(mx, my, sx, sy += toggleH + gap, sw, toggleH)) {
                boolean show = cfg.panicHideKeybinds == null || cfg.panicHideKeybinds != false;
                cfg.panicHideKeybinds = !show;
                return true;
            }
            return false;
        }
        return false;
    }

    private boolean clickModulesList(int mx, int my, int button) {
        int contentX = this.panelX + 96;
        int areaX = contentX + 12;
        int areaY = this.panelY + 44 + 10;
        int areaW = this.panelW - 96 - 24;
        int areaH = this.panelH - 44 - 16;
        if (!GuiScreen.inside(mx, my, areaX, areaY, areaW, areaH)) {
            return false;
        }
        String q = this.searchField.field.isVisible() ? this.searchField.field.getText().trim().toLowerCase() : "";
        List<HackModule> filtered = this.filteredModulesForTab(q);
        int gap = 8;
        int tileW = (areaW - gap) / 2;
        int tileH = 30;
        int startY = areaY - (int)this.modulesScroll;
        for (int i = 0; i < filtered.size(); ++i) {
            HackModule m = filtered.get(i);
            int col = i % 2;
            int x = areaX + col * (tileW + gap);
            int row = i / 2;
            int y = startY + row * (tileH + gap);
            if (!GuiScreen.inside(mx, my, x, y, tileW, tileH)) continue;
            if (button == 0) {
                if (m.id == ModuleId.ARMOR_EQUIPPER || m.id == ModuleId.ANTYKOSTKA) {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player != null) {
                        client.player.sendMessage((Text)Text.literal((String)"You cant turn that off"), true);
                        client.player.playSound(SoundEvents.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
                    }
                } else {
                    GuiClient.MODULES.toggleWithSound(m);
                }
                return true;
            }
            if (button == 2) {
                if (m.id == ModuleId.ERROR_KILLER) {
                    return true;
                }
                this.bindingTarget = m;
                this.clearFieldFocus();
                return true;
            }
            if (button != 1) continue;
            this.blockPickerOpen = false;
            this.blockSearchField.field.setText("");
            this.blockListScroll = 0.0f;
            this.blockSelectedScroll = 0.0f;
            this.blockListScrollTarget = 0.0f;
            this.blockSelectedScrollTarget = 0.0f;
            this.xrayPickerOpen = false;
            this.xraySearchField.field.setText("");
            this.xrayListScroll = 0.0f;
            this.xraySelectedScroll = 0.0f;
            this.xrayListScrollTarget = 0.0f;
            this.xraySelectedScrollTarget = 0.0f;
            this.itemPickerOpen = false;
            this.itemSearchField.field.setText("");
            this.itemListScroll = 0.0f;
            this.itemSelectedScroll = 0.0f;
            this.itemListScrollTarget = 0.0f;
            this.itemSelectedScrollTarget = 0.0f;
            this.selectedModule = m;
            this.modulesView = ModulesView.SETTINGS;
            this.moduleSettingsScroll = 0.0f;
            this.moduleSettingsScrollTarget = 0.0f;
            return true;
        }
        return false;
    }

    private boolean clickConfigs(int mx, int my) {
        int btnY;
        int listX = this.panelX + 96 + 12;
        int listY = this.panelY + 44 + 10;
        int listW = this.panelW - 96 - 24;
        int listH = this.panelH - 44 - 16;
        if (!GuiScreen.inside(mx, my, listX, listY, listW, listH)) {
            return false;
        }
        TextRenderer tr = GuiFonts.textRenderer();
        String openText = "Open folder";
        int pad = (int)Math.ceil(10.0) + 2;
        int btnH = 18;
        int btnW = Math.max(80, tr.getWidth(openText) + pad * 2);
        int btnX = listX + listW - btnW;
        if (GuiScreen.inside(mx, my, btnX, btnY = listY + listH - btnH, btnW, btnH)) {
            this.openConfigsFolder();
            return true;
        }
        int rowH = 24;
        int gap = 8;
        int rowPad = (int)Math.ceil(14.0) + 4;
        int yy = listY - (int)this.configsScroll;
        for (String name : this.configNames) {
            if (GuiScreen.inside(mx, my, listX, yy, listW, rowH)) {
                int loadW;
                String load = "Load";
                String del = "Del";
                int delW = tr.getWidth(del);
                int delX = listX + listW - rowPad - delW;
                int loadX = delX - 16 - (loadW = tr.getWidth(load));
                if (mx >= loadX && mx < loadX + loadW) {
                    GuiClient.CONFIGS.load(name);
                    this.refreshConfigs();
                    return true;
                }
                if (mx >= delX && mx < delX + delW) {
                    GuiClient.CONFIGS.delete(name);
                    this.refreshConfigs();
                    return true;
                }
            }
            yy += rowH + gap;
        }
        return false;
    }

    private boolean clickFriends(int mx, int my) {
        int toggleY;
        int toggleX;
        int listX = this.panelX + 96 + 12;
        int listY = this.panelY + 44 + 10;
        int listW = this.panelW - 96 - 24;
        int listH = this.panelH - 44 - 16;
        if (listH < 0) {
            listH = 0;
        }
        if (!this.friendAddExpanded && this.friendAddW > 0 && GuiScreen.inside(mx, my, this.friendAddX, this.friendAddY, this.friendAddW, this.friendAddH)) {
            this.friendNameField.field.setVisible(true);
            this.friendNameField.requestFocus();
            return true;
        }
        if (!GuiScreen.inside(mx, my, listX, listY, listW, listH)) {
            this.friendNameField.field.setFocused(false);
            this.friendSearchField.field.setFocused(false);
            return false;
        }
        String query = this.friendSearchField.field.isVisible() ? this.friendSearchField.field.getText().trim().toLowerCase() : "";
        List<String> friends = this.filteredFriends(query);
        GuiConfig cfg = GuiClient.CONFIG;
        TextRenderer tr = GuiFonts.textRenderer();
        int headerH = 24;
        int headerGap = 8;
        int colGap = 10;
        int colW = (listW - colGap) / 2;
        if (colW < 0) {
            colW = 0;
        }
        int rowPad = (int)Math.ceil(14.0) + 4;
        int toggleH = 18;
        int togglePad = 6;
        String toggleLabel = "Change Normal Nametag";
        int toggleTextW = tr.getWidth(toggleLabel);
        int toggleMinW = toggleTextW + (int)Math.ceil(10.0) * 2 + 26 + 8;
        int toggleW = Math.min(listW - togglePad * 2, Math.max(90, toggleMinW));
        if (toggleW < 0) {
            toggleW = listW;
        }
        if (GuiScreen.inside(mx, my, toggleX = listX + listW - toggleW - togglePad, toggleY = listY + listH - toggleH - togglePad, toggleW, toggleH)) {
            boolean show = cfg.friendsChangeNormalNametag == null || cfg.friendsChangeNormalNametag != false;
            cfg.friendsChangeNormalNametag = !show;
            this.saveActiveConfig();
            return true;
        }
        int listTop = listY + headerH + headerGap;
        int reservedBottom = toggleH + togglePad;
        int listViewH = listH - (headerH + headerGap + reservedBottom);
        if (listViewH < 0) {
            listViewH = 0;
        }
        int rowH = 24;
        int gap = 8;
        int totalH = friends.size() * (rowH + gap);
        float maxScroll = Math.max(0.0f, (float)(totalH - listViewH));
        this.friendsScrollTarget = GuiScreen.clamp(this.friendsScrollTarget, 0.0f, maxScroll);
        this.friendsScroll = GuiScreen.lerp(this.friendsScroll, this.friendsScrollTarget, 0.25f);
        if (Math.abs(this.friendsScroll - this.friendsScrollTarget) < 0.01f) {
            this.friendsScroll = this.friendsScrollTarget;
        }
        int yy = listTop - (int)this.friendsScroll;
        int rightX = listX + colW + colGap;
        boolean clickedField = false;
        for (String f : friends) {
            String del;
            int delW;
            int delX;
            if (GuiScreen.inside(mx, my, listX, yy, colW, rowH) && mx >= (delX = listX + colW - rowPad - (delW = tr.getWidth(del = "Remove"))) && mx < delX + delW) {
                GuiClient.FRIENDS.remove(f);
                this.displayNameFields.remove(GuiScreen.normalizeName(f));
                return true;
            }
            UiTextField field = this.displayNameField(f);
            field.setPos(rightX, yy);
            field.setSize(colW, rowH);
            field.field.setVisible(true);
            if (field.mouseClicked(mx, my, 0)) {
                clickedField = true;
                break;
            }
            yy += rowH + gap;
        }
        if (!clickedField) {
            for (UiTextField field : this.displayNameFields.values()) {
                field.field.setFocused(false);
            }
            this.friendNameField.field.setFocused(false);
            this.friendSearchField.field.setFocused(false);
        }
        return true;
    }

    private boolean clickGearRender(int mx, int my) {
        if (this.gearRenderButtons.isEmpty()) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        for (GearRenderButton btn : this.gearRenderButtons) {
            if (!GuiScreen.inside(mx, my, btn.x, btn.y, btn.w, btn.h)) continue;
            if (btn.type == GearRenderButtonType.TPA) {
                GearRenderUtil.requestTpa(client, btn.name);
            } else if (btn.type == GearRenderButtonType.BLOCK) {
                this.addGearBlacklistEntry(btn.name);
            } else if (btn.type == GearRenderButtonType.STATS) {
                GearRenderUtil.requestStats(client, btn.name);
            } else if (btn.type == GearRenderButtonType.AUTO_TP) {
                boolean enabled = GuiClient.CONFIG.gearRenderAutoTpa != null && GuiClient.CONFIG.gearRenderAutoTpa != false;
                GuiClient.CONFIG.gearRenderAutoTpa = !enabled;
                this.saveActiveConfig();
            } else if (btn.type == GearRenderButtonType.BLACKLIST) {
                this.openGearBlacklistOverlay();
            } else if (btn.type == GearRenderButtonType.CHANGE_CH) {
                GearRenderUtil.requestChangeCh(client);
            } else {
                GearRenderUtil.clearMissing();
            }
            return true;
        }
        return false;
    }

    private static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }

    private static int clamp(int v, int a, int b) {
        return Math.max(a, Math.min(b, v));
    }

    private static float clamp(float v, float a, float b) {
        return Math.max(a, Math.min(b, v));
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static int lerpColor(int a, int b, float t) {
        int aA = a >>> 24 & 0xFF;
        int aR = a >>> 16 & 0xFF;
        int aG = a >>> 8 & 0xFF;
        int aB = a & 0xFF;
        int bA = b >>> 24 & 0xFF;
        int bR = b >>> 16 & 0xFF;
        int bG = b >>> 8 & 0xFF;
        int bB = b & 0xFF;
        int oA = Math.round(GuiScreen.lerp(aA, bA, t));
        int oR = Math.round(GuiScreen.lerp(aR, bR, t));
        int oG = Math.round(GuiScreen.lerp(aG, bG, t));
        int oB = Math.round(GuiScreen.lerp(aB, bB, t));
        return oA << 24 | oR << 16 | oG << 8 | oB;
    }

    private static float easeOutCubic(float t) {
        return 1.0f - (float)Math.pow(1.0f - t, 3.0);
    }

    private void renderPanelGradient(DrawContext ctx, int x, int y, int w, int h, float radius, int accent, float openT) {
        if (w <= 0 || h <= 0) {
            return;
        }
        int baseCol = Theme.withAlpha(accent, (int)(26.0f * openT));
        Rounded.rect(ctx, x, y, w, h, radius, baseCol);
    }

    private void renderTabUnderline(DrawContext ctx, int x, int y, int w, int h, int accent, float openT) {
    }

    private int tabIndex(Tab tb) {
        int i = 0;
        for (Tab t : Tab.values()) {
            if (t == tb) {
                return i;
            }
            ++i;
        }
        return 0;
    }

    private float currentTabSlide(long now) {
        if (this.tabSwitchMs <= 0L) {
            return 0.0f;
        }
        float t = (float)(now - this.tabSwitchMs) / 260.0f;
        t = GuiScreen.clamp(t, 0.0f, 1.0f);
        float eased = GuiScreen.easeOutCubic(t);
        int dir = this.tabSwitchDir == 0 ? 1 : this.tabSwitchDir;
        return (1.0f - eased) * 18.0f * (float)dir;
    }

    private float currentTabFade(long now) {
        if (this.tabSwitchMs <= 0L) {
            return 1.0f;
        }
        float t = (float)(now - this.tabSwitchMs) / 260.0f;
        t = GuiScreen.clamp(t, 0.0f, 1.0f);
        return GuiScreen.easeOutCubic(t);
    }

    private float animateMap(Map<ModuleId, Float> map, ModuleId id, float target, float speed) {
        float cur = map.getOrDefault((Object)id, Float.valueOf(target)).floatValue();
        if (Math.abs((cur = GuiScreen.lerp(cur, target, speed)) - target) < 0.001f) {
            cur = target;
        }
        map.put(id, Float.valueOf(cur));
        return cur;
    }

    private float updateModulesViewAnim() {
        float target = this.modulesView == ModulesView.SETTINGS ? 1.0f : 0.0f;
        this.modulesViewAnim = GuiScreen.lerp(this.modulesViewAnim, target, 0.18f);
        if (Math.abs(this.modulesViewAnim - target) < 0.001f) {
            this.modulesViewAnim = target;
        }
        return this.modulesViewAnim;
    }

    private float updateSearchExpandAnim(boolean expanded) {
        float target = expanded ? 1.0f : 0.0f;
        this.searchExpandAnim = GuiScreen.lerp(this.searchExpandAnim, target, 0.25f);
        if (Math.abs(this.searchExpandAnim - target) < 0.001f) {
            this.searchExpandAnim = target;
        }
        return this.searchExpandAnim;
    }

    private float updateFriendAddExpandAnim(boolean expanded) {
        float target = expanded ? 1.0f : 0.0f;
        this.friendAddExpandAnim = GuiScreen.lerp(this.friendAddExpandAnim, target, 0.25f);
        if (Math.abs(this.friendAddExpandAnim - target) < 0.001f) {
            this.friendAddExpandAnim = target;
        }
        return this.friendAddExpandAnim;
    }

    private float currentModulesViewSlide() {
        if (this.tab != Tab.MODULES) {
            return 0.0f;
        }
        if (this.modulesView == ModulesView.LIST) {
            return -22.0f * this.modulesViewAnim;
        }
        return 22.0f * (1.0f - this.modulesViewAnim);
    }

    private void finishClose() {
        this.saveGuiState();
        this.saveActiveConfig();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.currentScreen == this) {
            client.setScreen(null);
        }
    }

    private void saveGuiState() {
        savedTab = this.tab;
        savedSettingsSub = this.settingsSub;
        savedModulesView = this.modulesView;
        savedSelectedModuleId = this.selectedModule != null ? this.selectedModule.id : null;
        savedModulesScroll = this.modulesScrollTarget;
        savedModuleSettingsScroll = this.moduleSettingsScrollTarget;
    }

    private void restoreGuiState() {
        this.tab = savedTab != null ? savedTab : Tab.MODULES;
        this.settingsSub = savedSettingsSub != null ? savedSettingsSub : SettingsSub.COLORS;
        this.modulesView = savedModulesView != null ? savedModulesView : ModulesView.LIST;
        this.modulesScroll = savedModulesScroll;
        this.modulesScrollTarget = savedModulesScroll;
        this.moduleSettingsScroll = savedModuleSettingsScroll;
        this.moduleSettingsScrollTarget = savedModuleSettingsScroll;
        if (this.modulesView == ModulesView.SETTINGS && savedSelectedModuleId != null) {
            this.selectedModule = GuiClient.MODULES.byId(savedSelectedModuleId);
            if (this.selectedModule == null) {
                this.modulesView = ModulesView.LIST;
            }
        } else {
            this.selectedModule = null;
        }
    }

    public static void openGearRender() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        savedTab = Tab.GEAR_RENDER;
        savedModulesView = ModulesView.LIST;
        savedSelectedModuleId = null;
        savedModulesScroll = 0.0f;
        savedModuleSettingsScroll = 0.0f;
        client.setScreen((Screen)new GuiScreen());
    }

    public static void openGearRenderViaBind() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        savedTab = Tab.GEAR_RENDER;
        savedModulesView = ModulesView.LIST;
        savedSelectedModuleId = null;
        savedModulesScroll = 0.0f;
        savedModuleSettingsScroll = 0.0f;
        GuiClient.openGui();
    }

    public static boolean isGearRenderOpen(Screen screen) {
        if (!(screen instanceof GuiScreen)) {
            return false;
        }
        GuiScreen gs = (GuiScreen)screen;
        return gs.tab == Tab.GEAR_RENDER;
    }

    private void saveActiveConfig() {
        String active = GuiClient.CONFIG.activeConfig;
        if (active == null || active.isBlank()) {
            active = "default";
        }
        GuiClient.CONFIGS.save(active);
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
            return n.toUpperCase();
        }
        return "KEY_" + key;
    }

    private static String formatKeyShort(int key) {
        if (key <= 0) {
            return "";
        }
        if (BindUtil.isMouseBind(key)) {
            int btn = BindUtil.mouseButton(key);
            return "M" + (btn + 1);
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
                return "SP";
            }
            case 340: {
                return "LS";
            }
            case 344: {
                return "RS";
            }
            case 341: {
                return "LC";
            }
            case 345: {
                return "RC";
            }
            case 342: {
                return "LA";
            }
            case 346: {
                return "RA";
            }
            case 256: {
                return "ESC";
            }
            case 257: {
                return "EN";
            }
            case 259: {
                return "BK";
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
            String up = n.toUpperCase();
            return up.length() > 3 ? up.substring(0, 3) : up;
        }
        return "K" + key;
    }

    private static enum Tab {
        MODULES("Modules"),
        CONFIGS("Configs"),
        FRIENDS("Friends"),
        SETTINGS("Settings"),
        GEAR_RENDER("Gear Render");

        public final String label;

        private Tab(String l) {
            this.label = l;
        }
    }

    private static enum SettingsSub {
        COLORS("Colors"),
        GUI("Gui Settings"),
        HUD("Hud");

        public final String label;

        private SettingsSub(String l) {
            this.label = l;
        }
    }

    private static enum ModulesView {
        LIST,
        SETTINGS;

    }

    private static enum OverlayType {
        NONE,
        BLOCK_PICKER,
        DISPLAY_NAMES,
        GEAR_BLACKLIST;

    }

    private static final class BlockPickerLayout {
        final int panelX;
        final int panelY;
        final int panelW;
        final int panelH;
        final int x;
        final int w;
        final int headerY;
        final int headerH;
        final int searchY;
        final int listTop;
        final int listH;
        final int selectedHeaderY;
        final int selectedHeaderH;
        final int selectedListY;
        final int selectedListH;

        BlockPickerLayout(int panelX, int panelY, int panelW, int panelH, int x, int w, int headerY, int headerH, int searchY, int listTop, int listH, int selectedHeaderY, int selectedHeaderH, int selectedListY, int selectedListH) {
            this.panelX = panelX;
            this.panelY = panelY;
            this.panelW = panelW;
            this.panelH = panelH;
            this.x = x;
            this.w = w;
            this.headerY = headerY;
            this.headerH = headerH;
            this.searchY = searchY;
            this.listTop = listTop;
            this.listH = listH;
            this.selectedHeaderY = selectedHeaderY;
            this.selectedHeaderH = selectedHeaderH;
            this.selectedListY = selectedListY;
            this.selectedListH = selectedListH;
        }
    }

    private static final class BlacklistLayout {
        final int panelX;
        final int panelY;
        final int panelW;
        final int panelH;
        final int headerX;
        final int headerY;
        final int headerW;
        final int headerH;
        final int inputX;
        final int inputY;
        final int inputW;
        final int inputH;
        final int listX;
        final int listY;
        final int listW;
        final int listH;

        BlacklistLayout(int panelX, int panelY, int panelW, int panelH, int headerX, int headerY, int headerW, int headerH, int inputX, int inputY, int inputW, int inputH, int listX, int listY, int listW, int listH) {
            this.panelX = panelX;
            this.panelY = panelY;
            this.panelW = panelW;
            this.panelH = panelH;
            this.headerX = headerX;
            this.headerY = headerY;
            this.headerW = headerW;
            this.headerH = headerH;
            this.inputX = inputX;
            this.inputY = inputY;
            this.inputW = inputW;
            this.inputH = inputH;
            this.listX = listX;
            this.listY = listY;
            this.listW = listW;
            this.listH = listH;
        }
    }

    private static final class BlockEntry {
        final String id;
        final String name;
        final String nameLower;
        final ItemStack stack;

        BlockEntry(String id, String name, ItemStack stack) {
            this.id = id;
            this.name = name;
            this.nameLower = name == null ? "" : name.toLowerCase();
            this.stack = stack == null ? ItemStack.EMPTY : stack;
        }
    }

    private static final class DisplayNamesLayout {
        final int panelX;
        final int panelY;
        final int panelW;
        final int panelH;
        final int headerX;
        final int headerY;
        final int headerW;
        final int headerH;
        final int leftX;
        final int rightX;
        final int colW;
        final int colGap;
        final int columnHeaderY;
        final int columnHeaderH;
        final int listTop;
        final int listH;

        DisplayNamesLayout(int panelX, int panelY, int panelW, int panelH, int headerX, int headerY, int headerW, int headerH, int leftX, int rightX, int colW, int colGap, int columnHeaderY, int columnHeaderH, int listTop, int listH) {
            this.panelX = panelX;
            this.panelY = panelY;
            this.panelW = panelW;
            this.panelH = panelH;
            this.headerX = headerX;
            this.headerY = headerY;
            this.headerW = headerW;
            this.headerH = headerH;
            this.leftX = leftX;
            this.rightX = rightX;
            this.colW = colW;
            this.colGap = colGap;
            this.columnHeaderY = columnHeaderY;
            this.columnHeaderH = columnHeaderH;
            this.listTop = listTop;
            this.listH = listH;
        }
    }

    private static final class GearRenderButton {
        final GearRenderButtonType type;
        final String name;
        final int x;
        final int y;
        final int w;
        final int h;

        GearRenderButton(GearRenderButtonType type, String name, int x, int y, int w, int h) {
            this.type = type;
            this.name = name;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    private static enum GearRenderButtonType {
        TPA,
        STATS,
        AUTO_TP,
        BLOCK,
        BLACKLIST,
        CHANGE_CH,
        RESET;

    }
}

