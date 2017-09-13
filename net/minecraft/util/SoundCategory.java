package net.minecraft.util;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;

public enum SoundCategory {
   MASTER("master"),
   MUSIC("music"),
   RECORDS("record"),
   WEATHER("weather"),
   BLOCKS("block"),
   HOSTILE("hostile"),
   NEUTRAL("neutral"),
   PLAYERS("player"),
   AMBIENT("ambient"),
   VOICE("voice");

   private static final Map SOUND_CATEGORIES = Maps.newHashMap();
   private final String name;

   private SoundCategory(String var3) {
      this.name = var3;
   }

   public String getName() {
      return this.name;
   }

   public static SoundCategory getByName(String var0) {
      return (SoundCategory)SOUND_CATEGORIES.get(var0);
   }

   public static Set getSoundCategoryNames() {
      return SOUND_CATEGORIES.keySet();
   }

   static {
      for(SoundCategory var3 : values()) {
         if (SOUND_CATEGORIES.containsKey(var3.getName())) {
            throw new Error("Clash in Sound Category name pools! Cannot insert " + var3);
         }

         SOUND_CATEGORIES.put(var3.getName(), var3);
      }

   }
}
