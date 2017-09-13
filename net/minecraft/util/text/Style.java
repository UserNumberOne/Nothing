package net.minecraft.util.text;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

      @SideOnly(Side.CLIENT)
      public String getFormattingCode() {
         return "";
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

   public String getFormattingCode() {
      if (this.isEmpty()) {
         return this.parentStyle != null ? this.parentStyle.getFormattingCode() : "";
      } else {
         StringBuilder var1 = new StringBuilder();
         if (this.getColor() != null) {
            var1.append(this.getColor());
         }

         if (this.getBold()) {
            var1.append(TextFormatting.BOLD);
         }

         if (this.getItalic()) {
            var1.append(TextFormatting.ITALIC);
         }

         if (this.getUnderlined()) {
            var1.append(TextFormatting.UNDERLINE);
         }

         if (this.getObfuscated()) {
            var1.append(TextFormatting.OBFUSCATED);
         }

         if (this.getStrikethrough()) {
            var1.append(TextFormatting.STRIKETHROUGH);
         }

         return var1.toString();
      }
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
         Style var3 = (Style)var1;
         if (this.getBold() == var3.getBold() && this.getColor() == var3.getColor() && this.getItalic() == var3.getItalic() && this.getObfuscated() == var3.getObfuscated() && this.getStrikethrough() == var3.getStrikethrough() && this.getUnderlined() == var3.getUnderlined()) {
            label71: {
               if (this.getClickEvent() != null) {
                  if (!this.getClickEvent().equals(var3.getClickEvent())) {
                     break label71;
                  }
               } else if (var3.getClickEvent() != null) {
                  break label71;
               }

               if (this.getHoverEvent() != null) {
                  if (!this.getHoverEvent().equals(var3.getHoverEvent())) {
                     break label71;
                  }
               } else if (var3.getHoverEvent() != null) {
                  break label71;
               }

               if (this.getInsertion() != null) {
                  if (!this.getInsertion().equals(var3.getInsertion())) {
                     break label71;
                  }
               } else if (var3.getInsertion() != null) {
                  break label71;
               }

               boolean var2 = true;
               return var2;
            }
         }

         boolean var4 = false;
         return var4;
      }
   }

   public int hashCode() {
      int var1 = this.color.hashCode();
      var1 = 31 * var1 + this.bold.hashCode();
      var1 = 31 * var1 + this.italic.hashCode();
      var1 = 31 * var1 + this.underlined.hashCode();
      var1 = 31 * var1 + this.strikethrough.hashCode();
      var1 = 31 * var1 + this.obfuscated.hashCode();
      var1 = 31 * var1 + this.clickEvent.hashCode();
      var1 = 31 * var1 + this.hoverEvent.hashCode();
      var1 = 31 * var1 + this.insertion.hashCode();
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
      public Style deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         if (var1.isJsonObject()) {
            Style var4 = new Style();
            JsonObject var5 = var1.getAsJsonObject();
            if (var5 == null) {
               return null;
            } else {
               if (var5.has("bold")) {
                  var4.bold = var5.get("bold").getAsBoolean();
               }

               if (var5.has("italic")) {
                  var4.italic = var5.get("italic").getAsBoolean();
               }

               if (var5.has("underlined")) {
                  var4.underlined = var5.get("underlined").getAsBoolean();
               }

               if (var5.has("strikethrough")) {
                  var4.strikethrough = var5.get("strikethrough").getAsBoolean();
               }

               if (var5.has("obfuscated")) {
                  var4.obfuscated = var5.get("obfuscated").getAsBoolean();
               }

               if (var5.has("color")) {
                  var4.color = (TextFormatting)var3.deserialize(var5.get("color"), TextFormatting.class);
               }

               if (var5.has("insertion")) {
                  var4.insertion = var5.get("insertion").getAsString();
               }

               if (var5.has("clickEvent")) {
                  JsonObject var6 = var5.getAsJsonObject("clickEvent");
                  if (var6 != null) {
                     JsonPrimitive var7 = var6.getAsJsonPrimitive("action");
                     ClickEvent.Action var8 = var7 == null ? null : ClickEvent.Action.getValueByCanonicalName(var7.getAsString());
                     JsonPrimitive var9 = var6.getAsJsonPrimitive("value");
                     String var10 = var9 == null ? null : var9.getAsString();
                     if (var8 != null && var10 != null && var8.shouldAllowInChat()) {
                        var4.clickEvent = new ClickEvent(var8, var10);
                     }
                  }
               }

               if (var5.has("hoverEvent")) {
                  JsonObject var11 = var5.getAsJsonObject("hoverEvent");
                  if (var11 != null) {
                     JsonPrimitive var12 = var11.getAsJsonPrimitive("action");
                     HoverEvent.Action var13 = var12 == null ? null : HoverEvent.Action.getValueByCanonicalName(var12.getAsString());
                     ITextComponent var14 = (ITextComponent)var3.deserialize(var11.get("value"), ITextComponent.class);
                     if (var13 != null && var14 != null && var13.shouldAllowInChat()) {
                        var4.hoverEvent = new HoverEvent(var13, var14);
                     }
                  }
               }

               return var4;
            }
         } else {
            return null;
         }
      }

      @Nullable
      public JsonElement serialize(Style var1, Type var2, JsonSerializationContext var3) {
         if (var1.isEmpty()) {
            return null;
         } else {
            JsonObject var4 = new JsonObject();
            if (var1.bold != null) {
               var4.addProperty("bold", var1.bold);
            }

            if (var1.italic != null) {
               var4.addProperty("italic", var1.italic);
            }

            if (var1.underlined != null) {
               var4.addProperty("underlined", var1.underlined);
            }

            if (var1.strikethrough != null) {
               var4.addProperty("strikethrough", var1.strikethrough);
            }

            if (var1.obfuscated != null) {
               var4.addProperty("obfuscated", var1.obfuscated);
            }

            if (var1.color != null) {
               var4.add("color", var3.serialize(var1.color));
            }

            if (var1.insertion != null) {
               var4.add("insertion", var3.serialize(var1.insertion));
            }

            if (var1.clickEvent != null) {
               JsonObject var5 = new JsonObject();
               var5.addProperty("action", var1.clickEvent.getAction().getCanonicalName());
               var5.addProperty("value", var1.clickEvent.getValue());
               var4.add("clickEvent", var5);
            }

            if (var1.hoverEvent != null) {
               JsonObject var6 = new JsonObject();
               var6.addProperty("action", var1.hoverEvent.getAction().getCanonicalName());
               var6.add("value", var3.serialize(var1.hoverEvent.getValue()));
               var4.add("hoverEvent", var6);
            }

            return var4;
         }
      }
   }
}
