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
      this.mcResourceManager = var1;
      this.sndManager = new SoundManager(this, var2);
   }

   public void onResourceManagerReload(IResourceManager var1) {
      this.soundRegistry.clearMap();

      for(String var3 : var1.getResourceDomains()) {
         try {
            for(IResource var5 : var1.getAllResources(new ResourceLocation(var3, "sounds.json"))) {
               try {
                  Map var6 = this.getSoundMap(var5.getInputStream());

                  for(Entry var8 : var6.entrySet()) {
                     this.loadSoundResource(new ResourceLocation(var3, (String)var8.getKey()), (SoundList)var8.getValue());
                  }
               } catch (RuntimeException var9) {
                  LOGGER.warn("Invalid sounds.json", var9);
               }
            }
         } catch (IOException var10) {
            ;
         }
      }

      for(ResourceLocation var13 : this.soundRegistry.getKeys()) {
         SoundEventAccessor var15 = (SoundEventAccessor)this.soundRegistry.getObject(var13);
         if (var15.getSubtitle() instanceof TextComponentTranslation) {
            String var16 = ((TextComponentTranslation)var15.getSubtitle()).getKey();
            if (!I18n.hasKey(var16)) {
               LOGGER.debug("Missing subtitle {} for event: {}", new Object[]{var16, var13});
            }
         }
      }

      for(ResourceLocation var14 : this.soundRegistry.getKeys()) {
         if (SoundEvent.REGISTRY.getObject(var14) == null) {
            LOGGER.debug("Not having sound event for: {}", new Object[]{var14});
         }
      }

      this.sndManager.reloadSoundSystem();
   }

   protected Map getSoundMap(InputStream var1) {
      Map var2;
      try {
         var2 = (Map)GSON.fromJson(new InputStreamReader(var1), TYPE);
      } finally {
         IOUtils.closeQuietly(var1);
      }

      return var2;
   }

   private void loadSoundResource(ResourceLocation var1, SoundList var2) {
      SoundEventAccessor var3 = (SoundEventAccessor)this.soundRegistry.getObject(var1);
      boolean var4 = var3 == null;
      if (var4 || var2.canReplaceExisting()) {
         if (!var4) {
            LOGGER.debug("Replaced sound event location {}", new Object[]{var1});
         }

         var3 = new SoundEventAccessor(var1, var2.getSubtitle());
         this.soundRegistry.add(var3);
      }

      for(Sound var6 : var2.getSounds()) {
         final ResourceLocation var7 = var6.getSoundLocation();
         Object var8;
         switch(var6.getType()) {
         case FILE:
            if (!this.validateSoundResource(var6, var1)) {
               continue;
            }

            var8 = var6;
            break;
         case SOUND_EVENT:
            var8 = new ISoundEventAccessor() {
               public int getWeight() {
                  SoundEventAccessor var1 = (SoundEventAccessor)SoundHandler.this.soundRegistry.getObject(var7);
                  return var1 == null ? 0 : var1.getWeight();
               }

               public Sound cloneEntry() {
                  SoundEventAccessor var1 = (SoundEventAccessor)SoundHandler.this.soundRegistry.getObject(var7);
                  return var1 == null ? SoundHandler.MISSING_SOUND : var1.cloneEntry();
               }
            };
            break;
         default:
            throw new IllegalStateException("Unknown SoundEventRegistration type: " + var6.getType());
         }

         var3.addSound((ISoundEventAccessor)var8);
      }

   }

   private boolean validateSoundResource(Sound var1, ResourceLocation var2) {
      ResourceLocation var3 = var1.getSoundAsOggLocation();
      IResource var4 = null;

      boolean var15;
      try {
         var4 = this.mcResourceManager.getResource(var3);
         var4.getInputStream();
         boolean var6 = true;
         return var6;
      } catch (FileNotFoundException var12) {
         LOGGER.warn("File {} does not exist, cannot add it to event {}", new Object[]{var3, var2});
         var15 = false;
      } catch (IOException var13) {
         LOGGER.warn("Could not load sound file {}, cannot add it to event {}", new Object[]{var3, var2, var13});
         var15 = false;
         boolean var7 = var15;
         return var7;
      } finally {
         IOUtils.closeQuietly(var4);
      }

      return var15;
   }

   @Nullable
   public SoundEventAccessor getAccessor(ResourceLocation var1) {
      return (SoundEventAccessor)this.soundRegistry.getObject(var1);
   }

   public void playSound(ISound var1) {
      this.sndManager.playSound(var1);
   }

   public void playDelayedSound(ISound var1, int var2) {
      this.sndManager.playDelayedSound(var1, var2);
   }

   public void setListener(EntityPlayer var1, float var2) {
      this.sndManager.setListener(var1, var2);
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
      if (var1 == SoundCategory.MASTER && var2 <= 0.0F) {
         this.stopSounds();
      }

      this.sndManager.setVolume(var1, var2);
   }

   public void stopSound(ISound var1) {
      this.sndManager.stopSound(var1);
   }

   public boolean isSoundPlaying(ISound var1) {
      return this.sndManager.isSoundPlaying(var1);
   }

   public void addListener(ISoundEventListener var1) {
      this.sndManager.addListener(var1);
   }

   public void removeListener(ISoundEventListener var1) {
      this.sndManager.removeListener(var1);
   }

   public void stop(String var1, SoundCategory var2) {
      this.sndManager.stop(var1, var2);
   }
}
