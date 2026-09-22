package me.Gui.gui.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import me.Gui.gui.client.GuiClient;
import me.Gui.gui.ui.GuiFonts;
import me.Gui.gui.ui.Theme;
import me.Gui.gui.ui.render.HudGlass;
import me.Gui.gui.ui.render.Rounded;
import me.Gui.gui.ui.widget.UiSlider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;

public final class HudEditScreen
extends Screen {
    private static final int MENU_PAD_X = 10;
    private static final int MENU_PAD_Y = 8;
    private static final int MENU_ITEM_H = 22;
    private static final int MENU_ITEM_GAP = 4;
    private static final int MENU_ICON_SIZE = 12;
    private static final int MENU_ICON_GAP = 8;
    private static final float MENU_RADIUS = 8.0f;
    private static final int MENU_MARGIN = 6;
    private static final int SETTINGS_PAD_X = 10;
    private static final int SETTINGS_PAD_Y = 8;
    private static final int SETTINGS_ROW_H = 26;
    private static final int SETTINGS_ROW_GAP = 6;
    private static final int SETTINGS_TITLE_H = 18;
    private static final int SETTINGS_BTN_H = 16;
    private static final int SETTINGS_BTN_PAD_X = 6;
    private static final int SETTINGS_BTN_GAP = 6;
    private static final int SETTINGS_RADIUS = 8;
    private static final String DELETE_LABEL = "Delete";
    private static final int SETTINGS_SLIDER_W = 180;
    private static final int SETTINGS_SLIDER_H = 26;
    private static final float NOTIF_SCALE_MIN = 0.35f;
    private static final float NOTIF_SCALE_MAX = 1.2f;
    private static final float HUD_ALPHA_MIN = 0.0f;
    private static final float HUD_ALPHA_MAX = 1.0f;
    private static final Map<HudItem, Bound> ITEM_BOUNDS = new HashMap<HudItem, Bound>();
    private static Object lastWorldRef = null;
    private static HudItem hoverItem = HudItem.NONE;
    private static long hoverAtMs = 0L;
    private static final HudItem[] ITEMS = new HudItem[]{HudItem.COORDINATES, HudItem.ARMOR, HudItem.POTIONS, HudItem.COOLDOWNS, HudItem.INVENTORY, HudItem.PING, HudItem.FPS, HudItem.KEYBINDS, HudItem.ARRAY_LIST, HudItem.NOTIFICATIONS};
    private static final HudItem[] HIT_PRIORITY = new HudItem[]{HudItem.COORDINATES, HudItem.ARMOR, HudItem.POTIONS, HudItem.COOLDOWNS, HudItem.INVENTORY, HudItem.PING, HudItem.FPS, HudItem.KEYBINDS, HudItem.ARRAY_LIST, HudItem.NOTIFICATIONS};
    private static final String[] ITEM_LABELS = new String[]{"Coordinates", "Armor HUD", "Potions", "Cooldowns", "Inventory", "Ping", "FPS", "Keybinds", "Array List", "Notifications"};
    private static final Set<HudItem> activeItems = new HashSet<HudItem>();
    private boolean menuOpen = false;
    private int menuX;
    private int menuY;
    private int menuW;
    private int menuH;
    private boolean settingsOpen = false;
    private HudItem settingsItem = HudItem.NONE;
    private int settingsX;
    private int settingsY;
    private int settingsW;
    private int settingsH;
    private UiSlider sizeSlider;
    private HudItem sizeSliderItem = HudItem.NONE;
    private UiSlider transparencySlider;
    private HudItem transparencySliderItem = HudItem.NONE;

    public HudEditScreen() {
        super((Text)Text.literal((String)"Hud Edit"));
        HudEditScreen.syncActiveFromConfig();
    }

    public static boolean isActive() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.currentScreen instanceof HudEditScreen;
    }

    public static boolean isActiveFor(HudItem item) {
        return HudEditScreen.isActive() && activeItems.contains((Object)item);
    }

    public static HudItem getActiveItem() {
        return HudItem.NONE;
    }

    public static void setBounds(HudItem item, int x, int y, int w, int h) {
        if (item == null || item == HudItem.NONE) {
            return;
        }
        HudEditScreen.syncWorld();
        ITEM_BOUNDS.put(item, new Bound(x, y, w, h, System.currentTimeMillis()));
    }

    public static void clearBounds(HudItem item) {
        if (item == null) {
            return;
        }
        ITEM_BOUNDS.remove((Object)item);
    }

    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        HudEditScreen.syncWorld();
        HudEditScreen.syncActiveFromConfig();
        HudEditScreen.updateHover(mouseX, mouseY);
        if (!this.settingsOpen) {
            this.sizeSliderItem = HudItem.NONE;
            this.transparencySliderItem = HudItem.NONE;
        }
        if (this.settingsOpen) {
            this.renderSettings(ctx, mouseX, mouseY);
        }
        if (this.menuOpen) {
            this.renderMenu(ctx, mouseX, mouseY);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int)Math.round(mouseX);
        int my = (int)Math.round(mouseY);
        if (button == 1) {
            HudItem hit = HudEditScreen.pickHoverOrHit(mx, my);
            if (hit != HudItem.NONE) {
                this.openSettings(hit);
                return true;
            }
            if (this.settingsOpen) {
                this.settingsOpen = false;
                this.settingsItem = HudItem.NONE;
                if (this.sizeSlider != null) {
                    this.sizeSlider.dragging = false;
                }
                if (this.transparencySlider != null) {
                    this.transparencySlider.dragging = false;
                }
                this.sizeSliderItem = HudItem.NONE;
                this.transparencySliderItem = HudItem.NONE;
            }
            if (this.menuOpen) {
                this.menuOpen = false;
            } else {
                this.openMenu(mx, my);
            }
            return true;
        }
        if (this.settingsOpen && button == 0) {
            if (this.handleSettingsClick(mx, my)) {
                return true;
            }
            if (!HudEditScreen.inside(mx, my, this.settingsX, this.settingsY, this.settingsW, this.settingsH)) {
                this.settingsOpen = false;
                this.settingsItem = HudItem.NONE;
                if (this.sizeSlider != null) {
                    this.sizeSlider.dragging = false;
                }
                if (this.transparencySlider != null) {
                    this.transparencySlider.dragging = false;
                }
                this.sizeSliderItem = HudItem.NONE;
                this.transparencySliderItem = HudItem.NONE;
            }
            return true;
        }
        if (this.settingsOpen && button == 2) {
            this.settingsOpen = false;
            this.settingsItem = HudItem.NONE;
            if (this.sizeSlider != null) {
                this.sizeSlider.dragging = false;
            }
            if (this.transparencySlider != null) {
                this.transparencySlider.dragging = false;
            }
            this.sizeSliderItem = HudItem.NONE;
            this.transparencySliderItem = HudItem.NONE;
            return true;
        }
        if (this.menuOpen && button == 0) {
            int idx = this.hitMenuItem(mx, my);
            if (idx >= 0) {
                this.toggleItem(idx);
            }
            this.menuOpen = false;
            return true;
        }
        if (this.menuOpen && button == 2) {
            this.menuOpen = false;
            return true;
        }
        return true;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.close();
            return true;
        }
        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.settingsOpen && this.transparencySlider != null && button == 0 && this.transparencySlider.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        if (this.settingsOpen && this.sizeSlider != null && button == 0 && this.sizeSlider.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.settingsOpen && button == 0) {
            boolean alphaWasDragging;
            boolean sizeWasDragging = this.sizeSlider != null && this.sizeSlider.dragging;
            boolean bl = alphaWasDragging = this.transparencySlider != null && this.transparencySlider.dragging;
            if (this.sizeSlider != null) {
                this.sizeSlider.mouseReleased(mouseX, mouseY, button);
            }
            if (this.transparencySlider != null) {
                this.transparencySlider.mouseReleased(mouseX, mouseY, button);
            }
            if (sizeWasDragging || alphaWasDragging) {
                HudEditScreen.saveConfig();
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean charTyped(char chr, int modifiers) {
        return true;
    }

    public boolean shouldPause() {
        return false;
    }

    private void openMenu(int x, int y) {
        this.menuOpen = true;
        this.settingsOpen = false;
        this.settingsItem = HudItem.NONE;
        this.sizeSliderItem = HudItem.NONE;
        this.transparencySliderItem = HudItem.NONE;
        this.updateMenuLayout();
        this.menuX = (this.width - this.menuW) / 2;
        this.menuY = (this.height - this.menuH) / 2;
        this.clampMenuToScreen();
    }

    private void renderMenu(DrawContext ctx, int mouseX, int mouseY) {
        this.updateMenuLayout();
        this.menuX = (this.width - this.menuW) / 2;
        this.menuY = (this.height - this.menuH) / 2;
        this.clampMenuToScreen();
        int accent = Theme.accentColor(System.currentTimeMillis());
        int textColor = Theme.withAlpha(accent, 255);
        HudGlass.panel(ctx, this.menuX, this.menuY, this.menuW, this.menuH, 8.0f, accent);
        TextRenderer tr = GuiFonts.textRendererOrDefault(MinecraftClient.getInstance().textRenderer);
        HudItem[] avail = HudEditScreen.availableItems();
        int y = this.menuY + 8;
        if (avail.length == 0) {
            String label = "All active";
            Objects.requireNonNull(tr);
            int labelY = y + (22 - 9) / 2;
            ctx.drawText(tr, (Text)Text.literal((String)label), this.menuX + 10, labelY, textColor, false);
            return;
        }
        for (int i = 0; i < avail.length; ++i) {
            boolean hover = HudEditScreen.inside(mouseX, mouseY, this.menuX, y, this.menuW, 22);
            if (hover) {
                int hoverBg = Theme.withAlpha(accent, 55);
                Rounded.rect(ctx, this.menuX + 2, y, this.menuW - 4, 22, 6.0f, hoverBg);
            }
            int iconX = this.menuX + 10;
            int iconY = y + 5;
            HudEditScreen.drawMenuIcon(ctx, avail[i], iconX, iconY, 12, textColor);
            int labelX = iconX + 12 + 8;
            Objects.requireNonNull(tr);
            int labelY = y + (22 - 9) / 2;
            ctx.drawText(tr, (Text)Text.literal((String)HudEditScreen.labelFor(avail[i])), labelX, labelY, textColor, false);
            y += 26;
        }
    }

    private void renderSettings(DrawContext ctx, int mouseX, int mouseY) {
        boolean rightHover;
        boolean leftHover;
        if (this.settingsItem == HudItem.NONE) {
            return;
        }
        TextRenderer tr = GuiFonts.textRendererOrDefault(MinecraftClient.getInstance().textRenderer);
        SettingsLayout layout = this.buildSettingsLayout(tr);
        if (layout == null) {
            return;
        }
        int accent = Theme.accentColor(System.currentTimeMillis());
        HudGlass.panel(ctx, layout.x, layout.y, layout.w, layout.h, 8.0f, accent);
        String title = HudEditScreen.labelFor(this.settingsItem);
        int n = layout.titleY;
        Objects.requireNonNull(tr);
        int titleY = n + (18 - 9) / 2;
        ctx.drawText(tr, (Text)Text.literal((String)title), layout.titleX, titleY, -1, false);
        boolean trashHover = HudEditScreen.inside(mouseX, mouseY, layout.trash.x, layout.trash.y, layout.trash.w, layout.trash.h);
        int trashBg = trashHover ? Theme.argb(190, 20, 20, 26) : Theme.argb(165, 14, 14, 18);
        Rounded.rect(ctx, layout.trash.x, layout.trash.y, layout.trash.w, layout.trash.h, 5.0f, trashBg);
        int delX = layout.trash.x + (layout.trash.w - tr.getWidth(DELETE_LABEL)) / 2;
        int n2 = layout.trash.y;
        int n3 = layout.trash.h;
        Objects.requireNonNull(tr);
        int delY = n2 + (n3 - 9) / 2;
        int delColor = Theme.withAlpha(accent, 255);
        ctx.drawText(tr, (Text)Text.literal((String)DELETE_LABEL), delX, delY, delColor, false);
        int sliderBg = Theme.argb(150, 20, 20, 26);
        int sliderFill = Theme.withAlpha(accent, 255);
        this.ensureSizeSlider(layout);
        if (this.sizeSlider != null) {
            this.sizeSlider.render(ctx, mouseX, mouseY, sliderBg, sliderFill, -1);
        }
        this.ensureTransparencySlider(layout);
        if (this.transparencySlider != null) {
            this.transparencySlider.render(ctx, mouseX, mouseY, sliderBg, sliderFill, -1);
        }
        if (layout.guiColor != null) {
            boolean useColor = this.getUseGuiColor(this.settingsItem);
            boolean hover = HudEditScreen.inside(mouseX, mouseY, layout.guiColor.x, layout.guiColor.y, layout.guiColor.w, layout.guiColor.h);
            HudEditScreen.drawButton(ctx, tr, layout.guiColor, "Use Gui Color", useColor, hover, accent);
        }
        if (layout.modeAll != null && layout.modeOnly != null) {
            leftHover = HudEditScreen.inside(mouseX, mouseY, layout.modeAll.x, layout.modeAll.y, layout.modeAll.w, layout.modeAll.h);
            rightHover = HudEditScreen.inside(mouseX, mouseY, layout.modeOnly.x, layout.modeOnly.y, layout.modeOnly.w, layout.modeOnly.h);
            if (this.settingsItem == HudItem.KEYBINDS) {
                boolean onlyEnabled = GuiClient.CONFIG.keybindsOnlyEnabled;
                HudEditScreen.drawButton(ctx, tr, layout.modeAll, "All", !onlyEnabled, leftHover, accent);
                HudEditScreen.drawButton(ctx, tr, layout.modeOnly, "Only Enabled", onlyEnabled, rightHover, accent);
            } else if (this.settingsItem == HudItem.ARMOR) {
                boolean percent = GuiClient.CONFIG.armorHudDurabilityPercent == null || GuiClient.CONFIG.armorHudDurabilityPercent != false;
                HudEditScreen.drawButton(ctx, tr, layout.modeAll, "Percent", percent, leftHover, accent);
                HudEditScreen.drawButton(ctx, tr, layout.modeOnly, "Amount", !percent, rightHover, accent);
            }
        }
        if (layout.modeAll2 != null && layout.modeOnly2 != null) {
            leftHover = HudEditScreen.inside(mouseX, mouseY, layout.modeAll2.x, layout.modeAll2.y, layout.modeAll2.w, layout.modeAll2.h);
            rightHover = HudEditScreen.inside(mouseX, mouseY, layout.modeOnly2.x, layout.modeOnly2.y, layout.modeOnly2.w, layout.modeOnly2.h);
            if (this.settingsItem == HudItem.ARMOR) {
                boolean vertical = GuiClient.CONFIG.armorHudVertical != null && GuiClient.CONFIG.armorHudVertical != false;
                HudEditScreen.drawButton(ctx, tr, layout.modeAll2, "Horizontal", !vertical, leftHover, accent);
                HudEditScreen.drawButton(ctx, tr, layout.modeOnly2, "Vertical", vertical, rightHover, accent);
            }
        }
    }

    private SettingsLayout buildSettingsLayout(TextRenderer tr) {
        if (this.settingsItem == HudItem.NONE) {
            return null;
        }
        String title = HudEditScreen.labelFor(this.settingsItem);
        int titleW = tr.getWidth(title);
        int deleteW = tr.getWidth(DELETE_LABEL) + 12;
        int sliderLabelW = tr.getWidth(HudEditScreen.sliderLabelFor(this.settingsItem));
        int sizeRowW = Math.max(180, sliderLabelW + 40);
        boolean showTransparency = HudEditScreen.showTransparency(this.settingsItem);
        int alphaRowW = 0;
        if (showTransparency) {
            int alphaLabelW = tr.getWidth(HudEditScreen.transparencyLabelFor(this.settingsItem));
            alphaRowW = Math.max(180, alphaLabelW + 40);
        }
        int modeRowW = 0;
        int modeRow2W = 0;
        if (this.settingsItem == HudItem.KEYBINDS) {
            int allW = tr.getWidth("All") + 12;
            int onlyW = tr.getWidth("Only Enabled") + 12;
            modeRowW = allW + 6 + onlyW;
        } else if (this.settingsItem == HudItem.ARMOR) {
            int pctW = tr.getWidth("Percent") + 12;
            int amtW = tr.getWidth("Amount") + 12;
            modeRowW = pctW + 6 + amtW;
            int horW = tr.getWidth("Horizontal") + 12;
            int verW = tr.getWidth("Vertical") + 12;
            modeRow2W = horW + 6 + verW;
        }
        boolean showGuiColor = this.settingsItem != HudItem.ARRAY_LIST;
        int guiColorW = showGuiColor ? tr.getWidth("Use Gui Color") + 12 : 0;
        int contentW = Math.max(sizeRowW, Math.max(alphaRowW, Math.max(modeRowW, Math.max(modeRow2W, guiColorW))));
        contentW = Math.max(contentW, titleW + deleteW + 6);
        int rows = 1;
        if (showTransparency) {
            ++rows;
        }
        if (showGuiColor) {
            ++rows;
        }
        if (this.settingsItem == HudItem.KEYBINDS) {
            ++rows;
        }
        if (this.settingsItem == HudItem.ARMOR) {
            rows += 2;
        }
        int w = contentW + 20;
        int h = 34 + rows * 26 + Math.max(0, rows - 1) * 6;
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;
        this.settingsX = x;
        this.settingsY = y;
        this.settingsW = w;
        this.settingsH = h;
        int titleX = x + 10;
        int titleY = y + 8;
        Rect trash = new Rect(x + w - 10 - deleteW, titleY + 1, deleteW, 16);
        int rowY = titleY + 18 + 6;
        int sliderW = w - 20;
        int sliderY = rowY + 0;
        Rect sizeSlider = new Rect(x + 10, sliderY, sliderW, 26);
        rowY += 32;
        Rect transparencySlider = null;
        if (showTransparency) {
            int alphaY = rowY + 0;
            transparencySlider = new Rect(x + 10, alphaY, sliderW, 26);
            rowY += 32;
        }
        Rect guiColor = null;
        if (showGuiColor) {
            int guiY = rowY + 5;
            guiColor = new Rect(x + 10, guiY, sliderW, 16);
            rowY += 32;
        }
        Rect modeAll = null;
        Rect modeOnly = null;
        Rect modeAll2 = null;
        Rect modeOnly2 = null;
        if (this.settingsItem == HudItem.KEYBINDS) {
            int allW = tr.getWidth("All") + 12;
            int onlyW = tr.getWidth("Only Enabled") + 12;
            int allX = x + 10;
            int allY = rowY + 5;
            modeAll = new Rect(allX, allY, allW, 16);
            modeOnly = new Rect(allX + allW + 6, allY, onlyW, 16);
        } else if (this.settingsItem == HudItem.ARMOR) {
            int pctW = tr.getWidth("Percent") + 12;
            int amtW = tr.getWidth("Amount") + 12;
            int pctX = x + 10;
            int pctY = rowY + 5;
            modeAll = new Rect(pctX, pctY, pctW, 16);
            modeOnly = new Rect(pctX + pctW + 6, pctY, amtW, 16);
            int row4Y = rowY + 26 + 6;
            int horW = tr.getWidth("Horizontal") + 12;
            int verW = tr.getWidth("Vertical") + 12;
            int horX = x + 10;
            int horY = row4Y + 5;
            modeAll2 = new Rect(horX, horY, horW, 16);
            modeOnly2 = new Rect(horX + horW + 6, horY, verW, 16);
        }
        return new SettingsLayout(x, y, w, h, titleX, titleY, trash, sizeSlider, transparencySlider, guiColor, modeAll, modeOnly, modeAll2, modeOnly2);
    }

    private boolean handleSettingsClick(int mx, int my) {
        if (this.settingsItem == HudItem.NONE) {
            return false;
        }
        TextRenderer tr = GuiFonts.textRendererOrDefault(MinecraftClient.getInstance().textRenderer);
        SettingsLayout layout = this.buildSettingsLayout(tr);
        if (layout == null) {
            return false;
        }
        this.ensureSizeSlider(layout);
        this.ensureTransparencySlider(layout);
        if (HudEditScreen.inside(mx, my, layout.trash.x, layout.trash.y, layout.trash.w, layout.trash.h)) {
            this.deactivateItem(this.settingsItem);
            this.settingsOpen = false;
            this.settingsItem = HudItem.NONE;
            this.sizeSliderItem = HudItem.NONE;
            return true;
        }
        if (layout.guiColor != null && HudEditScreen.inside(mx, my, layout.guiColor.x, layout.guiColor.y, layout.guiColor.w, layout.guiColor.h)) {
            this.setUseGuiColor(this.settingsItem, !this.getUseGuiColor(this.settingsItem));
            HudEditScreen.saveConfig();
            return true;
        }
        if (this.sizeSlider != null && this.sizeSlider.mouseClicked(mx, my, 0)) {
            return true;
        }
        if (this.transparencySlider != null && this.transparencySlider.mouseClicked(mx, my, 0)) {
            return true;
        }
        if (layout.modeAll != null && layout.modeOnly != null) {
            if (this.settingsItem == HudItem.KEYBINDS) {
                if (HudEditScreen.inside(mx, my, layout.modeAll.x, layout.modeAll.y, layout.modeAll.w, layout.modeAll.h)) {
                    GuiClient.CONFIG.keybindsOnlyEnabled = false;
                    HudEditScreen.saveConfig();
                    return true;
                }
                if (HudEditScreen.inside(mx, my, layout.modeOnly.x, layout.modeOnly.y, layout.modeOnly.w, layout.modeOnly.h)) {
                    GuiClient.CONFIG.keybindsOnlyEnabled = true;
                    HudEditScreen.saveConfig();
                    return true;
                }
            } else if (this.settingsItem == HudItem.ARMOR) {
                if (HudEditScreen.inside(mx, my, layout.modeAll.x, layout.modeAll.y, layout.modeAll.w, layout.modeAll.h)) {
                    GuiClient.CONFIG.armorHudDurabilityPercent = true;
                    HudEditScreen.saveConfig();
                    return true;
                }
                if (HudEditScreen.inside(mx, my, layout.modeOnly.x, layout.modeOnly.y, layout.modeOnly.w, layout.modeOnly.h)) {
                    GuiClient.CONFIG.armorHudDurabilityPercent = false;
                    HudEditScreen.saveConfig();
                    return true;
                }
            }
        }
        if (layout.modeAll2 != null && layout.modeOnly2 != null && this.settingsItem == HudItem.ARMOR) {
            if (HudEditScreen.inside(mx, my, layout.modeAll2.x, layout.modeAll2.y, layout.modeAll2.w, layout.modeAll2.h)) {
                GuiClient.CONFIG.armorHudVertical = false;
                HudEditScreen.saveConfig();
                return true;
            }
            if (HudEditScreen.inside(mx, my, layout.modeOnly2.x, layout.modeOnly2.y, layout.modeOnly2.w, layout.modeOnly2.h)) {
                GuiClient.CONFIG.armorHudVertical = true;
                HudEditScreen.saveConfig();
                return true;
            }
        }
        return false;
    }

    private void openSettings(HudItem item) {
        if (item == null || item == HudItem.NONE) {
            return;
        }
        this.settingsOpen = true;
        this.settingsItem = item;
        this.menuOpen = false;
        this.sizeSlider = null;
        this.transparencySlider = null;
        this.sizeSliderItem = HudItem.NONE;
        this.transparencySliderItem = HudItem.NONE;
    }

    private static HudItem hitItem(int mx, int my) {
        return HudEditScreen.selectItemAt(mx, my);
    }

    private static HudItem pickHoverOrHit(int mx, int my) {
        long now = System.currentTimeMillis();
        if (hoverItem != HudItem.NONE && now - hoverAtMs <= 200L) {
            return hoverItem;
        }
        return HudEditScreen.hitItem(mx, my);
    }

    private static void updateHover(int mx, int my) {
        hoverItem = HudEditScreen.selectItemAt(mx, my);
        hoverAtMs = System.currentTimeMillis();
    }

    private static HudItem selectItemAt(int mx, int my) {
        HudEditScreen.syncWorld();
        long now = System.currentTimeMillis();
        HudItem best = HudItem.NONE;
        long bestDist2 = Long.MAX_VALUE;
        int bestArea = Integer.MAX_VALUE;
        int bestPriority = Integer.MAX_VALUE;
        for (Map.Entry<HudItem, Bound> entry : ITEM_BOUNDS.entrySet()) {
            Bound b;
            HudItem item = entry.getKey();
            if (item == null || item == HudItem.NONE || !activeItems.contains((Object)item) || (b = entry.getValue()) == null || now - b.atMs() > 1500L || b.w <= 0 || b.h <= 0 || !HudEditScreen.inside(mx, my, b.x, b.y, b.w, b.h)) continue;
            int area = b.w * b.h;
            if (area <= 0) {
                area = Integer.MAX_VALUE;
            }
            int cx = b.x + b.w / 2;
            int cy = b.y + b.h / 2;
            long dx = (long)mx - (long)cx;
            long dy = (long)my - (long)cy;
            long dist2 = dx * dx + dy * dy;
            int prio = HudEditScreen.hitPriority(item);
            if (area >= bestArea && (area != bestArea || dist2 >= bestDist2) && (area != bestArea || dist2 != bestDist2 || prio >= bestPriority)) continue;
            best = item;
            bestArea = area;
            bestDist2 = dist2;
            bestPriority = prio;
        }
        return best;
    }

    private static void syncWorld() {
        ClientWorld world;
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld clientWorld = world = client == null ? null : client.world;
        if (world != lastWorldRef) {
            ITEM_BOUNDS.clear();
            lastWorldRef = world;
        }
    }

    private static int hitPriority(HudItem item) {
        for (int i = 0; i < HIT_PRIORITY.length; ++i) {
            if (HIT_PRIORITY[i] != item) continue;
            return i;
        }
        return Integer.MAX_VALUE;
    }

    private void deactivateItem(HudItem item) {
        if (item == null || item == HudItem.NONE) {
            return;
        }
        activeItems.remove((Object)item);
        HudEditScreen.clearBounds(item);
        this.sizeSliderItem = HudItem.NONE;
        this.transparencySliderItem = HudItem.NONE;
        switch (item.ordinal()) {
            case 1: {
                GuiClient.CONFIG.showKeybindsHud = false;
                break;
            }
            case 2: {
                GuiClient.CONFIG.showArrayList = false;
                break;
            }
            case 3: {
                GuiClient.CONFIG.showNotifications = false;
                break;
            }
            case 4: {
                GuiClient.CONFIG.showCoordinatesHud = false;
                break;
            }
            case 5: {
                GuiClient.CONFIG.showArmorHud = false;
                break;
            }
            case 6: {
                GuiClient.CONFIG.showPotionsHud = false;
                break;
            }
            case 7: {
                GuiClient.CONFIG.showCooldownsHud = false;
                break;
            }
            case 8: {
                GuiClient.CONFIG.showInventoryHud = false;
                break;
            }
            case 9: {
                GuiClient.CONFIG.showPingHud = false;
                break;
            }
            case 10: {
                GuiClient.CONFIG.showFpsHud = false;
                break;
            }
        }
        HudEditScreen.saveConfig();
    }

    private float getScale(HudItem item) {
        float v = switch (item.ordinal()) {
            case 1 -> GuiClient.CONFIG.bindListScale;
            case 2 -> GuiClient.CONFIG.arrayListScale;
            case 3 -> GuiClient.CONFIG.notificationsScale;
            case 4 -> GuiClient.CONFIG.coordinatesScale;
            case 5 -> GuiClient.CONFIG.armorHudScale;
            case 6 -> GuiClient.CONFIG.potionsHudScale;
            case 7 -> GuiClient.CONFIG.cooldownListScale;
            case 8 -> GuiClient.CONFIG.inventoryHudScale;
            case 9 -> GuiClient.CONFIG.pingHudScale;
            case 10 -> GuiClient.CONFIG.fpsHudScale;
            default -> 1.0f;
        };
        if (Float.isNaN(v) || Float.isInfinite(v)) {
            v = 1.0f;
        }
        return HudEditScreen.clamp(v, this.minScale(item), this.maxScale(item));
    }

    private void setScale(HudItem item, float value) {
        switch (item.ordinal()) {
            case 1: {
                GuiClient.CONFIG.bindListScale = value;
                break;
            }
            case 2: {
                GuiClient.CONFIG.arrayListScale = value;
                break;
            }
            case 3: {
                GuiClient.CONFIG.notificationsScale = value;
                break;
            }
            case 4: {
                GuiClient.CONFIG.coordinatesScale = value;
                break;
            }
            case 5: {
                GuiClient.CONFIG.armorHudScale = value;
                break;
            }
            case 6: {
                GuiClient.CONFIG.potionsHudScale = value;
                break;
            }
            case 7: {
                GuiClient.CONFIG.cooldownListScale = value;
                break;
            }
            case 8: {
                GuiClient.CONFIG.inventoryHudScale = value;
                break;
            }
            case 9: {
                GuiClient.CONFIG.pingHudScale = value;
                break;
            }
            case 10: {
                GuiClient.CONFIG.fpsHudScale = value;
                break;
            }
        }
    }

    private boolean getUseGuiColor(HudItem item) {
        switch (item.ordinal()) {
            case 1: {
                return Boolean.TRUE.equals(GuiClient.CONFIG.keybindsUseGuiColor);
            }
            case 2: {
                return true;
            }
            case 3: {
                return Boolean.TRUE.equals(GuiClient.CONFIG.notificationsUseGuiColor);
            }
            case 4: {
                return Boolean.TRUE.equals(GuiClient.CONFIG.coordinatesUseGuiColor);
            }
            case 5: {
                return Boolean.TRUE.equals(GuiClient.CONFIG.armorUseGuiColor);
            }
            case 6: {
                return Boolean.TRUE.equals(GuiClient.CONFIG.potionsUseGuiColor);
            }
            case 7: {
                return Boolean.TRUE.equals(GuiClient.CONFIG.cooldownsUseGuiColor);
            }
            case 8: {
                return Boolean.TRUE.equals(GuiClient.CONFIG.inventoryUseGuiColor);
            }
            case 9: {
                return Boolean.TRUE.equals(GuiClient.CONFIG.pingUseGuiColor);
            }
            case 10: {
                return Boolean.TRUE.equals(GuiClient.CONFIG.fpsUseGuiColor);
            }
        }
        return false;
    }

    public static float getFrameAlpha(HudItem item) {
        float v;
        switch (item.ordinal()) {
            case 1: {
                v = HudEditScreen.readAlpha(GuiClient.CONFIG.keybindsHudAlpha);
                break;
            }
            case 3: {
                v = HudEditScreen.readAlpha(GuiClient.CONFIG.notificationsHudAlpha);
                break;
            }
            case 4: {
                v = HudEditScreen.readAlpha(GuiClient.CONFIG.coordinatesHudAlpha);
                break;
            }
            case 5: {
                v = HudEditScreen.readAlpha(GuiClient.CONFIG.armorHudAlpha);
                break;
            }
            case 6: {
                return 1.0f;
            }
            case 7: {
                v = HudEditScreen.readAlpha(GuiClient.CONFIG.cooldownsHudAlpha);
                break;
            }
            case 8: {
                v = HudEditScreen.readAlpha(GuiClient.CONFIG.inventoryHudAlpha);
                break;
            }
            case 9: {
                return 1.0f;
            }
            case 10: {
                v = HudEditScreen.readAlpha(GuiClient.CONFIG.fpsHudAlpha);
                break;
            }
            default: {
                v = 1.0f;
            }
        }
        if (Float.isNaN(v) || Float.isInfinite(v)) {
            v = 1.0f;
        }
        return HudEditScreen.clamp(v, 0.0f, 1.0f);
    }

    private void setFrameAlpha(HudItem item, float value) {
        float v = HudEditScreen.clamp(value, 0.0f, 1.0f);
        switch (item.ordinal()) {
            case 1: {
                GuiClient.CONFIG.keybindsHudAlpha = Float.valueOf(v);
                break;
            }
            case 3: {
                GuiClient.CONFIG.notificationsHudAlpha = Float.valueOf(v);
                break;
            }
            case 4: {
                GuiClient.CONFIG.coordinatesHudAlpha = Float.valueOf(v);
                break;
            }
            case 5: {
                GuiClient.CONFIG.armorHudAlpha = Float.valueOf(v);
                break;
            }
            case 6: {
                GuiClient.CONFIG.potionsHudAlpha = Float.valueOf(1.0f);
                break;
            }
            case 7: {
                GuiClient.CONFIG.cooldownsHudAlpha = Float.valueOf(v);
                break;
            }
            case 8: {
                GuiClient.CONFIG.inventoryHudAlpha = Float.valueOf(v);
                break;
            }
            case 9: {
                GuiClient.CONFIG.pingHudAlpha = Float.valueOf(1.0f);
                break;
            }
            case 10: {
                GuiClient.CONFIG.fpsHudAlpha = Float.valueOf(v);
                break;
            }
        }
    }

    private static float readAlpha(Float v) {
        if (v == null || Float.isNaN(v.floatValue()) || Float.isInfinite(v.floatValue())) {
            return 1.0f;
        }
        return v.floatValue();
    }

    private void setUseGuiColor(HudItem item, boolean value) {
        switch (item.ordinal()) {
            case 1: {
                GuiClient.CONFIG.keybindsUseGuiColor = value;
                break;
            }
            case 2: {
                break;
            }
            case 3: {
                GuiClient.CONFIG.notificationsUseGuiColor = value;
                break;
            }
            case 4: {
                GuiClient.CONFIG.coordinatesUseGuiColor = value;
                break;
            }
            case 5: {
                GuiClient.CONFIG.armorUseGuiColor = value;
                break;
            }
            case 6: {
                GuiClient.CONFIG.potionsUseGuiColor = value;
                break;
            }
            case 7: {
                GuiClient.CONFIG.cooldownsUseGuiColor = value;
                break;
            }
            case 8: {
                GuiClient.CONFIG.inventoryUseGuiColor = value;
                break;
            }
            case 9: {
                GuiClient.CONFIG.pingUseGuiColor = value;
                break;
            }
            case 10: {
                GuiClient.CONFIG.fpsUseGuiColor = value;
                break;
            }
        }
    }

    private float minScale(HudItem item) {
        if (item == HudItem.NOTIFICATIONS) {
            return 0.35f;
        }
        if (item == HudItem.COOLDOWNS) {
            return 0.6f;
        }
        return 0.5f;
    }

    private static boolean showTransparency(HudItem item) {
        return item != HudItem.NONE && item != HudItem.ARRAY_LIST && item != HudItem.PING && item != HudItem.POTIONS;
    }

    private float maxScale(HudItem item) {
        if (item == HudItem.NOTIFICATIONS) {
            return 1.2f;
        }
        if (item == HudItem.COOLDOWNS) {
            return 1.4f;
        }
        return 1.4f;
    }

    private void ensureSizeSlider(SettingsLayout layout) {
        if (layout == null || this.settingsItem == HudItem.NONE) {
            return;
        }
        float min = this.minScale(this.settingsItem);
        float max = this.maxScale(this.settingsItem);
        float scale = this.getScale(this.settingsItem);
        float t = (scale - min) / (max - min);
        if (Float.isNaN(t) || Float.isInfinite(t)) {
            t = 0.0f;
        }
        t = HudEditScreen.clamp(t, 0.0f, 1.0f);
        if (this.sizeSlider == null || this.sizeSliderItem != this.settingsItem) {
            this.sizeSliderItem = this.settingsItem;
            String label = HudEditScreen.sliderLabelFor(this.settingsItem);
            HudItem item = this.settingsItem;
            this.sizeSlider = new UiSlider(layout.sizeSlider.x, layout.sizeSlider.y, layout.sizeSlider.w, layout.sizeSlider.h, (Text)Text.literal((String)label), t, v -> {
                float next = min + v * (max - min);
                this.setScale(item, HudEditScreen.clamp(next, min, max));
            });
        } else {
            this.sizeSlider.setBounds(layout.sizeSlider.x, layout.sizeSlider.y, layout.sizeSlider.w, layout.sizeSlider.h);
            this.sizeSlider.value = t;
        }
    }

    private void ensureTransparencySlider(SettingsLayout layout) {
        if (layout == null || this.settingsItem == HudItem.NONE || layout.transparencySlider == null) {
            this.transparencySlider = null;
            this.transparencySliderItem = HudItem.NONE;
            return;
        }
        float alpha = HudEditScreen.getFrameAlpha(this.settingsItem);
        float t = HudEditScreen.clamp(alpha, 0.0f, 1.0f);
        if (this.transparencySlider == null || this.transparencySliderItem != this.settingsItem) {
            this.transparencySliderItem = this.settingsItem;
            String label = HudEditScreen.transparencyLabelFor(this.settingsItem);
            HudItem item = this.settingsItem;
            this.transparencySlider = new UiSlider(layout.transparencySlider.x, layout.transparencySlider.y, layout.transparencySlider.w, layout.transparencySlider.h, (Text)Text.literal((String)label), t, v -> this.setFrameAlpha(item, v));
        } else {
            this.transparencySlider.setBounds(layout.transparencySlider.x, layout.transparencySlider.y, layout.transparencySlider.w, layout.transparencySlider.h);
            this.transparencySlider.value = t;
        }
    }

    private static void drawButton(DrawContext ctx, TextRenderer tr, Rect r, String label, boolean active, boolean hover, int accent) {
        int bg = active ? Theme.withAlpha(accent, 140) : (hover ? Theme.withAlpha(accent, 90) : Theme.argb(120, 20, 20, 26));
        Rounded.rect(ctx, r.x, r.y, r.w, r.h, 5.0f, bg);
        int tx = r.x + (r.w - tr.getWidth(label)) / 2;
        int n = r.y;
        int n2 = r.h;
        Objects.requireNonNull(tr);
        int ty = n + (n2 - 9) / 2;
        ctx.drawText(tr, (Text)Text.literal((String)label), tx, ty, -1, false);
    }

    private static void drawMenuIcon(DrawContext ctx, HudItem item, int x, int y, int size, int color) {
        int s = Math.max(6, size);
        int stroke = Math.max(1, Math.round((float)s * 0.12f));
        switch (item.ordinal()) {
            case 2: {
                int h = Math.max(1, s / 5);
                int gap = Math.max(1, h + 1);
                for (int i = 0; i < 3; ++i) {
                    int yy = y + i * gap;
                    ctx.fill(x, yy, x + s, yy + h, color);
                }
                break;
            }
            case 1: {
                int h = Math.max(4, s - 2);
                Rounded.outline(ctx, x, y + 1, s, h, 3.0f, stroke, color);
                int keyW = Math.max(1, s / 4);
                int keyH = Math.max(1, h / 3);
                int ky = y + 1 + h / 2 - keyH / 2;
                int kx = x + 2;
                for (int i = 0; i < 3; ++i) {
                    Rounded.rect(ctx, kx, ky, keyW, keyH, 2.0f, color);
                    kx += keyW + 2;
                }
                break;
            }
            case 3: {
                int w = Math.max(4, Math.round((float)s * 0.75f));
                int h = Math.max(4, Math.round((float)s * 0.7f));
                int bx = x + (s - w) / 2;
                int by = y + 1;
                Rounded.outline(ctx, bx, by, w, h, (float)w * 0.5f, stroke, color);
                int dot = Math.max(2, s / 5);
                Rounded.rect(ctx, x + s / 2 - dot / 2, by + h - 1, dot, dot, (float)dot * 0.5f, color);
                break;
            }
            case 4: {
                int dot = Math.max(2, s / 4);
                int gap = Math.max(1, (s - dot * 3) / 2);
                int cy = y + s / 2 - dot / 2;
                int cx = x;
                for (int i = 0; i < 3; ++i) {
                    Rounded.rect(ctx, cx, cy, dot, dot, (float)dot * 0.5f, color);
                    cx += dot + gap;
                }
                break;
            }
            case 5: {
                int w = Math.max(5, Math.round((float)s * 0.75f));
                int h = Math.max(6, Math.round((float)s * 0.85f));
                int bx = x + (s - w) / 2;
                int by = y + (s - h) / 2;
                Rounded.outline(ctx, bx, by, w, h, 3.0f, stroke, color);
                int notchW = Math.max(2, w / 3);
                int notchX = bx + w / 2 - notchW / 2;
                ctx.fill(notchX, by + h - 2, notchX + notchW, by + h, color);
                break;
            }
            case 6: {
                int bodyW = Math.max(4, Math.round((float)s * 0.65f));
                int bodyH = Math.max(4, Math.round((float)s * 0.6f));
                int neckW = Math.max(2, Math.round((float)s * 0.3f));
                int neckH = Math.max(2, Math.round((float)s * 0.2f));
                int bx = x + (s - bodyW) / 2;
                int by = y + s - bodyH;
                int nx = x + (s - neckW) / 2;
                int ny = by - neckH + 1;
                Rounded.outline(ctx, nx, ny, neckW, neckH, 2.0f, stroke, color);
                Rounded.outline(ctx, bx, by, bodyW, bodyH, 3.0f, stroke, color);
                break;
            }
            case 7: {
                Rounded.outline(ctx, x, y, s, s, (float)s * 0.5f, stroke, color);
                int cx = x + s / 2;
                int cy = y + s / 2;
                ctx.fill(cx, y + 2, cx + stroke, cy, color);
                break;
            }
            case 8: {
                int cell = Math.max(2, s / 3);
                int gap = Math.max(1, cell / 2);
                int startX = x;
                int startY = y;
                for (int row = 0; row < 2; ++row) {
                    for (int col = 0; col < 2; ++col) {
                        int cx = startX + col * (cell + gap);
                        int cy = startY + row * (cell + gap);
                        Rounded.rect(ctx, cx, cy, cell, cell, 2.0f, color);
                    }
                }
                break;
            }
            case 9: {
                int barW = Math.max(1, s / 4);
                int gap = Math.max(1, s / 8);
                int baseY = y + s - 1;
                int h1 = Math.max(2, s / 3);
                int h2 = Math.max(3, s / 2);
                int h3 = Math.max(4, s - 2);
                ctx.fill(x, baseY - h1, x + barW, baseY, color);
                ctx.fill(x + barW + gap, baseY - h2, x + barW * 2 + gap, baseY, color);
                ctx.fill(x + barW * 2 + gap * 2, baseY - h3, x + barW * 3 + gap * 2, baseY, color);
                break;
            }
            case 10: {
                int r = Math.max(2, Math.round((float)s * 0.42f));
                int cx = x + s / 2;
                int cy = y + s / 2;
                Rounded.outline(ctx, cx - r, cy - r, r * 2, r * 2, r, stroke, color);
                int needle = Math.max(1, r / 3);
                int nx = cx + r / 2;
                int ny = cy - needle;
                ctx.fill(nx, ny, nx + stroke, ny + needle, color);
                break;
            }
        }
    }

    private static void saveConfig() {
        String active = GuiClient.CONFIG.activeConfig;
        if (active == null || active.isBlank()) {
            active = "default";
        }
        GuiClient.CONFIGS.save(active);
    }

    private void updateMenuLayout() {
        TextRenderer tr = GuiFonts.textRendererOrDefault(MinecraftClient.getInstance().textRenderer);
        int maxRowW = 0;
        HudItem[] avail = HudEditScreen.availableItems();
        if (avail.length == 0) {
            maxRowW = Math.max(maxRowW, tr.getWidth("All active"));
        }
        for (int i = 0; i < avail.length; ++i) {
            int rowW = 20 + tr.getWidth(HudEditScreen.labelFor(avail[i]));
            if (rowW <= maxRowW) continue;
            maxRowW = rowW;
        }
        this.menuW = maxRowW + 20;
        int rows = Math.max(1, avail.length);
        this.menuH = 16 + rows * 22 + Math.max(0, rows - 1) * 4;
    }

    private void clampMenuToScreen() {
        if (this.width <= 0 || this.height <= 0) {
            return;
        }
        int maxX = Math.max(6, this.width - this.menuW - 6);
        int maxY = Math.max(6, this.height - this.menuH - 6);
        this.menuX = HudEditScreen.clamp(this.menuX, 6, maxX);
        this.menuY = HudEditScreen.clamp(this.menuY, 6, maxY);
    }

    private int hitMenuItem(int mx, int my) {
        HudItem[] avail = HudEditScreen.availableItems();
        if (avail.length == 0) {
            return -1;
        }
        int y = this.menuY + 8;
        for (int i = 0; i < avail.length; ++i) {
            if (HudEditScreen.inside(mx, my, this.menuX, y, this.menuW, 22)) {
                return i;
            }
            y += 26;
        }
        return -1;
    }

    private void toggleItem(int idx) {
        HudItem[] avail = HudEditScreen.availableItems();
        if (idx >= 0 && idx < avail.length) {
            HudItem item = avail[idx];
            activeItems.add(item);
            switch (item.ordinal()) {
                case 1: {
                    GuiClient.CONFIG.showKeybindsHud = true;
                    break;
                }
                case 2: {
                    GuiClient.CONFIG.showArrayList = true;
                    break;
                }
                case 3: {
                    GuiClient.CONFIG.showNotifications = true;
                    break;
                }
                case 4: {
                    GuiClient.CONFIG.showCoordinatesHud = true;
                    break;
                }
                case 5: {
                    GuiClient.CONFIG.showArmorHud = true;
                    break;
                }
                case 6: {
                    GuiClient.CONFIG.showPotionsHud = true;
                    break;
                }
                case 7: {
                    GuiClient.CONFIG.showCooldownsHud = true;
                    break;
                }
                case 8: {
                    GuiClient.CONFIG.showInventoryHud = true;
                    break;
                }
                case 9: {
                    GuiClient.CONFIG.showPingHud = true;
                    break;
                }
                case 10: {
                    GuiClient.CONFIG.showFpsHud = true;
                    break;
                }
            }
        }
    }

    private static void syncActiveFromConfig() {
        activeItems.clear();
        if (GuiClient.CONFIG.showKeybindsHud == null || GuiClient.CONFIG.showKeybindsHud.booleanValue()) {
            activeItems.add(HudItem.KEYBINDS);
        }
        if (GuiClient.CONFIG.showArrayList == null || GuiClient.CONFIG.showArrayList.booleanValue()) {
            activeItems.add(HudItem.ARRAY_LIST);
        }
        if (GuiClient.CONFIG.showNotifications == null || GuiClient.CONFIG.showNotifications.booleanValue()) {
            activeItems.add(HudItem.NOTIFICATIONS);
        }
        if (Boolean.TRUE.equals(GuiClient.CONFIG.showCoordinatesHud)) {
            activeItems.add(HudItem.COORDINATES);
        }
        if (Boolean.TRUE.equals(GuiClient.CONFIG.showArmorHud)) {
            activeItems.add(HudItem.ARMOR);
        }
        if (Boolean.TRUE.equals(GuiClient.CONFIG.showPotionsHud)) {
            activeItems.add(HudItem.POTIONS);
        }
        if (Boolean.TRUE.equals(GuiClient.CONFIG.showCooldownsHud)) {
            activeItems.add(HudItem.COOLDOWNS);
        }
        if (Boolean.TRUE.equals(GuiClient.CONFIG.showInventoryHud)) {
            activeItems.add(HudItem.INVENTORY);
        }
        if (Boolean.TRUE.equals(GuiClient.CONFIG.showPingHud)) {
            activeItems.add(HudItem.PING);
        }
        if (Boolean.TRUE.equals(GuiClient.CONFIG.showFpsHud)) {
            activeItems.add(HudItem.FPS);
        }
    }

    private static HudItem[] availableItems() {
        ArrayList<HudItem> items = new ArrayList<HudItem>();
        for (HudItem item : ITEMS) {
            if (activeItems.contains((Object)item)) continue;
            items.add(item);
        }
        return items.toArray(new HudItem[0]);
    }

    private static String labelFor(HudItem item) {
        switch (item.ordinal()) {
            case 4: {
                return ITEM_LABELS[0];
            }
            case 5: {
                return ITEM_LABELS[1];
            }
            case 6: {
                return ITEM_LABELS[2];
            }
            case 7: {
                return ITEM_LABELS[3];
            }
            case 8: {
                return ITEM_LABELS[4];
            }
            case 9: {
                return ITEM_LABELS[5];
            }
            case 10: {
                return ITEM_LABELS[6];
            }
            case 1: {
                return ITEM_LABELS[7];
            }
            case 2: {
                return ITEM_LABELS[8];
            }
            case 3: {
                return ITEM_LABELS[9];
            }
        }
        return "";
    }

    private static String sliderLabelFor(HudItem item) {
        return "Size";
    }

    private static String transparencyLabelFor(HudItem item) {
        return "Transparency";
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

    public static enum HudItem {
        NONE,
        KEYBINDS,
        ARRAY_LIST,
        NOTIFICATIONS,
        COORDINATES,
        ARMOR,
        POTIONS,
        COOLDOWNS,
        INVENTORY,
        PING,
        FPS;

    }

    private record Bound(int x, int y, int w, int h, long atMs) {
    }

    private record SettingsLayout(int x, int y, int w, int h, int titleX, int titleY, Rect trash, Rect sizeSlider, Rect transparencySlider, Rect guiColor, Rect modeAll, Rect modeOnly, Rect modeAll2, Rect modeOnly2) {
    }

    private record Rect(int x, int y, int w, int h) {
    }
}

