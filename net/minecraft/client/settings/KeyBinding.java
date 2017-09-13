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
      if (keyCode != 0) {
         KeyBinding keybinding = HASH.lookupActive(keyCode);
         if (keybinding != null) {
            ++keybinding.pressTime;
         }
      }

   }

   public static void setKeyBindState(int var0, boolean var1) {
      if (keyCode != 0) {
         for(KeyBinding keybinding : HASH.lookupAll(keyCode)) {
            if (keybinding != null) {
               keybinding.pressed = pressed;
            }
         }
      }

   }

   public static void updateKeyBindState() {
      for(KeyBinding keybinding : KEYBIND_ARRAY) {
         try {
            setKeyBindState(keybinding.keyCode, keybinding.keyCode < 256 && Keyboard.isKeyDown(keybinding.keyCode));
         } catch (IndexOutOfBoundsException var3) {
            ;
         }
      }

   }

   public static void unPressAllKeys() {
      for(KeyBinding keybinding : KEYBIND_ARRAY) {
         keybinding.unpressKey();
      }

   }

   public static void resetKeyBindingArrayAndHash() {
      HASH.clearMap();

      for(KeyBinding keybinding : KEYBIND_ARRAY) {
         HASH.addKey(keybinding.keyCode, keybinding);
      }

   }

   public static Set getKeybinds() {
      return KEYBIND_SET;
   }

   public KeyBinding(String var1, int var2, String var3) {
      this.keyModifierDefault = KeyModifier.NONE;
      this.keyModifier = KeyModifier.NONE;
      this.keyConflictContext = KeyConflictContext.UNIVERSAL;
      this.keyDescription = description;
      this.keyCode = keyCode;
      this.keyCodeDefault = keyCode;
      this.keyCategory = category;
      KEYBIND_ARRAY.add(this);
      HASH.addKey(keyCode, this);
      KEYBIND_SET.add(category);
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
      this.keyCode = keyCode;
   }

   public int compareTo(KeyBinding var1) {
      int i = I18n.format(this.keyCategory).compareTo(I18n.format(p_compareTo_1_.keyCategory));
      if (i == 0) {
         i = I18n.format(this.keyDescription).compareTo(I18n.format(p_compareTo_1_.keyDescription));
      }

      return i;
   }

   public KeyBinding(String var1, IKeyConflictContext var2, int var3, String var4) {
      this(description, keyConflictContext, KeyModifier.NONE, keyCode, category);
   }

   public KeyBinding(String var1, IKeyConflictContext var2, KeyModifier var3, int var4, String var5) {
      this.keyModifierDefault = KeyModifier.NONE;
      this.keyModifier = KeyModifier.NONE;
      this.keyConflictContext = KeyConflictContext.UNIVERSAL;
      this.keyDescription = description;
      this.keyCode = keyCode;
      this.keyCodeDefault = keyCode;
      this.keyCategory = category;
      this.keyConflictContext = keyConflictContext;
      this.keyModifier = keyModifier;
      this.keyModifierDefault = keyModifier;
      if (this.keyModifier.matches(keyCode)) {
         this.keyModifier = KeyModifier.NONE;
      }

      KEYBIND_ARRAY.add(this);
      HASH.addKey(keyCode, this);
      KEYBIND_SET.add(category);
   }

   public boolean isActiveAndMatches(int var1) {
      return keyCode != 0 && keyCode == this.getKeyCode() && this.getKeyConflictContext().isActive() && this.getKeyModifier().isActive(this.getKeyConflictContext());
   }

   public void setKeyConflictContext(IKeyConflictContext var1) {
      this.keyConflictContext = keyConflictContext;
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
      this.keyCode = keyCode;
      if (keyModifier.matches(keyCode)) {
         keyModifier = KeyModifier.NONE;
      }

      HASH.removeKey(this);
      this.keyModifier = keyModifier;
      HASH.addKey(keyCode, this);
   }

   public void setToDefault() {
      this.setKeyModifierAndCode(this.getKeyModifierDefault(), this.getKeyCodeDefault());
   }

   public boolean isSetToDefaultValue() {
      return this.getKeyCode() == this.getKeyCodeDefault() && this.getKeyModifier() == this.getKeyModifierDefault();
   }

   public boolean conflicts(KeyBinding var1) {
      if (this.getKeyConflictContext().conflicts(other.getKeyConflictContext()) || other.getKeyConflictContext().conflicts(this.getKeyConflictContext())) {
         KeyModifier keyModifier = this.getKeyModifier();
         KeyModifier otherKeyModifier = other.getKeyModifier();
         if (keyModifier.matches(other.getKeyCode()) || otherKeyModifier.matches(this.getKeyCode())) {
            return true;
         }

         if (this.getKeyCode() == other.getKeyCode()) {
            return keyModifier == otherKeyModifier || this.getKeyConflictContext().conflicts(KeyConflictContext.IN_GAME) && (keyModifier == KeyModifier.NONE || otherKeyModifier == KeyModifier.NONE);
         }
      }

      return false;
   }

   public boolean hasKeyCodeModifierConflict(KeyBinding var1) {
      return (this.getKeyConflictContext().conflicts(other.getKeyConflictContext()) || other.getKeyConflictContext().conflicts(this.getKeyConflictContext())) && (this.getKeyModifier().matches(other.getKeyCode()) || other.getKeyModifier().matches(this.getKeyCode()));
   }

   public String getDisplayName() {
      return this.getKeyModifier().getLocalizedComboName(this.getKeyCode());
   }
}
