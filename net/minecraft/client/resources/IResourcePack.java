package net.minecraft.client.resources;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IResourcePack {
   InputStream getInputStream(ResourceLocation var1) throws IOException;

   boolean resourceExists(ResourceLocation var1);

   Set getResourceDomains();

   IMetadataSection getPackMetadata(MetadataSerializer var1, String var2) throws IOException;

   BufferedImage getPackImage() throws IOException;

   String getPackName();
}
