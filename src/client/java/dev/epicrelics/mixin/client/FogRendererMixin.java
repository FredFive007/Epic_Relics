package dev.epicrelics.mixin.client;

import dev.epicrelics.client.ClientRelicState;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
abstract class FogRendererMixin {
	@Inject(method = "setupFog", at = @At("TAIL"))
	private void epicRelics$removeNormalNetherFog(Camera camera, int renderDistance,
			DeltaTracker deltaTracker, float partialTick, ClientLevel level,
			CallbackInfoReturnable<FogData> cir) {
		if (!ClientRelicState.isDragonVisionActive() || level.dimension() != Level.NETHER
				|| camera.getFluidInCamera() != FogType.NONE) {
			return;
		}
		FogData fog = cir.getReturnValue();
		float clearDistance = Math.max(renderDistance, fog.renderDistanceEnd);
		fog.environmentalStart = clearDistance;
		fog.environmentalEnd = clearDistance;
		fog.renderDistanceStart = clearDistance;
		fog.renderDistanceEnd = clearDistance;
	}
}
