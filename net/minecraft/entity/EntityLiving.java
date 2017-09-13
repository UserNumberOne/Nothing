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

   public EntityLiving(World var1) {
      super(var1);
      this.tasks = new EntityAITasks(var1 != null && var1.theProfiler != null ? var1.theProfiler : null);
      this.targetTasks = new EntityAITasks(var1 != null && var1.theProfiler != null ? var1.theProfiler : null);
      this.lookHelper = new EntityLookHelper(this);
      this.moveHelper = new EntityMoveHelper(this);
      this.jumpHelper = new EntityJumpHelper(this);
      this.bodyHelper = this.createBodyHelper();
      this.navigator = this.createNavigator(var1);
      this.senses = new EntitySenses(this);
      Arrays.fill(this.inventoryArmorDropChances, 0.085F);
      Arrays.fill(this.inventoryHandsDropChances, 0.085F);
      if (var1 != null && !var1.isRemote) {
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

   protected PathNavigate createNavigator(World var1) {
      return new PathNavigateGround(this, var1);
   }

   public float getPathPriority(PathNodeType var1) {
      Float var2 = (Float)this.mapPathPriority.get(var1);
      return var2 == null ? var1.getPriority() : var2.floatValue();
   }

   public void setPathPriority(PathNodeType var1, float var2) {
      this.mapPathPriority.put(var1, Float.valueOf(var2));
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

   public void setAttackTarget(@Nullable EntityLivingBase var1) {
      this.setGoalTarget(var1, TargetReason.UNKNOWN, true);
   }

   public boolean setGoalTarget(EntityLivingBase var1, TargetReason var2, boolean var3) {
      if (this.getAttackTarget() == var1) {
         return false;
      } else {
         if (var3) {
            if (var2 == TargetReason.UNKNOWN && this.getAttackTarget() != null && var1 == null) {
               var2 = this.getAttackTarget().isEntityAlive() ? TargetReason.FORGOT_TARGET : TargetReason.TARGET_DIED;
            }

            if (var2 == TargetReason.UNKNOWN) {
               this.world.getServer().getLogger().log(Level.WARNING, "Unknown target reason, please report on the issue tracker", new Exception());
            }

            CraftLivingEntity var4 = null;
            if (var1 != null) {
               var4 = (CraftLivingEntity)var1.getBukkitEntity();
            }

            EntityTargetLivingEntityEvent var5 = new EntityTargetLivingEntityEvent(this.getBukkitEntity(), var4, var2);
            this.world.getServer().getPluginManager().callEvent(var5);
            if (var5.isCancelled()) {
               return false;
            }

            if (var5.getTarget() != null) {
               var1 = ((CraftLivingEntity)var5.getTarget()).getHandle();
            } else {
               var1 = null;
            }
         }

         this.attackTarget = var1;
         return true;
      }
   }

   public boolean canAttackClass(Class var1) {
      return var1 != EntityGhast.class;
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
      SoundEvent var1 = this.getAmbientSound();
      if (var1 != null) {
         this.playSound(var1, this.getSoundVolume(), this.getSoundPitch());
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

   protected void playHurtSound(DamageSource var1) {
      this.applyEntityAI();
      super.playHurtSound(var1);
   }

   private void applyEntityAI() {
      this.livingSoundTime = -this.getTalkInterval();
   }

   protected int getExperiencePoints(EntityPlayer var1) {
      if (this.experienceValue > 0) {
         int var2 = this.experienceValue;

         for(int var3 = 0; var3 < this.inventoryArmor.length; ++var3) {
            if (this.inventoryArmor[var3] != null && this.inventoryArmorDropChances[var3] <= 1.0F) {
               var2 += 1 + this.rand.nextInt(3);
            }
         }

         for(int var4 = 0; var4 < this.inventoryHands.length; ++var4) {
            if (this.inventoryHands[var4] != null && this.inventoryHandsDropChances[var4] <= 1.0F) {
               var2 += 1 + this.rand.nextInt(3);
            }
         }

         return var2;
      } else {
         return this.experienceValue;
      }
   }

   public void spawnExplosionParticle() {
      if (this.world.isRemote) {
         for(int var1 = 0; var1 < 20; ++var1) {
            double var2 = this.rand.nextGaussian() * 0.02D;
            double var4 = this.rand.nextGaussian() * 0.02D;
            double var6 = this.rand.nextGaussian() * 0.02D;
            this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width - var2 * 10.0D, this.posY + (double)(this.rand.nextFloat() * this.height) - var4 * 10.0D, this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width - var6 * 10.0D, var2, var4, var6);
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
            boolean var1 = !(this.getControllingPassenger() instanceof EntityLiving);
            boolean var2 = !(this.getRidingEntity() instanceof EntityBoat);
            this.tasks.setControlFlag(1, var1);
            this.tasks.setControlFlag(4, var1 && var2);
            this.tasks.setControlFlag(2, var1);
         }
      }

   }

   protected float updateDistance(float var1, float var2) {
      this.bodyHelper.updateRenderAngles();
      return var2;
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return null;
   }

   @Nullable
   protected Item getDropItem() {
      return null;
   }

   protected void dropFewItems(boolean var1, int var2) {
      Item var3 = this.getDropItem();
      if (var3 != null) {
         int var4 = this.rand.nextInt(3);
         if (var2 > 0) {
            var4 += this.rand.nextInt(var2 + 1);
         }

         for(int var5 = 0; var5 < var4; ++var5) {
            this.dropItem(var3, 1);
         }
      }

   }

   public static void registerFixesMob(DataFixer var0, String var1) {
      var0.registerWalker(FixTypes.ENTITY, new ItemStackDataLists(var1, new String[]{"ArmorItems", "HandItems"}));
   }

   public static void registerFixesMob(DataFixer var0) {
      registerFixesMob(var0, "Mob");
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setBoolean("CanPickUpLoot", this.canPickUpLoot());
      var1.setBoolean("PersistenceRequired", this.persistenceRequired);
      NBTTagList var2 = new NBTTagList();

      for(ItemStack var6 : this.inventoryArmor) {
         NBTTagCompound var7 = new NBTTagCompound();
         if (var6 != null) {
            var6.writeToNBT(var7);
         }

         var2.appendTag(var7);
      }

      var1.setTag("ArmorItems", var2);
      NBTTagList var17 = new NBTTagList();

      for(ItemStack var9 : this.inventoryHands) {
         NBTTagCompound var10 = new NBTTagCompound();
         if (var9 != null) {
            var9.writeToNBT(var10);
         }

         var17.appendTag(var10);
      }

      var1.setTag("HandItems", var17);
      NBTTagList var20 = new NBTTagList();

      for(float var12 : this.inventoryArmorDropChances) {
         var20.appendTag(new NBTTagFloat(var12));
      }

      var1.setTag("ArmorDropChances", var20);
      NBTTagList var23 = new NBTTagList();

      for(float var15 : this.inventoryHandsDropChances) {
         var23.appendTag(new NBTTagFloat(var15));
      }

      var1.setTag("HandDropChances", var23);
      var1.setBoolean("Leashed", this.isLeashed);
      if (this.leashedToEntity != null) {
         NBTTagCompound var24 = new NBTTagCompound();
         if (this.leashedToEntity instanceof EntityLivingBase) {
            UUID var25 = this.leashedToEntity.getUniqueID();
            var24.setUniqueId("UUID", var25);
         } else if (this.leashedToEntity instanceof EntityHanging) {
            BlockPos var26 = ((EntityHanging)this.leashedToEntity).getHangingPosition();
            var24.setInteger("X", var26.getX());
            var24.setInteger("Y", var26.getY());
            var24.setInteger("Z", var26.getZ());
         }

         var1.setTag("Leash", var24);
      }

      var1.setBoolean("LeftHanded", this.isLeftHanded());
      if (this.deathLootTable != null) {
         var1.setString("DeathLootTable", this.deathLootTable.toString());
         if (this.deathLootTableSeed != 0L) {
            var1.setLong("DeathLootTableSeed", this.deathLootTableSeed);
         }
      }

      if (this.isAIDisabled()) {
         var1.setBoolean("NoAI", this.isAIDisabled());
      }

   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      if (var1.hasKey("CanPickUpLoot", 1)) {
         boolean var2 = var1.getBoolean("CanPickUpLoot");
         if (isLevelAtLeast(var1, 1) || var2) {
            this.setJumping(var2);
         }
      }

      boolean var5 = var1.getBoolean("PersistenceRequired");
      if (isLevelAtLeast(var1, 1) || var5) {
         this.persistenceRequired = var5;
      }

      if (var1.hasKey("ArmorItems", 9)) {
         NBTTagList var3 = var1.getTagList("ArmorItems", 10);

         for(int var4 = 0; var4 < this.inventoryArmor.length; ++var4) {
            this.inventoryArmor[var4] = ItemStack.loadItemStackFromNBT(var3.getCompoundTagAt(var4));
         }
      }

      if (var1.hasKey("HandItems", 9)) {
         NBTTagList var6 = var1.getTagList("HandItems", 10);

         for(int var9 = 0; var9 < this.inventoryHands.length; ++var9) {
            this.inventoryHands[var9] = ItemStack.loadItemStackFromNBT(var6.getCompoundTagAt(var9));
         }
      }

      if (var1.hasKey("ArmorDropChances", 9)) {
         NBTTagList var7 = var1.getTagList("ArmorDropChances", 5);

         for(int var10 = 0; var10 < var7.tagCount(); ++var10) {
            this.inventoryArmorDropChances[var10] = var7.getFloatAt(var10);
         }
      }

      if (var1.hasKey("HandDropChances", 9)) {
         NBTTagList var8 = var1.getTagList("HandDropChances", 5);

         for(int var11 = 0; var11 < var8.tagCount(); ++var11) {
            this.inventoryHandsDropChances[var11] = var8.getFloatAt(var11);
         }
      }

      this.isLeashed = var1.getBoolean("Leashed");
      if (this.isLeashed && var1.hasKey("Leash", 10)) {
         this.leashNBTTag = var1.getCompoundTag("Leash");
      }

      this.setLeftHanded(var1.getBoolean("LeftHanded"));
      if (var1.hasKey("DeathLootTable", 8)) {
         this.deathLootTable = new ResourceLocation(var1.getString("DeathLootTable"));
         this.deathLootTableSeed = var1.getLong("DeathLootTableSeed");
      }

      this.setNoAI(var1.getBoolean("NoAI"));
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return null;
   }

   protected void dropLoot(boolean var1, int var2, DamageSource var3) {
      ResourceLocation var4 = this.deathLootTable;
      if (var4 == null) {
         var4 = this.getLootTable();
      }

      if (var4 != null) {
         LootTable var5 = this.world.getLootTableManager().getLootTableFromLocation(var4);
         this.deathLootTable = null;
         LootContext.Builder var6 = (new LootContext.Builder((WorldServer)this.world)).withLootedEntity(this).withDamageSource(var3);
         if (var1 && this.attackingPlayer != null) {
            var6 = var6.withPlayer(this.attackingPlayer).withLuck(this.attackingPlayer.getLuck());
         }

         for(ItemStack var9 : var5.generateLootForPools(this.deathLootTableSeed == 0L ? this.rand : new Random(this.deathLootTableSeed), var6.build())) {
            this.entityDropItem(var9, 0.0F);
         }

         this.dropEquipment(var1, var2);
      } else {
         super.dropLoot(var1, var2, var3);
      }

   }

   public void setMoveForward(float var1) {
      this.moveForward = var1;
   }

   public void setMoveStrafing(float var1) {
      this.moveStrafing = var1;
   }

   public void setAIMoveSpeed(float var1) {
      super.setAIMoveSpeed(var1);
      this.setMoveForward(var1);
   }

   public void onLivingUpdate() {
      super.onLivingUpdate();
      this.world.theProfiler.startSection("looting");
      if (!this.world.isRemote && this.canPickUpLoot() && !this.dead && this.world.getGameRules().getBoolean("mobGriefing")) {
         for(EntityItem var3 : this.world.getEntitiesWithinAABB(EntityItem.class, this.getEntityBoundingBox().expand(1.0D, 0.0D, 1.0D))) {
            if (!var3.isDead && var3.getEntityItem() != null && !var3.cannotPickup()) {
               this.updateEquipmentIfNeeded(var3);
            }
         }
      }

      this.world.theProfiler.endSection();
   }

   protected void updateEquipmentIfNeeded(EntityItem var1) {
      ItemStack var2 = var1.getEntityItem();
      EntityEquipmentSlot var3 = getSlotForItemStack(var2);
      boolean var4 = true;
      ItemStack var5 = this.getItemStackFromSlot(var3);
      if (var5 != null) {
         if (var3.getSlotType() == EntityEquipmentSlot.Type.HAND) {
            if (var2.getItem() instanceof ItemSword && !(var5.getItem() instanceof ItemSword)) {
               var4 = true;
            } else if (var2.getItem() instanceof ItemSword && var5.getItem() instanceof ItemSword) {
               ItemSword var6 = (ItemSword)var2.getItem();
               ItemSword var7 = (ItemSword)var5.getItem();
               if (var6.getDamageVsEntity() == var7.getDamageVsEntity()) {
                  var4 = var2.getMetadata() > var5.getMetadata() || var2.hasTagCompound() && !var5.hasTagCompound();
               } else {
                  var4 = var6.getDamageVsEntity() > var7.getDamageVsEntity();
               }
            } else if (var2.getItem() instanceof ItemBow && var5.getItem() instanceof ItemBow) {
               var4 = var2.hasTagCompound() && !var5.hasTagCompound();
            } else {
               var4 = false;
            }
         } else if (var2.getItem() instanceof ItemArmor && !(var5.getItem() instanceof ItemArmor)) {
            var4 = true;
         } else if (var2.getItem() instanceof ItemArmor && var5.getItem() instanceof ItemArmor) {
            ItemArmor var11 = (ItemArmor)var2.getItem();
            ItemArmor var12 = (ItemArmor)var5.getItem();
            if (var11.damageReduceAmount == var12.damageReduceAmount) {
               var4 = var2.getMetadata() > var5.getMetadata() || var2.hasTagCompound() && !var5.hasTagCompound();
            } else {
               var4 = var11.damageReduceAmount > var12.damageReduceAmount;
            }
         } else {
            var4 = false;
         }
      }

      if (var4 && this.canEquipItem(var2)) {
         double var8;
         switch(EntityLiving.SyntheticClass_1.a[var3.getSlotType().ordinal()]) {
         case 1:
            var8 = (double)this.inventoryHandsDropChances[var3.getIndex()];
            break;
         case 2:
            var8 = (double)this.inventoryArmorDropChances[var3.getIndex()];
            break;
         default:
            var8 = 0.0D;
         }

         if (var5 != null && (double)(this.rand.nextFloat() - 0.1F) < var8) {
            this.entityDropItem(var5, 0.0F);
         }

         if (var2.getItem() == Items.DIAMOND && var1.getThrower() != null) {
            EntityPlayer var10 = this.world.getPlayerEntityByName(var1.getThrower());
            if (var10 != null) {
               var10.addStat(AchievementList.DIAMONDS_TO_YOU);
            }
         }

         this.setItemStackToSlot(var3, var2);
         switch(EntityLiving.SyntheticClass_1.a[var3.getSlotType().ordinal()]) {
         case 1:
            this.inventoryHandsDropChances[var3.getIndex()] = 2.0F;
            break;
         case 2:
            this.inventoryArmorDropChances[var3.getIndex()] = 2.0F;
         }

         this.persistenceRequired = true;
         this.onItemPickup(var1, 1);
         var1.setDead();
      }

   }

   protected boolean canEquipItem(ItemStack var1) {
      return true;
   }

   protected boolean canDespawn() {
      return true;
   }

   protected void despawnEntity() {
      if (this.persistenceRequired) {
         this.entityAge = 0;
      } else {
         EntityPlayer var1 = this.world.getClosestPlayerToEntity(this, -1.0D);
         if (var1 != null) {
            double var2 = var1.posX - this.posX;
            double var4 = var1.posY - this.posY;
            double var6 = var1.posZ - this.posZ;
            double var8 = var2 * var2 + var4 * var4 + var6 * var6;
            if (var8 > 16384.0D) {
               this.setDead();
            }

            if (this.entityAge > 600 && this.rand.nextInt(800) == 0 && var8 > 1024.0D) {
               this.setDead();
            } else if (var8 < 1024.0D) {
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
         EntityLiving var1 = (EntityLiving)this.getRidingEntity();
         var1.getNavigator().setPath(this.getNavigator().getPath(), 1.5D);
         var1.getMoveHelper().read(this.getMoveHelper());
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

   public void faceEntity(Entity var1, float var2, float var3) {
      double var4 = var1.posX - this.posX;
      double var6 = var1.posZ - this.posZ;
      double var9;
      if (var1 instanceof EntityLivingBase) {
         EntityLivingBase var8 = (EntityLivingBase)var1;
         var9 = var8.posY + (double)var8.getEyeHeight() - (this.posY + (double)this.getEyeHeight());
      } else {
         var9 = (var1.getEntityBoundingBox().minY + var1.getEntityBoundingBox().maxY) / 2.0D - (this.posY + (double)this.getEyeHeight());
      }

      double var11 = (double)MathHelper.sqrt(var4 * var4 + var6 * var6);
      float var13 = (float)(MathHelper.atan2(var6, var4) * 57.2957763671875D) - 90.0F;
      float var14 = (float)(-(MathHelper.atan2(var9, var11) * 57.2957763671875D));
      this.rotationPitch = this.updateRotation(this.rotationPitch, var14, var3);
      this.rotationYaw = this.updateRotation(this.rotationYaw, var13, var2);
   }

   private float updateRotation(float var1, float var2, float var3) {
      float var4 = MathHelper.wrapDegrees(var2 - var1);
      if (var4 > var3) {
         var4 = var3;
      }

      if (var4 < -var3) {
         var4 = -var3;
      }

      return var1 + var4;
   }

   public boolean getCanSpawnHere() {
      IBlockState var1 = this.world.getBlockState((new BlockPos(this)).down());
      return var1.canEntitySpawn(this);
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
         int var1 = (int)(this.getHealth() - this.getMaxHealth() * 0.33F);
         var1 = var1 - (3 - this.world.getDifficulty().getDifficultyId()) * 4;
         if (var1 < 0) {
            var1 = 0;
         }

         return var1 + 3;
      }
   }

   public Iterable getHeldEquipment() {
      return Arrays.asList(this.inventoryHands);
   }

   public Iterable getArmorInventoryList() {
      return Arrays.asList(this.inventoryArmor);
   }

   @Nullable
   public ItemStack getItemStackFromSlot(EntityEquipmentSlot var1) {
      ItemStack var2 = null;
      switch(EntityLiving.SyntheticClass_1.a[var1.getSlotType().ordinal()]) {
      case 1:
         var2 = this.inventoryHands[var1.getIndex()];
         break;
      case 2:
         var2 = this.inventoryArmor[var1.getIndex()];
      }

      return var2;
   }

   public void setItemStackToSlot(EntityEquipmentSlot var1, @Nullable ItemStack var2) {
      switch(EntityLiving.SyntheticClass_1.a[var1.getSlotType().ordinal()]) {
      case 1:
         this.inventoryHands[var1.getIndex()] = var2;
         break;
      case 2:
         this.inventoryArmor[var1.getIndex()] = var2;
      }

   }

   protected void dropEquipment(boolean var1, int var2) {
      for(EntityEquipmentSlot var6 : EntityEquipmentSlot.values()) {
         ItemStack var7 = this.getItemStackFromSlot(var6);
         double var8;
         switch(EntityLiving.SyntheticClass_1.a[var6.getSlotType().ordinal()]) {
         case 1:
            var8 = (double)this.inventoryHandsDropChances[var6.getIndex()];
            break;
         case 2:
            var8 = (double)this.inventoryArmorDropChances[var6.getIndex()];
            break;
         default:
            var8 = 0.0D;
         }

         boolean var10 = var8 > 1.0D;
         if (var7 != null && (var1 || var10) && (double)(this.rand.nextFloat() - (float)var2 * 0.01F) < var8) {
            if (!var10 && var7.isItemStackDamageable()) {
               int var11 = Math.max(var7.getMaxDamage() - 25, 1);
               int var12 = var7.getMaxDamage() - this.rand.nextInt(this.rand.nextInt(var11) + 1);
               if (var12 > var11) {
                  var12 = var11;
               }

               if (var12 < 1) {
                  var12 = 1;
               }

               var7.setItemDamage(var12);
            }

            this.entityDropItem(var7, 0.0F);
         }
      }

   }

   protected void setEquipmentBasedOnDifficulty(DifficultyInstance var1) {
      if (this.rand.nextFloat() < 0.15F * var1.getClampedAdditionalDifficulty()) {
         int var2 = this.rand.nextInt(2);
         float var3 = this.world.getDifficulty() == EnumDifficulty.HARD ? 0.1F : 0.25F;
         if (this.rand.nextFloat() < 0.095F) {
            ++var2;
         }

         if (this.rand.nextFloat() < 0.095F) {
            ++var2;
         }

         if (this.rand.nextFloat() < 0.095F) {
            ++var2;
         }

         boolean var4 = true;

         for(EntityEquipmentSlot var8 : EntityEquipmentSlot.values()) {
            if (var8.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
               ItemStack var9 = this.getItemStackFromSlot(var8);
               if (!var4 && this.rand.nextFloat() < var3) {
                  break;
               }

               var4 = false;
               if (var9 == null) {
                  Item var10 = getArmorByChance(var8, var2);
                  if (var10 != null) {
                     this.setItemStackToSlot(var8, new ItemStack(var10));
                  }
               }
            }
         }
      }

   }

   public static EntityEquipmentSlot getSlotForItemStack(ItemStack var0) {
      return var0.getItem() != Item.getItemFromBlock(Blocks.PUMPKIN) && var0.getItem() != Items.SKULL ? (var0.getItem() instanceof ItemArmor ? ((ItemArmor)var0.getItem()).armorType : (var0.getItem() == Items.ELYTRA ? EntityEquipmentSlot.CHEST : (var0.getItem() == Items.SHIELD ? EntityEquipmentSlot.OFFHAND : EntityEquipmentSlot.MAINHAND))) : EntityEquipmentSlot.HEAD;
   }

   public static Item getArmorByChance(EntityEquipmentSlot var0, int var1) {
      switch(EntityLiving.SyntheticClass_1.b[var0.ordinal()]) {
      case 1:
         if (var1 == 0) {
            return Items.LEATHER_HELMET;
         } else if (var1 == 1) {
            return Items.GOLDEN_HELMET;
         } else if (var1 == 2) {
            return Items.CHAINMAIL_HELMET;
         } else if (var1 == 3) {
            return Items.IRON_HELMET;
         } else if (var1 == 4) {
            return Items.DIAMOND_HELMET;
         }
      case 2:
         if (var1 == 0) {
            return Items.LEATHER_CHESTPLATE;
         } else if (var1 == 1) {
            return Items.GOLDEN_CHESTPLATE;
         } else if (var1 == 2) {
            return Items.CHAINMAIL_CHESTPLATE;
         } else if (var1 == 3) {
            return Items.IRON_CHESTPLATE;
         } else if (var1 == 4) {
            return Items.DIAMOND_CHESTPLATE;
         }
      case 3:
         if (var1 == 0) {
            return Items.LEATHER_LEGGINGS;
         } else if (var1 == 1) {
            return Items.GOLDEN_LEGGINGS;
         } else if (var1 == 2) {
            return Items.CHAINMAIL_LEGGINGS;
         } else if (var1 == 3) {
            return Items.IRON_LEGGINGS;
         } else if (var1 == 4) {
            return Items.DIAMOND_LEGGINGS;
         }
      case 4:
         if (var1 == 0) {
            return Items.LEATHER_BOOTS;
         } else if (var1 == 1) {
            return Items.GOLDEN_BOOTS;
         } else if (var1 == 2) {
            return Items.CHAINMAIL_BOOTS;
         } else if (var1 == 3) {
            return Items.IRON_BOOTS;
         } else if (var1 == 4) {
            return Items.DIAMOND_BOOTS;
         }
      default:
         return null;
      }
   }

   protected void setEnchantmentBasedOnDifficulty(DifficultyInstance var1) {
      float var2 = var1.getClampedAdditionalDifficulty();
      if (this.getHeldItemMainhand() != null && this.rand.nextFloat() < 0.25F * var2) {
         EnchantmentHelper.addRandomEnchantment(this.rand, this.getHeldItemMainhand(), (int)(5.0F + var2 * (float)this.rand.nextInt(18)), false);
      }

      for(EntityEquipmentSlot var6 : EntityEquipmentSlot.values()) {
         if (var6.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
            ItemStack var7 = this.getItemStackFromSlot(var6);
            if (var7 != null && this.rand.nextFloat() < 0.5F * var2) {
               EnchantmentHelper.addRandomEnchantment(this.rand, var7, (int)(5.0F + var2 * (float)this.rand.nextInt(18)), false);
            }
         }
      }

   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance var1, @Nullable IEntityLivingData var2) {
      this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).applyModifier(new AttributeModifier("Random spawn bonus", this.rand.nextGaussian() * 0.05D, 1));
      if (this.rand.nextFloat() < 0.05F) {
         this.setLeftHanded(true);
      } else {
         this.setLeftHanded(false);
      }

      return var2;
   }

   public boolean canBeSteered() {
      return false;
   }

   public void enablePersistence() {
      this.persistenceRequired = true;
   }

   public void setDropChance(EntityEquipmentSlot var1, float var2) {
      switch(EntityLiving.SyntheticClass_1.a[var1.getSlotType().ordinal()]) {
      case 1:
         this.inventoryHandsDropChances[var1.getIndex()] = var2;
         break;
      case 2:
         this.inventoryArmorDropChances[var1.getIndex()] = var2;
      }

   }

   public boolean canPickUpLoot() {
      return this.canPickUpLoot;
   }

   public void setCanPickUpLoot(boolean var1) {
      this.canPickUpLoot = var1;
   }

   public boolean isNoDespawnRequired() {
      return this.persistenceRequired;
   }

   public final boolean processInitialInteract(EntityPlayer var1, @Nullable ItemStack var2, EnumHand var3) {
      if (this.getLeashed() && this.getLeashedToEntity() == var1) {
         if (CraftEventFactory.callPlayerUnleashEntityEvent(this, var1).isCancelled()) {
            ((EntityPlayerMP)var1).connection.sendPacket(new SPacketEntityAttach(this, this.getLeashedToEntity()));
            return false;
         } else {
            this.clearLeashed(true, !var1.capabilities.isCreativeMode);
            return true;
         }
      } else if (var2 != null && var2.getItem() == Items.LEAD && this.canBeLeashedTo(var1)) {
         if (CraftEventFactory.callPlayerLeashEntityEvent(this, var1, var1).isCancelled()) {
            ((EntityPlayerMP)var1).connection.sendPacket(new SPacketEntityAttach(this, this.getLeashedToEntity()));
            return false;
         } else {
            this.setLeashedToEntity(var1, true);
            --var2.stackSize;
            return true;
         }
      } else {
         return this.processInteract(var1, var3, var2) ? true : super.processInitialInteract(var1, var2, var3);
      }
   }

   protected boolean processInteract(EntityPlayer var1, EnumHand var2, @Nullable ItemStack var3) {
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

   public void clearLeashed(boolean var1, boolean var2) {
      if (this.isLeashed) {
         this.isLeashed = false;
         this.leashedToEntity = null;
         if (!this.world.isRemote && var2) {
            this.forceDrops = true;
            this.dropItem(Items.LEAD, 1);
            this.forceDrops = false;
         }

         if (!this.world.isRemote && var1 && this.world instanceof WorldServer) {
            ((WorldServer)this.world).getEntityTracker().sendToTracking(this, new SPacketEntityAttach(this, (Entity)null));
         }
      }

   }

   public boolean canBeLeashedTo(EntityPlayer var1) {
      return !this.getLeashed() && !(this instanceof IMob);
   }

   public boolean getLeashed() {
      return this.isLeashed;
   }

   public Entity getLeashedToEntity() {
      return this.leashedToEntity;
   }

   public void setLeashedToEntity(Entity var1, boolean var2) {
      this.isLeashed = true;
      this.leashedToEntity = var1;
      if (!this.world.isRemote && var2 && this.world instanceof WorldServer) {
         ((WorldServer)this.world).getEntityTracker().sendToTracking(this, new SPacketEntityAttach(this, this.leashedToEntity));
      }

      if (this.isRiding()) {
         this.dismountRidingEntity();
      }

   }

   public boolean startRiding(Entity var1, boolean var2) {
      boolean var3 = super.startRiding(var1, var2);
      if (var3 && this.getLeashed()) {
         this.clearLeashed(true, true);
      }

      return var3;
   }

   private void recreateLeash() {
      if (this.isLeashed && this.leashNBTTag != null) {
         if (this.leashNBTTag.hasUniqueId("UUID")) {
            UUID var1 = this.leashNBTTag.getUniqueId("UUID");

            for(EntityLivingBase var4 : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expandXyz(10.0D))) {
               if (var4.getUniqueID().equals(var1)) {
                  this.leashedToEntity = var4;
                  break;
               }
            }
         } else if (this.leashNBTTag.hasKey("X", 99) && this.leashNBTTag.hasKey("Y", 99) && this.leashNBTTag.hasKey("Z", 99)) {
            BlockPos var5 = new BlockPos(this.leashNBTTag.getInteger("X"), this.leashNBTTag.getInteger("Y"), this.leashNBTTag.getInteger("Z"));
            EntityLeashKnot var6 = EntityLeashKnot.getKnotForPosition(this.world, var5);
            if (var6 == null) {
               var6 = EntityLeashKnot.createKnot(this.world, var5);
            }

            this.leashedToEntity = var6;
         } else {
            this.world.getServer().getPluginManager().callEvent(new EntityUnleashEvent(this.getBukkitEntity(), UnleashReason.UNKNOWN));
            this.clearLeashed(false, true);
         }
      }

      this.leashNBTTag = null;
   }

   public boolean replaceItemInInventory(int var1, @Nullable ItemStack var2) {
      EntityEquipmentSlot var3;
      if (var1 == 98) {
         var3 = EntityEquipmentSlot.MAINHAND;
      } else if (var1 == 99) {
         var3 = EntityEquipmentSlot.OFFHAND;
      } else if (var1 == 100 + EntityEquipmentSlot.HEAD.getIndex()) {
         var3 = EntityEquipmentSlot.HEAD;
      } else if (var1 == 100 + EntityEquipmentSlot.CHEST.getIndex()) {
         var3 = EntityEquipmentSlot.CHEST;
      } else if (var1 == 100 + EntityEquipmentSlot.LEGS.getIndex()) {
         var3 = EntityEquipmentSlot.LEGS;
      } else {
         if (var1 != 100 + EntityEquipmentSlot.FEET.getIndex()) {
            return false;
         }

         var3 = EntityEquipmentSlot.FEET;
      }

      if (var2 != null && !isItemStackInSlot(var3, var2) && var3 != EntityEquipmentSlot.HEAD) {
         return false;
      } else {
         this.setItemStackToSlot(var3, var2);
         return true;
      }
   }

   public static boolean isItemStackInSlot(EntityEquipmentSlot var0, ItemStack var1) {
      EntityEquipmentSlot var2 = getSlotForItemStack(var1);
      return var2 == var0 || var2 == EntityEquipmentSlot.MAINHAND && var0 == EntityEquipmentSlot.OFFHAND || var2 == EntityEquipmentSlot.OFFHAND && var0 == EntityEquipmentSlot.MAINHAND;
   }

   public boolean isServerWorld() {
      return super.isServerWorld() && !this.isAIDisabled();
   }

   public void setNoAI(boolean var1) {
      byte var2 = ((Byte)this.dataManager.get(AI_FLAGS)).byteValue();
      this.dataManager.set(AI_FLAGS, Byte.valueOf(var1 ? (byte)(var2 | 1) : (byte)(var2 & -2)));
   }

   public void setLeftHanded(boolean var1) {
      byte var2 = ((Byte)this.dataManager.get(AI_FLAGS)).byteValue();
      this.dataManager.set(AI_FLAGS, Byte.valueOf(var1 ? (byte)(var2 | 2) : (byte)(var2 & -3)));
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
