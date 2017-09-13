package net.minecraft.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Type;
import java.util.UUID;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.text.ITextComponent;

public class ServerStatusResponse {
   private ITextComponent description;
   private ServerStatusResponse.Players players;
   private ServerStatusResponse.Version version;
   private String favicon;

   public ITextComponent getServerDescription() {
      return this.description;
   }

   public void setServerDescription(ITextComponent var1) {
      this.description = var1;
   }

   public ServerStatusResponse.Players getPlayers() {
      return this.players;
   }

   public void setPlayers(ServerStatusResponse.Players var1) {
      this.players = var1;
   }

   public ServerStatusResponse.Version getVersion() {
      return this.version;
   }

   public void setVersion(ServerStatusResponse.Version var1) {
      this.version = var1;
   }

   public void setFavicon(String var1) {
      this.favicon = var1;
   }

   public String getFavicon() {
      return this.favicon;
   }

   public static class Players {
      private final int maxPlayers;
      private final int onlinePlayerCount;
      private GameProfile[] players;

      public Players(int var1, int var2) {
         this.maxPlayers = var1;
         this.onlinePlayerCount = var2;
      }

      public int getMaxPlayers() {
         return this.maxPlayers;
      }

      public int getOnlinePlayerCount() {
         return this.onlinePlayerCount;
      }

      public GameProfile[] getPlayers() {
         return this.players;
      }

      public void setPlayers(GameProfile[] var1) {
         this.players = var1;
      }

      public static class Serializer implements JsonDeserializer, JsonSerializer {
         public ServerStatusResponse.Players deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
            JsonObject var4 = JsonUtils.getJsonObject(var1, "players");
            ServerStatusResponse.Players var5 = new ServerStatusResponse.Players(JsonUtils.getInt(var4, "max"), JsonUtils.getInt(var4, "online"));
            if (JsonUtils.isJsonArray(var4, "sample")) {
               JsonArray var6 = JsonUtils.getJsonArray(var4, "sample");
               if (var6.size() > 0) {
                  GameProfile[] var7 = new GameProfile[var6.size()];

                  for(int var8 = 0; var8 < var7.length; ++var8) {
                     JsonObject var9 = JsonUtils.getJsonObject(var6.get(var8), "player[" + var8 + "]");
                     String var10 = JsonUtils.getString(var9, "id");
                     var7[var8] = new GameProfile(UUID.fromString(var10), JsonUtils.getString(var9, "name"));
                  }

                  var5.setPlayers(var7);
               }
            }

            return var5;
         }

         public JsonElement serialize(ServerStatusResponse.Players var1, Type var2, JsonSerializationContext var3) {
            JsonObject var4 = new JsonObject();
            var4.addProperty("max", Integer.valueOf(var1.getMaxPlayers()));
            var4.addProperty("online", Integer.valueOf(var1.getOnlinePlayerCount()));
            if (var1.getPlayers() != null && var1.getPlayers().length > 0) {
               JsonArray var5 = new JsonArray();

               for(int var6 = 0; var6 < var1.getPlayers().length; ++var6) {
                  JsonObject var7 = new JsonObject();
                  UUID var8 = var1.getPlayers()[var6].getId();
                  var7.addProperty("id", var8 == null ? "" : var8.toString());
                  var7.addProperty("name", var1.getPlayers()[var6].getName());
                  var5.add(var7);
               }

               var4.add("sample", var5);
            }

            return var4;
         }

         // $FF: synthetic method
         public JsonElement serialize(Object var1, Type var2, JsonSerializationContext var3) {
            return this.serialize((ServerStatusResponse.Players)var1, var2, var3);
         }

         // $FF: synthetic method
         public Object deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
            return this.deserialize(var1, var2, var3);
         }
      }
   }

   public static class Serializer implements JsonDeserializer, JsonSerializer {
      public ServerStatusResponse deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject var4 = JsonUtils.getJsonObject(var1, "status");
         ServerStatusResponse var5 = new ServerStatusResponse();
         if (var4.has("description")) {
            var5.setServerDescription((ITextComponent)var3.deserialize(var4.get("description"), ITextComponent.class));
         }

         if (var4.has("players")) {
            var5.setPlayers((ServerStatusResponse.Players)var3.deserialize(var4.get("players"), ServerStatusResponse.Players.class));
         }

         if (var4.has("version")) {
            var5.setVersion((ServerStatusResponse.Version)var3.deserialize(var4.get("version"), ServerStatusResponse.Version.class));
         }

         if (var4.has("favicon")) {
            var5.setFavicon(JsonUtils.getString(var4, "favicon"));
         }

         return var5;
      }

      public JsonElement serialize(ServerStatusResponse var1, Type var2, JsonSerializationContext var3) {
         JsonObject var4 = new JsonObject();
         if (var1.getServerDescription() != null) {
            var4.add("description", var3.serialize(var1.getServerDescription()));
         }

         if (var1.getPlayers() != null) {
            var4.add("players", var3.serialize(var1.getPlayers()));
         }

         if (var1.getVersion() != null) {
            var4.add("version", var3.serialize(var1.getVersion()));
         }

         if (var1.getFavicon() != null) {
            var4.addProperty("favicon", var1.getFavicon());
         }

         return var4;
      }

      // $FF: synthetic method
      public JsonElement serialize(Object var1, Type var2, JsonSerializationContext var3) {
         return this.serialize((ServerStatusResponse)var1, var2, var3);
      }

      // $FF: synthetic method
      public Object deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         return this.deserialize(var1, var2, var3);
      }
   }

   public static class Version {
      private final String name;
      private final int protocol;

      public Version(String var1, int var2) {
         this.name = var1;
         this.protocol = var2;
      }

      public String getName() {
         return this.name;
      }

      public int getProtocol() {
         return this.protocol;
      }

      public static class Serializer implements JsonDeserializer, JsonSerializer {
         public ServerStatusResponse.Version deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
            JsonObject var4 = JsonUtils.getJsonObject(var1, "version");
            return new ServerStatusResponse.Version(JsonUtils.getString(var4, "name"), JsonUtils.getInt(var4, "protocol"));
         }

         public JsonElement serialize(ServerStatusResponse.Version var1, Type var2, JsonSerializationContext var3) {
            JsonObject var4 = new JsonObject();
            var4.addProperty("name", var1.getName());
            var4.addProperty("protocol", Integer.valueOf(var1.getProtocol()));
            return var4;
         }

         // $FF: synthetic method
         public JsonElement serialize(Object var1, Type var2, JsonSerializationContext var3) {
            return this.serialize((ServerStatusResponse.Version)var1, var2, var3);
         }

         // $FF: synthetic method
         public Object deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
            return this.deserialize(var1, var2, var3);
         }
      }
   }
}
