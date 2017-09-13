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

      public Style setColor(TextFormatting enumchatformat) {
         throw new UnsupportedOperationException();
      }

      public Style setBold(Boolean obool) {
         throw new UnsupportedOperationException();
      }

      public Style setItalic(Boolean obool) {
         throw new UnsupportedOperationException();
      }

      public Style setStrikethrough(Boolean obool) {
         throw new UnsupportedOperationException();
      }

      public Style setUnderlined(Boolean obool) {
         throw new UnsupportedOperationException();
      }

      public Style setObfuscated(Boolean obool) {
         throw new UnsupportedOperationException();
      }

      public Style setClickEvent(ClickEvent chatclickable) {
         throw new UnsupportedOperationException();
      }

      public Style setHoverEvent(HoverEvent chathoverable) {
         throw new UnsupportedOperationException();
      }

      public Style setParentStyle(Style chatmodifier) {
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

   public Style setColor(TextFormatting enumchatformat) {
      this.color = enumchatformat;
      return this;
   }

   public Style setBold(Boolean obool) {
      this.bold = obool;
      return this;
   }

   public Style setItalic(Boolean obool) {
      this.italic = obool;
      return this;
   }

   public Style setStrikethrough(Boolean obool) {
      this.strikethrough = obool;
      return this;
   }

   public Style setUnderlined(Boolean obool) {
      this.underlined = obool;
      return this;
   }

   public Style setObfuscated(Boolean obool) {
      this.obfuscated = obool;
      return this;
   }

   public Style setClickEvent(ClickEvent chatclickable) {
      this.clickEvent = chatclickable;
      return this;
   }

   public Style setHoverEvent(HoverEvent chathoverable) {
      this.hoverEvent = chathoverable;
      return this;
   }

   public Style setInsertion(String s) {
      this.insertion = s;
      return this;
   }

   public Style setParentStyle(Style chatmodifier) {
      this.parentStyle = chatmodifier;
      return this;
   }

   private Style getParent() {
      return this.parentStyle == null ? ROOT : this.parentStyle;
   }

   public String toString() {
      return "Style{hasParent=" + (this.parentStyle != null) + ", color=" + this.color + ", bold=" + this.bold + ", italic=" + this.italic + ", underlined=" + this.underlined + ", obfuscated=" + this.obfuscated + ", clickEvent=" + this.getClickEvent() + ", hoverEvent=" + this.getHoverEvent() + ", insertion=" + this.getInsertion() + '}';
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof Style)) {
         return false;
      } else {
         label73: {
            Style chatmodifier = (Style)object;
            if (this.getBold() == chatmodifier.getBold() && this.getColor() == chatmodifier.getColor() && this.getItalic() == chatmodifier.getItalic() && this.getObfuscated() == chatmodifier.getObfuscated() && this.getStrikethrough() == chatmodifier.getStrikethrough() && this.getUnderlined() == chatmodifier.getUnderlined()) {
               label72: {
                  if (this.getClickEvent() != null) {
                     if (!this.getClickEvent().equals(chatmodifier.getClickEvent())) {
                        break label72;
                     }
                  } else if (chatmodifier.getClickEvent() != null) {
                     break label72;
                  }

                  if (this.getHoverEvent() != null) {
                     if (!this.getHoverEvent().equals(chatmodifier.getHoverEvent())) {
                        break label72;
                     }
                  } else if (chatmodifier.getHoverEvent() != null) {
                     break label72;
                  }

                  if (this.getInsertion() != null) {
                     if (this.getInsertion().equals(chatmodifier.getInsertion())) {
                        break label73;
                     }
                  } else if (chatmodifier.getInsertion() == null) {
                     break label73;
                  }
               }
            }

            boolean flag = false;
            return flag;
         }

         boolean flag = true;
         return flag;
      }
   }

   public int hashCode() {
      int i = this.color == null ? 0 : this.color.hashCode();
      i = 31 * i + (this.bold == null ? 0 : this.bold.hashCode());
      i = 31 * i + (this.italic == null ? 0 : this.italic.hashCode());
      i = 31 * i + (this.underlined == null ? 0 : this.underlined.hashCode());
      i = 31 * i + (this.strikethrough == null ? 0 : this.strikethrough.hashCode());
      i = 31 * i + (this.obfuscated == null ? 0 : this.obfuscated.hashCode());
      i = 31 * i + (this.clickEvent == null ? 0 : this.clickEvent.hashCode());
      i = 31 * i + (this.hoverEvent == null ? 0 : this.hoverEvent.hashCode());
      i = 31 * i + (this.insertion == null ? 0 : this.insertion.hashCode());
      return i;
   }

   public Style createShallowCopy() {
      Style chatmodifier = new Style();
      chatmodifier.bold = this.bold;
      chatmodifier.italic = this.italic;
      chatmodifier.strikethrough = this.strikethrough;
      chatmodifier.underlined = this.underlined;
      chatmodifier.obfuscated = this.obfuscated;
      chatmodifier.color = this.color;
      chatmodifier.clickEvent = this.clickEvent;
      chatmodifier.hoverEvent = this.hoverEvent;
      chatmodifier.parentStyle = this.parentStyle;
      chatmodifier.insertion = this.insertion;
      return chatmodifier;
   }

   public Style createDeepCopy() {
      Style chatmodifier = new Style();
      chatmodifier.setBold(Boolean.valueOf(this.getBold()));
      chatmodifier.setItalic(Boolean.valueOf(this.getItalic()));
      chatmodifier.setStrikethrough(Boolean.valueOf(this.getStrikethrough()));
      chatmodifier.setUnderlined(Boolean.valueOf(this.getUnderlined()));
      chatmodifier.setObfuscated(Boolean.valueOf(this.getObfuscated()));
      chatmodifier.setColor(this.getColor());
      chatmodifier.setClickEvent(this.getClickEvent());
      chatmodifier.setHoverEvent(this.getHoverEvent());
      chatmodifier.setInsertion(this.getInsertion());
      return chatmodifier;
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
