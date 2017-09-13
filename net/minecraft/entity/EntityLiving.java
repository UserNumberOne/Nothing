package net.minecraft.entity;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityJumpHelper;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.EntitySenses;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketEntityAttach;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.EntityUnleashEvent.UnleashReason;

public abstract class EntityLiving extends EntityLivingBase {
   private static final DataParameter AI_FLAGS = EntityDataManager.createKey(EntityLiving.class, DataSerializers.BYTE);
   public int livingSoundTime;
   protected int experienceValue;
   private final EntityLookHelper lookHelper;
   protected EntityMoveHelper moveHelper;
   protected EntityJumpHelper jumpHelper;
   private final EntityBodyHelper bodyHelper;
   protected PathNavigate navigator;
   public EntityAITasks tasks;
   public EntityAITasks targetTasks;
   private EntityLivingBase attackTarget;
   private final EntitySenses senses;
   private final ItemStack[] inventoryHands = new ItemStack[2];
   public float[] inventoryHandsDropChances = new float[2];
   private final ItemStack[] inventoryArmor = new ItemStack[4];
   public float[] inventoryArmorDropChances = new float[4];
   public boolean canPickUpLoot;
   public boolean persistenceRequired;
   private final Map mapPathPriority = Maps.newEnumMap(PathNodeType.class);
   private ResourceLocation deathLootTable;
   private long deathLootTableSeed;
   private boolean isLeashed;
   private Entity leashedToEntity;
   private NBTTagCompound leashNBTTag;

   public EntityLiving(World world) {
      super(world);
      this.tasks = new EntityAITasks(world != null && world.theProfiler != null ? world.theProfiler : null);
      this.targetTasks = new EntityAITasks(world != null && world.theProfiler != null ? world.theProfiler : null);
      this.lookHelper = new EntityLookHelper(this);
      this.moveHelper = new EntityMoveHelper(this);
      this.jumpHelper = new EntityJumpHelper(this);
      this.bodyHelper = this.createBodyHelper();
      this.navigator = this.createNavigator(world);
      this.senses = new EntitySenses(this);
      Arrays.fill(this.inventoryArmorDropChances, 0.085F);
      Arrays.fill(this.inventoryHandsDropChances, 0.085F);
      if (world != null && !world.isRemote) {
         this.initEntityAI();
      }

      this.persistenceRequired = !this.canDespawn();
   }

   protected void initEntityAI() {
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16.0D);
   }

   protected PathNavigate createNavigator(World world) {
      return new PathNavigateGround(this, world);
   }

   public float getPathPriority(PathNodeType pathtype) {
      Float ofloat = (Float)this.mapPathPriority.get(pathtype);
      return ofloat == null ? pathtype.getPriority() : ofloat.floatValue();
   }

   public void setPathPriority(PathNodeType pathtype, float f) {
      this.mapPathPriority.put(pathtype, Float.valueOf(f));
   }

   protected EntityBodyHelper createBodyHelper() {
      return new EntityBodyHelper(this);
   }

   public EntityLookHelper getLookHelper() {
      return this.lookHelper;
   }

   public EntityMoveHelper getMoveHelper() {
      return this.moveHelper;
   }

   public EntityJumpHelper getJumpHelper() {
      return this.jumpHelper;
   }

   public PathNavigate getNavigator() {
      return this.navigator;
   }

   public EntitySenses getEntitySenses() {
      return this.senses;
   }

   @Nullable
   public EntityLivingBase getAttackTarget() {
      return this.attackTarget;
   }

   public void setAttackTarget(@Nullable EntityLivingBase entityliving) {
      this.setGoalTarget(entityliving, TargetReason.UNKNOWN, true);
   }

   public boolean setGoalTarget(EntityLivingBase entityliving, TargetReason reason, boolean fireEvent) {
      if (this.getAttackTarget() == entityliving) {
         return false;
      } else {
         if (fireEvent) {
            if (reason == TargetReason.UNKNOWN && this.getAttackTarget() != null && entityliving == null) {
               reason = this.getAttackTarget().isEntityAlive() ? TargetReason.FORGOT_TARGET : TargetReason.TARGET_DIED;
            }

            if (reason == TargetReason.UNKNOWN) {
               this.world.getServer().getLogger().log(Level.WARNING, "Unknown target reason, please report on the issue tracker", new Exception());
            }

            CraftLivingEntity ctarget = null;
            if (entityliving != null) {
               ctarget = (CraftLivingEntity)entityliving.getBukkitEntity();
            }

            EntityTargetLivingEntityEvent event = new EntityTargetLivingEntityEvent(this.getBukkitEntity(), ctarget, reason);
            this.world.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
               return false;
            }

            if (event.getTarget() != null) {
               entityliving = ((CraftLivingEntity)event.getTarget()).getHandle();
            } else {
               entityliving = null;
            }
         }

         this.attackTarget = entityliving;
         return true;
      }
   }

   public boolean canAttackClass(Class oclass) {
      return oclass != EntityGhast.class;
   }

   public void eatGrassBonus() {
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(AI_FLAGS, Byte.valueOf((byte)0));
   }

   public int getTalkInterval() {
      return 80;
   }

   public void playLivingSound() {
      SoundEvent soundeffect = this.getAmbientSound();
      if (soundeffect != null) {
         this.playSound(soundeffect, this.getSoundVolume(), this.getSoundPitch());
      }

   }

   public void onEntityUpdate() {
      super.onEntityUpdate();
      this.world.theProfiler.startSection("mobBaseTick");
      if (this.isEntityAlive() && this.rand.nextInt(1000) < this.livingSoundTime++) {
         this.applyEntityAI();
         this.playLivingSound();
      }

      this.world.theProfiler.endSection();
   }

   protected void playHurtSound(DamageSource damagesource) {
      this.applyEntityAI();
      super.playHurtSound(damagesource);
   }

   private void applyEntityAI() {
      this.livingSoundTime = -this.getTalkInterval();
   }

   protected int getExperiencePoints(EntityPlayer entityhuman) {
      if (this.experienceValue > 0) {
         int i = this.experienceValue;

         for(int j = 0; j < this.inventoryArmor.length; ++j) {
            if (this.inventoryArmor[j] != null && this.inventoryArmorDropChances[j] <= 1.0F) {
               i += 1 + this.rand.nextInt(3);
            }
         }

         for(int var4 = 0; var4 < this.inventoryHands.length; ++var4) {
            if (this.inventoryHands[var4] != null && this.inventoryHandsDropChances[var4] <= 1.0F) {
               i += 1 + this.rand.nextInt(3);
            }
         }

         return i;
      } else {
         return this.experienceValue;
      }
   }

   public void spawnExplosionParticle() {
      if (this.world.isRemote) {
         for(int i = 0; i < 20; ++i) {
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            double d2 = this.rand.nextGaussian() * 0.02D;
            this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width - d0 * 10.0D, this.posY + (double)(this.rand.nextFloat() * this.height) - d1 * 10.0D, this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width - d2 * 10.0D, d0, d1, d2);
         }
      } else {
         this.world.setEntityState(this, (byte)20);
      }

   }

   public void onUpdate() {
      super.onUpdate();
      if (!this.world.isRemote) {
         this.updateLeashedState();
         if (this.ticksExisted % 5 == 0) {
            boolean flag = !(this.getControllingPassenger() instanceof EntityLiving);
            boolean flag1 = !(this.getRidingEntity() instanceof EntityBoat);
            this.tasks.setControlFlag(1, flag);
            this.tasks.setControlFlag(4, flag && flag1);
            this.tasks.setControlFlag(2, flag);
         }
      }

   }

   protected float updateDistance(float f, float f1) {
      this.bodyHelper.updateRenderAngles();
      return f1;
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return null;
   }

   @Nullable
   protected Item getDropItem() {
      return null;
   }

   protected void dropFewItems(boolean flag, int i) {
      Item item = this.getDropItem();
      if (item != null) {
         int j = this.rand.nextInt(3);
         if (i > 0) {
            j += this.rand.nextInt(i + 1);
         }

         for(int k = 0; k < j; ++k) {
            this.dropItem(item, 1);
         }
      }

   }

   public static void registerFixesMob(DataFixer dataconvertermanager, String s) {
      dataconvertermanager.registerWalker(FixTypes.ENTITY, new ItemStackDataLists(s, new String[]{"ArmorItems", "HandItems"}));
   }

   public static void registerFixesMob(DataFixer dataconvertermanager) {
      registerFixesMob(dataconvertermanager, "Mob");
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      super.writeEntityToNBT(nbttagcompound);
      nbttagcompound.setBoolean("CanPickUpLoot", this.canPickUpLoot());
      nbttagcompound.setBoolean("PersistenceRequired", this.persistenceRequired);
      NBTTagList nbttaglist = new NBTTagList();

      for(ItemStack itemstack : this.inventoryArmor) {
         NBTTagCompound nbttagcompound1 = new NBTTagCompound();
         if (itemstack != null) {
            itemstack.writeToNBT(nbttagcompound1);
         }

         nbttaglist.appendTag(nbttagcompound1);
      }

      nbttagcompound.setTag("ArmorItems", nbttaglist);
      NBTTagList nbttaglist1 = new NBTTagList();

      for(ItemStack itemstack1 : this.inventoryHands) {
         NBTTagCompound nbttagcompound2 = new NBTTagCompound();
         if (itemstack1 != null) {
            itemstack1.writeToNBT(nbttagcompound2);
         }

         nbttaglist1.appendTag(nbttagcompound2);
      }

      nbttagcompound.setTag("HandItems", nbttaglist1);
      NBTTagList nbttaglist2 = new NBTTagList();

      for(float f : this.inventoryArmorDropChances) {
         nbttaglist2.appendTag(new NBTTagFloat(f));
      }

      nbttagcompound.setTag("ArmorDropChances", nbttaglist2);
      NBTTagList nbttaglist3 = new NBTTagList();

      for(float f1 : this.inventoryHandsDropChances) {
         nbttaglist3.appendTag(new NBTTagFloat(f1));
      }

      nbttagcompound.setTag("HandDropChances", nbttaglist3);
      nbttagcompound.setBoolean("Leashed", this.isLeashed);
      if (this.leashedToEntity != null) {
         NBTTagCompound nbttagcompound3 = new NBTTagCompound();
         if (this.leashedToEntity instanceof EntityLivingBase) {
            UUID uuid = this.leashedToEntity.getUniqueID();
            nbttagcompound3.setUniqueId("UUID", uuid);
         } else if (this.leashedToEntity instanceof EntityHanging) {
            BlockPos blockposition = ((EntityHanging)this.leashedToEntity).getHangingPosition();
            nbttagcompound3.setInteger("X", blockposition.getX());
            nbttagcompound3.setInteger("Y", blockposition.getY());
            nbttagcompound3.setInteger("Z", blockposition.getZ());
         }

         nbttagcompound.setTag("Leash", nbttagcompound3);
      }

      nbttagcompound.setBoolean("LeftHanded", this.isLeftHanded());
      if (this.deathLootTable != null) {
         nbttagcompound.setString("DeathLootTable", this.deathLootTable.toString());
         if (this.deathLootTableSeed != 0L) {
            nbttagcompound.setLong("DeathLootTableSeed", this.deathLootTableSeed);
         }
      }

      if (this.isAIDisabled()) {
         nbttagcompound.setBoolean("NoAI", this.isAIDisabled());
      }

   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      super.readEntityFromNBT(nbttagcompound);
      if (nbttagcompound.hasKey("CanPickUpLoot", 1)) {
         boolean data = nbttagcompound.getBoolean("CanPickUpLoot");
         if (isLevelAtLeast(nbttagcompound, 1) || data) {
            this.setJumping(data);
         }
      }

      boolean data = nbttagcompound.getBoolean("PersistenceRequired");
      if (isLevelAtLeast(nbttagcompound, 1) || data) {
         this.persistenceRequired = data;
      }

      if (nbttagcompound.hasKey("ArmorItems", 9)) {
         NBTTagList nbttaglist = nbttagcompound.getTagList("ArmorItems", 10);

         for(int i = 0; i < this.inventoryArmor.length; ++i) {
            this.inventoryArmor[i] = ItemStack.loadItemStackFromNBT(nbttaglist.getCompoundTagAt(i));
         }
      }

      if (nbttagcompound.hasKey("HandItems", 9)) {
         NBTTagList nbttaglist = nbttagcompound.getTagList("HandItems", 10);

         for(int i = 0; i < this.inventoryHands.length; ++i) {
            this.inventoryHands[i] = ItemStack.loadItemStackFromNBT(nbttaglist.getCompoundTagAt(i));
         }
      }

      if (nbttagcompound.hasKey("ArmorDropChances", 9)) {
         NBTTagList nbttaglist = nbttagcompound.getTagList("ArmorDropChances", 5);

         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            this.inventoryArmorDropChances[i] = nbttaglist.getFloatAt(i);
         }
      }

      if (nbttagcompound.hasKey("HandDropChances", 9)) {
         NBTTagList nbttaglist = nbttagcompound.getTagList("HandDropChances", 5);

         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            this.inventoryHandsDropChances[i] = nbttaglist.getFloatAt(i);
         }
      }

      this.isLeashed = nbttagcompound.getBoolean("Leashed");
      if (this.isLeashed && nbttagcompound.hasKey("Leash", 10)) {
         this.leashNBTTag = nbttagcompound.getCompoundTag("Leash");
      }

      this.setLeftHanded(nbttagcompound.getBoolean("LeftHanded"));
      if (nbttagcompound.hasKey("DeathLootTable", 8)) {
         this.deathLootTable = new ResourceLocation(nbttagcompound.getString("DeathLootTable"));
         this.deathLootTableSeed = nbttagcompound.getLong("DeathLootTableSeed");
      }

      this.setNoAI(nbttagcompound.getBoolean("NoAI"));
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return null;
   }

   protected void dropLoot(boolean flag, int i, DamageSource damagesource) {
      ResourceLocation minecraftkey = this.deathLootTable;
      if (minecraftkey == null) {
         minecraftkey = this.getLootTable();
      }

      if (minecraftkey != null) {
         LootTable loottable = this.world.getLootTableManager().getLootTableFromLocation(minecraftkey);
         this.deathLootTable = null;
         LootContext.Builder loottableinfo_a = (new LootContext.Builder((WorldServer)this.world)).withLootedEntity(this).withDamageSource(damagesource);
         if (flag && this.attackingPlayer != null) {
            loottableinfo_a = loottableinfo_a.withPlayer(this.attackingPlayer).withLuck(this.attackingPlayer.getLuck());
         }

         for(ItemStack itemstack : loottable.generateLootForPools(this.deathLootTableSeed == 0L ? this.rand : new Random(this.deathLootTableSeed), loottableinfo_a.build())) {
            this.entityDropItem(itemstack, 0.0F);
         }

         this.dropEquipment(flag, i);
      } else {
         super.dropLoot(flag, i, damagesource);
      }

   }

   public void setMoveForward(float f) {
      this.moveForward = f;
   }

   public void setMoveStrafing(float f) {
      this.moveStrafing = f;
   }

   public void setAIMoveSpeed(float f) {
      super.setAIMoveSpeed(f);
      this.setMoveForward(f);
   }

   public void onLivingUpdate() {
      super.onLivingUpdate();
      this.world.theProfiler.startSection("looting");
      if (!this.world.isRemote && this.canPickUpLoot() && !this.dead && this.world.getGameRules().getBoolean("mobGriefing")) {
         for(EntityItem entityitem : this.world.getEntitiesWithinAABB(EntityItem.class, this.getEntityBoundingBox().expand(1.0D, 0.0D, 1.0D))) {
            if (!entityitem.isDead && entityitem.getEntityItem() != null && !entityitem.cannotPickup()) {
               this.updateEquipmentIfNeeded(entityitem);
            }
         }
      }

      this.world.theProfiler.endSection();
   }

   protected void updateEquipmentIfNeeded(EntityItem entityitem) {
      ItemStack itemstack = entityitem.getEntityItem();
      EntityEquipmentSlot enumitemslot = getSlotForItemStack(itemstack);
      boolean flag = true;
      ItemStack itemstack1 = this.getItemStackFromSlot(enumitemslot);
      if (itemstack1 != null) {
         if (enumitemslot.getSlotType() == EntityEquipmentSlot.Type.HAND) {
            if (itemstack.getItem() instanceof ItemSword && !(itemstack1.getItem() instanceof ItemSword)) {
               flag = true;
            } else if (itemstack.getItem() instanceof ItemSword && itemstack1.getItem() instanceof ItemSword) {
               ItemSword itemsword = (ItemSword)itemstack.getItem();
               ItemSword itemsword1 = (ItemSword)itemstack1.getItem();
               if (itemsword.getDamageVsEntity() == itemsword1.getDamageVsEntity()) {
                  flag = itemstack.getMetadata() > itemstack1.getMetadata() || itemstack.hasTagCompound() && !itemstack1.hasTagCompound();
               } else {
                  flag = itemsword.getDamageVsEntity() > itemsword1.getDamageVsEntity();
               }
            } else if (itemstack.getItem() instanceof ItemBow && itemstack1.getItem() instanceof ItemBow) {
               flag = itemstack.hasTagCompound() && !itemstack1.hasTagCompound();
            } else {
               flag = false;
            }
         } else if (itemstack.getItem() instanceof ItemArmor && !(itemstack1.getItem() instanceof ItemArmor)) {
            flag = true;
         } else if (itemstack.getItem() instanceof ItemArmor && itemstack1.getItem() instanceof ItemArmor) {
            ItemArmor itemarmor = (ItemArmor)itemstack.getItem();
            ItemArmor itemarmor1 = (ItemArmor)itemstack1.getItem();
            if (itemarmor.damageReduceAmount == itemarmor1.damageReduceAmount) {
               flag = itemstack.getMetadata() > itemstack1.getMetadata() || itemstack.hasTagCompound() && !itemstack1.hasTagCompound();
            } else {
               flag = itemarmor.damageReduceAmount > itemarmor1.damageReduceAmount;
            }
         } else {
            flag = false;
         }
      }

      if (flag && this.canEquipItem(itemstack)) {
         double d0;
         switch(EntityLiving.SyntheticClass_1.a[enumitemslot.getSlotType().ordinal()]) {
         case 1:
            d0 = (double)this.inventoryHandsDropChances[enumitemslot.getIndex()];
            break;
         case 2:
            d0 = (double)this.inventoryArmorDropChances[enumitemslot.getIndex()];
            break;
         default:
            d0 = 0.0D;
         }

         if (itemstack1 != null && (double)(this.rand.nextFloat() - 0.1F) < d0) {
            this.entityDropItem(itemstack1, 0.0F);
         }

         if (itemstack.getItem() == Items.DIAMOND && entityitem.getThrower() != null) {
            EntityPlayer entityhuman = this.world.getPlayerEntityByName(entityitem.getThrower());
            if (entityhuman != null) {
               entityhuman.addStat(AchievementList.DIAMONDS_TO_YOU);
            }
         }

         this.setItemStackToSlot(enumitemslot, itemstack);
         switch(EntityLiving.SyntheticClass_1.a[enumitemslot.getSlotType().ordinal()]) {
         case 1:
            this.inventoryHandsDropChances[enumitemslot.getIndex()] = 2.0F;
            break;
         case 2:
            this.inventoryArmorDropChances[enumitemslot.getIndex()] = 2.0F;
         }

         this.persistenceRequired = true;
         this.onItemPickup(entityitem, 1);
         entityitem.setDead();
      }

   }

   protected boolean canEquipItem(ItemStack itemstack) {
      return true;
   }

   protected boolean canDespawn() {
      return true;
   }

   protected void despawnEntity() {
      if (this.persistenceRequired) {
         this.entityAge = 0;
      } else {
         EntityPlayer entityhuman = this.world.getClosestPlayerToEntity(this, -1.0D);
         if (entityhuman != null) {
            double d0 = entityhuman.posX - this.posX;
            double d1 = entityhuman.posY - this.posY;
            double d2 = entityhuman.posZ - this.posZ;
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;
            if (d3 > 16384.0D) {
               this.setDead();
            }

            if (this.entityAge > 600 && this.rand.nextInt(800) == 0 && d3 > 1024.0D) {
               this.setDead();
            } else if (d3 < 1024.0D) {
               this.entityAge = 0;
            }
         }
      }

   }

   protected final void updateEntityActionState() {
      ++this.entityAge;
      this.world.theProfiler.startSection("checkDespawn");
      this.despawnEntity();
      this.world.theProfiler.endSection();
      this.world.theProfiler.startSection("sensing");
      this.senses.clearSensingCache();
      this.world.theProfiler.endSection();
      this.world.theProfiler.startSection("targetSelector");
      this.targetTasks.onUpdateTasks();
      this.world.theProfiler.endSection();
      this.world.theProfiler.startSection("goalSelector");
      this.tasks.onUpdateTasks();
      this.world.theProfiler.endSection();
      this.world.theProfiler.startSection("navigation");
      this.navigator.onUpdateNavigation();
      this.world.theProfiler.endSection();
      this.world.theProfiler.startSection("mob tick");
      this.updateAITasks();
      this.world.theProfiler.endSection();
      if (this.isRiding() && this.getRidingEntity() instanceof EntityLiving) {
         EntityLiving entityinsentient = (EntityLiving)this.getRidingEntity();
         entityinsentient.getNavigator().setPath(this.getNavigator().getPath(), 1.5D);
         entityinsentient.getMoveHelper().read(this.getMoveHelper());
      }

      this.world.theProfiler.startSection("controls");
      this.world.theProfiler.startSection("move");
      this.moveHelper.onUpdateMoveHelper();
      this.world.theProfiler.endStartSection("look");
      this.lookHelper.onUpdateLook();
      this.world.theProfiler.endStartSection("jump");
      this.jumpHelper.doJump();
      this.world.theProfiler.endSection();
      this.world.theProfiler.endSection();
   }

   protected void updateAITasks() {
   }

   public int getVerticalFaceSpeed() {
      return 40;
   }

   public int getHorizontalFaceSpeed() {
      return 10;
   }

   public void faceEntity(Entity entity, float f, float f1) {
      double d0 = entity.posX - this.posX;
      double d1 = entity.posZ - this.posZ;
      double d2;
      if (entity instanceof EntityLivingBase) {
         EntityLivingBase entityliving = (EntityLivingBase)entity;
         d2 = entityliving.posY + (double)entityliving.getEyeHeight() - (this.posY + (double)this.getEyeHeight());
      } else {
         d2 = (entity.getEntityBoundingBox().minY + entity.getEntityBoundingBox().maxY) / 2.0D - (this.posY + (double)this.getEyeHeight());
      }

      double d3 = (double)MathHelper.sqrt(d0 * d0 + d1 * d1);
      float f2 = (float)(MathHelper.atan2(d1, d0) * 57.2957763671875D) - 90.0F;
      float f3 = (float)(-(MathHelper.atan2(d2, d3) * 57.2957763671875D));
      this.rotationPitch = this.updateRotation(this.rotationPitch, f3, f1);
      this.rotationYaw = this.updateRotation(this.rotationYaw, f2, f);
   }

   private float updateRotation(float f, float f1, float f2) {
      float f3 = MathHelper.wrapDegrees(f1 - f);
      if (f3 > f2) {
         f3 = f2;
      }

      if (f3 < -f2) {
         f3 = -f2;
      }

      return f + f3;
   }

   public boolean getCanSpawnHere() {
      IBlockState iblockdata = this.world.getBlockState((new BlockPos(this)).down());
      return iblockdata.canEntitySpawn(this);
   }

   public boolean isNotColliding() {
      return !this.world.containsAnyLiquid(this.getEntityBoundingBox()) && this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty() && this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this);
   }

   public int getMaxSpawnedInChunk() {
      return 4;
   }

   public int getMaxFallHeight() {
      if (this.getAttackTarget() == null) {
         return 3;
      } else {
         int i = (int)(this.getHealth() - this.getMaxHealth() * 0.33F);
         i = i - (3 - this.world.getDifficulty().getDifficultyId()) * 4;
         if (i < 0) {
            i = 0;
         }

         return i + 3;
      }
   }

   public Iterable getHeldEquipment() {
      return Arrays.asList(this.inventoryHands);
   }

   public Iterable getArmorInventoryList() {
      return Arrays.asList(this.inventoryArmor);
   }

   @Nullable
   public ItemStack getItemStackFromSlot(EntityEquipmentSlot enumitemslot) {
      ItemStack itemstack = null;
      switch(EntityLiving.SyntheticClass_1.a[enumitemslot.getSlotType().ordinal()]) {
      case 1:
         itemstack = this.inventoryHands[enumitemslot.getIndex()];
         break;
      case 2:
         itemstack = this.inventoryArmor[enumitemslot.getIndex()];
      }

      return itemstack;
   }

   public void setItemStackToSlot(EntityEquipmentSlot enumitemslot, @Nullable ItemStack itemstack) {
      switch(EntityLiving.SyntheticClass_1.a[enumitemslot.getSlotType().ordinal()]) {
      case 1:
         this.inventoryHands[enumitemslot.getIndex()] = itemstack;
         break;
      case 2:
         this.inventoryArmor[enumitemslot.getIndex()] = itemstack;
      }

   }

   protected void dropEquipment(boolean flag, int i) {
      for(EntityEquipmentSlot enumitemslot : EntityEquipmentSlot.values()) {
         ItemStack itemstack = this.getItemStackFromSlot(enumitemslot);
         double d0;
         switch(EntityLiving.SyntheticClass_1.a[enumitemslot.getSlotType().ordinal()]) {
         case 1:
            d0 = (double)this.inventoryHandsDropChances[enumitemslot.getIndex()];
            break;
         case 2:
            d0 = (double)this.inventoryArmorDropChances[enumitemslot.getIndex()];
            break;
         default:
            d0 = 0.0D;
         }

         boolean flag1 = d0 > 1.0D;
         if (itemstack != null && (flag || flag1) && (double)(this.rand.nextFloat() - (float)i * 0.01F) < d0) {
            if (!flag1 && itemstack.isItemStackDamageable()) {
               int l = Math.max(itemstack.getMaxDamage() - 25, 1);
               int i1 = itemstack.getMaxDamage() - this.rand.nextInt(this.rand.nextInt(l) + 1);
               if (i1 > l) {
                  i1 = l;
               }

               if (i1 < 1) {
                  i1 = 1;
               }

               itemstack.setItemDamage(i1);
            }

            this.entityDropItem(itemstack, 0.0F);
         }
      }

   }

   protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficultydamagescaler) {
      if (this.rand.nextFloat() < 0.15F * difficultydamagescaler.getClampedAdditionalDifficulty()) {
         int i = this.rand.nextInt(2);
         float f = this.world.getDifficulty() == EnumDifficulty.HARD ? 0.1F : 0.25F;
         if (this.rand.nextFloat() < 0.095F) {
            ++i;
         }

         if (this.rand.nextFloat() < 0.095F) {
            ++i;
         }

         if (this.rand.nextFloat() < 0.095F) {
            ++i;
         }

         boolean flag = true;

         for(EntityEquipmentSlot enumitemslot : EntityEquipmentSlot.values()) {
            if (enumitemslot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
               ItemStack itemstack = this.getItemStackFromSlot(enumitemslot);
               if (!flag && this.rand.nextFloat() < f) {
                  break;
               }

               flag = false;
               if (itemstack == null) {
                  Item item = getArmorByChance(enumitemslot, i);
                  if (item != null) {
                     this.setItemStackToSlot(enumitemslot, new ItemStack(item));
                  }
               }
            }
         }
      }

   }

   public static EntityEquipmentSlot getSlotForItemStack(ItemStack itemstack) {
      return itemstack.getItem() != Item.getItemFromBlock(Blocks.PUMPKIN) && itemstack.getItem() != Items.SKULL ? (itemstack.getItem() instanceof ItemArmor ? ((ItemArmor)itemstack.getItem()).armorType : (itemstack.getItem() == Items.ELYTRA ? EntityEquipmentSlot.CHEST : (itemstack.getItem() == Items.SHIELD ? EntityEquipmentSlot.OFFHAND : EntityEquipmentSlot.MAINHAND))) : EntityEquipmentSlot.HEAD;
   }

   public static Item getArmorByChance(EntityEquipmentSlot enumitemslot, int i) {
      switch(EntityLiving.SyntheticClass_1.b[enumitemslot.ordinal()]) {
      case 1:
         if (i == 0) {
            return Items.LEATHER_HELMET;
         } else if (i == 1) {
            return Items.GOLDEN_HELMET;
         } else if (i == 2) {
            return Items.CHAINMAIL_HELMET;
         } else if (i == 3) {
            return Items.IRON_HELMET;
         } else if (i == 4) {
            return Items.DIAMOND_HELMET;
         }
      case 2:
         if (i == 0) {
            return Items.LEATHER_CHESTPLATE;
         } else if (i == 1) {
            return Items.GOLDEN_CHESTPLATE;
         } else if (i == 2) {
            return Items.CHAINMAIL_CHESTPLATE;
         } else if (i == 3) {
            return Items.IRON_CHESTPLATE;
         } else if (i == 4) {
            return Items.DIAMOND_CHESTPLATE;
         }
      case 3:
         if (i == 0) {
            return Items.LEATHER_LEGGINGS;
         } else if (i == 1) {
            return Items.GOLDEN_LEGGINGS;
         } else if (i == 2) {
            return Items.CHAINMAIL_LEGGINGS;
         } else if (i == 3) {
            return Items.IRON_LEGGINGS;
         } else if (i == 4) {
            return Items.DIAMOND_LEGGINGS;
         }
      case 4:
         if (i == 0) {
            return Items.LEATHER_BOOTS;
         } else if (i == 1) {
            return Items.GOLDEN_BOOTS;
         } else if (i == 2) {
            return Items.CHAINMAIL_BOOTS;
         } else if (i == 3) {
            return Items.IRON_BOOTS;
         } else if (i == 4) {
            return Items.DIAMOND_BOOTS;
         }
      default:
         return null;
      }
   }

   protected void setEnchantmentBasedOnDifficulty(DifficultyInstance difficultydamagescaler) {
      float f = difficultydamagescaler.getClampedAdditionalDifficulty();
      if (this.getHeldItemMainhand() != null && this.rand.nextFloat() < 0.25F * f) {
         EnchantmentHelper.addRandomEnchantment(this.rand, this.getHeldItemMainhand(), (int)(5.0F + f * (float)this.rand.nextInt(18)), false);
      }

      for(EntityEquipmentSlot enumitemslot : EntityEquipmentSlot.values()) {
         if (enumitemslot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
            ItemStack itemstack = this.getItemStackFromSlot(enumitemslot);
            if (itemstack != null && this.rand.nextFloat() < 0.5F * f) {
               EnchantmentHelper.addRandomEnchantment(this.rand, itemstack, (int)(5.0F + f * (float)this.rand.nextInt(18)), false);
            }
         }
      }

   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance difficultydamagescaler, @Nullable IEntityLivingData groupdataentity) {
      this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).applyModifier(new AttributeModifier("Random spawn bonus", this.rand.nextGaussian() * 0.05D, 1));
      if (this.rand.nextFloat() < 0.05F) {
         this.setLeftHanded(true);
      } else {
         this.setLeftHanded(false);
      }

      return groupdataentity;
   }

   public boolean canBeSteered() {
      return false;
   }

   public void enablePersistence() {
      this.persistenceRequired = true;
   }

   public void setDropChance(EntityEquipmentSlot enumitemslot, float f) {
      switch(EntityLiving.SyntheticClass_1.a[enumitemslot.getSlotType().ordinal()]) {
      case 1:
         this.inventoryHandsDropChances[enumitemslot.getIndex()] = f;
         break;
      case 2:
         this.inventoryArmorDropChances[enumitemslot.getIndex()] = f;
      }

   }

   public boolean canPickUpLoot() {
      return this.canPickUpLoot;
   }

   public void setCanPickUpLoot(boolean flag) {
      this.canPickUpLoot = flag;
   }

   public boolean isNoDespawnRequired() {
      return this.persistenceRequired;
   }

   public final boolean processInitialInteract(EntityPlayer entityhuman, @Nullable ItemStack itemstack, EnumHand enumhand) {
      if (this.getLeashed() && this.getLeashedToEntity() == entityhuman) {
         if (CraftEventFactory.callPlayerUnleashEntityEvent(this, entityhuman).isCancelled()) {
            ((EntityPlayerMP)entityhuman).connection.sendPacket(new SPacketEntityAttach(this, this.getLeashedToEntity()));
            return false;
         } else {
            this.clearLeashed(true, !entityhuman.capabilities.isCreativeMode);
            return true;
         }
      } else if (itemstack != null && itemstack.getItem() == Items.LEAD && this.canBeLeashedTo(entityhuman)) {
         if (CraftEventFactory.callPlayerLeashEntityEvent(this, entityhuman, entityhuman).isCancelled()) {
            ((EntityPlayerMP)entityhuman).connection.sendPacket(new SPacketEntityAttach(this, this.getLeashedToEntity()));
            return false;
         } else {
            this.setLeashedToEntity(entityhuman, true);
            --itemstack.stackSize;
            return true;
         }
      } else {
         return this.processInteract(entityhuman, enumhand, itemstack) ? true : super.processInitialInteract(entityhuman, itemstack, enumhand);
      }
   }

   protected boolean processInteract(EntityPlayer entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack) {
      return false;
   }

   protected void updateLeashedState() {
      if (this.leashNBTTag != null) {
         this.recreateLeash();
      }

      if (this.isLeashed) {
         if (!this.isEntityAlive()) {
            this.world.getServer().getPluginManager().callEvent(new EntityUnleashEvent(this.getBukkitEntity(), UnleashReason.PLAYER_UNLEASH));
            this.clearLeashed(true, true);
         }

         if (this.leashedToEntity == null || this.leashedToEntity.isDead) {
            this.world.getServer().getPluginManager().callEvent(new EntityUnleashEvent(this.getBukkitEntity(), UnleashReason.HOLDER_GONE));
            this.clearLeashed(true, true);
         }
      }

   }

   public void clearLeashed(boolean flag, boolean flag1) {
      if (this.isLeashed) {
         this.isLeashed = false;
         this.leashedToEntity = null;
         if (!this.world.isRemote && flag1) {
            this.forceDrops = true;
            this.dropItem(Items.LEAD, 1);
            this.forceDrops = false;
         }

         if (!this.world.isRemote && flag && this.world instanceof WorldServer) {
            ((WorldServer)this.world).getEntityTracker().sendToTracking(this, new SPacketEntityAttach(this, (Entity)null));
         }
      }

   }

   public boolean canBeLeashedTo(EntityPlayer entityhuman) {
      return !this.getLeashed() && !(this instanceof IMob);
   }

   public boolean getLeashed() {
      return this.isLeashed;
   }

   public Entity getLeashedToEntity() {
      return this.leashedToEntity;
   }

   public void setLeashedToEntity(Entity entity, boolean flag) {
      this.isLeashed = true;
      this.leashedToEntity = entity;
      if (!this.world.isRemote && flag && this.world instanceof WorldServer) {
         ((WorldServer)this.world).getEntityTracker().sendToTracking(this, new SPacketEntityAttach(this, this.leashedToEntity));
      }

      if (this.isRiding()) {
         this.dismountRidingEntity();
      }

   }

   public boolean startRiding(Entity entity, boolean flag) {
      boolean flag1 = super.startRiding(entity, flag);
      if (flag1 && this.getLeashed()) {
         this.clearLeashed(true, true);
      }

      return flag1;
   }

   private void recreateLeash() {
      if (this.isLeashed && this.leashNBTTag != null) {
         if (this.leashNBTTag.hasUniqueId("UUID")) {
            UUID uuid = this.leashNBTTag.getUniqueId("UUID");

            for(EntityLivingBase entityliving : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expandXyz(10.0D))) {
               if (entityliving.getUniqueID().equals(uuid)) {
                  this.leashedToEntity = entityliving;
                  break;
               }
            }
         } else if (this.leashNBTTag.hasKey("X", 99) && this.leashNBTTag.hasKey("Y", 99) && this.leashNBTTag.hasKey("Z", 99)) {
            BlockPos blockposition = new BlockPos(this.leashNBTTag.getInteger("X"), this.leashNBTTag.getInteger("Y"), this.leashNBTTag.getInteger("Z"));
            EntityLeashKnot entityleash = EntityLeashKnot.getKnotForPosition(this.world, blockposition);
            if (entityleash == null) {
               entityleash = EntityLeashKnot.createKnot(this.world, blockposition);
            }

            this.leashedToEntity = entityleash;
         } else {
            this.world.getServer().getPluginManager().callEvent(new EntityUnleashEvent(this.getBukkitEntity(), UnleashReason.UNKNOWN));
            this.clearLeashed(false, true);
         }
      }

      this.leashNBTTag = null;
   }

   public boolean replaceItemInInventory(int i, @Nullable ItemStack itemstack) {
      EntityEquipmentSlot enumitemslot;
      if (i == 98) {
         enumitemslot = EntityEquipmentSlot.MAINHAND;
      } else if (i == 99) {
         enumitemslot = EntityEquipmentSlot.OFFHAND;
      } else if (i == 100 + EntityEquipmentSlot.HEAD.getIndex()) {
         enumitemslot = EntityEquipmentSlot.HEAD;
      } else if (i == 100 + EntityEquipmentSlot.CHEST.getIndex()) {
         enumitemslot = EntityEquipmentSlot.CHEST;
      } else if (i == 100 + EntityEquipmentSlot.LEGS.getIndex()) {
         enumitemslot = EntityEquipmentSlot.LEGS;
      } else {
         if (i != 100 + EntityEquipmentSlot.FEET.getIndex()) {
            return false;
         }

         enumitemslot = EntityEquipmentSlot.FEET;
      }

      if (itemstack != null && !isItemStackInSlot(enumitemslot, itemstack) && enumitemslot != EntityEquipmentSlot.HEAD) {
         return false;
      } else {
         this.setItemStackToSlot(enumitemslot, itemstack);
         return true;
      }
   }

   public static boolean isItemStackInSlot(EntityEquipmentSlot enumitemslot, ItemStack itemstack) {
      EntityEquipmentSlot enumitemslot1 = getSlotForItemStack(itemstack);
      return enumitemslot1 == enumitemslot || enumitemslot1 == EntityEquipmentSlot.MAINHAND && enumitemslot == EntityEquipmentSlot.OFFHAND || enumitemslot1 == EntityEquipmentSlot.OFFHAND && enumitemslot == EntityEquipmentSlot.MAINHAND;
   }

   public boolean isServerWorld() {
      return super.isServerWorld() && !this.isAIDisabled();
   }

   public void setNoAI(boolean flag) {
      byte b0 = ((Byte)this.dataManager.get(AI_FLAGS)).byteValue();
      this.dataManager.set(AI_FLAGS, Byte.valueOf(flag ? (byte)(b0 | 1) : (byte)(b0 & -2)));
   }

   public void setLeftHanded(boolean flag) {
      byte b0 = ((Byte)this.dataManager.get(AI_FLAGS)).byteValue();
      this.dataManager.set(AI_FLAGS, Byte.valueOf(flag ? (byte)(b0 | 2) : (byte)(b0 & -3)));
   }

   public boolean isAIDisabled() {
      return (((Byte)this.dataManager.get(AI_FLAGS)).byteValue() & 1) != 0;
   }

   public boolean isLeftHanded() {
      return (((Byte)this.dataManager.get(AI_FLAGS)).byteValue() & 2) != 0;
   }

   public EnumHandSide getPrimaryHand() {
      return this.isLeftHanded() ? EnumHandSide.LEFT : EnumHandSide.RIGHT;
   }

   public static enum SpawnPlacementType {
      ON_GROUND,
      IN_AIR,
      IN_WATER;
   }

   static class SyntheticClass_1 {
      static final int[] a;
      static final int[] b = new int[EntityEquipmentSlot.values().length];

      static {
         try {
            b[EntityEquipmentSlot.HEAD.ordinal()] = 1;
         } catch (NoSuchFieldError var5) {
            ;
         }

         try {
            b[EntityEquipmentSlot.CHEST.ordinal()] = 2;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            b[EntityEquipmentSlot.LEGS.ordinal()] = 3;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            b[EntityEquipmentSlot.FEET.ordinal()] = 4;
         } catch (NoSuchFieldError var2) {
            ;
         }

         a = new int[EntityEquipmentSlot.Type.values().length];

         try {
            a[EntityEquipmentSlot.Type.HAND.ordinal()] = 1;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[EntityEquipmentSlot.Type.ARMOR.ordinal()] = 2;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
