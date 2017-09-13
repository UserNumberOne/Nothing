package net.minecraft.client.resources;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class I18n {
   private static Locale i18nLocale;

   static void setLocale(Locale var0) {
      i18nLocale = var0;
   }

   public static String format(String var0, Object... var1) {
      return i18nLocale.formatMessage(var0, var1);
   }

   public static boolean hasKey(String var0) {
      return i18nLocale.hasKey(var0);
   }
}
