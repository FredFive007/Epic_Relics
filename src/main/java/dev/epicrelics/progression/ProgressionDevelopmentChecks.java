package dev.epicrelics.progression;

import dev.epicrelics.EpicRelics;
import dev.epicrelics.item.ModItems;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/** Server-path checks against the loaded recipe, advancement and reward registries. */
public final class ProgressionDevelopmentChecks {
	private ProgressionDevelopmentChecks() {
	}

	public static void initialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			var cases = List.of(
					new TrialCase("dragon_sight", "dragon_sight_helmet", ProgressionItems.DRAGON_SIGHT_SIGIL, Items.NETHERITE_HELMET, ModItems.DRAGON_SIGHT_HELMET),
					new TrialCase("skywing", "skywing_chestplate", ProgressionItems.SKYWING_SIGIL, Items.NETHERITE_CHESTPLATE, ModItems.SKYWING_CHESTPLATE),
					new TrialCase("voidwalker", "voidwalker_leggings", ProgressionItems.VOIDWALKER_SIGIL, Items.NETHERITE_LEGGINGS, ModItems.VOIDWALKER_LEGGINGS),
					new TrialCase("heavy_core", "heavy_core_boots", ProgressionItems.HEAVY_CORE_SIGIL, Items.NETHERITE_BOOTS, ModItems.HEAVY_CORE_BOOTS),
					new TrialCase("gravity", "gravity_blade", ProgressionItems.GRAVITY_SIGIL, Items.NETHERITE_SWORD, ModItems.GRAVITY_BLADE),
					new TrialCase("resonance", "resonance_bow", ProgressionItems.RESONANCE_SIGIL, ModItems.NETHERITE_BOW_BLANK, ModItems.RESONANCE_BOW));
			var level = server.overworld();
			// This unspawned entity supplies the reward context without adding entities to the test world.
			var contextEntity = new ItemEntity(level, 0.0, 0.0, 0.0, new ItemStack(Items.STONE));
			var lootContext = new LootParams.Builder(level)
					.withParameter(LootContextParams.THIS_ENTITY, contextEntity)
					.withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
					.create(LootContextParamSets.ADVANCEMENT_REWARD);
			for (TrialCase testCase : cases) {
				ItemStack base = new ItemStack(testCase.base());
				Component customName = Component.literal("Relic progression check");
				base.set(DataComponents.CUSTOM_NAME, customName);
				var input = new SmithingRecipeInput(new ItemStack(testCase.sigil()), base, new ItemStack(Items.NETHER_STAR));
				var holder = server.getRecipeManager().getRecipeFor(RecipeType.SMITHING, input, level)
						.orElseThrow(() -> new IllegalStateException("Missing sigil recipe: " + testCase.path()));
				ItemStack result = holder.value().assemble(input);
				if (!holder.id().identifier().equals(EpicRelics.id(testCase.itemPath() + "_sigil_smithing"))
						|| !result.is(testCase.output()) || !customName.equals(result.get(DataComponents.CUSTOM_NAME))) {
					throw new IllegalStateException("Wrong sigil smithing result or lost name: " + testCase.path());
				}
				var incorrectCost = new SmithingRecipeInput(new ItemStack(testCase.sigil()), base, new ItemStack(Items.DIAMOND));
				if (holder.value().matches(incorrectCost, level)) {
					throw new IllegalStateException("Sigil smithing failed to require a Nether Star: " + testCase.path());
				}
				var trial = server.getAdvancements().get(EpicRelics.id("relics/trials/" + testCase.path()));
				if (trial == null || !trial.value().criteria().containsKey("explored") || trial.value().rewards().loot().size() != 1) {
					throw new IllegalStateException("Missing trial criterion/reward: " + testCase.path());
				}
				var rewardTable = trial.value().rewards().loot().get(0);
				if (!rewardTable.is(EpicRelics.id("rewards/" + testCase.path() + "_sigil"))) {
					throw new IllegalStateException("Wrong trial loot table reference: " + testCase.path());
				}
				var rewards = rewardTable.value().getRandomItems(lootContext, 42L);
				if (rewards.size() != 1 || !rewards.get(0).is(testCase.sigil()) || rewards.get(0).getCount() != 1) {
					throw new IllegalStateException("Trial must award exactly one matching sigil: " + testCase.path());
				}
			}

			var advancements = server.getAdvancements();
			var root = advancements.get(EpicRelics.id("relics/root"));
			if (root == null || !root.value().isRoot() || root.value().display().isEmpty()) {
				throw new IllegalStateException("Relic advancement tab is missing its display root");
			}
			int displayedCount = 0;
			for (var advancement : advancements.getAllAdvancements()) {
				if (!advancement.id().getNamespace().equals(EpicRelics.MOD_ID)
						|| !advancement.id().getPath().startsWith("relics/")) {
					continue;
				}
				if (advancement.value().display().isEmpty()) {
					throw new IllegalStateException("Missing relic advancement display: " + advancement.id());
				}
				advancement.value().parent().ifPresent(parent -> {
					if (advancements.get(parent) == null) {
						throw new IllegalStateException("Broken relic advancement parent: " + parent);
					}
				});
				displayedCount++;
			}
			if (displayedCount != 26) {
				throw new IllegalStateException("Expected 26 visible relic advancements, found " + displayedCount);
			}
			for (String path : List.of("adventure/revaulting", "adventure/overoverkill", "adventure/avoid_vibration")) {
				if (advancements.get(Identifier.withDefaultNamespace(path)) == null) {
					throw new IllegalStateException("Missing vanilla trial prerequisite: " + path);
				}
			}
			EpicRelics.LOGGER.info("Progression development check passed: six optional smithing routes, exact sigil rewards, 26 advancements and vanilla prerequisites verified");
		});
	}

	private record TrialCase(String path, String itemPath, Item sigil, Item base, Item output) {
	}
}
