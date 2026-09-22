package me.Gui.gui.modules;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import me.Gui.gui.client.GuiClient;
import me.Gui.gui.mixin.EntityAccessor;
import me.Gui.gui.mixin.LivingEntityAccessor;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

public final class LogoutSpots {
    private static final Object LOCK = new Object();
    private static final Map<UUID, Spot> SPOTS = new LinkedHashMap<UUID, Spot>();
    private static final Map<UUID, Snapshot> LAST_SEEN = new LinkedHashMap<UUID, Snapshot>();
    private static final Set<UUID> PREV_IDS = new LinkedHashSet<UUID>();
    private static int nextEntityId = -6200;

    private LogoutSpots() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void clearAll() {
        Object object = LOCK;
        synchronized (object) {
            SPOTS.clear();
            LAST_SEEN.clear();
            PREV_IDS.clear();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void tick(MinecraftClient client) {
        if (client == null || client.world == null || !LogoutSpots.isEnabled()) {
            Object object = LOCK;
            synchronized (object) {
                SPOTS.clear();
                LAST_SEEN.clear();
                PREV_IDS.clear();
            }
            return;
        }
        Object object = LOCK;
        synchronized (object) {
            LinkedHashSet<UUID> current = new LinkedHashSet<UUID>();
            for (PlayerEntity p : client.world.getPlayers()) {
                if (p == null || client.player != null && p == client.player) continue;
                LAST_SEEN.put(p.getUuid(), new Snapshot(p));
                current.add(p.getUuid());
                if (!SPOTS.containsKey(p.getUuid())) continue;
                SPOTS.remove(p.getUuid());
            }
            if (!PREV_IDS.isEmpty()) {
                for (UUID id : PREV_IDS) {
                    Spot spot;
                    Snapshot snap;
                    if (current.contains(id) || SPOTS.containsKey(id) || client.player != null && id.equals(client.player.getUuid()) || client.getNetworkHandler() != null && client.getNetworkHandler().getPlayerListEntry(id) != null || (snap = LAST_SEEN.get(id)) == null || (spot = LogoutSpots.buildSpot(client.world, snap)) == null) continue;
                    SPOTS.put(id, spot);
                    LAST_SEEN.remove(id);
                }
            }
            PREV_IDS.clear();
            PREV_IDS.addAll(current);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Collection<Spot> spots() {
        Object object = LOCK;
        synchronized (object) {
            return new ArrayList<Spot>(SPOTS.values());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void handlePlayerRemove(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        if (!LogoutSpots.isEnabled()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            return;
        }
        Object object = LOCK;
        synchronized (object) {
            for (UUID id : ids) {
                Spot spot;
                PlayerEntity live;
                if (id == null || SPOTS.containsKey(id) || client.player != null && id.equals(client.player.getUuid())) continue;
                Snapshot snap = LAST_SEEN.get(id);
                if (snap == null && (live = client.world.getPlayerByUuid(id)) != null) {
                    snap = new Snapshot(live);
                }
                if (snap == null || (spot = LogoutSpots.buildSpot(client.world, snap)) == null) continue;
                SPOTS.put(id, spot);
                LAST_SEEN.remove(id);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void handlePlayerAdd(UUID id) {
        if (id == null) {
            return;
        }
        Object object = LOCK;
        synchronized (object) {
            SPOTS.remove(id);
        }
    }

    private static Spot buildSpot(ClientWorld world, Snapshot snap) {
        if (world == null || snap == null) {
            return null;
        }
        OtherClientPlayerEntity ghost = new OtherClientPlayerEntity(world, snap.profile);
        ghost.updatePositionAndAngles(snap.pos.x, snap.pos.y, snap.pos.z, snap.yaw, snap.pitch);
        ghost.setYaw(snap.yaw);
        ghost.setPitch(snap.pitch);
        ghost.setBodyYaw(snap.bodyYaw);
        ghost.setHeadYaw(snap.headYaw);
        ghost.setPose(snap.pose);
        ghost.setOnGround(snap.onGround);
        EntityAccessor acc = (EntityAccessor)ghost;
        acc.setPrevX(snap.pos.x);
        acc.setPrevY(snap.pos.y);
        acc.setPrevZ(snap.pos.z);
        acc.setLastRenderX(snap.pos.x);
        acc.setLastRenderY(snap.pos.y);
        acc.setLastRenderZ(snap.pos.z);
        acc.setPrevYaw(snap.yaw);
        acc.setPrevPitch(snap.pitch);
        if (ghost instanceof LivingEntityAccessor) {
            LivingEntityAccessor lea = (LivingEntityAccessor)ghost;
            lea.setPrevBodyYaw(snap.bodyYaw);
            lea.setPrevHeadYaw(snap.headYaw);
        }
        ghost.setVelocity(Vec3d.ZERO);
        ghost.noClip = true;
        ghost.setNoGravity(true);
        ghost.setId(nextEntityId--);
        LogoutSpots.copyEquipment(snap, ghost);
        return new Spot(snap.uuid, snap.name, System.currentTimeMillis(), ghost, snap.modelPose);
    }

    private static PoseSnapshot capturePose(PlayerEntity player) {
        if (!(player instanceof AbstractClientPlayerEntity)) {
            return null;
        }
        AbstractClientPlayerEntity abstractClient = (AbstractClientPlayerEntity)player;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return null;
        }
        EntityRenderer renderer = client.getEntityRenderDispatcher().getRenderer((Entity)abstractClient);
        if (!(renderer instanceof PlayerEntityRenderer)) {
            return null;
        }
        PlayerEntityRenderer playerRenderer = (PlayerEntityRenderer)renderer;
        PlayerEntityModel model = (PlayerEntityModel)playerRenderer.getModel();
        PlayerEntityRenderState state = playerRenderer.createRenderState();
        playerRenderer.updateRenderState(abstractClient, state, 0.0f);
        model.setAngles(state);
        return new PoseSnapshot(model);
    }

    private static void copyEquipment(Snapshot snap, OtherClientPlayerEntity to) {
        EquipmentSlot[] slots = EquipmentSlot.values();
        for (int i = 0; i < slots.length && i < snap.equipment.length; ++i) {
            ItemStack stack = snap.equipment[i];
            to.equipStack(slots[i], stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        }
    }

    private static boolean isEnabled() {
        HackModule mod = GuiClient.MODULES.byId(ModuleId.LOGOUT_SPOTS);
        return mod != null && mod.isEnabled();
    }

    private static final class Snapshot {
        final UUID uuid;
        final String name;
        final GameProfile profile;
        final Vec3d pos;
        final float yaw;
        final float pitch;
        final float bodyYaw;
        final float headYaw;
        final EntityPose pose;
        final boolean onGround;
        final ItemStack[] equipment;
        final PoseSnapshot modelPose;

        Snapshot(PlayerEntity player) {
            this.uuid = player.getUuid();
            this.name = player.getName().getString();
            this.profile = player.getGameProfile();
            this.pos = player.getPos();
            this.yaw = player.getYaw();
            this.pitch = player.getPitch();
            this.bodyYaw = player.getBodyYaw();
            this.headYaw = player.getHeadYaw();
            this.pose = player.getPose();
            this.onGround = player.isOnGround();
            EquipmentSlot[] slots = EquipmentSlot.values();
            this.equipment = new ItemStack[slots.length];
            for (int i = 0; i < slots.length; ++i) {
                ItemStack stack = player.getEquippedStack(slots[i]);
                this.equipment[i] = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
            }
            this.modelPose = LogoutSpots.capturePose(player);
        }
    }

    public static final class Spot {
        public final UUID uuid;
        public final String name;
        public final long logoutAtMs;
        public final OtherClientPlayerEntity ghost;
        public final PoseSnapshot pose;

        private Spot(UUID uuid, String name, long logoutAtMs, OtherClientPlayerEntity ghost, PoseSnapshot pose) {
            this.uuid = uuid;
            this.name = name;
            this.logoutAtMs = logoutAtMs;
            this.ghost = ghost;
            this.pose = pose;
        }
    }

    public static final class PoseSnapshot {
        public final float headPitch;
        public final float headYaw;
        public final float headRoll;
        public final float bodyPitch;
        public final float bodyYaw;
        public final float bodyRoll;
        public final float rightArmPitch;
        public final float rightArmYaw;
        public final float rightArmRoll;
        public final float leftArmPitch;
        public final float leftArmYaw;
        public final float leftArmRoll;
        public final float rightLegPitch;
        public final float rightLegYaw;
        public final float rightLegRoll;
        public final float leftLegPitch;
        public final float leftLegYaw;
        public final float leftLegRoll;

        private PoseSnapshot(PlayerEntityModel model) {
            this.headPitch = model.head.pitch;
            this.headYaw = model.head.yaw;
            this.headRoll = model.head.roll;
            this.bodyPitch = model.body.pitch;
            this.bodyYaw = model.body.yaw;
            this.bodyRoll = model.body.roll;
            this.rightArmPitch = model.rightArm.pitch;
            this.rightArmYaw = model.rightArm.yaw;
            this.rightArmRoll = model.rightArm.roll;
            this.leftArmPitch = model.leftArm.pitch;
            this.leftArmYaw = model.leftArm.yaw;
            this.leftArmRoll = model.leftArm.roll;
            this.rightLegPitch = model.rightLeg.pitch;
            this.rightLegYaw = model.rightLeg.yaw;
            this.rightLegRoll = model.rightLeg.roll;
            this.leftLegPitch = model.leftLeg.pitch;
            this.leftLegYaw = model.leftLeg.yaw;
            this.leftLegRoll = model.leftLeg.roll;
        }
    }
}

