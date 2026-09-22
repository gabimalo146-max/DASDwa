package me.Gui.gui.ui.hud;

import java.util.Objects;
import me.Gui.gui.client.GuiClient;
import me.Gui.gui.ui.GuiFonts;
import me.Gui.gui.ui.HudEditScreen;
import me.Gui.gui.ui.Theme;
import me.Gui.gui.ui.render.HudGlass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;

public final class ArmorHud {
    private static boolean dragging = false;
    private static boolean lastDown = false;
    private static double dragOffX;
    private static double dragOffY;

    private ArmorHud() {
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        int rawH;
        int rawW;
        boolean showHeld;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        boolean editActive = HudEditScreen.isActiveFor(HudEditScreen.HudItem.ARMOR);
        if (HudEditScreen.isActive() && !editActive) {
            return;
        }
        if (!editActive && !Boolean.TRUE.equals(GuiClient.CONFIG.showArmorHud)) {
            return;
        }
        ItemStack helmet = ItemStack.EMPTY;
        ItemStack chest = ItemStack.EMPTY;
        ItemStack legs = ItemStack.EMPTY;
        ItemStack boots = ItemStack.EMPTY;
        ItemStack held = ItemStack.EMPTY;
        if (client.player != null) {
            helmet = client.player.getEquippedStack(EquipmentSlot.HEAD);
            chest = client.player.getEquippedStack(EquipmentSlot.CHEST);
            legs = client.player.getEquippedStack(EquipmentSlot.LEGS);
            boots = client.player.getEquippedStack(EquipmentSlot.FEET);
            held = client.player.getMainHandStack();
            if (held == null || held.isEmpty()) {
                held = client.player.getOffHandStack();
            }
        }
        if (editActive && helmet.isEmpty() && chest.isEmpty() && legs.isEmpty() && boots.isEmpty()) {
            helmet = new ItemStack((ItemConvertible)Items.DIAMOND_HELMET);
            chest = new ItemStack((ItemConvertible)Items.DIAMOND_CHESTPLATE);
            legs = new ItemStack((ItemConvertible)Items.DIAMOND_LEGGINGS);
            boots = new ItemStack((ItemConvertible)Items.DIAMOND_BOOTS);
        }
        if (editActive && (held == null || held.isEmpty())) {
            held = new ItemStack((ItemConvertible)Items.DIAMOND_SWORD);
        }
        boolean bl = showHeld = held != null && !held.isEmpty();
        if (!editActive && helmet.isEmpty() && chest.isEmpty() && legs.isEmpty() && boots.isEmpty() && !showHeld) {
            return;
        }
        TextRenderer tr = GuiFonts.textRendererOrDefault(client.textRenderer);
        boolean percent = GuiClient.CONFIG.armorHudDurabilityPercent == null || GuiClient.CONFIG.armorHudDurabilityPercent != false;
        boolean vertical = GuiClient.CONFIG.armorHudVertical != null && GuiClient.CONFIG.armorHudVertical != false;
        int icon = 16;
        int gap = 2;
        int padX = 6;
        int padY = 4;
        Objects.requireNonNull(tr);
        int textH = 9;
        int textGap = 2;
        int maxTextW = ArmorHud.maxTopTextWidth(tr, percent, helmet, chest, legs, boots, held);
        int slotW = Math.max(icon, maxTextW);
        int slots = 4 + (showHeld ? 1 : 0);
        if (vertical) {
            rawW = padX * 2 + icon + (maxTextW > 0 ? textGap + maxTextW : 0);
            rawH = padY * 2 + slots * icon + (slots - 1) * gap;
        } else {
            rawW = padX * 2 + slotW * slots + gap * Math.max(0, slots - 1);
            rawH = padY * 2 + icon + textH + textGap;
        }
        float scale = GuiClient.CONFIG.armorHudScale;
        if (Float.isNaN(scale) || Float.isInfinite(scale)) {
            scale = 0.9f;
        }
        scale = ArmorHud.clamp(scale, 0.5f, 1.4f);
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        int boxW = Math.round((float)rawW * scale);
        int boxH = Math.round((float)rawH * scale);
        int x = GuiClient.CONFIG.armorHudX;
        int y = GuiClient.CONFIG.armorHudY;
        int maxX = Math.max(4, sw - boxW - 4);
        int maxY = Math.max(4, sh - boxH - 4);
        x = ArmorHud.clamp(x, 4, maxX);
        y = ArmorHud.clamp(y, 4, maxY);
        GuiClient.CONFIG.armorHudX = x;
        GuiClient.CONFIG.armorHudY = y;
        if (editActive) {
            Window win = client.getWindow();
            long handle = win.getHandle();
            if (handle != 0L) {
                boolean down;
                int mx = ArmorHud.scaledMouseX(client);
                int my = ArmorHud.scaledMouseY(client);
                boolean bl2 = down = GLFW.glfwGetMouseButton((long)handle, (int)0) == 1;
                if (down && !lastDown && ArmorHud.inside(mx, my, x, y, boxW, boxH)) {
                    dragging = true;
                    dragOffX = mx - x;
                    dragOffY = my - y;
                }
                if (!down && lastDown && dragging) {
                    dragging = false;
                    ArmorHud.savePosition();
                }
                if (dragging) {
                    x = ArmorHud.clamp((int)((double)mx - dragOffX), 4, maxX);
                    y = ArmorHud.clamp((int)((double)my - dragOffY), 4, maxY);
                    GuiClient.CONFIG.armorHudX = x;
                    GuiClient.CONFIG.armorHudY = y;
                }
                lastDown = down;
            }
        } else {
            dragging = false;
            lastDown = false;
        }
        if (editActive) {
            HudEditScreen.setBounds(HudEditScreen.HudItem.ARMOR, x, y, boxW, boxH);
        } else {
            HudEditScreen.clearBounds(HudEditScreen.HudItem.ARMOR);
        }
        int accent = Theme.accentColor(System.currentTimeMillis());
        boolean useGuiColor = Boolean.TRUE.equals(GuiClient.CONFIG.armorUseGuiColor);
        float frameAlpha = HudEditScreen.getFrameAlpha(HudEditScreen.HudItem.ARMOR);
        int textColor = Theme.withAlpha(accent, 255);
        float radius = Math.min(8.0f, (float)rawH * 0.5f);
        ctx.getMatrices().push();
        ctx.getMatrices().translate((float)x, (float)y, 0.0f);
        ctx.getMatrices().scale(scale, scale, 1.0f);
        HudGlass.panelNoBorder(ctx, 0, 0, rawW, rawH, radius, accent, frameAlpha, useGuiColor);
        int xx = padX;
        if (vertical) {
            int yy = padY;
            int xxv = padX;
            int textX = xxv + icon + textGap;
            if (!helmet.isEmpty()) {
                ArmorHud.drawItemWithOverlay(ctx, tr, helmet, xxv, yy);
                ArmorHud.drawStackTextRight(ctx, tr, helmet, percent, textX, yy, icon, textColor);
                yy += icon + gap;
            }
            if (!chest.isEmpty()) {
                ArmorHud.drawItemWithOverlay(ctx, tr, chest, xxv, yy);
                ArmorHud.drawStackTextRight(ctx, tr, chest, percent, textX, yy, icon, textColor);
                yy += icon + gap;
            }
            if (!legs.isEmpty()) {
                ArmorHud.drawItemWithOverlay(ctx, tr, legs, xxv, yy);
                ArmorHud.drawStackTextRight(ctx, tr, legs, percent, textX, yy, icon, textColor);
                yy += icon + gap;
            }
            if (!boots.isEmpty()) {
                ArmorHud.drawItemWithOverlay(ctx, tr, boots, xxv, yy);
                ArmorHud.drawStackTextRight(ctx, tr, boots, percent, textX, yy, icon, textColor);
                yy += icon + gap;
            }
            if (showHeld) {
                ArmorHud.drawItemWithOverlay(ctx, tr, held, xxv, yy);
                ArmorHud.drawStackTextRight(ctx, tr, held, percent, textX, yy, icon, textColor);
            }
        } else {
            int iconX;
            int textY = padY;
            int yy = padY + textH + textGap;
            if (!helmet.isEmpty()) {
                iconX = xx + (slotW - icon) / 2;
                ArmorHud.drawItemWithOverlay(ctx, tr, helmet, iconX, yy);
                ArmorHud.drawStackText(ctx, tr, helmet, percent, xx, textY, slotW, textColor);
            }
            xx += slotW + gap;
            if (!chest.isEmpty()) {
                iconX = xx + (slotW - icon) / 2;
                ArmorHud.drawItemWithOverlay(ctx, tr, chest, iconX, yy);
                ArmorHud.drawStackText(ctx, tr, chest, percent, xx, textY, slotW, textColor);
            }
            xx += slotW + gap;
            if (!legs.isEmpty()) {
                iconX = xx + (slotW - icon) / 2;
                ArmorHud.drawItemWithOverlay(ctx, tr, legs, iconX, yy);
                ArmorHud.drawStackText(ctx, tr, legs, percent, xx, textY, slotW, textColor);
            }
            xx += slotW + gap;
            if (!boots.isEmpty()) {
                iconX = xx + (slotW - icon) / 2;
                ArmorHud.drawItemWithOverlay(ctx, tr, boots, iconX, yy);
                ArmorHud.drawStackText(ctx, tr, boots, percent, xx, textY, slotW, textColor);
            }
            if (showHeld) {
                iconX = (xx += slotW + gap) + (slotW - icon) / 2;
                ArmorHud.drawItemWithOverlay(ctx, tr, held, iconX, yy);
                ArmorHud.drawStackText(ctx, tr, held, percent, xx, textY, slotW, textColor);
            }
        }
        ctx.getMatrices().pop();
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

    private static void drawStackText(DrawContext ctx, TextRenderer tr, ItemStack stack, boolean percent, int x, int y, int w, int color) {
        String text = ArmorHud.stackTopText(stack, percent);
        if (text.isEmpty()) {
            return;
        }
        int tw = tr.getWidth(text);
        int tx = x + (w - tw) / 2;
        ctx.drawText(tr, text, tx, y, color, false);
    }

    private static void drawStackTextRight(DrawContext ctx, TextRenderer tr, ItemStack stack, boolean percent, int x, int y, int iconH, int color) {
        String text = ArmorHud.stackTopText(stack, percent);
        if (text.isEmpty()) {
            return;
        }
        Objects.requireNonNull(tr);
        int ty = y + (iconH - 9) / 2;
        ctx.drawText(tr, text, x, ty, color, false);
    }

    private static String stackTopText(ItemStack stack, boolean percent) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        int count = stack.getCount();
        if (count > 1) {
            return Integer.toString(count);
        }
        if (!stack.isDamageable()) {
            return "";
        }
        int max = stack.getMaxDamage();
        if (max <= 0) {
            return "";
        }
        int left = Math.max(0, max - stack.getDamage());
        if (percent) {
            int pct = Math.round((float)left * 100.0f / (float)max);
            return pct + "%";
        }
        return Integer.toString(left);
    }

    private static int maxTopTextWidth(TextRenderer tr, boolean percent, ItemStack ... stacks) {
        int max = 0;
        if (stacks == null) {
            return 0;
        }
        for (ItemStack s : stacks) {
            int w;
            String t = ArmorHud.stackTopText(s, percent);
            if (t.isEmpty() || (w = tr.getWidth(t)) <= max) continue;
            max = w;
        }
        return max;
    }

    private static void drawItemWithOverlay(DrawContext ctx, TextRenderer tr, ItemStack stack, int x, int y) {
        ctx.drawItem(stack, x, y);
        ctx.drawStackOverlay(tr, stack, x, y);
    }
}

