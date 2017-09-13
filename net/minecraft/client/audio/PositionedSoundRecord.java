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
      this(var1, var2, var3, var4, (float)var5.getX() + 0.5F, (float)var5.getY() + 0.5F, (float)var5.getZ() + 0.5F);
   }

   public static PositionedSoundRecord getMasterRecord(SoundEvent var0, float var1) {
      return new PositionedSoundRecord(var0, SoundCategory.MASTER, 0.25F, var1, false, 0, ISound.AttenuationType.NONE, 0.0F, 0.0F, 0.0F);
   }

   public static PositionedSoundRecord getMusicRecord(SoundEvent var0) {
      return new PositionedSoundRecord(var0, SoundCategory.MUSIC, 1.0F, 1.0F, false, 0, ISound.AttenuationType.NONE, 0.0F, 0.0F, 0.0F);
   }

   public static PositionedSoundRecord getRecordSoundRecord(SoundEvent var0, float var1, float var2, float var3) {
      return new PositionedSoundRecord(var0, SoundCategory.RECORDS, 4.0F, 1.0F, false, 0, ISound.AttenuationType.LINEAR, var1, var2, var3);
   }

   public PositionedSoundRecord(SoundEvent var1, SoundCategory var2, float var3, float var4, float var5, float var6, float var7) {
      this(var1, var2, var3, var4, false, 0, ISound.AttenuationType.LINEAR, var5, var6, var7);
   }

   private PositionedSoundRecord(SoundEvent var1, SoundCategory var2, float var3, float var4, boolean var5, int var6, ISound.AttenuationType var7, float var8, float var9, float var10) {
      this(var1.getSoundName(), var2, var3, var4, var5, var6, var7, var8, var9, var10);
   }

   public PositionedSoundRecord(ResourceLocation var1, SoundCategory var2, float var3, float var4, boolean var5, int var6, ISound.AttenuationType var7, float var8, float var9, float var10) {
      super(var1, var2);
      this.volume = var3;
      this.pitch = var4;
      this.xPosF = var8;
      this.yPosF = var9;
      this.zPosF = var10;
      this.repeat = var5;
      this.repeatDelay = var6;
      this.attenuationType = var7;
   }
}
