package net.minecraft.world.gen.structure;

import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeMesa;

public class MapGenMineshaft extends MapGenStructure {
   private double chance = 0.004D;

   public MapGenMineshaft() {
   }

   public String getStructureName() {
      return "Mineshaft";
   }

   public MapGenMineshaft(Map var1) {
      for(Entry entry : p_i2034_1_.entrySet()) {
         if (((String)entry.getKey()).equals("chance")) {
            this.chance = MathHelper.getDouble((String)entry.getValue(), this.chance);
         }
      }

   }

   protected boolean canSpawnStructureAtCoords(int var1, int var2) {
      return this.rand.nextDouble() < this.chance && this.rand.nextInt(80) < Math.max(Math.abs(chunkX), Math.abs(chunkZ));
   }

   protected StructureStart getStructureStart(int var1, int var2) {
      Biome biome = this.world.getBiome(new BlockPos((chunkX << 4) + 8, 64, (chunkZ << 4) + 8));
      MapGenMineshaft.Type mapgenmineshaft$type = biome instanceof BiomeMesa ? MapGenMineshaft.Type.MESA : MapGenMineshaft.Type.NORMAL;
      return new StructureMineshaftStart(this.world, this.rand, chunkX, chunkZ, mapgenmineshaft$type);
   }

   public static enum Type {
      NORMAL,
      MESA;

      public static MapGenMineshaft.Type byId(int var0) {
         return id >= 0 && id < values().length ? values()[id] : NORMAL;
      }
   }
}
