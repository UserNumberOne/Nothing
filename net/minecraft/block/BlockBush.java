package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.event.block.BlockPhysicsEvent;

public class BlockBush extends Block {
   protected static final AxisAlignedBB BUSH_AABB = new AxisAlignedBB(0.30000001192092896D, 0.0D, 0.30000001192092896D, 0.699999988079071D, 0.6000000238418579D, 0.699999988079071D);

   protected BlockBush() {
      this(Material.PLANTS);
   }

   protected BlockBush(Material var1) {
      this(var1, var1.getMaterialMapColor());
   }

   protected BlockBush(Material var1, MapColor var2) {
      super(var1, var2);
      this.setTickRandomly(true);
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return super.canPlaceBlockAt(var1, var2) && this.canSustainBush(var1.getBlockState(var2.down()));
   }

   protected boolean canSustainBush(IBlockState var1) {
      return var1.getBlock() == Blocks.GRASS || var1.getBlock() == Blocks.DIRT || var1.getBlock() == Blocks.FARMLAND;
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      super.neighborChanged(var1, var2, var3, var4);
      this.checkAndDropBlock(var2, var3, var1);
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      this.checkAndDropBlock(var1, var2, var3);
   }

   protected void checkAndDropBlock(World var1, BlockPos var2, IBlockState var3) {
      if (!this.canBlockStay(var1, var2, var3)) {
         org.bukkit.block.Block var4 = var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ());
         BlockPhysicsEvent var5 = new BlockPhysicsEvent(var4, var4.getTypeId());
         var1.getServer().getPluginManager().callEvent(var5);
         if (var5.isCancelled()) {
            return;
         }

         this.dropBlockAsItem(var1, var2, var3, 0);
         var1.setBlockState(var2, Blocks.AIR.getDefaultState(), 3);
      }

   }

   public boolean canBlockStay(World var1, BlockPos var2, IBlockState var3) {
      return this.canSustainBush(var1.getBlockState(var2.down()));
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return BUSH_AABB;
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return NULL_AABB;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }
}
