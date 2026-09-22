package me.Gui.gui.modules;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public final class AutoTotemUtil {
    private AutoTotemUtil() {
    }

    public static void tick(MinecraftClient client) {
        if (client == null || client.player == null || client.interactionManager == null) {
            return;
        }
        HackModule mod = GuiClient.MODULES.byId(ModuleId.AUTO_TOTEM);
        if (mod == null || !mod.isEnabled()) {
            return;
        }
        if (client.currentScreen != null && !(client.currentScreen instanceof InventoryScreen)) {
            return;
        }
        ClientPlayerEntity player = client.player;
        if (player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
            return;
        }
        if (player.currentScreenHandler == null || player.currentScreenHandler != player.playerScreenHandler) {
            return;
        }
        if (!player.currentScreenHandler.getCursorStack().isEmpty()) {
            return;
        }
        PlayerInventory inv = player.getInventory();
        int totemSlot = AutoTotemUtil.findTotemSlot(inv);
        if (totemSlot == -1) {
            return;
        }
        int containerSlot = totemSlot < 9 ? totemSlot + 36 : totemSlot;
        int offhandSlot = 45;
        int syncId = player.currentScreenHandler.syncId;
        client.interactionManager.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, (PlayerEntity)player);
        client.interactionManager.clickSlot(syncId, offhandSlot, 0, SlotActionType.PICKUP, (PlayerEntity)player);
        client.interactionManager.clickSlot(syncId, containerSlot, 0, SlotActionType.PICKUP, (PlayerEntity)player);
    }

    private static int findTotemSlot(PlayerInventory inv) {
        for (int i = 0; i < 36; ++i) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty() || !stack.isOf(Items.TOTEM_OF_UNDYING)) continue;
            return i;
        }
        return -1;
    }
}

