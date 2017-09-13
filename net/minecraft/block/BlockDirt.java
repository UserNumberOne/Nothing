package net.minecraft.block;

import java.util.List;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDirt extends Block {
   public static final PropertyEnum VARIANT = PropertyEnum.create("variant", BlockDirt.DirtType.class);
   public static final PropertyBool SNOWY = PropertyBool.create("snowy");

   protected BlockDirt() {
      super(Material.GROUND);
      this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockDirt.DirtType.DIRT).withProperty(SNOWY, Boolean.valueOf(false)));
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   public MapColor getMapColor(IBlockState var1) {
      return ((BlockDirt.DirtType)var1.getValue(VARIANT)).getColor();
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      if (var1.getValue(VARIANT) == BlockDirt.DirtType.PODZOL) {
         Block var4 = var2.getBlockState(var3.up()).getBlock();
         var1 = var1.withProperty(SNOWY, Boolean.valueOf(var4 == Blocks.SNOW || var4 == Blocks.SNOW_LAYER));
      }

      return var1;
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item var1, CreativeTabs var2, List var3) {
      var3.add(new ItemStack(this, 1, BlockDirt.DirtType.DIRT.getMetadata()));
      var3.add(new ItemStack(this, 1, BlockDirt.DirtType.COARSE_DIRT.getMetadata()));
      var3.add(new ItemStack(this, 1, BlockDirt.DirtType.PODZOL.getMetadata()));
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(this, 1, ((BlockDirt.DirtType)var3.getValue(VARIANT)).getMetadata());
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(VARIANT, BlockDirt.DirtType.byMetadata(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((BlockDirt.DirtType)var1.getValue(VARIANT)).getMetadata();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{VARIANT, SNOWY});
   }

   public int damageDropped(IBlockState var1) {
      BlockDirt.DirtType var2 = (BlockDirt.DirtType)var1.getValue(VARIANT);
      if (var2 == BlockDirt.DirtType.PODZOL) {
         var2 = BlockDirt.DirtType.DIRT;
      }

      return var2.getMetadata();
   }

   public static enum DirtType implements IStringSerializable {
      DIRT(0, "dirt", "default", MapColor.DIRT),
      COARSE_DIRT(1, "coarse_dirt", "coarse", MapColor.DIRT),
      PODZOL(2, "podzol", MapColor.OBSIDIAN);

      private static final BlockDirt.DirtType[] METADATA_LOOKUP = new BlockDirt.DirtType[values().length];
      private final int metadata;
      private final String name;
      private final String unlocalizedName;
      private final MapColor color;

      private DirtType(int var3, String var4, MapColor var5) {
         this(var3, var4, var4, var5);
      }

      private DirtType(int var3, String var4, String var5, MapColor var6) {
         this.metadata = var3;
         this.name = var4;
         this.unlocalizedName = var5;
         this.color = var6;
      }

      public int getMetadata() {
         return this.metadata;
      }

      public String getUnlocalizedName() {
         return this.unlocalizedName;
      }

      public MapColor getColor() {
         return this.color;
      }

      public String toString() {
         return this.name;
      }

      public static BlockDirt.DirtType byMetadata(int var0) {
         if (var0 < 0 || var0 >= METADATA_LOOKUP.length) {
            var0 = 0;
         }

         return METADATA_LOOKUP[var0];
      }

      public String getName() {
         return this.name;
      }

      static {
         for(BlockDirt.DirtType var3 : values()) {
            METADATA_LOOKUP[var3.getMetadata()] = var3;
         }

      }
   }
}
