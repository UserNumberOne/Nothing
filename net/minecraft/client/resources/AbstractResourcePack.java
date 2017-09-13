package net.minecraft.client.resources;

import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public abstract class AbstractResourcePack implements IResourcePack {
   private static final Logger LOGGER = LogManager.getLogger();
   protected final File resourcePackFile;

   public AbstractResourcePack(File var1) {
      this.resourcePackFile = resourcePackFileIn;
   }

   private static String locationToName(ResourceLocation var0) {
      return String.format("%s/%s/%s", "assets", location.getResourceDomain(), location.getResourcePath());
   }

   protected static String getRelativeName(File var0, File var1) {
      return p_110595_0_.toURI().relativize(p_110595_1_.toURI()).getPath();
   }

   public InputStream getInputStream(ResourceLocation var1) throws IOException {
      return this.getInputStreamByName(locationToName(location));
   }

   public boolean resourceExists(ResourceLocation var1) {
      return this.hasResourceName(locationToName(location));
   }

   protected abstract InputStream getInputStreamByName(String var1) throws IOException;

   protected abstract boolean hasResourceName(String var1);

   protected void logNameNotLowercase(String var1) {
      LOGGER.warn("ResourcePack: ignored non-lowercase namespace: {} in {}", new Object[]{name, this.resourcePackFile});
   }

   public IMetadataSection getPackMetadata(MetadataSerializer var1, String var2) throws IOException {
      return readMetadata(metadataSerializer, this.getInputStreamByName("pack.mcmeta"), metadataSectionName);
   }

   static IMetadataSection readMetadata(MetadataSerializer var0, InputStream var1, String var2) {
      JsonObject jsonobject = null;
      BufferedReader bufferedreader = null;

      try {
         bufferedreader = new BufferedReader(new InputStreamReader(p_110596_1_, Charsets.UTF_8));
         jsonobject = (new JsonParser()).parse(bufferedreader).getAsJsonObject();
      } catch (RuntimeException var9) {
         throw new JsonParseException(var9);
      } finally {
         IOUtils.closeQuietly(bufferedreader);
      }

      return metadataSerializer.parseMetadataSection(sectionName, jsonobject);
   }

   public BufferedImage getPackImage() throws IOException {
      return TextureUtil.readBufferedImage(this.getInputStreamByName("pack.png"));
   }

   public String getPackName() {
      return this.resourcePackFile.getName();
   }
}
