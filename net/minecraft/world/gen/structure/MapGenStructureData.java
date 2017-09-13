package net.minecraft.world.gen.structure;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;

public class MapGenStructureData extends WorldSavedData {
   private NBTTagCompound tagCompound = new NBTTagCompound();

   public MapGenStructureData(String var1) {
      super(name);
   }

   public void readFromNBT(NBTTagCompound var1) {
      this.tagCompound = nbt.getCompoundTag("Features");
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      compound.setTag("Features", this.tagCompound);
      return compound;
   }

   public void writeInstance(NBTTagCompound var1, int var2, int var3) {
      this.tagCompound.setTag(formatChunkCoords(chunkX, chunkZ), tagCompoundIn);
   }

   public static String formatChunkCoords(int var0, int var1) {
      return "[" + chunkX + "," + chunkZ + "]";
   }

   public NBTTagCompound getTagCompound() {
      return this.tagCompound;
   }
}
