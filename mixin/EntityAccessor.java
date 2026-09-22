package me.Gui.gui.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={Entity.class})
public interface EntityAccessor {
    @Accessor(value="prevX")
    public void setPrevX(double var1);

    @Accessor(value="prevY")
    public void setPrevY(double var1);

    @Accessor(value="prevZ")
    public void setPrevZ(double var1);

    @Accessor(value="lastRenderX")
    public void setLastRenderX(double var1);

    @Accessor(value="lastRenderY")
    public void setLastRenderY(double var1);

    @Accessor(value="lastRenderZ")
    public void setLastRenderZ(double var1);

    @Accessor(value="prevYaw")
    public void setPrevYaw(float var1);

    @Accessor(value="prevPitch")
    public void setPrevPitch(float var1);

    @Accessor(value="horizontalCollision")
    public boolean gui$getHorizontalCollision();

    @Accessor(value="boundingBox")
    public Box gui$getBoundingBox();
}

