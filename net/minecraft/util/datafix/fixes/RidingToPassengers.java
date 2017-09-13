package net.minecraft.util.datafix.fixes;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.datafix.IFixableData;

public class RidingToPassengers implements IFixableData {
   public int getFixVersion() {
      return 135;
   }

   public NBTTagCompound fixTagCompound(NBTTagCompound var1) {
      while(compound.hasKey("Riding", 10)) {
         NBTTagCompound nbttagcompound = this.extractVehicle(compound);
         this.addPassengerToVehicle(compound, nbttagcompound);
         compound = nbttagcompound;
      }

      return compound;
   }

   protected void addPassengerToVehicle(NBTTagCompound var1, NBTTagCompound var2) {
      NBTTagList nbttaglist = new NBTTagList();
      nbttaglist.appendTag(p_188219_1_);
      p_188219_2_.setTag("Passengers", nbttaglist);
   }

   protected NBTTagCompound extractVehicle(NBTTagCompound var1) {
      NBTTagCompound nbttagcompound = p_188220_1_.getCompoundTag("Riding");
      p_188220_1_.removeTag("Riding");
      return nbttagcompound;
   }
}
