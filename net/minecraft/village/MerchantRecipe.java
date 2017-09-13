package net.minecraft.village;

import javax.annotation.Nullable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftMerchantRecipe;

public class MerchantRecipe {
   public ItemStack itemToBuy;
   public ItemStack secondItemToBuy;
   public ItemStack itemToSell;
   public int toolUses;
   public int maxTradeUses;
   public boolean rewardsExp;
   private CraftMerchantRecipe bukkitHandle;

   public CraftMerchantRecipe asBukkit() {
      return this.bukkitHandle == null ? (this.bukkitHandle = new CraftMerchantRecipe(this)) : this.bukkitHandle;
   }

   public MerchantRecipe(ItemStack itemstack, ItemStack itemstack1, ItemStack itemstack2, int i, int j, CraftMerchantRecipe bukkit) {
      this(itemstack, itemstack1, itemstack2, i, j);
      this.bukkitHandle = bukkit;
   }

   public MerchantRecipe(NBTTagCompound nbttagcompound) {
      this.readFromTags(nbttagcompound);
   }

   public MerchantRecipe(ItemStack itemstack, @Nullable ItemStack itemstack1, ItemStack itemstack2) {
      this(itemstack, itemstack1, itemstack2, 0, 7);
   }

   public MerchantRecipe(ItemStack itemstack, @Nullable ItemStack itemstack1, ItemStack itemstack2, int i, int j) {
      this.itemToBuy = itemstack;
      this.secondItemToBuy = itemstack1;
      this.itemToSell = itemstack2;
      this.toolUses = i;
      this.maxTradeUses = j;
      this.rewardsExp = true;
   }

   public MerchantRecipe(ItemStack itemstack, ItemStack itemstack1) {
      this(itemstack, (ItemStack)null, itemstack1);
   }

   public MerchantRecipe(ItemStack itemstack, Item item) {
      this(itemstack, new ItemStack(item));
   }

   public ItemStack getItemToBuy() {
      return this.itemToBuy;
   }

   public ItemStack getSecondItemToBuy() {
      return this.secondItemToBuy;
   }

   public boolean hasSecondItemToBuy() {
      return this.secondItemToBuy != null;
   }

   public ItemStack getItemToSell() {
      return this.itemToSell;
   }

   public int getToolUses() {
      return this.toolUses;
   }

   public int getMaxTradeUses() {
      return this.maxTradeUses;
   }

   public void incrementToolUses() {
      ++this.toolUses;
   }

   public void increaseMaxTradeUses(int i) {
      this.maxTradeUses += i;
   }

   public boolean isRecipeDisabled() {
      return this.toolUses >= this.maxTradeUses;
   }

   public boolean getRewardsExp() {
      return this.rewardsExp;
   }

   public void readFromTags(NBTTagCompound nbttagcompound) {
      NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("buy");
      this.itemToBuy = ItemStack.loadItemStackFromNBT(nbttagcompound1);
      NBTTagCompound nbttagcompound2 = nbttagcompound.getCompoundTag("sell");
      this.itemToSell = ItemStack.loadItemStackFromNBT(nbttagcompound2);
      if (nbttagcompound.hasKey("buyB", 10)) {
         this.secondItemToBuy = ItemStack.loadItemStackFromNBT(nbttagcompound.getCompoundTag("buyB"));
      }

      if (nbttagcompound.hasKey("uses", 99)) {
         this.toolUses = nbttagcompound.getInteger("uses");
      }

      if (nbttagcompound.hasKey("maxUses", 99)) {
         this.maxTradeUses = nbttagcompound.getInteger("maxUses");
      } else {
         this.maxTradeUses = 7;
      }

      if (nbttagcompound.hasKey("rewardExp", 1)) {
         this.rewardsExp = nbttagcompound.getBoolean("rewardExp");
      } else {
         this.rewardsExp = true;
      }

   }

   public NBTTagCompound writeToTags() {
      NBTTagCompound nbttagcompound = new NBTTagCompound();
      nbttagcompound.setTag("buy", this.itemToBuy.writeToNBT(new NBTTagCompound()));
      nbttagcompound.setTag("sell", this.itemToSell.writeToNBT(new NBTTagCompound()));
      if (this.secondItemToBuy != null) {
         nbttagcompound.setTag("buyB", this.secondItemToBuy.writeToNBT(new NBTTagCompound()));
      }

      nbttagcompound.setInteger("uses", this.toolUses);
      nbttagcompound.setInteger("maxUses", this.maxTradeUses);
      nbttagcompound.setBoolean("rewardExp", this.rewardsExp);
      return nbttagcompound;
   }
}
