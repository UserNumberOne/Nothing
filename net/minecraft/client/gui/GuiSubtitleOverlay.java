package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISoundEventListener;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSubtitleOverlay extends Gui implements ISoundEventListener {
   private final Minecraft client;
   private final List subtitles = Lists.newArrayList();
   private boolean enabled;

   public GuiSubtitleOverlay(Minecraft var1) {
      this.client = var1;
   }

   public void renderSubtitles(ScaledResolution var1) {
      if (!this.enabled && this.client.gameSettings.showSubtitles) {
         this.client.getSoundHandler().addListener(this);
         this.enabled = true;
      } else if (this.enabled && !this.client.gameSettings.showSubtitles) {
         this.client.getSoundHandler().removeListener(this);
         this.enabled = false;
      }

      if (this.enabled && !this.subtitles.isEmpty()) {
         GlStateManager.pushMatrix();
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
         Vec3d var2 = new Vec3d(this.client.player.posX, this.client.player.posY + (double)this.client.player.getEyeHeight(), this.client.player.posZ);
         Vec3d var3 = (new Vec3d(0.0D, 0.0D, -1.0D)).rotatePitch(-this.client.player.rotationPitch * 0.017453292F).rotateYaw(-this.client.player.rotationYaw * 0.017453292F);
         Vec3d var4 = (new Vec3d(0.0D, 1.0D, 0.0D)).rotatePitch(-this.client.player.rotationPitch * 0.017453292F).rotateYaw(-this.client.player.rotationYaw * 0.017453292F);
         Vec3d var5 = var3.crossProduct(var4);
         int var6 = 0;
         int var7 = 0;
         Iterator var8 = this.subtitles.iterator();

         while(var8.hasNext()) {
            GuiSubtitleOverlay.Subtitle var9 = (GuiSubtitleOverlay.Subtitle)var8.next();
            if (var9.getStartTime() + 3000L <= Minecraft.getSystemTime()) {
               var8.remove();
            } else {
               var7 = Math.max(var7, this.client.fontRendererObj.getStringWidth(var9.getString()));
            }
         }

         var7 = var7 + this.client.fontRendererObj.getStringWidth("<") + this.client.fontRendererObj.getStringWidth(" ") + this.client.fontRendererObj.getStringWidth(">") + this.client.fontRendererObj.getStringWidth(" ");

         for(GuiSubtitleOverlay.Subtitle var10 : this.subtitles) {
            boolean var11 = true;
            String var12 = var10.getString();
            Vec3d var13 = var10.getLocation().subtract(var2).normalize();
            double var14 = -var5.dotProduct(var13);
            double var16 = -var3.dotProduct(var13);
            boolean var18 = var16 > 0.5D;
            int var19 = var7 / 2;
            int var20 = this.client.fontRendererObj.FONT_HEIGHT;
            int var21 = var20 / 2;
            float var22 = 1.0F;
            int var23 = this.client.fontRendererObj.getStringWidth(var12);
            int var24 = MathHelper.floor(MathHelper.clampedLerp(255.0D, 75.0D, (double)((float)(Minecraft.getSystemTime() - var10.getStartTime()) / 3000.0F)));
            int var25 = var24 << 16 | var24 << 8 | var24;
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)var1.getScaledWidth() - (float)var19 * 1.0F - 2.0F, (float)(var1.getScaledHeight() - 30) - (float)(var6 * (var20 + 1)) * 1.0F, 0.0F);
            GlStateManager.scale(1.0F, 1.0F, 1.0F);
            drawRect(-var19 - 1, -var21 - 1, var19 + 1, var21 + 1, -872415232);
            GlStateManager.enableBlend();
            if (!var18) {
               if (var14 > 0.0D) {
                  this.client.fontRendererObj.drawString(">", var19 - this.client.fontRendererObj.getStringWidth(">"), -var21, var25 + -16777216);
               } else if (var14 < 0.0D) {
                  this.client.fontRendererObj.drawString("<", -var19, -var21, var25 + -16777216);
               }
            }

            this.client.fontRendererObj.drawString(var12, -var23 / 2, -var21, var25 + -16777216);
            GlStateManager.popMatrix();
            ++var6;
         }

         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }

   }

   public void soundPlay(ISound var1, SoundEventAccessor var2) {
      if (var2.getSubtitle() != null) {
         String var3 = var2.getSubtitle().getFormattedText();
         if (!this.subtitles.isEmpty()) {
            for(GuiSubtitleOverlay.Subtitle var5 : this.subtitles) {
               if (var5.getString().equals(var3)) {
                  var5.refresh(new Vec3d((double)var1.getXPosF(), (double)var1.getYPosF(), (double)var1.getZPosF()));
                  return;
               }
            }
         }

         this.subtitles.add(new GuiSubtitleOverlay.Subtitle(var3, new Vec3d((double)var1.getXPosF(), (double)var1.getYPosF(), (double)var1.getZPosF())));
      }

   }

   @SideOnly(Side.CLIENT)
   public class Subtitle {
      private final String subtitle;
      private long startTime;
      private Vec3d location;

      public Subtitle(String var2, Vec3d var3) {
         this.subtitle = var2;
         this.location = var3;
         this.startTime = Minecraft.getSystemTime();
      }

      public String getString() {
         return this.subtitle;
      }

      public long getStartTime() {
         return this.startTime;
      }

      public Vec3d getLocation() {
         return this.location;
      }

      public void refresh(Vec3d var1) {
         this.location = var1;
         this.startTime = Minecraft.getSystemTime();
      }
   }
}
