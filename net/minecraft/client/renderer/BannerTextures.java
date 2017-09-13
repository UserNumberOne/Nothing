package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.LayeredColorMaskTexture;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BannerTextures {
   public static final BannerTextures.Cache BANNER_DESIGNS = new BannerTextures.Cache("B", new ResourceLocation("textures/entity/banner_base.png"), "textures/entity/banner/");
   public static final BannerTextures.Cache SHIELD_DESIGNS = new BannerTextures.Cache("S", new ResourceLocation("textures/entity/shield_base.png"), "textures/entity/shield/");
   public static final ResourceLocation SHIELD_BASE_TEXTURE = new ResourceLocation("textures/entity/shield_base_nopattern.png");
   public static final ResourceLocation BANNER_BASE_TEXTURE = new ResourceLocation("textures/entity/banner/base.png");

   @SideOnly(Side.CLIENT)
   public static class Cache {
      private final Map cacheMap = Maps.newLinkedHashMap();
      private final ResourceLocation cacheResourceLocation;
      private final String cacheResourceBase;
      private final String cacheId;

      public Cache(String var1, ResourceLocation var2, String var3) {
         this.cacheId = var1;
         this.cacheResourceLocation = var2;
         this.cacheResourceBase = var3;
      }

      @Nullable
      public ResourceLocation getResourceLocation(String var1, List var2, List var3) {
         if (var1.isEmpty()) {
            return null;
         } else {
            var1 = this.cacheId + var1;
            BannerTextures.CacheEntry var4 = (BannerTextures.CacheEntry)this.cacheMap.get(var1);
            if (var4 == null) {
               if (this.cacheMap.size() >= 256 && !this.freeCacheSlot()) {
                  return BannerTextures.BANNER_BASE_TEXTURE;
               }

               ArrayList var5 = Lists.newArrayList();

               for(TileEntityBanner.EnumBannerPattern var7 : var2) {
                  var5.add(this.cacheResourceBase + var7.getPatternName() + ".png");
               }

               var4 = new BannerTextures.CacheEntry();
               var4.textureLocation = new ResourceLocation(var1);
               Minecraft.getMinecraft().getTextureManager().loadTexture(var4.textureLocation, new LayeredColorMaskTexture(this.cacheResourceLocation, var5, var3));
               this.cacheMap.put(var1, var4);
            }

            var4.lastUseMillis = System.currentTimeMillis();
            return var4.textureLocation;
         }
      }

      private boolean freeCacheSlot() {
         long var1 = System.currentTimeMillis();
         Iterator var3 = this.cacheMap.keySet().iterator();

         while(var3.hasNext()) {
            String var4 = (String)var3.next();
            BannerTextures.CacheEntry var5 = (BannerTextures.CacheEntry)this.cacheMap.get(var4);
            if (var1 - var5.lastUseMillis > 5000L) {
               Minecraft.getMinecraft().getTextureManager().deleteTexture(var5.textureLocation);
               var3.remove();
               return true;
            }
         }

         return this.cacheMap.size() < 256;
      }
   }

   @SideOnly(Side.CLIENT)
   static class CacheEntry {
      public long lastUseMillis;
      public ResourceLocation textureLocation;

      private CacheEntry() {
      }
   }
}
