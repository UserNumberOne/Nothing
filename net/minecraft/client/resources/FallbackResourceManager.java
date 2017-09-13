package net.minecraft.client.resources;

import com.google.common.collect.Lists;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class FallbackResourceManager implements IResourceManager {
   private static final Logger LOGGER = LogManager.getLogger();
   protected final List resourcePacks = Lists.newArrayList();
   private final MetadataSerializer frmMetadataSerializer;

   public FallbackResourceManager(MetadataSerializer var1) {
      this.frmMetadataSerializer = var1;
   }

   public void addResourcePack(IResourcePack var1) {
      this.resourcePacks.add(var1);
   }

   public Set getResourceDomains() {
      return null;
   }

   public IResource getResource(ResourceLocation var1) throws IOException {
      this.checkResourcePath(var1);
      IResourcePack var2 = null;
      ResourceLocation var3 = getLocationMcmeta(var1);

      for(int var4 = this.resourcePacks.size() - 1; var4 >= 0; --var4) {
         IResourcePack var5 = (IResourcePack)this.resourcePacks.get(var4);
         if (var2 == null && var5.resourceExists(var3)) {
            var2 = var5;
         }

         if (var5.resourceExists(var1)) {
            InputStream var6 = null;
            if (var2 != null) {
               var6 = this.getInputStream(var3, var2);
            }

            return new SimpleResource(var5.getPackName(), var1, this.getInputStream(var1, var5), var6, this.frmMetadataSerializer);
         }
      }

      throw new FileNotFoundException(var1.toString());
   }

   protected InputStream getInputStream(ResourceLocation var1, IResourcePack var2) throws IOException {
      InputStream var3 = var2.getInputStream(var1);
      return (InputStream)(LOGGER.isDebugEnabled() ? new FallbackResourceManager.InputStreamLeakedResourceLogger(var3, var1, var2.getPackName()) : var3);
   }

   private void checkResourcePath(ResourceLocation var1) throws IOException {
      if (var1.getResourcePath().contains("..")) {
         throw new IOException("Invalid relative path to resource: " + var1);
      }
   }

   public List getAllResources(ResourceLocation var1) throws IOException {
      this.checkResourcePath(var1);
      ArrayList var2 = Lists.newArrayList();
      ResourceLocation var3 = getLocationMcmeta(var1);

      for(IResourcePack var5 : this.resourcePacks) {
         if (var5.resourceExists(var1)) {
            InputStream var6 = var5.resourceExists(var3) ? this.getInputStream(var3, var5) : null;
            var2.add(new SimpleResource(var5.getPackName(), var1, this.getInputStream(var1, var5), var6, this.frmMetadataSerializer));
         }
      }

      if (var2.isEmpty()) {
         throw new FileNotFoundException(var1.toString());
      } else {
         return var2;
      }
   }

   static ResourceLocation getLocationMcmeta(ResourceLocation var0) {
      return new ResourceLocation(var0.getResourceDomain(), var0.getResourcePath() + ".mcmeta");
   }

   @SideOnly(Side.CLIENT)
   static class InputStreamLeakedResourceLogger extends InputStream {
      private final InputStream inputStream;
      private final String message;
      private boolean isClosed;

      public InputStreamLeakedResourceLogger(InputStream var1, ResourceLocation var2, String var3) {
         this.inputStream = var1;
         ByteArrayOutputStream var4 = new ByteArrayOutputStream();
         (new Exception()).printStackTrace(new PrintStream(var4));
         this.message = "Leaked resource: '" + var2 + "' loaded from pack: '" + var3 + "'\n" + var4;
      }

      public void close() throws IOException {
         this.inputStream.close();
         this.isClosed = true;
      }

      protected void finalize() throws Throwable {
         if (!this.isClosed) {
            FallbackResourceManager.LOGGER.warn(this.message);
         }

         super.finalize();
      }

      public int read() throws IOException {
         return this.inputStream.read();
      }
   }
}
