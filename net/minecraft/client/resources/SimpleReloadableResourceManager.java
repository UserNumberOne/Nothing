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
      this.rmMetadataSerializer = rmMetadataSerializerIn;
   }

   public void reloadResourcePack(IResourcePack var1) {
      for(String s : resourcePack.getResourceDomains()) {
         this.setResourceDomains.add(s);
         FallbackResourceManager fallbackresourcemanager = (FallbackResourceManager)this.domainResourceManagers.get(s);
         if (fallbackresourcemanager == null) {
            fallbackresourcemanager = new FallbackResourceManager(this.rmMetadataSerializer);
            this.domainResourceManagers.put(s, fallbackresourcemanager);
         }

         fallbackresourcemanager.addResourcePack(resourcePack);
      }

   }

   public Set getResourceDomains() {
      return this.setResourceDomains;
   }

   public IResource getResource(ResourceLocation var1) throws IOException {
      IResourceManager iresourcemanager = (IResourceManager)this.domainResourceManagers.get(location.getResourceDomain());
      if (iresourcemanager != null) {
         return iresourcemanager.getResource(location);
      } else {
         throw new FileNotFoundException(location.toString());
      }
   }

   public List getAllResources(ResourceLocation var1) throws IOException {
      IResourceManager iresourcemanager = (IResourceManager)this.domainResourceManagers.get(location.getResourceDomain());
      if (iresourcemanager != null) {
         return iresourcemanager.getAllResources(location);
      } else {
         throw new FileNotFoundException(location.toString());
      }
   }

   private void clearResources() {
      this.domainResourceManagers.clear();
      this.setResourceDomains.clear();
   }

   public void reloadResources(List var1) {
      ProgressBar resReload = ProgressManager.push("Loading Resources", resourcesPacksList.size() + 1, true);
      this.clearResources();
      LOGGER.info("Reloading ResourceManager: {}", new Object[]{JOINER_RESOURCE_PACKS.join(Iterables.transform(resourcesPacksList, new Function() {
         public String apply(@Nullable IResourcePack var1) {
            return p_apply_1_ == null ? "<NULL>" : p_apply_1_.getPackName();
         }
      }))});

      for(IResourcePack iresourcepack : resourcesPacksList) {
         resReload.step(iresourcepack.getPackName());
         this.reloadResourcePack(iresourcepack);
      }

      resReload.step("Reloading listeners");
      this.notifyReloadListeners();
      ProgressManager.pop(resReload);
   }

   public void registerReloadListener(IResourceManagerReloadListener var1) {
      ProgressBar resReload = ProgressManager.push("Loading Resource", 1);
      resReload.step(reloadListener.getClass(), new String[0]);
      this.reloadListeners.add(reloadListener);
      reloadListener.onResourceManagerReload(this);
      ProgressManager.pop(resReload);
   }

   private void notifyReloadListeners() {
      ProgressBar resReload = ProgressManager.push("Reloading", this.reloadListeners.size());

      for(IResourceManagerReloadListener iresourcemanagerreloadlistener : this.reloadListeners) {
         resReload.step(iresourcemanagerreloadlistener.getClass(), new String[0]);
         iresourcemanagerreloadlistener.onResourceManagerReload(this);
      }

      ProgressManager.pop(resReload);
   }
}
