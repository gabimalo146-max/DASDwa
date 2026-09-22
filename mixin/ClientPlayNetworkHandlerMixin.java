package me.Gui.gui.mixin;

import me.Gui.gui.modules.GearRenderUtil;
import me.Gui.gui.modules.LogoutSpots;
import me.Gui.gui.modules.NoGuiSignUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPlayNetworkHandler.class})
public class ClientPlayNetworkHandlerMixin {
    @Inject(method={"onPlayerRemove"}, at={@At(value="HEAD")})
    private void gui$logoutSpotRemove(PlayerRemoveS2CPacket packet, CallbackInfo ci) {
        LogoutSpots.handlePlayerRemove(packet.profileIds());
    }

    @Inject(method={"onPlayerList"}, at={@At(value="TAIL")})
    private void gui$logoutSpotJoin(PlayerListS2CPacket packet, CallbackInfo ci) {
        if (!packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) {
            return;
        }
        for (PlayerListS2CPacket.Entry entry : packet.getPlayerAdditionEntries()) {
            LogoutSpots.handlePlayerAdd(entry.profileId());
        }
    }

    @Inject(method={"onPlayerPositionLook"}, at={@At(value="HEAD")})
    private void gui$teleportPre(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        GearRenderUtil.onTeleportPacketPre(MinecraftClient.getInstance());
    }

    @Inject(method={"onPlayerPositionLook"}, at={@At(value="TAIL")})
    private void gui$teleportPost(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        GearRenderUtil.onTeleportPacketPost(MinecraftClient.getInstance());
    }

    @Inject(method={"onSignEditorOpen"}, at={@At(value="HEAD")}, cancellable=true)
    private void gui$noGuiSign(SignEditorOpenS2CPacket packet, CallbackInfo ci) {
        if (!NoGuiSignUtil.isEnabled()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.currentScreen instanceof AbstractSignEditScreen) {
            client.setScreen(null);
        }
        ci.cancel();
    }
}

