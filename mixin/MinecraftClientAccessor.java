package me.Gui.gui.mixin;

import java.io.File;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={MinecraftClient.class})
public interface MinecraftClientAccessor {
    @Accessor(value="crosshairTarget")
    public HitResult getCrosshairTarget();

    @Accessor(value="crosshairTarget")
    public void setCrosshairTarget(HitResult var1);

    @Accessor(value="targetedEntity")
    public void setTargetedEntity(Entity var1);

    @Accessor(value="runDirectory")
    public File getRunDirectory();

    @Accessor(value="fontManager")
    public FontManager getFontManager();
}

