package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiScreenRealmsProxy extends GuiScreen {
   private final RealmsScreen proxy;

   public GuiScreenRealmsProxy(RealmsScreen var1) {
      this.proxy = var1;
      super.buttonList = Collections.synchronizedList(Lists.newArrayList());
   }

   public RealmsScreen getProxy() {
      return this.proxy;
   }

   public void initGui() {
      this.proxy.init();
      super.initGui();
   }

   public void drawCenteredString(String var1, int var2, int var3, int var4) {
      super.drawCenteredString(this.fontRendererObj, var1, var2, var3, var4);
   }

   public void drawString(String var1, int var2, int var3, int var4, boolean var5) {
      if (var5) {
         super.drawString(this.fontRendererObj, var1, var2, var3, var4);
      } else {
         this.fontRendererObj.drawString(var1, var2, var3, var4);
      }

   }

   public void drawTexturedModalRect(int var1, int var2, int var3, int var4, int var5, int var6) {
      this.proxy.blit(var1, var2, var3, var4, var5, var6);
      super.drawTexturedModalRect(var1, var2, var3, var4, var5, var6);
   }

   public void drawGradientRect(int var1, int var2, int var3, int var4, int var5, int var6) {
      super.drawGradientRect(var1, var2, var3, var4, var5, var6);
   }

   public void drawDefaultBackground() {
      super.drawDefaultBackground();
   }

   public boolean doesGuiPauseGame() {
      return super.doesGuiPauseGame();
   }

   public void drawWorldBackground(int var1) {
      super.drawWorldBackground(var1);
   }

   public void drawScreen(int var1, int var2, float var3) {
      this.proxy.render(var1, var2, var3);
   }

   public void renderToolTip(ItemStack var1, int var2, int var3) {
      super.renderToolTip(var1, var2, var3);
   }

   public void drawCreativeTabHoveringText(String var1, int var2, int var3) {
      super.drawCreativeTabHoveringText(var1, var2, var3);
   }

   public void drawHoveringText(List var1, int var2, int var3) {
      super.drawHoveringText(var1, var2, var3);
   }

   public void updateScreen() {
      this.proxy.tick();
      super.updateScreen();
   }

   public int getFontHeight() {
      return this.fontRendererObj.FONT_HEIGHT;
   }

   public int getStringWidth(String var1) {
      return this.fontRendererObj.getStringWidth(var1);
   }

   public void fontDrawShadow(String var1, int var2, int var3, int var4) {
      this.fontRendererObj.drawStringWithShadow(var1, (float)var2, (float)var3, var4);
   }

   public List fontSplit(String var1, int var2) {
      return this.fontRendererObj.listFormattedStringToWidth(var1, var2);
   }

   public final void actionPerformed(GuiButton var1) throws IOException {
      this.proxy.buttonClicked(((GuiButtonRealmsProxy)var1).getRealmsButton());
   }

   public void buttonsClear() {
      super.buttonList.clear();
   }

   public void buttonsAdd(RealmsButton var1) {
      super.buttonList.add(var1.getProxy());
   }

   public List buttons() {
      ArrayList var1 = Lists.newArrayListWithExpectedSize(super.buttonList.size());

      for(GuiButton var3 : super.buttonList) {
         var1.add(((GuiButtonRealmsProxy)var3).getRealmsButton());
      }

      return var1;
   }

   public void buttonsRemove(RealmsButton var1) {
      super.buttonList.remove(var1.getProxy());
   }

   public void mouseClicked(int var1, int var2, int var3) throws IOException {
      this.proxy.mouseClicked(var1, var2, var3);
      super.mouseClicked(var1, var2, var3);
   }

   public void handleMouseInput() throws IOException {
      this.proxy.mouseEvent();
      super.handleMouseInput();
   }

   public void handleKeyboardInput() throws IOException {
      this.proxy.keyboardEvent();
      super.handleKeyboardInput();
   }

   public void mouseReleased(int var1, int var2, int var3) {
      this.proxy.mouseReleased(var1, var2, var3);
   }

   public void mouseClickMove(int var1, int var2, int var3, long var4) {
      this.proxy.mouseDragged(var1, var2, var3, var4);
   }

   public void keyTyped(char var1, int var2) throws IOException {
      this.proxy.keyPressed(var1, var2);
   }

   public void confirmClicked(boolean var1, int var2) {
      this.proxy.confirmResult(var1, var2);
   }

   public void onGuiClosed() {
      this.proxy.removed();
      super.onGuiClosed();
   }
}
