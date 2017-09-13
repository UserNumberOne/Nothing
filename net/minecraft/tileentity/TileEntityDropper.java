package net.minecraft.tileentity;

import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;

public class TileEntityDropper extends TileEntityDispenser {
   public static void registerFixesDropper(DataFixer var0) {
      fixer.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists("Dropper", new String[]{"Items"}));
   }

   public String getName() {
      return this.hasCustomName() ? this.customName : "container.dropper";
   }

   public String getGuiID() {
      return "minecraft:dropper";
   }
}
