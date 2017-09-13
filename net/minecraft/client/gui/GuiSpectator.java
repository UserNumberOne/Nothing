package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.spectator.ISpectatorMenuObject;
import net.minecraft.client.gui.spectator.ISpectatorMenuRecipient;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.categories.SpectatorDetails;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSpectator extends Gui implements ISpectatorMenuRecipient {
   private static final ResourceLocation WIDGETS = new ResourceLocation("textures/gui/widgets.png");
   public static final ResourceLocation SPECTATOR_WIDGETS = new ResourceLocation("textures/gui/spectator_widgets.png");
   private final Minecraft mc;
   private long lastSelectionTime;
   private SpectatorMenu menu;

   public GuiSpectator(Minecraft var1) {
      this.mc = var1;
   }

   public void onHotbarSelected(int var1) {
      this.lastSelectionTime = Minecraft.getSystemTime();
      if (this.menu != null) {
         this.menu.selectSlot(var1);
      } else {
         this.menu = new SpectatorMenu(this);
      }

   }

   private float getHotbarAlpha() {
      long var1 = this.lastSelectionTime - Minecraft.getSystemTime() + 5000L;
      return MathHelper.clamp((float)var1 / 2000.0F, 0.0F, 1.0F);
   }

   public void renderTooltip(ScaledResolution var1, float var2) {
      if (this.menu != null) {
         float var3 = this.getHotbarAlpha();
         if (var3 <= 0.0F) {
            this.menu.exit();
         } else {
            int var4 = var1.getScaledWidth() / 2;
            float var5 = this.zLevel;
            this.zLevel = -90.0F;
            float var6 = (float)var1.getScaledHeight() - 22.0F * var3;
            SpectatorDetails var7 = this.menu.getCurrentPage();
            this.renderPage(var1, var3, var4, var6, var7);
            this.zLevel = var5;
         }
      }

   }

   protected void renderPage(ScaledResolution var1, float var2, int var3, float var4, SpectatorDetails var5) {
      GlStateManager.enableRescaleNormal();
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      GlStateManager.color(1.0F, 1.0F, 1.0F, var2);
      this.mc.getTextureManager().bindTexture(WIDGETS);
      this.drawTexturedModalRect((float)(var3 - 91), var4, 0, 0, 182, 22);
      if (var5.getSelectedSlot() >= 0) {
         this.drawTexturedModalRect((float)(var3 - 91 - 1 + var5.getSelectedSlot() * 20), var4 - 1.0F, 0, 22, 24, 22);
      }

      RenderHelper.enableGUIStandardItemLighting();

      for(int var6 = 0; var6 < 9; ++var6) {
         this.renderSlot(var6, var1.getScaledWidth() / 2 - 90 + var6 * 20 + 2, var4 + 3.0F, var2, var5.getObject(var6));
      }

      RenderHelper.disableStandardItemLighting();
      GlStateManager.disableRescaleNormal();
      GlStateManager.disableBlend();
   }

   private void renderSlot(int var1, int var2, float var3, float var4, ISpectatorMenuObject var5) {
      this.mc.getTextureManager().bindTexture(SPECTATOR_WIDGETS);
      if (var5 != SpectatorMenu.EMPTY_SLOT) {
         int var6 = (int)(var4 * 255.0F);
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)var2, var3, 0.0F);
         float var7 = var5.isEnabled() ? 1.0F : 0.25F;
         GlStateManager.color(var7, var7, var7, var4);
         var5.renderIcon(var7, var6);
         GlStateManager.popMatrix();
         String var8 = String.valueOf(this.mc.gameSettings.keyBindsHotbar[var1].getDisplayName());
         if (var6 > 3 && var5.isEnabled()) {
            this.mc.fontRendererObj.drawStringWithShadow(var8, (float)(var2 + 19 - 2 - this.mc.fontRendererObj.getStringWidth(var8)), var3 + 6.0F + 3.0F, 16777215 + (var6 << 24));
         }
      }

   }

   public void renderSelectedItem(ScaledResolution var1) {
      int var2 = (int)(this.getHotbarAlpha() * 255.0F);
      if (var2 > 3 && this.menu != null) {
         ISpectatorMenuObject var3 = this.menu.getSelectedItem();
         String var4 = var3 == SpectatorMenu.EMPTY_SLOT ? this.menu.getSelectedCategory().getPrompt().getFormattedText() : var3.getSpectatorName().getFormattedText();
         if (var4 != null) {
            int var5 = (var1.getScaledWidth() - this.mc.fontRendererObj.getStringWidth(var4)) / 2;
            int var6 = var1.getScaledHeight() - 35;
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            this.mc.fontRendererObj.drawStringWithShadow(var4, (float)var5, (float)var6, 16777215 + (var2 << 24));
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
         }
      }

   }

   public void onSpectatorMenuClosed(SpectatorMenu var1) {
      this.menu = null;
      this.lastSelectionTime = 0L;
   }

   public boolean isMenuActive() {
      return this.menu != null;
   }

   public void onMouseScroll(int var1) {
      int var2;
      for(var2 = this.menu.getSelectedSlot() + var1; var2 >= 0 && var2 <= 8 && (this.menu.getItem(var2) == SpectatorMenu.EMPTY_SLOT || !this.menu.getItem(var2).isEnabled()); var2 += var1) {
         ;
      }

      if (var2 >= 0 && var2 <= 8) {
         this.menu.selectSlot(var2);
         this.lastSelectionTime = Minecraft.getSystemTime();
      }

   }

   public void onMiddleClick() {
      this.lastSelectionTime = Minecraft.getSystemTime();
      if (this.isMenuActive()) {
         int var1 = this.menu.getSelectedSlot();
         if (var1 != -1) {
            this.menu.selectSlot(var1);
         }
      } else {
         this.menu = new SpectatorMenu(this);
      }

   }
}
