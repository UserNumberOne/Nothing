package net.minecraft.util.datafix.fixes;

import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

public class StringToUUID implements IFixableData {
   public int getFixVersion() {
      return 108;
   }

   public NBTTagCompound fixTagCompound(NBTTagCompound var1) {
      if (var1.hasKey("UUID", 8)) {
         var1.setUniqueId("UUID", UUID.fromString(var1.getString("UUID")));
      }

      return var1;
   }
}
