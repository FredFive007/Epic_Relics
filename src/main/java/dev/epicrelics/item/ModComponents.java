package dev.epicrelics.item;

import com.mojang.serialization.Codec;
import dev.epicrelics.EpicRelics;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;

public final class ModComponents {
	public static final DataComponentType<Boolean> RESONANCE_MODE = DataComponentType.<Boolean>builder()
			.persistent(Codec.BOOL)
			.networkSynchronized(ByteBufCodecs.BOOL)
			.build();

	private ModComponents() {
	}

	public static void initialize() {
		Registry.register(
				BuiltInRegistries.DATA_COMPONENT_TYPE,
				ResourceKey.create(Registries.DATA_COMPONENT_TYPE, EpicRelics.id("resonance_mode")),
				RESONANCE_MODE);
	}
}
