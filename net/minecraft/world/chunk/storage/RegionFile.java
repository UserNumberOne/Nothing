package net.minecraft.world.chunk.storage;

import com.google.common.collect.Lists;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import net.minecraft.src.MinecraftServer;

public class RegionFile {
   private static final byte[] EMPTY_SECTOR = new byte[4096];
   private final File fileName;
   private RandomAccessFile dataFile;
   private final int[] offsets = new int[1024];
   private final int[] chunkTimestamps = new int[1024];
   private List sectorFree;
   private int sizeDelta;
   private long lastModified;

   public RegionFile(File var1) {
      this.fileName = var1;
      this.sizeDelta = 0;

      try {
         if (var1.exists()) {
            this.lastModified = var1.lastModified();
         }

         this.dataFile = new RandomAccessFile(var1, "rw");
         if (this.dataFile.length() < 4096L) {
            this.dataFile.write(EMPTY_SECTOR);
            this.dataFile.write(EMPTY_SECTOR);
            this.sizeDelta += 8192;
         }

         if ((this.dataFile.length() & 4095L) != 0L) {
            for(int var2 = 0; (long)var2 < (this.dataFile.length() & 4095L); ++var2) {
               this.dataFile.write(0);
            }
         }

         int var7 = (int)this.dataFile.length() / 4096;
         this.sectorFree = Lists.newArrayListWithCapacity(var7);

         for(int var3 = 0; var3 < var7; ++var3) {
            this.sectorFree.add(Boolean.valueOf(true));
         }

         this.sectorFree.set(0, Boolean.valueOf(false));
         this.sectorFree.set(1, Boolean.valueOf(false));
         this.dataFile.seek(0L);

         for(int var8 = 0; var8 < 1024; ++var8) {
            int var4 = this.dataFile.readInt();
            this.offsets[var8] = var4;
            if (var4 != 0 && (var4 >> 8) + (var4 & 255) <= this.sectorFree.size()) {
               for(int var5 = 0; var5 < (var4 & 255); ++var5) {
                  this.sectorFree.set((var4 >> 8) + var5, Boolean.valueOf(false));
               }
            }
         }

         for(int var9 = 0; var9 < 1024; ++var9) {
            int var10 = this.dataFile.readInt();
            this.chunkTimestamps[var9] = var10;
         }
      } catch (IOException var6) {
         var6.printStackTrace();
      }

   }

   public synchronized boolean chunkExists(int var1, int var2) {
      if (this.outOfBounds(var1, var2)) {
         return false;
      } else {
         try {
            int var3 = this.getOffset(var1, var2);
            if (var3 == 0) {
               return false;
            } else {
               int var4 = var3 >> 8;
               int var5 = var3 & 255;
               if (var4 + var5 > this.sectorFree.size()) {
                  return false;
               } else {
                  this.dataFile.seek((long)(var4 * 4096));
                  int var6 = this.dataFile.readInt();
                  if (var6 <= 4096 * var5 && var6 > 0) {
                     byte var7 = this.dataFile.readByte();
                     return var7 == 1 || var7 == 2;
                  } else {
                     return false;
                  }
               }
            }
         } catch (IOException var8) {
            return false;
         }
      }
   }

   public synchronized DataInputStream getChunkDataInputStream(int var1, int var2) {
      if (this.outOfBounds(var1, var2)) {
         return null;
      } else {
         try {
            int var3 = this.getOffset(var1, var2);
            if (var3 == 0) {
               return null;
            } else {
               int var4 = var3 >> 8;
               int var5 = var3 & 255;
               if (var4 + var5 > this.sectorFree.size()) {
                  return null;
               } else {
                  this.dataFile.seek((long)(var4 * 4096));
                  int var6 = this.dataFile.readInt();
                  if (var6 > 4096 * var5) {
                     return null;
                  } else if (var6 <= 0) {
                     return null;
                  } else {
                     byte var7 = this.dataFile.readByte();
                     if (var7 == 1) {
                        byte[] var10 = new byte[var6 - 1];
                        this.dataFile.read(var10);
                        return new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(var10))));
                     } else if (var7 == 2) {
                        byte[] var8 = new byte[var6 - 1];
                        this.dataFile.read(var8);
                        return new DataInputStream(new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(var8))));
                     } else {
                        return null;
                     }
                  }
               }
            }
         } catch (IOException var9) {
            return null;
         }
      }
   }

   public DataOutputStream getChunkDataOutputStream(int var1, int var2) {
      return this.outOfBounds(var1, var2) ? null : new DataOutputStream(new BufferedOutputStream(new DeflaterOutputStream(new RegionFile.ChunkBuffer(var1, var2))));
   }

   protected synchronized void write(int var1, int var2, byte[] var3, int var4) {
      try {
         int var5 = this.getOffset(var1, var2);
         int var6 = var5 >> 8;
         int var7 = var5 & 255;
         int var8 = (var4 + 5) / 4096 + 1;
         if (var8 >= 256) {
            return;
         }

         if (var6 != 0 && var7 == var8) {
            this.write(var6, var3, var4);
         } else {
            for(int var9 = 0; var9 < var7; ++var9) {
               this.sectorFree.set(var6 + var9, Boolean.valueOf(true));
            }

            int var15 = this.sectorFree.indexOf(Boolean.valueOf(true));
            int var10 = 0;
            if (var15 != -1) {
               for(int var11 = var15; var11 < this.sectorFree.size(); ++var11) {
                  if (var10 != 0) {
                     if (((Boolean)this.sectorFree.get(var11)).booleanValue()) {
                        ++var10;
                     } else {
                        var10 = 0;
                     }
                  } else if (((Boolean)this.sectorFree.get(var11)).booleanValue()) {
                     var15 = var11;
                     var10 = 1;
                  }

                  if (var10 >= var8) {
                     break;
                  }
               }
            }

            if (var10 >= var8) {
               var6 = var15;
               this.setOffset(var1, var2, var15 << 8 | var8);

               for(int var17 = 0; var17 < var8; ++var17) {
                  this.sectorFree.set(var6 + var17, Boolean.valueOf(false));
               }

               this.write(var6, var3, var4);
            } else {
               this.dataFile.seek(this.dataFile.length());
               var6 = this.sectorFree.size();

               for(int var16 = 0; var16 < var8; ++var16) {
                  this.dataFile.write(EMPTY_SECTOR);
                  this.sectorFree.add(Boolean.valueOf(false));
               }

               this.sizeDelta += 4096 * var8;
               this.write(var6, var3, var4);
               this.setOffset(var1, var2, var6 << 8 | var8);
            }
         }

         this.setChunkTimestamp(var1, var2, (int)(MinecraftServer.av() / 1000L));
      } catch (IOException var12) {
         var12.printStackTrace();
      }

   }

   private void write(int var1, byte[] var2, int var3) throws IOException {
      this.dataFile.seek((long)(var1 * 4096));
      this.dataFile.writeInt(var3 + 1);
      this.dataFile.writeByte(2);
      this.dataFile.write(var2, 0, var3);
   }

   private boolean outOfBounds(int var1, int var2) {
      return var1 < 0 || var1 >= 32 || var2 < 0 || var2 >= 32;
   }

   private int getOffset(int var1, int var2) {
      return this.offsets[var1 + var2 * 32];
   }

   public boolean isChunkSaved(int var1, int var2) {
      return this.getOffset(var1, var2) != 0;
   }

   private void setOffset(int var1, int var2, int var3) throws IOException {
      this.offsets[var1 + var2 * 32] = var3;
      this.dataFile.seek((long)((var1 + var2 * 32) * 4));
      this.dataFile.writeInt(var3);
   }

   private void setChunkTimestamp(int var1, int var2, int var3) throws IOException {
      this.chunkTimestamps[var1 + var2 * 32] = var3;
      this.dataFile.seek((long)(4096 + (var1 + var2 * 32) * 4));
      this.dataFile.writeInt(var3);
   }

   public void close() throws IOException {
      if (this.dataFile != null) {
         this.dataFile.close();
      }

   }

   class ChunkBuffer extends ByteArrayOutputStream {
      private final int chunkX;
      private final int chunkZ;

      public ChunkBuffer(int var2, int var3) {
         super(8096);
         this.chunkX = var2;
         this.chunkZ = var3;
      }

      public void close() {
         RegionFile.this.write(this.chunkX, this.chunkZ, this.buf, this.count);
      }
   }
}
