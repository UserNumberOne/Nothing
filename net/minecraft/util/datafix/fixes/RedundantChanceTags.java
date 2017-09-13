package net.minecraft.util.datafix.fixes;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.datafix.IFixableData;

public class RedundantChanceTags implements IFixableData {
   public int getFixVersion() {
      return 113;
   }

   public NBTTagCompound fixTagCompound(NBTTagCompound var1) {
      if (var1.hasKey("HandDropChances", 9)) {
         NBTTagList var2 = var1.getTagList("HandDropChances", 5);
         if (var2.tagCount() == 2 && var2.getFloatAt(0) == 0.0F && var2.getFloatAt(1) == 0.0F) {
            var1.removeTag("HandDropChances");
         }
      }

      if (var1.hasKey("ArmorDropChances", 9)) {
         NBTTagList var3 = var1.getTagList("ArmorDropChances", 5);
         if (var3.tagCount() == 4 && var3.getFloatAt(0) == 0.0F && var3.getFloatAt(1) == 0.0F && var3.getFloatAt(2) == 0.0F && var3.getFloatAt(3) == 0.0F) {
            var1.removeTag("ArmorDropChances");
         }
      }

      return var1;
   }
}
