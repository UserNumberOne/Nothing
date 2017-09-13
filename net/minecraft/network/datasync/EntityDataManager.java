package net.minecraft.network.datasync;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ReportedException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityDataManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Map NEXT_ID_MAP = Maps.newHashMap();
   private final Entity entity;
   private final Map entries = Maps.newHashMap();
   private final ReadWriteLock lock = new ReentrantReadWriteLock();
   private boolean empty = true;
   private boolean dirty;

   public EntityDataManager(Entity var1) {
      this.entity = var1;
   }

   public static DataParameter createKey(Class var0, DataSerializer var1) {
      if (LOGGER.isDebugEnabled()) {
         try {
            Class var2 = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
            if (!var2.equals(var0)) {
               LOGGER.debug("defineId called for: {} from {}", new Object[]{var0, var2, new RuntimeException()});
            }
         } catch (ClassNotFoundException var5) {
            ;
         }
      }

      int var6;
      if (NEXT_ID_MAP.containsKey(var0)) {
         var6 = ((Integer)NEXT_ID_MAP.get(var0)).intValue() + 1;
      } else {
         int var3 = 0;
         Class var4 = var0;

         while(var4 != Entity.class) {
            var4 = var4.getSuperclass();
            if (NEXT_ID_MAP.containsKey(var4)) {
               var3 = ((Integer)NEXT_ID_MAP.get(var4)).intValue() + 1;
               break;
            }
         }

         var6 = var3;
      }

      if (var6 > 254) {
         throw new IllegalArgumentException("Data value id is too big with " + var6 + "! (Max is " + 254 + ")");
      } else {
         NEXT_ID_MAP.put(var0, Integer.valueOf(var6));
         return var1.createKey(var6);
      }
   }

   public void register(DataParameter var1, Object var2) {
      int var3 = var1.getId();
      if (var3 > 254) {
         throw new IllegalArgumentException("Data value id is too big with " + var3 + "! (Max is " + 254 + ")");
      } else if (this.entries.containsKey(Integer.valueOf(var3))) {
         throw new IllegalArgumentException("Duplicate id value for " + var3 + "!");
      } else if (DataSerializers.getSerializerId(var1.getSerializer()) < 0) {
         throw new IllegalArgumentException("Unregistered serializer " + var1.getSerializer() + " for " + var3 + "!");
      } else {
         this.setEntry(var1, var2);
      }
   }

   private void setEntry(DataParameter var1, Object var2) {
      EntityDataManager.DataEntry var3 = new EntityDataManager.DataEntry(var1, var2);
      this.lock.writeLock().lock();
      this.entries.put(Integer.valueOf(var1.getId()), var3);
      this.empty = false;
      this.lock.writeLock().unlock();
   }

   private EntityDataManager.DataEntry getEntry(DataParameter var1) {
      this.lock.readLock().lock();

      EntityDataManager.DataEntry var2;
      try {
         var2 = (EntityDataManager.DataEntry)this.entries.get(Integer.valueOf(var1.getId()));
      } catch (Throwable var6) {
         CrashReport var4 = CrashReport.makeCrashReport(var6, "Getting synched entity data");
         CrashReportCategory var5 = var4.makeCategory("Synched entity data");
         var5.addCrashSection("Data ID", var1);
         throw new ReportedException(var4);
      }

      this.lock.readLock().unlock();
      return var2;
   }

   public Object get(DataParameter var1) {
      return this.getEntry(var1).getValue();
   }

   public void set(DataParameter var1, Object var2) {
      EntityDataManager.DataEntry var3 = this.getEntry(var1);
      if (ObjectUtils.notEqual(var2, var3.getValue())) {
         var3.setValue(var2);
         this.entity.notifyDataManagerChange(var1);
         var3.setDirty(true);
         this.dirty = true;
      }

   }

   public void setDirty(DataParameter var1) {
      this.getEntry(var1).dirty = true;
      this.dirty = true;
   }

   public boolean isDirty() {
      return this.dirty;
   }

   public static void writeEntries(List var0, PacketBuffer var1) throws IOException {
      if (var0 != null) {
         int var2 = 0;

         for(int var3 = var0.size(); var2 < var3; ++var2) {
            EntityDataManager.DataEntry var4 = (EntityDataManager.DataEntry)var0.get(var2);
            writeEntry(var1, var4);
         }
      }

      var1.writeByte(255);
   }

   @Nullable
   public List getDirty() {
      ArrayList var1 = null;
      if (this.dirty) {
         this.lock.readLock().lock();

         for(EntityDataManager.DataEntry var3 : this.entries.values()) {
            if (var3.isDirty()) {
               var3.setDirty(false);
               if (var1 == null) {
                  var1 = Lists.newArrayList();
               }

               var1.add(var3);
            }
         }

         this.lock.readLock().unlock();
      }

      this.dirty = false;
      return var1;
   }

   public void writeEntries(PacketBuffer var1) throws IOException {
      this.lock.readLock().lock();

      for(EntityDataManager.DataEntry var3 : this.entries.values()) {
         writeEntry(var1, var3);
      }

      this.lock.readLock().unlock();
      var1.writeByte(255);
   }

   @Nullable
   public List getAll() {
      ArrayList var1 = null;
      this.lock.readLock().lock();

      for(EntityDataManager.DataEntry var3 : this.entries.values()) {
         if (var1 == null) {
            var1 = Lists.newArrayList();
         }

         var1.add(var3);
      }

      this.lock.readLock().unlock();
      return var1;
   }

   private static void writeEntry(PacketBuffer var0, EntityDataManager.DataEntry var1) throws IOException {
      DataParameter var2 = var1.getKey();
      int var3 = DataSerializers.getSerializerId(var2.getSerializer());
      if (var3 < 0) {
         throw new EncoderException("Unknown serializer type " + var2.getSerializer());
      } else {
         var0.writeByte(var2.getId());
         var0.writeVarInt(var3);
         var2.getSerializer().write(var0, var1.getValue());
      }
   }

   @Nullable
   public static List readEntries(PacketBuffer var0) throws IOException {
      ArrayList var1 = null;

      short var4;
      while((var4 = var0.readUnsignedByte()) != 255) {
         if (var1 == null) {
            var1 = Lists.newArrayList();
         }

         int var2 = var0.readVarInt();
         DataSerializer var3 = DataSerializers.getSerializer(var2);
         if (var3 == null) {
            throw new DecoderException("Unknown serializer type " + var2);
         }

         var1.add(new EntityDataManager.DataEntry(var3.createKey(var4), var3.read(var0)));
      }

      return var1;
   }

   public boolean isEmpty() {
      return this.empty;
   }

   public void setClean() {
      this.dirty = false;
   }

   public static class DataEntry {
      private final DataParameter key;
      private Object value;
      private boolean dirty;

      public DataEntry(DataParameter var1, Object var2) {
         this.key = var1;
         this.value = var2;
         this.dirty = true;
      }

      public DataParameter getKey() {
         return this.key;
      }

      public void setValue(Object var1) {
         this.value = var1;
      }

      public Object getValue() {
         return this.value;
      }

      public boolean isDirty() {
         return this.dirty;
      }

      public void setDirty(boolean var1) {
         this.dirty = var1;
      }
   }
}
