package dev.epicrelics.item;

import dev.epicrelics.EpicRelics;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTabs {
	private static final Identifier ID = Identifier.fromNamespaceAndPath(EpicRelics.MOD_ID, "main");
	private static final ResourceKey<CreativeModeTab> KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ID);

	public static final CreativeModeTab MAIN = RegistryHelper.register(
			BuiltInRegistries.CREATIVE_MODE_TAB,
			KEY,
			FabricCreativeModeTab.builder()
					.title(Component.translatable("itemGroup.epic_relics.main"))
					.icon(() -> new ItemStack(ModItems.GRAVITY_BLADE))
					.displayItems((parameters, output) -> {
						output.accept(ModItems.DRAGON_SIGHT_HELMET);
						output.accept(ModItems.SKYWING_CHESTPLATE);
						output.accept(ModItems.VOIDWALKER_LEGGINGS);
						output.accept(ModItems.HEAVY_CORE_BOOTS);
						output.accept(ModItems.GRAVITY_BLADE);
						output.accept(ModItems.RESONANCE_BOW);
						output.accept(ModItems.NETHERITE_BOW_BLANK);
					})
					.build());

	private ModCreativeTabs() {
	}

	public static void initialize() {
		EpicRelics.LOGGER.info("Registered Epic Relics creative tab");
	}
}
