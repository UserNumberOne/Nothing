package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLabel extends Gui {
   protected int width = 200;
   protected int height = 20;
   public int x;
   public int y;
   private final List labels;
   public int id;
   private boolean centered;
   public boolean visible = true;
   private boolean labelBgEnabled;
   private final int textColor;
   private int backColor;
   private int ulColor;
   private int brColor;
   private final FontRenderer fontRenderer;
   private int border;

   public GuiLabel(FontRenderer var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      this.fontRenderer = var1;
      this.id = var2;
      this.x = var3;
      this.y = var4;
      this.width = var5;
      this.height = var6;
      this.labels = Lists.newArrayList();
      this.centered = false;
      this.labelBgEnabled = false;
      this.textColor = var7;
      this.backColor = -1;
      this.ulColor = -1;
      this.brColor = -1;
      this.border = 0;
   }

   public void addLine(String var1) {
      this.labels.add(I18n.format(var1));
   }

   public GuiLabel setCentered() {
      this.centered = true;
      return this;
   }

   public void drawLabel(Minecraft var1, int var2, int var3) {
      if (this.visible) {
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
         this.drawLabelBackground(var1, var2, var3);
         int var4 = this.y + this.height / 2 + this.border / 2;
         int var5 = var4 - this.labels.size() * 10 / 2;

         for(int var6 = 0; var6 < this.labels.size(); ++var6) {
            if (this.centered) {
               this.drawCenteredString(this.fontRenderer, (String)this.labels.get(var6), this.x + this.width / 2, var5 + var6 * 10, this.textColor);
            } else {
               this.drawString(this.fontRenderer, (String)this.labels.get(var6), this.x, var5 + var6 * 10, this.textColor);
            }
         }
      }

   }

   protected void drawLabelBackground(Minecraft var1, int var2, int var3) {
      if (this.labelBgEnabled) {
         int var4 = this.width + this.border * 2;
         int var5 = this.height + this.border * 2;
         int var6 = this.x - this.border;
         int var7 = this.y - this.border;
         drawRect(var6, var7, var6 + var4, var7 + var5, this.backColor);
         this.drawHorizontalLine(var6, var6 + var4, var7, this.ulColor);
         this.drawHorizontalLine(var6, var6 + var4, var7 + var5, this.brColor);
         this.drawVerticalLine(var6, var7, var7 + var5, this.ulColor);
         this.drawVerticalLine(var6 + var4, var7, var7 + var5, this.brColor);
      }

   }
}
