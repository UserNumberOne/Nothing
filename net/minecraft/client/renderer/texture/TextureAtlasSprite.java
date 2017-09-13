package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import java.awt.image.BufferedImage;
import java.io.IOException;
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
      this.iconName = spriteName;
   }

   protected static TextureAtlasSprite makeAtlasSprite(ResourceLocation var0) {
      return new TextureAtlasSprite(spriteResourceLocation.toString());
   }

   public void initSprite(int var1, int var2, int var3, int var4, boolean var5) {
      this.originX = originInX;
      this.originY = originInY;
      this.rotated = rotatedIn;
      this.minU = (float)originInX / (float)inX;
      this.maxU = (float)(originInX + this.width) / (float)inX;
      this.minV = (float)originInY / (float)inY;
      this.maxV = (float)(originInY + this.height) / (float)inY;
   }

   public void copyFrom(TextureAtlasSprite var1) {
      this.originX = atlasSpirit.originX;
      this.originY = atlasSpirit.originY;
      this.width = atlasSpirit.width;
      this.height = atlasSpirit.height;
      this.rotated = atlasSpirit.rotated;
      this.minU = atlasSpirit.minU;
      this.maxU = atlasSpirit.maxU;
      this.minV = atlasSpirit.minV;
      this.maxV = atlasSpirit.maxV;
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
      float f = this.maxU - this.minU;
      return this.minU + f * (float)u / 16.0F;
   }

   public float getUnInterpolatedU(float var1) {
      float f = this.maxU - this.minU;
      return (p_188537_1_ - this.minU) / f * 16.0F;
   }

   public float getMinV() {
      return this.minV;
   }

   public float getMaxV() {
      return this.maxV;
   }

   public float getInterpolatedV(double var1) {
      float f = this.maxV - this.minV;
      return this.minV + f * (float)v / 16.0F;
   }

   public float getUnInterpolatedV(float var1) {
      float f = this.maxV - this.minV;
      return (p_188536_1_ - this.minV) / f * 16.0F;
   }

   public String getIconName() {
      return this.iconName;
   }

   public void updateAnimation() {
      ++this.tickCounter;
      if (this.tickCounter >= this.animationMetadata.getFrameTimeSingle(this.frameCounter)) {
         int i = this.animationMetadata.getFrameIndex(this.frameCounter);
         int j = this.animationMetadata.getFrameCount() == 0 ? this.framesTextureData.size() : this.animationMetadata.getFrameCount();
         this.frameCounter = (this.frameCounter + 1) % j;
         this.tickCounter = 0;
         int k = this.animationMetadata.getFrameIndex(this.frameCounter);
         if (i != k && k >= 0 && k < this.framesTextureData.size()) {
            TextureUtil.uploadTextureMipmap((int[][])this.framesTextureData.get(k), this.width, this.height, this.originX, this.originY, false, false);
         }
      } else if (this.animationMetadata.isInterpolate()) {
         this.updateAnimationInterpolated();
      }

   }

   private void updateAnimationInterpolated() {
      double d0 = 1.0D - (double)this.tickCounter / (double)this.animationMetadata.getFrameTimeSingle(this.frameCounter);
      int i = this.animationMetadata.getFrameIndex(this.frameCounter);
      int j = this.animationMetadata.getFrameCount() == 0 ? this.framesTextureData.size() : this.animationMetadata.getFrameCount();
      int k = this.animationMetadata.getFrameIndex((this.frameCounter + 1) % j);
      if (i != k && k >= 0 && k < this.framesTextureData.size()) {
         int[][] aint = (int[][])this.framesTextureData.get(i);
         int[][] aint1 = (int[][])this.framesTextureData.get(k);
         if (this.interpolatedFrameData == null || this.interpolatedFrameData.length != aint.length) {
            this.interpolatedFrameData = new int[aint.length][];
         }

         for(int l = 0; l < aint.length; ++l) {
            if (this.interpolatedFrameData[l] == null) {
               this.interpolatedFrameData[l] = new int[aint[l].length];
            }

            if (l < aint1.length && aint1[l].length == aint[l].length) {
               for(int i1 = 0; i1 < aint[l].length; ++i1) {
                  int j1 = aint[l][i1];
                  int k1 = aint1[l][i1];
                  int l1 = this.interpolateColor(d0, j1 >> 16 & 255, k1 >> 16 & 255);
                  int i2 = this.interpolateColor(d0, j1 >> 8 & 255, k1 >> 8 & 255);
                  int j2 = this.interpolateColor(d0, j1 & 255, k1 & 255);
                  this.interpolatedFrameData[l][i1] = j1 & -16777216 | l1 << 16 | i2 << 8 | j2;
               }
            }
         }

         TextureUtil.uploadTextureMipmap(this.interpolatedFrameData, this.width, this.height, this.originX, this.originY, false, false);
      }

   }

   private int interpolateColor(double var1, int var3, int var4) {
      return (int)(p_188535_1_ * (double)p_188535_3_ + (1.0D - p_188535_1_) * (double)p_188535_4_);
   }

   public int[][] getFrameTextureData(int var1) {
      return (int[][])this.framesTextureData.get(index);
   }

   public int getFrameCount() {
      return this.framesTextureData.size();
   }

   public void setIconWidth(int var1) {
      this.width = newWidth;
   }

   public void setIconHeight(int var1) {
      this.height = newHeight;
   }

   public void loadSprite(PngSizeInfo var1, boolean var2) throws IOException {
      this.resetSprite();
      this.width = sizeInfo.pngWidth;
      this.height = sizeInfo.pngHeight;
      if (p_188538_2_) {
         this.height = this.width;
      } else if (sizeInfo.pngHeight != sizeInfo.pngWidth) {
         throw new RuntimeException("broken aspect ratio and not an animation");
      }

   }

   public void loadSpriteFrames(IResource var1, int var2) throws IOException {
      BufferedImage bufferedimage = TextureUtil.readBufferedImage(resource.getInputStream());
      AnimationMetadataSection animationmetadatasection = (AnimationMetadataSection)resource.getMetadata("animation");
      int[][] aint = new int[mipmaplevels][];
      aint[0] = new int[bufferedimage.getWidth() * bufferedimage.getHeight()];
      bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), aint[0], 0, bufferedimage.getWidth());
      if (animationmetadatasection == null) {
         this.framesTextureData.add(aint);
      } else {
         int i = bufferedimage.getHeight() / this.width;
         if (animationmetadatasection.getFrameCount() > 0) {
            Iterator iterator = animationmetadatasection.getFrameIndexSet().iterator();

            while(iterator.hasNext()) {
               int j = ((Integer)iterator.next()).intValue();
               if (j >= i) {
                  throw new RuntimeException("invalid frameindex " + j);
               }

               this.allocateFrameTextureData(j);
               this.framesTextureData.set(j, getFrameTextureData(aint, this.width, this.width, j));
            }

            this.animationMetadata = animationmetadatasection;
         } else {
            List list = Lists.newArrayList();

            for(int k = 0; k < i; ++k) {
               this.framesTextureData.add(getFrameTextureData(aint, this.width, this.width, k));
               list.add(new AnimationFrame(k, -1));
            }

            this.animationMetadata = new AnimationMetadataSection(list, this.width, this.height, animationmetadatasection.getFrameTime(), animationmetadatasection.isInterpolate());
         }
      }

   }

   public void generateMipmaps(int var1) {
      List list = Lists.newArrayList();

      for(int i = 0; i < this.framesTextureData.size(); ++i) {
         final int[][] aint = (int[][])this.framesTextureData.get(i);
         if (aint != null) {
            try {
               list.add(TextureUtil.generateMipmapData(level, this.width, aint));
            } catch (Throwable var8) {
               CrashReport crashreport = CrashReport.makeCrashReport(var8, "Generating mipmaps for frame");
               CrashReportCategory crashreportcategory = crashreport.makeCategory("Frame being iterated");
               crashreportcategory.addCrashSection("Frame index", Integer.valueOf(i));
               crashreportcategory.setDetail("Frame sizes", new ICrashReportDetail() {
                  public String call() throws Exception {
                     StringBuilder stringbuilder = new StringBuilder();

                     for(int[] aint1 : aint) {
                        if (stringbuilder.length() > 0) {
                           stringbuilder.append(", ");
                        }

                        stringbuilder.append(aint1 == null ? "null" : aint1.length);
                     }

                     return stringbuilder.toString();
                  }
               });
               throw new ReportedException(crashreport);
            }
         }
      }

      this.setFramesTextureData(list);
   }

   private void allocateFrameTextureData(int var1) {
      if (this.framesTextureData.size() <= index) {
         for(int i = this.framesTextureData.size(); i <= index; ++i) {
            this.framesTextureData.add((int[][])null);
         }
      }

   }

   private static int[][] getFrameTextureData(int[][] var0, int var1, int var2, int var3) {
      int[][] aint = new int[data.length][];

      for(int i = 0; i < data.length; ++i) {
         int[] aint1 = data[i];
         if (aint1 != null) {
            aint[i] = new int[(rows >> i) * (columns >> i)];
            System.arraycopy(aint1, p_147962_3_ * aint[i].length, aint[i], 0, aint[i].length);
         }
      }

      return aint;
   }

   public void clearFramesTextureData() {
      this.framesTextureData.clear();
   }

   public boolean hasAnimationMetadata() {
      return this.animationMetadata != null;
   }

   public void setFramesTextureData(List var1) {
      this.framesTextureData = newFramesTextureData;
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
