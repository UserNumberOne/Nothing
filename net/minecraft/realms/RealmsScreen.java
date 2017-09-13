package net.minecraft.realms;

import com.mojang.util.UUIDTypeAdapter;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreenRealmsProxy;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RealmsScreen {
   public static final int SKIN_HEAD_U = 8;
   public static final int SKIN_HEAD_V = 8;
   public static final int SKIN_HEAD_WIDTH = 8;
   public static final int SKIN_HEAD_HEIGHT = 8;
   public static final int SKIN_HAT_U = 40;
   public static final int SKIN_HAT_V = 8;
   public static final int SKIN_HAT_WIDTH = 8;
   public static final int SKIN_HAT_HEIGHT = 8;
   public static final int SKIN_TEX_WIDTH = 64;
   public static final int SKIN_TEX_HEIGHT = 64;
   protected Minecraft minecraft;
   public int width;
   public int height;
   private final GuiScreenRealmsProxy proxy = new GuiScreenRealmsProxy(this);

   public GuiScreenRealmsProxy getProxy() {
      return this.proxy;
   }

   public void init() {
   }

   public void init(Minecraft var1, int var2, int var3) {
   }

   public void drawCenteredString(String var1, int var2, int var3, int var4) {
      this.proxy.drawCenteredString(p_drawCenteredString_1_, p_drawCenteredString_2_, p_drawCenteredString_3_, p_drawCenteredString_4_);
   }

   public void drawString(String var1, int var2, int var3, int var4) {
      this.drawString(p_drawString_1_, p_drawString_2_, p_drawString_3_, p_drawString_4_, true);
   }

   public void drawString(String var1, int var2, int var3, int var4, boolean var5) {
      this.proxy.drawString(p_drawString_1_, p_drawString_2_, p_drawString_3_, p_drawString_4_, false);
   }

   public void blit(int var1, int var2, int var3, int var4, int var5, int var6) {
      this.proxy.drawTexturedModalRect(p_blit_1_, p_blit_2_, p_blit_3_, p_blit_4_, p_blit_5_, p_blit_6_);
   }

   public static void blit(int var0, int var1, float var2, float var3, int var4, int var5, int var6, int var7, float var8, float var9) {
      Gui.drawScaledCustomSizeModalRect(p_blit_0_, p_blit_1_, p_blit_2_, p_blit_3_, p_blit_4_, p_blit_5_, p_blit_6_, p_blit_7_, p_blit_8_, p_blit_9_);
   }

   public static void blit(int var0, int var1, float var2, float var3, int var4, int var5, float var6, float var7) {
      Gui.drawModalRectWithCustomSizedTexture(p_blit_0_, p_blit_1_, p_blit_2_, p_blit_3_, p_blit_4_, p_blit_5_, p_blit_6_, p_blit_7_);
   }

   public void fillGradient(int var1, int var2, int var3, int var4, int var5, int var6) {
      this.proxy.drawGradientRect(p_fillGradient_1_, p_fillGradient_2_, p_fillGradient_3_, p_fillGradient_4_, p_fillGradient_5_, p_fillGradient_6_);
   }

   public void renderBackground() {
      this.proxy.drawDefaultBackground();
   }

   public boolean isPauseScreen() {
      return this.proxy.doesGuiPauseGame();
   }

   public void renderBackground(int var1) {
      this.proxy.drawWorldBackground(p_renderBackground_1_);
   }

   public void render(int var1, int var2, float var3) {
      for(int i = 0; i < this.proxy.buttons().size(); ++i) {
         ((RealmsButton)this.proxy.buttons().get(i)).render(p_render_1_, p_render_2_);
      }

   }

   public void renderTooltip(ItemStack var1, int var2, int var3) {
      this.proxy.renderToolTip(p_renderTooltip_1_, p_renderTooltip_2_, p_renderTooltip_3_);
   }

   public void renderTooltip(String var1, int var2, int var3) {
      this.proxy.drawCreativeTabHoveringText(p_renderTooltip_1_, p_renderTooltip_2_, p_renderTooltip_3_);
   }

   public void renderTooltip(List var1, int var2, int var3) {
      this.proxy.drawHoveringText(p_renderTooltip_1_, p_renderTooltip_2_, p_renderTooltip_3_);
   }

   public static void bindFace(String var0, String var1) {
      ResourceLocation resourcelocation = AbstractClientPlayer.getLocationSkin(p_bindFace_1_);
      if (resourcelocation == null) {
         resourcelocation = DefaultPlayerSkin.getDefaultSkin(UUIDTypeAdapter.fromString(p_bindFace_0_));
      }

      AbstractClientPlayer.getDownloadImageSkin(resourcelocation, p_bindFace_1_);
      Minecraft.getMinecraft().getTextureManager().bindTexture(resourcelocation);
   }

   public static void bind(String var0) {
      ResourceLocation resourcelocation = new ResourceLocation(p_bind_0_);
      Minecraft.getMinecraft().getTextureManager().bindTexture(resourcelocation);
   }

   public void tick() {
   }

   public int width() {
      return this.proxy.width;
   }

   public int height() {
      return this.proxy.height;
   }

   public int fontLineHeight() {
      return this.proxy.getFontHeight();
   }

   public int fontWidth(String var1) {
      return this.proxy.getStringWidth(p_fontWidth_1_);
   }

   public void fontDrawShadow(String var1, int var2, int var3, int var4) {
      this.proxy.fontDrawShadow(p_fontDrawShadow_1_, p_fontDrawShadow_2_, p_fontDrawShadow_3_, p_fontDrawShadow_4_);
   }

   public List fontSplit(String var1, int var2) {
      return this.proxy.fontSplit(p_fontSplit_1_, p_fontSplit_2_);
   }

   public void buttonClicked(RealmsButton var1) {
   }

   public static RealmsButton newButton(int var0, int var1, int var2, String var3) {
      return new RealmsButton(p_newButton_0_, p_newButton_1_, p_newButton_2_, p_newButton_3_);
   }

   public static RealmsButton newButton(int var0, int var1, int var2, int var3, int var4, String var5) {
      return new RealmsButton(p_newButton_0_, p_newButton_1_, p_newButton_2_, p_newButton_3_, p_newButton_4_, p_newButton_5_);
   }

   public void buttonsClear() {
      this.proxy.buttonsClear();
   }

   public void buttonsAdd(RealmsButton var1) {
      this.proxy.buttonsAdd(p_buttonsAdd_1_);
   }

   public List buttons() {
      return this.proxy.buttons();
   }

   public void buttonsRemove(RealmsButton var1) {
      this.proxy.buttonsRemove(p_buttonsRemove_1_);
   }

   public RealmsEditBox newEditBox(int var1, int var2, int var3, int var4, int var5) {
      return new RealmsEditBox(p_newEditBox_1_, p_newEditBox_2_, p_newEditBox_3_, p_newEditBox_4_, p_newEditBox_5_);
   }

   public void mouseClicked(int var1, int var2, int var3) {
   }

   public void mouseEvent() {
   }

   public void keyboardEvent() {
   }

   public void mouseReleased(int var1, int var2, int var3) {
   }

   public void mouseDragged(int var1, int var2, int var3, long var4) {
   }

   public void keyPressed(char var1, int var2) {
   }

   public void confirmResult(boolean var1, int var2) {
   }

   public static String getLocalizedString(String var0) {
      return I18n.format(p_getLocalizedString_0_);
   }

   public static String getLocalizedString(String var0, Object... var1) {
      return I18n.format(p_getLocalizedString_0_, p_getLocalizedString_1_);
   }

   public RealmsAnvilLevelStorageSource getLevelStorageSource() {
      return new RealmsAnvilLevelStorageSource(Minecraft.getMinecraft().getSaveLoader());
   }

   public void removed() {
   }
}
