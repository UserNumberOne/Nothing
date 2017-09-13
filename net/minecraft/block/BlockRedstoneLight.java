package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockRedstoneLight extends Block {
   private final boolean isOn;

   public BlockRedstoneLight(boolean var1) {
      super(Material.REDSTONE_LIGHT);
      this.isOn = var1;
      if (var1) {
         this.setLightLevel(1.0F);
      }

   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      if (!var1.isRemote) {
         if (this.isOn && !var1.isBlockPowered(var2)) {
            var1.setBlockState(var2, Blocks.REDSTONE_LAMP.getDefaultState(), 2);
         } else if (!this.isOn && var1.isBlockPowered(var2)) {
            var1.setBlockState(var2, Blocks.LIT_REDSTONE_LAMP.getDefaultState(), 2);
         }
      }

   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!var2.isRemote) {
         if (this.isOn && !var2.isBlockPowered(var3)) {
            var2.scheduleUpdate(var3, this, 4);
         } else if (!this.isOn && var2.isBlockPowered(var3)) {
            var2.setBlockState(var3, Blocks.LIT_REDSTONE_LAMP.getDefaultState(), 2);
         }
      }

   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!var1.isRemote && this.isOn && !var1.isBlockPowered(var2)) {
         var1.setBlockState(var2, Blocks.REDSTONE_LAMP.getDefaultState(), 2);
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Item.getItemFromBlock(Blocks.REDSTONE_LAMP);
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Blocks.REDSTONE_LAMP);
   }

   protected ItemStack getSilkTouchDrop(IBlockState var1) {
      return new ItemStack(Blocks.REDSTONE_LAMP);
   }
}
