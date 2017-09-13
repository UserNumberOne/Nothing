package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockFrostedIce extends BlockIce {
   public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);

   public BlockFrostedIce() {
      this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)var1.getValue(AGE)).intValue();
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(AGE, Integer.valueOf(MathHelper.clamp(var1, 0, 3)));
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if ((var4.nextInt(3) == 0 || this.countNeighbors(var1, var2) < 4) && var1.getLightFromNeighbors(var2) > 11 - ((Integer)var3.getValue(AGE)).intValue() - var3.getLightOpacity()) {
         this.slightlyMelt(var1, var2, var3, var4, true);
      } else {
         var1.scheduleUpdate(var2, this, MathHelper.getInt(var4, 20, 40));
      }

   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (var4 == this) {
         int var5 = this.countNeighbors(var2, var3);
         if (var5 < 2) {
            this.turnIntoWater(var2, var3);
         }
      }

   }

   private int countNeighbors(World var1, BlockPos var2) {
      int var3 = 0;

      for(EnumFacing var7 : EnumFacing.values()) {
         if (var1.getBlockState(var2.offset(var7)).getBlock() == this) {
            ++var3;
            if (var3 >= 4) {
               return var3;
            }
         }
      }

      return var3;
   }

   protected void slightlyMelt(World var1, BlockPos var2, IBlockState var3, Random var4, boolean var5) {
      int var6 = ((Integer)var3.getValue(AGE)).intValue();
      if (var6 < 3) {
         var1.setBlockState(var2, var3.withProperty(AGE, Integer.valueOf(var6 + 1)), 2);
         var1.scheduleUpdate(var2, this, MathHelper.getInt(var4, 20, 40));
      } else {
         this.turnIntoWater(var1, var2);
         if (var5) {
            for(EnumFacing var10 : EnumFacing.values()) {
               BlockPos var11 = var2.offset(var10);
               IBlockState var12 = var1.getBlockState(var11);
               if (var12.getBlock() == this) {
                  this.slightlyMelt(var1, var11, var12, var4, false);
               }
            }
         }
      }

   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AGE});
   }

   @Nullable
   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return null;
   }
}
