package net.minecraft.client.gui;

import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

@SideOnly(Side.CLIENT)
public class GuiKeyBindingList extends GuiListExtended {
   private final GuiControls controlsScreen;
   private final Minecraft mc;
   private final GuiListExtended.IGuiListEntry[] listEntries;
   private int maxListLabelWidth;

   public GuiKeyBindingList(GuiControls var1, Minecraft var2) {
      super(mcIn, controls.width + 45, controls.height, 63, controls.height - 32, 20);
      this.controlsScreen = controls;
      this.mc = mcIn;
      KeyBinding[] akeybinding = (KeyBinding[])ArrayUtils.clone(mcIn.gameSettings.keyBindings);
      this.listEntries = new GuiListExtended.IGuiListEntry[akeybinding.length + KeyBinding.getKeybinds().size()];
      Arrays.sort(akeybinding);
      int i = 0;
      String s = null;

      for(KeyBinding keybinding : akeybinding) {
         String s1 = keybinding.getKeyCategory();
         if (!s1.equals(s)) {
            s = s1;
            this.listEntries[i++] = new GuiKeyBindingList.CategoryEntry(s1);
         }

         int j = mcIn.fontRendererObj.getStringWidth(I18n.format(keybinding.getKeyDescription()));
         if (j > this.maxListLabelWidth) {
            this.maxListLabelWidth = j;
         }

         this.listEntries[i++] = new GuiKeyBindingList.KeyEntry(keybinding);
      }

   }

   protected int getSize() {
      return this.listEntries.length;
   }

   public GuiListExtended.IGuiListEntry getListEntry(int var1) {
      return this.listEntries[index];
   }

   protected int getScrollBarX() {
      return super.getScrollBarX() + 35;
   }

   public int getListWidth() {
      return super.getListWidth() + 32;
   }

   @SideOnly(Side.CLIENT)
   public class CategoryEntry implements GuiListExtended.IGuiListEntry {
      private final String labelText;
      private final int labelWidth;

      public CategoryEntry(String var2) {
         this.labelText = I18n.format(name);
         this.labelWidth = GuiKeyBindingList.this.mc.fontRendererObj.getStringWidth(this.labelText);
      }

      public void drawEntry(int var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
         GuiKeyBindingList.this.mc.fontRendererObj.drawString(this.labelText, GuiKeyBindingList.this.mc.currentScreen.width / 2 - this.labelWidth / 2, y + slotHeight - GuiKeyBindingList.this.mc.fontRendererObj.FONT_HEIGHT - 1, 16777215);
      }

      public boolean mousePressed(int var1, int var2, int var3, int var4, int var5, int var6) {
         return false;
      }

      public void mouseReleased(int var1, int var2, int var3, int var4, int var5, int var6) {
      }

      public void setSelected(int var1, int var2, int var3) {
      }
   }

   @SideOnly(Side.CLIENT)
   public class KeyEntry implements GuiListExtended.IGuiListEntry {
      private final KeyBinding keybinding;
      private final String keyDesc;
      private final GuiButton btnChangeKeyBinding;
      private final GuiButton btnReset;

      private KeyEntry(KeyBinding var2) {
         this.keybinding = name;
         this.keyDesc = I18n.format(name.getKeyDescription());
         this.btnChangeKeyBinding = new GuiButton(0, 0, 0, 95, 20, I18n.format(name.getKeyDescription()));
         this.btnReset = new GuiButton(0, 0, 0, 50, 20, I18n.format("controls.reset"));
      }

      public void drawEntry(int var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
         boolean flag = GuiKeyBindingList.this.controlsScreen.buttonId == this.keybinding;
         GuiKeyBindingList.this.mc.fontRendererObj.drawString(this.keyDesc, x + 90 - GuiKeyBindingList.this.maxListLabelWidth, y + slotHeight / 2 - GuiKeyBindingList.this.mc.fontRendererObj.FONT_HEIGHT / 2, 16777215);
         this.btnReset.xPosition = x + 210;
         this.btnReset.yPosition = y;
         this.btnReset.enabled = !this.keybinding.isSetToDefaultValue();
         this.btnReset.drawButton(GuiKeyBindingList.this.mc, mouseX, mouseY);
         this.btnChangeKeyBinding.xPosition = x + 105;
         this.btnChangeKeyBinding.yPosition = y;
         this.btnChangeKeyBinding.displayString = this.keybinding.getDisplayName();
         boolean flag1 = false;
         boolean keyCodeModifierConflict = true;
         if (this.keybinding.getKeyCode() != 0) {
            for(KeyBinding keybinding : GuiKeyBindingList.this.mc.gameSettings.keyBindings) {
               if (keybinding != this.keybinding && keybinding.conflicts(this.keybinding)) {
                  flag1 = true;
                  keyCodeModifierConflict &= keybinding.hasKeyCodeModifierConflict(this.keybinding);
               }
            }
         }

         if (flag) {
            this.btnChangeKeyBinding.displayString = TextFormatting.WHITE + "> " + TextFormatting.YELLOW + this.btnChangeKeyBinding.displayString + TextFormatting.WHITE + " <";
         } else if (flag1) {
            this.btnChangeKeyBinding.displayString = (keyCodeModifierConflict ? TextFormatting.GOLD : TextFormatting.RED) + this.btnChangeKeyBinding.displayString;
         }

         this.btnChangeKeyBinding.drawButton(GuiKeyBindingList.this.mc, mouseX, mouseY);
      }

      public boolean mousePressed(int var1, int var2, int var3, int var4, int var5, int var6) {
         if (this.btnChangeKeyBinding.mousePressed(GuiKeyBindingList.this.mc, mouseX, mouseY)) {
            GuiKeyBindingList.this.controlsScreen.buttonId = this.keybinding;
            return true;
         } else if (this.btnReset.mousePressed(GuiKeyBindingList.this.mc, mouseX, mouseY)) {
            this.keybinding.setToDefault();
            GuiKeyBindingList.this.mc.gameSettings.setOptionKeyBinding(this.keybinding, this.keybinding.getKeyCodeDefault());
            KeyBinding.resetKeyBindingArrayAndHash();
            return true;
         } else {
            return false;
         }
      }

      public void mouseReleased(int var1, int var2, int var3, int var4, int var5, int var6) {
         this.btnChangeKeyBinding.mouseReleased(x, y);
         this.btnReset.mouseReleased(x, y);
      }

      public void setSelected(int var1, int var2, int var3) {
      }
   }
}
