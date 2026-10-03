package dev.epicrelics.ability;

import dev.epicrelics.network.VoidStepCooldownPayload;
import dev.epicrelics.network.VoidStepRequestPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class VoidStepAbility {
	public static final double RANGE = 54.0;
	public static final int COOLDOWN_TICKS = 20;
	private static final Map<UUID, Integer> COOLDOWNS = new HashMap<>();

	private VoidStepAbility() {
	}

	public static void initialize() {
		PayloadTypeRegistry.serverboundPlay().register(VoidStepRequestPayload.TYPE, VoidStepRequestPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(VoidStepCooldownPayload.TYPE, VoidStepCooldownPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(VoidStepRequestPayload.TYPE,
				(payload, context) -> use(context.player()));
		ServerTickEvents.END_SERVER_TICK.register(server -> COOLDOWNS.replaceAll((uuid, ticks) -> ticks - 1));
		ServerTickEvents.END_SERVER_TICK.register(server -> COOLDOWNS.values().removeIf(ticks -> ticks <= 0));
	}

	public static Vec3 missEndpoint(Vec3 eyePosition, Vec3 lookDirection) {
		return eyePosition.add(lookDirection.normalize().scale(RANGE));
	}

	private static void use(ServerPlayer player) {
		if (!RelicEquipment.hasLeggings(player)) {
			RelicFeedback.explain(player, "requires_leggings");
			return;
		}
		if (COOLDOWNS.containsKey(player.getUUID())) {
			ServerPlayNetworking.send(player, new VoidStepCooldownPayload(COOLDOWNS.get(player.getUUID())));
			RelicFeedback.explain(player, "skill_cooling_down");
			return;
		}
		ServerLevel level = player.level();
		Vec3 start = player.getEyePosition();
		Vec3 end = missEndpoint(start, player.getLookAngle());
		HitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE, player));
		Vec3 destination = hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
		Vec3 departure = player.position().add(0.0, player.getBbHeight() * 0.5, 0.0);

		RelicParticles.ring(level, ParticleTypes.REVERSE_PORTAL, departure, 0.75, 20, 0.0);
		level.sendParticles(ParticleTypes.PORTAL, departure.x, departure.y, departure.z,
				24, 0.3, 0.75, 0.3, 0.1);
		player.teleportTo(destination.x, destination.y, destination.z);
		dev.epicrelics.progression.RelicProgression.onVoidStep(player);
		COOLDOWNS.put(player.getUUID(), COOLDOWN_TICKS);
		ServerPlayNetworking.send(player, new VoidStepCooldownPayload(COOLDOWN_TICKS));
		RelicParticles.ring(level, ParticleTypes.PORTAL, destination, 1.0, 28, 0.15);
		RelicParticles.ring(level, ParticleTypes.END_ROD, destination, 0.55, 12, 0.85);
		RelicParticles.wave(level, ParticleTypes.REVERSE_PORTAL, destination, 1.2, 16, 0.2, true);
		level.sendParticles(ParticleTypes.PORTAL, destination.x, destination.y, destination.z,
				28, 0.35, 0.8, 0.35, 0.12);
		level.playSound(null, destination.x, destination.y, destination.z,
				SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
	}
}
