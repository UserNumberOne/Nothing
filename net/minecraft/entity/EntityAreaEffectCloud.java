package net.minecraft.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.init.PotionTypes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityAreaEffectCloud extends Entity {
   private static final DataParameter RADIUS = EntityDataManager.createKey(EntityAreaEffectCloud.class, DataSerializers.FLOAT);
   private static final DataParameter COLOR = EntityDataManager.createKey(EntityAreaEffectCloud.class, DataSerializers.VARINT);
   private static final DataParameter IGNORE_RADIUS = EntityDataManager.createKey(EntityAreaEffectCloud.class, DataSerializers.BOOLEAN);
   private static final DataParameter PARTICLE = EntityDataManager.createKey(EntityAreaEffectCloud.class, DataSerializers.VARINT);
   private static final DataParameter PARTICLE_PARAM_1 = EntityDataManager.createKey(EntityAreaEffectCloud.class, DataSerializers.VARINT);
   private static final DataParameter PARTICLE_PARAM_2 = EntityDataManager.createKey(EntityAreaEffectCloud.class, DataSerializers.VARINT);
   private PotionType potion;
   private final List effects;
   private final Map reapplicationDelayMap;
   private int duration;
   private int waitTime;
   private int reapplicationDelay;
   private boolean colorSet;
   private int durationOnUse;
   private float radiusOnUse;
   private float radiusPerTick;
   private EntityLivingBase owner;
   private UUID ownerUniqueId;

   public EntityAreaEffectCloud(World var1) {
      super(var1);
      this.potion = PotionTypes.EMPTY;
      this.effects = Lists.newArrayList();
      this.reapplicationDelayMap = Maps.newHashMap();
      this.duration = 600;
      this.waitTime = 20;
      this.reapplicationDelay = 20;
      this.noClip = true;
      this.isImmuneToFire = true;
      this.setRadius(3.0F);
   }

   public EntityAreaEffectCloud(World var1, double var2, double var4, double var6) {
      this(var1);
      this.setPosition(var2, var4, var6);
   }

   protected void entityInit() {
      this.getDataManager().register(COLOR, Integer.valueOf(0));
      this.getDataManager().register(RADIUS, Float.valueOf(0.5F));
      this.getDataManager().register(IGNORE_RADIUS, Boolean.valueOf(false));
      this.getDataManager().register(PARTICLE, Integer.valueOf(EnumParticleTypes.SPELL_MOB.getParticleID()));
      this.getDataManager().register(PARTICLE_PARAM_1, Integer.valueOf(0));
      this.getDataManager().register(PARTICLE_PARAM_2, Integer.valueOf(0));
   }

   public void setRadius(float var1) {
      double var2 = this.posX;
      double var4 = this.posY;
      double var6 = this.posZ;
      this.setSize(var1 * 2.0F, 0.5F);
      this.setPosition(var2, var4, var6);
      if (!this.world.isRemote) {
         this.getDataManager().set(RADIUS, Float.valueOf(var1));
      }

   }

   public float getRadius() {
      return ((Float)this.getDataManager().get(RADIUS)).floatValue();
   }

   public void setPotion(PotionType var1) {
      this.potion = var1;
      if (!this.colorSet) {
         if (var1 == PotionTypes.EMPTY && this.effects.isEmpty()) {
            this.getDataManager().set(COLOR, Integer.valueOf(0));
         } else {
            this.getDataManager().set(COLOR, Integer.valueOf(PotionUtils.getPotionColorFromEffectList(PotionUtils.mergeEffects(var1, this.effects))));
         }
      }

   }

   public void addEffect(PotionEffect var1) {
      this.effects.add(var1);
      if (!this.colorSet) {
         this.getDataManager().set(COLOR, Integer.valueOf(PotionUtils.getPotionColorFromEffectList(PotionUtils.mergeEffects(this.potion, this.effects))));
      }

   }

   public int getColor() {
      return ((Integer)this.getDataManager().get(COLOR)).intValue();
   }

   public void setColor(int var1) {
      this.colorSet = true;
      this.getDataManager().set(COLOR, Integer.valueOf(var1));
   }

   public EnumParticleTypes getParticle() {
      return EnumParticleTypes.getParticleFromId(((Integer)this.getDataManager().get(PARTICLE)).intValue());
   }

   public void setParticle(EnumParticleTypes var1) {
      this.getDataManager().set(PARTICLE, Integer.valueOf(var1.getParticleID()));
   }

   public int getParticleParam1() {
      return ((Integer)this.getDataManager().get(PARTICLE_PARAM_1)).intValue();
   }

   public void setParticleParam1(int var1) {
      this.getDataManager().set(PARTICLE_PARAM_1, Integer.valueOf(var1));
   }

   public int getParticleParam2() {
      return ((Integer)this.getDataManager().get(PARTICLE_PARAM_2)).intValue();
   }

   public void setParticleParam2(int var1) {
      this.getDataManager().set(PARTICLE_PARAM_2, Integer.valueOf(var1));
   }

   protected void setIgnoreRadius(boolean var1) {
      this.getDataManager().set(IGNORE_RADIUS, Boolean.valueOf(var1));
   }

   public boolean shouldIgnoreRadius() {
      return ((Boolean)this.getDataManager().get(IGNORE_RADIUS)).booleanValue();
   }

   public int getDuration() {
      return this.duration;
   }

   public void setDuration(int var1) {
      this.duration = var1;
   }

   public void onUpdate() {
      super.onUpdate();
      boolean var1 = this.shouldIgnoreRadius();
      float var2 = this.getRadius();
      if (this.world.isRemote) {
         EnumParticleTypes var3 = this.getParticle();
         int[] var4 = new int[var3.getArgumentCount()];
         if (var4.length > 0) {
            var4[0] = this.getParticleParam1();
         }

         if (var4.length > 1) {
            var4[1] = this.getParticleParam2();
         }

         if (var1) {
            if (this.rand.nextBoolean()) {
               for(int var5 = 0; var5 < 2; ++var5) {
                  float var6 = this.rand.nextFloat() * 6.2831855F;
                  float var7 = MathHelper.sqrt(this.rand.nextFloat()) * 0.2F;
                  float var8 = MathHelper.cos(var6) * var7;
                  float var9 = MathHelper.sin(var6) * var7;
                  if (var3 == EnumParticleTypes.SPELL_MOB) {
                     int var10 = this.rand.nextBoolean() ? 16777215 : this.getColor();
                     int var11 = var10 >> 16 & 255;
                     int var12 = var10 >> 8 & 255;
                     int var13 = var10 & 255;
                     this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX + (double)var8, this.posY, this.posZ + (double)var9, (double)((float)var11 / 255.0F), (double)((float)var12 / 255.0F), (double)((float)var13 / 255.0F));
                  } else {
                     this.world.spawnParticle(var3, this.posX + (double)var8, this.posY, this.posZ + (double)var9, 0.0D, 0.0D, 0.0D, var4);
                  }
               }
            }
         } else {
            float var19 = 3.1415927F * var2 * var2;

            for(int var22 = 0; (float)var22 < var19; ++var22) {
               float var25 = this.rand.nextFloat() * 6.2831855F;
               float var28 = MathHelper.sqrt(this.rand.nextFloat()) * var2;
               float var30 = MathHelper.cos(var25) * var28;
               float var32 = MathHelper.sin(var25) * var28;
               if (var3 == EnumParticleTypes.SPELL_MOB) {
                  int var33 = this.getColor();
                  int var35 = var33 >> 16 & 255;
                  int var36 = var33 >> 8 & 255;
                  int var14 = var33 & 255;
                  this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX + (double)var30, this.posY, this.posZ + (double)var32, (double)((float)var35 / 255.0F), (double)((float)var36 / 255.0F), (double)((float)var14 / 255.0F));
               } else {
                  this.world.spawnParticle(var3, this.posX + (double)var30, this.posY, this.posZ + (double)var32, (0.5D - this.rand.nextDouble()) * 0.15D, 0.009999999776482582D, (0.5D - this.rand.nextDouble()) * 0.15D, var4);
               }
            }
         }
      } else {
         if (this.ticksExisted >= this.waitTime + this.duration) {
            this.setDead();
            return;
         }

         boolean var17 = this.ticksExisted < this.waitTime;
         if (var1 != var17) {
            this.setIgnoreRadius(var17);
         }

         if (var17) {
            return;
         }

         if (this.radiusPerTick != 0.0F) {
            var2 += this.radiusPerTick;
            if (var2 < 0.5F) {
               this.setDead();
               return;
            }

            this.setRadius(var2);
         }

         if (this.ticksExisted % 5 == 0) {
            Iterator var18 = this.reapplicationDelayMap.entrySet().iterator();

            while(var18.hasNext()) {
               Entry var20 = (Entry)var18.next();
               if (this.ticksExisted >= ((Integer)var20.getValue()).intValue()) {
                  var18.remove();
               }
            }

            ArrayList var21 = Lists.newArrayList();

            for(PotionEffect var26 : this.potion.getEffects()) {
               var21.add(new PotionEffect(var26.getPotion(), var26.getDuration() / 4, var26.getAmplifier(), var26.getIsAmbient(), var26.doesShowParticles()));
            }

            var21.addAll(this.effects);
            if (var21.isEmpty()) {
               this.reapplicationDelayMap.clear();
            } else {
               List var24 = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox());
               if (!var24.isEmpty()) {
                  for(EntityLivingBase var29 : var24) {
                     if (!this.reapplicationDelayMap.containsKey(var29) && var29.canBeHitWithPotion()) {
                        double var31 = var29.posX - this.posX;
                        double var34 = var29.posZ - this.posZ;
                        double var37 = var31 * var31 + var34 * var34;
                        if (var37 <= (double)(var2 * var2)) {
                           this.reapplicationDelayMap.put(var29, Integer.valueOf(this.ticksExisted + this.reapplicationDelay));

                           for(PotionEffect var16 : var21) {
                              if (var16.getPotion().isInstant()) {
                                 var16.getPotion().affectEntity(this, this.getOwner(), var29, var16.getAmplifier(), 0.5D);
                              } else {
                                 var29.addPotionEffect(new PotionEffect(var16));
                              }
                           }

                           if (this.radiusOnUse != 0.0F) {
                              var2 += this.radiusOnUse;
                              if (var2 < 0.5F) {
                                 this.setDead();
                                 return;
                              }

                              this.setRadius(var2);
                           }

                           if (this.durationOnUse != 0) {
                              this.duration += this.durationOnUse;
                              if (this.duration <= 0) {
                                 this.setDead();
                                 return;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

   }

   public void setRadiusOnUse(float var1) {
      this.radiusOnUse = var1;
   }

   public void setRadiusPerTick(float var1) {
      this.radiusPerTick = var1;
   }

   public void setWaitTime(int var1) {
      this.waitTime = var1;
   }

   public void setOwner(@Nullable EntityLivingBase var1) {
      this.owner = var1;
      this.ownerUniqueId = var1 == null ? null : var1.getUniqueID();
   }

   @Nullable
   public EntityLivingBase getOwner() {
      if (this.owner == null && this.ownerUniqueId != null && this.world instanceof WorldServer) {
         Entity var1 = ((WorldServer)this.world).getEntityFromUuid(this.ownerUniqueId);
         if (var1 instanceof EntityLivingBase) {
            this.owner = (EntityLivingBase)var1;
         }
      }

      return this.owner;
   }

   protected void readEntityFromNBT(NBTTagCompound var1) {
      this.ticksExisted = var1.getInteger("Age");
      this.duration = var1.getInteger("Duration");
      this.waitTime = var1.getInteger("WaitTime");
      this.reapplicationDelay = var1.getInteger("ReapplicationDelay");
      this.durationOnUse = var1.getInteger("DurationOnUse");
      this.radiusOnUse = var1.getFloat("RadiusOnUse");
      this.radiusPerTick = var1.getFloat("RadiusPerTick");
      this.setRadius(var1.getFloat("Radius"));
      this.ownerUniqueId = var1.getUniqueId("OwnerUUID");
      if (var1.hasKey("Particle", 8)) {
         EnumParticleTypes var2 = EnumParticleTypes.getByName(var1.getString("Particle"));
         if (var2 != null) {
            this.setParticle(var2);
            this.setParticleParam1(var1.getInteger("ParticleParam1"));
            this.setParticleParam2(var1.getInteger("ParticleParam2"));
         }
      }

      if (var1.hasKey("Color", 99)) {
         this.setColor(var1.getInteger("Color"));
      }

      if (var1.hasKey("Potion", 8)) {
         this.setPotion(PotionUtils.getPotionTypeFromNBT(var1));
      }

      if (var1.hasKey("Effects", 9)) {
         NBTTagList var5 = var1.getTagList("Effects", 10);
         this.effects.clear();

         for(int var3 = 0; var3 < var5.tagCount(); ++var3) {
            PotionEffect var4 = PotionEffect.readCustomPotionEffectFromNBT(var5.getCompoundTagAt(var3));
            if (var4 != null) {
               this.addEffect(var4);
            }
         }
      }

   }

   protected void writeEntityToNBT(NBTTagCompound var1) {
      var1.setInteger("Age", this.ticksExisted);
      var1.setInteger("Duration", this.duration);
      var1.setInteger("WaitTime", this.waitTime);
      var1.setInteger("ReapplicationDelay", this.reapplicationDelay);
      var1.setInteger("DurationOnUse", this.durationOnUse);
      var1.setFloat("RadiusOnUse", this.radiusOnUse);
      var1.setFloat("RadiusPerTick", this.radiusPerTick);
      var1.setFloat("Radius", this.getRadius());
      var1.setString("Particle", this.getParticle().getParticleName());
      var1.setInteger("ParticleParam1", this.getParticleParam1());
      var1.setInteger("ParticleParam2", this.getParticleParam2());
      if (this.ownerUniqueId != null) {
         var1.setUniqueId("OwnerUUID", this.ownerUniqueId);
      }

      if (this.colorSet) {
         var1.setInteger("Color", this.getColor());
      }

      if (this.potion != PotionTypes.EMPTY && this.potion != null) {
         var1.setString("Potion", ((ResourceLocation)PotionType.REGISTRY.getNameForObject(this.potion)).toString());
      }

      if (!this.effects.isEmpty()) {
         NBTTagList var2 = new NBTTagList();

         for(PotionEffect var4 : this.effects) {
            var2.appendTag(var4.writeCustomPotionEffectToNBT(new NBTTagCompound()));
         }

         var1.setTag("Effects", var2);
      }

   }

   public void notifyDataManagerChange(DataParameter var1) {
      if (RADIUS.equals(var1)) {
         this.setRadius(this.getRadius());
      }

      super.notifyDataManagerChange(var1);
   }

   public EnumPushReaction getPushReaction() {
      return EnumPushReaction.IGNORE;
   }
}
