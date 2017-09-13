package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenIcePath;
import net.minecraft.world.gen.feature.WorldGenIceSpike;
import net.minecraft.world.gen.feature.WorldGenTaiga2;

public class BiomeSnow extends Biome {
   private final boolean superIcy;
   private final WorldGenIceSpike iceSpike = new WorldGenIceSpike();
   private final WorldGenIcePath icePatch = new WorldGenIcePath(4);

   public BiomeSnow(boolean var1, Biome.BiomeProperties var2) {
      super(var2);
      this.superIcy = var1;
      if (var1) {
         this.topBlock = Blocks.SNOW.getDefaultState();
      }

      this.spawnableCreatureList.clear();
      this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityRabbit.class, 10, 2, 3));
      this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityPolarBear.class, 1, 1, 2));
   }

   public float getSpawningChance() {
      return 0.07F;
   }

   public void decorate(World var1, Random var2, BlockPos var3) {
      if (this.superIcy) {
         for(int var4 = 0; var4 < 3; ++var4) {
            int var5 = var2.nextInt(16) + 8;
            int var6 = var2.nextInt(16) + 8;
            this.iceSpike.generate(var1, var2, var1.getHeight(var3.add(var5, 0, var6)));
         }

         for(int var7 = 0; var7 < 2; ++var7) {
            int var8 = var2.nextInt(16) + 8;
            int var9 = var2.nextInt(16) + 8;
            this.icePatch.generate(var1, var2, var1.getHeight(var3.add(var8, 0, var9)));
         }
      }

      super.decorate(var1, var2, var3);
   }

   public WorldGenAbstractTree genBigTreeChance(Random var1) {
      return new WorldGenTaiga2(false);
   }
}
