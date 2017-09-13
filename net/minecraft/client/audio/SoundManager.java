package net.minecraft.client.audio;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import io.netty.util.internal.ThreadLocalRandom;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.sound.PlaySoundSourceEvent;
import net.minecraftforge.client.event.sound.PlayStreamingSourceEvent;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.client.event.sound.SoundSetupEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.SoundSystemLogger;
import paulscode.sound.Source;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

@SideOnly(Side.CLIENT)
public class SoundManager {
   private static final Marker LOG_MARKER = MarkerManager.getMarker("SOUNDS");
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Set UNABLE_TO_PLAY = Sets.newHashSet();
   public final SoundHandler sndHandler;
   private final GameSettings options;
   private SoundManager.SoundSystemStarterThread sndSystem;
   private boolean loaded;
   private int playTime;
   private final Map playingSounds = HashBiMap.create();
   private final Map invPlayingSounds;
   private final Multimap categorySounds;
   private final List tickableSounds;
   private final Map delayedSounds;
   private final Map playingSoundsStopTime;
   private final List listeners;
   private final List pausedChannels;

   public SoundManager(SoundHandler var1, GameSettings var2) {
      this.invPlayingSounds = ((BiMap)this.playingSounds).inverse();
      this.categorySounds = HashMultimap.create();
      this.tickableSounds = Lists.newArrayList();
      this.delayedSounds = Maps.newHashMap();
      this.playingSoundsStopTime = Maps.newHashMap();
      this.listeners = Lists.newArrayList();
      this.pausedChannels = Lists.newArrayList();
      this.sndHandler = p_i45119_1_;
      this.options = p_i45119_2_;

      try {
         SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);
         SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
         MinecraftForge.EVENT_BUS.post(new SoundSetupEvent(this));
      } catch (SoundSystemException var4) {
         LOGGER.error(LOG_MARKER, "Error linking with the LibraryJavaSound plug-in", var4);
      }

   }

   public void reloadSoundSystem() {
      UNABLE_TO_PLAY.clear();

      for(SoundEvent soundevent : SoundEvent.REGISTRY) {
         ResourceLocation resourcelocation = soundevent.getSoundName();
         if (this.sndHandler.getAccessor(resourcelocation) == null) {
            LOGGER.warn("Missing sound for event: {}", new Object[]{SoundEvent.REGISTRY.getNameForObject(soundevent)});
            UNABLE_TO_PLAY.add(resourcelocation);
         }
      }

      this.unloadSoundSystem();
      this.loadSoundSystem();
      MinecraftForge.EVENT_BUS.post(new SoundLoadEvent(this));
   }

   private synchronized void loadSoundSystem() {
      if (!this.loaded) {
         try {
            (new Thread(new Runnable() {
               public void run() {
                  SoundSystemConfig.setLogger(new SoundSystemLogger() {
                     public void message(String var1, int var2) {
                        if (!p_message_1_.isEmpty()) {
                           SoundManager.LOGGER.info(p_message_1_);
                        }

                     }

                     public void importantMessage(String var1, int var2) {
                        if (!p_importantMessage_1_.isEmpty()) {
                           SoundManager.LOGGER.warn(p_importantMessage_1_);
                        }

                     }

                     public void errorMessage(String var1, String var2, int var3) {
                        if (!p_errorMessage_2_.isEmpty()) {
                           SoundManager.LOGGER.error("Error in class '{}'", new Object[]{p_errorMessage_1_});
                           SoundManager.LOGGER.error(p_errorMessage_2_);
                        }

                     }
                  });
                  SoundManager var10000 = SoundManager.this;
                  SoundManager var10003 = SoundManager.this;
                  SoundManager.this.getClass();
                  var10000.sndSystem = var10003.new SoundSystemStarterThread();
                  SoundManager.this.loaded = true;
                  SoundManager.this.sndSystem.setMasterVolume(SoundManager.this.options.getSoundLevel(SoundCategory.MASTER));
                  SoundManager.LOGGER.info(SoundManager.LOG_MARKER, "Sound engine started");
               }
            }, "Sound Library Loader")).start();
         } catch (RuntimeException var2) {
            LOGGER.error(LOG_MARKER, "Error starting SoundSystem. Turning off sounds & music", var2);
            this.options.setSoundLevel(SoundCategory.MASTER, 0.0F);
            this.options.saveOptions();
         }
      }

   }

   private float getVolume(SoundCategory var1) {
      return category != null && category != SoundCategory.MASTER ? this.options.getSoundLevel(category) : 1.0F;
   }

   public void setVolume(SoundCategory var1, float var2) {
      if (this.loaded) {
         if (category == SoundCategory.MASTER) {
            this.sndSystem.setMasterVolume(volume);
         } else {
            for(String s : this.categorySounds.get(category)) {
               ISound isound = (ISound)this.playingSounds.get(s);
               float f = this.getClampedVolume(isound);
               if (f <= 0.0F) {
                  this.stopSound(isound);
               } else {
                  this.sndSystem.setVolume(s, f);
               }
            }
         }
      }

   }

   public void unloadSoundSystem() {
      if (this.loaded) {
         this.stopAllSounds();
         this.sndSystem.cleanup();
         this.loaded = false;
      }

   }

   public void stopAllSounds() {
      if (this.loaded) {
         for(String s : this.playingSounds.keySet()) {
            this.sndSystem.stop(s);
         }

         this.pausedChannels.clear();
         this.playingSounds.clear();
         this.delayedSounds.clear();
         this.tickableSounds.clear();
         this.categorySounds.clear();
         this.playingSoundsStopTime.clear();
      }

   }

   public void addListener(ISoundEventListener var1) {
      this.listeners.add(listener);
   }

   public void removeListener(ISoundEventListener var1) {
      this.listeners.remove(listener);
   }

   public void updateAllSounds() {
      ++this.playTime;

      for(ITickableSound itickablesound : this.tickableSounds) {
         itickablesound.update();
         if (itickablesound.isDonePlaying()) {
            this.stopSound(itickablesound);
         } else {
            String s = (String)this.invPlayingSounds.get(itickablesound);
            this.sndSystem.setVolume(s, this.getClampedVolume(itickablesound));
            this.sndSystem.setPitch(s, this.getClampedPitch(itickablesound));
            this.sndSystem.setPosition(s, itickablesound.getXPosF(), itickablesound.getYPosF(), itickablesound.getZPosF());
         }
      }

      Iterator iterator = this.playingSounds.entrySet().iterator();

      while(iterator.hasNext()) {
         Entry entry = (Entry)iterator.next();
         String s1 = (String)entry.getKey();
         ISound isound = (ISound)entry.getValue();
         if (!this.sndSystem.playing(s1)) {
            int i = ((Integer)this.playingSoundsStopTime.get(s1)).intValue();
            if (i <= this.playTime) {
               int j = isound.getRepeatDelay();
               if (isound.canRepeat() && j > 0) {
                  this.delayedSounds.put(isound, Integer.valueOf(this.playTime + j));
               }

               iterator.remove();
               LOGGER.debug(LOG_MARKER, "Removed channel {} because it's not playing anymore", new Object[]{s1});
               this.sndSystem.removeSource(s1);
               this.playingSoundsStopTime.remove(s1);

               try {
                  this.categorySounds.remove(isound.getCategory(), s1);
               } catch (RuntimeException var8) {
                  ;
               }

               if (isound instanceof ITickableSound) {
                  this.tickableSounds.remove(isound);
               }
            }
         }
      }

      Iterator iterator1 = this.delayedSounds.entrySet().iterator();

      while(iterator1.hasNext()) {
         Entry entry1 = (Entry)iterator1.next();
         if (this.playTime >= ((Integer)entry1.getValue()).intValue()) {
            ISound isound1 = (ISound)entry1.getKey();
            if (isound1 instanceof ITickableSound) {
               ((ITickableSound)isound1).update();
            }

            this.playSound(isound1);
            iterator1.remove();
         }
      }

   }

   public boolean isSoundPlaying(ISound var1) {
      if (!this.loaded) {
         return false;
      } else {
         String s = (String)this.invPlayingSounds.get(sound);
         return s == null ? false : this.sndSystem.playing(s) || this.playingSoundsStopTime.containsKey(s) && ((Integer)this.playingSoundsStopTime.get(s)).intValue() <= this.playTime;
      }
   }

   public void stopSound(ISound var1) {
      if (this.loaded) {
         String s = (String)this.invPlayingSounds.get(sound);
         if (s != null) {
            this.sndSystem.stop(s);
         }
      }

   }

   public void playSound(ISound var1) {
      if (this.loaded) {
         p_sound = ForgeHooksClient.playSound(this, p_sound);
         if (p_sound == null) {
            return;
         }

         SoundEventAccessor soundeventaccessor = p_sound.createAccessor(this.sndHandler);
         ResourceLocation resourcelocation = p_sound.getSoundLocation();
         if (soundeventaccessor == null) {
            if (UNABLE_TO_PLAY.add(resourcelocation)) {
               LOGGER.warn(LOG_MARKER, "Unable to play unknown soundEvent: {}", new Object[]{resourcelocation});
            }
         } else {
            if (!this.listeners.isEmpty()) {
               for(ISoundEventListener isoundeventlistener : this.listeners) {
                  isoundeventlistener.soundPlay(p_sound, soundeventaccessor);
               }
            }

            if (this.sndSystem.getMasterVolume() <= 0.0F) {
               LOGGER.debug(LOG_MARKER, "Skipped playing soundEvent: {}, master volume was zero", new Object[]{resourcelocation});
            } else {
               Sound sound = p_sound.getSound();
               if (sound == SoundHandler.MISSING_SOUND) {
                  if (UNABLE_TO_PLAY.add(resourcelocation)) {
                     LOGGER.warn(LOG_MARKER, "Unable to play empty soundEvent: {}", new Object[]{resourcelocation});
                  }
               } else {
                  float f3 = p_sound.getVolume();
                  float f = 16.0F;
                  if (f3 > 1.0F) {
                     f *= f3;
                  }

                  SoundCategory soundcategory = p_sound.getCategory();
                  float f1 = this.getClampedVolume(p_sound);
                  float f2 = this.getClampedPitch(p_sound);
                  if (f1 == 0.0F) {
                     LOGGER.debug(LOG_MARKER, "Skipped playing sound {}, volume was zero.", new Object[]{sound.getSoundLocation()});
                  } else {
                     boolean flag = p_sound.canRepeat() && p_sound.getRepeatDelay() == 0;
                     String s = MathHelper.getRandomUUID(ThreadLocalRandom.current()).toString();
                     ResourceLocation resourcelocation1 = sound.getSoundAsOggLocation();
                     if (sound.isStreaming()) {
                        this.sndSystem.newStreamingSource(false, s, getURLForSoundResource(resourcelocation1), resourcelocation1.toString(), flag, p_sound.getXPosF(), p_sound.getYPosF(), p_sound.getZPosF(), p_sound.getAttenuationType().getTypeInt(), f);
                        MinecraftForge.EVENT_BUS.post(new PlayStreamingSourceEvent(this, p_sound, s));
                     } else {
                        this.sndSystem.newSource(false, s, getURLForSoundResource(resourcelocation1), resourcelocation1.toString(), flag, p_sound.getXPosF(), p_sound.getYPosF(), p_sound.getZPosF(), p_sound.getAttenuationType().getTypeInt(), f);
                        MinecraftForge.EVENT_BUS.post(new PlaySoundSourceEvent(this, p_sound, s));
                     }

                     LOGGER.debug(LOG_MARKER, "Playing sound {} for event {} as channel {}", new Object[]{sound.getSoundLocation(), resourcelocation1, s});
                     this.sndSystem.setPitch(s, f2);
                     this.sndSystem.setVolume(s, f1);
                     this.sndSystem.play(s);
                     this.playingSoundsStopTime.put(s, Integer.valueOf(this.playTime + 20));
                     this.playingSounds.put(s, p_sound);
                     if (soundcategory != SoundCategory.MASTER) {
                        this.categorySounds.put(soundcategory, s);
                     }

                     if (p_sound instanceof ITickableSound) {
                        this.tickableSounds.add((ITickableSound)p_sound);
                     }
                  }
               }
            }
         }
      }

   }

   private float getClampedPitch(ISound var1) {
      return MathHelper.clamp(soundIn.getPitch(), 0.5F, 2.0F);
   }

   private float getClampedVolume(ISound var1) {
      return MathHelper.clamp(soundIn.getVolume() * this.getVolume(soundIn.getCategory()), 0.0F, 1.0F);
   }

   public void pauseAllSounds() {
      for(Entry entry : this.playingSounds.entrySet()) {
         String s = (String)entry.getKey();
         boolean flag = this.isSoundPlaying((ISound)entry.getValue());
         if (flag) {
            LOGGER.debug(LOG_MARKER, "Pausing channel {}", new Object[]{s});
            this.sndSystem.pause(s);
            this.pausedChannels.add(s);
         }
      }

   }

   public void resumeAllSounds() {
      for(String s : this.pausedChannels) {
         LOGGER.debug(LOG_MARKER, "Resuming channel {}", new Object[]{s});
         this.sndSystem.play(s);
      }

      this.pausedChannels.clear();
   }

   public void playDelayedSound(ISound var1, int var2) {
      this.delayedSounds.put(sound, Integer.valueOf(this.playTime + delay));
   }

   private static URL getURLForSoundResource(final ResourceLocation var0) {
      String s = String.format("%s:%s:%s", "mcsounddomain", p_148612_0_.getResourceDomain(), p_148612_0_.getResourcePath());
      URLStreamHandler urlstreamhandler = new URLStreamHandler() {
         protected URLConnection openConnection(URL var1) {
            return new URLConnection(p_openConnection_1_) {
               public void connect() throws IOException {
               }

               public InputStream getInputStream() throws IOException {
                  return Minecraft.getMinecraft().getResourceManager().getResource(p_148612_0_).getInputStream();
               }
            };
         }
      };

      try {
         return new URL((URL)null, s, urlstreamhandler);
      } catch (MalformedURLException var4) {
         throw new Error("TODO: Sanely handle url exception! :D");
      }
   }

   public void setListener(EntityPlayer var1, float var2) {
      if (this.loaded && player != null) {
         float f = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * p_148615_2_;
         float f1 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * p_148615_2_;
         double d0 = player.prevPosX + (player.posX - player.prevPosX) * (double)p_148615_2_;
         double d1 = player.prevPosY + (player.posY - player.prevPosY) * (double)p_148615_2_ + (double)player.getEyeHeight();
         double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)p_148615_2_;
         float f2 = MathHelper.cos((f1 + 90.0F) * 0.017453292F);
         float f3 = MathHelper.sin((f1 + 90.0F) * 0.017453292F);
         float f4 = MathHelper.cos(-f * 0.017453292F);
         float f5 = MathHelper.sin(-f * 0.017453292F);
         float f6 = MathHelper.cos((-f + 90.0F) * 0.017453292F);
         float f7 = MathHelper.sin((-f + 90.0F) * 0.017453292F);
         float f8 = f2 * f4;
         float f9 = f3 * f4;
         float f10 = f2 * f6;
         float f11 = f3 * f6;
         this.sndSystem.setListenerPosition((float)d0, (float)d1, (float)d2);
         this.sndSystem.setListenerOrientation(f8, f5, f9, f10, f7, f11);
      }

   }

   public void stop(String var1, SoundCategory var2) {
      if (p_189567_2_ != null) {
         for(String s : this.categorySounds.get(p_189567_2_)) {
            ISound isound = (ISound)this.playingSounds.get(s);
            if (!p_189567_1_.isEmpty()) {
               if (isound.getSoundLocation().equals(new ResourceLocation(p_189567_1_))) {
                  this.stopSound(isound);
               }
            } else {
               this.stopSound(isound);
            }
         }
      } else if (!p_189567_1_.isEmpty()) {
         for(ISound isound1 : this.playingSounds.values()) {
            if (isound1.getSoundLocation().equals(new ResourceLocation(p_189567_1_))) {
               this.stopSound(isound1);
            }
         }
      } else {
         this.stopAllSounds();
      }

   }

   @SideOnly(Side.CLIENT)
   class SoundSystemStarterThread extends SoundSystem {
      private SoundSystemStarterThread() {
      }

      public boolean playing(String var1) {
         synchronized(SoundSystemConfig.THREAD_SYNC) {
            if (this.soundLibrary == null) {
               return false;
            } else {
               Source source = (Source)this.soundLibrary.getSources().get(p_playing_1_);
               return source == null ? false : source.playing() || source.paused() || source.preLoad;
            }
         }
      }
   }
}
