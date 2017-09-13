package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationFrame;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TextureAtlasSprite {
   private final String iconName;
   protected List framesTextureData = Lists.newArrayList();
   protected int[][] interpolatedFrameData;
   private AnimationMetadataSection animationMetadata;
   protected boolean rotated;
   protected int originX;
   protected int originY;
   protected int width;
   protected int height;
   private float minU;
   private float maxU;
   private float minV;
   private float maxV;
   protected int frameCounter;
   protected int tickCounter;

   protected TextureAtlasSprite(String var1) {
      this.iconName = var1;
   }

   protected static TextureAtlasSprite makeAtlasSprite(ResourceLocation var0) {
      return new TextureAtlasSprite(var0.toString());
   }

   public void initSprite(int var1, int var2, int var3, int var4, boolean var5) {
      this.originX = var3;
      this.originY = var4;
      this.rotated = var5;
      this.minU = (float)var3 / (float)var1;
      this.maxU = (float)(var3 + this.width) / (float)var1;
      this.minV = (float)var4 / (float)var2;
      this.maxV = (float)(var4 + this.height) / (float)var2;
   }

   public void copyFrom(TextureAtlasSprite var1) {
      this.originX = var1.originX;
      this.originY = var1.originY;
      this.width = var1.width;
      this.height = var1.height;
      this.rotated = var1.rotated;
      this.minU = var1.minU;
      this.maxU = var1.maxU;
      this.minV = var1.minV;
      this.maxV = var1.maxV;
   }

   public int getOriginX() {
      return this.originX;
   }

   public int getOriginY() {
      return this.originY;
   }

   public int getIconWidth() {
      return this.width;
   }

   public int getIconHeight() {
      return this.height;
   }

   public float getMinU() {
      return this.minU;
   }

   public float getMaxU() {
      return this.maxU;
   }

   public float getInterpolatedU(double var1) {
      float var3 = this.maxU - this.minU;
      return this.minU + var3 * (float)var1 / 16.0F;
   }

   public float getUnInterpolatedU(float var1) {
      float var2 = this.maxU - this.minU;
      return (var1 - this.minU) / var2 * 16.0F;
   }

   public float getMinV() {
      return this.minV;
   }

   public float getMaxV() {
      return this.maxV;
   }

   public float getInterpolatedV(double var1) {
      float var3 = this.maxV - this.minV;
      return this.minV + var3 * (float)var1 / 16.0F;
   }

   public float getUnInterpolatedV(float var1) {
      float var2 = this.maxV - this.minV;
      return (var1 - this.minV) / var2 * 16.0F;
   }

   public String getIconName() {
      return this.iconName;
   }

   public void updateAnimation() {
      ++this.tickCounter;
      if (this.tickCounter >= this.animationMetadata.getFrameTimeSingle(this.frameCounter)) {
         int var1 = this.animationMetadata.getFrameIndex(this.frameCounter);
         int var2 = this.animationMetadata.getFrameCount() == 0 ? this.framesTextureData.size() : this.animationMetadata.getFrameCount();
         this.frameCounter = (this.frameCounter + 1) % var2;
         this.tickCounter = 0;
         int var3 = this.animationMetadata.getFrameIndex(this.frameCounter);
         if (var1 != var3 && var3 >= 0 && var3 < this.framesTextureData.size()) {
            TextureUtil.uploadTextureMipmap((int[][])this.framesTextureData.get(var3), this.width, this.height, this.originX, this.originY, false, false);
         }
      } else if (this.animationMetadata.isInterpolate()) {
         this.updateAnimationInterpolated();
      }

   }

   private void updateAnimationInterpolated() {
      double var1 = 1.0D - (double)this.tickCounter / (double)this.animationMetadata.getFrameTimeSingle(this.frameCounter);
      int var3 = this.animationMetadata.getFrameIndex(this.frameCounter);
      int var4 = this.animationMetadata.getFrameCount() == 0 ? this.framesTextureData.size() : this.animationMetadata.getFrameCount();
      int var5 = this.animationMetadata.getFrameIndex((this.frameCounter + 1) % var4);
      if (var3 != var5 && var5 >= 0 && var5 < this.framesTextureData.size()) {
         int[][] var6 = (int[][])this.framesTextureData.get(var3);
         int[][] var7 = (int[][])this.framesTextureData.get(var5);
         if (this.interpolatedFrameData == null || this.interpolatedFrameData.length != var6.length) {
            this.interpolatedFrameData = new int[var6.length][];
         }

         for(int var8 = 0; var8 < var6.length; ++var8) {
            if (this.interpolatedFrameData[var8] == null) {
               this.interpolatedFrameData[var8] = new int[var6[var8].length];
            }

            if (var8 < var7.length && var7[var8].length == var6[var8].length) {
               for(int var9 = 0; var9 < var6[var8].length; ++var9) {
                  int var10 = var6[var8][var9];
                  int var11 = var7[var8][var9];
                  int var12 = this.interpolateColor(var1, var10 >> 16 & 255, var11 >> 16 & 255);
                  int var13 = this.interpolateColor(var1, var10 >> 8 & 255, var11 >> 8 & 255);
                  int var14 = this.interpolateColor(var1, var10 & 255, var11 & 255);
                  this.interpolatedFrameData[var8][var9] = var10 & -16777216 | var12 << 16 | var13 << 8 | var14;
               }
            }
         }

         TextureUtil.uploadTextureMipmap(this.interpolatedFrameData, this.width, this.height, this.originX, this.originY, false, false);
      }

   }

   private int interpolateColor(double var1, int var3, int var4) {
      return (int)(var1 * (double)var3 + (1.0D - var1) * (double)var4);
   }

   public int[][] getFrameTextureData(int var1) {
      return (int[][])this.framesTextureData.get(var1);
   }

   public int getFrameCount() {
      return this.framesTextureData.size();
   }

   public void setIconWidth(int var1) {
      this.width = var1;
   }

   public void setIconHeight(int var1) {
      this.height = var1;
   }

   public void loadSprite(PngSizeInfo var1, boolean var2) throws IOException {
      this.resetSprite();
      this.width = var1.pngWidth;
      this.height = var1.pngHeight;
      if (var2) {
         this.height = this.width;
      } else if (var1.pngHeight != var1.pngWidth) {
         throw new RuntimeException("broken aspect ratio and not an animation");
      }

   }

   public void loadSpriteFrames(IResource var1, int var2) throws IOException {
      BufferedImage var3 = TextureUtil.readBufferedImage(var1.getInputStream());
      AnimationMetadataSection var4 = (AnimationMetadataSection)var1.getMetadata("animation");
      int[][] var5 = new int[var2][];
      var5[0] = new int[var3.getWidth() * var3.getHeight()];
      var3.getRGB(0, 0, var3.getWidth(), var3.getHeight(), var5[0], 0, var3.getWidth());
      if (var4 == null) {
         this.framesTextureData.add(var5);
      } else {
         int var6 = var3.getHeight() / this.width;
         if (var4.getFrameCount() > 0) {
            Iterator var7 = var4.getFrameIndexSet().iterator();

            while(var7.hasNext()) {
               int var8 = ((Integer)var7.next()).intValue();
               if (var8 >= var6) {
                  throw new RuntimeException("invalid frameindex " + var8);
               }

               this.allocateFrameTextureData(var8);
               this.framesTextureData.set(var8, getFrameTextureData(var5, this.width, this.width, var8));
            }

            this.animationMetadata = var4;
         } else {
            ArrayList var9 = Lists.newArrayList();

            for(int var10 = 0; var10 < var6; ++var10) {
               this.framesTextureData.add(getFrameTextureData(var5, this.width, this.width, var10));
               var9.add(new AnimationFrame(var10, -1));
            }

            this.animationMetadata = new AnimationMetadataSection(var9, this.width, this.height, var4.getFrameTime(), var4.isInterpolate());
         }
      }

   }

   public void generateMipmaps(int var1) {
      ArrayList var2 = Lists.newArrayList();

      for(int var3 = 0; var3 < this.framesTextureData.size(); ++var3) {
         final int[][] var4 = (int[][])this.framesTextureData.get(var3);
         if (var4 != null) {
            try {
               var2.add(TextureUtil.generateMipmapData(var1, this.width, var4));
            } catch (Throwable var8) {
               CrashReport var6 = CrashReport.makeCrashReport(var8, "Generating mipmaps for frame");
               CrashReportCategory var7 = var6.makeCategory("Frame being iterated");
               var7.addCrashSection("Frame index", Integer.valueOf(var3));
               var7.setDetail("Frame sizes", new ICrashReportDetail() {
                  public String call() throws Exception {
                     StringBuilder var1 = new StringBuilder();

                     for(int[] var5 : var4) {
                        if (var1.length() > 0) {
                           var1.append(", ");
                        }

                        var1.append(var5 == null ? "null" : var5.length);
                     }

                     return var1.toString();
                  }
               });
               throw new ReportedException(var6);
            }
         }
      }

      this.setFramesTextureData(var2);
   }

   private void allocateFrameTextureData(int var1) {
      if (this.framesTextureData.size() <= var1) {
         for(int var2 = this.framesTextureData.size(); var2 <= var1; ++var2) {
            this.framesTextureData.add((int[][])null);
         }
      }

   }

   private static int[][] getFrameTextureData(int[][] var0, int var1, int var2, int var3) {
      int[][] var4 = new int[var0.length][];

      for(int var5 = 0; var5 < var0.length; ++var5) {
         int[] var6 = var0[var5];
         if (var6 != null) {
            var4[var5] = new int[(var1 >> var5) * (var2 >> var5)];
            System.arraycopy(var6, var3 * var4[var5].length, var4[var5], 0, var4[var5].length);
         }
      }

      return var4;
   }

   public void clearFramesTextureData() {
      this.framesTextureData.clear();
   }

   public boolean hasAnimationMetadata() {
      return this.animationMetadata != null;
   }

   public void setFramesTextureData(List var1) {
      this.framesTextureData = var1;
   }

   private void resetSprite() {
      this.animationMetadata = null;
      this.setFramesTextureData(Lists.newArrayList());
      this.frameCounter = 0;
      this.tickCounter = 0;
   }

   public String toString() {
      return "TextureAtlasSprite{name='" + this.iconName + '\'' + ", frameCount=" + this.framesTextureData.size() + ", rotated=" + this.rotated + ", x=" + this.originX + ", y=" + this.originY + ", height=" + this.height + ", width=" + this.width + ", u0=" + this.minU + ", u1=" + this.maxU + ", v0=" + this.minV + ", v1=" + this.maxV + '}';
   }

   public boolean hasCustomLoader(IResourceManager var1, ResourceLocation var2) {
      return false;
   }

   public boolean load(IResourceManager var1, ResourceLocation var2) {
      return true;
   }
}
