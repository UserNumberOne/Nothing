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
      int i = stack.getMetadata();
      return super.getUnlocalizedName() + "." + EnumDyeColor.byDyeDamage(i).getUnlocalizedName();
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      if (!playerIn.canPlayerEdit(pos.offset(facing), facing, stack)) {
         return EnumActionResult.FAIL;
      } else {
         EnumDyeColor enumdyecolor = EnumDyeColor.byDyeDamage(stack.getMetadata());
         if (enumdyecolor == EnumDyeColor.WHITE) {
            if (applyBonemeal(stack, worldIn, pos, playerIn)) {
               if (!worldIn.isRemote) {
                  worldIn.playEvent(2005, pos, 0);
               }

               return EnumActionResult.SUCCESS;
            }
         } else if (enumdyecolor == EnumDyeColor.BROWN) {
            IBlockState iblockstate = worldIn.getBlockState(pos);
            Block block = iblockstate.getBlock();
            if (block == Blocks.LOG && iblockstate.getValue(BlockOldLog.VARIANT) == BlockPlanks.EnumType.JUNGLE) {
               if (facing != EnumFacing.DOWN && facing != EnumFacing.UP) {
                  pos = pos.offset(facing);
                  if (worldIn.isAirBlock(pos)) {
                     IBlockState iblockstate1 = Blocks.COCOA.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, 0, playerIn, stack);
                     worldIn.setBlockState(pos, iblockstate1, 10);
                     if (!playerIn.capabilities.isCreativeMode) {
                        --stack.stackSize;
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
      return worldIn instanceof WorldServer ? applyBonemeal(stack, worldIn, target, FakePlayerFactory.getMinecraft((WorldServer)worldIn)) : false;
   }

   public static boolean applyBonemeal(ItemStack var0, World var1, BlockPos var2, EntityPlayer var3) {
      IBlockState iblockstate = worldIn.getBlockState(target);
      int hook = ForgeEventFactory.onApplyBonemeal(player, worldIn, target, iblockstate, stack);
      if (hook != 0) {
         return hook > 0;
      } else {
         if (iblockstate.getBlock() instanceof IGrowable) {
            IGrowable igrowable = (IGrowable)iblockstate.getBlock();
            if (igrowable.canGrow(worldIn, target, iblockstate, worldIn.isRemote)) {
               if (!worldIn.isRemote) {
                  if (igrowable.canUseBonemeal(worldIn, worldIn.rand, target, iblockstate)) {
                     igrowable.grow(worldIn, worldIn.rand, target, iblockstate);
                  }

                  --stack.stackSize;
               }

               return true;
            }
         }

         return false;
      }
   }

   @SideOnly(Side.CLIENT)
   public static void spawnBonemealParticles(World var0, BlockPos var1, int var2) {
      if (amount == 0) {
         amount = 15;
      }

      IBlockState iblockstate = worldIn.getBlockState(pos);
      if (iblockstate.getMaterial() != Material.AIR) {
         for(int i = 0; i < amount; ++i) {
            double d0 = itemRand.nextGaussian() * 0.02D;
            double d1 = itemRand.nextGaussian() * 0.02D;
            double d2 = itemRand.nextGaussian() * 0.02D;
            worldIn.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, (double)((float)pos.getX() + itemRand.nextFloat()), (double)pos.getY() + (double)itemRand.nextFloat() * iblockstate.getBoundingBox(worldIn, pos).maxY, (double)((float)pos.getZ() + itemRand.nextFloat()), d0, d1, d2);
         }
      } else {
         for(int i1 = 0; i1 < amount; ++i1) {
            double d0 = itemRand.nextGaussian() * 0.02D;
            double d1 = itemRand.nextGaussian() * 0.02D;
            double d2 = itemRand.nextGaussian() * 0.02D;
            worldIn.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, (double)((float)pos.getX() + itemRand.nextFloat()), (double)pos.getY() + (double)itemRand.nextFloat() * 1.0D, (double)((float)pos.getZ() + itemRand.nextFloat()), d0, d1, d2);
         }
      }

   }

   public boolean itemInteractionForEntity(ItemStack var1, EntityPlayer var2, EntityLivingBase var3, EnumHand var4) {
      if (target instanceof EntitySheep) {
         EntitySheep entitysheep = (EntitySheep)target;
         EnumDyeColor enumdyecolor = EnumDyeColor.byDyeDamage(stack.getMetadata());
         if (!entitysheep.getSheared() && entitysheep.getFleeceColor() != enumdyecolor) {
            entitysheep.setFleeceColor(enumdyecolor);
            --stack.stackSize;
         }

         return true;
      } else {
         return false;
      }
   }

   @SideOnly(Side.CLIENT)
   public void getSubItems(Item var1, CreativeTabs var2, List var3) {
      for(int i = 0; i < 16; ++i) {
         subItems.add(new ItemStack(itemIn, 1, i));
      }

   }
}
