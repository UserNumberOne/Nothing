package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBeetroot extends BlockCrops {
   public static final PropertyInteger BEETROOT_AGE = PropertyInteger.create("age", 0, 3);
   private static final AxisAlignedBB[] BEETROOT_AABB = new AxisAlignedBB[]{new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.375D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D)};

   protected PropertyInteger getAgeProperty() {
      return BEETROOT_AGE;
   }

   public int getMaxAge() {
      return 3;
   }

   protected Item getSeed() {
      return Items.BEETROOT_SEEDS;
   }

   protected Item getCrop() {
      return Items.BEETROOT;
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (var4.nextInt(3) == 0) {
         this.checkAndDropBlock(var1, var2, var3);
      } else {
         super.updateTick(var1, var2, var3, var4);
      }

   }

   protected int getBonemealAgeIncrease(World var1) {
      return super.getBonemealAgeIncrease(var1) / 3;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{BEETROOT_AGE});
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return BEETROOT_AABB[((Integer)var1.getValue(this.getAgeProperty())).intValue()];
   }
}
