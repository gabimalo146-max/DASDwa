package me.Gui.gui.modules;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import me.Gui.gui.client.GuiClient;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import me.Gui.gui.ui.GuiScreen;
import me.Gui.gui.ui.hud.NotificationHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public final class AntyKostkaUtil {
    private static final LinkedList<List<ItemStack>> tickHistory = new LinkedList();
    private static List<ItemStack> savedCleanState = null;
    private static final List<Runnable> actionQueue = new ArrayList<Runnable>();
    private static boolean restoring = false;
    private static int reactionWindowTimer = 0;
    private static int cooldownTimer = 0;
    private static float actionBudget = 0.0f;

    private AntyKostkaUtil() {
    }

    public static void tick(MinecraftClient client) {
        if (client == null || client.player == null) {
            AntyKostkaUtil.reset();
            return;
        }
        if (!AntyKostkaUtil.isEnabled()) {
            AntyKostkaUtil.reset();
            return;
        }
        if (cooldownTimer > 0) {
            --cooldownTimer;
            tickHistory.clear();
        }
        if (!restoring && cooldownTimer <= 0 && client.currentScreen == null) {
            ArrayList<ItemStack> currentHotbar = new ArrayList<ItemStack>();
            for (int i = 0; i < 9; ++i) {
                currentHotbar.add(((ItemStack)client.player.getInventory().main.get(i)).copy());
            }
            tickHistory.addFirst(currentHotbar);
            if (tickHistory.size() > 7) {
                tickHistory.removeLast();
            }
            if (tickHistory.size() == 7) {
                List<ItemStack> old = tickHistory.getLast();
                int diff = 0;
                for (int i = 0; i < 9; ++i) {
                    if (AntyKostkaUtil.stacksMatch((ItemStack)currentHotbar.get(i), old.get(i))) continue;
                    ++diff;
                }
                if (diff >= 3 && reactionWindowTimer <= 0) {
                    savedCleanState = new ArrayList<ItemStack>(old);
                    reactionWindowTimer = 100;
                }
            }
        }
        if (reactionWindowTimer > 0) {
            --reactionWindowTimer;
        }
        if (!actionQueue.isEmpty() && client.currentScreen instanceof InventoryScreen) {
            float speed = AntyKostkaUtil.getSpeedMultiplier();
            int limit = Math.max(1, (int)Math.floor(actionBudget += speed));
            for (int i = 0; i < limit && !actionQueue.isEmpty(); ++i) {
                actionQueue.remove(0).run();
                actionBudget -= 1.0f;
            }
            if (actionQueue.isEmpty()) {
                actionBudget = 0.0f;
                restoring = false;
                cooldownTimer = 25;
                client.execute(() -> {
                    if (client.player != null) {
                        client.player.closeHandledScreen();
                    }
                });
                NotificationHud.postCustomText("Your Hotbar Has Been Fixed");
            }
        } else if (actionQueue.isEmpty()) {
            actionBudget = 0.0f;
        }
    }

    public static void triggerManual(MinecraftClient client) {
        if (client == null || client.player == null) {
            return;
        }
        if (!AntyKostkaUtil.isEnabled()) {
            return;
        }
        if (restoring) {
            return;
        }
        if (reactionWindowTimer <= 0 || savedCleanState == null) {
            return;
        }
        if (client.currentScreen != null) {
            if (client.currentScreen instanceof GuiScreen) {
                client.setScreen(null);
            } else {
                return;
            }
        }
        AntyKostkaUtil.stopAllMovement(client);
        AntyKostkaUtil.executeRestore(client, savedCleanState);
        reactionWindowTimer = 0;
    }

    private static boolean isEnabled() {
        HackModule mod = GuiClient.MODULES.byId(ModuleId.ANTYKOSTKA);
        return mod != null && mod.isEnabled();
    }

    private static void stopAllMovement(MinecraftClient client) {
        if (client.options == null) {
            return;
        }
        client.options.forwardKey.setPressed(false);
        client.options.backKey.setPressed(false);
        client.options.leftKey.setPressed(false);
        client.options.rightKey.setPressed(false);
    }

    private static void executeRestore(MinecraftClient client, List<ItemStack> target) {
        if (target == null || client.interactionManager == null) {
            return;
        }
        restoring = true;
        actionBudget = 0.0f;
        actionQueue.clear();
        client.setScreen((Screen)new InventoryScreen((PlayerEntity)client.player));
        for (int i = 0; i < 9; ++i) {
            int tSlot = i;
            ItemStack goal = target.get(i);
            if (goal.isEmpty()) continue;
            actionQueue.add(() -> {
                int found = -1;
                for (int j = 0; j < client.player.getInventory().size(); ++j) {
                    if (!AntyKostkaUtil.stacksMatch(client.player.getInventory().getStack(j), goal)) continue;
                    found = j;
                    break;
                }
                if (found != -1 && found != tSlot) {
                    int winSlot = found < 9 ? found + 36 : found;
                    client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, winSlot, tSlot, SlotActionType.SWAP, (PlayerEntity)client.player);
                }
            });
        }
    }

    private static void reset() {
        restoring = false;
        reactionWindowTimer = 0;
        cooldownTimer = 0;
        actionBudget = 0.0f;
        tickHistory.clear();
        actionQueue.clear();
        savedCleanState = null;
    }

    private static float getSpeedMultiplier() {
        float v = GuiClient.CONFIG.antyKostkaSpeed;
        if (Float.isNaN(v) || Float.isInfinite(v)) {
            v = 0.0f;
        }
        if (v < 0.0f) {
            v = 0.0f;
        }
        if (v > 1.0f) {
            v = 1.0f;
        }
        return 1.0f + v;
    }

    private static boolean stacksMatch(ItemStack a, ItemStack b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.getCount() != b.getCount()) {
            return false;
        }
        return ItemStack.areItemsAndComponentsEqual((ItemStack)a, (ItemStack)b);
    }
}

