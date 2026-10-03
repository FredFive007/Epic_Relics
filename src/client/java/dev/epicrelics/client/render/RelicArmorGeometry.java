package dev.epicrelics.client.render;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.EquipmentSlot;

/** Low-poly articulated geometry, sampling opaque swatches of the existing 64 x 32 armor atlas. */
final class RelicArmorGeometry {
	private static final int[] DARK = {8, 2};
	private static final int[] METAL = {11, 1};
	private static final int[] EDGE = {11, 0};
	private static final int[] VIOLET = {9, 10};
	private static final int[] AMBER = {4, 26};

	private RelicArmorGeometry() {
	}

	static LayerDefinition create(EquipmentSlot slot, boolean baby) {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		switch (slot) {
			case HEAD -> helmet(root, baby);
			case CHEST -> chest(root, baby);
			case LEGS -> leggings(root, baby);
			case FEET -> boots(root, baby);
			default -> throw new IllegalArgumentException("Not an armor slot: " + slot);
		}
		return LayerDefinition.create(mesh, 64, 32);
	}

	private static PartDefinition bone(PartDefinition root, String name) {
		return root.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO);
	}

	private static void box(PartDefinition part, String name, int[] color,
			float x, float y, float z, float width, float height, float depth) {
		box(part, name, color, x, y, z, width, height, depth, PartPose.ZERO);
	}

	private static void box(PartDefinition part, String name, int[] color,
			float x, float y, float z, float width, float height, float depth, PartPose pose) {
		// All six faces remain inside one existing opaque texel. No new raster assets are needed.
		part.addOrReplaceChild(name, CubeListBuilder.create().texOffs(color[0] * 64 + 16, color[1] * 64 + 16)
				.addBox(x, y, z, width, height, depth, CubeDeformation.NONE, 64.0F, 64.0F), pose);
	}

	private static void helmet(PartDefinition root, boolean baby) {
		PartDefinition head = bone(root, "head");
		float crown = baby ? -8.2F : -9.2F;
		float side = baby ? 4.8F : 4.3F;
		for (int sign : new int[] {-1, 1}) {
			String id = sign < 0 ? "right" : "left";
			PartDefinition horn = head.addOrReplaceChild(id + "_dragon_horn", CubeListBuilder.create(),
					PartPose.offsetAndRotation(sign * side, crown, 0.4F, -0.24F, 0, sign * 0.28F));
			box(horn, "root", DARK, -1.1F, -2.2F, -1.1F, 2.2F, 2.7F, 2.2F);
			box(horn, "ridge", METAL, -0.75F, -4.1F, -0.65F, 1.5F, 2.2F, 1.5F);
			box(horn, "tip", VIOLET, -0.35F, -5.4F, 0, 0.7F, 1.5F, 0.9F);
			box(head, id + "_temple", METAL, sign < 0 ? -5.25F : 4.2F, -6.8F, -3.6F,
					1.05F, 3.1F, 4.2F);
		}
		// Sits above the brows; eyes and the center of the face stay unobstructed.
		box(head, "crown", DARK, -3.3F, crown - 0.4F, -4.2F, 6.6F, 1.0F, 1.0F);
		box(head, "crown_sigil", VIOLET, -0.7F, crown - 0.6F, -4.35F, 1.4F, 1.3F, 0.5F);
	}

	private static void chest(PartDefinition root, boolean baby) {
		PartDefinition body = bone(root, "body");
		float s = baby ? 0.65F : 1.0F;
		float y = baby ? -3.0F : 0;
		box(body, "breastplate_frame", DARK, -3.1F * s, y + s, -3.35F * s, 6.2F * s, 6.3F * s, 0.8F * s);
		box(body, "breastplate_sigil", VIOLET, -0.65F * s, y + 2.4F * s, -3.65F * s, 1.3F * s, 2.8F * s, 0.5F * s);
		for (int sign : new int[] {-1, 1}) {
			PartDefinition arm = bone(root, sign < 0 ? "right_arm" : "left_arm");
			float center = baby ? 0 : sign;
			float a = baby ? 0.56F : 1.0F;
			float top = baby ? -0.55F : -3.0F;
			box(arm, "pauldron", DARK, center - 3.0F * a, top, -2.9F * a, 6.0F * a, 2.3F * a, 5.8F * a);
			box(arm, "pauldron_rim", EDGE, center - 3.1F * a, top + 1.8F * a, -3.0F * a, 6.2F * a, 0.55F * a, 6.0F * a);
			box(arm, "shoulder_rune", VIOLET, center - 1.0F * a, top + 0.35F * a, -3.08F * a, 2.0F * a, 0.7F * a, 0.3F * a);
			box(arm, "outer_flange", METAL, center + (sign < 0 ? -3.7F : 2.4F) * a,
					top + 0.8F * a, -2.3F * a, 1.3F * a, 2.5F * a, 4.6F * a);
		}
		// No back geometry: elytra and cape articulation retain clear space.
	}

	private static void leggings(PartDefinition root, boolean baby) {
		for (int sign : new int[] {-1, 1}) {
			PartDefinition leg = bone(root, sign < 0 ? "right_leg" : "left_leg");
			float s = baby ? 0.45F : 1.0F;
			float center = baby ? -sign * 0.5F : 0;
			for (int plate = 0; plate < 3; plate++) {
				box(leg, "tasset_" + plate, plate == 1 ? METAL : DARK,
						center - 2.2F * s, (0.5F + plate * 1.8F) * s, -2.8F * s - (baby ? 1.0F : 0),
						4.4F * s, 2.0F * s, 0.65F * s);
			}
			box(leg, "void_rune", VIOLET, center - 0.3F * s, 1.3F * s, -3.0F * s - (baby ? 1.0F : 0),
					0.6F * s, 3.9F * s, 0.4F * s);
			box(leg, "knee_guard", EDGE, center - 1.4F * s, 5.3F * s, -3.0F * s - (baby ? 1.0F : 0),
					2.8F * s, 1.6F * s, 1.0F * s);
		}
	}

	private static void boots(PartDefinition root, boolean baby) {
		for (int sign : new int[] {-1, 1}) {
			PartDefinition leg = bone(root, sign < 0 ? "right_leg" : "left_leg");
			float s = baby ? 0.65F : 1;
			float center = baby ? -sign * 0.5F : 0;
			float top = baby ? 2.0F : 8.0F;
			float heel = baby ? -1.0F : 0;
			box(leg, "heel_casing", DARK, center - 2.7F * s, top, heel - 2.5F * s, 5.4F * s, 3.1F * s, 5.4F * s);
			box(leg, "toe_cap", METAL, center - 2.7F * s, top + 1.0F * s, heel - 3.9F * s, 5.4F * s, 2.0F * s, 2.0F * s);
			box(leg, "sole", EDGE, center - 2.8F * s, top + 2.8F * s, heel - 3.9F * s, 5.6F * s, 0.9F * s, 6.8F * s);
			box(leg, "core_vent", AMBER, center - 1.4F * s, top + 0.45F * s, heel - 2.85F * s, 2.8F * s, 0.55F * s, 0.5F * s);
		}
	}
}
