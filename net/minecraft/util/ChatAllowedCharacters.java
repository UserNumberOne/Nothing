package net.minecraft.util;

import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

public class ChatAllowedCharacters {
   public static final Level NETTY_LEAK_DETECTION = Level.DISABLED;
   public static final char[] ILLEGAL_STRUCTURE_CHARACTERS = new char[]{'.', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '"'};
   public static final char[] ILLEGAL_FILE_CHARACTERS = new char[]{'/', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '"', ':'};

   public static boolean isAllowedCharacter(char var0) {
      return var0 != 167 && var0 >= ' ' && var0 != 127;
   }

   public static String filterAllowedCharacters(String var0) {
      StringBuilder var1 = new StringBuilder();

      for(char var5 : var0.toCharArray()) {
         if (isAllowedCharacter(var5)) {
            var1.append(var5);
         }
      }

      return var1.toString();
   }

   static {
      ResourceLeakDetector.setLevel(NETTY_LEAK_DETECTION);
   }
}
