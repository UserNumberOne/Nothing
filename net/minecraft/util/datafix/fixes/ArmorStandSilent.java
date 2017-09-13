package net.minecraft.util.datafix.fixes;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

public class ArmorStandSilent implements IFixableData {
   public int getFixVersion() {
      return 147;
   }

   public NBTTagCompound fixTagCompound(NBTTagCompound var1) {
      if ("ArmorStand".equals(var1.getString("id")) && var1.getBoolean("Silent") && !var1.getBoolean("Marker")) {
         var1.removeTag("Silent");
      }

      return var1;
   }
}
