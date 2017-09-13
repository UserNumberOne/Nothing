package net.minecraft.block;

import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPressurePlate extends BlockBasePressurePlate {
   public static final PropertyBool POWERED = PropertyBool.create("powered");
   private final BlockPressurePlate.Sensitivity sensitivity;

   protected BlockPressurePlate(Material var1, BlockPressurePlate.Sensitivity var2) {
      super(var1);
      this.setDefaultState(this.blockState.getBaseState().withProperty(POWERED, Boolean.valueOf(false)));
      this.sensitivity = var2;
   }

   protected int getRedstoneStrength(IBlockState var1) {
      return ((Boolean)var1.getValue(POWERED)).booleanValue() ? 15 : 0;
   }

   protected IBlockState setRedstoneStrength(IBlockState var1, int var2) {
      return var1.withProperty(POWERED, Boolean.valueOf(var2 > 0));
   }

   protected void playClickOnSound(World var1, BlockPos var2) {
      if (this.blockMaterial == Material.WOOD) {
         var1.playSound((EntityPlayer)null, var2, SoundEvents.BLOCK_WOOD_PRESSPLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.8F);
      } else {
         var1.playSound((EntityPlayer)null, var2, SoundEvents.BLOCK_STONE_PRESSPLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.6F);
      }

   }

   protected void playClickOffSound(World var1, BlockPos var2) {
      if (this.blockMaterial == Material.WOOD) {
         var1.playSound((EntityPlayer)null, var2, SoundEvents.BLOCK_WOOD_PRESSPLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.7F);
      } else {
         var1.playSound((EntityPlayer)null, var2, SoundEvents.BLOCK_STONE_PRESSPLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.5F);
      }

   }

   protected int computeRedstoneStrength(World var1, BlockPos var2) {
      AxisAlignedBB var3 = PRESSURE_AABB.offset(var2);
      List var4;
      switch(this.sensitivity) {
      case EVERYTHING:
         var4 = var1.getEntitiesWithinAABBExcludingEntity((Entity)null, var3);
         break;
      case MOBS:
         var4 = var1.getEntitiesWithinAABB(EntityLivingBase.class, var3);
         break;
      default:
         return 0;
      }

      if (!var4.isEmpty()) {
         for(Entity var6 : var4) {
            if (!var6.doesEntityNotTriggerPressurePlate()) {
               return 15;
            }
         }
      }

      return 0;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(POWERED, Boolean.valueOf(var1 == 1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Boolean)var1.getValue(POWERED)).booleanValue() ? 1 : 0;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{POWERED});
   }

   public static enum Sensitivity {
      EVERYTHING,
      MOBS;
   }
}
