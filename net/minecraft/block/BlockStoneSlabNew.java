package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public abstract class BlockStoneSlabNew extends BlockSlab {
   public static final PropertyBool SEAMLESS = PropertyBool.create("seamless");
   public static final PropertyEnum VARIANT = PropertyEnum.create("variant", BlockStoneSlabNew.EnumType.class);

   public BlockStoneSlabNew() {
      super(Material.ROCK);
      IBlockState var1 = this.blockState.getBaseState();
      if (this.isDouble()) {
         var1 = var1.withProperty(SEAMLESS, Boolean.valueOf(false));
      } else {
         var1 = var1.withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
      }

      this.setDefaultState(var1.withProperty(VARIANT, BlockStoneSlabNew.EnumType.RED_SANDSTONE));
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   public String getLocalizedName() {
      return I18n.translateToLocal(this.getUnlocalizedName() + ".red_sandstone.name");
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Item.getItemFromBlock(Blocks.STONE_SLAB2);
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Blocks.STONE_SLAB2, 1, ((BlockStoneSlabNew.EnumType)var3.getValue(VARIANT)).getMetadata());
   }

   public String getUnlocalizedName(int var1) {
      return super.getUnlocalizedName() + "." + BlockStoneSlabNew.EnumType.byMetadata(var1).getUnlocalizedName();
   }

   public IProperty getVariantProperty() {
      return VARIANT;
   }

   public Comparable getTypeForItem(ItemStack var1) {
      return BlockStoneSlabNew.EnumType.byMetadata(var1.getMetadata() & 7);
   }

   public IBlockState getStateFromMeta(int var1) {
      IBlockState var2 = this.getDefaultState().withProperty(VARIANT, BlockStoneSlabNew.EnumType.byMetadata(var1 & 7));
      if (this.isDouble()) {
         var2 = var2.withProperty(SEAMLESS, Boolean.valueOf((var1 & 8) != 0));
      } else {
         var2 = var2.withProperty(HALF, (var1 & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
      }

      return var2;
   }

   public int getMetaFromState(IBlockState var1) {
      int var2 = 0;
      var2 = var2 | ((BlockStoneSlabNew.EnumType)var1.getValue(VARIANT)).getMetadata();
      if (this.isDouble()) {
         if (((Boolean)var1.getValue(SEAMLESS)).booleanValue()) {
            var2 |= 8;
         }
      } else if (var1.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) {
         var2 |= 8;
      }

      return var2;
   }

   protected BlockStateContainer createBlockState() {
      return this.isDouble() ? new BlockStateContainer(this, new IProperty[]{SEAMLESS, VARIANT}) : new BlockStateContainer(this, new IProperty[]{HALF, VARIANT});
   }

   public MapColor getMapColor(IBlockState var1) {
      return ((BlockStoneSlabNew.EnumType)var1.getValue(VARIANT)).getMapColor();
   }

   public int damageDropped(IBlockState var1) {
      return ((BlockStoneSlabNew.EnumType)var1.getValue(VARIANT)).getMetadata();
   }

   public static enum EnumType implements IStringSerializable {
      RED_SANDSTONE(0, "red_sandstone", BlockSand.EnumType.RED_SAND.getMapColor());

      private static final BlockStoneSlabNew.EnumType[] META_LOOKUP = new BlockStoneSlabNew.EnumType[values().length];
      private final int meta;
      private final String name;
      private final MapColor mapColor;

      private EnumType(int var3, String var4, MapColor var5) {
         this.meta = var3;
         this.name = var4;
         this.mapColor = var5;
      }

      public int getMetadata() {
         return this.meta;
      }

      public MapColor getMapColor() {
         return this.mapColor;
      }

      public String toString() {
         return this.name;
      }

      public static BlockStoneSlabNew.EnumType byMetadata(int var0) {
         if (var0 < 0 || var0 >= META_LOOKUP.length) {
            var0 = 0;
         }

         return META_LOOKUP[var0];
      }

      public String getName() {
         return this.name;
      }

      public String getUnlocalizedName() {
         return this.name;
      }

      static {
         for(BlockStoneSlabNew.EnumType var3 : values()) {
            META_LOOKUP[var3.getMetadata()] = var3;
         }

      }
   }
}
