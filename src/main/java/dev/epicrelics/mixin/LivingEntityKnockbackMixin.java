package dev.epicrelics.mixin;

import dev.epicrelics.ability.RelicEquipment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
abstract class LivingEntityKnockbackMixin {
	@ModifyVariable(
			method = "knockback(DDDLnet/minecraft/world/damagesource/DamageSource;FZ)V",
			at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private double epicRelics$halveGlidingKnockback(double strength) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self instanceof Player && self.isFallFlying() && RelicEquipment.hasChestplate(self)) {
			return strength * 0.5;
		}
		return strength;
	}
}
