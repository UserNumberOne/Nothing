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

   public AnimalChest(String s, int i) {
      super(s, false, i);
   }

   public AnimalChest(String s, int i, EntityHorse horse) {
      super(s, false, i, (CraftHorse)horse.getBukkitEntity());
      this.horse = horse;
   }

   public ItemStack[] getContents() {
      return this.inventoryContents;
   }

   public void onOpen(CraftHumanEntity who) {
      this.transaction.add(who);
   }

   public void onClose(CraftHumanEntity who) {
      this.transaction.remove(who);
   }

   public List getViewers() {
      return this.transaction;
   }

   public InventoryHolder getOwner() {
      return (Horse)this.horse.getBukkitEntity();
   }

   public void setMaxStackSize(int size) {
      this.maxStack = size;
   }

   public int getInventoryStackLimit() {
      return this.maxStack;
   }
}
