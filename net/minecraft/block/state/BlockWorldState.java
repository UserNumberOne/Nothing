package net.minecraft.block.state;

import com.google.common.base.Predicate;
import javax.annotation.Nullable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockWorldState {
   private final World world;
   private final BlockPos pos;
   private final boolean forceLoad;
   private IBlockState state;
   private TileEntity tileEntity;
   private boolean tileEntityInitialized;

   public BlockWorldState(World var1, BlockPos var2, boolean var3) {
      this.world = var1;
      this.pos = var2;
      this.forceLoad = var3;
   }

   public IBlockState getBlockState() {
      if (this.state == null && (this.forceLoad || this.world.isBlockLoaded(this.pos))) {
         this.state = this.world.getBlockState(this.pos);
      }

      return this.state;
   }

   @Nullable
   public TileEntity getTileEntity() {
      if (this.tileEntity == null && !this.tileEntityInitialized) {
         this.tileEntity = this.world.getTileEntity(this.pos);
         this.tileEntityInitialized = true;
      }

      return this.tileEntity;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public static Predicate hasState(final Predicate var0) {
      return new Predicate() {
         public boolean apply(@Nullable BlockWorldState var1) {
            return var1 != null && var0.apply(var1.getBlockState());
         }
      };
   }
}
