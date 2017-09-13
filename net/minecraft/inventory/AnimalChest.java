package net.minecraft.inventory;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AnimalChest extends InventoryBasic {
   public AnimalChest(String var1, int var2) {
      super(inventoryName, false, slotCount);
   }

   @SideOnly(Side.CLIENT)
   public AnimalChest(ITextComponent var1, int var2) {
      super(invTitle, slotCount);
   }
}
