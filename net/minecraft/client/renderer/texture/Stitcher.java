package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.StitcherException;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Stitcher {
   private final int mipmapLevelStitcher;
   private final Set setStitchHolders = Sets.newHashSetWithExpectedSize(256);
   private final List stitchSlots = Lists.newArrayListWithCapacity(256);
   private int currentWidth;
   private int currentHeight;
   private final int maxWidth;
   private final int maxHeight;
   private final int maxTileDimension;

   public Stitcher(int var1, int var2, int var3, int var4) {
      this.mipmapLevelStitcher = var4;
      this.maxWidth = var1;
      this.maxHeight = var2;
      this.maxTileDimension = var3;
   }

   public int getCurrentWidth() {
      return this.currentWidth;
   }

   public int getCurrentHeight() {
      return this.currentHeight;
   }

   public void addSprite(TextureAtlasSprite var1) {
      Stitcher.Holder var2 = new Stitcher.Holder(var1, this.mipmapLevelStitcher);
      if (this.maxTileDimension > 0) {
         var2.setNewDimension(this.maxTileDimension);
      }

      this.setStitchHolders.add(var2);
   }

   public void doStitch() {
      Stitcher.Holder[] var1 = (Stitcher.Holder[])this.setStitchHolders.toArray(new Stitcher.Holder[this.setStitchHolders.size()]);
      Arrays.sort(var1);
      ProgressBar var2 = ProgressManager.push("Texture stitching", var1.length);

      for(Stitcher.Holder var6 : var1) {
         var2.step(var6.getAtlasSprite().getIconName());
         if (!this.allocateSlot(var6)) {
            String var7 = String.format("Unable to fit: %s - size: %dx%d - Maybe try a lowerresolution resourcepack?", var6.getAtlasSprite().getIconName(), var6.getAtlasSprite().getIconWidth(), var6.getAtlasSprite().getIconHeight());
            FMLLog.info(var7, new Object[0]);

            for(Stitcher.Holder var11 : var1) {
               FMLLog.info("  %s", new Object[]{var11});
            }

            throw new StitcherException(var6, var7);
         }
      }

      this.currentWidth = MathHelper.smallestEncompassingPowerOfTwo(this.currentWidth);
      this.currentHeight = MathHelper.smallestEncompassingPowerOfTwo(this.currentHeight);
      ProgressManager.pop(var2);
   }

   public List getStichSlots() {
      ArrayList var1 = Lists.newArrayList();

      for(Stitcher.Slot var3 : this.stitchSlots) {
         var3.getAllStitchSlots(var1);
      }

      ArrayList var7 = Lists.newArrayList();

      for(Stitcher.Slot var4 : var1) {
         Stitcher.Holder var5 = var4.getStitchHolder();
         TextureAtlasSprite var6 = var5.getAtlasSprite();
         var6.initSprite(this.currentWidth, this.currentHeight, var4.getOriginX(), var4.getOriginY(), var5.isRotated());
         var7.add(var6);
      }

      return var7;
   }

   private static int getMipmapDimension(int var0, int var1) {
      return (var0 >> var1) + ((var0 & (1 << var1) - 1) == 0 ? 0 : 1) << var1;
   }

   private boolean allocateSlot(Stitcher.Holder var1) {
      TextureAtlasSprite var2 = var1.getAtlasSprite();
      boolean var3 = var2.getIconWidth() != var2.getIconHeight();

      for(int var4 = 0; var4 < this.stitchSlots.size(); ++var4) {
         if (((Stitcher.Slot)this.stitchSlots.get(var4)).addSlot(var1)) {
            return true;
         }

         if (var3) {
            var1.rotate();
            if (((Stitcher.Slot)this.stitchSlots.get(var4)).addSlot(var1)) {
               return true;
            }

            var1.rotate();
         }
      }

      return this.expandAndAllocateSlot(var1);
   }

   private boolean expandAndAllocateSlot(Stitcher.Holder var1) {
      int var2 = Math.min(var1.getWidth(), var1.getHeight());
      int var3 = Math.max(var1.getWidth(), var1.getHeight());
      int var4 = MathHelper.smallestEncompassingPowerOfTwo(this.currentWidth);
      int var5 = MathHelper.smallestEncompassingPowerOfTwo(this.currentHeight);
      int var6 = MathHelper.smallestEncompassingPowerOfTwo(this.currentWidth + var2);
      int var7 = MathHelper.smallestEncompassingPowerOfTwo(this.currentHeight + var2);
      boolean var8 = var6 <= this.maxWidth;
      boolean var9 = var7 <= this.maxHeight;
      if (!var8 && !var9) {
         return false;
      } else {
         boolean var10 = var8 && var4 != var6;
         boolean var11 = var9 && var5 != var7;
         boolean var12;
         if (var10 ^ var11) {
            var12 = !var10 && var8;
         } else {
            var12 = var8 && var4 <= var5;
         }

         Stitcher.Slot var13;
         if (var12) {
            if (var1.getWidth() > var1.getHeight()) {
               var1.rotate();
            }

            if (this.currentHeight == 0) {
               this.currentHeight = var1.getHeight();
            }

            var13 = new Stitcher.Slot(this.currentWidth, 0, var1.getWidth(), this.currentHeight);
            this.currentWidth += var1.getWidth();
         } else {
            var13 = new Stitcher.Slot(0, this.currentHeight, this.currentWidth, var1.getHeight());
            this.currentHeight += var1.getHeight();
         }

         var13.addSlot(var1);
         this.stitchSlots.add(var13);
         return true;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class Holder implements Comparable {
      private final TextureAtlasSprite theTexture;
      private final int width;
      private final int height;
      private final int mipmapLevelHolder;
      private boolean rotated;
      private float scaleFactor = 1.0F;

      public Holder(TextureAtlasSprite var1, int var2) {
         this.theTexture = var1;
         this.width = var1.getIconWidth();
         this.height = var1.getIconHeight();
         this.mipmapLevelHolder = var2;
         this.rotated = Stitcher.getMipmapDimension(this.height, var2) > Stitcher.getMipmapDimension(this.width, var2);
      }

      public TextureAtlasSprite getAtlasSprite() {
         return this.theTexture;
      }

      public int getWidth() {
         int var1 = this.rotated ? this.height : this.width;
         return Stitcher.getMipmapDimension((int)((float)var1 * this.scaleFactor), this.mipmapLevelHolder);
      }

      public int getHeight() {
         int var1 = this.rotated ? this.width : this.height;
         return Stitcher.getMipmapDimension((int)((float)var1 * this.scaleFactor), this.mipmapLevelHolder);
      }

      public void rotate() {
         this.rotated = !this.rotated;
      }

      public boolean isRotated() {
         return this.rotated;
      }

      public void setNewDimension(int var1) {
         if (this.width > var1 && this.height > var1) {
            this.scaleFactor = (float)var1 / (float)Math.min(this.width, this.height);
         }

      }

      public String toString() {
         return "Holder{width=" + this.width + ", height=" + this.height + ", name=" + this.theTexture.getIconName() + '}';
      }

      public int compareTo(Stitcher.Holder var1) {
         int var2;
         if (this.getHeight() == var1.getHeight()) {
            if (this.getWidth() == var1.getWidth()) {
               if (this.theTexture.getIconName() == null) {
                  return var1.theTexture.getIconName() == null ? 0 : -1;
               }

               return this.theTexture.getIconName().compareTo(var1.theTexture.getIconName());
            }

            var2 = this.getWidth() < var1.getWidth() ? 1 : -1;
         } else {
            var2 = this.getHeight() < var1.getHeight() ? 1 : -1;
         }

         return var2;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class Slot {
      private final int originX;
      private final int originY;
      private final int width;
      private final int height;
      private List subSlots;
      private Stitcher.Holder holder;

      public Slot(int var1, int var2, int var3, int var4) {
         this.originX = var1;
         this.originY = var2;
         this.width = var3;
         this.height = var4;
      }

      public Stitcher.Holder getStitchHolder() {
         return this.holder;
      }

      public int getOriginX() {
         return this.originX;
      }

      public int getOriginY() {
         return this.originY;
      }

      public boolean addSlot(Stitcher.Holder var1) {
         if (this.holder != null) {
            return false;
         } else {
            int var2 = var1.getWidth();
            int var3 = var1.getHeight();
            if (var2 <= this.width && var3 <= this.height) {
               if (var2 == this.width && var3 == this.height) {
                  this.holder = var1;
                  return true;
               } else {
                  if (this.subSlots == null) {
                     this.subSlots = Lists.newArrayListWithCapacity(1);
                     this.subSlots.add(new Stitcher.Slot(this.originX, this.originY, var2, var3));
                     int var4 = this.width - var2;
                     int var5 = this.height - var3;
                     if (var5 > 0 && var4 > 0) {
                        int var6 = Math.max(this.height, var4);
                        int var7 = Math.max(this.width, var5);
                        if (var6 >= var7) {
                           this.subSlots.add(new Stitcher.Slot(this.originX, this.originY + var3, var2, var5));
                           this.subSlots.add(new Stitcher.Slot(this.originX + var2, this.originY, var4, this.height));
                        } else {
                           this.subSlots.add(new Stitcher.Slot(this.originX + var2, this.originY, var4, var3));
                           this.subSlots.add(new Stitcher.Slot(this.originX, this.originY + var3, this.width, var5));
                        }
                     } else if (var4 == 0) {
                        this.subSlots.add(new Stitcher.Slot(this.originX, this.originY + var3, var2, var5));
                     } else if (var5 == 0) {
                        this.subSlots.add(new Stitcher.Slot(this.originX + var2, this.originY, var4, var3));
                     }
                  }

                  for(Stitcher.Slot var9 : this.subSlots) {
                     if (var9.addSlot(var1)) {
                        return true;
                     }
                  }

                  return false;
               }
            } else {
               return false;
            }
         }
      }

      public void getAllStitchSlots(List var1) {
         if (this.holder != null) {
            var1.add(this);
         } else if (this.subSlots != null) {
            for(Stitcher.Slot var3 : this.subSlots) {
               var3.getAllStitchSlots(var1);
            }
         }

      }

      public String toString() {
         return "Slot{originX=" + this.originX + ", originY=" + this.originY + ", width=" + this.width + ", height=" + this.height + ", texture=" + this.holder + ", subSlots=" + this.subSlots + '}';
      }
   }
}
