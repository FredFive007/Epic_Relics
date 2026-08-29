package dev.epicrelics.item;

import dev.epicrelics.EpicRelics;
import java.util.function.Function;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.util.Unit;

public final class ModItems {
	private static final AttributeModifier.Operation ADD = AttributeModifier.Operation.ADD_VALUE;
	public static final ResourceKey<EquipmentAsset> DRAGON_SIGHT_EQUIPMENT_ASSET = ResourceKey.create(
			EquipmentAssets.ROOT_ID, EpicRelics.id("dragon_sight_helmet"));
	public static final ResourceKey<EquipmentAsset> SKYWING_EQUIPMENT_ASSET = ResourceKey.create(
			EquipmentAssets.ROOT_ID, EpicRelics.id("skywing_chestplate"));
	public static final ResourceKey<EquipmentAsset> VOIDWALKER_EQUIPMENT_ASSET = ResourceKey.create(
			EquipmentAssets.ROOT_ID, EpicRelics.id("voidwalker_leggings"));
	public static final ResourceKey<EquipmentAsset> HEAVY_CORE_EQUIPMENT_ASSET = ResourceKey.create(
			EquipmentAssets.ROOT_ID, EpicRelics.id("heavy_core_boots"));

	public static final Item DRAGON_SIGHT_HELMET = registerArmor(
			"dragon_sight_helmet", EquipmentSlot.HEAD, EquipmentSlotGroup.HEAD, 407, 5.0, 0.0,
			DRAGON_SIGHT_EQUIPMENT_ASSET);
	public static final Item SKYWING_CHESTPLATE = registerArmor(
			"skywing_chestplate", EquipmentSlot.CHEST, EquipmentSlotGroup.CHEST, 592, 10.0, 0.0,
			SKYWING_EQUIPMENT_ASSET);
	public static final Item VOIDWALKER_LEGGINGS = registerArmor(
			"voidwalker_leggings", EquipmentSlot.LEGS, EquipmentSlotGroup.LEGS, 555, 9.0, 0.0,
			VOIDWALKER_EQUIPMENT_ASSET);
	public static final Item HEAVY_CORE_BOOTS = registerArmor(
			"heavy_core_boots", EquipmentSlot.FEET, EquipmentSlotGroup.FEET, 481, 6.0, 0.5,
			HEAVY_CORE_EQUIPMENT_ASSET);

	public static final Item GRAVITY_BLADE = register("gravity_blade", GravityBladeItem::new,
			new Item.Properties()
					.stacksTo(1)
					.durability(2031)
					.rarity(Rarity.EPIC)
					.fireResistant()
					.enchantable(15)
					.component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
					.component(DataComponents.WEAPON, new Weapon(1))
					.attributes(ItemAttributeModifiers.builder()
							.add(Attributes.ATTACK_DAMAGE,
									new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 9.0, ADD),
									EquipmentSlotGroup.MAINHAND)
							.add(Attributes.ATTACK_SPEED,
									new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.4, ADD),
									EquipmentSlotGroup.MAINHAND)
							.build()));

	public static final Item RESONANCE_BOW = register("resonance_bow", ResonanceBowItem::new,
			new Item.Properties()
					.stacksTo(1)
					.durability(384)
					.rarity(Rarity.EPIC)
					.fireResistant()
					.enchantable(1)
					.component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
					.component(DataComponents.WEAPON, new Weapon(1)));

	public static final Item NETHERITE_BOW_BLANK = register("netherite_bow_blank", Item::new,
			new Item.Properties()
					.stacksTo(1)
					.rarity(Rarity.UNCOMMON)
					.fireResistant());

	private ModItems() {
	}

	public static void initialize() {
		EpicRelics.LOGGER.info("Registered 7 Epic Relics milestone M1 items");
	}

	private static Item registerArmor(
			String path,
			EquipmentSlot slot,
			EquipmentSlotGroup slotGroup,
			int durability,
			double armor,
			double knockbackResistance,
			ResourceKey<EquipmentAsset> equipmentAsset) {
		var attributes = ItemAttributeModifiers.builder()
				.add(Attributes.ARMOR,
						new AttributeModifier(id("armor." + path), armor, ADD), slotGroup)
				.add(Attributes.ARMOR_TOUGHNESS,
						new AttributeModifier(id("armor_toughness." + path), 3.0, ADD), slotGroup);

		if (knockbackResistance > 0.0) {
			attributes.add(Attributes.KNOCKBACK_RESISTANCE,
					new AttributeModifier(id("knockback_resistance." + path), knockbackResistance, ADD),
					slotGroup);
		}

		var equippable = Equippable.builder(slot)
				.setEquipSound(SoundEvents.ARMOR_EQUIP_NETHERITE)
				.setAsset(equipmentAsset)
				.build();

		var properties = new Item.Properties()
				.stacksTo(1)
				.durability(durability)
				.rarity(Rarity.EPIC)
				.fireResistant()
				.enchantable(15)
				.component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
				.component(DataComponents.EQUIPPABLE, equippable)
				.attributes(attributes.build());
		if (slot == EquipmentSlot.CHEST) {
			properties.component(DataComponents.GLIDER, Unit.INSTANCE);
		}
		return register(path, itemProperties -> new RelicArmorItem(itemProperties, path), properties);
	}

	private static <T extends Item> T register(String path, Function<Item.Properties, T> factory, Item.Properties properties) {
		Identifier identifier = id(path);
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, identifier);
		T item = factory.apply(properties.setId(key));
		RegistryHelper.register(BuiltInRegistries.ITEM, key, item);
		return item;
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(EpicRelics.MOD_ID, path);
	}
}
