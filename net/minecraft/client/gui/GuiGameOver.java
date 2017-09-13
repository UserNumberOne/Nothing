package net.minecraft.client.gui;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiGameOver extends GuiScreen implements GuiYesNoCallback {
   private int enableButtonsTimer;
   private final ITextComponent causeOfDeath;

   public GuiGameOver(@Nullable ITextComponent var1) {
      this.causeOfDeath = var1;
   }

   public void initGui() {
      this.buttonList.clear();
      this.enableButtonsTimer = 0;
      if (this.mc.world.getWorldInfo().isHardcoreModeEnabled()) {
         this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 72, I18n.format("deathScreen.spectate")));
         this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, I18n.format("deathScreen." + (this.mc.isIntegratedServerRunning() ? "deleteWorld" : "leaveServer"))));
      } else {
         this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 72, I18n.format("deathScreen.respawn")));
         this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, I18n.format("deathScreen.titleScreen")));
         if (this.mc.getSession() == null) {
            ((GuiButton)this.buttonList.get(1)).enabled = false;
         }
      }

      for(GuiButton var2 : this.buttonList) {
         var2.enabled = false;
      }

   }

   protected void keyTyped(char var1, int var2) throws IOException {
   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      switch(var1.id) {
      case 0:
         this.mc.player.respawnPlayer();
         this.mc.displayGuiScreen((GuiScreen)null);
         break;
      case 1:
         if (this.mc.world.getWorldInfo().isHardcoreModeEnabled()) {
            this.mc.displayGuiScreen(new GuiMainMenu());
         } else {
            GuiYesNo var2 = new GuiYesNo(this, I18n.format("deathScreen.quit.confirm"), "", I18n.format("deathScreen.titleScreen"), I18n.format("deathScreen.respawn"), 0);
            this.mc.displayGuiScreen(var2);
            var2.setButtonDelay(20);
         }
      }

   }

   public void confirmClicked(boolean var1, int var2) {
      if (var1) {
         if (this.mc.world != null) {
            this.mc.world.sendQuittingDisconnectingPacket();
         }

         this.mc.loadWorld((WorldClient)null);
         this.mc.displayGuiScreen(new GuiMainMenu());
      } else {
         this.mc.player.respawnPlayer();
         this.mc.displayGuiScreen((GuiScreen)null);
      }

   }

   public void drawScreen(int var1, int var2, float var3) {
      boolean var4 = this.mc.world.getWorldInfo().isHardcoreModeEnabled();
      this.drawGradientRect(0, 0, this.width, this.height, 1615855616, -1602211792);
      GlStateManager.pushMatrix();
      GlStateManager.scale(2.0F, 2.0F, 2.0F);
      this.drawCenteredString(this.fontRendererObj, I18n.format(var4 ? "deathScreen.title.hardcore" : "deathScreen.title"), this.width / 2 / 2, 30, 16777215);
      GlStateManager.popMatrix();
      if (this.causeOfDeath != null) {
         this.drawCenteredString(this.fontRendererObj, this.causeOfDeath.getFormattedText(), this.width / 2, 85, 16777215);
      }

      this.drawCenteredString(this.fontRendererObj, I18n.format("deathScreen.score") + ": " + TextFormatting.YELLOW + this.mc.player.getScore(), this.width / 2, 100, 16777215);
      if (this.causeOfDeath != null && var2 > 85 && var2 < 85 + this.fontRendererObj.FONT_HEIGHT) {
         ITextComponent var5 = this.getClickedComponentAt(var1);
         if (var5 != null && var5.getStyle().getHoverEvent() != null) {
            this.handleComponentHover(var5, var1, var2);
         }
      }

      super.drawScreen(var1, var2, var3);
   }

   @Nullable
   public ITextComponent getClickedComponentAt(int var1) {
      if (this.causeOfDeath == null) {
         return null;
      } else {
         int var2 = this.mc.fontRendererObj.getStringWidth(this.causeOfDeath.getFormattedText());
         int var3 = this.width / 2 - var2 / 2;
         int var4 = this.width / 2 + var2 / 2;
         int var5 = var3;
         if (var1 >= var3 && var1 <= var4) {
            for(ITextComponent var7 : this.causeOfDeath) {
               var5 += this.mc.fontRendererObj.getStringWidth(GuiUtilRenderComponents.removeTextColorsIfConfigured(var7.getUnformattedComponentText(), false));
               if (var5 > var1) {
                  return var7;
               }
            }

            return null;
         } else {
            return null;
         }
      }
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   public void updateScreen() {
      super.updateScreen();
      ++this.enableButtonsTimer;
      if (this.enableButtonsTimer == 20) {
         for(GuiButton var2 : this.buttonList) {
            var2.enabled = true;
         }
      }

   }
}
