package net.minecraft.client.audio;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PositionedSoundRecord extends PositionedSound {
   public PositionedSoundRecord(SoundEvent var1, SoundCategory var2, float var3, float var4, BlockPos var5) {
      this(soundIn, categoryIn, volumeIn, pitchIn, (float)pos.getX() + 0.5F, (float)pos.getY() + 0.5F, (float)pos.getZ() + 0.5F);
   }

   public static PositionedSoundRecord getMasterRecord(SoundEvent var0, float var1) {
      return new PositionedSoundRecord(soundIn, SoundCategory.MASTER, 0.25F, pitchIn, false, 0, ISound.AttenuationType.NONE, 0.0F, 0.0F, 0.0F);
   }

   public static PositionedSoundRecord getMusicRecord(SoundEvent var0) {
      return new PositionedSoundRecord(soundIn, SoundCategory.MUSIC, 1.0F, 1.0F, false, 0, ISound.AttenuationType.NONE, 0.0F, 0.0F, 0.0F);
   }

   public static PositionedSoundRecord getRecordSoundRecord(SoundEvent var0, float var1, float var2, float var3) {
      return new PositionedSoundRecord(soundIn, SoundCategory.RECORDS, 4.0F, 1.0F, false, 0, ISound.AttenuationType.LINEAR, xIn, yIn, zIn);
   }

   public PositionedSoundRecord(SoundEvent var1, SoundCategory var2, float var3, float var4, float var5, float var6, float var7) {
      this(soundIn, categoryIn, volumeIn, pitchIn, false, 0, ISound.AttenuationType.LINEAR, xIn, yIn, zIn);
   }

   private PositionedSoundRecord(SoundEvent var1, SoundCategory var2, float var3, float var4, boolean var5, int var6, ISound.AttenuationType var7, float var8, float var9, float var10) {
      this(soundIn.getSoundName(), categoryIn, volumeIn, pitchIn, repeatIn, repeatDelayIn, attenuationTypeIn, xIn, yIn, zIn);
   }

   public PositionedSoundRecord(ResourceLocation var1, SoundCategory var2, float var3, float var4, boolean var5, int var6, ISound.AttenuationType var7, float var8, float var9, float var10) {
      super(soundId, categoryIn);
      this.volume = volumeIn;
      this.pitch = pitchIn;
      this.xPosF = xIn;
      this.yPosF = yIn;
      this.zPosF = zIn;
      this.repeat = repeatIn;
      this.repeatDelay = repeatDelayIn;
      this.attenuationType = attenuationTypeIn;
   }
}
