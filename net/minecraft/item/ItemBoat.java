package net.minecraft.item;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemBoat extends Item {
   private final EntityBoat.Type type;

   public ItemBoat(EntityBoat.Type var1) {
      this.type = var1;
      this.maxStackSize = 1;
      this.setCreativeTab(CreativeTabs.TRANSPORTATION);
      this.setUnlocalizedName("boat." + var1.getName());
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      float var5 = 1.0F;
      float var6 = var3.prevRotationPitch + (var3.rotationPitch - var3.prevRotationPitch) * 1.0F;
      float var7 = var3.prevRotationYaw + (var3.rotationYaw - var3.prevRotationYaw) * 1.0F;
      double var8 = var3.prevPosX + (var3.posX - var3.prevPosX) * 1.0D;
      double var10 = var3.prevPosY + (var3.posY - var3.prevPosY) * 1.0D + (double)var3.getEyeHeight();
      double var12 = var3.prevPosZ + (var3.posZ - var3.prevPosZ) * 1.0D;
      Vec3d var14 = new Vec3d(var8, var10, var12);
      float var15 = MathHelper.cos(-var7 * 0.017453292F - 3.1415927F);
      float var16 = MathHelper.sin(-var7 * 0.017453292F - 3.1415927F);
      float var17 = -MathHelper.cos(-var6 * 0.017453292F);
      float var18 = MathHelper.sin(-var6 * 0.017453292F);
      float var19 = var16 * var17;
      float var20 = var15 * var17;
      double var21 = 5.0D;
      Vec3d var23 = var14.addVector((double)var19 * 5.0D, (double)var18 * 5.0D, (double)var20 * 5.0D);
      RayTraceResult var24 = var2.rayTraceBlocks(var14, var23, true);
      if (var24 == null) {
         return new ActionResult(EnumActionResult.PASS, var1);
      } else {
         Vec3d var25 = var3.getLook(1.0F);
         boolean var26 = false;
         List var27 = var2.getEntitiesWithinAABBExcludingEntity(var3, var3.getEntityBoundingBox().addCoord(var25.xCoord * 5.0D, var25.yCoord * 5.0D, var25.zCoord * 5.0D).expandXyz(1.0D));

         for(int var28 = 0; var28 < var27.size(); ++var28) {
            Entity var29 = (Entity)var27.get(var28);
            if (var29.canBeCollidedWith()) {
               AxisAlignedBB var30 = var29.getEntityBoundingBox().expandXyz((double)var29.getCollisionBorderSize());
               if (var30.isVecInside(var14)) {
                  var26 = true;
               }
            }
         }

         if (var26) {
            return new ActionResult(EnumActionResult.PASS, var1);
         } else if (var24.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult(EnumActionResult.PASS, var1);
         } else {
            Block var31 = var2.getBlockState(var24.getBlockPos()).getBlock();
            boolean var32 = var31 == Blocks.WATER || var31 == Blocks.FLOWING_WATER;
            EntityBoat var33 = new EntityBoat(var2, var24.hitVec.xCoord, var32 ? var24.hitVec.yCoord - 0.12D : var24.hitVec.yCoord, var24.hitVec.zCoord);
            var33.setBoatType(this.type);
            var33.rotationYaw = var3.rotationYaw;
            if (!var2.getCollisionBoxes(var33, var33.getEntityBoundingBox().expandXyz(-0.1D)).isEmpty()) {
               return new ActionResult(EnumActionResult.FAIL, var1);
            } else {
               if (!var2.isRemote) {
                  var2.spawnEntity(var33);
               }

               if (!var3.capabilities.isCreativeMode) {
                  --var1.stackSize;
               }

               var3.addStat(StatList.getObjectUseStats(this));
               return new ActionResult(EnumActionResult.SUCCESS, var1);
            }
         }
      }
   }
}
