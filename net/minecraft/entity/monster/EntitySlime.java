package net.minecraft.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFindEntityNearest;
import net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.loot.LootTableList;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class EntitySlime extends EntityLiving implements IMob {
   private static final DataParameter SLIME_SIZE = EntityDataManager.createKey(EntitySlime.class, DataSerializers.VARINT);
   public float squishAmount;
   public float squishFactor;
   public float prevSquishFactor;
   private boolean wasOnGround;

   public EntitySlime(World var1) {
      super(var1);
      this.moveHelper = new EntitySlime.SlimeMoveHelper(this);
   }

   protected void initEntityAI() {
      this.tasks.addTask(1, new EntitySlime.AISlimeFloat(this));
      this.tasks.addTask(2, new EntitySlime.AISlimeAttack(this));
      this.tasks.addTask(3, new EntitySlime.AISlimeFaceRandom(this));
      this.tasks.addTask(5, new EntitySlime.AISlimeHop(this));
      this.targetTasks.addTask(1, new EntityAIFindEntityNearestPlayer(this));
      this.targetTasks.addTask(3, new EntityAIFindEntityNearest(this, EntityIronGolem.class));
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(SLIME_SIZE, Integer.valueOf(1));
   }

   public void setSlimeSize(int var1) {
      this.dataManager.set(SLIME_SIZE, Integer.valueOf(var1));
      this.setSize(0.51000005F * (float)var1, 0.51000005F * (float)var1);
      this.setPosition(this.posX, this.posY, this.posZ);
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)(var1 * var1));
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((double)(0.2F + 0.1F * (float)var1));
      this.setHealth(this.getMaxHealth());
      this.experienceValue = var1;
   }

   public int getSlimeSize() {
      return ((Integer)this.dataManager.get(SLIME_SIZE)).intValue();
   }

   public static void registerFixesSlime(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "Slime");
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setInteger("Size", this.getSlimeSize() - 1);
      var1.setBoolean("wasOnGround", this.wasOnGround);
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      int var2 = var1.getInteger("Size");
      if (var2 < 0) {
         var2 = 0;
      }

      this.setSlimeSize(var2 + 1);
      this.wasOnGround = var1.getBoolean("wasOnGround");
   }

   public boolean isSmallSlime() {
      return this.getSlimeSize() <= 1;
   }

   protected EnumParticleTypes getParticleType() {
      return EnumParticleTypes.SLIME;
   }

   public void onUpdate() {
      if (!this.world.isRemote && this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.getSlimeSize() > 0) {
         this.isDead = true;
      }

      this.squishFactor += (this.squishAmount - this.squishFactor) * 0.5F;
      this.prevSquishFactor = this.squishFactor;
      super.onUpdate();
      if (this.onGround && !this.wasOnGround) {
         int var1 = this.getSlimeSize();

         for(int var2 = 0; var2 < var1 * 8; ++var2) {
            float var3 = this.rand.nextFloat() * 6.2831855F;
            float var4 = this.rand.nextFloat() * 0.5F + 0.5F;
            float var5 = MathHelper.sin(var3) * (float)var1 * 0.5F * var4;
            float var6 = MathHelper.cos(var3) * (float)var1 * 0.5F * var4;
            World var7 = this.world;
            EnumParticleTypes var8 = this.getParticleType();
            double var9 = this.posX + (double)var5;
            double var11 = this.posZ + (double)var6;
            var7.spawnParticle(var8, var9, this.getEntityBoundingBox().minY, var11, 0.0D, 0.0D, 0.0D);
         }

         this.playSound(this.getSquishSound(), this.getSoundVolume(), ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) / 0.8F);
         this.squishAmount = -0.5F;
      } else if (!this.onGround && this.wasOnGround) {
         this.squishAmount = 1.0F;
      }

      this.wasOnGround = this.onGround;
      this.alterSquishAmount();
   }

   protected void alterSquishAmount() {
      this.squishAmount *= 0.6F;
   }

   protected int getJumpDelay() {
      return this.rand.nextInt(20) + 10;
   }

   protected EntitySlime createInstance() {
      return new EntitySlime(this.world);
   }

   public void notifyDataManagerChange(DataParameter var1) {
      if (SLIME_SIZE.equals(var1)) {
         int var2 = this.getSlimeSize();
         this.setSize(0.51000005F * (float)var2, 0.51000005F * (float)var2);
         this.rotationYaw = this.rotationYawHead;
         this.renderYawOffset = this.rotationYawHead;
         if (this.isInWater() && this.rand.nextInt(20) == 0) {
            this.resetHeight();
         }
      }

      super.notifyDataManagerChange(var1);
   }

   public void setDead() {
      int var1 = this.getSlimeSize();
      if (!this.world.isRemote && var1 > 1 && this.getHealth() <= 0.0F) {
         int var2 = 2 + this.rand.nextInt(3);
         SlimeSplitEvent var3 = new SlimeSplitEvent((Slime)this.getBukkitEntity(), var2);
         this.world.getServer().getPluginManager().callEvent(var3);
         if (var3.isCancelled() || var3.getCount() <= 0) {
            super.setDead();
            return;
         }

         var2 = var3.getCount();

         for(int var4 = 0; var4 < var2; ++var4) {
            float var5 = ((float)(var4 % 2) - 0.5F) * (float)var1 / 4.0F;
            float var6 = ((float)(var4 / 2) - 0.5F) * (float)var1 / 4.0F;
            EntitySlime var7 = this.createInstance();
            if (this.hasCustomName()) {
               var7.setCustomNameTag(this.getCustomNameTag());
            }

            if (this.isNoDespawnRequired()) {
               var7.enablePersistence();
            }

            var7.setSlimeSize(var1 / 2);
            var7.setLocationAndAngles(this.posX + (double)var5, this.posY + 0.5D, this.posZ + (double)var6, this.rand.nextFloat() * 360.0F, 0.0F);
            this.world.addEntity(var7, SpawnReason.SLIME_SPLIT);
         }
      }

      super.setDead();
   }

   public void applyEntityCollision(Entity var1) {
      super.applyEntityCollision(var1);
      if (var1 instanceof EntityIronGolem && this.canDamagePlayer()) {
         this.dealDamage((EntityLivingBase)var1);
      }

   }

   public void onCollideWithPlayer(EntityPlayer var1) {
      if (this.canDamagePlayer()) {
         this.dealDamage(var1);
      }

   }

   protected void dealDamage(EntityLivingBase var1) {
      int var2 = this.getSlimeSize();
      if (this.canEntityBeSeen(var1) && this.getDistanceSqToEntity(var1) < 0.6D * (double)var2 * 0.6D * (double)var2 && var1.attackEntityFrom(DamageSource.causeMobDamage(this), (float)this.getAttackStrength())) {
         this.playSound(SoundEvents.ENTITY_SLIME_ATTACK, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
         this.applyEnchantments(this, var1);
      }

   }

   public float getEyeHeight() {
      return 0.625F * this.height;
   }

   protected boolean canDamagePlayer() {
      return !this.isSmallSlime();
   }

   protected int getAttackStrength() {
      return this.getSlimeSize();
   }

   protected SoundEvent getHurtSound() {
      return this.isSmallSlime() ? SoundEvents.ENTITY_SMALL_SLIME_HURT : SoundEvents.ENTITY_SLIME_HURT;
   }

   protected SoundEvent getDeathSound() {
      return this.isSmallSlime() ? SoundEvents.ENTITY_SMALL_SLIME_DEATH : SoundEvents.ENTITY_SLIME_DEATH;
   }

   protected SoundEvent getSquishSound() {
      return this.isSmallSlime() ? SoundEvents.ENTITY_SMALL_SLIME_SQUISH : SoundEvents.ENTITY_SLIME_SQUISH;
   }

   protected Item getDropItem() {
      return this.getSlimeSize() == 1 ? Items.SLIME_BALL : null;
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return this.getSlimeSize() == 1 ? LootTableList.ENTITIES_SLIME : LootTableList.EMPTY;
   }

   public boolean getCanSpawnHere() {
      BlockPos var1 = new BlockPos(MathHelper.floor(this.posX), 0, MathHelper.floor(this.posZ));
      Chunk var2 = this.world.getChunkFromBlockCoords(var1);
      if (this.world.getWorldInfo().getTerrainType() == WorldType.FLAT && this.rand.nextInt(4) != 1) {
         return false;
      } else {
         if (this.world.getDifficulty() != EnumDifficulty.PEACEFUL) {
            Biome var3 = this.world.getBiome(var1);
            if (var3 == Biomes.SWAMPLAND && this.posY > 50.0D && this.posY < 70.0D && this.rand.nextFloat() < 0.5F && this.rand.nextFloat() < this.world.getCurrentMoonPhaseFactor() && this.world.getLightFromNeighbors(new BlockPos(this)) <= this.rand.nextInt(8)) {
               return super.getCanSpawnHere();
            }

            if (this.rand.nextInt(10) == 0 && var2.getRandomWithSeed(987234911L).nextInt(10) == 0 && this.posY < 40.0D) {
               return super.getCanSpawnHere();
            }
         }

         return false;
      }
   }

   protected float getSoundVolume() {
      return 0.4F * (float)this.getSlimeSize();
   }

   public int getVerticalFaceSpeed() {
      return 0;
   }

   protected boolean makesSoundOnJump() {
      return this.getSlimeSize() > 0;
   }

   protected void jump() {
      this.motionY = 0.41999998688697815D;
      this.isAirBorne = true;
   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance var1, @Nullable IEntityLivingData var2) {
      int var3 = this.rand.nextInt(3);
      if (var3 < 2 && this.rand.nextFloat() < 0.5F * var1.getClampedAdditionalDifficulty()) {
         ++var3;
      }

      int var4 = 1 << var3;
      this.setSlimeSize(var4);
      return super.onInitialSpawn(var1, var2);
   }

   protected SoundEvent getJumpSound() {
      return this.isSmallSlime() ? SoundEvents.ENTITY_SMALL_SLIME_JUMP : SoundEvents.ENTITY_SLIME_JUMP;
   }

   static class AISlimeAttack extends EntityAIBase {
      private final EntitySlime slime;
      private int growTieredTimer;

      public AISlimeAttack(EntitySlime var1) {
         this.slime = var1;
         this.setMutexBits(2);
      }

      public boolean shouldExecute() {
         EntityLivingBase var1 = this.slime.getAttackTarget();
         return var1 == null ? false : (!var1.isEntityAlive() ? false : !(var1 instanceof EntityPlayer) || !((EntityPlayer)var1).capabilities.disableDamage);
      }

      public void startExecuting() {
         this.growTieredTimer = 300;
         super.startExecuting();
      }

      public boolean continueExecuting() {
         EntityLivingBase var1 = this.slime.getAttackTarget();
         return var1 == null ? false : (!var1.isEntityAlive() ? false : (var1 instanceof EntityPlayer && ((EntityPlayer)var1).capabilities.disableDamage ? false : --this.growTieredTimer > 0));
      }

      public void updateTask() {
         this.slime.faceEntity(this.slime.getAttackTarget(), 10.0F, 10.0F);
         ((EntitySlime.SlimeMoveHelper)this.slime.getMoveHelper()).setDirection(this.slime.rotationYaw, this.slime.canDamagePlayer());
      }
   }

   static class AISlimeFaceRandom extends EntityAIBase {
      private final EntitySlime slime;
      private float chosenDegrees;
      private int nextRandomizeTime;

      public AISlimeFaceRandom(EntitySlime var1) {
         this.slime = var1;
         this.setMutexBits(2);
      }

      public boolean shouldExecute() {
         return this.slime.getAttackTarget() == null && (this.slime.onGround || this.slime.isInWater() || this.slime.isInLava() || this.slime.isPotionActive(MobEffects.LEVITATION));
      }

      public void updateTask() {
         if (--this.nextRandomizeTime <= 0) {
            this.nextRandomizeTime = 40 + this.slime.getRNG().nextInt(60);
            this.chosenDegrees = (float)this.slime.getRNG().nextInt(360);
         }

         ((EntitySlime.SlimeMoveHelper)this.slime.getMoveHelper()).setDirection(this.chosenDegrees, false);
      }
   }

   static class AISlimeFloat extends EntityAIBase {
      private final EntitySlime slime;

      public AISlimeFloat(EntitySlime var1) {
         this.slime = var1;
         this.setMutexBits(5);
         ((PathNavigateGround)var1.getNavigator()).setCanSwim(true);
      }

      public boolean shouldExecute() {
         return this.slime.isInWater() || this.slime.isInLava();
      }

      public void updateTask() {
         if (this.slime.getRNG().nextFloat() < 0.8F) {
            this.slime.getJumpHelper().setJumping();
         }

         ((EntitySlime.SlimeMoveHelper)this.slime.getMoveHelper()).setSpeed(1.2D);
      }
   }

   static class AISlimeHop extends EntityAIBase {
      private final EntitySlime slime;

      public AISlimeHop(EntitySlime var1) {
         this.slime = var1;
         this.setMutexBits(5);
      }

      public boolean shouldExecute() {
         return true;
      }

      public void updateTask() {
         ((EntitySlime.SlimeMoveHelper)this.slime.getMoveHelper()).setSpeed(1.0D);
      }
   }

   static class SlimeMoveHelper extends EntityMoveHelper {
      private float yRot;
      private int jumpDelay;
      private final EntitySlime slime;
      private boolean isAggressive;

      public SlimeMoveHelper(EntitySlime var1) {
         super(var1);
         this.slime = var1;
         this.yRot = 180.0F * var1.rotationYaw / 3.1415927F;
      }

      public void setDirection(float var1, boolean var2) {
         this.yRot = var1;
         this.isAggressive = var2;
      }

      public void setSpeed(double var1) {
         this.speed = var1;
         this.action = EntityMoveHelper.Action.MOVE_TO;
      }

      public void onUpdateMoveHelper() {
         this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, this.yRot, 90.0F);
         this.entity.rotationYawHead = this.entity.rotationYaw;
         this.entity.renderYawOffset = this.entity.rotationYaw;
         if (this.action != EntityMoveHelper.Action.MOVE_TO) {
            this.entity.setMoveForward(0.0F);
         } else {
            this.action = EntityMoveHelper.Action.WAIT;
            if (this.entity.onGround) {
               this.entity.setAIMoveSpeed((float)(this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
               if (this.jumpDelay-- <= 0) {
                  this.jumpDelay = this.slime.getJumpDelay();
                  if (this.isAggressive) {
                     this.jumpDelay /= 3;
                  }

                  this.slime.getJumpHelper().setJumping();
                  if (this.slime.makesSoundOnJump()) {
                     this.slime.playSound(this.slime.getJumpSound(), this.slime.getSoundVolume(), ((this.slime.getRNG().nextFloat() - this.slime.getRNG().nextFloat()) * 0.2F + 1.0F) * 0.8F);
                  }
               } else {
                  this.slime.moveStrafing = 0.0F;
                  this.slime.moveForward = 0.0F;
                  this.entity.setAIMoveSpeed(0.0F);
               }
            } else {
               this.entity.setAIMoveSpeed((float)(this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
            }
         }

      }
   }
}
