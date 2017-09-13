package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockTNT extends Block {
   public static final PropertyBool EXPLODE = PropertyBool.create("explode");

   public BlockTNT() {
      super(Material.TNT);
      this.setDefaultState(this.blockState.getBaseState().withProperty(EXPLODE, Boolean.valueOf(false)));
      this.setCreativeTab(CreativeTabs.REDSTONE);
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      super.onBlockAdded(var1, var2, var3);
      if (var1.isBlockPowered(var2)) {
         this.onBlockDestroyedByPlayer(var1, var2, var3.withProperty(EXPLODE, Boolean.valueOf(true)));
         var1.setBlockToAir(var2);
      }

   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (var2.isBlockPowered(var3)) {
         this.onBlockDestroyedByPlayer(var2, var3, var1.withProperty(EXPLODE, Boolean.valueOf(true)));
         var2.setBlockToAir(var3);
      }

   }

   public void onBlockDestroyedByExplosion(World var1, BlockPos var2, Explosion var3) {
      if (!var1.isRemote) {
         EntityTNTPrimed var4 = new EntityTNTPrimed(var1, (double)((float)var2.getX() + 0.5F), (double)var2.getY(), (double)((float)var2.getZ() + 0.5F), var3.getExplosivePlacedBy());
         var4.setFuse((short)(var1.rand.nextInt(var4.getFuse() / 4) + var4.getFuse() / 8));
         var1.spawnEntity(var4);
      }

   }

   public void onBlockDestroyedByPlayer(World var1, BlockPos var2, IBlockState var3) {
      this.explode(var1, var2, var3, (EntityLivingBase)null);
   }

   public void explode(World var1, BlockPos var2, IBlockState var3, EntityLivingBase var4) {
      if (!var1.isRemote && ((Boolean)var3.getValue(EXPLODE)).booleanValue()) {
         EntityTNTPrimed var5 = new EntityTNTPrimed(var1, (double)((float)var2.getX() + 0.5F), (double)var2.getY(), (double)((float)var2.getZ() + 0.5F), var4);
         var1.spawnEntity(var5);
         var1.playSound((EntityPlayer)null, var5.posX, var5.posY, var5.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
      }

   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (var6 != null && (var6.getItem() == Items.FLINT_AND_STEEL || var6.getItem() == Items.FIRE_CHARGE)) {
         this.explode(var1, var2, var3.withProperty(EXPLODE, Boolean.valueOf(true)), var4);
         var1.setBlockState(var2, Blocks.AIR.getDefaultState(), 11);
         if (var6.getItem() == Items.FLINT_AND_STEEL) {
            var6.damageItem(1, var4);
         } else if (!var4.capabilities.isCreativeMode) {
            --var6.stackSize;
         }

         return true;
      } else {
         return super.onBlockActivated(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10);
      }
   }

   public void onEntityCollidedWithBlock(World var1, BlockPos var2, IBlockState var3, Entity var4) {
      if (!var1.isRemote && var4 instanceof EntityArrow) {
         EntityArrow var5 = (EntityArrow)var4;
         if (var5.isBurning()) {
            this.explode(var1, var2, var1.getBlockState(var2).withProperty(EXPLODE, Boolean.valueOf(true)), var5.shootingEntity instanceof EntityLivingBase ? (EntityLivingBase)var5.shootingEntity : null);
            var1.setBlockToAir(var2);
         }
      }

   }

   public boolean canDropFromExplosion(Explosion var1) {
      return false;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(EXPLODE, Boolean.valueOf((var1 & 1) > 0));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Boolean)var1.getValue(EXPLODE)).booleanValue() ? 1 : 0;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{EXPLODE});
   }
}
