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

   public EntityDataManager(Entity entity) {
      this.entity = entity;
   }

   public static DataParameter createKey(Class oclass, DataSerializer datawatcherserializer) {
      if (LOGGER.isDebugEnabled()) {
         try {
            Class oclass1 = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
            if (!oclass1.equals(oclass)) {
               LOGGER.debug("defineId called for: {} from {}", new Object[]{oclass, oclass1, new RuntimeException()});
            }
         } catch (ClassNotFoundException var5) {
            ;
         }
      }

      int i;
      if (NEXT_ID_MAP.containsKey(oclass)) {
         i = ((Integer)NEXT_ID_MAP.get(oclass)).intValue() + 1;
      } else {
         int j = 0;
         Class oclass2 = oclass;

         while(oclass2 != Entity.class) {
            oclass2 = oclass2.getSuperclass();
            if (NEXT_ID_MAP.containsKey(oclass2)) {
               j = ((Integer)NEXT_ID_MAP.get(oclass2)).intValue() + 1;
               break;
            }
         }

         i = j;
      }

      if (i > 254) {
         throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is " + 254 + ")");
      } else {
         NEXT_ID_MAP.put(oclass, Integer.valueOf(i));
         return datawatcherserializer.createKey(i);
      }
   }

   public void register(DataParameter datawatcherobject, Object t0) {
      int i = datawatcherobject.getId();
      if (i > 254) {
         throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is " + 254 + ")");
      } else if (this.entries.containsKey(Integer.valueOf(i))) {
         throw new IllegalArgumentException("Duplicate id value for " + i + "!");
      } else if (DataSerializers.getSerializerId(datawatcherobject.getSerializer()) < 0) {
         throw new IllegalArgumentException("Unregistered serializer " + datawatcherobject.getSerializer() + " for " + i + "!");
      } else {
         this.setEntry(datawatcherobject, t0);
      }
   }

   private void setEntry(DataParameter datawatcherobject, Object t0) {
      EntityDataManager.DataEntry datawatcher_item = new EntityDataManager.DataEntry(datawatcherobject, t0);
      this.lock.writeLock().lock();
      this.entries.put(Integer.valueOf(datawatcherobject.getId()), datawatcher_item);
      this.empty = false;
      this.lock.writeLock().unlock();
   }

   private EntityDataManager.DataEntry getEntry(DataParameter datawatcherobject) {
      this.lock.readLock().lock();

      EntityDataManager.DataEntry datawatcher_item;
      try {
         datawatcher_item = (EntityDataManager.DataEntry)this.entries.get(Integer.valueOf(datawatcherobject.getId()));
      } catch (Throwable var6) {
         CrashReport crashreport = CrashReport.makeCrashReport(var6, "Getting synched entity data");
         CrashReportCategory crashreportsystemdetails = crashreport.makeCategory("Synched entity data");
         crashreportsystemdetails.addCrashSection("Data ID", datawatcherobject);
         throw new ReportedException(crashreport);
      }

      this.lock.readLock().unlock();
      return datawatcher_item;
   }

   public Object get(DataParameter datawatcherobject) {
      return this.getEntry(datawatcherobject).getValue();
   }

   public void set(DataParameter datawatcherobject, Object t0) {
      EntityDataManager.DataEntry datawatcher_item = this.getEntry(datawatcherobject);
      if (ObjectUtils.notEqual(t0, datawatcher_item.getValue())) {
         datawatcher_item.setValue(t0);
         this.entity.notifyDataManagerChange(datawatcherobject);
         datawatcher_item.setDirty(true);
         this.dirty = true;
      }

   }

   public void setDirty(DataParameter datawatcherobject) {
      this.getEntry(datawatcherobject).dirty = true;
      this.dirty = true;
   }

   public boolean isDirty() {
      return this.dirty;
   }

   public static void writeEntries(List list, PacketBuffer packetdataserializer) throws IOException {
      if (list != null) {
         int i = 0;

         for(int j = list.size(); i < j; ++i) {
            EntityDataManager.DataEntry datawatcher_item = (EntityDataManager.DataEntry)list.get(i);
            writeEntry(packetdataserializer, datawatcher_item);
         }
      }

      packetdataserializer.writeByte(255);
   }

   @Nullable
   public List getDirty() {
      ArrayList arraylist = null;
      if (this.dirty) {
         this.lock.readLock().lock();

         for(EntityDataManager.DataEntry datawatcher_item : this.entries.values()) {
            if (datawatcher_item.isDirty()) {
               datawatcher_item.setDirty(false);
               if (arraylist == null) {
                  arraylist = Lists.newArrayList();
               }

               arraylist.add(datawatcher_item);
            }
         }

         this.lock.readLock().unlock();
      }

      this.dirty = false;
      return arraylist;
   }

   public void writeEntries(PacketBuffer packetdataserializer) throws IOException {
      this.lock.readLock().lock();

      for(EntityDataManager.DataEntry datawatcher_item : this.entries.values()) {
         writeEntry(packetdataserializer, datawatcher_item);
      }

      this.lock.readLock().unlock();
      packetdataserializer.writeByte(255);
   }

   @Nullable
   public List getAll() {
      ArrayList arraylist = null;
      this.lock.readLock().lock();

      for(EntityDataManager.DataEntry datawatcher_item : this.entries.values()) {
         if (arraylist == null) {
            arraylist = Lists.newArrayList();
         }

         arraylist.add(datawatcher_item);
      }

      this.lock.readLock().unlock();
      return arraylist;
   }

   private static void writeEntry(PacketBuffer packetdataserializer, EntityDataManager.DataEntry datawatcher_item) throws IOException {
      DataParameter datawatcherobject = datawatcher_item.getKey();
      int i = DataSerializers.getSerializerId(datawatcherobject.getSerializer());
      if (i < 0) {
         throw new EncoderException("Unknown serializer type " + datawatcherobject.getSerializer());
      } else {
         packetdataserializer.writeByte(datawatcherobject.getId());
         packetdataserializer.writeVarInt(i);
         datawatcherobject.getSerializer().write(packetdataserializer, datawatcher_item.getValue());
      }
   }

   @Nullable
   public static List readEntries(PacketBuffer packetdataserializer) throws IOException {
      ArrayList arraylist = null;

      short short0;
      while((short0 = packetdataserializer.readUnsignedByte()) != 255) {
         if (arraylist == null) {
            arraylist = Lists.newArrayList();
         }

         int i = packetdataserializer.readVarInt();
         DataSerializer datawatcherserializer = DataSerializers.getSerializer(i);
         if (datawatcherserializer == null) {
            throw new DecoderException("Unknown serializer type " + i);
         }

         arraylist.add(new EntityDataManager.DataEntry(datawatcherserializer.createKey(short0), datawatcherserializer.read(packetdataserializer)));
      }

      return arraylist;
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

      public DataEntry(DataParameter datawatcherobject, Object t0) {
         this.key = datawatcherobject;
         this.value = t0;
         this.dirty = true;
      }

      public DataParameter getKey() {
         return this.key;
      }

      public void setValue(Object t0) {
         this.value = t0;
      }

      public Object getValue() {
         return this.value;
      }

      public boolean isDirty() {
         return this.dirty;
      }

      public void setDirty(boolean flag) {
         this.dirty = flag;
      }
   }
}
