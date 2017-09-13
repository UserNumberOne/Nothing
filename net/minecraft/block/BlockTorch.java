package net.minecraft.block;

import com.google.common.base.Predicate;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTorch extends Block {
   public static final PropertyDirection FACING = PropertyDirection.create("facing", new Predicate() {
      public boolean apply(@Nullable EnumFacing var1) {
         return var1 != EnumFacing.DOWN;
      }
   });
   protected static final AxisAlignedBB STANDING_AABB = new AxisAlignedBB(0.4000000059604645D, 0.0D, 0.4000000059604645D, 0.6000000238418579D, 0.6000000238418579D, 0.6000000238418579D);
   protected static final AxisAlignedBB TORCH_NORTH_AABB = new AxisAlignedBB(0.3499999940395355D, 0.20000000298023224D, 0.699999988079071D, 0.6499999761581421D, 0.800000011920929D, 1.0D);
   protected static final AxisAlignedBB TORCH_SOUTH_AABB = new AxisAlignedBB(0.3499999940395355D, 0.20000000298023224D, 0.0D, 0.6499999761581421D, 0.800000011920929D, 0.30000001192092896D);
   protected static final AxisAlignedBB TORCH_WEST_AABB = new AxisAlignedBB(0.699999988079071D, 0.20000000298023224D, 0.3499999940395355D, 1.0D, 0.800000011920929D, 0.6499999761581421D);
   protected static final AxisAlignedBB TORCH_EAST_AABB = new AxisAlignedBB(0.0D, 0.20000000298023224D, 0.3499999940395355D, 0.30000001192092896D, 0.800000011920929D, 0.6499999761581421D);

   protected BlockTorch() {
      super(Material.CIRCUITS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.UP));
      this.setTickRandomly(true);
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      switch((EnumFacing)var1.getValue(FACING)) {
      case EAST:
         return TORCH_EAST_AABB;
      case WEST:
         return TORCH_WEST_AABB;
      case SOUTH:
         return TORCH_SOUTH_AABB;
      case NORTH:
         return TORCH_NORTH_AABB;
      default:
         return STANDING_AABB;
      }
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return NULL_AABB;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   private boolean canPlaceOn(World var1, BlockPos var2) {
      IBlockState var3 = var1.getBlockState(var2);
      return var3.isSideSolid(var1, var2, EnumFacing.UP) ? true : var3.getBlock().canPlaceTorchOnTop(var3, var1, var2);
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      for(EnumFacing var4 : FACING.getAllowedValues()) {
         if (this.canPlaceAt(var1, var2, var4)) {
            return true;
         }
      }

      return false;
   }

   private boolean canPlaceAt(World var1, BlockPos var2, EnumFacing var3) {
      BlockPos var4 = var2.offset(var3.getOpposite());
      boolean var5 = var3.getAxis().isHorizontal();
      return var5 && var1.isSideSolid(var4, var3, true) || var3.equals(EnumFacing.UP) && this.canPlaceOn(var1, var4);
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      if (this.canPlaceAt(var1, var2, var3)) {
         return this.getDefaultState().withProperty(FACING, var3);
      } else {
         for(EnumFacing var10 : EnumFacing.Plane.HORIZONTAL) {
            if (var1.isSideSolid(var2.offset(var10.getOpposite()), var10, true)) {
               return this.getDefaultState().withProperty(FACING, var10);
            }
         }

         return this.getDefaultState();
      }
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      this.checkForDrop(var1, var2, var3);
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      this.onNeighborChangeInternal(var2, var3, var1);
   }

   protected boolean onNeighborChangeInternal(World var1, BlockPos var2, IBlockState var3) {
      if (!this.checkForDrop(var1, var2, var3)) {
         return true;
      } else {
         EnumFacing var4 = (EnumFacing)var3.getValue(FACING);
         EnumFacing.Axis var5 = var4.getAxis();
         EnumFacing var6 = var4.getOpposite();
         boolean var7 = false;
         if (var5.isHorizontal() && !var1.isSideSolid(var2.offset(var6), var4, true)) {
            var7 = true;
         } else if (var5.isVertical() && !this.canPlaceOn(var1, var2.offset(var6))) {
            var7 = true;
         }

         if (var7) {
            this.dropBlockAsItem(var1, var2, var3, 0);
            var1.setBlockToAir(var2);
            return true;
         } else {
            return false;
         }
      }
   }

   protected boolean checkForDrop(World var1, BlockPos var2, IBlockState var3) {
      if (var3.getBlock() == this && this.canPlaceAt(var1, var2, (EnumFacing)var3.getValue(FACING))) {
         return true;
      } else {
         if (var1.getBlockState(var2).getBlock() == this) {
            this.dropBlockAsItem(var1, var2, var3, 0);
            var1.setBlockToAir(var2);
         }

         return false;
      }
   }

   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState var1, World var2, BlockPos var3, Random var4) {
      EnumFacing var5 = (EnumFacing)var1.getValue(FACING);
      double var6 = (double)var3.getX() + 0.5D;
      double var8 = (double)var3.getY() + 0.7D;
      double var10 = (double)var3.getZ() + 0.5D;
      double var12 = 0.22D;
      double var14 = 0.27D;
      if (var5.getAxis().isHorizontal()) {
         EnumFacing var16 = var5.getOpposite();
         var2.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, var6 + 0.27D * (double)var16.getFrontOffsetX(), var8 + 0.22D, var10 + 0.27D * (double)var16.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
         var2.spawnParticle(EnumParticleTypes.FLAME, var6 + 0.27D * (double)var16.getFrontOffsetX(), var8 + 0.22D, var10 + 0.27D * (double)var16.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
      } else {
         var2.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, var6, var8, var10, 0.0D, 0.0D, 0.0D);
         var2.spawnParticle(EnumParticleTypes.FLAME, var6, var8, var10, 0.0D, 0.0D, 0.0D);
      }

   }

   public IBlockState getStateFromMeta(int var1) {
      IBlockState var2 = this.getDefaultState();
      switch(var1) {
      case 1:
         var2 = var2.withProperty(FACING, EnumFacing.EAST);
         break;
      case 2:
         var2 = var2.withProperty(FACING, EnumFacing.WEST);
         break;
      case 3:
         var2 = var2.withProperty(FACING, EnumFacing.SOUTH);
         break;
      case 4:
         var2 = var2.withProperty(FACING, EnumFacing.NORTH);
         break;
      case 5:
      default:
         var2 = var2.withProperty(FACING, EnumFacing.UP);
      }

      return var2;
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public int getMetaFromState(IBlockState var1) {
      int var2 = 0;
      switch((EnumFacing)var1.getValue(FACING)) {
      case EAST:
         var2 = var2 | 1;
         break;
      case WEST:
         var2 = var2 | 2;
         break;
      case SOUTH:
         var2 = var2 | 3;
         break;
      case NORTH:
         var2 = var2 | 4;
         break;
      case DOWN:
      case UP:
      default:
         var2 = var2 | 5;
      }

      return var2;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return var1.withProperty(FACING, var2.rotate((EnumFacing)var1.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return var1.withRotation(var2.toRotation((EnumFacing)var1.getValue(FACING)));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING});
   }
}
