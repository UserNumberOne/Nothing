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
      this.resourceIndex = var1;
   }

   public InputStream getInputStream(ResourceLocation var1) throws IOException {
      InputStream var2 = this.getResourceStream(var1);
      if (var2 != null) {
         return var2;
      } else {
         InputStream var3 = this.getInputStreamAssets(var1);
         if (var3 != null) {
            return var3;
         } else {
            throw new FileNotFoundException(var1.getResourcePath());
         }
      }
   }

   @Nullable
   public InputStream getInputStreamAssets(ResourceLocation var1) throws IOException, FileNotFoundException {
      File var2 = this.resourceIndex.getFile(var1);
      return var2 != null && var2.isFile() ? new FileInputStream(var2) : null;
   }

   private InputStream getResourceStream(ResourceLocation var1) {
      return DefaultResourcePack.class.getResourceAsStream("/assets/" + var1.getResourceDomain() + "/" + var1.getResourcePath());
   }

   public boolean resourceExists(ResourceLocation var1) {
      return this.getResourceStream(var1) != null || this.resourceIndex.isFileExisting(var1);
   }

   public Set getResourceDomains() {
      return DEFAULT_RESOURCE_DOMAINS;
   }

   public IMetadataSection getPackMetadata(MetadataSerializer var1, String var2) throws IOException {
      try {
         FileInputStream var3 = new FileInputStream(this.resourceIndex.getPackMcmeta());
         return AbstractResourcePack.readMetadata(var1, var3, var2);
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
