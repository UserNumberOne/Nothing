package net.minecraft.world.storage;

import javax.annotation.Nullable;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SaveDataMemoryStorage extends MapStorage {
   public SaveDataMemoryStorage() {
      super((ISaveHandler)null);
   }

   @Nullable
   public WorldSavedData getOrLoadData(Class var1, String var2) {
      return (WorldSavedData)this.loadedDataMap.get(dataIdentifier);
   }

   public void setData(String var1, WorldSavedData var2) {
      this.loadedDataMap.put(dataIdentifier, data);
   }

   public void saveAllData() {
   }

   public int getUniqueDataId(String var1) {
      return 0;
   }
}
