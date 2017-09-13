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

   public PlayerProfileCache(GameProfileRepository var1, File var2) {
      this.profileRepo = var1;
      this.usercacheFile = var2;
      GsonBuilder var3 = new GsonBuilder();
      var3.registerTypeHierarchyAdapter(PlayerProfileCache.ProfileEntry.class, new PlayerProfileCache.Serializer((Object)null));
      this.gson = var3.create();
      this.load();
   }

   private static GameProfile lookupProfile(GameProfileRepository var0, String var1) {
      final GameProfile[] var2 = new GameProfile[1];
      ProfileLookupCallback var3 = new ProfileLookupCallback() {
         public void onProfileLookupSucceeded(GameProfile var1) {
            var2[0] = var1;
         }

         public void onProfileLookupFailed(GameProfile var1, Exception var2x) {
            var2[0] = null;
         }
      };
      var0.findProfilesByNames(new String[]{var1}, Agent.MINECRAFT, var3);
      if (!isOnlineMode() && var2[0] == null) {
         UUID var4 = EntityPlayer.getUUID(new GameProfile((UUID)null, var1));
         GameProfile var5 = new GameProfile(var4, var1);
         var3.onProfileLookupSucceeded(var5);
      }

      return var2[0];
   }

   public static void setOnlineMode(boolean var0) {
      onlineMode = var0;
   }

   private static boolean isOnlineMode() {
      return onlineMode;
   }

   public void addEntry(GameProfile var1) {
      this.addEntry(var1, (Date)null);
   }

   private void addEntry(GameProfile var1, Date var2) {
      UUID var3 = var1.getId();
      if (var2 == null) {
         Calendar var4 = Calendar.getInstance();
         var4.setTime(new Date());
         var4.add(2, 1);
         var2 = var4.getTime();
      }

      var1.getName().toLowerCase(Locale.ROOT);
      PlayerProfileCache.ProfileEntry var6 = new PlayerProfileCache.ProfileEntry(var1, var2, (Object)null);
      if (this.uuidToProfileEntryMap.containsKey(var3)) {
         PlayerProfileCache.ProfileEntry var5 = (PlayerProfileCache.ProfileEntry)this.uuidToProfileEntryMap.get(var3);
         this.usernameToProfileEntryMap.remove(var5.getGameProfile().getName().toLowerCase(Locale.ROOT));
         this.gameProfiles.remove(var1);
      }

      this.usernameToProfileEntryMap.put(var1.getName().toLowerCase(Locale.ROOT), var6);
      this.uuidToProfileEntryMap.put(var3, var6);
      this.gameProfiles.addFirst(var1);
      this.save();
   }

   @Nullable
   public GameProfile getGameProfileForUsername(String var1) {
      String var2 = var1.toLowerCase(Locale.ROOT);
      PlayerProfileCache.ProfileEntry var3 = (PlayerProfileCache.ProfileEntry)this.usernameToProfileEntryMap.get(var2);
      if (var3 != null && (new Date()).getTime() >= var3.expirationDate.getTime()) {
         this.uuidToProfileEntryMap.remove(var3.getGameProfile().getId());
         this.usernameToProfileEntryMap.remove(var3.getGameProfile().getName().toLowerCase(Locale.ROOT));
         this.gameProfiles.remove(var3.getGameProfile());
         var3 = null;
      }

      if (var3 != null) {
         GameProfile var4 = var3.getGameProfile();
         this.gameProfiles.remove(var4);
         this.gameProfiles.addFirst(var4);
      } else {
         GameProfile var5 = lookupProfile(this.profileRepo, var2);
         if (var5 != null) {
            this.addEntry(var5);
            var3 = (PlayerProfileCache.ProfileEntry)this.usernameToProfileEntryMap.get(var2);
         }
      }

      this.save();
      return var3 == null ? null : var3.getGameProfile();
   }

   public String[] getUsernames() {
      ArrayList var1 = Lists.newArrayList(this.usernameToProfileEntryMap.keySet());
      return (String[])var1.toArray(new String[var1.size()]);
   }

   @Nullable
   public GameProfile getProfileByUUID(UUID var1) {
      PlayerProfileCache.ProfileEntry var2 = (PlayerProfileCache.ProfileEntry)this.uuidToProfileEntryMap.get(var1);
      return var2 == null ? null : var2.getGameProfile();
   }

   private PlayerProfileCache.ProfileEntry getByUUID(UUID var1) {
      PlayerProfileCache.ProfileEntry var2 = (PlayerProfileCache.ProfileEntry)this.uuidToProfileEntryMap.get(var1);
      if (var2 != null) {
         GameProfile var3 = var2.getGameProfile();
         this.gameProfiles.remove(var3);
         this.gameProfiles.addFirst(var3);
      }

      return var2;
   }

   public void load() {
      BufferedReader var1 = null;

      try {
         var1 = Files.newReader(this.usercacheFile, Charsets.UTF_8);
         List var2 = (List)this.gson.fromJson(var1, TYPE);
         this.usernameToProfileEntryMap.clear();
         this.uuidToProfileEntryMap.clear();
         this.gameProfiles.clear();
         if (var2 != null) {
            for(PlayerProfileCache.ProfileEntry var4 : Lists.reverse(var2)) {
               if (var4 != null) {
                  this.addEntry(var4.getGameProfile(), var4.getExpirationDate());
               }
            }
         }
      } catch (FileNotFoundException var9) {
         ;
      } catch (JsonParseException var10) {
         ;
      } finally {
         IOUtils.closeQuietly(var1);
      }

   }

   public void save() {
      String var1 = this.gson.toJson(this.getEntriesWithLimit(1000));
      BufferedWriter var2 = null;

      try {
         var2 = Files.newWriter(this.usercacheFile, Charsets.UTF_8);
         var2.write(var1);
         return;
      } catch (FileNotFoundException var7) {
         ;
      } catch (IOException var8) {
         return;
      } finally {
         IOUtils.closeQuietly(var2);
      }

   }

   private List getEntriesWithLimit(int var1) {
      ArrayList var2 = Lists.newArrayList();

      for(GameProfile var5 : Lists.newArrayList(Iterators.limit(this.gameProfiles.iterator(), var1))) {
         PlayerProfileCache.ProfileEntry var6 = this.getByUUID(var5.getId());
         if (var6 != null) {
            var2.add(var6);
         }
      }

      return var2;
   }

   class ProfileEntry {
      private final GameProfile gameProfile;
      private final Date expirationDate;

      private ProfileEntry(GameProfile var2, Date var3) {
         this.gameProfile = var2;
         this.expirationDate = var3;
      }

      public GameProfile getGameProfile() {
         return this.gameProfile;
      }

      public Date getExpirationDate() {
         return this.expirationDate;
      }

      ProfileEntry(GameProfile var2, Date var3, Object var4) {
         this(var2, var3);
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

      Serializer(Object var2) {
         this();
      }
   }
}
