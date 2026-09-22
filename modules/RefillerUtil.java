package me.Gui.gui.modules;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public final class RefillerUtil {
    private static int tickCounter = 0;
    private static int pendingSlot = -1;

    private RefillerUtil() {
    }

    public static void tick(MinecraftClient client) {
        int currentSlot;
        PlayerInventory inv;
        ItemStack heldStack;
        if (client == null) {
            return;
        }
        HackModule mod = GuiClient.MODULES.byId(ModuleId.REFILLER);
        if (mod == null || !mod.isEnabled()) {
            RefillerUtil.resetState();
            return;
        }
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return;
        }
        if (client.currentScreen == null && tickCounter == 0 && RefillerUtil.needsRefill(heldStack = (inv = player.getInventory()).getStack(currentSlot = inv.selectedSlot)) && RefillerUtil.hasBackup(inv, heldStack)) {
            client.setScreen((Screen)new InventoryScreen((PlayerEntity)player));
            pendingSlot = currentSlot;
            tickCounter = 3;
        }
        if (tickCounter > 0 && --tickCounter == 0 && pendingSlot != -1 && client.currentScreen instanceof InventoryScreen) {
            RefillerUtil.fillToMax(client, player, pendingSlot);
            client.setScreen(null);
            player.closeHandledScreen();
            pendingSlot = -1;
        }
    }

    private static void resetState() {
        tickCounter = 0;
        pendingSlot = -1;
    }

    private static boolean needsRefill(ItemStack stack) {
        boolean pearls;
        if (stack.isEmpty() || stack.getCount() >= stack.getMaxCount()) {
            return false;
        }
        boolean obsidian = GuiClient.CONFIG.refillerObsidian == null || GuiClient.CONFIG.refillerObsidian != false;
        boolean cobwebs = GuiClient.CONFIG.refillerCobwebs == null || GuiClient.CONFIG.refillerCobwebs != false;
        boolean bl = pearls = GuiClient.CONFIG.refillerPearls == null || GuiClient.CONFIG.refillerPearls != false;
        if (stack.isOf(Items.OBSIDIAN)) {
            if (!obsidian) {
                return false;
            }
            return stack.getCount() <= 10;
        }
        if (stack.isOf(Items.COBWEB)) {
            if (!cobwebs) {
                return false;
            }
            return stack.getCount() <= 10;
        }
        if (stack.isOf(Items.ENDER_PEARL)) {
            if (!pearls) {
                return false;
            }
            return stack.getCount() <= 2;
        }
        return false;
    }

    private static boolean hasBackup(PlayerInventory inv, ItemStack heldStack) {
        for (int i = 9; i < 36; ++i) {
            ItemStack invStack = inv.getStack(i);
            if (invStack.isEmpty() || !invStack.isOf(heldStack.getItem())) continue;
            return true;
        }
        return false;
    }

    private static void fillToMax(MinecraftClient client, ClientPlayerEntity player, int targetSlot) {
        if (client.interactionManager == null) {
            return;
        }
        PlayerInventory inv = player.getInventory();
        ItemStack heldItem = inv.getStack(targetSlot);
        for (int i = 9; i < 36; ++i) {
            ItemStack invStack = inv.getStack(i);
            if (invStack.isEmpty() || !invStack.isOf(heldItem.getItem())) continue;
            client.interactionManager.clickSlot(player.playerScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)player);
            ItemStack updatedStack = player.getInventory().getStack(targetSlot);
            if (updatedStack.getCount() >= updatedStack.getMaxCount()) break;
        }
    }
}

