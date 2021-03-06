package net.minecraft.world.gen;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

public class FlatLayerInfo {
   private final int version;
   private IBlockState layerMaterial;
   private int layerCount;
   private int layerMinimumY;

   public FlatLayerInfo(int var1, Block var2) {
      this(3, var1, var2);
   }

   public FlatLayerInfo(int var1, int var2, Block var3) {
      this.layerCount = 1;
      this.version = var1;
      this.layerCount = var2;
      this.layerMaterial = var3.getDefaultState();
   }

   public FlatLayerInfo(int var1, int var2, Block var3, int var4) {
      this(var1, var2, var3);
      this.layerMaterial = var3.getStateFromMeta(var4);
   }

   public int getLayerCount() {
      return this.layerCount;
   }

   public IBlockState getLayerMaterial() {
      return this.layerMaterial;
   }

   private Block getLayerMaterialBlock() {
      return this.layerMaterial.getBlock();
   }

   private int getFillBlockMeta() {
      return this.layerMaterial.getBlock().getMetaFromState(this.layerMaterial);
   }

   public int getMinY() {
      return this.layerMinimumY;
   }

   public void setMinY(int var1) {
      this.layerMinimumY = var1;
   }

   public String toString() {
      String var2;
      if (this.version >= 3) {
         ResourceLocation var1 = (ResourceLocation)Block.REGISTRY.getNameForObject(this.getLayerMaterialBlock());
         var2 = var1 == null ? "null" : var1.toString();
         if (this.layerCount > 1) {
            var2 = this.layerCount + "*" + var2;
         }
      } else {
         var2 = Integer.toString(Block.getIdFromBlock(this.getLayerMaterialBlock()));
         if (this.layerCount > 1) {
            var2 = this.layerCount + "x" + var2;
         }
      }

      int var3 = this.getFillBlockMeta();
      if (var3 > 0) {
         var2 = var2 + ":" + var3;
      }

      return var2;
   }
}
