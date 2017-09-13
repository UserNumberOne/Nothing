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

public class BlockPlanks extends Block {
   public static final PropertyEnum VARIANT = PropertyEnum.create("variant", BlockPlanks.EnumType.class);

   public BlockPlanks() {
      super(Material.WOOD);
      this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockPlanks.EnumType.OAK));
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   public int damageDropped(IBlockState var1) {
      return ((BlockPlanks.EnumType)state.getValue(VARIANT)).getMetadata();
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item var1, CreativeTabs var2, List var3) {
      for(BlockPlanks.EnumType blockplanks$enumtype : BlockPlanks.EnumType.values()) {
         list.add(new ItemStack(itemIn, 1, blockplanks$enumtype.getMetadata()));
      }

   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(VARIANT, BlockPlanks.EnumType.byMetadata(meta));
   }

   public MapColor getMapColor(IBlockState var1) {
      return ((BlockPlanks.EnumType)state.getValue(VARIANT)).getMapColor();
   }

   public int getMetaFromState(IBlockState var1) {
      return ((BlockPlanks.EnumType)state.getValue(VARIANT)).getMetadata();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{VARIANT});
   }

   public static enum EnumType implements IStringSerializable {
      OAK(0, "oak", MapColor.WOOD),
      SPRUCE(1, "spruce", MapColor.OBSIDIAN),
      BIRCH(2, "birch", MapColor.SAND),
      JUNGLE(3, "jungle", MapColor.DIRT),
      ACACIA(4, "acacia", MapColor.ADOBE),
      DARK_OAK(5, "dark_oak", "big_oak", MapColor.BROWN);

      private static final BlockPlanks.EnumType[] META_LOOKUP = new BlockPlanks.EnumType[values().length];
      private final int meta;
      private final String name;
      private final String unlocalizedName;
      private final MapColor mapColor;

      private EnumType(int var3, String var4, MapColor var5) {
         this(metaIn, nameIn, nameIn, mapColorIn);
      }

      private EnumType(int var3, String var4, String var5, MapColor var6) {
         this.meta = metaIn;
         this.name = nameIn;
         this.unlocalizedName = unlocalizedNameIn;
         this.mapColor = mapColorIn;
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

      public static BlockPlanks.EnumType byMetadata(int var0) {
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
         for(BlockPlanks.EnumType blockplanks$enumtype : values()) {
            META_LOOKUP[blockplanks$enumtype.getMetadata()] = blockplanks$enumtype;
         }

      }
   }
}
