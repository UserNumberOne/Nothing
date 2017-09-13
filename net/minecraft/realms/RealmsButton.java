package net.minecraft.realms;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonRealmsProxy;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RealmsButton {
   protected static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
   private final GuiButtonRealmsProxy proxy;

   public RealmsButton(int var1, int var2, int var3, String var4) {
      this.proxy = new GuiButtonRealmsProxy(this, buttonId, x, y, text);
   }

   public RealmsButton(int var1, int var2, int var3, int var4, int var5, String var6) {
      this.proxy = new GuiButtonRealmsProxy(this, buttonId, x, y, text, widthIn, heightIn);
   }

   public GuiButton getProxy() {
      return this.proxy;
   }

   public int id() {
      return this.proxy.getId();
   }

   public boolean active() {
      return this.proxy.getEnabled();
   }

   public void active(boolean var1) {
      this.proxy.setEnabled(p_active_1_);
   }

   public void msg(String var1) {
      this.proxy.setText(p_msg_1_);
   }

   public int getWidth() {
      return this.proxy.getButtonWidth();
   }

   public int getHeight() {
      return this.proxy.getHeight();
   }

   public int y() {
      return this.proxy.getPositionY();
   }

   public void render(int var1, int var2) {
      this.proxy.drawButton(Minecraft.getMinecraft(), p_render_1_, p_render_2_);
   }

   public void clicked(int var1, int var2) {
   }

   public void released(int var1, int var2) {
   }

   public void blit(int var1, int var2, int var3, int var4, int var5, int var6) {
      this.proxy.drawTexturedModalRect(p_blit_1_, p_blit_2_, p_blit_3_, p_blit_4_, p_blit_5_, p_blit_6_);
   }

   public void renderBg(int var1, int var2) {
   }

   public int getYImage(boolean var1) {
      return this.proxy.getYImage(p_getYImage_1_);
   }
}
