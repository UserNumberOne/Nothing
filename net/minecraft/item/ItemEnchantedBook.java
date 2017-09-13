package net.minecraft.item;

import java.util.List;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemEnchantedBook extends Item {
   @SideOnly(Side.CLIENT)
   public boolean hasEffect(ItemStack var1) {
      return true;
   }

   public boolean isEnchantable(ItemStack var1) {
      return false;
   }

   public EnumRarity getRarity(ItemStack var1) {
      return this.getEnchantments(stack).hasNoTags() ? super.getRarity(stack) : EnumRarity.UNCOMMON;
   }

   public NBTTagList getEnchantments(ItemStack var1) {
      NBTTagCompound nbttagcompound = stack.getTagCompound();
      return nbttagcompound != null && nbttagcompound.hasKey("StoredEnchantments", 9) ? (NBTTagList)nbttagcompound.getTag("StoredEnchantments") : new NBTTagList();
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack var1, EntityPlayer var2, List var3, boolean var4) {
      super.addInformation(stack, playerIn, tooltip, advanced);
      NBTTagList nbttaglist = this.getEnchantments(stack);
      if (nbttaglist != null) {
         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            int j = nbttaglist.getCompoundTagAt(i).getShort("id");
            int k = nbttaglist.getCompoundTagAt(i).getShort("lvl");
            if (Enchantment.getEnchantmentByID(j) != null) {
               tooltip.add(Enchantment.getEnchantmentByID(j).getTranslatedName(k));
            }
         }
      }

   }

   public void addEnchantment(ItemStack var1, EnchantmentData var2) {
      NBTTagList nbttaglist = this.getEnchantments(stack);
      boolean flag = true;

      for(int i = 0; i < nbttaglist.tagCount(); ++i) {
         NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
         if (Enchantment.getEnchantmentByID(nbttagcompound.getShort("id")) == enchantment.enchantmentobj) {
            if (nbttagcompound.getShort("lvl") < enchantment.enchantmentLevel) {
               nbttagcompound.setShort("lvl", (short)enchantment.enchantmentLevel);
            }

            flag = false;
            break;
         }
      }

      if (flag) {
         NBTTagCompound nbttagcompound1 = new NBTTagCompound();
         nbttagcompound1.setShort("id", (short)Enchantment.getEnchantmentID(enchantment.enchantmentobj));
         nbttagcompound1.setShort("lvl", (short)enchantment.enchantmentLevel);
         nbttaglist.appendTag(nbttagcompound1);
      }

      if (!stack.hasTagCompound()) {
         stack.setTagCompound(new NBTTagCompound());
      }

      stack.getTagCompound().setTag("StoredEnchantments", nbttaglist);
   }

   public ItemStack getEnchantedItemStack(EnchantmentData var1) {
      ItemStack itemstack = new ItemStack(this);
      this.addEnchantment(itemstack, data);
      return itemstack;
   }

   @SideOnly(Side.CLIENT)
   public void getAll(Enchantment var1, List var2) {
      for(int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); ++i) {
         list.add(this.getEnchantedItemStack(new EnchantmentData(enchantment, i)));
      }

   }
}
