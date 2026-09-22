package me.Gui.gui.modules;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.client.sound.SoundUtil;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public final class AutoParawanUtil {
    private static final double RANGE = 5.0;
    private static final double RANGE_SQ = 25.0;
    private static boolean hadThreat = false;

    private AutoParawanUtil() {
    }

    public static void tick(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) {
            AutoParawanUtil.reset();
            return;
        }
        HackModule mod = GuiClient.MODULES.byId(ModuleId.AUTO_PARAWAN);
        if (mod == null || !mod.isEnabled()) {
            AutoParawanUtil.reset();
            return;
        }
        if (client.currentScreen != null) {
            AutoParawanUtil.reset();
            return;
        }
        boolean threat = AutoParawanUtil.hasThreatNearby(client);
        if (!threat) {
            hadThreat = false;
            return;
        }
        if (hadThreat) {
            return;
        }
        if (AutoParawanUtil.useParawan(client)) {
            SoundUtil.playAutoParawan();
        }
        hadThreat = true;
    }

    private static void reset() {
        hadThreat = false;
    }

    private static boolean hasThreatNearby(MinecraftClient client) {
        ClientPlayerEntity self = client.player;
        if (self == null) {
            return false;
        }
        boolean ignoreFriends = GuiClient.CONFIG.autoParawanDontAttackFriends == null || GuiClient.CONFIG.autoParawanDontAttackFriends != false;
        for (PlayerEntity p : client.world.getPlayers()) {
            if (p == null || p == self || !p.isAlive() || p.isSpectator() || ignoreFriends && GuiClient.FRIENDS.isFriend(p.getName().getString()) || !(self.squaredDistanceTo((Entity)p) <= 25.0)) continue;
            return true;
        }
        return false;
    }

    private static boolean useParawan(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.interactionManager == null) {
            return false;
        }
        if (player.currentScreenHandler == null || player.currentScreenHandler != player.playerScreenHandler) {
            return false;
        }
        if (!player.currentScreenHandler.getCursorStack().isEmpty()) {
            return false;
        }
        int parawanSlot = AutoParawanUtil.findParawanSlot(player);
        if (parawanSlot == -1) {
            return false;
        }
        int originalSlot = player.getInventory().selectedSlot;
        if (parawanSlot < 9) {
            player.getInventory().selectedSlot = parawanSlot;
            client.interactionManager.interactItem((PlayerEntity)player, Hand.MAIN_HAND);
            player.swingHand(Hand.MAIN_HAND);
            player.getInventory().selectedSlot = originalSlot;
            return true;
        }
        int containerSlot = parawanSlot < 9 ? parawanSlot + 36 : parawanSlot;
        client.interactionManager.clickSlot(player.playerScreenHandler.syncId, containerSlot, originalSlot, SlotActionType.SWAP, (PlayerEntity)player);
        client.interactionManager.interactItem((PlayerEntity)player, Hand.MAIN_HAND);
        player.swingHand(Hand.MAIN_HAND);
        client.interactionManager.clickSlot(player.playerScreenHandler.syncId, containerSlot, originalSlot, SlotActionType.SWAP, (PlayerEntity)player);
        player.getInventory().selectedSlot = originalSlot;
        return true;
    }

    private static int findParawanSlot(ClientPlayerEntity player) {
        for (int i = 0; i < 36; ++i) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!AutoParawanUtil.isParawan(stack)) continue;
            return i;
        }
        return -1;
    }

    private static boolean isParawan(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (!stack.isOf(Items.FEATHER)) {
            return false;
        }
        String name = stack.getName().getString();
        if (name == null) {
            return false;
        }
        return name.trim().equalsIgnoreCase("Parawan");
    }
}

