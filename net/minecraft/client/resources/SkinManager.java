package net.minecraft.client.resources;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecureTextureException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SkinManager {
   private static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(0, 2, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue());
   private final TextureManager textureManager;
   private final File skinCacheDir;
   private final MinecraftSessionService sessionService;
   private final LoadingCache skinCacheLoader;

   public SkinManager(TextureManager var1, File var2, MinecraftSessionService var3) {
      this.textureManager = textureManagerInstance;
      this.skinCacheDir = skinCacheDirectory;
      this.sessionService = sessionService;
      this.skinCacheLoader = CacheBuilder.newBuilder().expireAfterAccess(15L, TimeUnit.SECONDS).build(new CacheLoader() {
         public Map load(GameProfile var1) throws Exception {
            return Minecraft.getMinecraft().getSessionService().getTextures(p_load_1_, false);
         }
      });
   }

   public ResourceLocation loadSkin(MinecraftProfileTexture var1, Type var2) {
      return this.loadSkin(profileTexture, textureType, (SkinManager.SkinAvailableCallback)null);
   }

   public ResourceLocation loadSkin(final MinecraftProfileTexture var1, final Type var2, @Nullable final SkinManager.SkinAvailableCallback var3) {
      final ResourceLocation resourcelocation = new ResourceLocation("skins/" + profileTexture.getHash());
      ITextureObject itextureobject = this.textureManager.getTexture(resourcelocation);
      if (itextureobject != null) {
         if (skinAvailableCallback != null) {
            skinAvailableCallback.skinAvailable(textureType, resourcelocation, profileTexture);
         }
      } else {
         File file1 = new File(this.skinCacheDir, profileTexture.getHash().length() > 2 ? profileTexture.getHash().substring(0, 2) : "xx");
         File file2 = new File(file1, profileTexture.getHash());
         final IImageBuffer iimagebuffer = textureType == Type.SKIN ? new ImageBufferDownload() : null;
         ThreadDownloadImageData threaddownloadimagedata = new ThreadDownloadImageData(file2, profileTexture.getUrl(), DefaultPlayerSkin.getDefaultSkinLegacy(), new IImageBuffer() {
            public BufferedImage parseUserSkin(BufferedImage var1x) {
               if (iimagebuffer != null) {
                  image = iimagebuffer.parseUserSkin(image);
               }

               return image;
            }

            public void skinAvailable() {
               if (iimagebuffer != null) {
                  iimagebuffer.skinAvailable();
               }

               if (skinAvailableCallback != null) {
                  skinAvailableCallback.skinAvailable(textureType, resourcelocation, profileTexture);
               }

            }
         });
         this.textureManager.loadTexture(resourcelocation, threaddownloadimagedata);
      }

      return resourcelocation;
   }

   public void loadProfileTextures(final GameProfile var1, final SkinManager.SkinAvailableCallback var2, final boolean var3) {
      THREAD_POOL.submit(new Runnable() {
         public void run() {
            final Map map = Maps.newHashMap();

            try {
               map.putAll(SkinManager.this.sessionService.getTextures(profile, requireSecure));
            } catch (InsecureTextureException var3x) {
               ;
            }

            if (map.isEmpty() && profile.getId().equals(Minecraft.getMinecraft().getSession().getProfile().getId())) {
               profile.getProperties().clear();
               profile.getProperties().putAll(Minecraft.getMinecraft().getProfileProperties());
               map.putAll(SkinManager.this.sessionService.getTextures(profile, false));
            }

            Minecraft.getMinecraft().addScheduledTask(new Runnable() {
               public void run() {
                  if (map.containsKey(Type.SKIN)) {
                     SkinManager.this.loadSkin((MinecraftProfileTexture)map.get(Type.SKIN), Type.SKIN, skinAvailableCallback);
                  }

                  if (map.containsKey(Type.CAPE)) {
                     SkinManager.this.loadSkin((MinecraftProfileTexture)map.get(Type.CAPE), Type.CAPE, skinAvailableCallback);
                  }

               }
            });
         }
      });
   }

   public Map loadSkinFromCache(GameProfile var1) {
      return (Map)this.skinCacheLoader.getUnchecked(profile);
   }

   @SideOnly(Side.CLIENT)
   public interface SkinAvailableCallback {
      void skinAvailable(Type var1, ResourceLocation var2, MinecraftProfileTexture var3);
   }
}
