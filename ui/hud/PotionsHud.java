package me.Gui.gui.ui.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import me.Gui.gui.client.GuiClient;
import me.Gui.gui.ui.GuiFonts;
import me.Gui.gui.ui.HudEditScreen;
import me.Gui.gui.ui.Theme;
import me.Gui.gui.ui.render.HudGlass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class PotionsHud {
    private static boolean dragging = false;
    private static boolean lastDown = false;
    private static double dragOffX;
    private static double dragOffY;

    private PotionsHud() {
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        boolean editActive = HudEditScreen.isActiveFor(HudEditScreen.HudItem.POTIONS);
        if (HudEditScreen.isActive() && !editActive) {
            return;
        }
        if (!editActive && !Boolean.TRUE.equals(GuiClient.CONFIG.showPotionsHud)) {
            return;
        }
        ArrayList<StatusEffectInstance> effects = new ArrayList<StatusEffectInstance>();
        if (client.player != null) {
            effects.addAll(client.player.getStatusEffects());
        }
        effects.sort(Comparator.comparingInt(StatusEffectInstance::getDuration).reversed());
        if (effects.isEmpty() && editActive) {
            effects.add(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 260, 0));
            effects.add(new StatusEffectInstance(StatusEffects.SPEED, 520, 1));
        }
        if (effects.isEmpty()) {
            return;
        }
        TextRenderer tr = GuiFonts.textRendererOrDefault(client.textRenderer);
        int iconSize = 16;
        int padX = 8;
        int padY = 4;
        int gapX = 8;
        int rowGap = 6;
        Objects.requireNonNull(tr);
        int rowH = Math.max(iconSize, 9) + padY * 2;
        ArrayList<Row> rows = new ArrayList<Row>();
        int maxNameW = 0;
        int maxTimeW = 0;
        for (StatusEffectInstance e : effects) {
            String name = PotionsHud.effectName(e);
            String time = PotionsHud.formatDuration(e);
            int nameW = tr.getWidth(name);
            int timeW = tr.getWidth(time);
            maxNameW = Math.max(maxNameW, nameW);
            maxTimeW = Math.max(maxTimeW, timeW);
            rows.add(new Row(e, name, time));
        }
        int rawW = padX + iconSize + gapX + maxNameW + gapX + maxTimeW + padX;
        int rawH = rows.size() * rowH + Math.max(0, rows.size() - 1) * rowGap;
        float scale = GuiClient.CONFIG.potionsHudScale;
        if (Float.isNaN(scale) || Float.isInfinite(scale)) {
            scale = 0.9f;
        }
        scale = PotionsHud.clamp(scale, 0.5f, 1.4f);
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        int boxW = Math.round((float)rawW * scale);
        int boxH = Math.round((float)rawH * scale);
        int x = GuiClient.CONFIG.potionsHudX;
        int y = GuiClient.CONFIG.potionsHudY;
        int maxX = Math.max(4, sw - boxW - 4);
        int maxY = Math.max(4, sh - boxH - 4);
        x = PotionsHud.clamp(x, 4, maxX);
        y = PotionsHud.clamp(y, 4, maxY);
        GuiClient.CONFIG.potionsHudX = x;
        GuiClient.CONFIG.potionsHudY = y;
        if (editActive) {
            Window win = client.getWindow();
            long handle = win.getHandle();
            if (handle != 0L) {
                boolean down;
                int mx = PotionsHud.scaledMouseX(client);
                int my = PotionsHud.scaledMouseY(client);
                boolean bl = down = GLFW.glfwGetMouseButton((long)handle, (int)0) == 1;
                if (down && !lastDown && PotionsHud.inside(mx, my, x, y, boxW, boxH)) {
                    dragging = true;
                    dragOffX = mx - x;
                    dragOffY = my - y;
                }
                if (!down && lastDown && dragging) {
                    dragging = false;
                    PotionsHud.savePosition();
                }
                if (dragging) {
                    x = PotionsHud.clamp((int)((double)mx - dragOffX), 4, maxX);
                    y = PotionsHud.clamp((int)((double)my - dragOffY), 4, maxY);
                    GuiClient.CONFIG.potionsHudX = x;
                    GuiClient.CONFIG.potionsHudY = y;
                }
                lastDown = down;
            }
        } else {
            dragging = false;
            lastDown = false;
        }
        if (editActive) {
            HudEditScreen.setBounds(HudEditScreen.HudItem.POTIONS, x, y, boxW, boxH);
        } else {
            HudEditScreen.clearBounds(HudEditScreen.HudItem.POTIONS);
        }
        int accent = Theme.accentColor(System.currentTimeMillis());
        boolean useGuiColor = Boolean.TRUE.equals(GuiClient.CONFIG.potionsUseGuiColor);
        float frameAlpha = HudEditScreen.getFrameAlpha(HudEditScreen.HudItem.POTIONS);
        int textColor = Theme.withAlpha(accent, 255);
        float radius = Math.min(10.0f, (float)rowH * 0.5f);
        ctx.getMatrices().push();
        ctx.getMatrices().translate((float)x, (float)y, 0.0f);
        ctx.getMatrices().scale(scale, scale, 1.0f);
        HudGlass.panelNoBorder(ctx, 0, 0, rawW, rawH, radius, accent, frameAlpha, useGuiColor);
        int yy = 0;
        for (Row r : rows) {
            int iconX = padX;
            int iconY = yy + (rowH - iconSize) / 2;
            if (r.effect != null) {
                ctx.drawSpriteStretched(RenderLayer::getGuiTextured, client.getStatusEffectSpriteManager().getSprite(r.effect.getEffectType()), iconX, iconY, iconSize, iconSize);
            }
            Objects.requireNonNull(tr);
            int textY = yy + (rowH - 9) / 2;
            int nameX = iconX + iconSize + gapX;
            ctx.drawText(tr, r.name, nameX, textY, textColor, false);
            int timeX = nameX + maxNameW + gapX;
            ctx.drawText(tr, r.time, timeX, textY, textColor, false);
            yy += rowH + rowGap;
        }
        ctx.getMatrices().pop();
    }

    private static String effectName(StatusEffectInstance effect) {
        int amp;
        String base = PotionsHud.resolveEffectName(effect);
        if (base == null || base.isBlank()) {
            base = "Effect";
        }
        if ((amp = effect.getAmplifier()) > 0) {
            return base + " " + PotionsHud.toRoman(amp + 1);
        }
        return base;
    }

    private static String resolveEffectName(StatusEffectInstance effect) {
        String path;
        if (effect == null) {
            return null;
        }
        RegistryEntry type = effect.getEffectType();
        if (type == null) {
            return null;
        }
        StatusEffect value = (StatusEffect)type.value();
        if (value == null) {
            return null;
        }
        String key = value.getTranslationKey();
        String translated = null;
        if (key != null && !key.isBlank()) {
            translated = Text.translatable((String)key).getString();
        }
        if (translated != null && !translated.isBlank() && !translated.equals(key)) {
            return translated;
        }
        Identifier id = Registries.STATUS_EFFECT.getId(value);
        if (id != null && (path = id.getPath()) != null && !path.isBlank()) {
            return PotionsHud.humanizeIdPath(path);
        }
        if (translated != null && !translated.isBlank()) {
            return translated;
        }
        return null;
    }

    private static String humanizeIdPath(String path) {
        String[] parts = path.split("_");
        StringBuilder out = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() <= 1) continue;
            out.append(p.substring(1));
        }
        String s = out.toString();
        return s.isBlank() ? path.replace('_', ' ') : s;
    }

    private static String formatDuration(StatusEffectInstance effect) {
        if (effect == null) {
            return "-";
        }
        if (effect.isInfinite()) {
            return "inf";
        }
        int total = Math.max(0, effect.getDuration() / 20);
        int min = total / 60;
        int sec = total % 60;
        return String.format("%d:%02d", min, sec);
    }

    private static String toRoman(int v) {
        return switch (v) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> Integer.toString(v);
        };
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

    private record Row(StatusEffectInstance effect, String name, String time) {
    }
}

