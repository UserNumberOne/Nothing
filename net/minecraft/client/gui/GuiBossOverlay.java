package net.minecraft.client.gui;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.play.server.SPacketUpdateBossInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.BossInfoLerping;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderGameOverlayEvent.BossInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiBossOverlay extends Gui {
   private static final ResourceLocation GUI_BARS_TEXTURES = new ResourceLocation("textures/gui/bars.png");
   private final Minecraft client;
   private final Map mapBossInfos = Maps.newLinkedHashMap();

   public GuiBossOverlay(Minecraft var1) {
      this.client = var1;
   }

   public void renderBossHealth() {
      if (!this.mapBossInfos.isEmpty()) {
         ScaledResolution var1 = new ScaledResolution(this.client);
         int var2 = var1.getScaledWidth();
         int var3 = 12;

         for(BossInfoLerping var5 : this.mapBossInfos.values()) {
            int var6 = var2 / 2 - 91;
            BossInfo var7 = ForgeHooksClient.bossBarRenderPre(var1, var5, var6, var3, 10 + this.client.fontRendererObj.FONT_HEIGHT);
            if (!var7.isCanceled()) {
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               this.client.getTextureManager().bindTexture(GUI_BARS_TEXTURES);
               this.render(var6, var3, var5);
               String var8 = var5.getName().getFormattedText();
               this.client.fontRendererObj.drawStringWithShadow(var8, (float)(var2 / 2 - this.client.fontRendererObj.getStringWidth(var8) / 2), (float)(var3 - 9), 16777215);
            }

            var3 += var7.getIncrement();
            ForgeHooksClient.bossBarRenderPost(var1);
            if (var3 >= var1.getScaledHeight() / 3) {
               break;
            }
         }
      }

   }

   private void render(int var1, int var2, net.minecraft.world.BossInfo var3) {
      this.drawTexturedModalRect(var1, var2, 0, var3.getColor().ordinal() * 5 * 2, 182, 5);
      if (var3.getOverlay() != net.minecraft.world.BossInfo.Overlay.PROGRESS) {
         this.drawTexturedModalRect(var1, var2, 0, 80 + (var3.getOverlay().ordinal() - 1) * 5 * 2, 182, 5);
      }

      int var4 = (int)(var3.getPercent() * 183.0F);
      if (var4 > 0) {
         this.drawTexturedModalRect(var1, var2, 0, var3.getColor().ordinal() * 5 * 2 + 5, var4, 5);
         if (var3.getOverlay() != net.minecraft.world.BossInfo.Overlay.PROGRESS) {
            this.drawTexturedModalRect(var1, var2, 0, 80 + (var3.getOverlay().ordinal() - 1) * 5 * 2 + 5, var4, 5);
         }
      }

   }

   public void read(SPacketUpdateBossInfo var1) {
      if (var1.getOperation() == SPacketUpdateBossInfo.Operation.ADD) {
         this.mapBossInfos.put(var1.getUniqueId(), new BossInfoLerping(var1));
      } else if (var1.getOperation() == SPacketUpdateBossInfo.Operation.REMOVE) {
         this.mapBossInfos.remove(var1.getUniqueId());
      } else {
         ((BossInfoLerping)this.mapBossInfos.get(var1.getUniqueId())).updateFromPacket(var1);
      }

   }

   public void clearBossInfos() {
      this.mapBossInfos.clear();
   }

   public boolean shouldPlayEndBossMusic() {
      if (!this.mapBossInfos.isEmpty()) {
         for(net.minecraft.world.BossInfo var2 : this.mapBossInfos.values()) {
            if (var2.shouldPlayEndBossMusic()) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean shouldDarkenSky() {
      if (!this.mapBossInfos.isEmpty()) {
         for(net.minecraft.world.BossInfo var2 : this.mapBossInfos.values()) {
            if (var2.shouldDarkenSky()) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean shouldCreateFog() {
      if (!this.mapBossInfos.isEmpty()) {
         for(net.minecraft.world.BossInfo var2 : this.mapBossInfos.values()) {
            if (var2.shouldCreateFog()) {
               return true;
            }
         }
      }

      return false;
   }
}
