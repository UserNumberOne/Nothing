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
      super(materialIn, color);
      this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockHugeMushroom.EnumType.ALL_OUTSIDE));
      this.smallBlock = smallBlockIn;
   }

   public int quantityDropped(Random var1) {
      return Math.max(0, random.nextInt(10) - 7);
   }

   public MapColor getMapColor(IBlockState var1) {
      switch((BlockHugeMushroom.EnumType)state.getValue(VARIANT)) {
      case ALL_STEM:
         return MapColor.CLOTH;
      case ALL_INSIDE:
         return MapColor.SAND;
      case STEM:
         return MapColor.SAND;
      default:
         return super.getMapColor(state);
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
      return this.getDefaultState().withProperty(VARIANT, BlockHugeMushroom.EnumType.byMetadata(meta));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((BlockHugeMushroom.EnumType)state.getValue(VARIANT)).getMetadata();
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      switch(rot) {
      case CLOCKWISE_180:
         switch((BlockHugeMushroom.EnumType)state.getValue(VARIANT)) {
         case STEM:
            break;
         case NORTH_WEST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH_EAST);
         case NORTH:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH);
         case NORTH_EAST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH_WEST);
         case WEST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.EAST);
         case EAST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.WEST);
         case SOUTH_WEST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH_EAST);
         case SOUTH:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH);
         case SOUTH_EAST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH_WEST);
         default:
            return state;
         }
      case COUNTERCLOCKWISE_90:
         switch((BlockHugeMushroom.EnumType)state.getValue(VARIANT)) {
         case STEM:
            break;
         case NORTH_WEST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH_WEST);
         case NORTH:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.WEST);
         case NORTH_EAST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH_WEST);
         case WEST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH);
         case EAST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH);
         case SOUTH_WEST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH_EAST);
         case SOUTH:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.EAST);
         case SOUTH_EAST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH_EAST);
         default:
            return state;
         }
      case CLOCKWISE_90:
         switch((BlockHugeMushroom.EnumType)state.getValue(VARIANT)) {
         case STEM:
            break;
         case NORTH_WEST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH_EAST);
         case NORTH:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.EAST);
         case NORTH_EAST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH_EAST);
         case WEST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH);
         case EAST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH);
         case SOUTH_WEST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH_WEST);
         case SOUTH:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.WEST);
         case SOUTH_EAST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH_WEST);
         default:
            return state;
         }
      default:
         return state;
      }
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      BlockHugeMushroom.EnumType blockhugemushroom$enumtype = (BlockHugeMushroom.EnumType)state.getValue(VARIANT);
      switch(mirrorIn) {
      case LEFT_RIGHT:
         switch(blockhugemushroom$enumtype) {
         case NORTH_WEST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH_WEST);
         case NORTH:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH);
         case NORTH_EAST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH_EAST);
         case WEST:
         case EAST:
         default:
            return super.withMirror(state, mirrorIn);
         case SOUTH_WEST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH_WEST);
         case SOUTH:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH);
         case SOUTH_EAST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH_EAST);
         }
      case FRONT_BACK:
         switch(blockhugemushroom$enumtype) {
         case NORTH_WEST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH_EAST);
         case NORTH:
         case SOUTH:
         default:
            break;
         case NORTH_EAST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.NORTH_WEST);
         case WEST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.EAST);
         case EAST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.WEST);
         case SOUTH_WEST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH_EAST);
         case SOUTH_EAST:
            return state.withProperty(VARIANT, BlockHugeMushroom.EnumType.SOUTH_WEST);
         }
      default:
         return super.withMirror(state, mirrorIn);
      }
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{VARIANT});
   }

   public boolean rotateBlock(World var1, BlockPos var2, EnumFacing var3) {
      IBlockState state = world.getBlockState(pos);

      for(IProperty prop : state.getProperties().keySet()) {
         if (prop.getName().equals("variant")) {
            world.setBlockState(pos, state.cycleProperty(prop));
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
         this.meta = meta;
         this.name = name;
      }

      public int getMetadata() {
         return this.meta;
      }

      public String toString() {
         return this.name;
      }

      public static BlockHugeMushroom.EnumType byMetadata(int var0) {
         if (meta < 0 || meta >= META_LOOKUP.length) {
            meta = 0;
         }

         BlockHugeMushroom.EnumType blockhugemushroom$enumtype = META_LOOKUP[meta];
         return blockhugemushroom$enumtype == null ? META_LOOKUP[0] : blockhugemushroom$enumtype;
      }

      public String getName() {
         return this.name;
      }

      static {
         for(BlockHugeMushroom.EnumType blockhugemushroom$enumtype : values()) {
            META_LOOKUP[blockhugemushroom$enumtype.getMetadata()] = blockhugemushroom$enumtype;
         }

      }
   }
}
