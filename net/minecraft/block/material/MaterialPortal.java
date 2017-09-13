package net.minecraft.block.material;

public class MaterialPortal extends Material {
   public MaterialPortal(MapColor var1) {
      super(var1);
   }

   public boolean isSolid() {
      return false;
   }

   public boolean blocksLight() {
      return false;
   }

   public boolean blocksMovement() {
      return false;
   }
}
