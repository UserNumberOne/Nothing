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
      this.locationFontTexture = location;
      this.renderEngine = textureManagerIn;
      this.unicodeFlag = unicode;
      this.bindTexture(this.locationFontTexture);

      for(int i = 0; i < 32; ++i) {
         int j = (i >> 3 & 1) * 85;
         int k = (i >> 2 & 1) * 170 + j;
         int l = (i >> 1 & 1) * 170 + j;
         int i1 = (i >> 0 & 1) * 170 + j;
         if (i == 6) {
            k += 85;
         }

         if (gameSettingsIn.anaglyph) {
            int j1 = (k * 30 + l * 59 + i1 * 11) / 100;
            int k1 = (k * 30 + l * 70) / 100;
            int l1 = (k * 30 + i1 * 70) / 100;
            k = j1;
            l = k1;
            i1 = l1;
         }

         if (i >= 16) {
            k /= 4;
            l /= 4;
            i1 /= 4;
         }

         this.colorCode[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
      }

      this.readGlyphSizes();
   }

   public void onResourceManagerReload(IResourceManager var1) {
      this.readFontTexture();
      this.readGlyphSizes();
   }

   private void readFontTexture() {
      IResource iresource = null;

      BufferedImage bufferedimage;
      try {
         iresource = this.getResource(this.locationFontTexture);
         bufferedimage = TextureUtil.readBufferedImage(iresource.getInputStream());
      } catch (IOException var20) {
         throw new RuntimeException(var20);
      } finally {
         IOUtils.closeQuietly(iresource);
      }

      int var3 = bufferedimage.getWidth();
      int var4 = bufferedimage.getHeight();
      int[] var5 = new int[var3 * var4];
      bufferedimage.getRGB(0, 0, var3, var4, var5, 0, var3);
      int var6 = var4 / 16;
      int var7 = var3 / 16;
      boolean var8 = true;
      float var9 = 8.0F / (float)var7;

      for(int var10 = 0; var10 < 256; ++var10) {
         int j1 = var10 % 16;
         int k1 = var10 / 16;
         if (var10 == 32) {
            this.charWidth[var10] = 4;
         }

         int l1;
         for(l1 = var7 - 1; l1 >= 0; --l1) {
            int i2 = j1 * var7 + l1;
            boolean flag1 = true;

            for(int j2 = 0; j2 < var6 && flag1; ++j2) {
               int k2 = (k1 * var7 + j2) * var3;
               if ((var5[i2 + k2] >> 24 & 255) != 0) {
                  flag1 = false;
               }
            }

            if (!flag1) {
               break;
            }
         }

         ++l1;
         this.charWidth[var10] = (int)(0.5D + (double)((float)l1 * var9)) + 1;
      }

   }

   private void readGlyphSizes() {
      IResource iresource = null;

      try {
         iresource = this.getResource(new ResourceLocation("font/glyph_sizes.bin"));
         iresource.getInputStream().read(this.glyphWidth);
      } catch (IOException var6) {
         throw new RuntimeException(var6);
      } finally {
         IOUtils.closeQuietly(iresource);
      }

   }

   private float renderChar(char var1, boolean var2) {
      if (ch == ' ') {
         return 4.0F;
      } else {
         int i = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(ch);
         return i != -1 && !this.unicodeFlag ? this.renderDefaultChar(i, italic) : this.renderUnicodeChar(ch, italic);
      }
   }

   protected float renderDefaultChar(int var1, boolean var2) {
      int i = ch % 16 * 8;
      int j = ch / 16 * 8;
      int k = italic ? 1 : 0;
      this.bindTexture(this.locationFontTexture);
      int l = this.charWidth[ch];
      float f = (float)l - 0.01F;
      GlStateManager.glBegin(5);
      GlStateManager.glTexCoord2f((float)i / 128.0F, (float)j / 128.0F);
      GlStateManager.glVertex3f(this.posX + (float)k, this.posY, 0.0F);
      GlStateManager.glTexCoord2f((float)i / 128.0F, ((float)j + 7.99F) / 128.0F);
      GlStateManager.glVertex3f(this.posX - (float)k, this.posY + 7.99F, 0.0F);
      GlStateManager.glTexCoord2f(((float)i + f - 1.0F) / 128.0F, (float)j / 128.0F);
      GlStateManager.glVertex3f(this.posX + f - 1.0F + (float)k, this.posY, 0.0F);
      GlStateManager.glTexCoord2f(((float)i + f - 1.0F) / 128.0F, ((float)j + 7.99F) / 128.0F);
      GlStateManager.glVertex3f(this.posX + f - 1.0F - (float)k, this.posY + 7.99F, 0.0F);
      GlStateManager.glEnd();
      return (float)l;
   }

   private ResourceLocation getUnicodePageLocation(int var1) {
      if (UNICODE_PAGE_LOCATIONS[page] == null) {
         UNICODE_PAGE_LOCATIONS[page] = new ResourceLocation(String.format("textures/font/unicode_page_%02x.png", page));
      }

      return UNICODE_PAGE_LOCATIONS[page];
   }

   private void loadGlyphTexture(int var1) {
      this.bindTexture(this.getUnicodePageLocation(page));
   }

   protected float renderUnicodeChar(char var1, boolean var2) {
      int i = this.glyphWidth[ch] & 255;
      if (i == 0) {
         return 0.0F;
      } else {
         int j = ch / 256;
         this.loadGlyphTexture(j);
         int k = i >>> 4;
         int l = i & 15;
         float f = (float)k;
         float f1 = (float)(l + 1);
         float f2 = (float)(ch % 16 * 16) + f;
         float f3 = (float)((ch & 255) / 16 * 16);
         float f4 = f1 - f - 0.02F;
         float f5 = italic ? 1.0F : 0.0F;
         GlStateManager.glBegin(5);
         GlStateManager.glTexCoord2f(f2 / 256.0F, f3 / 256.0F);
         GlStateManager.glVertex3f(this.posX + f5, this.posY, 0.0F);
         GlStateManager.glTexCoord2f(f2 / 256.0F, (f3 + 15.98F) / 256.0F);
         GlStateManager.glVertex3f(this.posX - f5, this.posY + 7.99F, 0.0F);
         GlStateManager.glTexCoord2f((f2 + f4) / 256.0F, f3 / 256.0F);
         GlStateManager.glVertex3f(this.posX + f4 / 2.0F + f5, this.posY, 0.0F);
         GlStateManager.glTexCoord2f((f2 + f4) / 256.0F, (f3 + 15.98F) / 256.0F);
         GlStateManager.glVertex3f(this.posX + f4 / 2.0F - f5, this.posY + 7.99F, 0.0F);
         GlStateManager.glEnd();
         return (f1 - f) / 2.0F + 1.0F;
      }
   }

   public int drawStringWithShadow(String var1, float var2, float var3, int var4) {
      return this.drawString(text, x, y, color, true);
   }

   public int drawString(String var1, int var2, int var3, int var4) {
      return this.drawString(text, (float)x, (float)y, color, false);
   }

   public int drawString(String var1, float var2, float var3, int var4, boolean var5) {
      this.enableAlpha();
      this.resetStyles();
      int i;
      if (dropShadow) {
         i = this.renderString(text, x + 1.0F, y + 1.0F, color, true);
         i = Math.max(i, this.renderString(text, x, y, color, false));
      } else {
         i = this.renderString(text, x, y, color, false);
      }

      return i;
   }

   private String bidiReorder(String var1) {
      try {
         Bidi bidi = new Bidi((new ArabicShaping(8)).shape(text), 127);
         bidi.setReorderingMode(0);
         return bidi.writeReordered(2);
      } catch (ArabicShapingException var3) {
         return text;
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
      for(int i = 0; i < text.length(); ++i) {
         char c0 = text.charAt(i);
         if (c0 == 167 && i + 1 < text.length()) {
            int i1 = "0123456789abcdefklmnor".indexOf(text.toLowerCase(Locale.ENGLISH).charAt(i + 1));
            if (i1 < 16) {
               this.randomStyle = false;
               this.boldStyle = false;
               this.strikethroughStyle = false;
               this.underlineStyle = false;
               this.italicStyle = false;
               if (i1 < 0 || i1 > 15) {
                  i1 = 15;
               }

               if (shadow) {
                  i1 += 16;
               }

               int j1 = this.colorCode[i1];
               this.textColor = j1;
               this.setColor((float)(j1 >> 16) / 255.0F, (float)(j1 >> 8 & 255) / 255.0F, (float)(j1 & 255) / 255.0F, this.alpha);
            } else if (i1 == 16) {
               this.randomStyle = true;
            } else if (i1 == 17) {
               this.boldStyle = true;
            } else if (i1 == 18) {
               this.strikethroughStyle = true;
            } else if (i1 == 19) {
               this.underlineStyle = true;
            } else if (i1 == 20) {
               this.italicStyle = true;
            } else if (i1 == 21) {
               this.randomStyle = false;
               this.boldStyle = false;
               this.strikethroughStyle = false;
               this.underlineStyle = false;
               this.italicStyle = false;
               this.setColor(this.red, this.blue, this.green, this.alpha);
            }

            ++i;
         } else {
            int j = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(c0);
            if (this.randomStyle && j != -1) {
               int k = this.getCharWidth(c0);

               char c1;
               while(true) {
                  j = this.fontRandom.nextInt("ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".length());
                  c1 = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".charAt(j);
                  if (k == this.getCharWidth(c1)) {
                     break;
                  }
               }

               c0 = c1;
            }

            float f1 = j != -1 && !this.unicodeFlag ? 1.0F : 0.5F;
            boolean flag = (c0 == 0 || j == -1 || this.unicodeFlag) && shadow;
            if (flag) {
               this.posX -= f1;
               this.posY -= f1;
            }

            float f = this.renderChar(c0, this.italicStyle);
            if (flag) {
               this.posX += f1;
               this.posY += f1;
            }

            if (this.boldStyle) {
               this.posX += f1;
               if (flag) {
                  this.posX -= f1;
                  this.posY -= f1;
               }

               this.renderChar(c0, this.italicStyle);
               this.posX -= f1;
               if (flag) {
                  this.posX += f1;
                  this.posY += f1;
               }

               ++f;
            }

            this.doDraw(f);
         }
      }

   }

   protected void doDraw(float var1) {
      if (this.strikethroughStyle) {
         Tessellator tessellator = Tessellator.getInstance();
         VertexBuffer vertexbuffer = tessellator.getBuffer();
         GlStateManager.disableTexture2D();
         vertexbuffer.begin(7, DefaultVertexFormats.POSITION);
         vertexbuffer.pos((double)this.posX, (double)(this.posY + (float)(this.FONT_HEIGHT / 2)), 0.0D).endVertex();
         vertexbuffer.pos((double)(this.posX + f), (double)(this.posY + (float)(this.FONT_HEIGHT / 2)), 0.0D).endVertex();
         vertexbuffer.pos((double)(this.posX + f), (double)(this.posY + (float)(this.FONT_HEIGHT / 2) - 1.0F), 0.0D).endVertex();
         vertexbuffer.pos((double)this.posX, (double)(this.posY + (float)(this.FONT_HEIGHT / 2) - 1.0F), 0.0D).endVertex();
         tessellator.draw();
         GlStateManager.enableTexture2D();
      }

      if (this.underlineStyle) {
         Tessellator tessellator1 = Tessellator.getInstance();
         VertexBuffer vertexbuffer1 = tessellator1.getBuffer();
         GlStateManager.disableTexture2D();
         vertexbuffer1.begin(7, DefaultVertexFormats.POSITION);
         int l = this.underlineStyle ? -1 : 0;
         vertexbuffer1.pos((double)(this.posX + (float)l), (double)(this.posY + (float)this.FONT_HEIGHT), 0.0D).endVertex();
         vertexbuffer1.pos((double)(this.posX + f), (double)(this.posY + (float)this.FONT_HEIGHT), 0.0D).endVertex();
         vertexbuffer1.pos((double)(this.posX + f), (double)(this.posY + (float)this.FONT_HEIGHT - 1.0F), 0.0D).endVertex();
         vertexbuffer1.pos((double)(this.posX + (float)l), (double)(this.posY + (float)this.FONT_HEIGHT - 1.0F), 0.0D).endVertex();
         tessellator1.draw();
         GlStateManager.enableTexture2D();
      }

      this.posX += (float)((int)f);
   }

   private int renderStringAligned(String var1, int var2, int var3, int var4, int var5, boolean var6) {
      if (this.bidiFlag) {
         int i = this.getStringWidth(this.bidiReorder(text));
         x = x + width - i;
      }

      return this.renderString(text, (float)x, (float)y, color, dropShadow);
   }

   private int renderString(String var1, float var2, float var3, int var4, boolean var5) {
      if (text == null) {
         return 0;
      } else {
         if (this.bidiFlag) {
            text = this.bidiReorder(text);
         }

         if ((color & -67108864) == 0) {
            color |= -16777216;
         }

         if (dropShadow) {
            color = (color & 16579836) >> 2 | color & -16777216;
         }

         this.red = (float)(color >> 16 & 255) / 255.0F;
         this.blue = (float)(color >> 8 & 255) / 255.0F;
         this.green = (float)(color & 255) / 255.0F;
         this.alpha = (float)(color >> 24 & 255) / 255.0F;
         this.setColor(this.red, this.blue, this.green, this.alpha);
         this.posX = x;
         this.posY = y;
         this.renderStringAtPos(text, dropShadow);
         return (int)this.posX;
      }
   }

   public int getStringWidth(String var1) {
      if (text == null) {
         return 0;
      } else {
         int i = 0;
         boolean flag = false;

         for(int j = 0; j < text.length(); ++j) {
            char c0 = text.charAt(j);
            int k = this.getCharWidth(c0);
            if (k < 0 && j < text.length() - 1) {
               ++j;
               c0 = text.charAt(j);
               if (c0 != 'l' && c0 != 'L') {
                  if (c0 == 'r' || c0 == 'R') {
                     flag = false;
                  }
               } else {
                  flag = true;
               }

               k = 0;
            }

            i += k;
            if (flag && k > 0) {
               ++i;
            }
         }

         return i;
      }
   }

   public int getCharWidth(char var1) {
      if (character == 167) {
         return -1;
      } else if (character == ' ') {
         return 4;
      } else {
         int i = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(character);
         if (character > 0 && i != -1 && !this.unicodeFlag) {
            return this.charWidth[i];
         } else if (this.glyphWidth[character] != 0) {
            int j = this.glyphWidth[character] & 255;
            int k = j >>> 4;
            int l = j & 15;
            ++l;
            return (l - k) / 2 + 1;
         } else {
            return 0;
         }
      }
   }

   public String trimStringToWidth(String var1, int var2) {
      return this.trimStringToWidth(text, width, false);
   }

   public String trimStringToWidth(String var1, int var2, boolean var3) {
      StringBuilder stringbuilder = new StringBuilder();
      int i = 0;
      int j = reverse ? text.length() - 1 : 0;
      int k = reverse ? -1 : 1;
      boolean flag = false;
      boolean flag1 = false;

      for(int l = j; l >= 0 && l < text.length() && i < width; l += k) {
         char c0 = text.charAt(l);
         int i1 = this.getCharWidth(c0);
         if (flag) {
            flag = false;
            if (c0 != 'l' && c0 != 'L') {
               if (c0 == 'r' || c0 == 'R') {
                  flag1 = false;
               }
            } else {
               flag1 = true;
            }
         } else if (i1 < 0) {
            flag = true;
         } else {
            i += i1;
            if (flag1) {
               ++i;
            }
         }

         if (i > width) {
            break;
         }

         if (reverse) {
            stringbuilder.insert(0, c0);
         } else {
            stringbuilder.append(c0);
         }
      }

      return stringbuilder.toString();
   }

   private String trimStringNewline(String var1) {
      while(text != null && text.endsWith("\n")) {
         text = text.substring(0, text.length() - 1);
      }

      return text;
   }

   public void drawSplitString(String var1, int var2, int var3, int var4, int var5) {
      this.resetStyles();
      this.textColor = textColor;
      str = this.trimStringNewline(str);
      this.renderSplitString(str, x, y, wrapWidth, false);
   }

   private void renderSplitString(String var1, int var2, int var3, int var4, boolean var5) {
      for(String s : this.listFormattedStringToWidth(str, wrapWidth)) {
         this.renderStringAligned(s, x, y, wrapWidth, this.textColor, addShadow);
         y += this.FONT_HEIGHT;
      }

   }

   public int splitStringWidth(String var1, int var2) {
      return this.FONT_HEIGHT * this.listFormattedStringToWidth(str, maxLength).size();
   }

   public void setUnicodeFlag(boolean var1) {
      this.unicodeFlag = unicodeFlagIn;
   }

   public boolean getUnicodeFlag() {
      return this.unicodeFlag;
   }

   public void setBidiFlag(boolean var1) {
      this.bidiFlag = bidiFlagIn;
   }

   public List listFormattedStringToWidth(String var1, int var2) {
      return Arrays.asList(this.wrapFormattedStringToWidth(str, wrapWidth).split("\n"));
   }

   String wrapFormattedStringToWidth(String var1, int var2) {
      int i = this.sizeStringToWidth(str, wrapWidth);
      if (str.length() <= i) {
         return str;
      } else {
         String s = str.substring(0, i);
         char c0 = str.charAt(i);
         boolean flag = c0 == ' ' || c0 == '\n';
         String s1 = getFormatFromString(s) + str.substring(i + (flag ? 1 : 0));
         return s + "\n" + this.wrapFormattedStringToWidth(s1, wrapWidth);
      }
   }

   private int sizeStringToWidth(String var1, int var2) {
      int i = str.length();
      int j = 0;
      int k = 0;
      int l = -1;

      for(boolean flag = false; k < i; ++k) {
         char c0 = str.charAt(k);
         switch(c0) {
         case '\n':
            --k;
            break;
         case ' ':
            l = k;
         default:
            j += this.getCharWidth(c0);
            if (flag) {
               ++j;
            }
            break;
         case '§':
            if (k < i - 1) {
               ++k;
               char c1 = str.charAt(k);
               if (c1 != 'l' && c1 != 'L') {
                  if (c1 == 'r' || c1 == 'R' || isFormatColor(c1)) {
                     flag = false;
                  }
               } else {
                  flag = true;
               }
            }
         }

         if (c0 == '\n') {
            ++k;
            l = k;
            break;
         }

         if (j > wrapWidth) {
            break;
         }
      }

      return k != i && l != -1 && l < k ? l : k;
   }

   private static boolean isFormatColor(char var0) {
      return colorChar >= '0' && colorChar <= '9' || colorChar >= 'a' && colorChar <= 'f' || colorChar >= 'A' && colorChar <= 'F';
   }

   private static boolean isFormatSpecial(char var0) {
      return formatChar >= 'k' && formatChar <= 'o' || formatChar >= 'K' && formatChar <= 'O' || formatChar == 'r' || formatChar == 'R';
   }

   public static String getFormatFromString(String var0) {
      String s = "";
      int i = -1;
      int j = text.length();

      while((i = text.indexOf(167, i + 1)) != -1) {
         if (i < j - 1) {
            char c0 = text.charAt(i + 1);
            if (isFormatColor(c0)) {
               s = "§" + c0;
            } else if (isFormatSpecial(c0)) {
               s = s + "§" + c0;
            }
         }
      }

      return s;
   }

   public boolean getBidiFlag() {
      return this.bidiFlag;
   }

   protected void setColor(float var1, float var2, float var3, float var4) {
      GlStateManager.color(r, g, b, a);
   }

   protected void enableAlpha() {
      GlStateManager.enableAlpha();
   }

   protected void bindTexture(ResourceLocation var1) {
      this.renderEngine.bindTexture(location);
   }

   protected IResource getResource(ResourceLocation var1) throws IOException {
      return Minecraft.getMinecraft().getResourceManager().getResource(location);
   }

   public int getColorCode(char var1) {
      int i = "0123456789abcdef".indexOf(character);
      return i >= 0 && i < this.colorCode.length ? this.colorCode[i] : -1;
   }
}
