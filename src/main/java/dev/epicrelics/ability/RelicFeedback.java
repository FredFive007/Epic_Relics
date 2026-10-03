package dev.epicrelics.ability;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Brief, server-authoritative explanations, rate limited while a key is held. */
public final class RelicFeedback {
	private static final Map<UUID, Long> LAST_MESSAGE = new HashMap<>();

	private RelicFeedback() {}

	public static void initialize() {
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> LAST_MESSAGE.remove(handler.player.getUUID()));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> LAST_MESSAGE.clear());
	}

	public static void explain(ServerPlayer player, String reason) {
		long tick = player.level().getServer().getTickCount();
		Long previous = LAST_MESSAGE.get(player.getUUID());
		if (previous != null && tick >= previous && tick - previous < 15) {
			return;
		}
		LAST_MESSAGE.put(player.getUUID(), tick);
		player.sendSystemMessage(Component.translatable("message.epic_relics." + reason), true);
	}
}
