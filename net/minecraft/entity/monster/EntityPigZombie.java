package net.minecraft.entity.monster;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityPigZombie extends EntityZombie {
   private static final UUID ATTACK_SPEED_BOOST_MODIFIER_UUID = UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718");
   private static final AttributeModifier ATTACK_SPEED_BOOST_MODIFIER = (new AttributeModifier(ATTACK_SPEED_BOOST_MODIFIER_UUID, "Attacking speed boost", 0.05D, 0)).setSaved(false);
   private int angerLevel;
   private int randomSoundDelay;
   private UUID angerTargetUUID;

   public EntityPigZombie(World var1) {
      super(var1);
      this.isImmuneToFire = true;
   }

   public void setRevengeTarget(@Nullable EntityLivingBase var1) {
      super.setRevengeTarget(var1);
      if (var1 != null) {
         this.angerTargetUUID = var1.getUniqueID();
      }

   }

   protected void applyEntityAI() {
      this.targetTasks.addTask(1, new EntityPigZombie.AIHurtByAggressor(this));
      this.targetTasks.addTask(2, new EntityPigZombie.AITargetAggressor(this));
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(0.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23000000417232513D);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
   }

   public void onUpdate() {
      super.onUpdate();
   }

   protected void updateAITasks() {
      IAttributeInstance var1 = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
      if (this.isAngry()) {
         if (!this.isChild() && !var1.hasModifier(ATTACK_SPEED_BOOST_MODIFIER)) {
            var1.applyModifier(ATTACK_SPEED_BOOST_MODIFIER);
         }

         --this.angerLevel;
      } else if (var1.hasModifier(ATTACK_SPEED_BOOST_MODIFIER)) {
         var1.removeModifier(ATTACK_SPEED_BOOST_MODIFIER);
      }

      if (this.randomSoundDelay > 0 && --this.randomSoundDelay == 0) {
         this.playSound(SoundEvents.ENTITY_ZOMBIE_PIG_ANGRY, this.getSoundVolume() * 2.0F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 1.8F);
      }

      if (this.angerLevel > 0 && this.angerTargetUUID != null && this.getAITarget() == null) {
         EntityPlayer var2 = this.world.getPlayerEntityByUUID(this.angerTargetUUID);
         this.setRevengeTarget(var2);
         this.attackingPlayer = var2;
         this.recentlyHit = this.getRevengeTimer();
      }

      super.updateAITasks();
   }

   public boolean getCanSpawnHere() {
      return this.world.getDifficulty() != EnumDifficulty.PEACEFUL;
   }

   public boolean isNotColliding() {
      return this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this) && this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty() && !this.world.containsAnyLiquid(this.getEntityBoundingBox());
   }

   public static void registerFixesPigZombie(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "PigZombie");
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setShort("Anger", (short)this.angerLevel);
      if (this.angerTargetUUID != null) {
         var1.setString("HurtBy", this.angerTargetUUID.toString());
      } else {
         var1.setString("HurtBy", "");
      }

   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.angerLevel = var1.getShort("Anger");
      String var2 = var1.getString("HurtBy");
      if (!var2.isEmpty()) {
         this.angerTargetUUID = UUID.fromString(var2);
         EntityPlayer var3 = this.world.getPlayerEntityByUUID(this.angerTargetUUID);
         this.setRevengeTarget(var3);
         if (var3 != null) {
            this.attackingPlayer = var3;
            this.recentlyHit = this.getRevengeTimer();
         }
      }

   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isEntityInvulnerable(var1)) {
         return false;
      } else {
         Entity var3 = var1.getEntity();
         if (var3 instanceof EntityPlayer) {
            this.becomeAngryAt(var3);
         }

         return super.attackEntityFrom(var1, var2);
      }
   }

   private void becomeAngryAt(Entity var1) {
      this.angerLevel = 400 + this.rand.nextInt(400);
      this.randomSoundDelay = this.rand.nextInt(40);
      if (var1 instanceof EntityLivingBase) {
         this.setRevengeTarget((EntityLivingBase)var1);
      }

   }

   public boolean isAngry() {
      return this.angerLevel > 0;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_ZOMBIE_PIG_AMBIENT;
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_ZOMBIE_PIG_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_ZOMBIE_PIG_DEATH;
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_ZOMBIE_PIGMAN;
   }

   public boolean processInteract(EntityPlayer var1, EnumHand var2, @Nullable ItemStack var3) {
      return false;
   }

   protected void setEquipmentBasedOnDifficulty(DifficultyInstance var1) {
      this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance var1, @Nullable IEntityLivingData var2) {
      super.onInitialSpawn(var1, var2);
      this.setZombieType(ZombieType.NORMAL);
      return var2;
   }

   static class AIHurtByAggressor extends EntityAIHurtByTarget {
      public AIHurtByAggressor(EntityPigZombie var1) {
         super(var1, true);
      }

      protected void setEntityAttackTarget(EntityCreature var1, EntityLivingBase var2) {
         super.setEntityAttackTarget(var1, var2);
         if (var1 instanceof EntityPigZombie) {
            ((EntityPigZombie)var1).becomeAngryAt(var2);
         }

      }
   }

   static class AITargetAggressor extends EntityAINearestAttackableTarget {
      public AITargetAggressor(EntityPigZombie var1) {
         super(var1, EntityPlayer.class, true);
      }

      public boolean shouldExecute() {
         return ((EntityPigZombie)this.taskOwner).isAngry() && super.shouldExecute();
      }
   }
}
