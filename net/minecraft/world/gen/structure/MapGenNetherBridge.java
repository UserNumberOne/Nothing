package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class MapGenNetherBridge extends MapGenStructure {
   private final List spawnList = Lists.newArrayList();

   public MapGenNetherBridge() {
      this.spawnList.add(new Biome.SpawnListEntry(EntityBlaze.class, 10, 2, 3));
      this.spawnList.add(new Biome.SpawnListEntry(EntityPigZombie.class, 5, 4, 4));
      this.spawnList.add(new Biome.SpawnListEntry(EntitySkeleton.class, 10, 4, 4));
      this.spawnList.add(new Biome.SpawnListEntry(EntityMagmaCube.class, 3, 4, 4));
   }

   public String getStructureName() {
      return "Fortress";
   }

   public List getSpawnList() {
      return this.spawnList;
   }

   protected boolean canSpawnStructureAtCoords(int var1, int var2) {
      int var3 = var1 >> 4;
      int var4 = var2 >> 4;
      this.rand.setSeed((long)(var3 ^ var4 << 4) ^ this.world.getSeed());
      this.rand.nextInt();
      if (this.rand.nextInt(3) != 0) {
         return false;
      } else if (var1 != (var3 << 4) + 4 + this.rand.nextInt(8)) {
         return false;
      } else {
         return var2 == (var4 << 4) + 4 + this.rand.nextInt(8);
      }
   }

   protected StructureStart getStructureStart(int var1, int var2) {
      return new MapGenNetherBridge.Start(this.world, this.rand, var1, var2);
   }

   public static class Start extends StructureStart {
      public Start() {
      }

      public Start(World var1, Random var2, int var3, int var4) {
         super(var3, var4);
         StructureNetherBridgePieces.Start var5 = new StructureNetherBridgePieces.Start(var2, (var3 << 4) + 2, (var4 << 4) + 2);
         this.components.add(var5);
         var5.buildComponent(var5, this.components, var2);
         List var6 = var5.pendingChildren;

         while(!var6.isEmpty()) {
            int var7 = var2.nextInt(var6.size());
            StructureComponent var8 = (StructureComponent)var6.remove(var7);
            var8.buildComponent(var5, this.components, var2);
         }

         this.updateBoundingBox();
         this.setRandomHeight(var1, var2, 48, 70);
      }
   }
}
