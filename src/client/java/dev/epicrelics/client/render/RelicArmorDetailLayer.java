package dev.epicrelics.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.epicrelics.EpicRelics;
import dev.epicrelics.item.ModItems;
import java.util.EnumMap;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** An additive Fabric render layer: preserves the vanilla equipment UVs, trim and wing layers. */
public final class RelicArmorDetailLayer extends RenderLayer<HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
	private static final Identifier TEXTURE = EpicRelics.id("textures/entity/equipment/humanoid/relic_armor.png");
	private static final EquipmentSlot[] SLOTS = {
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
	};
	// Multiplying the existing amber texel (244, 128, 26) produces a muted antique gold.
	private static final int GILDING_COLOR = 0xFF96E6FF;
	private final EnumMap<EquipmentSlot, ArmorDetails> adults = new EnumMap<>(EquipmentSlot.class);
	private final EnumMap<EquipmentSlot, ArmorDetails> babies = new EnumMap<>(EquipmentSlot.class);

	public RelicArmorDetailLayer(RenderLayerParent<HumanoidRenderState, HumanoidModel<HumanoidRenderState>> parent) {
		super(parent);
		for (EquipmentSlot slot : SLOTS) {
			adults.put(slot, bake(slot, false));
			babies.put(slot, bake(slot, true));
		}
	}

	@Override
	public void submit(PoseStack poses, SubmitNodeCollector collector, int light,
			HumanoidRenderState state, float yaw, float pitch) {
		boolean baby = state.isBaby && state.entityType != EntityTypes.ARMOR_STAND;
		for (EquipmentSlot slot : SLOTS) {
			ItemStack item = switch (slot) {
				case HEAD -> state.headEquipment;
				case CHEST -> state.chestEquipment;
				case LEGS -> state.legsEquipment;
				case FEET -> state.feetEquipment;
				default -> ItemStack.EMPTY;
			};
			if (!isRelic(item, slot)) {
				continue;
			}
			ArmorDetails models = (baby ? babies : adults).get(slot);
			var renderType = item.hasFoil()
					? RenderTypes.armorCutoutNoCullGlint(TEXTURE) : RenderTypes.armorCutoutNoCull(TEXTURE);
			// Deferred submission copies transforms while rendering, not from a previous entity/frame.
			ArmorRenderer.submitTransformCopyingModel(getParentModel(), state, models.plates(), Unit.INSTANCE,
					false, collector.order(3), poses, renderType, light, OverlayTexture.NO_OVERLAY,
					-1, null, state.outlineColor);
			ArmorRenderer.submitTransformCopyingModel(getParentModel(), state, models.gilding(), Unit.INSTANCE,
					false, collector.order(4), poses, renderType, light, OverlayTexture.NO_OVERLAY,
					GILDING_COLOR, null, state.outlineColor);
		}
	}

	private static ArmorDetails bake(EquipmentSlot slot, boolean baby) {
		return new ArmorDetails(
				new Model.Simple(RelicArmorGeometry.create(slot, baby, false).bakeRoot(), RenderTypes::armorCutoutNoCull),
				new Model.Simple(RelicArmorGeometry.create(slot, baby, true).bakeRoot(), RenderTypes::armorCutoutNoCull));
	}

	private record ArmorDetails(Model.Simple plates, Model.Simple gilding) {}

	private static boolean isRelic(ItemStack item, EquipmentSlot slot) {
		return item.is(switch (slot) {
			case HEAD -> ModItems.DRAGON_SIGHT_HELMET;
			case CHEST -> ModItems.SKYWING_CHESTPLATE;
			case LEGS -> ModItems.VOIDWALKER_LEGGINGS;
			case FEET -> ModItems.HEAVY_CORE_BOOTS;
			default -> throw new IllegalArgumentException("Not an armor slot: " + slot);
		});
	}
}
