package net.minecraft.block;

import com.google.common.base.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class BlockOldLog extends BlockLog {
   public static final PropertyEnum VARIANT = PropertyEnum.create("variant", BlockPlanks.EnumType.class, new Predicate() {
      public boolean apply(@Nullable BlockPlanks.EnumType var1) {
         return var1.getMetadata() < 4;
      }

      // $FF: synthetic method
      public boolean apply(Object var1) {
         return this.apply((BlockPlanks.EnumType)var1);
      }
   });

   public BlockOldLog() {
      this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockPlanks.EnumType.OAK).withProperty(LOG_AXIS, BlockLog.EnumAxis.Y));
   }

   public MapColor getMapColor(IBlockState var1) {
      BlockPlanks.EnumType var2 = (BlockPlanks.EnumType)var1.getValue(VARIANT);
      switch((BlockLog.EnumAxis)var1.getValue(LOG_AXIS)) {
      case X:
      case Z:
      case NONE:
      default:
         switch(var2) {
         case OAK:
         default:
            return BlockPlanks.EnumType.SPRUCE.getMapColor();
         case SPRUCE:
            return BlockPlanks.EnumType.DARK_OAK.getMapColor();
         case BIRCH:
            return MapColor.QUARTZ;
         case JUNGLE:
            return BlockPlanks.EnumType.SPRUCE.getMapColor();
         }
      case Y:
         return var2.getMapColor();
      }
   }

   public IBlockState getStateFromMeta(int var1) {
      IBlockState var2 = this.getDefaultState().withProperty(VARIANT, BlockPlanks.EnumType.byMetadata((var1 & 3) % 4));
      switch(var1 & 12) {
      case 0:
         var2 = var2.withProperty(LOG_AXIS, BlockLog.EnumAxis.Y);
         break;
      case 4:
         var2 = var2.withProperty(LOG_AXIS, BlockLog.EnumAxis.X);
         break;
      case 8:
         var2 = var2.withProperty(LOG_AXIS, BlockLog.EnumAxis.Z);
         break;
      default:
         var2 = var2.withProperty(LOG_AXIS, BlockLog.EnumAxis.NONE);
      }

      return var2;
   }

   public int getMetaFromState(IBlockState var1) {
      int var2 = 0;
      var2 = var2 | ((BlockPlanks.EnumType)var1.getValue(VARIANT)).getMetadata();
      switch((BlockLog.EnumAxis)var1.getValue(LOG_AXIS)) {
      case X:
         var2 |= 4;
         break;
      case Z:
         var2 |= 8;
         break;
      case NONE:
         var2 |= 12;
      }

      return var2;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{VARIANT, LOG_AXIS});
   }

   protected ItemStack getSilkTouchDrop(IBlockState var1) {
      return new ItemStack(Item.getItemFromBlock(this), 1, ((BlockPlanks.EnumType)var1.getValue(VARIANT)).getMetadata());
   }

   public int damageDropped(IBlockState var1) {
      return ((BlockPlanks.EnumType)var1.getValue(VARIANT)).getMetadata();
   }
}
