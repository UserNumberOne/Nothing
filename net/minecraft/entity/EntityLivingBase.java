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

   public EntityLivingBase(World var1) {
      super(var1);
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

   protected void updateFallState(double var1, boolean var3, IBlockState var4, BlockPos var5) {
      if (!this.isInWater()) {
         this.handleWaterMovement();
      }

      if (!this.world.isRemote && this.fallDistance > 3.0F && var3) {
         float var6 = (float)MathHelper.ceil(this.fallDistance - 3.0F);
         if (var4.getMaterial() != Material.AIR) {
            double var7 = Math.min((double)(0.2F + var6 / 15.0F), 2.5D);
            int var9 = (int)(150.0D * var7);
            if (this instanceof EntityPlayerMP) {
               ((WorldServer)this.world).sendParticles((EntityPlayerMP)this, EnumParticleTypes.BLOCK_DUST, false, this.posX, this.posY, this.posZ, var9, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, Block.getStateId(var4));
            } else {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY, this.posZ, var9, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, Block.getStateId(var4));
            }
         }
      }

      super.updateFallState(var1, var3, var4, var5);
   }

   public boolean canBreatheUnderwater() {
      return false;
   }

   public void onEntityUpdate() {
      this.prevSwingProgress = this.swingProgress;
      super.onEntityUpdate();
      this.world.theProfiler.startSection("livingEntityBaseTick");
      boolean var1 = this instanceof EntityPlayer;
      if (this.isEntityAlive()) {
         if (this.isEntityInsideOpaqueBlock()) {
            this.attackEntityFrom(DamageSource.inWall, 1.0F);
         } else if (var1 && !this.world.getWorldBorder().contains(this.getEntityBoundingBox())) {
            double var2 = this.world.getWorldBorder().getClosestDistance(this) + this.world.getWorldBorder().getDamageBuffer();
            if (var2 < 0.0D) {
               double var4 = this.world.getWorldBorder().getDamageAmount();
               if (var4 > 0.0D) {
                  this.attackEntityFrom(DamageSource.inWall, (float)Math.max(1, MathHelper.floor(-var2 * var4)));
               }
            }
         }
      }

      if (this.isImmuneToFire() || this.world.isRemote) {
         this.extinguish();
      }

      boolean var6 = var1 && ((EntityPlayer)this).capabilities.disableDamage;
      if (this.isEntityAlive()) {
         if (!this.isInsideOfMaterial(Material.WATER)) {
            if (this.getAir() != 300) {
               this.setAir(this.maxAirTicks);
            }
         } else {
            if (!this.canBreatheUnderwater() && !this.isPotionActive(MobEffects.WATER_BREATHING) && !var6) {
               this.setAir(this.decreaseAirSupply(this.getAir()));
               if (this.getAir() == -20) {
                  this.setAir(0);

                  for(int var7 = 0; var7 < 8; ++var7) {
                     float var8 = this.rand.nextFloat() - this.rand.nextFloat();
                     float var9 = this.rand.nextFloat() - this.rand.nextFloat();
                     float var10 = this.rand.nextFloat() - this.rand.nextFloat();
                     this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX + (double)var8, this.posY + (double)var9, this.posZ + (double)var10, this.motionX, this.motionY, this.motionZ);
                  }

                  this.attackEntityFrom(DamageSource.drown, 2.0F);
               }
            }

            if (!this.world.isRemote && this.isRiding() && this.getRidingEntity() instanceof EntityLivingBase) {
               this.dismountRidingEntity();
            }
         }

         if (!this.world.isRemote) {
            BlockPos var11 = new BlockPos(this);
            if (!Objects.equal(this.prevBlockpos, var11)) {
               this.prevBlockpos = var11;
               this.frostWalk(var11);
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
      int var1 = this.getExperiencePoints(this.attackingPlayer);
      return !this.world.isRemote && (this.recentlyHit > 0 || this.isPlayer()) && this.canDropLoot() && this.world.getGameRules().getBoolean("doMobLoot") ? var1 : 0;
   }

   protected void frostWalk(BlockPos var1) {
      int var2 = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FROST_WALKER, this);
      if (var2 > 0) {
         EnchantmentFrostWalker.freezeNearby(this, this.world, var1, var2);
      }

   }

   public boolean isChild() {
      return false;
   }

   protected void onDeathUpdate() {
      ++this.deathTime;
      if (this.deathTime >= 20 && !this.isDead) {
         int var1 = this.expToDrop;

         while(var1 > 0) {
            int var2 = EntityXPOrb.getXPSplit(var1);
            var1 -= var2;
            this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY, this.posZ, var2));
         }

         this.expToDrop = 0;
         this.setDead();

         for(int var9 = 0; var9 < 20; ++var9) {
            double var3 = this.rand.nextGaussian() * 0.02D;
            double var5 = this.rand.nextGaussian() * 0.02D;
            double var7 = this.rand.nextGaussian() * 0.02D;
            this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, var3, var5, var7);
         }
      }

   }

   protected boolean canDropLoot() {
      return !this.isChild();
   }

   protected int decreaseAirSupply(int var1) {
      int var2 = EnchantmentHelper.getRespirationModifier(this);
      return var2 > 0 && this.rand.nextInt(var2 + 1) > 0 ? var1 : var1 - 1;
   }

   protected int getExperiencePoints(EntityPlayer var1) {
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

   public void setRevengeTarget(@Nullable EntityLivingBase var1) {
      this.entityLivingToAttack = var1;
      this.revengeTimer = this.ticksExisted;
   }

   public EntityLivingBase getLastAttacker() {
      return this.lastAttacker;
   }

   public int getLastAttackerTime() {
      return this.lastAttackerTime;
   }

   public void setLastAttacker(Entity var1) {
      if (var1 instanceof EntityLivingBase) {
         this.lastAttacker = (EntityLivingBase)var1;
      } else {
         this.lastAttacker = null;
      }

      this.lastAttackerTime = this.ticksExisted;
   }

   public int getAge() {
      return this.entityAge;
   }

   protected void playEquipSound(@Nullable ItemStack var1) {
      if (var1 != null) {
         SoundEvent var2 = SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
         Item var3 = var1.getItem();
         if (var3 instanceof ItemArmor) {
            var2 = ((ItemArmor)var3).getArmorMaterial().getSoundEvent();
         } else if (var3 == Items.ELYTRA) {
            var2 = SoundEvents.ITEM_ARMOR_EQUIP_LEATHER;
         }

         this.playSound(var2, 1.0F, 1.0F);
      }

   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      var1.setFloat("Health", this.getHealth());
      var1.setShort("HurtTime", (short)this.hurtTime);
      var1.setInteger("HurtByTimestamp", this.revengeTimer);
      var1.setShort("DeathTime", (short)this.deathTime);
      var1.setFloat("AbsorptionAmount", this.getAbsorptionAmount());

      for(EntityEquipmentSlot var5 : EntityEquipmentSlot.values()) {
         ItemStack var6 = this.getItemStackFromSlot(var5);
         if (var6 != null) {
            this.getAttributeMap().removeAttributeModifiers(var6.getAttributeModifiers(var5));
         }
      }

      var1.setTag("Attributes", SharedMonsterAttributes.writeBaseAttributeMapToNBT(this.getAttributeMap()));

      for(EntityEquipmentSlot var13 : EntityEquipmentSlot.values()) {
         ItemStack var14 = this.getItemStackFromSlot(var13);
         if (var14 != null) {
            this.getAttributeMap().applyAttributeModifiers(var14.getAttributeModifiers(var13));
         }
      }

      if (!this.activePotionsMap.isEmpty()) {
         NBTTagList var7 = new NBTTagList();

         for(PotionEffect var9 : this.activePotionsMap.values()) {
            var7.appendTag(var9.writeCustomPotionEffectToNBT(new NBTTagCompound()));
         }

         var1.setTag("ActiveEffects", var7);
      }

      var1.setBoolean("FallFlying", this.isElytraFlying());
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      this.setAbsorptionAmount(var1.getFloat("AbsorptionAmount"));
      if (var1.hasKey("Attributes", 9) && this.world != null && !this.world.isRemote) {
         SharedMonsterAttributes.setAttributeModifiers(this.getAttributeMap(), var1.getTagList("Attributes", 10));
      }

      if (var1.hasKey("ActiveEffects", 9)) {
         NBTTagList var2 = var1.getTagList("ActiveEffects", 10);

         for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
            NBTTagCompound var4 = var2.getCompoundTagAt(var3);
            PotionEffect var5 = PotionEffect.readCustomPotionEffectFromNBT(var4);
            if (var5 != null) {
               this.activePotionsMap.put(var5.getPotion(), var5);
            }
         }
      }

      if (var1.hasKey("Bukkit.MaxHealth")) {
         NBTBase var6 = var1.getTag("Bukkit.MaxHealth");
         if (var6.getId() == 5) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(((NBTTagFloat)var6).getDouble());
         } else if (var6.getId() == 3) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(((NBTTagInt)var6).getDouble());
         }
      }

      if (var1.hasKey("Health", 99)) {
         this.setHealth(var1.getFloat("Health"));
      }

      this.hurtTime = var1.getShort("HurtTime");
      this.deathTime = var1.getShort("DeathTime");
      this.revengeTimer = var1.getInteger("HurtByTimestamp");
      if (var1.hasKey("Team", 8)) {
         String var7 = var1.getString("Team");
         this.world.getScoreboard().addPlayerToTeam(this.getCachedUniqueIdString(), var7);
      }

      if (var1.getBoolean("FallFlying")) {
         this.setFlag(7, true);
      }

   }

   protected void updatePotionEffects() {
      Iterator var1 = this.activePotionsMap.keySet().iterator();
      this.isTickingEffects = true;

      while(var1.hasNext()) {
         Potion var2 = (Potion)var1.next();
         PotionEffect var3 = (PotionEffect)this.activePotionsMap.get(var2);
         if (!var3.onUpdate(this)) {
            if (!this.world.isRemote) {
               var1.remove();
               this.onFinishedPotionEffect(var3);
            }
         } else if (var3.getDuration() % 600 == 0) {
            this.onChangedPotionEffect(var3, false);
         }
      }

      this.isTickingEffects = false;

      for(Object var11 : this.effectsToProcess) {
         if (var11 instanceof PotionEffect) {
            this.addPotionEffect((PotionEffect)var11);
         } else {
            this.removePotionEffect((Potion)var11);
         }
      }

      if (this.potionsNeedUpdate) {
         if (!this.world.isRemote) {
            this.updatePotionMetadata();
         }

         this.potionsNeedUpdate = false;
      }

      int var12 = ((Integer)this.dataManager.get(POTION_EFFECTS)).intValue();
      boolean var14 = ((Boolean)this.dataManager.get(HIDE_PARTICLES)).booleanValue();
      if (var12 > 0) {
         boolean var4;
         if (this.isInvisible()) {
            var4 = this.rand.nextInt(15) == 0;
         } else {
            var4 = this.rand.nextBoolean();
         }

         if (var14) {
            var4 &= this.rand.nextInt(5) == 0;
         }

         if (var4 && var12 > 0) {
            double var5 = (double)(var12 >> 16 & 255) / 255.0D;
            double var7 = (double)(var12 >> 8 & 255) / 255.0D;
            double var9 = (double)(var12 >> 0 & 255) / 255.0D;
            this.world.spawnParticle(var14 ? EnumParticleTypes.SPELL_MOB_AMBIENT : EnumParticleTypes.SPELL_MOB, this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, var5, var7, var9);
         }
      }

   }

   protected void updatePotionMetadata() {
      if (this.activePotionsMap.isEmpty()) {
         this.resetPotionEffectMetadata();
         this.setInvisible(false);
      } else {
         Collection var1 = this.activePotionsMap.values();
         this.dataManager.set(HIDE_PARTICLES, Boolean.valueOf(areAllPotionsAmbient(var1)));
         this.dataManager.set(POTION_EFFECTS, Integer.valueOf(PotionUtils.getPotionColorFromEffectList(var1)));
         this.setInvisible(this.isPotionActive(MobEffects.INVISIBILITY));
      }

   }

   public static boolean areAllPotionsAmbient(Collection var0) {
      for(PotionEffect var2 : var0) {
         if (!var2.getIsAmbient()) {
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
         Iterator var1 = this.activePotionsMap.values().iterator();

         while(var1.hasNext()) {
            this.onFinishedPotionEffect((PotionEffect)var1.next());
            var1.remove();
         }
      }

   }

   public Collection getActivePotionEffects() {
      return this.activePotionsMap.values();
   }

   public boolean isPotionActive(Potion var1) {
      return this.activePotionsMap.containsKey(var1);
   }

   @Nullable
   public PotionEffect getActivePotionEffect(Potion var1) {
      return (PotionEffect)this.activePotionsMap.get(var1);
   }

   public void addPotionEffect(PotionEffect var1) {
      if (this.isTickingEffects) {
         this.effectsToProcess.add(var1);
      } else {
         if (this.isPotionApplicable(var1)) {
            PotionEffect var2 = (PotionEffect)this.activePotionsMap.get(var1.getPotion());
            if (var2 == null) {
               this.activePotionsMap.put(var1.getPotion(), var1);
               this.onNewPotionEffect(var1);
            } else {
               var2.combine(var1);
               this.onChangedPotionEffect(var2, true);
            }
         }

      }
   }

   public boolean isPotionApplicable(PotionEffect var1) {
      if (this.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD) {
         Potion var2 = var1.getPotion();
         if (var2 == MobEffects.REGENERATION || var2 == MobEffects.POISON) {
            return false;
         }
      }

      return true;
   }

   public boolean isEntityUndead() {
      return this.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD;
   }

   @Nullable
   public PotionEffect removeActivePotionEffect(@Nullable Potion var1) {
      if (this.isTickingEffects) {
         this.effectsToProcess.add(var1);
         return null;
      } else {
         return (PotionEffect)this.activePotionsMap.remove(var1);
      }
   }

   public void removePotionEffect(Potion var1) {
      PotionEffect var2 = this.removeActivePotionEffect(var1);
      if (var2 != null) {
         this.onFinishedPotionEffect(var2);
      }

   }

   protected void onNewPotionEffect(PotionEffect var1) {
      this.potionsNeedUpdate = true;
      if (!this.world.isRemote) {
         var1.getPotion().applyAttributesModifiersToEntity(this, this.getAttributeMap(), var1.getAmplifier());
      }

   }

   protected void onChangedPotionEffect(PotionEffect var1, boolean var2) {
      this.potionsNeedUpdate = true;
      if (var2 && !this.world.isRemote) {
         Potion var3 = var1.getPotion();
         var3.removeAttributesModifiersFromEntity(this, this.getAttributeMap(), var1.getAmplifier());
         var3.applyAttributesModifiersToEntity(this, this.getAttributeMap(), var1.getAmplifier());
      }

   }

   protected void onFinishedPotionEffect(PotionEffect var1) {
      this.potionsNeedUpdate = true;
      if (!this.world.isRemote) {
         var1.getPotion().removeAttributesModifiersFromEntity(this, this.getAttributeMap(), var1.getAmplifier());
      }

   }

   public void heal(float var1) {
      this.heal(var1, RegainReason.CUSTOM);
   }

   public void heal(float var1, RegainReason var2) {
      float var3 = this.getHealth();
      if (var3 > 0.0F) {
         EntityRegainHealthEvent var4 = new EntityRegainHealthEvent(this.getBukkitEntity(), (double)var1, var2);
         this.world.getServer().getPluginManager().callEvent(var4);
         if (!var4.isCancelled()) {
            this.setHealth((float)((double)this.getHealth() + var4.getAmount()));
         }
      }

   }

   public final float getHealth() {
      return this instanceof EntityPlayerMP ? (float)((EntityPlayerMP)this).getBukkitEntity().getHealth() : ((Float)this.dataManager.get(HEALTH)).floatValue();
   }

   public void setHealth(float var1) {
      if (this instanceof EntityPlayerMP) {
         CraftPlayer var2 = ((EntityPlayerMP)this).getBukkitEntity();
         if (var1 < 0.0F) {
            var2.setRealHealth(0.0D);
         } else if ((double)var1 > var2.getMaxHealth()) {
            var2.setRealHealth(var2.getMaxHealth());
         } else {
            var2.setRealHealth((double)var1);
         }

         this.dataManager.set(HEALTH, Float.valueOf(var2.getScaledHealth()));
      } else {
         this.dataManager.set(HEALTH, Float.valueOf(MathHelper.clamp(var1, 0.0F, this.getMaxHealth())));
      }
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isEntityInvulnerable(var1)) {
         return false;
      } else if (this.world.isRemote) {
         return false;
      } else {
         this.entityAge = 0;
         if (this.getHealth() <= 0.0F) {
            return false;
         } else if (var1.isFireDamage() && this.isPotionActive(MobEffects.FIRE_RESISTANCE)) {
            return false;
         } else {
            boolean var3 = var2 > 0.0F && this.canBlockDamageSource(var1);
            this.limbSwingAmount = 1.5F;
            boolean var4 = true;
            if ((float)this.hurtResistantTime > (float)this.maxHurtResistantTime / 2.0F) {
               if (var2 <= this.lastDamage) {
                  this.forceExplosionKnockback = true;
                  return false;
               }

               if (!this.damageEntity0(var1, var2 - this.lastDamage)) {
                  return false;
               }

               this.lastDamage = var2;
               var4 = false;
            } else {
               if (!this.damageEntity0(var1, var2)) {
                  return false;
               }

               this.lastDamage = var2;
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
            Entity var5 = var1.getEntity();
            if (var5 != null) {
               if (var5 instanceof EntityLivingBase) {
                  this.setRevengeTarget((EntityLivingBase)var5);
               }

               if (var5 instanceof EntityPlayer) {
                  this.recentlyHit = 100;
                  this.attackingPlayer = (EntityPlayer)var5;
               } else if (var5 instanceof EntityWolf) {
                  EntityWolf var6 = (EntityWolf)var5;
                  if (var6.isTamed()) {
                     this.recentlyHit = 100;
                     this.attackingPlayer = null;
                  }
               }
            }

            if (var4) {
               if (var3) {
                  this.world.setEntityState(this, (byte)29);
               } else if (var1 instanceof EntityDamageSource && ((EntityDamageSource)var1).getIsThornsDamage()) {
                  this.world.setEntityState(this, (byte)33);
               } else {
                  this.world.setEntityState(this, (byte)2);
               }

               if (var1 != DamageSource.drown && (!var3 || var2 > 0.0F)) {
                  this.setBeenAttacked();
               }

               if (var5 != null) {
                  double var7 = var5.posX - this.posX;

                  double var9;
                  for(var9 = var5.posZ - this.posZ; var7 * var7 + var9 * var9 < 1.0E-4D; var9 = (Math.random() - Math.random()) * 0.01D) {
                     var7 = (Math.random() - Math.random()) * 0.01D;
                  }

                  this.attackedAtYaw = (float)(MathHelper.atan2(var9, var7) * 57.2957763671875D - (double)this.rotationYaw);
                  this.knockBack(var5, 0.4F, var7, var9);
               } else {
                  this.attackedAtYaw = (float)((int)(Math.random() * 2.0D) * 180);
               }
            }

            if (this.getHealth() <= 0.0F) {
               SoundEvent var11 = this.getDeathSound();
               if (var4 && var11 != null) {
                  this.playSound(var11, this.getSoundVolume(), this.getSoundPitch());
               }

               this.onDeath(var1);
            } else if (var4) {
               this.playHurtSound(var1);
            }

            if (!var3 || var2 > 0.0F) {
               this.lastDamageSource = var1;
               this.lastDamageStamp = this.world.getTotalWorldTime();
            }

            return !var3 || var2 > 0.0F;
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

   protected void playHurtSound(DamageSource var1) {
      SoundEvent var2 = this.getHurtSound();
      if (var2 != null) {
         this.playSound(var2, this.getSoundVolume(), this.getSoundPitch());
      }

   }

   private boolean canBlockDamageSource(DamageSource var1) {
      if (!var1.isUnblockable() && this.isActiveItemStackBlocking()) {
         Vec3d var2 = var1.getDamageLocation();
         if (var2 != null) {
            Vec3d var3 = this.getLook(1.0F);
            Vec3d var4 = var2.subtractReverse(new Vec3d(this.posX, this.posY, this.posZ)).normalize();
            var4 = new Vec3d(var4.xCoord, 0.0D, var4.zCoord);
            if (var4.dotProduct(var3) < 0.0D) {
               return true;
            }
         }
      }

      return false;
   }

   public void renderBrokenItemStack(ItemStack var1) {
      this.playSound(SoundEvents.ENTITY_ITEM_BREAK, 0.8F, 0.8F + this.world.rand.nextFloat() * 0.4F);

      for(int var2 = 0; var2 < 5; ++var2) {
         Vec3d var3 = new Vec3d(((double)this.rand.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
         var3 = var3.rotatePitch(-this.rotationPitch * 0.017453292F);
         var3 = var3.rotateYaw(-this.rotationYaw * 0.017453292F);
         double var4 = (double)(-this.rand.nextFloat()) * 0.6D - 0.3D;
         Vec3d var6 = new Vec3d(((double)this.rand.nextFloat() - 0.5D) * 0.3D, var4, 0.6D);
         var6 = var6.rotatePitch(-this.rotationPitch * 0.017453292F);
         var6 = var6.rotateYaw(-this.rotationYaw * 0.017453292F);
         var6 = var6.addVector(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ);
         this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, var6.xCoord, var6.yCoord, var6.zCoord, var3.xCoord, var3.yCoord + 0.05D, var3.zCoord, Item.getIdFromItem(var1.getItem()));
      }

   }

   public void onDeath(DamageSource var1) {
      if (!this.dead) {
         Entity var2 = var1.getEntity();
         EntityLivingBase var3 = this.getAttackingEntity();
         if (this.scoreValue >= 0 && var3 != null) {
            var3.addToPlayerScore(this, this.scoreValue);
         }

         if (var2 != null) {
            var2.onKillEntity(this);
         }

         this.dead = true;
         this.getCombatTracker().reset();
         if (!this.world.isRemote) {
            int var4 = 0;
            if (var2 instanceof EntityPlayer) {
               var4 = EnchantmentHelper.getLootingModifier((EntityLivingBase)var2);
            }

            if (this.canDropLoot() && this.world.getGameRules().getBoolean("doMobLoot")) {
               boolean var5 = this.recentlyHit > 0;
               this.dropLoot(var5, var4, var1);
               CraftEventFactory.callEntityDeathEvent(this, this.drops);
               this.drops = new ArrayList();
            } else {
               CraftEventFactory.callEntityDeathEvent(this);
            }
         }

         this.world.setEntityState(this, (byte)3);
      }

   }

   protected void dropLoot(boolean var1, int var2, DamageSource var3) {
      this.dropFewItems(var1, var2);
      this.dropEquipment(var1, var2);
   }

   protected void dropEquipment(boolean var1, int var2) {
   }

   public void knockBack(Entity var1, float var2, double var3, double var5) {
      if (this.rand.nextDouble() >= this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue()) {
         this.isAirBorne = true;
         float var7 = MathHelper.sqrt(var3 * var3 + var5 * var5);
         this.motionX /= 2.0D;
         this.motionZ /= 2.0D;
         this.motionX -= var3 / (double)var7 * (double)var2;
         this.motionZ -= var5 / (double)var7 * (double)var2;
         if (this.onGround) {
            this.motionY /= 2.0D;
            this.motionY += (double)var2;
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

   protected SoundEvent getFallSound(int var1) {
      return var1 > 4 ? SoundEvents.ENTITY_GENERIC_BIG_FALL : SoundEvents.ENTITY_GENERIC_SMALL_FALL;
   }

   protected void dropFewItems(boolean var1, int var2) {
   }

   public boolean isOnLadder() {
      int var1 = MathHelper.floor(this.posX);
      int var2 = MathHelper.floor(this.getEntityBoundingBox().minY);
      int var3 = MathHelper.floor(this.posZ);
      if (this instanceof EntityPlayer && ((EntityPlayer)this).isSpectator()) {
         return false;
      } else {
         BlockPos var4 = new BlockPos(var1, var2, var3);
         IBlockState var5 = this.world.getBlockState(var4);
         Block var6 = var5.getBlock();
         return var6 != Blocks.LADDER && var6 != Blocks.VINE ? var6 instanceof BlockTrapDoor && this.canGoThroughtTrapDoorOnLadder(var4, var5) : true;
      }
   }

   private boolean canGoThroughtTrapDoorOnLadder(BlockPos var1, IBlockState var2) {
      if (((Boolean)var2.getValue(BlockTrapDoor.OPEN)).booleanValue()) {
         IBlockState var3 = this.world.getBlockState(var1.down());
         if (var3.getBlock() == Blocks.LADDER && var3.getValue(BlockLadder.FACING) == var2.getValue(BlockTrapDoor.FACING)) {
            return true;
         }
      }

      return false;
   }

   public boolean isEntityAlive() {
      return !this.isDead && this.getHealth() > 0.0F;
   }

   public void fall(float var1, float var2) {
      super.fall(var1, var2);
      PotionEffect var3 = this.getActivePotionEffect(MobEffects.JUMP_BOOST);
      float var4 = var3 == null ? 0.0F : (float)(var3.getAmplifier() + 1);
      int var5 = MathHelper.ceil((var1 - 3.0F - var4) * var2);
      if (var5 > 0) {
         if (!this.attackEntityFrom(DamageSource.fall, (float)var5)) {
            return;
         }

         this.playSound(this.getFallSound(var5), 1.0F, 1.0F);
         int var6 = MathHelper.floor(this.posX);
         int var7 = MathHelper.floor(this.posY - 0.20000000298023224D);
         int var8 = MathHelper.floor(this.posZ);
         IBlockState var9 = this.world.getBlockState(new BlockPos(var6, var7, var8));
         if (var9.getMaterial() != Material.AIR) {
            SoundType var10 = var9.getBlock().getSoundType();
            this.playSound(var10.getFallSound(), var10.getVolume() * 0.5F, var10.getPitch() * 0.75F);
         }
      }

   }

   public int getTotalArmorValue() {
      IAttributeInstance var1 = this.getEntityAttribute(SharedMonsterAttributes.ARMOR);
      return MathHelper.floor(var1.getAttributeValue());
   }

   protected void damageArmor(float var1) {
   }

   protected void damageShield(float var1) {
   }

   protected float applyArmorCalculations(DamageSource var1, float var2) {
      if (!var1.isUnblockable()) {
         var2 = CombatRules.getDamageAfterAbsorb(var2, (float)this.getTotalArmorValue(), (float)this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
      }

      return var2;
   }

   protected float applyPotionDamageCalculations(DamageSource var1, float var2) {
      if (var1.isDamageAbsolute()) {
         return var2;
      } else if (var2 <= 0.0F) {
         return 0.0F;
      } else {
         int var3 = EnchantmentHelper.getEnchantmentModifierDamage(this.getArmorInventoryList(), var1);
         if (var3 > 0) {
            var2 = CombatRules.getDamageAfterMagicAbsorb(var2, (float)var3);
         }

         return var2;
      }
   }

   protected boolean damageEntity0(final DamageSource var1, float var2) {
      if (!this.isEntityInvulnerable(var1)) {
         boolean var3 = this instanceof EntityPlayer;
         float var4 = var2;
         Function var5 = new Function() {
            public Double apply(Double var1x) {
               return (var1 == DamageSource.anvil || var1 == DamageSource.fallingBlock) && EntityLivingBase.this.getItemStackFromSlot(EntityEquipmentSlot.HEAD) != null ? -(var1x.doubleValue() - var1x.doubleValue() * 0.75D) : -0.0D;
            }
         };
         float var6 = ((Double)var5.apply(Double.valueOf((double)var2))).floatValue();
         var2 = var2 + var6;
         Function var7 = new Function() {
            public Double apply(Double var1x) {
               return -(EntityLivingBase.this.canBlockDamageSource(var1) ? (var1.isProjectile() ? var1x.doubleValue() : var1x.doubleValue() - var1x.doubleValue() * 0.33000001311302185D) : 0.0D);
            }
         };
         float var8 = ((Double)var7.apply(Double.valueOf((double)var2))).floatValue();
         var2 = var2 + var8;
         Function var9 = new Function() {
            public Double apply(Double var1x) {
               return -(var1x.doubleValue() - (double)EntityLivingBase.this.applyArmorCalculations(var1, var1x.floatValue()));
            }
         };
         float var10 = ((Double)var9.apply(Double.valueOf((double)var2))).floatValue();
         var2 = var2 + var10;
         Function var11 = new Function() {
            public Double apply(Double var1x) {
               if (!var1.isDamageAbsolute() && EntityLivingBase.this.isPotionActive(MobEffects.RESISTANCE) && var1 != DamageSource.outOfWorld) {
                  int var2 = (EntityLivingBase.this.getActivePotionEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 5;
                  int var3 = 25 - var2;
                  float var4 = var1x.floatValue() * (float)var3;
                  return -(var1x.doubleValue() - (double)(var4 / 25.0F));
               } else {
                  return -0.0D;
               }
            }
         };
         float var12 = ((Double)var11.apply(Double.valueOf((double)var2))).floatValue();
         var2 = var2 + var12;
         Function var13 = new Function() {
            public Double apply(Double var1x) {
               return -(var1x.doubleValue() - (double)EntityLivingBase.this.applyPotionDamageCalculations(var1, var1x.floatValue()));
            }
         };
         float var14 = ((Double)var13.apply(Double.valueOf((double)var2))).floatValue();
         var2 = var2 + var14;
         Function var15 = new Function() {
            public Double apply(Double var1) {
               return -Math.max(var1.doubleValue() - Math.max(var1.doubleValue() - (double)EntityLivingBase.this.getAbsorptionAmount(), 0.0D), 0.0D);
            }
         };
         float var16 = ((Double)var15.apply(Double.valueOf((double)var2))).floatValue();
         EntityDamageEvent var17 = CraftEventFactory.handleLivingEntityDamageEvent(this, var1, (double)var4, (double)var6, (double)var8, (double)var10, (double)var12, (double)var14, (double)var16, var5, var7, var9, var11, var13, var15);
         if (var17.isCancelled()) {
            return false;
         } else {
            var2 = (float)var17.getFinalDamage();
            if ((var1 == DamageSource.anvil || var1 == DamageSource.fallingBlock) && this.getItemStackFromSlot(EntityEquipmentSlot.HEAD) != null) {
               this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).damageItem((int)(var17.getDamage() * 4.0D + (double)this.rand.nextFloat() * var17.getDamage() * 2.0D), this);
            }

            if (!var1.isUnblockable()) {
               float var18 = (float)(var17.getDamage() + var17.getDamage(DamageModifier.BLOCKING) + var17.getDamage(DamageModifier.HARD_HAT));
               this.damageArmor(var18);
            }

            if (var17.getDamage(DamageModifier.BLOCKING) < 0.0D) {
               this.damageShield((float)var17.getDamage());
               if (var1.getSourceOfDamage() instanceof EntityLivingBase) {
                  ((EntityLivingBase)var1.getSourceOfDamage()).knockBack(this, 0.5F, this.posX - var1.getSourceOfDamage().posX, this.posZ - var1.getSourceOfDamage().posZ);
               }

               if (var2 <= 0.0F) {
                  return false;
               }
            }

            var16 = (float)(-var17.getDamage(DamageModifier.ABSORPTION));
            this.setAbsorptionAmount(Math.max(this.getAbsorptionAmount() - var16, 0.0F));
            if (var2 > 0.0F) {
               if (var3) {
                  ((EntityPlayer)this).addExhaustion(var1.getHungerDamage());
                  if (var2 < 3.4028235E37F) {
                     ((EntityPlayer)this).addStat(StatList.DAMAGE_TAKEN, Math.round(var2 * 10.0F));
                  }
               }

               float var26 = this.getHealth();
               this.setHealth(var26 - var2);
               this.getCombatTracker().trackDamage(var1, var26, var2);
               if (!var3) {
                  this.setAbsorptionAmount(this.getAbsorptionAmount() - var2);
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

   public final void setArrowCountInEntity(int var1) {
      this.dataManager.set(ARROW_COUNT_IN_ENTITY, Integer.valueOf(var1));
   }

   private int getArmSwingAnimationEnd() {
      return this.isPotionActive(MobEffects.HASTE) ? 6 - (1 + this.getActivePotionEffect(MobEffects.HASTE).getAmplifier()) : (this.isPotionActive(MobEffects.MINING_FATIGUE) ? 6 + (1 + this.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) * 2 : 6);
   }

   public void swingArm(EnumHand var1) {
      if (!this.isSwingInProgress || this.swingProgressInt >= this.getArmSwingAnimationEnd() / 2 || this.swingProgressInt < 0) {
         this.swingProgressInt = -1;
         this.isSwingInProgress = true;
         this.swingingHand = var1;
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).getEntityTracker().sendToTracking(this, new SPacketAnimation(this, var1 == EnumHand.MAIN_HAND ? 0 : 3));
         }
      }

   }

   protected void kill() {
      this.attackEntityFrom(DamageSource.outOfWorld, 4.0F);
   }

   protected void updateArmSwingProgress() {
      int var1 = this.getArmSwingAnimationEnd();
      if (this.isSwingInProgress) {
         ++this.swingProgressInt;
         if (this.swingProgressInt >= var1) {
            this.swingProgressInt = 0;
            this.isSwingInProgress = false;
         }
      } else {
         this.swingProgressInt = 0;
      }

      this.swingProgress = (float)this.swingProgressInt / (float)var1;
   }

   public IAttributeInstance getEntityAttribute(IAttribute var1) {
      return this.getAttributeMap().getAttributeInstance(var1);
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
   public ItemStack getHeldItem(EnumHand var1) {
      if (var1 == EnumHand.MAIN_HAND) {
         return this.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
      } else if (var1 == EnumHand.OFF_HAND) {
         return this.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);
      } else {
         throw new IllegalArgumentException("Invalid hand " + var1);
      }
   }

   public void setHeldItem(EnumHand var1, @Nullable ItemStack var2) {
      if (var1 == EnumHand.MAIN_HAND) {
         this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, var2);
      } else {
         if (var1 != EnumHand.OFF_HAND) {
            throw new IllegalArgumentException("Invalid hand " + var1);
         }

         this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, var2);
      }

   }

   public abstract Iterable getArmorInventoryList();

   @Nullable
   public abstract ItemStack getItemStackFromSlot(EntityEquipmentSlot var1);

   public abstract void setItemStackToSlot(EntityEquipmentSlot var1, @Nullable ItemStack var2);

   public void setSprinting(boolean var1) {
      super.setSprinting(var1);
      IAttributeInstance var2 = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
      if (var2.getModifier(SPRINTING_SPEED_BOOST_ID) != null) {
         var2.removeModifier(SPRINTING_SPEED_BOOST);
      }

      if (var1) {
         var2.applyModifier(SPRINTING_SPEED_BOOST);
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

   public void dismountEntity(Entity var1) {
      if (!(var1 instanceof EntityBoat) && !(var1 instanceof EntityHorse)) {
         double var38 = var1.posX;
         double var4 = var1.getEntityBoundingBox().minY + (double)var1.height;
         double var39 = var1.posZ;
         EnumFacing var40 = var1.getAdjustedHorizontalFacing();
         EnumFacing var9 = var40.rotateY();
         int[][] var10 = new int[][]{{0, 1}, {0, -1}, {-1, 1}, {-1, -1}, {1, 1}, {1, -1}, {-1, 0}, {1, 0}, {0, 1}};
         double var41 = Math.floor(this.posX) + 0.5D;
         double var13 = Math.floor(this.posZ) + 0.5D;
         double var15 = this.getEntityBoundingBox().maxX - this.getEntityBoundingBox().minX;
         double var17 = this.getEntityBoundingBox().maxZ - this.getEntityBoundingBox().minZ;
         AxisAlignedBB var19 = new AxisAlignedBB(var41 - var15 / 2.0D, this.getEntityBoundingBox().minY, var13 - var17 / 2.0D, var41 + var15 / 2.0D, this.getEntityBoundingBox().maxY, var13 + var17 / 2.0D);

         for(int[] var23 : var10) {
            double var24 = (double)(var40.getFrontOffsetX() * var23[0] + var9.getFrontOffsetX() * var23[1]);
            double var26 = (double)(var40.getFrontOffsetZ() * var23[0] + var9.getFrontOffsetZ() * var23[1]);
            double var28 = var41 + var24;
            double var30 = var13 + var26;
            AxisAlignedBB var32 = var19.offset(var24, 1.0D, var26);
            if (!this.world.collidesWithAnyBlock(var32)) {
               if (this.world.getBlockState(new BlockPos(var28, this.posY, var30)).isFullyOpaque()) {
                  this.setPositionAndUpdate(var28, this.posY + 1.0D, var30);
                  return;
               }

               BlockPos var33 = new BlockPos(var28, this.posY - 1.0D, var30);
               if (this.world.getBlockState(var33).isFullyOpaque() || this.world.getBlockState(var33).getMaterial() == Material.WATER) {
                  var38 = var28;
                  var4 = this.posY + 1.0D;
                  var39 = var30;
               }
            } else if (!this.world.collidesWithAnyBlock(var32.offset(0.0D, 1.0D, 0.0D)) && this.world.getBlockState(new BlockPos(var28, this.posY + 1.0D, var30)).isFullyOpaque()) {
               var38 = var28;
               var4 = this.posY + 2.0D;
               var39 = var30;
            }
         }

         this.setPositionAndUpdate(var38, var4, var39);
      } else {
         double var2 = (double)(this.width / 2.0F + var1.width / 2.0F) + 0.4D;
         float var34;
         if (var1 instanceof EntityBoat) {
            var34 = 0.0F;
         } else {
            var34 = 1.5707964F * (float)(this.getPrimaryHand() == EnumHandSide.RIGHT ? -1 : 1);
         }

         float var35 = -MathHelper.sin(-this.rotationYaw * 0.017453292F - 3.1415927F + var34);
         float var8 = -MathHelper.cos(-this.rotationYaw * 0.017453292F - 3.1415927F + var34);
         double var6 = Math.abs(var35) > Math.abs(var8) ? var2 / (double)Math.abs(var35) : var2 / (double)Math.abs(var8);
         double var36 = this.posX + (double)var35 * var6;
         double var11 = this.posZ + (double)var8 * var6;
         this.setPosition(var36, var1.posY + (double)var1.height + 0.001D, var11);
         if (this.world.collidesWithAnyBlock(this.getEntityBoundingBox())) {
            this.setPosition(var36, var1.posY + (double)var1.height + 1.001D, var11);
            if (this.world.collidesWithAnyBlock(this.getEntityBoundingBox())) {
               this.setPosition(var1.posX, var1.posY + (double)this.height + 0.001D, var1.posZ);
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
         float var1 = this.rotationYaw * 0.017453292F;
         this.motionX -= (double)(MathHelper.sin(var1) * 0.2F);
         this.motionZ += (double)(MathHelper.cos(var1) * 0.2F);
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

   public void moveEntityWithHeading(float var1, float var2) {
      if (this.isServerWorld() || this.canPassengerSteer()) {
         if (!this.isInWater() || this instanceof EntityPlayer && ((EntityPlayer)this).capabilities.isFlying) {
            if (!this.isInLava() || this instanceof EntityPlayer && ((EntityPlayer)this).capabilities.isFlying) {
               if (this.isElytraFlying()) {
                  if (this.motionY > -0.5D) {
                     this.fallDistance = 1.0F;
                  }

                  Vec3d var8 = this.getLookVec();
                  float var9 = this.rotationPitch * 0.017453292F;
                  double var10 = Math.sqrt(var8.xCoord * var8.xCoord + var8.zCoord * var8.zCoord);
                  double var12 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
                  double var14 = var8.lengthVector();
                  float var16 = MathHelper.cos(var9);
                  var16 = (float)((double)var16 * (double)var16 * Math.min(1.0D, var14 / 0.4D));
                  this.motionY += -0.08D + (double)var16 * 0.06D;
                  if (this.motionY < 0.0D && var10 > 0.0D) {
                     double var17 = this.motionY * -0.1D * (double)var16;
                     this.motionY += var17;
                     this.motionX += var8.xCoord * var17 / var10;
                     this.motionZ += var8.zCoord * var17 / var10;
                  }

                  if (var9 < 0.0F) {
                     double var34 = var12 * (double)(-MathHelper.sin(var9)) * 0.04D;
                     this.motionY += var34 * 3.2D;
                     this.motionX -= var8.xCoord * var34 / var10;
                     this.motionZ -= var8.zCoord * var34 / var10;
                  }

                  if (var10 > 0.0D) {
                     this.motionX += (var8.xCoord / var10 * var12 - this.motionX) * 0.1D;
                     this.motionZ += (var8.zCoord / var10 * var12 - this.motionZ) * 0.1D;
                  }

                  this.motionX *= 0.9900000095367432D;
                  this.motionY *= 0.9800000190734863D;
                  this.motionZ *= 0.9900000095367432D;
                  this.move(this.motionX, this.motionY, this.motionZ);
                  if (this.isCollidedHorizontally && !this.world.isRemote) {
                     double var35 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
                     double var19 = var12 - var35;
                     float var21 = (float)(var19 * 10.0D - 3.0D);
                     if (var21 > 0.0F) {
                        this.playSound(this.getFallSound((int)var21), 1.0F, 1.0F);
                        this.attackEntityFrom(DamageSource.flyIntoWall, var21);
                     }
                  }

                  if (this.onGround && !this.world.isRemote && this.getFlag(7) && !CraftEventFactory.callToggleGlideEvent(this, false).isCancelled()) {
                     this.setFlag(7, false);
                  }
               } else {
                  float var29 = 0.91F;
                  BlockPos.PooledMutableBlockPos var31 = BlockPos.PooledMutableBlockPos.retain(this.posX, this.getEntityBoundingBox().minY - 1.0D, this.posZ);
                  if (this.onGround) {
                     var29 = this.world.getBlockState(var31).getBlock().slipperiness * 0.91F;
                  }

                  float var25 = 0.16277136F / (var29 * var29 * var29);
                  float var26;
                  if (this.onGround) {
                     var26 = this.getAIMoveSpeed() * var25;
                  } else {
                     var26 = this.jumpMovementFactor;
                  }

                  this.moveRelative(var1, var2, var26);
                  var29 = 0.91F;
                  if (this.onGround) {
                     var29 = this.world.getBlockState(var31.setPos(this.posX, this.getEntityBoundingBox().minY - 1.0D, this.posZ)).getBlock().slipperiness * 0.91F;
                  }

                  if (this.isOnLadder()) {
                     float var27 = 0.15F;
                     this.motionX = MathHelper.clamp(this.motionX, -0.15000000596046448D, 0.15000000596046448D);
                     this.motionZ = MathHelper.clamp(this.motionZ, -0.15000000596046448D, 0.15000000596046448D);
                     this.fallDistance = 0.0F;
                     if (this.motionY < -0.15D) {
                        this.motionY = -0.15D;
                     }

                     boolean var22 = this.isSneaking() && this instanceof EntityPlayer;
                     if (var22 && this.motionY < 0.0D) {
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
                     var31.setPos(this.posX, 0.0D, this.posZ);
                     if (this.world.isRemote && (!this.world.isBlockLoaded(var31) || !this.world.getChunkFromBlockCoords(var31).isLoaded())) {
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
                  this.motionX *= (double)var29;
                  this.motionZ *= (double)var29;
                  var31.release();
               }
            } else {
               double var23 = this.posY;
               this.moveRelative(var1, var2, 0.02F);
               this.move(this.motionX, this.motionY, this.motionZ);
               this.motionX *= 0.5D;
               this.motionY *= 0.5D;
               this.motionZ *= 0.5D;
               if (!this.hasNoGravity()) {
                  this.motionY -= 0.02D;
               }

               if (this.isCollidedHorizontally && this.isOffsetPositionInLiquid(this.motionX, this.motionY + 0.6000000238418579D - this.posY + var23, this.motionZ)) {
                  this.motionY = 0.30000001192092896D;
               }
            }
         } else {
            double var3 = this.posY;
            float var5 = this.getWaterSlowDown();
            float var6 = 0.02F;
            float var7 = (float)EnchantmentHelper.getDepthStriderModifier(this);
            if (var7 > 3.0F) {
               var7 = 3.0F;
            }

            if (!this.onGround) {
               var7 *= 0.5F;
            }

            if (var7 > 0.0F) {
               var5 += (0.54600006F - var5) * var7 / 3.0F;
               var6 += (this.getAIMoveSpeed() - var6) * var7 / 3.0F;
            }

            this.moveRelative(var1, var2, var6);
            this.move(this.motionX, this.motionY, this.motionZ);
            this.motionX *= (double)var5;
            this.motionY *= 0.800000011920929D;
            this.motionZ *= (double)var5;
            if (!this.hasNoGravity()) {
               this.motionY -= 0.02D;
            }

            if (this.isCollidedHorizontally && this.isOffsetPositionInLiquid(this.motionX, this.motionY + 0.6000000238418579D - this.posY + var3, this.motionZ)) {
               this.motionY = 0.30000001192092896D;
            }
         }
      }

      this.prevLimbSwingAmount = this.limbSwingAmount;
      double var24 = this.posX - this.prevPosX;
      double var32 = this.posZ - this.prevPosZ;
      float var28 = MathHelper.sqrt(var24 * var24 + var32 * var32) * 4.0F;
      if (var28 > 1.0F) {
         var28 = 1.0F;
      }

      this.limbSwingAmount += (var28 - this.limbSwingAmount) * 0.4F;
      this.limbSwing += this.limbSwingAmount;
   }

   public float getAIMoveSpeed() {
      return this.landMovementFactor;
   }

   public void setAIMoveSpeed(float var1) {
      this.landMovementFactor = var1;
   }

   public boolean attackEntityAsMob(Entity var1) {
      this.setLastAttacker(var1);
      return false;
   }

   public boolean isPlayerSleeping() {
      return false;
   }

   public void onUpdate() {
      super.onUpdate();
      this.updateActiveHand();
      if (!this.world.isRemote) {
         int var1 = this.getArrowCountInEntity();
         if (var1 > 0) {
            if (this.arrowHitTimer <= 0) {
               this.arrowHitTimer = 20 * (30 - var1);
            }

            --this.arrowHitTimer;
            if (this.arrowHitTimer <= 0) {
               this.setArrowCountInEntity(var1 - 1);
            }
         }

         for(EntityEquipmentSlot var5 : EntityEquipmentSlot.values()) {
            ItemStack var6;
            switch(EntityLivingBase.SyntheticClass_1.a[var5.getSlotType().ordinal()]) {
            case 1:
               var6 = this.handInventory[var5.getIndex()];
               break;
            case 2:
               var6 = this.armorArray[var5.getIndex()];
               break;
            default:
               continue;
            }

            ItemStack var7 = this.getItemStackFromSlot(var5);
            if (!ItemStack.areItemStacksEqual(var7, var6)) {
               ((WorldServer)this.world).getEntityTracker().sendToTracking(this, new SPacketEntityEquipment(this.getEntityId(), var5, var7));
               if (var6 != null) {
                  this.getAttributeMap().removeAttributeModifiers(var6.getAttributeModifiers(var5));
               }

               if (var7 != null) {
                  this.getAttributeMap().applyAttributeModifiers(var7.getAttributeModifiers(var5));
               }

               switch(EntityLivingBase.SyntheticClass_1.a[var5.getSlotType().ordinal()]) {
               case 1:
                  this.handInventory[var5.getIndex()] = var7 == null ? null : var7.copy();
                  break;
               case 2:
                  this.armorArray[var5.getIndex()] = var7 == null ? null : var7.copy();
               }
            }
         }

         if (this.ticksExisted % 20 == 0) {
            this.getCombatTracker().reset();
         }

         if (!this.glowing) {
            boolean var13 = this.isPotionActive(MobEffects.GLOWING);
            if (this.getFlag(6) != var13) {
               this.setFlag(6, var13);
            }
         }
      }

      this.onLivingUpdate();
      double var8 = this.posX - this.prevPosX;
      double var10 = this.posZ - this.prevPosZ;
      float var14 = (float)(var8 * var8 + var10 * var10);
      float var15 = this.renderYawOffset;
      float var16 = 0.0F;
      this.prevOnGroundSpeedFactor = this.onGroundSpeedFactor;
      float var12 = 0.0F;
      if (var14 > 0.0025000002F) {
         var12 = 1.0F;
         var16 = (float)Math.sqrt((double)var14) * 3.0F;
         var15 = (float)MathHelper.atan2(var10, var8) * 57.295776F - 90.0F;
      }

      if (this.swingProgress > 0.0F) {
         var15 = this.rotationYaw;
      }

      if (!this.onGround) {
         var12 = 0.0F;
      }

      this.onGroundSpeedFactor += (var12 - this.onGroundSpeedFactor) * 0.3F;
      this.world.theProfiler.startSection("headTurn");
      var16 = this.updateDistance(var15, var16);
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
      this.movedDistance += var16;
      if (this.isElytraFlying()) {
         ++this.ticksElytraFlying;
      } else {
         this.ticksElytraFlying = 0;
      }

   }

   protected float updateDistance(float var1, float var2) {
      float var3 = MathHelper.wrapDegrees(var1 - this.renderYawOffset);
      this.renderYawOffset += var3 * 0.3F;
      float var4 = MathHelper.wrapDegrees(this.rotationYaw - this.renderYawOffset);
      boolean var5 = var4 < -90.0F || var4 >= 90.0F;
      if (var4 < -75.0F) {
         var4 = -75.0F;
      }

      if (var4 >= 75.0F) {
         var4 = 75.0F;
      }

      this.renderYawOffset = this.rotationYaw - var4;
      if (var4 * var4 > 2500.0F) {
         this.renderYawOffset += var4 * 0.2F;
      }

      if (var5) {
         var2 *= -1.0F;
      }

      return var2;
   }

   public void onLivingUpdate() {
      if (this.jumpTicks > 0) {
         --this.jumpTicks;
      }

      if (this.newPosRotationIncrements > 0 && !this.canPassengerSteer()) {
         double var1 = this.posX + (this.interpTargetX - this.posX) / (double)this.newPosRotationIncrements;
         double var3 = this.posY + (this.interpTargetY - this.posY) / (double)this.newPosRotationIncrements;
         double var5 = this.posZ + (this.interpTargetZ - this.posZ) / (double)this.newPosRotationIncrements;
         double var7 = MathHelper.wrapDegrees(this.interpTargetYaw - (double)this.rotationYaw);
         this.rotationYaw = (float)((double)this.rotationYaw + var7 / (double)this.newPosRotationIncrements);
         this.rotationPitch = (float)((double)this.rotationPitch + (this.interpTargetPitch - (double)this.rotationPitch) / (double)this.newPosRotationIncrements);
         --this.newPosRotationIncrements;
         this.setPosition(var1, var3, var5);
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
      boolean var1 = this.getFlag(7);
      if (var1 && !this.onGround && !this.isRiding()) {
         ItemStack var2 = this.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
         if (var2 != null && var2.getItem() == Items.ELYTRA && ItemElytra.isBroken(var2)) {
            var1 = true;
            if (!this.world.isRemote && (this.ticksElytraFlying + 1) % 20 == 0) {
               var2.damageItem(1, this);
            }
         } else {
            var1 = false;
         }
      } else {
         var1 = false;
      }

      if (!this.world.isRemote && var1 != this.getFlag(7) && !CraftEventFactory.callToggleGlideEvent(this, var1).isCancelled()) {
         this.setFlag(7, var1);
      }

   }

   protected void updateEntityActionState() {
   }

   protected void collideWithNearbyEntities() {
      List var1 = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox(), EntitySelectors.getTeamCollisionPredicate(this));
      if (!var1.isEmpty()) {
         for(int var2 = 0; var2 < var1.size(); ++var2) {
            Entity var3 = (Entity)var1.get(var2);
            this.collideWithEntity(var3);
         }
      }

   }

   protected void collideWithEntity(Entity var1) {
      var1.applyEntityCollision(this);
   }

   public void dismountRidingEntity() {
      Entity var1 = this.getRidingEntity();
      super.dismountRidingEntity();
      if (var1 != null && var1 != this.getRidingEntity() && !this.world.isRemote) {
         this.dismountEntity(var1);
      }

   }

   public void updateRidden() {
      super.updateRidden();
      this.prevOnGroundSpeedFactor = this.onGroundSpeedFactor;
      this.onGroundSpeedFactor = 0.0F;
      this.fallDistance = 0.0F;
   }

   public void setJumping(boolean var1) {
      this.isJumping = var1;
   }

   public void onItemPickup(Entity var1, int var2) {
      if (!var1.isDead && !this.world.isRemote) {
         EntityTracker var3 = ((WorldServer)this.world).getEntityTracker();
         if (var1 instanceof EntityItem) {
            var3.sendToTracking(var1, new SPacketCollectItem(var1.getEntityId(), this.getEntityId()));
         }

         if (var1 instanceof EntityArrow) {
            var3.sendToTracking(var1, new SPacketCollectItem(var1.getEntityId(), this.getEntityId()));
         }

         if (var1 instanceof EntityXPOrb) {
            var3.sendToTracking(var1, new SPacketCollectItem(var1.getEntityId(), this.getEntityId()));
         }
      }

   }

   public boolean canEntityBeSeen(Entity var1) {
      return this.world.rayTraceBlocks(new Vec3d(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ), new Vec3d(var1.posX, var1.posY + (double)var1.getEyeHeight(), var1.posZ), false, true, false) == null;
   }

   public Vec3d getLookVec() {
      return this.getLook(1.0F);
   }

   public Vec3d getLook(float var1) {
      if (var1 == 1.0F) {
         return this.getVectorForRotation(this.rotationPitch, this.rotationYawHead);
      } else {
         float var2 = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * var1;
         float var3 = this.prevRotationYawHead + (this.rotationYawHead - this.prevRotationYawHead) * var1;
         return this.getVectorForRotation(var2, var3);
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

   public void setRotationYawHead(float var1) {
      this.rotationYawHead = var1;
   }

   public void setRenderYawOffset(float var1) {
      this.renderYawOffset = var1;
   }

   public float getAbsorptionAmount() {
      return this.absorptionAmount;
   }

   public void setAbsorptionAmount(float var1) {
      if (var1 < 0.0F) {
         var1 = 0.0F;
      }

      this.absorptionAmount = var1;
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
         ItemStack var1 = this.getHeldItem(this.getActiveHand());
         if (var1 == this.activeItemStack) {
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

   public void setActiveHand(EnumHand var1) {
      ItemStack var2 = this.getHeldItem(var1);
      if (var2 != null && !this.isHandActive()) {
         this.activeItemStack = var2;
         this.activeItemStackUseCount = var2.getMaxItemUseDuration();
         if (!this.world.isRemote) {
            int var3 = 1;
            if (var1 == EnumHand.OFF_HAND) {
               var3 |= 2;
            }

            this.dataManager.set(HAND_STATES, Byte.valueOf((byte)var3));
         }
      }

   }

   public void notifyDataManagerChange(DataParameter var1) {
      super.notifyDataManagerChange(var1);
      if (HAND_STATES.equals(var1) && this.world.isRemote) {
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

   protected void updateItemUse(@Nullable ItemStack var1, int var2) {
      if (var1 != null && this.isHandActive()) {
         if (var1.getItemUseAction() == EnumAction.DRINK) {
            this.playSound(SoundEvents.ENTITY_GENERIC_DRINK, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
         }

         if (var1.getItemUseAction() == EnumAction.EAT) {
            for(int var3 = 0; var3 < var2; ++var3) {
               Vec3d var4 = new Vec3d(((double)this.rand.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
               var4 = var4.rotatePitch(-this.rotationPitch * 0.017453292F);
               var4 = var4.rotateYaw(-this.rotationYaw * 0.017453292F);
               double var5 = (double)(-this.rand.nextFloat()) * 0.6D - 0.3D;
               Vec3d var7 = new Vec3d(((double)this.rand.nextFloat() - 0.5D) * 0.3D, var5, 0.6D);
               var7 = var7.rotatePitch(-this.rotationPitch * 0.017453292F);
               var7 = var7.rotateYaw(-this.rotationYaw * 0.017453292F);
               var7 = var7.addVector(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ);
               if (var1.getHasSubtypes()) {
                  this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, var7.xCoord, var7.yCoord, var7.zCoord, var4.xCoord, var4.yCoord + 0.05D, var4.zCoord, Item.getIdFromItem(var1.getItem()), var1.getMetadata());
               } else {
                  this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, var7.xCoord, var7.yCoord, var7.zCoord, var4.xCoord, var4.yCoord + 0.05D, var4.zCoord, Item.getIdFromItem(var1.getItem()));
               }
            }

            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.5F + 0.5F * (float)this.rand.nextInt(2), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
         }
      }

   }

   protected void onItemUseFinish() {
      if (this.activeItemStack != null && this.isHandActive()) {
         this.updateItemUse(this.activeItemStack, 16);
         org.bukkit.inventory.ItemStack var1 = CraftItemStack.asBukkitCopy(this.activeItemStack);
         PlayerItemConsumeEvent var2 = new PlayerItemConsumeEvent((Player)this.getBukkitEntity(), var1);
         this.world.getServer().getPluginManager().callEvent(var2);
         if (var2.isCancelled()) {
            if (this instanceof EntityPlayerMP) {
               ((EntityPlayerMP)this).getBukkitEntity().updateInventory();
               ((EntityPlayerMP)this).getBukkitEntity().updateScaledHealth();
            }

            return;
         }

         ItemStack var3 = var1.equals(var2.getItem()) ? this.activeItemStack.onItemUseFinish(this.world, this) : CraftItemStack.asNMSCopy(var2.getItem()).onItemUseFinish(this.world, this);
         if (var3 != null && var3.stackSize == 0) {
            var3 = null;
         }

         this.setHeldItem(this.getActiveHand(), var3);
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
         Item var1 = this.activeItemStack.getItem();
         return var1.getItemUseAction(this.activeItemStack) != EnumAction.BLOCK ? false : var1.getMaxItemUseDuration(this.activeItemStack) - this.activeItemStackUseCount >= 5;
      } else {
         return false;
      }
   }

   public boolean isElytraFlying() {
      return this.getFlag(7);
   }

   public boolean attemptTeleport(double var1, double var3, double var5) {
      double var7 = this.posX;
      double var9 = this.posY;
      double var11 = this.posZ;
      this.posX = var1;
      this.posY = var3;
      this.posZ = var5;
      boolean var13 = false;
      BlockPos var14 = new BlockPos(this);
      World var15 = this.world;
      Random var16 = this.getRNG();
      if (var15.isBlockLoaded(var14)) {
         boolean var17 = false;

         while(!var17 && var14.getY() > 0) {
            BlockPos var18 = var14.down();
            IBlockState var19 = var15.getBlockState(var18);
            if (var19.getMaterial().blocksMovement()) {
               var17 = true;
            } else {
               --this.posY;
               var14 = var18;
            }
         }

         if (var17) {
            EntityTeleportEvent var32 = new EntityTeleportEvent(this.getBukkitEntity(), new Location(this.world.getWorld(), var7, var9, var11), new Location(this.world.getWorld(), this.posX, this.posY, this.posZ));
            this.world.getServer().getPluginManager().callEvent(var32);
            if (!var32.isCancelled()) {
               Location var34 = var32.getTo();
               this.setPositionAndUpdate(var34.getX(), var34.getY(), var34.getZ());
               if (var15.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty() && !var15.containsAnyLiquid(this.getEntityBoundingBox())) {
                  var13 = true;
               }
            }
         }
      }

      if (!var13) {
         this.setPositionAndUpdate(var7, var9, var11);
         return false;
      } else {
         boolean var31 = true;

         for(int var33 = 0; var33 < 128; ++var33) {
            double var20 = (double)var33 / 127.0D;
            float var22 = (var16.nextFloat() - 0.5F) * 0.2F;
            float var23 = (var16.nextFloat() - 0.5F) * 0.2F;
            float var24 = (var16.nextFloat() - 0.5F) * 0.2F;
            double var25 = var7 + (this.posX - var7) * var20 + (var16.nextDouble() - 0.5D) * (double)this.width * 2.0D;
            double var27 = var9 + (this.posY - var9) * var20 + var16.nextDouble() * (double)this.height;
            double var29 = var11 + (this.posZ - var11) * var20 + (var16.nextDouble() - 0.5D) * (double)this.width * 2.0D;
            var15.spawnParticle(EnumParticleTypes.PORTAL, var25, var27, var29, (double)var22, (double)var23, (double)var24);
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
