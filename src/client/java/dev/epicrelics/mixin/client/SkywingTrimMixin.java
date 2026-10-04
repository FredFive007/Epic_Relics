package dev.epicrelics.mixin.client;

import dev.epicrelics.item.ModItems;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Vanilla has no wing trim textures; keep the chestplate's real trim on its armor layer. */
@Mixin(WingsLayer.class)
public abstract class SkywingTrimMixin {
	@ModifyArg(
			method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/EquipmentLayerRenderer;renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V"),
			index = 4
	)
	private ItemStack epicRelics$withoutUnsupportedWingTrim(ItemStack stack) {
		if (!stack.is(ModItems.SKYWING_CHESTPLATE) || stack.get(DataComponents.TRIM) == null) {
			return stack;
		}
		ItemStack wingAppearance = stack.copy();
		wingAppearance.remove(DataComponents.TRIM);
		return wingAppearance;
	}
}
