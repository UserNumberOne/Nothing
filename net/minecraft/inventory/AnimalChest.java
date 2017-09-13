package net.minecraft.inventory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.item.ItemStack;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftHorse;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftHumanEntity;
import org.bukkit.entity.Horse;
import org.bukkit.inventory.InventoryHolder;

public class AnimalChest extends InventoryBasic {
   public List transaction = new ArrayList();
   private EntityHorse horse;
   private int maxStack = 64;

   public AnimalChest(String var1, int var2) {
      super(var1, false, var2);
   }

   public AnimalChest(String var1, int var2, EntityHorse var3) {
      super(var1, false, var2, (CraftHorse)var3.getBukkitEntity());
      this.horse = var3;
   }

   public ItemStack[] getContents() {
      return this.inventoryContents;
   }

   public void onOpen(CraftHumanEntity var1) {
      this.transaction.add(var1);
   }

   public void onClose(CraftHumanEntity var1) {
      this.transaction.remove(var1);
   }

   public List getViewers() {
      return this.transaction;
   }

   public InventoryHolder getOwner() {
      return (Horse)this.horse.getBukkitEntity();
   }

   public void setMaxStackSize(int var1) {
      this.maxStack = var1;
   }

   public int getInventoryStackLimit() {
      return this.maxStack;
   }
}
