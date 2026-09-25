package dev.epicrelics.mixin;

import dev.epicrelics.item.RelicArmorEnchantments;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingMenu.class)
abstract class SmithingMenuMixin {
	@Shadow
	@Final
	private Level level;

	@Inject(method = "createResult", at = @At("TAIL"))
	private void epicRelics$applyInnateProtections(CallbackInfo ci) {
		SmithingMenu menu = (SmithingMenu) (Object) this;
		RelicArmorEnchantments.enforceInnateProtections(
				menu.getSlot(menu.getResultSlot()).getItem(), level.registryAccess());
	}
}
