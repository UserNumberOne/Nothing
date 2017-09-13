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

   public RegionFile(File file) {
      this.fileName = file;
      this.sizeDelta = 0;

      try {
         if (file.exists()) {
            this.lastModified = file.lastModified();
         }

         this.dataFile = new RandomAccessFile(file, "rw");
         if (this.dataFile.length() < 4096L) {
            this.dataFile.write(EMPTY_SECTOR);
            this.dataFile.write(EMPTY_SECTOR);
            this.sizeDelta += 8192;
         }

         if ((this.dataFile.length() & 4095L) != 0L) {
            for(int i = 0; (long)i < (this.dataFile.length() & 4095L); ++i) {
               this.dataFile.write(0);
            }
         }

         int i = (int)this.dataFile.length() / 4096;
         this.sectorFree = Lists.newArrayListWithCapacity(i);

         for(int j = 0; j < i; ++j) {
            this.sectorFree.add(Boolean.valueOf(true));
         }

         this.sectorFree.set(0, Boolean.valueOf(false));
         this.sectorFree.set(1, Boolean.valueOf(false));
         this.dataFile.seek(0L);

         for(int var8 = 0; var8 < 1024; ++var8) {
            int k = this.dataFile.readInt();
            this.offsets[var8] = k;
            if (k != 0 && (k >> 8) + (k & 255) <= this.sectorFree.size()) {
               for(int l = 0; l < (k & 255); ++l) {
                  this.sectorFree.set((k >> 8) + l, Boolean.valueOf(false));
               }
            }
         }

         for(int var9 = 0; var9 < 1024; ++var9) {
            int k = this.dataFile.readInt();
            this.chunkTimestamps[var9] = k;
         }
      } catch (IOException var6) {
         var6.printStackTrace();
      }

   }

   public synchronized boolean chunkExists(int i, int j) {
      if (this.outOfBounds(i, j)) {
         return false;
      } else {
         try {
            int k = this.getOffset(i, j);
            if (k == 0) {
               return false;
            } else {
               int l = k >> 8;
               int i1 = k & 255;
               if (l + i1 > this.sectorFree.size()) {
                  return false;
               } else {
                  this.dataFile.seek((long)(l * 4096));
                  int j1 = this.dataFile.readInt();
                  if (j1 <= 4096 * i1 && j1 > 0) {
                     byte b0 = this.dataFile.readByte();
                     return b0 == 1 || b0 == 2;
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

   public synchronized DataInputStream getChunkDataInputStream(int i, int j) {
      if (this.outOfBounds(i, j)) {
         return null;
      } else {
         try {
            int k = this.getOffset(i, j);
            if (k == 0) {
               return null;
            } else {
               int l = k >> 8;
               int i1 = k & 255;
               if (l + i1 > this.sectorFree.size()) {
                  return null;
               } else {
                  this.dataFile.seek((long)(l * 4096));
                  int j1 = this.dataFile.readInt();
                  if (j1 > 4096 * i1) {
                     return null;
                  } else if (j1 <= 0) {
                     return null;
                  } else {
                     byte b0 = this.dataFile.readByte();
                     if (b0 == 1) {
                        byte[] abyte = new byte[j1 - 1];
                        this.dataFile.read(abyte);
                        return new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(abyte))));
                     } else if (b0 == 2) {
                        byte[] abyte = new byte[j1 - 1];
                        this.dataFile.read(abyte);
                        return new DataInputStream(new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(abyte))));
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

   public DataOutputStream getChunkDataOutputStream(int i, int j) {
      return this.outOfBounds(i, j) ? null : new DataOutputStream(new BufferedOutputStream(new DeflaterOutputStream(new RegionFile.ChunkBuffer(i, j))));
   }

   protected synchronized void write(int i, int j, byte[] abyte, int k) {
      try {
         int l = this.getOffset(i, j);
         int i1 = l >> 8;
         int j1 = l & 255;
         int k1 = (k + 5) / 4096 + 1;
         if (k1 >= 256) {
            return;
         }

         if (i1 != 0 && j1 == k1) {
            this.write(i1, abyte, k);
         } else {
            for(int l1 = 0; l1 < j1; ++l1) {
               this.sectorFree.set(i1 + l1, Boolean.valueOf(true));
            }

            int var15 = this.sectorFree.indexOf(Boolean.valueOf(true));
            int i2 = 0;
            if (var15 != -1) {
               for(int j2 = var15; j2 < this.sectorFree.size(); ++j2) {
                  if (i2 != 0) {
                     if (((Boolean)this.sectorFree.get(j2)).booleanValue()) {
                        ++i2;
                     } else {
                        i2 = 0;
                     }
                  } else if (((Boolean)this.sectorFree.get(j2)).booleanValue()) {
                     var15 = j2;
                     i2 = 1;
                  }

                  if (i2 >= k1) {
                     break;
                  }
               }
            }

            if (i2 >= k1) {
               i1 = var15;
               this.setOffset(i, j, var15 << 8 | k1);

               for(int j2 = 0; j2 < k1; ++j2) {
                  this.sectorFree.set(i1 + j2, Boolean.valueOf(false));
               }

               this.write(i1, abyte, k);
            } else {
               this.dataFile.seek(this.dataFile.length());
               i1 = this.sectorFree.size();

               for(int j2 = 0; j2 < k1; ++j2) {
                  this.dataFile.write(EMPTY_SECTOR);
                  this.sectorFree.add(Boolean.valueOf(false));
               }

               this.sizeDelta += 4096 * k1;
               this.write(i1, abyte, k);
               this.setOffset(i, j, i1 << 8 | k1);
            }
         }

         this.setChunkTimestamp(i, j, (int)(MinecraftServer.av() / 1000L));
      } catch (IOException var12) {
         var12.printStackTrace();
      }

   }

   private void write(int i, byte[] abyte, int j) throws IOException {
      this.dataFile.seek((long)(i * 4096));
      this.dataFile.writeInt(j + 1);
      this.dataFile.writeByte(2);
      this.dataFile.write(abyte, 0, j);
   }

   private boolean outOfBounds(int i, int j) {
      return i < 0 || i >= 32 || j < 0 || j >= 32;
   }

   private int getOffset(int i, int j) {
      return this.offsets[i + j * 32];
   }

   public boolean isChunkSaved(int i, int j) {
      return this.getOffset(i, j) != 0;
   }

   private void setOffset(int i, int j, int k) throws IOException {
      this.offsets[i + j * 32] = k;
      this.dataFile.seek((long)((i + j * 32) * 4));
      this.dataFile.writeInt(k);
   }

   private void setChunkTimestamp(int i, int j, int k) throws IOException {
      this.chunkTimestamps[i + j * 32] = k;
      this.dataFile.seek((long)(4096 + (i + j * 32) * 4));
      this.dataFile.writeInt(k);
   }

   public void close() throws IOException {
      if (this.dataFile != null) {
         this.dataFile.close();
      }

   }

   class ChunkBuffer extends ByteArrayOutputStream {
      private final int chunkX;
      private final int chunkZ;

      public ChunkBuffer(int i, int j) {
         super(8096);
         this.chunkX = i;
         this.chunkZ = j;
      }

      public void close() {
         RegionFile.this.write(this.chunkX, this.chunkZ, this.buf, this.count);
      }
   }
}
