package net.minecraft.world.gen.feature;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class WorldGenSpikes extends WorldGenerator {
   private boolean crystalInvulnerable;
   private WorldGenSpikes.EndSpike spike;
   private BlockPos beamTarget;

   public void setSpike(WorldGenSpikes.EndSpike var1) {
      this.spike = var1;
   }

   public void setCrystalInvulnerable(boolean var1) {
      this.crystalInvulnerable = var1;
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      if (this.spike == null) {
         throw new IllegalStateException("Decoration requires priming with a spike");
      } else {
         int var4 = this.spike.getRadius();

         for(BlockPos.MutableBlockPos var6 : BlockPos.getAllInBoxMutable(new BlockPos(var3.getX() - var4, 0, var3.getZ() - var4), new BlockPos(var3.getX() + var4, this.spike.getHeight() + 10, var3.getZ() + var4))) {
            if (var6.distanceSq((double)var3.getX(), (double)var6.getY(), (double)var3.getZ()) <= (double)(var4 * var4 + 1) && var6.getY() < this.spike.getHeight()) {
               this.setBlockAndNotifyAdequately(var1, var6, Blocks.OBSIDIAN.getDefaultState());
            } else if (var6.getY() > 65) {
               this.setBlockAndNotifyAdequately(var1, var6, Blocks.AIR.getDefaultState());
            }
         }

         if (this.spike.isGuarded()) {
            for(int var7 = -2; var7 <= 2; ++var7) {
               for(int var9 = -2; var9 <= 2; ++var9) {
                  if (MathHelper.abs(var7) == 2 || MathHelper.abs(var9) == 2) {
                     this.setBlockAndNotifyAdequately(var1, new BlockPos(var3.getX() + var7, this.spike.getHeight(), var3.getZ() + var9), Blocks.IRON_BARS.getDefaultState());
                     this.setBlockAndNotifyAdequately(var1, new BlockPos(var3.getX() + var7, this.spike.getHeight() + 1, var3.getZ() + var9), Blocks.IRON_BARS.getDefaultState());
                     this.setBlockAndNotifyAdequately(var1, new BlockPos(var3.getX() + var7, this.spike.getHeight() + 2, var3.getZ() + var9), Blocks.IRON_BARS.getDefaultState());
                  }

                  this.setBlockAndNotifyAdequately(var1, new BlockPos(var3.getX() + var7, this.spike.getHeight() + 3, var3.getZ() + var9), Blocks.IRON_BARS.getDefaultState());
               }
            }
         }

         EntityEnderCrystal var8 = new EntityEnderCrystal(var1);
         var8.setBeamTarget(this.beamTarget);
         var8.setEntityInvulnerable(this.crystalInvulnerable);
         var8.setLocationAndAngles((double)((float)var3.getX() + 0.5F), (double)(this.spike.getHeight() + 1), (double)((float)var3.getZ() + 0.5F), var2.nextFloat() * 360.0F, 0.0F);
         var1.spawnEntity(var8);
         this.setBlockAndNotifyAdequately(var1, new BlockPos(var3.getX(), this.spike.getHeight(), var3.getZ()), Blocks.BEDROCK.getDefaultState());
         return true;
      }
   }

   public void setBeamTarget(@Nullable BlockPos var1) {
      this.beamTarget = var1;
   }

   public static class EndSpike {
      private final int centerX;
      private final int centerZ;
      private final int radius;
      private final int height;
      private final boolean guarded;
      private final AxisAlignedBB topBoundingBox;

      public EndSpike(int var1, int var2, int var3, int var4, boolean var5) {
         this.centerX = var1;
         this.centerZ = var2;
         this.radius = var3;
         this.height = var4;
         this.guarded = var5;
         this.topBoundingBox = new AxisAlignedBB((double)(var1 - var3), 0.0D, (double)(var2 - var3), (double)(var1 + var3), 256.0D, (double)(var2 + var3));
      }

      public boolean doesStartInChunk(BlockPos var1) {
         int var2 = this.centerX - this.radius;
         int var3 = this.centerZ - this.radius;
         return var1.getX() == (var2 & -16) && var1.getZ() == (var3 & -16);
      }

      public int getCenterX() {
         return this.centerX;
      }

      public int getCenterZ() {
         return this.centerZ;
      }

      public int getRadius() {
         return this.radius;
      }

      public int getHeight() {
         return this.height;
      }

      public boolean isGuarded() {
         return this.guarded;
      }

      public AxisAlignedBB getTopBoundingBox() {
         return this.topBoundingBox;
      }
   }
}
