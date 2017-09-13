package net.minecraft.entity;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
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
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class EntityLiving extends EntityLivingBase {
   private static final DataParameter AI_FLAGS = EntityDataManager.createKey(EntityLiving.class, DataSerializers.BYTE);
   public int livingSoundTime;
   protected int experienceValue;
   private final EntityLookHelper lookHelper;
   protected EntityMoveHelper moveHelper;
   protected EntityJumpHelper jumpHelper;
   private final EntityBodyHelper bodyHelper;
   protected PathNavigate navigator;
   public final EntityAITasks tasks;
   public final EntityAITasks targetTasks;
   private EntityLivingBase attackTarget;
   private final EntitySenses senses;
   private final ItemStack[] inventoryHands = new ItemStack[2];
   protected float[] inventoryHandsDropChances = new float[2];
   private final ItemStack[] inventoryArmor = new ItemStack[4];
   protected float[] inventoryArmorDropChances = new float[4];
   private boolean canPickUpLoot;
   private boolean persistenceRequired;
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
      this.attackTarget = var1;
      ForgeHooks.onLivingSetAttackTarget(this, var1);
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
            double var8 = 10.0D;
            this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width - var2 * 10.0D, this.posY + (double)(this.rand.nextFloat() * this.height) - var4 * 10.0D, this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width - var6 * 10.0D, var2, var4, var6);
         }
      } else {
         this.world.setEntityState(this, (byte)20);
      }

   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte var1) {
      if (var1 == 20) {
         this.spawnExplosionParticle();
      } else {
         super.handleStatusUpdate(var1);
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
      NBTTagList var10 = new NBTTagList();

      for(ItemStack var20 : this.inventoryHands) {
         NBTTagCompound var8 = new NBTTagCompound();
         if (var20 != null) {
            var20.writeToNBT(var8);
         }

         var10.appendTag(var8);
      }

      var1.setTag("HandItems", var10);
      NBTTagList var12 = new NBTTagList();

      for(float var25 : this.inventoryArmorDropChances) {
         var12.appendTag(new NBTTagFloat(var25));
      }

      var1.setTag("ArmorDropChances", var12);
      NBTTagList var15 = new NBTTagList();

      for(float var9 : this.inventoryHandsDropChances) {
         var15.appendTag(new NBTTagFloat(var9));
      }

      var1.setTag("HandDropChances", var15);
      var1.setBoolean("Leashed", this.isLeashed);
      if (this.leashedToEntity != null) {
         NBTTagCompound var19 = new NBTTagCompound();
         if (this.leashedToEntity instanceof EntityLivingBase) {
            UUID var23 = this.leashedToEntity.getUniqueID();
            var19.setUniqueId("UUID", var23);
         } else if (this.leashedToEntity instanceof EntityHanging) {
            BlockPos var24 = ((EntityHanging)this.leashedToEntity).getHangingPosition();
            var19.setInteger("X", var24.getX());
            var19.setInteger("Y", var24.getY());
            var19.setInteger("Z", var24.getZ());
         }

         var1.setTag("Leash", var19);
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
         this.setCanPickUpLoot(var1.getBoolean("CanPickUpLoot"));
      }

      this.persistenceRequired = var1.getBoolean("PersistenceRequired");
      if (var1.hasKey("ArmorItems", 9)) {
         NBTTagList var2 = var1.getTagList("ArmorItems", 10);

         for(int var3 = 0; var3 < this.inventoryArmor.length; ++var3) {
            this.inventoryArmor[var3] = ItemStack.loadItemStackFromNBT(var2.getCompoundTagAt(var3));
         }
      }

      if (var1.hasKey("HandItems", 9)) {
         NBTTagList var4 = var1.getTagList("HandItems", 10);

         for(int var7 = 0; var7 < this.inventoryHands.length; ++var7) {
            this.inventoryHands[var7] = ItemStack.loadItemStackFromNBT(var4.getCompoundTagAt(var7));
         }
      }

      if (var1.hasKey("ArmorDropChances", 9)) {
         NBTTagList var5 = var1.getTagList("ArmorDropChances", 5);

         for(int var8 = 0; var8 < var5.tagCount(); ++var8) {
            this.inventoryArmorDropChances[var8] = var5.getFloatAt(var8);
         }
      }

      if (var1.hasKey("HandDropChances", 9)) {
         NBTTagList var6 = var1.getTagList("HandDropChances", 5);

         for(int var9 = 0; var9 < var6.tagCount(); ++var9) {
            this.inventoryHandsDropChances[var9] = var6.getFloatAt(var9);
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

         for(ItemStack var8 : var5.generateLootForPools(this.deathLootTableSeed == 0L ? this.rand : new Random(this.deathLootTableSeed), var6.build())) {
            this.entityDropItem(var8, 0.0F);
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
         for(EntityItem var2 : this.world.getEntitiesWithinAABB(EntityItem.class, this.getEntityBoundingBox().expand(1.0D, 0.0D, 1.0D))) {
            if (!var2.isDead && var2.getEntityItem() != null && !var2.cannotPickup()) {
               this.updateEquipmentIfNeeded(var2);
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
            ItemArmor var9 = (ItemArmor)var2.getItem();
            ItemArmor var11 = (ItemArmor)var5.getItem();
            if (var9.damageReduceAmount == var11.damageReduceAmount) {
               var4 = var2.getMetadata() > var5.getMetadata() || var2.hasTagCompound() && !var5.hasTagCompound();
            } else {
               var4 = var9.damageReduceAmount > var11.damageReduceAmount;
            }
         } else {
            var4 = false;
         }
      }

      if (var4 && this.canEquipItem(var2)) {
         double var10;
         switch(var3.getSlotType()) {
         case HAND:
            var10 = (double)this.inventoryHandsDropChances[var3.getIndex()];
            break;
         case ARMOR:
            var10 = (double)this.inventoryArmorDropChances[var3.getIndex()];
            break;
         default:
            var10 = 0.0D;
         }

         if (var5 != null && (double)(this.rand.nextFloat() - 0.1F) < var10) {
            this.entityDropItem(var5, 0.0F);
         }

         if (var2.getItem() == Items.DIAMOND && var1.getThrower() != null) {
            EntityPlayer var8 = this.world.getPlayerEntityByName(var1.getThrower());
            if (var8 != null) {
               var8.addStat(AchievementList.DIAMONDS_TO_YOU);
            }
         }

         this.setItemStackToSlot(var3, var2);
         switch(var3.getSlotType()) {
         case HAND:
            this.inventoryHandsDropChances[var3.getIndex()] = 2.0F;
            break;
         case ARMOR:
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
      Result var1 = null;
      if (this.persistenceRequired) {
         this.entityAge = 0;
      } else if ((this.entityAge & 31) == 31 && (var1 = ForgeEventFactory.canEntityDespawn(this)) != Result.DEFAULT) {
         if (var1 == Result.DENY) {
            this.entityAge = 0;
         } else {
            this.setDead();
         }
      } else {
         EntityPlayer var2 = this.world.getClosestPlayerToEntity(this, -1.0D);
         if (var2 != null) {
            double var3 = var2.posX - this.posX;
            double var5 = var2.posY - this.posY;
            double var7 = var2.posZ - this.posZ;
            double var9 = var3 * var3 + var5 * var5 + var7 * var7;
            if (this.canDespawn() && var9 > 16384.0D) {
               this.setDead();
            }

            if (this.entityAge > 600 && this.rand.nextInt(800) == 0 && var9 > 1024.0D && this.canDespawn()) {
               this.setDead();
            } else if (var9 < 1024.0D) {
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
      double var8;
      if (var1 instanceof EntityLivingBase) {
         EntityLivingBase var10 = (EntityLivingBase)var1;
         var8 = var10.posY + (double)var10.getEyeHeight() - (this.posY + (double)this.getEyeHeight());
      } else {
         var8 = (var1.getEntityBoundingBox().minY + var1.getEntityBoundingBox().maxY) / 2.0D - (this.posY + (double)this.getEyeHeight());
      }

      double var14 = (double)MathHelper.sqrt(var4 * var4 + var6 * var6);
      float var12 = (float)(MathHelper.atan2(var6, var4) * 57.29577951308232D) - 90.0F;
      float var13 = (float)(-(MathHelper.atan2(var8, var14) * 57.29577951308232D));
      this.rotationPitch = this.updateRotation(this.rotationPitch, var13, var3);
      this.rotationYaw = this.updateRotation(this.rotationYaw, var12, var2);
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

   public float getRenderSizeModifier() {
      return 1.0F;
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
      switch(var1.getSlotType()) {
      case HAND:
         var2 = this.inventoryHands[var1.getIndex()];
         break;
      case ARMOR:
         var2 = this.inventoryArmor[var1.getIndex()];
      }

      return var2;
   }

   public void setItemStackToSlot(EntityEquipmentSlot var1, @Nullable ItemStack var2) {
      switch(var1.getSlotType()) {
      case HAND:
         this.inventoryHands[var1.getIndex()] = var2;
         break;
      case ARMOR:
         this.inventoryArmor[var1.getIndex()] = var2;
      }

   }

   protected void dropEquipment(boolean var1, int var2) {
      for(EntityEquipmentSlot var6 : EntityEquipmentSlot.values()) {
         ItemStack var7 = this.getItemStackFromSlot(var6);
         double var8;
         switch(var6.getSlotType()) {
         case HAND:
            var8 = (double)this.inventoryHandsDropChances[var6.getIndex()];
            break;
         case ARMOR:
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
      switch(var0) {
      case HEAD:
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
      case CHEST:
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
      case LEGS:
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
      case FEET:
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
      switch(var1.getSlotType()) {
      case HAND:
         this.inventoryHandsDropChances[var1.getIndex()] = var2;
         break;
      case ARMOR:
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
         this.clearLeashed(true, !var1.capabilities.isCreativeMode);
         return true;
      } else if (var2 != null && var2.getItem() == Items.LEAD && this.canBeLeashedTo(var1)) {
         this.setLeashedToEntity(var1, true);
         --var2.stackSize;
         return true;
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
            this.clearLeashed(true, true);
         }

         if (this.leashedToEntity == null || this.leashedToEntity.isDead) {
            this.clearLeashed(true, true);
         }
      }

   }

   public void clearLeashed(boolean var1, boolean var2) {
      if (this.isLeashed) {
         this.isLeashed = false;
         this.leashedToEntity = null;
         if (!this.world.isRemote && var2) {
            this.dropItem(Items.LEAD, 1);
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

            for(EntityLivingBase var3 : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expandXyz(10.0D))) {
               if (var3.getUniqueID().equals(var1)) {
                  this.leashedToEntity = var3;
                  break;
               }
            }
         } else if (this.leashNBTTag.hasKey("X", 99) && this.leashNBTTag.hasKey("Y", 99) && this.leashNBTTag.hasKey("Z", 99)) {
            BlockPos var4 = new BlockPos(this.leashNBTTag.getInteger("X"), this.leashNBTTag.getInteger("Y"), this.leashNBTTag.getInteger("Z"));
            EntityLeashKnot var5 = EntityLeashKnot.getKnotForPosition(this.world, var4);
            if (var5 == null) {
               var5 = EntityLeashKnot.createKnot(this.world, var4);
            }

            this.leashedToEntity = var5;
         } else {
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
}
