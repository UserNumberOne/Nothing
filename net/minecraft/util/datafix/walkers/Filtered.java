package net.minecraft.util.datafix.walkers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;

public abstract class Filtered implements IDataWalker {
   private final String key;
   private final String value;

   public Filtered(String var1, String var2) {
      this.key = var1;
      this.value = var2;
   }

   public NBTTagCompound process(IDataFixer var1, NBTTagCompound var2, int var3) {
      if (var2.getString(this.key).equals(this.value)) {
         var2 = this.filteredProcess(var1, var2, var3);
      }

      return var2;
   }

   abstract NBTTagCompound filteredProcess(IDataFixer var1, NBTTagCompound var2, int var3);
}
