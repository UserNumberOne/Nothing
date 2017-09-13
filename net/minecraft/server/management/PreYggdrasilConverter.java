package net.minecraft.server.management;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.yggdrasil.ProfileNotFoundException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.PropertyManager;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PreYggdrasilConverter {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final File OLD_IPBAN_FILE = new File("banned-ips.txt");
   public static final File OLD_PLAYERBAN_FILE = new File("banned-players.txt");
   public static final File OLD_OPS_FILE = new File("ops.txt");
   public static final File OLD_WHITELIST_FILE = new File("white-list.txt");

   private static void lookupNames(MinecraftServer var0, Collection var1, ProfileLookupCallback var2) {
      String[] var3 = (String[])Iterators.toArray(Iterators.filter(var1.iterator(), new Predicate() {
         public boolean apply(@Nullable String var1) {
            return !StringUtils.isNullOrEmpty(var1);
         }
      }), String.class);
      if (var0.isServerInOnlineMode()) {
         var0.getGameProfileRepository().findProfilesByNames(var3, Agent.MINECRAFT, var2);
      } else {
         for(String var7 : var3) {
            UUID var8 = EntityPlayer.getUUID(new GameProfile((UUID)null, var7));
            GameProfile var9 = new GameProfile(var8, var7);
            var2.onProfileLookupSucceeded(var9);
         }
      }

   }

   public static String convertMobOwnerIfNeeded(final MinecraftServer var0, String var1) {
      if (!StringUtils.isNullOrEmpty(var1) && var1.length() <= 16) {
         GameProfile var2 = var0.getPlayerProfileCache().getGameProfileForUsername(var1);
         if (var2 != null && var2.getId() != null) {
            return var2.getId().toString();
         } else if (!var0.isSinglePlayer() && var0.isServerInOnlineMode()) {
            final ArrayList var3 = Lists.newArrayList();
            ProfileLookupCallback var4 = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile var1) {
                  var0.getPlayerProfileCache().addEntry(var1);
                  var3.add(var1);
               }

               public void onProfileLookupFailed(GameProfile var1, Exception var2) {
                  PreYggdrasilConverter.LOGGER.warn("Could not lookup user whitelist entry for {}", new Object[]{var1.getName(), var2});
               }
            };
            lookupNames(var0, Lists.newArrayList(new String[]{var1}), var4);
            return !var3.isEmpty() && ((GameProfile)var3.get(0)).getId() != null ? ((GameProfile)var3.get(0)).getId().toString() : "";
         } else {
            return EntityPlayer.getUUID(new GameProfile((UUID)null, var1)).toString();
         }
      } else {
         return var1;
      }
   }

   @SideOnly(Side.SERVER)
   static List readFile(File var0, Map var1) throws IOException {
      List var2 = Files.readLines(var0, Charsets.UTF_8);

      for(String var4 : var2) {
         var4 = var4.trim();
         if (!var4.startsWith("#") && var4.length() >= 1) {
            String[] var5 = var4.split("\\|");
            var1.put(var5[0].toLowerCase(Locale.ROOT), var5);
         }
      }

      return var2;
   }

   @SideOnly(Side.SERVER)
   public static boolean convertUserBanlist(final MinecraftServer var0) throws IOException {
      final UserListBans var1 = new UserListBans(PlayerList.FILE_PLAYERBANS);
      if (OLD_PLAYERBAN_FILE.exists() && OLD_PLAYERBAN_FILE.isFile()) {
         if (var1.getSaveFile().exists()) {
            try {
               var1.readSavedFile();
            } catch (FileNotFoundException var6) {
               LOGGER.warn("Could not load existing file {}", new Object[]{var1.getSaveFile().getName(), var6});
            }
         }

         try {
            final HashMap var2 = Maps.newHashMap();
            readFile(OLD_PLAYERBAN_FILE, var2);
            ProfileLookupCallback var3 = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile var1x) {
                  var0.getPlayerProfileCache().addEntry(var1x);
                  String[] var2x = (String[])var2.get(var1x.getName().toLowerCase(Locale.ROOT));
                  if (var2x == null) {
                     PreYggdrasilConverter.LOGGER.warn("Could not convert user banlist entry for {}", new Object[]{var1x.getName()});
                     throw new PreYggdrasilConverter.ConversionError("Profile not in the conversionlist");
                  } else {
                     Date var3 = var2x.length > 1 ? PreYggdrasilConverter.parseDate(var2x[1], (Date)null) : null;
                     String var4 = var2x.length > 2 ? var2x[2] : null;
                     Date var5 = var2x.length > 3 ? PreYggdrasilConverter.parseDate(var2x[3], (Date)null) : null;
                     String var6 = var2x.length > 4 ? var2x[4] : null;
                     var1.addEntry(new UserListBansEntry(var1x, var3, var4, var5, var6));
                  }
               }

               public void onProfileLookupFailed(GameProfile var1x, Exception var2x) {
                  PreYggdrasilConverter.LOGGER.warn("Could not lookup user banlist entry for {}", new Object[]{var1x.getName(), var2x});
                  if (!(var2x instanceof ProfileNotFoundException)) {
                     throw new PreYggdrasilConverter.ConversionError("Could not request user " + var1x.getName() + " from backend systems", var2x);
                  }
               }
            };
            lookupNames(var0, var2.keySet(), var3);
            var1.writeChanges();
            backupConverted(OLD_PLAYERBAN_FILE);
            return true;
         } catch (IOException var4) {
            LOGGER.warn("Could not read old user banlist to convert it!", var4);
            return false;
         } catch (PreYggdrasilConverter.ConversionError var5) {
            LOGGER.error("Conversion failed, please try again later", var5);
            return false;
         }
      } else {
         return true;
      }
   }

   @SideOnly(Side.SERVER)
   public static boolean convertIpBanlist(MinecraftServer var0) throws IOException {
      UserListIPBans var1 = new UserListIPBans(PlayerList.FILE_IPBANS);
      if (OLD_IPBAN_FILE.exists() && OLD_IPBAN_FILE.isFile()) {
         if (var1.getSaveFile().exists()) {
            try {
               var1.readSavedFile();
            } catch (FileNotFoundException var11) {
               LOGGER.warn("Could not load existing file {}", new Object[]{var1.getSaveFile().getName(), var11});
            }
         }

         try {
            HashMap var2 = Maps.newHashMap();
            readFile(OLD_IPBAN_FILE, var2);

            for(String var4 : var2.keySet()) {
               String[] var5 = (String[])var2.get(var4);
               Date var6 = var5.length > 1 ? parseDate(var5[1], (Date)null) : null;
               String var7 = var5.length > 2 ? var5[2] : null;
               Date var8 = var5.length > 3 ? parseDate(var5[3], (Date)null) : null;
               String var9 = var5.length > 4 ? var5[4] : null;
               var1.addEntry(new UserListIPBansEntry(var4, var6, var7, var8, var9));
            }

            var1.writeChanges();
            backupConverted(OLD_IPBAN_FILE);
            return true;
         } catch (IOException var10) {
            LOGGER.warn("Could not parse old ip banlist to convert it!", var10);
            return false;
         }
      } else {
         return true;
      }
   }

   @SideOnly(Side.SERVER)
   public static boolean convertOplist(final MinecraftServer var0) throws IOException {
      final UserListOps var1 = new UserListOps(PlayerList.FILE_OPS);
      if (OLD_OPS_FILE.exists() && OLD_OPS_FILE.isFile()) {
         if (var1.getSaveFile().exists()) {
            try {
               var1.readSavedFile();
            } catch (FileNotFoundException var6) {
               LOGGER.warn("Could not load existing file {}", new Object[]{var1.getSaveFile().getName(), var6});
            }
         }

         try {
            List var2 = Files.readLines(OLD_OPS_FILE, Charsets.UTF_8);
            ProfileLookupCallback var3 = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile var1x) {
                  var0.getPlayerProfileCache().addEntry(var1x);
                  var1.addEntry(new UserListOpsEntry(var1x, var0.getOpPermissionLevel(), false));
               }

               public void onProfileLookupFailed(GameProfile var1x, Exception var2) {
                  PreYggdrasilConverter.LOGGER.warn("Could not lookup oplist entry for {}", new Object[]{var1x.getName(), var2});
                  if (!(var2 instanceof ProfileNotFoundException)) {
                     throw new PreYggdrasilConverter.ConversionError("Could not request user " + var1x.getName() + " from backend systems", var2);
                  }
               }
            };
            lookupNames(var0, var2, var3);
            var1.writeChanges();
            backupConverted(OLD_OPS_FILE);
            return true;
         } catch (IOException var4) {
            LOGGER.warn("Could not read old oplist to convert it!", var4);
            return false;
         } catch (PreYggdrasilConverter.ConversionError var5) {
            LOGGER.error("Conversion failed, please try again later", var5);
            return false;
         }
      } else {
         return true;
      }
   }

   @SideOnly(Side.SERVER)
   public static boolean convertWhitelist(final MinecraftServer var0) throws IOException {
      final UserListWhitelist var1 = new UserListWhitelist(PlayerList.FILE_WHITELIST);
      if (OLD_WHITELIST_FILE.exists() && OLD_WHITELIST_FILE.isFile()) {
         if (var1.getSaveFile().exists()) {
            try {
               var1.readSavedFile();
            } catch (FileNotFoundException var6) {
               LOGGER.warn("Could not load existing file {}", new Object[]{var1.getSaveFile().getName(), var6});
            }
         }

         try {
            List var2 = Files.readLines(OLD_WHITELIST_FILE, Charsets.UTF_8);
            ProfileLookupCallback var3 = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile var1x) {
                  var0.getPlayerProfileCache().addEntry(var1x);
                  var1.addEntry(new UserListWhitelistEntry(var1x));
               }

               public void onProfileLookupFailed(GameProfile var1x, Exception var2) {
                  PreYggdrasilConverter.LOGGER.warn("Could not lookup user whitelist entry for {}", new Object[]{var1x.getName(), var2});
                  if (!(var2 instanceof ProfileNotFoundException)) {
                     throw new PreYggdrasilConverter.ConversionError("Could not request user " + var1x.getName() + " from backend systems", var2);
                  }
               }
            };
            lookupNames(var0, var2, var3);
            var1.writeChanges();
            backupConverted(OLD_WHITELIST_FILE);
            return true;
         } catch (IOException var4) {
            LOGGER.warn("Could not read old whitelist to convert it!", var4);
            return false;
         } catch (PreYggdrasilConverter.ConversionError var5) {
            LOGGER.error("Conversion failed, please try again later", var5);
            return false;
         }
      } else {
         return true;
      }
   }

   @SideOnly(Side.SERVER)
   public static boolean convertSaveFiles(final DedicatedServer var0, PropertyManager var1) {
      final File var2 = getPlayersDirectory(var1);
      final File var3 = new File(var2.getParentFile(), "playerdata");
      final File var4 = new File(var2.getParentFile(), "unknownplayers");
      if (var2.exists() && var2.isDirectory()) {
         File[] var5 = var2.listFiles();
         ArrayList var6 = Lists.newArrayList();

         for(File var10 : var5) {
            String var11 = var10.getName();
            if (var11.toLowerCase(Locale.ROOT).endsWith(".dat")) {
               String var12 = var11.substring(0, var11.length() - ".dat".length());
               if (!var12.isEmpty()) {
                  var6.add(var12);
               }
            }
         }

         try {
            final String[] var14 = (String[])var6.toArray(new String[var6.size()]);
            ProfileLookupCallback var15 = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile var1) {
                  var0.getPlayerProfileCache().addEntry(var1);
                  UUID var2x = var1.getId();
                  if (var2x == null) {
                     throw new PreYggdrasilConverter.ConversionError("Missing UUID for user profile " + var1.getName());
                  } else {
                     this.renamePlayerFile(var3, this.getFileNameForProfile(var1), var2x.toString());
                  }
               }

               public void onProfileLookupFailed(GameProfile var1, Exception var2x) {
                  PreYggdrasilConverter.LOGGER.warn("Could not lookup user uuid for {}", new Object[]{var1.getName(), var2x});
                  if (var2x instanceof ProfileNotFoundException) {
                     String var3x = this.getFileNameForProfile(var1);
                     this.renamePlayerFile(var4, var3x, var3x);
                  } else {
                     throw new PreYggdrasilConverter.ConversionError("Could not request user " + var1.getName() + " from backend systems", var2x);
                  }
               }

               private void renamePlayerFile(File var1, String var2x, String var3x) {
                  File var4x = new File(var2, var2x + ".dat");
                  File var5 = new File(var1, var3x + ".dat");
                  PreYggdrasilConverter.mkdir(var1);
                  if (!var4x.renameTo(var5)) {
                     throw new PreYggdrasilConverter.ConversionError("Could not convert file for " + var2x);
                  }
               }

               private String getFileNameForProfile(GameProfile var1) {
                  String var2x = null;

                  for(String var6 : var14) {
                     if (var6 != null && var6.equalsIgnoreCase(var1.getName())) {
                        var2x = var6;
                        break;
                     }
                  }

                  if (var2x == null) {
                     throw new PreYggdrasilConverter.ConversionError("Could not find the filename for " + var1.getName() + " anymore");
                  } else {
                     return var2x;
                  }
               }
            };
            lookupNames(var0, Lists.newArrayList(var14), var15);
            return true;
         } catch (PreYggdrasilConverter.ConversionError var13) {
            LOGGER.error("Conversion failed, please try again later", var13);
            return false;
         }
      } else {
         return true;
      }
   }

   @SideOnly(Side.SERVER)
   private static void mkdir(File var0) {
      if (var0.exists()) {
         if (!var0.isDirectory()) {
            throw new PreYggdrasilConverter.ConversionError("Can't create directory " + var0.getName() + " in world save directory.");
         }
      } else if (!var0.mkdirs()) {
         throw new PreYggdrasilConverter.ConversionError("Can't create directory " + var0.getName() + " in world save directory.");
      }

   }

   @SideOnly(Side.SERVER)
   public static boolean tryConvert(PropertyManager var0) {
      boolean var1 = hasUnconvertableFiles(var0);
      var1 = var1 && hasUnconvertablePlayerFiles(var0);
      return var1;
   }

   @SideOnly(Side.SERVER)
   private static boolean hasUnconvertableFiles(PropertyManager var0) {
      boolean var1 = false;
      if (OLD_PLAYERBAN_FILE.exists() && OLD_PLAYERBAN_FILE.isFile()) {
         var1 = true;
      }

      boolean var2 = false;
      if (OLD_IPBAN_FILE.exists() && OLD_IPBAN_FILE.isFile()) {
         var2 = true;
      }

      boolean var3 = false;
      if (OLD_OPS_FILE.exists() && OLD_OPS_FILE.isFile()) {
         var3 = true;
      }

      boolean var4 = false;
      if (OLD_WHITELIST_FILE.exists() && OLD_WHITELIST_FILE.isFile()) {
         var4 = true;
      }

      if (!var1 && !var2 && !var3 && !var4) {
         return true;
      } else {
         LOGGER.warn("**** FAILED TO START THE SERVER AFTER ACCOUNT CONVERSION!");
         LOGGER.warn("** please remove the following files and restart the server:");
         if (var1) {
            LOGGER.warn("* {}", new Object[]{OLD_PLAYERBAN_FILE.getName()});
         }

         if (var2) {
            LOGGER.warn("* {}", new Object[]{OLD_IPBAN_FILE.getName()});
         }

         if (var3) {
            LOGGER.warn("* {}", new Object[]{OLD_OPS_FILE.getName()});
         }

         if (var4) {
            LOGGER.warn("* {}", new Object[]{OLD_WHITELIST_FILE.getName()});
         }

         return false;
      }
   }

   @SideOnly(Side.SERVER)
   private static boolean hasUnconvertablePlayerFiles(PropertyManager var0) {
      File var1 = getPlayersDirectory(var0);
      if (var1.exists() && var1.isDirectory() && (var1.list().length > 0 || !var1.delete())) {
         LOGGER.warn("**** DETECTED OLD PLAYER DIRECTORY IN THE WORLD SAVE");
         LOGGER.warn("**** THIS USUALLY HAPPENS WHEN THE AUTOMATIC CONVERSION FAILED IN SOME WAY");
         LOGGER.warn("** please restart the server and if the problem persists, remove the directory '{}'", new Object[]{var1.getPath()});
         return false;
      } else {
         return true;
      }
   }

   @SideOnly(Side.SERVER)
   private static File getPlayersDirectory(PropertyManager var0) {
      String var1 = var0.getStringProperty("level-name", "world");
      File var2 = new File(var1);
      return new File(var2, "players");
   }

   @SideOnly(Side.SERVER)
   private static void backupConverted(File var0) {
      File var1 = new File(var0.getName() + ".converted");
      var0.renameTo(var1);
   }

   @SideOnly(Side.SERVER)
   private static Date parseDate(String var0, Date var1) {
      Date var2;
      try {
         var2 = UserListEntryBan.DATE_FORMAT.parse(var0);
      } catch (ParseException var4) {
         var2 = var1;
      }

      return var2;
   }

   @SideOnly(Side.SERVER)
   static class ConversionError extends RuntimeException {
      private ConversionError(String var1, Throwable var2) {
         super(var1, var2);
      }

      private ConversionError(String var1) {
         super(var1);
      }
   }
}
