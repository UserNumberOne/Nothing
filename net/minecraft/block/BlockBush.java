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

   protected BlockBush(Material material) {
      this(material, material.getMaterialMapColor());
   }

   protected BlockBush(Material material, MapColor materialmapcolor) {
      super(material, materialmapcolor);
      this.setTickRandomly(true);
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public boolean canPlaceBlockAt(World world, BlockPos blockposition) {
      return super.canPlaceBlockAt(world, blockposition) && this.canSustainBush(world.getBlockState(blockposition.down()));
   }

   protected boolean canSustainBush(IBlockState iblockdata) {
      return iblockdata.getBlock() == Blocks.GRASS || iblockdata.getBlock() == Blocks.DIRT || iblockdata.getBlock() == Blocks.FARMLAND;
   }

   public void neighborChanged(IBlockState iblockdata, World world, BlockPos blockposition, Block block) {
      super.neighborChanged(iblockdata, world, blockposition, block);
      this.checkAndDropBlock(world, blockposition, iblockdata);
   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      this.checkAndDropBlock(world, blockposition, iblockdata);
   }

   protected void checkAndDropBlock(World world, BlockPos blockposition, IBlockState iblockdata) {
      if (!this.canBlockStay(world, blockposition, iblockdata)) {
         org.bukkit.block.Block block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
         BlockPhysicsEvent event = new BlockPhysicsEvent(block, block.getTypeId());
         world.getServer().getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            return;
         }

         this.dropBlockAsItem(world, blockposition, iblockdata, 0);
         world.setBlockState(blockposition, Blocks.AIR.getDefaultState(), 3);
      }

   }

   public boolean canBlockStay(World world, BlockPos blockposition, IBlockState iblockdata) {
      return this.canSustainBush(world.getBlockState(blockposition.down()));
   }

   public AxisAlignedBB getBoundingBox(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      return BUSH_AABB;
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState iblockdata, World world, BlockPos blockposition) {
      return NULL_AABB;
   }

   public boolean isOpaqueCube(IBlockState iblockdata) {
      return false;
   }

   public boolean isFullCube(IBlockState iblockdata) {
      return false;
   }
}
