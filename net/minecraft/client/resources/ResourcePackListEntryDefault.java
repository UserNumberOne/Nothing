package net.minecraft.client.resources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenResourcePacks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ResourcePackListEntryDefault extends ResourcePackListEntryServer {
   public ResourcePackListEntryDefault(GuiScreenResourcePacks var1) {
      super(var1, Minecraft.getMinecraft().getResourcePackRepository().rprDefaultResourcePack);
   }

   protected String getResourcePackName() {
      return "Default";
   }

   public boolean isServerPack() {
      return false;
   }
}
