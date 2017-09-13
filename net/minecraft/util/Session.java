package net.minecraft.util;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.util.UUIDTypeAdapter;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class Session {
   private final String username;
   private final String playerID;
   private final String token;
   private final Session.Type sessionType;
   private PropertyMap properties;

   public Session(String var1, String var2, String var3, String var4) {
      if (var1 == null || var1.isEmpty()) {
         var1 = "MissingName";
         var3 = "NotValid";
         var2 = "NotValid";
         Logger var5 = FMLLog.getLogger();
         var5.log(Level.WARN, "=========================================================");
         var5.log(Level.WARN, "WARNING!! the username was not set for this session, typically");
         var5.log(Level.WARN, "this means you installed Forge incorrectly. We have set your");
         var5.log(Level.WARN, "name to \"MissingName\" and your session to nothing. Please");
         var5.log(Level.WARN, "check your installation and post a console log from the launcher");
         var5.log(Level.WARN, "when asking for help!");
         var5.log(Level.WARN, "=========================================================");
      }

      this.username = var1;
      this.playerID = var2;
      this.token = var3;
      this.sessionType = Session.Type.setSessionType(var4);
   }

   public String getSessionID() {
      return "token:" + this.token + ":" + this.playerID;
   }

   public String getPlayerID() {
      return this.playerID;
   }

   public String getUsername() {
      return this.username;
   }

   public String getToken() {
      return this.token;
   }

   public GameProfile getProfile() {
      try {
         UUID var1 = UUIDTypeAdapter.fromString(this.getPlayerID());
         GameProfile var2 = new GameProfile(var1, this.getUsername());
         if (this.properties != null) {
            var2.getProperties().putAll(this.properties);
         }

         return var2;
      } catch (IllegalArgumentException var3) {
         return new GameProfile(EntityPlayer.getUUID(new GameProfile((UUID)null, this.getUsername())), this.getUsername());
      }
   }

   public void setProperties(PropertyMap var1) {
      if (this.properties == null) {
         this.properties = var1;
      }

   }

   public boolean hasCachedProperties() {
      return this.properties != null;
   }

   @SideOnly(Side.CLIENT)
   public static enum Type {
      LEGACY("legacy"),
      MOJANG("mojang");

      private static final Map SESSION_TYPES = Maps.newHashMap();
      private final String sessionType;

      private Type(String var3) {
         this.sessionType = var3;
      }

      @Nullable
      public static Session.Type setSessionType(String var0) {
         return (Session.Type)SESSION_TYPES.get(var0.toLowerCase());
      }

      static {
         for(Session.Type var3 : values()) {
            SESSION_TYPES.put(var3.sessionType, var3);
         }

      }
   }
}
