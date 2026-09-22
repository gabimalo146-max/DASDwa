package me.Gui.gui.modules;

import java.io.File;
import java.text.Normalizer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.Gui.gui.client.GuiClient;
import me.Gui.gui.mixin.MinecraftClientAccessor;
import me.Gui.gui.modules.NameProtectUtil;
import me.Gui.gui.ui.GuiScreen;
import me.Gui.gui.ui.hud.NotificationHud;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class GearRenderUtil {
    public static final long REFRESH_MS = 2000L;
    private static final long AUTO_TPA_DELAY_MS = 1000L;
    private static final long AUTO_TPA_APPEAR_DELAY_MS = 1000L;
    private static final long ACCEPT_WINDOW_MS = 30000L;
    private static final long TELEPORT_COOLDOWN_MS = 1500L;
    private static final long AUTO_JUMP_INTERVAL_MS = 60000L;
    private static final double TELEPORT_DIST_SQ = 4096.0;
    private static final double AUTO_LOGOUT_MIN_DIST_SQ = 10000.0;
    private static final double AUTO_LOGOUT_TELEPORT_MIN_DIST_SQ = 16.0;
    private static final double AUTO_TPA_TARGET_RADIUS_SQ = 144.0;
    private static final long AUTO_TPA_TARGET_GRACE_MS = 30000L;
    private static final long AUTO_LOGOUT_ACTIVE_MS = 30000L;
    private static final double GOOD_ITEM_RADIUS_SQ = 64.0;
    private static final double BIG_TELEPORT_DIST_SQ = 90000.0;
    private static final double TELEPORT_ALERT_RADIUS_SQ = 100.0;
    private static final int TELEPORT_ALERT_MAX_PLAYERS = 4;
    private static final long AUTO_CH_INTERVAL_MS = 120000L;
    private static final long LIST_RESET_MS = 30000L;
    private static final long CH_GUI_TIMEOUT_MS = 6000L;
    private static final long CH_REOPEN_DELAY_MS = 500L;
    private static final long CH_REOPEN_RETRY_MS = 500L;
    private static final long CH_REOPEN_KEYBIND_DELAY_MS = 2000L;
    private static final long AUTO_CH_CLICK_DELAY_MS = 50L;
    private static final long AUTO_CH_REOPEN_DELAY_MS = 2000L;
    private static final long AUTO_LOGOUT_GRACE_MS = 1000L;
    private static final long CH_FORCE_CLOSE_AFTER_MS = 2000L;
    private static final long TELEPORT_CLOSE_GRACE_MS = 1000L;
    private static final int CH_REOPEN_MAX_TRIES = 3;
    private static final double AUTO_TPA_EXTRA_DAMAGE_LIMIT = 49.0;
    private static final Random AUTO_CH_RNG = new Random();
    private static final Map<UUID, SeenInfo> seen = new HashMap<UUID, SeenInfo>();
    private static final Map<UUID, MissingEntry> missingById = new LinkedHashMap<UUID, MissingEntry>();
    private static final Map<UUID, Long> missingFirstSeenAt = new HashMap<UUID, Long>();
    private static final List<GearEntry> inRange = new ArrayList<GearEntry>();
    private static final List<MissingEntry> missing = new ArrayList<MissingEntry>();
    private static long lastRefresh = 0L;
    private static int lastVisibleCount = 0;
    private static int lastPercent = 0;
    private static String lastServerAddress = null;
    private static final ArrayDeque<UUID> autoTpaQueue = new ArrayDeque();
    private static final Set<UUID> autoTpaQueued = new HashSet<UUID>();
    private static final Set<UUID> autoTpaHandled = new HashSet<UUID>();
    private static long lastAutoTpaAt = 0L;
    private static long lastAutoTpaSentAt = 0L;
    private static String lastAutoTpaTargetName = null;
    private static long lastAutoTpaTargetAt = 0L;
    private static long autoLogoutArmedUntil = 0L;
    private static boolean lastAutoTpaEnabled = false;
    private static long lastAutoJumpAt = 0L;
    private static double lastPlayerX = 0.0;
    private static double lastPlayerY = 0.0;
    private static double lastPlayerZ = 0.0;
    private static boolean hasLastPlayerPos = false;
    private static long autoLogoutSampleAt = 0L;
    private static double autoLogoutSampleX = 0.0;
    private static double autoLogoutSampleY = 0.0;
    private static double autoLogoutSampleZ = 0.0;
    private static long lastTeleportHandledAt = 0L;
    private static double teleportPacketPreX = 0.0;
    private static double teleportPacketPreY = 0.0;
    private static double teleportPacketPreZ = 0.0;
    private static long teleportPacketPreAt = 0L;
    private static final String EXTRA_DAMAGE_LABEL = "dodatkowe obrazenia";
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[-+]?\\d+(?:[\\.,]\\d+)?");
    private static boolean pendingReopen = false;
    private static boolean waitingServerGui = false;
    private static long pendingSince = 0L;
    private static long pendingReopenAt = 0L;
    private static int pendingReopenTries = 0;
    private static boolean pendingReopenKeybind = false;
    private static long pendingReopenKeybindAt = 0L;
    private static long autoLogoutGraceUntil = 0L;
    private static long teleportCloseGraceUntil = 0L;
    private static boolean pendingCloseReset = false;
    private static long autoChClickedAt = 0L;
    private static boolean autoChPending = false;
    private static long autoChRequestedAt = 0L;
    private static long lastAutoChAt = 0L;
    private static boolean lastAutoChEnabled = false;
    private static boolean pendingChReset = false;
    private static boolean lastGearRenderOpen = false;

    private GearRenderUtil() {
    }

    public static void ensureRefreshed(MinecraftClient client) {
        long now = System.currentTimeMillis();
        if (now - lastRefresh < 2000L) {
            return;
        }
        GearRenderUtil.refresh(client, now);
    }

    public static List<GearEntry> getInRange() {
        return inRange;
    }

    public static List<MissingEntry> getMissing() {
        return missing;
    }

    public static int getInRangePercent() {
        return lastPercent;
    }

    public static int getVisibleCount() {
        return lastVisibleCount;
    }

    public static void requestTpa(MinecraftClient client, String name) {
        if (client == null || client.getNetworkHandler() == null || name == null || name.isBlank()) {
            return;
        }
        client.getNetworkHandler().sendChatCommand("tpa " + name);
    }

    public static void requestStats(MinecraftClient client, String name) {
        if (client == null || client.getNetworkHandler() == null || name == null || name.isBlank()) {
            return;
        }
        client.getNetworkHandler().sendChatCommand("stats " + name);
        pendingReopen = true;
        waitingServerGui = true;
        pendingSince = System.currentTimeMillis();
        pendingReopenAt = 0L;
        pendingReopenTries = 0;
        pendingReopenKeybind = false;
        pendingReopenKeybindAt = 0L;
    }

    public static void requestChangeCh(MinecraftClient client) {
        if (client == null || client.getNetworkHandler() == null) {
            return;
        }
        client.getNetworkHandler().sendChatCommand("ch");
    }

    public static void triggerAutoChangeCh(MinecraftClient client) {
    }

    private static void requestChangeChInternal(MinecraftClient client, boolean autoClick, boolean manual) {
        Screen openScreen;
        if (client == null || client.player == null || client.getNetworkHandler() == null) {
            return;
        }
        if (!NameProtectUtil.isOnAnarchiaServer()) {
            if (manual) {
                GearRenderUtil.notifyChBlocked(client, "Dziala tylko na anarchia.gg");
            }
            return;
        }
        if (autoChPending) {
            if (!manual) {
                return;
            }
            autoChPending = false;
            autoChRequestedAt = 0L;
        }
        if ((openScreen = client.currentScreen) != null) {
            if (openScreen instanceof HandledScreen && client.player != null) {
                client.player.closeHandledScreen();
            }
            client.setScreen(null);
        }
        client.getNetworkHandler().sendChatCommand("ch");
        long now = System.currentTimeMillis();
        pendingReopen = true;
        waitingServerGui = true;
        pendingSince = now;
        pendingReopenAt = 0L;
        pendingReopenTries = 0;
        pendingReopenKeybind = true;
        pendingReopenKeybindAt = now + 2000L;
        pendingChReset = true;
        if (autoClick) {
            autoChPending = true;
            autoChRequestedAt = now;
            lastAutoChAt = now;
        }
    }

    public static void clearMissing() {
        missingById.clear();
        missingFirstSeenAt.clear();
        missing.clear();
        for (SeenInfo info : seen.values()) {
            if (info == null) continue;
            info.nearSponge = false;
        }
        GearRenderUtil.resetAutoTpaState();
    }

    public static void resetForChannelChange() {
        seen.clear();
        missingById.clear();
        missingFirstSeenAt.clear();
        inRange.clear();
        missing.clear();
        lastVisibleCount = 0;
        lastPercent = 0;
        lastRefresh = 0L;
        autoLogoutSampleAt = 0L;
        if (GearRenderUtil.isAutoLogoutTargetActive(System.currentTimeMillis())) {
            GearRenderUtil.resetAutoTpaQueueStateKeepTarget();
        } else {
            GearRenderUtil.resetAutoTpaState();
        }
    }

    private static void resetListOnly() {
        seen.clear();
        missingById.clear();
        missingFirstSeenAt.clear();
        inRange.clear();
        missing.clear();
        lastVisibleCount = 0;
        lastPercent = 0;
        lastRefresh = 0L;
        autoLogoutSampleAt = 0L;
        autoTpaQueue.clear();
        autoTpaQueued.clear();
        autoTpaHandled.clear();
        lastAutoTpaAt = 0L;
        lastAutoTpaSentAt = 0L;
    }

    public static void onTeleportPacketPre(MinecraftClient client) {
        if (client == null || client.player == null) {
            teleportPacketPreAt = 0L;
            return;
        }
        teleportPacketPreX = client.player.getX();
        teleportPacketPreY = client.player.getY();
        teleportPacketPreZ = client.player.getZ();
        teleportPacketPreAt = System.currentTimeMillis();
    }

    public static void onTeleportPacketPost(MinecraftClient client) {
        if (client == null || client.player == null) {
            return;
        }
        if (teleportPacketPreAt <= 0L) {
            return;
        }
        long now = System.currentTimeMillis();
        if (!GuiScreen.isGearRenderOpen(client.currentScreen) && lastGearRenderOpen) {
            teleportCloseGraceUntil = Math.max(teleportCloseGraceUntil, now + 1000L);
            pendingCloseReset = true;
        }
        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();
        double dx = x - teleportPacketPreX;
        double dy = y - teleportPacketPreY;
        double dz = z - teleportPacketPreZ;
        double distSq = dx * dx + dy * dy + dz * dz;
        teleportPacketPreAt = 0L;
        if (GearRenderUtil.checkAutoLogoutCoordChange(client, now)) {
            lastTeleportHandledAt = now;
            return;
        }
        if (autoLogoutGraceUntil > now && GearRenderUtil.isAutoLogoutTargetActive(now) && distSq >= 16.0) {
            PlayerEntity target = GearRenderUtil.findAutoLogoutTarget(client, lastAutoTpaTargetName);
            List<PlayerEntity> near = GearRenderUtil.collectNearbyPlayers(client, (PlayerEntity)client.player, 100.0);
            if (near.isEmpty() && target != null) {
                near = List.of(target);
            }
            String msg = GearRenderUtil.buildTeleportAlertMessage(near, GearRenderUtil.formatCoords((PlayerEntity)client.player));
            NotificationHud.postCustomText(msg);
            GearRenderUtil.takeTeleportScreenshot(client);
            GearRenderUtil.forceLogout(client, msg);
            GearRenderUtil.resetAutoTpaState();
            return;
        }
        if (distSq < 4096.0) {
            lastPlayerX = x;
            lastPlayerY = y;
            lastPlayerZ = z;
            hasLastPlayerPos = true;
            return;
        }
        if (now - lastTeleportHandledAt < 1500L) {
            lastPlayerX = x;
            lastPlayerY = y;
            lastPlayerZ = z;
            hasLastPlayerPos = true;
            return;
        }
        lastPlayerX = teleportPacketPreX;
        lastPlayerY = teleportPacketPreY;
        lastPlayerZ = teleportPacketPreZ;
        hasLastPlayerPos = true;
        GearRenderUtil.handleTeleportDetect(client);
    }

    public static void tick(MinecraftClient client) {
        boolean allowTeleportDetect;
        PlayerEntity target;
        boolean open;
        long now = System.currentTimeMillis();
        boolean bl = open = client != null && GuiScreen.isGearRenderOpen(client.currentScreen);
        if (lastAutoTpaTargetAt > 0L && now - lastAutoTpaTargetAt > 30000L) {
            lastAutoTpaTargetAt = 0L;
            lastAutoTpaTargetName = null;
        }
        if (GearRenderUtil.checkAutoLogoutCoordChange(client, now)) {
            return;
        }
        if (autoLogoutGraceUntil > now && GearRenderUtil.isAutoLogoutTargetActive(now) && (target = GearRenderUtil.findAutoLogoutTarget(client, lastAutoTpaTargetName)) != null) {
            List<PlayerEntity> near = GearRenderUtil.collectNearbyPlayers(client, (PlayerEntity)client.player, 100.0);
            if (near.isEmpty()) {
                near = List.of(target);
            }
            String msg = GearRenderUtil.buildTeleportAlertMessage(near, GearRenderUtil.formatCoords((PlayerEntity)client.player));
            NotificationHud.postCustomText(msg);
            GearRenderUtil.takeTeleportScreenshot(client);
            GearRenderUtil.forceLogout(client, msg);
            GearRenderUtil.resetAutoTpaState();
            return;
        }
        if (open && !lastGearRenderOpen) {
            lastAutoJumpAt = now;
            lastAutoChAt = now;
            autoLogoutGraceUntil = 0L;
            teleportCloseGraceUntil = 0L;
            pendingCloseReset = false;
            autoLogoutSampleAt = 0L;
        }
        if (!open && lastGearRenderOpen) {
            if (GearRenderUtil.isAutoLogoutEnabled()) {
                autoLogoutGraceUntil = now + 1000L;
                if (client != null && client.player != null) {
                    autoLogoutSampleAt = now;
                    autoLogoutSampleX = client.player.getX();
                    autoLogoutSampleY = client.player.getY();
                    autoLogoutSampleZ = client.player.getZ();
                } else {
                    autoLogoutSampleAt = 0L;
                }
            }
            pendingCloseReset = true;
            teleportCloseGraceUntil = now + 1000L;
        }
        long closeResetAt = Math.max(autoLogoutGraceUntil, teleportCloseGraceUntil);
        if (!open && pendingCloseReset && closeResetAt > 0L && now >= closeResetAt) {
            GearRenderUtil.resetForChannelChange();
            pendingCloseReset = false;
            autoLogoutGraceUntil = 0L;
            teleportCloseGraceUntil = 0L;
        }
        lastGearRenderOpen = open;
        boolean bl2 = allowTeleportDetect = GearRenderUtil.isAutoLogoutEnabled() || open || autoLogoutGraceUntil > now || teleportCloseGraceUntil > now;
        if (allowTeleportDetect && (GearRenderUtil.isAutoTpaEnabled() || GearRenderUtil.isAutoLogoutEnabled())) {
            GearRenderUtil.handleTeleportDetect(client);
        }
        GearRenderUtil.tickAutoCh(client);
        if (pendingReopen && client != null) {
            Screen screen = client.currentScreen;
            if (GuiScreen.isGearRenderOpen(screen)) {
                pendingReopen = false;
                pendingReopenAt = 0L;
                pendingReopenTries = 0;
                pendingReopenKeybind = false;
                pendingReopenKeybindAt = 0L;
                autoChClickedAt = 0L;
                return;
            }
            if (pendingReopenAt <= 0L) {
                long l = pendingReopenAt = pendingChReset ? now + 500L : now;
            }
            if (now < pendingReopenAt) {
                return;
            }
            if (pendingChReset) {
                GearRenderUtil.resetForChannelChange();
                pendingChReset = false;
            }
            if (autoChPending && autoChClickedAt <= 0L && autoChRequestedAt > 0L && now - autoChRequestedAt < 6000L) {
                return;
            }
            if (client.player != null && screen instanceof HandledScreen) {
                if (autoChPending) {
                    return;
                }
                if (autoChClickedAt > 0L && now - autoChClickedAt < 2000L) {
                    return;
                }
                if (autoChClickedAt <= 0L && now - pendingSince < 6000L) {
                    return;
                }
                client.player.closeHandledScreen();
                client.setScreen(null);
                screen = null;
            } else if (screen != null) {
                client.setScreen(null);
                screen = null;
            }
            GuiScreen.openGearRenderViaBind();
            if (!GuiScreen.isGearRenderOpen(client.currentScreen)) {
                if (pendingReopenTries < 2) {
                    ++pendingReopenTries;
                    pendingReopenAt = now + 500L;
                    return;
                }
                if (pendingReopenKeybind && pendingReopenKeybindAt > 0L && now < pendingReopenKeybindAt) {
                    pendingReopenAt = pendingReopenKeybindAt;
                    return;
                }
            }
            pendingReopen = false;
            pendingReopenAt = 0L;
            pendingReopenTries = 0;
            pendingReopenKeybind = false;
            pendingReopenKeybindAt = 0L;
            autoChClickedAt = 0L;
        }
        GearRenderUtil.tickAutoTpa(client);
    }

    private static void tickAutoTpa(MinecraftClient client) {
        boolean enabled = GearRenderUtil.isAutoTpaEnabled();
        if (!enabled) {
            if (lastAutoTpaEnabled) {
                if (GearRenderUtil.isAutoLogoutEnabled()) {
                    GearRenderUtil.resetAutoTpaQueueState();
                } else {
                    GearRenderUtil.resetAutoTpaState();
                }
            }
            lastAutoTpaEnabled = false;
            return;
        }
        if (!lastAutoTpaEnabled) {
            lastAutoJumpAt = System.currentTimeMillis();
        }
        lastAutoTpaEnabled = true;
        if (client == null || client.player == null || client.world == null) {
            GearRenderUtil.resetAutoTpaState();
            return;
        }
        if (!GuiScreen.isGearRenderOpen(client.currentScreen)) {
            if (GearRenderUtil.isAutoLogoutGraceActive()) {
                return;
            }
            if (GearRenderUtil.isAutoLogoutEnabled()) {
                GearRenderUtil.resetAutoTpaQueueStateKeepTarget();
                return;
            }
            GearRenderUtil.resetAutoTpaState();
            return;
        }
        if (!NameProtectUtil.isOnAnarchiaServer()) {
            GearRenderUtil.resetAutoTpaState();
            return;
        }
        GearRenderUtil.ensureRefreshed(client);
        GearRenderUtil.tickAutoJump(client);
        long now = System.currentTimeMillis();
        if (now - lastAutoTpaAt < 1000L) {
            return;
        }
        if (autoTpaQueue.isEmpty()) {
            return;
        }
        int tries = autoTpaQueue.size();
        for (int i = 0; i < tries; ++i) {
            UUID nextId = autoTpaQueue.poll();
            if (nextId == null) {
                return;
            }
            MissingEntry entry = missingById.get(nextId);
            if (entry == null || entry.name == null || entry.name.isBlank()) {
                autoTpaQueued.remove(nextId);
                continue;
            }
            if (GearRenderUtil.isBlacklistedName(entry.name)) {
                autoTpaQueued.remove(nextId);
                continue;
            }
            if (!GearRenderUtil.isMissingReadyForAutoTpa(nextId, now)) {
                autoTpaQueue.add(nextId);
                continue;
            }
            if (GearRenderUtil.isExtraDamageOverLimit(entry.sawSword, entry.swordExtra)) {
                autoTpaQueued.remove(nextId);
                autoTpaHandled.add(nextId);
                lastAutoTpaAt = now;
                return;
            }
            GearRenderUtil.requestTpa(client, entry.name);
            autoTpaQueued.remove(nextId);
            autoTpaHandled.add(nextId);
            lastAutoTpaAt = now;
            lastAutoTpaSentAt = now;
            lastAutoTpaTargetName = GearRenderUtil.normalizeNameSimple(entry.name);
            lastAutoTpaTargetAt = now;
            return;
        }
    }

    private static void tickAutoCh(MinecraftClient client) {
        boolean enabled = GearRenderUtil.isAutoTpaEnabled();
        if (!enabled) {
            if (lastAutoChEnabled) {
                GearRenderUtil.resetAutoChState();
            }
            return;
        }
        long now = System.currentTimeMillis();
        if (!lastAutoChEnabled) {
            lastAutoChAt = now;
        }
        lastAutoChEnabled = true;
        if (client == null || client.player == null || client.getNetworkHandler() == null) {
            return;
        }
        if (!NameProtectUtil.isOnAnarchiaServer()) {
            lastAutoChAt = now;
            return;
        }
        if (autoChPending) {
            if (autoChRequestedAt > 0L && now - autoChRequestedAt >= 6000L) {
                autoChPending = false;
                autoChRequestedAt = 0L;
                waitingServerGui = false;
                if (client.currentScreen instanceof HandledScreen && client.player != null) {
                    client.player.closeHandledScreen();
                }
                if (client.currentScreen != null) {
                    client.setScreen(null);
                }
                pendingReopen = true;
                pendingReopenAt = now + 2000L;
                pendingReopenTries = 0;
                pendingReopenKeybind = true;
                pendingReopenKeybindAt = pendingReopenAt;
                return;
            }
            if (client.currentScreen instanceof HandledScreen) {
                long sinceReq;
                long l = sinceReq = autoChRequestedAt > 0L ? now - autoChRequestedAt : 50L;
                if (sinceReq >= 50L && GearRenderUtil.tryClickRandomChFrame(client)) {
                    autoChPending = false;
                    autoChRequestedAt = 0L;
                    autoChClickedAt = now;
                    if (client.player != null) {
                        client.player.closeHandledScreen();
                    }
                    client.setScreen(null);
                    pendingReopen = true;
                    pendingReopenAt = now + 2000L;
                    pendingReopenTries = 0;
                    pendingReopenKeybind = true;
                    pendingReopenKeybindAt = pendingReopenAt;
                    return;
                }
            }
            return;
        }
        if (pendingReopen || waitingServerGui) {
            return;
        }
        if (!GuiScreen.isGearRenderOpen(client.currentScreen)) {
            return;
        }
        if (now - lastAutoChAt < 120000L) {
            return;
        }
        GearRenderUtil.requestChangeChInternal(client, true, false);
    }

    private static void tickAutoJump(MinecraftClient client) {
        if (client == null || client.player == null) {
            lastAutoJumpAt = 0L;
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastAutoJumpAt < 60000L) {
            return;
        }
        if (!client.player.isOnGround()) {
            return;
        }
        client.player.jump();
        lastAutoJumpAt = now;
    }

    private static boolean checkAutoLogoutCoordChange(MinecraftClient client, long now) {
        double dz;
        double dy;
        if (!GearRenderUtil.isAutoLogoutEnabled()) {
            autoLogoutSampleAt = 0L;
            return false;
        }
        if (autoLogoutGraceUntil <= now) {
            autoLogoutSampleAt = 0L;
            return false;
        }
        if (client == null || client.player == null) {
            autoLogoutSampleAt = 0L;
            return false;
        }
        if (autoLogoutSampleAt <= 0L) {
            autoLogoutSampleAt = now;
            autoLogoutSampleX = client.player.getX();
            autoLogoutSampleY = client.player.getY();
            autoLogoutSampleZ = client.player.getZ();
            return false;
        }
        double dx = client.player.getX() - autoLogoutSampleX;
        double distSq = dx * dx + (dy = client.player.getY() - autoLogoutSampleY) * dy + (dz = client.player.getZ() - autoLogoutSampleZ) * dz;
        if (distSq >= 10000.0) {
            GearRenderUtil.resetListOnly();
            GearRenderUtil.resetAutoTpaState();
            GearRenderUtil.forceLogout(client, "Auto logout");
            autoLogoutSampleAt = 0L;
            return true;
        }
        return false;
    }

    private static void handleTeleportDetect(MinecraftClient client) {
        if (client == null || client.player == null) {
            hasLastPlayerPos = false;
            return;
        }
        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();
        lastPlayerX = x;
        lastPlayerY = y;
        lastPlayerZ = z;
        hasLastPlayerPos = true;
    }

    private static List<PlayerEntity> collectNearbyPlayers(MinecraftClient client, PlayerEntity self, double radiusSq) {
        ArrayList<PlayerEntity> out = new ArrayList<PlayerEntity>();
        if (client == null || client.world == null || self == null) {
            return out;
        }
        for (PlayerEntity p : client.world.getPlayers()) {
            double distSq;
            if (p == null || p == self || !((distSq = p.squaredDistanceTo((Entity)self)) <= radiusSq)) continue;
            out.add(p);
        }
        out.sort(Comparator.comparingDouble(arg_0 -> ((PlayerEntity)self).squaredDistanceTo(arg_0)));
        return out;
    }

    private static PlayerEntity findAutoLogoutTarget(MinecraftClient client, String targetName) {
        if (client == null || client.world == null || targetName == null || targetName.isBlank()) {
            return null;
        }
        ClientPlayerEntity self = client.player;
        String key = GearRenderUtil.normalizeNameSimple(targetName);
        for (PlayerEntity p : client.world.getPlayers()) {
            String name;
            if (p == null || p == self || !GearRenderUtil.normalizeNameSimple(name = p.getName().getString()).equals(key)) continue;
            return p;
        }
        return null;
    }

    private static PlayerEntity findAutoTpaTargetNearby(MinecraftClient client, PlayerEntity self, long now) {
        if (client == null || client.world == null || self == null) {
            return null;
        }
        if (lastAutoTpaTargetAt <= 0L) {
            return null;
        }
        if (now - lastAutoTpaTargetAt > 30000L) {
            return null;
        }
        if (lastAutoTpaTargetName == null || lastAutoTpaTargetName.isBlank()) {
            return null;
        }
        for (PlayerEntity p : client.world.getPlayers()) {
            String name;
            if (p == null || p == self || p.squaredDistanceTo((Entity)self) > 144.0 || !GearRenderUtil.normalizeNameSimple(name = p.getName().getString()).equals(lastAutoTpaTargetName)) continue;
            return p;
        }
        return null;
    }

    private static String buildTeleportAlertMessage(List<PlayerEntity> players, String coords) {
        PlayerEntity p;
        int i;
        int total;
        String coordLabel = coords == null || coords.isBlank() ? "kordy: -" : coords;
        int n = total = players == null ? 0 : players.size();
        if (total <= 0) {
            return "Ilosc graczy: 0 | Nicki: - | " + coordLabel;
        }
        int shown = Math.min(total, 4);
        StringBuilder sb = new StringBuilder();
        sb.append("Ilosc graczy: ").append(total);
        sb.append(" | Nicki: ");
        for (i = 0; i < shown; ++i) {
            p = players.get(i);
            if (p == null) continue;
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(p.getName().getString());
        }
        if (total > shown) {
            sb.append(" +").append(total - shown);
        }
        for (i = 0; i < shown; ++i) {
            p = players.get(i);
            if (p == null) continue;
            sb.append(" | ").append(i + 1).append(" ").append(p.getName().getString()).append(": ");
            sb.append(GearRenderUtil.formatArmorSet(p)).append(" | ").append(GearRenderUtil.formatHandItem(p));
        }
        sb.append(" | ").append(coordLabel);
        return sb.toString();
    }

    private static boolean shouldStartAutoLogoutGrace(long now) {
        if (!GearRenderUtil.isAutoLogoutEnabled() || !GearRenderUtil.isAutoTpaEnabled()) {
            return false;
        }
        if (lastAutoTpaSentAt <= 0L) {
            return false;
        }
        return now - lastAutoTpaSentAt <= 30000L;
    }

    private static boolean isAutoLogoutGraceActive() {
        return autoLogoutGraceUntil > System.currentTimeMillis();
    }

    private static boolean tryClickRandomChFrame(MinecraftClient client) {
        if (client == null || client.player == null || client.interactionManager == null) {
            return false;
        }
        Screen screen = client.currentScreen;
        if (!(screen instanceof HandledScreen)) {
            return false;
        }
        HandledScreen handled = (HandledScreen)screen;
        ScreenHandler handler = handled.getScreenHandler();
        if (handler == null) {
            return false;
        }
        ItemStack cursor = handler.getCursorStack();
        if (cursor != null && !cursor.isEmpty()) {
            return false;
        }
        ArrayList<Integer> candidates = new ArrayList<Integer>();
        for (int i = 0; i < handler.slots.size(); ++i) {
            ItemStack stack;
            Slot slot = (Slot)handler.slots.get(i);
            if (slot == null || slot.inventory == client.player.getInventory() || (stack = slot.getStack()) == null || stack.isEmpty() || !GearRenderUtil.isChSlotCandidate(stack)) continue;
            candidates.add(i);
        }
        if (candidates.size() < 2) {
            return false;
        }
        int pick = (Integer)candidates.get(AUTO_CH_RNG.nextInt(candidates.size()));
        client.interactionManager.clickSlot(handler.syncId, pick, 0, SlotActionType.PICKUP, (PlayerEntity)client.player);
        waitingServerGui = false;
        pendingReopenAt = System.currentTimeMillis() + 500L;
        GearRenderUtil.resetForChannelChange();
        autoChClickedAt = System.currentTimeMillis();
        return true;
    }

    private static boolean isChSlotCandidate(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        if (item == Items.BARRIER) {
            return false;
        }
        if (item == Items.ARROW || item == Items.SPECTRAL_ARROW) {
            return false;
        }
        if (item == Items.ITEM_FRAME || item == Items.GLOW_ITEM_FRAME) {
            return true;
        }
        if (item == Items.PAPER || item == Items.MAP || item == Items.FILLED_MAP) {
            return true;
        }
        int count = stack.getCount();
        if (count >= 1 && count <= 9) {
            return true;
        }
        String name = stack.getName().getString();
        if (name == null || name.isBlank()) {
            return false;
        }
        for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            if (c < '0' || c > '9') continue;
            return true;
        }
        return false;
    }

    private static PlayerEntity findNearbyGoodPlayer(MinecraftClient client, PlayerEntity self) {
        if (client == null || client.world == null || self == null) {
            return null;
        }
        PlayerEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (PlayerEntity p : client.world.getPlayers()) {
            double distSq;
            if (p == null || p == self || !GearRenderUtil.hasNetheriteGear(p) || !((distSq = p.squaredDistanceTo((Entity)self)) <= 64.0) || !(distSq < bestDist)) continue;
            bestDist = distSq;
            best = p;
        }
        return best;
    }

    private static String buildAcceptMessage(PlayerEntity player, String coords) {
        String name = player == null ? "?" : player.getName().getString();
        String set = GearRenderUtil.formatArmorSet(player);
        String hand = GearRenderUtil.formatHandItem(player);
        String coordLabel = coords == null || coords.isBlank() ? "kordy: -" : coords;
        return name + " zaakceptowal twoja prosbe i ma " + set + " | " + hand + " | " + coordLabel;
    }

    private static String formatCoords(PlayerEntity player) {
        if (player == null) {
            return "kordy: -";
        }
        int bx = (int)Math.floor(player.getX());
        int by = (int)Math.floor(player.getY());
        int bz = (int)Math.floor(player.getZ());
        return "kordy: " + bx + " " + by + " " + bz;
    }

    private static String formatArmorSet(PlayerEntity player) {
        if (player == null) {
            return "set: -";
        }
        ItemStack h = player.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack c = player.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack l = player.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack b = player.getEquippedStack(EquipmentSlot.FEET);
        String hs = GearRenderUtil.armorLabel(h, EquipmentSlot.HEAD);
        String cs = GearRenderUtil.armorLabel(c, EquipmentSlot.CHEST);
        String ls = GearRenderUtil.armorLabel(l, EquipmentSlot.LEGS);
        String bs = GearRenderUtil.armorLabel(b, EquipmentSlot.FEET);
        return "set: " + hs + "/" + cs + "/" + ls + "/" + bs;
    }

    private static String formatHandItem(PlayerEntity player) {
        if (player == null) {
            return "reka: -";
        }
        ItemStack main = player.getMainHandStack();
        if (main == null || main.isEmpty()) {
            return "reka: -";
        }
        String name = GearRenderUtil.shortItemName(main, 14);
        String pct = GearRenderUtil.swordPercent(main);
        if (pct != null) {
            return "reka: " + name + " (" + pct + ")";
        }
        return "reka: " + name;
    }

    private static String shortItemName(ItemStack stack, int maxLen) {
        if (stack == null || stack.isEmpty()) {
            return "-";
        }
        String name = stack.getName().getString();
        if (name == null) {
            return "-";
        }
        name = name.trim();
        if (maxLen > 4 && name.length() > maxLen) {
            return name.substring(0, maxLen);
        }
        return name;
    }

    private static String armorLabel(ItemStack stack, EquipmentSlot slot) {
        if (stack == null || stack.isEmpty()) {
            return "-";
        }
        Item item = stack.getItem();
        if (slot == EquipmentSlot.HEAD && item == Items.NETHERITE_HELMET) {
            return "nethe bania";
        }
        if (slot == EquipmentSlot.CHEST && item == Items.NETHERITE_CHESTPLATE) {
            return "nethe klata";
        }
        if (slot == EquipmentSlot.LEGS && item == Items.NETHERITE_LEGGINGS) {
            return "nethe spodnie";
        }
        if (slot == EquipmentSlot.FEET && item == Items.NETHERITE_BOOTS) {
            return "nethe buty";
        }
        return "-";
    }

    private static String swordPercent(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        if (!GearRenderUtil.isSword(stack)) {
            return null;
        }
        if (!stack.isDamageable() || stack.getMaxDamage() <= 0) {
            return null;
        }
        int remaining = stack.getMaxDamage() - stack.getDamage();
        int pct = Math.round(100.0f * (float)remaining / (float)stack.getMaxDamage());
        pct = Math.max(0, Math.min(100, pct));
        return pct + "%";
    }

    private static boolean isAutoTpaEnabled() {
        return GuiClient.CONFIG.gearRenderAutoTpa != null && GuiClient.CONFIG.gearRenderAutoTpa != false;
    }

    public static boolean isAutoTpaAcceptFor(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        if (!GearRenderUtil.isAutoTpaEnabled()) {
            return false;
        }
        if (lastAutoTpaTargetName == null || lastAutoTpaTargetName.isBlank()) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (lastAutoTpaSentAt <= 0L || now - lastAutoTpaSentAt > 30000L) {
            return false;
        }
        return GearRenderUtil.normalizeNameSimple(name).equals(lastAutoTpaTargetName);
    }

    private static boolean isAutoLogoutEnabled() {
        return false;
    }

    private static boolean isAutoLogoutTargetActive(long now) {
        if (!GearRenderUtil.isAutoLogoutEnabled()) {
            return false;
        }
        if (lastAutoTpaTargetAt <= 0L) {
            return false;
        }
        if (now - lastAutoTpaTargetAt > 30000L) {
            return false;
        }
        return lastAutoTpaTargetName != null && !lastAutoTpaTargetName.isBlank();
    }

    private static boolean isAutoLogoutActive(long now) {
        if (!GearRenderUtil.isAutoLogoutEnabled()) {
            return false;
        }
        return autoLogoutArmedUntil > 0L && now <= autoLogoutArmedUntil;
    }

    private static void armAutoLogout(long now) {
        autoLogoutArmedUntil = Math.max(autoLogoutArmedUntil, now + 30000L);
    }

    private static void resetAutoChState() {
        autoChPending = false;
        autoChRequestedAt = 0L;
        lastAutoChAt = 0L;
        lastAutoChEnabled = false;
    }

    private static void notifyChBlocked(MinecraftClient client, String msg) {
        if (client == null || client.player == null) {
            return;
        }
        if (msg == null || msg.isBlank()) {
            return;
        }
        client.player.sendMessage((Text)Text.literal((String)msg), true);
    }

    private static boolean isBlacklistedName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        List<String> list = GuiClient.CONFIG.gearRenderBlacklist;
        if (list == null || list.isEmpty()) {
            return false;
        }
        String key = GearRenderUtil.normalizeNameSimple(name);
        for (String raw : list) {
            if (raw == null || raw.isBlank() || !GearRenderUtil.normalizeNameSimple(raw).equals(key)) continue;
            return true;
        }
        return false;
    }

    private static boolean isMissingReadyForAutoTpa(UUID id, long now) {
        if (id == null) {
            return false;
        }
        Long firstSeen = missingFirstSeenAt.get(id);
        return firstSeen == null || now - firstSeen >= 1000L;
    }

    private static String normalizeNameSimple(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().toLowerCase(Locale.ROOT);
    }

    private static void syncAutoTpaQueue(Set<UUID> currentMissing) {
        Set<UUID> missingNow = currentMissing == null ? Set.of() : currentMissing;
        autoTpaHandled.removeIf(id -> !missingNow.contains(id));
        autoTpaQueued.removeIf(id -> !missingNow.contains(id));
        autoTpaQueue.removeIf(id -> !missingNow.contains(id));
        for (UUID uUID : missingNow) {
            if (autoTpaHandled.contains(uUID) || autoTpaQueued.contains(uUID)) continue;
            autoTpaQueue.add(uUID);
            autoTpaQueued.add(uUID);
        }
    }

    private static void resetAutoTpaState() {
        autoTpaQueue.clear();
        autoTpaQueued.clear();
        autoTpaHandled.clear();
        lastAutoTpaAt = 0L;
        lastAutoTpaSentAt = 0L;
        lastAutoTpaTargetName = null;
        lastAutoTpaTargetAt = 0L;
        lastTeleportHandledAt = 0L;
        lastAutoJumpAt = 0L;
        if (!GearRenderUtil.isAutoLogoutEnabled()) {
            hasLastPlayerPos = false;
        }
    }

    private static void resetAutoTpaAfterTeleport() {
        if (!autoTpaQueue.isEmpty()) {
            return;
        }
        GearRenderUtil.resetAutoTpaState();
    }

    private static void resetAutoTpaQueueState() {
        autoTpaQueue.clear();
        autoTpaQueued.clear();
        autoTpaHandled.clear();
        lastAutoTpaAt = 0L;
        lastAutoTpaSentAt = 0L;
        lastAutoTpaTargetName = null;
        lastAutoTpaTargetAt = 0L;
        lastAutoJumpAt = 0L;
    }

    private static void resetAutoTpaQueueStateKeepTarget() {
        autoTpaQueue.clear();
        autoTpaQueued.clear();
        autoTpaHandled.clear();
        lastAutoTpaAt = 0L;
        lastAutoTpaSentAt = 0L;
        lastAutoJumpAt = 0L;
    }

    private static void forceLogout(MinecraftClient client, String reason) {
        if (client == null) {
            return;
        }
        ClientPlayNetworkHandler handler = client.getNetworkHandler();
        if (handler == null || handler.getConnection() == null) {
            return;
        }
        MutableText msg = reason == null || reason.isBlank() ? Text.literal((String)"Auto logout") : Text.literal((String)reason);
        handler.getConnection().disconnect((Text)msg);
    }

    private static void takeTeleportScreenshot(MinecraftClient client) {
        if (client == null) {
            return;
        }
        try {
            File runDir = ((MinecraftClientAccessor)client).getRunDirectory();
            if (runDir == null) {
                return;
            }
            Framebuffer framebuffer = client.getFramebuffer();
            if (framebuffer == null) {
                return;
            }
            ScreenshotRecorder.saveScreenshot((File)runDir, (Framebuffer)framebuffer, message -> {});
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void refresh(MinecraftClient client, long now) {
        String address;
        if (client == null) {
            GearRenderUtil.resetForChannelChange();
            return;
        }
        if (!GuiScreen.isGearRenderOpen(client.currentScreen)) {
            return;
        }
        lastRefresh = now;
        inRange.clear();
        missing.clear();
        lastVisibleCount = 0;
        lastPercent = 0;
        if (client == null || client.world == null || client.player == null) {
            seen.clear();
            missingById.clear();
            missingFirstSeenAt.clear();
            return;
        }
        ServerInfo serverInfo = client.getCurrentServerEntry();
        String string = address = serverInfo != null ? serverInfo.address : null;
        if (!Objects.equals(address, lastServerAddress)) {
            lastServerAddress = address;
            seen.clear();
            missingById.clear();
            missingFirstSeenAt.clear();
        }
        if (!NameProtectUtil.isOnAnarchiaServer()) {
            seen.clear();
            missingById.clear();
            missingFirstSeenAt.clear();
            return;
        }
        ClientWorld world = client.world;
        HashSet<UUID> current = new HashSet<UUID>();
        ArrayList<GearEntry> nextInRange = new ArrayList<GearEntry>();
        int visible = 0;
        PlayerEntity autoLogoutTarget = null;
        boolean checkAutoLogoutTarget = GearRenderUtil.isAutoLogoutEnabled() && lastAutoTpaTargetAt > 0L && now - lastAutoTpaTargetAt <= 30000L && lastAutoTpaTargetName != null && !lastAutoTpaTargetName.isBlank() && autoLogoutGraceUntil > now;
        for (PlayerEntity p : world.getPlayers()) {
            String name;
            ItemStack sword = ItemStack.EMPTY;
            if (p == null || client.player.getUuid().equals(p.getUuid())) continue;
            ++visible;
            UUID id = p.getUuid();
            current.add(id);
            Object info = seen.get(id);
            if (info == null) {
                info = new SeenInfo();
                seen.put(id, (SeenInfo)info);
            }
            ((SeenInfo)info).lastSeenAt = now;
            ((SeenInfo)info).name = p.getName().getString();
            ItemStack itemStack = p.getMainHandStack();
            ItemStack off = p.getOffHandStack();
            ItemStack itemStack2 = GearRenderUtil.isSword(itemStack) ? itemStack : (sword = GearRenderUtil.isSword(off) ? off : ItemStack.EMPTY);
            if (!sword.isEmpty()) {
                ((SeenInfo)info).sawSword = true;
                String extra = GearRenderUtil.extractExtraDamage(client, sword);
                if (extra != null && !extra.isBlank()) {
                    ((SeenInfo)info).swordExtra = extra;
                }
            }
            ((SeenInfo)info).nearSponge = GearRenderUtil.isNearSponge((World)world, p.getBlockPos(), 4);
            ItemStack helmet = GearRenderUtil.copyStack(p.getEquippedStack(EquipmentSlot.HEAD));
            ItemStack chest = GearRenderUtil.copyStack(p.getEquippedStack(EquipmentSlot.CHEST));
            ItemStack legs = GearRenderUtil.copyStack(p.getEquippedStack(EquipmentSlot.LEGS));
            ItemStack boots = GearRenderUtil.copyStack(p.getEquippedStack(EquipmentSlot.FEET));
            ((SeenInfo)info).helmet = helmet;
            ((SeenInfo)info).chest = chest;
            ((SeenInfo)info).legs = legs;
            ((SeenInfo)info).boots = boots;
            if (GearRenderUtil.hasNetheriteGear(p)) {
                nextInRange.add(new GearEntry(((SeenInfo)info).name, helmet, chest, legs, boots, ((SeenInfo)info).sawSword, ((SeenInfo)info).swordExtra));
                ((SeenInfo)info).hadInRange = true;
                ((SeenInfo)info).lastHelmet = helmet;
                ((SeenInfo)info).lastChest = chest;
                ((SeenInfo)info).lastLegs = legs;
                ((SeenInfo)info).lastBoots = boots;
                ((SeenInfo)info).lastSawSword = ((SeenInfo)info).sawSword;
                ((SeenInfo)info).lastSwordExtra = ((SeenInfo)info).swordExtra;
            }
            if (!checkAutoLogoutTarget || autoLogoutTarget != null || !GearRenderUtil.normalizeNameSimple(name = p.getName().getString()).equals(lastAutoTpaTargetName)) continue;
            autoLogoutTarget = p;
        }
        if (autoLogoutTarget != null) {
            List<PlayerEntity> near = GearRenderUtil.collectNearbyPlayers(client, (PlayerEntity)client.player, 100.0);
            if (near.isEmpty()) {
                near = List.of(autoLogoutTarget);
            }
            String msg = GearRenderUtil.buildTeleportAlertMessage(near, GearRenderUtil.formatCoords((PlayerEntity)client.player));
            NotificationHud.postCustomText(msg);
            GearRenderUtil.takeTeleportScreenshot(client);
            GearRenderUtil.forceLogout(client, msg);
            GearRenderUtil.resetAutoTpaState();
            return;
        }
        long staleBefore = now - 30000L;
        ArrayList<UUID> stale = new ArrayList<UUID>();
        for (Map.Entry<UUID, SeenInfo> entry : seen.entrySet()) {
            UUID id = entry.getKey();
            SeenInfo info = entry.getValue();
            if (info == null || info.name == null) {
                stale.add(id);
                continue;
            }
            if (info.lastSeenAt > 0L && info.lastSeenAt < staleBefore) {
                stale.add(id);
                continue;
            }
            if (current.contains(id) || !info.nearSponge || !info.hadInRange || GearRenderUtil.isBlacklistedName(info.name)) continue;
            if (!missingById.containsKey(id)) {
                missingFirstSeenAt.put(id, now);
            }
            missingById.put(id, new MissingEntry(id, info.name, GearRenderUtil.copyStack(info.lastHelmet), GearRenderUtil.copyStack(info.lastChest), GearRenderUtil.copyStack(info.lastLegs), GearRenderUtil.copyStack(info.lastBoots), info.lastSawSword, info.lastSwordExtra));
        }
        for (UUID uUID : stale) {
            seen.remove(uUID);
            missingById.remove(uUID);
            missingFirstSeenAt.remove(uUID);
        }
        for (UUID uUID : current) {
            missingById.remove(uUID);
            missingFirstSeenAt.remove(uUID);
        }
        if (!missingById.isEmpty()) {
            Iterator<Map.Entry<UUID, MissingEntry>> it = missingById.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, MissingEntry> entry = it.next();
                MissingEntry m = entry.getValue();
                if (m != null && !GearRenderUtil.isBlacklistedName(m.name)) continue;
                UUID id = entry.getKey();
                it.remove();
                missingFirstSeenAt.remove(id);
                autoTpaQueue.remove(id);
                autoTpaQueued.remove(id);
                autoTpaHandled.remove(id);
            }
        }
        nextInRange.sort(Comparator.comparing(a -> a.name.toLowerCase(Locale.ROOT)));
        inRange.addAll(nextInRange);
        missing.addAll(missingById.values());
        missing.sort(Comparator.comparing(a -> a.name.toLowerCase(Locale.ROOT)));
        if (GearRenderUtil.isAutoTpaEnabled()) {
            HashSet<UUID> autoIds = new HashSet<UUID>();
            for (MissingEntry e : missingById.values()) {
                if (e == null || e.name == null || e.name.isBlank() || GearRenderUtil.isBlacklistedName(e.name) || GearRenderUtil.isExtraDamageOverLimit(e.sawSword, e.swordExtra)) continue;
                autoIds.add(e.id);
            }
            GearRenderUtil.syncAutoTpaQueue(autoIds);
        }
        lastVisibleCount = visible;
        lastPercent = visible > 0 ? Math.round(100.0f * (float)inRange.size() / (float)visible) : 0;
    }

    private static boolean hasNetheriteGear(PlayerEntity p) {
        if (p == null) {
            return false;
        }
        if (GearRenderUtil.isNetheriteArmor(p.getEquippedStack(EquipmentSlot.HEAD))) {
            return true;
        }
        if (GearRenderUtil.isNetheriteArmor(p.getEquippedStack(EquipmentSlot.CHEST))) {
            return true;
        }
        if (GearRenderUtil.isNetheriteArmor(p.getEquippedStack(EquipmentSlot.LEGS))) {
            return true;
        }
        if (GearRenderUtil.isNetheriteArmor(p.getEquippedStack(EquipmentSlot.FEET))) {
            return true;
        }
        if (GearRenderUtil.isNetheriteTool(p.getMainHandStack())) {
            return true;
        }
        return GearRenderUtil.isNetheriteTool(p.getOffHandStack());
    }

    private static boolean isNetheriteArmor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        return item == Items.NETHERITE_HELMET || item == Items.NETHERITE_CHESTPLATE || item == Items.NETHERITE_LEGGINGS || item == Items.NETHERITE_BOOTS;
    }

    private static boolean isNetheriteTool(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        return item == Items.NETHERITE_SWORD || item == Items.NETHERITE_PICKAXE;
    }

    private static boolean isSword(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.getItem() instanceof SwordItem;
    }

    private static boolean isOutsideNoLogoutZone(PlayerEntity player) {
        if (player == null) {
            return true;
        }
        double x = player.getX();
        double z = player.getZ();
        return x < -150.0 || x > 150.0 || z < -150.0 || z > 150.0;
    }

    private static ItemStack copyStack(ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }

    private static String extractExtraDamage(MinecraftClient client, ItemStack stack) {
        if (client == null || client.player == null || stack == null || stack.isEmpty()) {
            return null;
        }
        List<Text> tooltip = stack.getTooltip(Item.TooltipContext.DEFAULT, (PlayerEntity)client.player, (TooltipType)TooltipType.BASIC);
        if (tooltip == null || tooltip.isEmpty()) {
            return null;
        }
        for (Text t : tooltip) {
            String num;
            String normalized;
            String raw;
            if (t == null || (raw = t.getString()) == null || raw.isBlank() || !(normalized = GearRenderUtil.normalizeAscii(raw).toLowerCase(Locale.ROOT)).contains(EXTRA_DAMAGE_LABEL) || (num = GearRenderUtil.extractNumber(raw)) == null || num.isBlank()) continue;
            return num;
        }
        return null;
    }

    private static boolean isExtraDamageOverLimit(boolean sawSword, String swordExtra) {
        if (!sawSword) {
            return false;
        }
        Double value = GearRenderUtil.parseExtraDamageValue(swordExtra);
        return value != null && value > 49.0;
    }

    private static Double parseExtraDamageValue(String swordExtra) {
        if (swordExtra == null || swordExtra.isBlank()) {
            return null;
        }
        Matcher m = NUMBER_PATTERN.matcher(swordExtra);
        if (!m.find()) {
            return null;
        }
        String num = m.group().replace(',', '.');
        try {
            return Double.parseDouble(num);
        }
        catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String extractNumber(String raw) {
        if (raw == null) {
            return null;
        }
        Matcher m = NUMBER_PATTERN.matcher(raw);
        if (!m.find()) {
            return null;
        }
        String num = m.group();
        if (num.startsWith("+")) {
            num = num.substring(1);
        }
        return num;
    }

    private static String normalizeAscii(String s) {
        if (s == null) {
            return "";
        }
        String norm = Normalizer.normalize(s, Normalizer.Form.NFD);
        StringBuilder sb = new StringBuilder(norm.length());
        for (int i = 0; i < norm.length(); ++i) {
            char c = norm.charAt(i);
            if (Character.getType(c) == 6 || c > '\u007f') continue;
            sb.append(c);
        }
        return sb.toString();
    }

    private static boolean isNearSponge(World world, BlockPos center, int radius) {
        if (world == null || center == null) {
            return false;
        }
        int r = Math.max(0, radius);
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int dx = -r; dx <= r; ++dx) {
            for (int dy = -r; dy <= r; ++dy) {
                for (int dz = -r; dz <= r; ++dz) {
                    pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    Block block = world.getBlockState((BlockPos)pos).getBlock();
                    if (block != Blocks.SPONGE && block != Blocks.WET_SPONGE) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private static final class SeenInfo {
        String name;
        boolean sawSword;
        boolean nearSponge;
        long lastSeenAt;
        ItemStack helmet = ItemStack.EMPTY;
        ItemStack chest = ItemStack.EMPTY;
        ItemStack legs = ItemStack.EMPTY;
        ItemStack boots = ItemStack.EMPTY;
        String swordExtra;
        boolean hadInRange;
        ItemStack lastHelmet = ItemStack.EMPTY;
        ItemStack lastChest = ItemStack.EMPTY;
        ItemStack lastLegs = ItemStack.EMPTY;
        ItemStack lastBoots = ItemStack.EMPTY;
        boolean lastSawSword;
        String lastSwordExtra;

        private SeenInfo() {
        }
    }

    public static final class MissingEntry {
        public final String name;
        public final UUID id;
        public final ItemStack helmet;
        public final ItemStack chest;
        public final ItemStack legs;
        public final ItemStack boots;
        public final boolean sawSword;
        public final String swordExtra;

        private MissingEntry(UUID id, String name, ItemStack helmet, ItemStack chest, ItemStack legs, ItemStack boots, boolean sawSword, String swordExtra) {
            this.id = id;
            this.name = name;
            this.helmet = helmet == null ? ItemStack.EMPTY : helmet;
            this.chest = chest == null ? ItemStack.EMPTY : chest;
            this.legs = legs == null ? ItemStack.EMPTY : legs;
            this.boots = boots == null ? ItemStack.EMPTY : boots;
            this.sawSword = sawSword;
            this.swordExtra = swordExtra;
        }
    }

    public static final class GearEntry {
        public final String name;
        public final ItemStack helmet;
        public final ItemStack chest;
        public final ItemStack legs;
        public final ItemStack boots;
        public final boolean sawSword;
        public final String swordExtra;

        private GearEntry(String name, ItemStack helmet, ItemStack chest, ItemStack legs, ItemStack boots, boolean sawSword, String swordExtra) {
            this.name = name;
            this.helmet = helmet;
            this.chest = chest;
            this.legs = legs;
            this.boots = boots;
            this.sawSword = sawSword;
            this.swordExtra = swordExtra;
        }
    }
}

