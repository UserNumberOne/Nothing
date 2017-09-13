package net.minecraft.world.chunk;

import com.google.common.base.Predicate;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EmptyChunk extends Chunk {
   public EmptyChunk(World var1, int var2, int var3) {
      super(worldIn, x, z);
   }

   public boolean isAtLocation(int var1, int var2) {
      return x == this.xPosition && z == this.zPosition;
   }

   public int getHeightValue(int var1, int var2) {
      return 0;
   }

   public void generateHeightMap() {
   }

   public void generateSkylightMap() {
   }

   public IBlockState getBlockState(BlockPos var1) {
      return Blocks.AIR.getDefaultState();
   }

   public int getBlockLightOpacity(BlockPos var1) {
      return 255;
   }

   public int getLightFor(EnumSkyBlock var1, BlockPos var2) {
      return p_177413_1_.defaultLightValue;
   }

   public void setLightFor(EnumSkyBlock var1, BlockPos var2, int var3) {
   }

   public int getLightSubtracted(BlockPos var1, int var2) {
      return 0;
   }

   public void addEntity(Entity var1) {
   }

   public void removeEntity(Entity var1) {
   }

   public void removeEntityAtIndex(Entity var1, int var2) {
   }

   public boolean canSeeSky(BlockPos var1) {
      return false;
   }

   @Nullable
   public TileEntity getTileEntity(BlockPos var1, Chunk.EnumCreateEntityType var2) {
      return null;
   }

   public void addTileEntity(TileEntity var1) {
   }

   public void addTileEntity(BlockPos var1, TileEntity var2) {
   }

   public void removeTileEntity(BlockPos var1) {
   }

   public void onChunkLoad() {
   }

   public void onChunkUnload() {
   }

   public void setChunkModified() {
   }

   public void getEntitiesWithinAABBForEntity(@Nullable Entity var1, AxisAlignedBB var2, List var3, Predicate var4) {
   }

   public void getEntitiesOfTypeWithinAAAB(Class var1, AxisAlignedBB var2, List var3, Predicate var4) {
   }

   public boolean needsSaving(boolean var1) {
      return false;
   }

   public Random getRandomWithSeed(long var1) {
      return new Random(this.getWorld().getSeed() + (long)(this.xPosition * this.xPosition * 4987142) + (long)(this.xPosition * 5947611) + (long)(this.zPosition * this.zPosition) * 4392871L + (long)(this.zPosition * 389711) ^ seed);
   }

   public boolean isEmpty() {
      return true;
   }

   public boolean getAreLevelsEmpty(int var1, int var2) {
      return true;
   }
}
