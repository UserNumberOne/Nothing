package net.minecraft.entity;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentFrostWalker;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.network.play.server.SPacketCollectItem;
import net.minecraft.network.play.server.SPacketEntityEquipment;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.stats.StatList;
import net.minecraft.util.CombatRules;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public abstract class EntityLivingBase extends Entity {
   private static final UUID SPRINTING_SPEED_BOOST_ID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
   private static final AttributeModifier SPRINTING_SPEED_BOOST = (new AttributeModifier(SPRINTING_SPEED_BOOST_ID, "Sprinting speed boost", 0.30000001192092896D, 2)).setSaved(false);
   protected static final DataParameter HAND_STATES = EntityDataManager.createKey(EntityLivingBase.class, DataSerializers.BYTE);
   public static final DataParameter HEALTH = EntityDataManager.createKey(EntityLivingBase.class, DataSerializers.FLOAT);
   private static final DataParameter POTION_EFFECTS = EntityDataManager.createKey(EntityLivingBase.class, DataSerializers.VARINT);
   private static final DataParameter HIDE_PARTICLES = EntityDataManager.createKey(EntityLivingBase.class, DataSerializers.BOOLEAN);
   private static final DataParameter ARROW_COUNT_IN_ENTITY = EntityDataManager.createKey(EntityLivingBase.class, DataSerializers.VARINT);
   private AbstractAttributeMap attributeMap;
   public CombatTracker _combatTracker = new CombatTracker(this);
   public final Map activePotionsMap = Maps.newHashMap();
   private final ItemStack[] handInventory = new ItemStack[2];
   private final ItemStack[] armorArray = new ItemStack[4];
   public boolean isSwingInProgress;
   public EnumHand swingingHand;
   public int swingProgressInt;
   public int arrowHitTimer;
   public int hurtTime;
   public int maxHurtTime;
   public float attackedAtYaw;
   public int deathTime;
   public float prevSwingProgress;
   public float swingProgress;
   protected int ticksSinceLastSwing;
   public float prevLimbSwingAmount;
   public float limbSwingAmount;
   public float limbSwing;
   public int maxHurtResistantTime = 20;
   public float prevCameraPitch;
   public float cameraPitch;
   public float randomUnused2;
   public float randomUnused1;
   public float renderYawOffset;
   public float prevRenderYawOffset;
   public float rotationYawHead;
   public float prevRotationYawHead;
   public float jumpMovementFactor = 0.02F;
   public EntityPlayer attackingPlayer;
   protected int recentlyHit;
   protected boolean dead;
   protected int entityAge;
   protected float prevOnGroundSpeedFactor;
   protected float onGroundSpeedFactor;
   protected float movedDistance;
   protected float prevMovedDistance;
   protected float unused180;
   protected int scoreValue;
   public float lastDamage;
   protected boolean isJumping;
   public float moveStrafing;
   public float moveForward;
   public float randomYawVelocity;
   protected int newPosRotationIncrements;
   protected double interpTargetX;
   protected double interpTargetY;
   protected double interpTargetZ;
   protected double interpTargetYaw;
   protected double interpTargetPitch;
   public boolean potionsNeedUpdate = true;
   public EntityLivingBase entityLivingToAttack;
   public int revengeTimer;
   private EntityLivingBase lastAttacker;
   private int lastAttackerTime;
   private float landMovementFactor;
   private int jumpTicks;
   private float absorptionAmount;
   protected ItemStack activeItemStack;
   protected int activeItemStackUseCount;
   protected int ticksElytraFlying;
   private BlockPos prevBlockpos;
   private DamageSource lastDamageSource;
   private long lastDamageStamp;
   public int expToDrop;
   public int maxAirTicks = 300;
   boolean forceDrops;
   ArrayList drops = new ArrayList();
   public CraftAttributeMap craftAttributes;
   public boolean collides = true;
   private boolean isTickingEffects = false;
   private List effectsToProcess = Lists.newArrayList();

   public void onKillCommand() {
      this.attackEntityFrom(DamageSource.outOfWorld, Float.MAX_VALUE);
   }

   public EntityLivingBase(World world) {
      super(world);
      this.applyEntityAttributes();
      this.dataManager.set(HEALTH, Float.valueOf((float)this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue()));
      this.preventEntitySpawning = true;
      this.randomUnused1 = (float)((Math.random() + 1.0D) * 0.009999999776482582D);
      this.setPosition(this.posX, this.posY, this.posZ);
      this.randomUnused2 = (float)Math.random() * 12398.0F;
      this.rotationYaw = (float)(Math.random() * 6.2831854820251465D);
      this.rotationYawHead = this.rotationYaw;
      this.stepHeight = 0.6F;
   }

   protected void entityInit() {
      this.dataManager.register(HAND_STATES, Byte.valueOf((byte)0));
      this.dataManager.register(POTION_EFFECTS, Integer.valueOf(0));
      this.dataManager.register(HIDE_PARTICLES, Boolean.valueOf(false));
      this.dataManager.register(ARROW_COUNT_IN_ENTITY, Integer.valueOf(0));
      this.dataManager.register(HEALTH, Float.valueOf(1.0F));
   }

   protected void applyEntityAttributes() {
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.MAX_HEALTH);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ARMOR);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS);
   }

   protected void updateFallState(double d0, boolean flag, IBlockState iblockdata, BlockPos blockposition) {
      if (!this.isInWater()) {
         this.handleWaterMovement();
      }

      if (!this.world.isRemote && this.fallDistance > 3.0F && flag) {
         float f = (float)MathHelper.ceil(this.fallDistance - 3.0F);
         if (iblockdata.getMaterial() != Material.AIR) {
            double d1 = Math.min((double)(0.2F + f / 15.0F), 2.5D);
            int i = (int)(150.0D * d1);
            if (this instanceof EntityPlayerMP) {
               ((WorldServer)this.world).sendParticles((EntityPlayerMP)this, EnumParticleTypes.BLOCK_DUST, false, this.posX, this.posY, this.posZ, i, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, Block.getStateId(iblockdata));
            } else {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY, this.posZ, i, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, Block.getStateId(iblockdata));
            }
         }
      }

      super.updateFallState(d0, flag, iblockdata, blockposition);
   }

   public boolean canBreatheUnderwater() {
      return false;
   }

   public void onEntityUpdate() {
      this.prevSwingProgress = this.swingProgress;
      super.onEntityUpdate();
      this.world.theProfiler.startSection("livingEntityBaseTick");
      boolean flag = this instanceof EntityPlayer;
      if (this.isEntityAlive()) {
         if (this.isEntityInsideOpaqueBlock()) {
            this.attackEntityFrom(DamageSource.inWall, 1.0F);
         } else if (flag && !this.world.getWorldBorder().contains(this.getEntityBoundingBox())) {
            double d0 = this.world.getWorldBorder().getClosestDistance(this) + this.world.getWorldBorder().getDamageBuffer();
            if (d0 < 0.0D) {
               double d1 = this.world.getWorldBorder().getDamageAmount();
               if (d1 > 0.0D) {
                  this.attackEntityFrom(DamageSource.inWall, (float)Math.max(1, MathHelper.floor(-d0 * d1)));
               }
            }
         }
      }

      if (this.isImmuneToFire() || this.world.isRemote) {
         this.extinguish();
      }

      boolean flag1 = flag && ((EntityPlayer)this).capabilities.disableDamage;
      if (this.isEntityAlive()) {
         if (!this.isInsideOfMaterial(Material.WATER)) {
            if (this.getAir() != 300) {
               this.setAir(this.maxAirTicks);
            }
         } else {
            if (!this.canBreatheUnderwater() && !this.isPotionActive(MobEffects.WATER_BREATHING) && !flag1) {
               this.setAir(this.decreaseAirSupply(this.getAir()));
               if (this.getAir() == -20) {
                  this.setAir(0);

                  for(int i = 0; i < 8; ++i) {
                     float f = this.rand.nextFloat() - this.rand.nextFloat();
                     float f1 = this.rand.nextFloat() - this.rand.nextFloat();
                     float f2 = this.rand.nextFloat() - this.rand.nextFloat();
                     this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX + (double)f, this.posY + (double)f1, this.posZ + (double)f2, this.motionX, this.motionY, this.motionZ);
                  }

                  this.attackEntityFrom(DamageSource.drown, 2.0F);
               }
            }

            if (!this.world.isRemote && this.isRiding() && this.getRidingEntity() instanceof EntityLivingBase) {
               this.dismountRidingEntity();
            }
         }

         if (!this.world.isRemote) {
            BlockPos blockposition = new BlockPos(this);
            if (!Objects.equal(this.prevBlockpos, blockposition)) {
               this.prevBlockpos = blockposition;
               this.frostWalk(blockposition);
            }
         }
      }

      if (this.isEntityAlive() && this.isWet()) {
         this.extinguish();
      }

      this.prevCameraPitch = this.cameraPitch;
      if (this.hurtTime > 0) {
         --this.hurtTime;
      }

      if (this.hurtResistantTime > 0 && !(this instanceof EntityPlayerMP)) {
         --this.hurtResistantTime;
      }

      if (this.getHealth() <= 0.0F) {
         this.onDeathUpdate();
      }

      if (this.recentlyHit > 0) {
         --this.recentlyHit;
      } else {
         this.attackingPlayer = null;
      }

      if (this.lastAttacker != null && !this.lastAttacker.isEntityAlive()) {
         this.lastAttacker = null;
      }

      if (this.entityLivingToAttack != null) {
         if (!this.entityLivingToAttack.isEntityAlive()) {
            this.setRevengeTarget((EntityLivingBase)null);
         } else if (this.ticksExisted - this.revengeTimer > 100) {
            this.setRevengeTarget((EntityLivingBase)null);
         }
      }

      this.updatePotionEffects();
      this.prevMovedDistance = this.movedDistance;
      this.prevRenderYawOffset = this.renderYawOffset;
      this.prevRotationYawHead = this.rotationYawHead;
      this.prevRotationYaw = this.rotationYaw;
      this.prevRotationPitch = this.rotationPitch;
      this.world.theProfiler.endSection();
   }

   public int getExpReward() {
      int exp = this.getExperiencePoints(this.attackingPlayer);
      return !this.world.isRemote && (this.recentlyHit > 0 || this.isPlayer()) && this.canDropLoot() && this.world.getGameRules().getBoolean("doMobLoot") ? exp : 0;
   }

   protected void frostWalk(BlockPos blockposition) {
      int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FROST_WALKER, this);
      if (i > 0) {
         EnchantmentFrostWalker.freezeNearby(this, this.world, blockposition, i);
      }

   }

   public boolean isChild() {
      return false;
   }

   protected void onDeathUpdate() {
      ++this.deathTime;
      if (this.deathTime >= 20 && !this.isDead) {
         int i = this.expToDrop;

         while(i > 0) {
            int j = EntityXPOrb.getXPSplit(i);
            i -= j;
            this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY, this.posZ, j));
         }

         this.expToDrop = 0;
         this.setDead();

         for(int var9 = 0; var9 < 20; ++var9) {
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            double d2 = this.rand.nextGaussian() * 0.02D;
            this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d0, d1, d2);
         }
      }

   }

   protected boolean canDropLoot() {
      return !this.isChild();
   }

   protected int decreaseAirSupply(int i) {
      int j = EnchantmentHelper.getRespirationModifier(this);
      return j > 0 && this.rand.nextInt(j + 1) > 0 ? i : i - 1;
   }

   protected int getExperiencePoints(EntityPlayer entityhuman) {
      return 0;
   }

   protected boolean isPlayer() {
      return false;
   }

   public Random getRNG() {
      return this.rand;
   }

   public EntityLivingBase getAITarget() {
      return this.entityLivingToAttack;
   }

   public int getRevengeTimer() {
      return this.revengeTimer;
   }

   public void setRevengeTarget(@Nullable EntityLivingBase entityliving) {
      this.entityLivingToAttack = entityliving;
      this.revengeTimer = this.ticksExisted;
   }

   public EntityLivingBase getLastAttacker() {
      return this.lastAttacker;
   }

   public int getLastAttackerTime() {
      return this.lastAttackerTime;
   }

   public void setLastAttacker(Entity entity) {
      if (entity instanceof EntityLivingBase) {
         this.lastAttacker = (EntityLivingBase)entity;
      } else {
         this.lastAttacker = null;
      }

      this.lastAttackerTime = this.ticksExisted;
   }

   public int getAge() {
      return this.entityAge;
   }

   protected void playEquipSound(@Nullable ItemStack itemstack) {
      if (itemstack != null) {
         SoundEvent soundeffect = SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
         Item item = itemstack.getItem();
         if (item instanceof ItemArmor) {
            soundeffect = ((ItemArmor)item).getArmorMaterial().getSoundEvent();
         } else if (item == Items.ELYTRA) {
            soundeffect = SoundEvents.ITEM_ARMOR_EQUIP_LEATHER;
         }

         this.playSound(soundeffect, 1.0F, 1.0F);
      }

   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setFloat("Health", this.getHealth());
      nbttagcompound.setShort("HurtTime", (short)this.hurtTime);
      nbttagcompound.setInteger("HurtByTimestamp", this.revengeTimer);
      nbttagcompound.setShort("DeathTime", (short)this.deathTime);
      nbttagcompound.setFloat("AbsorptionAmount", this.getAbsorptionAmount());

      for(EntityEquipmentSlot enumitemslot : EntityEquipmentSlot.values()) {
         ItemStack itemstack = this.getItemStackFromSlot(enumitemslot);
         if (itemstack != null) {
            this.getAttributeMap().removeAttributeModifiers(itemstack.getAttributeModifiers(enumitemslot));
         }
      }

      nbttagcompound.setTag("Attributes", SharedMonsterAttributes.writeBaseAttributeMapToNBT(this.getAttributeMap()));

      for(EntityEquipmentSlot enumitemslot : EntityEquipmentSlot.values()) {
         ItemStack itemstack = this.getItemStackFromSlot(enumitemslot);
         if (itemstack != null) {
            this.getAttributeMap().applyAttributeModifiers(itemstack.getAttributeModifiers(enumitemslot));
         }
      }

      if (!this.activePotionsMap.isEmpty()) {
         NBTTagList nbttaglist = new NBTTagList();

         for(PotionEffect mobeffect : this.activePotionsMap.values()) {
            nbttaglist.appendTag(mobeffect.writeCustomPotionEffectToNBT(new NBTTagCompound()));
         }

         nbttagcompound.setTag("ActiveEffects", nbttaglist);
      }

      nbttagcompound.setBoolean("FallFlying", this.isElytraFlying());
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      this.setAbsorptionAmount(nbttagcompound.getFloat("AbsorptionAmount"));
      if (nbttagcompound.hasKey("Attributes", 9) && this.world != null && !this.world.isRemote) {
         SharedMonsterAttributes.setAttributeModifiers(this.getAttributeMap(), nbttagcompound.getTagList("Attributes", 10));
      }

      if (nbttagcompound.hasKey("ActiveEffects", 9)) {
         NBTTagList nbttaglist = nbttagcompound.getTagList("ActiveEffects", 10);

         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            PotionEffect mobeffect = PotionEffect.readCustomPotionEffectFromNBT(nbttagcompound1);
            if (mobeffect != null) {
               this.activePotionsMap.put(mobeffect.getPotion(), mobeffect);
            }
         }
      }

      if (nbttagcompound.hasKey("Bukkit.MaxHealth")) {
         NBTBase nbtbase = nbttagcompound.getTag("Bukkit.MaxHealth");
         if (nbtbase.getId() == 5) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(((NBTTagFloat)nbtbase).getDouble());
         } else if (nbtbase.getId() == 3) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(((NBTTagInt)nbtbase).getDouble());
         }
      }

      if (nbttagcompound.hasKey("Health", 99)) {
         this.setHealth(nbttagcompound.getFloat("Health"));
      }

      this.hurtTime = nbttagcompound.getShort("HurtTime");
      this.deathTime = nbttagcompound.getShort("DeathTime");
      this.revengeTimer = nbttagcompound.getInteger("HurtByTimestamp");
      if (nbttagcompound.hasKey("Team", 8)) {
         String s = nbttagcompound.getString("Team");
         this.world.getScoreboard().addPlayerToTeam(this.getCachedUniqueIdString(), s);
      }

      if (nbttagcompound.getBoolean("FallFlying")) {
         this.setFlag(7, true);
      }

   }

   protected void updatePotionEffects() {
      Iterator iterator = this.activePotionsMap.keySet().iterator();
      this.isTickingEffects = true;

      while(iterator.hasNext()) {
         Potion mobeffectlist = (Potion)iterator.next();
         PotionEffect mobeffect = (PotionEffect)this.activePotionsMap.get(mobeffectlist);
         if (!mobeffect.onUpdate(this)) {
            if (!this.world.isRemote) {
               iterator.remove();
               this.onFinishedPotionEffect(mobeffect);
            }
         } else if (mobeffect.getDuration() % 600 == 0) {
            this.onChangedPotionEffect(mobeffect, false);
         }
      }

      this.isTickingEffects = false;

      for(Object e : this.effectsToProcess) {
         if (e instanceof PotionEffect) {
            this.addPotionEffect((PotionEffect)e);
         } else {
            this.removePotionEffect((Potion)e);
         }
      }

      if (this.potionsNeedUpdate) {
         if (!this.world.isRemote) {
            this.updatePotionMetadata();
         }

         this.potionsNeedUpdate = false;
      }

      int i = ((Integer)this.dataManager.get(POTION_EFFECTS)).intValue();
      boolean flag = ((Boolean)this.dataManager.get(HIDE_PARTICLES)).booleanValue();
      if (i > 0) {
         boolean flag1;
         if (this.isInvisible()) {
            flag1 = this.rand.nextInt(15) == 0;
         } else {
            flag1 = this.rand.nextBoolean();
         }

         if (flag) {
            flag1 &= this.rand.nextInt(5) == 0;
         }

         if (flag1 && i > 0) {
            double d0 = (double)(i >> 16 & 255) / 255.0D;
            double d1 = (double)(i >> 8 & 255) / 255.0D;
            double d2 = (double)(i >> 0 & 255) / 255.0D;
            this.world.spawnParticle(flag ? EnumParticleTypes.SPELL_MOB_AMBIENT : EnumParticleTypes.SPELL_MOB, this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, d0, d1, d2);
         }
      }

   }

   protected void updatePotionMetadata() {
      if (this.activePotionsMap.isEmpty()) {
         this.resetPotionEffectMetadata();
         this.setInvisible(false);
      } else {
         Collection collection = this.activePotionsMap.values();
         this.dataManager.set(HIDE_PARTICLES, Boolean.valueOf(areAllPotionsAmbient(collection)));
         this.dataManager.set(POTION_EFFECTS, Integer.valueOf(PotionUtils.getPotionColorFromEffectList(collection)));
         this.setInvisible(this.isPotionActive(MobEffects.INVISIBILITY));
      }

   }

   public static boolean areAllPotionsAmbient(Collection collection) {
      for(PotionEffect mobeffect : collection) {
         if (!mobeffect.getIsAmbient()) {
            return false;
         }
      }

      return true;
   }

   protected void resetPotionEffectMetadata() {
      this.dataManager.set(HIDE_PARTICLES, Boolean.valueOf(false));
      this.dataManager.set(POTION_EFFECTS, Integer.valueOf(0));
   }

   public void clearActivePotions() {
      if (!this.world.isRemote) {
         Iterator iterator = this.activePotionsMap.values().iterator();

         while(iterator.hasNext()) {
            this.onFinishedPotionEffect((PotionEffect)iterator.next());
            iterator.remove();
         }
      }

   }

   public Collection getActivePotionEffects() {
      return this.activePotionsMap.values();
   }

   public boolean isPotionActive(Potion mobeffectlist) {
      return this.activePotionsMap.containsKey(mobeffectlist);
   }

   @Nullable
   public PotionEffect getActivePotionEffect(Potion mobeffectlist) {
      return (PotionEffect)this.activePotionsMap.get(mobeffectlist);
   }

   public void addPotionEffect(PotionEffect mobeffect) {
      if (this.isTickingEffects) {
         this.effectsToProcess.add(mobeffect);
      } else {
         if (this.isPotionApplicable(mobeffect)) {
            PotionEffect mobeffect1 = (PotionEffect)this.activePotionsMap.get(mobeffect.getPotion());
            if (mobeffect1 == null) {
               this.activePotionsMap.put(mobeffect.getPotion(), mobeffect);
               this.onNewPotionEffect(mobeffect);
            } else {
               mobeffect1.combine(mobeffect);
               this.onChangedPotionEffect(mobeffect1, true);
            }
         }

      }
   }

   public boolean isPotionApplicable(PotionEffect mobeffect) {
      if (this.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD) {
         Potion mobeffectlist = mobeffect.getPotion();
         if (mobeffectlist == MobEffects.REGENERATION || mobeffectlist == MobEffects.POISON) {
            return false;
         }
      }

      return true;
   }

   public boolean isEntityUndead() {
      return this.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD;
   }

   @Nullable
   public PotionEffect removeActivePotionEffect(@Nullable Potion mobeffectlist) {
      if (this.isTickingEffects) {
         this.effectsToProcess.add(mobeffectlist);
         return null;
      } else {
         return (PotionEffect)this.activePotionsMap.remove(mobeffectlist);
      }
   }

   public void removePotionEffect(Potion mobeffectlist) {
      PotionEffect mobeffect = this.removeActivePotionEffect(mobeffectlist);
      if (mobeffect != null) {
         this.onFinishedPotionEffect(mobeffect);
      }

   }

   protected void onNewPotionEffect(PotionEffect mobeffect) {
      this.potionsNeedUpdate = true;
      if (!this.world.isRemote) {
         mobeffect.getPotion().applyAttributesModifiersToEntity(this, this.getAttributeMap(), mobeffect.getAmplifier());
      }

   }

   protected void onChangedPotionEffect(PotionEffect mobeffect, boolean flag) {
      this.potionsNeedUpdate = true;
      if (flag && !this.world.isRemote) {
         Potion mobeffectlist = mobeffect.getPotion();
         mobeffectlist.removeAttributesModifiersFromEntity(this, this.getAttributeMap(), mobeffect.getAmplifier());
         mobeffectlist.applyAttributesModifiersToEntity(this, this.getAttributeMap(), mobeffect.getAmplifier());
      }

   }

   protected void onFinishedPotionEffect(PotionEffect mobeffect) {
      this.potionsNeedUpdate = true;
      if (!this.world.isRemote) {
         mobeffect.getPotion().removeAttributesModifiersFromEntity(this, this.getAttributeMap(), mobeffect.getAmplifier());
      }

   }

   public void heal(float f) {
      this.heal(f, RegainReason.CUSTOM);
   }

   public void heal(float f, RegainReason regainReason) {
      float f1 = this.getHealth();
      if (f1 > 0.0F) {
         EntityRegainHealthEvent event = new EntityRegainHealthEvent(this.getBukkitEntity(), (double)f, regainReason);
         this.world.getServer().getPluginManager().callEvent(event);
         if (!event.isCancelled()) {
            this.setHealth((float)((double)this.getHealth() + event.getAmount()));
         }
      }

   }

   public final float getHealth() {
      return this instanceof EntityPlayerMP ? (float)((EntityPlayerMP)this).getBukkitEntity().getHealth() : ((Float)this.dataManager.get(HEALTH)).floatValue();
   }

   public void setHealth(float f) {
      if (this instanceof EntityPlayerMP) {
         CraftPlayer player = ((EntityPlayerMP)this).getBukkitEntity();
         if (f < 0.0F) {
            player.setRealHealth(0.0D);
         } else if ((double)f > player.getMaxHealth()) {
            player.setRealHealth(player.getMaxHealth());
         } else {
            player.setRealHealth((double)f);
         }

         this.dataManager.set(HEALTH, Float.valueOf(player.getScaledHealth()));
      } else {
         this.dataManager.set(HEALTH, Float.valueOf(MathHelper.clamp(f, 0.0F, this.getMaxHealth())));
      }
   }

   public boolean attackEntityFrom(DamageSource damagesource, float f) {
      if (this.isEntityInvulnerable(damagesource)) {
         return false;
      } else if (this.world.isRemote) {
         return false;
      } else {
         this.entityAge = 0;
         if (this.getHealth() <= 0.0F) {
            return false;
         } else if (damagesource.isFireDamage() && this.isPotionActive(MobEffects.FIRE_RESISTANCE)) {
            return false;
         } else {
            boolean flag = f > 0.0F && this.canBlockDamageSource(damagesource);
            this.limbSwingAmount = 1.5F;
            boolean flag1 = true;
            if ((float)this.hurtResistantTime > (float)this.maxHurtResistantTime / 2.0F) {
               if (f <= this.lastDamage) {
                  this.forceExplosionKnockback = true;
                  return false;
               }

               if (!this.damageEntity0(damagesource, f - this.lastDamage)) {
                  return false;
               }

               this.lastDamage = f;
               flag1 = false;
            } else {
               if (!this.damageEntity0(damagesource, f)) {
                  return false;
               }

               this.lastDamage = f;
               this.hurtResistantTime = this.maxHurtResistantTime;
               this.maxHurtTime = 10;
               this.hurtTime = this.maxHurtTime;
            }

            if (this instanceof EntityAnimal) {
               ((EntityAnimal)this).resetInLove();
               if (this instanceof EntityTameable) {
                  ((EntityTameable)this).getAISit().setSitting(false);
               }
            }

            this.attackedAtYaw = 0.0F;
            Entity entity = damagesource.getEntity();
            if (entity != null) {
               if (entity instanceof EntityLivingBase) {
                  this.setRevengeTarget((EntityLivingBase)entity);
               }

               if (entity instanceof EntityPlayer) {
                  this.recentlyHit = 100;
                  this.attackingPlayer = (EntityPlayer)entity;
               } else if (entity instanceof EntityWolf) {
                  EntityWolf entitywolf = (EntityWolf)entity;
                  if (entitywolf.isTamed()) {
                     this.recentlyHit = 100;
                     this.attackingPlayer = null;
                  }
               }
            }

            if (flag1) {
               if (flag) {
                  this.world.setEntityState(this, (byte)29);
               } else if (damagesource instanceof EntityDamageSource && ((EntityDamageSource)damagesource).getIsThornsDamage()) {
                  this.world.setEntityState(this, (byte)33);
               } else {
                  this.world.setEntityState(this, (byte)2);
               }

               if (damagesource != DamageSource.drown && (!flag || f > 0.0F)) {
                  this.setBeenAttacked();
               }

               if (entity != null) {
                  double d0 = entity.posX - this.posX;

                  double d1;
                  for(d1 = entity.posZ - this.posZ; d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
                     d0 = (Math.random() - Math.random()) * 0.01D;
                  }

                  this.attackedAtYaw = (float)(MathHelper.atan2(d1, d0) * 57.2957763671875D - (double)this.rotationYaw);
                  this.knockBack(entity, 0.4F, d0, d1);
               } else {
                  this.attackedAtYaw = (float)((int)(Math.random() * 2.0D) * 180);
               }
            }

            if (this.getHealth() <= 0.0F) {
               SoundEvent soundeffect = this.getDeathSound();
               if (flag1 && soundeffect != null) {
                  this.playSound(soundeffect, this.getSoundVolume(), this.getSoundPitch());
               }

               this.onDeath(damagesource);
            } else if (flag1) {
               this.playHurtSound(damagesource);
            }

            if (!flag || f > 0.0F) {
               this.lastDamageSource = damagesource;
               this.lastDamageStamp = this.world.getTotalWorldTime();
            }

            return !flag || f > 0.0F;
         }
      }
   }

   @Nullable
   public DamageSource getLastDamageSource() {
      if (this.world.getTotalWorldTime() - this.lastDamageStamp > 40L) {
         this.lastDamageSource = null;
      }

      return this.lastDamageSource;
   }

   protected void playHurtSound(DamageSource damagesource) {
      SoundEvent soundeffect = this.getHurtSound();
      if (soundeffect != null) {
         this.playSound(soundeffect, this.getSoundVolume(), this.getSoundPitch());
      }

   }

   private boolean canBlockDamageSource(DamageSource damagesource) {
      if (!damagesource.isUnblockable() && this.isActiveItemStackBlocking()) {
         Vec3d vec3d = damagesource.getDamageLocation();
         if (vec3d != null) {
            Vec3d vec3d1 = this.getLook(1.0F);
            Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(this.posX, this.posY, this.posZ)).normalize();
            vec3d2 = new Vec3d(vec3d2.xCoord, 0.0D, vec3d2.zCoord);
            if (vec3d2.dotProduct(vec3d1) < 0.0D) {
               return true;
            }
         }
      }

      return false;
   }

   public void renderBrokenItemStack(ItemStack itemstack) {
      this.playSound(SoundEvents.ENTITY_ITEM_BREAK, 0.8F, 0.8F + this.world.rand.nextFloat() * 0.4F);

      for(int i = 0; i < 5; ++i) {
         Vec3d vec3d = new Vec3d(((double)this.rand.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
         vec3d = vec3d.rotatePitch(-this.rotationPitch * 0.017453292F);
         vec3d = vec3d.rotateYaw(-this.rotationYaw * 0.017453292F);
         double d0 = (double)(-this.rand.nextFloat()) * 0.6D - 0.3D;
         Vec3d vec3d1 = new Vec3d(((double)this.rand.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
         vec3d1 = vec3d1.rotatePitch(-this.rotationPitch * 0.017453292F);
         vec3d1 = vec3d1.rotateYaw(-this.rotationYaw * 0.017453292F);
         vec3d1 = vec3d1.addVector(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ);
         this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, vec3d1.xCoord, vec3d1.yCoord, vec3d1.zCoord, vec3d.xCoord, vec3d.yCoord + 0.05D, vec3d.zCoord, Item.getIdFromItem(itemstack.getItem()));
      }

   }

   public void onDeath(DamageSource damagesource) {
      if (!this.dead) {
         Entity entity = damagesource.getEntity();
         EntityLivingBase entityliving = this.getAttackingEntity();
         if (this.scoreValue >= 0 && entityliving != null) {
            entityliving.addToPlayerScore(this, this.scoreValue);
         }

         if (entity != null) {
            entity.onKillEntity(this);
         }

         this.dead = true;
         this.getCombatTracker().reset();
         if (!this.world.isRemote) {
            int i = 0;
            if (entity instanceof EntityPlayer) {
               i = EnchantmentHelper.getLootingModifier((EntityLivingBase)entity);
            }

            if (this.canDropLoot() && this.world.getGameRules().getBoolean("doMobLoot")) {
               boolean flag = this.recentlyHit > 0;
               this.dropLoot(flag, i, damagesource);
               CraftEventFactory.callEntityDeathEvent(this, this.drops);
               this.drops = new ArrayList();
            } else {
               CraftEventFactory.callEntityDeathEvent(this);
            }
         }

         this.world.setEntityState(this, (byte)3);
      }

   }

   protected void dropLoot(boolean flag, int i, DamageSource damagesource) {
      this.dropFewItems(flag, i);
      this.dropEquipment(flag, i);
   }

   protected void dropEquipment(boolean flag, int i) {
   }

   public void knockBack(Entity entity, float f, double d0, double d1) {
      if (this.rand.nextDouble() >= this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue()) {
         this.isAirBorne = true;
         float f1 = MathHelper.sqrt(d0 * d0 + d1 * d1);
         this.motionX /= 2.0D;
         this.motionZ /= 2.0D;
         this.motionX -= d0 / (double)f1 * (double)f;
         this.motionZ -= d1 / (double)f1 * (double)f;
         if (this.onGround) {
            this.motionY /= 2.0D;
            this.motionY += (double)f;
            if (this.motionY > 0.4000000059604645D) {
               this.motionY = 0.4000000059604645D;
            }
         }
      }

   }

   @Nullable
   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_GENERIC_HURT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_GENERIC_DEATH;
   }

   protected SoundEvent getFallSound(int i) {
      return i > 4 ? SoundEvents.ENTITY_GENERIC_BIG_FALL : SoundEvents.ENTITY_GENERIC_SMALL_FALL;
   }

   protected void dropFewItems(boolean flag, int i) {
   }

   public boolean isOnLadder() {
      int i = MathHelper.floor(this.posX);
      int j = MathHelper.floor(this.getEntityBoundingBox().minY);
      int k = MathHelper.floor(this.posZ);
      if (this instanceof EntityPlayer && ((EntityPlayer)this).isSpectator()) {
         return false;
      } else {
         BlockPos blockposition = new BlockPos(i, j, k);
         IBlockState iblockdata = this.world.getBlockState(blockposition);
         Block block = iblockdata.getBlock();
         return block != Blocks.LADDER && block != Blocks.VINE ? block instanceof BlockTrapDoor && this.canGoThroughtTrapDoorOnLadder(blockposition, iblockdata) : true;
      }
   }

   private boolean canGoThroughtTrapDoorOnLadder(BlockPos blockposition, IBlockState iblockdata) {
      if (((Boolean)iblockdata.getValue(BlockTrapDoor.OPEN)).booleanValue()) {
         IBlockState iblockdata1 = this.world.getBlockState(blockposition.down());
         if (iblockdata1.getBlock() == Blocks.LADDER && iblockdata1.getValue(BlockLadder.FACING) == iblockdata.getValue(BlockTrapDoor.FACING)) {
            return true;
         }
      }

      return false;
   }

   public boolean isEntityAlive() {
      return !this.isDead && this.getHealth() > 0.0F;
   }

   public void fall(float f, float f1) {
      super.fall(f, f1);
      PotionEffect mobeffect = this.getActivePotionEffect(MobEffects.JUMP_BOOST);
      float f2 = mobeffect == null ? 0.0F : (float)(mobeffect.getAmplifier() + 1);
      int i = MathHelper.ceil((f - 3.0F - f2) * f1);
      if (i > 0) {
         if (!this.attackEntityFrom(DamageSource.fall, (float)i)) {
            return;
         }

         this.playSound(this.getFallSound(i), 1.0F, 1.0F);
         int j = MathHelper.floor(this.posX);
         int k = MathHelper.floor(this.posY - 0.20000000298023224D);
         int l = MathHelper.floor(this.posZ);
         IBlockState iblockdata = this.world.getBlockState(new BlockPos(j, k, l));
         if (iblockdata.getMaterial() != Material.AIR) {
            SoundType soundeffecttype = iblockdata.getBlock().getSoundType();
            this.playSound(soundeffecttype.getFallSound(), soundeffecttype.getVolume() * 0.5F, soundeffecttype.getPitch() * 0.75F);
         }
      }

   }

   public int getTotalArmorValue() {
      IAttributeInstance attributeinstance = this.getEntityAttribute(SharedMonsterAttributes.ARMOR);
      return MathHelper.floor(attributeinstance.getAttributeValue());
   }

   protected void damageArmor(float f) {
   }

   protected void damageShield(float f) {
   }

   protected float applyArmorCalculations(DamageSource damagesource, float f) {
      if (!damagesource.isUnblockable()) {
         f = CombatRules.getDamageAfterAbsorb(f, (float)this.getTotalArmorValue(), (float)this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
      }

      return f;
   }

   protected float applyPotionDamageCalculations(DamageSource damagesource, float f) {
      if (damagesource.isDamageAbsolute()) {
         return f;
      } else if (f <= 0.0F) {
         return 0.0F;
      } else {
         int i = EnchantmentHelper.getEnchantmentModifierDamage(this.getArmorInventoryList(), damagesource);
         if (i > 0) {
            f = CombatRules.getDamageAfterMagicAbsorb(f, (float)i);
         }

         return f;
      }
   }

   protected boolean damageEntity0(final DamageSource damagesource, float f) {
      if (!this.isEntityInvulnerable(damagesource)) {
         boolean human = this instanceof EntityPlayer;
         float originalDamage = f;
         Function hardHat = new Function() {
            public Double apply(Double f) {
               return (damagesource == DamageSource.anvil || damagesource == DamageSource.fallingBlock) && EntityLivingBase.this.getItemStackFromSlot(EntityEquipmentSlot.HEAD) != null ? -(f.doubleValue() - f.doubleValue() * 0.75D) : -0.0D;
            }
         };
         float hardHatModifier = ((Double)hardHat.apply(Double.valueOf((double)f))).floatValue();
         f = f + hardHatModifier;
         Function blocking = new Function() {
            public Double apply(Double f) {
               return -(EntityLivingBase.this.canBlockDamageSource(damagesource) ? (damagesource.isProjectile() ? f.doubleValue() : f.doubleValue() - f.doubleValue() * 0.33000001311302185D) : 0.0D);
            }
         };
         float blockingModifier = ((Double)blocking.apply(Double.valueOf((double)f))).floatValue();
         f = f + blockingModifier;
         Function armor = new Function() {
            public Double apply(Double f) {
               return -(f.doubleValue() - (double)EntityLivingBase.this.applyArmorCalculations(damagesource, f.floatValue()));
            }
         };
         float armorModifier = ((Double)armor.apply(Double.valueOf((double)f))).floatValue();
         f = f + armorModifier;
         Function resistance = new Function() {
            public Double apply(Double f) {
               if (!damagesource.isDamageAbsolute() && EntityLivingBase.this.isPotionActive(MobEffects.RESISTANCE) && damagesource != DamageSource.outOfWorld) {
                  int i = (EntityLivingBase.this.getActivePotionEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 5;
                  int j = 25 - i;
                  float f1 = f.floatValue() * (float)j;
                  return -(f.doubleValue() - (double)(f1 / 25.0F));
               } else {
                  return -0.0D;
               }
            }
         };
         float resistanceModifier = ((Double)resistance.apply(Double.valueOf((double)f))).floatValue();
         f = f + resistanceModifier;
         Function magic = new Function() {
            public Double apply(Double f) {
               return -(f.doubleValue() - (double)EntityLivingBase.this.applyPotionDamageCalculations(damagesource, f.floatValue()));
            }
         };
         float magicModifier = ((Double)magic.apply(Double.valueOf((double)f))).floatValue();
         f = f + magicModifier;
         Function absorption = new Function() {
            public Double apply(Double f) {
               return -Math.max(f.doubleValue() - Math.max(f.doubleValue() - (double)EntityLivingBase.this.getAbsorptionAmount(), 0.0D), 0.0D);
            }
         };
         float absorptionModifier = ((Double)absorption.apply(Double.valueOf((double)f))).floatValue();
         EntityDamageEvent event = CraftEventFactory.handleLivingEntityDamageEvent(this, damagesource, (double)originalDamage, (double)hardHatModifier, (double)blockingModifier, (double)armorModifier, (double)resistanceModifier, (double)magicModifier, (double)absorptionModifier, hardHat, blocking, armor, resistance, magic, absorption);
         if (event.isCancelled()) {
            return false;
         } else {
            f = (float)event.getFinalDamage();
            if ((damagesource == DamageSource.anvil || damagesource == DamageSource.fallingBlock) && this.getItemStackFromSlot(EntityEquipmentSlot.HEAD) != null) {
               this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).damageItem((int)(event.getDamage() * 4.0D + (double)this.rand.nextFloat() * event.getDamage() * 2.0D), this);
            }

            if (!damagesource.isUnblockable()) {
               float armorDamage = (float)(event.getDamage() + event.getDamage(DamageModifier.BLOCKING) + event.getDamage(DamageModifier.HARD_HAT));
               this.damageArmor(armorDamage);
            }

            if (event.getDamage(DamageModifier.BLOCKING) < 0.0D) {
               this.damageShield((float)event.getDamage());
               if (damagesource.getSourceOfDamage() instanceof EntityLivingBase) {
                  ((EntityLivingBase)damagesource.getSourceOfDamage()).knockBack(this, 0.5F, this.posX - damagesource.getSourceOfDamage().posX, this.posZ - damagesource.getSourceOfDamage().posZ);
               }

               if (f <= 0.0F) {
                  return false;
               }
            }

            absorptionModifier = (float)(-event.getDamage(DamageModifier.ABSORPTION));
            this.setAbsorptionAmount(Math.max(this.getAbsorptionAmount() - absorptionModifier, 0.0F));
            if (f > 0.0F) {
               if (human) {
                  ((EntityPlayer)this).addExhaustion(damagesource.getHungerDamage());
                  if (f < 3.4028235E37F) {
                     ((EntityPlayer)this).addStat(StatList.DAMAGE_TAKEN, Math.round(f * 10.0F));
                  }
               }

               float f2 = this.getHealth();
               this.setHealth(f2 - f);
               this.getCombatTracker().trackDamage(damagesource, f2, f);
               if (!human) {
                  this.setAbsorptionAmount(this.getAbsorptionAmount() - f);
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public CombatTracker getCombatTracker() {
      return this._combatTracker;
   }

   @Nullable
   public EntityLivingBase getAttackingEntity() {
      return (EntityLivingBase)(this._combatTracker.getBestAttacker() != null ? this._combatTracker.getBestAttacker() : (this.attackingPlayer != null ? this.attackingPlayer : (this.entityLivingToAttack != null ? this.entityLivingToAttack : null)));
   }

   public final float getMaxHealth() {
      return (float)this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue();
   }

   public final int getArrowCountInEntity() {
      return ((Integer)this.dataManager.get(ARROW_COUNT_IN_ENTITY)).intValue();
   }

   public final void setArrowCountInEntity(int i) {
      this.dataManager.set(ARROW_COUNT_IN_ENTITY, Integer.valueOf(i));
   }

   private int getArmSwingAnimationEnd() {
      return this.isPotionActive(MobEffects.HASTE) ? 6 - (1 + this.getActivePotionEffect(MobEffects.HASTE).getAmplifier()) : (this.isPotionActive(MobEffects.MINING_FATIGUE) ? 6 + (1 + this.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) * 2 : 6);
   }

   public void swingArm(EnumHand enumhand) {
      if (!this.isSwingInProgress || this.swingProgressInt >= this.getArmSwingAnimationEnd() / 2 || this.swingProgressInt < 0) {
         this.swingProgressInt = -1;
         this.isSwingInProgress = true;
         this.swingingHand = enumhand;
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).getEntityTracker().sendToTracking(this, new SPacketAnimation(this, enumhand == EnumHand.MAIN_HAND ? 0 : 3));
         }
      }

   }

   protected void kill() {
      this.attackEntityFrom(DamageSource.outOfWorld, 4.0F);
   }

   protected void updateArmSwingProgress() {
      int i = this.getArmSwingAnimationEnd();
      if (this.isSwingInProgress) {
         ++this.swingProgressInt;
         if (this.swingProgressInt >= i) {
            this.swingProgressInt = 0;
            this.isSwingInProgress = false;
         }
      } else {
         this.swingProgressInt = 0;
      }

      this.swingProgress = (float)this.swingProgressInt / (float)i;
   }

   public IAttributeInstance getEntityAttribute(IAttribute iattribute) {
      return this.getAttributeMap().getAttributeInstance(iattribute);
   }

   public AbstractAttributeMap getAttributeMap() {
      if (this.attributeMap == null) {
         this.attributeMap = new AttributeMap();
         this.craftAttributes = new CraftAttributeMap(this.attributeMap);
      }

      return this.attributeMap;
   }

   public EnumCreatureAttribute getCreatureAttribute() {
      return EnumCreatureAttribute.UNDEFINED;
   }

   @Nullable
   public ItemStack getHeldItemMainhand() {
      return this.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
   }

   @Nullable
   public ItemStack getHeldItemOffhand() {
      return this.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);
   }

   @Nullable
   public ItemStack getHeldItem(EnumHand enumhand) {
      if (enumhand == EnumHand.MAIN_HAND) {
         return this.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
      } else if (enumhand == EnumHand.OFF_HAND) {
         return this.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);
      } else {
         throw new IllegalArgumentException("Invalid hand " + enumhand);
      }
   }

   public void setHeldItem(EnumHand enumhand, @Nullable ItemStack itemstack) {
      if (enumhand == EnumHand.MAIN_HAND) {
         this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, itemstack);
      } else {
         if (enumhand != EnumHand.OFF_HAND) {
            throw new IllegalArgumentException("Invalid hand " + enumhand);
         }

         this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, itemstack);
      }

   }

   public abstract Iterable getArmorInventoryList();

   @Nullable
   public abstract ItemStack getItemStackFromSlot(EntityEquipmentSlot var1);

   public abstract void setItemStackToSlot(EntityEquipmentSlot var1, @Nullable ItemStack var2);

   public void setSprinting(boolean flag) {
      super.setSprinting(flag);
      IAttributeInstance attributeinstance = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
      if (attributeinstance.getModifier(SPRINTING_SPEED_BOOST_ID) != null) {
         attributeinstance.removeModifier(SPRINTING_SPEED_BOOST);
      }

      if (flag) {
         attributeinstance.applyModifier(SPRINTING_SPEED_BOOST);
      }

   }

   protected float getSoundVolume() {
      return 1.0F;
   }

   protected float getSoundPitch() {
      return this.isChild() ? (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.5F : (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F;
   }

   protected boolean isMovementBlocked() {
      return this.getHealth() <= 0.0F;
   }

   public void dismountEntity(Entity entity) {
      if (!(entity instanceof EntityBoat) && !(entity instanceof EntityHorse)) {
         double d1 = entity.posX;
         double d2 = entity.getEntityBoundingBox().minY + (double)entity.height;
         double d0 = entity.posZ;
         EnumFacing enumdirection = entity.getAdjustedHorizontalFacing();
         EnumFacing enumdirection1 = enumdirection.rotateY();
         int[][] aint = new int[][]{{0, 1}, {0, -1}, {-1, 1}, {-1, -1}, {1, 1}, {1, -1}, {-1, 0}, {1, 0}, {0, 1}};
         double d3 = Math.floor(this.posX) + 0.5D;
         double d4 = Math.floor(this.posZ) + 0.5D;
         double d5 = this.getEntityBoundingBox().maxX - this.getEntityBoundingBox().minX;
         double d6 = this.getEntityBoundingBox().maxZ - this.getEntityBoundingBox().minZ;
         AxisAlignedBB axisalignedbb = new AxisAlignedBB(d3 - d5 / 2.0D, this.getEntityBoundingBox().minY, d4 - d6 / 2.0D, d3 + d5 / 2.0D, this.getEntityBoundingBox().maxY, d4 + d6 / 2.0D);

         for(int[] aint2 : aint) {
            double d7 = (double)(enumdirection.getFrontOffsetX() * aint2[0] + enumdirection1.getFrontOffsetX() * aint2[1]);
            double d8 = (double)(enumdirection.getFrontOffsetZ() * aint2[0] + enumdirection1.getFrontOffsetZ() * aint2[1]);
            double d9 = d3 + d7;
            double d10 = d4 + d8;
            AxisAlignedBB axisalignedbb1 = axisalignedbb.offset(d7, 1.0D, d8);
            if (!this.world.collidesWithAnyBlock(axisalignedbb1)) {
               if (this.world.getBlockState(new BlockPos(d9, this.posY, d10)).isFullyOpaque()) {
                  this.setPositionAndUpdate(d9, this.posY + 1.0D, d10);
                  return;
               }

               BlockPos blockposition = new BlockPos(d9, this.posY - 1.0D, d10);
               if (this.world.getBlockState(blockposition).isFullyOpaque() || this.world.getBlockState(blockposition).getMaterial() == Material.WATER) {
                  d1 = d9;
                  d2 = this.posY + 1.0D;
                  d0 = d10;
               }
            } else if (!this.world.collidesWithAnyBlock(axisalignedbb1.offset(0.0D, 1.0D, 0.0D)) && this.world.getBlockState(new BlockPos(d9, this.posY + 1.0D, d10)).isFullyOpaque()) {
               d1 = d9;
               d2 = this.posY + 2.0D;
               d0 = d10;
            }
         }

         this.setPositionAndUpdate(d1, d2, d0);
      } else {
         double d11 = (double)(this.width / 2.0F + entity.width / 2.0F) + 0.4D;
         float f;
         if (entity instanceof EntityBoat) {
            f = 0.0F;
         } else {
            f = 1.5707964F * (float)(this.getPrimaryHand() == EnumHandSide.RIGHT ? -1 : 1);
         }

         float f1 = -MathHelper.sin(-this.rotationYaw * 0.017453292F - 3.1415927F + f);
         float f2 = -MathHelper.cos(-this.rotationYaw * 0.017453292F - 3.1415927F + f);
         double d0 = Math.abs(f1) > Math.abs(f2) ? d11 / (double)Math.abs(f1) : d11 / (double)Math.abs(f2);
         double d12 = this.posX + (double)f1 * d0;
         double d13 = this.posZ + (double)f2 * d0;
         this.setPosition(d12, entity.posY + (double)entity.height + 0.001D, d13);
         if (this.world.collidesWithAnyBlock(this.getEntityBoundingBox())) {
            this.setPosition(d12, entity.posY + (double)entity.height + 1.001D, d13);
            if (this.world.collidesWithAnyBlock(this.getEntityBoundingBox())) {
               this.setPosition(entity.posX, entity.posY + (double)this.height + 0.001D, entity.posZ);
            }
         }
      }

   }

   protected float getJumpUpwardsMotion() {
      return 0.42F;
   }

   protected void jump() {
      this.motionY = (double)this.getJumpUpwardsMotion();
      if (this.isPotionActive(MobEffects.JUMP_BOOST)) {
         this.motionY += (double)((float)(this.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1F);
      }

      if (this.isSprinting()) {
         float f = this.rotationYaw * 0.017453292F;
         this.motionX -= (double)(MathHelper.sin(f) * 0.2F);
         this.motionZ += (double)(MathHelper.cos(f) * 0.2F);
      }

      this.isAirBorne = true;
   }

   protected void handleJumpWater() {
      this.motionY += 0.03999999910593033D;
   }

   protected void handleJumpLava() {
      this.motionY += 0.03999999910593033D;
   }

   protected float getWaterSlowDown() {
      return 0.8F;
   }

   public void moveEntityWithHeading(float f, float f1) {
      if (this.isServerWorld() || this.canPassengerSteer()) {
         if (!this.isInWater() || this instanceof EntityPlayer && ((EntityPlayer)this).capabilities.isFlying) {
            if (!this.isInLava() || this instanceof EntityPlayer && ((EntityPlayer)this).capabilities.isFlying) {
               if (this.isElytraFlying()) {
                  if (this.motionY > -0.5D) {
                     this.fallDistance = 1.0F;
                  }

                  Vec3d vec3d = this.getLookVec();
                  float f5 = this.rotationPitch * 0.017453292F;
                  double d0 = Math.sqrt(vec3d.xCoord * vec3d.xCoord + vec3d.zCoord * vec3d.zCoord);
                  double d2 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
                  double d3 = vec3d.lengthVector();
                  float f6 = MathHelper.cos(f5);
                  f6 = (float)((double)f6 * (double)f6 * Math.min(1.0D, d3 / 0.4D));
                  this.motionY += -0.08D + (double)f6 * 0.06D;
                  if (this.motionY < 0.0D && d0 > 0.0D) {
                     double d4 = this.motionY * -0.1D * (double)f6;
                     this.motionY += d4;
                     this.motionX += vec3d.xCoord * d4 / d0;
                     this.motionZ += vec3d.zCoord * d4 / d0;
                  }

                  if (f5 < 0.0F) {
                     double d4 = d2 * (double)(-MathHelper.sin(f5)) * 0.04D;
                     this.motionY += d4 * 3.2D;
                     this.motionX -= vec3d.xCoord * d4 / d0;
                     this.motionZ -= vec3d.zCoord * d4 / d0;
                  }

                  if (d0 > 0.0D) {
                     this.motionX += (vec3d.xCoord / d0 * d2 - this.motionX) * 0.1D;
                     this.motionZ += (vec3d.zCoord / d0 * d2 - this.motionZ) * 0.1D;
                  }

                  this.motionX *= 0.9900000095367432D;
                  this.motionY *= 0.9800000190734863D;
                  this.motionZ *= 0.9900000095367432D;
                  this.move(this.motionX, this.motionY, this.motionZ);
                  if (this.isCollidedHorizontally && !this.world.isRemote) {
                     double d4 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
                     double d5 = d2 - d4;
                     float f7 = (float)(d5 * 10.0D - 3.0D);
                     if (f7 > 0.0F) {
                        this.playSound(this.getFallSound((int)f7), 1.0F, 1.0F);
                        this.attackEntityFrom(DamageSource.flyIntoWall, f7);
                     }
                  }

                  if (this.onGround && !this.world.isRemote && this.getFlag(7) && !CraftEventFactory.callToggleGlideEvent(this, false).isCancelled()) {
                     this.setFlag(7, false);
                  }
               } else {
                  float f8 = 0.91F;
                  BlockPos.PooledMutableBlockPos blockposition_pooledblockposition = BlockPos.PooledMutableBlockPos.retain(this.posX, this.getEntityBoundingBox().minY - 1.0D, this.posZ);
                  if (this.onGround) {
                     f8 = this.world.getBlockState(blockposition_pooledblockposition).getBlock().slipperiness * 0.91F;
                  }

                  float f4 = 0.16277136F / (f8 * f8 * f8);
                  float f3;
                  if (this.onGround) {
                     f3 = this.getAIMoveSpeed() * f4;
                  } else {
                     f3 = this.jumpMovementFactor;
                  }

                  this.moveRelative(f, f1, f3);
                  f8 = 0.91F;
                  if (this.onGround) {
                     f8 = this.world.getBlockState(blockposition_pooledblockposition.setPos(this.posX, this.getEntityBoundingBox().minY - 1.0D, this.posZ)).getBlock().slipperiness * 0.91F;
                  }

                  if (this.isOnLadder()) {
                     float f2 = 0.15F;
                     this.motionX = MathHelper.clamp(this.motionX, -0.15000000596046448D, 0.15000000596046448D);
                     this.motionZ = MathHelper.clamp(this.motionZ, -0.15000000596046448D, 0.15000000596046448D);
                     this.fallDistance = 0.0F;
                     if (this.motionY < -0.15D) {
                        this.motionY = -0.15D;
                     }

                     boolean flag = this.isSneaking() && this instanceof EntityPlayer;
                     if (flag && this.motionY < 0.0D) {
                        this.motionY = 0.0D;
                     }
                  }

                  this.move(this.motionX, this.motionY, this.motionZ);
                  if (this.isCollidedHorizontally && this.isOnLadder()) {
                     this.motionY = 0.2D;
                  }

                  if (this.isPotionActive(MobEffects.LEVITATION)) {
                     this.motionY += (0.05D * (double)(this.getActivePotionEffect(MobEffects.LEVITATION).getAmplifier() + 1) - this.motionY) * 0.2D;
                  } else {
                     blockposition_pooledblockposition.setPos(this.posX, 0.0D, this.posZ);
                     if (this.world.isRemote && (!this.world.isBlockLoaded(blockposition_pooledblockposition) || !this.world.getChunkFromBlockCoords(blockposition_pooledblockposition).isLoaded())) {
                        if (this.posY > 0.0D) {
                           this.motionY = -0.1D;
                        } else {
                           this.motionY = 0.0D;
                        }
                     } else if (!this.hasNoGravity()) {
                        this.motionY -= 0.08D;
                     }
                  }

                  this.motionY *= 0.9800000190734863D;
                  this.motionX *= (double)f8;
                  this.motionZ *= (double)f8;
                  blockposition_pooledblockposition.release();
               }
            } else {
               double d1 = this.posY;
               this.moveRelative(f, f1, 0.02F);
               this.move(this.motionX, this.motionY, this.motionZ);
               this.motionX *= 0.5D;
               this.motionY *= 0.5D;
               this.motionZ *= 0.5D;
               if (!this.hasNoGravity()) {
                  this.motionY -= 0.02D;
               }

               if (this.isCollidedHorizontally && this.isOffsetPositionInLiquid(this.motionX, this.motionY + 0.6000000238418579D - this.posY + d1, this.motionZ)) {
                  this.motionY = 0.30000001192092896D;
               }
            }
         } else {
            double d1 = this.posY;
            float f4 = this.getWaterSlowDown();
            float f3 = 0.02F;
            float f2 = (float)EnchantmentHelper.getDepthStriderModifier(this);
            if (f2 > 3.0F) {
               f2 = 3.0F;
            }

            if (!this.onGround) {
               f2 *= 0.5F;
            }

            if (f2 > 0.0F) {
               f4 += (0.54600006F - f4) * f2 / 3.0F;
               f3 += (this.getAIMoveSpeed() - f3) * f2 / 3.0F;
            }

            this.moveRelative(f, f1, f3);
            this.move(this.motionX, this.motionY, this.motionZ);
            this.motionX *= (double)f4;
            this.motionY *= 0.800000011920929D;
            this.motionZ *= (double)f4;
            if (!this.hasNoGravity()) {
               this.motionY -= 0.02D;
            }

            if (this.isCollidedHorizontally && this.isOffsetPositionInLiquid(this.motionX, this.motionY + 0.6000000238418579D - this.posY + d1, this.motionZ)) {
               this.motionY = 0.30000001192092896D;
            }
         }
      }

      this.prevLimbSwingAmount = this.limbSwingAmount;
      double d1 = this.posX - this.prevPosX;
      double d0 = this.posZ - this.prevPosZ;
      float f2 = MathHelper.sqrt(d1 * d1 + d0 * d0) * 4.0F;
      if (f2 > 1.0F) {
         f2 = 1.0F;
      }

      this.limbSwingAmount += (f2 - this.limbSwingAmount) * 0.4F;
      this.limbSwing += this.limbSwingAmount;
   }

   public float getAIMoveSpeed() {
      return this.landMovementFactor;
   }

   public void setAIMoveSpeed(float f) {
      this.landMovementFactor = f;
   }

   public boolean attackEntityAsMob(Entity entity) {
      this.setLastAttacker(entity);
      return false;
   }

   public boolean isPlayerSleeping() {
      return false;
   }

   public void onUpdate() {
      super.onUpdate();
      this.updateActiveHand();
      if (!this.world.isRemote) {
         int i = this.getArrowCountInEntity();
         if (i > 0) {
            if (this.arrowHitTimer <= 0) {
               this.arrowHitTimer = 20 * (30 - i);
            }

            --this.arrowHitTimer;
            if (this.arrowHitTimer <= 0) {
               this.setArrowCountInEntity(i - 1);
            }
         }

         for(EntityEquipmentSlot enumitemslot : EntityEquipmentSlot.values()) {
            ItemStack itemstack;
            switch(EntityLivingBase.SyntheticClass_1.a[enumitemslot.getSlotType().ordinal()]) {
            case 1:
               itemstack = this.handInventory[enumitemslot.getIndex()];
               break;
            case 2:
               itemstack = this.armorArray[enumitemslot.getIndex()];
               break;
            default:
               continue;
            }

            ItemStack itemstack1 = this.getItemStackFromSlot(enumitemslot);
            if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
               ((WorldServer)this.world).getEntityTracker().sendToTracking(this, new SPacketEntityEquipment(this.getEntityId(), enumitemslot, itemstack1));
               if (itemstack != null) {
                  this.getAttributeMap().removeAttributeModifiers(itemstack.getAttributeModifiers(enumitemslot));
               }

               if (itemstack1 != null) {
                  this.getAttributeMap().applyAttributeModifiers(itemstack1.getAttributeModifiers(enumitemslot));
               }

               switch(EntityLivingBase.SyntheticClass_1.a[enumitemslot.getSlotType().ordinal()]) {
               case 1:
                  this.handInventory[enumitemslot.getIndex()] = itemstack1 == null ? null : itemstack1.copy();
                  break;
               case 2:
                  this.armorArray[enumitemslot.getIndex()] = itemstack1 == null ? null : itemstack1.copy();
               }
            }
         }

         if (this.ticksExisted % 20 == 0) {
            this.getCombatTracker().reset();
         }

         if (!this.glowing) {
            boolean flag = this.isPotionActive(MobEffects.GLOWING);
            if (this.getFlag(6) != flag) {
               this.setFlag(6, flag);
            }
         }
      }

      this.onLivingUpdate();
      double d0 = this.posX - this.prevPosX;
      double d1 = this.posZ - this.prevPosZ;
      float f = (float)(d0 * d0 + d1 * d1);
      float f1 = this.renderYawOffset;
      float f2 = 0.0F;
      this.prevOnGroundSpeedFactor = this.onGroundSpeedFactor;
      float f3 = 0.0F;
      if (f > 0.0025000002F) {
         f3 = 1.0F;
         f2 = (float)Math.sqrt((double)f) * 3.0F;
         f1 = (float)MathHelper.atan2(d1, d0) * 57.295776F - 90.0F;
      }

      if (this.swingProgress > 0.0F) {
         f1 = this.rotationYaw;
      }

      if (!this.onGround) {
         f3 = 0.0F;
      }

      this.onGroundSpeedFactor += (f3 - this.onGroundSpeedFactor) * 0.3F;
      this.world.theProfiler.startSection("headTurn");
      f2 = this.updateDistance(f1, f2);
      this.world.theProfiler.endSection();
      this.world.theProfiler.startSection("rangeChecks");

      while(this.rotationYaw - this.prevRotationYaw < -180.0F) {
         this.prevRotationYaw -= 360.0F;
      }

      while(this.rotationYaw - this.prevRotationYaw >= 180.0F) {
         this.prevRotationYaw += 360.0F;
      }

      while(this.renderYawOffset - this.prevRenderYawOffset < -180.0F) {
         this.prevRenderYawOffset -= 360.0F;
      }

      while(this.renderYawOffset - this.prevRenderYawOffset >= 180.0F) {
         this.prevRenderYawOffset += 360.0F;
      }

      while(this.rotationPitch - this.prevRotationPitch < -180.0F) {
         this.prevRotationPitch -= 360.0F;
      }

      while(this.rotationPitch - this.prevRotationPitch >= 180.0F) {
         this.prevRotationPitch += 360.0F;
      }

      while(this.rotationYawHead - this.prevRotationYawHead < -180.0F) {
         this.prevRotationYawHead -= 360.0F;
      }

      while(this.rotationYawHead - this.prevRotationYawHead >= 180.0F) {
         this.prevRotationYawHead += 360.0F;
      }

      this.world.theProfiler.endSection();
      this.movedDistance += f2;
      if (this.isElytraFlying()) {
         ++this.ticksElytraFlying;
      } else {
         this.ticksElytraFlying = 0;
      }

   }

   protected float updateDistance(float f, float f1) {
      float f2 = MathHelper.wrapDegrees(f - this.renderYawOffset);
      this.renderYawOffset += f2 * 0.3F;
      float f3 = MathHelper.wrapDegrees(this.rotationYaw - this.renderYawOffset);
      boolean flag = f3 < -90.0F || f3 >= 90.0F;
      if (f3 < -75.0F) {
         f3 = -75.0F;
      }

      if (f3 >= 75.0F) {
         f3 = 75.0F;
      }

      this.renderYawOffset = this.rotationYaw - f3;
      if (f3 * f3 > 2500.0F) {
         this.renderYawOffset += f3 * 0.2F;
      }

      if (flag) {
         f1 *= -1.0F;
      }

      return f1;
   }

   public void onLivingUpdate() {
      if (this.jumpTicks > 0) {
         --this.jumpTicks;
      }

      if (this.newPosRotationIncrements > 0 && !this.canPassengerSteer()) {
         double d0 = this.posX + (this.interpTargetX - this.posX) / (double)this.newPosRotationIncrements;
         double d1 = this.posY + (this.interpTargetY - this.posY) / (double)this.newPosRotationIncrements;
         double d2 = this.posZ + (this.interpTargetZ - this.posZ) / (double)this.newPosRotationIncrements;
         double d3 = MathHelper.wrapDegrees(this.interpTargetYaw - (double)this.rotationYaw);
         this.rotationYaw = (float)((double)this.rotationYaw + d3 / (double)this.newPosRotationIncrements);
         this.rotationPitch = (float)((double)this.rotationPitch + (this.interpTargetPitch - (double)this.rotationPitch) / (double)this.newPosRotationIncrements);
         --this.newPosRotationIncrements;
         this.setPosition(d0, d1, d2);
         this.setRotation(this.rotationYaw, this.rotationPitch);
      } else if (!this.isServerWorld()) {
         this.motionX *= 0.98D;
         this.motionY *= 0.98D;
         this.motionZ *= 0.98D;
      }

      if (Math.abs(this.motionX) < 0.003D) {
         this.motionX = 0.0D;
      }

      if (Math.abs(this.motionY) < 0.003D) {
         this.motionY = 0.0D;
      }

      if (Math.abs(this.motionZ) < 0.003D) {
         this.motionZ = 0.0D;
      }

      this.world.theProfiler.startSection("ai");
      if (this.isMovementBlocked()) {
         this.isJumping = false;
         this.moveStrafing = 0.0F;
         this.moveForward = 0.0F;
         this.randomYawVelocity = 0.0F;
      } else if (this.isServerWorld()) {
         this.world.theProfiler.startSection("newAi");
         this.updateEntityActionState();
         this.world.theProfiler.endSection();
      }

      this.world.theProfiler.endSection();
      this.world.theProfiler.startSection("jump");
      if (this.isJumping) {
         if (this.isInWater()) {
            this.handleJumpWater();
         } else if (this.isInLava()) {
            this.handleJumpLava();
         } else if (this.onGround && this.jumpTicks == 0) {
            this.jump();
            this.jumpTicks = 10;
         }
      } else {
         this.jumpTicks = 0;
      }

      this.world.theProfiler.endSection();
      this.world.theProfiler.startSection("travel");
      this.moveStrafing *= 0.98F;
      this.moveForward *= 0.98F;
      this.randomYawVelocity *= 0.9F;
      this.updateElytra();
      this.moveEntityWithHeading(this.moveStrafing, this.moveForward);
      this.world.theProfiler.endSection();
      this.world.theProfiler.startSection("push");
      this.collideWithNearbyEntities();
      this.world.theProfiler.endSection();
   }

   private void updateElytra() {
      boolean flag = this.getFlag(7);
      if (flag && !this.onGround && !this.isRiding()) {
         ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
         if (itemstack != null && itemstack.getItem() == Items.ELYTRA && ItemElytra.isBroken(itemstack)) {
            flag = true;
            if (!this.world.isRemote && (this.ticksElytraFlying + 1) % 20 == 0) {
               itemstack.damageItem(1, this);
            }
         } else {
            flag = false;
         }
      } else {
         flag = false;
      }

      if (!this.world.isRemote && flag != this.getFlag(7) && !CraftEventFactory.callToggleGlideEvent(this, flag).isCancelled()) {
         this.setFlag(7, flag);
      }

   }

   protected void updateEntityActionState() {
   }

   protected void collideWithNearbyEntities() {
      List list = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox(), EntitySelectors.getTeamCollisionPredicate(this));
      if (!list.isEmpty()) {
         for(int i = 0; i < list.size(); ++i) {
            Entity entity = (Entity)list.get(i);
            this.collideWithEntity(entity);
         }
      }

   }

   protected void collideWithEntity(Entity entity) {
      entity.applyEntityCollision(this);
   }

   public void dismountRidingEntity() {
      Entity entity = this.getRidingEntity();
      super.dismountRidingEntity();
      if (entity != null && entity != this.getRidingEntity() && !this.world.isRemote) {
         this.dismountEntity(entity);
      }

   }

   public void updateRidden() {
      super.updateRidden();
      this.prevOnGroundSpeedFactor = this.onGroundSpeedFactor;
      this.onGroundSpeedFactor = 0.0F;
      this.fallDistance = 0.0F;
   }

   public void setJumping(boolean flag) {
      this.isJumping = flag;
   }

   public void onItemPickup(Entity entity, int i) {
      if (!entity.isDead && !this.world.isRemote) {
         EntityTracker entitytracker = ((WorldServer)this.world).getEntityTracker();
         if (entity instanceof EntityItem) {
            entitytracker.sendToTracking(entity, new SPacketCollectItem(entity.getEntityId(), this.getEntityId()));
         }

         if (entity instanceof EntityArrow) {
            entitytracker.sendToTracking(entity, new SPacketCollectItem(entity.getEntityId(), this.getEntityId()));
         }

         if (entity instanceof EntityXPOrb) {
            entitytracker.sendToTracking(entity, new SPacketCollectItem(entity.getEntityId(), this.getEntityId()));
         }
      }

   }

   public boolean canEntityBeSeen(Entity entity) {
      return this.world.rayTraceBlocks(new Vec3d(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ), new Vec3d(entity.posX, entity.posY + (double)entity.getEyeHeight(), entity.posZ), false, true, false) == null;
   }

   public Vec3d getLookVec() {
      return this.getLook(1.0F);
   }

   public Vec3d getLook(float f) {
      if (f == 1.0F) {
         return this.getVectorForRotation(this.rotationPitch, this.rotationYawHead);
      } else {
         float f1 = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * f;
         float f2 = this.prevRotationYawHead + (this.rotationYawHead - this.prevRotationYawHead) * f;
         return this.getVectorForRotation(f1, f2);
      }
   }

   public boolean isServerWorld() {
      return !this.world.isRemote;
   }

   public boolean canBeCollidedWith() {
      return !this.isDead && this.collides;
   }

   public boolean canBePushed() {
      return !this.isDead && this.collides;
   }

   protected void setBeenAttacked() {
      this.velocityChanged = this.rand.nextDouble() >= this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue();
   }

   public float getRotationYawHead() {
      return this.rotationYawHead;
   }

   public void setRotationYawHead(float f) {
      this.rotationYawHead = f;
   }

   public void setRenderYawOffset(float f) {
      this.renderYawOffset = f;
   }

   public float getAbsorptionAmount() {
      return this.absorptionAmount;
   }

   public void setAbsorptionAmount(float f) {
      if (f < 0.0F) {
         f = 0.0F;
      }

      this.absorptionAmount = f;
   }

   public void sendEnterCombat() {
   }

   public void sendEndCombat() {
   }

   protected void markPotionsDirty() {
      this.potionsNeedUpdate = true;
   }

   public abstract EnumHandSide getPrimaryHand();

   public boolean isHandActive() {
      return (((Byte)this.dataManager.get(HAND_STATES)).byteValue() & 1) > 0;
   }

   public EnumHand getActiveHand() {
      return (((Byte)this.dataManager.get(HAND_STATES)).byteValue() & 2) > 0 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
   }

   protected void updateActiveHand() {
      if (this.isHandActive()) {
         ItemStack itemstack = this.getHeldItem(this.getActiveHand());
         if (itemstack == this.activeItemStack) {
            if (this.getItemInUseCount() <= 25 && this.getItemInUseCount() % 4 == 0) {
               this.updateItemUse(this.activeItemStack, 5);
            }

            if (--this.activeItemStackUseCount == 0 && !this.world.isRemote) {
               this.onItemUseFinish();
            }
         } else {
            this.resetActiveHand();
         }
      }

   }

   public void setActiveHand(EnumHand enumhand) {
      ItemStack itemstack = this.getHeldItem(enumhand);
      if (itemstack != null && !this.isHandActive()) {
         this.activeItemStack = itemstack;
         this.activeItemStackUseCount = itemstack.getMaxItemUseDuration();
         if (!this.world.isRemote) {
            int i = 1;
            if (enumhand == EnumHand.OFF_HAND) {
               i |= 2;
            }

            this.dataManager.set(HAND_STATES, Byte.valueOf((byte)i));
         }
      }

   }

   public void notifyDataManagerChange(DataParameter datawatcherobject) {
      super.notifyDataManagerChange(datawatcherobject);
      if (HAND_STATES.equals(datawatcherobject) && this.world.isRemote) {
         if (this.isHandActive() && this.activeItemStack == null) {
            this.activeItemStack = this.getHeldItem(this.getActiveHand());
            if (this.activeItemStack != null) {
               this.activeItemStackUseCount = this.activeItemStack.getMaxItemUseDuration();
            }
         } else if (!this.isHandActive() && this.activeItemStack != null) {
            this.activeItemStack = null;
            this.activeItemStackUseCount = 0;
         }
      }

   }

   protected void updateItemUse(@Nullable ItemStack itemstack, int i) {
      if (itemstack != null && this.isHandActive()) {
         if (itemstack.getItemUseAction() == EnumAction.DRINK) {
            this.playSound(SoundEvents.ENTITY_GENERIC_DRINK, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
         }

         if (itemstack.getItemUseAction() == EnumAction.EAT) {
            for(int j = 0; j < i; ++j) {
               Vec3d vec3d = new Vec3d(((double)this.rand.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
               vec3d = vec3d.rotatePitch(-this.rotationPitch * 0.017453292F);
               vec3d = vec3d.rotateYaw(-this.rotationYaw * 0.017453292F);
               double d0 = (double)(-this.rand.nextFloat()) * 0.6D - 0.3D;
               Vec3d vec3d1 = new Vec3d(((double)this.rand.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
               vec3d1 = vec3d1.rotatePitch(-this.rotationPitch * 0.017453292F);
               vec3d1 = vec3d1.rotateYaw(-this.rotationYaw * 0.017453292F);
               vec3d1 = vec3d1.addVector(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ);
               if (itemstack.getHasSubtypes()) {
                  this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, vec3d1.xCoord, vec3d1.yCoord, vec3d1.zCoord, vec3d.xCoord, vec3d.yCoord + 0.05D, vec3d.zCoord, Item.getIdFromItem(itemstack.getItem()), itemstack.getMetadata());
               } else {
                  this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, vec3d1.xCoord, vec3d1.yCoord, vec3d1.zCoord, vec3d.xCoord, vec3d.yCoord + 0.05D, vec3d.zCoord, Item.getIdFromItem(itemstack.getItem()));
               }
            }

            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.5F + 0.5F * (float)this.rand.nextInt(2), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
         }
      }

   }

   protected void onItemUseFinish() {
      if (this.activeItemStack != null && this.isHandActive()) {
         this.updateItemUse(this.activeItemStack, 16);
         org.bukkit.inventory.ItemStack craftItem = CraftItemStack.asBukkitCopy(this.activeItemStack);
         PlayerItemConsumeEvent event = new PlayerItemConsumeEvent((Player)this.getBukkitEntity(), craftItem);
         this.world.getServer().getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            if (this instanceof EntityPlayerMP) {
               ((EntityPlayerMP)this).getBukkitEntity().updateInventory();
               ((EntityPlayerMP)this).getBukkitEntity().updateScaledHealth();
            }

            return;
         }

         ItemStack itemstack = craftItem.equals(event.getItem()) ? this.activeItemStack.onItemUseFinish(this.world, this) : CraftItemStack.asNMSCopy(event.getItem()).onItemUseFinish(this.world, this);
         if (itemstack != null && itemstack.stackSize == 0) {
            itemstack = null;
         }

         this.setHeldItem(this.getActiveHand(), itemstack);
         this.resetActiveHand();
      }

   }

   @Nullable
   public ItemStack getActiveItemStack() {
      return this.activeItemStack;
   }

   public int getItemInUseCount() {
      return this.activeItemStackUseCount;
   }

   public int getItemInUseMaxCount() {
      return this.isHandActive() ? this.activeItemStack.getMaxItemUseDuration() - this.getItemInUseCount() : 0;
   }

   public void stopActiveHand() {
      if (this.activeItemStack != null) {
         this.activeItemStack.onPlayerStoppedUsing(this.world, this, this.getItemInUseCount());
      }

      this.resetActiveHand();
   }

   public void resetActiveHand() {
      if (!this.world.isRemote) {
         this.dataManager.set(HAND_STATES, Byte.valueOf((byte)0));
      }

      this.activeItemStack = null;
      this.activeItemStackUseCount = 0;
   }

   public boolean isActiveItemStackBlocking() {
      if (this.isHandActive() && this.activeItemStack != null) {
         Item item = this.activeItemStack.getItem();
         return item.getItemUseAction(this.activeItemStack) != EnumAction.BLOCK ? false : item.getMaxItemUseDuration(this.activeItemStack) - this.activeItemStackUseCount >= 5;
      } else {
         return false;
      }
   }

   public boolean isElytraFlying() {
      return this.getFlag(7);
   }

   public boolean attemptTeleport(double d0, double d1, double d2) {
      double d3 = this.posX;
      double d4 = this.posY;
      double d5 = this.posZ;
      this.posX = d0;
      this.posY = d1;
      this.posZ = d2;
      boolean flag = false;
      BlockPos blockposition = new BlockPos(this);
      World world = this.world;
      Random random = this.getRNG();
      if (world.isBlockLoaded(blockposition)) {
         boolean flag1 = false;

         while(!flag1 && blockposition.getY() > 0) {
            BlockPos blockposition1 = blockposition.down();
            IBlockState iblockdata = world.getBlockState(blockposition1);
            if (iblockdata.getMaterial().blocksMovement()) {
               flag1 = true;
            } else {
               --this.posY;
               blockposition = blockposition1;
            }
         }

         if (flag1) {
            EntityTeleportEvent teleport = new EntityTeleportEvent(this.getBukkitEntity(), new Location(this.world.getWorld(), d3, d4, d5), new Location(this.world.getWorld(), this.posX, this.posY, this.posZ));
            this.world.getServer().getPluginManager().callEvent(teleport);
            if (!teleport.isCancelled()) {
               Location to = teleport.getTo();
               this.setPositionAndUpdate(to.getX(), to.getY(), to.getZ());
               if (world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty() && !world.containsAnyLiquid(this.getEntityBoundingBox())) {
                  flag = true;
               }
            }
         }
      }

      if (!flag) {
         this.setPositionAndUpdate(d3, d4, d5);
         return false;
      } else {
         boolean flag1 = true;

         for(int i = 0; i < 128; ++i) {
            double d6 = (double)i / 127.0D;
            float f = (random.nextFloat() - 0.5F) * 0.2F;
            float f1 = (random.nextFloat() - 0.5F) * 0.2F;
            float f2 = (random.nextFloat() - 0.5F) * 0.2F;
            double d7 = d3 + (this.posX - d3) * d6 + (random.nextDouble() - 0.5D) * (double)this.width * 2.0D;
            double d8 = d4 + (this.posY - d4) * d6 + random.nextDouble() * (double)this.height;
            double d9 = d5 + (this.posZ - d5) * d6 + (random.nextDouble() - 0.5D) * (double)this.width * 2.0D;
            world.spawnParticle(EnumParticleTypes.PORTAL, d7, d8, d9, (double)f, (double)f1, (double)f2);
         }

         if (this instanceof EntityCreature) {
            ((EntityCreature)this).getNavigator().clearPathEntity();
         }

         return true;
      }
   }

   public boolean canBeHitWithPotion() {
      return true;
   }

   static class SyntheticClass_1 {
      static final int[] a = new int[EntityEquipmentSlot.Type.values().length];

      static {
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
