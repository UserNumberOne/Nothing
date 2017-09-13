package net.minecraft.block.material;

public class MaterialTransparent extends Material {
   public MaterialTransparent(MapColor var1) {
      super(var1);
      this.setReplaceable();
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
