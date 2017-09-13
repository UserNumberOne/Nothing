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

   public ItemBoat(EntityBoat.Type entityboat_enumboattype) {
      this.type = entityboat_enumboattype;
      this.maxStackSize = 1;
      this.setCreativeTab(CreativeTabs.TRANSPORTATION);
      this.setUnlocalizedName("boat." + entityboat_enumboattype.getName());
   }

   public ActionResult onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityhuman, EnumHand enumhand) {
      float f1 = entityhuman.prevRotationPitch + (entityhuman.rotationPitch - entityhuman.prevRotationPitch) * 1.0F;
      float f2 = entityhuman.prevRotationYaw + (entityhuman.rotationYaw - entityhuman.prevRotationYaw) * 1.0F;
      double d0 = entityhuman.prevPosX + (entityhuman.posX - entityhuman.prevPosX) * 1.0D;
      double d1 = entityhuman.prevPosY + (entityhuman.posY - entityhuman.prevPosY) * 1.0D + (double)entityhuman.getEyeHeight();
      double d2 = entityhuman.prevPosZ + (entityhuman.posZ - entityhuman.prevPosZ) * 1.0D;
      Vec3d vec3d = new Vec3d(d0, d1, d2);
      float f3 = MathHelper.cos(-f2 * 0.017453292F - 3.1415927F);
      float f4 = MathHelper.sin(-f2 * 0.017453292F - 3.1415927F);
      float f5 = -MathHelper.cos(-f1 * 0.017453292F);
      float f6 = MathHelper.sin(-f1 * 0.017453292F);
      float f7 = f4 * f5;
      float f8 = f3 * f5;
      Vec3d vec3d1 = vec3d.addVector((double)f7 * 5.0D, (double)f6 * 5.0D, (double)f8 * 5.0D);
      RayTraceResult movingobjectposition = world.rayTraceBlocks(vec3d, vec3d1, true);
      if (movingobjectposition == null) {
         return new ActionResult(EnumActionResult.PASS, itemstack);
      } else {
         Vec3d vec3d2 = entityhuman.getLook(1.0F);
         boolean flag = false;
         List list = world.getEntitiesWithinAABBExcludingEntity(entityhuman, entityhuman.getEntityBoundingBox().addCoord(vec3d2.xCoord * 5.0D, vec3d2.yCoord * 5.0D, vec3d2.zCoord * 5.0D).expandXyz(1.0D));

         for(int i = 0; i < list.size(); ++i) {
            Entity entity = (Entity)list.get(i);
            if (entity.canBeCollidedWith()) {
               AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().expandXyz((double)entity.getCollisionBorderSize());
               if (axisalignedbb.isVecInside(vec3d)) {
                  flag = true;
               }
            }
         }

         if (flag) {
            return new ActionResult(EnumActionResult.PASS, itemstack);
         } else if (movingobjectposition.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult(EnumActionResult.PASS, itemstack);
         } else {
            PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(entityhuman, Action.RIGHT_CLICK_BLOCK, movingobjectposition.getBlockPos(), movingobjectposition.sideHit, itemstack, enumhand);
            if (event.isCancelled()) {
               return new ActionResult(EnumActionResult.PASS, itemstack);
            } else {
               Block block = world.getBlockState(movingobjectposition.getBlockPos()).getBlock();
               boolean flag1 = block == Blocks.WATER || block == Blocks.FLOWING_WATER;
               EntityBoat entityboat = new EntityBoat(world, movingobjectposition.hitVec.xCoord, flag1 ? movingobjectposition.hitVec.yCoord - 0.12D : movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
               entityboat.setBoatType(this.type);
               entityboat.rotationYaw = entityhuman.rotationYaw;
               if (!world.getCollisionBoxes(entityboat, entityboat.getEntityBoundingBox().expandXyz(-0.1D)).isEmpty()) {
                  return new ActionResult(EnumActionResult.FAIL, itemstack);
               } else {
                  if (!world.isRemote) {
                     world.spawnEntity(entityboat);
                  }

                  if (!entityhuman.capabilities.isCreativeMode) {
                     --itemstack.stackSize;
                  }

                  entityhuman.addStat(StatList.getObjectUseStats(this));
                  return new ActionResult(EnumActionResult.SUCCESS, itemstack);
               }
            }
         }
      }
   }
}
