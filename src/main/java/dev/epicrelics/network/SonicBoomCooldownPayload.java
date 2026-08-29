package dev.epicrelics.network;

import dev.epicrelics.EpicRelics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SonicBoomCooldownPayload(int ticks) implements CustomPacketPayload {
	public static final Type<SonicBoomCooldownPayload> TYPE = new Type<>(EpicRelics.id("sonic_boom_cooldown"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SonicBoomCooldownPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, SonicBoomCooldownPayload::ticks, SonicBoomCooldownPayload::new);

	@Override
	public Type<SonicBoomCooldownPayload> type() {
		return TYPE;
	}
}
