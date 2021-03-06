package net.minecraft.entity.passive;

import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.storage.loot.LootTableList;

public enum HorseType {
   HORSE("EntityHorse", "horse_white", SoundEvents.ENTITY_HORSE_AMBIENT, SoundEvents.ENTITY_HORSE_HURT, SoundEvents.ENTITY_HORSE_DEATH, LootTableList.ENTITIES_HORSE),
   DONKEY("Donkey", "donkey", SoundEvents.ENTITY_DONKEY_AMBIENT, SoundEvents.ENTITY_DONKEY_HURT, SoundEvents.ENTITY_DONKEY_DEATH, LootTableList.ENTITIES_HORSE),
   MULE("Mule", "mule", SoundEvents.ENTITY_MULE_AMBIENT, SoundEvents.ENTITY_MULE_HURT, SoundEvents.ENTITY_MULE_DEATH, LootTableList.ENTITIES_HORSE),
   ZOMBIE("ZombieHorse", "horse_zombie", SoundEvents.ENTITY_ZOMBIE_HORSE_AMBIENT, SoundEvents.ENTITY_ZOMBIE_HORSE_HURT, SoundEvents.ENTITY_ZOMBIE_HORSE_DEATH, LootTableList.ENTITIES_ZOMBIE_HORSE),
   SKELETON("SkeletonHorse", "horse_skeleton", SoundEvents.ENTITY_SKELETON_HORSE_AMBIENT, SoundEvents.ENTITY_SKELETON_HORSE_HURT, SoundEvents.ENTITY_SKELETON_HORSE_DEATH, LootTableList.ENTITIES_SKELETON_HORSE);

   private final TextComponentTranslation name;
   private final ResourceLocation texture;
   private final SoundEvent hurtSound;
   private final SoundEvent ambientSound;
   private final SoundEvent deathSound;
   private final ResourceLocation lootTable;

   private HorseType(String var3, String var4, SoundEvent var5, SoundEvent var6, SoundEvent var7, ResourceLocation var8) {
      this.name = new TextComponentTranslation("entity." + var3 + ".name", new Object[0]);
      this.texture = new ResourceLocation("textures/entity/horse/" + var4 + ".png");
      this.hurtSound = var6;
      this.ambientSound = var5;
      this.deathSound = var7;
      this.lootTable = var8;
   }

   public SoundEvent getAmbientSound() {
      return this.ambientSound;
   }

   public SoundEvent getHurtSound() {
      return this.hurtSound;
   }

   public SoundEvent getDeathSound() {
      return this.deathSound;
   }

   public TextComponentTranslation getDefaultName() {
      return this.name;
   }

   public boolean canBeChested() {
      return this == DONKEY || this == MULE;
   }

   public boolean hasMuleEars() {
      return this == DONKEY || this == MULE;
   }

   public boolean isUndead() {
      return this == ZOMBIE || this == SKELETON;
   }

   public boolean canMate() {
      return !this.isUndead() && this != MULE;
   }

   public boolean isHorse() {
      return this == HORSE;
   }

   public int getOrdinal() {
      return this.ordinal();
   }

   public static HorseType getArmorType(int var0) {
      return values()[var0];
   }

   public ResourceLocation getLootTable() {
      return this.lootTable;
   }
}
