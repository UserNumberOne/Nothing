package net.minecraft.client.resources;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenWorking;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.client.resources.data.PackMetadataSection;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class ResourcePackRepository {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final FileFilter RESOURCE_PACK_FILTER = new FileFilter() {
      public boolean accept(File var1) {
         boolean var2 = var1.isFile() && var1.getName().endsWith(".zip");
         boolean var3 = var1.isDirectory() && (new File(var1, "pack.mcmeta")).isFile();
         return var2 || var3;
      }
   };
   private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
   private final File dirResourcepacks;
   public final IResourcePack rprDefaultResourcePack;
   private final File dirServerResourcepacks;
   public final MetadataSerializer rprMetadataSerializer;
   private IResourcePack resourcePackInstance;
   private final ReentrantLock lock = new ReentrantLock();
   private ListenableFuture downloadingPacks;
   private List repositoryEntriesAll = Lists.newArrayList();
   private final List repositoryEntries = Lists.newArrayList();

   public ResourcePackRepository(File var1, File var2, IResourcePack var3, MetadataSerializer var4, GameSettings var5) {
      this.dirResourcepacks = var1;
      this.dirServerResourcepacks = var2;
      this.rprDefaultResourcePack = var3;
      this.rprMetadataSerializer = var4;
      this.fixDirResourcepacks();
      this.updateRepositoryEntriesAll();
      Iterator var6 = var5.resourcePacks.iterator();

      while(var6.hasNext()) {
         String var7 = (String)var6.next();

         for(ResourcePackRepository.Entry var9 : this.repositoryEntriesAll) {
            if (var9.getResourcePackName().equals(var7)) {
               if (var9.getPackFormat() == 2 || var5.incompatibleResourcePacks.contains(var9.getResourcePackName())) {
                  this.repositoryEntries.add(var9);
                  break;
               }

               var6.remove();
               LOGGER.warn("Removed selected resource pack {} because it's no longer compatible", new Object[]{var9.getResourcePackName()});
            }
         }
      }

   }

   public static Map getDownloadHeaders() {
      HashMap var0 = Maps.newHashMap();
      var0.put("X-Minecraft-Username", Minecraft.getMinecraft().getSession().getUsername());
      var0.put("X-Minecraft-UUID", Minecraft.getMinecraft().getSession().getPlayerID());
      var0.put("X-Minecraft-Version", "1.10.2");
      return var0;
   }

   private void fixDirResourcepacks() {
      if (this.dirResourcepacks.exists()) {
         if (!this.dirResourcepacks.isDirectory() && (!this.dirResourcepacks.delete() || !this.dirResourcepacks.mkdirs())) {
            LOGGER.warn("Unable to recreate resourcepack folder, it exists but is not a directory: {}", new Object[]{this.dirResourcepacks});
         }
      } else if (!this.dirResourcepacks.mkdirs()) {
         LOGGER.warn("Unable to create resourcepack folder: {}", new Object[]{this.dirResourcepacks});
      }

   }

   private List getResourcePackFiles() {
      return this.dirResourcepacks.isDirectory() ? Arrays.asList(this.dirResourcepacks.listFiles(RESOURCE_PACK_FILTER)) : Collections.emptyList();
   }

   public void updateRepositoryEntriesAll() {
      ArrayList var1 = Lists.newArrayList();

      for(File var3 : this.getResourcePackFiles()) {
         ResourcePackRepository.Entry var4 = new ResourcePackRepository.Entry(var3);
         if (this.repositoryEntriesAll.contains(var4)) {
            int var5 = this.repositoryEntriesAll.indexOf(var4);
            if (var5 > -1 && var5 < this.repositoryEntriesAll.size()) {
               var1.add(this.repositoryEntriesAll.get(var5));
            }
         } else {
            try {
               var4.updateResourcePack();
               var1.add(var4);
            } catch (Exception var6) {
               var1.remove(var4);
            }
         }
      }

      this.repositoryEntriesAll.removeAll(var1);

      for(ResourcePackRepository.Entry var8 : this.repositoryEntriesAll) {
         var8.closeResourcePack();
      }

      this.repositoryEntriesAll = var1;
   }

   @Nullable
   public ResourcePackRepository.Entry getResourcePackEntry() {
      if (this.resourcePackInstance != null) {
         ResourcePackRepository.Entry var1 = new ResourcePackRepository.Entry(this.resourcePackInstance);

         try {
            var1.updateResourcePack();
            return var1;
         } catch (IOException var3) {
            ;
         }
      }

      return null;
   }

   public List getRepositoryEntriesAll() {
      return ImmutableList.copyOf(this.repositoryEntriesAll);
   }

   public List getRepositoryEntries() {
      return ImmutableList.copyOf(this.repositoryEntries);
   }

   public void setRepositories(List var1) {
      this.repositoryEntries.clear();
      this.repositoryEntries.addAll(var1);
   }

   public File getDirResourcepacks() {
      return this.dirResourcepacks;
   }

   public ListenableFuture downloadResourcePack(String var1, String var2) {
      String var3 = DigestUtils.sha1Hex(var1);
      final String var4 = SHA1.matcher(var2).matches() ? var2 : "";
      final File var5 = new File(this.dirServerResourcepacks, var3);
      this.lock.lock();

      try {
         this.clearResourcePack();
         if (var5.exists()) {
            if (this.checkHash(var4, var5)) {
               ListenableFuture var15 = this.setResourcePackInstance(var5);
               ListenableFuture var16 = var15;
               return var16;
            }

            LOGGER.warn("Deleting file {}", new Object[]{var5});
            FileUtils.deleteQuietly(var5);
         }

         this.deleteOldServerResourcesPacks();
         final GuiScreenWorking var6 = new GuiScreenWorking();
         Map var7 = getDownloadHeaders();
         final Minecraft var8 = Minecraft.getMinecraft();
         Futures.getUnchecked(var8.addScheduledTask(new Runnable() {
            public void run() {
               var8.displayGuiScreen(var6);
            }
         }));
         final SettableFuture var9 = SettableFuture.create();
         this.downloadingPacks = HttpUtil.downloadResourcePack(var5, var1, var7, 52428800, var6, var8.getProxy());
         Futures.addCallback(this.downloadingPacks, new FutureCallback() {
            public void onSuccess(@Nullable Object var1) {
               if (ResourcePackRepository.this.checkHash(var4, var5)) {
                  ResourcePackRepository.this.setResourcePackInstance(var5);
                  var9.set((Object)null);
               } else {
                  ResourcePackRepository.LOGGER.warn("Deleting file {}", new Object[]{var5});
                  FileUtils.deleteQuietly(var5);
               }

            }

            public void onFailure(Throwable var1) {
               FileUtils.deleteQuietly(var5);
               var9.setException(var1);
            }
         });
         ListenableFuture var10 = this.downloadingPacks;
         ListenableFuture var11 = var10;
         return var11;
      } finally {
         this.lock.unlock();
      }
   }

   private boolean checkHash(String var1, File var2) {
      try {
         String var3 = DigestUtils.sha1Hex(new FileInputStream(var2));
         if (var1.isEmpty()) {
            LOGGER.info("Found file {} without verification hash", new Object[]{var2});
            return true;
         }

         if (var3.toLowerCase().equals(var1.toLowerCase())) {
            LOGGER.info("Found file {} matching requested hash {}", new Object[]{var2, var1});
            return true;
         }

         LOGGER.warn("File {} had wrong hash (expected {}, found {}).", new Object[]{var2, var1, var3});
      } catch (IOException var4) {
         LOGGER.warn("File {} couldn't be hashed.", new Object[]{var2, var4});
      }

      return false;
   }

   private boolean validatePack(File var1) {
      ResourcePackRepository.Entry var2 = new ResourcePackRepository.Entry(new FileResourcePack(var1));

      try {
         var2.updateResourcePack();
         return true;
      } catch (Exception var4) {
         LOGGER.warn("Server resourcepack is invalid, ignoring it", var4);
         return false;
      }
   }

   private void deleteOldServerResourcesPacks() {
      try {
         ArrayList var1 = Lists.newArrayList(FileUtils.listFiles(this.dirServerResourcepacks, TrueFileFilter.TRUE, (IOFileFilter)null));
         Collections.sort(var1, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
         int var2 = 0;

         for(File var4 : var1) {
            if (var2++ >= 10) {
               LOGGER.info("Deleting old server resource pack {}", new Object[]{var4.getName()});
               FileUtils.deleteQuietly(var4);
            }
         }
      } catch (IllegalArgumentException var5) {
         LOGGER.error("Error while deleting old server resource pack : {}", new Object[]{var5.getMessage()});
      }

   }

   public ListenableFuture setResourcePackInstance(File var1) {
      if (!this.validatePack(var1)) {
         return Futures.immediateFailedFuture(new RuntimeException("Invalid resourcepack"));
      } else {
         this.resourcePackInstance = new FileResourcePack(var1);
         return Minecraft.getMinecraft().scheduleResourcesRefresh();
      }
   }

   public IResourcePack getResourcePackInstance() {
      return this.resourcePackInstance;
   }

   public void clearResourcePack() {
      this.lock.lock();

      try {
         if (this.downloadingPacks != null) {
            this.downloadingPacks.cancel(true);
         }

         this.downloadingPacks = null;
         if (this.resourcePackInstance != null) {
            this.resourcePackInstance = null;
            Minecraft.getMinecraft().scheduleResourcesRefresh();
         }
      } finally {
         this.lock.unlock();
      }

   }

   @SideOnly(Side.CLIENT)
   public class Entry {
      private final IResourcePack reResourcePack;
      private PackMetadataSection rePackMetadataSection;
      private ResourceLocation locationTexturePackIcon;

      private Entry(File var2) {
         this((IResourcePack)(var2.isDirectory() ? new FolderResourcePack(var2) : new FileResourcePack(var2)));
      }

      private Entry(IResourcePack var2) {
         this.reResourcePack = var2;
      }

      public void updateResourcePack() throws IOException {
         this.rePackMetadataSection = (PackMetadataSection)this.reResourcePack.getPackMetadata(ResourcePackRepository.this.rprMetadataSerializer, "pack");
         this.closeResourcePack();
      }

      public void bindTexturePackIcon(TextureManager var1) {
         BufferedImage var2 = null;

         try {
            var2 = this.reResourcePack.getPackImage();
         } catch (IOException var5) {
            ;
         }

         if (var2 == null) {
            try {
               var2 = ResourcePackRepository.this.rprDefaultResourcePack.getPackImage();
            } catch (IOException var4) {
               throw new Error("Couldn't bind resource pack icon", var4);
            }
         }

         if (this.locationTexturePackIcon == null) {
            this.locationTexturePackIcon = var1.getDynamicTextureLocation("texturepackicon", new DynamicTexture(var2));
         }

         var1.bindTexture(this.locationTexturePackIcon);
      }

      public void closeResourcePack() {
         if (this.reResourcePack instanceof Closeable) {
            IOUtils.closeQuietly((Closeable)this.reResourcePack);
         }

      }

      public IResourcePack getResourcePack() {
         return this.reResourcePack;
      }

      public String getResourcePackName() {
         return this.reResourcePack.getPackName();
      }

      public String getTexturePackDescription() {
         return this.rePackMetadataSection == null ? TextFormatting.RED + "Invalid pack.mcmeta (or missing 'pack' section)" : this.rePackMetadataSection.getPackDescription().getFormattedText();
      }

      public int getPackFormat() {
         return this.rePackMetadataSection == null ? 0 : this.rePackMetadataSection.getPackFormat();
      }

      public boolean equals(Object var1) {
         return this == var1 ? true : (var1 instanceof ResourcePackRepository.Entry ? this.toString().equals(var1.toString()) : false);
      }

      public int hashCode() {
         return this.toString().hashCode();
      }

      public String toString() {
         return String.format("%s:%s", this.reResourcePack.getPackName(), this.reResourcePack instanceof FolderResourcePack ? "folder" : "zip");
      }
   }
}
