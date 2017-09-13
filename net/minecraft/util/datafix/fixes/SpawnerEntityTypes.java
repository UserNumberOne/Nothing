package net.minecraft.util.datafix.fixes;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.datafix.IFixableData;

public class SpawnerEntityTypes implements IFixableData {
   public int getFixVersion() {
      return 107;
   }

   public NBTTagCompound fixTagCompound(NBTTagCompound var1) {
      if (!"MobSpawner".equals(var1.getString("id"))) {
         return var1;
      } else {
         if (var1.hasKey("EntityId", 8)) {
            String var2 = var1.getString("EntityId");
            NBTTagCompound var3 = var1.getCompoundTag("SpawnData");
            var3.setString("id", var2.isEmpty() ? "Pig" : var2);
            var1.setTag("SpawnData", var3);
            var1.removeTag("EntityId");
         }

         if (var1.hasKey("SpawnPotentials", 9)) {
            NBTTagList var6 = var1.getTagList("SpawnPotentials", 10);

            for(int var7 = 0; var7 < var6.tagCount(); ++var7) {
               NBTTagCompound var4 = var6.getCompoundTagAt(var7);
               if (var4.hasKey("Type", 8)) {
                  NBTTagCompound var5 = var4.getCompoundTag("Properties");
                  var5.setString("id", var4.getString("Type"));
                  var4.setTag("Entity", var5);
                  var4.removeTag("Type");
                  var4.removeTag("Properties");
               }
            }
         }

         return var1;
      }
   }
}
