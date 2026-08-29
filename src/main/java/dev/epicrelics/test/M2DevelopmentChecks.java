package dev.epicrelics.test;

import dev.epicrelics.EpicRelics;
import dev.epicrelics.ability.StompAbility;
import dev.epicrelics.ability.VoidStepAbility;
import dev.epicrelics.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.phys.Vec3;

/** Development-only deterministic checks for milestone M2 invariants. */
public final class M2DevelopmentChecks {
	private M2DevelopmentChecks() {
	}

	public static void initialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			if (!ModItems.SKYWING_CHESTPLATE.getDefaultInstance().has(DataComponents.GLIDER)
					|| ModItems.DRAGON_SIGHT_HELMET.getDefaultInstance().has(DataComponents.GLIDER)
					|| ModItems.VOIDWALKER_LEGGINGS.getDefaultInstance().has(DataComponents.GLIDER)
					|| ModItems.HEAVY_CORE_BOOTS.getDefaultInstance().has(DataComponents.GLIDER)) {
				throw new IllegalStateException("M2 glider component must exist on Skywing chestplate only");
			}

			Vec3 start = new Vec3(3.25, 70.0, -8.75);
			Vec3 end = VoidStepAbility.missEndpoint(start, new Vec3(2.0, -1.0, 4.0));
			if (Math.abs(start.distanceTo(end) - VoidStepAbility.RANGE) > 1.0E-9) {
				throw new IllegalStateException("Void Step miss endpoint is not exactly 54 blocks");
			}

			assertTier(4.0, 3.5, 4.0F, 0.8);
			assertTier(7.99, 3.5, 4.0F, 0.8);
			assertTier(8.0, 5.0, 7.0F, 1.2);
			assertTier(15.99, 5.0, 7.0F, 1.2);
			assertTier(16.0, 7.0, 10.0F, 1.6);

			EpicRelics.LOGGER.info("M2 development check passed: chest-only glider, exact 54-block ray, and stomp tier boundaries verified");
		});
	}

	private static void assertTier(double distance, double radius, float damage, double knockback) {
		StompAbility.Tier tier = StompAbility.Tier.forDistance(distance);
		if (tier.radius() != radius || tier.damage() != damage || tier.knockback() != knockback) {
			throw new IllegalStateException("Wrong stomp tier at fall distance " + distance);
		}
	}
}
