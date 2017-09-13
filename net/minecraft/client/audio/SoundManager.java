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
      this.sndHandler = var1;
      this.options = var2;

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

      for(SoundEvent var2 : SoundEvent.REGISTRY) {
         ResourceLocation var3 = var2.getSoundName();
         if (this.sndHandler.getAccessor(var3) == null) {
            LOGGER.warn("Missing sound for event: {}", new Object[]{SoundEvent.REGISTRY.getNameForObject(var2)});
            UNABLE_TO_PLAY.add(var3);
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
                        if (!var1.isEmpty()) {
                           SoundManager.LOGGER.info(var1);
                        }

                     }

                     public void importantMessage(String var1, int var2) {
                        if (!var1.isEmpty()) {
                           SoundManager.LOGGER.warn(var1);
                        }

                     }

                     public void errorMessage(String var1, String var2, int var3) {
                        if (!var2.isEmpty()) {
                           SoundManager.LOGGER.error("Error in class '{}'", new Object[]{var1});
                           SoundManager.LOGGER.error(var2);
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
      return var1 != null && var1 != SoundCategory.MASTER ? this.options.getSoundLevel(var1) : 1.0F;
   }

   public void setVolume(SoundCategory var1, float var2) {
      if (this.loaded) {
         if (var1 == SoundCategory.MASTER) {
            this.sndSystem.setMasterVolume(var2);
         } else {
            for(String var4 : this.categorySounds.get(var1)) {
               ISound var5 = (ISound)this.playingSounds.get(var4);
               float var6 = this.getClampedVolume(var5);
               if (var6 <= 0.0F) {
                  this.stopSound(var5);
               } else {
                  this.sndSystem.setVolume(var4, var6);
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
         for(String var2 : this.playingSounds.keySet()) {
            this.sndSystem.stop(var2);
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
      this.listeners.add(var1);
   }

   public void removeListener(ISoundEventListener var1) {
      this.listeners.remove(var1);
   }

   public void updateAllSounds() {
      ++this.playTime;

      for(ITickableSound var2 : this.tickableSounds) {
         var2.update();
         if (var2.isDonePlaying()) {
            this.stopSound(var2);
         } else {
            String var3 = (String)this.invPlayingSounds.get(var2);
            this.sndSystem.setVolume(var3, this.getClampedVolume(var2));
            this.sndSystem.setPitch(var3, this.getClampedPitch(var2));
            this.sndSystem.setPosition(var3, var2.getXPosF(), var2.getYPosF(), var2.getZPosF());
         }
      }

      Iterator var9 = this.playingSounds.entrySet().iterator();

      while(var9.hasNext()) {
         Entry var10 = (Entry)var9.next();
         String var12 = (String)var10.getKey();
         ISound var4 = (ISound)var10.getValue();
         if (!this.sndSystem.playing(var12)) {
            int var5 = ((Integer)this.playingSoundsStopTime.get(var12)).intValue();
            if (var5 <= this.playTime) {
               int var6 = var4.getRepeatDelay();
               if (var4.canRepeat() && var6 > 0) {
                  this.delayedSounds.put(var4, Integer.valueOf(this.playTime + var6));
               }

               var9.remove();
               LOGGER.debug(LOG_MARKER, "Removed channel {} because it's not playing anymore", new Object[]{var12});
               this.sndSystem.removeSource(var12);
               this.playingSoundsStopTime.remove(var12);

               try {
                  this.categorySounds.remove(var4.getCategory(), var12);
               } catch (RuntimeException var8) {
                  ;
               }

               if (var4 instanceof ITickableSound) {
                  this.tickableSounds.remove(var4);
               }
            }
         }
      }

      Iterator var11 = this.delayedSounds.entrySet().iterator();

      while(var11.hasNext()) {
         Entry var13 = (Entry)var11.next();
         if (this.playTime >= ((Integer)var13.getValue()).intValue()) {
            ISound var14 = (ISound)var13.getKey();
            if (var14 instanceof ITickableSound) {
               ((ITickableSound)var14).update();
            }

            this.playSound(var14);
            var11.remove();
         }
      }

   }

   public boolean isSoundPlaying(ISound var1) {
      if (!this.loaded) {
         return false;
      } else {
         String var2 = (String)this.invPlayingSounds.get(var1);
         return var2 == null ? false : this.sndSystem.playing(var2) || this.playingSoundsStopTime.containsKey(var2) && ((Integer)this.playingSoundsStopTime.get(var2)).intValue() <= this.playTime;
      }
   }

   public void stopSound(ISound var1) {
      if (this.loaded) {
         String var2 = (String)this.invPlayingSounds.get(var1);
         if (var2 != null) {
            this.sndSystem.stop(var2);
         }
      }

   }

   public void playSound(ISound var1) {
      if (this.loaded) {
         var1 = ForgeHooksClient.playSound(this, var1);
         if (var1 == null) {
            return;
         }

         SoundEventAccessor var2 = var1.createAccessor(this.sndHandler);
         ResourceLocation var3 = var1.getSoundLocation();
         if (var2 == null) {
            if (UNABLE_TO_PLAY.add(var3)) {
               LOGGER.warn(LOG_MARKER, "Unable to play unknown soundEvent: {}", new Object[]{var3});
            }
         } else {
            if (!this.listeners.isEmpty()) {
               for(ISoundEventListener var5 : this.listeners) {
                  var5.soundPlay(var1, var2);
               }
            }

            if (this.sndSystem.getMasterVolume() <= 0.0F) {
               LOGGER.debug(LOG_MARKER, "Skipped playing soundEvent: {}, master volume was zero", new Object[]{var3});
            } else {
               Sound var14 = var1.getSound();
               if (var14 == SoundHandler.MISSING_SOUND) {
                  if (UNABLE_TO_PLAY.add(var3)) {
                     LOGGER.warn(LOG_MARKER, "Unable to play empty soundEvent: {}", new Object[]{var3});
                  }
               } else {
                  float var15 = var1.getVolume();
                  float var6 = 16.0F;
                  if (var15 > 1.0F) {
                     var6 *= var15;
                  }

                  SoundCategory var7 = var1.getCategory();
                  float var8 = this.getClampedVolume(var1);
                  float var9 = this.getClampedPitch(var1);
                  if (var8 == 0.0F) {
                     LOGGER.debug(LOG_MARKER, "Skipped playing sound {}, volume was zero.", new Object[]{var14.getSoundLocation()});
                  } else {
                     boolean var10 = var1.canRepeat() && var1.getRepeatDelay() == 0;
                     String var11 = MathHelper.getRandomUUID(ThreadLocalRandom.current()).toString();
                     ResourceLocation var12 = var14.getSoundAsOggLocation();
                     if (var14.isStreaming()) {
                        this.sndSystem.newStreamingSource(false, var11, getURLForSoundResource(var12), var12.toString(), var10, var1.getXPosF(), var1.getYPosF(), var1.getZPosF(), var1.getAttenuationType().getTypeInt(), var6);
                        MinecraftForge.EVENT_BUS.post(new PlayStreamingSourceEvent(this, var1, var11));
                     } else {
                        this.sndSystem.newSource(false, var11, getURLForSoundResource(var12), var12.toString(), var10, var1.getXPosF(), var1.getYPosF(), var1.getZPosF(), var1.getAttenuationType().getTypeInt(), var6);
                        MinecraftForge.EVENT_BUS.post(new PlaySoundSourceEvent(this, var1, var11));
                     }

                     LOGGER.debug(LOG_MARKER, "Playing sound {} for event {} as channel {}", new Object[]{var14.getSoundLocation(), var12, var11});
                     this.sndSystem.setPitch(var11, var9);
                     this.sndSystem.setVolume(var11, var8);
                     this.sndSystem.play(var11);
                     this.playingSoundsStopTime.put(var11, Integer.valueOf(this.playTime + 20));
                     this.playingSounds.put(var11, var1);
                     if (var7 != SoundCategory.MASTER) {
                        this.categorySounds.put(var7, var11);
                     }

                     if (var1 instanceof ITickableSound) {
                        this.tickableSounds.add((ITickableSound)var1);
                     }
                  }
               }
            }
         }
      }

   }

   private float getClampedPitch(ISound var1) {
      return MathHelper.clamp(var1.getPitch(), 0.5F, 2.0F);
   }

   private float getClampedVolume(ISound var1) {
      return MathHelper.clamp(var1.getVolume() * this.getVolume(var1.getCategory()), 0.0F, 1.0F);
   }

   public void pauseAllSounds() {
      for(Entry var2 : this.playingSounds.entrySet()) {
         String var3 = (String)var2.getKey();
         boolean var4 = this.isSoundPlaying((ISound)var2.getValue());
         if (var4) {
            LOGGER.debug(LOG_MARKER, "Pausing channel {}", new Object[]{var3});
            this.sndSystem.pause(var3);
            this.pausedChannels.add(var3);
         }
      }

   }

   public void resumeAllSounds() {
      for(String var2 : this.pausedChannels) {
         LOGGER.debug(LOG_MARKER, "Resuming channel {}", new Object[]{var2});
         this.sndSystem.play(var2);
      }

      this.pausedChannels.clear();
   }

   public void playDelayedSound(ISound var1, int var2) {
      this.delayedSounds.put(var1, Integer.valueOf(this.playTime + var2));
   }

   private static URL getURLForSoundResource(final ResourceLocation var0) {
      String var1 = String.format("%s:%s:%s", "mcsounddomain", var0.getResourceDomain(), var0.getResourcePath());
      URLStreamHandler var2 = new URLStreamHandler() {
         protected URLConnection openConnection(URL var1) {
            return new URLConnection(var1) {
               public void connect() throws IOException {
               }

               public InputStream getInputStream() throws IOException {
                  return Minecraft.getMinecraft().getResourceManager().getResource(var0).getInputStream();
               }
            };
         }
      };

      try {
         return new URL((URL)null, var1, var2);
      } catch (MalformedURLException var4) {
         throw new Error("TODO: Sanely handle url exception! :D");
      }
   }

   public void setListener(EntityPlayer var1, float var2) {
      if (this.loaded && var1 != null) {
         float var3 = var1.prevRotationPitch + (var1.rotationPitch - var1.prevRotationPitch) * var2;
         float var4 = var1.prevRotationYaw + (var1.rotationYaw - var1.prevRotationYaw) * var2;
         double var5 = var1.prevPosX + (var1.posX - var1.prevPosX) * (double)var2;
         double var7 = var1.prevPosY + (var1.posY - var1.prevPosY) * (double)var2 + (double)var1.getEyeHeight();
         double var9 = var1.prevPosZ + (var1.posZ - var1.prevPosZ) * (double)var2;
         float var11 = MathHelper.cos((var4 + 90.0F) * 0.017453292F);
         float var12 = MathHelper.sin((var4 + 90.0F) * 0.017453292F);
         float var13 = MathHelper.cos(-var3 * 0.017453292F);
         float var14 = MathHelper.sin(-var3 * 0.017453292F);
         float var15 = MathHelper.cos((-var3 + 90.0F) * 0.017453292F);
         float var16 = MathHelper.sin((-var3 + 90.0F) * 0.017453292F);
         float var17 = var11 * var13;
         float var18 = var12 * var13;
         float var19 = var11 * var15;
         float var20 = var12 * var15;
         this.sndSystem.setListenerPosition((float)var5, (float)var7, (float)var9);
         this.sndSystem.setListenerOrientation(var17, var14, var18, var19, var16, var20);
      }

   }

   public void stop(String var1, SoundCategory var2) {
      if (var2 != null) {
         for(String var4 : this.categorySounds.get(var2)) {
            ISound var5 = (ISound)this.playingSounds.get(var4);
            if (!var1.isEmpty()) {
               if (var5.getSoundLocation().equals(new ResourceLocation(var1))) {
                  this.stopSound(var5);
               }
            } else {
               this.stopSound(var5);
            }
         }
      } else if (!var1.isEmpty()) {
         for(ISound var7 : this.playingSounds.values()) {
            if (var7.getSoundLocation().equals(new ResourceLocation(var1))) {
               this.stopSound(var7);
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
               Source var3 = (Source)this.soundLibrary.getSources().get(var1);
               return var3 == null ? false : var3.playing() || var3.paused() || var3.preLoad;
            }
         }
      }
   }
}
