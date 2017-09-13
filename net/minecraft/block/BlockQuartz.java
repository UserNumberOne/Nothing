package net.minecraft.block;

import com.google.common.collect.UnmodifiableIterator;
import java.util.List;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockQuartz extends Block {
   public static final PropertyEnum VARIANT = PropertyEnum.create("variant", BlockQuartz.EnumType.class);

   public BlockQuartz() {
      super(Material.ROCK);
      this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockQuartz.EnumType.DEFAULT));
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      if (meta == BlockQuartz.EnumType.LINES_Y.getMetadata()) {
         switch(facing.getAxis()) {
         case Z:
            return this.getDefaultState().withProperty(VARIANT, BlockQuartz.EnumType.LINES_Z);
         case X:
            return this.getDefaultState().withProperty(VARIANT, BlockQuartz.EnumType.LINES_X);
         case Y:
            return this.getDefaultState().withProperty(VARIANT, BlockQuartz.EnumType.LINES_Y);
         }
      }

      return meta == BlockQuartz.EnumType.CHISELED.getMetadata() ? this.getDefaultState().withProperty(VARIANT, BlockQuartz.EnumType.CHISELED) : this.getDefaultState().withProperty(VARIANT, BlockQuartz.EnumType.DEFAULT);
   }

   public int damageDropped(IBlockState var1) {
      BlockQuartz.EnumType blockquartz$enumtype = (BlockQuartz.EnumType)state.getValue(VARIANT);
      return blockquartz$enumtype != BlockQuartz.EnumType.LINES_X && blockquartz$enumtype != BlockQuartz.EnumType.LINES_Z ? blockquartz$enumtype.getMetadata() : BlockQuartz.EnumType.LINES_Y.getMetadata();
   }

   protected ItemStack getSilkTouchDrop(IBlockState var1) {
      BlockQuartz.EnumType blockquartz$enumtype = (BlockQuartz.EnumType)state.getValue(VARIANT);
      return blockquartz$enumtype != BlockQuartz.EnumType.LINES_X && blockquartz$enumtype != BlockQuartz.EnumType.LINES_Z ? super.getSilkTouchDrop(state) : new ItemStack(Item.getItemFromBlock(this), 1, BlockQuartz.EnumType.LINES_Y.getMetadata());
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item var1, CreativeTabs var2, List var3) {
      list.add(new ItemStack(itemIn, 1, BlockQuartz.EnumType.DEFAULT.getMetadata()));
      list.add(new ItemStack(itemIn, 1, BlockQuartz.EnumType.CHISELED.getMetadata()));
      list.add(new ItemStack(itemIn, 1, BlockQuartz.EnumType.LINES_Y.getMetadata()));
   }

   public MapColor getMapColor(IBlockState var1) {
      return MapColor.QUARTZ;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(VARIANT, BlockQuartz.EnumType.byMetadata(meta));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((BlockQuartz.EnumType)state.getValue(VARIANT)).getMetadata();
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      switch(rot) {
      case COUNTERCLOCKWISE_90:
      case CLOCKWISE_90:
         switch((BlockQuartz.EnumType)state.getValue(VARIANT)) {
         case LINES_X:
            return state.withProperty(VARIANT, BlockQuartz.EnumType.LINES_Z);
         case LINES_Z:
            return state.withProperty(VARIANT, BlockQuartz.EnumType.LINES_X);
         default:
            return state;
         }
      default:
         return state;
      }
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{VARIANT});
   }

   public boolean rotateBlock(World var1, BlockPos var2, EnumFacing var3) {
      IBlockState state = world.getBlockState(pos);
      UnmodifiableIterator var5 = state.getProperties().keySet().iterator();

      while(var5.hasNext()) {
         IProperty prop = (IProperty)var5.next();
         if (prop.getName().equals("variant") && prop.getValueClass() == BlockQuartz.EnumType.class) {
            BlockQuartz.EnumType current = (BlockQuartz.EnumType)state.getValue(prop);
            BlockQuartz.EnumType next = current == BlockQuartz.EnumType.LINES_X ? BlockQuartz.EnumType.LINES_Y : (current == BlockQuartz.EnumType.LINES_Y ? BlockQuartz.EnumType.LINES_Z : (current == BlockQuartz.EnumType.LINES_Z ? BlockQuartz.EnumType.LINES_X : current));
            if (next == current) {
               return false;
            }

            world.setBlockState(pos, state.withProperty(prop, next));
            return true;
         }
      }

      return false;
   }

   public static enum EnumType implements IStringSerializable {
      DEFAULT(0, "default", "default"),
      CHISELED(1, "chiseled", "chiseled"),
      LINES_Y(2, "lines_y", "lines"),
      LINES_X(3, "lines_x", "lines"),
      LINES_Z(4, "lines_z", "lines");

      private static final BlockQuartz.EnumType[] META_LOOKUP = new BlockQuartz.EnumType[values().length];
      private final int meta;
      private final String serializedName;
      private final String unlocalizedName;

      private EnumType(int var3, String var4, String var5) {
         this.meta = meta;
         this.serializedName = name;
         this.unlocalizedName = unlocalizedName;
      }

      public int getMetadata() {
         return this.meta;
      }

      public String toString() {
         return this.unlocalizedName;
      }

      public static BlockQuartz.EnumType byMetadata(int var0) {
         if (meta < 0 || meta >= META_LOOKUP.length) {
            meta = 0;
         }

         return META_LOOKUP[meta];
      }

      public String getName() {
         return this.serializedName;
      }

      static {
         for(BlockQuartz.EnumType blockquartz$enumtype : values()) {
            META_LOOKUP[blockquartz$enumtype.getMetadata()] = blockquartz$enumtype;
         }

      }
   }
}
