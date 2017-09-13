package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockMagma extends Block {
   public BlockMagma() {
      super(Material.ROCK);
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
      this.setLightLevel(0.2F);
      this.setTickRandomly(true);
   }

   public MapColor getMapColor(IBlockState var1) {
      return MapColor.NETHERRACK;
   }

   public void onEntityWalk(World var1, BlockPos var2, Entity var3) {
      if (!var3.isImmuneToFire() && var3 instanceof EntityLivingBase && !EnchantmentHelper.hasFrostWalkerEnchantment((EntityLivingBase)var3)) {
         var3.attackEntityFrom(DamageSource.hotFloor, 1.0F);
      }

      super.onEntityWalk(var1, var2, var3);
   }

   @SideOnly(Side.CLIENT)
   public int getPackedLightmapCoords(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return 15728880;
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      BlockPos var5 = var2.up();
      IBlockState var6 = var1.getBlockState(var5);
      if (var6.getBlock() == Blocks.WATER || var6.getBlock() == Blocks.FLOWING_WATER) {
         var1.setBlockToAir(var5);
         var1.playSound((EntityPlayer)null, var2, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (var1.rand.nextFloat() - var1.rand.nextFloat()) * 0.8F);
         if (var1 instanceof WorldServer) {
            ((WorldServer)var1).spawnParticle(EnumParticleTypes.SMOKE_LARGE, (double)var5.getX() + 0.5D, (double)var5.getY() + 0.25D, (double)var5.getZ() + 0.5D, 8, 0.5D, 0.25D, 0.5D, 0.0D);
         }
      }

   }

   public boolean canEntitySpawn(IBlockState var1, Entity var2) {
      return var2.isImmuneToFire();
   }
}
