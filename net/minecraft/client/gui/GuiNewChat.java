package net.minecraft.client.gui;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class GuiNewChat extends Gui {
   private static final Splitter NEWLINE_SPLITTER = Splitter.on('\n');
   private static final Joiner NEWLINE_STRING_JOINER = Joiner.on("\\n");
   private static final Logger LOGGER = LogManager.getLogger();
   private final Minecraft mc;
   private final List sentMessages = Lists.newArrayList();
   private final List chatLines = Lists.newArrayList();
   private final List drawnChatLines = Lists.newArrayList();
   private int scrollPos;
   private boolean isScrolled;

   public GuiNewChat(Minecraft var1) {
      this.mc = var1;
   }

   public void drawChat(int var1) {
      if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
         int var2 = this.getLineCount();
         int var3 = this.drawnChatLines.size();
         float var4 = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;
         if (var3 > 0) {
            boolean var5 = false;
            if (this.getChatOpen()) {
               var5 = true;
            }

            float var6 = this.getChatScale();
            int var7 = MathHelper.ceil((float)this.getChatWidth() / var6);
            GlStateManager.pushMatrix();
            GlStateManager.translate(2.0F, 8.0F, 0.0F);
            GlStateManager.scale(var6, var6, 1.0F);
            int var8 = 0;

            for(int var9 = 0; var9 + this.scrollPos < this.drawnChatLines.size() && var9 < var2; ++var9) {
               ChatLine var10 = (ChatLine)this.drawnChatLines.get(var9 + this.scrollPos);
               if (var10 != null) {
                  int var11 = var1 - var10.getUpdatedCounter();
                  if (var11 < 200 || var5) {
                     double var12 = (double)var11 / 200.0D;
                     var12 = 1.0D - var12;
                     var12 = var12 * 10.0D;
                     var12 = MathHelper.clamp(var12, 0.0D, 1.0D);
                     var12 = var12 * var12;
                     int var14 = (int)(255.0D * var12);
                     if (var5) {
                        var14 = 255;
                     }

                     var14 = (int)((float)var14 * var4);
                     ++var8;
                     if (var14 > 3) {
                        boolean var15 = false;
                        int var16 = -var9 * 9;
                        drawRect(-2, var16 - 9, 0 + var7 + 4, var16, var14 / 2 << 24);
                        String var17 = var10.getChatComponent().getFormattedText();
                        GlStateManager.enableBlend();
                        this.mc.fontRendererObj.drawStringWithShadow(var17, 0.0F, (float)(var16 - 8), 16777215 + (var14 << 24));
                        GlStateManager.disableAlpha();
                        GlStateManager.disableBlend();
                     }
                  }
               }
            }

            if (var5) {
               int var18 = this.mc.fontRendererObj.FONT_HEIGHT;
               GlStateManager.translate(-3.0F, 0.0F, 0.0F);
               int var19 = var3 * var18 + var3;
               int var20 = var8 * var18 + var8;
               int var25 = this.scrollPos * var20 / var3;
               int var13 = var20 * var20 / var19;
               if (var19 != var20) {
                  int var27 = var25 > 0 ? 170 : 96;
                  int var28 = this.isScrolled ? 13382451 : 3355562;
                  drawRect(0, -var25, 2, -var25 - var13, var28 + (var27 << 24));
                  drawRect(2, -var25, 1, -var25 - var13, 13421772 + (var27 << 24));
               }
            }

            GlStateManager.popMatrix();
         }
      }

   }

   public void clearChatMessages() {
      this.drawnChatLines.clear();
      this.chatLines.clear();
      this.sentMessages.clear();
   }

   public void printChatMessage(ITextComponent var1) {
      this.printChatMessageWithOptionalDeletion(var1, 0);
   }

   public void printChatMessageWithOptionalDeletion(ITextComponent var1, int var2) {
      this.setChatLine(var1, var2, this.mc.ingameGUI.getUpdateCounter(), false);
      LOGGER.info("[CHAT] {}", new Object[]{NEWLINE_STRING_JOINER.join(NEWLINE_SPLITTER.split(var1.getUnformattedText()))});
   }

   private void setChatLine(ITextComponent var1, int var2, int var3, boolean var4) {
      if (var2 != 0) {
         this.deleteChatLine(var2);
      }

      int var5 = MathHelper.floor((float)this.getChatWidth() / this.getChatScale());
      List var6 = GuiUtilRenderComponents.splitText(var1, var5, this.mc.fontRendererObj, false, false);
      boolean var7 = this.getChatOpen();

      for(ITextComponent var9 : var6) {
         if (var7 && this.scrollPos > 0) {
            this.isScrolled = true;
            this.scroll(1);
         }

         this.drawnChatLines.add(0, new ChatLine(var3, var9, var2));
      }

      while(this.drawnChatLines.size() > 100) {
         this.drawnChatLines.remove(this.drawnChatLines.size() - 1);
      }

      if (!var4) {
         this.chatLines.add(0, new ChatLine(var3, var1, var2));

         while(this.chatLines.size() > 100) {
            this.chatLines.remove(this.chatLines.size() - 1);
         }
      }

   }

   public void refreshChat() {
      this.drawnChatLines.clear();
      this.resetScroll();

      for(int var1 = this.chatLines.size() - 1; var1 >= 0; --var1) {
         ChatLine var2 = (ChatLine)this.chatLines.get(var1);
         this.setChatLine(var2.getChatComponent(), var2.getChatLineID(), var2.getUpdatedCounter(), true);
      }

   }

   public List getSentMessages() {
      return this.sentMessages;
   }

   public void addToSentMessages(String var1) {
      if (this.sentMessages.isEmpty() || !((String)this.sentMessages.get(this.sentMessages.size() - 1)).equals(var1)) {
         this.sentMessages.add(var1);
      }

   }

   public void resetScroll() {
      this.scrollPos = 0;
      this.isScrolled = false;
   }

   public void scroll(int var1) {
      this.scrollPos += var1;
      int var2 = this.drawnChatLines.size();
      if (this.scrollPos > var2 - this.getLineCount()) {
         this.scrollPos = var2 - this.getLineCount();
      }

      if (this.scrollPos <= 0) {
         this.scrollPos = 0;
         this.isScrolled = false;
      }

   }

   @Nullable
   public ITextComponent getChatComponent(int var1, int var2) {
      if (!this.getChatOpen()) {
         return null;
      } else {
         ScaledResolution var3 = new ScaledResolution(this.mc);
         int var4 = var3.getScaleFactor();
         float var5 = this.getChatScale();
         int var6 = var1 / var4 - 2;
         int var7 = var2 / var4 - 40;
         var6 = MathHelper.floor((float)var6 / var5);
         var7 = MathHelper.floor((float)var7 / var5);
         if (var6 >= 0 && var7 >= 0) {
            int var8 = Math.min(this.getLineCount(), this.drawnChatLines.size());
            if (var6 <= MathHelper.floor((float)this.getChatWidth() / this.getChatScale()) && var7 < this.mc.fontRendererObj.FONT_HEIGHT * var8 + var8) {
               int var9 = var7 / this.mc.fontRendererObj.FONT_HEIGHT + this.scrollPos;
               if (var9 >= 0 && var9 < this.drawnChatLines.size()) {
                  ChatLine var10 = (ChatLine)this.drawnChatLines.get(var9);
                  int var11 = 0;

                  for(ITextComponent var13 : var10.getChatComponent()) {
                     if (var13 instanceof TextComponentString) {
                        var11 += this.mc.fontRendererObj.getStringWidth(GuiUtilRenderComponents.removeTextColorsIfConfigured(((TextComponentString)var13).getText(), false));
                        if (var11 > var6) {
                           return var13;
                        }
                     }
                  }
               }

               return null;
            } else {
               return null;
            }
         } else {
            return null;
         }
      }
   }

   public boolean getChatOpen() {
      return this.mc.currentScreen instanceof GuiChat;
   }

   public void deleteChatLine(int var1) {
      Iterator var2 = this.drawnChatLines.iterator();

      while(var2.hasNext()) {
         ChatLine var3 = (ChatLine)var2.next();
         if (var3.getChatLineID() == var1) {
            var2.remove();
         }
      }

      var2 = this.chatLines.iterator();

      while(var2.hasNext()) {
         ChatLine var5 = (ChatLine)var2.next();
         if (var5.getChatLineID() == var1) {
            var2.remove();
            break;
         }
      }

   }

   public int getChatWidth() {
      return calculateChatboxWidth(this.mc.gameSettings.chatWidth);
   }

   public int getChatHeight() {
      return calculateChatboxHeight(this.getChatOpen() ? this.mc.gameSettings.chatHeightFocused : this.mc.gameSettings.chatHeightUnfocused);
   }

   public float getChatScale() {
      return this.mc.gameSettings.chatScale;
   }

   public static int calculateChatboxWidth(float var0) {
      boolean var1 = true;
      boolean var2 = true;
      return MathHelper.floor(var0 * 280.0F + 40.0F);
   }

   public static int calculateChatboxHeight(float var0) {
      boolean var1 = true;
      boolean var2 = true;
      return MathHelper.floor(var0 * 160.0F + 20.0F);
   }

   public int getLineCount() {
      return this.getChatHeight() / 9;
   }
}
