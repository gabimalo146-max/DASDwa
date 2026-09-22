package me.Gui.gui.mixin;

import me.Gui.gui.modules.FreecamUtil;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Camera.class})
public abstract class CameraMixin {
    @Shadow
    protected abstract void setPos(Vec3d var1);

    @Shadow
    protected abstract void setRotation(float var1, float var2);

    @Inject(method={"update"}, at={@At(value="TAIL")})
    private void gui$freecamCamera(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (!FreecamUtil.isActive() || FreecamUtil.followBody()) {
            return;
        }
        Vec3d pos = FreecamUtil.getCameraPos(tickDelta);
        float yaw = FreecamUtil.getCameraYaw(tickDelta);
        float pitch = FreecamUtil.getCameraPitch(tickDelta);
        this.setPos(pos);
        this.setRotation(yaw, pitch);
    }
}

