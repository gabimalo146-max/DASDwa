package me.Gui.gui.ui.hud;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import me.Gui.gui.client.GuiClient;
import me.Gui.gui.config.GuiConfig;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import me.Gui.gui.ui.GuiFonts;
import me.Gui.gui.ui.HudEditScreen;
import me.Gui.gui.ui.Theme;
import me.Gui.gui.ui.render.Rounded;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public final class SwordInfoHud {
    private static boolean dragging = false;
    private static boolean lastDown = false;
    private static double dragOffX;
    private static double dragOffY;
    private static boolean draggingAll;
    private static boolean lastDownAll;
    private static double dragAllOffX;
    private static double dragAllOffY;
    private static final String EXTRA_KEYWORD = "dodatkowe";
    private static final int CACHE_LIMIT = 128;
    private static final int ALL_MAX_ROWS = 10;
    private static final Map<UUID, CachedSword> LAST_SWORD;

    private SwordInfoHud() {
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        boolean editOpen;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null) {
            LAST_SWORD.clear();
            return;
        }
        HackModule mod = GuiClient.MODULES.byId(ModuleId.SWORD_INFO);
        if (mod == null || !mod.isEnabled()) {
            return;
        }
        if (HudEditScreen.isActive()) {
            return;
        }
        GuiConfig cfg = GuiClient.CONFIG;
        boolean dragOpen = editOpen = HudEditScreen.isActive();
        boolean showAll = cfg.swordInfoShowAllPlayers != null && cfg.swordInfoShowAllPlayers != false;
        MutableText extraLine = null;
        if (dragOpen) {
            extraLine = Text.literal((String)"Przykladowe Obrazenia");
        } else {
            CachedSword cached;
            PlayerEntity target;
            EntityHitResult ehr;
            Entity entity;
            HitResult hit = client.crosshairTarget;
            if (hit instanceof EntityHitResult && (entity = (ehr = (EntityHitResult)hit).getEntity()) instanceof PlayerEntity && (target = (PlayerEntity)entity) != client.player && (cached = SwordInfoHud.updateCachedSword(target)) != null && cached.extraLine != null && !cached.extraLine.isBlank()) {
                extraLine = Text.literal((String)cached.extraLine);
            }
        }
        if (extraLine == null && !showAll) {
            return;
        }
        if (extraLine != null) {
            SwordInfoHud.renderSingle(ctx, client, cfg, dragOpen, (Text)extraLine);
        }
        if (showAll) {
            SwordInfoHud.renderAllPlayers(ctx, client, cfg, dragOpen);
        }
    }

    private static void renderSingle(DrawContext ctx, MinecraftClient client, GuiConfig cfg, boolean dragOpen, Text extraLine) {
        ArrayList<Text> lines = new ArrayList<Text>();
        lines.add(extraLine);
        float scale = cfg.swordInfoScale;
        if (Float.isNaN(scale) || Float.isInfinite(scale)) {
            scale = 0.9f;
        }
        if (scale < 0.6f) {
            scale = 0.6f;
        }
        if (scale > 1.4f) {
            scale = 1.4f;
        }
        TextRenderer tr = GuiFonts.textRendererOrDefault(client.textRenderer);
        Objects.requireNonNull(tr);
        int lineH = 9 + 2;
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        int maxW = 0;
        for (Text s : lines) {
            int w = tr.getWidth((StringVisitable)s);
            if (w <= maxW) continue;
            maxW = w;
        }
        boolean frame = cfg.swordInfoFrame == null || cfg.swordInfoFrame != false;
        int padX = frame ? 4 : 0;
        int padY = frame ? 2 : 0;
        int rawW = maxW + padX * 2;
        int rawH = lineH * lines.size() + padY * 2;
        int boxW = Math.round((float)rawW * scale);
        int boxH = Math.round((float)rawH * scale);
        int defX = sw / 2 - 91;
        int defY = sh - 54 - boxH;
        int x = cfg.swordInfoX;
        int y = cfg.swordInfoY;
        if (x < 0) {
            x = defX;
        }
        if (y < 0) {
            y = defY;
        }
        int pad = 4;
        int maxX = Math.max(pad, sw - boxW - pad);
        int maxY = Math.max(pad, sh - boxH - pad);
        x = SwordInfoHud.clamp(x, pad, maxX);
        y = SwordInfoHud.clamp(y, pad, maxY);
        if (cfg.swordInfoX < 0 || cfg.swordInfoY < 0) {
            cfg.swordInfoX = x;
            cfg.swordInfoY = y;
        }
        if (dragOpen) {
            long handle = client.getWindow().getHandle();
            if (handle != 0L) {
                boolean down;
                int mx = SwordInfoHud.scaledMouseX(client);
                int my = SwordInfoHud.scaledMouseY(client);
                boolean bl = down = GLFW.glfwGetMouseButton((long)handle, (int)0) == 1;
                if (down && !lastDown && SwordInfoHud.inside(mx, my, x, y, boxW, boxH)) {
                    dragging = true;
                    dragOffX = mx - x;
                    dragOffY = my - y;
                }
                if (!down && lastDown && dragging) {
                    dragging = false;
                    SwordInfoHud.savePosition();
                }
                if (dragging) {
                    x = SwordInfoHud.clamp((int)((double)mx - dragOffX), pad, maxX);
                    y = SwordInfoHud.clamp((int)((double)my - dragOffY), pad, maxY);
                    cfg.swordInfoX = x;
                    cfg.swordInfoY = y;
                }
                lastDown = down;
            }
        } else {
            dragging = false;
            lastDown = false;
        }
        int baseColor = cfg.swordInfoExtraColor == null ? 5611775 : cfg.swordInfoExtraColor;
        int col = 0xFF000000 | baseColor & 0xFFFFFF;
        ctx.getMatrices().push();
        ctx.getMatrices().translate((float)x, (float)y, 0.0f);
        ctx.getMatrices().scale(scale, scale, 1.0f);
        if (frame) {
            int bg = Theme.argb(120, 16, 16, 20);
            float r = Math.min(6.0f, (float)rawH * 0.5f);
            Rounded.rect(ctx, 0, 0, rawW, rawH, r, bg);
        }
        int textY = padY;
        for (Text t : lines) {
            ctx.drawText(tr, t, padX, textY, col, false);
            textY += lineH;
        }
        ctx.getMatrices().pop();
    }

    private static void renderAllPlayers(DrawContext ctx, MinecraftClient client, GuiConfig cfg, boolean dragOpen) {
        if (client.world == null || client.player == null) {
            return;
        }
        List<Row> rows = SwordInfoHud.collectRows(client);
        if (rows.isEmpty()) {
            return;
        }
        TextRenderer tr = GuiFonts.textRendererOrDefault(client.textRenderer);
        int armorSize = 16;
        int swordSize = 16;
        int iconGap = 4;
        int armorGap = 2;
        int rowGap = 2;
        int padX = 5;
        int padY = 4;
        Objects.requireNonNull(tr);
        int rowH = Math.max(armorSize, 9) + 4;
        int maxNameW = 0;
        int maxPctW = 0;
        for (Row r : rows) {
            int pw;
            int w = tr.getWidth(r.nameText);
            if (w > maxNameW) {
                maxNameW = w;
            }
            if ((pw = tr.getWidth(r.percentText)) <= maxPctW) continue;
            maxPctW = pw;
        }
        int armorW = armorSize * 4 + armorGap * 3;
        int rawW = padX + armorW + iconGap + maxNameW + iconGap + swordSize + iconGap + maxPctW + padX;
        int rawH = padY * 2 + rows.size() * rowH + Math.max(0, rows.size() - 1) * rowGap;
        float scale = cfg.swordInfoAllScale;
        if (Float.isNaN(scale) || Float.isInfinite(scale)) {
            scale = 0.8f;
        }
        scale = SwordInfoHud.clamp(scale, 0.6f, 1.2f);
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        int boxW = Math.round((float)rawW * scale);
        int boxH = Math.round((float)rawH * scale);
        int defX = sw - boxW - 12;
        int defY = sh - boxH - 12;
        int x = cfg.swordInfoAllX;
        int y = cfg.swordInfoAllY;
        if (x < 0) {
            x = defX;
        }
        if (y < 0) {
            y = defY;
        }
        int pad = 4;
        int maxX = Math.max(pad, sw - boxW - pad);
        int maxY = Math.max(pad, sh - boxH - pad);
        x = SwordInfoHud.clamp(x, pad, maxX);
        y = SwordInfoHud.clamp(y, pad, maxY);
        if (cfg.swordInfoAllX < 0 || cfg.swordInfoAllY < 0) {
            cfg.swordInfoAllX = x;
            cfg.swordInfoAllY = y;
        }
        if (dragOpen) {
            long handle = client.getWindow().getHandle();
            if (handle != 0L) {
                boolean down;
                int mx = SwordInfoHud.scaledMouseX(client);
                int my = SwordInfoHud.scaledMouseY(client);
                boolean bl = down = GLFW.glfwGetMouseButton((long)handle, (int)0) == 1;
                if (down && !lastDownAll && SwordInfoHud.inside(mx, my, x, y, boxW, boxH)) {
                    draggingAll = true;
                    dragAllOffX = mx - x;
                    dragAllOffY = my - y;
                }
                if (!down && lastDownAll && draggingAll) {
                    draggingAll = false;
                    SwordInfoHud.savePosition();
                }
                if (draggingAll) {
                    x = SwordInfoHud.clamp((int)((double)mx - dragAllOffX), pad, maxX);
                    y = SwordInfoHud.clamp((int)((double)my - dragAllOffY), pad, maxY);
                    cfg.swordInfoAllX = x;
                    cfg.swordInfoAllY = y;
                }
                lastDownAll = down;
            }
        } else {
            draggingAll = false;
            lastDownAll = false;
        }
        int accent = Theme.accentColor(System.currentTimeMillis());
        int pctColor = Theme.withAlpha(accent, 255);
        int textColor = -1;
        ctx.getMatrices().push();
        ctx.getMatrices().translate((float)x, (float)y, 0.0f);
        ctx.getMatrices().scale(scale, scale, 1.0f);
        int bg = Theme.argb(130, 16, 16, 20);
        float r = Math.min(6.0f, (float)rawH * 0.5f);
        Rounded.rect(ctx, 0, 0, rawW, rawH, r, bg);
        int rowY = padY;
        for (Row rRow : rows) {
            int armorX = padX;
            int armorY = rowY + (rowH - armorSize) / 2;
            SwordInfoHud.drawArmorRow(ctx, armorX, armorY, armorSize, armorGap, rRow.helmet, rRow.chest, rRow.legs, rRow.boots);
            int textX = armorX + armorW + iconGap;
            Objects.requireNonNull(tr);
            int textY = rowY + (rowH - 9) / 2;
            ctx.drawText(tr, (Text)Text.literal((String)rRow.nameText), textX, textY, textColor, false);
            int swordX = textX + maxNameW + iconGap;
            int swordY = rowY + (rowH - swordSize) / 2;
            if (rRow.swordStack != null && !rRow.swordStack.isEmpty()) {
                ctx.drawItemWithoutEntity(rRow.swordStack, swordX, swordY);
            }
            int pctX = swordX + swordSize + iconGap;
            ctx.drawText(tr, (Text)Text.literal((String)rRow.percentText), pctX, textY, pctColor, false);
            rowY += rowH + rowGap;
        }
        ctx.getMatrices().pop();
    }

    private static boolean isSword(ItemStack stack) {
        if (stack.isIn(ItemTags.SWORDS)) {
            return true;
        }
        return stack.getItem() instanceof SwordItem;
    }

    private static List<Row> collectRows(MinecraftClient client) {
        ArrayList<Row> rows = new ArrayList<Row>();
        ClientPlayerEntity self = client.player;
        if (client.world == null || self == null) {
            return rows;
        }
        ArrayList<PlayerEntity> players = new ArrayList<PlayerEntity>(client.world.getPlayers());
        players.removeIf(arg_0 -> SwordInfoHud.lambda$collectRows$0((PlayerEntity)self, arg_0));
        players.sort((a, b) -> {
            String bn;
            String an = SwordInfoHud.resolveSortName(a);
            int cmp = an.compareToIgnoreCase(bn = SwordInfoHud.resolveSortName(b));
            if (cmp != 0) {
                return cmp;
            }
            return a.getUuid().compareTo(b.getUuid());
        });
        ArrayList<PlayerEntity> armored = new ArrayList<PlayerEntity>(players.size());
        for (PlayerEntity p : players) {
            ItemStack helmet = p.getEquippedStack(EquipmentSlot.HEAD);
            ItemStack chest = p.getEquippedStack(EquipmentSlot.CHEST);
            ItemStack legs = p.getEquippedStack(EquipmentSlot.LEGS);
            ItemStack boots = p.getEquippedStack(EquipmentSlot.FEET);
            if (!(helmet != null && !helmet.isEmpty() || chest != null && !chest.isEmpty() || legs != null && !legs.isEmpty() || boots != null && !boots.isEmpty())) continue;
            armored.add(p);
        }
        int limit = Math.min(10, armored.size());
        for (int i = 0; i < limit; ++i) {
            PlayerEntity p = (PlayerEntity)armored.get(i);
            ItemStack helmet = p.getEquippedStack(EquipmentSlot.HEAD);
            ItemStack chest = p.getEquippedStack(EquipmentSlot.CHEST);
            ItemStack legs = p.getEquippedStack(EquipmentSlot.LEGS);
            ItemStack boots = p.getEquippedStack(EquipmentSlot.FEET);
            CachedSword cached = SwordInfoHud.updateCachedSword(p);
            ItemStack sword = cached != null ? cached.stack : ItemStack.EMPTY;
            String pct = cached != null && cached.extraValue != null && !cached.extraValue.isBlank() ? cached.extraValue : "-";
            String name = SwordInfoHud.resolveDisplayName(p);
            rows.add(new Row(p, name, pct, sword, helmet, chest, legs, boots));
        }
        return rows;
    }

    private static String resolveDisplayName(PlayerEntity player) {
        if (player == null) {
            return "?";
        }
        String base = player.getName().getString();
        String display = GuiClient.FRIENDS.getDisplayName(base);
        String use = display != null && !display.isBlank() ? display : base;
        return SwordInfoHud.shorten(use, 12);
    }

    private static String resolveSortName(PlayerEntity player) {
        if (player == null) {
            return "";
        }
        String base = player.getName().getString();
        String display = GuiClient.FRIENDS.getDisplayName(base);
        String use = display != null && !display.isBlank() ? display : base;
        return use == null ? "" : use;
    }

    private static void drawArmorRow(DrawContext ctx, int x, int y, int size, int gap, ItemStack helmet, ItemStack chest, ItemStack legs, ItemStack boots) {
        int xx = x;
        if (helmet != null && !helmet.isEmpty()) {
            ctx.drawItemWithoutEntity(helmet, xx, y);
        }
        xx += size + gap;
        if (chest != null && !chest.isEmpty()) {
            ctx.drawItemWithoutEntity(chest, xx, y);
        }
        xx += size + gap;
        if (legs != null && !legs.isEmpty()) {
            ctx.drawItemWithoutEntity(legs, xx, y);
        }
        xx += size + gap;
        if (boots != null && !boots.isEmpty()) {
            ctx.drawItemWithoutEntity(boots, xx, y);
        }
    }

    private static CachedSword updateCachedSword(PlayerEntity player) {
        String value;
        String extra;
        if (player == null) {
            return null;
        }
        ItemStack main = player.getMainHandStack();
        ItemStack off = player.getOffHandStack();
        ItemStack stack = ItemStack.EMPTY;
        if (main != null && !main.isEmpty() && SwordInfoHud.isSword(main)) {
            stack = main;
        } else if (off != null && !off.isEmpty() && SwordInfoHud.isSword(off)) {
            stack = off;
        }
        if (!(stack.isEmpty() || SwordInfoHud.hasKnockback2OrMore(stack) || (extra = SwordInfoHud.findExtraDamageString(stack)) == null || extra.isBlank() || (value = SwordInfoHud.extractExtraValue(extra)) == null || value.isBlank())) {
            LAST_SWORD.put(player.getUuid(), new CachedSword(stack.copy(), extra, value));
        }
        return LAST_SWORD.get(player.getUuid());
    }

    private static String shorten(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        String t = text.trim();
        if (t.length() <= maxLen) {
            return t;
        }
        return t.substring(0, maxLen);
    }

    private static String findExtraDamageString(ItemStack stack) {
        LoreComponent lore = (LoreComponent)stack.get(DataComponentTypes.LORE);
        if (lore == null) {
            return null;
        }
        for (Text line : lore.lines()) {
            String text;
            if (line == null || (text = line.getString()) == null || text.isBlank() || !SwordInfoHud.matchesExtraDamage(text)) continue;
            return text;
        }
        return null;
    }

    private static String extractExtraValue(String line) {
        if (line == null) {
            return null;
        }
        String best = null;
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (c >= '0' && c <= '9') {
                char cc;
                int end;
                int start = i;
                for (end = i + 1; end < line.length() && ((cc = line.charAt(end)) >= '0' && cc <= '9' || cc == '.' || cc == ','); ++end) {
                }
                boolean pct = end < line.length() && line.charAt(end) == '%';
                int finalEnd = pct ? end + 1 : end;
                String val = line.substring(start, finalEnd).trim();
                if (!val.isEmpty()) {
                    best = val.replace(',', '.');
                }
                i = end + 1;
                continue;
            }
            ++i;
        }
        return best;
    }

    private static boolean matchesExtraDamage(String raw) {
        String normalized = SwordInfoHud.normalizeAscii(raw).toLowerCase(Locale.ROOT);
        if (!normalized.contains(EXTRA_KEYWORD)) {
            return false;
        }
        return normalized.contains("obrazen") || normalized.contains("obrazenia");
    }

    private static String normalizeAscii(String s) {
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

    private static boolean hasKnockback2OrMore(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        ItemEnchantmentsComponent ench = EnchantmentHelper.getEnchantments((ItemStack)stack);
        if (ench == null) {
            return false;
        }
        for (RegistryEntry<Enchantment> entry : ench.getEnchantments()) {
            String key = entry.getKey().map(registryKey -> registryKey.getValue().getPath()).orElse("");
            if (!"knockback".equals(key)) continue;
            return ench.getLevel(entry) >= 2;
        }
        return false;
    }

    private static int scaledMouseX(MinecraftClient client) {
        Window win = client.getWindow();
        return (int)(client.mouse.getX() * (double)win.getScaledWidth() / (double)win.getWidth());
    }

    private static int scaledMouseY(MinecraftClient client) {
        Window win = client.getWindow();
        return (int)(client.mouse.getY() * (double)win.getScaledHeight() / (double)win.getHeight());
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

    private static void savePosition() {
        String active = GuiClient.CONFIG.activeConfig;
        if (active == null || active.isBlank()) {
            active = "default";
        }
        GuiClient.CONFIGS.save(active);
    }

    private static /* synthetic */ boolean lambda$collectRows$0(PlayerEntity self, PlayerEntity p) {
        return p == null || p == self;
    }

    static {
        draggingAll = false;
        lastDownAll = false;
        LAST_SWORD = new LinkedHashMap<UUID, CachedSword>(64, 0.75f, true){

            @Override
            protected boolean removeEldestEntry(Map.Entry<UUID, CachedSword> eldest) {
                return this.size() > 128;
            }
        };
    }

    private record CachedSword(ItemStack stack, String extraLine, String extraValue) {
    }

    private record Row(PlayerEntity player, String nameText, String percentText, ItemStack swordStack, ItemStack helmet, ItemStack chest, ItemStack legs, ItemStack boots) {
    }
}

