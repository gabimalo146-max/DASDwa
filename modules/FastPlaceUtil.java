package me.Gui.gui.modules;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public final class FastPlaceUtil {
    private static long nextUseMs = 0L;
    private static int lastMode = -1;
    private static boolean lastEnabled = false;

    private FastPlaceUtil() {
    }

    public static boolean isEnabled() {
        HackModule m = GuiClient.MODULES.byId(ModuleId.FAST_PLACE);
        return m != null && m.isEnabled();
    }

    public static boolean shouldApply(MinecraftClient client) {
        if (!FastPlaceUtil.isEnabled() || client == null || client.player == null) {
            return false;
        }
        if (FastPlaceUtil.isTrapdoorTarget(client)) {
            return false;
        }
        ItemStack main = client.player.getMainHandStack();
        if (FastPlaceUtil.isBlockItem(main)) {
            return true;
        }
        if (main != null && !main.isEmpty()) {
            return false;
        }
        ItemStack off = client.player.getOffHandStack();
        return FastPlaceUtil.isBlockItem(off);
    }

    private static boolean isTrapdoorTarget(MinecraftClient client) {
        if (client == null || client.world == null) {
            return false;
        }
        if (client.player != null && client.player.isSneaking()) {
            return false;
        }
        if (client.crosshairTarget == null || client.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        HitResult hitResult = client.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult)) {
            return false;
        }
        BlockHitResult hit = (BlockHitResult)hitResult;
        BlockState state = client.world.getBlockState(hit.getBlockPos());
        return state.getBlock() instanceof TrapdoorBlock;
    }

    public static boolean allowUseNow() {
        boolean enabled = FastPlaceUtil.isEnabled();
        if (!enabled) {
            FastPlaceUtil.reset();
            return true;
        }
        int mode = FastPlaceUtil.clampMode(GuiClient.CONFIG.fastPlaceDelay);
        if (!lastEnabled || mode != lastMode) {
            nextUseMs = 0L;
            lastMode = mode;
        }
        lastEnabled = true;
        long now = System.currentTimeMillis();
        long interval = FastPlaceUtil.intervalMs(mode);
        if (interval <= 0L) {
            return true;
        }
        if (now < nextUseMs) {
            return false;
        }
        nextUseMs = now + interval;
        return true;
    }

    private static boolean isBlockItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.getItem() instanceof BlockItem;
    }

    private static void reset() {
        nextUseMs = 0L;
        lastMode = -1;
        lastEnabled = false;
    }

    private static int clampMode(int mode) {
        if (mode < 0) {
            return 0;
        }
        if (mode > 3) {
            return 3;
        }
        return mode;
    }

    private static long intervalMs(int mode) {
        return switch (mode) {
            case 0 -> 0L;
            case 1 -> 50L;
            case 2 -> 75L;
            case 3 -> 100L;
            default -> 0L;
        };
    }
}

