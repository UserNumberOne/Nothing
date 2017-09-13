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

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return FARMLAND_AABB;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      int var5 = ((Integer)var3.getValue(MOISTURE)).intValue();
      if (!this.hasWater(var1, var2) && !var1.isRainingAt(var2.up())) {
         if (var5 > 0) {
            var1.setBlockState(var2, var3.withProperty(MOISTURE, Integer.valueOf(var5 - 1)), 2);
         } else if (!this.hasCrops(var1, var2)) {
            org.bukkit.block.Block var6 = var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ());
            if (CraftEventFactory.callBlockFadeEvent(var6, Blocks.DIRT).isCancelled()) {
               return;
            }

            var1.setBlockState(var2, Blocks.DIRT.getDefaultState());
         }
      } else if (var5 < 7) {
         var1.setBlockState(var2, var3.withProperty(MOISTURE, Integer.valueOf(7)), 2);
      }

   }

   public void onFallenUpon(World var1, BlockPos var2, Entity var3, float var4) {
      super.onFallenUpon(var1, var2, var3, var4);
      if (!var1.isRemote && var1.rand.nextFloat() < var4 - 0.5F && var3 instanceof EntityLivingBase && (var3 instanceof EntityPlayer || var1.getGameRules().getBoolean("mobGriefing")) && var3.width * var3.width * var3.height > 0.512F) {
         Object var5;
         if (var3 instanceof EntityPlayer) {
            var5 = CraftEventFactory.callPlayerInteractEvent((EntityPlayer)var3, Action.PHYSICAL, var2, (EnumFacing)null, (ItemStack)null, (EnumHand)null);
         } else {
            var5 = new EntityInteractEvent(var3.getBukkitEntity(), var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ()));
            var1.getServer().getPluginManager().callEvent((EntityInteractEvent)var5);
         }

         if (((Cancellable)var5).isCancelled()) {
            return;
         }

         if (CraftEventFactory.callEntityChangeBlockEvent(var3, var2, Blocks.DIRT, 0).isCancelled()) {
            return;
         }

         var1.setBlockState(var2, Blocks.DIRT.getDefaultState());
      }

   }

   private boolean hasCrops(World var1, BlockPos var2) {
      Block var3 = var1.getBlockState(var2.up()).getBlock();
      return var3 instanceof BlockCrops || var3 instanceof BlockStem;
   }

   private boolean hasWater(World var1, BlockPos var2) {
      for(BlockPos.MutableBlockPos var4 : BlockPos.getAllInBoxMutable(var2.add(-4, 0, -4), var2.add(4, 1, 4))) {
         if (var1.getBlockState(var4).getMaterial() == Material.WATER) {
            return true;
         }
      }

      return false;
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      super.neighborChanged(var1, var2, var3, var4);
      if (var2.getBlockState(var3.up()).getMaterial().isSolid()) {
         var2.setBlockState(var3, Blocks.DIRT.getDefaultState());
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Blocks.DIRT.getItemDropped(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT), var2, var3);
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Blocks.DIRT);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(MOISTURE, Integer.valueOf(var1 & 7));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)var1.getValue(MOISTURE)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{MOISTURE});
   }
}
