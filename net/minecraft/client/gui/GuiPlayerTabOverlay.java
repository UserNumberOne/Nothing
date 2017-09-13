package net.minecraft.client.gui;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.mojang.authlib.GameProfile;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPlayerTabOverlay extends Gui {
   private static final Ordering ENTRY_ORDERING = Ordering.from(new GuiPlayerTabOverlay.PlayerComparator());
   private final Minecraft mc;
   private final GuiIngame guiIngame;
   private ITextComponent footer;
   private ITextComponent header;
   private long lastTimeOpened;
   private boolean isBeingRendered;

   public GuiPlayerTabOverlay(Minecraft var1, GuiIngame var2) {
      this.mc = var1;
      this.guiIngame = var2;
   }

   public String getPlayerName(NetworkPlayerInfo var1) {
      return var1.getDisplayName() != null ? var1.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(var1.getPlayerTeam(), var1.getGameProfile().getName());
   }

   public void updatePlayerList(boolean var1) {
      if (var1 && !this.isBeingRendered) {
         this.lastTimeOpened = Minecraft.getSystemTime();
      }

      this.isBeingRendered = var1;
   }

   public void renderPlayerlist(int var1, Scoreboard var2, @Nullable ScoreObjective var3) {
      NetHandlerPlayClient var4 = this.mc.player.connection;
      List var5 = ENTRY_ORDERING.sortedCopy(var4.getPlayerInfoMap());
      int var6 = 0;
      int var7 = 0;

      for(NetworkPlayerInfo var9 : var5) {
         int var10 = this.mc.fontRendererObj.getStringWidth(this.getPlayerName(var9));
         var6 = Math.max(var6, var10);
         if (var3 != null && var3.getRenderType() != IScoreCriteria.EnumRenderType.HEARTS) {
            var10 = this.mc.fontRendererObj.getStringWidth(" " + var2.getOrCreateScore(var9.getGameProfile().getName(), var3).getScorePoints());
            var7 = Math.max(var7, var10);
         }
      }

      var5 = var5.subList(0, Math.min(var5.size(), 80));
      int var33 = var5.size();
      int var34 = var33;

      int var36;
      for(var36 = 1; var34 > 20; var34 = (var33 + var36 - 1) / var36) {
         ++var36;
      }

      boolean var11 = this.mc.isIntegratedServerRunning() || this.mc.getConnection().getNetworkManager().isEncrypted();
      int var12;
      if (var3 != null) {
         if (var3.getRenderType() == IScoreCriteria.EnumRenderType.HEARTS) {
            var12 = 90;
         } else {
            var12 = var7;
         }
      } else {
         var12 = 0;
      }

      int var13 = Math.min(var36 * ((var11 ? 9 : 0) + var6 + var12 + 13), var1 - 50) / var36;
      int var14 = var1 / 2 - (var13 * var36 + (var36 - 1) * 5) / 2;
      int var15 = 10;
      int var16 = var13 * var36 + (var36 - 1) * 5;
      List var17 = null;
      if (this.header != null) {
         var17 = this.mc.fontRendererObj.listFormattedStringToWidth(this.header.getFormattedText(), var1 - 50);

         for(String var19 : var17) {
            var16 = Math.max(var16, this.mc.fontRendererObj.getStringWidth(var19));
         }
      }

      List var38 = null;
      if (this.footer != null) {
         var38 = this.mc.fontRendererObj.listFormattedStringToWidth(this.footer.getFormattedText(), var1 - 50);

         for(String var20 : var38) {
            var16 = Math.max(var16, this.mc.fontRendererObj.getStringWidth(var20));
         }
      }

      if (var17 != null) {
         drawRect(var1 / 2 - var16 / 2 - 1, var15 - 1, var1 / 2 + var16 / 2 + 1, var15 + var17.size() * this.mc.fontRendererObj.FONT_HEIGHT, Integer.MIN_VALUE);

         for(String var43 : var17) {
            int var21 = this.mc.fontRendererObj.getStringWidth(var43);
            this.mc.fontRendererObj.drawStringWithShadow(var43, (float)(var1 / 2 - var21 / 2), (float)var15, -1);
            var15 += this.mc.fontRendererObj.FONT_HEIGHT;
         }

         ++var15;
      }

      drawRect(var1 / 2 - var16 / 2 - 1, var15 - 1, var1 / 2 + var16 / 2 + 1, var15 + var34 * 9, Integer.MIN_VALUE);

      for(int var41 = 0; var41 < var33; ++var41) {
         int var44 = var41 / var34;
         int var46 = var41 % var34;
         int var22 = var14 + var44 * var13 + var44 * 5;
         int var23 = var15 + var46 * 9;
         drawRect(var22, var23, var22 + var13, var23 + 8, 553648127);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.enableAlpha();
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
         if (var41 < var5.size()) {
            NetworkPlayerInfo var24 = (NetworkPlayerInfo)var5.get(var41);
            GameProfile var25 = var24.getGameProfile();
            if (var11) {
               EntityPlayer var26 = this.mc.world.getPlayerEntityByUUID(var25.getId());
               boolean var27 = var26 != null && var26.isWearing(EnumPlayerModelParts.CAPE) && ("Dinnerbone".equals(var25.getName()) || "Grumm".equals(var25.getName()));
               this.mc.getTextureManager().bindTexture(var24.getLocationSkin());
               int var28 = 8 + (var27 ? 8 : 0);
               int var29 = 8 * (var27 ? -1 : 1);
               Gui.drawScaledCustomSizeModalRect(var22, var23, 8.0F, (float)var28, 8, var29, 8, 8, 64.0F, 64.0F);
               if (var26 != null && var26.isWearing(EnumPlayerModelParts.HAT)) {
                  int var30 = 8 + (var27 ? 8 : 0);
                  int var31 = 8 * (var27 ? -1 : 1);
                  Gui.drawScaledCustomSizeModalRect(var22, var23, 40.0F, (float)var30, 8, var31, 8, 8, 64.0F, 64.0F);
               }

               var22 += 9;
            }

            String var48 = this.getPlayerName(var24);
            if (var24.getGameType() == GameType.SPECTATOR) {
               this.mc.fontRendererObj.drawStringWithShadow(TextFormatting.ITALIC + var48, (float)var22, (float)var23, -1862270977);
            } else {
               this.mc.fontRendererObj.drawStringWithShadow(var48, (float)var22, (float)var23, -1);
            }

            if (var3 != null && var24.getGameType() != GameType.SPECTATOR) {
               int var49 = var22 + var6 + 1;
               int var50 = var49 + var12;
               if (var50 - var49 > 5) {
                  this.drawScoreboardValues(var3, var23, var25.getName(), var49, var50, var24);
               }
            }

            this.drawPing(var13, var22 - (var11 ? 9 : 0), var23, var24);
         }
      }

      if (var38 != null) {
         var15 = var15 + var34 * 9 + 1;
         drawRect(var1 / 2 - var16 / 2 - 1, var15 - 1, var1 / 2 + var16 / 2 + 1, var15 + var38.size() * this.mc.fontRendererObj.FONT_HEIGHT, Integer.MIN_VALUE);

         for(String var45 : var38) {
            int var47 = this.mc.fontRendererObj.getStringWidth(var45);
            this.mc.fontRendererObj.drawStringWithShadow(var45, (float)(var1 / 2 - var47 / 2), (float)var15, -1);
            var15 += this.mc.fontRendererObj.FONT_HEIGHT;
         }
      }

   }

   protected void drawPing(int var1, int var2, int var3, NetworkPlayerInfo var4) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(ICONS);
      boolean var5 = false;
      byte var6;
      if (var4.getResponseTime() < 0) {
         var6 = 5;
      } else if (var4.getResponseTime() < 150) {
         var6 = 0;
      } else if (var4.getResponseTime() < 300) {
         var6 = 1;
      } else if (var4.getResponseTime() < 600) {
         var6 = 2;
      } else if (var4.getResponseTime() < 1000) {
         var6 = 3;
      } else {
         var6 = 4;
      }

      this.zLevel += 100.0F;
      this.drawTexturedModalRect(var2 + var1 - 11, var3, 0, 176 + var6 * 8, 10, 8);
      this.zLevel -= 100.0F;
   }

   private void drawScoreboardValues(ScoreObjective var1, int var2, String var3, int var4, int var5, NetworkPlayerInfo var6) {
      int var7 = var1.getScoreboard().getOrCreateScore(var3, var1).getScorePoints();
      if (var1.getRenderType() == IScoreCriteria.EnumRenderType.HEARTS) {
         this.mc.getTextureManager().bindTexture(ICONS);
         if (this.lastTimeOpened == var6.getRenderVisibilityId()) {
            if (var7 < var6.getLastHealth()) {
               var6.setLastHealthTime(Minecraft.getSystemTime());
               var6.setHealthBlinkTime((long)(this.guiIngame.getUpdateCounter() + 20));
            } else if (var7 > var6.getLastHealth()) {
               var6.setLastHealthTime(Minecraft.getSystemTime());
               var6.setHealthBlinkTime((long)(this.guiIngame.getUpdateCounter() + 10));
            }
         }

         if (Minecraft.getSystemTime() - var6.getLastHealthTime() > 1000L || this.lastTimeOpened != var6.getRenderVisibilityId()) {
            var6.setLastHealth(var7);
            var6.setDisplayHealth(var7);
            var6.setLastHealthTime(Minecraft.getSystemTime());
         }

         var6.setRenderVisibilityId(this.lastTimeOpened);
         var6.setLastHealth(var7);
         int var8 = MathHelper.ceil((float)Math.max(var7, var6.getDisplayHealth()) / 2.0F);
         int var9 = Math.max(MathHelper.ceil((float)(var7 / 2)), Math.max(MathHelper.ceil((float)(var6.getDisplayHealth() / 2)), 10));
         boolean var10 = var6.getHealthBlinkTime() > (long)this.guiIngame.getUpdateCounter() && (var6.getHealthBlinkTime() - (long)this.guiIngame.getUpdateCounter()) / 3L % 2L == 1L;
         if (var8 > 0) {
            float var11 = Math.min((float)(var5 - var4 - 4) / (float)var9, 9.0F);
            if (var11 > 3.0F) {
               for(int var12 = var8; var12 < var9; ++var12) {
                  this.drawTexturedModalRect((float)var4 + (float)var12 * var11, (float)var2, var10 ? 25 : 16, 0, 9, 9);
               }

               for(int var16 = 0; var16 < var8; ++var16) {
                  this.drawTexturedModalRect((float)var4 + (float)var16 * var11, (float)var2, var10 ? 25 : 16, 0, 9, 9);
                  if (var10) {
                     if (var16 * 2 + 1 < var6.getDisplayHealth()) {
                        this.drawTexturedModalRect((float)var4 + (float)var16 * var11, (float)var2, 70, 0, 9, 9);
                     }

                     if (var16 * 2 + 1 == var6.getDisplayHealth()) {
                        this.drawTexturedModalRect((float)var4 + (float)var16 * var11, (float)var2, 79, 0, 9, 9);
                     }
                  }

                  if (var16 * 2 + 1 < var7) {
                     this.drawTexturedModalRect((float)var4 + (float)var16 * var11, (float)var2, var16 >= 10 ? 160 : 52, 0, 9, 9);
                  }

                  if (var16 * 2 + 1 == var7) {
                     this.drawTexturedModalRect((float)var4 + (float)var16 * var11, (float)var2, var16 >= 10 ? 169 : 61, 0, 9, 9);
                  }
               }
            } else {
               float var17 = MathHelper.clamp((float)var7 / 20.0F, 0.0F, 1.0F);
               int var13 = (int)((1.0F - var17) * 255.0F) << 16 | (int)(var17 * 255.0F) << 8;
               String var14 = "" + (float)var7 / 2.0F;
               if (var5 - this.mc.fontRendererObj.getStringWidth(var14 + "hp") >= var4) {
                  var14 = var14 + "hp";
               }

               this.mc.fontRendererObj.drawStringWithShadow(var14, (float)((var5 + var4) / 2 - this.mc.fontRendererObj.getStringWidth(var14) / 2), (float)var2, var13);
            }
         }
      } else {
         String var15 = TextFormatting.YELLOW + "" + var7;
         this.mc.fontRendererObj.drawStringWithShadow(var15, (float)(var5 - this.mc.fontRendererObj.getStringWidth(var15)), (float)var2, 16777215);
      }

   }

   public void setFooter(@Nullable ITextComponent var1) {
      this.footer = var1;
   }

   public void setHeader(@Nullable ITextComponent var1) {
      this.header = var1;
   }

   public void resetFooterHeader() {
      this.header = null;
      this.footer = null;
   }

   @SideOnly(Side.CLIENT)
   static class PlayerComparator implements Comparator {
      private PlayerComparator() {
      }

      public int compare(NetworkPlayerInfo var1, NetworkPlayerInfo var2) {
         ScorePlayerTeam var3 = var1.getPlayerTeam();
         ScorePlayerTeam var4 = var2.getPlayerTeam();
         return ComparisonChain.start().compareTrueFirst(var1.getGameType() != GameType.SPECTATOR, var2.getGameType() != GameType.SPECTATOR).compare(var3 != null ? var3.getRegisteredName() : "", var4 != null ? var4.getRegisteredName() : "").compare(var1.getGameProfile().getName(), var2.getGameProfile().getName()).result();
      }
   }
}
