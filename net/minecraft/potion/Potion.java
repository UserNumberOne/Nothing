package net.minecraft.potion;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketUpdateHealth;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_10_R1.potion.CraftPotionEffectType;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.potion.PotionEffectType;

public class Potion {
   public static final RegistryNamespaced REGISTRY = new RegistryNamespaced();
   private final Map attributeModifierMap = Maps.newHashMap();
   private final boolean isBadEffect;
   private final int liquidColor;
   private String name = "";
   private int statusIconIndex = -1;
   public double effectiveness;
   private boolean beneficial;

   @Nullable
   public static Potion getPotionById(int i) {
      return (Potion)REGISTRY.getObjectById(i);
   }

   public static int getIdFromPotion(Potion mobeffectlist) {
      return REGISTRY.getIDForObject(mobeffectlist);
   }

   @Nullable
   public static Potion getPotionFromResourceLocation(String s) {
      return (Potion)REGISTRY.getObject(new ResourceLocation(s));
   }

   protected Potion(boolean flag, int i) {
      this.isBadEffect = flag;
      if (flag) {
         this.effectiveness = 0.5D;
      } else {
         this.effectiveness = 1.0D;
      }

      this.liquidColor = i;
   }

   protected Potion setIconIndex(int i, int j) {
      this.statusIconIndex = i + j * 8;
      return this;
   }

   public void performEffect(EntityLivingBase entityliving, int i) {
      if (this == MobEffects.REGENERATION) {
         if (entityliving.getHealth() < entityliving.getMaxHealth()) {
            entityliving.heal(1.0F, RegainReason.MAGIC_REGEN);
         }
      } else if (this == MobEffects.POISON) {
         if (entityliving.getHealth() > 1.0F) {
            entityliving.attackEntityFrom(CraftEventFactory.POISON, 1.0F);
         }
      } else if (this == MobEffects.WITHER) {
         entityliving.attackEntityFrom(DamageSource.wither, 1.0F);
      } else if (this == MobEffects.HUNGER && entityliving instanceof EntityPlayer) {
         ((EntityPlayer)entityliving).addExhaustion(0.025F * (float)(i + 1));
      } else if (this == MobEffects.SATURATION && entityliving instanceof EntityPlayer) {
         if (!entityliving.world.isRemote) {
            EntityPlayer entityhuman = (EntityPlayer)entityliving;
            int oldFoodLevel = entityhuman.getFoodStats().foodLevel;
            FoodLevelChangeEvent event = CraftEventFactory.callFoodLevelChangeEvent(entityhuman, i + 1 + oldFoodLevel);
            if (!event.isCancelled()) {
               entityhuman.getFoodStats().addStats(event.getFoodLevel() - oldFoodLevel, 1.0F);
            }

            ((EntityPlayerMP)entityhuman).connection.sendPacket(new SPacketUpdateHealth(((EntityPlayerMP)entityhuman).getBukkitEntity().getScaledHealth(), entityhuman.getFoodStats().foodLevel, entityhuman.getFoodStats().foodSaturationLevel));
         }
      } else if ((this != MobEffects.INSTANT_HEALTH || entityliving.isEntityUndead()) && (this != MobEffects.INSTANT_DAMAGE || !entityliving.isEntityUndead())) {
         if (this == MobEffects.INSTANT_DAMAGE && !entityliving.isEntityUndead() || this == MobEffects.INSTANT_HEALTH && entityliving.isEntityUndead()) {
            entityliving.attackEntityFrom(DamageSource.magic, (float)(6 << i));
         }
      } else {
         entityliving.heal((float)Math.max(4 << i, 0), RegainReason.MAGIC);
      }

   }

   public void affectEntity(@Nullable Entity entity, @Nullable Entity entity1, EntityLivingBase entityliving, int i, double d0) {
      if ((this != MobEffects.INSTANT_HEALTH || entityliving.isEntityUndead()) && (this != MobEffects.INSTANT_DAMAGE || !entityliving.isEntityUndead())) {
         if (this == MobEffects.INSTANT_DAMAGE && !entityliving.isEntityUndead() || this == MobEffects.INSTANT_HEALTH && entityliving.isEntityUndead()) {
            int j = (int)(d0 * (double)(6 << i) + 0.5D);
            if (entity == null) {
               entityliving.attackEntityFrom(DamageSource.magic, (float)j);
            } else {
               entityliving.attackEntityFrom(DamageSource.causeIndirectMagicDamage(entity, entity1), (float)j);
            }
         }
      } else {
         int j = (int)(d0 * (double)(4 << i) + 0.5D);
         entityliving.heal((float)j, RegainReason.MAGIC);
      }

   }

   public boolean isReady(int i, int j) {
      if (this == MobEffects.REGENERATION) {
         int k = 50 >> j;
         return k > 0 ? i % k == 0 : true;
      } else if (this == MobEffects.POISON) {
         int k = 25 >> j;
         return k > 0 ? i % k == 0 : true;
      } else if (this == MobEffects.WITHER) {
         int k = 40 >> j;
         return k > 0 ? i % k == 0 : true;
      } else {
         return this == MobEffects.HUNGER;
      }
   }

   public boolean isInstant() {
      return false;
   }

   public Potion setPotionName(String s) {
      this.name = s;
      return this;
   }

   public String getName() {
      return this.name;
   }

   protected Potion setEffectiveness(double d0) {
      this.effectiveness = d0;
      return this;
   }

   public int getLiquidColor() {
      return this.liquidColor;
   }

   public Potion registerPotionAttributeModifier(IAttribute iattribute, String s, double d0, int i) {
      AttributeModifier attributemodifier = new AttributeModifier(UUID.fromString(s), this.getName(), d0, i);
      this.attributeModifierMap.put(iattribute, attributemodifier);
      return this;
   }

   public void removeAttributesModifiersFromEntity(EntityLivingBase entityliving, AbstractAttributeMap attributemapbase, int i) {
      for(Entry entry : this.attributeModifierMap.entrySet()) {
         IAttributeInstance attributeinstance = attributemapbase.getAttributeInstance((IAttribute)entry.getKey());
         if (attributeinstance != null) {
            attributeinstance.removeModifier((AttributeModifier)entry.getValue());
         }
      }

   }

   public void applyAttributesModifiersToEntity(EntityLivingBase entityliving, AbstractAttributeMap attributemapbase, int i) {
      for(Entry entry : this.attributeModifierMap.entrySet()) {
         IAttributeInstance attributeinstance = attributemapbase.getAttributeInstance((IAttribute)entry.getKey());
         if (attributeinstance != null) {
            AttributeModifier attributemodifier = (AttributeModifier)entry.getValue();
            attributeinstance.removeModifier(attributemodifier);
            attributeinstance.applyModifier(new AttributeModifier(attributemodifier.getID(), this.getName() + " " + i, this.getAttributeModifierAmount(i, attributemodifier), attributemodifier.getOperation()));
         }
      }

   }

   public double getAttributeModifierAmount(int i, AttributeModifier attributemodifier) {
      return attributemodifier.getAmount() * (double)(i + 1);
   }

   public Potion setBeneficial() {
      this.beneficial = true;
      return this;
   }

   public static void registerPotions() {
      REGISTRY.register(1, new ResourceLocation("speed"), (new Potion(false, 8171462)).setPotionName("effect.moveSpeed").setIconIndex(0, 0).registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "91AEAA56-376B-4498-935B-2F7F68070635", 0.20000000298023224D, 2).setBeneficial());
      REGISTRY.register(2, new ResourceLocation("slowness"), (new Potion(true, 5926017)).setPotionName("effect.moveSlowdown").setIconIndex(1, 0).registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-514C1F160890", -0.15000000596046448D, 2));
      REGISTRY.register(3, new ResourceLocation("haste"), (new Potion(false, 14270531)).setPotionName("effect.digSpeed").setIconIndex(2, 0).setEffectiveness(1.5D).setBeneficial().registerPotionAttributeModifier(SharedMonsterAttributes.ATTACK_SPEED, "AF8B6E3F-3328-4C0A-AA36-5BA2BB9DBEF3", 0.10000000149011612D, 2));
      REGISTRY.register(4, new ResourceLocation("mining_fatigue"), (new Potion(true, 4866583)).setPotionName("effect.digSlowDown").setIconIndex(3, 0).registerPotionAttributeModifier(SharedMonsterAttributes.ATTACK_SPEED, "55FCED67-E92A-486E-9800-B47F202C4386", -0.10000000149011612D, 2));
      REGISTRY.register(5, new ResourceLocation("strength"), (new PotionAttackDamage(false, 9643043, 3.0D)).setPotionName("effect.damageBoost").setIconIndex(4, 0).registerPotionAttributeModifier(SharedMonsterAttributes.ATTACK_DAMAGE, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 0.0D, 0).setBeneficial());
      REGISTRY.register(6, new ResourceLocation("instant_health"), (new PotionHealth(false, 16262179)).setPotionName("effect.heal").setBeneficial());
      REGISTRY.register(7, new ResourceLocation("instant_damage"), (new PotionHealth(true, 4393481)).setPotionName("effect.harm").setBeneficial());
      REGISTRY.register(8, new ResourceLocation("jump_boost"), (new Potion(false, 2293580)).setPotionName("effect.jump").setIconIndex(2, 1).setBeneficial());
      REGISTRY.register(9, new ResourceLocation("nausea"), (new Potion(true, 5578058)).setPotionName("effect.confusion").setIconIndex(3, 1).setEffectiveness(0.25D));
      REGISTRY.register(10, new ResourceLocation("regeneration"), (new Potion(false, 13458603)).setPotionName("effect.regeneration").setIconIndex(7, 0).setEffectiveness(0.25D).setBeneficial());
      REGISTRY.register(11, new ResourceLocation("resistance"), (new Potion(false, 10044730)).setPotionName("effect.resistance").setIconIndex(6, 1).setBeneficial());
      REGISTRY.register(12, new ResourceLocation("fire_resistance"), (new Potion(false, 14981690)).setPotionName("effect.fireResistance").setIconIndex(7, 1).setBeneficial());
      REGISTRY.register(13, new ResourceLocation("water_breathing"), (new Potion(false, 3035801)).setPotionName("effect.waterBreathing").setIconIndex(0, 2).setBeneficial());
      REGISTRY.register(14, new ResourceLocation("invisibility"), (new Potion(false, 8356754)).setPotionName("effect.invisibility").setIconIndex(0, 1).setBeneficial());
      REGISTRY.register(15, new ResourceLocation("blindness"), (new Potion(true, 2039587)).setPotionName("effect.blindness").setIconIndex(5, 1).setEffectiveness(0.25D));
      REGISTRY.register(16, new ResourceLocation("night_vision"), (new Potion(false, 2039713)).setPotionName("effect.nightVision").setIconIndex(4, 1).setBeneficial());
      REGISTRY.register(17, new ResourceLocation("hunger"), (new Potion(true, 5797459)).setPotionName("effect.hunger").setIconIndex(1, 1));
      REGISTRY.register(18, new ResourceLocation("weakness"), (new PotionAttackDamage(true, 4738376, -4.0D)).setPotionName("effect.weakness").setIconIndex(5, 0).registerPotionAttributeModifier(SharedMonsterAttributes.ATTACK_DAMAGE, "22653B89-116E-49DC-9B6B-9971489B5BE5", 0.0D, 0));
      REGISTRY.register(19, new ResourceLocation("poison"), (new Potion(true, 5149489)).setPotionName("effect.poison").setIconIndex(6, 0).setEffectiveness(0.25D));
      REGISTRY.register(20, new ResourceLocation("wither"), (new Potion(true, 3484199)).setPotionName("effect.wither").setIconIndex(1, 2).setEffectiveness(0.25D));
      REGISTRY.register(21, new ResourceLocation("health_boost"), (new PotionHealthBoost(false, 16284963)).setPotionName("effect.healthBoost").setIconIndex(7, 2).registerPotionAttributeModifier(SharedMonsterAttributes.MAX_HEALTH, "5D6F0BA2-1186-46AC-B896-C61C5CEE99CC", 4.0D, 0).setBeneficial());
      REGISTRY.register(22, new ResourceLocation("absorption"), (new PotionAbsorption(false, 2445989)).setPotionName("effect.absorption").setIconIndex(2, 2).setBeneficial());
      REGISTRY.register(23, new ResourceLocation("saturation"), (new PotionHealth(false, 16262179)).setPotionName("effect.saturation").setBeneficial());
      REGISTRY.register(24, new ResourceLocation("glowing"), (new Potion(false, 9740385)).setPotionName("effect.glowing").setIconIndex(4, 2));
      REGISTRY.register(25, new ResourceLocation("levitation"), (new Potion(true, 13565951)).setPotionName("effect.levitation").setIconIndex(3, 2));
      REGISTRY.register(26, new ResourceLocation("luck"), (new Potion(false, 3381504)).setPotionName("effect.luck").setIconIndex(5, 2).setBeneficial().registerPotionAttributeModifier(SharedMonsterAttributes.LUCK, "03C3C89D-7037-4B42-869F-B146BCB64D2E", 1.0D, 0));
      REGISTRY.register(27, new ResourceLocation("unluck"), (new Potion(true, 12624973)).setPotionName("effect.unluck").setIconIndex(6, 2).registerPotionAttributeModifier(SharedMonsterAttributes.LUCK, "CC5AF142-2BD2-4215-B636-2605AED11727", -1.0D, 0));

      for(Object effect : REGISTRY) {
         PotionEffectType.registerPotionEffectType(new CraftPotionEffectType((Potion)effect));
      }

   }
}
