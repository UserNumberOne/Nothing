package net.minecraft.world.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.structure.template.TemplateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;

public class SaveHandler implements ISaveHandler, IPlayerFileData {
   private static final Logger LOGGER = LogManager.getLogger();
   private final File worldDirectory;
   private final File playersDirectory;
   private final File mapDataDir;
   private final long initializationTime = MinecraftServer.av();
   private final String saveDirectoryName;
   private final TemplateManager structureTemplateManager;
   protected final DataFixer dataFixer;
   private UUID uuid = null;

   public SaveHandler(File var1, String var2, boolean var3, DataFixer var4) {
      this.dataFixer = var4;
      this.worldDirectory = new File(var1, var2);
      this.worldDirectory.mkdirs();
      this.playersDirectory = new File(this.worldDirectory, "playerdata");
      this.mapDataDir = new File(this.worldDirectory, "data");
      this.mapDataDir.mkdirs();
      this.saveDirectoryName = var2;
      if (var3) {
         this.playersDirectory.mkdirs();
         this.structureTemplateManager = new TemplateManager((new File(this.worldDirectory, "structures")).toString());
      } else {
         this.structureTemplateManager = null;
      }

      this.setSessionLock();
   }

   private void setSessionLock() {
      try {
         File var1 = new File(this.worldDirectory, "session.lock");
         DataOutputStream var2 = new DataOutputStream(new FileOutputStream(var1));

         try {
            var2.writeLong(this.initializationTime);
         } finally {
            var2.close();
         }

      } catch (IOException var7) {
         var7.printStackTrace();
         throw new RuntimeException("Failed to check session lock, aborting");
      }
   }

   public File getWorldDirectory() {
      return this.worldDirectory;
   }

   public void checkSessionLock() throws MinecraftException {
      try {
         File var1 = new File(this.worldDirectory, "session.lock");
         DataInputStream var2 = new DataInputStream(new FileInputStream(var1));

         try {
            if (var2.readLong() != this.initializationTime) {
               throw new MinecraftException("The save is being accessed from another location, aborting");
            }
         } finally {
            var2.close();
         }

      } catch (IOException var7) {
         throw new MinecraftException("Failed to check session lock, aborting");
      }
   }

   public IChunkLoader getChunkLoader(WorldProvider var1) {
      throw new RuntimeException("Old Chunk Storage is no longer supported.");
   }

   public WorldInfo loadWorldInfo() {
      File var1 = new File(this.worldDirectory, "level.dat");
      if (var1.exists()) {
         WorldInfo var2 = SaveFormatOld.getWorldData(var1, this.dataFixer);
         if (var2 != null) {
            return var2;
         }
      }

      var1 = new File(this.worldDirectory, "level.dat_old");
      return var1.exists() ? SaveFormatOld.getWorldData(var1, this.dataFixer) : null;
   }

   public void saveWorldInfoWithPlayer(WorldInfo var1, @Nullable NBTTagCompound var2) {
      NBTTagCompound var3 = var1.cloneNBTCompound(var2);
      NBTTagCompound var4 = new NBTTagCompound();
      var4.setTag("Data", var3);

      try {
         File var5 = new File(this.worldDirectory, "level.dat_new");
         File var6 = new File(this.worldDirectory, "level.dat_old");
         File var7 = new File(this.worldDirectory, "level.dat");
         CompressedStreamTools.writeCompressed(var4, new FileOutputStream(var5));
         if (var6.exists()) {
            var6.delete();
         }

         var7.renameTo(var6);
         if (var7.exists()) {
            var7.delete();
         }

         var5.renameTo(var7);
         if (var5.exists()) {
            var5.delete();
         }
      } catch (Exception var8) {
         var8.printStackTrace();
      }

   }

   public void saveWorldInfo(WorldInfo var1) {
      this.saveWorldInfoWithPlayer(var1, (NBTTagCompound)null);
   }

   public void writePlayerData(EntityPlayer var1) {
      try {
         NBTTagCompound var2 = var1.writeToNBT(new NBTTagCompound());
         File var3 = new File(this.playersDirectory, var1.getCachedUniqueIdString() + ".dat.tmp");
         File var4 = new File(this.playersDirectory, var1.getCachedUniqueIdString() + ".dat");
         CompressedStreamTools.writeCompressed(var2, new FileOutputStream(var3));
         if (var4.exists()) {
            var4.delete();
         }

         var3.renameTo(var4);
      } catch (Exception var5) {
         LOGGER.warn("Failed to save player data for {}", new Object[]{var1.getName()});
      }

   }

   public NBTTagCompound readPlayerData(EntityPlayer var1) {
      NBTTagCompound var2 = null;

      try {
         File var3 = new File(this.playersDirectory, var1.getCachedUniqueIdString() + ".dat");
         if (var3.exists() && var3.isFile()) {
            var2 = CompressedStreamTools.readCompressed(new FileInputStream(var3));
         }
      } catch (Exception var6) {
         LOGGER.warn("Failed to load player data for {}", new Object[]{var1.getName()});
      }

      if (var2 != null) {
         if (var1 instanceof EntityPlayerMP) {
            CraftPlayer var7 = (CraftPlayer)var1.getBukkitEntity();
            long var4 = (new File(this.playersDirectory, var1.getUniqueID().toString() + ".dat")).lastModified();
            if (var4 < var7.getFirstPlayed()) {
               var7.setFirstPlayed(var4);
            }
         }

         var1.readFromNBT(this.dataFixer.process(FixTypes.PLAYER, var2));
      }

      return var2;
   }

   public NBTTagCompound getPlayerData(String var1) {
      try {
         File var2 = new File(this.playersDirectory, var1 + ".dat");
         if (var2.exists()) {
            return CompressedStreamTools.readCompressed(new FileInputStream(var2));
         }
      } catch (Exception var3) {
         LOGGER.warn("Failed to load player data for " + var1);
      }

      return null;
   }

   public IPlayerFileData getPlayerNBTManager() {
      return this;
   }

   public String[] getAvailablePlayerDat() {
      String[] var1 = this.playersDirectory.list();
      if (var1 == null) {
         var1 = new String[0];
      }

      for(int var2 = 0; var2 < var1.length; ++var2) {
         if (var1[var2].endsWith(".dat")) {
            var1[var2] = var1[var2].substring(0, var1[var2].length() - 4);
         }
      }

      return var1;
   }

   public void flush() {
   }

   public File getMapFileFromName(String var1) {
      return new File(this.mapDataDir, var1 + ".dat");
   }

   public TemplateManager getStructureTemplateManager() {
      return this.structureTemplateManager;
   }

   public UUID getUUID() {
      if (this.uuid != null) {
         return this.uuid;
      } else {
         File var1 = new File(this.worldDirectory, "uid.dat");
         if (var1.exists()) {
            label204: {
               DataInputStream var2 = null;

               UUID var3;
               try {
                  var2 = new DataInputStream(new FileInputStream(var1));
                  var3 = this.uuid = new UUID(var2.readLong(), var2.readLong());
               } catch (IOException var28) {
                  LOGGER.warn("Failed to read " + var1 + ", generating new random UUID", var28);
                  break label204;
               } finally {
                  if (var2 != null) {
                     try {
                        var2.close();
                     } catch (IOException var25) {
                        ;
                     }
                  }

               }

               return var3;
            }
         }

         this.uuid = UUID.randomUUID();
         DataOutputStream var30 = null;

         try {
            var30 = new DataOutputStream(new FileOutputStream(var1));
            var30.writeLong(this.uuid.getMostSignificantBits());
            var30.writeLong(this.uuid.getLeastSignificantBits());
         } catch (IOException var26) {
            LOGGER.warn("Failed to write " + var1, var26);
         } finally {
            if (var30 != null) {
               try {
                  var30.close();
               } catch (IOException var24) {
                  ;
               }
            }

         }

         return this.uuid;
      }
   }

   public File getPlayerDir() {
      return this.playersDirectory;
   }
}
