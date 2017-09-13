package net.minecraft.world.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.world.WorldSavedData;

public class MapStorage {
   private final ISaveHandler saveHandler;
   protected Map loadedDataMap = Maps.newHashMap();
   private final List loadedDataList = Lists.newArrayList();
   private final Map idCounts = Maps.newHashMap();

   public MapStorage(ISaveHandler var1) {
      this.saveHandler = var1;
      this.loadIdCounts();
   }

   @Nullable
   public WorldSavedData getOrLoadData(Class var1, String var2) {
      WorldSavedData var3 = (WorldSavedData)this.loadedDataMap.get(var2);
      if (var3 != null) {
         return var3;
      } else {
         if (this.saveHandler != null) {
            try {
               File var4 = this.saveHandler.getMapFileFromName(var2);
               if (var4 != null && var4.exists()) {
                  try {
                     var3 = (WorldSavedData)var1.getConstructor(String.class).newInstance(var2);
                  } catch (Exception var7) {
                     throw new RuntimeException("Failed to instantiate " + var1, var7);
                  }

                  FileInputStream var5 = new FileInputStream(var4);
                  NBTTagCompound var6 = CompressedStreamTools.readCompressed(var5);
                  var5.close();
                  var3.readFromNBT(var6.getCompoundTag("data"));
               }
            } catch (Exception var8) {
               var8.printStackTrace();
            }
         }

         if (var3 != null) {
            this.loadedDataMap.put(var2, var3);
            this.loadedDataList.add(var3);
         }

         return var3;
      }
   }

   public void setData(String var1, WorldSavedData var2) {
      if (this.loadedDataMap.containsKey(var1)) {
         this.loadedDataList.remove(this.loadedDataMap.remove(var1));
      }

      this.loadedDataMap.put(var1, var2);
      this.loadedDataList.add(var2);
   }

   public void saveAllData() {
      for(int var1 = 0; var1 < this.loadedDataList.size(); ++var1) {
         WorldSavedData var2 = (WorldSavedData)this.loadedDataList.get(var1);
         if (var2.isDirty()) {
            this.saveData(var2);
            var2.setDirty(false);
         }
      }

   }

   private void saveData(WorldSavedData var1) {
      if (this.saveHandler != null) {
         try {
            File var2 = this.saveHandler.getMapFileFromName(var1.mapName);
            if (var2 != null) {
               NBTTagCompound var3 = new NBTTagCompound();
               var3.setTag("data", var1.writeToNBT(new NBTTagCompound()));
               FileOutputStream var4 = new FileOutputStream(var2);
               CompressedStreamTools.writeCompressed(var3, var4);
               var4.close();
            }
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }

   }

   private void loadIdCounts() {
      try {
         this.idCounts.clear();
         if (this.saveHandler == null) {
            return;
         }

         File var1 = this.saveHandler.getMapFileFromName("idcounts");
         if (var1 != null && var1.exists()) {
            DataInputStream var2 = new DataInputStream(new FileInputStream(var1));
            NBTTagCompound var3 = CompressedStreamTools.read(var2);
            var2.close();

            for(String var5 : var3.getKeySet()) {
               NBTBase var6 = var3.getTag(var5);
               if (var6 instanceof NBTTagShort) {
                  NBTTagShort var7 = (NBTTagShort)var6;
                  short var8 = var7.getShort();
                  this.idCounts.put(var5, Short.valueOf(var8));
               }
            }
         }
      } catch (Exception var9) {
         var9.printStackTrace();
      }

   }

   public int getUniqueDataId(String var1) {
      Short var2 = (Short)this.idCounts.get(var1);
      if (var2 == null) {
         var2 = 0;
      } else {
         var2 = (short)(var2.shortValue() + 1);
      }

      this.idCounts.put(var1, var2);
      if (this.saveHandler == null) {
         return var2.shortValue();
      } else {
         try {
            File var3 = this.saveHandler.getMapFileFromName("idcounts");
            if (var3 != null) {
               NBTTagCompound var4 = new NBTTagCompound();

               for(String var6 : this.idCounts.keySet()) {
                  var4.setShort(var6, ((Short)this.idCounts.get(var6)).shortValue());
               }

               DataOutputStream var9 = new DataOutputStream(new FileOutputStream(var3));
               CompressedStreamTools.write(var4, var9);
               var9.close();
            }
         } catch (Exception var7) {
            var7.printStackTrace();
         }

         return var2.shortValue();
      }
   }
}
