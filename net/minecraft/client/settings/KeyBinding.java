package net.minecraft.client.settings;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyBindingMap;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class KeyBinding implements Comparable {
   private static final List KEYBIND_ARRAY = Lists.newArrayList();
   private static final KeyBindingMap HASH = new KeyBindingMap();
   private static final Set KEYBIND_SET = Sets.newHashSet();
   private final String keyDescription;
   private final int keyCodeDefault;
   private final String keyCategory;
   private int keyCode;
   private boolean pressed;
   private int pressTime;
   private KeyModifier keyModifierDefault;
   private KeyModifier keyModifier;
   private IKeyConflictContext keyConflictContext;

   public static void onTick(int var0) {
      if (var0 != 0) {
         KeyBinding var1 = HASH.lookupActive(var0);
         if (var1 != null) {
            ++var1.pressTime;
         }
      }

   }

   public static void setKeyBindState(int var0, boolean var1) {
      if (var0 != 0) {
         for(KeyBinding var3 : HASH.lookupAll(var0)) {
            if (var3 != null) {
               var3.pressed = var1;
            }
         }
      }

   }

   public static void updateKeyBindState() {
      for(KeyBinding var1 : KEYBIND_ARRAY) {
         try {
            setKeyBindState(var1.keyCode, var1.keyCode < 256 && Keyboard.isKeyDown(var1.keyCode));
         } catch (IndexOutOfBoundsException var3) {
            ;
         }
      }

   }

   public static void unPressAllKeys() {
      for(KeyBinding var1 : KEYBIND_ARRAY) {
         var1.unpressKey();
      }

   }

   public static void resetKeyBindingArrayAndHash() {
      HASH.clearMap();

      for(KeyBinding var1 : KEYBIND_ARRAY) {
         HASH.addKey(var1.keyCode, var1);
      }

   }

   public static Set getKeybinds() {
      return KEYBIND_SET;
   }

   public KeyBinding(String var1, int var2, String var3) {
      this.keyModifierDefault = KeyModifier.NONE;
      this.keyModifier = KeyModifier.NONE;
      this.keyConflictContext = KeyConflictContext.UNIVERSAL;
      this.keyDescription = var1;
      this.keyCode = var2;
      this.keyCodeDefault = var2;
      this.keyCategory = var3;
      KEYBIND_ARRAY.add(this);
      HASH.addKey(var2, this);
      KEYBIND_SET.add(var3);
   }

   public boolean isKeyDown() {
      return this.pressed && this.getKeyConflictContext().isActive() && this.getKeyModifier().isActive(this.getKeyConflictContext());
   }

   public String getKeyCategory() {
      return this.keyCategory;
   }

   public boolean isPressed() {
      if (this.pressTime == 0) {
         return false;
      } else {
         --this.pressTime;
         return true;
      }
   }

   private void unpressKey() {
      this.pressTime = 0;
      this.pressed = false;
   }

   public String getKeyDescription() {
      return this.keyDescription;
   }

   public int getKeyCodeDefault() {
      return this.keyCodeDefault;
   }

   public int getKeyCode() {
      return this.keyCode;
   }

   public void setKeyCode(int var1) {
      this.keyCode = var1;
   }

   public int compareTo(KeyBinding var1) {
      int var2 = I18n.format(this.keyCategory).compareTo(I18n.format(var1.keyCategory));
      if (var2 == 0) {
         var2 = I18n.format(this.keyDescription).compareTo(I18n.format(var1.keyDescription));
      }

      return var2;
   }

   public KeyBinding(String var1, IKeyConflictContext var2, int var3, String var4) {
      this(var1, var2, KeyModifier.NONE, var3, var4);
   }

   public KeyBinding(String var1, IKeyConflictContext var2, KeyModifier var3, int var4, String var5) {
      this.keyModifierDefault = KeyModifier.NONE;
      this.keyModifier = KeyModifier.NONE;
      this.keyConflictContext = KeyConflictContext.UNIVERSAL;
      this.keyDescription = var1;
      this.keyCode = var4;
      this.keyCodeDefault = var4;
      this.keyCategory = var5;
      this.keyConflictContext = var2;
      this.keyModifier = var3;
      this.keyModifierDefault = var3;
      if (this.keyModifier.matches(var4)) {
         this.keyModifier = KeyModifier.NONE;
      }

      KEYBIND_ARRAY.add(this);
      HASH.addKey(var4, this);
      KEYBIND_SET.add(var5);
   }

   public boolean isActiveAndMatches(int var1) {
      return var1 != 0 && var1 == this.getKeyCode() && this.getKeyConflictContext().isActive() && this.getKeyModifier().isActive(this.getKeyConflictContext());
   }

   public void setKeyConflictContext(IKeyConflictContext var1) {
      this.keyConflictContext = var1;
   }

   public IKeyConflictContext getKeyConflictContext() {
      return this.keyConflictContext;
   }

   public KeyModifier getKeyModifierDefault() {
      return this.keyModifierDefault;
   }

   public KeyModifier getKeyModifier() {
      return this.keyModifier;
   }

   public void setKeyModifierAndCode(KeyModifier var1, int var2) {
      this.keyCode = var2;
      if (var1.matches(var2)) {
         var1 = KeyModifier.NONE;
      }

      HASH.removeKey(this);
      this.keyModifier = var1;
      HASH.addKey(var2, this);
   }

   public void setToDefault() {
      this.setKeyModifierAndCode(this.getKeyModifierDefault(), this.getKeyCodeDefault());
   }

   public boolean isSetToDefaultValue() {
      return this.getKeyCode() == this.getKeyCodeDefault() && this.getKeyModifier() == this.getKeyModifierDefault();
   }

   public boolean conflicts(KeyBinding var1) {
      if (this.getKeyConflictContext().conflicts(var1.getKeyConflictContext()) || var1.getKeyConflictContext().conflicts(this.getKeyConflictContext())) {
         KeyModifier var2 = this.getKeyModifier();
         KeyModifier var3 = var1.getKeyModifier();
         if (var2.matches(var1.getKeyCode()) || var3.matches(this.getKeyCode())) {
            return true;
         }

         if (this.getKeyCode() == var1.getKeyCode()) {
            return var2 == var3 || this.getKeyConflictContext().conflicts(KeyConflictContext.IN_GAME) && (var2 == KeyModifier.NONE || var3 == KeyModifier.NONE);
         }
      }

      return false;
   }

   public boolean hasKeyCodeModifierConflict(KeyBinding var1) {
      return (this.getKeyConflictContext().conflicts(var1.getKeyConflictContext()) || var1.getKeyConflictContext().conflicts(this.getKeyConflictContext())) && (this.getKeyModifier().matches(var1.getKeyCode()) || var1.getKeyModifier().matches(this.getKeyCode()));
   }

   public String getDisplayName() {
      return this.getKeyModifier().getLocalizedComboName(this.getKeyCode());
   }
}
