package dev.epicrelics.item;

import dev.epicrelics.ability.GravityBladeAbility;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public final class GravityBladeItem extends Item {
	public GravityBladeItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
			Consumer<Component> builder, TooltipFlag flag) {
		super.appendHoverText(stack, context, display, builder, flag);
		builder.accept(Component.translatable("tooltip.epic_relics.gravity_blade.plunge")
				.withStyle(ChatFormatting.DARK_PURPLE));
		builder.accept(Component.translatable("tooltip.epic_relics.gravity_blade.field")
				.withStyle(ChatFormatting.GRAY));
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			GravityBladeAbility.cast(serverPlayer);
		}
		return level.isClientSide() ? InteractionResult.PASS : InteractionResult.SUCCESS;
	}

	@Override
	public float getAttackDamageBonus(Entity victim, float damage, DamageSource source) {
		if (source.getDirectEntity() instanceof Player attacker && GravityBladeAbility.canPlunge(attacker)) {
			return (float) GravityBladeAbility.plungeBonus(attacker.fallDistance);
		}
		return 0.0F;
	}

	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (attacker instanceof Player player && GravityBladeAbility.canPlunge(player)) {
			GravityBladeAbility.consumePlunge(player);
			GravityBladeAbility.spawnPlungeImpact(player, target);
		}
	}
}
