package net.minecraft.util;

import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class StringUtils {
   private static final Pattern PATTERN_CONTROL_CODE = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");

   @SideOnly(Side.CLIENT)
   public static String ticksToElapsedTime(int var0) {
      int i = ticks / 20;
      int j = i / 60;
      i = i % 60;
      return i < 10 ? j + ":0" + i : j + ":" + i;
   }

   @SideOnly(Side.CLIENT)
   public static String stripControlCodes(String var0) {
      return PATTERN_CONTROL_CODE.matcher(text).replaceAll("");
   }

   public static boolean isNullOrEmpty(@Nullable String var0) {
      return org.apache.commons.lang3.StringUtils.isEmpty(string);
   }
}
