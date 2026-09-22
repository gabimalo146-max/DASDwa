package me.Gui.gui.mixin;

import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FontStorage;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={FontManager.class})
public interface FontManagerAccessor {
    @Invoker(value="getStorage")
    public FontStorage gui$getStorage(Identifier var1);
}

