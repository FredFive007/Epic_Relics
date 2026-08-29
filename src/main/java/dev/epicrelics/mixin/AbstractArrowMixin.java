package dev.epicrelics.mixin;

import dev.epicrelics.ability.ResonanceBowAbility;
import dev.epicrelics.ability.DarknessArrowMarker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin implements DarknessArrowMarker {
	@Unique
	private boolean epicRelics$darknessArrow;

	@Override
	public void epicRelics$setDarknessArrow(boolean value) {
		this.epicRelics$darknessArrow = value;
	}

	@Override
	public boolean epicRelics$isDarknessArrow() {
		return this.epicRelics$darknessArrow;
	}

	@Inject(method = "onHitEntity", at = @At("TAIL"))
	private void epicRelics$darknessOnEntityHit(EntityHitResult result, CallbackInfo ci) {
		this.epicRelics$applyDarkness(result.getLocation());
	}

	@Inject(method = "onHitBlock", at = @At("TAIL"))
	private void epicRelics$darknessOnBlockHit(BlockHitResult result, CallbackInfo ci) {
		this.epicRelics$applyDarkness(result.getLocation());
	}

	private void epicRelics$applyDarkness(Vec3 pos) {
		if (!this.epicRelics$darknessArrow) {
			return;
		}
		AbstractArrow arrow = (AbstractArrow) (Object) this;
		if (arrow.level() instanceof ServerLevel level && arrow.getOwner() instanceof ServerPlayer player) {
			ResonanceBowAbility.applyDarknessPulse(level, pos, player);
		}
	}
}
