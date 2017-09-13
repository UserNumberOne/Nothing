package net.minecraft.util;

import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class StringUtils {
   private static final Pattern PATTERN_CONTROL_CODE = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");

   @SideOnly(Side.CLIENT)
   public static String ticksToElapsedTime(int var0) {
      int var1 = var0 / 20;
      int var2 = var1 / 60;
      var1 = var1 % 60;
      return var1 < 10 ? var2 + ":0" + var1 : var2 + ":" + var1;
   }

   @SideOnly(Side.CLIENT)
   public static String stripControlCodes(String var0) {
      return PATTERN_CONTROL_CODE.matcher(var0).replaceAll("");
   }

   public static boolean isNullOrEmpty(@Nullable String var0) {
      return org.apache.commons.lang3.StringUtils.isEmpty(var0);
   }
}
