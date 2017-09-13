package net.minecraft.world;

import java.util.UUID;
import net.minecraft.util.text.ITextComponent;

public abstract class BossInfo {
   private final UUID uniqueId;
   protected ITextComponent name;
   protected float percent;
   protected BossInfo.Color color;
   protected BossInfo.Overlay overlay;
   protected boolean darkenSky;
   protected boolean playEndBossMusic;
   protected boolean createFog;

   public BossInfo(UUID var1, ITextComponent var2, BossInfo.Color var3, BossInfo.Overlay var4) {
      this.uniqueId = var1;
      this.name = var2;
      this.color = var3;
      this.overlay = var4;
      this.percent = 1.0F;
   }

   public UUID getUniqueId() {
      return this.uniqueId;
   }

   public ITextComponent getName() {
      return this.name;
   }

   public void setName(ITextComponent var1) {
      this.name = var1;
   }

   public float getPercent() {
      return this.percent;
   }

   public void setPercent(float var1) {
      this.percent = var1;
   }

   public BossInfo.Color getColor() {
      return this.color;
   }

   public void setColor(BossInfo.Color var1) {
      this.color = var1;
   }

   public BossInfo.Overlay getOverlay() {
      return this.overlay;
   }

   public void setOverlay(BossInfo.Overlay var1) {
      this.overlay = var1;
   }

   public boolean shouldDarkenSky() {
      return this.darkenSky;
   }

   public BossInfo setDarkenSky(boolean var1) {
      this.darkenSky = var1;
      return this;
   }

   public boolean shouldPlayEndBossMusic() {
      return this.playEndBossMusic;
   }

   public BossInfo setPlayEndBossMusic(boolean var1) {
      this.playEndBossMusic = var1;
      return this;
   }

   public BossInfo setCreateFog(boolean var1) {
      this.createFog = var1;
      return this;
   }

   public boolean shouldCreateFog() {
      return this.createFog;
   }

   public static enum Color {
      PINK,
      BLUE,
      RED,
      GREEN,
      YELLOW,
      PURPLE,
      WHITE;
   }

   public static enum Overlay {
      PROGRESS,
      NOTCHED_6,
      NOTCHED_10,
      NOTCHED_12,
      NOTCHED_20;
   }
}
