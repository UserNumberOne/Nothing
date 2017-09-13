package net.minecraft.client.resources;

import java.io.File;
import java.io.FileNotFoundException;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ResourcePackFileNotFoundException extends FileNotFoundException {
   public ResourcePackFileNotFoundException(File var1, String var2) {
      super(String.format("'%s' in ResourcePack '%s'", p_i1294_2_, resourcePack));
   }
}
