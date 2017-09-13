package net.minecraft.entity;

import com.google.common.base.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Hanging;
import org.bukkit.event.Event;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;

public abstract class EntityHanging extends Entity {
   private static final Predicate IS_HANGING_ENTITY = new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return var1 instanceof EntityHanging;
      }

      public boolean apply(Object var1) {
         return this.apply((Entity)var1);
      }
   };
   private int tickCounter1;
   public BlockPos hangingPosition;
   @Nullable
   public EnumFacing facingDirection;

   public EntityHanging(World var1) {
      super(var1);
      this.setSize(0.5F, 0.5F);
   }

   public EntityHanging(World var1, BlockPos var2) {
      this(var1);
      this.hangingPosition = var2;
   }

   protected void entityInit() {
   }

   public void updateFacingWithBoundingBox(EnumFacing var1) {
      Validate.notNull(var1);
      Validate.isTrue(var1.getAxis().isHorizontal());
      this.facingDirection = var1;
      this.rotationYaw = (float)(this.facingDirection.getHorizontalIndex() * 90);
      this.prevRotationYaw = this.rotationYaw;
      this.updateBoundingBox();
   }

   public static AxisAlignedBB calculateBoundingBox(Entity var0, BlockPos var1, EnumFacing var2, int var3, int var4) {
      double var5 = (double)var1.getX() + 0.5D;
      double var7 = (double)var1.getY() + 0.5D;
      double var9 = (double)var1.getZ() + 0.5D;
      double var11 = offs(var3);
      double var13 = offs(var4);
      var5 = var5 - (double)var2.getFrontOffsetX() * 0.46875D;
      var9 = var9 - (double)var2.getFrontOffsetZ() * 0.46875D;
      var7 = var7 + var13;
      EnumFacing var15 = var2.rotateYCCW();
      var5 = var5 + var11 * (double)var15.getFrontOffsetX();
      var9 = var9 + var11 * (double)var15.getFrontOffsetZ();
      if (var0 != null) {
         var0.posX = var5;
         var0.posY = var7;
         var0.posZ = var9;
      }

      double var16 = (double)var3;
      double var18 = (double)var4;
      double var20 = (double)var3;
      if (var2.getAxis() == EnumFacing.Axis.Z) {
         var20 = 1.0D;
      } else {
         var16 = 1.0D;
      }

      var16 = var16 / 32.0D;
      var18 = var18 / 32.0D;
      var20 = var20 / 32.0D;
      return new AxisAlignedBB(var5 - var16, var7 - var18, var9 - var20, var5 + var16, var7 + var18, var9 + var20);
   }

   protected void updateBoundingBox() {
      if (this.facingDirection != null) {
         this.setEntityBoundingBox(calculateBoundingBox(this, this.hangingPosition, this.facingDirection, this.getWidthPixels(), this.getHeightPixels()));
      }

   }

   private static double offs(int var0) {
      return var0 % 32 == 0 ? 0.5D : 0.0D;
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (this.tickCounter1++ == 100 && !this.world.isRemote) {
         this.tickCounter1 = 0;
         if (!this.isDead && !this.onValidSurface()) {
            Material var1 = this.world.getBlockState(new BlockPos(this)).getMaterial();
            RemoveCause var2;
            if (!var1.equals(Material.AIR)) {
               var2 = RemoveCause.OBSTRUCTION;
            } else {
               var2 = RemoveCause.PHYSICS;
            }

            HangingBreakEvent var3 = new HangingBreakEvent((Hanging)this.getBukkitEntity(), var2);
            this.world.getServer().getPluginManager().callEvent(var3);
            if (this.isDead || var3.isCancelled()) {
               return;
            }

            this.setDead();
            this.onBroken((Entity)null);
         }
      }

   }

   public boolean onValidSurface() {
      if (!this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty()) {
         return false;
      } else {
         int var1 = Math.max(1, this.getWidthPixels() / 16);
         int var2 = Math.max(1, this.getHeightPixels() / 16);
         BlockPos var3 = this.hangingPosition.offset(this.facingDirection.getOpposite());
         EnumFacing var4 = this.facingDirection.rotateYCCW();
         BlockPos.MutableBlockPos var5 = new BlockPos.MutableBlockPos();

         for(int var6 = 0; var6 < var1; ++var6) {
            for(int var7 = 0; var7 < var2; ++var7) {
               int var8 = (var1 - 1) / -2;
               int var9 = (var2 - 1) / -2;
               var5.setPos(var3).move(var4, var6 + var8).move(EnumFacing.UP, var7 + var9);
               IBlockState var10 = this.world.getBlockState(var5);
               if (!var10.getMaterial().isSolid() && !BlockRedstoneDiode.isDiode(var10)) {
                  return false;
               }
            }
         }

         return this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox(), IS_HANGING_ENTITY).isEmpty();
      }
   }

   public boolean canBeCollidedWith() {
      return true;
   }

   public boolean hitByEntity(Entity var1) {
      return var1 instanceof EntityPlayer ? this.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)var1), 0.0F) : false;
   }

   public EnumFacing getHorizontalFacing() {
      return this.facingDirection;
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isEntityInvulnerable(var1)) {
         return false;
      } else {
         if (!this.isDead && !this.world.isRemote) {
            Object var3 = new HangingBreakEvent((Hanging)this.getBukkitEntity(), RemoveCause.DEFAULT);
            if (var1.getEntity() != null) {
               var3 = new HangingBreakByEntityEvent((Hanging)this.getBukkitEntity(), var1.getEntity() == null ? null : var1.getEntity().getBukkitEntity());
            } else if (var1.isExplosion()) {
               var3 = new HangingBreakEvent((Hanging)this.getBukkitEntity(), RemoveCause.EXPLOSION);
            }

            this.world.getServer().getPluginManager().callEvent((Event)var3);
            if (this.isDead || ((HangingBreakEvent)var3).isCancelled()) {
               return true;
            }

            this.setDead();
            this.setBeenAttacked();
            this.onBroken(var1.getEntity());
         }

         return true;
      }
   }

   public void move(double var1, double var3, double var5) {
      if (!this.world.isRemote && !this.isDead && var1 * var1 + var3 * var3 + var5 * var5 > 0.0D) {
         if (this.isDead) {
            return;
         }

         HangingBreakEvent var7 = new HangingBreakEvent((Hanging)this.getBukkitEntity(), RemoveCause.PHYSICS);
         this.world.getServer().getPluginManager().callEvent(var7);
         if (this.isDead || var7.isCancelled()) {
            return;
         }

         this.setDead();
         this.onBroken((Entity)null);
      }

   }

   public void addVelocity(double var1, double var3, double var5) {
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      var1.setByte("Facing", (byte)this.facingDirection.getHorizontalIndex());
      BlockPos var2 = this.getHangingPosition();
      var1.setInteger("TileX", var2.getX());
      var1.setInteger("TileY", var2.getY());
      var1.setInteger("TileZ", var2.getZ());
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      this.hangingPosition = new BlockPos(var1.getInteger("TileX"), var1.getInteger("TileY"), var1.getInteger("TileZ"));
      this.updateFacingWithBoundingBox(EnumFacing.getHorizontal(var1.getByte("Facing")));
   }

   public abstract int getWidthPixels();

   public abstract int getHeightPixels();

   public abstract void onBroken(@Nullable Entity var1);

   public abstract void playPlaceSound();

   public EntityItem entityDropItem(ItemStack var1, float var2) {
      EntityItem var3 = new EntityItem(this.world, this.posX + (double)((float)this.facingDirection.getFrontOffsetX() * 0.15F), this.posY + (double)var2, this.posZ + (double)((float)this.facingDirection.getFrontOffsetZ() * 0.15F), var1);
      var3.setDefaultPickupDelay();
      this.world.spawnEntity(var3);
      return var3;
   }

   protected boolean shouldSetPosAfterLoading() {
      return false;
   }

   public void setPosition(double var1, double var3, double var5) {
      this.hangingPosition = new BlockPos(var1, var3, var5);
      this.updateBoundingBox();
      this.isAirBorne = true;
   }

   public BlockPos getHangingPosition() {
      return this.hangingPosition;
   }

   public float getRotatedYaw(Rotation var1) {
      if (this.facingDirection != null && this.facingDirection.getAxis() != EnumFacing.Axis.Y) {
         switch(EntityHanging.SyntheticClass_1.a[var1.ordinal()]) {
         case 1:
            this.facingDirection = this.facingDirection.getOpposite();
            break;
         case 2:
            this.facingDirection = this.facingDirection.rotateYCCW();
            break;
         case 3:
            this.facingDirection = this.facingDirection.rotateY();
         }
      }

      float var2 = MathHelper.wrapDegrees(this.rotationYaw);
      switch(EntityHanging.SyntheticClass_1.a[var1.ordinal()]) {
      case 1:
         return var2 + 180.0F;
      case 2:
         return var2 + 90.0F;
      case 3:
         return var2 + 270.0F;
      default:
         return var2;
      }
   }

   public float getMirroredYaw(Mirror var1) {
      return this.getRotatedYaw(var1.toRotation(this.facingDirection));
   }

   public void onStruckByLightning(EntityLightningBolt var1) {
   }

   static class SyntheticClass_1 {
      static final int[] a = new int[Rotation.values().length];

      static {
         try {
            a[Rotation.CLOCKWISE_180.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[Rotation.COUNTERCLOCKWISE_90.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[Rotation.CLOCKWISE_90.ordinal()] = 3;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
