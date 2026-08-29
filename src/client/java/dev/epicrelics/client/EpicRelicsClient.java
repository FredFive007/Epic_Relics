package dev.epicrelics.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.epicrelics.EpicRelics;
import dev.epicrelics.ability.RelicEquipment;
import dev.epicrelics.item.ModComponents;
import dev.epicrelics.network.GravityFieldCooldownPayload;
import dev.epicrelics.network.ResonanceModeRequestPayload;
import dev.epicrelics.network.SonicBoomCooldownPayload;
import dev.epicrelics.network.VoidStepCooldownPayload;
import dev.epicrelics.network.VoidStepRequestPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EpicRelicsClient implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("epic_relics/client");
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(EpicRelics.id("key_category"));
	private static final KeyMapping DRAGON_VISION_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.epic_relics.dragon_vision", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY));
	private static final KeyMapping VOID_STEP_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.epic_relics.void_step", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY));
	private static final KeyMapping RESONANCE_MODE_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.epic_relics.resonance_mode", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY));
	private static int voidStepCooldownTicks;
	private static int gravityFieldCooldownTicks;
	private static int sonicBoomCooldownTicks;

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(VoidStepCooldownPayload.TYPE,
				(payload, context) -> voidStepCooldownTicks = payload.ticks());
		ClientPlayNetworking.registerGlobalReceiver(GravityFieldCooldownPayload.TYPE,
				(payload, context) -> gravityFieldCooldownTicks = payload.ticks());
		ClientPlayNetworking.registerGlobalReceiver(SonicBoomCooldownPayload.TYPE,
				(payload, context) -> sonicBoomCooldownTicks = payload.ticks());
		HudElementRegistry.addLast(EpicRelics.id("void_step_cooldown"), (graphics, deltaTracker) -> {
			if (voidStepCooldownTicks > 0) {
				graphics.centeredText(net.minecraft.client.Minecraft.getInstance().font,
						Component.translatable("hud.epic_relics.void_step_cooldown",
								String.format(java.util.Locale.ROOT, "%.1f", voidStepCooldownTicks / 20.0)),
						graphics.guiWidth() / 2, graphics.guiHeight() - 58, 0xB991FF);
			}
		});
		HudElementRegistry.addLast(EpicRelics.id("gravity_field_cooldown"), (graphics, deltaTracker) -> {
			if (gravityFieldCooldownTicks > 0) {
				graphics.centeredText(net.minecraft.client.Minecraft.getInstance().font,
						Component.translatable("hud.epic_relics.gravity_field_cooldown",
								String.format(java.util.Locale.ROOT, "%.1f", gravityFieldCooldownTicks / 20.0)),
						graphics.guiWidth() / 2, graphics.guiHeight() - 46, 0xE0AFFF);
			}
		});
		HudElementRegistry.addLast(EpicRelics.id("resonance_bow_hud"), (graphics, deltaTracker) -> {
			var minecraft = net.minecraft.client.Minecraft.getInstance();
			if (minecraft.player == null || !RelicEquipment.hasResonanceBow(minecraft.player)) {
				return;
			}
			boolean sonic = minecraft.player.getMainHandItem().getOrDefault(ModComponents.RESONANCE_MODE, false);
			graphics.centeredText(minecraft.font, Component.translatable(sonic
					? "hud.epic_relics.resonance_mode.sonic"
					: "hud.epic_relics.resonance_mode.darkness"),
					graphics.guiWidth() / 2, graphics.guiHeight() - 70, 0x7FD6FF);
			if (sonic && sonicBoomCooldownTicks > 0) {
				graphics.centeredText(minecraft.font, Component.translatable("hud.epic_relics.sonic_boom_cooldown",
						String.format(java.util.Locale.ROOT, "%.1f", sonicBoomCooldownTicks / 20.0)),
						graphics.guiWidth() / 2, graphics.guiHeight() - 58, 0x7FD6FF);
			}
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (voidStepCooldownTicks > 0 && !client.isPaused()) {
				voidStepCooldownTicks--;
			}
			if (gravityFieldCooldownTicks > 0 && !client.isPaused()) {
				gravityFieldCooldownTicks--;
			}
			if (sonicBoomCooldownTicks > 0 && !client.isPaused()) {
				sonicBoomCooldownTicks--;
			}
			while (DRAGON_VISION_KEY.consumeClick()) {
				boolean enabled = ClientRelicState.toggleDragonVision();
				if (client.player != null) {
					client.player.sendOverlayMessage(Component.translatable(
							enabled ? "message.epic_relics.dragon_vision.on" : "message.epic_relics.dragon_vision.off"));
				}
			}
			while (VOID_STEP_KEY.consumeClick()) {
				if (client.player != null && RelicEquipment.hasLeggings(client.player)
						&& voidStepCooldownTicks <= 0 && ClientPlayNetworking.canSend(VoidStepRequestPayload.TYPE)) {
					ClientPlayNetworking.send(VoidStepRequestPayload.INSTANCE);
				}
			}
			while (RESONANCE_MODE_KEY.consumeClick()) {
				if (client.player != null && RelicEquipment.hasResonanceBow(client.player)
						&& ClientPlayNetworking.canSend(ResonanceModeRequestPayload.TYPE)) {
					ClientPlayNetworking.send(ResonanceModeRequestPayload.INSTANCE);
				}
			}
		});
		LOGGER.info("Epic Relics client initialization complete");
	}
}
