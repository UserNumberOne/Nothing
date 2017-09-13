package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class ChunkCache implements IBlockAccess {
   protected int chunkX;
   protected int chunkZ;
   protected Chunk[][] chunkArray;
   protected boolean hasExtendedLevels;
   protected World world;

   public ChunkCache(World var1, BlockPos var2, BlockPos var3, int var4) {
      this.world = var1;
      this.chunkX = var2.getX() - var4 >> 4;
      this.chunkZ = var2.getZ() - var4 >> 4;
      int var5 = var3.getX() + var4 >> 4;
      int var6 = var3.getZ() + var4 >> 4;
      this.chunkArray = new Chunk[var5 - this.chunkX + 1][var6 - this.chunkZ + 1];
      this.hasExtendedLevels = true;

      for(int var7 = this.chunkX; var7 <= var5; ++var7) {
         for(int var8 = this.chunkZ; var8 <= var6; ++var8) {
            this.chunkArray[var7 - this.chunkX][var8 - this.chunkZ] = var1.getChunkFromChunkCoords(var7, var8);
         }
      }

      for(int var10 = var2.getX() >> 4; var10 <= var3.getX() >> 4; ++var10) {
         for(int var11 = var2.getZ() >> 4; var11 <= var3.getZ() >> 4; ++var11) {
            Chunk var9 = this.chunkArray[var10 - this.chunkX][var11 - this.chunkZ];
            if (var9 != null && !var9.getAreLevelsEmpty(var2.getY(), var3.getY())) {
               this.hasExtendedLevels = false;
            }
         }
      }

   }

   @Nullable
   public TileEntity getTileEntity(BlockPos var1) {
      return this.getTileEntity(var1, Chunk.EnumCreateEntityType.IMMEDIATE);
   }

   @Nullable
   public TileEntity getTileEntity(BlockPos var1, Chunk.EnumCreateEntityType var2) {
      int var3 = (var1.getX() >> 4) - this.chunkX;
      int var4 = (var1.getZ() >> 4) - this.chunkZ;
      return this.chunkArray[var3][var4].getTileEntity(var1, var2);
   }

   public IBlockState getBlockState(BlockPos var1) {
      if (var1.getY() >= 0 && var1.getY() < 256) {
         int var2 = (var1.getX() >> 4) - this.chunkX;
         int var3 = (var1.getZ() >> 4) - this.chunkZ;
         if (var2 >= 0 && var2 < this.chunkArray.length && var3 >= 0 && var3 < this.chunkArray[var2].length) {
            Chunk var4 = this.chunkArray[var2][var3];
            if (var4 != null) {
               return var4.getBlockState(var1);
            }
         }
      }

      return Blocks.AIR.getDefaultState();
   }

   public boolean isAirBlock(BlockPos var1) {
      return this.getBlockState(var1).getMaterial() == Material.AIR;
   }

   public int getStrongPower(BlockPos var1, EnumFacing var2) {
      return this.getBlockState(var1).getStrongPower(this, var1, var2);
   }
}
