package dev.epicrelics.item;

import dev.epicrelics.ability.ResonanceBowAbility;
import dev.epicrelics.ability.DarknessArrowMarker;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class ResonanceBowItem extends BowItem {
	public ResonanceBowItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
		if (entity instanceof Player player && ResonanceBowAbility.isSonicMode(stack)) {
			int timeHeld = this.getUseDuration(stack, entity) - remainingTime;
			if (BowItem.getPowerForTime(timeHeld) < 1.0F) {
				return false;
			}
			if (level instanceof ServerLevel && player instanceof ServerPlayer serverPlayer) {
				if (!ResonanceBowAbility.isSonicReady(serverPlayer)) {
					return false;
				}
				ItemStack projectile = player.getProjectile(stack);
				if (projectile.isEmpty()) {
					return false;
				}
				ItemStack ammo = useAmmo(stack, projectile, player, false);
				if (ammo.isEmpty()) {
					return false;
				}
				ResonanceBowAbility.castSonicBoom(serverPlayer);
			}
			return true;
		}
		return super.releaseUsing(stack, level, entity, remainingTime);
	}

	@Override
	protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack projectile, boolean isCrit) {
		Projectile result = super.createProjectile(level, shooter, weapon, projectile, isCrit);
		if (result instanceof DarknessArrowMarker marker && !ResonanceBowAbility.isSonicMode(weapon)) {
			marker.epicRelics$setDarknessArrow(true);
		}
		return result;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		super.appendHoverText(stack, context, display, builder, flag);
		boolean sonic = ResonanceBowAbility.isSonicMode(stack);
		builder.accept(Component.translatable(sonic
				? "tooltip.epic_relics.resonance_mode.sonic"
				: "tooltip.epic_relics.resonance_mode.darkness")
				.withStyle(sonic ? ChatFormatting.AQUA : ChatFormatting.DARK_PURPLE));
		builder.accept(Component.translatable(sonic
				? "tooltip.epic_relics.resonance_bow.sonic"
				: "tooltip.epic_relics.resonance_bow.darkness")
				.withStyle(ChatFormatting.GRAY));
		builder.accept(Component.translatable("tooltip.epic_relics.resonance_bow.toggle")
				.withStyle(ChatFormatting.DARK_GRAY));
	}
}
