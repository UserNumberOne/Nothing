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

   public EntityAreaEffectCloud(World world) {
      super(world);
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

   public EntityAreaEffectCloud(World world, double d0, double d1, double d2) {
      this(world);
      this.setPosition(d0, d1, d2);
   }

   protected void entityInit() {
      this.getDataManager().register(COLOR, Integer.valueOf(0));
      this.getDataManager().register(RADIUS, Float.valueOf(0.5F));
      this.getDataManager().register(IGNORE_RADIUS, Boolean.valueOf(false));
      this.getDataManager().register(PARTICLE, Integer.valueOf(EnumParticleTypes.SPELL_MOB.getParticleID()));
      this.getDataManager().register(PARTICLE_PARAM_1, Integer.valueOf(0));
      this.getDataManager().register(PARTICLE_PARAM_2, Integer.valueOf(0));
   }

   public void setRadius(float f) {
      double d0 = this.posX;
      double d1 = this.posY;
      double d2 = this.posZ;
      this.setSize(f * 2.0F, 0.5F);
      this.setPosition(d0, d1, d2);
      if (!this.world.isRemote) {
         this.getDataManager().set(RADIUS, Float.valueOf(f));
      }

   }

   public float getRadius() {
      return ((Float)this.getDataManager().get(RADIUS)).floatValue();
   }

   public void setPotion(PotionType potionregistry) {
      this.potion = potionregistry;
      if (!this.colorSet) {
         if (potionregistry == PotionTypes.EMPTY && this.effects.isEmpty()) {
            this.getDataManager().set(COLOR, Integer.valueOf(0));
         } else {
            this.getDataManager().set(COLOR, Integer.valueOf(PotionUtils.getPotionColorFromEffectList(PotionUtils.mergeEffects(potionregistry, this.effects))));
         }
      }

   }

   public void addEffect(PotionEffect mobeffect) {
      this.effects.add(mobeffect);
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

   public void setType(String string) {
      this.setPotion((PotionType)PotionType.REGISTRY.getObject(new ResourceLocation(string)));
   }

   public int getColor() {
      return ((Integer)this.getDataManager().get(COLOR)).intValue();
   }

   public void setColor(int i) {
      this.colorSet = true;
      this.getDataManager().set(COLOR, Integer.valueOf(i));
   }

   public EnumParticleTypes getParticle() {
      return EnumParticleTypes.getParticleFromId(((Integer)this.getDataManager().get(PARTICLE)).intValue());
   }

   public void setParticle(EnumParticleTypes enumparticle) {
      this.getDataManager().set(PARTICLE, Integer.valueOf(enumparticle.getParticleID()));
   }

   public int getParticleParam1() {
      return ((Integer)this.getDataManager().get(PARTICLE_PARAM_1)).intValue();
   }

   public void setParticleParam1(int i) {
      this.getDataManager().set(PARTICLE_PARAM_1, Integer.valueOf(i));
   }

   public int getParticleParam2() {
      return ((Integer)this.getDataManager().get(PARTICLE_PARAM_2)).intValue();
   }

   public void setParticleParam2(int i) {
      this.getDataManager().set(PARTICLE_PARAM_2, Integer.valueOf(i));
   }

   protected void setIgnoreRadius(boolean flag) {
      this.getDataManager().set(IGNORE_RADIUS, Boolean.valueOf(flag));
   }

   public boolean shouldIgnoreRadius() {
      return ((Boolean)this.getDataManager().get(IGNORE_RADIUS)).booleanValue();
   }

   public int getDuration() {
      return this.duration;
   }

   public void setDuration(int i) {
      this.duration = i;
   }

   public void onUpdate() {
      super.onUpdate();
      boolean flag = this.shouldIgnoreRadius();
      float f = this.getRadius();
      if (this.world.isRemote) {
         EnumParticleTypes enumparticle = this.getParticle();
         int[] aint = new int[enumparticle.getArgumentCount()];
         if (aint.length > 0) {
            aint[0] = this.getParticleParam1();
         }

         if (aint.length > 1) {
            aint[1] = this.getParticleParam2();
         }

         if (flag) {
            if (this.rand.nextBoolean()) {
               for(int l = 0; l < 2; ++l) {
                  float f4 = this.rand.nextFloat() * 6.2831855F;
                  float f1 = MathHelper.sqrt(this.rand.nextFloat()) * 0.2F;
                  float f2 = MathHelper.cos(f4) * f1;
                  float f3 = MathHelper.sin(f4) * f1;
                  if (enumparticle == EnumParticleTypes.SPELL_MOB) {
                     int i1 = this.rand.nextBoolean() ? 16777215 : this.getColor();
                     int i = i1 >> 16 & 255;
                     int j = i1 >> 8 & 255;
                     int k = i1 & 255;
                     this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX + (double)f2, this.posY, this.posZ + (double)f3, (double)((float)i / 255.0F), (double)((float)j / 255.0F), (double)((float)k / 255.0F));
                  } else {
                     this.world.spawnParticle(enumparticle, this.posX + (double)f2, this.posY, this.posZ + (double)f3, 0.0D, 0.0D, 0.0D, aint);
                  }
               }
            }
         } else {
            float f5 = 3.1415927F * f * f;

            for(int j1 = 0; (float)j1 < f5; ++j1) {
               float f1 = this.rand.nextFloat() * 6.2831855F;
               float f2 = MathHelper.sqrt(this.rand.nextFloat()) * f;
               float f3 = MathHelper.cos(f1) * f2;
               float f6 = MathHelper.sin(f1) * f2;
               if (enumparticle == EnumParticleTypes.SPELL_MOB) {
                  int i = this.getColor();
                  int j = i >> 16 & 255;
                  int k = i >> 8 & 255;
                  int k1 = i & 255;
                  this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX + (double)f3, this.posY, this.posZ + (double)f6, (double)((float)j / 255.0F), (double)((float)k / 255.0F), (double)((float)k1 / 255.0F));
               } else {
                  this.world.spawnParticle(enumparticle, this.posX + (double)f3, this.posY, this.posZ + (double)f6, (0.5D - this.rand.nextDouble()) * 0.15D, 0.009999999776482582D, (0.5D - this.rand.nextDouble()) * 0.15D, aint);
               }
            }
         }
      } else {
         if (this.ticksExisted >= this.waitTime + this.duration) {
            this.setDead();
            return;
         }

         boolean flag1 = this.ticksExisted < this.waitTime;
         if (flag != flag1) {
            this.setIgnoreRadius(flag1);
         }

         if (flag1) {
            return;
         }

         if (this.radiusPerTick != 0.0F) {
            f += this.radiusPerTick;
            if (f < 0.5F) {
               this.setDead();
               return;
            }

            this.setRadius(f);
         }

         if (this.ticksExisted % 5 == 0) {
            Iterator iterator = this.reapplicationDelayMap.entrySet().iterator();

            while(iterator.hasNext()) {
               Entry entry = (Entry)iterator.next();
               if (this.ticksExisted >= ((Integer)entry.getValue()).intValue()) {
                  iterator.remove();
               }
            }

            ArrayList arraylist = Lists.newArrayList();

            for(PotionEffect mobeffect : this.potion.getEffects()) {
               arraylist.add(new PotionEffect(mobeffect.getPotion(), mobeffect.getDuration() / 4, mobeffect.getAmplifier(), mobeffect.getIsAmbient(), mobeffect.doesShowParticles()));
            }

            arraylist.addAll(this.effects);
            if (arraylist.isEmpty()) {
               this.reapplicationDelayMap.clear();
            } else {
               List list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox());
               if (!list.isEmpty()) {
                  Iterator iterator2 = list.iterator();
                  List entities = new ArrayList();

                  while(iterator2.hasNext()) {
                     EntityLivingBase entityliving = (EntityLivingBase)iterator2.next();
                     if (!this.reapplicationDelayMap.containsKey(entityliving) && entityliving.canBeHitWithPotion()) {
                        double d0 = entityliving.posX - this.posX;
                        double d1 = entityliving.posZ - this.posZ;
                        double d2 = d0 * d0 + d1 * d1;
                        if (d2 <= (double)(f * f)) {
                           entities.add((LivingEntity)entityliving.getBukkitEntity());
                        }
                     }
                  }

                  AreaEffectCloudApplyEvent event = CraftEventFactory.callAreaEffectCloudApplyEvent(this, entities);

                  for(LivingEntity entity : event.getAffectedEntities()) {
                     if (entity instanceof CraftLivingEntity) {
                        EntityLivingBase entityliving = ((CraftLivingEntity)entity).getHandle();
                        this.reapplicationDelayMap.put(entityliving, Integer.valueOf(this.ticksExisted + this.reapplicationDelay));

                        for(PotionEffect mobeffect1 : arraylist) {
                           if (mobeffect1.getPotion().isInstant()) {
                              mobeffect1.getPotion().affectEntity(this, this.getOwner(), entityliving, mobeffect1.getAmplifier(), 0.5D);
                           } else {
                              entityliving.addPotionEffect(new PotionEffect(mobeffect1));
                           }
                        }

                        if (this.radiusOnUse != 0.0F) {
                           f += this.radiusOnUse;
                           if (f < 0.5F) {
                              this.setDead();
                              return;
                           }

                           this.setRadius(f);
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

   public void setRadiusOnUse(float f) {
      this.radiusOnUse = f;
   }

   public void setRadiusPerTick(float f) {
      this.radiusPerTick = f;
   }

   public void setWaitTime(int i) {
      this.waitTime = i;
   }

   public void setOwner(@Nullable EntityLivingBase entityliving) {
      this.owner = entityliving;
      this.ownerUniqueId = entityliving == null ? null : entityliving.getUniqueID();
   }

   @Nullable
   public EntityLivingBase getOwner() {
      if (this.owner == null && this.ownerUniqueId != null && this.world instanceof WorldServer) {
         Entity entity = ((WorldServer)this.world).getEntityFromUuid(this.ownerUniqueId);
         if (entity instanceof EntityLivingBase) {
            this.owner = (EntityLivingBase)entity;
         }
      }

      return this.owner;
   }

   protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      this.ticksExisted = nbttagcompound.getInteger("Age");
      this.duration = nbttagcompound.getInteger("Duration");
      this.waitTime = nbttagcompound.getInteger("WaitTime");
      this.reapplicationDelay = nbttagcompound.getInteger("ReapplicationDelay");
      this.durationOnUse = nbttagcompound.getInteger("DurationOnUse");
      this.radiusOnUse = nbttagcompound.getFloat("RadiusOnUse");
      this.radiusPerTick = nbttagcompound.getFloat("RadiusPerTick");
      this.setRadius(nbttagcompound.getFloat("Radius"));
      this.ownerUniqueId = nbttagcompound.getUniqueId("OwnerUUID");
      if (nbttagcompound.hasKey("Particle", 8)) {
         EnumParticleTypes enumparticle = EnumParticleTypes.getByName(nbttagcompound.getString("Particle"));
         if (enumparticle != null) {
            this.setParticle(enumparticle);
            this.setParticleParam1(nbttagcompound.getInteger("ParticleParam1"));
            this.setParticleParam2(nbttagcompound.getInteger("ParticleParam2"));
         }
      }

      if (nbttagcompound.hasKey("Color", 99)) {
         this.setColor(nbttagcompound.getInteger("Color"));
      }

      if (nbttagcompound.hasKey("Potion", 8)) {
         this.setPotion(PotionUtils.getPotionTypeFromNBT(nbttagcompound));
      }

      if (nbttagcompound.hasKey("Effects", 9)) {
         NBTTagList nbttaglist = nbttagcompound.getTagList("Effects", 10);
         this.effects.clear();

         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            PotionEffect mobeffect = PotionEffect.readCustomPotionEffectFromNBT(nbttaglist.getCompoundTagAt(i));
            if (mobeffect != null) {
               this.addEffect(mobeffect);
            }
         }
      }

   }

   protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setInteger("Age", this.ticksExisted);
      nbttagcompound.setInteger("Duration", this.duration);
      nbttagcompound.setInteger("WaitTime", this.waitTime);
      nbttagcompound.setInteger("ReapplicationDelay", this.reapplicationDelay);
      nbttagcompound.setInteger("DurationOnUse", this.durationOnUse);
      nbttagcompound.setFloat("RadiusOnUse", this.radiusOnUse);
      nbttagcompound.setFloat("RadiusPerTick", this.radiusPerTick);
      nbttagcompound.setFloat("Radius", this.getRadius());
      nbttagcompound.setString("Particle", this.getParticle().getParticleName());
      nbttagcompound.setInteger("ParticleParam1", this.getParticleParam1());
      nbttagcompound.setInteger("ParticleParam2", this.getParticleParam2());
      if (this.ownerUniqueId != null) {
         nbttagcompound.setUniqueId("OwnerUUID", this.ownerUniqueId);
      }

      if (this.colorSet) {
         nbttagcompound.setInteger("Color", this.getColor());
      }

      if (this.potion != PotionTypes.EMPTY && this.potion != null) {
         nbttagcompound.setString("Potion", ((ResourceLocation)PotionType.REGISTRY.getNameForObject(this.potion)).toString());
      }

      if (!this.effects.isEmpty()) {
         NBTTagList nbttaglist = new NBTTagList();

         for(PotionEffect mobeffect : this.effects) {
            nbttaglist.appendTag(mobeffect.writeCustomPotionEffectToNBT(new NBTTagCompound()));
         }

         nbttagcompound.setTag("Effects", nbttaglist);
      }

   }

   public void notifyDataManagerChange(DataParameter datawatcherobject) {
      if (RADIUS.equals(datawatcherobject)) {
         this.setRadius(this.getRadius());
      }

      super.notifyDataManagerChange(datawatcherobject);
   }

   public EnumPushReaction getPushReaction() {
      return EnumPushReaction.IGNORE;
   }
}
