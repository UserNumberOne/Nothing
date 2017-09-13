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
      for(Entry var3 : var1.entrySet()) {
         if (((String)var3.getKey()).equals("chance")) {
            this.chance = MathHelper.getDouble((String)var3.getValue(), this.chance);
         }
      }

   }

   protected boolean canSpawnStructureAtCoords(int var1, int var2) {
      return this.rand.nextDouble() < this.chance && this.rand.nextInt(80) < Math.max(Math.abs(var1), Math.abs(var2));
   }

   protected StructureStart getStructureStart(int var1, int var2) {
      Biome var3 = this.world.getBiome(new BlockPos((var1 << 4) + 8, 64, (var2 << 4) + 8));
      MapGenMineshaft.Type var4 = var3 instanceof BiomeMesa ? MapGenMineshaft.Type.MESA : MapGenMineshaft.Type.NORMAL;
      return new StructureMineshaftStart(this.world, this.rand, var1, var2, var4);
   }

   public static enum Type {
      NORMAL,
      MESA;

      public static MapGenMineshaft.Type byId(int var0) {
         return var0 >= 0 && var0 < values().length ? values()[var0] : NORMAL;
      }
   }
}
