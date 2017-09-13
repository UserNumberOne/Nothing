package net.minecraft.client.resources;

import com.google.common.collect.ImmutableSet;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DefaultResourcePack implements IResourcePack {
   public static final Set DEFAULT_RESOURCE_DOMAINS = ImmutableSet.of("minecraft", "realms");
   private final ResourceIndex resourceIndex;

   public DefaultResourcePack(ResourceIndex var1) {
      this.resourceIndex = resourceIndexIn;
   }

   public InputStream getInputStream(ResourceLocation var1) throws IOException {
      InputStream inputstream = this.getResourceStream(location);
      if (inputstream != null) {
         return inputstream;
      } else {
         InputStream inputstream1 = this.getInputStreamAssets(location);
         if (inputstream1 != null) {
            return inputstream1;
         } else {
            throw new FileNotFoundException(location.getResourcePath());
         }
      }
   }

   @Nullable
   public InputStream getInputStreamAssets(ResourceLocation var1) throws IOException, FileNotFoundException {
      File file1 = this.resourceIndex.getFile(location);
      return file1 != null && file1.isFile() ? new FileInputStream(file1) : null;
   }

   private InputStream getResourceStream(ResourceLocation var1) {
      return DefaultResourcePack.class.getResourceAsStream("/assets/" + location.getResourceDomain() + "/" + location.getResourcePath());
   }

   public boolean resourceExists(ResourceLocation var1) {
      return this.getResourceStream(location) != null || this.resourceIndex.isFileExisting(location);
   }

   public Set getResourceDomains() {
      return DEFAULT_RESOURCE_DOMAINS;
   }

   public IMetadataSection getPackMetadata(MetadataSerializer var1, String var2) throws IOException {
      try {
         InputStream inputstream = new FileInputStream(this.resourceIndex.getPackMcmeta());
         return AbstractResourcePack.readMetadata(metadataSerializer, inputstream, metadataSectionName);
      } catch (RuntimeException var4) {
         return (IMetadataSection)null;
      } catch (FileNotFoundException var5) {
         return (IMetadataSection)null;
      }
   }

   public BufferedImage getPackImage() throws IOException {
      return TextureUtil.readBufferedImage(DefaultResourcePack.class.getResourceAsStream("/" + (new ResourceLocation("pack.png")).getResourcePath()));
   }

   public String getPackName() {
      return "Default";
   }
}
