package net.minecraft.util.text.translation;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.InputStream;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.regex.Pattern;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

public class LanguageMap {
   private static final Pattern NUMERIC_VARIABLE_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");
   private static final Splitter EQUAL_SIGN_SPLITTER = Splitter.on('=').limit(2);
   private static final LanguageMap instance = new LanguageMap();
   private final Map languageList = Maps.newHashMap();
   private long lastUpdateTimeInMilliseconds;

   public LanguageMap() {
      InputStream inputstream = LanguageMap.class.getResourceAsStream("/assets/minecraft/lang/en_US.lang");
      inject(this, inputstream);
   }

   public static void inject(InputStream var0) {
      inject(instance, inputstream);
   }

   private static void inject(LanguageMap var0, InputStream var1) {
      Map map = parseLangFile(inputstream);
      inst.languageList.putAll(map);
      inst.lastUpdateTimeInMilliseconds = System.currentTimeMillis();
   }

   public static Map parseLangFile(InputStream var0) {
      Map table = Maps.newHashMap();

      try {
         inputstream = FMLCommonHandler.instance().loadLanguage(table, inputstream);
         if (inputstream == null) {
            return table;
         }

         for(String s : IOUtils.readLines(inputstream, Charsets.UTF_8)) {
            if (!s.isEmpty() && s.charAt(0) != '#') {
               String[] astring = (String[])Iterables.toArray(EQUAL_SIGN_SPLITTER.split(s), String.class);
               if (astring != null && astring.length == 2) {
                  String s1 = astring[0];
                  String s2 = NUMERIC_VARIABLE_PATTERN.matcher(astring[1]).replaceAll("%$1s");
                  table.put(s1, s2);
               }
            }
         }
      } catch (IOException var7) {
         ;
      } catch (Exception var8) {
         ;
      }

      return table;
   }

   static LanguageMap getInstance() {
      return instance;
   }

   @SideOnly(Side.CLIENT)
   public static synchronized void replaceWith(Map var0) {
      instance.languageList.clear();
      instance.languageList.putAll(p_135063_0_);
      instance.lastUpdateTimeInMilliseconds = System.currentTimeMillis();
   }

   public synchronized String translateKey(String var1) {
      return this.tryTranslateKey(key);
   }

   public synchronized String translateKeyFormat(String var1, Object... var2) {
      String s = this.tryTranslateKey(key);

      try {
         return String.format(s, format);
      } catch (IllegalFormatException var5) {
         return "Format error: " + s;
      }
   }

   private String tryTranslateKey(String var1) {
      String s = (String)this.languageList.get(key);
      return s == null ? key : s;
   }

   public synchronized boolean isKeyTranslated(String var1) {
      return this.languageList.containsKey(key);
   }

   public long getLastUpdateTimeInMilliseconds() {
      return this.lastUpdateTimeInMilliseconds;
   }
}
