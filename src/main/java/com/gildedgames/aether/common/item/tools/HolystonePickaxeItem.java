package com.gildedgames.aether.common.item.tools;

import com.gildedgames.aether.common.item.tools.abilities.IHolystoneToolItem;
import com.gildedgames.aether.common.registry.AetherItemGroups;
import com.gildedgames.aether.common.registry.AetherItemTiers;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HolystonePickaxeItem extends PickaxeItem implements IHolystoneToolItem
{
	public HolystonePickaxeItem() {
		super(AetherItemTiers.HOLYSTONE, 1, -2.8F, new Item.Properties().tab(AetherItemGroups.AETHER_TOOLS));
	}
	
	@Override
	public boolean mineBlock(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
		spawnAmbrosiumDrops(worldIn, pos);
		return super.mineBlock(stack, worldIn, state, pos, entityLiving);
	}
}
