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

   public BlockRedstoneOre(boolean var1) {
      super(Material.ROCK);
      if (var1) {
         this.setTickRandomly(true);
      }

      this.isOn = var1;
   }

   public int tickRate(World var1) {
      return 30;
   }

   public void onBlockClicked(World var1, BlockPos var2, EntityPlayer var3) {
      this.interact(var1, var2, var3);
      super.onBlockClicked(var1, var2, var3);
   }

   public void onEntityWalk(World var1, BlockPos var2, Entity var3) {
      if (var3 instanceof EntityPlayer) {
         PlayerInteractEvent var4 = CraftEventFactory.callPlayerInteractEvent((EntityPlayer)var3, Action.PHYSICAL, var2, (EnumFacing)null, (ItemStack)null, (EnumHand)null);
         if (!var4.isCancelled()) {
            this.interact(var1, var2, var3);
            super.onEntityWalk(var1, var2, var3);
         }
      } else {
         EntityInteractEvent var5 = new EntityInteractEvent(var3.getBukkitEntity(), var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ()));
         var1.getServer().getPluginManager().callEvent(var5);
         if (!var5.isCancelled()) {
            this.interact(var1, var2, var3);
            super.onEntityWalk(var1, var2, var3);
         }
      }

   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      this.interact(var1, var2, var4);
      return super.onBlockActivated(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10);
   }

   private void interact(World var1, BlockPos var2, Entity var3) {
      this.spawnParticles(var1, var2);
      if (this == Blocks.REDSTONE_ORE) {
         if (CraftEventFactory.callEntityChangeBlockEvent(var3, var2, Blocks.LIT_REDSTONE_ORE, 0).isCancelled()) {
            return;
         }

         var1.setBlockState(var2, Blocks.LIT_REDSTONE_ORE.getDefaultState());
      }

   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (this == Blocks.LIT_REDSTONE_ORE) {
         if (CraftEventFactory.callBlockFadeEvent(var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ()), Blocks.REDSTONE_ORE).isCancelled()) {
            return;
         }

         var1.setBlockState(var2, Blocks.REDSTONE_ORE.getDefaultState());
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.REDSTONE;
   }

   public int quantityDroppedWithBonus(int var1, Random var2) {
      return this.quantityDropped(var2) + var2.nextInt(var1 + 1);
   }

   public int quantityDropped(Random var1) {
      return 4 + var1.nextInt(2);
   }

   public void dropBlockAsItemWithChance(World var1, BlockPos var2, IBlockState var3, float var4, int var5) {
      super.dropBlockAsItemWithChance(var1, var2, var3, var4, var5);
   }

   public int getExpDrop(World var1, IBlockState var2, int var3) {
      if (this.getItemDropped(var2, var1.rand, var3) != Item.getItemFromBlock(this)) {
         int var4 = 1 + var1.rand.nextInt(5);
         return var4;
      } else {
         return 0;
      }
   }

   private void spawnParticles(World var1, BlockPos var2) {
      Random var3 = var1.rand;

      for(int var4 = 0; var4 < 6; ++var4) {
         double var5 = (double)((float)var2.getX() + var3.nextFloat());
         double var7 = (double)((float)var2.getY() + var3.nextFloat());
         double var9 = (double)((float)var2.getZ() + var3.nextFloat());
         if (var4 == 0 && !var1.getBlockState(var2.up()).isOpaqueCube()) {
            var7 = (double)var2.getY() + 0.0625D + 1.0D;
         }

         if (var4 == 1 && !var1.getBlockState(var2.down()).isOpaqueCube()) {
            var7 = (double)var2.getY() - 0.0625D;
         }

         if (var4 == 2 && !var1.getBlockState(var2.south()).isOpaqueCube()) {
            var9 = (double)var2.getZ() + 0.0625D + 1.0D;
         }

         if (var4 == 3 && !var1.getBlockState(var2.north()).isOpaqueCube()) {
            var9 = (double)var2.getZ() - 0.0625D;
         }

         if (var4 == 4 && !var1.getBlockState(var2.east()).isOpaqueCube()) {
            var5 = (double)var2.getX() + 0.0625D + 1.0D;
         }

         if (var4 == 5 && !var1.getBlockState(var2.west()).isOpaqueCube()) {
            var5 = (double)var2.getX() - 0.0625D;
         }

         if (var5 < (double)var2.getX() || var5 > (double)(var2.getX() + 1) || var7 < 0.0D || var7 > (double)(var2.getY() + 1) || var9 < (double)var2.getZ() || var9 > (double)(var2.getZ() + 1)) {
            var1.spawnParticle(EnumParticleTypes.REDSTONE, var5, var7, var9, 0.0D, 0.0D, 0.0D);
         }
      }

   }

   protected ItemStack getSilkTouchDrop(IBlockState var1) {
      return new ItemStack(Blocks.REDSTONE_ORE);
   }

   @Nullable
   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Item.getItemFromBlock(Blocks.REDSTONE_ORE), 1, this.damageDropped(var3));
   }
}
