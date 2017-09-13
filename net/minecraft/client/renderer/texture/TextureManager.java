package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class TextureManager implements ITickable, IResourceManagerReloadListener {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Map mapTextureObjects = Maps.newHashMap();
   private final List listTickables = Lists.newArrayList();
   private final Map mapTextureCounters = Maps.newHashMap();
   private final IResourceManager theResourceManager;

   public TextureManager(IResourceManager resourceManager) {
      this.theResourceManager = resourceManager;
   }

   public void bindTexture(ResourceLocation resource) {
      ITextureObject itextureobject = (ITextureObject)this.mapTextureObjects.get(resource);
      if (itextureobject == null) {
         itextureobject = new SimpleTexture(resource);
         this.loadTexture(resource, itextureobject);
      }

      TextureUtil.bindTexture(itextureobject.getGlTextureId());
   }

   public boolean loadTickableTexture(ResourceLocation textureLocation, ITickableTextureObject textureObj) {
      if (this.loadTexture(textureLocation, textureObj)) {
         this.listTickables.add(textureObj);
         return true;
      } else {
         return false;
      }
   }

   public boolean loadTexture(ResourceLocation textureLocation, final ITextureObject textureObj) {
      boolean flag = true;

      try {
         textureObj.loadTexture(this.theResourceManager);
      } catch (IOException var8) {
         LOGGER.warn("Failed to load texture: {}", new Object[]{textureLocation, var8});
         textureObj = TextureUtil.MISSING_TEXTURE;
         this.mapTextureObjects.put(textureLocation, textureObj);
         flag = false;
      } catch (Throwable var9) {
         CrashReport crashreport = CrashReport.makeCrashReport(var9, "Registering texture");
         CrashReportCategory crashreportcategory = crashreport.makeCategory("Resource location being registered");
         crashreportcategory.addCrashSection("Resource location", textureLocation);
         crashreportcategory.setDetail("Texture object class", new ICrashReportDetail() {
            public String call() throws Exception {
               return textureObj.getClass().getName();
            }
         });
         throw new ReportedException(crashreport);
      }

      this.mapTextureObjects.put(textureLocation, textureObj);
      return flag;
   }

   public ITextureObject getTexture(ResourceLocation textureLocation) {
      return (ITextureObject)this.mapTextureObjects.get(textureLocation);
   }

   public ResourceLocation getDynamicTextureLocation(String name, DynamicTexture texture) {
      Integer integer = (Integer)this.mapTextureCounters.get(name);
      if (integer == null) {
         integer = Integer.valueOf(1);
      } else {
         integer = integer.intValue() + 1;
      }

      this.mapTextureCounters.put(name, integer);
      ResourceLocation resourcelocation = new ResourceLocation(String.format("dynamic/%s_%d", name, integer));
      this.loadTexture(resourcelocation, texture);
      return resourcelocation;
   }

   public void tick() {
      for(ITickable itickable : this.listTickables) {
         itickable.tick();
      }

   }

   public void deleteTexture(ResourceLocation textureLocation) {
      ITextureObject itextureobject = this.getTexture(textureLocation);
      if (itextureobject != null) {
         TextureUtil.deleteTexture(itextureobject.getGlTextureId());
      }

   }

   public void onResourceManagerReload(IResourceManager resourceManager) {
      ProgressBar bar = ProgressManager.push("Reloading Texture Manager", this.mapTextureObjects.keySet().size(), true);

      for(Entry entry : this.mapTextureObjects.entrySet()) {
         bar.step(((ResourceLocation)entry.getKey()).toString());
         this.loadTexture((ResourceLocation)entry.getKey(), (ITextureObject)entry.getValue());
      }

      ProgressManager.pop(bar);
   }
}
