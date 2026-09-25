package dev.epicrelics.item;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class RelicArmorEnchantments {
	public static final int INNATE_PROTECTION_LEVEL = 5;
	private static final List<ResourceKey<Enchantment>> INNATE_PROTECTIONS = List.of(
			Enchantments.PROTECTION,
			Enchantments.FIRE_PROTECTION,
			Enchantments.BLAST_PROTECTION,
			Enchantments.PROJECTILE_PROTECTION);

	private RelicArmorEnchantments() {
	}

	public static boolean isRelicArmor(ItemStack stack) {
		return stack.is(ModItems.DRAGON_SIGHT_HELMET)
				|| stack.is(ModItems.SKYWING_CHESTPLATE)
				|| stack.is(ModItems.VOIDWALKER_LEGGINGS)
				|| stack.is(ModItems.HEAVY_CORE_BOOTS);
	}

	public static void enforceInnateProtections(ItemStack stack, RegistryAccess registryAccess) {
		if (!isRelicArmor(stack)) {
			return;
		}

		var enchantments = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
		EnchantmentHelper.updateEnchantments(stack, mutable -> {
			for (ResourceKey<Enchantment> protection : INNATE_PROTECTIONS) {
				mutable.set(enchantments.getOrThrow(protection), INNATE_PROTECTION_LEVEL);
			}
		});
	}

	public static boolean hasInnateProtections(ItemStack stack, RegistryAccess registryAccess) {
		if (!isRelicArmor(stack)) {
			return false;
		}

		var enchantments = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
		for (ResourceKey<Enchantment> protection : INNATE_PROTECTIONS) {
			if (EnchantmentHelper.getItemEnchantmentLevel(enchantments.getOrThrow(protection), stack)
					!= INNATE_PROTECTION_LEVEL) {
				return false;
			}
		}
		return true;
	}

	public static int getGrindstoneExperience(ItemStack stack) {
		int experience = 0;
		ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
		for (var entry : enchantments.entrySet()) {
			Holder<Enchantment> enchantment = entry.getKey();
			if (!enchantment.is(EnchantmentTags.CURSE)
					&& (!isRelicArmor(stack) || !isInnateProtection(enchantment))) {
				experience += enchantment.value().getMinCost(entry.getIntValue());
			}
		}
		return experience;
	}

	private static boolean isInnateProtection(Holder<Enchantment> enchantment) {
		for (ResourceKey<Enchantment> protection : INNATE_PROTECTIONS) {
			if (enchantment.is(protection)) {
				return true;
			}
		}
		return false;
	}
}
