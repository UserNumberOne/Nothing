package net.minecraft.client.gui;

import java.io.IOException;
import java.net.URI;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class GuiScreenDemo extends GuiScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final ResourceLocation DEMO_BACKGROUND_LOCATION = new ResourceLocation("textures/gui/demo_background.png");

   public void initGui() {
      this.buttonList.clear();
      boolean var1 = true;
      this.buttonList.add(new GuiButton(1, this.width / 2 - 116, this.height / 2 + 62 + -16, 114, 20, I18n.format("demo.help.buy")));
      this.buttonList.add(new GuiButton(2, this.width / 2 + 2, this.height / 2 + 62 + -16, 114, 20, I18n.format("demo.help.later")));
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      switch(var1.id) {
      case 1:
         var1.enabled = false;

         try {
            Class var2 = Class.forName("java.awt.Desktop");
            Object var3 = var2.getMethod("getDesktop").invoke((Object)null);
            var2.getMethod("browse", URI.class).invoke(var3, new URI("http://www.minecraft.net/store?source=demo"));
         } catch (Throwable var4) {
            LOGGER.error("Couldn't open link", var4);
         }
         break;
      case 2:
         this.mc.displayGuiScreen((GuiScreen)null);
         this.mc.setIngameFocus();
      }

   }

   public void updateScreen() {
      super.updateScreen();
   }

   public void drawDefaultBackground() {
      super.drawDefaultBackground();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(DEMO_BACKGROUND_LOCATION);
      int var1 = (this.width - 248) / 2;
      int var2 = (this.height - 166) / 2;
      this.drawTexturedModalRect(var1, var2, 0, 0, 248, 166);
   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      int var4 = (this.width - 248) / 2 + 10;
      int var5 = (this.height - 166) / 2 + 8;
      this.fontRendererObj.drawString(I18n.format("demo.help.title"), var4, var5, 2039583);
      var5 = var5 + 12;
      GameSettings var6 = this.mc.gameSettings;
      this.fontRendererObj.drawString(I18n.format("demo.help.movementShort", var6.keyBindForward.getDisplayName(), var6.keyBindLeft.getDisplayName(), var6.keyBindBack.getDisplayName(), var6.keyBindRight.getDisplayName()), var4, var5, 5197647);
      this.fontRendererObj.drawString(I18n.format("demo.help.movementMouse"), var4, var5 + 12, 5197647);
      this.fontRendererObj.drawString(I18n.format("demo.help.jump", var6.keyBindJump.getDisplayName()), var4, var5 + 24, 5197647);
      this.fontRendererObj.drawString(I18n.format("demo.help.inventory", var6.keyBindInventory.getDisplayName()), var4, var5 + 36, 5197647);
      this.fontRendererObj.drawSplitString(I18n.format("demo.help.fullWrapped"), var4, var5 + 68, 218, 2039583);
      super.drawScreen(var1, var2, var3);
   }
}
