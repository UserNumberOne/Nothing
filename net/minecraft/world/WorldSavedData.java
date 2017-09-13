package net.minecraft.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class WorldSavedData implements INBTSerializable {
   public final String mapName;
   private boolean dirty;

   public WorldSavedData(String var1) {
      this.mapName = var1;
   }

   public abstract void readFromNBT(NBTTagCompound var1);

   public abstract NBTTagCompound writeToNBT(NBTTagCompound var1);

   public void markDirty() {
      this.setDirty(true);
   }

   public void setDirty(boolean var1) {
      this.dirty = var1;
   }

   public boolean isDirty() {
      return this.dirty;
   }

   public void deserializeNBT(NBTTagCompound var1) {
      this.readFromNBT(var1);
   }

   public NBTTagCompound serializeNBT() {
      return this.writeToNBT(new NBTTagCompound());
   }
}
