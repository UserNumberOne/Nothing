package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;

public class BlockFarmland extends Block {
   public static final PropertyInteger MOISTURE = PropertyInteger.create("moisture", 0, 7);
   protected static final AxisAlignedBB FARMLAND_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.9375D, 1.0D);

   protected BlockFarmland() {
      super(Material.GROUND);
      this.setDefaultState(this.blockState.getBaseState().withProperty(MOISTURE, Integer.valueOf(0)));
      this.setTickRandomly(true);
      this.setLightOpacity(255);
   }

   public AxisAlignedBB getBoundingBox(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      return FARMLAND_AABB;
   }

   public boolean isOpaqueCube(IBlockState iblockdata) {
      return false;
   }

   public boolean isFullCube(IBlockState iblockdata) {
      return false;
   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      int i = ((Integer)iblockdata.getValue(MOISTURE)).intValue();
      if (!this.hasWater(world, blockposition) && !world.isRainingAt(blockposition.up())) {
         if (i > 0) {
            world.setBlockState(blockposition, iblockdata.withProperty(MOISTURE, Integer.valueOf(i - 1)), 2);
         } else if (!this.hasCrops(world, blockposition)) {
            org.bukkit.block.Block block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
            if (CraftEventFactory.callBlockFadeEvent(block, Blocks.DIRT).isCancelled()) {
               return;
            }

            world.setBlockState(blockposition, Blocks.DIRT.getDefaultState());
         }
      } else if (i < 7) {
         world.setBlockState(blockposition, iblockdata.withProperty(MOISTURE, Integer.valueOf(7)), 2);
      }

   }

   public void onFallenUpon(World world, BlockPos blockposition, Entity entity, float f) {
      super.onFallenUpon(world, blockposition, entity, f);
      if (!world.isRemote && world.rand.nextFloat() < f - 0.5F && entity instanceof EntityLivingBase && (entity instanceof EntityPlayer || world.getGameRules().getBoolean("mobGriefing")) && entity.width * entity.width * entity.height > 0.512F) {
         Cancellable cancellable;
         if (entity instanceof EntityPlayer) {
            cancellable = CraftEventFactory.callPlayerInteractEvent((EntityPlayer)entity, Action.PHYSICAL, blockposition, (EnumFacing)null, (ItemStack)null, (EnumHand)null);
         } else {
            cancellable = new EntityInteractEvent(entity.getBukkitEntity(), world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()));
            world.getServer().getPluginManager().callEvent((EntityInteractEvent)cancellable);
         }

         if (cancellable.isCancelled()) {
            return;
         }

         if (CraftEventFactory.callEntityChangeBlockEvent(entity, blockposition, Blocks.DIRT, 0).isCancelled()) {
            return;
         }

         world.setBlockState(blockposition, Blocks.DIRT.getDefaultState());
      }

   }

   private boolean hasCrops(World world, BlockPos blockposition) {
      Block block = world.getBlockState(blockposition.up()).getBlock();
      return block instanceof BlockCrops || block instanceof BlockStem;
   }

   private boolean hasWater(World world, BlockPos blockposition) {
      for(BlockPos.MutableBlockPos blockposition_mutableblockposition : BlockPos.getAllInBoxMutable(blockposition.add(-4, 0, -4), blockposition.add(4, 1, 4))) {
         if (world.getBlockState(blockposition_mutableblockposition).getMaterial() == Material.WATER) {
            return true;
         }
      }

      return false;
   }

   public void neighborChanged(IBlockState iblockdata, World world, BlockPos blockposition, Block block) {
      super.neighborChanged(iblockdata, world, blockposition, block);
      if (world.getBlockState(blockposition.up()).getMaterial().isSolid()) {
         world.setBlockState(blockposition, Blocks.DIRT.getDefaultState());
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState iblockdata, Random random, int i) {
      return Blocks.DIRT.getItemDropped(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT), random, i);
   }

   public ItemStack getItem(World world, BlockPos blockposition, IBlockState iblockdata) {
      return new ItemStack(Blocks.DIRT);
   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(MOISTURE, Integer.valueOf(i & 7));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      return ((Integer)iblockdata.getValue(MOISTURE)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{MOISTURE});
   }
}
