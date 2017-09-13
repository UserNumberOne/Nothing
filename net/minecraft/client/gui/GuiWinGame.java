package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class GuiWinGame extends GuiScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final ResourceLocation MINECRAFT_LOGO = new ResourceLocation("textures/gui/title/minecraft.png");
   private static final ResourceLocation VIGNETTE_TEXTURE = new ResourceLocation("textures/misc/vignette.png");
   private int time;
   private List lines;
   private int totalScrollLength;
   private final float scrollSpeed = 0.5F;

   public void updateScreen() {
      MusicTicker var1 = this.mc.getMusicTicker();
      SoundHandler var2 = this.mc.getSoundHandler();
      if (this.time == 0) {
         var1.stopMusic();
         var1.playMusic(MusicTicker.MusicType.CREDITS);
         var2.resumeSounds();
      }

      var2.update();
      ++this.time;
      float var3 = (float)(this.totalScrollLength + this.height + this.height + 24) / 0.5F;
      if ((float)this.time > var3) {
         this.sendRespawnPacket();
      }

   }

   protected void keyTyped(char var1, int var2) throws IOException {
      if (var2 == 1) {
         this.sendRespawnPacket();
      }

   }

   private void sendRespawnPacket() {
      this.mc.player.connection.sendPacket(new CPacketClientStatus(CPacketClientStatus.State.PERFORM_RESPAWN));
      this.mc.displayGuiScreen((GuiScreen)null);
   }

   public boolean doesGuiPauseGame() {
      return true;
   }

   public void initGui() {
      if (this.lines == null) {
         this.lines = Lists.newArrayList();
         IResource var1 = null;

         try {
            String var2 = "" + TextFormatting.WHITE + TextFormatting.OBFUSCATED + TextFormatting.GREEN + TextFormatting.AQUA;
            boolean var3 = true;
            var1 = this.mc.getResourceManager().getResource(new ResourceLocation("texts/end.txt"));
            InputStream var4 = var1.getInputStream();
            BufferedReader var5 = new BufferedReader(new InputStreamReader(var4, Charsets.UTF_8));
            Random var6 = new Random(8124371L);

            String var7;
            while((var7 = var5.readLine()) != null) {
               String var8;
               String var9;
               for(var7 = var7.replaceAll("PLAYERNAME", this.mc.getSession().getUsername()); var7.contains(var2); var7 = var8 + TextFormatting.WHITE + TextFormatting.OBFUSCATED + "XXXXXXXX".substring(0, var6.nextInt(4) + 3) + var9) {
                  int var10 = var7.indexOf(var2);
                  var8 = var7.substring(0, var10);
                  var9 = var7.substring(var10 + var2.length());
               }

               this.lines.addAll(this.mc.fontRendererObj.listFormattedStringToWidth(var7, 274));
               this.lines.add("");
            }

            var4.close();

            for(int var22 = 0; var22 < 8; ++var22) {
               this.lines.add("");
            }

            var4 = this.mc.getResourceManager().getResource(new ResourceLocation("texts/credits.txt")).getInputStream();
            var5 = new BufferedReader(new InputStreamReader(var4, Charsets.UTF_8));

            while((var7 = var5.readLine()) != null) {
               var7 = var7.replaceAll("PLAYERNAME", this.mc.getSession().getUsername());
               var7 = var7.replaceAll("\t", "    ");
               this.lines.addAll(this.mc.fontRendererObj.listFormattedStringToWidth(var7, 274));
               this.lines.add("");
            }

            var4.close();
            this.totalScrollLength = this.lines.size() * 12;
         } catch (Exception var14) {
            LOGGER.error("Couldn't load credits", var14);
         } finally {
            IOUtils.closeQuietly(var1);
         }
      }

   }

   private void drawWinGameScreen(int var1, int var2, float var3) {
      Tessellator var4 = Tessellator.getInstance();
      VertexBuffer var5 = var4.getBuffer();
      this.mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
      var5.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      int var6 = this.width;
      float var7 = 0.0F - ((float)this.time + var3) * 0.5F * 0.5F;
      float var8 = (float)this.height - ((float)this.time + var3) * 0.5F * 0.5F;
      float var9 = 0.015625F;
      float var10 = ((float)this.time + var3 - 0.0F) * 0.02F;
      float var11 = (float)(this.totalScrollLength + this.height + this.height + 24) / 0.5F;
      float var12 = (var11 - 20.0F - ((float)this.time + var3)) * 0.005F;
      if (var12 < var10) {
         var10 = var12;
      }

      if (var10 > 1.0F) {
         var10 = 1.0F;
      }

      var10 = var10 * var10;
      var10 = var10 * 96.0F / 255.0F;
      var5.pos(0.0D, (double)this.height, (double)this.zLevel).tex(0.0D, (double)(var7 * 0.015625F)).color(var10, var10, var10, 1.0F).endVertex();
      var5.pos((double)var6, (double)this.height, (double)this.zLevel).tex((double)((float)var6 * 0.015625F), (double)(var7 * 0.015625F)).color(var10, var10, var10, 1.0F).endVertex();
      var5.pos((double)var6, 0.0D, (double)this.zLevel).tex((double)((float)var6 * 0.015625F), (double)(var8 * 0.015625F)).color(var10, var10, var10, 1.0F).endVertex();
      var5.pos(0.0D, 0.0D, (double)this.zLevel).tex(0.0D, (double)(var8 * 0.015625F)).color(var10, var10, var10, 1.0F).endVertex();
      var4.draw();
   }

   public void drawScreen(int var1, int var2, float var3) {
      this.drawWinGameScreen(var1, var2, var3);
      Tessellator var4 = Tessellator.getInstance();
      VertexBuffer var5 = var4.getBuffer();
      boolean var6 = true;
      int var7 = this.width / 2 - 137;
      int var8 = this.height + 50;
      float var9 = -((float)this.time + var3) * 0.5F;
      GlStateManager.pushMatrix();
      GlStateManager.translate(0.0F, var9, 0.0F);
      this.mc.getTextureManager().bindTexture(MINECRAFT_LOGO);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.drawTexturedModalRect(var7, var8, 0, 0, 155, 44);
      this.drawTexturedModalRect(var7 + 155, var8, 0, 45, 155, 44);
      int var10 = var8 + 200;

      for(int var11 = 0; var11 < this.lines.size(); ++var11) {
         if (var11 == this.lines.size() - 1) {
            float var12 = (float)var10 + var9 - (float)(this.height / 2 - 6);
            if (var12 < 0.0F) {
               GlStateManager.translate(0.0F, -var12, 0.0F);
            }
         }

         if ((float)var10 + var9 + 12.0F + 8.0F > 0.0F && (float)var10 + var9 < (float)this.height) {
            String var14 = (String)this.lines.get(var11);
            if (var14.startsWith("[C]")) {
               this.fontRendererObj.drawStringWithShadow(var14.substring(3), (float)(var7 + (274 - this.fontRendererObj.getStringWidth(var14.substring(3))) / 2), (float)var10, 16777215);
            } else {
               this.fontRendererObj.fontRandom.setSeed((long)var11 * 4238972211L + (long)(this.time / 4));
               this.fontRendererObj.drawStringWithShadow(var14, (float)var7, (float)var10, 16777215);
            }
         }

         var10 += 12;
      }

      GlStateManager.popMatrix();
      this.mc.getTextureManager().bindTexture(VIGNETTE_TEXTURE);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
      int var13 = this.width;
      int var15 = this.height;
      var5.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      var5.pos(0.0D, (double)var15, (double)this.zLevel).tex(0.0D, 1.0D).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
      var5.pos((double)var13, (double)var15, (double)this.zLevel).tex(1.0D, 1.0D).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
      var5.pos((double)var13, 0.0D, (double)this.zLevel).tex(1.0D, 0.0D).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
      var5.pos(0.0D, 0.0D, (double)this.zLevel).tex(0.0D, 0.0D).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
      var4.draw();
      GlStateManager.disableBlend();
      super.drawScreen(var1, var2, var3);
   }
}
