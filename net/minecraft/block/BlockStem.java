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

   protected BlockStem(Block block) {
      this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)).withProperty(FACING, EnumFacing.UP));
      this.crop = block;
      this.setTickRandomly(true);
      this.setCreativeTab((CreativeTabs)null);
   }

   public AxisAlignedBB getBoundingBox(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      return STEM_AABB[((Integer)iblockdata.getValue(AGE)).intValue()];
   }

   public IBlockState getActualState(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      int i = ((Integer)iblockdata.getValue(AGE)).intValue();
      iblockdata = iblockdata.withProperty(FACING, EnumFacing.UP);

      for(EnumFacing enumdirection : EnumFacing.Plane.HORIZONTAL) {
         if (iblockaccess.getBlockState(blockposition.offset(enumdirection)).getBlock() == this.crop && i == 7) {
            iblockdata = iblockdata.withProperty(FACING, enumdirection);
            break;
         }
      }

      return iblockdata;
   }

   protected boolean canSustainBush(IBlockState iblockdata) {
      return iblockdata.getBlock() == Blocks.FARMLAND;
   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      super.updateTick(world, blockposition, iblockdata, random);
      if (world.getLightFromNeighbors(blockposition.up()) >= 9) {
         float f = BlockCrops.getGrowthChance(this, world, blockposition);
         if (random.nextInt((int)(25.0F / f) + 1) == 0) {
            int i = ((Integer)iblockdata.getValue(AGE)).intValue();
            if (i < 7) {
               iblockdata = iblockdata.withProperty(AGE, Integer.valueOf(i + 1));
               CraftEventFactory.handleBlockGrowEvent(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this, this.getMetaFromState(iblockdata));
            } else {
               for(EnumFacing enumdirection : EnumFacing.Plane.HORIZONTAL) {
                  if (world.getBlockState(blockposition.offset(enumdirection)).getBlock() == this.crop) {
                     return;
                  }
               }

               blockposition = blockposition.offset(EnumFacing.Plane.HORIZONTAL.random(random));
               Block block = world.getBlockState(blockposition.down()).getBlock();
               if (world.getBlockState(blockposition).getBlock().blockMaterial == Material.AIR && (block == Blocks.FARMLAND || block == Blocks.DIRT || block == Blocks.GRASS)) {
                  CraftEventFactory.handleBlockGrowEvent(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this.crop, 0);
               }
            }
         }
      }

   }

   public void growStem(World world, BlockPos blockposition, IBlockState iblockdata) {
      int i = ((Integer)iblockdata.getValue(AGE)).intValue() + MathHelper.getInt(world.rand, 2, 5);
      CraftEventFactory.handleBlockGrowEvent(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this, Math.min(7, i));
   }

   public void dropBlockAsItemWithChance(World world, BlockPos blockposition, IBlockState iblockdata, float f, int i) {
      super.dropBlockAsItemWithChance(world, blockposition, iblockdata, f, i);
      if (!world.isRemote) {
         Item item = this.getSeedItem();
         if (item != null) {
            int j = ((Integer)iblockdata.getValue(AGE)).intValue();

            for(int k = 0; k < 3; ++k) {
               if (world.rand.nextInt(15) <= j) {
                  spawnAsEntity(world, blockposition, new ItemStack(item));
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
   public Item getItemDropped(IBlockState iblockdata, Random random, int i) {
      return null;
   }

   @Nullable
   public ItemStack getItem(World world, BlockPos blockposition, IBlockState iblockdata) {
      Item item = this.getSeedItem();
      return item == null ? null : new ItemStack(item);
   }

   public boolean canGrow(World world, BlockPos blockposition, IBlockState iblockdata, boolean flag) {
      return ((Integer)iblockdata.getValue(AGE)).intValue() != 7;
   }

   public boolean canUseBonemeal(World world, Random random, BlockPos blockposition, IBlockState iblockdata) {
      return true;
   }

   public void grow(World world, Random random, BlockPos blockposition, IBlockState iblockdata) {
      this.growStem(world, blockposition, iblockdata);
   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(AGE, Integer.valueOf(i));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      return ((Integer)iblockdata.getValue(AGE)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AGE, FACING});
   }
}
