package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class BlockCocoa extends BlockHorizontal implements IGrowable {
   public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 2);
   protected static final AxisAlignedBB[] COCOA_EAST_AABB = new AxisAlignedBB[]{new AxisAlignedBB(0.6875D, 0.4375D, 0.375D, 0.9375D, 0.75D, 0.625D), new AxisAlignedBB(0.5625D, 0.3125D, 0.3125D, 0.9375D, 0.75D, 0.6875D), new AxisAlignedBB(0.5625D, 0.3125D, 0.3125D, 0.9375D, 0.75D, 0.6875D)};
   protected static final AxisAlignedBB[] COCOA_WEST_AABB = new AxisAlignedBB[]{new AxisAlignedBB(0.0625D, 0.4375D, 0.375D, 0.3125D, 0.75D, 0.625D), new AxisAlignedBB(0.0625D, 0.3125D, 0.3125D, 0.4375D, 0.75D, 0.6875D), new AxisAlignedBB(0.0625D, 0.3125D, 0.3125D, 0.4375D, 0.75D, 0.6875D)};
   protected static final AxisAlignedBB[] COCOA_NORTH_AABB = new AxisAlignedBB[]{new AxisAlignedBB(0.375D, 0.4375D, 0.0625D, 0.625D, 0.75D, 0.3125D), new AxisAlignedBB(0.3125D, 0.3125D, 0.0625D, 0.6875D, 0.75D, 0.4375D), new AxisAlignedBB(0.3125D, 0.3125D, 0.0625D, 0.6875D, 0.75D, 0.4375D)};
   protected static final AxisAlignedBB[] COCOA_SOUTH_AABB = new AxisAlignedBB[]{new AxisAlignedBB(0.375D, 0.4375D, 0.6875D, 0.625D, 0.75D, 0.9375D), new AxisAlignedBB(0.3125D, 0.3125D, 0.5625D, 0.6875D, 0.75D, 0.9375D), new AxisAlignedBB(0.3125D, 0.3125D, 0.5625D, 0.6875D, 0.75D, 0.9375D)};

   public BlockCocoa() {
      super(Material.PLANTS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(AGE, Integer.valueOf(0)));
      this.setTickRandomly(true);
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!this.canBlockStay(var1, var2, var3)) {
         this.dropBlock(var1, var2, var3);
      } else if (var1.rand.nextInt(5) == 0) {
         int var5 = ((Integer)var3.getValue(AGE)).intValue();
         if (var5 < 2) {
            IBlockState var6 = var3.withProperty(AGE, Integer.valueOf(var5 + 1));
            CraftEventFactory.handleBlockGrowEvent(var1, var2.getX(), var2.getY(), var2.getZ(), this, this.getMetaFromState(var6));
         }
      }

   }

   public boolean canBlockStay(World var1, BlockPos var2, IBlockState var3) {
      var2 = var2.offset((EnumFacing)var3.getValue(FACING));
      IBlockState var4 = var1.getBlockState(var2);
      return var4.getBlock() == Blocks.LOG && var4.getValue(BlockOldLog.VARIANT) == BlockPlanks.EnumType.JUNGLE;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      int var4 = ((Integer)var1.getValue(AGE)).intValue();
      switch(BlockCocoa.SyntheticClass_1.a[((EnumFacing)var1.getValue(FACING)).ordinal()]) {
      case 1:
         return COCOA_SOUTH_AABB[var4];
      case 2:
      default:
         return COCOA_NORTH_AABB[var4];
      case 3:
         return COCOA_WEST_AABB[var4];
      case 4:
         return COCOA_EAST_AABB[var4];
      }
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return var1.withProperty(FACING, var2.rotate((EnumFacing)var1.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return var1.withRotation(var2.toRotation((EnumFacing)var1.getValue(FACING)));
   }

   public void onBlockPlacedBy(World var1, BlockPos var2, IBlockState var3, EntityLivingBase var4, ItemStack var5) {
      EnumFacing var6 = EnumFacing.fromAngle((double)var4.rotationYaw);
      var1.setBlockState(var2, var3.withProperty(FACING, var6), 2);
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      if (!var3.getAxis().isHorizontal()) {
         var3 = EnumFacing.NORTH;
      }

      return this.getDefaultState().withProperty(FACING, var3.getOpposite()).withProperty(AGE, Integer.valueOf(0));
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!this.canBlockStay(var2, var3, var1)) {
         this.dropBlock(var2, var3, var1);
      }

   }

   private void dropBlock(World var1, BlockPos var2, IBlockState var3) {
      var1.setBlockState(var2, Blocks.AIR.getDefaultState(), 3);
      this.dropBlockAsItem(var1, var2, var3, 0);
   }

   public void dropBlockAsItemWithChance(World var1, BlockPos var2, IBlockState var3, float var4, int var5) {
      int var6 = ((Integer)var3.getValue(AGE)).intValue();
      byte var7 = 1;
      if (var6 >= 2) {
         var7 = 3;
      }

      for(int var8 = 0; var8 < var7; ++var8) {
         spawnAsEntity(var1, var2, new ItemStack(Items.DYE, 1, EnumDyeColor.BROWN.getDyeDamage()));
      }

   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Items.DYE, 1, EnumDyeColor.BROWN.getDyeDamage());
   }

   public boolean canGrow(World var1, BlockPos var2, IBlockState var3, boolean var4) {
      return ((Integer)var3.getValue(AGE)).intValue() < 2;
   }

   public boolean canUseBonemeal(World var1, Random var2, BlockPos var3, IBlockState var4) {
      return true;
   }

   public void grow(World var1, Random var2, BlockPos var3, IBlockState var4) {
      IBlockState var5 = var4.withProperty(AGE, Integer.valueOf(((Integer)var4.getValue(AGE)).intValue() + 1));
      CraftEventFactory.handleBlockGrowEvent(var1, var3.getX(), var3.getY(), var3.getZ(), this, this.getMetaFromState(var5));
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(var1)).withProperty(AGE, Integer.valueOf((var1 & 15) >> 2));
   }

   public int getMetaFromState(IBlockState var1) {
      byte var2 = 0;
      int var3 = var2 | ((EnumFacing)var1.getValue(FACING)).getHorizontalIndex();
      var3 = var3 | ((Integer)var1.getValue(AGE)).intValue() << 2;
      return var3;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, AGE});
   }

   static class SyntheticClass_1 {
      static final int[] a = new int[EnumFacing.values().length];

      static {
         try {
            a[EnumFacing.SOUTH.ordinal()] = 1;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            a[EnumFacing.NORTH.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[EnumFacing.WEST.ordinal()] = 3;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[EnumFacing.EAST.ordinal()] = 4;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
