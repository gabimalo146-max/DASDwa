package me.Gui.gui.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={LivingEntity.class})
public interface LivingEntityAccessor {
    @Accessor(value="jumpingCooldown")
    public void setJumpingCooldown(int var1);

    @Accessor(value="prevBodyYaw")
    public void setPrevBodyYaw(float var1);

    @Accessor(value="prevHeadYaw")
    public void setPrevHeadYaw(float var1);
}

