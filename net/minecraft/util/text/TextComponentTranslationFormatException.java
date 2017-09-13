package net.minecraft.util.text;

public class TextComponentTranslationFormatException extends IllegalArgumentException {
   public TextComponentTranslationFormatException(TextComponentTranslation var1, String var2) {
      super(String.format("Error parsing: %s: %s", component, message));
   }

   public TextComponentTranslationFormatException(TextComponentTranslation var1, int var2) {
      super(String.format("Invalid index %d requested for %s", index, component));
   }

   public TextComponentTranslationFormatException(TextComponentTranslation var1, Throwable var2) {
      super(String.format("Error while parsing: %s", component), cause);
   }
}
