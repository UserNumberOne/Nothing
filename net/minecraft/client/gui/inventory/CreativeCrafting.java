package net.minecraft.client.gui.inventory;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CreativeCrafting implements IContainerListener {
   private final Minecraft mc;

   public CreativeCrafting(Minecraft var1) {
      this.mc = mc;
   }

   public void updateCraftingInventory(Container var1, List var2) {
   }

   public void sendSlotContents(Container var1, int var2, ItemStack var3) {
      this.mc.playerController.sendSlotPacket(stack, slotInd);
   }

   public void sendProgressBarUpdate(Container var1, int var2, int var3) {
   }

   public void sendAllWindowProperties(Container var1, IInventory var2) {
   }
}
