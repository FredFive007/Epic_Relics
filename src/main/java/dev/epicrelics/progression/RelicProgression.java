package dev.epicrelics.progression;

import dev.epicrelics.EpicRelics;
import dev.epicrelics.ability.RelicEquipment;
import dev.epicrelics.item.ModItems;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;

/** Server-owned progression. Advancement progress persists across death, logout and restart. */
public final class RelicProgression {
	private static final Map<UUID, Integer> SKYWING_FLIGHT_TICKS = new HashMap<>();

	private RelicProgression() {
	}

	public static void initialize() {
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SKYWING_FLIGHT_TICKS.clear());
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			boolean secondElapsed = server.getTickCount() % 20 == 0;
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (player.isAlive() && !player.isSpectator()) {
					checkFlightChallenge(player);
					if (secondElapsed) {
						checkExploration(player);
						checkArmorChallenges(player);
					}
				} else {
					SKYWING_FLIGHT_TICKS.remove(player.getUUID());
				}
			}
			SKYWING_FLIGHT_TICKS.keySet().removeIf(id -> server.getPlayerList().getPlayer(id) == null);
		});
	}

	private static void checkExploration(ServerPlayer player) {
		boolean inEnd = player.level().dimension().equals(Level.END);
		if (inEnd && has(player, Items.DRAGON_HEAD) && has(player, Items.DRAGON_BREATH)) {
			award(player, "trials/dragon_sight", "explored");
		}
		if (inEnd && player.isFallFlying() && player.getFallFlyingTicks() >= 200) {
			award(player, "trials/skywing", "explored");
		}
		if (inEnd && has(player, Items.DRAGON_BREATH) && has(player, Items.CHORUS_FRUIT)) {
			award(player, "trials/voidwalker", "explored");
		}
		if (has(player, Items.HEAVY_CORE) && completed(player, Identifier.withDefaultNamespace("adventure/revaulting"))) {
			award(player, "trials/heavy_core", "explored");
		}
		if (has(player, Items.MACE) && completed(player, Identifier.withDefaultNamespace("adventure/overoverkill"))) {
			award(player, "trials/gravity", "explored");
		}
		if (has(player, Items.ECHO_SHARD) && player.level().getBiome(player.blockPosition()).is(Biomes.DEEP_DARK)
				&& completed(player, Identifier.withDefaultNamespace("adventure/avoid_vibration"))) {
			award(player, "trials/resonance", "explored");
		}
	}

	private static void checkArmorChallenges(ServerPlayer player) {
		if (RelicEquipment.hasHelmet(player)) {
			if (player.level().dimension().equals(Level.NETHER)) {
				award(player, "mastery/dragon_sight", "nether");
			}
			if (player.level().dimension().equals(Level.END)) {
				award(player, "mastery/dragon_sight", "end");
			}
			if (player.level().getBiome(player.blockPosition()).is(Biomes.DEEP_DARK)) {
				award(player, "mastery/dragon_sight", "deep_dark");
			}
		}
	}

	private static void checkFlightChallenge(ServerPlayer player) {
		if (RelicEquipment.hasChestplate(player) && player.isFallFlying()) {
			int ticks = Math.min(SKYWING_FLIGHT_TICKS.getOrDefault(player.getUUID(), 0) + 1, 201);
			SKYWING_FLIGHT_TICKS.put(player.getUUID(), ticks);
			if (ticks == 200) {
				award(player, "mastery/skywing", "flight");
			}
		} else {
			SKYWING_FLIGHT_TICKS.remove(player.getUUID());
		}
	}

	private static boolean has(ServerPlayer player, Item item) {
		return player.getInventory().contains(stack -> stack.is(item));
	}

	/** Call only after the teleport has actually completed on the server. */
	public static void onVoidStep(ServerPlayer player) {
		if (!RelicEquipment.hasLeggings(player)) {
			return;
		}
		var dimension = player.level().dimension();
		if (dimension.equals(Level.OVERWORLD)) {
			award(player, "mastery/voidwalker", "overworld");
		} else if (dimension.equals(Level.NETHER)) {
			award(player, "mastery/voidwalker", "nether");
		} else if (dimension.equals(Level.END)) {
			award(player, "mastery/voidwalker", "end");
		}
	}

	/** Call only when at least one legal target accepted stomp damage. */
	public static void onStompHit(ServerPlayer player, double fallDistance) {
		if (RelicEquipment.hasBoots(player) && fallDistance >= 4.0) {
			String tier = fallDistance >= 16.0 ? "heavy" : fallDistance >= 8.0 ? "medium" : "light";
			award(player, "mastery/heavy_core", tier);
		}
	}

	/** Call after a legal target accepted the field's slowness effect. */
	public static void onGravityFieldHit(ServerPlayer player) {
		if (player.getMainHandItem().is(ModItems.GRAVITY_BLADE)) {
			award(player, "mastery/gravity", "field");
		}
	}

	/** Call after a successful plunge damage event, never on an attempted attack. */
	public static void onPlungeHit(ServerPlayer player) {
		if (player.getMainHandItem().is(ModItems.GRAVITY_BLADE)) {
			award(player, "mastery/gravity", "plunge");
		}
	}

	/** Call only when the selected target accepts sonic damage. */
	public static void onSonicHit(ServerPlayer player) {
		award(player, "mastery/resonance", "sonic");
	}

	/** Arrow ownership has already been checked by the darkness pulse server path. */
	public static void onDarknessHit(ServerPlayer player) {
		award(player, "mastery/resonance", "darkness");
	}

	public static boolean completed(ServerPlayer player, Identifier id) {
		var advancement = player.level().getServer().getAdvancements().get(id);
		return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
	}

	private static void award(ServerPlayer player, String path, String criterion) {
		if (player.isSpectator()) {
			return;
		}
		var advancement = player.level().getServer().getAdvancements().get(EpicRelics.id("relics/" + path));
		if (advancement != null) {
			player.getAdvancements().award(advancement, criterion);
		}
	}
}
