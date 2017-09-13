package net.minecraft.entity.passive;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityChicken extends EntityAnimal {
   private static final Set TEMPTATION_ITEMS = Sets.newHashSet(new Item[]{Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS});
   public float wingRotation;
   public float destPos;
   public float oFlapSpeed;
   public float oFlap;
   public float wingRotDelta = 1.0F;
   public int timeUntilNextEgg;
   public boolean chickenJockey;

   public EntityChicken(World world) {
      super(world);
      this.setSize(0.4F, 0.7F);
      this.timeUntilNextEgg = this.rand.nextInt(6000) + 6000;
      this.setPathPriority(PathNodeType.WATER, 0.0F);
   }

   protected void initEntityAI() {
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(1, new EntityAIPanic(this, 1.4D));
      this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
      this.tasks.addTask(3, new EntityAITempt(this, 1.0D, false, TEMPTATION_ITEMS));
      this.tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
      this.tasks.addTask(5, new EntityAIWander(this, 1.0D));
      this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
      this.tasks.addTask(7, new EntityAILookIdle(this));
   }

   public float getEyeHeight() {
      return this.height;
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
   }

   public void onLivingUpdate() {
      if (this.isChickenJockey()) {
         this.spawnableBlock = !this.canDespawn();
      }

      super.onLivingUpdate();
      this.oFlap = this.wingRotation;
      this.oFlapSpeed = this.destPos;
      this.destPos = (float)((double)this.destPos + (double)(this.onGround ? -1 : 4) * 0.3D);
      this.destPos = MathHelper.clamp(this.destPos, 0.0F, 1.0F);
      if (!this.onGround && this.wingRotDelta < 1.0F) {
         this.wingRotDelta = 1.0F;
      }

      this.wingRotDelta = (float)((double)this.wingRotDelta * 0.9D);
      if (!this.onGround && this.motionY < 0.0D) {
         this.motionY *= 0.6D;
      }

      this.wingRotation += this.wingRotDelta * 2.0F;
      if (!this.world.isRemote && !this.isChild() && !this.isChickenJockey() && --this.timeUntilNextEgg <= 0) {
         this.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
         this.forceDrops = true;
         this.dropItem(Items.EGG, 1);
         this.forceDrops = false;
         this.timeUntilNextEgg = this.rand.nextInt(6000) + 6000;
      }

   }

   public void fall(float f, float f1) {
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_CHICKEN_AMBIENT;
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_CHICKEN_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_CHICKEN_DEATH;
   }

   protected void playStepSound(BlockPos blockposition, Block block) {
      this.playSound(SoundEvents.ENTITY_CHICKEN_STEP, 0.15F, 1.0F);
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_CHICKEN;
   }

   public EntityChicken createChild(EntityAgeable entityageable) {
      return new EntityChicken(this.world);
   }

   public boolean isBreedingItem(@Nullable ItemStack itemstack) {
      return itemstack != null && TEMPTATION_ITEMS.contains(itemstack.getItem());
   }

   protected int getExperiencePoints(EntityPlayer entityhuman) {
      return this.isChickenJockey() ? 10 : super.getExperiencePoints(entityhuman);
   }

   public static void registerFixesChicken(DataFixer dataconvertermanager) {
      EntityLiving.registerFixesMob(dataconvertermanager, "Chicken");
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      super.readEntityFromNBT(nbttagcompound);
      this.chickenJockey = nbttagcompound.getBoolean("IsChickenJockey");
      if (nbttagcompound.hasKey("EggLayTime")) {
         this.timeUntilNextEgg = nbttagcompound.getInteger("EggLayTime");
      }

   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      super.writeEntityToNBT(nbttagcompound);
      nbttagcompound.setBoolean("IsChickenJockey", this.chickenJockey);
      nbttagcompound.setInteger("EggLayTime", this.timeUntilNextEgg);
   }

   protected boolean canDespawn() {
      return this.isChickenJockey() && !this.isBeingRidden();
   }

   public void updatePassenger(Entity entity) {
      super.updatePassenger(entity);
      float f = MathHelper.sin(this.renderYawOffset * 0.017453292F);
      float f1 = MathHelper.cos(this.renderYawOffset * 0.017453292F);
      entity.setPosition(this.posX + (double)(0.1F * f), this.posY + (double)(this.height * 0.5F) + entity.getYOffset() + 0.0D, this.posZ - (double)(0.1F * f1));
      if (entity instanceof EntityLivingBase) {
         ((EntityLivingBase)entity).renderYawOffset = this.renderYawOffset;
      }

   }

   public boolean isChickenJockey() {
      return this.chickenJockey;
   }

   public void setChickenJockey(boolean flag) {
      this.chickenJockey = flag;
   }

   public EntityAgeable createChild(EntityAgeable entityageable) {
      return this.createChild(entityageable);
   }
}
