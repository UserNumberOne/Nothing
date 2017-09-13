package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.io.IOException;
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
      this.proxy = proxyIn;
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
      super.drawCenteredString(this.fontRendererObj, p_154325_1_, p_154325_2_, p_154325_3_, p_154325_4_);
   }

   public void drawString(String var1, int var2, int var3, int var4, boolean var5) {
      if (p_154322_5_) {
         super.drawString(this.fontRendererObj, p_154322_1_, p_154322_2_, p_154322_3_, p_154322_4_);
      } else {
         this.fontRendererObj.drawString(p_154322_1_, p_154322_2_, p_154322_3_, p_154322_4_);
      }

   }

   public void drawTexturedModalRect(int var1, int var2, int var3, int var4, int var5, int var6) {
      this.proxy.blit(x, y, textureX, textureY, width, height);
      super.drawTexturedModalRect(x, y, textureX, textureY, width, height);
   }

   public void drawGradientRect(int var1, int var2, int var3, int var4, int var5, int var6) {
      super.drawGradientRect(left, top, right, bottom, startColor, endColor);
   }

   public void drawDefaultBackground() {
      super.drawDefaultBackground();
   }

   public boolean doesGuiPauseGame() {
      return super.doesGuiPauseGame();
   }

   public void drawWorldBackground(int var1) {
      super.drawWorldBackground(tint);
   }

   public void drawScreen(int var1, int var2, float var3) {
      this.proxy.render(mouseX, mouseY, partialTicks);
   }

   public void renderToolTip(ItemStack var1, int var2, int var3) {
      super.renderToolTip(stack, x, y);
   }

   public void drawCreativeTabHoveringText(String var1, int var2, int var3) {
      super.drawCreativeTabHoveringText(tabName, mouseX, mouseY);
   }

   public void drawHoveringText(List var1, int var2, int var3) {
      super.drawHoveringText(textLines, x, y);
   }

   public void updateScreen() {
      this.proxy.tick();
      super.updateScreen();
   }

   public int getFontHeight() {
      return this.fontRendererObj.FONT_HEIGHT;
   }

   public int getStringWidth(String var1) {
      return this.fontRendererObj.getStringWidth(p_154326_1_);
   }

   public void fontDrawShadow(String var1, int var2, int var3, int var4) {
      this.fontRendererObj.drawStringWithShadow(p_154319_1_, (float)p_154319_2_, (float)p_154319_3_, p_154319_4_);
   }

   public List fontSplit(String var1, int var2) {
      return this.fontRendererObj.listFormattedStringToWidth(p_154323_1_, p_154323_2_);
   }

   public final void actionPerformed(GuiButton var1) throws IOException {
      this.proxy.buttonClicked(((GuiButtonRealmsProxy)button).getRealmsButton());
   }

   public void buttonsClear() {
      super.buttonList.clear();
   }

   public void buttonsAdd(RealmsButton var1) {
      super.buttonList.add(button.getProxy());
   }

   public List buttons() {
      List list = Lists.newArrayListWithExpectedSize(super.buttonList.size());

      for(GuiButton guibutton : super.buttonList) {
         list.add(((GuiButtonRealmsProxy)guibutton).getRealmsButton());
      }

      return list;
   }

   public void buttonsRemove(RealmsButton var1) {
      super.buttonList.remove(button.getProxy());
   }

   public void mouseClicked(int var1, int var2, int var3) throws IOException {
      this.proxy.mouseClicked(mouseX, mouseY, mouseButton);
      super.mouseClicked(mouseX, mouseY, mouseButton);
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
      this.proxy.mouseReleased(mouseX, mouseY, state);
   }

   public void mouseClickMove(int var1, int var2, int var3, long var4) {
      this.proxy.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
   }

   public void keyTyped(char var1, int var2) throws IOException {
      this.proxy.keyPressed(typedChar, keyCode);
   }

   public void confirmClicked(boolean var1, int var2) {
      this.proxy.confirmResult(result, id);
   }

   public void onGuiClosed() {
      this.proxy.removed();
      super.onGuiClosed();
   }
}
