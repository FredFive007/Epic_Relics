package dev.epicrelics.test;

import dev.epicrelics.EpicRelics;
import dev.epicrelics.ability.ResonanceBowAbility;
import dev.epicrelics.item.ModComponents;
import dev.epicrelics.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Development-only deterministic checks for milestone M4 invariants. */
public final class M4DevelopmentChecks {
	private M4DevelopmentChecks() {
	}

	public static void initialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			if (ResonanceBowAbility.DARKNESS_RADIUS != 4.0
					|| ResonanceBowAbility.DARKNESS_TICKS != 120
					|| ResonanceBowAbility.SONIC_RANGE != 32.0
					|| ResonanceBowAbility.SONIC_DAMAGE != 16.0F
					|| ResonanceBowAbility.SONIC_HITBOX_MARGIN != 0.5
					|| ResonanceBowAbility.SONIC_COOLDOWN_TICKS != 60) {
				throw new IllegalStateException("M4 resonance bow constants are wrong");
			}

			ItemStack bow = ModItems.RESONANCE_BOW.getDefaultInstance();
			if (bow.getOrDefault(ModComponents.RESONANCE_MODE, false)) {
				throw new IllegalStateException("M4 resonance mode must default to darkness");
			}
			bow.set(ModComponents.RESONANCE_MODE, true);
			if (!bow.getOrDefault(ModComponents.RESONANCE_MODE, false)) {
				throw new IllegalStateException("M4 resonance mode failed to persist sonic=true");
			}
			bow.set(ModComponents.RESONANCE_MODE, null);
			if (bow.getOrDefault(ModComponents.RESONANCE_MODE, false)) {
				throw new IllegalStateException("M4 resonance mode failed to reset to darkness");
			}

			Vec3 rayStart = new Vec3(0.0, 2.5, 0.0);
			Vec3 end = new Vec3(0.0, 2.5, 32.0);
			AABB tallTarget = new AABB(-0.3, 0.0, 9.7, 0.3, 2.9, 10.3);
			AABB clearMiss = new AABB(1.1, 0.0, 9.7, 1.7, 2.9, 10.3);
			if (!Double.isFinite(ResonanceBowAbility.sonicIntersectionDistanceSqr(
					rayStart, end, tallTarget))) {
				throw new IllegalStateException("M4 sonic hitbox ray missed a targeted tall entity");
			}
			if (Double.isFinite(ResonanceBowAbility.sonicIntersectionDistanceSqr(
					rayStart, end, clearMiss))) {
				throw new IllegalStateException("M4 sonic hitbox margin accepted a clear miss");
			}

			EpicRelics.LOGGER.info("M4 development check passed: resonance constants, mode persistence, and sonic hitbox ray verified");
		});
	}
}
