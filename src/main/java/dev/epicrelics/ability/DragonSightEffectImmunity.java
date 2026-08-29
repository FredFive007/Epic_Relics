package dev.epicrelics.ability;

import net.fabricmc.fabric.api.entity.event.v1.effect.ServerMobEffectEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.effect.MobEffects;

public final class DragonSightEffectImmunity {
	private DragonSightEffectImmunity() {
	}

	public static void initialize() {
		ServerMobEffectEvents.ALLOW_ADD.register((effect, entity, context) ->
				!RelicEquipment.hasHelmet(entity) || (!effect.is(MobEffects.BLINDNESS) && !effect.is(MobEffects.DARKNESS)));
		ServerTickEvents.END_SERVER_TICK.register(server -> server.getPlayerList().getPlayers().forEach(player -> {
			if (RelicEquipment.hasHelmet(player)) {
				player.removeEffect(MobEffects.BLINDNESS);
				player.removeEffect(MobEffects.DARKNESS);
			}
		}));
	}
}
