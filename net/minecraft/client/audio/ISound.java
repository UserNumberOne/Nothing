package net.minecraft.client.audio;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ISound {
   ResourceLocation getSoundLocation();

   @Nullable
   SoundEventAccessor createAccessor(SoundHandler var1);

   Sound getSound();

   SoundCategory getCategory();

   boolean canRepeat();

   int getRepeatDelay();

   float getVolume();

   float getPitch();

   float getXPosF();

   float getYPosF();

   float getZPosF();

   ISound.AttenuationType getAttenuationType();

   @SideOnly(Side.CLIENT)
   public static enum AttenuationType {
      NONE(0),
      LINEAR(2);

      private final int type;

      private AttenuationType(int var3) {
         this.type = var3;
      }

      public int getTypeInt() {
         return this.type;
      }
   }
}
