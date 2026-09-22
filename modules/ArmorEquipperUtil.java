package me.Gui.gui.modules;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import me.Gui.gui.ui.GuiScreen;
import me.Gui.gui.ui.hud.NotificationHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public final class ArmorEquipperUtil {
    private static final Item[] NETHERITE = new Item[]{Items.NETHERITE_BOOTS, Items.NETHERITE_LEGGINGS, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_HELMET};
    private static final Item[] DIAMOND = new Item[]{Items.DIAMOND_BOOTS, Items.DIAMOND_LEGGINGS, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_HELMET};
    private static final EquipmentSlot[] SLOTS = new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
    private static boolean pendingStart = false;
    private static boolean swapping = false;
    private static int stepIndex = 0;
    private static int delayTicks = 0;
    private static boolean finishPending = false;

    private ArmorEquipperUtil() {
    }

    public static void requestSwap(MinecraftClient client) {
        if (client == null || client.player == null) {
            return;
        }
        if (!ArmorEquipperUtil.isEnabled()) {
            return;
        }
        if (swapping) {
            return;
        }
        if (client.currentScreen != null) {
            if (client.currentScreen instanceof GuiScreen) {
                client.setScreen(null);
            } else {
                return;
            }
        }
        pendingStart = true;
        finishPending = false;
    }

    public static void tick(MinecraftClient client) {
        if (client == null || client.player == null || client.interactionManager == null) {
            ArmorEquipperUtil.reset();
            return;
        }
        if (!ArmorEquipperUtil.isEnabled()) {
            ArmorEquipperUtil.reset();
            return;
        }
        if (pendingStart && !swapping) {
            ArmorEquipperUtil.startSwap(client);
        }
        if (!swapping) {
            return;
        }
        if (delayTicks > 0) {
            --delayTicks;
            return;
        }
        if (finishPending) {
            ArmorEquipperUtil.finishSwap(client);
            return;
        }
        if (stepIndex >= SLOTS.length) {
            finishPending = true;
            delayTicks = 1;
            return;
        }
        if (!ArmorEquipperUtil.ensureInventoryReady(client)) {
            return;
        }
        EquipmentSlot slot = SLOTS[stepIndex];
        Item a = NETHERITE[stepIndex];
        Item b = DIAMOND[stepIndex];
        ArmorEquipperUtil.swapBestForSlot((PlayerEntity)client.player, slot, a, b);
        if (++stepIndex >= SLOTS.length) {
            finishPending = true;
            delayTicks = 1;
            return;
        }
        delayTicks = Math.max(1, ArmorEquipperUtil.getDelayTicks());
    }

    private static boolean isEnabled() {
        HackModule mod = GuiClient.MODULES.byId(ModuleId.ARMOR_EQUIPPER);
        return mod != null && mod.isEnabled();
    }

    private static void startSwap(MinecraftClient client) {
        pendingStart = false;
        swapping = true;
        finishPending = false;
        stepIndex = 0;
        delayTicks = 1;
        client.setScreen((Screen)new InventoryScreen((PlayerEntity)client.player));
    }

    private static void finishSwap(MinecraftClient client) {
        swapping = false;
        finishPending = false;
        stepIndex = 0;
        delayTicks = 0;
        if (client.currentScreen instanceof InventoryScreen) {
            client.setScreen(null);
            client.player.closeHandledScreen();
        }
        NotificationHud.postCustomItem("Your Set Has Been Changed", Items.NETHERITE_CHESTPLATE);
    }

    private static void reset() {
        pendingStart = false;
        swapping = false;
        finishPending = false;
        stepIndex = 0;
        delayTicks = 0;
    }

    private static boolean ensureInventoryReady(MinecraftClient client) {
        if (!(client.currentScreen instanceof InventoryScreen)) {
            client.setScreen((Screen)new InventoryScreen((PlayerEntity)client.player));
            delayTicks = Math.max(1, ArmorEquipperUtil.getDelayTicks());
            return false;
        }
        if (client.player.currentScreenHandler == null || client.player.currentScreenHandler != client.player.playerScreenHandler) {
            delayTicks = 1;
            return false;
        }
        if (!ArmorEquipperUtil.clearCursor(client, (PlayerEntity)client.player)) {
            delayTicks = 1;
            return false;
        }
        return true;
    }

    private static boolean clearCursor(MinecraftClient client, PlayerEntity player) {
        ItemStack cursor = player.currentScreenHandler.getCursorStack();
        if (cursor == null || cursor.isEmpty()) {
            return true;
        }
        int empty = player.getInventory().getEmptySlot();
        if (empty < 0) {
            return false;
        }
        int containerSlot = empty < 9 ? empty + 36 : empty;
        client.interactionManager.clickSlot(player.currentScreenHandler.syncId, containerSlot, 0, SlotActionType.PICKUP, player);
        return player.currentScreenHandler.getCursorStack().isEmpty();
    }

    private static int getDelayTicks() {
        int d = GuiClient.CONFIG.armorEquipperDelayTicks;
        if (d < 0) {
            d = 0;
        }
        if (d > 20) {
            d = 20;
        }
        return d;
    }

    private static void swapBestForSlot(PlayerEntity player, EquipmentSlot slot, Item itemA, Item itemB) {
        int containerSlot;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.interactionManager == null) {
            return;
        }
        PlayerInventory inv = player.getInventory();
        ItemStack equipped = player.getEquippedStack(slot);
        int currentRemaining = ArmorEquipperUtil.remainingDurability(equipped, itemA, itemB);
        int bestSlot = -1;
        int bestRemaining = currentRemaining;
        for (int i = 0; i < 36; ++i) {
            int rem;
            ItemStack stack = inv.getStack(i);
            if (!ArmorEquipperUtil.isCandidate(stack, itemA, itemB) || (rem = ArmorEquipperUtil.remaining(stack)) <= bestRemaining) continue;
            bestRemaining = rem;
            bestSlot = i;
        }
        if (bestSlot == -1) {
            return;
        }
        int armorSlotId = -1;
        if (slot == EquipmentSlot.HEAD) {
            armorSlotId = 5;
        } else if (slot == EquipmentSlot.CHEST) {
            armorSlotId = 6;
        } else if (slot == EquipmentSlot.LEGS) {
            armorSlotId = 7;
        } else if (slot == EquipmentSlot.FEET) {
            armorSlotId = 8;
        }
        if (armorSlotId < 0) {
            return;
        }
        int n = containerSlot = bestSlot < 9 ? bestSlot + 36 : bestSlot;
        if (player.currentScreenHandler == null) {
            return;
        }
        int syncId = player.currentScreenHandler.syncId;
        client.interactionManager.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, player);
        client.interactionManager.clickSlot(syncId, armorSlotId, 0, SlotActionType.PICKUP, player);
        client.interactionManager.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, player);
    }

    private static boolean isCandidate(ItemStack stack, Item itemA, Item itemB) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item it = stack.getItem();
        return it == itemA || it == itemB;
    }

    private static int remainingDurability(ItemStack stack, Item itemA, Item itemB) {
        if (stack == null || stack.isEmpty()) {
            return -1;
        }
        Item it = stack.getItem();
        if (it != itemA && it != itemB) {
            return -1;
        }
        return ArmorEquipperUtil.remaining(stack);
    }

    private static int remaining(ItemStack stack) {
        if (!stack.isDamageable()) {
            return 0;
        }
        return Math.max(0, stack.getMaxDamage() - stack.getDamage());
    }
}

