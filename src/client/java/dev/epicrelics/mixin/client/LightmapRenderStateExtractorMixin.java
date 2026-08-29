package dev.epicrelics.mixin.client;

import dev.epicrelics.client.ClientRelicState;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
abstract class LightmapRenderStateExtractorMixin {
	@Inject(method = "extract", at = @At("TAIL"))
	private void epicRelics$applyDragonSightFullBright(LightmapRenderState state, float partialTick,
			CallbackInfo ci) {
		if (ClientRelicState.isDragonVisionActive()) {
			state.ambientColor = new Vector3f(1.0F, 1.0F, 1.0F);
			state.needsUpdate = true;
		}
	}
}
