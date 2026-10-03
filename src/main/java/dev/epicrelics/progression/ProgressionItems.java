package dev.epicrelics.progression;

import dev.epicrelics.EpicRelics;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** Optional, once-per-player exploration rewards; legacy smithing recipes stay available. */
public final class ProgressionItems {
	public static final Item DRAGON_SIGHT_SIGIL = register("dragon_sight_sigil");
	public static final Item SKYWING_SIGIL = register("skywing_sigil");
	public static final Item VOIDWALKER_SIGIL = register("voidwalker_sigil");
	public static final Item HEAVY_CORE_SIGIL = register("heavy_core_sigil");
	public static final Item GRAVITY_SIGIL = register("gravity_sigil");
	public static final Item RESONANCE_SIGIL = register("resonance_sigil");
	public static final List<Item> ALL_SIGILS = List.of(DRAGON_SIGHT_SIGIL, SKYWING_SIGIL,
			VOIDWALKER_SIGIL, HEAVY_CORE_SIGIL, GRAVITY_SIGIL, RESONANCE_SIGIL);

	private ProgressionItems() {
	}

	private static Item register(String path) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, EpicRelics.id(path));
		return Registry.register(BuiltInRegistries.ITEM, key,
				new SigilItem(new Item.Properties().setId(key).stacksTo(1).rarity(Rarity.RARE).fireResistant()));
	}

	public static void initialize() {
		EpicRelics.LOGGER.info("Registered six optional relic trial sigils");
	}

	private static final class SigilItem extends Item {
		private SigilItem(Properties properties) {
			super(properties);
		}

		@Override
		public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
				Consumer<Component> builder, TooltipFlag flag) {
			super.appendHoverText(stack, context, display, builder, flag);
			builder.accept(Component.translatable("tooltip.epic_relics.sigil.origin").withStyle(ChatFormatting.AQUA));
			builder.accept(Component.translatable("tooltip.epic_relics.sigil.smithing").withStyle(ChatFormatting.GRAY));
			builder.accept(Component.translatable("tooltip.epic_relics.sigil.legacy").withStyle(ChatFormatting.DARK_GRAY));
		}
	}
}
