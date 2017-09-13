package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ChunkCache implements IBlockAccess {
   protected int chunkX;
   protected int chunkZ;
   protected Chunk[][] chunkArray;
   protected boolean hasExtendedLevels;
   protected World world;

   public ChunkCache(World var1, BlockPos var2, BlockPos var3, int var4) {
      this.world = worldIn;
      this.chunkX = posFromIn.getX() - subIn >> 4;
      this.chunkZ = posFromIn.getZ() - subIn >> 4;
      int i = posToIn.getX() + subIn >> 4;
      int j = posToIn.getZ() + subIn >> 4;
      this.chunkArray = new Chunk[i - this.chunkX + 1][j - this.chunkZ + 1];
      this.hasExtendedLevels = true;

      for(int k = this.chunkX; k <= i; ++k) {
         for(int l = this.chunkZ; l <= j; ++l) {
            this.chunkArray[k - this.chunkX][l - this.chunkZ] = worldIn.getChunkFromChunkCoords(k, l);
         }
      }

      for(int i1 = posFromIn.getX() >> 4; i1 <= posToIn.getX() >> 4; ++i1) {
         for(int j1 = posFromIn.getZ() >> 4; j1 <= posToIn.getZ() >> 4; ++j1) {
            Chunk chunk = this.chunkArray[i1 - this.chunkX][j1 - this.chunkZ];
            if (chunk != null && !chunk.getAreLevelsEmpty(posFromIn.getY(), posToIn.getY())) {
               this.hasExtendedLevels = false;
            }
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public boolean extendedLevelsInChunkCache() {
      return this.hasExtendedLevels;
   }

   @Nullable
   public TileEntity getTileEntity(BlockPos var1) {
      return this.getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
   }

   @Nullable
   public TileEntity getTileEntity(BlockPos var1, Chunk.EnumCreateEntityType var2) {
      int i = (p_190300_1_.getX() >> 4) - this.chunkX;
      int j = (p_190300_1_.getZ() >> 4) - this.chunkZ;
      if (i >= 0 && i < this.chunkArray.length && j >= 0 && j < this.chunkArray[i].length) {
         return this.chunkArray[i][j] == null ? null : this.chunkArray[i][j].getTileEntity(p_190300_1_, p_190300_2_);
      } else {
         return null;
      }
   }

   @SideOnly(Side.CLIENT)
   public int getCombinedLight(BlockPos var1, int var2) {
      int i = this.getLightForExt(EnumSkyBlock.SKY, pos);
      int j = this.getLightForExt(EnumSkyBlock.BLOCK, pos);
      if (j < lightValue) {
         j = lightValue;
      }

      return i << 20 | j << 4;
   }

   public IBlockState getBlockState(BlockPos var1) {
      if (pos.getY() >= 0 && pos.getY() < 256) {
         int i = (pos.getX() >> 4) - this.chunkX;
         int j = (pos.getZ() >> 4) - this.chunkZ;
         if (i >= 0 && i < this.chunkArray.length && j >= 0 && j < this.chunkArray[i].length) {
            Chunk chunk = this.chunkArray[i][j];
            if (chunk != null) {
               return chunk.getBlockState(pos);
            }
         }
      }

      return Blocks.AIR.getDefaultState();
   }

   @SideOnly(Side.CLIENT)
   public Biome getBiome(BlockPos var1) {
      int i = (pos.getX() >> 4) - this.chunkX;
      int j = (pos.getZ() >> 4) - this.chunkZ;
      return this.chunkArray[i][j].getBiome(pos, this.world.getBiomeProvider());
   }

   @SideOnly(Side.CLIENT)
   private int getLightForExt(EnumSkyBlock var1, BlockPos var2) {
      if (type == EnumSkyBlock.SKY && this.world.provider.hasNoSky()) {
         return 0;
      } else if (pos.getY() >= 0 && pos.getY() < 256) {
         if (this.getBlockState(pos).useNeighborBrightness()) {
            int l = 0;

            for(EnumFacing enumfacing : EnumFacing.values()) {
               int k = this.getLightFor(type, pos.offset(enumfacing));
               if (k > l) {
                  l = k;
               }

               if (l >= 15) {
                  return l;
               }
            }

            return l;
         } else {
            int i = (pos.getX() >> 4) - this.chunkX;
            int j = (pos.getZ() >> 4) - this.chunkZ;
            if (i >= 0 && i < this.chunkArray.length && j >= 0 && j < this.chunkArray[i].length) {
               return this.chunkArray[i][j] == null ? type.defaultLightValue : this.chunkArray[i][j].getLightFor(type, pos);
            } else {
               return type.defaultLightValue;
            }
         }
      } else {
         return type.defaultLightValue;
      }
   }

   public boolean isAirBlock(BlockPos var1) {
      IBlockState state = this.getBlockState(pos);
      return state.getBlock().isAir(state, this, pos);
   }

   @SideOnly(Side.CLIENT)
   public int getLightFor(EnumSkyBlock var1, BlockPos var2) {
      if (pos.getY() >= 0 && pos.getY() < 256) {
         int i = (pos.getX() >> 4) - this.chunkX;
         int j = (pos.getZ() >> 4) - this.chunkZ;
         return i >= 0 && i < this.chunkArray.length && j >= 0 && j < this.chunkArray[i].length ? this.chunkArray[i][j].getLightFor(p_175628_1_, pos) : p_175628_1_.defaultLightValue;
      } else {
         return p_175628_1_.defaultLightValue;
      }
   }

   public int getStrongPower(BlockPos var1, EnumFacing var2) {
      return this.getBlockState(pos).getStrongPower(this, pos, direction);
   }

   @SideOnly(Side.CLIENT)
   public WorldType getWorldType() {
      return this.world.getWorldType();
   }

   public boolean isSideSolid(BlockPos var1, EnumFacing var2, boolean var3) {
      int x = (pos.getX() >> 4) - this.chunkX;
      int z = (pos.getZ() >> 4) - this.chunkZ;
      if (pos.getY() >= 0 && pos.getY() < 256) {
         if (x >= 0 && x < this.chunkArray.length && z >= 0 && z < this.chunkArray[x].length) {
            if (this.chunkArray[x][z] == null) {
               return _default;
            } else {
               IBlockState state = this.getBlockState(pos);
               return state.getBlock().isSideSolid(state, this, pos, side);
            }
         } else {
            return _default;
         }
      } else {
         return _default;
      }
   }
}
