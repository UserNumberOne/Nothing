package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.MathHelper;

public class EntityMoveHelper {
   protected final EntityLiving entity;
   protected double posX;
   protected double posY;
   protected double posZ;
   protected double speed;
   protected float moveForward;
   protected float moveStrafe;
   protected EntityMoveHelper.Action action = EntityMoveHelper.Action.WAIT;

   public EntityMoveHelper(EntityLiving var1) {
      this.entity = var1;
   }

   public boolean isUpdating() {
      return this.action == EntityMoveHelper.Action.MOVE_TO;
   }

   public double getSpeed() {
      return this.speed;
   }

   public void setMoveTo(double var1, double var3, double var5, double var7) {
      this.posX = var1;
      this.posY = var3;
      this.posZ = var5;
      this.speed = var7;
      this.action = EntityMoveHelper.Action.MOVE_TO;
   }

   public void strafe(float var1, float var2) {
      this.action = EntityMoveHelper.Action.STRAFE;
      this.moveForward = var1;
      this.moveStrafe = var2;
      this.speed = 0.25D;
   }

   public void read(EntityMoveHelper var1) {
      this.action = var1.action;
      this.posX = var1.posX;
      this.posY = var1.posY;
      this.posZ = var1.posZ;
      this.speed = Math.max(var1.speed, 1.0D);
      this.moveForward = var1.moveForward;
      this.moveStrafe = var1.moveStrafe;
   }

   public void onUpdateMoveHelper() {
      if (this.action == EntityMoveHelper.Action.STRAFE) {
         float var1 = (float)this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
         float var2 = (float)this.speed * var1;
         float var3 = this.moveForward;
         float var4 = this.moveStrafe;
         float var5 = MathHelper.sqrt(var3 * var3 + var4 * var4);
         if (var5 < 1.0F) {
            var5 = 1.0F;
         }

         var5 = var2 / var5;
         var3 = var3 * var5;
         var4 = var4 * var5;
         float var6 = MathHelper.sin(this.entity.rotationYaw * 0.017453292F);
         float var7 = MathHelper.cos(this.entity.rotationYaw * 0.017453292F);
         float var8 = var3 * var7 - var4 * var6;
         float var9 = var4 * var7 + var3 * var6;
         PathNavigate var10 = this.entity.getNavigator();
         if (var10 != null) {
            NodeProcessor var11 = var10.getNodeProcessor();
            if (var11 != null && var11.getPathNodeType(this.entity.world, MathHelper.floor(this.entity.posX + (double)var8), MathHelper.floor(this.entity.posY), MathHelper.floor(this.entity.posZ + (double)var9)) != PathNodeType.WALKABLE) {
               this.moveForward = 1.0F;
               this.moveStrafe = 0.0F;
               var2 = var1;
            }
         }

         this.entity.setAIMoveSpeed(var2);
         this.entity.setMoveForward(this.moveForward);
         this.entity.setMoveStrafing(this.moveStrafe);
         this.action = EntityMoveHelper.Action.WAIT;
      } else if (this.action == EntityMoveHelper.Action.MOVE_TO) {
         this.action = EntityMoveHelper.Action.WAIT;
         double var12 = this.posX - this.entity.posX;
         double var14 = this.posZ - this.entity.posZ;
         double var17 = this.posY - this.entity.posY;
         double var18 = var12 * var12 + var17 * var17 + var14 * var14;
         if (var18 < 2.500000277905201E-7D) {
            this.entity.setMoveForward(0.0F);
            return;
         }

         float var19 = (float)(MathHelper.atan2(var14, var12) * 57.29577951308232D) - 90.0F;
         this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, var19, 90.0F);
         this.entity.setAIMoveSpeed((float)(this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
         if (var17 > (double)this.entity.stepHeight && var12 * var12 + var14 * var14 < (double)Math.max(1.0F, this.entity.width)) {
            this.entity.getJumpHelper().setJumping();
         }
      } else {
         this.entity.setMoveForward(0.0F);
      }

   }

   protected float limitAngle(float var1, float var2, float var3) {
      float var4 = MathHelper.wrapDegrees(var2 - var1);
      if (var4 > var3) {
         var4 = var3;
      }

      if (var4 < -var3) {
         var4 = -var3;
      }

      float var5 = var1 + var4;
      if (var5 < 0.0F) {
         var5 += 360.0F;
      } else if (var5 > 360.0F) {
         var5 -= 360.0F;
      }

      return var5;
   }

   public double getX() {
      return this.posX;
   }

   public double getY() {
      return this.posY;
   }

   public double getZ() {
      return this.posZ;
   }

   public static enum Action {
      WAIT,
      MOVE_TO,
      STRAFE;
   }
}
