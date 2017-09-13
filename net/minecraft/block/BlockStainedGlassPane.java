package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockStainedGlassPane extends BlockPane {
   public static final PropertyEnum COLOR = PropertyEnum.create("color", EnumDyeColor.class);

   public BlockStainedGlassPane() {
      super(Material.GLASS, false);
      this.setDefaultState(this.blockState.getBaseState().withProperty(NORTH, Boolean.valueOf(false)).withProperty(EAST, Boolean.valueOf(false)).withProperty(SOUTH, Boolean.valueOf(false)).withProperty(WEST, Boolean.valueOf(false)).withProperty(COLOR, EnumDyeColor.WHITE));
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public int damageDropped(IBlockState var1) {
      return ((EnumDyeColor)var1.getValue(COLOR)).getMetadata();
   }

   public MapColor getMapColor(IBlockState var1) {
      return ((EnumDyeColor)var1.getValue(COLOR)).getMapColor();
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(COLOR, EnumDyeColor.byMetadata(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((EnumDyeColor)var1.getValue(COLOR)).getMetadata();
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      switch(var2) {
      case CLOCKWISE_180:
         return var1.withProperty(NORTH, var1.getValue(SOUTH)).withProperty(EAST, var1.getValue(WEST)).withProperty(SOUTH, var1.getValue(NORTH)).withProperty(WEST, var1.getValue(EAST));
      case COUNTERCLOCKWISE_90:
         return var1.withProperty(NORTH, var1.getValue(EAST)).withProperty(EAST, var1.getValue(SOUTH)).withProperty(SOUTH, var1.getValue(WEST)).withProperty(WEST, var1.getValue(NORTH));
      case CLOCKWISE_90:
         return var1.withProperty(NORTH, var1.getValue(WEST)).withProperty(EAST, var1.getValue(NORTH)).withProperty(SOUTH, var1.getValue(EAST)).withProperty(WEST, var1.getValue(SOUTH));
      default:
         return var1;
      }
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      switch(var2) {
      case LEFT_RIGHT:
         return var1.withProperty(NORTH, var1.getValue(SOUTH)).withProperty(SOUTH, var1.getValue(NORTH));
      case FRONT_BACK:
         return var1.withProperty(EAST, var1.getValue(WEST)).withProperty(WEST, var1.getValue(EAST));
      default:
         return super.withMirror(var1, var2);
      }
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{NORTH, EAST, WEST, SOUTH, COLOR});
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      if (!var1.isRemote) {
         BlockBeacon.updateColorAsync(var1, var2);
      }

   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      if (!var1.isRemote) {
         BlockBeacon.updateColorAsync(var1, var2);
      }

   }
}
