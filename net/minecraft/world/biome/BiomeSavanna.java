package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenSavannaTree;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType;

public class BiomeSavanna extends Biome {
   private static final WorldGenSavannaTree SAVANNA_TREE = new WorldGenSavannaTree(false);

   public BiomeSavanna(Biome.BiomeProperties var1) {
      super(var1);
      this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityHorse.class, 1, 2, 6));
      this.theBiomeDecorator.treesPerChunk = 1;
      this.theBiomeDecorator.flowersPerChunk = 4;
      this.theBiomeDecorator.grassPerChunk = 20;
   }

   public WorldGenAbstractTree genBigTreeChance(Random var1) {
      return (WorldGenAbstractTree)(var1.nextInt(5) > 0 ? SAVANNA_TREE : TREE_FEATURE);
   }

   public void decorate(World var1, Random var2, BlockPos var3) {
      DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.GRASS);
      if (TerrainGen.decorate(var1, var2, var3, EventType.GRASS)) {
         for(int var4 = 0; var4 < 7; ++var4) {
            int var5 = var2.nextInt(16) + 8;
            int var6 = var2.nextInt(16) + 8;
            int var7 = var2.nextInt(var1.getHeight(var3.add(var5, 0, var6)).getY() + 32);
            DOUBLE_PLANT_GENERATOR.generate(var1, var2, var3.add(var5, var7, var6));
         }
      }

      super.decorate(var1, var2, var3);
   }

   public Class getBiomeClass() {
      return BiomeSavanna.class;
   }
}
