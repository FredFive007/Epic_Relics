package dev.epicrelics.mixin;

import dev.epicrelics.ability.StompAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityLandingMixin {
	@Inject(method = "causeFallDamage", at = @At("HEAD"))
	private void epicRelics$triggerStomp(double fallDistance, float multiplier, DamageSource source,
			CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof ServerPlayer player) {
			StompAbility.onLanding(player, fallDistance);
		}
	}
}
