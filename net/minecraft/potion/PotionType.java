package net.minecraft.potion;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.init.MobEffects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespacedDefaultedByKey;

public class PotionType {
   private static final ResourceLocation WATER = new ResourceLocation("water");
   public static final RegistryNamespacedDefaultedByKey REGISTRY = new RegistryNamespacedDefaultedByKey(WATER);
   private static int nextPotionTypeId;
   private final String baseName;
   private final ImmutableList effects;

   public static int getID(PotionType var0) {
      return REGISTRY.getIDForObject(var0);
   }

   @Nullable
   public static PotionType getPotionTypeForName(String var0) {
      return (PotionType)REGISTRY.getObject(new ResourceLocation(var0));
   }

   public PotionType(PotionEffect... var1) {
      this((String)null, var1);
   }

   public PotionType(@Nullable String var1, PotionEffect... var2) {
      this.baseName = var1;
      this.effects = ImmutableList.copyOf(var2);
   }

   public String getNamePrefixed(String var1) {
      return this.baseName == null ? var1 + ((ResourceLocation)REGISTRY.getNameForObject(this)).getResourcePath() : var1 + this.baseName;
   }

   public List getEffects() {
      return this.effects;
   }

   public static void registerPotionTypes() {
      registerPotionType("empty", new PotionType(new PotionEffect[0]));
      registerPotionType("water", new PotionType(new PotionEffect[0]));
      registerPotionType("mundane", new PotionType(new PotionEffect[0]));
      registerPotionType("thick", new PotionType(new PotionEffect[0]));
      registerPotionType("awkward", new PotionType(new PotionEffect[0]));
      registerPotionType("night_vision", new PotionType(new PotionEffect[]{new PotionEffect(MobEffects.NIGHT_VISION, 3600)}));
      registerPotionType("long_night_vision", new PotionType("night_vision", new PotionEffect[]{new PotionEffect(MobEffects.NIGHT_VISION, 9600)}));
      registerPotionType("invisibility", new PotionType(new PotionEffect[]{new PotionEffect(MobEffects.INVISIBILITY, 3600)}));
      registerPotionType("long_invisibility", new PotionType("invisibility", new PotionEffect[]{new PotionEffect(MobEffects.INVISIBILITY, 9600)}));
      registerPotionType("leaping", new PotionType(new PotionEffect[]{new PotionEffect(MobEffects.JUMP_BOOST, 3600)}));
      registerPotionType("long_leaping", new PotionType("leaping", new PotionEffect[]{new PotionEffect(MobEffects.JUMP_BOOST, 9600)}));
      registerPotionType("strong_leaping", new PotionType("leaping", new PotionEffect[]{new PotionEffect(MobEffects.JUMP_BOOST, 1800, 1)}));
      registerPotionType("fire_resistance", new PotionType(new PotionEffect[]{new PotionEffect(MobEffects.FIRE_RESISTANCE, 3600)}));
      registerPotionType("long_fire_resistance", new PotionType("fire_resistance", new PotionEffect[]{new PotionEffect(MobEffects.FIRE_RESISTANCE, 9600)}));
      registerPotionType("swiftness", new PotionType(new PotionEffect[]{new PotionEffect(MobEffects.SPEED, 3600)}));
      registerPotionType("long_swiftness", new PotionType("swiftness", new PotionEffect[]{new PotionEffect(MobEffects.SPEED, 9600)}));
      registerPotionType("strong_swiftness", new PotionType("swiftness", new PotionEffect[]{new PotionEffect(MobEffects.SPEED, 1800, 1)}));
      registerPotionType("slowness", new PotionType(new PotionEffect[]{new PotionEffect(MobEffects.SLOWNESS, 1800)}));
      registerPotionType("long_slowness", new PotionType("slowness", new PotionEffect[]{new PotionEffect(MobEffects.SLOWNESS, 4800)}));
      registerPotionType("water_breathing", new PotionType(new PotionEffect[]{new PotionEffect(MobEffects.WATER_BREATHING, 3600)}));
      registerPotionType("long_water_breathing", new PotionType("water_breathing", new PotionEffect[]{new PotionEffect(MobEffects.WATER_BREATHING, 9600)}));
      registerPotionType("healing", new PotionType(new PotionEffect[]{new PotionEffect(MobEffects.INSTANT_HEALTH, 1)}));
      registerPotionType("strong_healing", new PotionType("healing", new PotionEffect[]{new PotionEffect(MobEffects.INSTANT_HEALTH, 1, 1)}));
      registerPotionType("harming", new PotionType(new PotionEffect[]{new PotionEffect(MobEffects.INSTANT_DAMAGE, 1)}));
      registerPotionType("strong_harming", new PotionType("harming", new PotionEffect[]{new PotionEffect(MobEffects.INSTANT_DAMAGE, 1, 1)}));
      registerPotionType("poison", new PotionType(new PotionEffect[]{new PotionEffect(MobEffects.POISON, 900)}));
      registerPotionType("long_poison", new PotionType("poison", new PotionEffect[]{new PotionEffect(MobEffects.POISON, 1800)}));
      registerPotionType("strong_poison", new PotionType("poison", new PotionEffect[]{new PotionEffect(MobEffects.POISON, 432, 1)}));
      registerPotionType("regeneration", new PotionType(new PotionEffect[]{new PotionEffect(MobEffects.REGENERATION, 900)}));
      registerPotionType("long_regeneration", new PotionType("regeneration", new PotionEffect[]{new PotionEffect(MobEffects.REGENERATION, 1800)}));
      registerPotionType("strong_regeneration", new PotionType("regeneration", new PotionEffect[]{new PotionEffect(MobEffects.REGENERATION, 450, 1)}));
      registerPotionType("strength", new PotionType(new PotionEffect[]{new PotionEffect(MobEffects.STRENGTH, 3600)}));
      registerPotionType("long_strength", new PotionType("strength", new PotionEffect[]{new PotionEffect(MobEffects.STRENGTH, 9600)}));
      registerPotionType("strong_strength", new PotionType("strength", new PotionEffect[]{new PotionEffect(MobEffects.STRENGTH, 1800, 1)}));
      registerPotionType("weakness", new PotionType(new PotionEffect[]{new PotionEffect(MobEffects.WEAKNESS, 1800)}));
      registerPotionType("long_weakness", new PotionType("weakness", new PotionEffect[]{new PotionEffect(MobEffects.WEAKNESS, 4800)}));
      registerPotionType("luck", new PotionType("luck", new PotionEffect[]{new PotionEffect(MobEffects.LUCK, 6000)}));
      REGISTRY.validateKey();
   }

   protected static void registerPotionType(String var0, PotionType var1) {
      REGISTRY.register(nextPotionTypeId++, new ResourceLocation(var0), var1);
   }
}
