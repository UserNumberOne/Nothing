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

   public SaveHandler(File file, String s, boolean flag, DataFixer dataconvertermanager) {
      this.dataFixer = dataconvertermanager;
      this.worldDirectory = new File(file, s);
      this.worldDirectory.mkdirs();
      this.playersDirectory = new File(this.worldDirectory, "playerdata");
      this.mapDataDir = new File(this.worldDirectory, "data");
      this.mapDataDir.mkdirs();
      this.saveDirectoryName = s;
      if (flag) {
         this.playersDirectory.mkdirs();
         this.structureTemplateManager = new TemplateManager((new File(this.worldDirectory, "structures")).toString());
      } else {
         this.structureTemplateManager = null;
      }

      this.setSessionLock();
   }

   private void setSessionLock() {
      try {
         File file = new File(this.worldDirectory, "session.lock");
         DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(file));

         try {
            dataoutputstream.writeLong(this.initializationTime);
         } finally {
            dataoutputstream.close();
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
         File file = new File(this.worldDirectory, "session.lock");
         DataInputStream datainputstream = new DataInputStream(new FileInputStream(file));

         try {
            if (datainputstream.readLong() != this.initializationTime) {
               throw new MinecraftException("The save is being accessed from another location, aborting");
            }
         } finally {
            datainputstream.close();
         }

      } catch (IOException var7) {
         throw new MinecraftException("Failed to check session lock, aborting");
      }
   }

   public IChunkLoader getChunkLoader(WorldProvider worldprovider) {
      throw new RuntimeException("Old Chunk Storage is no longer supported.");
   }

   public WorldInfo loadWorldInfo() {
      File file = new File(this.worldDirectory, "level.dat");
      if (file.exists()) {
         WorldInfo worlddata = SaveFormatOld.getWorldData(file, this.dataFixer);
         if (worlddata != null) {
            return worlddata;
         }
      }

      file = new File(this.worldDirectory, "level.dat_old");
      return file.exists() ? SaveFormatOld.getWorldData(file, this.dataFixer) : null;
   }

   public void saveWorldInfoWithPlayer(WorldInfo worlddata, @Nullable NBTTagCompound nbttagcompound) {
      NBTTagCompound nbttagcompound1 = worlddata.cloneNBTCompound(nbttagcompound);
      NBTTagCompound nbttagcompound2 = new NBTTagCompound();
      nbttagcompound2.setTag("Data", nbttagcompound1);

      try {
         File file = new File(this.worldDirectory, "level.dat_new");
         File file1 = new File(this.worldDirectory, "level.dat_old");
         File file2 = new File(this.worldDirectory, "level.dat");
         CompressedStreamTools.writeCompressed(nbttagcompound2, new FileOutputStream(file));
         if (file1.exists()) {
            file1.delete();
         }

         file2.renameTo(file1);
         if (file2.exists()) {
            file2.delete();
         }

         file.renameTo(file2);
         if (file.exists()) {
            file.delete();
         }
      } catch (Exception var8) {
         var8.printStackTrace();
      }

   }

   public void saveWorldInfo(WorldInfo worlddata) {
      this.saveWorldInfoWithPlayer(worlddata, (NBTTagCompound)null);
   }

   public void writePlayerData(EntityPlayer entityhuman) {
      try {
         NBTTagCompound nbttagcompound = entityhuman.writeToNBT(new NBTTagCompound());
         File file = new File(this.playersDirectory, entityhuman.getCachedUniqueIdString() + ".dat.tmp");
         File file1 = new File(this.playersDirectory, entityhuman.getCachedUniqueIdString() + ".dat");
         CompressedStreamTools.writeCompressed(nbttagcompound, new FileOutputStream(file));
         if (file1.exists()) {
            file1.delete();
         }

         file.renameTo(file1);
      } catch (Exception var5) {
         LOGGER.warn("Failed to save player data for {}", new Object[]{entityhuman.getName()});
      }

   }

   public NBTTagCompound readPlayerData(EntityPlayer entityhuman) {
      NBTTagCompound nbttagcompound = null;

      try {
         File file = new File(this.playersDirectory, entityhuman.getCachedUniqueIdString() + ".dat");
         if (file.exists() && file.isFile()) {
            nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(file));
         }
      } catch (Exception var6) {
         LOGGER.warn("Failed to load player data for {}", new Object[]{entityhuman.getName()});
      }

      if (nbttagcompound != null) {
         if (entityhuman instanceof EntityPlayerMP) {
            CraftPlayer player = (CraftPlayer)entityhuman.getBukkitEntity();
            long modified = (new File(this.playersDirectory, entityhuman.getUniqueID().toString() + ".dat")).lastModified();
            if (modified < player.getFirstPlayed()) {
               player.setFirstPlayed(modified);
            }
         }

         entityhuman.readFromNBT(this.dataFixer.process(FixTypes.PLAYER, nbttagcompound));
      }

      return nbttagcompound;
   }

   public NBTTagCompound getPlayerData(String s) {
      try {
         File file1 = new File(this.playersDirectory, s + ".dat");
         if (file1.exists()) {
            return CompressedStreamTools.readCompressed(new FileInputStream(file1));
         }
      } catch (Exception var3) {
         LOGGER.warn("Failed to load player data for " + s);
      }

      return null;
   }

   public IPlayerFileData getPlayerNBTManager() {
      return this;
   }

   public String[] getAvailablePlayerDat() {
      String[] astring = this.playersDirectory.list();
      if (astring == null) {
         astring = new String[0];
      }

      for(int i = 0; i < astring.length; ++i) {
         if (astring[i].endsWith(".dat")) {
            astring[i] = astring[i].substring(0, astring[i].length() - 4);
         }
      }

      return astring;
   }

   public void flush() {
   }

   public File getMapFileFromName(String s) {
      return new File(this.mapDataDir, s + ".dat");
   }

   public TemplateManager getStructureTemplateManager() {
      return this.structureTemplateManager;
   }

   public UUID getUUID() {
      if (this.uuid != null) {
         return this.uuid;
      } else {
         File file1 = new File(this.worldDirectory, "uid.dat");
         if (file1.exists()) {
            label204: {
               DataInputStream dis = null;

               UUID var3;
               try {
                  dis = new DataInputStream(new FileInputStream(file1));
                  var3 = this.uuid = new UUID(dis.readLong(), dis.readLong());
               } catch (IOException var28) {
                  LOGGER.warn("Failed to read " + file1 + ", generating new random UUID", var28);
                  break label204;
               } finally {
                  if (dis != null) {
                     try {
                        dis.close();
                     } catch (IOException var25) {
                        ;
                     }
                  }

               }

               return var3;
            }
         }

         this.uuid = UUID.randomUUID();
         DataOutputStream dos = null;

         try {
            dos = new DataOutputStream(new FileOutputStream(file1));
            dos.writeLong(this.uuid.getMostSignificantBits());
            dos.writeLong(this.uuid.getLeastSignificantBits());
         } catch (IOException var26) {
            LOGGER.warn("Failed to write " + file1, var26);
         } finally {
            if (dos != null) {
               try {
                  dos.close();
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
