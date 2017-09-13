package net.minecraft.client.audio;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SoundList {
   private final List sounds;
   private final boolean replaceExisting;
   private final String subtitle;

   public SoundList(List soundsIn, boolean replceIn, String subtitleIn) {
      this.sounds = soundsIn;
      this.replaceExisting = replceIn;
      this.subtitle = subtitleIn;
   }

   public List getSounds() {
      return this.sounds;
   }

   public boolean canReplaceExisting() {
      return this.replaceExisting;
   }

   @Nullable
   public String getSubtitle() {
      return this.subtitle;
   }
}
