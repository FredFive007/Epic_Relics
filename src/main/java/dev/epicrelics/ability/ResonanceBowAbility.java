package dev.epicrelics.ability;

import dev.epicrelics.item.ModComponents;
import dev.epicrelics.item.ModItems;
import dev.epicrelics.network.ResonanceModeRequestPayload;
import dev.epicrelics.network.SonicBoomCooldownPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ResonanceBowAbility {
	public static final double DARKNESS_RADIUS = 4.0;
	public static final int DARKNESS_TICKS = 120;
	public static final double SONIC_RANGE = 32.0;
	public static final float SONIC_DAMAGE = 16.0F;
	public static final int SONIC_COOLDOWN_TICKS = 60;
	public static final double SONIC_HITBOX_MARGIN = 0.5;

	private static final Map<UUID, Integer> SONIC_COOLDOWNS = new HashMap<>();

	private ResonanceBowAbility() {
	}

	public static void initialize() {
		PayloadTypeRegistry.clientboundPlay().register(SonicBoomCooldownPayload.TYPE, SonicBoomCooldownPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ResonanceModeRequestPayload.TYPE, ResonanceModeRequestPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ResonanceModeRequestPayload.TYPE,
				(payload, context) -> toggleMode(context.player()));
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			SONIC_COOLDOWNS.replaceAll((uuid, ticks) -> ticks - 1);
			SONIC_COOLDOWNS.values().removeIf(ticks -> ticks <= 0);
		});
	}

	public static boolean isSonicMode(ItemStack stack) {
		return stack.getOrDefault(ModComponents.RESONANCE_MODE, false);
	}

	public static boolean isSonicReady(ServerPlayer player) {
		return !SONIC_COOLDOWNS.containsKey(player.getUUID());
	}

	public static void toggleMode(ServerPlayer player) {
		ItemStack stack = player.getMainHandItem();
		if (!stack.is(ModItems.RESONANCE_BOW)) {
			return;
		}
		boolean sonic = !isSonicMode(stack);
		stack.set(ModComponents.RESONANCE_MODE, sonic);
		player.getInventory().setChanged();
		player.sendSystemMessage(Component.translatable(sonic
				? "message.epic_relics.resonance_mode.sonic"
				: "message.epic_relics.resonance_mode.darkness"), true);
	}

	public static void castSonicBoom(ServerPlayer player) {
		if (!isSonicReady(player)) {
			return;
		}
		ServerLevel level = player.level();
		LivingEntity target = findSonicTarget(level, player);
		Vec3 start = player.getEyePosition();
		Vec3 look = player.getLookAngle();
		Vec3 end = start.add(look.scale(SONIC_RANGE));
		RelicParticles.line(level, ParticleTypes.SONIC_BOOM, start.add(look.scale(2.0)), end, 8);
		RelicParticles.line(level, ParticleTypes.ELECTRIC_SPARK, start, end, 24);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.0F, 1.0F);
		if (target != null) {
			target.hurtServer(level, level.damageSources().sonicBoom(player), SONIC_DAMAGE);
			level.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY(0.6), target.getZ(),
					12, 0.35, 0.45, 0.35, 0.04);
		}
		SONIC_COOLDOWNS.put(player.getUUID(), SONIC_COOLDOWN_TICKS);
		ServerPlayNetworking.send(player, new SonicBoomCooldownPayload(SONIC_COOLDOWN_TICKS));
	}

	public static void applyDarknessPulse(ServerLevel level, Vec3 pos, ServerPlayer shooter) {
		for (LivingEntity target : level.getEntities(EntityTypeTest.forClass(LivingEntity.class),
				new AABB(pos, pos).inflate(DARKNESS_RADIUS), t -> StompAbility.isLegalTarget(shooter, t))) {
			if (target.distanceToSqr(pos.x, pos.y, pos.z) <= DARKNESS_RADIUS * DARKNESS_RADIUS) {
				target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, DARKNESS_TICKS, 0), shooter);
			}
		}
		RelicParticles.ring(level, ParticleTypes.SCULK_SOUL, pos, DARKNESS_RADIUS, 28, 0.18);
		RelicParticles.ring(level, ParticleTypes.PORTAL, pos, DARKNESS_RADIUS * 0.55, 20, 0.55);
		level.sendParticles(ParticleTypes.SOUL, pos.x, pos.y + 0.4, pos.z,
				18, 0.65, 0.5, 0.65, 0.03);
	}

	public static LivingEntity findSonicTarget(ServerLevel level, ServerPlayer player) {
		Vec3 start = player.getEyePosition();
		Vec3 look = player.getLookAngle();
		Vec3 end = start.add(look.scale(SONIC_RANGE));
		AABB area = new AABB(start, end).inflate(SONIC_HITBOX_MARGIN);
		LivingEntity best = null;
		double bestDistanceSqr = Double.POSITIVE_INFINITY;
		for (LivingEntity target : level.getEntities(EntityTypeTest.forClass(LivingEntity.class),
				area, t -> isLegalSonicTarget(player, t))) {
			double distanceSqr = sonicIntersectionDistanceSqr(start, end, target.getBoundingBox());
			if (distanceSqr < bestDistanceSqr) {
				bestDistanceSqr = distanceSqr;
				best = target;
			}
		}
		return best;
	}

	static boolean isLegalSonicTarget(ServerPlayer player, LivingEntity target) {
		if (!target.isAlive() || target == player || target.isAlliedTo(player)) {
			return false;
		}
		if (target instanceof TamableAnimal tame && tame.isOwnedBy(player)) {
			return false;
		}
		if (target instanceof ServerPlayer other) {
			return player.canHarmPlayer(other);
		}
		return true;
	}

	public static double sonicIntersectionDistanceSqr(Vec3 start, Vec3 end, AABB targetBounds) {
		AABB forgivingBounds = targetBounds.inflate(SONIC_HITBOX_MARGIN);
		if (forgivingBounds.contains(start)) {
			return 0.0;
		}
		return forgivingBounds.clip(start, end)
				.map(start::distanceToSqr)
				.orElse(Double.POSITIVE_INFINITY);
	}

}
