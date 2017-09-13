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
      IBlockState iblockstate = this.blockState.getBaseState();
      if (this.isDouble()) {
         iblockstate = iblockstate.withProperty(SEAMLESS, Boolean.valueOf(false));
      } else {
         iblockstate = iblockstate.withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
      }

      this.setDefaultState(iblockstate.withProperty(VARIANT, BlockStoneSlab.EnumType.STONE));
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Item.getItemFromBlock(Blocks.STONE_SLAB);
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Blocks.STONE_SLAB, 1, ((BlockStoneSlab.EnumType)state.getValue(VARIANT)).getMetadata());
   }

   public String getUnlocalizedName(int var1) {
      return super.getUnlocalizedName() + "." + BlockStoneSlab.EnumType.byMetadata(meta).getUnlocalizedName();
   }

   public IProperty getVariantProperty() {
      return VARIANT;
   }

   public Comparable getTypeForItem(ItemStack var1) {
      return BlockStoneSlab.EnumType.byMetadata(stack.getMetadata() & 7);
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item var1, CreativeTabs var2, List var3) {
      if (itemIn != Item.getItemFromBlock(Blocks.DOUBLE_STONE_SLAB)) {
         for(BlockStoneSlab.EnumType blockstoneslab$enumtype : BlockStoneSlab.EnumType.values()) {
            if (blockstoneslab$enumtype != BlockStoneSlab.EnumType.WOOD) {
               list.add(new ItemStack(itemIn, 1, blockstoneslab$enumtype.getMetadata()));
            }
         }
      }

   }

   public IBlockState getStateFromMeta(int var1) {
      IBlockState iblockstate = this.getDefaultState().withProperty(VARIANT, BlockStoneSlab.EnumType.byMetadata(meta & 7));
      if (this.isDouble()) {
         iblockstate = iblockstate.withProperty(SEAMLESS, Boolean.valueOf((meta & 8) != 0));
      } else {
         iblockstate = iblockstate.withProperty(HALF, (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
      }

      return iblockstate;
   }

   public int getMetaFromState(IBlockState var1) {
      int i = 0;
      i = i | ((BlockStoneSlab.EnumType)state.getValue(VARIANT)).getMetadata();
      if (this.isDouble()) {
         if (((Boolean)state.getValue(SEAMLESS)).booleanValue()) {
            i |= 8;
         }
      } else if (state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) {
         i |= 8;
      }

      return i;
   }

   protected BlockStateContainer createBlockState() {
      return this.isDouble() ? new BlockStateContainer(this, new IProperty[]{SEAMLESS, VARIANT}) : new BlockStateContainer(this, new IProperty[]{HALF, VARIANT});
   }

   public int damageDropped(IBlockState var1) {
      return ((BlockStoneSlab.EnumType)state.getValue(VARIANT)).getMetadata();
   }

   public MapColor getMapColor(IBlockState var1) {
      return ((BlockStoneSlab.EnumType)state.getValue(VARIANT)).getMapColor();
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
         this(p_i46381_3_, p_i46381_4_, p_i46381_5_, p_i46381_5_);
      }

      private EnumType(int var3, MapColor var4, String var5, String var6) {
         this.meta = p_i46382_3_;
         this.mapColor = p_i46382_4_;
         this.name = p_i46382_5_;
         this.unlocalizedName = p_i46382_6_;
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
         for(BlockStoneSlab.EnumType blockstoneslab$enumtype : values()) {
            META_LOOKUP[blockstoneslab$enumtype.getMetadata()] = blockstoneslab$enumtype;
         }

      }
   }
}
