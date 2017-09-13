package net.minecraft.client.resources;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class SimpleReloadableResourceManager implements IReloadableResourceManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Joiner JOINER_RESOURCE_PACKS = Joiner.on(", ");
   private final Map domainResourceManagers = Maps.newHashMap();
   private final List reloadListeners = Lists.newArrayList();
   private final Set setResourceDomains = Sets.newLinkedHashSet();
   private final MetadataSerializer rmMetadataSerializer;

   public SimpleReloadableResourceManager(MetadataSerializer var1) {
      this.rmMetadataSerializer = var1;
   }

   public void reloadResourcePack(IResourcePack var1) {
      for(String var3 : var1.getResourceDomains()) {
         this.setResourceDomains.add(var3);
         FallbackResourceManager var4 = (FallbackResourceManager)this.domainResourceManagers.get(var3);
         if (var4 == null) {
            var4 = new FallbackResourceManager(this.rmMetadataSerializer);
            this.domainResourceManagers.put(var3, var4);
         }

         var4.addResourcePack(var1);
      }

   }

   public Set getResourceDomains() {
      return this.setResourceDomains;
   }

   public IResource getResource(ResourceLocation var1) throws IOException {
      IResourceManager var2 = (IResourceManager)this.domainResourceManagers.get(var1.getResourceDomain());
      if (var2 != null) {
         return var2.getResource(var1);
      } else {
         throw new FileNotFoundException(var1.toString());
      }
   }

   public List getAllResources(ResourceLocation var1) throws IOException {
      IResourceManager var2 = (IResourceManager)this.domainResourceManagers.get(var1.getResourceDomain());
      if (var2 != null) {
         return var2.getAllResources(var1);
      } else {
         throw new FileNotFoundException(var1.toString());
      }
   }

   private void clearResources() {
      this.domainResourceManagers.clear();
      this.setResourceDomains.clear();
   }

   public void reloadResources(List var1) {
      ProgressBar var2 = ProgressManager.push("Loading Resources", var1.size() + 1, true);
      this.clearResources();
      LOGGER.info("Reloading ResourceManager: {}", new Object[]{JOINER_RESOURCE_PACKS.join(Iterables.transform(var1, new Function() {
         public String apply(@Nullable IResourcePack var1) {
            return var1 == null ? "<NULL>" : var1.getPackName();
         }
      }))});

      for(IResourcePack var4 : var1) {
         var2.step(var4.getPackName());
         this.reloadResourcePack(var4);
      }

      var2.step("Reloading listeners");
      this.notifyReloadListeners();
      ProgressManager.pop(var2);
   }

   public void registerReloadListener(IResourceManagerReloadListener var1) {
      ProgressBar var2 = ProgressManager.push("Loading Resource", 1);
      var2.step(var1.getClass(), new String[0]);
      this.reloadListeners.add(var1);
      var1.onResourceManagerReload(this);
      ProgressManager.pop(var2);
   }

   private void notifyReloadListeners() {
      ProgressBar var1 = ProgressManager.push("Reloading", this.reloadListeners.size());

      for(IResourceManagerReloadListener var3 : this.reloadListeners) {
         var1.step(var3.getClass(), new String[0]);
         var3.onResourceManagerReload(this);
      }

      ProgressManager.pop(var1);
   }
}
