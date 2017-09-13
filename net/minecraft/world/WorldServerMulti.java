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
      super(var1, var2, new DerivedWorldInfo(var4.getWorldInfo()), var3, var5);
      this.delegate = var4;
      this.borderListener = new IBorderListener() {
         public void onSizeChanged(WorldBorder var1, double var2) {
            WorldServerMulti.this.getWorldBorder().setTransition(var2);
         }

         public void onTransitionStarted(WorldBorder var1, double var2, double var4, long var6) {
            WorldServerMulti.this.getWorldBorder().setTransition(var2, var4, var6);
         }

         public void onCenterChanged(WorldBorder var1, double var2, double var4) {
            WorldServerMulti.this.getWorldBorder().setCenter(var2, var4);
         }

         public void onWarningTimeChanged(WorldBorder var1, int var2) {
            WorldServerMulti.this.getWorldBorder().setWarningTime(var2);
         }

         public void onWarningDistanceChanged(WorldBorder var1, int var2) {
            WorldServerMulti.this.getWorldBorder().setWarningDistance(var2);
         }

         public void onDamageAmountChanged(WorldBorder var1, double var2) {
            WorldServerMulti.this.getWorldBorder().setDamageAmount(var2);
         }

         public void onDamageBufferChanged(WorldBorder var1, double var2) {
            WorldServerMulti.this.getWorldBorder().setDamageBuffer(var2);
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
      String var1 = VillageCollection.fileNameForProvider(this.provider);
      VillageCollection var2 = (VillageCollection)this.perWorldStorage.getOrLoadData(VillageCollection.class, var1);
      if (var2 == null) {
         this.villageCollectionObj = new VillageCollection(this);
         this.perWorldStorage.setData(var1, this.villageCollectionObj);
      } else {
         this.villageCollectionObj = var2;
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
