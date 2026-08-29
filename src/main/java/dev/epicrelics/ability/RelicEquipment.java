package dev.epicrelics.ability;

import dev.epicrelics.item.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public final class RelicEquipment {
	private RelicEquipment() {
	}

	public static boolean hasHelmet(LivingEntity entity) {
		return entity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.DRAGON_SIGHT_HELMET);
	}

	public static boolean hasChestplate(LivingEntity entity) {
		return entity.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.SKYWING_CHESTPLATE);
	}

	public static boolean hasLeggings(LivingEntity entity) {
		return entity.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.VOIDWALKER_LEGGINGS);
	}

	public static boolean hasBoots(LivingEntity entity) {
		return entity.getItemBySlot(EquipmentSlot.FEET).is(ModItems.HEAVY_CORE_BOOTS);
	}

	public static boolean hasResonanceBow(LivingEntity entity) {
		return entity.getMainHandItem().is(ModItems.RESONANCE_BOW);
	}
}
