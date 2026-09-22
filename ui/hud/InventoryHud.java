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
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public final class InventoryHud {
    private static boolean dragging = false;
    private static boolean lastDown = false;
    private static double dragOffX;
    private static double dragOffY;

    private InventoryHud() {
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        int rowW;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        boolean editActive = HudEditScreen.isActiveFor(HudEditScreen.HudItem.INVENTORY);
        if (HudEditScreen.isActive() && !editActive) {
            return;
        }
        if (!editActive && !Boolean.TRUE.equals(GuiClient.CONFIG.showInventoryHud)) {
            return;
        }
        TextRenderer tr = GuiFonts.textRendererOrDefault(client.textRenderer);
        PlayerInventory inv = client.player == null ? null : client.player.getInventory();
        int cols = 9;
        int rows = 3;
        int slot = 18;
        int slotGap = 0;
        int rowPadX = 0;
        int rowPadY = 0;
        int rowGap = 0;
        int rowH = slot + rowPadY * 2;
        int rawW = rowW = cols * slot + (cols - 1) * slotGap + rowPadX * 2;
        int rawH = rows * rowH + Math.max(0, rows - 1) * rowGap;
        float scale = GuiClient.CONFIG.inventoryHudScale;
        if (Float.isNaN(scale) || Float.isInfinite(scale)) {
            scale = 0.9f;
        }
        scale = InventoryHud.clamp(scale, 0.5f, 1.4f);
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        int boxW = Math.round((float)rawW * scale);
        int boxH = Math.round((float)rawH * scale);
        int x = GuiClient.CONFIG.inventoryHudX;
        int y = GuiClient.CONFIG.inventoryHudY;
        int maxX = Math.max(4, sw - boxW - 4);
        int maxY = Math.max(4, sh - boxH - 4);
        x = InventoryHud.clamp(x, 4, maxX);
        y = InventoryHud.clamp(y, 4, maxY);
        GuiClient.CONFIG.inventoryHudX = x;
        GuiClient.CONFIG.inventoryHudY = y;
        if (editActive) {
            Window win = client.getWindow();
            long handle = win.getHandle();
            if (handle != 0L) {
                boolean down;
                int mx = InventoryHud.scaledMouseX(client);
                int my = InventoryHud.scaledMouseY(client);
                boolean bl = down = GLFW.glfwGetMouseButton((long)handle, (int)0) == 1;
                if (down && !lastDown && InventoryHud.inside(mx, my, x, y, boxW, boxH)) {
                    dragging = true;
                    dragOffX = mx - x;
                    dragOffY = my - y;
                }
                if (!down && lastDown && dragging) {
                    dragging = false;
                    InventoryHud.savePosition();
                }
                if (dragging) {
                    x = InventoryHud.clamp((int)((double)mx - dragOffX), 4, maxX);
                    y = InventoryHud.clamp((int)((double)my - dragOffY), 4, maxY);
                    GuiClient.CONFIG.inventoryHudX = x;
                    GuiClient.CONFIG.inventoryHudY = y;
                }
                lastDown = down;
            }
        } else {
            dragging = false;
            lastDown = false;
        }
        if (editActive) {
            HudEditScreen.setBounds(HudEditScreen.HudItem.INVENTORY, x, y, boxW, boxH);
        } else {
            HudEditScreen.clearBounds(HudEditScreen.HudItem.INVENTORY);
        }
        int accent = Theme.accentColor(System.currentTimeMillis());
        boolean useGuiColor = Boolean.TRUE.equals(GuiClient.CONFIG.inventoryUseGuiColor);
        float frameAlpha = HudEditScreen.getFrameAlpha(HudEditScreen.HudItem.INVENTORY);
        float radius = 8.0f;
        int itemSize = 16;
        float countScale = 0.75f;
        ctx.getMatrices().push();
        ctx.getMatrices().translate((float)x, (float)y, 0.0f);
        ctx.getMatrices().scale(scale, scale, 1.0f);
        int rowsStartY = 0;
        int sepColor = Theme.withAlpha(accent, Math.round(70.0f * frameAlpha));
        int sepTopPad = 3;
        HudGlass.panelNoBorder(ctx, 0, 0, rawW, rawH, radius, accent, frameAlpha, useGuiColor);
        for (int row = 0; row < rows; ++row) {
            int col;
            int rowY = rowsStartY + row * (rowH + rowGap);
            for (col = 1; col < cols; ++col) {
                int sepX = rowPadX + col * slot + (col - 1) * slotGap;
                ctx.fill(sepX, rowY + sepTopPad, sepX + slotGap, rowY + rowH - sepTopPad, sepColor);
            }
            for (col = 0; col < cols; ++col) {
                int idx;
                int slotX = rowPadX + col * (slot + slotGap);
                int slotY = rowY + rowPadY;
                ItemStack stack = ItemStack.EMPTY;
                if (inv != null && (idx = row * cols + col + 9) >= 0 && idx < inv.main.size()) {
                    stack = (ItemStack)inv.main.get(idx);
                }
                if (stack == null || stack.isEmpty()) continue;
                int iconX = slotX + 1;
                int iconY = slotY + 1;
                ctx.drawItemWithoutEntity(stack, iconX, iconY);
                if (stack.getCount() <= 1) continue;
                String count = Integer.toString(stack.getCount());
                int tw = tr.getWidth(count);
                Objects.requireNonNull(tr);
                int th = 9;
                int tx = slotX + slot - 1;
                int ty = rowY + rowH - 1;
                ctx.getMatrices().push();
                ctx.getMatrices().translate(0.0f, 0.0f, 200.0f);
                ctx.getMatrices().translate((float)tx, (float)ty, 0.0f);
                ctx.getMatrices().scale(countScale, countScale, 1.0f);
                ctx.drawTextWithShadow(tr, count, -tw, -th, -1);
                ctx.getMatrices().pop();
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
}

