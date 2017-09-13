package net.minecraft.item;

import java.util.List;
import java.util.Random;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Rotations;
import net.minecraft.world.World;

public class ItemArmorStand extends Item {
   public ItemArmorStand() {
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      if (var6 == EnumFacing.DOWN) {
         return EnumActionResult.FAIL;
      } else {
         boolean var10 = var3.getBlockState(var4).getBlock().isReplaceable(var3, var4);
         BlockPos var11 = var10 ? var4 : var4.offset(var6);
         if (!var2.canPlayerEdit(var11, var6, var1)) {
            return EnumActionResult.FAIL;
         } else {
            BlockPos var12 = var11.up();
            boolean var13 = !var3.isAirBlock(var11) && !var3.getBlockState(var11).getBlock().isReplaceable(var3, var11);
            var13 = var13 | (!var3.isAirBlock(var12) && !var3.getBlockState(var12).getBlock().isReplaceable(var3, var12));
            if (var13) {
               return EnumActionResult.FAIL;
            } else {
               double var14 = (double)var11.getX();
               double var16 = (double)var11.getY();
               double var18 = (double)var11.getZ();
               List var20 = var3.getEntitiesWithinAABBExcludingEntity((Entity)null, new AxisAlignedBB(var14, var16, var18, var14 + 1.0D, var16 + 2.0D, var18 + 1.0D));
               if (!var20.isEmpty()) {
                  return EnumActionResult.FAIL;
               } else {
                  if (!var3.isRemote) {
                     var3.setBlockToAir(var11);
                     var3.setBlockToAir(var12);
                     EntityArmorStand var21 = new EntityArmorStand(var3, var14 + 0.5D, var16, var18 + 0.5D);
                     float var22 = (float)MathHelper.floor((MathHelper.wrapDegrees(var2.rotationYaw - 180.0F) + 22.5F) / 45.0F) * 45.0F;
                     var21.setLocationAndAngles(var14 + 0.5D, var16, var18 + 0.5D, var22, 0.0F);
                     this.applyRandomRotations(var21, var3.rand);
                     ItemMonsterPlacer.applyItemEntityDataToEntity(var3, var2, var1, var21);
                     var3.spawnEntity(var21);
                     var3.playSound((EntityPlayer)null, var21.posX, var21.posY, var21.posZ, SoundEvents.ENTITY_ARMORSTAND_PLACE, SoundCategory.BLOCKS, 0.75F, 0.8F);
                  }

                  --var1.stackSize;
                  return EnumActionResult.SUCCESS;
               }
            }
         }
      }
   }

   private void applyRandomRotations(EntityArmorStand var1, Random var2) {
      Rotations var3 = var1.getHeadRotation();
      float var4 = var2.nextFloat() * 5.0F;
      float var5 = var2.nextFloat() * 20.0F - 10.0F;
      Rotations var6 = new Rotations(var3.getX() + var4, var3.getY() + var5, var3.getZ());
      var1.setHeadRotation(var6);
      var3 = var1.getBodyRotation();
      var4 = var2.nextFloat() * 10.0F - 5.0F;
      var6 = new Rotations(var3.getX(), var3.getY() + var4, var3.getZ());
      var1.setBodyRotation(var6);
   }
}
