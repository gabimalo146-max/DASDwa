package me.Gui.gui.mixin;

import me.Gui.gui.modules.AntiTrapUtil;
import me.Gui.gui.modules.BetterPaintingUtil;
import me.Gui.gui.modules.FastLeverUtil;
import me.Gui.gui.modules.FreecamUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ClientPlayerInteractionManager.class})
public class ClientPlayerInteractionManagerMixin {
    @Inject(method={"interactEntity"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$antiTrapInteract(PlayerEntity player, Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (AntiTrapUtil.shouldIgnore(entity)) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method={"interactEntityAtLocation"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$antiTrapInteractAt(PlayerEntity player, Entity entity, EntityHitResult hit, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (AntiTrapUtil.shouldIgnore(entity)) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method={"interactBlock"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$freecamBlockReach(ClientPlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        Vec3d origin;
        if (!FreecamUtil.isEnabled()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        Entity cam = client.getCameraEntity();
        if (cam == null) {
            return;
        }
        Vec3d pos = hit.getPos();
        double max = 4.0;
        Vec3d vec3d = origin = FreecamUtil.isActive() ? FreecamUtil.originPos() : cam.getPos();
        if (origin == null) {
            origin = cam.getPos();
        }
        if (origin.squaredDistanceTo(pos) > max * max) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

    @Inject(method={"interactBlock"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$betterPaintingNoWeb(ClientPlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        BlockHitResult replaced;
        if (hand != Hand.MAIN_HAND) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (!BetterPaintingUtil.shouldHandlePainting(client)) {
            return;
        }
        if (client == null || client.world == null || hit == null) {
            return;
        }
        if (BetterPaintingUtil.isCobweb(client.world.getBlockState(hit.getBlockPos())) && ((replaced = BetterPaintingUtil.skipPassThrough(client, 1.0f, hit)) == null || replaced.getBlockPos().equals((Object)hit.getBlockPos()))) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

    @Inject(method={"attackBlock"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$betterPaintingNoBreakThroughWeb(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!BetterPaintingUtil.shouldHandlePainting(client)) {
            return;
        }
        cir.setReturnValue(false);
    }

    @Inject(method={"updateBlockBreakingProgress"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$betterPaintingNoBreakProgressThroughWeb(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!BetterPaintingUtil.shouldHandlePainting(client)) {
            return;
        }
        cir.setReturnValue(false);
    }

    @Inject(method={"interactBlock"}, at={@At(value="HEAD")})
    private void gui$fastLeverTrackSelfFluid(ClientPlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        FastLeverUtil.noteFriendlyFluidPlace(MinecraftClient.getInstance(), hand, hit);
    }

    @Inject(method={"attackEntity"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$antiTrapAttack(PlayerEntity player, Entity entity, CallbackInfo ci) {
        if (AntiTrapUtil.shouldIgnore(entity) && !AntiTrapUtil.allowAttack()) {
            ci.cancel();
        }
    }
}

