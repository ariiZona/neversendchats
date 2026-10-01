package com.blokker.neversendchats.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class NeverSendChatsClient implements ClientModInitializer {
	public static boolean muted = false;

	@Override
	public void onInitializeClient() {
		KeyBinding toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.neversendchats.toggle",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_M,
				"category.neversendchats"));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.wasPressed()) {
				muted = !muted;
				if (client.inGameHud != null) {
					client.inGameHud.setOverlayMessage(
							Text.literal("Outgoing chat: " + (muted ? "BLOCKED" : "open")), false);
				}
			}
		});

		ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
			if (!muted) return true;
			echoBlocked(message);
			return false;
		});

		ClientSendMessageEvents.ALLOW_COMMAND.register(command -> {
			if (!muted) return true;
			echoBlocked("/" + command);
			return false;
		});
	}

	private static void echoBlocked(String text) {
		MinecraftClient.getInstance().inGameHud.getChatHud()
				.addMessage(Text.literal("[blocked] " + text).formatted(Formatting.GRAY));
	}
}