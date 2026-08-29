package dev.epicrelics.ability;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;

public final class StompAbility {
	private StompAbility() {
	}

	public static void onLanding(ServerPlayer player, double fallDistance) {
		if (!RelicEquipment.hasBoots(player) || player.isShiftKeyDown() || fallDistance < 4.0) {
			return;
		}
		Tier tier = Tier.forDistance(fallDistance);
		ServerLevel level = player.level();
		var damageSource = level.damageSources().playerAttack(player);
		var area = player.getBoundingBox().inflate(tier.radius, 2.0, tier.radius);
		for (LivingEntity target : level.getEntities(EntityTypeTest.forClass(LivingEntity.class),
				area, target -> isLegalTarget(player, target))) {
			if (target.distanceToSqr(player) > tier.radius * tier.radius) {
				continue;
			}
			if (target.hurtServer(level, damageSource, tier.damage)) {
				target.knockback(tier.knockback,
						player.getX() - target.getX(), player.getZ() - target.getZ(), damageSource, 0.0F);
			}
		}

		level.sendParticles(ParticleTypes.DUST_PLUME, player.getX(), player.getY() + 0.1, player.getZ(),
				tier.particleCount, tier.radius * 0.45, 0.15, tier.radius * 0.45, 0.08);
		level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.05, player.getZ(),
				tier.particleCount / 2, tier.radius * 0.35, 0.05, tier.radius * 0.35, 0.04);
		Vec3 center = player.position();
		RelicParticles.ring(level, ParticleTypes.DUST_PLUME, center,
				tier.radius * 0.55, Math.max(16, tier.particleCount / 2), 0.08);
		RelicParticles.ring(level, ParticleTypes.CLOUD, center,
				tier.radius, Math.max(20, tier.particleCount / 2), 0.12);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), tier.sound,
				SoundSource.PLAYERS, tier.volume, 0.85F);
	}

	static boolean isLegalTarget(ServerPlayer player, LivingEntity target) {
		if (target == player || target.isAlliedTo(player)) {
			return false;
		}
		if (target instanceof TamableAnimal tame && tame.isOwnedBy(player)) {
			return false;
		}
		if (target instanceof ServerPlayer other) {
			return player.canHarmPlayer(other);
		}
		return target instanceof Enemy;
	}

	public record Tier(double radius, float damage, double knockback, int particleCount,
			net.minecraft.sounds.SoundEvent sound, float volume) {
		public static Tier forDistance(double fallDistance) {
			if (fallDistance >= 16.0) {
				return new Tier(7.0, 10.0F, 1.6, 90, SoundEvents.MACE_SMASH_GROUND_HEAVY, 1.5F);
			}
			if (fallDistance >= 8.0) {
				return new Tier(5.0, 7.0F, 1.2, 60, SoundEvents.MACE_SMASH_GROUND, 1.25F);
			}
			return new Tier(3.5, 4.0F, 0.8, 30, SoundEvents.MACE_SMASH_AIR, 1.0F);
		}
	}
}
