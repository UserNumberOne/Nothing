package net.minecraft.client.audio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class SoundHandler implements IResourceManagerReloadListener, ITickable {
   public static final Sound MISSING_SOUND = new Sound("meta:missing_sound", 1.0F, 1.0F, 1, Sound.Type.FILE, false);
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = (new GsonBuilder()).registerTypeHierarchyAdapter(ITextComponent.class, new ITextComponent.Serializer()).registerTypeAdapter(SoundList.class, new SoundListSerializer()).create();
   private static final ParameterizedType TYPE = new ParameterizedType() {
      public Type[] getActualTypeArguments() {
         return new Type[]{String.class, SoundList.class};
      }

      public Type getRawType() {
         return Map.class;
      }

      public Type getOwnerType() {
         return null;
      }
   };
   private final SoundRegistry soundRegistry = new SoundRegistry();
   private final SoundManager sndManager;
   private final IResourceManager mcResourceManager;

   public SoundHandler(IResourceManager var1, GameSettings var2) {
      this.mcResourceManager = manager;
      this.sndManager = new SoundManager(this, gameSettingsIn);
   }

   public void onResourceManagerReload(IResourceManager var1) {
      this.soundRegistry.clearMap();

      for(String s : resourceManager.getResourceDomains()) {
         try {
            for(IResource iresource : resourceManager.getAllResources(new ResourceLocation(s, "sounds.json"))) {
               try {
                  Map map = this.getSoundMap(iresource.getInputStream());

                  for(Entry entry : map.entrySet()) {
                     this.loadSoundResource(new ResourceLocation(s, (String)entry.getKey()), (SoundList)entry.getValue());
                  }
               } catch (RuntimeException var9) {
                  LOGGER.warn("Invalid sounds.json", var9);
               }
            }
         } catch (IOException var10) {
            ;
         }
      }

      for(ResourceLocation resourcelocation : this.soundRegistry.getKeys()) {
         SoundEventAccessor soundeventaccessor = (SoundEventAccessor)this.soundRegistry.getObject(resourcelocation);
         if (soundeventaccessor.getSubtitle() instanceof TextComponentTranslation) {
            String s1 = ((TextComponentTranslation)soundeventaccessor.getSubtitle()).getKey();
            if (!I18n.hasKey(s1)) {
               LOGGER.debug("Missing subtitle {} for event: {}", new Object[]{s1, resourcelocation});
            }
         }
      }

      for(ResourceLocation resourcelocation1 : this.soundRegistry.getKeys()) {
         if (SoundEvent.REGISTRY.getObject(resourcelocation1) == null) {
            LOGGER.debug("Not having sound event for: {}", new Object[]{resourcelocation1});
         }
      }

      this.sndManager.reloadSoundSystem();
   }

   protected Map getSoundMap(InputStream var1) {
      Map map;
      try {
         map = (Map)GSON.fromJson(new InputStreamReader(stream), TYPE);
      } finally {
         IOUtils.closeQuietly(stream);
      }

      return map;
   }

   private void loadSoundResource(ResourceLocation var1, SoundList var2) {
      SoundEventAccessor soundeventaccessor = (SoundEventAccessor)this.soundRegistry.getObject(location);
      boolean flag = soundeventaccessor == null;
      if (flag || sounds.canReplaceExisting()) {
         if (!flag) {
            LOGGER.debug("Replaced sound event location {}", new Object[]{location});
         }

         soundeventaccessor = new SoundEventAccessor(location, sounds.getSubtitle());
         this.soundRegistry.add(soundeventaccessor);
      }

      for(Sound sound : sounds.getSounds()) {
         final ResourceLocation resourcelocation = sound.getSoundLocation();
         Object isoundeventaccessor;
         switch(sound.getType()) {
         case FILE:
            if (!this.validateSoundResource(sound, location)) {
               continue;
            }

            isoundeventaccessor = sound;
            break;
         case SOUND_EVENT:
            isoundeventaccessor = new ISoundEventAccessor() {
               public int getWeight() {
                  SoundEventAccessor soundeventaccessor1 = (SoundEventAccessor)SoundHandler.this.soundRegistry.getObject(resourcelocation);
                  return soundeventaccessor1 == null ? 0 : soundeventaccessor1.getWeight();
               }

               public Sound cloneEntry() {
                  SoundEventAccessor soundeventaccessor1 = (SoundEventAccessor)SoundHandler.this.soundRegistry.getObject(resourcelocation);
                  return soundeventaccessor1 == null ? SoundHandler.MISSING_SOUND : soundeventaccessor1.cloneEntry();
               }
            };
            break;
         default:
            throw new IllegalStateException("Unknown SoundEventRegistration type: " + sound.getType());
         }

         soundeventaccessor.addSound(isoundeventaccessor);
      }

   }

   private boolean validateSoundResource(Sound var1, ResourceLocation var2) {
      ResourceLocation resourcelocation = p_184401_1_.getSoundAsOggLocation();
      IResource iresource = null;

      boolean flag;
      try {
         iresource = this.mcResourceManager.getResource(resourcelocation);
         iresource.getInputStream();
         boolean var6 = true;
         return var6;
      } catch (FileNotFoundException var12) {
         LOGGER.warn("File {} does not exist, cannot add it to event {}", new Object[]{resourcelocation, p_184401_2_});
         flag = false;
      } catch (IOException var13) {
         LOGGER.warn("Could not load sound file {}, cannot add it to event {}", new Object[]{resourcelocation, p_184401_2_, var13});
         flag = false;
         boolean var7 = flag;
         return var7;
      } finally {
         IOUtils.closeQuietly(iresource);
      }

      return flag;
   }

   @Nullable
   public SoundEventAccessor getAccessor(ResourceLocation var1) {
      return (SoundEventAccessor)this.soundRegistry.getObject(location);
   }

   public void playSound(ISound var1) {
      this.sndManager.playSound(sound);
   }

   public void playDelayedSound(ISound var1, int var2) {
      this.sndManager.playDelayedSound(sound, delay);
   }

   public void setListener(EntityPlayer var1, float var2) {
      this.sndManager.setListener(player, p_147691_2_);
   }

   public void pauseSounds() {
      this.sndManager.pauseAllSounds();
   }

   public void stopSounds() {
      this.sndManager.stopAllSounds();
   }

   public void unloadSounds() {
      this.sndManager.unloadSoundSystem();
   }

   public void update() {
      this.sndManager.updateAllSounds();
   }

   public void resumeSounds() {
      this.sndManager.resumeAllSounds();
   }

   public void setSoundLevel(SoundCategory var1, float var2) {
      if (category == SoundCategory.MASTER && volume <= 0.0F) {
         this.stopSounds();
      }

      this.sndManager.setVolume(category, volume);
   }

   public void stopSound(ISound var1) {
      this.sndManager.stopSound(soundIn);
   }

   public boolean isSoundPlaying(ISound var1) {
      return this.sndManager.isSoundPlaying(sound);
   }

   public void addListener(ISoundEventListener var1) {
      this.sndManager.addListener(listener);
   }

   public void removeListener(ISoundEventListener var1) {
      this.sndManager.removeListener(listener);
   }

   public void stop(String var1, SoundCategory var2) {
      this.sndManager.stop(p_189520_1_, p_189520_2_);
   }
}
