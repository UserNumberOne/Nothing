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

   public WorldServerMulti(MinecraftServer minecraftserver, ISaveHandler idatamanager, int i, WorldServer worldserver, Profiler methodprofiler, WorldInfo worldData, Environment env, ChunkGenerator gen) {
      super(minecraftserver, idatamanager, worldData, i, methodprofiler, env, gen);
      this.delegate = worldserver;
   }

   public World init() {
      this.mapStorage = this.delegate.getMapStorage();
      this.worldScoreboard = this.delegate.getScoreboard();
      this.lootTable = this.delegate.getLootTableManager();
      String s = VillageCollection.fileNameForProvider(this.provider);
      VillageCollection persistentvillage = (VillageCollection)this.mapStorage.getOrLoadData(VillageCollection.class, s);
      if (persistentvillage == null) {
         this.villageCollectionObj = new VillageCollection(this);
         this.mapStorage.setData(s, this.villageCollectionObj);
      } else {
         this.villageCollectionObj = persistentvillage;
         this.villageCollectionObj.setWorldsForAll(this);
      }

      return super.init();
   }

   public void saveAdditionalData() {
      this.provider.onWorldSave();
   }
}
