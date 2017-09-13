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
      return this.getEnchantments(var1).hasNoTags() ? super.getRarity(var1) : EnumRarity.UNCOMMON;
   }

   public NBTTagList getEnchantments(ItemStack var1) {
      NBTTagCompound var2 = var1.getTagCompound();
      return var2 != null && var2.hasKey("StoredEnchantments", 9) ? (NBTTagList)var2.getTag("StoredEnchantments") : new NBTTagList();
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack var1, EntityPlayer var2, List var3, boolean var4) {
      super.addInformation(var1, var2, var3, var4);
      NBTTagList var5 = this.getEnchantments(var1);
      if (var5 != null) {
         for(int var6 = 0; var6 < var5.tagCount(); ++var6) {
            short var7 = var5.getCompoundTagAt(var6).getShort("id");
            short var8 = var5.getCompoundTagAt(var6).getShort("lvl");
            if (Enchantment.getEnchantmentByID(var7) != null) {
               var3.add(Enchantment.getEnchantmentByID(var7).getTranslatedName(var8));
            }
         }
      }

   }

   public void addEnchantment(ItemStack var1, EnchantmentData var2) {
      NBTTagList var3 = this.getEnchantments(var1);
      boolean var4 = true;

      for(int var5 = 0; var5 < var3.tagCount(); ++var5) {
         NBTTagCompound var6 = var3.getCompoundTagAt(var5);
         if (Enchantment.getEnchantmentByID(var6.getShort("id")) == var2.enchantmentobj) {
            if (var6.getShort("lvl") < var2.enchantmentLevel) {
               var6.setShort("lvl", (short)var2.enchantmentLevel);
            }

            var4 = false;
            break;
         }
      }

      if (var4) {
         NBTTagCompound var7 = new NBTTagCompound();
         var7.setShort("id", (short)Enchantment.getEnchantmentID(var2.enchantmentobj));
         var7.setShort("lvl", (short)var2.enchantmentLevel);
         var3.appendTag(var7);
      }

      if (!var1.hasTagCompound()) {
         var1.setTagCompound(new NBTTagCompound());
      }

      var1.getTagCompound().setTag("StoredEnchantments", var3);
   }

   public ItemStack getEnchantedItemStack(EnchantmentData var1) {
      ItemStack var2 = new ItemStack(this);
      this.addEnchantment(var2, var1);
      return var2;
   }

   @SideOnly(Side.CLIENT)
   public void getAll(Enchantment var1, List var2) {
      for(int var3 = var1.getMinLevel(); var3 <= var1.getMaxLevel(); ++var3) {
         var2.add(this.getEnchantedItemStack(new EnchantmentData(var1, var3)));
      }

   }
}
