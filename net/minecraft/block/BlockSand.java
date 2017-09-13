package net.minecraft.block;

import java.util.List;
import net.minecraft.block.material.MapColor;
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

public class BlockSand extends BlockFalling {
   public static final PropertyEnum VARIANT = PropertyEnum.create("variant", BlockSand.EnumType.class);

   public BlockSand() {
      this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockSand.EnumType.SAND));
   }

   public int damageDropped(IBlockState var1) {
      return ((BlockSand.EnumType)state.getValue(VARIANT)).getMetadata();
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item var1, CreativeTabs var2, List var3) {
      for(BlockSand.EnumType blocksand$enumtype : BlockSand.EnumType.values()) {
         list.add(new ItemStack(itemIn, 1, blocksand$enumtype.getMetadata()));
      }

   }

   public MapColor getMapColor(IBlockState var1) {
      return ((BlockSand.EnumType)state.getValue(VARIANT)).getMapColor();
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(VARIANT, BlockSand.EnumType.byMetadata(meta));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((BlockSand.EnumType)state.getValue(VARIANT)).getMetadata();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{VARIANT});
   }

   @SideOnly(Side.CLIENT)
   public int getDustColor(IBlockState var1) {
      BlockSand.EnumType blocksand$enumtype = (BlockSand.EnumType)p_189876_1_.getValue(VARIANT);
      return blocksand$enumtype.getDustColor();
   }

   public static enum EnumType implements IStringSerializable {
      SAND(0, "sand", "default", MapColor.SAND, -2370656),
      RED_SAND(1, "red_sand", "red", MapColor.ADOBE, -5679071);

      private static final BlockSand.EnumType[] META_LOOKUP = new BlockSand.EnumType[values().length];
      private final int meta;
      private final String name;
      private final MapColor mapColor;
      private final String unlocalizedName;
      private final int dustColor;

      private EnumType(int var3, String var4, String var5, MapColor var6, int var7) {
         this.meta = p_i47157_3_;
         this.name = p_i47157_4_;
         this.mapColor = p_i47157_6_;
         this.unlocalizedName = p_i47157_5_;
         this.dustColor = p_i47157_7_;
      }

      @SideOnly(Side.CLIENT)
      public int getDustColor() {
         return this.dustColor;
      }

      public int getMetadata() {
         return this.meta;
      }

      public String toString() {
         return this.name;
      }

      public MapColor getMapColor() {
         return this.mapColor;
      }

      public static BlockSand.EnumType byMetadata(int var0) {
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
         for(BlockSand.EnumType blocksand$enumtype : values()) {
            META_LOOKUP[blocksand$enumtype.getMetadata()] = blocksand$enumtype;
         }

      }
   }
}
