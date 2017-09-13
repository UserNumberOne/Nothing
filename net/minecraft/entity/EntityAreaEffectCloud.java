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
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;

public class EntityAreaEffectCloud extends Entity {
   private static final DataParameter RADIUS = EntityDataManager.createKey(EntityAreaEffectCloud.class, DataSerializers.FLOAT);
   private static final DataParameter COLOR = EntityDataManager.createKey(EntityAreaEffectCloud.class, DataSerializers.VARINT);
   private static final DataParameter IGNORE_RADIUS = EntityDataManager.createKey(EntityAreaEffectCloud.class, DataSerializers.BOOLEAN);
   private static final DataParameter PARTICLE = EntityDataManager.createKey(EntityAreaEffectCloud.class, DataSerializers.VARINT);
   private static final DataParameter PARTICLE_PARAM_1 = EntityDataManager.createKey(EntityAreaEffectCloud.class, DataSerializers.VARINT);
   private static final DataParameter PARTICLE_PARAM_2 = EntityDataManager.createKey(EntityAreaEffectCloud.class, DataSerializers.VARINT);
   private PotionType potion;
   public List effects;
   private final Map reapplicationDelayMap;
   private int duration;
   public int waitTime;
   public int reapplicationDelay;
   private boolean colorSet;
   public int durationOnUse;
   public float radiusOnUse;
   public float radiusPerTick;
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

   public void refreshEffects() {
      if (!this.colorSet) {
         this.getDataManager().set(COLOR, Integer.valueOf(PotionUtils.getPotionColorFromEffectList(PotionUtils.mergeEffects(this.potion, this.effects))));
      }

   }

   public String getType() {
      return ((ResourceLocation)PotionType.REGISTRY.getNameForObject(this.potion)).toString();
   }

   public void setType(String var1) {
      this.setPotion((PotionType)PotionType.REGISTRY.getObject(new ResourceLocation(var1)));
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
            float var24 = 3.1415927F * var2 * var2;

            for(int var26 = 0; (float)var26 < var24; ++var26) {
               float var28 = this.rand.nextFloat() * 6.2831855F;
               float var31 = MathHelper.sqrt(this.rand.nextFloat()) * var2;
               float var33 = MathHelper.cos(var28) * var31;
               float var36 = MathHelper.sin(var28) * var31;
               if (var3 == EnumParticleTypes.SPELL_MOB) {
                  int var38 = this.getColor();
                  int var40 = var38 >> 16 & 255;
                  int var42 = var38 >> 8 & 255;
                  int var14 = var38 & 255;
                  this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX + (double)var33, this.posY, this.posZ + (double)var36, (double)((float)var40 / 255.0F), (double)((float)var42 / 255.0F), (double)((float)var14 / 255.0F));
               } else {
                  this.world.spawnParticle(var3, this.posX + (double)var33, this.posY, this.posZ + (double)var36, (0.5D - this.rand.nextDouble()) * 0.15D, 0.009999999776482582D, (0.5D - this.rand.nextDouble()) * 0.15D, var4);
               }
            }
         }
      } else {
         if (this.ticksExisted >= this.waitTime + this.duration) {
            this.setDead();
            return;
         }

         boolean var22 = this.ticksExisted < this.waitTime;
         if (var1 != var22) {
            this.setIgnoreRadius(var22);
         }

         if (var22) {
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
            Iterator var23 = this.reapplicationDelayMap.entrySet().iterator();

            while(var23.hasNext()) {
               Entry var29 = (Entry)var23.next();
               if (this.ticksExisted >= ((Integer)var29.getValue()).intValue()) {
                  var23.remove();
               }
            }

            ArrayList var30 = Lists.newArrayList();

            for(PotionEffect var34 : this.potion.getEffects()) {
               var30.add(new PotionEffect(var34.getPotion(), var34.getDuration() / 4, var34.getAmplifier(), var34.getIsAmbient(), var34.doesShowParticles()));
            }

            var30.addAll(this.effects);
            if (var30.isEmpty()) {
               this.reapplicationDelayMap.clear();
            } else {
               List var35 = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox());
               if (!var35.isEmpty()) {
                  Iterator var39 = var35.iterator();
                  ArrayList var41 = new ArrayList();

                  while(var39.hasNext()) {
                     EntityLivingBase var43 = (EntityLivingBase)var39.next();
                     if (!this.reapplicationDelayMap.containsKey(var43) && var43.canBeHitWithPotion()) {
                        double var15 = var43.posX - this.posX;
                        double var17 = var43.posZ - this.posZ;
                        double var19 = var15 * var15 + var17 * var17;
                        if (var19 <= (double)(var2 * var2)) {
                           var41.add((LivingEntity)var43.getBukkitEntity());
                        }
                     }
                  }

                  AreaEffectCloudApplyEvent var44 = CraftEventFactory.callAreaEffectCloudApplyEvent(this, var41);

                  for(LivingEntity var25 : var44.getAffectedEntities()) {
                     if (var25 instanceof CraftLivingEntity) {
                        EntityLivingBase var37 = ((CraftLivingEntity)var25).getHandle();
                        this.reapplicationDelayMap.put(var37, Integer.valueOf(this.ticksExisted + this.reapplicationDelay));

                        for(PotionEffect var21 : var30) {
                           if (var21.getPotion().isInstant()) {
                              var21.getPotion().affectEntity(this, this.getOwner(), var37, var21.getAmplifier(), 0.5D);
                           } else {
                              var37.addPotionEffect(new PotionEffect(var21));
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
