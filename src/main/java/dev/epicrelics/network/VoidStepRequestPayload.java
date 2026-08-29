package dev.epicrelics.network;

import dev.epicrelics.EpicRelics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record VoidStepRequestPayload() implements CustomPacketPayload {
	public static final VoidStepRequestPayload INSTANCE = new VoidStepRequestPayload();
	public static final Type<VoidStepRequestPayload> TYPE = new Type<>(EpicRelics.id("void_step_request"));
	public static final StreamCodec<RegistryFriendlyByteBuf, VoidStepRequestPayload> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<VoidStepRequestPayload> type() {
		return TYPE;
	}
}
