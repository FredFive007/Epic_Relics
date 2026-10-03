package dev.epicrelics.client;

import dev.epicrelics.ability.RelicEquipment;
import dev.epicrelics.ability.VoidStepAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** A local estimate of the exact server destination. Never changes the teleport target. */
public final class VoidStepPreview {
	private static Preview current;
	private static int particleTicks;

	private VoidStepPreview() {
	}

	public static Preview current() {
		return current;
	}

	public static void reset() {
		current = null;
		particleTicks = 0;
	}

	public static void tick(Minecraft client) {
		var player = client.player;
		var level = client.level;
		if (player == null || level == null || !player.isAlive() || player.isSpectator()
				|| !RelicEquipment.hasLeggings(player) || client.gui.screen() != null || client.gui.hud.isHidden()) {
			reset();
			return;
		}
		if (client.isPaused()) {
			return;
		}
		Vec3 start = player.getEyePosition();
		Vec3 end = VoidStepAbility.missEndpoint(start, player.getLookAngle());
		HitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE, player));
		Vec3 destination = hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
		AABB body = player.getBoundingBox().move(destination.subtract(player.position())).deflate(1.0E-5);
		Risk risk;
		if (body.minY < level.getMinY() || body.maxY > level.getMaxY() + 1
				|| !level.getWorldBorder().isWithinBounds(body)) {
			risk = Risk.BOUNDARY;
		} else if (!level.hasChunksAt(BlockPos.containing(body.minX, body.minY - 0.15, body.minZ),
				BlockPos.containing(body.maxX, body.maxY, body.maxZ))) {
			risk = Risk.UNLOADED;
		} else if (level.getBlockStates(body.expandTowards(0.0, -0.15, 0.0)).anyMatch(state ->
				state.getFluidState().is(FluidTags.LAVA) || state.is(BlockTags.FIRE)
						|| state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.CACTUS)
						|| state.is(Blocks.SWEET_BERRY_BUSH) || CampfireBlock.isLitCampfire(state))) {
			risk = Risk.HAZARD;
		} else if (!level.noCollision(player, body)) {
			risk = Risk.OBSTRUCTED;
		} else {
			AABB footing = new AABB(body.minX, destination.y - 0.15, body.minZ,
					body.maxX, destination.y - 0.01, body.maxZ);
			risk = level.noBlockCollision(player, footing) ? Risk.AIRBORNE : Risk.CLEAR;
		}
		current = new Preview(destination, start.distanceTo(destination), risk);
		if (++particleTicks % 6 != 0 || ClientAbilityCooldowns.voidStepCooldownTicks() > 0) {
			return;
		}
		// Vanilla's regular particle path cuts off at 32 blocks, below this skill's 54-block range.
		// Honor the particle setting here before bypassing only that distance-limited path.
		ParticleStatus particleSetting = client.options.particles().get();
		if (particleSetting == ParticleStatus.MINIMAL) {
			return;
		}
		int points = particleSetting == ParticleStatus.DECREASED ? 2 : 4;
		var particle = risk == Risk.CLEAR ? ParticleTypes.END_ROD : ParticleTypes.ELECTRIC_SPARK;
		for (int index = 0; index < points; index++) {
			double angle = Math.PI * 2.0 * index / points;
			level.addParticle(particle, true, false,
					destination.x + Math.cos(angle) * 0.34, destination.y + 0.12,
					destination.z + Math.sin(angle) * 0.34, 0.0, 0.0, 0.0);
		}
	}

	public record Preview(Vec3 destination, double distance, Risk risk) {
	}

	public enum Risk {
		CLEAR,
		AIRBORNE,
		OBSTRUCTED,
		HAZARD,
		UNLOADED,
		BOUNDARY
	}
}
