package dev.epicrelics.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public final class RelicArmorItem extends Item {
	private final String tooltipPath;

	public RelicArmorItem(Properties properties, String tooltipPath) {
		super(properties);
		this.tooltipPath = tooltipPath;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
			Consumer<Component> builder, TooltipFlag flag) {
		super.appendHoverText(stack, context, display, builder, flag);
		builder.accept(Component.translatable("tooltip.epic_relics." + tooltipPath + ".ability")
				.withStyle(ChatFormatting.DARK_PURPLE));
		builder.accept(Component.translatable("tooltip.epic_relics." + tooltipPath + ".detail")
				.withStyle(ChatFormatting.GRAY));
		builder.accept(Component.translatable("tooltip.epic_relics." + tooltipPath + ".control")
				.withStyle(ChatFormatting.DARK_GRAY));
	}
}
