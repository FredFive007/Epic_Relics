package dev.epicrelics.network;

import dev.epicrelics.EpicRelics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record VoidStepCooldownPayload(int ticks) implements CustomPacketPayload {
	public static final Type<VoidStepCooldownPayload> TYPE = new Type<>(EpicRelics.id("void_step_cooldown"));
	public static final StreamCodec<RegistryFriendlyByteBuf, VoidStepCooldownPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, VoidStepCooldownPayload::ticks, VoidStepCooldownPayload::new);

	@Override
	public Type<VoidStepCooldownPayload> type() {
		return TYPE;
	}
}
