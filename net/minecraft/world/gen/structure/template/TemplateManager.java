package net.minecraft.world.gen.structure.template;

import com.google.common.collect.Maps;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

public class TemplateManager {
   private final Map templates = Maps.newHashMap();
   private final String baseFolder;

   public TemplateManager(String var1) {
      this.baseFolder = var1;
   }

   public Template getTemplate(@Nullable MinecraftServer var1, ResourceLocation var2) {
      Template var3 = this.get(var1, var2);
      if (var3 == null) {
         var3 = new Template();
         this.templates.put(var2.getResourcePath(), var3);
      }

      return var3;
   }

   @Nullable
   public Template get(@Nullable MinecraftServer var1, ResourceLocation var2) {
      String var3 = var2.getResourcePath();
      if (this.templates.containsKey(var3)) {
         return (Template)this.templates.get(var3);
      } else {
         if (var1 != null) {
            this.readTemplate(var1, var2);
         } else {
            this.readTemplateFromJar(var2);
         }

         return this.templates.containsKey(var3) ? (Template)this.templates.get(var3) : null;
      }
   }

   public boolean readTemplate(MinecraftServer var1, ResourceLocation var2) {
      String var3 = var2.getResourcePath();
      File var4 = new File(this.baseFolder, var3 + ".nbt");
      if (!var4.exists()) {
         return this.readTemplateFromJar(var2);
      } else {
         FileInputStream var5 = null;

         boolean var6;
         try {
            var5 = new FileInputStream(var4);
            this.readTemplateFromStream(var3, var5);
            boolean var7 = true;
            return var7;
         } catch (Throwable var11) {
            var6 = false;
         } finally {
            IOUtils.closeQuietly(var5);
         }

         return var6;
      }
   }

   private boolean readTemplateFromJar(ResourceLocation var1) {
      String var2 = var1.getResourceDomain();
      String var3 = var1.getResourcePath();
      InputStream var4 = null;

      boolean var5;
      try {
         var4 = MinecraftServer.class.getResourceAsStream("/assets/" + var2 + "/structures/" + var3 + ".nbt");
         this.readTemplateFromStream(var3, var4);
         boolean var6 = true;
         return var6;
      } catch (Throwable var10) {
         var5 = false;
      } finally {
         IOUtils.closeQuietly(var4);
      }

      return var5;
   }

   private void readTemplateFromStream(String var1, InputStream var2) throws IOException {
      NBTTagCompound var3 = CompressedStreamTools.readCompressed(var2);
      Template var4 = new Template();
      var4.read(var3);
      this.templates.put(var1, var4);
   }

   public boolean writeTemplate(@Nullable MinecraftServer var1, ResourceLocation var2) {
      String var3 = var2.getResourcePath();
      if (var1 != null && this.templates.containsKey(var3)) {
         File var4 = new File(this.baseFolder);
         if (!var4.exists()) {
            if (!var4.mkdirs()) {
               return false;
            }
         } else if (!var4.isDirectory()) {
            return false;
         }

         File var5 = new File(var4, var3 + ".nbt");
         Template var6 = (Template)this.templates.get(var3);
         FileOutputStream var7 = null;

         boolean var8;
         try {
            NBTTagCompound var9 = var6.writeToNBT(new NBTTagCompound());
            var7 = new FileOutputStream(var5);
            CompressedStreamTools.writeCompressed(var9, var7);
            boolean var10 = true;
            return var10;
         } catch (Throwable var14) {
            var8 = false;
         } finally {
            IOUtils.closeQuietly(var7);
         }

         return var8;
      } else {
         return false;
      }
   }

   public void remove(ResourceLocation var1) {
      this.templates.remove(var1.getResourcePath());
   }
}
