package me.Gui.gui.client.sound;

import me.Gui.gui.client.GuiClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public final class SoundUtil {
    private SoundUtil() {
    }

    private static float volume() {
        float v = GuiClient.CONFIG.uiVolume;
        if (Float.isNaN(v) || Float.isInfinite(v)) {
            v = 0.6f;
        }
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    private static float tpaVolume() {
        Float v = GuiClient.CONFIG.tpaAcceptSoundVolume;
        if (v == null || Float.isNaN(v.floatValue()) || Float.isInfinite(v.floatValue())) {
            v = Float.valueOf(1.0f);
        }
        return Math.max(0.0f, Math.min(1.0f, v.floatValue()));
    }

    public static void playToggle(boolean enabled) {
        float pitch = enabled ? 1.15f : 0.9f;
        SoundUtil.playUi(pitch);
    }

    public static void playGuiOpen() {
        SoundUtil.playUi(1.0f);
    }

    private static void playUi(float pitch) {
        SoundUtil.playSound((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), pitch);
    }

    public static void playAutoTpaAccept() {
        MinecraftClient client = MinecraftClient.getInstance();
        float vol = 1.8f * SoundUtil.tpaVolume() * 12.0f;
        vol = SoundUtil.bypassMasterVolume(client, vol);
        SoundUtil.playSoundAbsolute((SoundEvent)SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 1.7f, vol, true);
    }

    public static void playAutoParawan() {
        float vol = SoundUtil.tpaVolume();
        SoundUtil.playSoundAbsolute((SoundEvent)SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.2f, vol);
    }

    private static void playSound(SoundEvent sound, float pitch) {
        SoundUtil.playSound(sound, pitch, 1.0f);
    }

    private static void playSound(SoundEvent sound, float pitch, float volumeScale) {
        SoundUtil.playSoundInternal(sound, pitch, SoundUtil.volume(), volumeScale, false);
    }

    private static void playSoundAbsolute(SoundEvent sound, float pitch, float volume) {
        SoundUtil.playSoundInternal(sound, pitch, volume, 1.0f, false);
    }

    private static void playSoundAbsolute(SoundEvent sound, float pitch, float volume, boolean allowOverdrive) {
        SoundUtil.playSoundInternal(sound, pitch, volume, 1.0f, allowOverdrive);
    }

    private static void playSoundInternal(SoundEvent sound, float pitch, float baseVolume, float volumeScale, boolean allowOverdrive) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        float vol = baseVolume * volumeScale;
        if (vol <= 0.0f) {
            return;
        }
        if (!allowOverdrive) {
            if (vol > 1.0f) {
                vol = 1.0f;
            }
            SoundUtil.playOnce(client, sound, pitch, vol);
            return;
        }
        SoundUtil.playStacked(client, sound, pitch, vol);
    }

    private static void playStacked(MinecraftClient client, SoundEvent sound, float pitch, float volume) {
        int layers = (int)Math.floor(volume);
        float rem = volume - (float)layers;
        if (layers <= 0) {
            SoundUtil.playOnce(client, sound, pitch, volume);
            return;
        }
        int maxLayers = 32;
        if (layers > maxLayers) {
            layers = maxLayers;
        }
        for (int i = 0; i < layers; ++i) {
            SoundUtil.playOnce(client, sound, SoundUtil.pitchJitter(pitch, i), 1.0f);
        }
        if (rem > 0.0f && layers < maxLayers) {
            SoundUtil.playOnce(client, sound, SoundUtil.pitchJitter(pitch, layers), rem);
        }
    }

    private static float bypassMasterVolume(MinecraftClient client, float vol) {
        if (client == null || client.options == null) {
            return vol;
        }
        float master = client.options.getSoundVolume(SoundCategory.MASTER);
        if (Float.isNaN(master) || Float.isInfinite(master) || master <= 0.0f) {
            return vol;
        }
        return vol / master;
    }

    private static float pitchJitter(float pitch, int index) {
        float delta = index % 2 == 0 ? 0.01f : -0.01f;
        return pitch + delta * (float)(1 + index % 3);
    }

    private static void playOnce(MinecraftClient client, SoundEvent sound, float pitch, float volume) {
        SoundManager soundManager = client.getSoundManager();
        if (soundManager != null) {
            soundManager.play((SoundInstance)PositionedSoundInstance.master((SoundEvent)sound, (float)pitch, (float)volume));
            return;
        }
        if (client.player != null) {
            client.player.playSound(sound, volume, pitch);
        }
    }
}

