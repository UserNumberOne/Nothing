package net.minecraft.block;

import java.util.List;
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
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockStoneSlab extends BlockSlab {
   public static final PropertyBool SEAMLESS = PropertyBool.create("seamless");
   public static final PropertyEnum VARIANT = PropertyEnum.create("variant", BlockStoneSlab.EnumType.class);

   public BlockStoneSlab() {
      super(Material.ROCK);
      IBlockState var1 = this.blockState.getBaseState();
      if (this.isDouble()) {
         var1 = var1.withProperty(SEAMLESS, Boolean.valueOf(false));
      } else {
         var1 = var1.withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
      }

      this.setDefaultState(var1.withProperty(VARIANT, BlockStoneSlab.EnumType.STONE));
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Item.getItemFromBlock(Blocks.STONE_SLAB);
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Blocks.STONE_SLAB, 1, ((BlockStoneSlab.EnumType)var3.getValue(VARIANT)).getMetadata());
   }

   public String getUnlocalizedName(int var1) {
      return super.getUnlocalizedName() + "." + BlockStoneSlab.EnumType.byMetadata(var1).getUnlocalizedName();
   }

   public IProperty getVariantProperty() {
      return VARIANT;
   }

   public Comparable getTypeForItem(ItemStack var1) {
      return BlockStoneSlab.EnumType.byMetadata(var1.getMetadata() & 7);
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item var1, CreativeTabs var2, List var3) {
      if (var1 != Item.getItemFromBlock(Blocks.DOUBLE_STONE_SLAB)) {
         for(BlockStoneSlab.EnumType var7 : BlockStoneSlab.EnumType.values()) {
            if (var7 != BlockStoneSlab.EnumType.WOOD) {
               var3.add(new ItemStack(var1, 1, var7.getMetadata()));
            }
         }
      }

   }

   public IBlockState getStateFromMeta(int var1) {
      IBlockState var2 = this.getDefaultState().withProperty(VARIANT, BlockStoneSlab.EnumType.byMetadata(var1 & 7));
      if (this.isDouble()) {
         var2 = var2.withProperty(SEAMLESS, Boolean.valueOf((var1 & 8) != 0));
      } else {
         var2 = var2.withProperty(HALF, (var1 & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
      }

      return var2;
   }

   public int getMetaFromState(IBlockState var1) {
      int var2 = 0;
      var2 = var2 | ((BlockStoneSlab.EnumType)var1.getValue(VARIANT)).getMetadata();
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

   public int damageDropped(IBlockState var1) {
      return ((BlockStoneSlab.EnumType)var1.getValue(VARIANT)).getMetadata();
   }

   public MapColor getMapColor(IBlockState var1) {
      return ((BlockStoneSlab.EnumType)var1.getValue(VARIANT)).getMapColor();
   }

   public static enum EnumType implements IStringSerializable {
      STONE(0, MapColor.STONE, "stone"),
      SAND(1, MapColor.SAND, "sandstone", "sand"),
      WOOD(2, MapColor.WOOD, "wood_old", "wood"),
      COBBLESTONE(3, MapColor.STONE, "cobblestone", "cobble"),
      BRICK(4, MapColor.RED, "brick"),
      SMOOTHBRICK(5, MapColor.STONE, "stone_brick", "smoothStoneBrick"),
      NETHERBRICK(6, MapColor.NETHERRACK, "nether_brick", "netherBrick"),
      QUARTZ(7, MapColor.QUARTZ, "quartz");

      private static final BlockStoneSlab.EnumType[] META_LOOKUP = new BlockStoneSlab.EnumType[values().length];
      private final int meta;
      private final MapColor mapColor;
      private final String name;
      private final String unlocalizedName;

      private EnumType(int var3, MapColor var4, String var5) {
         this(var3, var4, var5, var5);
      }

      private EnumType(int var3, MapColor var4, String var5, String var6) {
         this.meta = var3;
         this.mapColor = var4;
         this.name = var5;
         this.unlocalizedName = var6;
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

      public static BlockStoneSlab.EnumType byMetadata(int var0) {
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
         for(BlockStoneSlab.EnumType var3 : values()) {
            META_LOOKUP[var3.getMetadata()] = var3;
         }

      }
   }
}
