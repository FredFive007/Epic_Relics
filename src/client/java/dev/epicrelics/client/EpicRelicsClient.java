package dev.epicrelics.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.epicrelics.EpicRelics;
import dev.epicrelics.ability.RelicEquipment;
import dev.epicrelics.network.GravityFieldCooldownPayload;
import dev.epicrelics.network.ResonanceModeRequestPayload;
import dev.epicrelics.network.SonicBoomCooldownPayload;
import dev.epicrelics.network.VoidStepCooldownPayload;
import dev.epicrelics.network.VoidStepRequestPayload;
import java.util.Locale;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EpicRelicsClient implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("epic_relics/client");
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(EpicRelics.id("key_category"));
	private static final KeyMapping DRAGON_VISION_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.epic_relics.dragon_vision", InputConstants.Type.KEYBOARD, InputConstants.KEY_V, CATEGORY));
	private static final KeyMapping VOID_STEP_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.epic_relics.void_step", InputConstants.Type.KEYBOARD, InputConstants.KEY_R, CATEGORY));
	private static final KeyMapping RESONANCE_MODE_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.epic_relics.resonance_mode", InputConstants.Type.KEYBOARD, InputConstants.KEY_G, CATEGORY));
	private static int feedbackCooldown;

	@Override
	public void onInitializeClient() {
		dev.epicrelics.client.render.RelicVisuals.initialize();
		dev.epicrelics.client.test.VisualCheckClient.initialize();
		ClientPlayNetworking.registerGlobalReceiver(VoidStepCooldownPayload.TYPE,
				(payload, context) -> ClientAbilityCooldowns.setVoidStepCooldown(payload.ticks()));
		ClientPlayNetworking.registerGlobalReceiver(GravityFieldCooldownPayload.TYPE,
				(payload, context) -> ClientAbilityCooldowns.setGravityCooldown(payload.ticks()));
		ClientPlayNetworking.registerGlobalReceiver(SonicBoomCooldownPayload.TYPE,
				(payload, context) -> ClientAbilityCooldowns.setSonicCooldown(payload.ticks()));
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> resetSession());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> resetSession());
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ClientAbilityCooldowns.tick(client);
			VoidStepPreview.tick(client);
			if (feedbackCooldown > 0 && !client.isPaused()) {
				feedbackCooldown--;
			}
			while (DRAGON_VISION_KEY.consumeClick()) {
				if (!canUseKeys(client)) {
					continue;
				}
				if (!RelicEquipment.hasHelmet(client.player)) {
					feedback(client, Component.translatable("message.epic_relics.requires_helmet"));
					continue;
				}
				boolean enabled = ClientRelicState.toggleDragonVision();
				client.player.sendOverlayMessage(Component.translatable(
						enabled ? "message.epic_relics.dragon_vision.on" : "message.epic_relics.dragon_vision.off"));
			}
			while (VOID_STEP_KEY.consumeClick()) {
				if (!canUseKeys(client)) {
					continue;
				}
				if (!RelicEquipment.hasLeggings(client.player)) {
					feedback(client, Component.translatable("message.epic_relics.requires_leggings"));
				} else if (ClientAbilityCooldowns.voidStepCooldownTicks() > 0) {
					feedback(client, Component.translatable("message.epic_relics.void_step_wait",
							String.format(Locale.ROOT, "%.1f", ClientAbilityCooldowns.voidStepCooldownTicks() / 20.0)));
				} else if (ClientPlayNetworking.canSend(VoidStepRequestPayload.TYPE)) {
					ClientPlayNetworking.send(VoidStepRequestPayload.INSTANCE);
				}
			}
			while (RESONANCE_MODE_KEY.consumeClick()) {
				if (!canUseKeys(client)) {
					continue;
				}
				if (!RelicEquipment.hasResonanceBow(client.player)) {
					feedback(client, Component.translatable("message.epic_relics.requires_bow"));
				} else if (ClientPlayNetworking.canSend(ResonanceModeRequestPayload.TYPE)) {
					ClientPlayNetworking.send(ResonanceModeRequestPayload.INSTANCE);
				}
			}
		});
		LOGGER.info("Epic Relics client initialization complete");
	}

	private static boolean canUseKeys(Minecraft client) {
		return client.player != null && client.player.isAlive() && !client.player.isSpectator()
				&& client.gui.screen() == null && !client.isPaused();
	}

	private static void feedback(Minecraft client, Component message) {
		if (feedbackCooldown == 0 && client.player != null) {
			client.player.sendOverlayMessage(message);
			feedbackCooldown = 10;
		}
	}

	private static void resetSession() {
		ClientAbilityCooldowns.reset();
		VoidStepPreview.reset();
		ClientRelicState.reset();
		feedbackCooldown = 0;
	}
}
