package net.minecraft.util.datafix.fixes;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.datafix.IFixableData;

public class RidingToPassengers implements IFixableData {
   public int getFixVersion() {
      return 135;
   }

   public NBTTagCompound fixTagCompound(NBTTagCompound var1) {
      while(var1.hasKey("Riding", 10)) {
         NBTTagCompound var2 = this.extractVehicle(var1);
         this.addPassengerToVehicle(var1, var2);
         var1 = var2;
      }

      return var1;
   }

   protected void addPassengerToVehicle(NBTTagCompound var1, NBTTagCompound var2) {
      NBTTagList var3 = new NBTTagList();
      var3.appendTag(var1);
      var2.setTag("Passengers", var3);
   }

   protected NBTTagCompound extractVehicle(NBTTagCompound var1) {
      NBTTagCompound var2 = var1.getCompoundTag("Riding");
      var1.removeTag("Riding");
      return var2;
   }
}
