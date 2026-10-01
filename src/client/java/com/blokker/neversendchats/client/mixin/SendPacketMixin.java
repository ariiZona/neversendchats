package com.blokker.neversendchats.client.mixin;

import com.blokker.neversendchats.client.NeverSendChatsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public class SendPacketMixin {
	@Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
	private void neversendchats$blockOutgoing(Packet<?> packet, CallbackInfo ci) {
		if (!NeverSendChatsClient.muted) return;

		String blocked = null;
		if (packet instanceof ChatMessageC2SPacket p) blocked = p.chatMessage();
		else if (packet instanceof CommandExecutionC2SPacket p) blocked = "/" + p.command();
		else if (packet instanceof ChatCommandSignedC2SPacket p) blocked = "/" + p.command();
		else if (packet instanceof RequestCommandCompletionsC2SPacket) {
			ci.cancel(); // tab-completion leaks partial commands to the server
			return;
		}

		if (blocked != null) {
			ci.cancel();
			MinecraftClient.getInstance().inGameHud.getChatHud()
					.addMessage(Text.literal("[blocked] " + blocked).formatted(Formatting.GRAY));
		}
	}
}