package net.minecraft.client.resources.data;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AnimationMetadataSection implements IMetadataSection {
   private final List animationFrames;
   private final int frameWidth;
   private final int frameHeight;
   private final int frameTime;
   private final boolean interpolate;

   public AnimationMetadataSection(List var1, int var2, int var3, int var4, boolean var5) {
      this.animationFrames = animationFramesIn;
      this.frameWidth = frameWidthIn;
      this.frameHeight = frameHeightIn;
      this.frameTime = frameTimeIn;
      this.interpolate = interpolateIn;
   }

   public int getFrameHeight() {
      return this.frameHeight;
   }

   public int getFrameWidth() {
      return this.frameWidth;
   }

   public int getFrameCount() {
      return this.animationFrames.size();
   }

   public int getFrameTime() {
      return this.frameTime;
   }

   public boolean isInterpolate() {
      return this.interpolate;
   }

   private AnimationFrame getAnimationFrame(int var1) {
      return (AnimationFrame)this.animationFrames.get(frame);
   }

   public int getFrameTimeSingle(int var1) {
      AnimationFrame animationframe = this.getAnimationFrame(frame);
      return animationframe.hasNoTime() ? this.frameTime : animationframe.getFrameTime();
   }

   public boolean frameHasTime(int var1) {
      return !((AnimationFrame)this.animationFrames.get(frame)).hasNoTime();
   }

   public int getFrameIndex(int var1) {
      return ((AnimationFrame)this.animationFrames.get(frame)).getFrameIndex();
   }

   public Set getFrameIndexSet() {
      Set set = Sets.newHashSet();

      for(AnimationFrame animationframe : this.animationFrames) {
         set.add(Integer.valueOf(animationframe.getFrameIndex()));
      }

      return set;
   }
}
