package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
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

   public TextureMap(String basePathIn) {
      this(basePathIn, (ITextureMapPopulator)null);
   }

   public TextureMap(String basePathIn, @Nullable ITextureMapPopulator iconCreatorIn) {
      this(basePathIn, iconCreatorIn, false);
   }

   public TextureMap(String basePathIn, boolean skipFirst) {
      this(basePathIn, (ITextureMapPopulator)null, skipFirst);
   }

   public TextureMap(String basePathIn, ITextureMapPopulator iconCreatorIn, boolean skipFirst) {
      this.skipFirst = false;
      this.listAnimatedSprites = Lists.newArrayList();
      this.mapRegisteredSprites = Maps.newHashMap();
      this.mapUploadedSprites = Maps.newHashMap();
      this.missingImage = new TextureAtlasSprite("missingno");
      this.basePath = basePathIn;
      this.iconCreator = iconCreatorIn;
      this.skipFirst = skipFirst && ENABLE_SKIP;
   }

   private void initMissingImage() {
      int[] aint = TextureUtil.MISSING_TEXTURE_DATA;
      this.missingImage.setIconWidth(16);
      this.missingImage.setIconHeight(16);
      int[][] aint1 = new int[this.mipmapLevels + 1][];
      aint1[0] = aint;
      this.missingImage.setFramesTextureData(Lists.newArrayList(new int[][][]{aint1}));
   }

   public void loadTexture(IResourceManager resourceManager) throws IOException {
      if (this.iconCreator != null) {
         this.loadSprites(resourceManager, this.iconCreator);
      }

   }

   public void loadSprites(IResourceManager resourceManager, ITextureMapPopulator iconCreatorIn) {
      this.mapRegisteredSprites.clear();
      iconCreatorIn.registerSprites(this);
      this.initMissingImage();
      this.deleteGlTexture();
      this.loadTextureAtlas(resourceManager);
   }

   public void loadTextureAtlas(IResourceManager resourceManager) {
      int i = Minecraft.getGLMaximumTextureSize();
      Stitcher stitcher = new Stitcher(i, i, 0, this.mipmapLevels);
      this.mapUploadedSprites.clear();
      this.listAnimatedSprites.clear();
      int j = Integer.MAX_VALUE;
      int k = 1 << this.mipmapLevels;
      ForgeHooksClient.onTextureStitchedPre(this);
      FMLLog.info("Max texture size: %d", new Object[]{i});
      ProgressBar bar = ProgressManager.push("Texture stitching", this.skipFirst ? 0 : this.mapRegisteredSprites.size());
      if (!this.skipFirst) {
         Iterator l = this.mapRegisteredSprites.entrySet().iterator();

         label186:
         while(true) {
            TextureAtlasSprite textureatlassprite;
            ResourceLocation resourcelocation;
            while(true) {
               if (!l.hasNext()) {
                  break label186;
               }

               Entry entry = (Entry)l.next();
               textureatlassprite = (TextureAtlasSprite)entry.getValue();
               resourcelocation = this.getResourceLocation(textureatlassprite);
               bar.step(resourcelocation.getResourcePath());
               IResource iresource = null;
               if (textureatlassprite.hasCustomLoader(resourceManager, resourcelocation)) {
                  if (textureatlassprite.load(resourceManager, resourcelocation)) {
                     continue;
                  }
                  break;
               } else {
                  try {
                     PngSizeInfo pngsizeinfo = PngSizeInfo.makeFromResource(resourceManager.getResource(resourcelocation));
                     iresource = resourceManager.getResource(resourcelocation);
                     boolean flag = iresource.getMetadata("animation") != null;
                     textureatlassprite.loadSprite(pngsizeinfo, flag);
                     break;
                  } catch (RuntimeException var23) {
                     FMLClientHandler.instance().trackBrokenTexture(resourcelocation, var23.getMessage());
                  } catch (IOException var24) {
                     FMLClientHandler.instance().trackMissingTexture(resourcelocation);
                  } finally {
                     IOUtils.closeQuietly(iresource);
                  }
               }
            }

            j = Math.min(j, Math.min(textureatlassprite.getIconWidth(), textureatlassprite.getIconHeight()));
            int lvt_11_2_ = Math.min(Integer.lowestOneBit(textureatlassprite.getIconWidth()), Integer.lowestOneBit(textureatlassprite.getIconHeight()));
            if (lvt_11_2_ < k) {
               LOGGER.warn("Texture {} with size {}x{} will have visual artifacts at mip level {}, it can only support level {}. Please report to the mod author that the texture should be some multiple of 16x16.", new Object[]{resourcelocation, textureatlassprite.getIconWidth(), textureatlassprite.getIconHeight(), MathHelper.log2(k), MathHelper.log2(lvt_11_2_)});
            }

            stitcher.addSprite(textureatlassprite);
         }
      }

      ProgressManager.pop(bar);
      int l = Math.min(j, k);
      int i1 = MathHelper.log2(l);
      this.missingImage.generateMipmaps(this.mipmapLevels);
      stitcher.addSprite(this.missingImage);
      this.skipFirst = false;
      bar = ProgressManager.push("Texture creation", 2);

      try {
         bar.step("Stitching");
         stitcher.doStitch();
      } catch (StitcherException var22) {
         throw var22;
      }

      LOGGER.info("Created: {}x{} {}-atlas", new Object[]{stitcher.getCurrentWidth(), stitcher.getCurrentHeight(), this.basePath});
      bar.step("Allocating GL texture");
      TextureUtil.allocateTextureImpl(this.getGlTextureId(), this.mipmapLevels, stitcher.getCurrentWidth(), stitcher.getCurrentHeight());
      Map map = Maps.newHashMap(this.mapRegisteredSprites);
      ProgressManager.pop(bar);
      bar = ProgressManager.push("Texture mipmap and upload", stitcher.getStichSlots().size());

      for(TextureAtlasSprite textureatlassprite1 : stitcher.getStichSlots()) {
         bar.step(textureatlassprite1.getIconName());
         if (textureatlassprite1 == this.missingImage || this.generateMipmaps(resourceManager, textureatlassprite1)) {
            String s = textureatlassprite1.getIconName();
            map.remove(s);
            this.mapUploadedSprites.put(s, textureatlassprite1);

            try {
               TextureUtil.uploadTextureMipmap(textureatlassprite1.getFrameTextureData(0), textureatlassprite1.getIconWidth(), textureatlassprite1.getIconHeight(), textureatlassprite1.getOriginX(), textureatlassprite1.getOriginY(), false, false);
            } catch (Throwable var21) {
               CrashReport crashreport = CrashReport.makeCrashReport(var21, "Stitching texture atlas");
               CrashReportCategory crashreportcategory = crashreport.makeCategory("Texture being stitched together");
               crashreportcategory.addCrashSection("Atlas path", this.basePath);
               crashreportcategory.addCrashSection("Sprite", textureatlassprite1);
               throw new ReportedException(crashreport);
            }

            if (textureatlassprite1.hasAnimationMetadata()) {
               this.listAnimatedSprites.add(textureatlassprite1);
            }
         }
      }

      for(TextureAtlasSprite textureatlassprite2 : map.values()) {
         textureatlassprite2.copyFrom(this.missingImage);
      }

      ForgeHooksClient.onTextureStitchedPost(this);
      ProgressManager.pop(bar);
   }

   private boolean generateMipmaps(IResourceManager resourceManager, final TextureAtlasSprite texture) {
      ResourceLocation resourcelocation = this.getResourceLocation(texture);
      IResource iresource = null;
      if (!texture.hasCustomLoader(resourceManager, resourcelocation)) {
         label60: {
            boolean flag;
            try {
               iresource = resourceManager.getResource(resourcelocation);
               texture.loadSpriteFrames(iresource, this.mipmapLevels + 1);
               break label60;
            } catch (RuntimeException var14) {
               LOGGER.error("Unable to parse metadata from {}", new Object[]{resourcelocation, var14});
               flag = false;
            } catch (IOException var15) {
               LOGGER.error("Using missing texture, unable to load {}", new Object[]{resourcelocation, var15});
               flag = false;
               boolean crashreportcategory = flag;
               return crashreportcategory;
            } finally {
               IOUtils.closeQuietly(iresource);
            }

            return flag;
         }
      }

      try {
         texture.generateMipmaps(this.mipmapLevels);
         return true;
      } catch (Throwable var13) {
         CrashReport crashreport = CrashReport.makeCrashReport(var13, "Applying mipmap");
         CrashReportCategory crashreportcategory = crashreport.makeCategory("Sprite being mipmapped");
         crashreportcategory.setDetail("Sprite name", new ICrashReportDetail() {
            public String call() throws Exception {
               return texture.getIconName();
            }
         });
         crashreportcategory.setDetail("Sprite size", new ICrashReportDetail() {
            public String call() throws Exception {
               return texture.getIconWidth() + " x " + texture.getIconHeight();
            }
         });
         crashreportcategory.setDetail("Sprite frames", new ICrashReportDetail() {
            public String call() throws Exception {
               return texture.getFrameCount() + " frames";
            }
         });
         crashreportcategory.addCrashSection("Mipmap levels", Integer.valueOf(this.mipmapLevels));
         throw new ReportedException(crashreport);
      }
   }

   private ResourceLocation getResourceLocation(TextureAtlasSprite p_184396_1_) {
      ResourceLocation resourcelocation = new ResourceLocation(p_184396_1_.getIconName());
      return new ResourceLocation(resourcelocation.getResourceDomain(), String.format("%s/%s%s", this.basePath, resourcelocation.getResourcePath(), ".png"));
   }

   public TextureAtlasSprite getAtlasSprite(String iconName) {
      TextureAtlasSprite textureatlassprite = (TextureAtlasSprite)this.mapUploadedSprites.get(iconName);
      if (textureatlassprite == null) {
         textureatlassprite = this.missingImage;
      }

      return textureatlassprite;
   }

   public void updateAnimations() {
      TextureUtil.bindTexture(this.getGlTextureId());

      for(TextureAtlasSprite textureatlassprite : this.listAnimatedSprites) {
         textureatlassprite.updateAnimation();
      }

   }

   public TextureAtlasSprite registerSprite(ResourceLocation location) {
      if (location == null) {
         throw new IllegalArgumentException("Location cannot be null!");
      } else {
         TextureAtlasSprite textureatlassprite = (TextureAtlasSprite)this.mapRegisteredSprites.get(location.toString());
         if (textureatlassprite == null) {
            textureatlassprite = TextureAtlasSprite.makeAtlasSprite(location);
            this.mapRegisteredSprites.put(location.toString(), textureatlassprite);
         }

         return textureatlassprite;
      }
   }

   public void tick() {
      this.updateAnimations();
   }

   public void setMipmapLevels(int mipmapLevelsIn) {
      this.mipmapLevels = mipmapLevelsIn;
   }

   public TextureAtlasSprite getMissingSprite() {
      return this.missingImage;
   }

   public TextureAtlasSprite getTextureExtry(String name) {
      return (TextureAtlasSprite)this.mapRegisteredSprites.get(name);
   }

   /** @deprecated */
   @Deprecated
   public boolean setTextureEntry(String name, TextureAtlasSprite entry) {
      if (!this.mapRegisteredSprites.containsKey(name)) {
         this.mapRegisteredSprites.put(name, entry);
         return true;
      } else {
         return false;
      }
   }

   public boolean setTextureEntry(TextureAtlasSprite entry) {
      return this.setTextureEntry(entry.getIconName(), entry);
   }

   public String getBasePath() {
      return this.basePath;
   }

   public int getMipmapLevels() {
      return this.mipmapLevels;
   }
}
