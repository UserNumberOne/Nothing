package net.minecraft.client.audio;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ISoundEventListener {
   void soundPlay(ISound var1, SoundEventAccessor var2);
}
