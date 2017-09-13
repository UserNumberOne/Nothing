package net.minecraft.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockWoodSlab extends BlockSlab {
   public static final PropertyEnum VARIANT = PropertyEnum.create("variant", BlockPlanks.EnumType.class);

   public BlockWoodSlab() {
      super(Material.WOOD);
      IBlockState var1 = this.blockState.getBaseState();
      if (!this.isDouble()) {
         var1 = var1.withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
      }

      this.setDefaultState(var1.withProperty(VARIANT, BlockPlanks.EnumType.OAK));
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   public MapColor getMapColor(IBlockState var1) {
      return ((BlockPlanks.EnumType)var1.getValue(VARIANT)).getMapColor();
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Item.getItemFromBlock(Blocks.WOODEN_SLAB);
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Blocks.WOODEN_SLAB, 1, ((BlockPlanks.EnumType)var3.getValue(VARIANT)).getMetadata());
   }

   public String getUnlocalizedName(int var1) {
      return super.getUnlocalizedName() + "." + BlockPlanks.EnumType.byMetadata(var1).getUnlocalizedName();
   }

   public IProperty getVariantProperty() {
      return VARIANT;
   }

   public Comparable getTypeForItem(ItemStack var1) {
      return BlockPlanks.EnumType.byMetadata(var1.getMetadata() & 7);
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item var1, CreativeTabs var2, List var3) {
      if (var1 != Item.getItemFromBlock(Blocks.DOUBLE_WOODEN_SLAB)) {
         for(BlockPlanks.EnumType var7 : BlockPlanks.EnumType.values()) {
            var3.add(new ItemStack(var1, 1, var7.getMetadata()));
         }
      }

   }

   public IBlockState getStateFromMeta(int var1) {
      IBlockState var2 = this.getDefaultState().withProperty(VARIANT, BlockPlanks.EnumType.byMetadata(var1 & 7));
      if (!this.isDouble()) {
         var2 = var2.withProperty(HALF, (var1 & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
      }

      return var2;
   }

   public int getMetaFromState(IBlockState var1) {
      int var2 = 0;
      var2 = var2 | ((BlockPlanks.EnumType)var1.getValue(VARIANT)).getMetadata();
      if (!this.isDouble() && var1.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) {
         var2 |= 8;
      }

      return var2;
   }

   protected BlockStateContainer createBlockState() {
      return this.isDouble() ? new BlockStateContainer(this, new IProperty[]{VARIANT}) : new BlockStateContainer(this, new IProperty[]{HALF, VARIANT});
   }

   public int damageDropped(IBlockState var1) {
      return ((BlockPlanks.EnumType)var1.getValue(VARIANT)).getMetadata();
   }
}
