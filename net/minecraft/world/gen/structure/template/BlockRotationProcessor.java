package net.minecraft.world.gen.structure.template;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockRotationProcessor implements ITemplateProcessor {
   private final float chance;
   private final Random random;

   public BlockRotationProcessor(BlockPos var1, PlacementSettings var2) {
      this.chance = var2.getIntegrity();
      this.random = var2.getRandom(var1);
   }

   @Nullable
   public Template.BlockInfo processBlock(World var1, BlockPos var2, Template.BlockInfo var3) {
      return this.chance < 1.0F && this.random.nextFloat() > this.chance ? null : var3;
   }
}
