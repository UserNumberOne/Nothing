package net.minecraft.server.management;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.commons.io.IOUtils;

public class PlayerProfileCache {
   public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
   private static boolean onlineMode;
   private final Map usernameToProfileEntryMap = Maps.newHashMap();
   private final Map uuidToProfileEntryMap = Maps.newHashMap();
   private final Deque gameProfiles = new LinkedBlockingDeque();
   private final GameProfileRepository profileRepo;
   protected final Gson gson;
   private final File usercacheFile;
   private static final ParameterizedType TYPE = new ParameterizedType() {
      public Type[] getActualTypeArguments() {
         return new Type[]{PlayerProfileCache.ProfileEntry.class};
      }

      public Type getRawType() {
         return List.class;
      }

      public Type getOwnerType() {
         return null;
      }
   };

   public PlayerProfileCache(GameProfileRepository gameprofilerepository, File file) {
      this.profileRepo = gameprofilerepository;
      this.usercacheFile = file;
      GsonBuilder gsonbuilder = new GsonBuilder();
      gsonbuilder.registerTypeHierarchyAdapter(PlayerProfileCache.ProfileEntry.class, new PlayerProfileCache.Serializer((Object)null));
      this.gson = gsonbuilder.create();
      this.load();
   }

   private static GameProfile lookupProfile(GameProfileRepository gameprofilerepository, String s) {
      final GameProfile[] agameprofile = new GameProfile[1];
      ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback() {
         public void onProfileLookupSucceeded(GameProfile gameprofile) {
            agameprofile[0] = gameprofile;
         }

         public void onProfileLookupFailed(GameProfile gameprofile, Exception exception) {
            agameprofile[0] = null;
         }
      };
      gameprofilerepository.findProfilesByNames(new String[]{s}, Agent.MINECRAFT, profilelookupcallback);
      if (!isOnlineMode() && agameprofile[0] == null) {
         UUID uuid = EntityPlayer.getUUID(new GameProfile((UUID)null, s));
         GameProfile gameprofile = new GameProfile(uuid, s);
         profilelookupcallback.onProfileLookupSucceeded(gameprofile);
      }

      return agameprofile[0];
   }

   public static void setOnlineMode(boolean flag) {
      onlineMode = flag;
   }

   private static boolean isOnlineMode() {
      return onlineMode;
   }

   public void addEntry(GameProfile gameprofile) {
      this.addEntry(gameprofile, (Date)null);
   }

   private void addEntry(GameProfile gameprofile, Date date) {
      UUID uuid = gameprofile.getId();
      if (date == null) {
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(new Date());
         calendar.add(2, 1);
         date = calendar.getTime();
      }

      gameprofile.getName().toLowerCase(Locale.ROOT);
      PlayerProfileCache.ProfileEntry usercache_usercacheentry = new PlayerProfileCache.ProfileEntry(gameprofile, date, (Object)null);
      if (this.uuidToProfileEntryMap.containsKey(uuid)) {
         PlayerProfileCache.ProfileEntry usercache_usercacheentry1 = (PlayerProfileCache.ProfileEntry)this.uuidToProfileEntryMap.get(uuid);
         this.usernameToProfileEntryMap.remove(usercache_usercacheentry1.getGameProfile().getName().toLowerCase(Locale.ROOT));
         this.gameProfiles.remove(gameprofile);
      }

      this.usernameToProfileEntryMap.put(gameprofile.getName().toLowerCase(Locale.ROOT), usercache_usercacheentry);
      this.uuidToProfileEntryMap.put(uuid, usercache_usercacheentry);
      this.gameProfiles.addFirst(gameprofile);
      this.save();
   }

   @Nullable
   public GameProfile getGameProfileForUsername(String s) {
      String s1 = s.toLowerCase(Locale.ROOT);
      PlayerProfileCache.ProfileEntry usercache_usercacheentry = (PlayerProfileCache.ProfileEntry)this.usernameToProfileEntryMap.get(s1);
      if (usercache_usercacheentry != null && (new Date()).getTime() >= usercache_usercacheentry.expirationDate.getTime()) {
         this.uuidToProfileEntryMap.remove(usercache_usercacheentry.getGameProfile().getId());
         this.usernameToProfileEntryMap.remove(usercache_usercacheentry.getGameProfile().getName().toLowerCase(Locale.ROOT));
         this.gameProfiles.remove(usercache_usercacheentry.getGameProfile());
         usercache_usercacheentry = null;
      }

      if (usercache_usercacheentry != null) {
         GameProfile gameprofile = usercache_usercacheentry.getGameProfile();
         this.gameProfiles.remove(gameprofile);
         this.gameProfiles.addFirst(gameprofile);
      } else {
         GameProfile gameprofile = lookupProfile(this.profileRepo, s1);
         if (gameprofile != null) {
            this.addEntry(gameprofile);
            usercache_usercacheentry = (PlayerProfileCache.ProfileEntry)this.usernameToProfileEntryMap.get(s1);
         }
      }

      this.save();
      return usercache_usercacheentry == null ? null : usercache_usercacheentry.getGameProfile();
   }

   public String[] getUsernames() {
      ArrayList arraylist = Lists.newArrayList(this.usernameToProfileEntryMap.keySet());
      return (String[])arraylist.toArray(new String[arraylist.size()]);
   }

   @Nullable
   public GameProfile getProfileByUUID(UUID uuid) {
      PlayerProfileCache.ProfileEntry usercache_usercacheentry = (PlayerProfileCache.ProfileEntry)this.uuidToProfileEntryMap.get(uuid);
      return usercache_usercacheentry == null ? null : usercache_usercacheentry.getGameProfile();
   }

   private PlayerProfileCache.ProfileEntry getByUUID(UUID uuid) {
      PlayerProfileCache.ProfileEntry usercache_usercacheentry = (PlayerProfileCache.ProfileEntry)this.uuidToProfileEntryMap.get(uuid);
      if (usercache_usercacheentry != null) {
         GameProfile gameprofile = usercache_usercacheentry.getGameProfile();
         this.gameProfiles.remove(gameprofile);
         this.gameProfiles.addFirst(gameprofile);
      }

      return usercache_usercacheentry;
   }

   public void load() {
      BufferedReader bufferedreader = null;

      try {
         bufferedreader = Files.newReader(this.usercacheFile, Charsets.UTF_8);
         List list = (List)this.gson.fromJson(bufferedreader, TYPE);
         this.usernameToProfileEntryMap.clear();
         this.uuidToProfileEntryMap.clear();
         this.gameProfiles.clear();
         if (list != null) {
            for(PlayerProfileCache.ProfileEntry usercache_usercacheentry : Lists.reverse(list)) {
               if (usercache_usercacheentry != null) {
                  this.addEntry(usercache_usercacheentry.getGameProfile(), usercache_usercacheentry.getExpirationDate());
               }
            }
         }
      } catch (FileNotFoundException var9) {
         ;
      } catch (JsonParseException var10) {
         ;
      } finally {
         IOUtils.closeQuietly(bufferedreader);
      }

   }

   public void save() {
      String s = this.gson.toJson(this.getEntriesWithLimit(1000));
      BufferedWriter bufferedwriter = null;

      try {
         bufferedwriter = Files.newWriter(this.usercacheFile, Charsets.UTF_8);
         bufferedwriter.write(s);
         return;
      } catch (FileNotFoundException var7) {
         ;
      } catch (IOException var8) {
         return;
      } finally {
         IOUtils.closeQuietly(bufferedwriter);
      }

   }

   private List getEntriesWithLimit(int i) {
      ArrayList arraylist = Lists.newArrayList();

      for(GameProfile gameprofile : Lists.newArrayList(Iterators.limit(this.gameProfiles.iterator(), i))) {
         PlayerProfileCache.ProfileEntry usercache_usercacheentry = this.getByUUID(gameprofile.getId());
         if (usercache_usercacheentry != null) {
            arraylist.add(usercache_usercacheentry);
         }
      }

      return arraylist;
   }

   class ProfileEntry {
      private final GameProfile gameProfile;
      private final Date expirationDate;

      private ProfileEntry(GameProfile gameprofile, Date date) {
         this.gameProfile = gameprofile;
         this.expirationDate = date;
      }

      public GameProfile getGameProfile() {
         return this.gameProfile;
      }

      public Date getExpirationDate() {
         return this.expirationDate;
      }

      ProfileEntry(GameProfile gameprofile, Date date, Object object) {
         this(gameprofile, date);
      }
   }

   class Serializer implements JsonDeserializer, JsonSerializer {
      private Serializer() {
      }

      public JsonElement serialize(PlayerProfileCache.ProfileEntry param1, Type param2, JsonSerializationContext param3) {
         // $FF: Couldn't be decompiled
      }

      public PlayerProfileCache.ProfileEntry deserialize(JsonElement param1, Type param2, JsonDeserializationContext param3) throws JsonParseException {
         // $FF: Couldn't be decompiled
      }

      public JsonElement serialize(PlayerProfileCache.ProfileEntry param1, Type param2, JsonSerializationContext param3) {
         // $FF: Couldn't be decompiled
      }

      public PlayerProfileCache.ProfileEntry deserialize(JsonElement param1, Type param2, JsonDeserializationContext param3) throws JsonParseException {
         // $FF: Couldn't be decompiled
      }

      Serializer(Object object) {
         this();
      }
   }
}
