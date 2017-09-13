package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCactus extends Block implements IPlantable {
   public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 15);
   protected static final AxisAlignedBB CACTUS_COLLISION_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.9375D, 0.9375D);
   protected static final AxisAlignedBB CACTUS_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 1.0D, 0.9375D);

   protected BlockCactus() {
      super(Material.CACTUS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)));
      this.setTickRandomly(true);
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      BlockPos blockpos = pos.up();
      if (worldIn.isAirBlock(blockpos)) {
         int i;
         for(i = 1; worldIn.getBlockState(pos.down(i)).getBlock() == this; ++i) {
            ;
         }

         if (i < 3) {
            int j = ((Integer)state.getValue(AGE)).intValue();
            if (ForgeHooks.onCropsGrowPre(worldIn, blockpos, state, true)) {
               if (j == 15) {
                  worldIn.setBlockState(blockpos, this.getDefaultState());
                  IBlockState iblockstate = state.withProperty(AGE, Integer.valueOf(0));
                  worldIn.setBlockState(pos, iblockstate, 4);
                  iblockstate.neighborChanged(worldIn, blockpos, this);
               } else {
                  worldIn.setBlockState(pos, state.withProperty(AGE, Integer.valueOf(j + 1)), 4);
               }

               ForgeHooks.onCropsGrowPost(worldIn, pos, state, worldIn.getBlockState(pos));
            }
         }
      }

   }

   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return CACTUS_COLLISION_AABB;
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getSelectedBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return CACTUS_AABB.offset(pos);
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return super.canPlaceBlockAt(worldIn, pos) ? this.canBlockStay(worldIn, pos) : false;
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!this.canBlockStay(worldIn, pos)) {
         worldIn.destroyBlock(pos, true);
      }

   }

   public boolean canBlockStay(World var1, BlockPos var2) {
      for(EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
         Material material = worldIn.getBlockState(pos.offset(enumfacing)).getMaterial();
         if (material.isSolid() || material == Material.LAVA) {
            return false;
         }
      }

      IBlockState state = worldIn.getBlockState(pos.down());
      return state.getBlock().canSustainPlant(state, worldIn, pos.down(), EnumFacing.UP, this) && !worldIn.getBlockState(pos.up()).getMaterial().isLiquid();
   }

   public void onEntityCollidedWithBlock(World var1, BlockPos var2, IBlockState var3, Entity var4) {
      entityIn.attackEntityFrom(DamageSource.cactus, 1.0F);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(AGE, Integer.valueOf(meta));
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)state.getValue(AGE)).intValue();
   }

   public EnumPlantType getPlantType(IBlockAccess var1, BlockPos var2) {
      return EnumPlantType.Desert;
   }

   public IBlockState getPlant(IBlockAccess var1, BlockPos var2) {
      return this.getDefaultState();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AGE});
   }
}
