package dev.epicrelics.client.render;

import dev.epicrelics.EpicRelics;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;

/** Registers cosmetic layers only; vanilla armor, trims and elytra keep their own renderers. */
public final class RelicVisuals {
	private RelicVisuals() {
	}

	@SuppressWarnings("unchecked")
	public static void initialize() {
		ConditionalItemModelProperties.ID_MAPPER.put(EpicRelics.id("sonic_mode"), SonicModeProperty.MAP_CODEC);
		LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, renderer, helper, context) -> {
			if (renderer.getModel() instanceof HumanoidModel<?>) {
				var parent = (RenderLayerParent<HumanoidRenderState, HumanoidModel<HumanoidRenderState>>) (Object) renderer;
				helper.register(new RelicArmorDetailLayer(parent));
			}
		});
	}
}
