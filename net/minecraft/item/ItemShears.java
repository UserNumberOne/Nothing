package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemShears extends Item {
   public ItemShears() {
      this.setMaxStackSize(1);
      this.setMaxDamage(238);
      this.setCreativeTab(CreativeTabs.TOOLS);
   }

   public boolean onBlockDestroyed(ItemStack var1, World var2, IBlockState var3, BlockPos var4, EntityLivingBase var5) {
      var1.damageItem(1, var5);
      Block var6 = var3.getBlock();
      return var3.getMaterial() != Material.LEAVES && var6 != Blocks.WEB && var6 != Blocks.TALLGRASS && var6 != Blocks.VINE && var6 != Blocks.TRIPWIRE && var6 != Blocks.WOOL ? super.onBlockDestroyed(var1, var2, var3, var4, var5) : true;
   }

   public boolean canHarvestBlock(IBlockState var1) {
      Block var2 = var1.getBlock();
      return var2 == Blocks.WEB || var2 == Blocks.REDSTONE_WIRE || var2 == Blocks.TRIPWIRE;
   }

   public float getStrVsBlock(ItemStack var1, IBlockState var2) {
      Block var3 = var2.getBlock();
      if (var3 != Blocks.WEB && var2.getMaterial() != Material.LEAVES) {
         return var3 == Blocks.WOOL ? 5.0F : super.getStrVsBlock(var1, var2);
      } else {
         return 15.0F;
      }
   }
}
