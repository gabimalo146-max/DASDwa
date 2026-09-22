package me.Gui.gui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets={"net/minecraft/entity/player/ItemCooldownManager$Entry"})
public interface ItemCooldownManagerCooldownAccessor {
    @Accessor(value="startTick")
    public int getStartTick();

    @Accessor(value="endTick")
    public int getEndTick();
}

