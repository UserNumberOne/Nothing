package net.minecraft.client.resources;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class ResourceIndex {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Map resourceMap = Maps.newHashMap();

   protected ResourceIndex() {
   }

   public ResourceIndex(File var1, String var2) {
      File var3 = new File(var1, "objects");
      File var4 = new File(var1, "indexes/" + var2 + ".json");
      BufferedReader var5 = null;

      try {
         var5 = Files.newReader(var4, Charsets.UTF_8);
         JsonObject var6 = (new JsonParser()).parse(var5).getAsJsonObject();
         JsonObject var7 = JsonUtils.getJsonObject(var6, "objects", (JsonObject)null);
         if (var7 != null) {
            for(Entry var9 : var7.entrySet()) {
               JsonObject var10 = (JsonObject)var9.getValue();
               String var11 = (String)var9.getKey();
               String[] var12 = var11.split("/", 2);
               String var13 = var12.length == 1 ? var12[0] : var12[0] + ":" + var12[1];
               String var14 = JsonUtils.getString(var10, "hash");
               File var15 = new File(var3, var14.substring(0, 2) + "/" + var14);
               this.resourceMap.put(var13, var15);
            }
         }
      } catch (JsonParseException var20) {
         LOGGER.error("Unable to parse resource index file: {}", new Object[]{var4});
      } catch (FileNotFoundException var21) {
         LOGGER.error("Can't find the resource index file: {}", new Object[]{var4});
      } finally {
         IOUtils.closeQuietly(var5);
      }

   }

   @Nullable
   public File getFile(ResourceLocation var1) {
      String var2 = var1.toString();
      return (File)this.resourceMap.get(var2);
   }

   public boolean isFileExisting(ResourceLocation var1) {
      File var2 = this.getFile(var1);
      return var2 != null && var2.isFile();
   }

   public File getPackMcmeta() {
      return (File)this.resourceMap.get("pack.mcmeta");
   }
}
