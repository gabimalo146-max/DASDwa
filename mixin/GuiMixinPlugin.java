package me.Gui.gui.mixin;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class GuiMixinPlugin
implements IMixinConfigPlugin {
    private static final Set<String> AXOLOTL_IDS = Set.of("axolotlclient", "axolotlclient-mod", "axolotl_client");
    private static final String AXOLOTL_NAME = "axolotlclient";
    private static boolean hasAxolotlClient = false;

    public void onLoad(String mixinPackage) {
        hasAxolotlClient = GuiMixinPlugin.hasModLike(AXOLOTL_IDS, AXOLOTL_NAME);
    }

    public String getRefMapperConfig() {
        return null;
    }

    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return !hasAxolotlClient || !mixinClassName.endsWith(".MouseMixin");
    }

    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    public List<String> getMixins() {
        return null;
    }

    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    private static boolean hasModLike(Set<String> ids, String nameNeedle) {
        FabricLoader loader = FabricLoader.getInstance();
        String needle = nameNeedle == null ? "" : nameNeedle.toLowerCase(Locale.ROOT);
        for (ModContainer mod : loader.getAllMods()) {
            String idLower;
            String id = mod.getMetadata().getId();
            if (id != null && (ids.contains(idLower = id.toLowerCase(Locale.ROOT)) || !needle.isEmpty() && idLower.contains(needle))) {
                return true;
            }
            String name = mod.getMetadata().getName();
            if (name == null) continue;
            String nameLower = name.toLowerCase(Locale.ROOT);
            if (needle.isEmpty() || !nameLower.contains(needle)) continue;
            return true;
        }
        return false;
    }
}

