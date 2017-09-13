package net.minecraft.client.renderer;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.chunk.IRenderChunkFactory;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ViewFrustum {
   protected final RenderGlobal renderGlobal;
   protected final World world;
   protected int countChunksY;
   protected int countChunksX;
   protected int countChunksZ;
   public RenderChunk[] renderChunks;

   public ViewFrustum(World var1, int var2, RenderGlobal var3, IRenderChunkFactory var4) {
      this.renderGlobal = var3;
      this.world = var1;
      this.setCountChunksXYZ(var2);
      this.createRenderChunks(var4);
   }

   protected void createRenderChunks(IRenderChunkFactory var1) {
      int var2 = this.countChunksX * this.countChunksY * this.countChunksZ;
      this.renderChunks = new RenderChunk[var2];
      int var3 = 0;

      for(int var4 = 0; var4 < this.countChunksX; ++var4) {
         for(int var5 = 0; var5 < this.countChunksY; ++var5) {
            for(int var6 = 0; var6 < this.countChunksZ; ++var6) {
               int var7 = (var6 * this.countChunksY + var5) * this.countChunksX + var4;
               this.renderChunks[var7] = var1.create(this.world, this.renderGlobal, var3++);
               this.renderChunks[var7].setPosition(var4 * 16, var5 * 16, var6 * 16);
            }
         }
      }

   }

   public void deleteGlResources() {
      for(RenderChunk var4 : this.renderChunks) {
         var4.deleteGlResources();
      }

   }

   protected void setCountChunksXYZ(int var1) {
      int var2 = var1 * 2 + 1;
      this.countChunksX = var2;
      this.countChunksY = 16;
      this.countChunksZ = var2;
   }

   public void updateChunkPositions(double var1, double var3) {
      int var5 = MathHelper.floor(var1) - 8;
      int var6 = MathHelper.floor(var3) - 8;
      int var7 = this.countChunksX * 16;

      for(int var8 = 0; var8 < this.countChunksX; ++var8) {
         int var9 = this.getBaseCoordinate(var5, var7, var8);

         for(int var10 = 0; var10 < this.countChunksZ; ++var10) {
            int var11 = this.getBaseCoordinate(var6, var7, var10);

            for(int var12 = 0; var12 < this.countChunksY; ++var12) {
               int var13 = var12 * 16;
               RenderChunk var14 = this.renderChunks[(var10 * this.countChunksY + var12) * this.countChunksX + var8];
               var14.setPosition(var9, var13, var11);
            }
         }
      }

   }

   private int getBaseCoordinate(int var1, int var2, int var3) {
      int var4 = var3 * 16;
      int var5 = var4 - var1 + var2 / 2;
      if (var5 < 0) {
         var5 -= var2 - 1;
      }

      return var4 - var5 / var2 * var2;
   }

   public void markBlocksForUpdate(int var1, int var2, int var3, int var4, int var5, int var6, boolean var7) {
      int var8 = MathHelper.intFloorDiv(var1, 16);
      int var9 = MathHelper.intFloorDiv(var2, 16);
      int var10 = MathHelper.intFloorDiv(var3, 16);
      int var11 = MathHelper.intFloorDiv(var4, 16);
      int var12 = MathHelper.intFloorDiv(var5, 16);
      int var13 = MathHelper.intFloorDiv(var6, 16);

      for(int var14 = var8; var14 <= var11; ++var14) {
         int var15 = var14 % this.countChunksX;
         if (var15 < 0) {
            var15 += this.countChunksX;
         }

         for(int var16 = var9; var16 <= var12; ++var16) {
            int var17 = var16 % this.countChunksY;
            if (var17 < 0) {
               var17 += this.countChunksY;
            }

            for(int var18 = var10; var18 <= var13; ++var18) {
               int var19 = var18 % this.countChunksZ;
               if (var19 < 0) {
                  var19 += this.countChunksZ;
               }

               int var20 = (var19 * this.countChunksY + var17) * this.countChunksX + var15;
               RenderChunk var21 = this.renderChunks[var20];
               var21.setNeedsUpdate(var7);
            }
         }
      }

   }

   @Nullable
   protected RenderChunk getRenderChunk(BlockPos var1) {
      int var2 = MathHelper.intFloorDiv(var1.getX(), 16);
      int var3 = MathHelper.intFloorDiv(var1.getY(), 16);
      int var4 = MathHelper.intFloorDiv(var1.getZ(), 16);
      if (var3 >= 0 && var3 < this.countChunksY) {
         var2 = var2 % this.countChunksX;
         if (var2 < 0) {
            var2 += this.countChunksX;
         }

         var4 = var4 % this.countChunksZ;
         if (var4 < 0) {
            var4 += this.countChunksZ;
         }

         int var5 = (var4 * this.countChunksY + var3) * this.countChunksX + var2;
         return this.renderChunks[var5];
      } else {
         return null;
      }
   }
}
