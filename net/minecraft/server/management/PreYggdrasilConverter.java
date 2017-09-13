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
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.PropertyManager;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PreYggdrasilConverter {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final File OLD_IPBAN_FILE = new File("banned-ips.txt");
   public static final File OLD_PLAYERBAN_FILE = new File("banned-players.txt");
   public static final File OLD_OPS_FILE = new File("ops.txt");
   public static final File OLD_WHITELIST_FILE = new File("white-list.txt");

   static List readFile(File file, Map map) throws IOException {
      List list = Files.readLines(file, Charsets.UTF_8);

      for(String s : list) {
         s = s.trim();
         if (!s.startsWith("#") && s.length() >= 1) {
            String[] astring = s.split("\\|");
            map.put(astring[0].toLowerCase(Locale.ROOT), astring);
         }
      }

      return list;
   }

   private static void a(MinecraftServer minecraftserver, Collection collection, ProfileLookupCallback profilelookupcallback) {
      String[] astring = (String[])Iterators.toArray(Iterators.filter(collection.iterator(), new Predicate() {
         public boolean apply(@Nullable String s) {
            return !StringUtils.isNullOrEmpty(s);
         }

         public boolean apply(Object object) {
            return this.apply((String)object);
         }
      }), String.class);
      if (minecraftserver.getOnlineMode()) {
         minecraftserver.getGameProfileRepository().findProfilesByNames(astring, Agent.MINECRAFT, profilelookupcallback);
      } else {
         for(String s : astring) {
            UUID uuid = EntityPlayer.getUUID(new GameProfile((UUID)null, s));
            GameProfile gameprofile = new GameProfile(uuid, s);
            profilelookupcallback.onProfileLookupSucceeded(gameprofile);
         }
      }

   }

   public static boolean a(final MinecraftServer minecraftserver) {
      final UserListBans gameprofilebanlist = new UserListBans(PlayerList.FILE_PLAYERBANS);
      if (OLD_PLAYERBAN_FILE.exists() && OLD_PLAYERBAN_FILE.isFile()) {
         if (gameprofilebanlist.getSaveFile().exists()) {
            try {
               gameprofilebanlist.readSavedFile();
            } catch (IOException var6) {
               LOGGER.warn("Could not load existing file {}", new Object[]{gameprofilebanlist.getSaveFile().getName()});
            }
         }

         try {
            final HashMap hashmap = Maps.newHashMap();
            readFile(OLD_PLAYERBAN_FILE, hashmap);
            ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile gameprofile) {
                  minecraftserver.getUserCache().addEntry(gameprofile);
                  String[] astring = (String[])hashmap.get(gameprofile.getName().toLowerCase(Locale.ROOT));
                  if (astring == null) {
                     PreYggdrasilConverter.LOGGER.warn("Could not convert user banlist entry for {}", new Object[]{gameprofile.getName()});
                     throw new PreYggdrasilConverter.ConversionError("Profile not in the conversionlist", (Throwable)null, (PreYggdrasilConverter.ConversionError)null);
                  } else {
                     Date date = astring.length > 1 ? PreYggdrasilConverter.parseDate(astring[1], (Date)null) : null;
                     String s = astring.length > 2 ? astring[2] : null;
                     Date date1 = astring.length > 3 ? PreYggdrasilConverter.parseDate(astring[3], (Date)null) : null;
                     String s1 = astring.length > 4 ? astring[4] : null;
                     gameprofilebanlist.addEntry(new UserListBansEntry(gameprofile, date, s, date1, s1));
                  }
               }

               public void onProfileLookupFailed(GameProfile gameprofile, Exception exception) {
                  PreYggdrasilConverter.LOGGER.warn("Could not lookup user banlist entry for {}", new Object[]{gameprofile.getName(), exception});
                  if (!(exception instanceof ProfileNotFoundException)) {
                     throw new PreYggdrasilConverter.ConversionError("Could not request user " + gameprofile.getName() + " from backend systems", exception, (Object)null);
                  }
               }
            };
            a(minecraftserver, hashmap.keySet(), profilelookupcallback);
            gameprofilebanlist.writeChanges();
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

   public static boolean b(MinecraftServer minecraftserver) {
      UserListIPBans ipbanlist = new UserListIPBans(PlayerList.FILE_IPBANS);
      if (OLD_IPBAN_FILE.exists() && OLD_IPBAN_FILE.isFile()) {
         if (ipbanlist.getSaveFile().exists()) {
            try {
               ipbanlist.readSavedFile();
            } catch (IOException var11) {
               LOGGER.warn("Could not load existing file {}", new Object[]{ipbanlist.getSaveFile().getName()});
            }
         }

         try {
            HashMap hashmap = Maps.newHashMap();
            readFile(OLD_IPBAN_FILE, hashmap);

            for(String s : hashmap.keySet()) {
               String[] astring = (String[])hashmap.get(s);
               Date date = astring.length > 1 ? parseDate(astring[1], (Date)null) : null;
               String s1 = astring.length > 2 ? astring[2] : null;
               Date date1 = astring.length > 3 ? parseDate(astring[3], (Date)null) : null;
               String s2 = astring.length > 4 ? astring[4] : null;
               ipbanlist.addEntry(new UserListIPBansEntry(s, date, s1, date1, s2));
            }

            ipbanlist.writeChanges();
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

   public static boolean c(final MinecraftServer minecraftserver) {
      final UserListOps oplist = new UserListOps(PlayerList.FILE_OPS);
      if (OLD_OPS_FILE.exists() && OLD_OPS_FILE.isFile()) {
         if (oplist.getSaveFile().exists()) {
            try {
               oplist.readSavedFile();
            } catch (IOException var6) {
               LOGGER.warn("Could not load existing file {}", new Object[]{oplist.getSaveFile().getName()});
            }
         }

         try {
            List list = Files.readLines(OLD_OPS_FILE, Charsets.UTF_8);
            ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile gameprofile) {
                  minecraftserver.getUserCache().addEntry(gameprofile);
                  oplist.addEntry(new UserListOpsEntry(gameprofile, minecraftserver.q(), false));
               }

               public void onProfileLookupFailed(GameProfile gameprofile, Exception exception) {
                  PreYggdrasilConverter.LOGGER.warn("Could not lookup oplist entry for {}", new Object[]{gameprofile.getName(), exception});
                  if (!(exception instanceof ProfileNotFoundException)) {
                     throw new PreYggdrasilConverter.ConversionError("Could not request user " + gameprofile.getName() + " from backend systems", exception, (Object)null);
                  }
               }
            };
            a(minecraftserver, list, profilelookupcallback);
            oplist.writeChanges();
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

   public static boolean d(final MinecraftServer minecraftserver) {
      final UserListWhitelist whitelist = new UserListWhitelist(PlayerList.FILE_WHITELIST);
      if (OLD_WHITELIST_FILE.exists() && OLD_WHITELIST_FILE.isFile()) {
         if (whitelist.getSaveFile().exists()) {
            try {
               whitelist.readSavedFile();
            } catch (IOException var6) {
               LOGGER.warn("Could not load existing file {}", new Object[]{whitelist.getSaveFile().getName()});
            }
         }

         try {
            List list = Files.readLines(OLD_WHITELIST_FILE, Charsets.UTF_8);
            ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile gameprofile) {
                  minecraftserver.getUserCache().addEntry(gameprofile);
                  whitelist.addEntry(new UserListWhitelistEntry(gameprofile));
               }

               public void onProfileLookupFailed(GameProfile gameprofile, Exception exception) {
                  PreYggdrasilConverter.LOGGER.warn("Could not lookup user whitelist entry for {}", new Object[]{gameprofile.getName(), exception});
                  if (!(exception instanceof ProfileNotFoundException)) {
                     throw new PreYggdrasilConverter.ConversionError("Could not request user " + gameprofile.getName() + " from backend systems", exception, (Object)null);
                  }
               }
            };
            a(minecraftserver, list, profilelookupcallback);
            whitelist.writeChanges();
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

   public static String a(final MinecraftServer minecraftserver, String s) {
      if (!StringUtils.isNullOrEmpty(s) && s.length() <= 16) {
         GameProfile gameprofile = minecraftserver.getUserCache().getGameProfileForUsername(s);
         if (gameprofile != null && gameprofile.getId() != null) {
            return gameprofile.getId().toString();
         } else if (!minecraftserver.R() && minecraftserver.getOnlineMode()) {
            final ArrayList arraylist = Lists.newArrayList();
            ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile gameprofile) {
                  minecraftserver.getUserCache().addEntry(gameprofile);
                  arraylist.add(gameprofile);
               }

               public void onProfileLookupFailed(GameProfile gameprofile, Exception exception) {
                  PreYggdrasilConverter.LOGGER.warn("Could not lookup user whitelist entry for {}", new Object[]{gameprofile.getName(), exception});
               }
            };
            a(minecraftserver, Lists.newArrayList(new String[]{s}), profilelookupcallback);
            return !arraylist.isEmpty() && ((GameProfile)arraylist.get(0)).getId() != null ? ((GameProfile)arraylist.get(0)).getId().toString() : "";
         } else {
            return EntityPlayer.getUUID(new GameProfile((UUID)null, s)).toString();
         }
      } else {
         return s;
      }
   }

   public static boolean convertSaveFiles(final DedicatedServer dedicatedserver, PropertyManager propertymanager) {
      final File file = getPlayersDirectory(propertymanager);
      new File(file.getParentFile(), "playerdata");
      final File file2 = new File(file.getParentFile(), "unknownplayers");
      if (file.exists() && file.isDirectory()) {
         File[] afile = file.listFiles();
         ArrayList arraylist = Lists.newArrayList();

         for(File file3 : afile) {
            String s = file3.getName();
            if (s.toLowerCase(Locale.ROOT).endsWith(".dat")) {
               String s1 = s.substring(0, s.length() - ".dat".length());
               if (!s1.isEmpty()) {
                  arraylist.add(s1);
               }
            }
         }

         try {
            final String[] astring = (String[])arraylist.toArray(new String[arraylist.size()]);
            ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback() {
               public void onProfileLookupSucceeded(GameProfile gameprofile) {
                  dedicatedserver.getUserCache().addEntry(gameprofile);
                  UUID uuid = gameprofile.getId();
                  if (uuid == null) {
                     throw new PreYggdrasilConverter.ConversionError("Missing UUID for user profile " + gameprofile.getName(), (Throwable)null, (PreYggdrasilConverter.ConversionError)null);
                  } else {
                     this.renamePlayerFile(file, this.getFileNameForProfile(gameprofile), uuid.toString());
                  }
               }

               public void onProfileLookupFailed(GameProfile gameprofile, Exception exception) {
                  PreYggdrasilConverter.LOGGER.warn("Could not lookup user uuid for {}", new Object[]{gameprofile.getName(), exception});
                  if (exception instanceof ProfileNotFoundException) {
                     String s = this.getFileNameForProfile(gameprofile);
                     this.renamePlayerFile(file, s, s);
                  } else {
                     throw new PreYggdrasilConverter.ConversionError("Could not request user " + gameprofile.getName() + " from backend systems", exception, (Object)null);
                  }
               }

               private void renamePlayerFile(File filex, String s, String s1) {
                  File file1 = new File(file2, s + ".dat");
                  File file3 = new File(file, s1 + ".dat");
                  NBTTagCompound root = null;

                  try {
                     root = CompressedStreamTools.readCompressed(new FileInputStream(file1));
                  } catch (Exception var10) {
                     var10.printStackTrace();
                  }

                  if (root != null) {
                     if (!root.hasKey("bukkit")) {
                        root.setTag("bukkit", new NBTTagCompound());
                     }

                     NBTTagCompound data = root.getCompoundTag("bukkit");
                     data.setString("lastKnownName", s);

                     try {
                        CompressedStreamTools.writeCompressed(root, new FileOutputStream(file2));
                     } catch (Exception var9) {
                        var9.printStackTrace();
                     }
                  }

                  PreYggdrasilConverter.mkdir(file);
                  if (!file1.renameTo(file3)) {
                     throw new PreYggdrasilConverter.ConversionError("Could not convert file for " + s, (Throwable)null, (PreYggdrasilConverter.ConversionError)null);
                  }
               }

               private String getFileNameForProfile(GameProfile gameprofile) {
                  String s = null;
                  int i = astring.length;

                  for(int j = 0; j < i; ++j) {
                     String s1 = astring[j];
                     if (s1 != null && s1.equalsIgnoreCase(gameprofile.getName())) {
                        s = s1;
                        break;
                     }
                  }

                  if (s == null) {
                     throw new PreYggdrasilConverter.ConversionError("Could not find the filename for " + gameprofile.getName() + " anymore", (Throwable)null, (PreYggdrasilConverter.ConversionError)null);
                  } else {
                     return s;
                  }
               }
            };
            a(dedicatedserver, Lists.newArrayList(astring), profilelookupcallback);
            return true;
         } catch (PreYggdrasilConverter.ConversionError var12) {
            LOGGER.error("Conversion failed, please try again later", var12);
            return false;
         }
      } else {
         return true;
      }
   }

   private static void mkdir(File file) {
      if (file.exists()) {
         if (!file.isDirectory()) {
            throw new PreYggdrasilConverter.ConversionError("Can't create directory " + file.getName() + " in world save directory.", (Throwable)null, (PreYggdrasilConverter.ConversionError)null);
         }
      } else if (!file.mkdirs()) {
         throw new PreYggdrasilConverter.ConversionError("Can't create directory " + file.getName() + " in world save directory.", (Throwable)null, (PreYggdrasilConverter.ConversionError)null);
      }

   }

   public static boolean tryConvert(PropertyManager propertymanager) {
      boolean flag = hasUnconvertableFiles(propertymanager);
      flag = flag && hasUnconvertablePlayerFiles(propertymanager);
      return flag;
   }

   private static boolean hasUnconvertableFiles(PropertyManager propertymanager) {
      boolean flag = false;
      if (OLD_PLAYERBAN_FILE.exists() && OLD_PLAYERBAN_FILE.isFile()) {
         flag = true;
      }

      boolean flag1 = false;
      if (OLD_IPBAN_FILE.exists() && OLD_IPBAN_FILE.isFile()) {
         flag1 = true;
      }

      boolean flag2 = false;
      if (OLD_OPS_FILE.exists() && OLD_OPS_FILE.isFile()) {
         flag2 = true;
      }

      boolean flag3 = false;
      if (OLD_WHITELIST_FILE.exists() && OLD_WHITELIST_FILE.isFile()) {
         flag3 = true;
      }

      if (!flag && !flag1 && !flag2 && !flag3) {
         return true;
      } else {
         LOGGER.warn("**** FAILED TO START THE SERVER AFTER ACCOUNT CONVERSION!");
         LOGGER.warn("** please remove the following files and restart the server:");
         if (flag) {
            LOGGER.warn("* {}", new Object[]{OLD_PLAYERBAN_FILE.getName()});
         }

         if (flag1) {
            LOGGER.warn("* {}", new Object[]{OLD_IPBAN_FILE.getName()});
         }

         if (flag2) {
            LOGGER.warn("* {}", new Object[]{OLD_OPS_FILE.getName()});
         }

         if (flag3) {
            LOGGER.warn("* {}", new Object[]{OLD_WHITELIST_FILE.getName()});
         }

         return false;
      }
   }

   private static boolean hasUnconvertablePlayerFiles(PropertyManager propertymanager) {
      File file = getPlayersDirectory(propertymanager);
      if (!file.exists() || !file.isDirectory() || file.list().length <= 0 && file.delete()) {
         return true;
      } else {
         LOGGER.warn("**** DETECTED OLD PLAYER DIRECTORY IN THE WORLD SAVE");
         LOGGER.warn("**** THIS USUALLY HAPPENS WHEN THE AUTOMATIC CONVERSION FAILED IN SOME WAY");
         LOGGER.warn("** please restart the server and if the problem persists, remove the directory '{}'", new Object[]{file.getPath()});
         return false;
      }
   }

   private static File getPlayersDirectory(PropertyManager propertymanager) {
      String s = propertymanager.getStringProperty("level-name", "world");
      File file = new File(MinecraftServer.getServer().server.getWorldContainer(), s);
      return new File(file, "players");
   }

   private static void backupConverted(File file) {
      File file1 = new File(file.getName() + ".converted");
      file.renameTo(file1);
   }

   private static Date parseDate(String s, Date date) {
      Date date1;
      try {
         date1 = UserListEntryBan.DATE_FORMAT.parse(s);
      } catch (ParseException var3) {
         date1 = date;
      }

      return date1;
   }

   static class ConversionError extends RuntimeException {
      private ConversionError(String s, Throwable throwable) {
         super(s, throwable);
      }

      private ConversionError(String s) {
         super(s);
      }

      ConversionError(String s, Object object) {
         this(s);
      }

      ConversionError(String s, Throwable throwable, Object object) {
         this(s, throwable);
      }

      // $FF: synthetic method
      ConversionError(String var1, Throwable var2, PreYggdrasilConverter.ConversionError var3) {
         this(var1, var2);
      }
   }
}
