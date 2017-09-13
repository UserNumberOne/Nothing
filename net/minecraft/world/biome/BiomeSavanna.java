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
      super(properties);
      this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityHorse.class, 1, 2, 6));
      this.theBiomeDecorator.treesPerChunk = 1;
      this.theBiomeDecorator.flowersPerChunk = 4;
      this.theBiomeDecorator.grassPerChunk = 20;
   }

   public WorldGenAbstractTree genBigTreeChance(Random var1) {
      return (WorldGenAbstractTree)(rand.nextInt(5) > 0 ? SAVANNA_TREE : TREE_FEATURE);
   }

   public void decorate(World var1, Random var2, BlockPos var3) {
      DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.GRASS);
      if (TerrainGen.decorate(worldIn, rand, pos, EventType.GRASS)) {
         for(int i = 0; i < 7; ++i) {
            int j = rand.nextInt(16) + 8;
            int k = rand.nextInt(16) + 8;
            int l = rand.nextInt(worldIn.getHeight(pos.add(j, 0, k)).getY() + 32);
            DOUBLE_PLANT_GENERATOR.generate(worldIn, rand, pos.add(j, l, k));
         }
      }

      super.decorate(worldIn, rand, pos);
   }

   public Class getBiomeClass() {
      return BiomeSavanna.class;
   }
}
