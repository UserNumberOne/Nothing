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
      super(var1);
      this.dragonPartArray = new EntityDragonPart[]{this.dragonPartHead, this.dragonPartNeck, this.dragonPartBody, this.dragonPartTail1, this.dragonPartTail2, this.dragonPartTail3, this.dragonPartWing1, this.dragonPartWing2};
      this.setHealth(this.getMaxHealth());
      this.setSize(16.0F, 8.0F);
      this.noClip = true;
      this.isImmuneToFire = true;
      this.growlTime = 100;
      this.ignoreFrustumCheck = true;
      if (!var1.isRemote && var1.provider instanceof WorldProviderEnd) {
         this.fightManager = ((WorldProviderEnd)var1.provider).getDragonFightManager();
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
         var2 = 0.0F;
      }

      var2 = 1.0F - var2;
      int var3 = this.ringBufferIndex - var1 & 63;
      int var4 = this.ringBufferIndex - var1 - 1 & 63;
      double[] var5 = new double[3];
      double var6 = this.ringBuffer[var3][0];
      double var8 = MathHelper.wrapDegrees(this.ringBuffer[var4][0] - var6);
      var5[0] = var6 + var8 * (double)var2;
      var6 = this.ringBuffer[var3][1];
      var8 = this.ringBuffer[var4][1] - var6;
      var5[1] = var6 + var8 * (double)var2;
      var5[2] = this.ringBuffer[var3][2] + (this.ringBuffer[var4][2] - this.ringBuffer[var3][2]) * (double)var2;
      return var5;
   }

   public void onLivingUpdate() {
      if (this.world.isRemote) {
         this.setHealth(this.getHealth());
         if (!this.isSilent()) {
            float var1 = MathHelper.cos(this.animTime * 6.2831855F);
            float var2 = MathHelper.cos(this.prevAnimTime * 6.2831855F);
            if (var2 <= -0.3F && var1 >= -0.3F) {
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
         float var22 = (this.rand.nextFloat() - 0.5F) * 8.0F;
         float var25 = (this.rand.nextFloat() - 0.5F) * 4.0F;
         float var3 = (this.rand.nextFloat() - 0.5F) * 8.0F;
         this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX + (double)var22, this.posY + 2.0D + (double)var25, this.posZ + (double)var3, 0.0D, 0.0D, 0.0D);
      } else {
         this.updateDragonEnderCrystal();
         float var23 = 0.2F / (MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ) * 10.0F + 1.0F);
         var23 = var23 * (float)Math.pow(2.0D, this.motionY);
         if (this.phaseManager.getCurrentPhase().getIsStationary()) {
            this.animTime += 0.1F;
         } else if (this.slowed) {
            this.animTime += var23 * 0.5F;
         } else {
            this.animTime += var23;
         }

         this.rotationYaw = MathHelper.wrapDegrees(this.rotationYaw);
         if (this.isAIDisabled()) {
            this.animTime = 0.5F;
         } else {
            if (this.ringBufferIndex < 0) {
               for(int var26 = 0; var26 < this.ringBuffer.length; ++var26) {
                  this.ringBuffer[var26][0] = (double)this.rotationYaw;
                  this.ringBuffer[var26][1] = this.posY;
               }
            }

            if (++this.ringBufferIndex == this.ringBuffer.length) {
               this.ringBufferIndex = 0;
            }

            this.ringBuffer[this.ringBufferIndex][0] = (double)this.rotationYaw;
            this.ringBuffer[this.ringBufferIndex][1] = this.posY;
            if (this.world.isRemote) {
               if (this.newPosRotationIncrements > 0) {
                  double var27 = this.posX + (this.interpTargetX - this.posX) / (double)this.newPosRotationIncrements;
                  double var4 = this.posY + (this.interpTargetY - this.posY) / (double)this.newPosRotationIncrements;
                  double var6 = this.posZ + (this.interpTargetZ - this.posZ) / (double)this.newPosRotationIncrements;
                  double var8 = MathHelper.wrapDegrees(this.interpTargetYaw - (double)this.rotationYaw);
                  this.rotationYaw = (float)((double)this.rotationYaw + var8 / (double)this.newPosRotationIncrements);
                  this.rotationPitch = (float)((double)this.rotationPitch + (this.interpTargetPitch - (double)this.rotationPitch) / (double)this.newPosRotationIncrements);
                  --this.newPosRotationIncrements;
                  this.setPosition(var27, var4, var6);
                  this.setRotation(this.rotationYaw, this.rotationPitch);
               }

               this.phaseManager.getCurrentPhase().doClientRenderEffects();
            } else {
               IPhase var28 = this.phaseManager.getCurrentPhase();
               var28.doLocalUpdate();
               if (this.phaseManager.getCurrentPhase() != var28) {
                  var28 = this.phaseManager.getCurrentPhase();
                  var28.doLocalUpdate();
               }

               Vec3d var30 = var28.getTargetLocation();
               if (var30 != null) {
                  double var32 = var30.xCoord - this.posX;
                  double var34 = var30.yCoord - this.posY;
                  double var37 = var30.zCoord - this.posZ;
                  double var10 = var32 * var32 + var34 * var34 + var37 * var37;
                  float var12 = var28.getMaxRiseOrFall();
                  var34 = MathHelper.clamp(var34 / (double)MathHelper.sqrt(var32 * var32 + var37 * var37), (double)(-var12), (double)var12);
                  this.motionY += var34 * 0.10000000149011612D;
                  this.rotationYaw = MathHelper.wrapDegrees(this.rotationYaw);
                  double var13 = MathHelper.clamp(MathHelper.wrapDegrees(180.0D - MathHelper.atan2(var32, var37) * 57.29577951308232D - (double)this.rotationYaw), -50.0D, 50.0D);
                  Vec3d var15 = (new Vec3d(var30.xCoord - this.posX, var30.yCoord - this.posY, var30.zCoord - this.posZ)).normalize();
                  Vec3d var16 = (new Vec3d((double)MathHelper.sin(this.rotationYaw * 0.017453292F), this.motionY, (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)))).normalize();
                  float var17 = Math.max(((float)var16.dotProduct(var15) + 0.5F) / 1.5F, 0.0F);
                  this.randomYawVelocity *= 0.8F;
                  this.randomYawVelocity = (float)((double)this.randomYawVelocity + var13 * (double)var28.getYawFactor());
                  this.rotationYaw += this.randomYawVelocity * 0.1F;
                  float var18 = (float)(2.0D / (var10 + 1.0D));
                  float var19 = 0.06F;
                  this.moveRelative(0.0F, -1.0F, 0.06F * (var17 * var18 + (1.0F - var18)));
                  if (this.slowed) {
                     this.move(this.motionX * 0.800000011920929D, this.motionY * 0.800000011920929D, this.motionZ * 0.800000011920929D);
                  } else {
                     this.move(this.motionX, this.motionY, this.motionZ);
                  }

                  Vec3d var20 = (new Vec3d(this.motionX, this.motionY, this.motionZ)).normalize();
                  float var21 = ((float)var20.dotProduct(var16) + 1.0F) / 2.0F;
                  var21 = 0.8F + 0.15F * var21;
                  this.motionX *= (double)var21;
                  this.motionZ *= (double)var21;
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
            float var29 = (float)(this.getMovementOffsets(5, 1.0F)[1] - this.getMovementOffsets(10, 1.0F)[1]) * 10.0F * 0.017453292F;
            float var31 = MathHelper.cos(var29);
            float var33 = MathHelper.sin(var29);
            float var5 = this.rotationYaw * 0.017453292F;
            float var36 = MathHelper.sin(var5);
            float var7 = MathHelper.cos(var5);
            this.dragonPartBody.onUpdate();
            this.dragonPartBody.setLocationAndAngles(this.posX + (double)(var36 * 0.5F), this.posY, this.posZ - (double)(var7 * 0.5F), 0.0F, 0.0F);
            this.dragonPartWing1.onUpdate();
            this.dragonPartWing1.setLocationAndAngles(this.posX + (double)(var7 * 4.5F), this.posY + 2.0D, this.posZ + (double)(var36 * 4.5F), 0.0F, 0.0F);
            this.dragonPartWing2.onUpdate();
            this.dragonPartWing2.setLocationAndAngles(this.posX - (double)(var7 * 4.5F), this.posY + 2.0D, this.posZ - (double)(var36 * 4.5F), 0.0F, 0.0F);
            if (!this.world.isRemote && this.hurtTime == 0) {
               this.collideWithEntities(this.world.getEntitiesWithinAABBExcludingEntity(this, this.dragonPartWing1.getEntityBoundingBox().expand(4.0D, 2.0D, 4.0D).offset(0.0D, -2.0D, 0.0D)));
               this.collideWithEntities(this.world.getEntitiesWithinAABBExcludingEntity(this, this.dragonPartWing2.getEntityBoundingBox().expand(4.0D, 2.0D, 4.0D).offset(0.0D, -2.0D, 0.0D)));
               this.attackEntitiesInList(this.world.getEntitiesWithinAABBExcludingEntity(this, this.dragonPartHead.getEntityBoundingBox().expandXyz(1.0D)));
               this.attackEntitiesInList(this.world.getEntitiesWithinAABBExcludingEntity(this, this.dragonPartNeck.getEntityBoundingBox().expandXyz(1.0D)));
            }

            double[] var38 = this.getMovementOffsets(5, 1.0F);
            float var9 = MathHelper.sin(this.rotationYaw * 0.017453292F - this.randomYawVelocity * 0.01F);
            float var39 = MathHelper.cos(this.rotationYaw * 0.017453292F - this.randomYawVelocity * 0.01F);
            this.dragonPartHead.onUpdate();
            this.dragonPartNeck.onUpdate();
            float var11 = this.getHeadYOffset(1.0F);
            this.dragonPartHead.setLocationAndAngles(this.posX + (double)(var9 * 6.5F * var31), this.posY + (double)var11 + (double)(var33 * 6.5F), this.posZ - (double)(var39 * 6.5F * var31), 0.0F, 0.0F);
            this.dragonPartNeck.setLocationAndAngles(this.posX + (double)(var9 * 5.5F * var31), this.posY + (double)var11 + (double)(var33 * 5.5F), this.posZ - (double)(var39 * 5.5F * var31), 0.0F, 0.0F);

            for(int var40 = 0; var40 < 3; ++var40) {
               EntityDragonPart var41 = null;
               if (var40 == 0) {
                  var41 = this.dragonPartTail1;
               }

               if (var40 == 1) {
                  var41 = this.dragonPartTail2;
               }

               if (var40 == 2) {
                  var41 = this.dragonPartTail3;
               }

               double[] var14 = this.getMovementOffsets(12 + var40 * 2, 1.0F);
               float var42 = this.rotationYaw * 0.017453292F + this.simplifyAngle(var14[0] - var38[0]) * 0.017453292F;
               float var43 = MathHelper.sin(var42);
               float var44 = MathHelper.cos(var42);
               float var45 = 1.5F;
               float var46 = (float)(var40 + 1) * 2.0F;
               var41.onUpdate();
               var41.setLocationAndAngles(this.posX - (double)((var36 * 1.5F + var43 * var46) * var31), this.posY + (var14[1] - var38[1]) - (double)((var46 + 1.5F) * var33) + 1.5D, this.posZ + (double)((var7 * 1.5F + var44 * var46) * var31), 0.0F, 0.0F);
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
      double var2;
      if (this.phaseManager.getCurrentPhase().getIsStationary()) {
         var2 = -1.0D;
      } else {
         double[] var4 = this.getMovementOffsets(5, 1.0F);
         double[] var5 = this.getMovementOffsets(0, 1.0F);
         var2 = var4[1] - var5[0];
      }

      return (float)var2;
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
         List var1 = this.world.getEntitiesWithinAABB(EntityEnderCrystal.class, this.getEntityBoundingBox().expandXyz(32.0D));
         EntityEnderCrystal var2 = null;
         double var3 = Double.MAX_VALUE;

         for(EntityEnderCrystal var6 : var1) {
            double var7 = var6.getDistanceSqToEntity(this);
            if (var7 < var3) {
               var3 = var7;
               var2 = var6;
            }
         }

         this.healingEnderCrystal = var2;
      }

   }

   private void collideWithEntities(List var1) {
      double var2 = (this.dragonPartBody.getEntityBoundingBox().minX + this.dragonPartBody.getEntityBoundingBox().maxX) / 2.0D;
      double var4 = (this.dragonPartBody.getEntityBoundingBox().minZ + this.dragonPartBody.getEntityBoundingBox().maxZ) / 2.0D;

      for(Entity var7 : var1) {
         if (var7 instanceof EntityLivingBase) {
            double var8 = var7.posX - var2;
            double var10 = var7.posZ - var4;
            double var12 = var8 * var8 + var10 * var10;
            var7.addVelocity(var8 / var12 * 4.0D, 0.20000000298023224D, var10 / var12 * 4.0D);
            if (!this.phaseManager.getCurrentPhase().getIsStationary() && ((EntityLivingBase)var7).getRevengeTimer() < var7.ticksExisted - 2) {
               var7.attackEntityFrom(DamageSource.causeMobDamage(this), 5.0F);
               this.applyEnchantments(this, var7);
            }
         }
      }

   }

   private void attackEntitiesInList(List var1) {
      for(int var2 = 0; var2 < var1.size(); ++var2) {
         Entity var3 = (Entity)var1.get(var2);
         if (var3 instanceof EntityLivingBase) {
            var3.attackEntityFrom(DamageSource.causeMobDamage(this), 10.0F);
            this.applyEnchantments(this, var3);
         }
      }

   }

   private float simplifyAngle(double var1) {
      return (float)MathHelper.wrapDegrees(var1);
   }

   private boolean destroyBlocksInAABB(AxisAlignedBB var1) {
      int var2 = MathHelper.floor(var1.minX);
      int var3 = MathHelper.floor(var1.minY);
      int var4 = MathHelper.floor(var1.minZ);
      int var5 = MathHelper.floor(var1.maxX);
      int var6 = MathHelper.floor(var1.maxY);
      int var7 = MathHelper.floor(var1.maxZ);
      boolean var8 = false;
      boolean var9 = false;

      for(int var10 = var2; var10 <= var5; ++var10) {
         for(int var11 = var3; var11 <= var6; ++var11) {
            for(int var12 = var4; var12 <= var7; ++var12) {
               BlockPos var13 = new BlockPos(var10, var11, var12);
               IBlockState var14 = this.world.getBlockState(var13);
               Block var15 = var14.getBlock();
               if (!var15.isAir(var14, this.world, var13) && var14.getMaterial() != Material.FIRE) {
                  if (!this.world.getGameRules().getBoolean("mobGriefing")) {
                     var8 = true;
                  } else if (!var15.canEntityDestroy(var14, this.world, var13, this)) {
                     var8 = true;
                  } else if (var15 != Blocks.COMMAND_BLOCK && var15 != Blocks.REPEATING_COMMAND_BLOCK && var15 != Blocks.CHAIN_COMMAND_BLOCK && var15 != Blocks.IRON_BARS && var15 != Blocks.END_GATEWAY) {
                     var9 = this.world.setBlockToAir(var13) || var9;
                  } else {
                     var8 = true;
                  }
               }
            }
         }
      }

      if (var9) {
         double var16 = var1.minX + (var1.maxX - var1.minX) * (double)this.rand.nextFloat();
         double var17 = var1.minY + (var1.maxY - var1.minY) * (double)this.rand.nextFloat();
         double var18 = var1.minZ + (var1.maxZ - var1.minZ) * (double)this.rand.nextFloat();
         this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, var16, var17, var18, 0.0D, 0.0D, 0.0D);
      }

      return var8;
   }

   public boolean attackEntityFromPart(EntityDragonPart var1, DamageSource var2, float var3) {
      var3 = this.phaseManager.getCurrentPhase().getAdjustedDamage(var1, var2, var3);
      if (var1 != this.dragonPartHead) {
         var3 = var3 / 4.0F + Math.min(var3, 1.0F);
      }

      if (var3 < 0.01F) {
         return false;
      } else {
         if (var2.getEntity() instanceof EntityPlayer || var2.isExplosion()) {
            float var4 = this.getHealth();
            this.attackDragonFrom(var2, var3);
            if (this.getHealth() <= 0.0F && !this.phaseManager.getCurrentPhase().getIsStationary()) {
               this.setHealth(1.0F);
               this.phaseManager.setPhase(PhaseList.DYING);
            }

            if (this.phaseManager.getCurrentPhase().getIsStationary()) {
               this.sittingDamageReceived = (int)((float)this.sittingDamageReceived + (var4 - this.getHealth()));
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
      if (var1 instanceof EntityDamageSource && ((EntityDamageSource)var1).getIsThornsDamage()) {
         this.attackEntityFromPart(this.dragonPartBody, var1, var2);
      }

      return false;
   }

   protected boolean attackDragonFrom(DamageSource var1, float var2) {
      return super.attackEntityFrom(var1, var2);
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
         float var1 = (this.rand.nextFloat() - 0.5F) * 8.0F;
         float var2 = (this.rand.nextFloat() - 0.5F) * 4.0F;
         float var3 = (this.rand.nextFloat() - 0.5F) * 8.0F;
         this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX + (double)var1, this.posY + 2.0D + (double)var2, this.posZ + (double)var3, 0.0D, 0.0D, 0.0D);
      }

      boolean var4 = this.world.getGameRules().getBoolean("doMobLoot");
      short var5 = 500;
      if (this.fightManager != null && !this.fightManager.hasPreviouslyKilledDragon()) {
         var5 = 12000;
      }

      if (!this.world.isRemote) {
         if (this.deathTicks > 150 && this.deathTicks % 5 == 0 && var4) {
            this.dropExperience(MathHelper.floor((float)var5 * 0.08F));
         }

         if (this.deathTicks == 1) {
            this.world.playBroadcastSound(1028, new BlockPos(this), 0);
         }
      }

      this.move(0.0D, 0.10000000149011612D, 0.0D);
      this.rotationYaw += 20.0F;
      this.renderYawOffset = this.rotationYaw;
      if (this.deathTicks == 200 && !this.world.isRemote) {
         if (var4) {
            this.dropExperience(MathHelper.floor((float)var5 * 0.2F));
         }

         if (this.fightManager != null) {
            this.fightManager.processDragonDeath(this);
         }

         this.setDead();
      }

   }

   private void dropExperience(int var1) {
      while(var1 > 0) {
         int var2 = EntityXPOrb.getXPSplit(var1);
         var1 -= var2;
         this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY, this.posZ, var2));
      }

   }

   public int initPathPoints() {
      if (this.pathPoints[0] == null) {
         for(int var1 = 0; var1 < 24; ++var1) {
            int var2 = 5;
            int var3;
            int var4;
            if (var1 < 12) {
               var3 = (int)(60.0F * MathHelper.cos(2.0F * (-3.1415927F + 0.2617994F * (float)var1)));
               var4 = (int)(60.0F * MathHelper.sin(2.0F * (-3.1415927F + 0.2617994F * (float)var1)));
            } else if (var1 < 20) {
               int var5 = var1 - 12;
               var3 = (int)(40.0F * MathHelper.cos(2.0F * (-3.1415927F + 0.3926991F * (float)var5)));
               var4 = (int)(40.0F * MathHelper.sin(2.0F * (-3.1415927F + 0.3926991F * (float)var5)));
               var2 += 10;
            } else {
               int var6 = var1 - 20;
               var3 = (int)(20.0F * MathHelper.cos(2.0F * (-3.1415927F + 0.7853982F * (float)var6)));
               var4 = (int)(20.0F * MathHelper.sin(2.0F * (-3.1415927F + 0.7853982F * (float)var6)));
            }

            int var7 = Math.max(this.world.getSeaLevel() + 10, this.world.getTopSolidOrLiquidBlock(new BlockPos(var3, 0, var4)).getY() + var2);
            this.pathPoints[var1] = new PathPoint(var3, var7, var4);
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
      float var7 = 10000.0F;
      int var8 = 0;
      PathPoint var9 = new PathPoint(MathHelper.floor(var1), MathHelper.floor(var3), MathHelper.floor(var5));
      byte var10 = 0;
      if (this.fightManager == null || this.fightManager.getNumAliveCrystals() == 0) {
         var10 = 12;
      }

      for(int var11 = var10; var11 < 24; ++var11) {
         if (this.pathPoints[var11] != null) {
            float var12 = this.pathPoints[var11].distanceToSquared(var9);
            if (var12 < var7) {
               var7 = var12;
               var8 = var11;
            }
         }
      }

      return var8;
   }

   @Nullable
   public Path findPath(int var1, int var2, @Nullable PathPoint var3) {
      for(int var4 = 0; var4 < 24; ++var4) {
         PathPoint var5 = this.pathPoints[var4];
         var5.visited = false;
         var5.distanceToTarget = 0.0F;
         var5.totalPathDistance = 0.0F;
         var5.distanceToNext = 0.0F;
         var5.previous = null;
         var5.index = -1;
      }

      PathPoint var13 = this.pathPoints[var1];
      PathPoint var14 = this.pathPoints[var2];
      var13.totalPathDistance = 0.0F;
      var13.distanceToNext = var13.distanceTo(var14);
      var13.distanceToTarget = var13.distanceToNext;
      this.pathFindQueue.clearPath();
      this.pathFindQueue.addPoint(var13);
      PathPoint var6 = var13;
      byte var7 = 0;
      if (this.fightManager == null || this.fightManager.getNumAliveCrystals() == 0) {
         var7 = 12;
      }

      while(!this.pathFindQueue.isPathEmpty()) {
         PathPoint var8 = this.pathFindQueue.dequeue();
         if (var8.equals(var14)) {
            if (var3 != null) {
               var3.previous = var14;
               var14 = var3;
            }

            return this.makePath(var13, var14);
         }

         if (var8.distanceTo(var14) < var6.distanceTo(var14)) {
            var6 = var8;
         }

         var8.visited = true;
         int var9 = 0;

         for(int var10 = 0; var10 < 24; ++var10) {
            if (this.pathPoints[var10] == var8) {
               var9 = var10;
               break;
            }
         }

         for(int var15 = var7; var15 < 24; ++var15) {
            if ((this.neighbors[var9] & 1 << var15) > 0) {
               PathPoint var11 = this.pathPoints[var15];
               if (!var11.visited) {
                  float var12 = var8.totalPathDistance + var8.distanceTo(var11);
                  if (!var11.isAssigned() || var12 < var11.totalPathDistance) {
                     var11.previous = var8;
                     var11.totalPathDistance = var12;
                     var11.distanceToNext = var11.distanceTo(var14);
                     if (var11.isAssigned()) {
                        this.pathFindQueue.changeDistance(var11, var11.totalPathDistance + var11.distanceToNext);
                     } else {
                        var11.distanceToTarget = var11.totalPathDistance + var11.distanceToNext;
                        this.pathFindQueue.addPoint(var11);
                     }
                  }
               }
            }
         }
      }

      if (var6 == var13) {
         return null;
      } else {
         LOGGER.debug("Failed to find path from {} to {}", new Object[]{var1, var2});
         if (var3 != null) {
            var3.previous = var6;
            var6 = var3;
         }

         return this.makePath(var13, var6);
      }
   }

   private Path makePath(PathPoint var1, PathPoint var2) {
      int var3 = 1;

      for(PathPoint var4 = var2; var4.previous != null; var4 = var4.previous) {
         ++var3;
      }

      PathPoint[] var7 = new PathPoint[var3];
      PathPoint var5 = var2;
      --var3;

      for(var7[var3] = var2; var5.previous != null; var7[var3] = var5) {
         var5 = var5.previous;
         --var3;
      }

      return new Path(var7);
   }

   public static void registerFixesDragon(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "EnderDragon");
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setInteger("DragonPhase", this.phaseManager.getCurrentPhase().getPhaseList().getId());
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      if (var1.hasKey("DragonPhase")) {
         this.phaseManager.setPhase(PhaseList.getById(var1.getInteger("DragonPhase")));
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
      IPhase var4 = this.phaseManager.getCurrentPhase();
      PhaseList var5 = var4.getPhaseList();
      double var6;
      if (var5 != PhaseList.LANDING && var5 != PhaseList.TAKEOFF) {
         if (var4.getIsStationary()) {
            var6 = (double)var1;
         } else if (var1 == 6) {
            var6 = 0.0D;
         } else {
            var6 = var3[1] - var2[1];
         }
      } else {
         BlockPos var8 = this.world.getTopSolidOrLiquidBlock(WorldGenEndPodium.END_PODIUM_LOCATION);
         float var9 = Math.max(MathHelper.sqrt(this.getDistanceSqToCenter(var8)) / 4.0F, 1.0F);
         var6 = (double)((float)var1 / var9);
      }

      return (float)var6;
   }

   public Vec3d getHeadLookVec(float var1) {
      IPhase var2 = this.phaseManager.getCurrentPhase();
      PhaseList var3 = var2.getPhaseList();
      Vec3d var4;
      if (var3 != PhaseList.LANDING && var3 != PhaseList.TAKEOFF) {
         if (var2.getIsStationary()) {
            float var10 = this.rotationPitch;
            float var11 = 1.5F;
            this.rotationPitch = -45.0F;
            var4 = this.getLook(var1);
            this.rotationPitch = var10;
         } else {
            var4 = this.getLook(var1);
         }
      } else {
         BlockPos var5 = this.world.getTopSolidOrLiquidBlock(WorldGenEndPodium.END_PODIUM_LOCATION);
         float var6 = Math.max(MathHelper.sqrt(this.getDistanceSqToCenter(var5)) / 4.0F, 1.0F);
         float var7 = 6.0F / var6;
         float var8 = this.rotationPitch;
         float var9 = 1.5F;
         this.rotationPitch = -var7 * 1.5F * 5.0F;
         var4 = this.getLook(var1);
         this.rotationPitch = var8;
      }

      return var4;
   }

   public void onCrystalDestroyed(EntityEnderCrystal var1, BlockPos var2, DamageSource var3) {
      EntityPlayer var4;
      if (var3.getEntity() instanceof EntityPlayer) {
         var4 = (EntityPlayer)var3.getEntity();
      } else {
         var4 = this.world.getNearestAttackablePlayer(var2, 64.0D, 64.0D);
      }

      if (var1 == this.healingEnderCrystal) {
         this.attackEntityFromPart(this.dragonPartHead, DamageSource.causeExplosionDamage(var4), 10.0F);
      }

      this.phaseManager.getCurrentPhase().onCrystalDestroyed(var1, var2, var3, var4);
   }

   public void notifyDataManagerChange(DataParameter var1) {
      if (PHASE.equals(var1) && this.world.isRemote) {
         this.phaseManager.setPhase(PhaseList.getById(((Integer)this.getDataManager().get(PHASE)).intValue()));
      }

      super.notifyDataManagerChange(var1);
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
