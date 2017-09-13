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
      if (usernameIn == null || usernameIn.isEmpty()) {
         usernameIn = "MissingName";
         tokenIn = "NotValid";
         playerIDIn = "NotValid";
         Logger logger = FMLLog.getLogger();
         logger.log(Level.WARN, "=========================================================");
         logger.log(Level.WARN, "WARNING!! the username was not set for this session, typically");
         logger.log(Level.WARN, "this means you installed Forge incorrectly. We have set your");
         logger.log(Level.WARN, "name to \"MissingName\" and your session to nothing. Please");
         logger.log(Level.WARN, "check your installation and post a console log from the launcher");
         logger.log(Level.WARN, "when asking for help!");
         logger.log(Level.WARN, "=========================================================");
      }

      this.username = usernameIn;
      this.playerID = playerIDIn;
      this.token = tokenIn;
      this.sessionType = Session.Type.setSessionType(sessionTypeIn);
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
         UUID uuid = UUIDTypeAdapter.fromString(this.getPlayerID());
         GameProfile ret = new GameProfile(uuid, this.getUsername());
         if (this.properties != null) {
            ret.getProperties().putAll(this.properties);
         }

         return ret;
      } catch (IllegalArgumentException var3) {
         return new GameProfile(EntityPlayer.getUUID(new GameProfile((UUID)null, this.getUsername())), this.getUsername());
      }
   }

   public void setProperties(PropertyMap var1) {
      if (this.properties == null) {
         this.properties = properties;
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
         this.sessionType = sessionTypeIn;
      }

      @Nullable
      public static Session.Type setSessionType(String var0) {
         return (Session.Type)SESSION_TYPES.get(sessionTypeIn.toLowerCase());
      }

      static {
         for(Session.Type session$type : values()) {
            SESSION_TYPES.put(session$type.sessionType, session$type);
         }

      }
   }
}
