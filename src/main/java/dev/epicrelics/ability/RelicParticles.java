package dev.epicrelics.ability;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class RelicParticles {
	private static final List<Wave> WAVES = new ArrayList<>();
	private static final int MAX_ACTIVE_WAVES = 128;

	private RelicParticles() {
	}

	public static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			var iterator = WAVES.iterator();
			while (iterator.hasNext()) {
				Wave wave = iterator.next();
				// Server ticks remain monotonic when /tick freeze pauses world time.
				long elapsed = server.getTickCount() - wave.startTick();
				if (elapsed >= 12 || elapsed < 0) {
					iterator.remove();
				} else if (elapsed == 3 || elapsed == 6 || elapsed == 9) {
					double progress = elapsed / 9.0;
					double radius = wave.radius() * (wave.inward() ? 1.0 - progress * 0.9 : progress);
					ring(wave.level(), wave.particle(), wave.center(), radius, wave.points(), wave.height());
				}
			}
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> WAVES.clear());
	}

	/** Three short frames clarify motion without continuous area-wide particle spam. */
	public static void wave(ServerLevel level, ParticleOptions particle, Vec3 center,
			double radius, int points, double height, boolean inward) {
		if (WAVES.size() < MAX_ACTIVE_WAVES) {
			WAVES.add(new Wave(level, particle, center, radius, Math.min(32, Math.max(8, points)),
					height, inward, level.getServer().getTickCount()));
		}
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

	private record Wave(ServerLevel level, ParticleOptions particle, Vec3 center, double radius,
			int points, double height, boolean inward, long startTick) {}
}
