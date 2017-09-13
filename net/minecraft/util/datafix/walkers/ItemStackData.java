package net.minecraft.util.datafix.walkers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.IDataFixer;

public class ItemStackData extends Filtered {
   private final String[] matchingTags;

   public ItemStackData(String var1, String... var2) {
      super("id", var1);
      this.matchingTags = var2;
   }

   NBTTagCompound filteredProcess(IDataFixer var1, NBTTagCompound var2, int var3) {
      for(String var7 : this.matchingTags) {
         var2 = DataFixesManager.processItemStack(var1, var2, var3, var7);
      }

      return var2;
   }
}
