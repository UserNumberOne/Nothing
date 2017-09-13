package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiVideoSettings extends GuiScreen {
   private final GuiScreen parentGuiScreen;
   protected String screenTitle = "Video Settings";
   private final GameSettings guiGameSettings;
   private GuiListExtended optionsRowList;
   private static final GameSettings.Options[] VIDEO_OPTIONS = new GameSettings.Options[]{GameSettings.Options.GRAPHICS, GameSettings.Options.RENDER_DISTANCE, GameSettings.Options.AMBIENT_OCCLUSION, GameSettings.Options.FRAMERATE_LIMIT, GameSettings.Options.ANAGLYPH, GameSettings.Options.VIEW_BOBBING, GameSettings.Options.GUI_SCALE, GameSettings.Options.ATTACK_INDICATOR, GameSettings.Options.GAMMA, GameSettings.Options.RENDER_CLOUDS, GameSettings.Options.PARTICLES, GameSettings.Options.USE_FULLSCREEN, GameSettings.Options.ENABLE_VSYNC, GameSettings.Options.MIPMAP_LEVELS, GameSettings.Options.USE_VBO, GameSettings.Options.ENTITY_SHADOWS};

   public GuiVideoSettings(GuiScreen var1, GameSettings var2) {
      this.parentGuiScreen = var1;
      this.guiGameSettings = var2;
   }

   public void initGui() {
      this.screenTitle = I18n.format("options.videoTitle");
      this.buttonList.clear();
      this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height - 27, I18n.format("gui.done")));
      if (OpenGlHelper.vboSupported) {
         this.optionsRowList = new GuiOptionsRowList(this.mc, this.width, this.height, 32, this.height - 32, 25, VIDEO_OPTIONS);
      } else {
         GameSettings.Options[] var1 = new GameSettings.Options[VIDEO_OPTIONS.length - 1];
         int var2 = 0;

         for(GameSettings.Options var6 : VIDEO_OPTIONS) {
            if (var6 == GameSettings.Options.USE_VBO) {
               break;
            }

            var1[var2] = var6;
            ++var2;
         }

         this.optionsRowList = new GuiOptionsRowList(this.mc, this.width, this.height, 32, this.height - 32, 25, var1);
      }

   }

   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      this.optionsRowList.handleMouseInput();
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      if (var1.enabled && var1.id == 200) {
         this.mc.gameSettings.saveOptions();
         this.mc.displayGuiScreen(this.parentGuiScreen);
      }

   }

   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      int var4 = this.guiGameSettings.guiScale;
      super.mouseClicked(var1, var2, var3);
      this.optionsRowList.mouseClicked(var1, var2, var3);
      if (this.guiGameSettings.guiScale != var4) {
         ScaledResolution var5 = new ScaledResolution(this.mc);
         int var6 = var5.getScaledWidth();
         int var7 = var5.getScaledHeight();
         this.setWorldAndResolution(this.mc, var6, var7);
      }

   }

   protected void mouseReleased(int var1, int var2, int var3) {
      int var4 = this.guiGameSettings.guiScale;
      super.mouseReleased(var1, var2, var3);
      this.optionsRowList.mouseReleased(var1, var2, var3);
      if (this.guiGameSettings.guiScale != var4) {
         ScaledResolution var5 = new ScaledResolution(this.mc);
         int var6 = var5.getScaledWidth();
         int var7 = var5.getScaledHeight();
         this.setWorldAndResolution(this.mc, var6, var7);
      }

   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      this.optionsRowList.drawScreen(var1, var2, var3);
      this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 5, 16777215);
      super.drawScreen(var1, var2, var3);
   }

   public void onGuiClosed() {
      super.onGuiClosed();
      this.mc.gameSettings.onGuiClosed();
   }
}
