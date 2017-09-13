package net.minecraft.client.resources.data;

import com.google.gson.JsonDeserializer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IMetadataSectionSerializer extends JsonDeserializer {
   String getSectionName();
}