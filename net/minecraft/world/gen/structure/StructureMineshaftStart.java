package net.minecraft.world.gen.structure;

import java.util.Random;
import net.minecraft.world.World;

public class StructureMineshaftStart extends StructureStart {
   private MapGenMineshaft.Type mineShaftType;

   public StructureMineshaftStart() {
   }

   public StructureMineshaftStart(World var1, Random var2, int var3, int var4, MapGenMineshaft.Type var5) {
      super(var3, var4);
      this.mineShaftType = var5;
      StructureMineshaftPieces.Room var6 = new StructureMineshaftPieces.Room(0, var2, (var3 << 4) + 2, (var4 << 4) + 2, this.mineShaftType);
      this.components.add(var6);
      var6.buildComponent(var6, this.components, var2);
      this.updateBoundingBox();
      if (var5 == MapGenMineshaft.Type.MESA) {
         boolean var7 = true;
         int var8 = var1.getSeaLevel() - this.boundingBox.maxY + this.boundingBox.getYSize() / 2 - -5;
         this.boundingBox.offset(0, var8, 0);

         for(StructureComponent var10 : this.components) {
            var10.offset(0, var8, 0);
         }
      } else {
         this.markAvailableHeight(var1, var2, 10);
      }

   }
}
