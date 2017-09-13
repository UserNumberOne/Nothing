package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiControls extends GuiScreen {
   private static final GameSettings.Options[] OPTIONS_ARR = new GameSettings.Options[]{GameSettings.Options.INVERT_MOUSE, GameSettings.Options.SENSITIVITY, GameSettings.Options.TOUCHSCREEN, GameSettings.Options.AUTO_JUMP};
   private final GuiScreen parentScreen;
   protected String screenTitle = "Controls";
   private final GameSettings options;
   public KeyBinding buttonId;
   public long time;
   private GuiKeyBindingList keyBindingList;
   private GuiButton buttonReset;

   public GuiControls(GuiScreen var1, GameSettings var2) {
      this.parentScreen = var1;
      this.options = var2;
   }

   public void initGui() {
      this.keyBindingList = new GuiKeyBindingList(this, this.mc);
      this.buttonList.add(new GuiButton(200, this.width / 2 - 155, this.height - 29, 150, 20, I18n.format("gui.done")));
      this.buttonReset = this.addButton(new GuiButton(201, this.width / 2 - 155 + 160, this.height - 29, 150, 20, I18n.format("controls.resetAll")));
      this.screenTitle = I18n.format("controls.title");
      int var1 = 0;

      for(GameSettings.Options var5 : OPTIONS_ARR) {
         if (var5.getEnumFloat()) {
            this.buttonList.add(new GuiOptionSlider(var5.returnEnumOrdinal(), this.width / 2 - 155 + var1 % 2 * 160, 18 + 24 * (var1 >> 1), var5));
         } else {
            this.buttonList.add(new GuiOptionButton(var5.returnEnumOrdinal(), this.width / 2 - 155 + var1 % 2 * 160, 18 + 24 * (var1 >> 1), var5, this.options.getKeyBinding(var5)));
         }

         ++var1;
      }

   }

   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      this.keyBindingList.handleMouseInput();
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      if (var1.id == 200) {
         this.mc.displayGuiScreen(this.parentScreen);
      } else if (var1.id == 201) {
         for(KeyBinding var5 : this.mc.gameSettings.keyBindings) {
            var5.setToDefault();
         }

         KeyBinding.resetKeyBindingArrayAndHash();
      } else if (var1.id < 100 && var1 instanceof GuiOptionButton) {
         this.options.setOptionValue(((GuiOptionButton)var1).returnEnumOptions(), 1);
         var1.displayString = this.options.getKeyBinding(GameSettings.Options.getEnumOptions(var1.id));
      }

   }

   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      if (this.buttonId != null) {
         this.buttonId.setKeyModifierAndCode(KeyModifier.getActiveModifier(), -100 + var3);
         this.options.setOptionKeyBinding(this.buttonId, -100 + var3);
         this.buttonId = null;
         KeyBinding.resetKeyBindingArrayAndHash();
      } else if (var3 != 0 || !this.keyBindingList.mouseClicked(var1, var2, var3)) {
         super.mouseClicked(var1, var2, var3);
      }

   }

   protected void mouseReleased(int var1, int var2, int var3) {
      if (var3 != 0 || !this.keyBindingList.mouseReleased(var1, var2, var3)) {
         super.mouseReleased(var1, var2, var3);
      }

   }

   protected void keyTyped(char var1, int var2) throws IOException {
      if (this.buttonId != null) {
         if (var2 == 1) {
            this.buttonId.setKeyModifierAndCode(KeyModifier.NONE, 0);
            this.options.setOptionKeyBinding(this.buttonId, 0);
         } else if (var2 != 0) {
            this.buttonId.setKeyModifierAndCode(KeyModifier.getActiveModifier(), var2);
            this.options.setOptionKeyBinding(this.buttonId, var2);
         } else if (var1 > 0) {
            this.buttonId.setKeyModifierAndCode(KeyModifier.getActiveModifier(), var1 + 256);
            this.options.setOptionKeyBinding(this.buttonId, var1 + 256);
         }

         if (!KeyModifier.isKeyCodeModifier(var2)) {
            this.buttonId = null;
         }

         this.time = Minecraft.getSystemTime();
         KeyBinding.resetKeyBindingArrayAndHash();
      } else {
         super.keyTyped(var1, var2);
      }

   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      this.keyBindingList.drawScreen(var1, var2, var3);
      this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 8, 16777215);
      boolean var4 = false;

      for(KeyBinding var8 : this.options.keyBindings) {
         if (!var8.isSetToDefaultValue()) {
            var4 = true;
            break;
         }
      }

      this.buttonReset.enabled = var4;
      super.drawScreen(var1, var2, var3);
   }
}
