package net.minecraft.client.gui;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;

@SideOnly(Side.CLIENT)
public class FontRenderer implements IResourceManagerReloadListener {
   private static final ResourceLocation[] UNICODE_PAGE_LOCATIONS = new ResourceLocation[256];
   protected final int[] charWidth = new int[256];
   public int FONT_HEIGHT = 9;
   public Random fontRandom = new Random();
   protected final byte[] glyphWidth = new byte[65536];
   private final int[] colorCode = new int[32];
   protected final ResourceLocation locationFontTexture;
   private final TextureManager renderEngine;
   protected float posX;
   protected float posY;
   private boolean unicodeFlag;
   private boolean bidiFlag;
   private float red;
   private float blue;
   private float green;
   private float alpha;
   private int textColor;
   private boolean randomStyle;
   private boolean boldStyle;
   private boolean italicStyle;
   private boolean underlineStyle;
   private boolean strikethroughStyle;

   public FontRenderer(GameSettings var1, ResourceLocation var2, TextureManager var3, boolean var4) {
      this.locationFontTexture = var2;
      this.renderEngine = var3;
      this.unicodeFlag = var4;
      this.bindTexture(this.locationFontTexture);

      for(int var5 = 0; var5 < 32; ++var5) {
         int var6 = (var5 >> 3 & 1) * 85;
         int var7 = (var5 >> 2 & 1) * 170 + var6;
         int var8 = (var5 >> 1 & 1) * 170 + var6;
         int var9 = (var5 >> 0 & 1) * 170 + var6;
         if (var5 == 6) {
            var7 += 85;
         }

         if (var1.anaglyph) {
            int var10 = (var7 * 30 + var8 * 59 + var9 * 11) / 100;
            int var11 = (var7 * 30 + var8 * 70) / 100;
            int var12 = (var7 * 30 + var9 * 70) / 100;
            var7 = var10;
            var8 = var11;
            var9 = var12;
         }

         if (var5 >= 16) {
            var7 /= 4;
            var8 /= 4;
            var9 /= 4;
         }

         this.colorCode[var5] = (var7 & 255) << 16 | (var8 & 255) << 8 | var9 & 255;
      }

      this.readGlyphSizes();
   }

   public void onResourceManagerReload(IResourceManager var1) {
      this.readFontTexture();
      this.readGlyphSizes();
   }

   private void readFontTexture() {
      IResource var1 = null;

      BufferedImage var2;
      try {
         var1 = this.getResource(this.locationFontTexture);
         var2 = TextureUtil.readBufferedImage(var1.getInputStream());
      } catch (IOException var20) {
         throw new RuntimeException(var20);
      } finally {
         IOUtils.closeQuietly(var1);
      }

      int var3 = var2.getWidth();
      int var4 = var2.getHeight();
      int[] var5 = new int[var3 * var4];
      var2.getRGB(0, 0, var3, var4, var5, 0, var3);
      int var6 = var4 / 16;
      int var7 = var3 / 16;
      boolean var8 = true;
      float var9 = 8.0F / (float)var7;

      for(int var10 = 0; var10 < 256; ++var10) {
         int var11 = var10 % 16;
         int var12 = var10 / 16;
         if (var10 == 32) {
            this.charWidth[var10] = 4;
         }

         int var13;
         for(var13 = var7 - 1; var13 >= 0; --var13) {
            int var14 = var11 * var7 + var13;
            boolean var15 = true;

            for(int var16 = 0; var16 < var6 && var15; ++var16) {
               int var17 = (var12 * var7 + var16) * var3;
               if ((var5[var14 + var17] >> 24 & 255) != 0) {
                  var15 = false;
               }
            }

            if (!var15) {
               break;
            }
         }

         ++var13;
         this.charWidth[var10] = (int)(0.5D + (double)((float)var13 * var9)) + 1;
      }

   }

   private void readGlyphSizes() {
      IResource var1 = null;

      try {
         var1 = this.getResource(new ResourceLocation("font/glyph_sizes.bin"));
         var1.getInputStream().read(this.glyphWidth);
      } catch (IOException var6) {
         throw new RuntimeException(var6);
      } finally {
         IOUtils.closeQuietly(var1);
      }

   }

   private float renderChar(char var1, boolean var2) {
      if (var1 == ' ') {
         return 4.0F;
      } else {
         int var3 = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(var1);
         return var3 != -1 && !this.unicodeFlag ? this.renderDefaultChar(var3, var2) : this.renderUnicodeChar(var1, var2);
      }
   }

   protected float renderDefaultChar(int var1, boolean var2) {
      int var3 = var1 % 16 * 8;
      int var4 = var1 / 16 * 8;
      int var5 = var2 ? 1 : 0;
      this.bindTexture(this.locationFontTexture);
      int var6 = this.charWidth[var1];
      float var7 = (float)var6 - 0.01F;
      GlStateManager.glBegin(5);
      GlStateManager.glTexCoord2f((float)var3 / 128.0F, (float)var4 / 128.0F);
      GlStateManager.glVertex3f(this.posX + (float)var5, this.posY, 0.0F);
      GlStateManager.glTexCoord2f((float)var3 / 128.0F, ((float)var4 + 7.99F) / 128.0F);
      GlStateManager.glVertex3f(this.posX - (float)var5, this.posY + 7.99F, 0.0F);
      GlStateManager.glTexCoord2f(((float)var3 + var7 - 1.0F) / 128.0F, (float)var4 / 128.0F);
      GlStateManager.glVertex3f(this.posX + var7 - 1.0F + (float)var5, this.posY, 0.0F);
      GlStateManager.glTexCoord2f(((float)var3 + var7 - 1.0F) / 128.0F, ((float)var4 + 7.99F) / 128.0F);
      GlStateManager.glVertex3f(this.posX + var7 - 1.0F - (float)var5, this.posY + 7.99F, 0.0F);
      GlStateManager.glEnd();
      return (float)var6;
   }

   private ResourceLocation getUnicodePageLocation(int var1) {
      if (UNICODE_PAGE_LOCATIONS[var1] == null) {
         UNICODE_PAGE_LOCATIONS[var1] = new ResourceLocation(String.format("textures/font/unicode_page_%02x.png", var1));
      }

      return UNICODE_PAGE_LOCATIONS[var1];
   }

   private void loadGlyphTexture(int var1) {
      this.bindTexture(this.getUnicodePageLocation(var1));
   }

   protected float renderUnicodeChar(char var1, boolean var2) {
      int var3 = this.glyphWidth[var1] & 255;
      if (var3 == 0) {
         return 0.0F;
      } else {
         int var4 = var1 / 256;
         this.loadGlyphTexture(var4);
         int var5 = var3 >>> 4;
         int var6 = var3 & 15;
         float var7 = (float)var5;
         float var8 = (float)(var6 + 1);
         float var9 = (float)(var1 % 16 * 16) + var7;
         float var10 = (float)((var1 & 255) / 16 * 16);
         float var11 = var8 - var7 - 0.02F;
         float var12 = var2 ? 1.0F : 0.0F;
         GlStateManager.glBegin(5);
         GlStateManager.glTexCoord2f(var9 / 256.0F, var10 / 256.0F);
         GlStateManager.glVertex3f(this.posX + var12, this.posY, 0.0F);
         GlStateManager.glTexCoord2f(var9 / 256.0F, (var10 + 15.98F) / 256.0F);
         GlStateManager.glVertex3f(this.posX - var12, this.posY + 7.99F, 0.0F);
         GlStateManager.glTexCoord2f((var9 + var11) / 256.0F, var10 / 256.0F);
         GlStateManager.glVertex3f(this.posX + var11 / 2.0F + var12, this.posY, 0.0F);
         GlStateManager.glTexCoord2f((var9 + var11) / 256.0F, (var10 + 15.98F) / 256.0F);
         GlStateManager.glVertex3f(this.posX + var11 / 2.0F - var12, this.posY + 7.99F, 0.0F);
         GlStateManager.glEnd();
         return (var8 - var7) / 2.0F + 1.0F;
      }
   }

   public int drawStringWithShadow(String var1, float var2, float var3, int var4) {
      return this.drawString(var1, var2, var3, var4, true);
   }

   public int drawString(String var1, int var2, int var3, int var4) {
      return this.drawString(var1, (float)var2, (float)var3, var4, false);
   }

   public int drawString(String var1, float var2, float var3, int var4, boolean var5) {
      this.enableAlpha();
      this.resetStyles();
      int var7;
      if (var5) {
         var7 = this.renderString(var1, var2 + 1.0F, var3 + 1.0F, var4, true);
         var7 = Math.max(var7, this.renderString(var1, var2, var3, var4, false));
      } else {
         var7 = this.renderString(var1, var2, var3, var4, false);
      }

      return var7;
   }

   private String bidiReorder(String var1) {
      try {
         Bidi var2 = new Bidi((new ArabicShaping(8)).shape(var1), 127);
         var2.setReorderingMode(0);
         return var2.writeReordered(2);
      } catch (ArabicShapingException var3) {
         return var1;
      }
   }

   private void resetStyles() {
      this.randomStyle = false;
      this.boldStyle = false;
      this.italicStyle = false;
      this.underlineStyle = false;
      this.strikethroughStyle = false;
   }

   private void renderStringAtPos(String var1, boolean var2) {
      for(int var3 = 0; var3 < var1.length(); ++var3) {
         char var4 = var1.charAt(var3);
         if (var4 == 167 && var3 + 1 < var1.length()) {
            int var9 = "0123456789abcdefklmnor".indexOf(var1.toLowerCase(Locale.ENGLISH).charAt(var3 + 1));
            if (var9 < 16) {
               this.randomStyle = false;
               this.boldStyle = false;
               this.strikethroughStyle = false;
               this.underlineStyle = false;
               this.italicStyle = false;
               if (var9 < 0 || var9 > 15) {
                  var9 = 15;
               }

               if (var2) {
                  var9 += 16;
               }

               int var11 = this.colorCode[var9];
               this.textColor = var11;
               this.setColor((float)(var11 >> 16) / 255.0F, (float)(var11 >> 8 & 255) / 255.0F, (float)(var11 & 255) / 255.0F, this.alpha);
            } else if (var9 == 16) {
               this.randomStyle = true;
            } else if (var9 == 17) {
               this.boldStyle = true;
            } else if (var9 == 18) {
               this.strikethroughStyle = true;
            } else if (var9 == 19) {
               this.underlineStyle = true;
            } else if (var9 == 20) {
               this.italicStyle = true;
            } else if (var9 == 21) {
               this.randomStyle = false;
               this.boldStyle = false;
               this.strikethroughStyle = false;
               this.underlineStyle = false;
               this.italicStyle = false;
               this.setColor(this.red, this.blue, this.green, this.alpha);
            }

            ++var3;
         } else {
            int var5 = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(var4);
            if (this.randomStyle && var5 != -1) {
               int var6 = this.getCharWidth(var4);

               char var7;
               while(true) {
                  var5 = this.fontRandom.nextInt("ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".length());
                  var7 = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".charAt(var5);
                  if (var6 == this.getCharWidth(var7)) {
                     break;
                  }
               }

               var4 = var7;
            }

            float var10 = var5 != -1 && !this.unicodeFlag ? 1.0F : 0.5F;
            boolean var12 = (var4 == 0 || var5 == -1 || this.unicodeFlag) && var2;
            if (var12) {
               this.posX -= var10;
               this.posY -= var10;
            }

            float var8 = this.renderChar(var4, this.italicStyle);
            if (var12) {
               this.posX += var10;
               this.posY += var10;
            }

            if (this.boldStyle) {
               this.posX += var10;
               if (var12) {
                  this.posX -= var10;
                  this.posY -= var10;
               }

               this.renderChar(var4, this.italicStyle);
               this.posX -= var10;
               if (var12) {
                  this.posX += var10;
                  this.posY += var10;
               }

               ++var8;
            }

            this.doDraw(var8);
         }
      }

   }

   protected void doDraw(float var1) {
      if (this.strikethroughStyle) {
         Tessellator var2 = Tessellator.getInstance();
         VertexBuffer var3 = var2.getBuffer();
         GlStateManager.disableTexture2D();
         var3.begin(7, DefaultVertexFormats.POSITION);
         var3.pos((double)this.posX, (double)(this.posY + (float)(this.FONT_HEIGHT / 2)), 0.0D).endVertex();
         var3.pos((double)(this.posX + var1), (double)(this.posY + (float)(this.FONT_HEIGHT / 2)), 0.0D).endVertex();
         var3.pos((double)(this.posX + var1), (double)(this.posY + (float)(this.FONT_HEIGHT / 2) - 1.0F), 0.0D).endVertex();
         var3.pos((double)this.posX, (double)(this.posY + (float)(this.FONT_HEIGHT / 2) - 1.0F), 0.0D).endVertex();
         var2.draw();
         GlStateManager.enableTexture2D();
      }

      if (this.underlineStyle) {
         Tessellator var5 = Tessellator.getInstance();
         VertexBuffer var6 = var5.getBuffer();
         GlStateManager.disableTexture2D();
         var6.begin(7, DefaultVertexFormats.POSITION);
         int var4 = this.underlineStyle ? -1 : 0;
         var6.pos((double)(this.posX + (float)var4), (double)(this.posY + (float)this.FONT_HEIGHT), 0.0D).endVertex();
         var6.pos((double)(this.posX + var1), (double)(this.posY + (float)this.FONT_HEIGHT), 0.0D).endVertex();
         var6.pos((double)(this.posX + var1), (double)(this.posY + (float)this.FONT_HEIGHT - 1.0F), 0.0D).endVertex();
         var6.pos((double)(this.posX + (float)var4), (double)(this.posY + (float)this.FONT_HEIGHT - 1.0F), 0.0D).endVertex();
         var5.draw();
         GlStateManager.enableTexture2D();
      }

      this.posX += (float)((int)var1);
   }

   private int renderStringAligned(String var1, int var2, int var3, int var4, int var5, boolean var6) {
      if (this.bidiFlag) {
         int var7 = this.getStringWidth(this.bidiReorder(var1));
         var2 = var2 + var4 - var7;
      }

      return this.renderString(var1, (float)var2, (float)var3, var5, var6);
   }

   private int renderString(String var1, float var2, float var3, int var4, boolean var5) {
      if (var1 == null) {
         return 0;
      } else {
         if (this.bidiFlag) {
            var1 = this.bidiReorder(var1);
         }

         if ((var4 & -67108864) == 0) {
            var4 |= -16777216;
         }

         if (var5) {
            var4 = (var4 & 16579836) >> 2 | var4 & -16777216;
         }

         this.red = (float)(var4 >> 16 & 255) / 255.0F;
         this.blue = (float)(var4 >> 8 & 255) / 255.0F;
         this.green = (float)(var4 & 255) / 255.0F;
         this.alpha = (float)(var4 >> 24 & 255) / 255.0F;
         this.setColor(this.red, this.blue, this.green, this.alpha);
         this.posX = var2;
         this.posY = var3;
         this.renderStringAtPos(var1, var5);
         return (int)this.posX;
      }
   }

   public int getStringWidth(String var1) {
      if (var1 == null) {
         return 0;
      } else {
         int var2 = 0;
         boolean var3 = false;

         for(int var4 = 0; var4 < var1.length(); ++var4) {
            char var5 = var1.charAt(var4);
            int var6 = this.getCharWidth(var5);
            if (var6 < 0 && var4 < var1.length() - 1) {
               ++var4;
               var5 = var1.charAt(var4);
               if (var5 != 'l' && var5 != 'L') {
                  if (var5 == 'r' || var5 == 'R') {
                     var3 = false;
                  }
               } else {
                  var3 = true;
               }

               var6 = 0;
            }

            var2 += var6;
            if (var3 && var6 > 0) {
               ++var2;
            }
         }

         return var2;
      }
   }

   public int getCharWidth(char var1) {
      if (var1 == 167) {
         return -1;
      } else if (var1 == ' ') {
         return 4;
      } else {
         int var2 = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(var1);
         if (var1 > 0 && var2 != -1 && !this.unicodeFlag) {
            return this.charWidth[var2];
         } else if (this.glyphWidth[var1] != 0) {
            int var3 = this.glyphWidth[var1] & 255;
            int var4 = var3 >>> 4;
            int var5 = var3 & 15;
            ++var5;
            return (var5 - var4) / 2 + 1;
         } else {
            return 0;
         }
      }
   }

   public String trimStringToWidth(String var1, int var2) {
      return this.trimStringToWidth(var1, var2, false);
   }

   public String trimStringToWidth(String var1, int var2, boolean var3) {
      StringBuilder var4 = new StringBuilder();
      int var5 = 0;
      int var6 = var3 ? var1.length() - 1 : 0;
      int var7 = var3 ? -1 : 1;
      boolean var8 = false;
      boolean var9 = false;

      for(int var10 = var6; var10 >= 0 && var10 < var1.length() && var5 < var2; var10 += var7) {
         char var11 = var1.charAt(var10);
         int var12 = this.getCharWidth(var11);
         if (var8) {
            var8 = false;
            if (var11 != 'l' && var11 != 'L') {
               if (var11 == 'r' || var11 == 'R') {
                  var9 = false;
               }
            } else {
               var9 = true;
            }
         } else if (var12 < 0) {
            var8 = true;
         } else {
            var5 += var12;
            if (var9) {
               ++var5;
            }
         }

         if (var5 > var2) {
            break;
         }

         if (var3) {
            var4.insert(0, var11);
         } else {
            var4.append(var11);
         }
      }

      return var4.toString();
   }

   private String trimStringNewline(String var1) {
      while(var1 != null && var1.endsWith("\n")) {
         var1 = var1.substring(0, var1.length() - 1);
      }

      return var1;
   }

   public void drawSplitString(String var1, int var2, int var3, int var4, int var5) {
      this.resetStyles();
      this.textColor = var5;
      var1 = this.trimStringNewline(var1);
      this.renderSplitString(var1, var2, var3, var4, false);
   }

   private void renderSplitString(String var1, int var2, int var3, int var4, boolean var5) {
      for(String var7 : this.listFormattedStringToWidth(var1, var4)) {
         this.renderStringAligned(var7, var2, var3, var4, this.textColor, var5);
         var3 += this.FONT_HEIGHT;
      }

   }

   public int splitStringWidth(String var1, int var2) {
      return this.FONT_HEIGHT * this.listFormattedStringToWidth(var1, var2).size();
   }

   public void setUnicodeFlag(boolean var1) {
      this.unicodeFlag = var1;
   }

   public boolean getUnicodeFlag() {
      return this.unicodeFlag;
   }

   public void setBidiFlag(boolean var1) {
      this.bidiFlag = var1;
   }

   public List listFormattedStringToWidth(String var1, int var2) {
      return Arrays.asList(this.wrapFormattedStringToWidth(var1, var2).split("\n"));
   }

   String wrapFormattedStringToWidth(String var1, int var2) {
      int var3 = this.sizeStringToWidth(var1, var2);
      if (var1.length() <= var3) {
         return var1;
      } else {
         String var4 = var1.substring(0, var3);
         char var5 = var1.charAt(var3);
         boolean var6 = var5 == ' ' || var5 == '\n';
         String var7 = getFormatFromString(var4) + var1.substring(var3 + (var6 ? 1 : 0));
         return var4 + "\n" + this.wrapFormattedStringToWidth(var7, var2);
      }
   }

   private int sizeStringToWidth(String var1, int var2) {
      int var3 = var1.length();
      int var4 = 0;
      int var5 = 0;
      int var6 = -1;

      for(boolean var7 = false; var5 < var3; ++var5) {
         char var8 = var1.charAt(var5);
         switch(var8) {
         case '\n':
            --var5;
            break;
         case ' ':
            var6 = var5;
         default:
            var4 += this.getCharWidth(var8);
            if (var7) {
               ++var4;
            }
            break;
         case '§':
            if (var5 < var3 - 1) {
               ++var5;
               char var9 = var1.charAt(var5);
               if (var9 != 'l' && var9 != 'L') {
                  if (var9 == 'r' || var9 == 'R' || isFormatColor(var9)) {
                     var7 = false;
                  }
               } else {
                  var7 = true;
               }
            }
         }

         if (var8 == '\n') {
            ++var5;
            var6 = var5;
            break;
         }

         if (var4 > var2) {
            break;
         }
      }

      return var5 != var3 && var6 != -1 && var6 < var5 ? var6 : var5;
   }

   private static boolean isFormatColor(char var0) {
      return var0 >= '0' && var0 <= '9' || var0 >= 'a' && var0 <= 'f' || var0 >= 'A' && var0 <= 'F';
   }

   private static boolean isFormatSpecial(char var0) {
      return var0 >= 'k' && var0 <= 'o' || var0 >= 'K' && var0 <= 'O' || var0 == 'r' || var0 == 'R';
   }

   public static String getFormatFromString(String var0) {
      String var1 = "";
      int var2 = -1;
      int var3 = var0.length();

      while((var2 = var0.indexOf(167, var2 + 1)) != -1) {
         if (var2 < var3 - 1) {
            char var4 = var0.charAt(var2 + 1);
            if (isFormatColor(var4)) {
               var1 = "§" + var4;
            } else if (isFormatSpecial(var4)) {
               var1 = var1 + "§" + var4;
            }
         }
      }

      return var1;
   }

   public boolean getBidiFlag() {
      return this.bidiFlag;
   }

   protected void setColor(float var1, float var2, float var3, float var4) {
      GlStateManager.color(var1, var2, var3, var4);
   }

   protected void enableAlpha() {
      GlStateManager.enableAlpha();
   }

   protected void bindTexture(ResourceLocation var1) {
      this.renderEngine.bindTexture(var1);
   }

   protected IResource getResource(ResourceLocation var1) throws IOException {
      return Minecraft.getMinecraft().getResourceManager().getResource(var1);
   }

   public int getColorCode(char var1) {
      int var2 = "0123456789abcdef".indexOf(var1);
      return var2 >= 0 && var2 < this.colorCode.length ? this.colorCode[var2] : -1;
   }
}
