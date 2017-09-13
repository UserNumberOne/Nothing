package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.StitcherException;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class TextureMap extends AbstractTexture implements ITickableTextureObject {
   private static final boolean ENABLE_SKIP = Boolean.parseBoolean(System.getProperty("fml.skipFirstTextureLoad", "true"));
   private static final Logger LOGGER = LogManager.getLogger();
   public static final ResourceLocation LOCATION_MISSING_TEXTURE = new ResourceLocation("missingno");
   public static final ResourceLocation LOCATION_BLOCKS_TEXTURE = new ResourceLocation("textures/atlas/blocks.png");
   private final List listAnimatedSprites;
   private final Map mapRegisteredSprites;
   private final Map mapUploadedSprites;
   private final String basePath;
   private final ITextureMapPopulator iconCreator;
   private int mipmapLevels;
   private final TextureAtlasSprite missingImage;
   private boolean skipFirst;

   public TextureMap(String var1) {
      this(var1, (ITextureMapPopulator)null);
   }

   public TextureMap(String var1, @Nullable ITextureMapPopulator var2) {
      this(var1, var2, false);
   }

   public TextureMap(String var1, boolean var2) {
      this(var1, (ITextureMapPopulator)null, var2);
   }

   public TextureMap(String var1, ITextureMapPopulator var2, boolean var3) {
      this.skipFirst = false;
      this.listAnimatedSprites = Lists.newArrayList();
      this.mapRegisteredSprites = Maps.newHashMap();
      this.mapUploadedSprites = Maps.newHashMap();
      this.missingImage = new TextureAtlasSprite("missingno");
      this.basePath = var1;
      this.iconCreator = var2;
      this.skipFirst = var3 && ENABLE_SKIP;
   }

   private void initMissingImage() {
      int[] var1 = TextureUtil.MISSING_TEXTURE_DATA;
      this.missingImage.setIconWidth(16);
      this.missingImage.setIconHeight(16);
      int[][] var2 = new int[this.mipmapLevels + 1][];
      var2[0] = var1;
      this.missingImage.setFramesTextureData(Lists.newArrayList(new int[][][]{var2}));
   }

   public void loadTexture(IResourceManager var1) throws IOException {
      if (this.iconCreator != null) {
         this.loadSprites(var1, this.iconCreator);
      }

   }

   public void loadSprites(IResourceManager var1, ITextureMapPopulator var2) {
      this.mapRegisteredSprites.clear();
      var2.registerSprites(this);
      this.initMissingImage();
      this.deleteGlTexture();
      this.loadTextureAtlas(var1);
   }

   public void loadTextureAtlas(IResourceManager var1) {
      int var2 = Minecraft.getGLMaximumTextureSize();
      Stitcher var3 = new Stitcher(var2, var2, 0, this.mipmapLevels);
      this.mapUploadedSprites.clear();
      this.listAnimatedSprites.clear();
      int var4 = Integer.MAX_VALUE;
      int var5 = 1 << this.mipmapLevels;
      ForgeHooksClient.onTextureStitchedPre(this);
      FMLLog.info("Max texture size: %d", new Object[]{var2});
      ProgressBar var6 = ProgressManager.push("Texture stitching", this.skipFirst ? 0 : this.mapRegisteredSprites.size());
      if (!this.skipFirst) {
         Iterator var7 = this.mapRegisteredSprites.entrySet().iterator();

         label186:
         while(true) {
            TextureAtlasSprite var9;
            ResourceLocation var10;
            while(true) {
               if (!var7.hasNext()) {
                  break label186;
               }

               Entry var8 = (Entry)var7.next();
               var9 = (TextureAtlasSprite)var8.getValue();
               var10 = this.getResourceLocation(var9);
               var6.step(var10.getResourcePath());
               IResource var11 = null;
               if (var9.hasCustomLoader(var1, var10)) {
                  if (var9.load(var1, var10)) {
                     continue;
                  }
                  break;
               } else {
                  try {
                     PngSizeInfo var12 = PngSizeInfo.makeFromResource(var1.getResource(var10));
                     var11 = var1.getResource(var10);
                     boolean var13 = var11.getMetadata("animation") != null;
                     var9.loadSprite(var12, var13);
                     break;
                  } catch (RuntimeException var23) {
                     FMLClientHandler.instance().trackBrokenTexture(var10, var23.getMessage());
                  } catch (IOException var24) {
                     FMLClientHandler.instance().trackMissingTexture(var10);
                  } finally {
                     IOUtils.closeQuietly(var11);
                  }
               }
            }

            var4 = Math.min(var4, Math.min(var9.getIconWidth(), var9.getIconHeight()));
            int var35 = Math.min(Integer.lowestOneBit(var9.getIconWidth()), Integer.lowestOneBit(var9.getIconHeight()));
            if (var35 < var5) {
               LOGGER.warn("Texture {} with size {}x{} will have visual artifacts at mip level {}, it can only support level {}. Please report to the mod author that the texture should be some multiple of 16x16.", new Object[]{var10, var9.getIconWidth(), var9.getIconHeight(), MathHelper.log2(var5), MathHelper.log2(var35)});
            }

            var3.addSprite(var9);
         }
      }

      ProgressManager.pop(var6);
      int var28 = Math.min(var4, var5);
      int var29 = MathHelper.log2(var28);
      this.missingImage.generateMipmaps(this.mipmapLevels);
      var3.addSprite(this.missingImage);
      this.skipFirst = false;
      var6 = ProgressManager.push("Texture creation", 2);

      try {
         var6.step("Stitching");
         var3.doStitch();
      } catch (StitcherException var22) {
         throw var22;
      }

      LOGGER.info("Created: {}x{} {}-atlas", new Object[]{var3.getCurrentWidth(), var3.getCurrentHeight(), this.basePath});
      var6.step("Allocating GL texture");
      TextureUtil.allocateTextureImpl(this.getGlTextureId(), this.mipmapLevels, var3.getCurrentWidth(), var3.getCurrentHeight());
      HashMap var30 = Maps.newHashMap(this.mapRegisteredSprites);
      ProgressManager.pop(var6);
      var6 = ProgressManager.push("Texture mipmap and upload", var3.getStichSlots().size());

      for(TextureAtlasSprite var33 : var3.getStichSlots()) {
         var6.step(var33.getIconName());
         if (var33 == this.missingImage || this.generateMipmaps(var1, var33)) {
            String var36 = var33.getIconName();
            var30.remove(var36);
            this.mapUploadedSprites.put(var36, var33);

            try {
               TextureUtil.uploadTextureMipmap(var33.getFrameTextureData(0), var33.getIconWidth(), var33.getIconHeight(), var33.getOriginX(), var33.getOriginY(), false, false);
            } catch (Throwable var21) {
               CrashReport var14 = CrashReport.makeCrashReport(var21, "Stitching texture atlas");
               CrashReportCategory var15 = var14.makeCategory("Texture being stitched together");
               var15.addCrashSection("Atlas path", this.basePath);
               var15.addCrashSection("Sprite", var33);
               throw new ReportedException(var14);
            }

            if (var33.hasAnimationMetadata()) {
               this.listAnimatedSprites.add(var33);
            }
         }
      }

      for(TextureAtlasSprite var34 : var30.values()) {
         var34.copyFrom(this.missingImage);
      }

      ForgeHooksClient.onTextureStitchedPost(this);
      ProgressManager.pop(var6);
   }

   private boolean generateMipmaps(IResourceManager var1, final TextureAtlasSprite var2) {
      ResourceLocation var3 = this.getResourceLocation(var2);
      IResource var4 = null;
      if (!var2.hasCustomLoader(var1, var3)) {
         label60: {
            boolean var17;
            try {
               var4 = var1.getResource(var3);
               var2.loadSpriteFrames(var4, this.mipmapLevels + 1);
               break label60;
            } catch (RuntimeException var14) {
               LOGGER.error("Unable to parse metadata from {}", new Object[]{var3, var14});
               var17 = false;
            } catch (IOException var15) {
               LOGGER.error("Using missing texture, unable to load {}", new Object[]{var3, var15});
               var17 = false;
               boolean var7 = var17;
               return var7;
            } finally {
               IOUtils.closeQuietly(var4);
            }

            return var17;
         }
      }

      try {
         var2.generateMipmaps(this.mipmapLevels);
         return true;
      } catch (Throwable var13) {
         CrashReport var6 = CrashReport.makeCrashReport(var13, "Applying mipmap");
         CrashReportCategory var18 = var6.makeCategory("Sprite being mipmapped");
         var18.setDetail("Sprite name", new ICrashReportDetail() {
            public String call() throws Exception {
               return var2.getIconName();
            }
         });
         var18.setDetail("Sprite size", new ICrashReportDetail() {
            public String call() throws Exception {
               return var2.getIconWidth() + " x " + var2.getIconHeight();
            }
         });
         var18.setDetail("Sprite frames", new ICrashReportDetail() {
            public String call() throws Exception {
               return var2.getFrameCount() + " frames";
            }
         });
         var18.addCrashSection("Mipmap levels", Integer.valueOf(this.mipmapLevels));
         throw new ReportedException(var6);
      }
   }

   private ResourceLocation getResourceLocation(TextureAtlasSprite var1) {
      ResourceLocation var2 = new ResourceLocation(var1.getIconName());
      return new ResourceLocation(var2.getResourceDomain(), String.format("%s/%s%s", this.basePath, var2.getResourcePath(), ".png"));
   }

   public TextureAtlasSprite getAtlasSprite(String var1) {
      TextureAtlasSprite var2 = (TextureAtlasSprite)this.mapUploadedSprites.get(var1);
      if (var2 == null) {
         var2 = this.missingImage;
      }

      return var2;
   }

   public void updateAnimations() {
      TextureUtil.bindTexture(this.getGlTextureId());

      for(TextureAtlasSprite var2 : this.listAnimatedSprites) {
         var2.updateAnimation();
      }

   }

   public TextureAtlasSprite registerSprite(ResourceLocation var1) {
      if (var1 == null) {
         throw new IllegalArgumentException("Location cannot be null!");
      } else {
         TextureAtlasSprite var2 = (TextureAtlasSprite)this.mapRegisteredSprites.get(var1.toString());
         if (var2 == null) {
            var2 = TextureAtlasSprite.makeAtlasSprite(var1);
            this.mapRegisteredSprites.put(var1.toString(), var2);
         }

         return var2;
      }
   }

   public void tick() {
      this.updateAnimations();
   }

   public void setMipmapLevels(int var1) {
      this.mipmapLevels = var1;
   }

   public TextureAtlasSprite getMissingSprite() {
      return this.missingImage;
   }

   public TextureAtlasSprite getTextureExtry(String var1) {
      return (TextureAtlasSprite)this.mapRegisteredSprites.get(var1);
   }

   /** @deprecated */
   @Deprecated
   public boolean setTextureEntry(String var1, TextureAtlasSprite var2) {
      if (!this.mapRegisteredSprites.containsKey(var1)) {
         this.mapRegisteredSprites.put(var1, var2);
         return true;
      } else {
         return false;
      }
   }

   public boolean setTextureEntry(TextureAtlasSprite var1) {
      return this.setTextureEntry(var1.getIconName(), var1);
   }

   public String getBasePath() {
      return this.basePath;
   }

   public int getMipmapLevels() {
      return this.mipmapLevels;
   }
}
