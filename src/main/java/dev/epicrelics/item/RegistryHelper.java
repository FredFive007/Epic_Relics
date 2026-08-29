package dev.epicrelics.item;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

final class RegistryHelper {
	private RegistryHelper() {
	}

	static <T> T register(Registry<T> registry, ResourceKey<T> key, T value) {
		return Registry.register(registry, key, value);
	}
}
