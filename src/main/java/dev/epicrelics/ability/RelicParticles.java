package dev.epicrelics.ability;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class RelicParticles {
	private RelicParticles() {
	}

	public static void ring(ServerLevel level, ParticleOptions particle, Vec3 center,
			double radius, int points, double yOffset) {
		for (int i = 0; i < points; i++) {
			Vec3 pos = ringPoint(center, radius, i, points).add(0.0, yOffset, 0.0);
			level.sendParticles(particle, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
		}
	}

	public static void line(ServerLevel level, ParticleOptions particle, Vec3 start,
			Vec3 end, int points) {
		if (points <= 0) {
			throw new IllegalArgumentException("points must be positive");
		}
		Vec3 delta = end.subtract(start);
		for (int i = 0; i <= points; i++) {
			Vec3 pos = start.add(delta.scale((double) i / points));
			level.sendParticles(particle, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
		}
	}

	public static Vec3 ringPoint(Vec3 center, double radius, int index, int points) {
		if (points <= 0) {
			throw new IllegalArgumentException("points must be positive");
		}
		double angle = Math.PI * 2.0 * index / points;
		return center.add(Math.cos(angle) * radius, 0.0, Math.sin(angle) * radius);
	}
}
