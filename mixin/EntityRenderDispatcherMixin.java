package me.Gui.gui.mixin;

import me.Gui.gui.modules.AntiTrapUtil;
import me.Gui.gui.modules.BetterHitboxesUtil;
import me.Gui.gui.modules.FreecamUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={EntityRenderDispatcher.class})
public class EntityRenderDispatcherMixin {
    @Inject(method={"render"}, at={@At(value="HEAD")}, cancellable=true)
    private <E extends Entity> void gui$hideAntiTrapEntities(E entity, double x, double y, double z, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        MinecraftClient client;
        if (FreecamUtil.isActive() && (client = MinecraftClient.getInstance()) != null && entity == client.player) {
            ci.cancel();
            return;
        }
        if (AntiTrapUtil.shouldIgnore(entity)) {
            ci.cancel();
        }
    }

    @Inject(method={"renderHitbox"}, at={@At(value="HEAD")}, cancellable=true)
    private static void gui$renderScaledHitbox(MatrixStack matrices, VertexConsumer vertices, Entity entity, float tickDelta, float red, float green, float blue, CallbackInfo ci) {
        Entity vehicle;
        Box base;
        if (entity == null) {
            return;
        }
        float scaleX = BetterHitboxesUtil.readScaleX();
        float scaleY = BetterHitboxesUtil.readScaleY();
        if (scaleX <= 1.0f && scaleY <= 1.0f) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        Box scaledWorld = BetterHitboxesUtil.getScaledDebugHitbox(client, entity, base = entity.getBoundingBox());
        if (scaledWorld == base) {
            return;
        }
        Box box = scaledWorld.offset(-entity.getX(), -entity.getY(), -entity.getZ());
        VertexRendering.drawBox((MatrixStack)matrices, (VertexConsumer)vertices, (Box)box, (float)red, (float)green, (float)blue, (float)1.0f);
        if (entity instanceof EnderDragonEntity) {
            EnderDragonEntity dragon = (EnderDragonEntity)entity;
            double d = -MathHelper.lerp((double)tickDelta, (double)entity.lastRenderX, (double)entity.getX());
            double e = -MathHelper.lerp((double)tickDelta, (double)entity.lastRenderY, (double)entity.getY());
            double f = -MathHelper.lerp((double)tickDelta, (double)entity.lastRenderZ, (double)entity.getZ());
            for (EnderDragonPart part : dragon.getBodyParts()) {
                matrices.push();
                double g = d + MathHelper.lerp((double)tickDelta, (double)part.lastRenderX, (double)part.getX());
                double h = e + MathHelper.lerp((double)tickDelta, (double)part.lastRenderY, (double)part.getY());
                double i = f + MathHelper.lerp((double)tickDelta, (double)part.lastRenderZ, (double)part.getZ());
                matrices.translate(g, h, i);
                Box partBox = part.getBoundingBox().offset(-part.getX(), -part.getY(), -part.getZ());
                VertexRendering.drawBox((MatrixStack)matrices, (VertexConsumer)vertices, (Box)partBox, (float)0.25f, (float)1.0f, (float)0.0f, (float)1.0f);
                matrices.pop();
            }
        }
        if (entity instanceof LivingEntity) {
            float j = 0.01f;
            VertexRendering.drawBox((MatrixStack)matrices, (VertexConsumer)vertices, (double)box.minX, (double)(entity.getStandingEyeHeight() - j), (double)box.minZ, (double)box.maxX, (double)(entity.getStandingEyeHeight() + j), (double)box.maxZ, (float)1.0f, (float)0.0f, (float)0.0f, (float)1.0f);
        }
        if ((vehicle = entity.getVehicle()) != null) {
            float k = Math.min(vehicle.getWidth(), entity.getWidth()) / 2.0f;
            float l = 0.0625f;
            Vec3d vec3d = vehicle.getPassengerRidingPos(entity).subtract(entity.getPos());
            VertexRendering.drawBox((MatrixStack)matrices, (VertexConsumer)vertices, (double)(vec3d.x - (double)k), (double)vec3d.y, (double)(vec3d.z - (double)k), (double)(vec3d.x + (double)k), (double)(vec3d.y + (double)l), (double)(vec3d.z + (double)k), (float)1.0f, (float)1.0f, (float)0.0f, (float)1.0f);
        }
        VertexRendering.drawVector((MatrixStack)matrices, (VertexConsumer)vertices, (Vector3f)new Vector3f(0.0f, entity.getStandingEyeHeight(), 0.0f), (Vec3d)entity.getRotationVec(tickDelta).multiply(2.0), (int)-16776961);
        ci.cancel();
    }
}

