package me.Gui.gui.ui.hud;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.ui.HudEditScreen;
import me.Gui.gui.ui.Theme;
import me.Gui.gui.ui.render.Rounded;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;

public final class ErrorKillerHud {
    private static boolean dragging = false;
    private static boolean lastDown = false;
    private static double dragOffX;
    private static double dragOffY;

    private ErrorKillerHud() {
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        Boolean showObs;
        boolean showObsRow;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }
        if (HudEditScreen.isActive()) {
            return;
        }
        Boolean showCob = GuiClient.CONFIG.errorKillerShowMissing;
        boolean showCobRow = showCob != null && showCob != false;
        int rows = (showCobRow ? 1 : 0) + ((showObsRow = (showObs = GuiClient.CONFIG.errorKillerShowMissingObsidian) != null && showObs != false) ? 1 : 0);
        if (rows == 0) {
            return;
        }
        int pad = 6;
        int slot = 20;
        int gap = 10;
        int rowGap = 6;
        int w = pad * 2 + slot * 2 + gap;
        int h = pad * 2 + slot * rows + rowGap * Math.max(0, rows - 1);
        int x = GuiClient.CONFIG.errorKillerHudX;
        int y = GuiClient.CONFIG.errorKillerHudY;
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        x = ErrorKillerHud.clamp(x, 4, Math.max(4, sw - w - 4));
        y = ErrorKillerHud.clamp(y, 4, Math.max(4, sh - h - 4));
        GuiClient.CONFIG.errorKillerHudX = x;
        GuiClient.CONFIG.errorKillerHudY = y;
        boolean editOpen = HudEditScreen.isActive();
        if (editOpen) {
            Window win = client.getWindow();
            long handle = win.getHandle();
            if (handle != 0L) {
                boolean down;
                int mx = ErrorKillerHud.scaledMouseX(client);
                int my = ErrorKillerHud.scaledMouseY(client);
                boolean bl = down = GLFW.glfwGetMouseButton((long)handle, (int)0) == 1;
                if (down && !lastDown && ErrorKillerHud.inside(mx, my, x, y, w, h)) {
                    dragging = true;
                    dragOffX = mx - x;
                    dragOffY = my - y;
                }
                if (!down && lastDown && dragging) {
                    dragging = false;
                    ErrorKillerHud.savePosition();
                }
                if (dragging) {
                    x = ErrorKillerHud.clamp((int)((double)mx - dragOffX), 4, Math.max(4, sw - w - 4));
                    y = ErrorKillerHud.clamp((int)((double)my - dragOffY), 4, Math.max(4, sh - h - 4));
                    GuiClient.CONFIG.errorKillerHudX = x;
                    GuiClient.CONFIG.errorKillerHudY = y;
                }
                lastDown = down;
            }
        } else {
            dragging = false;
            lastDown = false;
        }
        int bg = Theme.argb(140, 16, 16, 20);
        int accent = Theme.withAlpha(Theme.accentColor(System.currentTimeMillis()), 170);
        float r = 6.0f;
        Rounded.rect(ctx, x, y, w, h, r, bg);
        Rounded.outline(ctx, x, y, w, h, r, 1, Theme.withAlpha(accent, 180));
        boolean hasWater = ErrorKillerHud.hasWaterOrLavaBucket(client);
        int slotX = x + pad;
        int slotY = y + pad;
        if (showCobRow) {
            boolean hasWeb = ErrorKillerHud.hasHotbarItem(client, Items.COBWEB);
            ErrorKillerHud.drawStatusSlot(ctx, slotX, slotY, slot, new ItemStack((ItemConvertible)Items.WATER_BUCKET), hasWater);
            ErrorKillerHud.drawStatusSlot(ctx, slotX + slot + gap, slotY, slot, new ItemStack((ItemConvertible)Items.COBWEB), hasWeb);
            slotY += slot + rowGap;
        }
        if (showObsRow) {
            boolean hasObs = ErrorKillerHud.hasHotbarItem(client, Items.OBSIDIAN);
            ErrorKillerHud.drawStatusSlot(ctx, slotX, slotY, slot, new ItemStack((ItemConvertible)Items.WATER_BUCKET), hasWater);
            ErrorKillerHud.drawStatusSlot(ctx, slotX + slot + gap, slotY, slot, new ItemStack((ItemConvertible)Items.OBSIDIAN), hasObs);
        }
    }

    private static void drawStatusSlot(DrawContext ctx, int x, int y, int size, ItemStack stack, boolean ok) {
        int bg = ok ? Theme.argb(150, 20, 20, 26) : -1996554240;
        Rounded.rect(ctx, x, y, size, size, 4.0f, bg);
        if (!stack.isEmpty()) {
            int iconX = x + (size - 16) / 2;
            int iconY = y + (size - 16) / 2;
            ctx.drawItemWithoutEntity(stack, iconX, iconY);
            if (!ok) {
                ctx.fill(x + 1, y + 1, x + size - 1, y + size - 1, -1711341568);
            }
        }
    }

    private static boolean hasHotbarItem(MinecraftClient client, Item item) {
        if (client == null || client.player == null) {
            return false;
        }
        for (int i = 0; i < 9; ++i) {
            if (!client.player.getInventory().getStack(i).isOf(item)) continue;
            return true;
        }
        return false;
    }

    private static boolean hasWaterOrLavaBucket(MinecraftClient client) {
        if (client == null || client.player == null) {
            return false;
        }
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (!stack.isOf(Items.WATER_BUCKET) && !stack.isOf(Items.LAVA_BUCKET)) continue;
            return true;
        }
        return false;
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
}

