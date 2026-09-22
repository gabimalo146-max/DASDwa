package me.Gui.gui.mixin;

import me.Gui.gui.modules.BetterPaintingUtil;
import me.Gui.gui.modules.FastPlaceUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MinecraftClient.class})
public class MinecraftClientFastPlaceMixin {
    @Shadow
    private int itemUseCooldown;

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void gui$fastPlaceTick(CallbackInfo ci) {
        MinecraftClient client = ((MinecraftClient)(Object)this);
        if (FastPlaceUtil.shouldApply(client)) {
            this.itemUseCooldown = 0;
        }
    }

    @Inject(method={"doItemUse"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$fastPlaceGate(CallbackInfo ci) {
        MinecraftClient client = ((MinecraftClient)(Object)this);
        if (this.gui$betterPaintingPlace(client, ci)) {
            return;
        }
        if (!FastPlaceUtil.shouldApply(client)) {
            return;
        }
        if (!FastPlaceUtil.allowUseNow()) {
            ci.cancel();
            return;
        }
        this.itemUseCooldown = 0;
    }

    @Inject(method={"doItemUse"}, at={@At(value="TAIL")})
    private void gui$fastPlaceClearCooldown(CallbackInfo ci) {
        MinecraftClient client = ((MinecraftClient)(Object)this);
        if (FastPlaceUtil.shouldApply(client)) {
            this.itemUseCooldown = 0;
        }
    }

    private boolean gui$betterPaintingPlace(MinecraftClient client, CallbackInfo ci) {
        ActionResult result;
        EntityHitResult ehr;
        if (client == null || client.player == null || client.world == null || client.interactionManager == null) {
            return false;
        }
        if (!BetterPaintingUtil.shouldHandlePainting(client)) {
            return false;
        }
        HitResult current = client.crosshairTarget;
        if (current == null) {
            return false;
        }
        BlockHitResult target = null;
        if (current instanceof BlockHitResult) {
            BlockHitResult bhr = (BlockHitResult)current;
            if (BetterPaintingUtil.isCobweb(client.world.getBlockState(bhr.getBlockPos()))) {
                target = BetterPaintingUtil.skipCobwebs(client, 1.0f, bhr);
            }
        } else if (current instanceof EntityHitResult && (ehr = (EntityHitResult)current).getEntity() instanceof PlayerEntity) {
            BlockHitResult hit = BetterPaintingUtil.raycastFromCamera(client, 1.0f, null);
            if (hit != null && BetterPaintingUtil.isCobweb(client.world.getBlockState(hit.getBlockPos()))) {
                hit = BetterPaintingUtil.skipCobwebs(client, 1.0f, hit);
            }
            target = hit;
        }
        if (target == null) {
            return false;
        }
        if (FastPlaceUtil.shouldApply(client) && !FastPlaceUtil.allowUseNow()) {
            ci.cancel();
            return true;
        }
        BlockHitResult placeHit = target;
        Entity cam = client.getCameraEntity();
        if (cam != null) {
            Vec3d center = Vec3d.ofCenter((Vec3i)target.getBlockPos());
            Vec3d camPos = cam.getCameraPosVec(1.0f);
            Direction side = BetterPaintingUtil.horizontalFaceTowardCamera(center, camPos);
            Vec3d hitPos = center.add((double)side.getOffsetX() * 0.5, (double)side.getOffsetY() * 0.5, (double)side.getOffsetZ() * 0.5);
            placeHit = new BlockHitResult(hitPos, side, target.getBlockPos(), false);
        }
        if ((result = client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, placeHit)).isAccepted()) {
            client.player.swingHand(Hand.MAIN_HAND);
        }
        ci.cancel();
        return true;
    }
}

