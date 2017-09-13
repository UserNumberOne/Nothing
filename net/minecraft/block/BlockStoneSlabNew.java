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
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockStoneSlabNew extends BlockSlab {
   public static final PropertyBool SEAMLESS = PropertyBool.create("seamless");
   public static final PropertyEnum VARIANT = PropertyEnum.create("variant", BlockStoneSlabNew.EnumType.class);

   public BlockStoneSlabNew() {
      super(Material.ROCK);
      IBlockState iblockstate = this.blockState.getBaseState();
      if (this.isDouble()) {
         iblockstate = iblockstate.withProperty(SEAMLESS, Boolean.valueOf(false));
      } else {
         iblockstate = iblockstate.withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
      }

      this.setDefaultState(iblockstate.withProperty(VARIANT, BlockStoneSlabNew.EnumType.RED_SANDSTONE));
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
      return new ItemStack(Blocks.STONE_SLAB2, 1, ((BlockStoneSlabNew.EnumType)state.getValue(VARIANT)).getMetadata());
   }

   public String getUnlocalizedName(int var1) {
      return super.getUnlocalizedName() + "." + BlockStoneSlabNew.EnumType.byMetadata(meta).getUnlocalizedName();
   }

   public IProperty getVariantProperty() {
      return VARIANT;
   }

   public Comparable getTypeForItem(ItemStack var1) {
      return BlockStoneSlabNew.EnumType.byMetadata(stack.getMetadata() & 7);
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item var1, CreativeTabs var2, List var3) {
      if (itemIn != Item.getItemFromBlock(Blocks.DOUBLE_STONE_SLAB2)) {
         for(BlockStoneSlabNew.EnumType blockstoneslabnew$enumtype : BlockStoneSlabNew.EnumType.values()) {
            list.add(new ItemStack(itemIn, 1, blockstoneslabnew$enumtype.getMetadata()));
         }
      }

   }

   public IBlockState getStateFromMeta(int var1) {
      IBlockState iblockstate = this.getDefaultState().withProperty(VARIANT, BlockStoneSlabNew.EnumType.byMetadata(meta & 7));
      if (this.isDouble()) {
         iblockstate = iblockstate.withProperty(SEAMLESS, Boolean.valueOf((meta & 8) != 0));
      } else {
         iblockstate = iblockstate.withProperty(HALF, (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
      }

      return iblockstate;
   }

   public int getMetaFromState(IBlockState var1) {
      int i = 0;
      i = i | ((BlockStoneSlabNew.EnumType)state.getValue(VARIANT)).getMetadata();
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

   public MapColor getMapColor(IBlockState var1) {
      return ((BlockStoneSlabNew.EnumType)state.getValue(VARIANT)).getMapColor();
   }

   public int damageDropped(IBlockState var1) {
      return ((BlockStoneSlabNew.EnumType)state.getValue(VARIANT)).getMetadata();
   }

   public static enum EnumType implements IStringSerializable {
      RED_SANDSTONE(0, "red_sandstone", BlockSand.EnumType.RED_SAND.getMapColor());

      private static final BlockStoneSlabNew.EnumType[] META_LOOKUP = new BlockStoneSlabNew.EnumType[values().length];
      private final int meta;
      private final String name;
      private final MapColor mapColor;

      private EnumType(int var3, String var4, MapColor var5) {
         this.meta = p_i46391_3_;
         this.name = p_i46391_4_;
         this.mapColor = p_i46391_5_;
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
         if (meta < 0 || meta >= META_LOOKUP.length) {
            meta = 0;
         }

         return META_LOOKUP[meta];
      }

      public String getName() {
         return this.name;
      }

      public String getUnlocalizedName() {
         return this.name;
      }

      static {
         for(BlockStoneSlabNew.EnumType blockstoneslabnew$enumtype : values()) {
            META_LOOKUP[blockstoneslabnew$enumtype.getMetadata()] = blockstoneslabnew$enumtype;
         }

      }
   }
}
