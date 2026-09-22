package me.Gui.gui.mixin;

import java.util.Map;
import net.minecraft.entity.player.ItemCooldownManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ItemCooldownManager.class})
public interface ItemCooldownManagerAccessor {
    @Accessor(value="entries")
    public Map<Object, Object> getEntries();

    @Accessor(value="tick")
    public int getTick();
}

