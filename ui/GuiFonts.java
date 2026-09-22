package me.Gui.gui.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;

public final class GuiFonts {
    private GuiFonts() {
    }

    public static TextRenderer textRenderer() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null ? client.textRenderer : null;
    }

    public static TextRenderer textRendererOrDefault(TextRenderer fallback) {
        TextRenderer tr = GuiFonts.textRenderer();
        return tr != null ? tr : fallback;
    }
}

