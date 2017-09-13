package net.minecraft.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAICreeperSwell;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.CreeperPowerEvent.PowerCause;

public class EntityCreeper extends EntityMob {
   private static final DataParameter STATE = EntityDataManager.createKey(EntityCreeper.class, DataSerializers.VARINT);
   private static final DataParameter POWERED = EntityDataManager.createKey(EntityCreeper.class, DataSerializers.BOOLEAN);
   private static final DataParameter IGNITED = EntityDataManager.createKey(EntityCreeper.class, DataSerializers.BOOLEAN);
   private int lastActiveTime;
   private int timeSinceIgnited;
   private int fuseTime = 30;
   private int explosionRadius = 3;
   private int droppedSkulls;

   public EntityCreeper(World world) {
      super(world);
      this.setSize(0.6F, 1.7F);
   }

   protected void initEntityAI() {
      this.tasks.addTask(1, new EntityAISwimming(this));
      this.tasks.addTask(2, new EntityAICreeperSwell(this));
      this.tasks.addTask(3, new EntityAIAvoidEntity(this, EntityOcelot.class, 6.0F, 1.0D, 1.2D));
      this.tasks.addTask(4, new EntityAIAttackMelee(this, 1.0D, false));
      this.tasks.addTask(5, new EntityAIWander(this, 0.8D));
      this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.tasks.addTask(6, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
      this.targetTasks.addTask(2, new EntityAIHurtByTarget(this, false, new Class[0]));
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
   }

   public int getMaxFallHeight() {
      return this.getAttackTarget() == null ? 3 : 3 + (int)(this.getHealth() - 1.0F);
   }

   public void fall(float f, float f1) {
      super.fall(f, f1);
      this.timeSinceIgnited = (int)((float)this.timeSinceIgnited + f * 1.5F);
      if (this.timeSinceIgnited > this.fuseTime - 5) {
         this.timeSinceIgnited = this.fuseTime - 5;
      }

   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(STATE, Integer.valueOf(-1));
      this.dataManager.register(POWERED, Boolean.valueOf(false));
      this.dataManager.register(IGNITED, Boolean.valueOf(false));
   }

   public static void registerFixesCreeper(DataFixer dataconvertermanager) {
      EntityLiving.registerFixesMob(dataconvertermanager, "Creeper");
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      super.writeEntityToNBT(nbttagcompound);
      if (((Boolean)this.dataManager.get(POWERED)).booleanValue()) {
         nbttagcompound.setBoolean("powered", true);
      }

      nbttagcompound.setShort("Fuse", (short)this.fuseTime);
      nbttagcompound.setByte("ExplosionRadius", (byte)this.explosionRadius);
      nbttagcompound.setBoolean("ignited", this.hasIgnited());
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      super.readEntityFromNBT(nbttagcompound);
      this.dataManager.set(POWERED, Boolean.valueOf(nbttagcompound.getBoolean("powered")));
      if (nbttagcompound.hasKey("Fuse", 99)) {
         this.fuseTime = nbttagcompound.getShort("Fuse");
      }

      if (nbttagcompound.hasKey("ExplosionRadius", 99)) {
         this.explosionRadius = nbttagcompound.getByte("ExplosionRadius");
      }

      if (nbttagcompound.getBoolean("ignited")) {
         this.ignite();
      }

   }

   public void onUpdate() {
      if (this.isEntityAlive()) {
         this.lastActiveTime = this.timeSinceIgnited;
         if (this.hasIgnited()) {
            this.setCreeperState(1);
         }

         int i = this.getCreeperState();
         if (i > 0 && this.timeSinceIgnited == 0) {
            this.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1.0F, 0.5F);
         }

         this.timeSinceIgnited += i;
         if (this.timeSinceIgnited < 0) {
            this.timeSinceIgnited = 0;
         }

         if (this.timeSinceIgnited >= this.fuseTime) {
            this.timeSinceIgnited = this.fuseTime;
            this.explode();
         }
      }

      super.onUpdate();
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_CREEPER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_CREEPER_DEATH;
   }

   public void onDeath(DamageSource damagesource) {
      if (this.world.getGameRules().getBoolean("doMobLoot")) {
         if (damagesource.getEntity() instanceof EntitySkeleton) {
            int i = Item.getIdFromItem(Items.RECORD_13);
            int j = Item.getIdFromItem(Items.RECORD_WAIT);
            int k = i + this.rand.nextInt(j - i + 1);
            this.dropItem(Item.getItemById(k), 1);
         } else if (damagesource.getEntity() instanceof EntityCreeper && damagesource.getEntity() != this && ((EntityCreeper)damagesource.getEntity()).getPowered() && ((EntityCreeper)damagesource.getEntity()).isAIEnabled()) {
            ((EntityCreeper)damagesource.getEntity()).incrementDroppedSkulls();
            this.entityDropItem(new ItemStack(Items.SKULL, 1, 4), 0.0F);
         }
      }

      super.onDeath(damagesource);
   }

   public boolean attackEntityAsMob(Entity entity) {
      return true;
   }

   public boolean getPowered() {
      return ((Boolean)this.dataManager.get(POWERED)).booleanValue();
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_CREEPER;
   }

   public int getCreeperState() {
      return ((Integer)this.dataManager.get(STATE)).intValue();
   }

   public void setCreeperState(int i) {
      this.dataManager.set(STATE, Integer.valueOf(i));
   }

   public void onStruckByLightning(EntityLightningBolt entitylightning) {
      super.onStruckByLightning(entitylightning);
      if (!CraftEventFactory.callCreeperPowerEvent(this, entitylightning, PowerCause.LIGHTNING).isCancelled()) {
         this.setPowered(true);
      }
   }

   public void setPowered(boolean powered) {
      this.dataManager.set(POWERED, Boolean.valueOf(powered));
   }

   protected boolean processInteract(EntityPlayer entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack) {
      if (itemstack != null && itemstack.getItem() == Items.FLINT_AND_STEEL) {
         this.world.playSound(entityhuman, this.posX, this.posY, this.posZ, SoundEvents.ITEM_FLINTANDSTEEL_USE, this.getSoundCategory(), 1.0F, this.rand.nextFloat() * 0.4F + 0.8F);
         entityhuman.swingArm(enumhand);
         if (!this.world.isRemote) {
            this.ignite();
            itemstack.damageItem(1, entityhuman);
            return true;
         }
      }

      return super.processInteract(entityhuman, enumhand, itemstack);
   }

   private void explode() {
      if (!this.world.isRemote) {
         boolean flag = this.world.getGameRules().getBoolean("mobGriefing");
         float f = this.getPowered() ? 2.0F : 1.0F;
         ExplosionPrimeEvent event = new ExplosionPrimeEvent(this.getBukkitEntity(), (float)this.explosionRadius * f, false);
         this.world.getServer().getPluginManager().callEvent(event);
         if (!event.isCancelled()) {
            this.dead = true;
            this.world.newExplosion(this, this.posX, this.posY, this.posZ, event.getRadius(), event.getFire(), flag);
            this.setDead();
         } else {
            this.timeSinceIgnited = 0;
         }
      }

   }

   public boolean hasIgnited() {
      return ((Boolean)this.dataManager.get(IGNITED)).booleanValue();
   }

   public void ignite() {
      this.dataManager.set(IGNITED, Boolean.valueOf(true));
   }

   public boolean isAIEnabled() {
      return this.droppedSkulls < 1 && this.world.getGameRules().getBoolean("doMobLoot");
   }

   public void incrementDroppedSkulls() {
      ++this.droppedSkulls;
   }
}
