package dev.epicrelics.ability;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;

public final class RelicDamageRules {
	private RelicDamageRules() {
	}

	public static void initialize() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (!(entity instanceof ServerPlayer player)) {
				return true;
			}
			if (RelicEquipment.hasChestplate(player) && source.is(DamageTypes.FLY_INTO_WALL)) {
				return false;
			}
			if (RelicEquipment.hasLeggings(player) && source.is(DamageTypes.ENDER_PEARL)) {
				return false;
			}
			return !(RelicEquipment.hasBoots(player) && source.is(DamageTypes.FALL));
		});
	}
}
