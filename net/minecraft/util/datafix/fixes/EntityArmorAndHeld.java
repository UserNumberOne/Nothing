package net.minecraft.util.datafix.fixes;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.datafix.IFixableData;

public class EntityArmorAndHeld implements IFixableData {
   public int getFixVersion() {
      return 100;
   }

   public NBTTagCompound fixTagCompound(NBTTagCompound var1) {
      NBTTagList var2 = var1.getTagList("Equipment", 10);
      if (var2.tagCount() > 0 && !var1.hasKey("HandItems", 10)) {
         NBTTagList var3 = new NBTTagList();
         var3.appendTag(var2.get(0));
         var3.appendTag(new NBTTagCompound());
         var1.setTag("HandItems", var3);
      }

      if (var2.tagCount() > 1 && !var1.hasKey("ArmorItem", 10)) {
         NBTTagList var5 = new NBTTagList();
         var5.appendTag(var2.getCompoundTagAt(1));
         var5.appendTag(var2.getCompoundTagAt(2));
         var5.appendTag(var2.getCompoundTagAt(3));
         var5.appendTag(var2.getCompoundTagAt(4));
         var1.setTag("ArmorItems", var5);
      }

      var1.removeTag("Equipment");
      if (var1.hasKey("DropChances", 9)) {
         NBTTagList var6 = var1.getTagList("DropChances", 5);
         if (!var1.hasKey("HandDropChances", 10)) {
            NBTTagList var4 = new NBTTagList();
            var4.appendTag(new NBTTagFloat(var6.getFloatAt(0)));
            var4.appendTag(new NBTTagFloat(0.0F));
            var1.setTag("HandDropChances", var4);
         }

         if (!var1.hasKey("ArmorDropChances", 10)) {
            NBTTagList var7 = new NBTTagList();
            var7.appendTag(new NBTTagFloat(var6.getFloatAt(1)));
            var7.appendTag(new NBTTagFloat(var6.getFloatAt(2)));
            var7.appendTag(new NBTTagFloat(var6.getFloatAt(3)));
            var7.appendTag(new NBTTagFloat(var6.getFloatAt(4)));
            var1.setTag("ArmorDropChances", var7);
         }

         var1.removeTag("DropChances");
      }

      return var1;
   }
}
