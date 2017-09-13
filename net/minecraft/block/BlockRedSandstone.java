package net.minecraft.block;

import java.util.List;
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

public class BlockRedSandstone extends Block {
   public static final PropertyEnum TYPE = PropertyEnum.create("type", BlockRedSandstone.EnumType.class);

   public BlockRedSandstone() {
      super(Material.ROCK, BlockSand.EnumType.RED_SAND.getMapColor());
      this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, BlockRedSandstone.EnumType.DEFAULT));
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   public int damageDropped(IBlockState var1) {
      return ((BlockRedSandstone.EnumType)var1.getValue(TYPE)).getMetadata();
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item var1, CreativeTabs var2, List var3) {
      for(BlockRedSandstone.EnumType var7 : BlockRedSandstone.EnumType.values()) {
         var3.add(new ItemStack(var1, 1, var7.getMetadata()));
      }

   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(TYPE, BlockRedSandstone.EnumType.byMetadata(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((BlockRedSandstone.EnumType)var1.getValue(TYPE)).getMetadata();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{TYPE});
   }

   public static enum EnumType implements IStringSerializable {
      DEFAULT(0, "red_sandstone", "default"),
      CHISELED(1, "chiseled_red_sandstone", "chiseled"),
      SMOOTH(2, "smooth_red_sandstone", "smooth");

      private static final BlockRedSandstone.EnumType[] META_LOOKUP = new BlockRedSandstone.EnumType[values().length];
      private final int meta;
      private final String name;
      private final String unlocalizedName;

      private EnumType(int var3, String var4, String var5) {
         this.meta = var3;
         this.name = var4;
         this.unlocalizedName = var5;
      }

      public int getMetadata() {
         return this.meta;
      }

      public String toString() {
         return this.name;
      }

      public static BlockRedSandstone.EnumType byMetadata(int var0) {
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
         for(BlockRedSandstone.EnumType var3 : values()) {
            META_LOOKUP[var3.getMetadata()] = var3;
         }

      }
   }
}
