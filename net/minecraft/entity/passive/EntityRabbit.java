package net.minecraft.entity.passive;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCarrot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIMoveToBlock;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityJumpHelper;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDesert;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityRabbit extends EntityAnimal {
   private static final DataParameter RABBIT_TYPE = EntityDataManager.createKey(EntityRabbit.class, DataSerializers.VARINT);
   private int jumpTicks;
   private int jumpDuration;
   private boolean wasOnGround;
   private int currentMoveTypeDuration;
   private int carrotTicks;

   public EntityRabbit(World var1) {
      super(var1);
      this.setSize(0.4F, 0.5F);
      this.jumpHelper = new EntityRabbit.RabbitJumpHelper(this);
      this.moveHelper = new EntityRabbit.RabbitMoveHelper(this);
      this.setMovementSpeed(0.0D);
   }

   protected void initEntityAI() {
      this.tasks.addTask(1, new EntityAISwimming(this));
      this.tasks.addTask(1, new EntityRabbit.AIPanic(this, 2.2D));
      this.tasks.addTask(2, new EntityAIMate(this, 0.8D));
      this.tasks.addTask(3, new EntityAITempt(this, 1.0D, Items.CARROT, false));
      this.tasks.addTask(3, new EntityAITempt(this, 1.0D, Items.GOLDEN_CARROT, false));
      this.tasks.addTask(3, new EntityAITempt(this, 1.0D, Item.getItemFromBlock(Blocks.YELLOW_FLOWER), false));
      this.tasks.addTask(4, new EntityRabbit.AIAvoidEntity(this, EntityPlayer.class, 8.0F, 2.2D, 2.2D));
      this.tasks.addTask(4, new EntityRabbit.AIAvoidEntity(this, EntityWolf.class, 10.0F, 2.2D, 2.2D));
      this.tasks.addTask(4, new EntityRabbit.AIAvoidEntity(this, EntityMob.class, 4.0F, 2.2D, 2.2D));
      this.tasks.addTask(5, new EntityRabbit.AIRaidFarm(this));
      this.tasks.addTask(6, new EntityAIWander(this, 0.6D));
      this.tasks.addTask(11, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F));
   }

   protected float getJumpUpwardsMotion() {
      if (!this.isCollidedHorizontally && (!this.moveHelper.isUpdating() || this.moveHelper.getY() <= this.posY + 0.5D)) {
         Path var1 = this.navigator.getPath();
         if (var1 != null && var1.getCurrentPathIndex() < var1.getCurrentPathLength()) {
            Vec3d var2 = var1.getPosition(this);
            if (var2.yCoord > this.posY) {
               return 0.5F;
            }
         }

         return this.moveHelper.getSpeed() <= 0.6D ? 0.2F : 0.3F;
      } else {
         return 0.5F;
      }
   }

   protected void jump() {
      super.jump();
      double var1 = this.moveHelper.getSpeed();
      if (var1 > 0.0D) {
         double var3 = this.motionX * this.motionX + this.motionZ * this.motionZ;
         if (var3 < 0.010000000000000002D) {
            this.moveRelative(0.0F, 1.0F, 0.1F);
         }
      }

      if (!this.world.isRemote) {
         this.world.setEntityState(this, (byte)1);
      }

   }

   @SideOnly(Side.CLIENT)
   public float setJumpCompletion(float var1) {
      return this.jumpDuration == 0 ? 0.0F : ((float)this.jumpTicks + var1) / (float)this.jumpDuration;
   }

   public void setMovementSpeed(double var1) {
      this.getNavigator().setSpeed(var1);
      this.moveHelper.setMoveTo(this.moveHelper.getX(), this.moveHelper.getY(), this.moveHelper.getZ(), var1);
   }

   public void setJumping(boolean var1) {
      super.setJumping(var1);
      if (var1) {
         this.playSound(this.getJumpSound(), this.getSoundVolume(), ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 0.8F);
      }

   }

   public void startJumping() {
      this.setJumping(true);
      this.jumpDuration = 10;
      this.jumpTicks = 0;
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(RABBIT_TYPE, Integer.valueOf(0));
   }

   public void updateAITasks() {
      if (this.currentMoveTypeDuration > 0) {
         --this.currentMoveTypeDuration;
      }

      if (this.carrotTicks > 0) {
         this.carrotTicks -= this.rand.nextInt(3);
         if (this.carrotTicks < 0) {
            this.carrotTicks = 0;
         }
      }

      if (this.onGround) {
         if (!this.wasOnGround) {
            this.setJumping(false);
            this.checkLandingDelay();
         }

         if (this.getRabbitType() == 99 && this.currentMoveTypeDuration == 0) {
            EntityLivingBase var1 = this.getAttackTarget();
            if (var1 != null && this.getDistanceSqToEntity(var1) < 16.0D) {
               this.calculateRotationYaw(var1.posX, var1.posZ);
               this.moveHelper.setMoveTo(var1.posX, var1.posY, var1.posZ, this.moveHelper.getSpeed());
               this.startJumping();
               this.wasOnGround = true;
            }
         }

         EntityRabbit.RabbitJumpHelper var4 = (EntityRabbit.RabbitJumpHelper)this.jumpHelper;
         if (!var4.getIsJumping()) {
            if (this.moveHelper.isUpdating() && this.currentMoveTypeDuration == 0) {
               Path var2 = this.navigator.getPath();
               Vec3d var3 = new Vec3d(this.moveHelper.getX(), this.moveHelper.getY(), this.moveHelper.getZ());
               if (var2 != null && var2.getCurrentPathIndex() < var2.getCurrentPathLength()) {
                  var3 = var2.getPosition(this);
               }

               this.calculateRotationYaw(var3.xCoord, var3.zCoord);
               this.startJumping();
            }
         } else if (!var4.canJump()) {
            this.enableJumpControl();
         }
      }

      this.wasOnGround = this.onGround;
   }

   public void spawnRunningParticles() {
   }

   private void calculateRotationYaw(double var1, double var3) {
      this.rotationYaw = (float)(MathHelper.atan2(var3 - this.posZ, var1 - this.posX) * 57.29577951308232D) - 90.0F;
   }

   private void enableJumpControl() {
      ((EntityRabbit.RabbitJumpHelper)this.jumpHelper).setCanJump(true);
   }

   private void disableJumpControl() {
      ((EntityRabbit.RabbitJumpHelper)this.jumpHelper).setCanJump(false);
   }

   private void updateMoveTypeDuration() {
      if (this.moveHelper.getSpeed() < 2.2D) {
         this.currentMoveTypeDuration = 10;
      } else {
         this.currentMoveTypeDuration = 1;
      }

   }

   private void checkLandingDelay() {
      this.updateMoveTypeDuration();
      this.disableJumpControl();
   }

   public void onLivingUpdate() {
      super.onLivingUpdate();
      if (this.jumpTicks != this.jumpDuration) {
         ++this.jumpTicks;
      } else if (this.jumpDuration != 0) {
         this.jumpTicks = 0;
         this.jumpDuration = 0;
         this.setJumping(false);
      }

   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(3.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.30000001192092896D);
   }

   public static void registerFixesRabbit(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "Rabbit");
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setInteger("RabbitType", this.getRabbitType());
      var1.setInteger("MoreCarrotTicks", this.carrotTicks);
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.setRabbitType(var1.getInteger("RabbitType"));
      this.carrotTicks = var1.getInteger("MoreCarrotTicks");
   }

   protected SoundEvent getJumpSound() {
      return SoundEvents.ENTITY_RABBIT_JUMP;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_RABBIT_AMBIENT;
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_RABBIT_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_RABBIT_DEATH;
   }

   public boolean attackEntityAsMob(Entity var1) {
      if (this.getRabbitType() == 99) {
         this.playSound(SoundEvents.ENTITY_RABBIT_ATTACK, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
         return var1.attackEntityFrom(DamageSource.causeMobDamage(this), 8.0F);
      } else {
         return var1.attackEntityFrom(DamageSource.causeMobDamage(this), 3.0F);
      }
   }

   public SoundCategory getSoundCategory() {
      return this.getRabbitType() == 99 ? SoundCategory.HOSTILE : SoundCategory.NEUTRAL;
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      return this.isEntityInvulnerable(var1) ? false : super.attackEntityFrom(var1, var2);
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_RABBIT;
   }

   private boolean isRabbitBreedingItem(Item var1) {
      return var1 == Items.CARROT || var1 == Items.GOLDEN_CARROT || var1 == Item.getItemFromBlock(Blocks.YELLOW_FLOWER);
   }

   public EntityRabbit createChild(EntityAgeable var1) {
      EntityRabbit var2 = new EntityRabbit(this.world);
      int var3 = this.getRandomRabbitType();
      if (this.rand.nextInt(20) != 0) {
         if (var1 instanceof EntityRabbit && this.rand.nextBoolean()) {
            var3 = ((EntityRabbit)var1).getRabbitType();
         } else {
            var3 = this.getRabbitType();
         }
      }

      var2.setRabbitType(var3);
      return var2;
   }

   public boolean isBreedingItem(@Nullable ItemStack var1) {
      return var1 != null && this.isRabbitBreedingItem(var1.getItem());
   }

   public int getRabbitType() {
      return ((Integer)this.dataManager.get(RABBIT_TYPE)).intValue();
   }

   public void setRabbitType(int var1) {
      if (var1 == 99) {
         this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(8.0D);
         this.tasks.addTask(4, new EntityRabbit.AIEvilAttack(this));
         this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
         this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
         this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityWolf.class, true));
         if (!this.hasCustomName()) {
            this.setCustomNameTag(I18n.translateToLocal("entity.KillerBunny.name"));
         }
      }

      this.dataManager.set(RABBIT_TYPE, Integer.valueOf(var1));
   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance var1, @Nullable IEntityLivingData var2) {
      var2 = super.onInitialSpawn(var1, var2);
      int var3 = this.getRandomRabbitType();
      boolean var4 = false;
      if (var2 instanceof EntityRabbit.RabbitTypeData) {
         var3 = ((EntityRabbit.RabbitTypeData)var2).typeData;
         var4 = true;
      } else {
         var2 = new EntityRabbit.RabbitTypeData(var3);
      }

      this.setRabbitType(var3);
      if (var4) {
         this.setGrowingAge(-24000);
      }

      return var2;
   }

   private int getRandomRabbitType() {
      Biome var1 = this.world.getBiome(new BlockPos(this));
      int var2 = this.rand.nextInt(100);
      return var1.isSnowyBiome() ? (var2 < 80 ? 1 : 3) : (var1 instanceof BiomeDesert ? 4 : (var2 < 50 ? 0 : (var2 < 90 ? 5 : 2)));
   }

   private boolean isCarrotEaten() {
      return this.carrotTicks == 0;
   }

   protected void createEatingParticles() {
      BlockCarrot var1 = (BlockCarrot)Blocks.CARROTS;
      IBlockState var2 = var1.withAge(var1.getMaxAge());
      this.world.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, 0.0D, 0.0D, 0.0D, Block.getStateId(var2));
      this.carrotTicks = 40;
   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte var1) {
      if (var1 == 1) {
         this.createRunningParticles();
         this.jumpDuration = 10;
         this.jumpTicks = 0;
      } else {
         super.handleStatusUpdate(var1);
      }

   }

   public void notifyDataManagerChange(DataParameter var1) {
      super.notifyDataManagerChange(var1);
   }

   static class AIAvoidEntity extends EntityAIAvoidEntity {
      private final EntityRabbit entityInstance;

      public AIAvoidEntity(EntityRabbit var1, Class var2, float var3, double var4, double var6) {
         super(var1, var2, var3, var4, var6);
         this.entityInstance = var1;
      }

      public boolean shouldExecute() {
         return this.entityInstance.getRabbitType() != 99 && super.shouldExecute();
      }
   }

   static class AIEvilAttack extends EntityAIAttackMelee {
      public AIEvilAttack(EntityRabbit var1) {
         super(var1, 1.4D, true);
      }

      protected double getAttackReachSqr(EntityLivingBase var1) {
         return (double)(4.0F + var1.width);
      }
   }

   static class AIPanic extends EntityAIPanic {
      private final EntityRabbit theEntity;

      public AIPanic(EntityRabbit var1, double var2) {
         super(var1, var2);
         this.theEntity = var1;
      }

      public void updateTask() {
         super.updateTask();
         this.theEntity.setMovementSpeed(this.speed);
      }
   }

   static class AIRaidFarm extends EntityAIMoveToBlock {
      private final EntityRabbit rabbit;
      private boolean wantsToRaid;
      private boolean canRaid;

      public AIRaidFarm(EntityRabbit var1) {
         super(var1, 0.699999988079071D, 16);
         this.rabbit = var1;
      }

      public boolean shouldExecute() {
         if (this.runDelay <= 0) {
            if (!this.rabbit.world.getGameRules().getBoolean("mobGriefing")) {
               return false;
            }

            this.canRaid = false;
            this.wantsToRaid = this.rabbit.isCarrotEaten();
            this.wantsToRaid = true;
         }

         return super.shouldExecute();
      }

      public boolean continueExecuting() {
         return this.canRaid && super.continueExecuting();
      }

      public void startExecuting() {
         super.startExecuting();
      }

      public void resetTask() {
         super.resetTask();
      }

      public void updateTask() {
         super.updateTask();
         this.rabbit.getLookHelper().setLookPosition((double)this.destinationBlock.getX() + 0.5D, (double)(this.destinationBlock.getY() + 1), (double)this.destinationBlock.getZ() + 0.5D, 10.0F, (float)this.rabbit.getVerticalFaceSpeed());
         if (this.getIsAboveDestination()) {
            World var1 = this.rabbit.world;
            BlockPos var2 = this.destinationBlock.up();
            IBlockState var3 = var1.getBlockState(var2);
            Block var4 = var3.getBlock();
            if (this.canRaid && var4 instanceof BlockCarrot) {
               Integer var5 = (Integer)var3.getValue(BlockCarrot.AGE);
               if (var5.intValue() == 0) {
                  var1.setBlockState(var2, Blocks.AIR.getDefaultState(), 2);
                  var1.destroyBlock(var2, true);
               } else {
                  var1.setBlockState(var2, var3.withProperty(BlockCarrot.AGE, Integer.valueOf(var5.intValue() - 1)), 2);
                  var1.playEvent(2001, var2, Block.getStateId(var3));
               }

               this.rabbit.createEatingParticles();
            }

            this.canRaid = false;
            this.runDelay = 10;
         }

      }

      protected boolean shouldMoveTo(World var1, BlockPos var2) {
         Block var3 = var1.getBlockState(var2).getBlock();
         if (var3 == Blocks.FARMLAND && this.wantsToRaid && !this.canRaid) {
            var2 = var2.up();
            IBlockState var4 = var1.getBlockState(var2);
            var3 = var4.getBlock();
            if (var3 instanceof BlockCarrot && ((BlockCarrot)var3).isMaxAge(var4)) {
               this.canRaid = true;
               return true;
            }
         }

         return false;
      }
   }

   public class RabbitJumpHelper extends EntityJumpHelper {
      private final EntityRabbit theEntity;
      private boolean canJump;

      public RabbitJumpHelper(EntityRabbit var2) {
         super(var2);
         this.theEntity = var2;
      }

      public boolean getIsJumping() {
         return this.isJumping;
      }

      public boolean canJump() {
         return this.canJump;
      }

      public void setCanJump(boolean var1) {
         this.canJump = var1;
      }

      public void doJump() {
         if (this.isJumping) {
            this.theEntity.startJumping();
            this.isJumping = false;
         }

      }
   }

   static class RabbitMoveHelper extends EntityMoveHelper {
      private final EntityRabbit theEntity;
      private double nextJumpSpeed;

      public RabbitMoveHelper(EntityRabbit var1) {
         super(var1);
         this.theEntity = var1;
      }

      public void onUpdateMoveHelper() {
         if (this.theEntity.onGround && !this.theEntity.isJumping && !((EntityRabbit.RabbitJumpHelper)this.theEntity.jumpHelper).getIsJumping()) {
            this.theEntity.setMovementSpeed(0.0D);
         } else if (this.isUpdating()) {
            this.theEntity.setMovementSpeed(this.nextJumpSpeed);
         }

         super.onUpdateMoveHelper();
      }

      public void setMoveTo(double var1, double var3, double var5, double var7) {
         if (this.theEntity.isInWater()) {
            var7 = 1.5D;
         }

         super.setMoveTo(var1, var3, var5, var7);
         if (var7 > 0.0D) {
            this.nextJumpSpeed = var7;
         }

      }
   }

   public static class RabbitTypeData implements IEntityLivingData {
      public int typeData;

      public RabbitTypeData(int var1) {
         this.typeData = var1;
      }
   }
}
