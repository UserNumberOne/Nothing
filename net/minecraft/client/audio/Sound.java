package net.minecraft.client.audio;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Sound implements ISoundEventAccessor {
   private final ResourceLocation name;
   private final float volume;
   private final float pitch;
   private final int weight;
   private final Sound.Type type;
   private final boolean streaming;

   public Sound(String var1, float var2, float var3, int var4, Sound.Type var5, boolean var6) {
      this.name = new ResourceLocation(var1);
      this.volume = var2;
      this.pitch = var3;
      this.weight = var4;
      this.type = var5;
      this.streaming = var6;
   }

   public ResourceLocation getSoundLocation() {
      return this.name;
   }

   public ResourceLocation getSoundAsOggLocation() {
      return new ResourceLocation(this.name.getResourceDomain(), "sounds/" + this.name.getResourcePath() + ".ogg");
   }

   public float getVolume() {
      return this.volume;
   }

   public float getPitch() {
      return this.pitch;
   }

   public int getWeight() {
      return this.weight;
   }

   public Sound cloneEntry() {
      return this;
   }

   public Sound.Type getType() {
      return this.type;
   }

   public boolean isStreaming() {
      return this.streaming;
   }

   @SideOnly(Side.CLIENT)
   public static enum Type {
      FILE("file"),
      SOUND_EVENT("event");

      private final String name;

      private Type(String var3) {
         this.name = var3;
      }

      public static Sound.Type getByName(String var0) {
         for(Sound.Type var4 : values()) {
            if (var4.name.equals(var0)) {
               return var4;
            }
         }

         return null;
      }
   }
}
