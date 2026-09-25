package dev.epicrelics.test;

import dev.epicrelics.EpicRelics;
import dev.epicrelics.item.ModItems;
import dev.epicrelics.item.RelicArmorEnchantments;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

/** Development-only acceptance checks; never registered in a production launch. */
public final class M1DevelopmentChecks {
	private M1DevelopmentChecks() {
	}

	public static void initialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			verifyGrindstoneMixinTargets();
			var enchantments = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
			var mending = enchantments.getOrThrow(Enchantments.MENDING);
			var cases = List.of(
					new SmithingCase("dragon_sight_helmet_smithing", Items.DRAGON_HEAD, Items.NETHERITE_HELMET, Items.NETHER_STAR, ModItems.DRAGON_SIGHT_HELMET),
					new SmithingCase("skywing_chestplate_smithing", Items.ELYTRA, Items.NETHERITE_CHESTPLATE, Items.NETHER_STAR, ModItems.SKYWING_CHESTPLATE),
					new SmithingCase("voidwalker_leggings_smithing", Items.DRAGON_EGG, Items.NETHERITE_LEGGINGS, Items.NETHER_STAR, ModItems.VOIDWALKER_LEGGINGS),
					new SmithingCase("heavy_core_boots_smithing", Items.HEAVY_CORE, Items.NETHERITE_BOOTS, Items.NETHER_STAR, ModItems.HEAVY_CORE_BOOTS),
					new SmithingCase("gravity_blade_smithing", Items.MACE, Items.NETHERITE_SWORD, Items.NETHER_STAR, ModItems.GRAVITY_BLADE),
					new SmithingCase("resonance_bow_smithing", Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, ModItems.NETHERITE_BOW_BLANK, Items.NETHER_STAR, ModItems.RESONANCE_BOW),
					new SmithingCase("netherite_bow_blank_smithing", Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, Items.BOW, Items.NETHERITE_INGOT, ModItems.NETHERITE_BOW_BLANK));

			for (SmithingCase testCase : cases) {
				ItemStack base = new ItemStack(testCase.base());
				boolean relicArmorOutput = RelicArmorEnchantments.isRelicArmor(new ItemStack(testCase.output()));
				if (relicArmorOutput) {
					EnchantmentHelper.updateEnchantments(base, mutable -> {
						mutable.set(mending, 1);
						mutable.set(enchantments.getOrThrow(Enchantments.PROTECTION), 2);
					});
				}
				var input = new SmithingRecipeInput(
						new ItemStack(testCase.template()),
						base,
						new ItemStack(testCase.addition()));
				var holder = server.getRecipeManager()
						.getRecipeFor(RecipeType.SMITHING, input, server.overworld())
						.orElseThrow(() -> new IllegalStateException("No smithing recipe matched " + testCase.id()));
				Identifier actualId = holder.id().identifier();
				ItemStack actualStack = holder.value().assemble(input);
				Item actualOutput = actualStack.getItem();
				if (!actualId.equals(id(testCase.id())) || actualOutput != testCase.output()) {
					throw new IllegalStateException("Wrong smithing result for " + testCase.id()
							+ ": recipe=" + actualId + ", output=" + actualOutput);
				}
				if (relicArmorOutput) {
					RelicArmorEnchantments.enforceInnateProtections(actualStack, server.registryAccess());
					if (!RelicArmorEnchantments.hasInnateProtections(actualStack, server.registryAccess())
							|| EnchantmentHelper.getItemEnchantmentLevel(mending, actualStack) != 1) {
						throw new IllegalStateException("Relic armor smithing did not preserve other enchantments "
								+ "and enforce four protection V enchantments for " + testCase.id());
					}
				}
			}

			ItemStack grindstoneInput = new ItemStack(ModItems.DRAGON_SIGHT_HELMET);
			RelicArmorEnchantments.enforceInnateProtections(grindstoneInput, server.registryAccess());
			if (RelicArmorEnchantments.getGrindstoneExperience(grindstoneInput) != 0) {
				throw new IllegalStateException("Innate relic armor protections must not yield grindstone experience");
			}
			EnchantmentHelper.updateEnchantments(grindstoneInput, mutable -> mutable.set(mending, 1));
			if (RelicArmorEnchantments.getGrindstoneExperience(grindstoneInput) <= 0) {
				throw new IllegalStateException("Ordinary relic armor enchantments must still yield grindstone experience");
			}
			ItemStack grindstoneResult = grindstoneInput.copy();
			EnchantmentHelper.updateEnchantments(grindstoneResult,
					mutable -> mutable.removeIf(enchantment -> !enchantment.is(EnchantmentTags.CURSE)));
			RelicArmorEnchantments.enforceInnateProtections(grindstoneResult, server.registryAccess());
			if (!RelicArmorEnchantments.hasInnateProtections(grindstoneResult, server.registryAccess())
					|| EnchantmentHelper.getItemEnchantmentLevel(mending, grindstoneResult) != 0) {
				throw new IllegalStateException("Grindstone must retain innate protections and remove ordinary enchantments");
			}

			var finalItems = List.of(
					ModItems.DRAGON_SIGHT_HELMET,
					ModItems.SKYWING_CHESTPLATE,
					ModItems.VOIDWALKER_LEGGINGS,
					ModItems.HEAVY_CORE_BOOTS,
					ModItems.GRAVITY_BLADE,
					ModItems.RESONANCE_BOW);
			for (Item item : finalItems) {
				ItemStack stack = item.getDefaultInstance();
				if (stack.getRarity() != Rarity.EPIC
						|| stack.getMaxDamage() <= 0
						|| !stack.has(DataComponents.UNBREAKABLE)
						|| !stack.has(DataComponents.DAMAGE_RESISTANT)
						|| !stack.has(DataComponents.ENCHANTABLE)) {
					throw new IllegalStateException("Final item is missing an M1 invariant: " + item);
				}
			}

			var armorCases = List.of(
					new ArmorCase(ModItems.DRAGON_SIGHT_HELMET, EquipmentSlot.HEAD, ItemTags.HEAD_ARMOR, 5.0),
					new ArmorCase(ModItems.SKYWING_CHESTPLATE, EquipmentSlot.CHEST, ItemTags.CHEST_ARMOR, 10.0),
					new ArmorCase(ModItems.VOIDWALKER_LEGGINGS, EquipmentSlot.LEGS, ItemTags.LEG_ARMOR, 9.0),
					new ArmorCase(ModItems.HEAVY_CORE_BOOTS, EquipmentSlot.FEET, ItemTags.FOOT_ARMOR, 6.0));
			double totalArmor = 0.0;
			for (ArmorCase armorCase : armorCases) {
				ItemStack stack = armorCase.item().getDefaultInstance();
				var modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
				double armor = modifiers.compute(Attributes.ARMOR, 0.0, armorCase.slot());
				double toughness = modifiers.compute(Attributes.ARMOR_TOUGHNESS, 0.0, armorCase.slot());
				if (!armorCase.item().builtInRegistryHolder().is(armorCase.tag())
						|| armor != armorCase.armor()
						|| toughness != 3.0) {
					throw new IllegalStateException("Armor attribute/tag invariant failed for " + armorCase.item());
				}
				totalArmor += armor;
			}
			if (totalArmor != 30.0
					|| !ModItems.GRAVITY_BLADE.builtInRegistryHolder().is(ItemTags.SWORDS)
					|| !ModItems.RESONANCE_BOW.builtInRegistryHolder().is(ItemTags.BOW_ENCHANTABLE)) {
				throw new IllegalStateException("M1 armor total or weapon enchantment tag invariant failed");
			}

			EpicRelics.LOGGER.info("M1 development check passed: recipes, innate enchantments, grindstone handling, and 30-point armor set verified");
		});
	}

	private static void verifyGrindstoneMixinTargets() {
		try {
			Class.forName("net.minecraft.world.inventory.GrindstoneMenu");
			Class.forName("net.minecraft.world.inventory.GrindstoneMenu$4");
		} catch (ClassNotFoundException exception) {
			throw new IllegalStateException("Grindstone mixin target changed", exception);
		}
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(EpicRelics.MOD_ID, path);
	}

	private record SmithingCase(String id, Item template, Item base, Item addition, Item output) {
	}

	private record ArmorCase(Item item, EquipmentSlot slot, net.minecraft.tags.TagKey<Item> tag, double armor) {
	}
}
