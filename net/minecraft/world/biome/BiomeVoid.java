package net.minecraft.world.biome;

public class BiomeVoid extends Biome {
   public BiomeVoid(Biome.BiomeProperties var1) {
      super(var1);
      this.spawnableMonsterList.clear();
      this.spawnableCreatureList.clear();
      this.spawnableWaterCreatureList.clear();
      this.spawnableCaveCreatureList.clear();
      this.theBiomeDecorator = new BiomeVoidDecorator();
   }

   public boolean ignorePlayerSpawnSuitability() {
      return true;
   }
}
