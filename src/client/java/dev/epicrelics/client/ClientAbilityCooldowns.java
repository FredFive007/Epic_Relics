package dev.epicrelics.client;

import dev.epicrelics.ability.RelicEquipment;
import dev.epicrelics.item.ModComponents;
import dev.epicrelics.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;

/** Local cooldown estimates for input validation and quiet ready sounds. */
public final class ClientAbilityCooldowns {
	private static int voidTicks;
	private static int gravityTicks;
	private static int sonicTicks;

	private ClientAbilityCooldowns() {
	}

	public static void setVoidStepCooldown(int ticks) {
		voidTicks = Math.max(0, ticks);
	}

	public static void setGravityCooldown(int ticks) {
		gravityTicks = Math.max(0, ticks);
	}

	public static void setSonicCooldown(int ticks) {
		sonicTicks = Math.max(0, ticks);
	}

	public static int voidStepCooldownTicks() {
		return voidTicks;
	}

	public static void reset() {
		voidTicks = 0;
		gravityTicks = 0;
		sonicTicks = 0;
	}

	public static void tick(Minecraft client) {
		if (client.player == null || client.level == null || client.isPaused()) {
			return;
		}
		if (voidTicks > 0) {
			voidTicks--;
		}
		boolean readySound = false;
		if (gravityTicks > 0 && --gravityTicks == 0) {
			readySound = client.player.getMainHandItem().is(ModItems.GRAVITY_BLADE);
		}
		if (sonicTicks > 0 && --sonicTicks == 0) {
			readySound |= RelicEquipment.hasResonanceBow(client.player)
					&& client.player.getMainHandItem().getOrDefault(ModComponents.RESONANCE_MODE, false);
		}
		if (readySound && client.gui.screen() == null && client.player.isAlive() && !client.player.isSpectator()) {
			client.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.16F, 1.65F);
		}
	}
}
