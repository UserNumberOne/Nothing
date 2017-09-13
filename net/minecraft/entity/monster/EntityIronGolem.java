package net.minecraft.entity.monster;

import com.google.common.base.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIDefendVillage;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookAtVillager;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

public class EntityIronGolem extends EntityGolem {
   protected static final DataParameter PLAYER_CREATED = EntityDataManager.createKey(EntityIronGolem.class, DataSerializers.BYTE);
   private int homeCheckTimer;
   Village villageObj;
   private int attackTimer;
   private int holdRoseTick;

   public EntityIronGolem(World var1) {
      super(var1);
      this.setSize(1.4F, 2.7F);
   }

   protected void initEntityAI() {
      this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.0D, true));
      this.tasks.addTask(2, new EntityAIMoveTowardsTarget(this, 0.9D, 32.0F));
      this.tasks.addTask(3, new EntityAIMoveThroughVillage(this, 0.6D, true));
      this.tasks.addTask(4, new EntityAIMoveTowardsRestriction(this, 1.0D));
      this.tasks.addTask(5, new EntityAILookAtVillager(this));
      this.tasks.addTask(6, new EntityAIWander(this, 0.6D));
      this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
      this.tasks.addTask(8, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAIDefendVillage(this));
      this.targetTasks.addTask(2, new EntityAIHurtByTarget(this, false, new Class[0]));
      this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityLiving.class, 10, false, true, new Predicate() {
         public boolean apply(@Nullable EntityLiving var1) {
            return var1 != null && IMob.VISIBLE_MOB_SELECTOR.apply(var1) && !(var1 instanceof EntityCreeper);
         }

         public boolean apply(Object var1) {
            return this.apply((EntityLiving)var1);
         }
      }));
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(PLAYER_CREATED, Byte.valueOf((byte)0));
   }

   protected void updateAITasks() {
      if (--this.homeCheckTimer <= 0) {
         this.homeCheckTimer = 70 + this.rand.nextInt(50);
         this.villageObj = this.world.getVillageCollection().getNearestVillage(new BlockPos(this), 32);
         if (this.villageObj == null) {
            this.detachHome();
         } else {
            BlockPos var1 = this.villageObj.getCenter();
            this.setHomePosAndDistance(var1, (int)((float)this.villageObj.getVillageRadius() * 0.6F));
         }
      }

      super.updateAITasks();
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
      this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
   }

   protected int decreaseAirSupply(int var1) {
      return var1;
   }

   protected void collideWithEntity(Entity var1) {
      if (var1 instanceof IMob && !(var1 instanceof EntityCreeper) && this.getRNG().nextInt(20) == 0) {
         this.setGoalTarget((EntityLivingBase)var1, TargetReason.COLLISION, true);
      }

      super.collideWithEntity(var1);
   }

   public void onLivingUpdate() {
      super.onLivingUpdate();
      if (this.attackTimer > 0) {
         --this.attackTimer;
      }

      if (this.holdRoseTick > 0) {
         --this.holdRoseTick;
      }

      if (this.motionX * this.motionX + this.motionZ * this.motionZ > 2.500000277905201E-7D && this.rand.nextInt(5) == 0) {
         int var1 = MathHelper.floor(this.posX);
         int var2 = MathHelper.floor(this.posY - 0.20000000298023224D);
         int var3 = MathHelper.floor(this.posZ);
         IBlockState var4 = this.world.getBlockState(new BlockPos(var1, var2, var3));
         if (var4.getMaterial() != Material.AIR) {
            this.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, this.getEntityBoundingBox().minY + 0.1D, this.posZ + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, 4.0D * ((double)this.rand.nextFloat() - 0.5D), 0.5D, ((double)this.rand.nextFloat() - 0.5D) * 4.0D, Block.getStateId(var4));
         }
      }

   }

   public boolean canAttackClass(Class var1) {
      return this.isPlayerCreated() && EntityPlayer.class.isAssignableFrom(var1) ? false : (var1 == EntityCreeper.class ? false : super.canAttackClass(var1));
   }

   public static void registerFixesIronGolem(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "VillagerGolem");
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setBoolean("PlayerCreated", this.isPlayerCreated());
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.setPlayerCreated(var1.getBoolean("PlayerCreated"));
   }

   public boolean attackEntityAsMob(Entity var1) {
      this.attackTimer = 10;
      this.world.setEntityState(this, (byte)4);
      boolean var2 = var1.attackEntityFrom(DamageSource.causeMobDamage(this), (float)(7 + this.rand.nextInt(15)));
      if (var2) {
         var1.motionY += 0.4000000059604645D;
         this.applyEnchantments(this, var1);
      }

      this.playSound(SoundEvents.ENTITY_IRONGOLEM_ATTACK, 1.0F, 1.0F);
      return var2;
   }

   public Village getVillage() {
      return this.villageObj;
   }

   public void setHoldingRose(boolean var1) {
      this.holdRoseTick = var1 ? 400 : 0;
      this.world.setEntityState(this, (byte)11);
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_IRONGOLEM_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_IRONGOLEM_DEATH;
   }

   protected void playStepSound(BlockPos var1, Block var2) {
      this.playSound(SoundEvents.ENTITY_IRONGOLEM_STEP, 1.0F, 1.0F);
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_IRON_GOLEM;
   }

   public int getHoldRoseTick() {
      return this.holdRoseTick;
   }

   public boolean isPlayerCreated() {
      return (((Byte)this.dataManager.get(PLAYER_CREATED)).byteValue() & 1) != 0;
   }

   public void setPlayerCreated(boolean var1) {
      byte var2 = ((Byte)this.dataManager.get(PLAYER_CREATED)).byteValue();
      if (var1) {
         this.dataManager.set(PLAYER_CREATED, Byte.valueOf((byte)(var2 | 1)));
      } else {
         this.dataManager.set(PLAYER_CREATED, Byte.valueOf((byte)(var2 & -2)));
      }

   }

   public void onDeath(DamageSource var1) {
      if (!this.isPlayerCreated() && this.attackingPlayer != null && this.villageObj != null) {
         this.villageObj.modifyPlayerReputation(this.attackingPlayer.getName(), -5);
      }

      super.onDeath(var1);
   }
}
