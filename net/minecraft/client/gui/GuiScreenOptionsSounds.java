package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiScreenOptionsSounds extends GuiScreen {
   private final GuiScreen parent;
   private final GameSettings game_settings_4;
   protected String title = "Options";
   private String offDisplayString;

   public GuiScreenOptionsSounds(GuiScreen var1, GameSettings var2) {
      this.parent = var1;
      this.game_settings_4 = var2;
   }

   public void initGui() {
      this.title = I18n.format("options.sounds.title");
      this.offDisplayString = I18n.format("options.off");
      int var1 = 0;
      this.buttonList.add(new GuiScreenOptionsSounds.Button(SoundCategory.MASTER.ordinal(), this.width / 2 - 155 + var1 % 2 * 160, this.height / 6 - 12 + 24 * (var1 >> 1), SoundCategory.MASTER, true));
      var1 = var1 + 2;

      for(SoundCategory var5 : SoundCategory.values()) {
         if (var5 != SoundCategory.MASTER) {
            this.buttonList.add(new GuiScreenOptionsSounds.Button(var5.ordinal(), this.width / 2 - 155 + var1 % 2 * 160, this.height / 6 - 12 + 24 * (var1 >> 1), var5, false));
            ++var1;
         }
      }

      int var8 = this.width / 2 - 75;
      int var9 = this.height / 6 - 12;
      ++var1;
      this.buttonList.add(new GuiOptionButton(201, var8, var9 + 24 * (var1 >> 1), GameSettings.Options.SHOW_SUBTITLES, this.game_settings_4.getKeyBinding(GameSettings.Options.SHOW_SUBTITLES)));
      this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168, I18n.format("gui.done")));
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      if (var1.enabled) {
         if (var1.id == 200) {
            this.mc.gameSettings.saveOptions();
            this.mc.displayGuiScreen(this.parent);
         } else if (var1.id == 201) {
            this.mc.gameSettings.setOptionValue(GameSettings.Options.SHOW_SUBTITLES, 1);
            var1.displayString = this.mc.gameSettings.getKeyBinding(GameSettings.Options.SHOW_SUBTITLES);
            this.mc.gameSettings.saveOptions();
         }
      }

   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 15, 16777215);
      super.drawScreen(var1, var2, var3);
   }

   protected String getDisplayString(SoundCategory var1) {
      float var2 = this.game_settings_4.getSoundLevel(var1);
      return var2 == 0.0F ? this.offDisplayString : (int)(var2 * 100.0F) + "%";
   }

   @SideOnly(Side.CLIENT)
   class Button extends GuiButton {
      private final SoundCategory category;
      private final String categoryName;
      public float volume = 1.0F;
      public boolean pressed;

      public Button(int var2, int var3, int var4, SoundCategory var5, boolean var6) {
         super(var2, var3, var4, var6 ? 310 : 150, 20, "");
         this.category = var5;
         this.categoryName = I18n.format("soundCategory." + var5.getName());
         this.displayString = this.categoryName + ": " + GuiScreenOptionsSounds.this.getDisplayString(var5);
         this.volume = GuiScreenOptionsSounds.this.game_settings_4.getSoundLevel(var5);
      }

      protected int getHoverState(boolean var1) {
         return 0;
      }

      protected void mouseDragged(Minecraft var1, int var2, int var3) {
         if (this.visible) {
            if (this.pressed) {
               this.volume = (float)(var2 - (this.xPosition + 4)) / (float)(this.width - 8);
               this.volume = MathHelper.clamp(this.volume, 0.0F, 1.0F);
               var1.gameSettings.setSoundLevel(this.category, this.volume);
               var1.gameSettings.saveOptions();
               this.displayString = this.categoryName + ": " + GuiScreenOptionsSounds.this.getDisplayString(this.category);
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(this.xPosition + (int)(this.volume * (float)(this.width - 8)), this.yPosition, 0, 66, 4, 20);
            this.drawTexturedModalRect(this.xPosition + (int)(this.volume * (float)(this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
         }

      }

      public boolean mousePressed(Minecraft var1, int var2, int var3) {
         if (super.mousePressed(var1, var2, var3)) {
            this.volume = (float)(var2 - (this.xPosition + 4)) / (float)(this.width - 8);
            this.volume = MathHelper.clamp(this.volume, 0.0F, 1.0F);
            var1.gameSettings.setSoundLevel(this.category, this.volume);
            var1.gameSettings.saveOptions();
            this.displayString = this.categoryName + ": " + GuiScreenOptionsSounds.this.getDisplayString(this.category);
            this.pressed = true;
            return true;
         } else {
            return false;
         }
      }

      public void playPressSound(SoundHandler var1) {
      }

      public void mouseReleased(int var1, int var2) {
         if (this.pressed) {
            GuiScreenOptionsSounds.this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         }

         this.pressed = false;
      }
   }
}
