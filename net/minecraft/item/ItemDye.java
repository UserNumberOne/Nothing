package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.DyeColor;
import org.bukkit.entity.Sheep;
import org.bukkit.event.entity.SheepDyeWoolEvent;

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
            if (applyBonemeal(var1, var3, var4)) {
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
                     IBlockState var13 = Blocks.COCOA.getStateForPlacement(var3, var4, var6, var7, var8, var9, 0, var2);
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
      IBlockState var3 = var1.getBlockState(var2);
      if (var3.getBlock() instanceof IGrowable) {
         IGrowable var4 = (IGrowable)var3.getBlock();
         if (var4.canGrow(var1, var2, var3, var1.isRemote)) {
            if (!var1.isRemote) {
               if (var4.canUseBonemeal(var1, var1.rand, var2, var3)) {
                  var4.grow(var1, var1.rand, var2, var3);
               }

               --var0.stackSize;
            }

            return true;
         }
      }

      return false;
   }

   public boolean itemInteractionForEntity(ItemStack var1, EntityPlayer var2, EntityLivingBase var3, EnumHand var4) {
      if (var3 instanceof EntitySheep) {
         EntitySheep var5 = (EntitySheep)var3;
         EnumDyeColor var6 = EnumDyeColor.byDyeDamage(var1.getMetadata());
         if (!var5.getSheared() && var5.getFleeceColor() != var6) {
            byte var7 = (byte)var6.getMetadata();
            SheepDyeWoolEvent var8 = new SheepDyeWoolEvent((Sheep)var5.getBukkitEntity(), DyeColor.getByData(var7));
            var5.world.getServer().getPluginManager().callEvent(var8);
            if (var8.isCancelled()) {
               return false;
            }

            var6 = EnumDyeColor.byMetadata(var8.getColor().getWoolData());
            var5.setFleeceColor(var6);
            --var1.stackSize;
         }

         return true;
      } else {
         return false;
      }
   }
}
