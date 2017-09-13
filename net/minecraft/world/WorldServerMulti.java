package net.minecraft.world;

import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.ISaveHandler;

public class WorldServerMulti extends WorldServer {
   private final WorldServer delegate;
   private IBorderListener borderListener;

   public WorldServerMulti(MinecraftServer var1, ISaveHandler var2, int var3, WorldServer var4, Profiler var5) {
      super(server, saveHandlerIn, new DerivedWorldInfo(delegate.getWorldInfo()), dimensionId, profilerIn);
      this.delegate = delegate;
      this.borderListener = new IBorderListener() {
         public void onSizeChanged(WorldBorder var1, double var2) {
            WorldServerMulti.this.getWorldBorder().setTransition(newSize);
         }

         public void onTransitionStarted(WorldBorder var1, double var2, double var4, long var6) {
            WorldServerMulti.this.getWorldBorder().setTransition(oldSize, newSize, time);
         }

         public void onCenterChanged(WorldBorder var1, double var2, double var4) {
            WorldServerMulti.this.getWorldBorder().setCenter(x, z);
         }

         public void onWarningTimeChanged(WorldBorder var1, int var2) {
            WorldServerMulti.this.getWorldBorder().setWarningTime(newTime);
         }

         public void onWarningDistanceChanged(WorldBorder var1, int var2) {
            WorldServerMulti.this.getWorldBorder().setWarningDistance(newDistance);
         }

         public void onDamageAmountChanged(WorldBorder var1, double var2) {
            WorldServerMulti.this.getWorldBorder().setDamageAmount(newAmount);
         }

         public void onDamageBufferChanged(WorldBorder var1, double var2) {
            WorldServerMulti.this.getWorldBorder().setDamageBuffer(newSize);
         }
      };
      this.delegate.getWorldBorder().addListener(this.borderListener);
   }

   protected void saveLevel() throws MinecraftException {
      this.perWorldStorage.saveAllData();
   }

   public World init() {
      this.mapStorage = this.delegate.getMapStorage();
      this.worldScoreboard = this.delegate.getScoreboard();
      this.lootTable = this.delegate.getLootTableManager();
      String s = VillageCollection.fileNameForProvider(this.provider);
      VillageCollection villagecollection = (VillageCollection)this.perWorldStorage.getOrLoadData(VillageCollection.class, s);
      if (villagecollection == null) {
         this.villageCollectionObj = new VillageCollection(this);
         this.perWorldStorage.setData(s, this.villageCollectionObj);
      } else {
         this.villageCollectionObj = villagecollection;
         this.villageCollectionObj.setWorldsForAll(this);
      }

      this.initCapabilities();
      return this;
   }

   public void flush() {
      super.flush();
      this.delegate.getWorldBorder().removeListener(this.borderListener);
   }

   public void saveAdditionalData() {
      this.provider.onWorldSave();
   }
}
