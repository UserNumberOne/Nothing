package net.minecraft.util;

import net.minecraft.nbt.NBTTagCompound;

public class WeightedSpawnerEntity extends WeightedRandom.Item {
   private final NBTTagCompound nbt;

   public WeightedSpawnerEntity() {
      super(1);
      this.nbt = new NBTTagCompound();
      this.nbt.setString("id", "Pig");
   }

   public WeightedSpawnerEntity(NBTTagCompound var1) {
      this(var1.hasKey("Weight", 99) ? var1.getInteger("Weight") : 1, var1.getCompoundTag("Entity"));
   }

   public WeightedSpawnerEntity(int var1, NBTTagCompound var2) {
      super(var1);
      this.nbt = var2;
   }

   public NBTTagCompound toCompoundTag() {
      NBTTagCompound var1 = new NBTTagCompound();
      var1.setTag("Entity", this.nbt);
      var1.setInteger("Weight", this.itemWeight);
      return var1;
   }

   public NBTTagCompound getNbt() {
      return this.nbt;
   }
}
