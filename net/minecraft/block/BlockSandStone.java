package net.minecraft.block;

import java.util.List;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSandStone extends Block {
   public static final PropertyEnum TYPE = PropertyEnum.create("type", BlockSandStone.EnumType.class);

   public BlockSandStone() {
      super(Material.ROCK);
      this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, BlockSandStone.EnumType.DEFAULT));
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   public int damageDropped(IBlockState var1) {
      return ((BlockSandStone.EnumType)state.getValue(TYPE)).getMetadata();
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item var1, CreativeTabs var2, List var3) {
      for(BlockSandStone.EnumType blocksandstone$enumtype : BlockSandStone.EnumType.values()) {
         list.add(new ItemStack(itemIn, 1, blocksandstone$enumtype.getMetadata()));
      }

   }

   public MapColor getMapColor(IBlockState var1) {
      return MapColor.SAND;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(TYPE, BlockSandStone.EnumType.byMetadata(meta));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((BlockSandStone.EnumType)state.getValue(TYPE)).getMetadata();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{TYPE});
   }

   public static enum EnumType implements IStringSerializable {
      DEFAULT(0, "sandstone", "default"),
      CHISELED(1, "chiseled_sandstone", "chiseled"),
      SMOOTH(2, "smooth_sandstone", "smooth");

      private static final BlockSandStone.EnumType[] META_LOOKUP = new BlockSandStone.EnumType[values().length];
      private final int metadata;
      private final String name;
      private final String unlocalizedName;

      private EnumType(int var3, String var4, String var5) {
         this.metadata = meta;
         this.name = name;
         this.unlocalizedName = unlocalizedName;
      }

      public int getMetadata() {
         return this.metadata;
      }

      public String toString() {
         return this.name;
      }

      public static BlockSandStone.EnumType byMetadata(int var0) {
         if (meta < 0 || meta >= META_LOOKUP.length) {
            meta = 0;
         }

         return META_LOOKUP[meta];
      }

      public String getName() {
         return this.name;
      }

      public String getUnlocalizedName() {
         return this.unlocalizedName;
      }

      static {
         for(BlockSandStone.EnumType blocksandstone$enumtype : values()) {
            META_LOOKUP[blocksandstone$enumtype.getMetadata()] = blocksandstone$enumtype;
         }

      }
   }
}
