package net.minecraft.item;

import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

public class ItemShears extends Item {
   public ItemShears() {
      this.setMaxStackSize(1);
      this.setMaxDamage(238);
      this.setCreativeTab(CreativeTabs.TOOLS);
   }

   public boolean onBlockDestroyed(ItemStack var1, World var2, IBlockState var3, BlockPos var4, EntityLivingBase var5) {
      var1.damageItem(1, var5);
      Block var6 = var3.getBlock();
      return var3.getMaterial() != Material.LEAVES && var6 != Blocks.WEB && var6 != Blocks.TALLGRASS && var6 != Blocks.VINE && var6 != Blocks.TRIPWIRE && var6 != Blocks.WOOL && !(var3 instanceof IShearable) ? super.onBlockDestroyed(var1, var2, var3, var4, var5) : true;
   }

   public boolean canHarvestBlock(IBlockState var1) {
      Block var2 = var1.getBlock();
      return var2 == Blocks.WEB || var2 == Blocks.REDSTONE_WIRE || var2 == Blocks.TRIPWIRE;
   }

   public float getStrVsBlock(ItemStack var1, IBlockState var2) {
      Block var3 = var2.getBlock();
      return var3 != Blocks.WEB && var2.getMaterial() != Material.LEAVES ? (var3 == Blocks.WOOL ? 5.0F : super.getStrVsBlock(var1, var2)) : 15.0F;
   }

   public boolean itemInteractionForEntity(ItemStack var1, EntityPlayer var2, EntityLivingBase var3, EnumHand var4) {
      if (var3.world.isRemote) {
         return false;
      } else if (!(var3 instanceof IShearable)) {
         return false;
      } else {
         IShearable var5 = (IShearable)var3;
         BlockPos var6 = new BlockPos(var3.posX, var3.posY, var3.posZ);
         if (var5.isShearable(var1, var3.world, var6)) {
            List var7 = var5.onSheared(var1, var3.world, var6, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, var1));
            Random var8 = new Random();

            for(ItemStack var10 : var7) {
               EntityItem var11 = var3.entityDropItem(var10, 1.0F);
               var11.motionY += (double)(var8.nextFloat() * 0.05F);
               var11.motionX += (double)((var8.nextFloat() - var8.nextFloat()) * 0.1F);
               var11.motionZ += (double)((var8.nextFloat() - var8.nextFloat()) * 0.1F);
            }

            var1.damageItem(1, var3);
         }

         return true;
      }
   }

   public boolean onBlockStartBreak(ItemStack var1, BlockPos var2, EntityPlayer var3) {
      if (!var3.world.isRemote && !var3.capabilities.isCreativeMode) {
         Block var4 = var3.world.getBlockState(var2).getBlock();
         if (var4 instanceof IShearable) {
            IShearable var5 = (IShearable)var4;
            if (var5.isShearable(var1, var3.world, var2)) {
               List var6 = var5.onSheared(var1, var3.world, var2, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, var1));
               Random var7 = new Random();

               for(ItemStack var9 : var6) {
                  float var10 = 0.7F;
                  double var11 = (double)(var7.nextFloat() * var10) + (double)(1.0F - var10) * 0.5D;
                  double var13 = (double)(var7.nextFloat() * var10) + (double)(1.0F - var10) * 0.5D;
                  double var15 = (double)(var7.nextFloat() * var10) + (double)(1.0F - var10) * 0.5D;
                  EntityItem var17 = new EntityItem(var3.world, (double)var2.getX() + var11, (double)var2.getY() + var13, (double)var2.getZ() + var15, var9);
                  var17.setDefaultPickupDelay();
                  var3.world.spawnEntity(var17);
               }

               var1.damageItem(1, var3);
               var3.addStat(StatList.getBlockStats(var4));
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
