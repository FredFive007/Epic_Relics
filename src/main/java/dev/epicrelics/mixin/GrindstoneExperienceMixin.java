package dev.epicrelics.mixin;

import dev.epicrelics.item.RelicArmorEnchantments;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.GrindstoneMenu$4")
abstract class GrindstoneExperienceMixin {
	@Inject(method = "getExperienceFromItem", at = @At("HEAD"), cancellable = true)
	private void epicRelics$excludeInnateProtections(
			ItemStack item, CallbackInfoReturnable<Integer> cir) {
		if (RelicArmorEnchantments.isRelicArmor(item)) {
			cir.setReturnValue(RelicArmorEnchantments.getGrindstoneExperience(item));
		}
	}
}
