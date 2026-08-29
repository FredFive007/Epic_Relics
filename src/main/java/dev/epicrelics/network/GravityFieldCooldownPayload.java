package dev.epicrelics.network;

import dev.epicrelics.EpicRelics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record GravityFieldCooldownPayload(int ticks) implements CustomPacketPayload {
	public static final Type<GravityFieldCooldownPayload> TYPE = new Type<>(EpicRelics.id("gravity_field_cooldown"));
	public static final StreamCodec<RegistryFriendlyByteBuf, GravityFieldCooldownPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, GravityFieldCooldownPayload::ticks, GravityFieldCooldownPayload::new);

	@Override
	public Type<GravityFieldCooldownPayload> type() {
		return TYPE;
	}
}
