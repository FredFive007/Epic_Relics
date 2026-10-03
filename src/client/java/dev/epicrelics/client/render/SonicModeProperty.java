package dev.epicrelics.client.render;

import com.mojang.serialization.MapCodec;
import dev.epicrelics.item.ModComponents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/** Reads the existing server-synchronized component; there is no client-only mode cache. */
public record SonicModeProperty() implements ConditionalItemModelProperty {
	public static final MapCodec<SonicModeProperty> MAP_CODEC = MapCodec.unit(new SonicModeProperty());

	@Override
	public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner,
			int seed, ItemDisplayContext context) {
		return stack.getOrDefault(ModComponents.RESONANCE_MODE, false);
	}

	@Override
	public MapCodec<SonicModeProperty> type() {
		return MAP_CODEC;
	}
}
