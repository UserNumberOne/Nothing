package net.minecraft.util.text.event;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.util.text.ITextComponent;

public class HoverEvent {
   private final HoverEvent.Action action;
   private final ITextComponent value;

   public HoverEvent(HoverEvent.Action var1, ITextComponent var2) {
      this.action = var1;
      this.value = var2;
   }

   public HoverEvent.Action getAction() {
      return this.action;
   }

   public ITextComponent getValue() {
      return this.value;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (var1 != null && this.getClass() == var1.getClass()) {
         HoverEvent var2 = (HoverEvent)var1;
         if (this.action != var2.action) {
            return false;
         } else {
            if (this.value != null) {
               if (!this.value.equals(var2.value)) {
                  return false;
               }
            } else if (var2.value != null) {
               return false;
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public String toString() {
      return "HoverEvent{action=" + this.action + ", value='" + this.value + '\'' + '}';
   }

   public int hashCode() {
      int var1 = this.action.hashCode();
      var1 = 31 * var1 + (this.value != null ? this.value.hashCode() : 0);
      return var1;
   }

   public static enum Action {
      SHOW_TEXT("show_text", true),
      SHOW_ACHIEVEMENT("show_achievement", true),
      SHOW_ITEM("show_item", true),
      SHOW_ENTITY("show_entity", true);

      private static final Map NAME_MAPPING = Maps.newHashMap();
      private final boolean allowedInChat;
      private final String canonicalName;

      private Action(String var3, boolean var4) {
         this.canonicalName = var3;
         this.allowedInChat = var4;
      }

      public boolean shouldAllowInChat() {
         return this.allowedInChat;
      }

      public String getCanonicalName() {
         return this.canonicalName;
      }

      public static HoverEvent.Action getValueByCanonicalName(String var0) {
         return (HoverEvent.Action)NAME_MAPPING.get(var0);
      }

      static {
         for(HoverEvent.Action var3 : values()) {
            NAME_MAPPING.put(var3.getCanonicalName(), var3);
         }

      }
   }
}
