package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockOre extends Block {
   public BlockOre() {
      this(Material.ROCK.getMaterialMapColor());
   }

   public BlockOre(MapColor var1) {
      super(Material.ROCK, var1);
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return this == Blocks.COAL_ORE ? Items.COAL : (this == Blocks.DIAMOND_ORE ? Items.DIAMOND : (this == Blocks.LAPIS_ORE ? Items.DYE : (this == Blocks.EMERALD_ORE ? Items.EMERALD : (this == Blocks.QUARTZ_ORE ? Items.QUARTZ : Item.getItemFromBlock(this)))));
   }

   public int quantityDropped(Random var1) {
      return this == Blocks.LAPIS_ORE ? 4 + var1.nextInt(5) : 1;
   }

   public int quantityDroppedWithBonus(int var1, Random var2) {
      if (var1 > 0 && Item.getItemFromBlock(this) != this.getItemDropped((IBlockState)this.getBlockState().getValidStates().iterator().next(), var2, var1)) {
         int var3 = var2.nextInt(var1 + 2) - 1;
         if (var3 < 0) {
            var3 = 0;
         }

         return this.quantityDropped(var2) * (var3 + 1);
      } else {
         return this.quantityDropped(var2);
      }
   }

   public void dropBlockAsItemWithChance(World var1, BlockPos var2, IBlockState var3, float var4, int var5) {
      super.dropBlockAsItemWithChance(var1, var2, var3, var4, var5);
   }

   public int getExpDrop(IBlockState var1, IBlockAccess var2, BlockPos var3, int var4) {
      Random var5 = var2 instanceof World ? ((World)var2).rand : new Random();
      if (this.getItemDropped(var1, var5, var4) != Item.getItemFromBlock(this)) {
         int var6 = 0;
         if (this == Blocks.COAL_ORE) {
            var6 = MathHelper.getInt(var5, 0, 2);
         } else if (this == Blocks.DIAMOND_ORE) {
            var6 = MathHelper.getInt(var5, 3, 7);
         } else if (this == Blocks.EMERALD_ORE) {
            var6 = MathHelper.getInt(var5, 3, 7);
         } else if (this == Blocks.LAPIS_ORE) {
            var6 = MathHelper.getInt(var5, 2, 5);
         } else if (this == Blocks.QUARTZ_ORE) {
            var6 = MathHelper.getInt(var5, 2, 5);
         }

         return var6;
      } else {
         return 0;
      }
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(this);
   }

   public int damageDropped(IBlockState var1) {
      return this == Blocks.LAPIS_ORE ? EnumDyeColor.BLUE.getDyeDamage() : 0;
   }
}
