package dev.epicrelics.test;

import dev.epicrelics.EpicRelics;
import dev.epicrelics.ability.RelicParticles;
import dev.epicrelics.item.GravityBladeItem;
import dev.epicrelics.item.ModItems;
import dev.epicrelics.item.RelicArmorItem;
import dev.epicrelics.item.ResonanceBowItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.world.phys.Vec3;

/** Development-only deterministic checks for milestone M5 tooltip and particle integration. */
public final class M5DevelopmentChecks {
	private M5DevelopmentChecks() {
	}

	public static void initialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			if (!(ModItems.DRAGON_SIGHT_HELMET instanceof RelicArmorItem)
					|| !(ModItems.SKYWING_CHESTPLATE instanceof RelicArmorItem)
					|| !(ModItems.VOIDWALKER_LEGGINGS instanceof RelicArmorItem)
					|| !(ModItems.HEAVY_CORE_BOOTS instanceof RelicArmorItem)
					|| !(ModItems.GRAVITY_BLADE instanceof GravityBladeItem)
					|| !(ModItems.RESONANCE_BOW instanceof ResonanceBowItem)) {
				throw new IllegalStateException("M5 final relic tooltip item classes are incomplete");
			}

			Vec3 center = new Vec3(3.0, 7.0, -2.0);
			Vec3 east = RelicParticles.ringPoint(center, 4.0, 0, 4);
			Vec3 south = RelicParticles.ringPoint(center, 4.0, 1, 4);
			if (east.distanceTo(new Vec3(7.0, 7.0, -2.0)) > 1.0E-9
					|| south.distanceTo(new Vec3(3.0, 7.0, 2.0)) > 1.0E-9) {
				throw new IllegalStateException("M5 particle ring geometry is wrong");
			}

			EpicRelics.LOGGER.info("M5 development check passed: six relic tooltip classes and particle ring geometry verified");
		});
	}
}
