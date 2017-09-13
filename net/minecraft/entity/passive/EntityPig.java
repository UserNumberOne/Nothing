package net.minecraft.entity.passive;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class EntityPig extends EntityAnimal {
   private static final DataParameter SADDLED = EntityDataManager.createKey(EntityPig.class, DataSerializers.BOOLEAN);
   private static final Set TEMPTATION_ITEMS = Sets.newHashSet(new Item[]{Items.CARROT, Items.POTATO, Items.BEETROOT});
   private boolean boosting;
   private int boostTime;
   private int totalBoostTime;

   public EntityPig(World var1) {
      super(var1);
      this.setSize(0.9F, 0.9F);
   }

   protected void initEntityAI() {
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(1, new EntityAIPanic(this, 1.25D));
      this.tasks.addTask(3, new EntityAIMate(this, 1.0D));
      this.tasks.addTask(4, new EntityAITempt(this, 1.2D, Items.CARROT_ON_A_STICK, false));
      this.tasks.addTask(4, new EntityAITempt(this, 1.2D, false, TEMPTATION_ITEMS));
      this.tasks.addTask(5, new EntityAIFollowParent(this, 1.1D));
      this.tasks.addTask(6, new EntityAIWander(this, 1.0D));
      this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
      this.tasks.addTask(8, new EntityAILookIdle(this));
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
   }

   @Nullable
   public Entity getControllingPassenger() {
      return this.getPassengers().isEmpty() ? null : (Entity)this.getPassengers().get(0);
   }

   public boolean canBeSteered() {
      Entity var1 = this.getControllingPassenger();
      if (!(var1 instanceof EntityPlayer)) {
         return false;
      } else {
         EntityPlayer var2 = (EntityPlayer)var1;
         ItemStack var3 = var2.getHeldItemMainhand();
         if (var3 != null && var3.getItem() == Items.CARROT_ON_A_STICK) {
            return true;
         } else {
            var3 = var2.getHeldItemOffhand();
            return var3 != null && var3.getItem() == Items.CARROT_ON_A_STICK;
         }
      }
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(SADDLED, Boolean.valueOf(false));
   }

   public static void registerFixesPig(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "Pig");
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setBoolean("Saddle", this.getSaddled());
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.setSaddled(var1.getBoolean("Saddle"));
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_PIG_AMBIENT;
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_PIG_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_PIG_DEATH;
   }

   protected void playStepSound(BlockPos var1, Block var2) {
      this.playSound(SoundEvents.ENTITY_PIG_STEP, 0.15F, 1.0F);
   }

   public boolean processInteract(EntityPlayer var1, EnumHand var2, @Nullable ItemStack var3) {
      if (!super.processInteract(var1, var2, var3)) {
         if (this.getSaddled() && !this.world.isRemote && !this.isBeingRidden()) {
            var1.startRiding(this);
            return true;
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   protected void dropEquipment(boolean var1, int var2) {
      super.dropEquipment(var1, var2);
      if (this.getSaddled()) {
         this.dropItem(Items.SADDLE, 1);
      }

   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_PIG;
   }

   public boolean getSaddled() {
      return ((Boolean)this.dataManager.get(SADDLED)).booleanValue();
   }

   public void setSaddled(boolean var1) {
      if (var1) {
         this.dataManager.set(SADDLED, Boolean.valueOf(true));
      } else {
         this.dataManager.set(SADDLED, Boolean.valueOf(false));
      }

   }

   public void onStruckByLightning(EntityLightningBolt var1) {
      if (!this.world.isRemote && !this.isDead) {
         EntityPigZombie var2 = new EntityPigZombie(this.world);
         if (CraftEventFactory.callPigZapEvent(this, var1, var2).isCancelled()) {
            return;
         }

         var2.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
         var2.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
         var2.setNoAI(this.isAIDisabled());
         if (this.hasCustomName()) {
            var2.setCustomNameTag(this.getCustomNameTag());
            var2.setAlwaysRenderNameTag(this.getAlwaysRenderNameTag());
         }

         this.world.addEntity(var2, SpawnReason.LIGHTNING);
         this.setDead();
      }

   }

   public void fall(float var1, float var2) {
      super.fall(var1, var2);
      if (var1 > 5.0F) {
         for(EntityPlayer var4 : this.getRecursivePassengersByType(EntityPlayer.class)) {
            var4.addStat(AchievementList.FLY_PIG);
         }
      }

   }

   public void moveEntityWithHeading(float var1, float var2) {
      Entity var3 = this.getPassengers().isEmpty() ? null : (Entity)this.getPassengers().get(0);
      if (this.isBeingRidden() && this.canBeSteered()) {
         this.rotationYaw = var3.rotationYaw;
         this.prevRotationYaw = this.rotationYaw;
         this.rotationPitch = var3.rotationPitch * 0.5F;
         this.setRotation(this.rotationYaw, this.rotationPitch);
         this.renderYawOffset = this.rotationYaw;
         this.rotationYawHead = this.rotationYaw;
         this.stepHeight = 1.0F;
         this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;
         if (this.canPassengerSteer()) {
            float var4 = (float)this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() * 0.225F;
            if (this.boosting) {
               if (this.boostTime++ > this.totalBoostTime) {
                  this.boosting = false;
               }

               var4 += var4 * 1.15F * MathHelper.sin((float)this.boostTime / (float)this.totalBoostTime * 3.1415927F);
            }

            this.setAIMoveSpeed(var4);
            super.moveEntityWithHeading(0.0F, 1.0F);
         } else {
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
         }

         this.prevLimbSwingAmount = this.limbSwingAmount;
         double var5 = this.posX - this.prevPosX;
         double var7 = this.posZ - this.prevPosZ;
         float var9 = MathHelper.sqrt(var5 * var5 + var7 * var7) * 4.0F;
         if (var9 > 1.0F) {
            var9 = 1.0F;
         }

         this.limbSwingAmount += (var9 - this.limbSwingAmount) * 0.4F;
         this.limbSwing += this.limbSwingAmount;
      } else {
         this.stepHeight = 0.5F;
         this.jumpMovementFactor = 0.02F;
         super.moveEntityWithHeading(var1, var2);
      }

   }

   public boolean boost() {
      if (this.boosting) {
         return false;
      } else {
         this.boosting = true;
         this.boostTime = 0;
         this.totalBoostTime = this.getRNG().nextInt(841) + 140;
         return true;
      }
   }

   public EntityPig createChild(EntityAgeable var1) {
      return new EntityPig(this.world);
   }

   public boolean isBreedingItem(@Nullable ItemStack var1) {
      return var1 != null && TEMPTATION_ITEMS.contains(var1.getItem());
   }

   public EntityAgeable createChild(EntityAgeable var1) {
      return this.createChild(var1);
   }
}
