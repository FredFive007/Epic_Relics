package dev.epicrelics.ability;

import dev.epicrelics.item.ModItems;
import dev.epicrelics.network.GravityFieldCooldownPayload;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;

public final class GravityBladeAbility {
	public static final double PLUNGE_THRESHOLD = 1.5;
	public static final double PLUNGE_FACTOR = 0.75;
	public static final double PLUNGE_CAP = 8.0;
	public static final double GRAVITY_RADIUS = 10.0;
	public static final int GRAVITY_COOLDOWN_TICKS = 200;
	public static final int SLOWNESS_TICKS = 100;
	public static final int SLOWNESS_AMPLIFIER = 3;

	private static final Set<UUID> PLUNGE_USED = new HashSet<>();
	private static final Map<UUID, Integer> GRAVITY_COOLDOWNS = new HashMap<>();

	private GravityBladeAbility() {
	}

	public static void initialize() {
		PayloadTypeRegistry.clientboundPlay().register(GravityFieldCooldownPayload.TYPE, GravityFieldCooldownPayload.CODEC);
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			GRAVITY_COOLDOWNS.replaceAll((uuid, ticks) -> ticks - 1);
			GRAVITY_COOLDOWNS.values().removeIf(ticks -> ticks <= 0);
			server.getPlayerList().getPlayers().forEach(player -> {
				if (player.onGround() || player.fallDistance == 0.0F) {
					PLUNGE_USED.remove(player.getUUID());
				}
			});
		});
	}

	public static boolean canPlunge(Player player) {
		return player.fallDistance > PLUNGE_THRESHOLD
				&& !player.isFallFlying()
				&& !PLUNGE_USED.contains(player.getUUID());
	}

	public static double plungeBonus(double fallDistance) {
		return Math.min(Math.max(fallDistance, 0.0) * PLUNGE_FACTOR, PLUNGE_CAP);
	}

	public static void consumePlunge(Player player) {
		PLUNGE_USED.add(player.getUUID());
	}

	public static void spawnPlungeImpact(Player attacker, LivingEntity target) {
		if (!(attacker.level() instanceof ServerLevel level)) {
			return;
		}
		if (attacker instanceof ServerPlayer serverPlayer) {
			dev.epicrelics.progression.RelicProgression.onPlungeHit(serverPlayer);
		}
		level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.6), target.getZ(),
				8, 0.3, 0.3, 0.3, 0.3);
		RelicParticles.ring(level, ParticleTypes.DUST_PLUME, target.position(), 1.25, 18, 0.08);
		RelicParticles.ring(level, ParticleTypes.END_ROD, target.position(), 0.65, 10, 0.55);
		level.playSound(null, target.getX(), target.getY(), target.getZ(),
				SoundEvents.MACE_SMASH_AIR, SoundSource.PLAYERS, 0.8F, 1.0F);
	}

	public static void cast(ServerPlayer player) {
		if (!player.getMainHandItem().is(ModItems.GRAVITY_BLADE)) {
			RelicFeedback.explain(player, "requires_blade");
			return;
		}
		if (GRAVITY_COOLDOWNS.containsKey(player.getUUID())) {
			ServerPlayNetworking.send(player, new GravityFieldCooldownPayload(GRAVITY_COOLDOWNS.get(player.getUUID())));
			RelicFeedback.explain(player, "skill_cooling_down");
			return;
		}
		ServerLevel level = player.level();
		Vec3 center = player.position();
		for (LivingEntity target : level.getEntities(EntityTypeTest.forClass(LivingEntity.class),
				player.getBoundingBox().inflate(GRAVITY_RADIUS, GRAVITY_RADIUS, GRAVITY_RADIUS),
				target -> StompAbility.isLegalTarget(player, target))) {
			if (target.distanceToSqr(player) > GRAVITY_RADIUS * GRAVITY_RADIUS) {
				continue;
			}
			if (target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SLOWNESS_TICKS, SLOWNESS_AMPLIFIER), player)) {
				dev.epicrelics.progression.RelicProgression.onGravityFieldHit(player);
			}
			Vec3 inward = center.subtract(target.position()).normalize();
			level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.6), target.getZ(),
					0, inward.x, inward.y * 0.25 + 0.1, inward.z, 0.25);
		}

		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 0.9F);
		level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(),
				40, 0.8, 0.8, 0.8, 0.0);
		RelicParticles.ring(level, ParticleTypes.REVERSE_PORTAL, center,
				GRAVITY_RADIUS, 32, 0.18);
		RelicParticles.wave(level, ParticleTypes.END_ROD, center,
				GRAVITY_RADIUS, 24, 0.3, true);
		GRAVITY_COOLDOWNS.put(player.getUUID(), GRAVITY_COOLDOWN_TICKS);
		ServerPlayNetworking.send(player, new GravityFieldCooldownPayload(GRAVITY_COOLDOWN_TICKS));
	}
}
