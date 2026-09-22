package me.Gui.gui.config;

import java.util.ArrayList;
import java.util.List;

public class GuiConfig {
    public float guiScale = 0.85f;
    public float transparency = 0.2f;
    public float uiVolume = 0.7f;
    public Float tpaAcceptSoundVolume = Float.valueOf(1.0f);
    public Boolean showKeybindsHud = true;
    public Boolean showArrayList = true;
    public Boolean showNotifications = true;
    public Boolean showCoordinatesHud = false;
    public Boolean showArmorHud = false;
    public Boolean armorHudDurabilityPercent = true;
    public Boolean armorHudVertical = false;
    public Boolean showPotionsHud = false;
    public Boolean showCooldownsHud = false;
    public Boolean showInventoryHud = false;
    public Boolean showPingHud = false;
    public Boolean showFpsHud = false;
    public Boolean keybindsUseGuiColor = false;
    public Boolean arrayListUseGuiColor = false;
    public Boolean notificationsUseGuiColor = false;
    public Boolean coordinatesUseGuiColor = false;
    public Boolean armorUseGuiColor = false;
    public Boolean potionsUseGuiColor = false;
    public Boolean cooldownsUseGuiColor = false;
    public Boolean inventoryUseGuiColor = false;
    public Boolean pingUseGuiColor = false;
    public Boolean fpsUseGuiColor = false;
    public Float keybindsHudAlpha = Float.valueOf(1.0f);
    public Float notificationsHudAlpha = Float.valueOf(1.0f);
    public Float coordinatesHudAlpha = Float.valueOf(1.0f);
    public Float armorHudAlpha = Float.valueOf(1.0f);
    public Float potionsHudAlpha = Float.valueOf(1.0f);
    public Float cooldownsHudAlpha = Float.valueOf(1.0f);
    public Float inventoryHudAlpha = Float.valueOf(1.0f);
    public Float pingHudAlpha = Float.valueOf(1.0f);
    public Float fpsHudAlpha = Float.valueOf(1.0f);
    public Integer notificationsX = null;
    public Integer notificationsY = null;
    public float notificationsScale = 0.5f;
    public Boolean panicHideArrayList = true;
    public Boolean panicHideNotifications = true;
    public Boolean panicHideKeybinds = true;
    public boolean keybindsOnlyEnabled = false;
    public float fullbrightStrength = 0.5f;
    public Boolean pearlShowTrajectory = true;
    public Boolean pearlShowLanding = true;
    public Boolean pearlShowNickname = true;
    public Boolean pearlShowCountdown = true;
    public Boolean pearlShowOwn = true;
    public float pearlLineWidth = 2.0f;
    public Boolean logoutShowNick = true;
    public Boolean logoutShowTime = true;
    public Boolean gearRenderAutoTpa = false;
    public Boolean gearRenderAutoLogout = true;
    public List<String> gearRenderBlacklist = new ArrayList<String>();
    public int errorKillerDelayMs = 1;
    public int errorKillerBindObsidian = -1;
    public int errorKillerDelayMsObsidian = 1;
    public Boolean errorKillerShowMissing = false;
    public Boolean errorKillerShowMissingObsidian = false;
    public int errorKillerHudX = 14;
    public int errorKillerHudY = 120;
    public String spammerMessage = "";
    public Boolean spammerAntiSpam = false;
    public int spammerDelaySec = 3;
    public NameProtectMode nameProtectMode = NameProtectMode.SELF;
    public String nameProtectName = "";
    public Boolean nameProtectChangeStats = false;
    public String nameProtectStatTime = "";
    public String nameProtectStatMoney = "";
    public String nameProtectStatKills = "";
    public String nameProtectStatDeaths = "";
    public Boolean tracerPlayers = true;
    public Boolean tracerMobs = true;
    public Boolean tracerFriends = true;
    public Boolean friendsChangeNormalNametag = true;
    public Boolean espBox = true;
    public int espPlayerColor = 0xFFFFFF;
    public int espFriendColor = 0x33F233;
    public float espLineWidth = 4.0f;
    public float tracerPlayerHue = 0.0f;
    public float tracerMobHue = 0.33f;
    public float tracerFriendHue = 0.55f;
    public float tracerDistance = 64.0f;
    public Boolean swordInfoShowName = true;
    public Boolean swordInfoShowEnchant = true;
    public Boolean swordInfoShowExtra = true;
    public Boolean swordInfoFrame = true;
    public Integer swordInfoExtraColor = 5611775;
    public float swordInfoScale = 0.9f;
    public int swordInfoX = -1;
    public int swordInfoY = -1;
    public Boolean swordInfoShowAllPlayers = false;
    public float swordInfoAllScale = 0.8f;
    public int swordInfoAllX = -1;
    public int swordInfoAllY = -1;
    public float cooldownListScale = 0.9f;
    public int cooldownListX = 14;
    public int cooldownListY = 200;
    public float coordinatesScale = 0.9f;
    public int coordinatesX = 14;
    public int coordinatesY = 14;
    public float armorHudScale = 0.9f;
    public int armorHudX = 14;
    public int armorHudY = 40;
    public float potionsHudScale = 0.9f;
    public int potionsHudX = 14;
    public int potionsHudY = 70;
    public float inventoryHudScale = 0.9f;
    public int inventoryHudX = 14;
    public int inventoryHudY = 120;
    public float pingHudScale = 0.9f;
    public int pingHudX = 14;
    public int pingHudY = 240;
    public float fpsHudScale = 0.9f;
    public int fpsHudX = 14;
    public int fpsHudY = 260;
    public List<String> blockEspIds = new ArrayList<String>();
    public List<String> itemEspIds = new ArrayList<String>();
    public Boolean itemEspAddAll = false;
    @Deprecated
    public Boolean itemEspOverlay = false;
    public ItemEspMode itemEspMode = ItemEspMode.SQUARE;
    public List<String> xrayIds = new ArrayList<String>();
    public Boolean antiTrapItemFrame = true;
    public Boolean antiTrapItemFrameAutoBreak = true;
    public Boolean antiTrapItemFrameVisual = true;
    public Boolean antiTrapPainting = true;
    public Boolean antiTrapPaintingAutoBreak = true;
    public Boolean antiTrapPaintingVisual = true;
    public Boolean antiTrapMinecart = true;
    public Boolean antiTrapArmorStand = true;
    public Boolean antiTrapMobs = false;
    public Boolean antiTrapAutoPlotek = false;
    public Boolean autoParawanDontAttackFriends = true;
    public float aimAssistRange = 6.0f;
    public Boolean aimAssistTargetLock = false;
    public Boolean aimAssistMobs = false;
    public float betterHitboxSizeX = 100.0f;
    public float betterHitboxSizeY = 100.0f;
    public int fastPlaceDelay = 0;
    public int armorEquipperDelayTicks = 1;
    public float fastLeverSpeed = 100.0f;
    public int autoPlotekCooldownMs = 80;
    public float antyKostkaSpeed = 0.0f;
    public Boolean noPushPlayers = true;
    public Boolean noPushBlocks = true;
    public Boolean noPushLiquids = true;
    public Boolean freecamCancelMove = true;
    public float freecamSpeedX = 0.45f;
    public float freecamSpeedY = 0.3f;
    public Boolean refillerObsidian = true;
    public Boolean refillerCobwebs = true;
    public Boolean refillerPearls = true;
    public Boolean nametagsShowSelf = true;
    public Boolean nametagsShowDistance = true;
    public Boolean nametagsShowName = true;
    public Boolean nametagsShowHealth = true;
    public Boolean nametagsShowEnchantments = true;
    public Boolean nametagsShowSetBonus = true;
    public Boolean nametagsShowEffects = true;
    public Boolean nametagsShowEffectDuration = true;
    public Boolean nametagsShowDurability = true;
    public Boolean nametagsShowArmorSet = true;
    public Boolean nametagsShowHandItems = true;
    public Boolean nametagsShowItemCount = true;
    public Boolean nametagsShortenDistance = false;
    public int nametagsTextColor = 0xFFFFFF;
    public int nametagsBackgroundColor = 22015;
    public int nametagsBorderColor = 0xFFFFFF;
    public int nametagsTextAlpha = 255;
    public int nametagsBackgroundAlpha = 118;
    public int nametagsScalePercent = 100;
    public float bindListScale = 0.75f;
    public float arrayListScale = 1.0f;
    public int arrayListX = 8;
    public int arrayListY = 8;
    public Boolean arrayListRight = true;
    public ColorMode colorMode = ColorMode.SINGLE;
    public float singleHue = 0.6f;
    public float gradHueA = 0.6f;
    public float gradHueB = 0.9f;
    public float gradSpeed = 0.35f;
    public float rainbowSpeed = 0.45f;
    public String activeConfig = "default";
    public int bindListX = 14;
    public int bindListY = 80;

    public static enum NameProtectMode {
        SELF,
        EVERYONE;

    }

    public static enum ItemEspMode {
        OVERLAY,
        SQUARE;

    }

    public static enum ColorMode {
        SINGLE,
        GRADIENT,
        RAINBOW;

    }
}

