package net.minecraft.item;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemFireworkCharge extends Item {
   @SideOnly(Side.CLIENT)
   public static NBTBase getExplosionTag(ItemStack var0, String var1) {
      if (var0.hasTagCompound()) {
         NBTTagCompound var2 = var0.getTagCompound().getCompoundTag("Explosion");
         if (var2 != null) {
            return var2.getTag(var1);
         }
      }

      return null;
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack var1, EntityPlayer var2, List var3, boolean var4) {
      if (var1.hasTagCompound()) {
         NBTTagCompound var5 = var1.getTagCompound().getCompoundTag("Explosion");
         if (var5 != null) {
            addExplosionInfo(var5, var3);
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public static void addExplosionInfo(NBTTagCompound var0, List var1) {
      byte var2 = var0.getByte("Type");
      if (var2 >= 0 && var2 <= 4) {
         var1.add(I18n.translateToLocal("item.fireworksCharge.type." + var2).trim());
      } else {
         var1.add(I18n.translateToLocal("item.fireworksCharge.type").trim());
      }

      int[] var3 = var0.getIntArray("Colors");
      if (var3.length > 0) {
         boolean var4 = true;
         String var5 = "";

         for(int var9 : var3) {
            if (!var4) {
               var5 = var5 + ", ";
            }

            var4 = false;
            boolean var10 = false;

            for(int var11 = 0; var11 < ItemDye.DYE_COLORS.length; ++var11) {
               if (var9 == ItemDye.DYE_COLORS[var11]) {
                  var10 = true;
                  var5 = var5 + I18n.translateToLocal("item.fireworksCharge." + EnumDyeColor.byDyeDamage(var11).getUnlocalizedName());
                  break;
               }
            }

            if (!var10) {
               var5 = var5 + I18n.translateToLocal("item.fireworksCharge.customColor");
            }
         }

         var1.add(var5);
      }

      int[] var13 = var0.getIntArray("FadeColors");
      if (var13.length > 0) {
         boolean var14 = true;
         String var16 = I18n.translateToLocal("item.fireworksCharge.fadeTo") + " ";

         for(int var21 : var13) {
            if (!var14) {
               var16 = var16 + ", ";
            }

            var14 = false;
            boolean var22 = false;

            for(int var12 = 0; var12 < 16; ++var12) {
               if (var21 == ItemDye.DYE_COLORS[var12]) {
                  var22 = true;
                  var16 = var16 + I18n.translateToLocal("item.fireworksCharge." + EnumDyeColor.byDyeDamage(var12).getUnlocalizedName());
                  break;
               }
            }

            if (!var22) {
               var16 = var16 + I18n.translateToLocal("item.fireworksCharge.customColor");
            }
         }

         var1.add(var16);
      }

      boolean var15 = var0.getBoolean("Trail");
      if (var15) {
         var1.add(I18n.translateToLocal("item.fireworksCharge.trail"));
      }

      boolean var17 = var0.getBoolean("Flicker");
      if (var17) {
         var1.add(I18n.translateToLocal("item.fireworksCharge.flicker"));
      }

   }
}
