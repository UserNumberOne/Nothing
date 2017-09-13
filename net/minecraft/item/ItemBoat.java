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
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ItemBoat extends Item {
   private final EntityBoat.Type type;

   public ItemBoat(EntityBoat.Type var1) {
      this.type = var1;
      this.maxStackSize = 1;
      this.setCreativeTab(CreativeTabs.TRANSPORTATION);
      this.setUnlocalizedName("boat." + var1.getName());
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      float var5 = var3.prevRotationPitch + (var3.rotationPitch - var3.prevRotationPitch) * 1.0F;
      float var6 = var3.prevRotationYaw + (var3.rotationYaw - var3.prevRotationYaw) * 1.0F;
      double var7 = var3.prevPosX + (var3.posX - var3.prevPosX) * 1.0D;
      double var9 = var3.prevPosY + (var3.posY - var3.prevPosY) * 1.0D + (double)var3.getEyeHeight();
      double var11 = var3.prevPosZ + (var3.posZ - var3.prevPosZ) * 1.0D;
      Vec3d var13 = new Vec3d(var7, var9, var11);
      float var14 = MathHelper.cos(-var6 * 0.017453292F - 3.1415927F);
      float var15 = MathHelper.sin(-var6 * 0.017453292F - 3.1415927F);
      float var16 = -MathHelper.cos(-var5 * 0.017453292F);
      float var17 = MathHelper.sin(-var5 * 0.017453292F);
      float var18 = var15 * var16;
      float var19 = var14 * var16;
      Vec3d var20 = var13.addVector((double)var18 * 5.0D, (double)var17 * 5.0D, (double)var19 * 5.0D);
      RayTraceResult var21 = var2.rayTraceBlocks(var13, var20, true);
      if (var21 == null) {
         return new ActionResult(EnumActionResult.PASS, var1);
      } else {
         Vec3d var22 = var3.getLook(1.0F);
         boolean var23 = false;
         List var24 = var2.getEntitiesWithinAABBExcludingEntity(var3, var3.getEntityBoundingBox().addCoord(var22.xCoord * 5.0D, var22.yCoord * 5.0D, var22.zCoord * 5.0D).expandXyz(1.0D));

         for(int var25 = 0; var25 < var24.size(); ++var25) {
            Entity var26 = (Entity)var24.get(var25);
            if (var26.canBeCollidedWith()) {
               AxisAlignedBB var27 = var26.getEntityBoundingBox().expandXyz((double)var26.getCollisionBorderSize());
               if (var27.isVecInside(var13)) {
                  var23 = true;
               }
            }
         }

         if (var23) {
            return new ActionResult(EnumActionResult.PASS, var1);
         } else if (var21.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult(EnumActionResult.PASS, var1);
         } else {
            PlayerInteractEvent var29 = CraftEventFactory.callPlayerInteractEvent(var3, Action.RIGHT_CLICK_BLOCK, var21.getBlockPos(), var21.sideHit, var1, var4);
            if (var29.isCancelled()) {
               return new ActionResult(EnumActionResult.PASS, var1);
            } else {
               Block var30 = var2.getBlockState(var21.getBlockPos()).getBlock();
               boolean var31 = var30 == Blocks.WATER || var30 == Blocks.FLOWING_WATER;
               EntityBoat var28 = new EntityBoat(var2, var21.hitVec.xCoord, var31 ? var21.hitVec.yCoord - 0.12D : var21.hitVec.yCoord, var21.hitVec.zCoord);
               var28.setBoatType(this.type);
               var28.rotationYaw = var3.rotationYaw;
               if (!var2.getCollisionBoxes(var28, var28.getEntityBoundingBox().expandXyz(-0.1D)).isEmpty()) {
                  return new ActionResult(EnumActionResult.FAIL, var1);
               } else {
                  if (!var2.isRemote) {
                     var2.spawnEntity(var28);
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
}
