package net.minecraft.world;

import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketUpdateBossInfo;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BossInfoLerping extends BossInfo {
   protected float rawPercent;
   protected long percentSetTime;

   public BossInfoLerping(SPacketUpdateBossInfo var1) {
      super(var1.getUniqueId(), var1.getName(), var1.getColor(), var1.getOverlay());
      this.rawPercent = var1.getPercent();
      this.percent = var1.getPercent();
      this.percentSetTime = Minecraft.getSystemTime();
      this.setDarkenSky(var1.shouldDarkenSky());
      this.setPlayEndBossMusic(var1.shouldPlayEndBossMusic());
      this.setCreateFog(var1.shouldCreateFog());
   }

   public void setPercent(float var1) {
      this.percent = this.getPercent();
      this.rawPercent = var1;
      this.percentSetTime = Minecraft.getSystemTime();
   }

   public float getPercent() {
      long var1 = Minecraft.getSystemTime() - this.percentSetTime;
      float var3 = MathHelper.clamp((float)var1 / 100.0F, 0.0F, 1.0F);
      return this.percent + (this.rawPercent - this.percent) * var3;
   }

   public void updateFromPacket(SPacketUpdateBossInfo var1) {
      switch(var1.getOperation()) {
      case UPDATE_NAME:
         this.setName(var1.getName());
         break;
      case UPDATE_PCT:
         this.setPercent(var1.getPercent());
         break;
      case UPDATE_STYLE:
         this.setColor(var1.getColor());
         this.setOverlay(var1.getOverlay());
         break;
      case UPDATE_PROPERTIES:
         this.setDarkenSky(var1.shouldDarkenSky());
         this.setPlayEndBossMusic(var1.shouldPlayEndBossMusic());
      }

   }
}
