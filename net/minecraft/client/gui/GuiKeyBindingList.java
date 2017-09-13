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
      super(var2, var1.width + 45, var1.height, 63, var1.height - 32, 20);
      this.controlsScreen = var1;
      this.mc = var2;
      KeyBinding[] var3 = (KeyBinding[])ArrayUtils.clone(var2.gameSettings.keyBindings);
      this.listEntries = new GuiListExtended.IGuiListEntry[var3.length + KeyBinding.getKeybinds().size()];
      Arrays.sort(var3);
      int var4 = 0;
      String var5 = null;

      for(KeyBinding var9 : var3) {
         String var10 = var9.getKeyCategory();
         if (!var10.equals(var5)) {
            var5 = var10;
            this.listEntries[var4++] = new GuiKeyBindingList.CategoryEntry(var10);
         }

         int var11 = var2.fontRendererObj.getStringWidth(I18n.format(var9.getKeyDescription()));
         if (var11 > this.maxListLabelWidth) {
            this.maxListLabelWidth = var11;
         }

         this.listEntries[var4++] = new GuiKeyBindingList.KeyEntry(var9);
      }

   }

   protected int getSize() {
      return this.listEntries.length;
   }

   public GuiListExtended.IGuiListEntry getListEntry(int var1) {
      return this.listEntries[var1];
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
         this.labelText = I18n.format(var2);
         this.labelWidth = GuiKeyBindingList.this.mc.fontRendererObj.getStringWidth(this.labelText);
      }

      public void drawEntry(int var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
         GuiKeyBindingList.this.mc.fontRendererObj.drawString(this.labelText, GuiKeyBindingList.this.mc.currentScreen.width / 2 - this.labelWidth / 2, var3 + var5 - GuiKeyBindingList.this.mc.fontRendererObj.FONT_HEIGHT - 1, 16777215);
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
         this.keybinding = var2;
         this.keyDesc = I18n.format(var2.getKeyDescription());
         this.btnChangeKeyBinding = new GuiButton(0, 0, 0, 95, 20, I18n.format(var2.getKeyDescription()));
         this.btnReset = new GuiButton(0, 0, 0, 50, 20, I18n.format("controls.reset"));
      }

      public void drawEntry(int var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
         boolean var9 = GuiKeyBindingList.this.controlsScreen.buttonId == this.keybinding;
         GuiKeyBindingList.this.mc.fontRendererObj.drawString(this.keyDesc, var2 + 90 - GuiKeyBindingList.this.maxListLabelWidth, var3 + var5 / 2 - GuiKeyBindingList.this.mc.fontRendererObj.FONT_HEIGHT / 2, 16777215);
         this.btnReset.xPosition = var2 + 210;
         this.btnReset.yPosition = var3;
         this.btnReset.enabled = !this.keybinding.isSetToDefaultValue();
         this.btnReset.drawButton(GuiKeyBindingList.this.mc, var6, var7);
         this.btnChangeKeyBinding.xPosition = var2 + 105;
         this.btnChangeKeyBinding.yPosition = var3;
         this.btnChangeKeyBinding.displayString = this.keybinding.getDisplayName();
         boolean var10 = false;
         boolean var11 = true;
         if (this.keybinding.getKeyCode() != 0) {
            for(KeyBinding var15 : GuiKeyBindingList.this.mc.gameSettings.keyBindings) {
               if (var15 != this.keybinding && var15.conflicts(this.keybinding)) {
                  var10 = true;
                  var11 &= var15.hasKeyCodeModifierConflict(this.keybinding);
               }
            }
         }

         if (var9) {
            this.btnChangeKeyBinding.displayString = TextFormatting.WHITE + "> " + TextFormatting.YELLOW + this.btnChangeKeyBinding.displayString + TextFormatting.WHITE + " <";
         } else if (var10) {
            this.btnChangeKeyBinding.displayString = (var11 ? TextFormatting.GOLD : TextFormatting.RED) + this.btnChangeKeyBinding.displayString;
         }

         this.btnChangeKeyBinding.drawButton(GuiKeyBindingList.this.mc, var6, var7);
      }

      public boolean mousePressed(int var1, int var2, int var3, int var4, int var5, int var6) {
         if (this.btnChangeKeyBinding.mousePressed(GuiKeyBindingList.this.mc, var2, var3)) {
            GuiKeyBindingList.this.controlsScreen.buttonId = this.keybinding;
            return true;
         } else if (this.btnReset.mousePressed(GuiKeyBindingList.this.mc, var2, var3)) {
            this.keybinding.setToDefault();
            GuiKeyBindingList.this.mc.gameSettings.setOptionKeyBinding(this.keybinding, this.keybinding.getKeyCodeDefault());
            KeyBinding.resetKeyBindingArrayAndHash();
            return true;
         } else {
            return false;
         }
      }

      public void mouseReleased(int var1, int var2, int var3, int var4, int var5, int var6) {
         this.btnChangeKeyBinding.mouseReleased(var2, var3);
         this.btnReset.mouseReleased(var2, var3);
      }

      public void setSelected(int var1, int var2, int var3) {
      }
   }
}
