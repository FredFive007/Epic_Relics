package dev.epicrelics.test;

import dev.epicrelics.EpicRelics;
import dev.epicrelics.ability.GravityBladeAbility;
import dev.epicrelics.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/** Development-only deterministic checks for milestone M3 invariants. */
public final class M3DevelopmentChecks {
	private M3DevelopmentChecks() {
	}

	public static void initialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			assertPlunge(0.0, 0.0);
			assertPlunge(4.0, 3.0);
			assertPlunge(8.0, 6.0);
			assertPlunge(10.667, 8.0);
			assertPlunge(20.0, 8.0);

			if (GravityBladeAbility.GRAVITY_RADIUS != 10.0
					|| GravityBladeAbility.GRAVITY_COOLDOWN_TICKS != 200
					|| GravityBladeAbility.SLOWNESS_TICKS != 100
					|| GravityBladeAbility.SLOWNESS_AMPLIFIER != 3) {
				throw new IllegalStateException("M3 gravity field constants are wrong");
			}

			ItemStack blade = ModItems.GRAVITY_BLADE.getDefaultInstance();
			var modifiers = blade.get(DataComponents.ATTRIBUTE_MODIFIERS);
			double damage = modifiers.compute(Attributes.ATTACK_DAMAGE, 1.0, EquipmentSlot.MAINHAND);
			double speed = modifiers.compute(Attributes.ATTACK_SPEED, 4.0, EquipmentSlot.MAINHAND);
			if (Math.abs(damage - 10.0) > 1.0E-6 || Math.abs(speed - 1.6) > 1.0E-6) {
				throw new IllegalStateException("M3 gravity blade attributes are wrong: damage=" + damage + ", speed=" + speed);
			}

			EpicRelics.LOGGER.info("M3 development check passed: plunge formula, gravity field constants, and blade attributes verified");
		});
	}

	private static void assertPlunge(double fallDistance, double expected) {
		double bonus = GravityBladeAbility.plungeBonus(fallDistance);
		if (Math.abs(bonus - expected) > 1.0E-6) {
			throw new IllegalStateException("Wrong plunge bonus at fall distance " + fallDistance + ": " + bonus);
		}
	}
}
