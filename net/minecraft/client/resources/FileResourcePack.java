package net.minecraft.client.resources;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FileResourcePack extends AbstractResourcePack implements Closeable {
   public static final Splitter ENTRY_NAME_SPLITTER = Splitter.on('/').omitEmptyStrings().limit(3);
   private ZipFile resourcePackZipFile;

   public FileResourcePack(File var1) {
      super(var1);
   }

   private ZipFile getResourcePackZipFile() throws IOException {
      if (this.resourcePackZipFile == null) {
         this.resourcePackZipFile = new ZipFile(this.resourcePackFile);
      }

      return this.resourcePackZipFile;
   }

   protected InputStream getInputStreamByName(String var1) throws IOException {
      ZipFile var2 = this.getResourcePackZipFile();
      ZipEntry var3 = var2.getEntry(var1);
      if (var3 == null) {
         throw new ResourcePackFileNotFoundException(this.resourcePackFile, var1);
      } else {
         return var2.getInputStream(var3);
      }
   }

   public boolean hasResourceName(String var1) {
      try {
         return this.getResourcePackZipFile().getEntry(var1) != null;
      } catch (IOException var3) {
         return false;
      }
   }

   public Set getResourceDomains() {
      ZipFile var1;
      try {
         var1 = this.getResourcePackZipFile();
      } catch (IOException var8) {
         return Collections.emptySet();
      }

      Enumeration var2 = var1.entries();
      HashSet var3 = Sets.newHashSet();

      while(var2.hasMoreElements()) {
         ZipEntry var4 = (ZipEntry)var2.nextElement();
         String var5 = var4.getName();
         if (var5.startsWith("assets/")) {
            ArrayList var6 = Lists.newArrayList(ENTRY_NAME_SPLITTER.split(var5));
            if (var6.size() > 1) {
               String var7 = (String)var6.get(1);
               if (var7.equals(var7.toLowerCase())) {
                  var3.add(var7);
               } else {
                  this.logNameNotLowercase(var7);
               }
            }
         }
      }

      return var3;
   }

   protected void finalize() throws Throwable {
      this.close();
      super.finalize();
   }

   public void close() throws IOException {
      if (this.resourcePackZipFile != null) {
         this.resourcePackZipFile.close();
         this.resourcePackZipFile = null;
      }

   }
}
