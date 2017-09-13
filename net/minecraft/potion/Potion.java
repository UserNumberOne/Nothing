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
   public static Potion getPotionById(int var0) {
      return (Potion)REGISTRY.getObjectById(var0);
   }

   public static int getIdFromPotion(Potion var0) {
      return REGISTRY.getIDForObject(var0);
   }

   @Nullable
   public static Potion getPotionFromResourceLocation(String var0) {
      return (Potion)REGISTRY.getObject(new ResourceLocation(var0));
   }

   protected Potion(boolean var1, int var2) {
      this.isBadEffect = var1;
      if (var1) {
         this.effectiveness = 0.5D;
      } else {
         this.effectiveness = 1.0D;
      }

      this.liquidColor = var2;
   }

   protected Potion setIconIndex(int var1, int var2) {
      this.statusIconIndex = var1 + var2 * 8;
      return this;
   }

   public void performEffect(EntityLivingBase var1, int var2) {
      if (this == MobEffects.REGENERATION) {
         if (var1.getHealth() < var1.getMaxHealth()) {
            var1.heal(1.0F, RegainReason.MAGIC_REGEN);
         }
      } else if (this == MobEffects.POISON) {
         if (var1.getHealth() > 1.0F) {
            var1.attackEntityFrom(CraftEventFactory.POISON, 1.0F);
         }
      } else if (this == MobEffects.WITHER) {
         var1.attackEntityFrom(DamageSource.wither, 1.0F);
      } else if (this == MobEffects.HUNGER && var1 instanceof EntityPlayer) {
         ((EntityPlayer)var1).addExhaustion(0.025F * (float)(var2 + 1));
      } else if (this == MobEffects.SATURATION && var1 instanceof EntityPlayer) {
         if (!var1.world.isRemote) {
            EntityPlayer var3 = (EntityPlayer)var1;
            int var4 = var3.getFoodStats().foodLevel;
            FoodLevelChangeEvent var5 = CraftEventFactory.callFoodLevelChangeEvent(var3, var2 + 1 + var4);
            if (!var5.isCancelled()) {
               var3.getFoodStats().addStats(var5.getFoodLevel() - var4, 1.0F);
            }

            ((EntityPlayerMP)var3).connection.sendPacket(new SPacketUpdateHealth(((EntityPlayerMP)var3).getBukkitEntity().getScaledHealth(), var3.getFoodStats().foodLevel, var3.getFoodStats().foodSaturationLevel));
         }
      } else if ((this != MobEffects.INSTANT_HEALTH || var1.isEntityUndead()) && (this != MobEffects.INSTANT_DAMAGE || !var1.isEntityUndead())) {
         if (this == MobEffects.INSTANT_DAMAGE && !var1.isEntityUndead() || this == MobEffects.INSTANT_HEALTH && var1.isEntityUndead()) {
            var1.attackEntityFrom(DamageSource.magic, (float)(6 << var2));
         }
      } else {
         var1.heal((float)Math.max(4 << var2, 0), RegainReason.MAGIC);
      }

   }

   public void affectEntity(@Nullable Entity var1, @Nullable Entity var2, EntityLivingBase var3, int var4, double var5) {
      if ((this != MobEffects.INSTANT_HEALTH || var3.isEntityUndead()) && (this != MobEffects.INSTANT_DAMAGE || !var3.isEntityUndead())) {
         if (this == MobEffects.INSTANT_DAMAGE && !var3.isEntityUndead() || this == MobEffects.INSTANT_HEALTH && var3.isEntityUndead()) {
            int var8 = (int)(var5 * (double)(6 << var4) + 0.5D);
            if (var1 == null) {
               var3.attackEntityFrom(DamageSource.magic, (float)var8);
            } else {
               var3.attackEntityFrom(DamageSource.causeIndirectMagicDamage(var1, var2), (float)var8);
            }
         }
      } else {
         int var7 = (int)(var5 * (double)(4 << var4) + 0.5D);
         var3.heal((float)var7, RegainReason.MAGIC);
      }

   }

   public boolean isReady(int var1, int var2) {
      if (this == MobEffects.REGENERATION) {
         int var5 = 50 >> var2;
         return var5 > 0 ? var1 % var5 == 0 : true;
      } else if (this == MobEffects.POISON) {
         int var4 = 25 >> var2;
         return var4 > 0 ? var1 % var4 == 0 : true;
      } else if (this == MobEffects.WITHER) {
         int var3 = 40 >> var2;
         return var3 > 0 ? var1 % var3 == 0 : true;
      } else {
         return this == MobEffects.HUNGER;
      }
   }

   public boolean isInstant() {
      return false;
   }

   public Potion setPotionName(String var1) {
      this.name = var1;
      return this;
   }

   public String getName() {
      return this.name;
   }

   protected Potion setEffectiveness(double var1) {
      this.effectiveness = var1;
      return this;
   }

   public int getLiquidColor() {
      return this.liquidColor;
   }

   public Potion registerPotionAttributeModifier(IAttribute var1, String var2, double var3, int var5) {
      AttributeModifier var6 = new AttributeModifier(UUID.fromString(var2), this.getName(), var3, var5);
      this.attributeModifierMap.put(var1, var6);
      return this;
   }

   public void removeAttributesModifiersFromEntity(EntityLivingBase var1, AbstractAttributeMap var2, int var3) {
      for(Entry var5 : this.attributeModifierMap.entrySet()) {
         IAttributeInstance var6 = var2.getAttributeInstance((IAttribute)var5.getKey());
         if (var6 != null) {
            var6.removeModifier((AttributeModifier)var5.getValue());
         }
      }

   }

   public void applyAttributesModifiersToEntity(EntityLivingBase var1, AbstractAttributeMap var2, int var3) {
      for(Entry var5 : this.attributeModifierMap.entrySet()) {
         IAttributeInstance var6 = var2.getAttributeInstance((IAttribute)var5.getKey());
         if (var6 != null) {
            AttributeModifier var7 = (AttributeModifier)var5.getValue();
            var6.removeModifier(var7);
            var6.applyModifier(new AttributeModifier(var7.getID(), this.getName() + " " + var3, this.getAttributeModifierAmount(var3, var7), var7.getOperation()));
         }
      }

   }

   public double getAttributeModifierAmount(int var1, AttributeModifier var2) {
      return var2.getAmount() * (double)(var1 + 1);
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

      for(Object var1 : REGISTRY) {
         PotionEffectType.registerPotionEffectType(new CraftPotionEffectType((Potion)var1));
      }

   }
}
