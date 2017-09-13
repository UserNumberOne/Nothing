package net.minecraft.item;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemDye extends Item {
   public static final int[] DYE_COLORS = new int[]{1973019, 11743532, 3887386, 5320730, 2437522, 8073150, 2651799, 11250603, 4408131, 14188952, 4312372, 14602026, 6719955, 12801229, 15435844, 15790320};

   public ItemDye() {
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setCreativeTab(CreativeTabs.MATERIALS);
   }

   public String getUnlocalizedName(ItemStack var1) {
      int var2 = var1.getMetadata();
      return super.getUnlocalizedName() + "." + EnumDyeColor.byDyeDamage(var2).getUnlocalizedName();
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      if (!var2.canPlayerEdit(var4.offset(var6), var6, var1)) {
         return EnumActionResult.FAIL;
      } else {
         EnumDyeColor var10 = EnumDyeColor.byDyeDamage(var1.getMetadata());
         if (var10 == EnumDyeColor.WHITE) {
            if (applyBonemeal(var1, var3, var4, var2)) {
               if (!var3.isRemote) {
                  var3.playEvent(2005, var4, 0);
               }

               return EnumActionResult.SUCCESS;
            }
         } else if (var10 == EnumDyeColor.BROWN) {
            IBlockState var11 = var3.getBlockState(var4);
            Block var12 = var11.getBlock();
            if (var12 == Blocks.LOG && var11.getValue(BlockOldLog.VARIANT) == BlockPlanks.EnumType.JUNGLE) {
               if (var6 != EnumFacing.DOWN && var6 != EnumFacing.UP) {
                  var4 = var4.offset(var6);
                  if (var3.isAirBlock(var4)) {
                     IBlockState var13 = Blocks.COCOA.getStateForPlacement(var3, var4, var6, var7, var8, var9, 0, var2, var1);
                     var3.setBlockState(var4, var13, 10);
                     if (!var2.capabilities.isCreativeMode) {
                        --var1.stackSize;
                     }
                  }

                  return EnumActionResult.SUCCESS;
               }

               return EnumActionResult.FAIL;
            }

            return EnumActionResult.FAIL;
         }

         return EnumActionResult.PASS;
      }
   }

   public static boolean applyBonemeal(ItemStack var0, World var1, BlockPos var2) {
      return var1 instanceof WorldServer ? applyBonemeal(var0, var1, var2, FakePlayerFactory.getMinecraft((WorldServer)var1)) : false;
   }

   public static boolean applyBonemeal(ItemStack var0, World var1, BlockPos var2, EntityPlayer var3) {
      IBlockState var4 = var1.getBlockState(var2);
      int var5 = ForgeEventFactory.onApplyBonemeal(var3, var1, var2, var4, var0);
      if (var5 != 0) {
         return var5 > 0;
      } else {
         if (var4.getBlock() instanceof IGrowable) {
            IGrowable var6 = (IGrowable)var4.getBlock();
            if (var6.canGrow(var1, var2, var4, var1.isRemote)) {
               if (!var1.isRemote) {
                  if (var6.canUseBonemeal(var1, var1.rand, var2, var4)) {
                     var6.grow(var1, var1.rand, var2, var4);
                  }

                  --var0.stackSize;
               }

               return true;
            }
         }

         return false;
      }
   }

   @SideOnly(Side.CLIENT)
   public static void spawnBonemealParticles(World var0, BlockPos var1, int var2) {
      if (var2 == 0) {
         var2 = 15;
      }

      IBlockState var3 = var0.getBlockState(var1);
      if (var3.getMaterial() != Material.AIR) {
         for(int var4 = 0; var4 < var2; ++var4) {
            double var5 = itemRand.nextGaussian() * 0.02D;
            double var7 = itemRand.nextGaussian() * 0.02D;
            double var9 = itemRand.nextGaussian() * 0.02D;
            var0.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, (double)((float)var1.getX() + itemRand.nextFloat()), (double)var1.getY() + (double)itemRand.nextFloat() * var3.getBoundingBox(var0, var1).maxY, (double)((float)var1.getZ() + itemRand.nextFloat()), var5, var7, var9);
         }
      } else {
         for(int var11 = 0; var11 < var2; ++var11) {
            double var12 = itemRand.nextGaussian() * 0.02D;
            double var13 = itemRand.nextGaussian() * 0.02D;
            double var14 = itemRand.nextGaussian() * 0.02D;
            var0.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, (double)((float)var1.getX() + itemRand.nextFloat()), (double)var1.getY() + (double)itemRand.nextFloat() * 1.0D, (double)((float)var1.getZ() + itemRand.nextFloat()), var12, var13, var14);
         }
      }

   }

   public boolean itemInteractionForEntity(ItemStack var1, EntityPlayer var2, EntityLivingBase var3, EnumHand var4) {
      if (var3 instanceof EntitySheep) {
         EntitySheep var5 = (EntitySheep)var3;
         EnumDyeColor var6 = EnumDyeColor.byDyeDamage(var1.getMetadata());
         if (!var5.getSheared() && var5.getFleeceColor() != var6) {
            var5.setFleeceColor(var6);
            --var1.stackSize;
         }

         return true;
      } else {
         return false;
      }
   }

   @SideOnly(Side.CLIENT)
   public void getSubItems(Item var1, CreativeTabs var2, List var3) {
      for(int var4 = 0; var4 < 16; ++var4) {
         var3.add(new ItemStack(var1, 1, var4));
      }

   }
}
