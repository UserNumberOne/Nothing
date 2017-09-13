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
      if (var7 == BlockQuartz.EnumType.LINES_Y.getMetadata()) {
         switch(var3.getAxis()) {
         case Z:
            return this.getDefaultState().withProperty(VARIANT, BlockQuartz.EnumType.LINES_Z);
         case X:
            return this.getDefaultState().withProperty(VARIANT, BlockQuartz.EnumType.LINES_X);
         case Y:
            return this.getDefaultState().withProperty(VARIANT, BlockQuartz.EnumType.LINES_Y);
         }
      }

      return var7 == BlockQuartz.EnumType.CHISELED.getMetadata() ? this.getDefaultState().withProperty(VARIANT, BlockQuartz.EnumType.CHISELED) : this.getDefaultState().withProperty(VARIANT, BlockQuartz.EnumType.DEFAULT);
   }

   public int damageDropped(IBlockState var1) {
      BlockQuartz.EnumType var2 = (BlockQuartz.EnumType)var1.getValue(VARIANT);
      return var2 != BlockQuartz.EnumType.LINES_X && var2 != BlockQuartz.EnumType.LINES_Z ? var2.getMetadata() : BlockQuartz.EnumType.LINES_Y.getMetadata();
   }

   protected ItemStack getSilkTouchDrop(IBlockState var1) {
      BlockQuartz.EnumType var2 = (BlockQuartz.EnumType)var1.getValue(VARIANT);
      return var2 != BlockQuartz.EnumType.LINES_X && var2 != BlockQuartz.EnumType.LINES_Z ? super.getSilkTouchDrop(var1) : new ItemStack(Item.getItemFromBlock(this), 1, BlockQuartz.EnumType.LINES_Y.getMetadata());
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item var1, CreativeTabs var2, List var3) {
      var3.add(new ItemStack(var1, 1, BlockQuartz.EnumType.DEFAULT.getMetadata()));
      var3.add(new ItemStack(var1, 1, BlockQuartz.EnumType.CHISELED.getMetadata()));
      var3.add(new ItemStack(var1, 1, BlockQuartz.EnumType.LINES_Y.getMetadata()));
   }

   public MapColor getMapColor(IBlockState var1) {
      return MapColor.QUARTZ;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(VARIANT, BlockQuartz.EnumType.byMetadata(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((BlockQuartz.EnumType)var1.getValue(VARIANT)).getMetadata();
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      switch(var2) {
      case COUNTERCLOCKWISE_90:
      case CLOCKWISE_90:
         switch((BlockQuartz.EnumType)var1.getValue(VARIANT)) {
         case LINES_X:
            return var1.withProperty(VARIANT, BlockQuartz.EnumType.LINES_Z);
         case LINES_Z:
            return var1.withProperty(VARIANT, BlockQuartz.EnumType.LINES_X);
         default:
            return var1;
         }
      default:
         return var1;
      }
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{VARIANT});
   }

   public boolean rotateBlock(World var1, BlockPos var2, EnumFacing var3) {
      IBlockState var4 = var1.getBlockState(var2);
      UnmodifiableIterator var5 = var4.getProperties().keySet().iterator();

      while(var5.hasNext()) {
         IProperty var6 = (IProperty)var5.next();
         if (var6.getName().equals("variant") && var6.getValueClass() == BlockQuartz.EnumType.class) {
            BlockQuartz.EnumType var7 = (BlockQuartz.EnumType)var4.getValue(var6);
            BlockQuartz.EnumType var8 = var7 == BlockQuartz.EnumType.LINES_X ? BlockQuartz.EnumType.LINES_Y : (var7 == BlockQuartz.EnumType.LINES_Y ? BlockQuartz.EnumType.LINES_Z : (var7 == BlockQuartz.EnumType.LINES_Z ? BlockQuartz.EnumType.LINES_X : var7));
            if (var8 == var7) {
               return false;
            }

            var1.setBlockState(var2, var4.withProperty(var6, var8));
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
         this.meta = var3;
         this.serializedName = var4;
         this.unlocalizedName = var5;
      }

      public int getMetadata() {
         return this.meta;
      }

      public String toString() {
         return this.unlocalizedName;
      }

      public static BlockQuartz.EnumType byMetadata(int var0) {
         if (var0 < 0 || var0 >= META_LOOKUP.length) {
            var0 = 0;
         }

         return META_LOOKUP[var0];
      }

      public String getName() {
         return this.serializedName;
      }

      static {
         for(BlockQuartz.EnumType var3 : values()) {
            META_LOOKUP[var3.getMetadata()] = var3;
         }

      }
   }
}
