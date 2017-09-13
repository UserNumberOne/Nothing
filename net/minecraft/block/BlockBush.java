package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockBush extends Block implements IPlantable {
   protected static final AxisAlignedBB BUSH_AABB = new AxisAlignedBB(0.30000001192092896D, 0.0D, 0.30000001192092896D, 0.699999988079071D, 0.6000000238418579D, 0.699999988079071D);

   protected BlockBush() {
      this(Material.PLANTS);
   }

   protected BlockBush(Material var1) {
      this(materialIn, materialIn.getMaterialMapColor());
   }

   protected BlockBush(Material var1, MapColor var2) {
      super(materialIn, mapColorIn);
      this.setTickRandomly(true);
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      IBlockState soil = worldIn.getBlockState(pos.down());
      return super.canPlaceBlockAt(worldIn, pos) && soil.getBlock().canSustainPlant(soil, worldIn, pos.down(), EnumFacing.UP, this);
   }

   protected boolean canSustainBush(IBlockState var1) {
      return state.getBlock() == Blocks.GRASS || state.getBlock() == Blocks.DIRT || state.getBlock() == Blocks.FARMLAND;
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      super.neighborChanged(state, worldIn, pos, blockIn);
      this.checkAndDropBlock(worldIn, pos, state);
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      this.checkAndDropBlock(worldIn, pos, state);
   }

   protected void checkAndDropBlock(World var1, BlockPos var2, IBlockState var3) {
      if (!this.canBlockStay(worldIn, pos, state)) {
         this.dropBlockAsItem(worldIn, pos, state, 0);
         worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
      }

   }

   public boolean canBlockStay(World var1, BlockPos var2, IBlockState var3) {
      if (state.getBlock() == this) {
         IBlockState soil = worldIn.getBlockState(pos.down());
         return soil.getBlock().canSustainPlant(soil, worldIn, pos.down(), EnumFacing.UP, this);
      } else {
         return this.canSustainBush(worldIn.getBlockState(pos.down()));
      }
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return BUSH_AABB;
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

   public EnumPlantType getPlantType(IBlockAccess var1, BlockPos var2) {
      if (this == Blocks.WHEAT) {
         return EnumPlantType.Crop;
      } else if (this == Blocks.CARROTS) {
         return EnumPlantType.Crop;
      } else if (this == Blocks.POTATOES) {
         return EnumPlantType.Crop;
      } else if (this == Blocks.MELON_STEM) {
         return EnumPlantType.Crop;
      } else if (this == Blocks.PUMPKIN_STEM) {
         return EnumPlantType.Crop;
      } else if (this == Blocks.DEADBUSH) {
         return EnumPlantType.Desert;
      } else if (this == Blocks.WATERLILY) {
         return EnumPlantType.Water;
      } else if (this == Blocks.RED_MUSHROOM) {
         return EnumPlantType.Cave;
      } else if (this == Blocks.BROWN_MUSHROOM) {
         return EnumPlantType.Cave;
      } else if (this == Blocks.NETHER_WART) {
         return EnumPlantType.Nether;
      } else if (this == Blocks.SAPLING) {
         return EnumPlantType.Plains;
      } else if (this == Blocks.TALLGRASS) {
         return EnumPlantType.Plains;
      } else if (this == Blocks.DOUBLE_PLANT) {
         return EnumPlantType.Plains;
      } else if (this == Blocks.RED_FLOWER) {
         return EnumPlantType.Plains;
      } else {
         return this == Blocks.YELLOW_FLOWER ? EnumPlantType.Plains : EnumPlantType.Plains;
      }
   }

   public IBlockState getPlant(IBlockAccess var1, BlockPos var2) {
      IBlockState state = world.getBlockState(pos);
      return state.getBlock() != this ? this.getDefaultState() : state;
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }
}
