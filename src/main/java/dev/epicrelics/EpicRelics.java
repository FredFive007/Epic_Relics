package dev.epicrelics;

import dev.epicrelics.item.ModCreativeTabs;
import dev.epicrelics.item.ModComponents;
import dev.epicrelics.item.ModItems;
import dev.epicrelics.ability.RelicDamageRules;
import dev.epicrelics.ability.VoidStepAbility;
import dev.epicrelics.ability.DragonSightEffectImmunity;
import dev.epicrelics.ability.GravityBladeAbility;
import dev.epicrelics.ability.ResonanceBowAbility;
import dev.epicrelics.test.M1DevelopmentChecks;
import dev.epicrelics.test.M2DevelopmentChecks;
import dev.epicrelics.test.M3DevelopmentChecks;
import dev.epicrelics.test.M4DevelopmentChecks;
import dev.epicrelics.test.M5DevelopmentChecks;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EpicRelics implements ModInitializer {
	public static final String MOD_ID = "epic_relics";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static net.minecraft.resources.Identifier id(String path) {
		return net.minecraft.resources.Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModComponents.initialize();
		ModItems.initialize();
		ModCreativeTabs.initialize();
		RelicDamageRules.initialize();
		DragonSightEffectImmunity.initialize();
		VoidStepAbility.initialize();
		GravityBladeAbility.initialize();
		ResonanceBowAbility.initialize();
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			M1DevelopmentChecks.initialize();
			M2DevelopmentChecks.initialize();
			M3DevelopmentChecks.initialize();
			M4DevelopmentChecks.initialize();
			M5DevelopmentChecks.initialize();
		}
		LOGGER.info("Epic Relics common initialization complete");
	}
}
