package dev.epicrelics.mixin;

import dev.epicrelics.ability.RelicEquipment;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enderman.class)
abstract class EnderManMixin {
	@Inject(method = "isBeingStaredBy", at = @At("HEAD"), cancellable = true)
	private void epicRelics$ignoreDragonSightStare(Player player, CallbackInfoReturnable<Boolean> cir) {
		if (RelicEquipment.hasHelmet(player)) {
			cir.setReturnValue(false);
		}
	}
}
