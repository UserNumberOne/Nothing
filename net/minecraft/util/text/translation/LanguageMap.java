package net.minecraft.util.text.translation;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.InputStream;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

public class LanguageMap {
   private static final Pattern NUMERIC_VARIABLE_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");
   private static final Splitter EQUAL_SIGN_SPLITTER = Splitter.on('=').limit(2);
   private static final LanguageMap instance = new LanguageMap();
   private final Map languageList = Maps.newHashMap();
   private long lastUpdateTimeInMilliseconds;

   public LanguageMap() {
      try {
         InputStream var1 = LanguageMap.class.getResourceAsStream("/assets/minecraft/lang/en_US.lang");

         for(String var3 : IOUtils.readLines(var1, Charsets.UTF_8)) {
            if (!var3.isEmpty() && var3.charAt(0) != '#') {
               String[] var4 = (String[])Iterables.toArray(EQUAL_SIGN_SPLITTER.split(var3), String.class);
               if (var4 != null && var4.length == 2) {
                  String var5 = var4[0];
                  String var6 = NUMERIC_VARIABLE_PATTERN.matcher(var4[1]).replaceAll("%$1s");
                  this.languageList.put(var5, var6);
               }
            }
         }

         this.lastUpdateTimeInMilliseconds = System.currentTimeMillis();
      } catch (IOException var7) {
         ;
      }

   }

   static LanguageMap getInstance() {
      return instance;
   }

   public synchronized String translateKey(String var1) {
      return this.tryTranslateKey(var1);
   }

   public synchronized String translateKeyFormat(String var1, Object... var2) {
      String var3 = this.tryTranslateKey(var1);

      try {
         return String.format(var3, var2);
      } catch (IllegalFormatException var5) {
         return "Format error: " + var3;
      }
   }

   private String tryTranslateKey(String var1) {
      String var2 = (String)this.languageList.get(var1);
      return var2 == null ? var1 : var2;
   }

   public synchronized boolean isKeyTranslated(String var1) {
      return this.languageList.containsKey(var1);
   }

   public long getLastUpdateTimeInMilliseconds() {
      return this.lastUpdateTimeInMilliseconds;
   }
}
