package net.minecraft.util.datafix.fixes;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

public class HorseSaddle implements IFixableData {
   public int getFixVersion() {
      return 110;
   }

   public NBTTagCompound fixTagCompound(NBTTagCompound var1) {
      if ("EntityHorse".equals(var1.getString("id")) && !var1.hasKey("SaddleItem", 10) && var1.getBoolean("Saddle")) {
         NBTTagCompound var2 = new NBTTagCompound();
         var2.setString("id", "minecraft:saddle");
         var2.setByte("Count", (byte)1);
         var2.setShort("Damage", (short)0);
         var1.setTag("SaddleItem", var2);
         var1.removeTag("Saddle");
      }

      return var1;
   }
}
