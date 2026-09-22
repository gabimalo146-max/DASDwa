package me.Gui.gui.modules;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import net.minecraft.block.Block;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.CobwebBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.lwjgl.glfw.GLFW;

public final class ErrorKillerUtil {
    private static int step = -1;
    private static long lastStepTime = 0L;
    private static boolean lastRightClickPressed = false;
    private static boolean moveFixSprint = false;
    private static int originalSlot = -1;
    private static BlockHitResult target = null;
    private static Mode currentMode = Mode.COBWEB;
    private static boolean useOriginalSlotForPlace = false;

    private ErrorKillerUtil() {
    }

    public static void requestAction(MinecraftClient client) {
        ErrorKillerUtil.requestAction(client, Mode.COBWEB);
    }

    public static void requestAction(MinecraftClient client, Mode mode) {
        if (client == null || client.player == null || client.interactionManager == null) {
            return;
        }
        if (!ErrorKillerUtil.isEnabled()) {
            return;
        }
        if (client.currentScreen != null) {
            return;
        }
        if (step != -1) {
            return;
        }
        if (client.crosshairTarget == null || client.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            return;
        }
        originalSlot = client.player.getInventory().selectedSlot;
        target = ErrorKillerUtil.normalizeLeverTarget(client, (BlockHitResult)client.crosshairTarget);
        currentMode = mode == null ? Mode.COBWEB : mode;
        useOriginalSlotForPlace = false;
        step = 0;
    }

    public static void tick(MinecraftClient client) {
        if (client == null || client.player == null || client.interactionManager == null) {
            lastRightClickPressed = false;
            ErrorKillerUtil.reset(client);
            return;
        }
        if (!ErrorKillerUtil.isEnabled()) {
            lastRightClickPressed = false;
            ErrorKillerUtil.reset(client);
            return;
        }
        if (client.currentScreen != null) {
            lastRightClickPressed = false;
            return;
        }
        if (ErrorKillerUtil.isHeadInCobweb(client)) {
            lastRightClickPressed = false;
            ErrorKillerUtil.reset(client);
            return;
        }
        if (step == -1) {
            ErrorKillerUtil.detectAutoTrigger(client);
            return;
        }
        ErrorKillerUtil.applyMoveFix(client);
        ErrorKillerUtil.runMacro(client);
    }

    private static boolean isEnabled() {
        HackModule mod = GuiClient.MODULES.byId(ModuleId.ERROR_KILLER);
        return mod != null && mod.isEnabled();
    }

    private static void reset(MinecraftClient client) {
        if (client != null && client.player != null && originalSlot != -1) {
            client.player.getInventory().selectedSlot = originalSlot;
        }
        step = -1;
        target = null;
        originalSlot = -1;
        currentMode = Mode.COBWEB;
        useOriginalSlotForPlace = false;
        moveFixSprint = false;
    }

    private static void detectAutoTrigger(MinecraftClient client) {
        BlockHitResult bhr;
        HitResult hitResult;
        ItemStack handStack;
        boolean isRightClick;
        boolean bl = isRightClick = GLFW.glfwGetMouseButton((long)client.getWindow().getHandle(), (int)1) == 1;
        if (isRightClick && !lastRightClickPressed && step == -1 && ErrorKillerUtil.isValidTriggerItem(client, handStack = client.player.getMainHandStack()) && (hitResult = client.crosshairTarget) instanceof BlockHitResult && ErrorKillerUtil.isTargetingPlacementOnTrigger(client, bhr = (BlockHitResult)hitResult)) {
            originalSlot = client.player.getInventory().selectedSlot;
            target = ErrorKillerUtil.normalizeLeverTarget(client, bhr);
            if (handStack.isOf(Items.COBWEB)) {
                currentMode = Mode.COBWEB;
                useOriginalSlotForPlace = false;
            } else {
                currentMode = Mode.OBSIDIAN;
                useOriginalSlotForPlace = true;
            }
            moveFixSprint = client.options != null && (client.player.isSprinting() || client.options.sprintKey.isPressed());
            step = 0;
        }
        lastRightClickPressed = isRightClick;
    }

    private static boolean isValidTriggerItem(MinecraftClient client, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (stack.isOf(Items.COBWEB)) {
            return true;
        }
        Item item = stack.getItem();
        if (!(item instanceof BlockItem)) {
            return false;
        }
        BlockItem blockItem = (BlockItem)item;
        if (client.world == null) {
            return false;
        }
        Block block = blockItem.getBlock();
        return block.getDefaultState().isFullCube((BlockView)client.world, BlockPos.ORIGIN);
    }

    private static boolean isTargetingPlacementOnTrigger(MinecraftClient client, BlockHitResult bhr) {
        if (client.world == null) {
            return false;
        }
        BlockPos pos = bhr.getBlockPos();
        Block blockAtPos = client.world.getBlockState(pos).getBlock();
        if (blockAtPos instanceof LeverBlock || blockAtPos instanceof ButtonBlock) {
            return true;
        }
        BlockPos offsetPos = pos.offset(bhr.getSide());
        Block blockAtOffset = client.world.getBlockState(offsetPos).getBlock();
        return blockAtOffset instanceof LeverBlock || blockAtOffset instanceof ButtonBlock;
    }

    private static BlockHitResult normalizeLeverTarget(MinecraftClient client, BlockHitResult bhr) {
        BlockPos supportPos;
        if (client == null || client.world == null || bhr == null) {
            return bhr;
        }
        BlockPos pos = bhr.getBlockPos();
        Block blockAtPos = client.world.getBlockState(pos).getBlock();
        if ((blockAtPos instanceof LeverBlock || blockAtPos instanceof ButtonBlock) && !client.world.getBlockState(supportPos = pos.offset(bhr.getSide().getOpposite())).isAir()) {
            return new BlockHitResult(bhr.getPos(), bhr.getSide(), supportPos, bhr.isInsideBlock());
        }
        return bhr;
    }

    private static boolean isHeadInCobweb(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) {
            return false;
        }
        BlockPos headPos = client.player.getBlockPos().up();
        return client.world.getBlockState(headPos).getBlock() instanceof CobwebBlock;
    }

    private static void runMacro(MinecraftClient client) {
        HitResult hitResult;
        BlockHitResult bhr;
        int delayMs;
        long now = System.currentTimeMillis();
        int fluidBucket = -1;
        int emptyBucket = -1;
        int placeSlot = -1;
        boolean placeCobweb = currentMode == Mode.COBWEB;
        Item placeItem = placeCobweb ? Items.COBWEB : Items.OBSIDIAN;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.isOf(Items.WATER_BUCKET) || stack.isOf(Items.LAVA_BUCKET)) {
                fluidBucket = i;
            }
            if (stack.isOf(Items.BUCKET)) {
                emptyBucket = i;
            }
            if (!stack.isOf(placeItem)) continue;
            placeSlot = i;
        }
        if (!placeCobweb && useOriginalSlotForPlace && originalSlot >= 0 && originalSlot <= 8) {
            placeSlot = originalSlot;
        }
        int n = delayMs = currentMode == Mode.OBSIDIAN ? GuiClient.CONFIG.errorKillerDelayMsObsidian : GuiClient.CONFIG.errorKillerDelayMs;
        if (delayMs < 1 && (delayMs = GuiClient.CONFIG.errorKillerDelayMs) < 1) {
            delayMs = 1;
        }
        if ((bhr = target) == null && (hitResult = client.crosshairTarget) instanceof BlockHitResult) {
            BlockHitResult hit = (BlockHitResult)hitResult;
            bhr = ErrorKillerUtil.normalizeLeverTarget(client, hit);
        }
        if (delayMs <= 1) {
            if (fluidBucket != -1 && placeSlot != -1 && bhr != null) {
                int toPickUp;
                client.player.getInventory().selectedSlot = fluidBucket;
                client.interactionManager.interactItem((PlayerEntity)client.player, Hand.MAIN_HAND);
                ErrorKillerUtil.applyMoveFix(client);
                int n2 = toPickUp = fluidBucket != -1 ? fluidBucket : emptyBucket;
                if (toPickUp != -1) {
                    client.player.getInventory().selectedSlot = toPickUp;
                    client.interactionManager.interactItem((PlayerEntity)client.player, Hand.MAIN_HAND);
                    ErrorKillerUtil.applyMoveFix(client);
                }
                client.player.getInventory().selectedSlot = placeSlot;
                client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, bhr);
                ErrorKillerUtil.applyMoveFix(client);
            }
            ErrorKillerUtil.reset(client);
            return;
        }
        switch (step) {
            case 0: {
                if (fluidBucket != -1) {
                    client.player.getInventory().selectedSlot = fluidBucket;
                    client.interactionManager.interactItem((PlayerEntity)client.player, Hand.MAIN_HAND);
                    ErrorKillerUtil.applyMoveFix(client);
                    lastStepTime = now;
                    step = 1;
                    break;
                }
                ErrorKillerUtil.reset(client);
                break;
            }
            case 1: {
                if (now - lastStepTime < (long)delayMs) break;
                step = 2;
                break;
            }
            case 2: {
                int toPick;
                int n3 = toPick = fluidBucket != -1 ? fluidBucket : emptyBucket;
                if (toPick != -1) {
                    client.player.getInventory().selectedSlot = toPick;
                    client.interactionManager.interactItem((PlayerEntity)client.player, Hand.MAIN_HAND);
                    ErrorKillerUtil.applyMoveFix(client);
                }
                step = 3;
                break;
            }
            case 3: {
                if (placeSlot != -1 && bhr != null) {
                    client.player.getInventory().selectedSlot = placeSlot;
                    client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, bhr);
                    ErrorKillerUtil.applyMoveFix(client);
                }
                ErrorKillerUtil.reset(client);
                break;
            }
            default: {
                ErrorKillerUtil.reset(client);
            }
        }
    }

    private static void applyMoveFix(MinecraftClient client) {
        if (!moveFixSprint) {
            return;
        }
        if (client == null || client.player == null || client.options == null) {
            return;
        }
        if (client.options.sneakKey.isPressed()) {
            return;
        }
        if (!client.options.forwardKey.isPressed()) {
            return;
        }
        client.player.setSprinting(true);
    }

    public static enum Mode {
        COBWEB,
        OBSIDIAN;

    }
}

