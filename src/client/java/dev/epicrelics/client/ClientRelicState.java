package dev.epicrelics.client;

import dev.epicrelics.ability.RelicEquipment;
import net.minecraft.client.Minecraft;

public final class ClientRelicState {
	private static boolean dragonVisionEnabled = true;

	private ClientRelicState() {
	}

	public static boolean isDragonVisionActive() {
		Minecraft minecraft = Minecraft.getInstance();
		return dragonVisionEnabled && minecraft.player != null && RelicEquipment.hasHelmet(minecraft.player);
	}

	public static boolean toggleDragonVision() {
		dragonVisionEnabled = !dragonVisionEnabled;
		return dragonVisionEnabled;
	}
}
