package net.minecraft.entity.monster;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateClimber;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class EntitySpider extends EntityMob {
   private static final DataParameter CLIMBING = EntityDataManager.createKey(EntitySpider.class, DataSerializers.BYTE);

   public EntitySpider(World var1) {
      super(var1);
      this.setSize(1.4F, 0.9F);
   }

   public static void registerFixesSpider(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "Spider");
   }

   protected void initEntityAI() {
      this.tasks.addTask(1, new EntityAISwimming(this));
      this.tasks.addTask(3, new EntityAILeapAtTarget(this, 0.4F));
      this.tasks.addTask(4, new EntitySpider.AISpiderAttack(this));
      this.tasks.addTask(5, new EntityAIWander(this, 0.8D));
      this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.tasks.addTask(6, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
      this.targetTasks.addTask(2, new EntitySpider.AISpiderTarget(this, EntityPlayer.class));
      this.targetTasks.addTask(3, new EntitySpider.AISpiderTarget(this, EntityIronGolem.class));
   }

   public double getMountedYOffset() {
      return (double)(this.height * 0.5F);
   }

   protected PathNavigate createNavigator(World var1) {
      return new PathNavigateClimber(this, var1);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(CLIMBING, Byte.valueOf((byte)0));
   }

   public void onUpdate() {
      super.onUpdate();
      if (!this.world.isRemote) {
         this.setBesideClimbableBlock(this.isCollidedHorizontally);
      }

   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.30000001192092896D);
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_SPIDER_AMBIENT;
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_SPIDER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_SPIDER_DEATH;
   }

   protected void playStepSound(BlockPos var1, Block var2) {
      this.playSound(SoundEvents.ENTITY_SPIDER_STEP, 0.15F, 1.0F);
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_SPIDER;
   }

   public boolean isOnLadder() {
      return this.isBesideClimbableBlock();
   }

   public void setInWeb() {
   }

   public EnumCreatureAttribute getCreatureAttribute() {
      return EnumCreatureAttribute.ARTHROPOD;
   }

   public boolean isPotionApplicable(PotionEffect var1) {
      return var1.getPotion() == MobEffects.POISON ? false : super.isPotionApplicable(var1);
   }

   public boolean isBesideClimbableBlock() {
      return (((Byte)this.dataManager.get(CLIMBING)).byteValue() & 1) != 0;
   }

   public void setBesideClimbableBlock(boolean var1) {
      byte var2 = ((Byte)this.dataManager.get(CLIMBING)).byteValue();
      if (var1) {
         var2 = (byte)(var2 | 1);
      } else {
         var2 = (byte)(var2 & -2);
      }

      this.dataManager.set(CLIMBING, Byte.valueOf(var2));
   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance var1, @Nullable IEntityLivingData var2) {
      Object var3 = super.onInitialSpawn(var1, var2);
      if (this.world.rand.nextInt(100) == 0) {
         EntitySkeleton var4 = new EntitySkeleton(this.world);
         var4.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
         var4.onInitialSpawn(var1, (IEntityLivingData)null);
         this.world.addEntity(var4, SpawnReason.JOCKEY);
         var4.startRiding(this);
      }

      if (var3 == null) {
         var3 = new EntitySpider.GroupData();
         if (this.world.getDifficulty() == EnumDifficulty.HARD && this.world.rand.nextFloat() < 0.1F * var1.getClampedAdditionalDifficulty()) {
            ((EntitySpider.GroupData)var3).setRandomEffect(this.world.rand);
         }
      }

      if (var3 instanceof EntitySpider.GroupData) {
         Potion var5 = ((EntitySpider.GroupData)var3).effect;
         if (var5 != null) {
            this.addPotionEffect(new PotionEffect(var5, Integer.MAX_VALUE));
         }
      }

      return (IEntityLivingData)var3;
   }

   public float getEyeHeight() {
      return 0.65F;
   }

   static class AISpiderAttack extends EntityAIAttackMelee {
      public AISpiderAttack(EntitySpider var1) {
         super(var1, 1.0D, true);
      }

      public boolean continueExecuting() {
         float var1 = this.attacker.getBrightness(1.0F);
         if (var1 >= 0.5F && this.attacker.getRNG().nextInt(100) == 0) {
            this.attacker.setAttackTarget((EntityLivingBase)null);
            return false;
         } else {
            return super.continueExecuting();
         }
      }

      protected double getAttackReachSqr(EntityLivingBase var1) {
         return (double)(4.0F + var1.width);
      }
   }

   static class AISpiderTarget extends EntityAINearestAttackableTarget {
      public AISpiderTarget(EntitySpider var1, Class var2) {
         super(var1, var2, true);
      }

      public boolean shouldExecute() {
         float var1 = this.taskOwner.getBrightness(1.0F);
         return var1 >= 0.5F ? false : super.shouldExecute();
      }
   }

   public static class GroupData implements IEntityLivingData {
      public Potion effect;

      public void setRandomEffect(Random var1) {
         int var2 = var1.nextInt(5);
         if (var2 <= 1) {
            this.effect = MobEffects.SPEED;
         } else if (var2 <= 2) {
            this.effect = MobEffects.STRENGTH;
         } else if (var2 <= 3) {
            this.effect = MobEffects.REGENERATION;
         } else if (var2 <= 4) {
            this.effect = MobEffects.INVISIBILITY;
         }

      }
   }
}
