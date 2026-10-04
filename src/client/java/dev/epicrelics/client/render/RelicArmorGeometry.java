package dev.epicrelics.client.render;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.EquipmentSlot;

/** Articulated draconic platework; both material passes sample the unchanged armor texture. */
final class RelicArmorGeometry {
	private static final float DIAMOND = (float) Math.PI / 4.0F;

	private RelicArmorGeometry() {
	}

	static LayerDefinition create(EquipmentSlot slot, boolean baby, boolean gilding) {
		Geometry geometry = new Geometry(gilding);
		switch (slot) {
			case HEAD -> helmet(geometry, baby);
			case CHEST -> chest(geometry, baby);
			case LEGS -> leggings(geometry, baby);
			case FEET -> boots(geometry, baby);
			default -> throw new IllegalArgumentException("Not an armor slot: " + slot);
		}
		return LayerDefinition.create(geometry.mesh, 64, 32);
	}

	private static void helmet(Geometry g, boolean baby) {
		PartDefinition head = g.bone("head", 0, baby ? 0.9F : 0, 0,
				baby ? 1.04F : 1, baby ? 1.04F : 1, baby ? 1.04F : 1);
		// A closed face backing conceals the vanilla skin, including the lower beard area.
		g.plate(head, "visor_backing", Material.BLACK, 0, -4.85F, -4.91F, 8.9F, 3.0F, 0.50F);
		g.plate(head, "forehead_mask", Material.DARK, 0, -6.72F, -5.14F, 8.9F, 1.60F, 0.30F);
		g.plate(head, "jaw_backing", Material.BLACK, 0, -1.75F, -4.70F, 8.0F, 3.7F, 0.55F);
		g.plate(head, "rear_jaw_wrap", Material.BLACK, 0, -1.75F, 4.38F, 8.8F, 3.60F, 0.65F);
		g.plate(head, "forehead_keel", Material.METAL, 0, -7.0F, -5.03F, 1.1F, 2.4F, 0.58F);
		g.plate(head, "crown_sigil", Material.VIOLET, 0, -7.35F, -5.37F, 0.7F, 0.7F, 0.16F, 0, 0, DIAMOND);
		g.plate(head, "nose_keel", Material.METAL, 0, -3.0F, -5.40F, 1.15F, 3.1F, 0.78F);
		g.plate(head, "chin_scale", Material.DARK, 0, -0.6F, -4.97F, 1.7F, 1.7F, 0.58F, 0, 0, DIAMOND);
		for (int sign : new int[] {-1, 1}) {
			String side = sign < 0 ? "right" : "left";
			g.plate(head, side + "_eye_slit", Material.VIOLET, sign * 2.15F, -4.77F, -5.23F,
					2.55F, 0.24F, 0.14F, 0, 0, sign * -0.10F);
			g.plate(head, side + "_brow_ridge", Material.GOLD, sign * 2.15F, -5.20F, -5.30F,
					3.0F, 0.32F, 0.32F, 0, 0, sign * -0.10F);
			g.plate(head, side + "_cheek_scale", Material.DARK, sign * 2.4F, -2.1F, -5.06F,
					3.55F, 3.35F, 0.58F, 0, sign * -0.12F, sign * -0.16F);
			g.plate(head, side + "_jaw_edge", Material.GOLD, sign * 2.05F, -0.50F, -5.30F,
					2.25F, 0.20F, 0.21F, 0, 0, sign * -0.17F);
			g.plate(head, side + "_temple_scale", Material.METAL, sign * 4.56F, -5.1F, -0.6F,
					0.80F, 3.45F, 4.8F, -0.14F, 0, sign * -0.10F);
			g.plate(head, side + "_jaw_wrap", Material.DARK, sign * 4.42F, -1.75F, 0.1F,
					0.80F, 3.90F, 8.5F);
			g.plate(head, side + "_jaw_wrap_edge", Material.GOLD, sign * 4.88F, -0.17F, -0.5F,
					0.16F, 0.19F, 5.6F);
			// Three shrinking segments sweep backwards, rather than extending vertically like aerials.
			g.strut(head, side + "_horn_root", Material.DARK,
					sign * 3.8F, -8.2F, 0.5F, sign * 4.80F, -10.40F, 1.7F, 1.65F, 1.5F);
			g.strut(head, side + "_horn_middle", Material.METAL,
					sign * 4.80F, -10.40F, 1.7F, sign * 5.60F, -11.90F, 3.1F, 1.22F, 1.10F);
			g.strut(head, side + "_horn_tip", Material.DARK,
					sign * 5.60F, -11.90F, 3.1F, sign * 6.0F, -12.10F, 5.0F, 0.55F, 0.60F);
			g.strut(head, side + "_horn_collar", Material.GOLD,
					sign * 4.17F, -9.0F, 0.94F, sign * 4.35F, -9.37F, 1.13F, 1.72F, 1.57F);
		}
	}

	private static void chest(Geometry g, boolean baby) {
		PartDefinition body = g.bone("body", 0, baby ? -3.0F : 0, 0,
				baby ? 0.73F : 1, baby ? 0.56F : 1, baby ? 0.84F : 1);
		// A low articulated gorget covers the vanilla shirt collar while allowing head rotation.
		g.plate(body, "gorget_front", Material.BLACK, 0, 0.4F, -3.50F, 5.7F, 2.10F, 0.70F);
		g.plate(body, "gorget_rear", Material.BLACK, 0, 0.4F, 2.55F, 5.6F, 1.70F, 0.55F);
		g.plate(body, "gorget_right", Material.DARK, -2.65F, 0.45F, -0.10F, 0.65F, 1.80F, 5.3F);
		g.plate(body, "gorget_left", Material.DARK, 2.65F, 0.45F, -0.10F, 0.65F, 1.80F, 5.3F);
		g.plate(body, "gorget_edge", Material.GOLD, 0, 1.15F, -3.93F, 5.1F, 0.16F, 0.15F);
		// Two diagonal rows meet at the breastbone; the waist is deliberately narrower.
		for (int sign : new int[] {-1, 1}) {
			String side = sign < 0 ? "right" : "left";
			g.plate(body, side + "_pectoral_scale", Material.DARK, sign * 2.0F, 2.9F, -3.21F,
					4.5F, 2.10F, 0.76F, 0, sign * 0.08F, sign * -0.20F);
			g.plate(body, side + "_pectoral_edge", Material.GOLD, sign * 2.0F, 2.11F, -3.66F,
					4.35F, 0.22F, 0.20F, 0, 0, sign * -0.20F);
			g.plate(body, side + "_rib_scale", Material.METAL, sign * 1.75F, 5.15F, -3.22F,
					3.8F, 1.65F, 0.59F, 0, sign * 0.07F, sign * -0.25F);
			g.plate(body, side + "_rib_edge", Material.GOLD, sign * 1.75F, 5.85F, -3.55F,
					3.70F, 0.19F, 0.17F, 0, 0, sign * -0.25F);
		}
		g.plate(body, "heart_setting", Material.GOLD, 0, 3.10F, -3.90F, 2.12F, 2.12F, 0.46F, 0, 0, DIAMOND);
		g.plate(body, "heart_crystal", Material.VIOLET, 0, 3.10F, -4.20F, 1.56F, 1.56F, 0.30F, 0, 0, DIAMOND);
		g.plate(body, "heart_facet", Material.METAL, 0.42F, 3.43F, -4.38F, 0.42F, 0.78F, 0.12F, 0, 0, DIAMOND);
		g.plate(body, "waist_scale", Material.DARK, 0, 8.00F, -3.11F, 5.3F, 2.35F, 0.48F);
		g.plate(body, "waist_clasp", Material.GOLD, 0, 9.18F, -3.42F, 1.35F, 0.55F, 0.22F);

		for (int sign : new int[] {-1, 1}) {
			PartDefinition arm = g.bone(sign < 0 ? "right_arm" : "left_arm",
					baby ? 0 : sign, baby ? 0.2F : 0, 0,
					baby ? 0.63F : 1, baby ? 0.48F : 1, baby ? 0.72F : 1);
			for (int layer = 0; layer < 3; layer++) {
				float width = 6.10F - layer * 0.45F;
				float depth = 6.35F - layer * 0.15F;
				float centerX = sign * (0.15F + layer * 0.25F);
				float centerY = -3.05F + layer * 1.25F;
				float angle = sign * -0.23F;
				g.plate(arm, "shoulder_scale_" + layer, layer == 1 ? Material.METAL : Material.DARK,
						centerX, centerY, 0, width, 1.28F, depth, 0, 0, angle);
				g.plate(arm, "shoulder_edge_" + layer, Material.GOLD,
						centerX, centerY + 0.43F, -depth / 2 - 0.11F, width - 0.14F, 0.20F, 0.19F, 0, 0, angle);
			}
			g.plate(arm, "vambrace_shell", Material.BLACK, 0, 6.28F, 0, 4.55F, 4.40F, 4.65F);
			g.plate(arm, "vambrace_scale", Material.DARK, sign * 0.32F, 5.70F, -2.56F,
					3.6F, 2.45F, 0.58F, 0, 0, sign * -0.12F);
			g.plate(arm, "vambrace_rune", Material.VIOLET, sign * 0.32F, 5.85F, -2.90F,
					0.23F, 1.68F, 0.14F, 0, 0, sign * -0.12F);
			g.plate(arm, "wrist_rim", Material.GOLD, 0, 8.10F, -2.49F, 4.35F, 0.30F, 0.21F);
			g.plate(arm, "gauntlet", Material.BLACK, 0, 9.22F, -0.08F, 4.36F, 1.86F, 4.36F);
			g.plate(arm, "knuckle_plate", Material.METAL, 0, 9.1F, -2.40F, 3.35F, 1.0F, 0.43F);
		}
		// No rear chest attachments: the original wing and cape layers keep their clearance.
	}

	private static void leggings(Geometry g, boolean baby) {
		for (int sign : new int[] {-1, 1}) {
			PartDefinition leg = g.bone(sign < 0 ? "right_leg" : "left_leg",
					baby ? -sign * 0.5F : 0, 0, baby ? -0.55F : 0,
					baby ? 0.72F : 1, baby ? 0.34F : 1, baby ? 0.73F : 1);
			for (int layer = 0; layer < 2; layer++) {
				g.plate(leg, "thigh_scale_" + layer, layer == 0 ? Material.DARK : Material.METAL,
						sign * 0.1F, 1.2F + layer * 1.60F, -2.83F,
						3.85F - layer * 0.30F, 1.93F, 0.50F, 0, 0, sign * -0.12F);
				g.plate(leg, "thigh_edge_" + layer, Material.GOLD,
						sign * 0.1F, 1.94F + layer * 1.60F, -3.12F,
						3.55F - layer * 0.30F, 0.18F, 0.17F, 0, 0, sign * -0.12F);
				g.plate(leg, "side_tasset_" + layer, Material.DARK,
						sign * (2.65F + layer * 0.16F), 1.15F + layer * 1.65F, -0.15F,
						0.72F, 2.35F - layer * 0.28F, 4.55F - layer * 0.5F, 0, 0, sign * -0.16F);
			}
			g.plate(leg, "knee_setting", Material.GOLD, 0, 5.90F, -3.06F, 1.87F, 1.87F, 0.42F, 0, 0, DIAMOND);
			g.plate(leg, "knee_scale", Material.DARK, 0, 5.90F, -3.35F, 1.47F, 1.47F, 0.34F, 0, 0, DIAMOND);
			g.plate(leg, "knee_rune", Material.VIOLET, 0, 5.90F, -3.55F, 0.35F, 0.35F, 0.12F, 0, 0, DIAMOND);
		}
	}

	private static void boots(Geometry g, boolean baby) {
		for (int sign : new int[] {-1, 1}) {
			PartDefinition leg = g.bone(sign < 0 ? "right_leg" : "left_leg",
					baby ? -sign * 0.5F : 0, 0, baby ? -0.55F : 0,
					baby ? 0.72F : 1, baby ? 0.34F : 1, baby ? 0.73F : 1);
			// Conceals the old orange upper-boot stripe without changing its shared texture.
			g.plate(leg, "upper_greave_band", Material.BLACK, 0, 6.5F, 0, 5.95F, 1.04F, 5.95F);
			g.plate(leg, "shin_scale", Material.DARK, 0, 8.04F, -2.91F, 3.20F, 2.70F, 0.50F);
			g.plate(leg, "shin_inlay", Material.VIOLET, 0, 7.98F, -3.20F, 0.23F, 1.50F, 0.13F);
			g.plate(leg, "ankle_scale", Material.BLACK, 0, 9.78F, -2.89F, 3.85F, 0.66F, 0.44F);
			g.plate(leg, "ankle_rim", Material.GOLD, 0, 10.20F, -3.17F, 3.82F, 0.18F, 0.15F);
			g.plate(leg, "instep", Material.DARK, 0, 11.12F, -2.90F, 4.50F, 1.28F, 1.42F, -0.15F, 0, 0);
			g.plate(leg, "toe_scale", Material.METAL, 0, 11.54F, -3.32F, 4.07F, 0.74F, 0.86F);
			g.plate(leg, "toe_rim", Material.GOLD, 0, 11.69F, -3.79F, 3.86F, 0.15F, 0.15F);
			g.plate(leg, "thin_sole", Material.BLACK, 0, 12.13F, -0.34F, 5.03F, 0.29F, 6.10F);
		}
	}

	private enum Material {
		BLACK(8, 2), DARK(8, 0), METAL(10, 1), VIOLET(9, 10), GOLD(4, 26);
		final int u;
		final int v;

		Material(int u, int v) {
			this.u = u;
			this.v = v;
		}
	}

	private static final class Geometry {
		final MeshDefinition mesh = new MeshDefinition();
		private final boolean gilding;

		Geometry(boolean gilding) {
			this.gilding = gilding;
		}

		PartDefinition bone(String name, float x, float y, float z, float sx, float sy, float sz) {
			PartDefinition bone = mesh.getRoot().addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO);
			// The uniquely named child retains its fit adjustment when the named parent bone is copied.
			return bone.addOrReplaceChild("relic_fit", CubeListBuilder.create(), new PartPose(x, y, z, 0, 0, 0, sx, sy, sz));
		}

		void plate(PartDefinition parent, String name, Material material, float x, float y, float z,
				float width, float height, float depth) {
			plate(parent, name, material, x, y, z, width, height, depth, 0, 0, 0);
		}

		void plate(PartDefinition parent, String name, Material material, float x, float y, float z,
				float width, float height, float depth, float rx, float ry, float rz) {
			if ((material == Material.GOLD) != gilding) {
				return;
			}
			// Every face stays within one opaque texel in the existing 64 x 32 atlas.
			parent.addOrReplaceChild(name, CubeListBuilder.create().texOffs(material.u * 64 + 16, material.v * 64 + 16)
					.addBox(-width / 2, -height / 2, -depth / 2, width, height, depth,
							CubeDeformation.NONE, 64.0F, 64.0F), PartPose.offsetAndRotation(x, y, z, rx, ry, rz));
		}

		void strut(PartDefinition parent, String name, Material material,
				float ax, float ay, float az, float bx, float by, float bz, float width, float depth) {
			float dx = bx - ax;
			float dy = by - ay;
			float dz = bz - az;
			float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
			// ModelPart uses Rz * Ry * Rx. Align the local long Y axis to the segment.
			float rx = (float) Math.asin(dz / length);
			float rz = (float) Math.atan2(-dx, dy);
			plate(parent, name, material, (ax + bx) / 2, (ay + by) / 2, (az + bz) / 2,
					width, length + 0.12F, depth, rx, 0, rz);
		}
	}
}
