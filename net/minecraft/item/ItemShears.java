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
      stack.damageItem(1, entityLiving);
      Block block = state.getBlock();
      return state.getMaterial() != Material.LEAVES && block != Blocks.WEB && block != Blocks.TALLGRASS && block != Blocks.VINE && block != Blocks.TRIPWIRE && block != Blocks.WOOL && !(state instanceof IShearable) ? super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving) : true;
   }

   public boolean canHarvestBlock(IBlockState var1) {
      Block block = blockIn.getBlock();
      return block == Blocks.WEB || block == Blocks.REDSTONE_WIRE || block == Blocks.TRIPWIRE;
   }

   public float getStrVsBlock(ItemStack var1, IBlockState var2) {
      Block block = state.getBlock();
      return block != Blocks.WEB && state.getMaterial() != Material.LEAVES ? (block == Blocks.WOOL ? 5.0F : super.getStrVsBlock(stack, state)) : 15.0F;
   }

   public boolean itemInteractionForEntity(ItemStack var1, EntityPlayer var2, EntityLivingBase var3, EnumHand var4) {
      if (entity.world.isRemote) {
         return false;
      } else if (!(entity instanceof IShearable)) {
         return false;
      } else {
         IShearable target = (IShearable)entity;
         BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
         if (target.isShearable(itemstack, entity.world, pos)) {
            List drops = target.onSheared(itemstack, entity.world, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, itemstack));
            Random rand = new Random();

            for(ItemStack stack : drops) {
               EntityItem ent = entity.entityDropItem(stack, 1.0F);
               ent.motionY += (double)(rand.nextFloat() * 0.05F);
               ent.motionX += (double)((rand.nextFloat() - rand.nextFloat()) * 0.1F);
               ent.motionZ += (double)((rand.nextFloat() - rand.nextFloat()) * 0.1F);
            }

            itemstack.damageItem(1, entity);
         }

         return true;
      }
   }

   public boolean onBlockStartBreak(ItemStack var1, BlockPos var2, EntityPlayer var3) {
      if (!player.world.isRemote && !player.capabilities.isCreativeMode) {
         Block block = player.world.getBlockState(pos).getBlock();
         if (block instanceof IShearable) {
            IShearable target = (IShearable)block;
            if (target.isShearable(itemstack, player.world, pos)) {
               List drops = target.onSheared(itemstack, player.world, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, itemstack));
               Random rand = new Random();

               for(ItemStack stack : drops) {
                  float f = 0.7F;
                  double d = (double)(rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
                  double d1 = (double)(rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
                  double d2 = (double)(rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
                  EntityItem entityitem = new EntityItem(player.world, (double)pos.getX() + d, (double)pos.getY() + d1, (double)pos.getZ() + d2, stack);
                  entityitem.setDefaultPickupDelay();
                  player.world.spawnEntity(entityitem);
               }

               itemstack.damageItem(1, player);
               player.addStat(StatList.getBlockStats(block));
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
