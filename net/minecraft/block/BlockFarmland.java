package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockFarmland extends Block {
   public static final PropertyInteger MOISTURE = PropertyInteger.create("moisture", 0, 7);
   protected static final AxisAlignedBB FARMLAND_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.9375D, 1.0D);

   protected BlockFarmland() {
      super(Material.GROUND);
      this.setDefaultState(this.blockState.getBaseState().withProperty(MOISTURE, Integer.valueOf(0)));
      this.setTickRandomly(true);
      this.setLightOpacity(255);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return FARMLAND_AABB;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      int i = ((Integer)state.getValue(MOISTURE)).intValue();
      if (!this.hasWater(worldIn, pos) && !worldIn.isRainingAt(pos.up())) {
         if (i > 0) {
            worldIn.setBlockState(pos, state.withProperty(MOISTURE, Integer.valueOf(i - 1)), 2);
         } else if (!this.hasCrops(worldIn, pos)) {
            worldIn.setBlockState(pos, Blocks.DIRT.getDefaultState());
         }
      } else if (i < 7) {
         worldIn.setBlockState(pos, state.withProperty(MOISTURE, Integer.valueOf(7)), 2);
      }

   }

   public void onFallenUpon(World var1, BlockPos var2, Entity var3, float var4) {
      if (!worldIn.isRemote && worldIn.rand.nextFloat() < fallDistance - 0.5F && entityIn instanceof EntityLivingBase && (entityIn instanceof EntityPlayer || worldIn.getGameRules().getBoolean("mobGriefing")) && entityIn.width * entityIn.width * entityIn.height > 0.512F) {
         worldIn.setBlockState(pos, Blocks.DIRT.getDefaultState());
      }

      super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
   }

   private boolean hasCrops(World var1, BlockPos var2) {
      Block block = worldIn.getBlockState(pos.up()).getBlock();
      return block instanceof IPlantable && this.canSustainPlant(worldIn.getBlockState(pos), worldIn, pos, EnumFacing.UP, (IPlantable)block);
   }

   private boolean hasWater(World var1, BlockPos var2) {
      for(BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(pos.add(-4, 0, -4), pos.add(4, 1, 4))) {
         if (worldIn.getBlockState(blockpos$mutableblockpos).getMaterial() == Material.WATER) {
            return true;
         }
      }

      return false;
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      super.neighborChanged(state, worldIn, pos, blockIn);
      if (worldIn.getBlockState(pos.up()).getMaterial().isSolid()) {
         worldIn.setBlockState(pos, Blocks.DIRT.getDefaultState());
      }

   }

   @SideOnly(Side.CLIENT)
   public boolean shouldSideBeRendered(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      switch(side) {
      case UP:
         return true;
      case NORTH:
      case SOUTH:
      case WEST:
      case EAST:
         IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
         Block block = iblockstate.getBlock();
         return !iblockstate.isOpaqueCube() && block != Blocks.FARMLAND && block != Blocks.GRASS_PATH;
      default:
         return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
      }
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Blocks.DIRT.getItemDropped(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT), rand, fortune);
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Blocks.DIRT);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(MOISTURE, Integer.valueOf(meta & 7));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)state.getValue(MOISTURE)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{MOISTURE});
   }
}
