package net.minecraft.util.text.translation;

@Deprecated
public class I18n {
   private static final LanguageMap localizedName = LanguageMap.getInstance();
   private static final LanguageMap fallbackTranslator = new LanguageMap();

   @Deprecated
   public static String translateToLocal(String var0) {
      return localizedName.translateKey(var0);
   }

   @Deprecated
   public static String translateToLocalFormatted(String var0, Object... var1) {
      return localizedName.translateKeyFormat(var0, var1);
   }

   @Deprecated
   public static String translateToFallback(String var0) {
      return fallbackTranslator.translateKey(var0);
   }

   @Deprecated
   public static boolean canTranslate(String var0) {
      return localizedName.isKeyTranslated(var0);
   }

   public static long getLastTranslationUpdateTimeInMilliseconds() {
      return localizedName.getLastUpdateTimeInMilliseconds();
   }
}
