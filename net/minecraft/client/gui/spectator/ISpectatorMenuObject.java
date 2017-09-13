package net.minecraft.client.gui.spectator;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ISpectatorMenuObject {
   void selectItem(SpectatorMenu var1);

   ITextComponent getSpectatorName();

   void renderIcon(float var1, int var2);

   boolean isEnabled();
}
