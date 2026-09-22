package me.Gui.gui.ui.hud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import me.Gui.gui.client.GuiClient;
import me.Gui.gui.mixin.ItemCooldownManagerAccessor;
import me.Gui.gui.mixin.ItemCooldownManagerCooldownAccessor;
import me.Gui.gui.ui.GuiFonts;
import me.Gui.gui.ui.HudEditScreen;
import me.Gui.gui.ui.Theme;
import me.Gui.gui.ui.render.HudGlass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class CooldownListHud {
    private static final long READY_SHOW_MS = 3000L;
    private static final int ICON_SIZE = 16;
    private static boolean dragging = false;
    private static boolean lastDown = false;
    private static double dragOffX;
    private static double dragOffY;
    private static final Set<Item> lastActive;
    private static final Map<Item, Long> readyAt;

    private CooldownListHud() {
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            CooldownListHud.resetState();
            return;
        }
        boolean editActive = HudEditScreen.isActiveFor(HudEditScreen.HudItem.COOLDOWNS);
        if (HudEditScreen.isActive() && !editActive) {
            CooldownListHud.resetState();
            return;
        }
        if (!editActive && !Boolean.TRUE.equals(GuiClient.CONFIG.showCooldownsHud)) {
            CooldownListHud.resetState();
            return;
        }
        ItemCooldownManager manager = client.player.getItemCooldownManager();
        if (manager == null) {
            CooldownListHud.resetState();
            return;
        }
        Map<Object, Object> map = ((ItemCooldownManagerAccessor)manager).getEntries();
        if (map == null) {
            CooldownListHud.resetState();
            return;
        }
        int nowTick = ((ItemCooldownManagerAccessor)manager).getTick();
        long nowMs = System.currentTimeMillis();
        ArrayList<Row> rows = new ArrayList<Row>();
        HashSet<Item> active = new HashSet<Item>();
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object cd;
            Item item = CooldownListHud.resolveItem(entry.getKey());
            if (item == null || item == Items.AIR || item == Items.ENDER_PEARL || (cd = entry.getValue()) == null) continue;
            int start = ((ItemCooldownManagerCooldownAccessor)cd).getStartTick();
            int end = ((ItemCooldownManagerCooldownAccessor)cd).getEndTick();
            int duration = end - start;
            if (duration <= 0) continue;
            int remainingTicks = Math.max(0, end - nowTick);
            float remainingFrac = CooldownListHud.clamp((float)remainingTicks / (float)duration, 0.0f, 1.0f);
            active.add(item);
            readyAt.remove(item);
            rows.add(CooldownListHud.buildRow(CooldownListHud.resolveStack(client, item), remainingTicks, remainingFrac, false));
        }
        for (Item item : lastActive) {
            if (active.contains(item) || item == Items.ENDER_PEARL) continue;
            readyAt.put(item, nowMs);
        }
        lastActive.clear();
        lastActive.addAll(active);
        if (!readyAt.isEmpty()) {
            ArrayList<Item> toRemove = new ArrayList<Item>();
            for (Map.Entry<Item, Long> entry : readyAt.entrySet()) {
                long t = entry.getValue();
                if (nowMs - t > 3000L) {
                    toRemove.add(entry.getKey());
                    continue;
                }
                Item item = entry.getKey();
                if (item == null || item == Items.AIR || item == Items.ENDER_PEARL) {
                    toRemove.add(item);
                    continue;
                }
                rows.add(CooldownListHud.buildRow(CooldownListHud.resolveStack(client, item), 0, 0.0f, true));
            }
            for (Item item : toRemove) {
                readyAt.remove(item);
            }
        }
        if (rows.isEmpty() && editActive) {
            rows.add(CooldownListHud.buildRow(new ItemStack((ItemConvertible)Items.GOLDEN_APPLE), 40, 0.5f, false));
            rows.add(CooldownListHud.buildRow(new ItemStack((ItemConvertible)Items.TOTEM_OF_UNDYING), 0, 0.0f, true));
        }
        if (rows.isEmpty()) {
            return;
        }
        rows.sort((a, b) -> {
            if (a.ready && !b.ready) {
                return 1;
            }
            if (!a.ready && b.ready) {
                return -1;
            }
            int cmp = Integer.compare(b.remainingTicks, a.remainingTicks);
            if (cmp != 0) {
                return cmp;
            }
            return a.name.compareToIgnoreCase(b.name);
        });
        TextRenderer tr = GuiFonts.textRendererOrDefault(client.textRenderer);
        int n = 5;
        int n2 = 4;
        int rowGap = 2;
        int iconGap = 6;
        Objects.requireNonNull(tr);
        int rowH = Math.max(16, 9) + 4;
        int maxNameW = 0;
        int maxCdW = 0;
        for (Row r : rows) {
            maxNameW = Math.max(maxNameW, tr.getWidth(r.name));
            maxCdW = Math.max(maxCdW, tr.getWidth(r.cooldownText));
        }
        int rawW = n + 16 + iconGap + maxNameW + iconGap + maxCdW + n;
        int rowCount = rows.isEmpty() ? 1 : rows.size();
        int rawH = n2 * 2 + rowCount * rowH + Math.max(0, rowCount - 1) * rowGap;
        float scale = GuiClient.CONFIG.cooldownListScale;
        if (Float.isNaN(scale) || Float.isInfinite(scale)) {
            scale = 0.9f;
        }
        scale = CooldownListHud.clamp(scale, 0.6f, 1.4f);
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        int boxW = Math.round((float)rawW * scale);
        int boxH = Math.round((float)rawH * scale);
        int x = GuiClient.CONFIG.cooldownListX;
        int y = GuiClient.CONFIG.cooldownListY;
        int maxX = Math.max(4, sw - boxW - 4);
        int maxY = Math.max(4, sh - boxH - 4);
        x = CooldownListHud.clamp(x, 4, maxX);
        y = CooldownListHud.clamp(y, 4, maxY);
        GuiClient.CONFIG.cooldownListX = x;
        GuiClient.CONFIG.cooldownListY = y;
        if (editActive) {
            Window win = client.getWindow();
            long handle = win.getHandle();
            if (handle != 0L) {
                boolean down;
                int mx = CooldownListHud.scaledMouseX(client);
                int my = CooldownListHud.scaledMouseY(client);
                boolean bl = down = GLFW.glfwGetMouseButton((long)handle, (int)0) == 1;
                if (down && !lastDown && CooldownListHud.inside(mx, my, x, y, boxW, boxH)) {
                    dragging = true;
                    dragOffX = mx - x;
                    dragOffY = my - y;
                }
                if (!down && lastDown && dragging) {
                    dragging = false;
                    CooldownListHud.savePosition();
                }
                if (dragging) {
                    x = CooldownListHud.clamp((int)((double)mx - dragOffX), 4, maxX);
                    y = CooldownListHud.clamp((int)((double)my - dragOffY), 4, maxY);
                    GuiClient.CONFIG.cooldownListX = x;
                    GuiClient.CONFIG.cooldownListY = y;
                }
                lastDown = down;
            }
        } else {
            dragging = false;
            lastDown = false;
        }
        if (editActive) {
            HudEditScreen.setBounds(HudEditScreen.HudItem.COOLDOWNS, x, y, boxW, boxH);
        } else {
            HudEditScreen.clearBounds(HudEditScreen.HudItem.COOLDOWNS);
        }
        float r = Math.min(8.0f, (float)rawH * 0.5f);
        int accent = Theme.accentColor(System.currentTimeMillis());
        boolean useGuiColor = Boolean.TRUE.equals(GuiClient.CONFIG.cooldownsUseGuiColor);
        float frameAlpha = HudEditScreen.getFrameAlpha(HudEditScreen.HudItem.COOLDOWNS);
        int textColor = Theme.withAlpha(accent, 255);
        ctx.getMatrices().push();
        ctx.getMatrices().translate((float)x, (float)y, 0.0f);
        ctx.getMatrices().scale(scale, scale, 1.0f);
        HudGlass.panelNoBorder(ctx, 0, 0, rawW, rawH, r, accent, frameAlpha, useGuiColor);
        int yy = n2;
        for (Row rRow : rows) {
            int iconX = n;
            int iconY = yy + (rowH - 16) / 2;
            if (!rRow.stack.isEmpty()) {
                ctx.drawItemWithoutEntity(rRow.stack, iconX, iconY);
            }
            int nameX = iconX + 16 + iconGap;
            Objects.requireNonNull(tr);
            int textY = yy + (rowH - 9) / 2;
            ctx.drawText(tr, rRow.name, nameX, textY, textColor, false);
            int cdX = nameX + maxNameW + iconGap;
            ctx.drawText(tr, rRow.cooldownText, cdX, textY, textColor, false);
            yy += rowH + rowGap;
        }
        ctx.getMatrices().pop();
    }

    private static Row buildRow(ItemStack stack, int remainingTicks, float remainingFrac, boolean ready) {
        Item item;
        if (stack == null) {
            stack = ItemStack.EMPTY;
        }
        Item item2 = item = stack.isEmpty() ? Items.AIR : stack.getItem();
        if (stack.isEmpty() && item != Items.AIR) {
            stack = new ItemStack((ItemConvertible)item);
        } else if (!stack.isEmpty()) {
            stack = stack.copy();
        }
        String name = stack.getName().getString();
        String cdText = CooldownListHud.formatTime(remainingTicks);
        int color = ready ? -11151510 : (remainingFrac <= 0.2f ? -11702 : -1);
        return new Row(item, stack, name, cdText, remainingTicks, remainingFrac, ready, color);
    }

    private static Item resolveItem(Object key) {
        if (key instanceof Item) {
            Item item = (Item)key;
            return item;
        }
        if (key instanceof Identifier) {
            Identifier id = (Identifier)key;
            return (Item)Registries.ITEM.get(id);
        }
        return null;
    }

    private static ItemStack resolveStack(MinecraftClient client, Item item) {
        if (client == null || client.player == null || item == null || item == Items.AIR) {
            return new ItemStack((ItemConvertible)(item == null ? Items.AIR : item));
        }
        ItemStack main = client.player.getMainHandStack();
        ItemStack off = client.player.getOffHandStack();
        ItemStack preferred = ItemStack.EMPTY;
        if (CooldownListHud.matchesItem(main, item)) {
            preferred = main;
            if (CooldownListHud.hasCustomDisplayName(main) || CooldownListHud.hasCustomModelData(main)) {
                return main;
            }
        }
        if (CooldownListHud.matchesItem(off, item)) {
            if (CooldownListHud.hasCustomDisplayName(off) || CooldownListHud.hasCustomModelData(off)) {
                return off;
            }
            if (preferred.isEmpty()) {
                preferred = off;
            }
        }
        PlayerInventory inv = client.player.getInventory();
        ItemStack first = ItemStack.EMPTY;
        if (inv != null) {
            int size = inv.size();
            for (int i = 0; i < size; ++i) {
                ItemStack stack = inv.getStack(i);
                if (!CooldownListHud.matchesItem(stack, item)) continue;
                if (first.isEmpty()) {
                    first = stack;
                }
                if (!CooldownListHud.hasCustomDisplayName(stack) && !CooldownListHud.hasCustomModelData(stack)) continue;
                return stack;
            }
        }
        if (!preferred.isEmpty()) {
            return preferred;
        }
        if (!first.isEmpty()) {
            return first;
        }
        return new ItemStack((ItemConvertible)item);
    }

    private static boolean matchesItem(ItemStack stack, Item item) {
        if (stack == null || stack.isEmpty() || item == null) {
            return false;
        }
        return stack.getItem() == item;
    }

    private static boolean hasCustomModelData(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.get(DataComponentTypes.CUSTOM_MODEL_DATA) != null;
    }

    private static boolean hasCustomDisplayName(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.get(DataComponentTypes.CUSTOM_NAME) != null;
    }

    private static String formatTime(int ticks) {
        if (ticks <= 0) {
            return "0s";
        }
        float sec = (float)ticks / 20.0f;
        if (sec >= 60.0f) {
            int m = (int)(sec / 60.0f);
            int s = Math.round(sec - (float)m * 60.0f);
            if (s >= 60) {
                ++m;
                s = 0;
            }
            return m + "m " + s + "s";
        }
        if (sec >= 10.0f) {
            return String.format(Locale.US, "%.0fs", Float.valueOf(sec));
        }
        return String.format(Locale.US, "%.1fs", Float.valueOf(sec));
    }

    private static void resetState() {
        dragging = false;
        lastDown = false;
        lastActive.clear();
        readyAt.clear();
    }

    private static void savePosition() {
        String active = GuiClient.CONFIG.activeConfig;
        if (active == null || active.isBlank()) {
            active = "default";
        }
        GuiClient.CONFIGS.save(active);
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

    static {
        lastActive = new HashSet<Item>();
        readyAt = new HashMap<Item, Long>();
    }

    private record Row(Item item, ItemStack stack, String name, String cooldownText, int remainingTicks, float remainingFrac, boolean ready, int color) {
    }
}

