package net.minecraft.server.dedicated;

import com.mojang.authlib.GameProfile;
import java.io.IOException;
import net.minecraft.server.management.PlayerList;
import net.minecraft.src.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DedicatedPlayerList extends PlayerList {
   private static final Logger LOGGER = LogManager.getLogger();

   public DedicatedPlayerList(DedicatedServer var1) {
      super(var1);
      this.setViewDistance(var1.getIntProperty("view-distance", 10));
      this.maxPlayers = var1.getIntProperty("max-players", 20);
      this.setWhiteListEnabled(var1.getBooleanProperty("white-list", false));
      if (!var1.R()) {
         this.getBannedPlayers().setLanServer(true);
         this.getBannedIPs().setLanServer(true);
      }

      this.loadPlayerBanList();
      this.savePlayerBanList();
      this.loadIPBanList();
      this.saveIPBanList();
      this.loadOpsList();
      this.readWhiteList();
      this.saveOpsList();
      if (!this.getWhitelistedPlayers().getSaveFile().exists()) {
         this.saveWhiteList();
      }

   }

   public void setWhiteListEnabled(boolean var1) {
      super.setWhiteListEnabled(var1);
      this.getServerInstance().setProperty("white-list", Boolean.valueOf(var1));
      this.getServerInstance().saveProperties();
   }

   public void addOp(GameProfile var1) {
      super.addOp(var1);
      this.saveOpsList();
   }

   public void removeOp(GameProfile var1) {
      super.removeOp(var1);
      this.saveOpsList();
   }

   public void removePlayerFromWhitelist(GameProfile var1) {
      super.removePlayerFromWhitelist(var1);
      this.saveWhiteList();
   }

   public void addWhitelistedPlayer(GameProfile var1) {
      super.addWhitelistedPlayer(var1);
      this.saveWhiteList();
   }

   public void reloadWhitelist() {
      this.readWhiteList();
   }

   private void saveIPBanList() {
      try {
         this.getBannedIPs().writeChanges();
      } catch (IOException var2) {
         LOGGER.warn("Failed to save ip banlist: ", var2);
      }

   }

   private void savePlayerBanList() {
      try {
         this.getBannedPlayers().writeChanges();
      } catch (IOException var2) {
         LOGGER.warn("Failed to save user banlist: ", var2);
      }

   }

   private void loadIPBanList() {
      try {
         this.getBannedIPs().readSavedFile();
      } catch (IOException var2) {
         LOGGER.warn("Failed to load ip banlist: ", var2);
      }

   }

   private void loadPlayerBanList() {
      try {
         this.getBannedPlayers().readSavedFile();
      } catch (IOException var2) {
         LOGGER.warn("Failed to load user banlist: ", var2);
      }

   }

   private void loadOpsList() {
      try {
         this.getOppedPlayers().readSavedFile();
      } catch (Exception var2) {
         LOGGER.warn("Failed to load operators list: ", var2);
      }

   }

   private void saveOpsList() {
      try {
         this.getOppedPlayers().writeChanges();
      } catch (Exception var2) {
         LOGGER.warn("Failed to save operators list: ", var2);
      }

   }

   private void readWhiteList() {
      try {
         this.getWhitelistedPlayers().readSavedFile();
      } catch (Exception var2) {
         LOGGER.warn("Failed to load white-list: ", var2);
      }

   }

   private void saveWhiteList() {
      try {
         this.getWhitelistedPlayers().writeChanges();
      } catch (Exception var2) {
         LOGGER.warn("Failed to save white-list: ", var2);
      }

   }

   public boolean canJoin(GameProfile var1) {
      return !this.isWhiteListEnabled() || this.canSendCommands(var1) || this.getWhitelistedPlayers().isWhitelisted(var1);
   }

   public DedicatedServer getServerInstance() {
      return (DedicatedServer)super.getServer();
   }

   public boolean bypassesPlayerLimit(GameProfile var1) {
      return this.getOppedPlayers().bypassesPlayerLimit(var1);
   }

   // $FF: synthetic method
   public MinecraftServer getServer() {
      return this.getServerInstance();
   }
}
