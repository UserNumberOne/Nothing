package net.minecraft.block;

import java.util.List;
import java.util.Random;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockStainedGlass extends BlockBreakable {
   public static final PropertyEnum COLOR = PropertyEnum.create("color", EnumDyeColor.class);

   public BlockStainedGlass(Material var1) {
      super(materialIn, false);
      this.setDefaultState(this.blockState.getBaseState().withProperty(COLOR, EnumDyeColor.WHITE));
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   public int damageDropped(IBlockState var1) {
      return ((EnumDyeColor)state.getValue(COLOR)).getMetadata();
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item var1, CreativeTabs var2, List var3) {
      for(EnumDyeColor enumdyecolor : EnumDyeColor.values()) {
         list.add(new ItemStack(itemIn, 1, enumdyecolor.getMetadata()));
      }

   }

   public MapColor getMapColor(IBlockState var1) {
      return ((EnumDyeColor)state.getValue(COLOR)).getMapColor();
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.TRANSLUCENT;
   }

   public int quantityDropped(Random var1) {
      return 0;
   }

   protected boolean canSilkHarvest() {
      return true;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(COLOR, EnumDyeColor.byMetadata(meta));
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      if (!worldIn.isRemote) {
         BlockBeacon.updateColorAsync(worldIn, pos);
      }

   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      if (!worldIn.isRemote) {
         BlockBeacon.updateColorAsync(worldIn, pos);
      }

   }

   public int getMetaFromState(IBlockState var1) {
      return ((EnumDyeColor)state.getValue(COLOR)).getMetadata();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{COLOR});
   }
}
