package net.minecraft.entity.boss;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.dragon.phase.IPhase;
import net.minecraft.entity.boss.dragon.phase.PhaseList;
import net.minecraft.entity.boss.dragon.phase.PhaseManager;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathHeap;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.gen.feature.WorldGenEndPodium;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityDragon extends EntityLiving implements IEntityMultiPart, IMob {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final DataParameter PHASE = EntityDataManager.createKey(EntityDragon.class, DataSerializers.VARINT);
   public double[][] ringBuffer = new double[64][3];
   public int ringBufferIndex = -1;
   public EntityDragonPart[] dragonPartArray;
   public EntityDragonPart dragonPartHead = new EntityDragonPart(this, "head", 6.0F, 6.0F);
   public EntityDragonPart dragonPartNeck = new EntityDragonPart(this, "neck", 6.0F, 6.0F);
   public EntityDragonPart dragonPartBody = new EntityDragonPart(this, "body", 8.0F, 8.0F);
   public EntityDragonPart dragonPartTail1 = new EntityDragonPart(this, "tail", 4.0F, 4.0F);
   public EntityDragonPart dragonPartTail2 = new EntityDragonPart(this, "tail", 4.0F, 4.0F);
   public EntityDragonPart dragonPartTail3 = new EntityDragonPart(this, "tail", 4.0F, 4.0F);
   public EntityDragonPart dragonPartWing1 = new EntityDragonPart(this, "wing", 4.0F, 4.0F);
   public EntityDragonPart dragonPartWing2 = new EntityDragonPart(this, "wing", 4.0F, 4.0F);
   public float prevAnimTime;
   public float animTime;
   public boolean slowed;
   public int deathTicks;
   public EntityEnderCrystal healingEnderCrystal;
   private final DragonFightManager fightManager;
   private final PhaseManager phaseManager;
   private int growlTime = 200;
   private int sittingDamageReceived;
   private final PathPoint[] pathPoints = new PathPoint[24];
   private final int[] neighbors = new int[24];
   private final PathHeap pathFindQueue = new PathHeap();

   public EntityDragon(World var1) {
      super(worldIn);
      this.dragonPartArray = new EntityDragonPart[]{this.dragonPartHead, this.dragonPartNeck, this.dragonPartBody, this.dragonPartTail1, this.dragonPartTail2, this.dragonPartTail3, this.dragonPartWing1, this.dragonPartWing2};
      this.setHealth(this.getMaxHealth());
      this.setSize(16.0F, 8.0F);
      this.noClip = true;
      this.isImmuneToFire = true;
      this.growlTime = 100;
      this.ignoreFrustumCheck = true;
      if (!worldIn.isRemote && worldIn.provider instanceof WorldProviderEnd) {
         this.fightManager = ((WorldProviderEnd)worldIn.provider).getDragonFightManager();
      } else {
         this.fightManager = null;
      }

      this.phaseManager = new PhaseManager(this);
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(200.0D);
   }

   protected void entityInit() {
      super.entityInit();
      this.getDataManager().register(PHASE, Integer.valueOf(PhaseList.HOVER.getId()));
   }

   public double[] getMovementOffsets(int var1, float var2) {
      if (this.getHealth() <= 0.0F) {
         p_70974_2_ = 0.0F;
      }

      p_70974_2_ = 1.0F - p_70974_2_;
      int i = this.ringBufferIndex - p_70974_1_ & 63;
      int j = this.ringBufferIndex - p_70974_1_ - 1 & 63;
      double[] adouble = new double[3];
      double d0 = this.ringBuffer[i][0];
      double d1 = MathHelper.wrapDegrees(this.ringBuffer[j][0] - d0);
      adouble[0] = d0 + d1 * (double)p_70974_2_;
      d0 = this.ringBuffer[i][1];
      d1 = this.ringBuffer[j][1] - d0;
      adouble[1] = d0 + d1 * (double)p_70974_2_;
      adouble[2] = this.ringBuffer[i][2] + (this.ringBuffer[j][2] - this.ringBuffer[i][2]) * (double)p_70974_2_;
      return adouble;
   }

   public void onLivingUpdate() {
      if (this.world.isRemote) {
         this.setHealth(this.getHealth());
         if (!this.isSilent()) {
            float f = MathHelper.cos(this.animTime * 6.2831855F);
            float f1 = MathHelper.cos(this.prevAnimTime * 6.2831855F);
            if (f1 <= -0.3F && f >= -0.3F) {
               this.world.playSound(this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERDRAGON_FLAP, this.getSoundCategory(), 5.0F, 0.8F + this.rand.nextFloat() * 0.3F, false);
            }

            if (!this.phaseManager.getCurrentPhase().getIsStationary() && --this.growlTime < 0) {
               this.world.playSound(this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERDRAGON_GROWL, this.getSoundCategory(), 2.5F, 0.8F + this.rand.nextFloat() * 0.3F, false);
               this.growlTime = 200 + this.rand.nextInt(200);
            }
         }
      }

      this.prevAnimTime = this.animTime;
      if (this.getHealth() <= 0.0F) {
         float f13 = (this.rand.nextFloat() - 0.5F) * 8.0F;
         float f15 = (this.rand.nextFloat() - 0.5F) * 4.0F;
         float f17 = (this.rand.nextFloat() - 0.5F) * 8.0F;
         this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX + (double)f13, this.posY + 2.0D + (double)f15, this.posZ + (double)f17, 0.0D, 0.0D, 0.0D);
      } else {
         this.updateDragonEnderCrystal();
         float f12 = 0.2F / (MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ) * 10.0F + 1.0F);
         f12 = f12 * (float)Math.pow(2.0D, this.motionY);
         if (this.phaseManager.getCurrentPhase().getIsStationary()) {
            this.animTime += 0.1F;
         } else if (this.slowed) {
            this.animTime += f12 * 0.5F;
         } else {
            this.animTime += f12;
         }

         this.rotationYaw = MathHelper.wrapDegrees(this.rotationYaw);
         if (this.isAIDisabled()) {
            this.animTime = 0.5F;
         } else {
            if (this.ringBufferIndex < 0) {
               for(int i = 0; i < this.ringBuffer.length; ++i) {
                  this.ringBuffer[i][0] = (double)this.rotationYaw;
                  this.ringBuffer[i][1] = this.posY;
               }
            }

            if (++this.ringBufferIndex == this.ringBuffer.length) {
               this.ringBufferIndex = 0;
            }

            this.ringBuffer[this.ringBufferIndex][0] = (double)this.rotationYaw;
            this.ringBuffer[this.ringBufferIndex][1] = this.posY;
            if (this.world.isRemote) {
               if (this.newPosRotationIncrements > 0) {
                  double d5 = this.posX + (this.interpTargetX - this.posX) / (double)this.newPosRotationIncrements;
                  double d0 = this.posY + (this.interpTargetY - this.posY) / (double)this.newPosRotationIncrements;
                  double d1 = this.posZ + (this.interpTargetZ - this.posZ) / (double)this.newPosRotationIncrements;
                  double d2 = MathHelper.wrapDegrees(this.interpTargetYaw - (double)this.rotationYaw);
                  this.rotationYaw = (float)((double)this.rotationYaw + d2 / (double)this.newPosRotationIncrements);
                  this.rotationPitch = (float)((double)this.rotationPitch + (this.interpTargetPitch - (double)this.rotationPitch) / (double)this.newPosRotationIncrements);
                  --this.newPosRotationIncrements;
                  this.setPosition(d5, d0, d1);
                  this.setRotation(this.rotationYaw, this.rotationPitch);
               }

               this.phaseManager.getCurrentPhase().doClientRenderEffects();
            } else {
               IPhase iphase = this.phaseManager.getCurrentPhase();
               iphase.doLocalUpdate();
               if (this.phaseManager.getCurrentPhase() != iphase) {
                  iphase = this.phaseManager.getCurrentPhase();
                  iphase.doLocalUpdate();
               }

               Vec3d vec3d = iphase.getTargetLocation();
               if (vec3d != null) {
                  double d6 = vec3d.xCoord - this.posX;
                  double d7 = vec3d.yCoord - this.posY;
                  double d8 = vec3d.zCoord - this.posZ;
                  double d3 = d6 * d6 + d7 * d7 + d8 * d8;
                  float f6 = iphase.getMaxRiseOrFall();
                  d7 = MathHelper.clamp(d7 / (double)MathHelper.sqrt(d6 * d6 + d8 * d8), (double)(-f6), (double)f6);
                  this.motionY += d7 * 0.10000000149011612D;
                  this.rotationYaw = MathHelper.wrapDegrees(this.rotationYaw);
                  double d4 = MathHelper.clamp(MathHelper.wrapDegrees(180.0D - MathHelper.atan2(d6, d8) * 57.29577951308232D - (double)this.rotationYaw), -50.0D, 50.0D);
                  Vec3d vec3d1 = (new Vec3d(vec3d.xCoord - this.posX, vec3d.yCoord - this.posY, vec3d.zCoord - this.posZ)).normalize();
                  Vec3d vec3d2 = (new Vec3d((double)MathHelper.sin(this.rotationYaw * 0.017453292F), this.motionY, (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)))).normalize();
                  float f8 = Math.max(((float)vec3d2.dotProduct(vec3d1) + 0.5F) / 1.5F, 0.0F);
                  this.randomYawVelocity *= 0.8F;
                  this.randomYawVelocity = (float)((double)this.randomYawVelocity + d4 * (double)iphase.getYawFactor());
                  this.rotationYaw += this.randomYawVelocity * 0.1F;
                  float f9 = (float)(2.0D / (d3 + 1.0D));
                  float f10 = 0.06F;
                  this.moveRelative(0.0F, -1.0F, 0.06F * (f8 * f9 + (1.0F - f9)));
                  if (this.slowed) {
                     this.move(this.motionX * 0.800000011920929D, this.motionY * 0.800000011920929D, this.motionZ * 0.800000011920929D);
                  } else {
                     this.move(this.motionX, this.motionY, this.motionZ);
                  }

                  Vec3d vec3d3 = (new Vec3d(this.motionX, this.motionY, this.motionZ)).normalize();
                  float f11 = ((float)vec3d3.dotProduct(vec3d2) + 1.0F) / 2.0F;
                  f11 = 0.8F + 0.15F * f11;
                  this.motionX *= (double)f11;
                  this.motionZ *= (double)f11;
                  this.motionY *= 0.9100000262260437D;
               }
            }

            this.renderYawOffset = this.rotationYaw;
            this.dragonPartHead.width = 1.0F;
            this.dragonPartHead.height = 1.0F;
            this.dragonPartNeck.width = 3.0F;
            this.dragonPartNeck.height = 3.0F;
            this.dragonPartTail1.width = 2.0F;
            this.dragonPartTail1.height = 2.0F;
            this.dragonPartTail2.width = 2.0F;
            this.dragonPartTail2.height = 2.0F;
            this.dragonPartTail3.width = 2.0F;
            this.dragonPartTail3.height = 2.0F;
            this.dragonPartBody.height = 3.0F;
            this.dragonPartBody.width = 5.0F;
            this.dragonPartWing1.height = 2.0F;
            this.dragonPartWing1.width = 4.0F;
            this.dragonPartWing2.height = 3.0F;
            this.dragonPartWing2.width = 4.0F;
            float f14 = (float)(this.getMovementOffsets(5, 1.0F)[1] - this.getMovementOffsets(10, 1.0F)[1]) * 10.0F * 0.017453292F;
            float f16 = MathHelper.cos(f14);
            float f18 = MathHelper.sin(f14);
            float f2 = this.rotationYaw * 0.017453292F;
            float f19 = MathHelper.sin(f2);
            float f3 = MathHelper.cos(f2);
            this.dragonPartBody.onUpdate();
            this.dragonPartBody.setLocationAndAngles(this.posX + (double)(f19 * 0.5F), this.posY, this.posZ - (double)(f3 * 0.5F), 0.0F, 0.0F);
            this.dragonPartWing1.onUpdate();
            this.dragonPartWing1.setLocationAndAngles(this.posX + (double)(f3 * 4.5F), this.posY + 2.0D, this.posZ + (double)(f19 * 4.5F), 0.0F, 0.0F);
            this.dragonPartWing2.onUpdate();
            this.dragonPartWing2.setLocationAndAngles(this.posX - (double)(f3 * 4.5F), this.posY + 2.0D, this.posZ - (double)(f19 * 4.5F), 0.0F, 0.0F);
            if (!this.world.isRemote && this.hurtTime == 0) {
               this.collideWithEntities(this.world.getEntitiesWithinAABBExcludingEntity(this, this.dragonPartWing1.getEntityBoundingBox().expand(4.0D, 2.0D, 4.0D).offset(0.0D, -2.0D, 0.0D)));
               this.collideWithEntities(this.world.getEntitiesWithinAABBExcludingEntity(this, this.dragonPartWing2.getEntityBoundingBox().expand(4.0D, 2.0D, 4.0D).offset(0.0D, -2.0D, 0.0D)));
               this.attackEntitiesInList(this.world.getEntitiesWithinAABBExcludingEntity(this, this.dragonPartHead.getEntityBoundingBox().expandXyz(1.0D)));
               this.attackEntitiesInList(this.world.getEntitiesWithinAABBExcludingEntity(this, this.dragonPartNeck.getEntityBoundingBox().expandXyz(1.0D)));
            }

            double[] adouble = this.getMovementOffsets(5, 1.0F);
            float f4 = MathHelper.sin(this.rotationYaw * 0.017453292F - this.randomYawVelocity * 0.01F);
            float f20 = MathHelper.cos(this.rotationYaw * 0.017453292F - this.randomYawVelocity * 0.01F);
            this.dragonPartHead.onUpdate();
            this.dragonPartNeck.onUpdate();
            float f5 = this.getHeadYOffset(1.0F);
            this.dragonPartHead.setLocationAndAngles(this.posX + (double)(f4 * 6.5F * f16), this.posY + (double)f5 + (double)(f18 * 6.5F), this.posZ - (double)(f20 * 6.5F * f16), 0.0F, 0.0F);
            this.dragonPartNeck.setLocationAndAngles(this.posX + (double)(f4 * 5.5F * f16), this.posY + (double)f5 + (double)(f18 * 5.5F), this.posZ - (double)(f20 * 5.5F * f16), 0.0F, 0.0F);

            for(int j = 0; j < 3; ++j) {
               EntityDragonPart entitydragonpart = null;
               if (j == 0) {
                  entitydragonpart = this.dragonPartTail1;
               }

               if (j == 1) {
                  entitydragonpart = this.dragonPartTail2;
               }

               if (j == 2) {
                  entitydragonpart = this.dragonPartTail3;
               }

               double[] adouble1 = this.getMovementOffsets(12 + j * 2, 1.0F);
               float f21 = this.rotationYaw * 0.017453292F + this.simplifyAngle(adouble1[0] - adouble[0]) * 0.017453292F;
               float f22 = MathHelper.sin(f21);
               float f7 = MathHelper.cos(f21);
               float f23 = 1.5F;
               float f24 = (float)(j + 1) * 2.0F;
               entitydragonpart.onUpdate();
               entitydragonpart.setLocationAndAngles(this.posX - (double)((f19 * 1.5F + f22 * f24) * f16), this.posY + (adouble1[1] - adouble[1]) - (double)((f24 + 1.5F) * f18) + 1.5D, this.posZ + (double)((f3 * 1.5F + f7 * f24) * f16), 0.0F, 0.0F);
            }

            if (!this.world.isRemote) {
               this.slowed = this.destroyBlocksInAABB(this.dragonPartHead.getEntityBoundingBox()) | this.destroyBlocksInAABB(this.dragonPartNeck.getEntityBoundingBox()) | this.destroyBlocksInAABB(this.dragonPartBody.getEntityBoundingBox());
               if (this.fightManager != null) {
                  this.fightManager.dragonUpdate(this);
               }
            }
         }
      }

   }

   private float getHeadYOffset(float var1) {
      double d0;
      if (this.phaseManager.getCurrentPhase().getIsStationary()) {
         d0 = -1.0D;
      } else {
         double[] adouble = this.getMovementOffsets(5, 1.0F);
         double[] adouble1 = this.getMovementOffsets(0, 1.0F);
         d0 = adouble[1] - adouble1[0];
      }

      return (float)d0;
   }

   private void updateDragonEnderCrystal() {
      if (this.healingEnderCrystal != null) {
         if (this.healingEnderCrystal.isDead) {
            this.healingEnderCrystal = null;
         } else if (this.ticksExisted % 10 == 0 && this.getHealth() < this.getMaxHealth()) {
            this.setHealth(this.getHealth() + 1.0F);
         }
      }

      if (this.rand.nextInt(10) == 0) {
         List list = this.world.getEntitiesWithinAABB(EntityEnderCrystal.class, this.getEntityBoundingBox().expandXyz(32.0D));
         EntityEnderCrystal entityendercrystal = null;
         double d0 = Double.MAX_VALUE;

         for(EntityEnderCrystal entityendercrystal1 : list) {
            double d1 = entityendercrystal1.getDistanceSqToEntity(this);
            if (d1 < d0) {
               d0 = d1;
               entityendercrystal = entityendercrystal1;
            }
         }

         this.healingEnderCrystal = entityendercrystal;
      }

   }

   private void collideWithEntities(List var1) {
      double d0 = (this.dragonPartBody.getEntityBoundingBox().minX + this.dragonPartBody.getEntityBoundingBox().maxX) / 2.0D;
      double d1 = (this.dragonPartBody.getEntityBoundingBox().minZ + this.dragonPartBody.getEntityBoundingBox().maxZ) / 2.0D;

      for(Entity entity : p_70970_1_) {
         if (entity instanceof EntityLivingBase) {
            double d2 = entity.posX - d0;
            double d3 = entity.posZ - d1;
            double d4 = d2 * d2 + d3 * d3;
            entity.addVelocity(d2 / d4 * 4.0D, 0.20000000298023224D, d3 / d4 * 4.0D);
            if (!this.phaseManager.getCurrentPhase().getIsStationary() && ((EntityLivingBase)entity).getRevengeTimer() < entity.ticksExisted - 2) {
               entity.attackEntityFrom(DamageSource.causeMobDamage(this), 5.0F);
               this.applyEnchantments(this, entity);
            }
         }
      }

   }

   private void attackEntitiesInList(List var1) {
      for(int i = 0; i < p_70971_1_.size(); ++i) {
         Entity entity = (Entity)p_70971_1_.get(i);
         if (entity instanceof EntityLivingBase) {
            entity.attackEntityFrom(DamageSource.causeMobDamage(this), 10.0F);
            this.applyEnchantments(this, entity);
         }
      }

   }

   private float simplifyAngle(double var1) {
      return (float)MathHelper.wrapDegrees(p_70973_1_);
   }

   private boolean destroyBlocksInAABB(AxisAlignedBB var1) {
      int i = MathHelper.floor(p_70972_1_.minX);
      int j = MathHelper.floor(p_70972_1_.minY);
      int k = MathHelper.floor(p_70972_1_.minZ);
      int l = MathHelper.floor(p_70972_1_.maxX);
      int i1 = MathHelper.floor(p_70972_1_.maxY);
      int j1 = MathHelper.floor(p_70972_1_.maxZ);
      boolean flag = false;
      boolean flag1 = false;

      for(int k1 = i; k1 <= l; ++k1) {
         for(int l1 = j; l1 <= i1; ++l1) {
            for(int i2 = k; i2 <= j1; ++i2) {
               BlockPos blockpos = new BlockPos(k1, l1, i2);
               IBlockState iblockstate = this.world.getBlockState(blockpos);
               Block block = iblockstate.getBlock();
               if (!block.isAir(iblockstate, this.world, blockpos) && iblockstate.getMaterial() != Material.FIRE) {
                  if (!this.world.getGameRules().getBoolean("mobGriefing")) {
                     flag = true;
                  } else if (!block.canEntityDestroy(iblockstate, this.world, blockpos, this)) {
                     flag = true;
                  } else if (block != Blocks.COMMAND_BLOCK && block != Blocks.REPEATING_COMMAND_BLOCK && block != Blocks.CHAIN_COMMAND_BLOCK && block != Blocks.IRON_BARS && block != Blocks.END_GATEWAY) {
                     flag1 = this.world.setBlockToAir(blockpos) || flag1;
                  } else {
                     flag = true;
                  }
               }
            }
         }
      }

      if (flag1) {
         double d0 = p_70972_1_.minX + (p_70972_1_.maxX - p_70972_1_.minX) * (double)this.rand.nextFloat();
         double d1 = p_70972_1_.minY + (p_70972_1_.maxY - p_70972_1_.minY) * (double)this.rand.nextFloat();
         double d2 = p_70972_1_.minZ + (p_70972_1_.maxZ - p_70972_1_.minZ) * (double)this.rand.nextFloat();
         this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
      }

      return flag;
   }

   public boolean attackEntityFromPart(EntityDragonPart var1, DamageSource var2, float var3) {
      damage = this.phaseManager.getCurrentPhase().getAdjustedDamage(dragonPart, source, damage);
      if (dragonPart != this.dragonPartHead) {
         damage = damage / 4.0F + Math.min(damage, 1.0F);
      }

      if (damage < 0.01F) {
         return false;
      } else {
         if (source.getEntity() instanceof EntityPlayer || source.isExplosion()) {
            float f = this.getHealth();
            this.attackDragonFrom(source, damage);
            if (this.getHealth() <= 0.0F && !this.phaseManager.getCurrentPhase().getIsStationary()) {
               this.setHealth(1.0F);
               this.phaseManager.setPhase(PhaseList.DYING);
            }

            if (this.phaseManager.getCurrentPhase().getIsStationary()) {
               this.sittingDamageReceived = (int)((float)this.sittingDamageReceived + (f - this.getHealth()));
               if ((float)this.sittingDamageReceived > 0.25F * this.getMaxHealth()) {
                  this.sittingDamageReceived = 0;
                  this.phaseManager.setPhase(PhaseList.TAKEOFF);
               }
            }
         }

         return true;
      }
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (source instanceof EntityDamageSource && ((EntityDamageSource)source).getIsThornsDamage()) {
         this.attackEntityFromPart(this.dragonPartBody, source, amount);
      }

      return false;
   }

   protected boolean attackDragonFrom(DamageSource var1, float var2) {
      return super.attackEntityFrom(source, amount);
   }

   public void onKillCommand() {
      this.setDead();
      if (this.fightManager != null) {
         this.fightManager.dragonUpdate(this);
         this.fightManager.processDragonDeath(this);
      }

   }

   protected void onDeathUpdate() {
      if (this.fightManager != null) {
         this.fightManager.dragonUpdate(this);
      }

      ++this.deathTicks;
      if (this.deathTicks >= 180 && this.deathTicks <= 200) {
         float f = (this.rand.nextFloat() - 0.5F) * 8.0F;
         float f1 = (this.rand.nextFloat() - 0.5F) * 4.0F;
         float f2 = (this.rand.nextFloat() - 0.5F) * 8.0F;
         this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX + (double)f, this.posY + 2.0D + (double)f1, this.posZ + (double)f2, 0.0D, 0.0D, 0.0D);
      }

      boolean flag = this.world.getGameRules().getBoolean("doMobLoot");
      int i = 500;
      if (this.fightManager != null && !this.fightManager.hasPreviouslyKilledDragon()) {
         i = 12000;
      }

      if (!this.world.isRemote) {
         if (this.deathTicks > 150 && this.deathTicks % 5 == 0 && flag) {
            this.dropExperience(MathHelper.floor((float)i * 0.08F));
         }

         if (this.deathTicks == 1) {
            this.world.playBroadcastSound(1028, new BlockPos(this), 0);
         }
      }

      this.move(0.0D, 0.10000000149011612D, 0.0D);
      this.rotationYaw += 20.0F;
      this.renderYawOffset = this.rotationYaw;
      if (this.deathTicks == 200 && !this.world.isRemote) {
         if (flag) {
            this.dropExperience(MathHelper.floor((float)i * 0.2F));
         }

         if (this.fightManager != null) {
            this.fightManager.processDragonDeath(this);
         }

         this.setDead();
      }

   }

   private void dropExperience(int var1) {
      while(p_184668_1_ > 0) {
         int i = EntityXPOrb.getXPSplit(p_184668_1_);
         p_184668_1_ -= i;
         this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY, this.posZ, i));
      }

   }

   public int initPathPoints() {
      if (this.pathPoints[0] == null) {
         for(int i = 0; i < 24; ++i) {
            int j = 5;
            int l;
            int i1;
            if (i < 12) {
               l = (int)(60.0F * MathHelper.cos(2.0F * (-3.1415927F + 0.2617994F * (float)i)));
               i1 = (int)(60.0F * MathHelper.sin(2.0F * (-3.1415927F + 0.2617994F * (float)i)));
            } else if (i < 20) {
               int lvt_3_1_ = i - 12;
               l = (int)(40.0F * MathHelper.cos(2.0F * (-3.1415927F + 0.3926991F * (float)lvt_3_1_)));
               i1 = (int)(40.0F * MathHelper.sin(2.0F * (-3.1415927F + 0.3926991F * (float)lvt_3_1_)));
               j += 10;
            } else {
               int k1 = i - 20;
               l = (int)(20.0F * MathHelper.cos(2.0F * (-3.1415927F + 0.7853982F * (float)k1)));
               i1 = (int)(20.0F * MathHelper.sin(2.0F * (-3.1415927F + 0.7853982F * (float)k1)));
            }

            int j1 = Math.max(this.world.getSeaLevel() + 10, this.world.getTopSolidOrLiquidBlock(new BlockPos(l, 0, i1)).getY() + j);
            this.pathPoints[i] = new PathPoint(l, j1, i1);
         }

         this.neighbors[0] = 6146;
         this.neighbors[1] = 8197;
         this.neighbors[2] = 8202;
         this.neighbors[3] = 16404;
         this.neighbors[4] = 32808;
         this.neighbors[5] = 32848;
         this.neighbors[6] = 65696;
         this.neighbors[7] = 131392;
         this.neighbors[8] = 131712;
         this.neighbors[9] = 263424;
         this.neighbors[10] = 526848;
         this.neighbors[11] = 525313;
         this.neighbors[12] = 1581057;
         this.neighbors[13] = 3166214;
         this.neighbors[14] = 2138120;
         this.neighbors[15] = 6373424;
         this.neighbors[16] = 4358208;
         this.neighbors[17] = 12910976;
         this.neighbors[18] = 9044480;
         this.neighbors[19] = 9706496;
         this.neighbors[20] = 15216640;
         this.neighbors[21] = 13688832;
         this.neighbors[22] = 11763712;
         this.neighbors[23] = 8257536;
      }

      return this.getNearestPpIdx(this.posX, this.posY, this.posZ);
   }

   public int getNearestPpIdx(double var1, double var3, double var5) {
      float f = 10000.0F;
      int i = 0;
      PathPoint pathpoint = new PathPoint(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
      int j = 0;
      if (this.fightManager == null || this.fightManager.getNumAliveCrystals() == 0) {
         j = 12;
      }

      for(int k = j; k < 24; ++k) {
         if (this.pathPoints[k] != null) {
            float f1 = this.pathPoints[k].distanceToSquared(pathpoint);
            if (f1 < f) {
               f = f1;
               i = k;
            }
         }
      }

      return i;
   }

   @Nullable
   public Path findPath(int var1, int var2, @Nullable PathPoint var3) {
      for(int i = 0; i < 24; ++i) {
         PathPoint pathpoint = this.pathPoints[i];
         pathpoint.visited = false;
         pathpoint.distanceToTarget = 0.0F;
         pathpoint.totalPathDistance = 0.0F;
         pathpoint.distanceToNext = 0.0F;
         pathpoint.previous = null;
         pathpoint.index = -1;
      }

      PathPoint pathpoint4 = this.pathPoints[startIdx];
      PathPoint pathpoint5 = this.pathPoints[finishIdx];
      pathpoint4.totalPathDistance = 0.0F;
      pathpoint4.distanceToNext = pathpoint4.distanceTo(pathpoint5);
      pathpoint4.distanceToTarget = pathpoint4.distanceToNext;
      this.pathFindQueue.clearPath();
      this.pathFindQueue.addPoint(pathpoint4);
      PathPoint pathpoint1 = pathpoint4;
      int j = 0;
      if (this.fightManager == null || this.fightManager.getNumAliveCrystals() == 0) {
         j = 12;
      }

      while(!this.pathFindQueue.isPathEmpty()) {
         PathPoint pathpoint2 = this.pathFindQueue.dequeue();
         if (pathpoint2.equals(pathpoint5)) {
            if (andThen != null) {
               andThen.previous = pathpoint5;
               pathpoint5 = andThen;
            }

            return this.makePath(pathpoint4, pathpoint5);
         }

         if (pathpoint2.distanceTo(pathpoint5) < pathpoint1.distanceTo(pathpoint5)) {
            pathpoint1 = pathpoint2;
         }

         pathpoint2.visited = true;
         int k = 0;

         for(int l = 0; l < 24; ++l) {
            if (this.pathPoints[l] == pathpoint2) {
               k = l;
               break;
            }
         }

         for(int i1 = j; i1 < 24; ++i1) {
            if ((this.neighbors[k] & 1 << i1) > 0) {
               PathPoint pathpoint3 = this.pathPoints[i1];
               if (!pathpoint3.visited) {
                  float f = pathpoint2.totalPathDistance + pathpoint2.distanceTo(pathpoint3);
                  if (!pathpoint3.isAssigned() || f < pathpoint3.totalPathDistance) {
                     pathpoint3.previous = pathpoint2;
                     pathpoint3.totalPathDistance = f;
                     pathpoint3.distanceToNext = pathpoint3.distanceTo(pathpoint5);
                     if (pathpoint3.isAssigned()) {
                        this.pathFindQueue.changeDistance(pathpoint3, pathpoint3.totalPathDistance + pathpoint3.distanceToNext);
                     } else {
                        pathpoint3.distanceToTarget = pathpoint3.totalPathDistance + pathpoint3.distanceToNext;
                        this.pathFindQueue.addPoint(pathpoint3);
                     }
                  }
               }
            }
         }
      }

      if (pathpoint1 == pathpoint4) {
         return null;
      } else {
         LOGGER.debug("Failed to find path from {} to {}", new Object[]{startIdx, finishIdx});
         if (andThen != null) {
            andThen.previous = pathpoint1;
            pathpoint1 = andThen;
         }

         return this.makePath(pathpoint4, pathpoint1);
      }
   }

   private Path makePath(PathPoint var1, PathPoint var2) {
      int i = 1;

      for(PathPoint pathpoint = finish; pathpoint.previous != null; pathpoint = pathpoint.previous) {
         ++i;
      }

      PathPoint[] apathpoint = new PathPoint[i];
      PathPoint pathpoint1 = finish;
      --i;

      for(apathpoint[i] = finish; pathpoint1.previous != null; apathpoint[i] = pathpoint1) {
         pathpoint1 = pathpoint1.previous;
         --i;
      }

      return new Path(apathpoint);
   }

   public static void registerFixesDragon(DataFixer var0) {
      EntityLiving.registerFixesMob(fixer, "EnderDragon");
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(compound);
      compound.setInteger("DragonPhase", this.phaseManager.getCurrentPhase().getPhaseList().getId());
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(compound);
      if (compound.hasKey("DragonPhase")) {
         this.phaseManager.setPhase(PhaseList.getById(compound.getInteger("DragonPhase")));
      }

   }

   protected void despawnEntity() {
   }

   public Entity[] getParts() {
      return this.dragonPartArray;
   }

   public boolean canBeCollidedWith() {
      return false;
   }

   public World getWorld() {
      return this.world;
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.HOSTILE;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_ENDERDRAGON_AMBIENT;
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_ENDERDRAGON_HURT;
   }

   protected float getSoundVolume() {
      return 5.0F;
   }

   @SideOnly(Side.CLIENT)
   public float getHeadPartYOffset(int var1, double[] var2, double[] var3) {
      IPhase iphase = this.phaseManager.getCurrentPhase();
      PhaseList phaselist = iphase.getPhaseList();
      double d0;
      if (phaselist != PhaseList.LANDING && phaselist != PhaseList.TAKEOFF) {
         if (iphase.getIsStationary()) {
            d0 = (double)p_184667_1_;
         } else if (p_184667_1_ == 6) {
            d0 = 0.0D;
         } else {
            d0 = p_184667_3_[1] - p_184667_2_[1];
         }
      } else {
         BlockPos blockpos = this.world.getTopSolidOrLiquidBlock(WorldGenEndPodium.END_PODIUM_LOCATION);
         float f = Math.max(MathHelper.sqrt(this.getDistanceSqToCenter(blockpos)) / 4.0F, 1.0F);
         d0 = (double)((float)p_184667_1_ / f);
      }

      return (float)d0;
   }

   public Vec3d getHeadLookVec(float var1) {
      IPhase iphase = this.phaseManager.getCurrentPhase();
      PhaseList phaselist = iphase.getPhaseList();
      Vec3d vec3d;
      if (phaselist != PhaseList.LANDING && phaselist != PhaseList.TAKEOFF) {
         if (iphase.getIsStationary()) {
            float f4 = this.rotationPitch;
            float f5 = 1.5F;
            this.rotationPitch = -45.0F;
            vec3d = this.getLook(p_184665_1_);
            this.rotationPitch = f4;
         } else {
            vec3d = this.getLook(p_184665_1_);
         }
      } else {
         BlockPos blockpos = this.world.getTopSolidOrLiquidBlock(WorldGenEndPodium.END_PODIUM_LOCATION);
         float f = Math.max(MathHelper.sqrt(this.getDistanceSqToCenter(blockpos)) / 4.0F, 1.0F);
         float f1 = 6.0F / f;
         float f2 = this.rotationPitch;
         float f3 = 1.5F;
         this.rotationPitch = -f1 * 1.5F * 5.0F;
         vec3d = this.getLook(p_184665_1_);
         this.rotationPitch = f2;
      }

      return vec3d;
   }

   public void onCrystalDestroyed(EntityEnderCrystal var1, BlockPos var2, DamageSource var3) {
      EntityPlayer entityplayer;
      if (dmgSrc.getEntity() instanceof EntityPlayer) {
         entityplayer = (EntityPlayer)dmgSrc.getEntity();
      } else {
         entityplayer = this.world.getNearestAttackablePlayer(pos, 64.0D, 64.0D);
      }

      if (crystal == this.healingEnderCrystal) {
         this.attackEntityFromPart(this.dragonPartHead, DamageSource.causeExplosionDamage(entityplayer), 10.0F);
      }

      this.phaseManager.getCurrentPhase().onCrystalDestroyed(crystal, pos, dmgSrc, entityplayer);
   }

   public void notifyDataManagerChange(DataParameter var1) {
      if (PHASE.equals(key) && this.world.isRemote) {
         this.phaseManager.setPhase(PhaseList.getById(((Integer)this.getDataManager().get(PHASE)).intValue()));
      }

      super.notifyDataManagerChange(key);
   }

   public PhaseManager getPhaseManager() {
      return this.phaseManager;
   }

   @Nullable
   public DragonFightManager getFightManager() {
      return this.fightManager;
   }

   public void addPotionEffect(PotionEffect var1) {
   }

   protected boolean canBeRidden(Entity var1) {
      return false;
   }

   public boolean isNonBoss() {
      return false;
   }
}
