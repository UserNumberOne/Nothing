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

   public SoundList(List var1, boolean var2, String var3) {
      this.sounds = var1;
      this.replaceExisting = var2;
      this.subtitle = var3;
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
