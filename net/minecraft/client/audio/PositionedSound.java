package net.minecraft.client.audio;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class PositionedSound implements ISound {
   protected Sound sound;
   @Nullable
   private SoundEventAccessor soundEvent;
   protected SoundCategory category;
   protected ResourceLocation positionedSoundLocation;
   protected float volume;
   protected float pitch;
   protected float xPosF;
   protected float yPosF;
   protected float zPosF;
   protected boolean repeat;
   protected int repeatDelay;
   protected ISound.AttenuationType attenuationType;

   protected PositionedSound(SoundEvent var1, SoundCategory var2) {
      this(var1.getSoundName(), var2);
   }

   protected PositionedSound(ResourceLocation var1, SoundCategory var2) {
      this.volume = 1.0F;
      this.pitch = 1.0F;
      this.attenuationType = ISound.AttenuationType.LINEAR;
      this.positionedSoundLocation = var1;
      this.category = var2;
   }

   public ResourceLocation getSoundLocation() {
      return this.positionedSoundLocation;
   }

   public SoundEventAccessor createAccessor(SoundHandler var1) {
      this.soundEvent = var1.getAccessor(this.positionedSoundLocation);
      if (this.soundEvent == null) {
         this.sound = SoundHandler.MISSING_SOUND;
      } else {
         this.sound = this.soundEvent.cloneEntry();
      }

      return this.soundEvent;
   }

   public Sound getSound() {
      return this.sound;
   }

   public SoundCategory getCategory() {
      return this.category;
   }

   public boolean canRepeat() {
      return this.repeat;
   }

   public int getRepeatDelay() {
      return this.repeatDelay;
   }

   public float getVolume() {
      return this.volume * this.sound.getVolume();
   }

   public float getPitch() {
      return this.pitch * this.sound.getPitch();
   }

   public float getXPosF() {
      return this.xPosF;
   }

   public float getYPosF() {
      return this.yPosF;
   }

   public float getZPosF() {
      return this.zPosF;
   }

   public ISound.AttenuationType getAttenuationType() {
      return this.attenuationType;
   }
}
