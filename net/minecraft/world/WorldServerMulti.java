package net.minecraft.world;

import net.minecraft.profiler.Profiler;
import net.minecraft.src.MinecraftServer;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.bukkit.World.Environment;
import org.bukkit.generator.ChunkGenerator;

public class WorldServerMulti extends WorldServer {
   private final WorldServer delegate;

   public WorldServerMulti(MinecraftServer var1, ISaveHandler var2, int var3, WorldServer var4, Profiler var5, WorldInfo var6, Environment var7, ChunkGenerator var8) {
      super(var1, var2, var6, var3, var5, var7, var8);
      this.delegate = var4;
   }

   public World init() {
      this.mapStorage = this.delegate.getMapStorage();
      this.worldScoreboard = this.delegate.getScoreboard();
      this.lootTable = this.delegate.getLootTableManager();
      String var1 = VillageCollection.fileNameForProvider(this.provider);
      VillageCollection var2 = (VillageCollection)this.mapStorage.getOrLoadData(VillageCollection.class, var1);
      if (var2 == null) {
         this.villageCollectionObj = new VillageCollection(this);
         this.mapStorage.setData(var1, this.villageCollectionObj);
      } else {
         this.villageCollectionObj = var2;
         this.villageCollectionObj.setWorldsForAll(this);
      }

      return super.init();
   }

   public void saveAdditionalData() {
      this.provider.onWorldSave();
   }
}
