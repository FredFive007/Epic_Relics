package dev.epicrelics.network;

import dev.epicrelics.EpicRelics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ResonanceModeRequestPayload() implements CustomPacketPayload {
	public static final ResonanceModeRequestPayload INSTANCE = new ResonanceModeRequestPayload();
	public static final Type<ResonanceModeRequestPayload> TYPE = new Type<>(EpicRelics.id("resonance_mode_request"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ResonanceModeRequestPayload> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<ResonanceModeRequestPayload> type() {
		return TYPE;
	}
}
