package net.minecraft.client.gui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.ResourcePackListEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiResourcePackSelected extends GuiResourcePackList {
   public GuiResourcePackSelected(Minecraft var1, int var2, int var3, List var4) {
      super(mcIn, p_i45056_2_, p_i45056_3_, p_i45056_4_);
   }

   protected String getListHeader() {
      return I18n.format("resourcePack.selected.title");
   }
}
