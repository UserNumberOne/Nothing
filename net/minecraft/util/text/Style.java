package net.minecraft.util.text;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class Style {
   private Style parentStyle;
   private TextFormatting color;
   private Boolean bold;
   private Boolean italic;
   private Boolean underlined;
   private Boolean strikethrough;
   private Boolean obfuscated;
   private ClickEvent clickEvent;
   private HoverEvent hoverEvent;
   private String insertion;
   private static final Style ROOT = new Style() {
      @Nullable
      public TextFormatting getColor() {
         return null;
      }

      public boolean getBold() {
         return false;
      }

      public boolean getItalic() {
         return false;
      }

      public boolean getStrikethrough() {
         return false;
      }

      public boolean getUnderlined() {
         return false;
      }

      public boolean getObfuscated() {
         return false;
      }

      @Nullable
      public ClickEvent getClickEvent() {
         return null;
      }

      @Nullable
      public HoverEvent getHoverEvent() {
         return null;
      }

      @Nullable
      public String getInsertion() {
         return null;
      }

      public Style setColor(TextFormatting var1) {
         throw new UnsupportedOperationException();
      }

      public Style setBold(Boolean var1) {
         throw new UnsupportedOperationException();
      }

      public Style setItalic(Boolean var1) {
         throw new UnsupportedOperationException();
      }

      public Style setStrikethrough(Boolean var1) {
         throw new UnsupportedOperationException();
      }

      public Style setUnderlined(Boolean var1) {
         throw new UnsupportedOperationException();
      }

      public Style setObfuscated(Boolean var1) {
         throw new UnsupportedOperationException();
      }

      public Style setClickEvent(ClickEvent var1) {
         throw new UnsupportedOperationException();
      }

      public Style setHoverEvent(HoverEvent var1) {
         throw new UnsupportedOperationException();
      }

      public Style setParentStyle(Style var1) {
         throw new UnsupportedOperationException();
      }

      public String toString() {
         return "Style.ROOT";
      }

      public Style createShallowCopy() {
         return this;
      }

      public Style createDeepCopy() {
         return this;
      }
   };

   @Nullable
   public TextFormatting getColor() {
      return this.color == null ? this.getParent().getColor() : this.color;
   }

   public boolean getBold() {
      return this.bold == null ? this.getParent().getBold() : this.bold.booleanValue();
   }

   public boolean getItalic() {
      return this.italic == null ? this.getParent().getItalic() : this.italic.booleanValue();
   }

   public boolean getStrikethrough() {
      return this.strikethrough == null ? this.getParent().getStrikethrough() : this.strikethrough.booleanValue();
   }

   public boolean getUnderlined() {
      return this.underlined == null ? this.getParent().getUnderlined() : this.underlined.booleanValue();
   }

   public boolean getObfuscated() {
      return this.obfuscated == null ? this.getParent().getObfuscated() : this.obfuscated.booleanValue();
   }

   public boolean isEmpty() {
      return this.bold == null && this.italic == null && this.strikethrough == null && this.underlined == null && this.obfuscated == null && this.color == null && this.clickEvent == null && this.hoverEvent == null && this.insertion == null;
   }

   @Nullable
   public ClickEvent getClickEvent() {
      return this.clickEvent == null ? this.getParent().getClickEvent() : this.clickEvent;
   }

   @Nullable
   public HoverEvent getHoverEvent() {
      return this.hoverEvent == null ? this.getParent().getHoverEvent() : this.hoverEvent;
   }

   @Nullable
   public String getInsertion() {
      return this.insertion == null ? this.getParent().getInsertion() : this.insertion;
   }

   public Style setColor(TextFormatting var1) {
      this.color = var1;
      return this;
   }

   public Style setBold(Boolean var1) {
      this.bold = var1;
      return this;
   }

   public Style setItalic(Boolean var1) {
      this.italic = var1;
      return this;
   }

   public Style setStrikethrough(Boolean var1) {
      this.strikethrough = var1;
      return this;
   }

   public Style setUnderlined(Boolean var1) {
      this.underlined = var1;
      return this;
   }

   public Style setObfuscated(Boolean var1) {
      this.obfuscated = var1;
      return this;
   }

   public Style setClickEvent(ClickEvent var1) {
      this.clickEvent = var1;
      return this;
   }

   public Style setHoverEvent(HoverEvent var1) {
      this.hoverEvent = var1;
      return this;
   }

   public Style setInsertion(String var1) {
      this.insertion = var1;
      return this;
   }

   public Style setParentStyle(Style var1) {
      this.parentStyle = var1;
      return this;
   }

   private Style getParent() {
      return this.parentStyle == null ? ROOT : this.parentStyle;
   }

   public String toString() {
      return "Style{hasParent=" + (this.parentStyle != null) + ", color=" + this.color + ", bold=" + this.bold + ", italic=" + this.italic + ", underlined=" + this.underlined + ", obfuscated=" + this.obfuscated + ", clickEvent=" + this.getClickEvent() + ", hoverEvent=" + this.getHoverEvent() + ", insertion=" + this.getInsertion() + '}';
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof Style)) {
         return false;
      } else {
         label73: {
            Style var2 = (Style)var1;
            if (this.getBold() == var2.getBold() && this.getColor() == var2.getColor() && this.getItalic() == var2.getItalic() && this.getObfuscated() == var2.getObfuscated() && this.getStrikethrough() == var2.getStrikethrough() && this.getUnderlined() == var2.getUnderlined()) {
               label72: {
                  if (this.getClickEvent() != null) {
                     if (!this.getClickEvent().equals(var2.getClickEvent())) {
                        break label72;
                     }
                  } else if (var2.getClickEvent() != null) {
                     break label72;
                  }

                  if (this.getHoverEvent() != null) {
                     if (!this.getHoverEvent().equals(var2.getHoverEvent())) {
                        break label72;
                     }
                  } else if (var2.getHoverEvent() != null) {
                     break label72;
                  }

                  if (this.getInsertion() != null) {
                     if (this.getInsertion().equals(var2.getInsertion())) {
                        break label73;
                     }
                  } else if (var2.getInsertion() == null) {
                     break label73;
                  }
               }
            }

            boolean var4 = false;
            return var4;
         }

         boolean var3 = true;
         return var3;
      }
   }

   public int hashCode() {
      int var1 = this.color == null ? 0 : this.color.hashCode();
      var1 = 31 * var1 + (this.bold == null ? 0 : this.bold.hashCode());
      var1 = 31 * var1 + (this.italic == null ? 0 : this.italic.hashCode());
      var1 = 31 * var1 + (this.underlined == null ? 0 : this.underlined.hashCode());
      var1 = 31 * var1 + (this.strikethrough == null ? 0 : this.strikethrough.hashCode());
      var1 = 31 * var1 + (this.obfuscated == null ? 0 : this.obfuscated.hashCode());
      var1 = 31 * var1 + (this.clickEvent == null ? 0 : this.clickEvent.hashCode());
      var1 = 31 * var1 + (this.hoverEvent == null ? 0 : this.hoverEvent.hashCode());
      var1 = 31 * var1 + (this.insertion == null ? 0 : this.insertion.hashCode());
      return var1;
   }

   public Style createShallowCopy() {
      Style var1 = new Style();
      var1.bold = this.bold;
      var1.italic = this.italic;
      var1.strikethrough = this.strikethrough;
      var1.underlined = this.underlined;
      var1.obfuscated = this.obfuscated;
      var1.color = this.color;
      var1.clickEvent = this.clickEvent;
      var1.hoverEvent = this.hoverEvent;
      var1.parentStyle = this.parentStyle;
      var1.insertion = this.insertion;
      return var1;
   }

   public Style createDeepCopy() {
      Style var1 = new Style();
      var1.setBold(Boolean.valueOf(this.getBold()));
      var1.setItalic(Boolean.valueOf(this.getItalic()));
      var1.setStrikethrough(Boolean.valueOf(this.getStrikethrough()));
      var1.setUnderlined(Boolean.valueOf(this.getUnderlined()));
      var1.setObfuscated(Boolean.valueOf(this.getObfuscated()));
      var1.setColor(this.getColor());
      var1.setClickEvent(this.getClickEvent());
      var1.setHoverEvent(this.getHoverEvent());
      var1.setInsertion(this.getInsertion());
      return var1;
   }

   public static class Serializer implements JsonDeserializer, JsonSerializer {
      @Nullable
      public Style deserialize(JsonElement param1, Type param2, JsonDeserializationContext param3) throws JsonParseException {
         // $FF: Couldn't be decompiled
      }

      @Nullable
      public JsonElement serialize(Style param1, Type param2, JsonSerializationContext param3) {
         // $FF: Couldn't be decompiled
      }

      public JsonElement serialize(Style param1, Type param2, JsonSerializationContext param3) {
         // $FF: Couldn't be decompiled
      }

      public Style deserialize(JsonElement param1, Type param2, JsonDeserializationContext param3) throws JsonParseException {
         // $FF: Couldn't be decompiled
      }
   }
}
