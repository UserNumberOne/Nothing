package net.minecraft.entity.monster;

import com.google.common.base.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityGuardian extends EntityMob {
   private static final DataParameter STATUS = EntityDataManager.createKey(EntityGuardian.class, DataSerializers.BYTE);
   private static final DataParameter TARGET_ENTITY = EntityDataManager.createKey(EntityGuardian.class, DataSerializers.VARINT);
   private float clientSideTailAnimation;
   private float clientSideTailAnimationO;
   private float clientSideTailAnimationSpeed;
   private float clientSideSpikesAnimation;
   private float clientSideSpikesAnimationO;
   private EntityLivingBase targetedEntity;
   private int clientSideAttackTime;
   private boolean clientSideTouchedGround;
   public EntityAIWander wander;

   public EntityGuardian(World var1) {
      super(var1);
      this.experienceValue = 10;
      this.setSize(0.85F, 0.85F);
      this.moveHelper = new EntityGuardian.GuardianMoveHelper(this);
      this.clientSideTailAnimation = this.rand.nextFloat();
      this.clientSideTailAnimationO = this.clientSideTailAnimation;
   }

   protected void initEntityAI() {
      EntityAIMoveTowardsRestriction var1 = new EntityAIMoveTowardsRestriction(this, 1.0D);
      this.wander = new EntityAIWander(this, 1.0D, 80);
      this.tasks.addTask(4, new EntityGuardian.AIGuardianAttack(this));
      this.tasks.addTask(5, var1);
      this.tasks.addTask(7, this.wander);
      this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityGuardian.class, 12.0F, 0.01F));
      this.tasks.addTask(9, new EntityAILookIdle(this));
      this.wander.setMutexBits(3);
      var1.setMutexBits(3);
      this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityLivingBase.class, 10, true, false, new EntityGuardian.GuardianTargetSelector(this)));
   }

   public void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);
      this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
   }

   public static void registerFixesGuardian(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "Guardian");
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.setElder(var1.getBoolean("Elder"));
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setBoolean("Elder", this.isElder());
   }

   protected PathNavigate createNavigator(World var1) {
      return new PathNavigateSwimmer(this, var1);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(STATUS, Byte.valueOf((byte)0));
      this.dataManager.register(TARGET_ENTITY, Integer.valueOf(0));
   }

   private boolean isSyncedFlagSet(int var1) {
      return (((Byte)this.dataManager.get(STATUS)).byteValue() & var1) != 0;
   }

   private void setSyncedFlag(int var1, boolean var2) {
      byte var3 = ((Byte)this.dataManager.get(STATUS)).byteValue();
      if (var2) {
         this.dataManager.set(STATUS, Byte.valueOf((byte)(var3 | var1)));
      } else {
         this.dataManager.set(STATUS, Byte.valueOf((byte)(var3 & ~var1)));
      }

   }

   public boolean isMoving() {
      return this.isSyncedFlagSet(2);
   }

   private void setMoving(boolean var1) {
      this.setSyncedFlag(2, var1);
   }

   public int getAttackDuration() {
      return this.isElder() ? 60 : 80;
   }

   public boolean isElder() {
      return this.isSyncedFlagSet(4);
   }

   public void setElder(boolean var1) {
      this.setSyncedFlag(4, var1);
      if (var1) {
         this.setSize(1.9975F, 1.9975F);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.30000001192092896D);
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(8.0D);
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(80.0D);
         this.enablePersistence();
         if (this.wander != null) {
            this.wander.setExecutionChance(400);
         }
      }

   }

   private void setTargetedEntity(int var1) {
      this.dataManager.set(TARGET_ENTITY, Integer.valueOf(var1));
   }

   public boolean hasTargetedEntity() {
      return ((Integer)this.dataManager.get(TARGET_ENTITY)).intValue() != 0;
   }

   public EntityLivingBase getTargetedEntity() {
      if (!this.hasTargetedEntity()) {
         return null;
      } else if (this.world.isRemote) {
         if (this.targetedEntity != null) {
            return this.targetedEntity;
         } else {
            Entity var1 = this.world.getEntityByID(((Integer)this.dataManager.get(TARGET_ENTITY)).intValue());
            if (var1 instanceof EntityLivingBase) {
               this.targetedEntity = (EntityLivingBase)var1;
               return this.targetedEntity;
            } else {
               return null;
            }
         }
      } else {
         return this.getAttackTarget();
      }
   }

   public void notifyDataManagerChange(DataParameter var1) {
      super.notifyDataManagerChange(var1);
      if (STATUS.equals(var1)) {
         if (this.isElder() && this.width < 1.0F) {
            this.setSize(1.9975F, 1.9975F);
         }
      } else if (TARGET_ENTITY.equals(var1)) {
         this.clientSideAttackTime = 0;
         this.targetedEntity = null;
      }

   }

   public int getTalkInterval() {
      return 160;
   }

   protected SoundEvent getAmbientSound() {
      if (this.isElder()) {
         return this.isInWater() ? SoundEvents.ENTITY_ELDER_GUARDIAN_AMBIENT : SoundEvents.ENTITY_ELDERGUARDIAN_AMBIENTLAND;
      } else {
         return this.isInWater() ? SoundEvents.ENTITY_GUARDIAN_AMBIENT : SoundEvents.ENTITY_GUARDIAN_AMBIENT_LAND;
      }
   }

   protected SoundEvent getHurtSound() {
      if (this.isElder()) {
         return this.isInWater() ? SoundEvents.ENTITY_ELDER_GUARDIAN_HURT : SoundEvents.ENTITY_ELDER_GUARDIAN_HURT_LAND;
      } else {
         return this.isInWater() ? SoundEvents.ENTITY_GUARDIAN_HURT : SoundEvents.ENTITY_GUARDIAN_HURT_LAND;
      }
   }

   protected SoundEvent getDeathSound() {
      if (this.isElder()) {
         return this.isInWater() ? SoundEvents.ENTITY_ELDER_GUARDIAN_DEATH : SoundEvents.ENTITY_ELDER_GUARDIAN_DEATH_LAND;
      } else {
         return this.isInWater() ? SoundEvents.ENTITY_GUARDIAN_DEATH : SoundEvents.ENTITY_GUARDIAN_DEATH_LAND;
      }
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   public float getEyeHeight() {
      return this.height * 0.5F;
   }

   public float getBlockPathWeight(BlockPos var1) {
      return this.world.getBlockState(var1).getMaterial() == Material.WATER ? 10.0F + this.world.getLightBrightness(var1) - 0.5F : super.getBlockPathWeight(var1);
   }

   public void onLivingUpdate() {
      if (this.world.isRemote) {
         this.clientSideTailAnimationO = this.clientSideTailAnimation;
         if (!this.isInWater()) {
            this.clientSideTailAnimationSpeed = 2.0F;
            if (this.motionY > 0.0D && this.clientSideTouchedGround && !this.isSilent()) {
               this.world.playSound(this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GUARDIAN_FLOP, this.getSoundCategory(), 1.0F, 1.0F, false);
            }

            this.clientSideTouchedGround = this.motionY < 0.0D && this.world.isBlockNormalCube((new BlockPos(this)).down(), false);
         } else if (this.isMoving()) {
            if (this.clientSideTailAnimationSpeed < 0.5F) {
               this.clientSideTailAnimationSpeed = 4.0F;
            } else {
               this.clientSideTailAnimationSpeed += (0.5F - this.clientSideTailAnimationSpeed) * 0.1F;
            }
         } else {
            this.clientSideTailAnimationSpeed += (0.125F - this.clientSideTailAnimationSpeed) * 0.2F;
         }

         this.clientSideTailAnimation += this.clientSideTailAnimationSpeed;
         this.clientSideSpikesAnimationO = this.clientSideSpikesAnimation;
         if (!this.isInWater()) {
            this.clientSideSpikesAnimation = this.rand.nextFloat();
         } else if (this.isMoving()) {
            this.clientSideSpikesAnimation += (0.0F - this.clientSideSpikesAnimation) * 0.25F;
         } else {
            this.clientSideSpikesAnimation += (1.0F - this.clientSideSpikesAnimation) * 0.06F;
         }

         if (this.isMoving() && this.isInWater()) {
            Vec3d var1 = this.getLook(0.0F);

            for(int var2 = 0; var2 < 2; ++var2) {
               this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width - var1.xCoord * 1.5D, this.posY + this.rand.nextDouble() * (double)this.height - var1.yCoord * 1.5D, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width - var1.zCoord * 1.5D, 0.0D, 0.0D, 0.0D);
            }
         }

         if (this.hasTargetedEntity()) {
            if (this.clientSideAttackTime < this.getAttackDuration()) {
               ++this.clientSideAttackTime;
            }

            EntityLivingBase var15 = this.getTargetedEntity();
            if (var15 != null) {
               this.getLookHelper().setLookPositionWithEntity(var15, 90.0F, 90.0F);
               this.getLookHelper().onUpdateLook();
               double var3 = (double)this.getAttackAnimationScale(0.0F);
               double var5 = var15.posX - this.posX;
               double var7 = var15.posY + (double)(var15.height * 0.5F) - (this.posY + (double)this.getEyeHeight());
               double var9 = var15.posZ - this.posZ;
               double var11 = Math.sqrt(var5 * var5 + var7 * var7 + var9 * var9);
               var5 = var5 / var11;
               var7 = var7 / var11;
               var9 = var9 / var11;
               double var13 = this.rand.nextDouble();

               while(var13 < var11) {
                  var13 += 1.8D - var3 + this.rand.nextDouble() * (1.7D - var3);
                  this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX + var5 * var13, this.posY + var7 * var13 + (double)this.getEyeHeight(), this.posZ + var9 * var13, 0.0D, 0.0D, 0.0D);
               }
            }
         }
      }

      if (this.inWater) {
         this.setAir(300);
      } else if (this.onGround) {
         this.motionY += 0.5D;
         this.motionX += (double)((this.rand.nextFloat() * 2.0F - 1.0F) * 0.4F);
         this.motionZ += (double)((this.rand.nextFloat() * 2.0F - 1.0F) * 0.4F);
         this.rotationYaw = this.rand.nextFloat() * 360.0F;
         this.onGround = false;
         this.isAirBorne = true;
      }

      if (this.hasTargetedEntity()) {
         this.rotationYaw = this.rotationYawHead;
      }

      super.onLivingUpdate();
   }

   public float getAttackAnimationScale(float var1) {
      return ((float)this.clientSideAttackTime + var1) / (float)this.getAttackDuration();
   }

   protected void updateAITasks() {
      super.updateAITasks();
      if (this.isElder()) {
         boolean var1 = true;
         boolean var2 = true;
         boolean var3 = true;
         boolean var4 = true;
         if ((this.ticksExisted + this.getEntityId()) % 1200 == 0) {
            Potion var5 = MobEffects.MINING_FATIGUE;

            for(EntityPlayerMP var8 : this.world.getPlayers(EntityPlayerMP.class, new Predicate() {
               public boolean apply(@Nullable EntityPlayerMP var1) {
                  return EntityGuardian.this.getDistanceSqToEntity(var1) < 2500.0D && var1.interactionManager.survivalOrAdventure();
               }

               // $FF: synthetic method
               public boolean apply(Object var1) {
                  return this.apply((EntityPlayerMP)var1);
               }
            })) {
               if (!var8.isPotionActive(var5) || var8.getActivePotionEffect(var5).getAmplifier() < 2 || var8.getActivePotionEffect(var5).getDuration() < 1200) {
                  var8.connection.sendPacket(new SPacketChangeGameState(10, 0.0F));
                  var8.addPotionEffect(new PotionEffect(var5, 6000, 2));
               }
            }
         }

         if (!this.hasHome()) {
            this.setHomePosAndDistance(new BlockPos(this), 16);
         }
      }

   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return this.isElder() ? LootTableList.ENTITIES_ELDER_GUARDIAN : LootTableList.ENTITIES_GUARDIAN;
   }

   protected boolean isValidLightLevel() {
      return true;
   }

   public boolean isNotColliding() {
      return this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this) && this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty();
   }

   public boolean getCanSpawnHere() {
      return (this.rand.nextInt(20) == 0 || !this.world.canBlockSeeSky(new BlockPos(this))) && super.getCanSpawnHere();
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (!this.isMoving() && !var1.isMagicDamage() && var1.getSourceOfDamage() instanceof EntityLivingBase) {
         EntityLivingBase var3 = (EntityLivingBase)var1.getSourceOfDamage();
         if (!var1.isExplosion()) {
            var3.attackEntityFrom(DamageSource.causeThornsDamage(this), 2.0F);
         }
      }

      if (this.wander != null) {
         this.wander.makeUpdate();
      }

      return super.attackEntityFrom(var1, var2);
   }

   public int getVerticalFaceSpeed() {
      return 180;
   }

   public void moveEntityWithHeading(float var1, float var2) {
      if (this.isServerWorld()) {
         if (this.isInWater()) {
            this.moveRelative(var1, var2, 0.1F);
            this.move(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.8999999761581421D;
            this.motionY *= 0.8999999761581421D;
            this.motionZ *= 0.8999999761581421D;
            if (!this.isMoving() && this.getAttackTarget() == null) {
               this.motionY -= 0.005D;
            }
         } else {
            super.moveEntityWithHeading(var1, var2);
         }
      } else {
         super.moveEntityWithHeading(var1, var2);
      }

   }

   static class AIGuardianAttack extends EntityAIBase {
      private final EntityGuardian theEntity;
      private int tickCounter;

      public AIGuardianAttack(EntityGuardian var1) {
         this.theEntity = var1;
         this.setMutexBits(3);
      }

      public boolean shouldExecute() {
         EntityLivingBase var1 = this.theEntity.getAttackTarget();
         return var1 != null && var1.isEntityAlive();
      }

      public boolean continueExecuting() {
         return super.continueExecuting() && (this.theEntity.isElder() || this.theEntity.getDistanceSqToEntity(this.theEntity.getAttackTarget()) > 9.0D);
      }

      public void startExecuting() {
         this.tickCounter = -10;
         this.theEntity.getNavigator().clearPathEntity();
         this.theEntity.getLookHelper().setLookPositionWithEntity(this.theEntity.getAttackTarget(), 90.0F, 90.0F);
         this.theEntity.isAirBorne = true;
      }

      public void resetTask() {
         this.theEntity.setTargetedEntity(0);
         this.theEntity.setAttackTarget((EntityLivingBase)null);
         this.theEntity.wander.makeUpdate();
      }

      public void updateTask() {
         EntityLivingBase var1 = this.theEntity.getAttackTarget();
         this.theEntity.getNavigator().clearPathEntity();
         this.theEntity.getLookHelper().setLookPositionWithEntity(var1, 90.0F, 90.0F);
         if (!this.theEntity.canEntityBeSeen(var1)) {
            this.theEntity.setAttackTarget((EntityLivingBase)null);
         } else {
            ++this.tickCounter;
            if (this.tickCounter == 0) {
               this.theEntity.setTargetedEntity(this.theEntity.getAttackTarget().getEntityId());
               this.theEntity.world.setEntityState(this.theEntity, (byte)21);
            } else if (this.tickCounter >= this.theEntity.getAttackDuration()) {
               float var2 = 1.0F;
               if (this.theEntity.world.getDifficulty() == EnumDifficulty.HARD) {
                  var2 += 2.0F;
               }

               if (this.theEntity.isElder()) {
                  var2 += 2.0F;
               }

               var1.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this.theEntity, this.theEntity), var2);
               var1.attackEntityFrom(DamageSource.causeMobDamage(this.theEntity), (float)this.theEntity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
               this.theEntity.setAttackTarget((EntityLivingBase)null);
            }

            super.updateTask();
         }
      }
   }

   static class GuardianMoveHelper extends EntityMoveHelper {
      private final EntityGuardian entityGuardian;

      public GuardianMoveHelper(EntityGuardian var1) {
         super(var1);
         this.entityGuardian = var1;
      }

      public void onUpdateMoveHelper() {
         if (this.action == EntityMoveHelper.Action.MOVE_TO && !this.entityGuardian.getNavigator().noPath()) {
            double var1 = this.posX - this.entityGuardian.posX;
            double var3 = this.posY - this.entityGuardian.posY;
            double var5 = this.posZ - this.entityGuardian.posZ;
            double var7 = var1 * var1 + var3 * var3 + var5 * var5;
            var7 = (double)MathHelper.sqrt(var7);
            var3 = var3 / var7;
            float var9 = (float)(MathHelper.atan2(var5, var1) * 57.2957763671875D) - 90.0F;
            this.entityGuardian.rotationYaw = this.limitAngle(this.entityGuardian.rotationYaw, var9, 90.0F);
            this.entityGuardian.renderYawOffset = this.entityGuardian.rotationYaw;
            float var10 = (float)(this.speed * this.entityGuardian.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
            this.entityGuardian.setAIMoveSpeed(this.entityGuardian.getAIMoveSpeed() + (var10 - this.entityGuardian.getAIMoveSpeed()) * 0.125F);
            double var11 = Math.sin((double)(this.entityGuardian.ticksExisted + this.entityGuardian.getEntityId()) * 0.5D) * 0.05D;
            double var13 = Math.cos((double)(this.entityGuardian.rotationYaw * 0.017453292F));
            double var15 = Math.sin((double)(this.entityGuardian.rotationYaw * 0.017453292F));
            this.entityGuardian.motionX += var11 * var13;
            this.entityGuardian.motionZ += var11 * var15;
            var11 = Math.sin((double)(this.entityGuardian.ticksExisted + this.entityGuardian.getEntityId()) * 0.75D) * 0.05D;
            this.entityGuardian.motionY += var11 * (var15 + var13) * 0.25D;
            this.entityGuardian.motionY += (double)this.entityGuardian.getAIMoveSpeed() * var3 * 0.1D;
            EntityLookHelper var17 = this.entityGuardian.getLookHelper();
            double var18 = this.entityGuardian.posX + var1 / var7 * 2.0D;
            double var20 = (double)this.entityGuardian.getEyeHeight() + this.entityGuardian.posY + var3 / var7;
            double var22 = this.entityGuardian.posZ + var5 / var7 * 2.0D;
            double var24 = var17.getLookPosX();
            double var26 = var17.getLookPosY();
            double var28 = var17.getLookPosZ();
            if (!var17.getIsLooking()) {
               var24 = var18;
               var26 = var20;
               var28 = var22;
            }

            this.entityGuardian.getLookHelper().setLookPosition(var24 + (var18 - var24) * 0.125D, var26 + (var20 - var26) * 0.125D, var28 + (var22 - var28) * 0.125D, 10.0F, 40.0F);
            this.entityGuardian.setMoving(true);
         } else {
            this.entityGuardian.setAIMoveSpeed(0.0F);
            this.entityGuardian.setMoving(false);
         }
      }
   }

   static class GuardianTargetSelector implements Predicate {
      private final EntityGuardian parentEntity;

      public GuardianTargetSelector(EntityGuardian var1) {
         this.parentEntity = var1;
      }

      public boolean apply(@Nullable EntityLivingBase var1) {
         return (var1 instanceof EntityPlayer || var1 instanceof EntitySquid) && var1.getDistanceSqToEntity(this.parentEntity) > 9.0D;
      }

      // $FF: synthetic method
      public boolean apply(Object var1) {
         return this.apply((EntityLivingBase)var1);
      }
   }
}
