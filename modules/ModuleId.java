package me.Gui.gui.modules;

public enum ModuleId {
    GUI("Gui", "Otwiera GUI"),
    NO_PUSH("No Push", "Bloki cie nie wypychaja"),
    FREECAM("Freecam", "Mozesz latac przez bloki"),
    XRAY("Xray", "Widzisz tylko wybrane bloki"),
    BLOCK_ESP("Block Esp", "Podswietla wybrane bloki"),
    ITEM_ESP("Item Esp", "Podswietla wybrane itemy"),
    TRACERS("Tracers", "Pokazuje linie do graczy"),
    ESP("ESP", "Pokazuje hitboxy graczy"),
    BETTER_HITBOXES("Better Hitboxes", "Powieksza hitbox graczy"),
    NAMETAGS("Nametags", "Pokazuje informacje o graczu"),
    NAME_PROTECT("NameProtect", "Ukrywa twoj nick"),
    ERROR_KILLER("Error Killer", "Podmienia dzwignie na weba"),
    ANTYKOSTKA("Antykostka", "Ustawia twoje eq pod bindem", false, true),
    ARMOR_EQUIPPER("Armor Equipper", "Przebiera twojego seta", false, true),
    FAST_LEVER("FastLever", "Samo zabiera wode przeciwnika"),
    BETTER_PAINTING("BetterPainting", "! Pozwala stawiac obrazy przez przeszkody"),
    AUTO_TOTEM("AutoTotem", "! Automatycznie zaklada ci totemy"),
    AUTO_PARAWAN("AutoParawan", "Uzywa parawanu gdy gracz jest blisko"),
    AIM_ASSIST("Aim Assist", "Automatycznie namierza najblizszego wroga"),
    SPAMMER("Spammer", "Automatycznie pisze na chacie"),
    FAST_PLACE("FastPlace", "Pozwala ci szybciej stawiac bloki"),
    AUTO_DRIP("AutoDrip", "Auto stawia dripstone pod trapdoorem"),
    NOJUMP_DELAY("NojumpDelay", "pozwala ci szybciej skakac"),
    NO_BAD_EFFECTS("NoBadEfects", "Wylacza wizualne efekty potek"),
    NO_HURT_CAM("NoHurtCam", "Kamera sie nie trzesie po dmg"),
    ANTI_TRAP("AntiTrap", "! Usuwa trapy np. minecarty"),
    FULLBRIGHT("Fullbright", "Oswietla teren"),
    PEARL_TRAJECTORY("PearlTrajectory", "pokazuje dodatkowe info do perel"),
    LOGOUT_SPOTS("LogoutSpots", "pokazuje miejsca wylogowania"),
    SWORD_INFO("SwordInfo", "pokazuje procenty na mieczu"),
    PANIC_MODE("PanicMode", "Szybko chowa HUD"),
    REFILLER("Refiller", "Automatycznie dobiera rzeczy na pasek"),
    TPACCEPT_SOUNDS("Tpaccept Sounds", "Dzwiek po akceptacji tpa"),
    NO_GUI_SIGN("NoGuiSign", "Nie otwiera gui tabliczek"),
    NO_GUI_BLUR("NoGuiBlur", "Wylacza rozmycie tla", true, true);

    public final String display;
    public final String description;
    public final boolean hidden;
    public final boolean forceEnabled;

    private ModuleId(String display, String description) {
        this(display, description, false, false);
    }

    private ModuleId(String display, String description, boolean hidden, boolean forceEnabled) {
        this.display = display;
        this.description = description;
        this.hidden = hidden;
        this.forceEnabled = forceEnabled;
    }
}

