package net.minecraft.entity;

import com.google.common.base.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.BlockRedstoneDiode;
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

public abstract class EntityHanging extends Entity {
   private static final Predicate IS_HANGING_ENTITY = new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return var1 instanceof EntityHanging;
      }
   };
   private int tickCounter1;
   protected BlockPos hangingPosition;
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

   protected void updateFacingWithBoundingBox(EnumFacing var1) {
      Validate.notNull(var1);
      Validate.isTrue(var1.getAxis().isHorizontal());
      this.facingDirection = var1;
      this.rotationYaw = (float)(this.facingDirection.getHorizontalIndex() * 90);
      this.prevRotationYaw = this.rotationYaw;
      this.updateBoundingBox();
   }

   protected void updateBoundingBox() {
      if (this.facingDirection != null) {
         double var1 = (double)this.hangingPosition.getX() + 0.5D;
         double var3 = (double)this.hangingPosition.getY() + 0.5D;
         double var5 = (double)this.hangingPosition.getZ() + 0.5D;
         double var7 = 0.46875D;
         double var9 = this.offs(this.getWidthPixels());
         double var11 = this.offs(this.getHeightPixels());
         var1 = var1 - (double)this.facingDirection.getFrontOffsetX() * 0.46875D;
         var5 = var5 - (double)this.facingDirection.getFrontOffsetZ() * 0.46875D;
         var3 = var3 + var11;
         EnumFacing var13 = this.facingDirection.rotateYCCW();
         var1 = var1 + var9 * (double)var13.getFrontOffsetX();
         var5 = var5 + var9 * (double)var13.getFrontOffsetZ();
         this.posX = var1;
         this.posY = var3;
         this.posZ = var5;
         double var14 = (double)this.getWidthPixels();
         double var16 = (double)this.getHeightPixels();
         double var18 = (double)this.getWidthPixels();
         if (this.facingDirection.getAxis() == EnumFacing.Axis.Z) {
            var18 = 1.0D;
         } else {
            var14 = 1.0D;
         }

         var14 = var14 / 32.0D;
         var16 = var16 / 32.0D;
         var18 = var18 / 32.0D;
         this.setEntityBoundingBox(new AxisAlignedBB(var1 - var14, var3 - var16, var5 - var18, var1 + var14, var3 + var16, var5 + var18));
      }

   }

   private double offs(int var1) {
      return var1 % 32 == 0 ? 0.5D : 0.0D;
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (this.tickCounter1++ == 100 && !this.world.isRemote) {
         this.tickCounter1 = 0;
         if (!this.isDead && !this.onValidSurface()) {
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
               if (!var10.isSideSolid(this.world, var5, this.facingDirection) && !var10.getMaterial().isSolid() && !BlockRedstoneDiode.isDiode(var10)) {
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
            this.setDead();
            this.setBeenAttacked();
            this.onBroken(var1.getEntity());
         }

         return true;
      }
   }

   public void move(double var1, double var3, double var5) {
      if (!this.world.isRemote && !this.isDead && var1 * var1 + var3 * var3 + var5 * var5 > 0.0D) {
         this.setDead();
         this.onBroken((Entity)null);
      }

   }

   public void addVelocity(double var1, double var3, double var5) {
      if (!this.world.isRemote && !this.isDead && var1 * var1 + var3 * var3 + var5 * var5 > 0.0D) {
         this.setDead();
         this.onBroken((Entity)null);
      }

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
         switch(var1) {
         case CLOCKWISE_180:
            this.facingDirection = this.facingDirection.getOpposite();
            break;
         case COUNTERCLOCKWISE_90:
            this.facingDirection = this.facingDirection.rotateYCCW();
            break;
         case CLOCKWISE_90:
            this.facingDirection = this.facingDirection.rotateY();
         }
      }

      float var2 = MathHelper.wrapDegrees(this.rotationYaw);
      switch(var1) {
      case CLOCKWISE_180:
         return var2 + 180.0F;
      case COUNTERCLOCKWISE_90:
         return var2 + 90.0F;
      case CLOCKWISE_90:
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
}
