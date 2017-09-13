package net.minecraft.client.resources;

import java.util.List;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IReloadableResourceManager extends IResourceManager {
   void reloadResources(List var1);

   void registerReloadListener(IResourceManagerReloadListener var1);
}
