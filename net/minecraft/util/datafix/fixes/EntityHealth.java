package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

public class EntityHealth implements IFixableData {
   private static final Set ENTITY_LIST = Sets.newHashSet(new String[]{"ArmorStand", "Bat", "Blaze", "CaveSpider", "Chicken", "Cow", "Creeper", "EnderDragon", "Enderman", "Endermite", "EntityHorse", "Ghast", "Giant", "Guardian", "LavaSlime", "MushroomCow", "Ozelot", "Pig", "PigZombie", "Rabbit", "Sheep", "Shulker", "Silverfish", "Skeleton", "Slime", "SnowMan", "Spider", "Squid", "Villager", "VillagerGolem", "Witch", "WitherBoss", "Wolf", "Zombie"});

   public int getFixVersion() {
      return 109;
   }

   public NBTTagCompound fixTagCompound(NBTTagCompound var1) {
      if (ENTITY_LIST.contains(var1.getString("id"))) {
         float var2;
         if (var1.hasKey("HealF", 99)) {
            var2 = var1.getFloat("HealF");
            var1.removeTag("HealF");
         } else {
            if (!var1.hasKey("Health", 99)) {
               return var1;
            }

            var2 = var1.getFloat("Health");
         }

         var1.setFloat("Health", var2);
      }

      return var1;
   }
}
