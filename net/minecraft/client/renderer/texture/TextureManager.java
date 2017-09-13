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

   public TextureManager(IResourceManager var1) {
      this.theResourceManager = var1;
   }

   public void bindTexture(ResourceLocation var1) {
      Object var2 = (ITextureObject)this.mapTextureObjects.get(var1);
      if (var2 == null) {
         var2 = new SimpleTexture(var1);
         this.loadTexture(var1, (ITextureObject)var2);
      }

      TextureUtil.bindTexture(((ITextureObject)var2).getGlTextureId());
   }

   public boolean loadTickableTexture(ResourceLocation var1, ITickableTextureObject var2) {
      if (this.loadTexture(var1, var2)) {
         this.listTickables.add(var2);
         return true;
      } else {
         return false;
      }
   }

   public boolean loadTexture(ResourceLocation var1, final ITextureObject var2) {
      boolean var3 = true;

      try {
         ((ITextureObject)var2).loadTexture(this.theResourceManager);
      } catch (IOException var8) {
         LOGGER.warn("Failed to load texture: {}", new Object[]{var1, var8});
         var2 = TextureUtil.MISSING_TEXTURE;
         this.mapTextureObjects.put(var1, var2);
         var3 = false;
      } catch (Throwable var9) {
         CrashReport var6 = CrashReport.makeCrashReport(var9, "Registering texture");
         CrashReportCategory var7 = var6.makeCategory("Resource location being registered");
         var7.addCrashSection("Resource location", var1);
         var7.setDetail("Texture object class", new ICrashReportDetail() {
            public String call() throws Exception {
               return var2.getClass().getName();
            }
         });
         throw new ReportedException(var6);
      }

      this.mapTextureObjects.put(var1, var2);
      return var3;
   }

   public ITextureObject getTexture(ResourceLocation var1) {
      return (ITextureObject)this.mapTextureObjects.get(var1);
   }

   public ResourceLocation getDynamicTextureLocation(String var1, DynamicTexture var2) {
      Integer var3 = (Integer)this.mapTextureCounters.get(var1);
      if (var3 == null) {
         var3 = Integer.valueOf(1);
      } else {
         var3 = var3.intValue() + 1;
      }

      this.mapTextureCounters.put(var1, var3);
      ResourceLocation var4 = new ResourceLocation(String.format("dynamic/%s_%d", var1, var3));
      this.loadTexture(var4, var2);
      return var4;
   }

   public void tick() {
      for(ITickable var2 : this.listTickables) {
         var2.tick();
      }

   }

   public void deleteTexture(ResourceLocation var1) {
      ITextureObject var2 = this.getTexture(var1);
      if (var2 != null) {
         TextureUtil.deleteTexture(var2.getGlTextureId());
      }

   }

   public void onResourceManagerReload(IResourceManager var1) {
      ProgressBar var2 = ProgressManager.push("Reloading Texture Manager", this.mapTextureObjects.keySet().size(), true);

      for(Entry var4 : this.mapTextureObjects.entrySet()) {
         var2.step(((ResourceLocation)var4.getKey()).toString());
         this.loadTexture((ResourceLocation)var4.getKey(), (ITextureObject)var4.getValue());
      }

      ProgressManager.pop(var2);
   }
}
