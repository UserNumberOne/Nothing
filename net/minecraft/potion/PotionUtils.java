package net.minecraft.potion;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

public class PotionUtils {
   public static List getEffectsFromStack(ItemStack var0) {
      return getEffectsFromTag(var0.getTagCompound());
   }

   public static List mergeEffects(PotionType var0, Collection var1) {
      ArrayList var2 = Lists.newArrayList();
      var2.addAll(var0.getEffects());
      var2.addAll(var1);
      return var2;
   }

   public static List getEffectsFromTag(@Nullable NBTTagCompound var0) {
      ArrayList var1 = Lists.newArrayList();
      var1.addAll(getPotionTypeFromNBT(var0).getEffects());
      addCustomPotionEffectToList(var0, var1);
      return var1;
   }

   public static List getFullEffectsFromItem(ItemStack var0) {
      return getFullEffectsFromTag(var0.getTagCompound());
   }

   public static List getFullEffectsFromTag(@Nullable NBTTagCompound var0) {
      ArrayList var1 = Lists.newArrayList();
      addCustomPotionEffectToList(var0, var1);
      return var1;
   }

   public static void addCustomPotionEffectToList(@Nullable NBTTagCompound var0, List var1) {
      if (var0 != null && var0.hasKey("CustomPotionEffects", 9)) {
         NBTTagList var2 = var0.getTagList("CustomPotionEffects", 10);

         for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
            NBTTagCompound var4 = var2.getCompoundTagAt(var3);
            PotionEffect var5 = PotionEffect.readCustomPotionEffectFromNBT(var4);
            if (var5 != null) {
               var1.add(var5);
            }
         }
      }

   }

   public static int getPotionColorFromEffectList(Collection var0) {
      int var1 = 3694022;
      if (var0.isEmpty()) {
         return 3694022;
      } else {
         float var2 = 0.0F;
         float var3 = 0.0F;
         float var4 = 0.0F;
         int var5 = 0;

         for(PotionEffect var7 : var0) {
            if (var7.doesShowParticles()) {
               int var8 = var7.getPotion().getLiquidColor();
               int var9 = var7.getAmplifier() + 1;
               var2 += (float)(var9 * (var8 >> 16 & 255)) / 255.0F;
               var3 += (float)(var9 * (var8 >> 8 & 255)) / 255.0F;
               var4 += (float)(var9 * (var8 >> 0 & 255)) / 255.0F;
               var5 += var9;
            }
         }

         if (var5 == 0) {
            return 0;
         } else {
            var2 = var2 / (float)var5 * 255.0F;
            var3 = var3 / (float)var5 * 255.0F;
            var4 = var4 / (float)var5 * 255.0F;
            return (int)var2 << 16 | (int)var3 << 8 | (int)var4;
         }
      }
   }

   public static PotionType getPotionFromItem(ItemStack var0) {
      return getPotionTypeFromNBT(var0.getTagCompound());
   }

   public static PotionType getPotionTypeFromNBT(@Nullable NBTTagCompound var0) {
      return var0 == null ? PotionTypes.WATER : PotionType.getPotionTypeForName(var0.getString("Potion"));
   }

   public static ItemStack addPotionToItemStack(ItemStack var0, PotionType var1) {
      ResourceLocation var2 = (ResourceLocation)PotionType.REGISTRY.getNameForObject(var1);
      if (var2 != null) {
         NBTTagCompound var3 = var0.hasTagCompound() ? var0.getTagCompound() : new NBTTagCompound();
         var3.setString("Potion", var2.toString());
         var0.setTagCompound(var3);
      }

      return var0;
   }

   public static ItemStack appendEffects(ItemStack var0, Collection var1) {
      if (var1.isEmpty()) {
         return var0;
      } else {
         NBTTagCompound var2 = (NBTTagCompound)Objects.firstNonNull(var0.getTagCompound(), new NBTTagCompound());
         NBTTagList var3 = var2.getTagList("CustomPotionEffects", 9);

         for(PotionEffect var5 : var1) {
            var3.appendTag(var5.writeCustomPotionEffectToNBT(new NBTTagCompound()));
         }

         var2.setTag("CustomPotionEffects", var3);
         var0.setTagCompound(var2);
         return var0;
      }
   }
}
