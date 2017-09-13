package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockRedstoneOre extends Block {
   private final boolean isOn;

   public BlockRedstoneOre(boolean flag) {
      super(Material.ROCK);
      if (flag) {
         this.setTickRandomly(true);
      }

      this.isOn = flag;
   }

   public int tickRate(World world) {
      return 30;
   }

   public void onBlockClicked(World world, BlockPos blockposition, EntityPlayer entityhuman) {
      this.interact(world, blockposition, entityhuman);
      super.onBlockClicked(world, blockposition, entityhuman);
   }

   public void onEntityWalk(World world, BlockPos blockposition, Entity entity) {
      if (entity instanceof EntityPlayer) {
         PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent((EntityPlayer)entity, Action.PHYSICAL, blockposition, (EnumFacing)null, (ItemStack)null, (EnumHand)null);
         if (!event.isCancelled()) {
            this.interact(world, blockposition, entity);
            super.onEntityWalk(world, blockposition, entity);
         }
      } else {
         EntityInteractEvent event = new EntityInteractEvent(entity.getBukkitEntity(), world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()));
         world.getServer().getPluginManager().callEvent(event);
         if (!event.isCancelled()) {
            this.interact(world, blockposition, entity);
            super.onEntityWalk(world, blockposition, entity);
         }
      }

   }

   public boolean onBlockActivated(World world, BlockPos blockposition, IBlockState iblockdata, EntityPlayer entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack, EnumFacing enumdirection, float f, float f1, float f2) {
      this.interact(world, blockposition, entityhuman);
      return super.onBlockActivated(world, blockposition, iblockdata, entityhuman, enumhand, itemstack, enumdirection, f, f1, f2);
   }

   private void interact(World world, BlockPos blockposition, Entity entity) {
      this.spawnParticles(world, blockposition);
      if (this == Blocks.REDSTONE_ORE) {
         if (CraftEventFactory.callEntityChangeBlockEvent(entity, blockposition, Blocks.LIT_REDSTONE_ORE, 0).isCancelled()) {
            return;
         }

         world.setBlockState(blockposition, Blocks.LIT_REDSTONE_ORE.getDefaultState());
      }

   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      if (this == Blocks.LIT_REDSTONE_ORE) {
         if (CraftEventFactory.callBlockFadeEvent(world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()), Blocks.REDSTONE_ORE).isCancelled()) {
            return;
         }

         world.setBlockState(blockposition, Blocks.REDSTONE_ORE.getDefaultState());
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState iblockdata, Random random, int i) {
      return Items.REDSTONE;
   }

   public int quantityDroppedWithBonus(int i, Random random) {
      return this.quantityDropped(random) + random.nextInt(i + 1);
   }

   public int quantityDropped(Random random) {
      return 4 + random.nextInt(2);
   }

   public void dropBlockAsItemWithChance(World world, BlockPos blockposition, IBlockState iblockdata, float f, int i) {
      super.dropBlockAsItemWithChance(world, blockposition, iblockdata, f, i);
   }

   public int getExpDrop(World world, IBlockState data, int i) {
      if (this.getItemDropped(data, world.rand, i) != Item.getItemFromBlock(this)) {
         int j = 1 + world.rand.nextInt(5);
         return j;
      } else {
         return 0;
      }
   }

   private void spawnParticles(World world, BlockPos blockposition) {
      Random random = world.rand;

      for(int i = 0; i < 6; ++i) {
         double d1 = (double)((float)blockposition.getX() + random.nextFloat());
         double d2 = (double)((float)blockposition.getY() + random.nextFloat());
         double d3 = (double)((float)blockposition.getZ() + random.nextFloat());
         if (i == 0 && !world.getBlockState(blockposition.up()).isOpaqueCube()) {
            d2 = (double)blockposition.getY() + 0.0625D + 1.0D;
         }

         if (i == 1 && !world.getBlockState(blockposition.down()).isOpaqueCube()) {
            d2 = (double)blockposition.getY() - 0.0625D;
         }

         if (i == 2 && !world.getBlockState(blockposition.south()).isOpaqueCube()) {
            d3 = (double)blockposition.getZ() + 0.0625D + 1.0D;
         }

         if (i == 3 && !world.getBlockState(blockposition.north()).isOpaqueCube()) {
            d3 = (double)blockposition.getZ() - 0.0625D;
         }

         if (i == 4 && !world.getBlockState(blockposition.east()).isOpaqueCube()) {
            d1 = (double)blockposition.getX() + 0.0625D + 1.0D;
         }

         if (i == 5 && !world.getBlockState(blockposition.west()).isOpaqueCube()) {
            d1 = (double)blockposition.getX() - 0.0625D;
         }

         if (d1 < (double)blockposition.getX() || d1 > (double)(blockposition.getX() + 1) || d2 < 0.0D || d2 > (double)(blockposition.getY() + 1) || d3 < (double)blockposition.getZ() || d3 > (double)(blockposition.getZ() + 1)) {
            world.spawnParticle(EnumParticleTypes.REDSTONE, d1, d2, d3, 0.0D, 0.0D, 0.0D);
         }
      }

   }

   protected ItemStack getSilkTouchDrop(IBlockState iblockdata) {
      return new ItemStack(Blocks.REDSTONE_ORE);
   }

   @Nullable
   public ItemStack getItem(World world, BlockPos blockposition, IBlockState iblockdata) {
      return new ItemStack(Item.getItemFromBlock(Blocks.REDSTONE_ORE), 1, this.damageDropped(iblockdata));
   }
}
