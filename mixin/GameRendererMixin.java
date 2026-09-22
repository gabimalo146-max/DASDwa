package me.Gui.gui.mixin;

import me.Gui.gui.client.GuiClient;
import me.Gui.gui.mixin.MinecraftClientAccessor;
import me.Gui.gui.modules.AntiTrapUtil;
import me.Gui.gui.modules.BetterHitboxesUtil;
import me.Gui.gui.modules.BetterPaintingUtil;
import me.Gui.gui.modules.FreecamUtil;
import me.Gui.gui.modules.HackModule;
import me.Gui.gui.modules.ModuleId;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={GameRenderer.class})
public class GameRendererMixin {
    @Inject(method={"getNightVisionStrength"}, at={@At(value="HEAD")}, cancellable=true)
    private static void gui$fullbrightStrength(LivingEntity entity, float tickDelta, CallbackInfoReturnable<Float> cir) {
        HackModule fb = GuiClient.MODULES.byId(ModuleId.FULLBRIGHT);
        if (fb == null || !fb.isEnabled()) {
            return;
        }
        float s = GuiClient.CONFIG.fullbrightStrength;
        if (Float.isNaN(s) || Float.isInfinite(s)) {
            s = 0.5f;
        }
        s = Math.max(0.0f, Math.min(1.0f, s * 2.0f));
        cir.setReturnValue(Float.valueOf(s));
    }

    @Inject(method={"renderBlur"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$noGuiBlur(CallbackInfo ci) {
        HackModule m = GuiClient.MODULES.byId(ModuleId.NO_GUI_BLUR);
        if (m != null && m.isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method={"updateCrosshairTarget"}, at={@At(value="TAIL")})
    private void gui$antiTrapCrosshair(float tickDelta, CallbackInfo ci) {
        BlockHitResult blockHit;
        Entity cam;
        EntityHitResult ehr;
        Entity entity;
        HitResult current;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        if (AntiTrapUtil.isEnabled() && (current = ((MinecraftClientAccessor)client).getCrosshairTarget()) instanceof EntityHitResult && AntiTrapUtil.shouldIgnore(entity = (ehr = (EntityHitResult)current).getEntity()) && (cam = client.getCameraEntity()) != null) {
            double range = 4.5;
            if (client.player != null) {
                range = client.player.getBlockInteractionRange();
            }
            blockHit = (BlockHitResult)cam.raycast(range, tickDelta, false);
            ((MinecraftClientAccessor)client).setCrosshairTarget((HitResult)blockHit);
            ((MinecraftClientAccessor)client).setTargetedEntity(null);
        }
        FreecamUtil.updateCrosshair(client, tickDelta);
        boolean paintingActive = BetterPaintingUtil.shouldHandlePainting(client);
        if (paintingActive) {
            EntityHitResult paintingHit;
            Entity cam2 = client.getCameraEntity();
            if (cam2 == null) {
                return;
            }
            double range = 4.5;
            if (client.player != null) {
                range = client.player.getBlockInteractionRange();
            }
            Vec3d start = cam2.getCameraPosVec(tickDelta);
            Vec3d dir = cam2.getRotationVec(tickDelta);
            blockHit = BetterPaintingUtil.raycastFromCamera(client, tickDelta, null);
            boolean passThrough = false;
            if (blockHit != null && client.world != null) {
                passThrough = BetterPaintingUtil.isPassThroughBlock(client.world.getBlockState(blockHit.getBlockPos()));
            }
            if ((paintingHit = BetterPaintingUtil.raycastPainting(client, start, dir, range)) != null && (passThrough || blockHit == null)) {
                ((MinecraftClientAccessor)client).setCrosshairTarget((HitResult)paintingHit);
                ((MinecraftClientAccessor)client).setTargetedEntity(paintingHit.getEntity());
            }
        }
        if (paintingActive) {
            BetterHitboxesUtil.clearTarget();
        } else {
            BetterHitboxesUtil.updateCrosshair(client, tickDelta);
        }
    }
}

