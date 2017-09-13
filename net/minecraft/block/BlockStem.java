package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class BlockStem extends BlockBush implements IGrowable {
   public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 7);
   public static final PropertyDirection FACING = BlockTorch.FACING;
   private final Block crop;
   protected static final AxisAlignedBB[] STEM_AABB = new AxisAlignedBB[]{new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.125D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.25D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.375D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.5D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.625D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.75D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.875D, 0.625D), new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D)};

   protected BlockStem(Block var1) {
      this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)).withProperty(FACING, EnumFacing.UP));
      this.crop = var1;
      this.setTickRandomly(true);
      this.setCreativeTab((CreativeTabs)null);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return STEM_AABB[((Integer)var1.getValue(AGE)).intValue()];
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      int var4 = ((Integer)var1.getValue(AGE)).intValue();
      var1 = var1.withProperty(FACING, EnumFacing.UP);

      for(EnumFacing var6 : EnumFacing.Plane.HORIZONTAL) {
         if (var2.getBlockState(var3.offset(var6)).getBlock() == this.crop && var4 == 7) {
            var1 = var1.withProperty(FACING, var6);
            break;
         }
      }

      return var1;
   }

   protected boolean canSustainBush(IBlockState var1) {
      return var1.getBlock() == Blocks.FARMLAND;
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      super.updateTick(var1, var2, var3, var4);
      if (var1.getLightFromNeighbors(var2.up()) >= 9) {
         float var5 = BlockCrops.getGrowthChance(this, var1, var2);
         if (var4.nextInt((int)(25.0F / var5) + 1) == 0) {
            int var6 = ((Integer)var3.getValue(AGE)).intValue();
            if (var6 < 7) {
               var3 = var3.withProperty(AGE, Integer.valueOf(var6 + 1));
               CraftEventFactory.handleBlockGrowEvent(var1, var2.getX(), var2.getY(), var2.getZ(), this, this.getMetaFromState(var3));
            } else {
               for(EnumFacing var8 : EnumFacing.Plane.HORIZONTAL) {
                  if (var1.getBlockState(var2.offset(var8)).getBlock() == this.crop) {
                     return;
                  }
               }

               var2 = var2.offset(EnumFacing.Plane.HORIZONTAL.random(var4));
               Block var11 = var1.getBlockState(var2.down()).getBlock();
               if (var1.getBlockState(var2).getBlock().blockMaterial == Material.AIR && (var11 == Blocks.FARMLAND || var11 == Blocks.DIRT || var11 == Blocks.GRASS)) {
                  CraftEventFactory.handleBlockGrowEvent(var1, var2.getX(), var2.getY(), var2.getZ(), this.crop, 0);
               }
            }
         }
      }

   }

   public void growStem(World var1, BlockPos var2, IBlockState var3) {
      int var4 = ((Integer)var3.getValue(AGE)).intValue() + MathHelper.getInt(var1.rand, 2, 5);
      CraftEventFactory.handleBlockGrowEvent(var1, var2.getX(), var2.getY(), var2.getZ(), this, Math.min(7, var4));
   }

   public void dropBlockAsItemWithChance(World var1, BlockPos var2, IBlockState var3, float var4, int var5) {
      super.dropBlockAsItemWithChance(var1, var2, var3, var4, var5);
      if (!var1.isRemote) {
         Item var6 = this.getSeedItem();
         if (var6 != null) {
            int var7 = ((Integer)var3.getValue(AGE)).intValue();

            for(int var8 = 0; var8 < 3; ++var8) {
               if (var1.rand.nextInt(15) <= var7) {
                  spawnAsEntity(var1, var2, new ItemStack(var6));
               }
            }
         }
      }

   }

   @Nullable
   protected Item getSeedItem() {
      return this.crop == Blocks.PUMPKIN ? Items.PUMPKIN_SEEDS : (this.crop == Blocks.MELON_BLOCK ? Items.MELON_SEEDS : null);
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return null;
   }

   @Nullable
   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      Item var4 = this.getSeedItem();
      return var4 == null ? null : new ItemStack(var4);
   }

   public boolean canGrow(World var1, BlockPos var2, IBlockState var3, boolean var4) {
      return ((Integer)var3.getValue(AGE)).intValue() != 7;
   }

   public boolean canUseBonemeal(World var1, Random var2, BlockPos var3, IBlockState var4) {
      return true;
   }

   public void grow(World var1, Random var2, BlockPos var3, IBlockState var4) {
      this.growStem(var1, var3, var4);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(AGE, Integer.valueOf(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)var1.getValue(AGE)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AGE, FACING});
   }
}
