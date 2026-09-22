package me.Gui.gui.mixin;

import java.util.List;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={LivingEntityRenderer.class})
public interface LivingEntityRendererAccessor {
    @Accessor(value="features")
    public List<FeatureRenderer<?, ?>> getFeatures();
}

