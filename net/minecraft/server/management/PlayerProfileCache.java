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
import com.google.gson.JsonObject;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.commons.io.IOUtils;

public class PlayerProfileCache {
   public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
   private static boolean onlineMode;
   private final Map usernameToProfileEntryMap = Maps.newHashMap();
   private final Map uuidToProfileEntryMap = Maps.newHashMap();
   private final Deque gameProfiles = Lists.newLinkedList();
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
      var3.registerTypeHierarchyAdapter(PlayerProfileCache.ProfileEntry.class, new PlayerProfileCache.Serializer());
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

      String var7 = var1.getName().toLowerCase(Locale.ROOT);
      PlayerProfileCache.ProfileEntry var5 = new PlayerProfileCache.ProfileEntry(var1, var2);
      if (this.uuidToProfileEntryMap.containsKey(var3)) {
         PlayerProfileCache.ProfileEntry var6 = (PlayerProfileCache.ProfileEntry)this.uuidToProfileEntryMap.get(var3);
         this.usernameToProfileEntryMap.remove(var6.getGameProfile().getName().toLowerCase(Locale.ROOT));
         this.gameProfiles.remove(var1);
      }

      this.usernameToProfileEntryMap.put(var1.getName().toLowerCase(Locale.ROOT), var5);
      this.uuidToProfileEntryMap.put(var3, var5);
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
      } catch (FileNotFoundException var8) {
         ;
      } catch (IOException var9) {
         return;
      } finally {
         IOUtils.closeQuietly(var2);
      }

   }

   private List getEntriesWithLimit(int var1) {
      ArrayList var2 = Lists.newArrayList();

      for(GameProfile var4 : Lists.newArrayList(Iterators.limit(this.gameProfiles.iterator(), var1))) {
         PlayerProfileCache.ProfileEntry var5 = this.getByUUID(var4.getId());
         if (var5 != null) {
            var2.add(var5);
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
   }

   class Serializer implements JsonDeserializer, JsonSerializer {
      private Serializer() {
      }

      public JsonElement serialize(PlayerProfileCache.ProfileEntry var1, Type var2, JsonSerializationContext var3) {
         JsonObject var4 = new JsonObject();
         var4.addProperty("name", var1.getGameProfile().getName());
         UUID var5 = var1.getGameProfile().getId();
         var4.addProperty("uuid", var5 == null ? "" : var5.toString());
         var4.addProperty("expiresOn", PlayerProfileCache.DATE_FORMAT.format(var1.getExpirationDate()));
         return var4;
      }

      public PlayerProfileCache.ProfileEntry deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         if (var1.isJsonObject()) {
            JsonObject var4 = var1.getAsJsonObject();
            JsonElement var5 = var4.get("name");
            JsonElement var6 = var4.get("uuid");
            JsonElement var7 = var4.get("expiresOn");
            if (var5 != null && var6 != null) {
               String var8 = var6.getAsString();
               String var9 = var5.getAsString();
               Date var10 = null;
               if (var7 != null) {
                  try {
                     var10 = PlayerProfileCache.DATE_FORMAT.parse(var7.getAsString());
                  } catch (ParseException var14) {
                     var10 = null;
                  }
               }

               if (var9 != null && var8 != null) {
                  UUID var11;
                  try {
                     var11 = UUID.fromString(var8);
                  } catch (Throwable var13) {
                     return null;
                  }

                  PlayerProfileCache var10002 = PlayerProfileCache.this;
                  PlayerProfileCache.this.getClass();
                  return var10002.new ProfileEntry(new GameProfile(var11, var9), var10);
               } else {
                  return null;
               }
            } else {
               return null;
            }
         } else {
            return null;
         }
      }
   }
}
