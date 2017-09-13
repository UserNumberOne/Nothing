package net.minecraft.util.datafix.walkers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.IDataFixer;

public class ItemStackDataLists extends Filtered {
   private final String[] matchingTags;

   public ItemStackDataLists(String var1, String... var2) {
      super("id", id);
      this.matchingTags = tags;
   }

   NBTTagCompound filteredProcess(IDataFixer var1, NBTTagCompound var2, int var3) {
      for(String s : this.matchingTags) {
         compound = DataFixesManager.processInventory(fixer, compound, versionIn, s);
      }

      return compound;
   }
}
