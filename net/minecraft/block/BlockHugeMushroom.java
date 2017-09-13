package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockHugeMushroom extends Block {
   public static final PropertyEnum VARIANT = PropertyEnum.create("variant", BlockHugeMushroom.EnumType.class);
   private final Block smallBlock;

   public BlockHugeMushroom(Material var1, MapColor var2, Block var3) {
      super(var1, var2);
      this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockHugeMushroom.EnumType.ALL_OUTSIDE));
      this.smallBlock = var3;
   }

   public int quantityDropped(Random var1) {
      return Math.max(0, var1.nextInt(10) - 7);
   }

   public MapColor getMapColor(IBlockState var1) {
      switch((BlockHugeMushroom.EnumType)var1.getValue(VARIANT)) {
      case ALL_STEM:
         return MapColor.CLOTH;
      case ALL_INSIDE:
         return MapColor.SAND;
      case STEM:
         return MapColor.SAND;
      default:
         return super.getMapColor(var1);
      }
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Item.getItemFromBlock(this.smallBlock);
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(this.smallBlock);
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return this.getDefaultState();
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(VARIANT, BlockHugeMushroom.EnumType.byMetadata(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((BlockHugeMushroom.EnumType)var1.getValue(VARIANT)).getMetadata();
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      switch(var2) {
      case CLOCKWISE_180:
         switch((BlockHugeMushroom.EnumType)var1.getValue(VARIANT)) {
         case STEM:
            break;
         case NORTH_WEST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH_EAST);
         case NORTH:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH);
         case NORTH_EAST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH_WEST);
         case WEST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.EAST);
         case EAST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.WEST);
         case SOUTH_WEST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH_EAST);
         case SOUTH:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH);
         case SOUTH_EAST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH_WEST);
         default:
            return var1;
         }
      case COUNTERCLOCKWISE_90:
         switch((BlockHugeMushroom.EnumType)var1.getValue(VARIANT)) {
         case STEM:
            break;
         case NORTH_WEST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH_WEST);
         case NORTH:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.WEST);
         case NORTH_EAST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH_WEST);
         case WEST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH);
         case EAST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH);
         case SOUTH_WEST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH_EAST);
         case SOUTH:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.EAST);
         case SOUTH_EAST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH_EAST);
         default:
            return var1;
         }
      case CLOCKWISE_90:
         switch((BlockHugeMushroom.EnumType)var1.getValue(VARIANT)) {
         case STEM:
            break;
         case NORTH_WEST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH_EAST);
         case NORTH:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.EAST);
         case NORTH_EAST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH_EAST);
         case WEST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH);
         case EAST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH);
         case SOUTH_WEST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH_WEST);
         case SOUTH:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.WEST);
         case SOUTH_EAST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH_WEST);
         default:
            return var1;
         }
      default:
         return var1;
      }
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      BlockHugeMushroom.EnumType var3 = (BlockHugeMushroom.EnumType)var1.getValue(VARIANT);
      switch(var2) {
      case LEFT_RIGHT:
         switch(var3) {
         case NORTH_WEST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH_WEST);
         case NORTH:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH);
         case NORTH_EAST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH_EAST);
         case WEST:
         case EAST:
         default:
            return super.withMirror(var1, var2);
         case SOUTH_WEST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH_WEST);
         case SOUTH:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH);
         case SOUTH_EAST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH_EAST);
         }
      case FRONT_BACK:
         switch(var3) {
         case NORTH_WEST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH_EAST);
         case NORTH:
         case SOUTH:
         default:
            break;
         case NORTH_EAST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH_WEST);
         case WEST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.EAST);
         case EAST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.WEST);
         case SOUTH_WEST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH_EAST);
         case SOUTH_EAST:
            return var1.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH_WEST);
         }
      default:
         return super.withMirror(var1, var2);
      }
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{VARIANT});
   }

   public boolean rotateBlock(World var1, BlockPos var2, EnumFacing var3) {
      IBlockState var4 = var1.getBlockState(var2);

      for(IProperty var6 : var4.getProperties().keySet()) {
         if (var6.getName().equals("variant")) {
            var1.setBlockState(var2, var4.cycleProperty(var6));
            return true;
         }
      }

      return false;
   }

   public static enum EnumType implements IStringSerializable {
      NORTH_WEST(1, "north_west"),
      NORTH(2, "north"),
      NORTH_EAST(3, "north_east"),
      WEST(4, "west"),
      CENTER(5, "center"),
      EAST(6, "east"),
      SOUTH_WEST(7, "south_west"),
      SOUTH(8, "south"),
      SOUTH_EAST(9, "south_east"),
      STEM(10, "stem"),
      ALL_INSIDE(0, "all_inside"),
      ALL_OUTSIDE(14, "all_outside"),
      ALL_STEM(15, "all_stem");

      private static final BlockHugeMushroom.EnumType[] META_LOOKUP = new BlockHugeMushroom.EnumType[16];
      private final int meta;
      private final String name;

      private EnumType(int var3, String var4) {
         this.meta = var3;
         this.name = var4;
      }

      public int getMetadata() {
         return this.meta;
      }

      public String toString() {
         return this.name;
      }

      public static BlockHugeMushroom.EnumType byMetadata(int var0) {
         if (var0 < 0 || var0 >= META_LOOKUP.length) {
            var0 = 0;
         }

         BlockHugeMushroom.EnumType var1 = META_LOOKUP[var0];
         return var1 == null ? META_LOOKUP[0] : var1;
      }

      public String getName() {
         return this.name;
      }

      static {
         for(BlockHugeMushroom.EnumType var3 : values()) {
            META_LOOKUP[var3.getMetadata()] = var3;
         }

      }
   }
}
