package dev.epicrelics.mixin;

import dev.epicrelics.item.RelicArmorEnchantments;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrindstoneMenu.class)
abstract class GrindstoneMenuMixin {
	@Shadow
	@Final
	private ContainerLevelAccess access;

	@Inject(method = "removeNonCursesFrom", at = @At("RETURN"))
	private void epicRelics$restoreInnateProtections(
			ItemStack item, CallbackInfoReturnable<ItemStack> cir) {
		ItemStack result = cir.getReturnValue();
		this.access.execute((level, pos) ->
				RelicArmorEnchantments.enforceInnateProtections(result, level.registryAccess()));
	}
}
