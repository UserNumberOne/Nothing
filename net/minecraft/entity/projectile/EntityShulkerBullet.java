package net.minecraft.entity.projectile;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.bukkit.entity.LivingEntity;

public class EntityShulkerBullet extends Entity {
   private EntityLivingBase owner;
   private Entity target;
   @Nullable
   private EnumFacing direction;
   private int steps;
   private double targetDeltaX;
   private double targetDeltaY;
   private double targetDeltaZ;
   @Nullable
   private UUID ownerUniqueId;
   private BlockPos ownerBlockPos;
   @Nullable
   private UUID targetUniqueId;
   private BlockPos targetBlockPos;

   public EntityShulkerBullet(World var1) {
      super(var1);
      this.setSize(0.3125F, 0.3125F);
      this.noClip = true;
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.HOSTILE;
   }

   public EntityShulkerBullet(World var1, EntityLivingBase var2, Entity var3, EnumFacing.Axis var4) {
      this(var1);
      this.owner = var2;
      BlockPos var5 = new BlockPos(var2);
      double var6 = (double)var5.getX() + 0.5D;
      double var8 = (double)var5.getY() + 0.5D;
      double var10 = (double)var5.getZ() + 0.5D;
      this.setLocationAndAngles(var6, var8, var10, this.rotationYaw, this.rotationPitch);
      this.target = var3;
      this.direction = EnumFacing.UP;
      this.selectNextMoveDirection(var4);
      this.projectileSource = (LivingEntity)var2.getBukkitEntity();
   }

   public EntityLivingBase getShooter() {
      return this.owner;
   }

   public void setShooter(EntityLivingBase var1) {
      this.owner = var1;
   }

   public Entity getTarget() {
      return this.target;
   }

   public void setTarget(Entity var1) {
      this.target = var1;
      this.direction = EnumFacing.UP;
      this.selectNextMoveDirection(EnumFacing.Axis.X);
   }

   protected void writeEntityToNBT(NBTTagCompound var1) {
      if (this.owner != null) {
         BlockPos var2 = new BlockPos(this.owner);
         NBTTagCompound var3 = NBTUtil.createUUIDTag(this.owner.getUniqueID());
         var3.setInteger("X", var2.getX());
         var3.setInteger("Y", var2.getY());
         var3.setInteger("Z", var2.getZ());
         var1.setTag("Owner", var3);
      }

      if (this.target != null) {
         BlockPos var4 = new BlockPos(this.target);
         NBTTagCompound var5 = NBTUtil.createUUIDTag(this.target.getUniqueID());
         var5.setInteger("X", var4.getX());
         var5.setInteger("Y", var4.getY());
         var5.setInteger("Z", var4.getZ());
         var1.setTag("Target", var5);
      }

      if (this.direction != null) {
         var1.setInteger("Dir", this.direction.getIndex());
      }

      var1.setInteger("Steps", this.steps);
      var1.setDouble("TXD", this.targetDeltaX);
      var1.setDouble("TYD", this.targetDeltaY);
      var1.setDouble("TZD", this.targetDeltaZ);
   }

   protected void readEntityFromNBT(NBTTagCompound var1) {
      this.steps = var1.getInteger("Steps");
      this.targetDeltaX = var1.getDouble("TXD");
      this.targetDeltaY = var1.getDouble("TYD");
      this.targetDeltaZ = var1.getDouble("TZD");
      if (var1.hasKey("Dir", 99)) {
         this.direction = EnumFacing.getFront(var1.getInteger("Dir"));
      }

      if (var1.hasKey("Owner", 10)) {
         NBTTagCompound var2 = var1.getCompoundTag("Owner");
         this.ownerUniqueId = NBTUtil.getUUIDFromTag(var2);
         this.ownerBlockPos = new BlockPos(var2.getInteger("X"), var2.getInteger("Y"), var2.getInteger("Z"));
      }

      if (var1.hasKey("Target", 10)) {
         NBTTagCompound var3 = var1.getCompoundTag("Target");
         this.targetUniqueId = NBTUtil.getUUIDFromTag(var3);
         this.targetBlockPos = new BlockPos(var3.getInteger("X"), var3.getInteger("Y"), var3.getInteger("Z"));
      }

   }

   protected void entityInit() {
   }

   private void setDirection(@Nullable EnumFacing var1) {
      this.direction = var1;
   }

   private void selectNextMoveDirection(@Nullable EnumFacing.Axis var1) {
      double var2 = 0.5D;
      BlockPos var4;
      if (this.target == null) {
         var4 = (new BlockPos(this)).down();
      } else {
         var2 = (double)this.target.height * 0.5D;
         var4 = new BlockPos(this.target.posX, this.target.posY + var2, this.target.posZ);
      }

      double var5 = (double)var4.getX() + 0.5D;
      double var7 = (double)var4.getY() + var2;
      double var9 = (double)var4.getZ() + 0.5D;
      EnumFacing var11 = null;
      if (var4.distanceSqToCenter(this.posX, this.posY, this.posZ) >= 4.0D) {
         BlockPos var12 = new BlockPos(this);
         ArrayList var13 = Lists.newArrayList();
         if (var1 != EnumFacing.Axis.X) {
            if (var12.getX() < var4.getX() && this.world.isAirBlock(var12.east())) {
               var13.add(EnumFacing.EAST);
            } else if (var12.getX() > var4.getX() && this.world.isAirBlock(var12.west())) {
               var13.add(EnumFacing.WEST);
            }
         }

         if (var1 != EnumFacing.Axis.Y) {
            if (var12.getY() < var4.getY() && this.world.isAirBlock(var12.up())) {
               var13.add(EnumFacing.UP);
            } else if (var12.getY() > var4.getY() && this.world.isAirBlock(var12.down())) {
               var13.add(EnumFacing.DOWN);
            }
         }

         if (var1 != EnumFacing.Axis.Z) {
            if (var12.getZ() < var4.getZ() && this.world.isAirBlock(var12.south())) {
               var13.add(EnumFacing.SOUTH);
            } else if (var12.getZ() > var4.getZ() && this.world.isAirBlock(var12.north())) {
               var13.add(EnumFacing.NORTH);
            }
         }

         var11 = EnumFacing.random(this.rand);
         if (var13.isEmpty()) {
            for(int var14 = 5; !this.world.isAirBlock(var12.offset(var11)) && var14 > 0; --var14) {
               var11 = EnumFacing.random(this.rand);
            }
         } else {
            var11 = (EnumFacing)var13.get(this.rand.nextInt(var13.size()));
         }

         var5 = this.posX + (double)var11.getFrontOffsetX();
         var7 = this.posY + (double)var11.getFrontOffsetY();
         var9 = this.posZ + (double)var11.getFrontOffsetZ();
      }

      this.setDirection(var11);
      double var15 = var5 - this.posX;
      double var17 = var7 - this.posY;
      double var19 = var9 - this.posZ;
      double var21 = (double)MathHelper.sqrt(var15 * var15 + var17 * var17 + var19 * var19);
      if (var21 == 0.0D) {
         this.targetDeltaX = 0.0D;
         this.targetDeltaY = 0.0D;
         this.targetDeltaZ = 0.0D;
      } else {
         this.targetDeltaX = var15 / var21 * 0.15D;
         this.targetDeltaY = var17 / var21 * 0.15D;
         this.targetDeltaZ = var19 / var21 * 0.15D;
      }

      this.isAirBorne = true;
      this.steps = 10 + this.rand.nextInt(5) * 10;
   }

   public void onUpdate() {
      if (!this.world.isRemote && this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
         this.setDead();
      } else {
         super.onUpdate();
         if (!this.world.isRemote) {
            if (this.target == null && this.targetUniqueId != null) {
               for(EntityLivingBase var3 : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.targetBlockPos.add(-2, -2, -2), this.targetBlockPos.add(2, 2, 2)))) {
                  if (var3.getUniqueID().equals(this.targetUniqueId)) {
                     this.target = var3;
                     break;
                  }
               }

               this.targetUniqueId = null;
            }

            if (this.owner == null && this.ownerUniqueId != null) {
               for(EntityLivingBase var9 : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.ownerBlockPos.add(-2, -2, -2), this.ownerBlockPos.add(2, 2, 2)))) {
                  if (var9.getUniqueID().equals(this.ownerUniqueId)) {
                     this.owner = var9;
                     break;
                  }
               }

               this.ownerUniqueId = null;
            }

            if (this.target == null || !this.target.isEntityAlive() || this.target instanceof EntityPlayer && ((EntityPlayer)this.target).isSpectator()) {
               if (!this.hasNoGravity()) {
                  this.motionY -= 0.04D;
               }
            } else {
               this.targetDeltaX = MathHelper.clamp(this.targetDeltaX * 1.025D, -1.0D, 1.0D);
               this.targetDeltaY = MathHelper.clamp(this.targetDeltaY * 1.025D, -1.0D, 1.0D);
               this.targetDeltaZ = MathHelper.clamp(this.targetDeltaZ * 1.025D, -1.0D, 1.0D);
               this.motionX += (this.targetDeltaX - this.motionX) * 0.2D;
               this.motionY += (this.targetDeltaY - this.motionY) * 0.2D;
               this.motionZ += (this.targetDeltaZ - this.motionZ) * 0.2D;
            }

            RayTraceResult var4 = ProjectileHelper.forwardsRaycast(this, true, false, this.owner);
            if (var4 != null) {
               this.bulletHit(var4);
            }
         }

         this.setPosition(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
         ProjectileHelper.rotateTowardsMovement(this, 0.5F);
         if (this.world.isRemote) {
            this.world.spawnParticle(EnumParticleTypes.END_ROD, this.posX - this.motionX, this.posY - this.motionY + 0.15D, this.posZ - this.motionZ, 0.0D, 0.0D, 0.0D);
         } else if (this.target != null && !this.target.isDead) {
            if (this.steps > 0) {
               --this.steps;
               if (this.steps == 0) {
                  this.selectNextMoveDirection(this.direction == null ? null : this.direction.getAxis());
               }
            }

            if (this.direction != null) {
               BlockPos var6 = new BlockPos(this);
               EnumFacing.Axis var8 = this.direction.getAxis();
               if (this.world.isBlockNormalCube(var6.offset(this.direction), false)) {
                  this.selectNextMoveDirection(var8);
               } else {
                  BlockPos var10 = new BlockPos(this.target);
                  if (var8 == EnumFacing.Axis.X && var6.getX() == var10.getX() || var8 == EnumFacing.Axis.Z && var6.getZ() == var10.getZ() || var8 == EnumFacing.Axis.Y && var6.getY() == var10.getY()) {
                     this.selectNextMoveDirection(var8);
                  }
               }
            }
         }
      }

   }

   public boolean isBurning() {
      return false;
   }

   public float getBrightness(float var1) {
      return 1.0F;
   }

   protected void bulletHit(RayTraceResult var1) {
      if (var1.entityHit == null) {
         ((WorldServer)this.world).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 2, 0.2D, 0.2D, 0.2D, 0.0D);
         this.playSound(SoundEvents.ENTITY_SHULKER_BULLET_HIT, 1.0F, 1.0F);
      } else {
         boolean var2 = var1.entityHit.attackEntityFrom(DamageSource.causeIndirectDamage(this, this.owner).setProjectile(), 4.0F);
         if (var2) {
            this.applyEnchantments(this.owner, var1.entityHit);
            if (var1.entityHit instanceof EntityLivingBase) {
               ((EntityLivingBase)var1.entityHit).addPotionEffect(new PotionEffect(MobEffects.LEVITATION, 200));
            }
         }
      }

      this.setDead();
   }

   public boolean canBeCollidedWith() {
      return true;
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (!this.world.isRemote) {
         this.playSound(SoundEvents.ENTITY_SHULKER_BULLET_HURT, 1.0F, 1.0F);
         ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY, this.posZ, 15, 0.2D, 0.2D, 0.2D, 0.0D);
         this.setDead();
      }

      return true;
   }
}
