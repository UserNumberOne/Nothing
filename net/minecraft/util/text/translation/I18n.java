package net.minecraft.util.text.translation;

/** @deprecated */
@Deprecated
public class I18n {
   private static final LanguageMap localizedName = LanguageMap.getInstance();
   private static final LanguageMap fallbackTranslator = new LanguageMap();

   /** @deprecated */
   @Deprecated
   public static String translateToLocal(String var0) {
      return localizedName.translateKey(key);
   }

   /** @deprecated */
   @Deprecated
   public static String translateToLocalFormatted(String var0, Object... var1) {
      return localizedName.translateKeyFormat(key, format);
   }

   /** @deprecated */
   @Deprecated
   public static String translateToFallback(String var0) {
      return fallbackTranslator.translateKey(key);
   }

   /** @deprecated */
   @Deprecated
   public static boolean canTranslate(String var0) {
      return localizedName.isKeyTranslated(key);
   }

   public static long getLastTranslationUpdateTimeInMilliseconds() {
      return localizedName.getLastUpdateTimeInMilliseconds();
   }
}
