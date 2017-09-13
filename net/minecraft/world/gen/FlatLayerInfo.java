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
      this(3, p_i45467_1_, layerMaterialIn);
   }

   public FlatLayerInfo(int var1, int var2, Block var3) {
      this.layerCount = 1;
      this.version = p_i45627_1_;
      this.layerCount = height;
      this.layerMaterial = layerMaterialIn.getDefaultState();
   }

   public FlatLayerInfo(int var1, int var2, Block var3, int var4) {
      this(p_i45628_1_, p_i45628_2_, layerMaterialIn);
      this.layerMaterial = layerMaterialIn.getStateFromMeta(p_i45628_4_);
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
      this.layerMinimumY = minY;
   }

   public String toString() {
      String s;
      if (this.version >= 3) {
         ResourceLocation resourcelocation = (ResourceLocation)Block.REGISTRY.getNameForObject(this.getLayerMaterialBlock());
         s = resourcelocation == null ? "null" : resourcelocation.toString();
         if (this.layerCount > 1) {
            s = this.layerCount + "*" + s;
         }
      } else {
         s = Integer.toString(Block.getIdFromBlock(this.getLayerMaterialBlock()));
         if (this.layerCount > 1) {
            s = this.layerCount + "x" + s;
         }
      }

      int i = this.getFillBlockMeta();
      if (i > 0) {
         s = s + ":" + i;
      }

      return s;
   }
}
