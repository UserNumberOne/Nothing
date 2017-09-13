package net.minecraft.util.datafix.fixes;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

public class SpawnEggNames implements IFixableData {
   private static final String[] ENTITY_IDS = new String[256];

   public int getFixVersion() {
      return 105;
   }

   public NBTTagCompound fixTagCompound(NBTTagCompound var1) {
      if ("minecraft:spawn_egg".equals(var1.getString("id"))) {
         NBTTagCompound var2 = var1.getCompoundTag("tag");
         NBTTagCompound var3 = var2.getCompoundTag("EntityTag");
         short var4 = var1.getShort("Damage");
         if (!var3.hasKey("id", 8)) {
            String var5 = ENTITY_IDS[var4 & 255];
            if (var5 != null) {
               var3.setString("id", var5);
               var2.setTag("EntityTag", var3);
               var1.setTag("tag", var2);
            }
         }

         if (var4 != 0) {
            var1.setShort("Damage", (short)0);
         }
      }

      return var1;
   }

   static {
      String[] var0 = ENTITY_IDS;
      var0[1] = "Item";
      var0[2] = "XPOrb";
      var0[7] = "ThrownEgg";
      var0[8] = "LeashKnot";
      var0[9] = "Painting";
      var0[10] = "Arrow";
      var0[11] = "Snowball";
      var0[12] = "Fireball";
      var0[13] = "SmallFireball";
      var0[14] = "ThrownEnderpearl";
      var0[15] = "EyeOfEnderSignal";
      var0[16] = "ThrownPotion";
      var0[17] = "ThrownExpBottle";
      var0[18] = "ItemFrame";
      var0[19] = "WitherSkull";
      var0[20] = "PrimedTnt";
      var0[21] = "FallingSand";
      var0[22] = "FireworksRocketEntity";
      var0[23] = "TippedArrow";
      var0[24] = "SpectralArrow";
      var0[25] = "ShulkerBullet";
      var0[26] = "DragonFireball";
      var0[30] = "ArmorStand";
      var0[41] = "Boat";
      var0[42] = "MinecartRideable";
      var0[43] = "MinecartChest";
      var0[44] = "MinecartFurnace";
      var0[45] = "MinecartTNT";
      var0[46] = "MinecartHopper";
      var0[47] = "MinecartSpawner";
      var0[40] = "MinecartCommandBlock";
      var0[48] = "Mob";
      var0[49] = "Monster";
      var0[50] = "Creeper";
      var0[51] = "Skeleton";
      var0[52] = "Spider";
      var0[53] = "Giant";
      var0[54] = "Zombie";
      var0[55] = "Slime";
      var0[56] = "Ghast";
      var0[57] = "PigZombie";
      var0[58] = "Enderman";
      var0[59] = "CaveSpider";
      var0[60] = "Silverfish";
      var0[61] = "Blaze";
      var0[62] = "LavaSlime";
      var0[63] = "EnderDragon";
      var0[64] = "WitherBoss";
      var0[65] = "Bat";
      var0[66] = "Witch";
      var0[67] = "Endermite";
      var0[68] = "Guardian";
      var0[69] = "Shulker";
      var0[90] = "Pig";
      var0[91] = "Sheep";
      var0[92] = "Cow";
      var0[93] = "Chicken";
      var0[94] = "Squid";
      var0[95] = "Wolf";
      var0[96] = "MushroomCow";
      var0[97] = "SnowMan";
      var0[98] = "Ozelot";
      var0[99] = "VillagerGolem";
      var0[100] = "EntityHorse";
      var0[101] = "Rabbit";
      var0[120] = "Villager";
      var0[200] = "EnderCrystal";
   }
}
