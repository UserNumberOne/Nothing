package net.minecraft.util.datafix.fixes;

import java.util.Random;
import net.minecraft.entity.monster.ZombieType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

public class ZombieProfToType implements IFixableData {
   private static final Random RANDOM = new Random();

   public int getFixVersion() {
      return 502;
   }

   public NBTTagCompound fixTagCompound(NBTTagCompound var1) {
      if ("Zombie".equals(var1.getString("id")) && var1.getBoolean("IsVillager")) {
         if (!var1.hasKey("ZombieType", 99)) {
            ZombieType var2 = null;
            if (var1.hasKey("VillagerProfession", 99)) {
               try {
                  var2 = ZombieType.getByOrdinal(var1.getInteger("VillagerProfession") + 1);
               } catch (RuntimeException var4) {
                  ;
               }
            }

            if (var2 == null) {
               var2 = ZombieType.getByOrdinal(RANDOM.nextInt(5) + 1);
            }

            var1.setInteger("ZombieType", var2.getId());
         }

         var1.removeTag("IsVillager");
      }

      return var1;
   }
}
