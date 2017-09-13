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
      return ((BlockSand.EnumType)var1.getValue(VARIANT)).getMetadata();
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item var1, CreativeTabs var2, List var3) {
      for(BlockSand.EnumType var7 : BlockSand.EnumType.values()) {
         var3.add(new ItemStack(var1, 1, var7.getMetadata()));
      }

   }

   public MapColor getMapColor(IBlockState var1) {
      return ((BlockSand.EnumType)var1.getValue(VARIANT)).getMapColor();
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(VARIANT, BlockSand.EnumType.byMetadata(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((BlockSand.EnumType)var1.getValue(VARIANT)).getMetadata();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{VARIANT});
   }

   @SideOnly(Side.CLIENT)
   public int getDustColor(IBlockState var1) {
      BlockSand.EnumType var2 = (BlockSand.EnumType)var1.getValue(VARIANT);
      return var2.getDustColor();
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
         this.meta = var3;
         this.name = var4;
         this.mapColor = var6;
         this.unlocalizedName = var5;
         this.dustColor = var7;
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
         if (var0 < 0 || var0 >= META_LOOKUP.length) {
            var0 = 0;
         }

         return META_LOOKUP[var0];
      }

      public String getName() {
         return this.name;
      }

      public String getUnlocalizedName() {
         return this.unlocalizedName;
      }

      static {
         for(BlockSand.EnumType var3 : values()) {
            META_LOOKUP[var3.getMetadata()] = var3;
         }

      }
   }
}
