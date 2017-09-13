package net.minecraft.item;

import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class ItemEnderEye extends Item {
   public ItemEnderEye() {
      this.setCreativeTab(CreativeTabs.MISC);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      IBlockState var10 = var3.getBlockState(var4);
      if (var2.canPlayerEdit(var4.offset(var6), var6, var1) && var10.getBlock() == Blocks.END_PORTAL_FRAME && !((Boolean)var10.getValue(BlockEndPortalFrame.EYE)).booleanValue()) {
         if (var3.isRemote) {
            return EnumActionResult.SUCCESS;
         } else {
            var3.setBlockState(var4, var10.withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(true)), 2);
            var3.updateComparatorOutputLevel(var4, Blocks.END_PORTAL_FRAME);
            --var1.stackSize;

            for(int var11 = 0; var11 < 16; ++var11) {
               double var12 = (double)((float)var4.getX() + (5.0F + itemRand.nextFloat() * 6.0F) / 16.0F);
               double var14 = (double)((float)var4.getY() + 0.8125F);
               double var16 = (double)((float)var4.getZ() + (5.0F + itemRand.nextFloat() * 6.0F) / 16.0F);
               double var18 = 0.0D;
               double var20 = 0.0D;
               double var22 = 0.0D;
               var3.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, var12, var14, var16, 0.0D, 0.0D, 0.0D);
            }

            BlockPattern.PatternHelper var24 = BlockEndPortalFrame.getOrCreatePortalShape().match(var3, var4);
            if (var24 != null) {
               BlockPos var25 = var24.getFrontTopLeft().add(-3, 0, -3);

               for(int var13 = 0; var13 < 3; ++var13) {
                  for(int var26 = 0; var26 < 3; ++var26) {
                     var3.setBlockState(var25.add(var13, 0, var26), Blocks.END_PORTAL.getDefaultState(), 2);
                  }
               }
            }

            return EnumActionResult.SUCCESS;
         }
      } else {
         return EnumActionResult.FAIL;
      }
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      RayTraceResult var5 = this.rayTrace(var2, var3, false);
      if (var5 != null && var5.typeOfHit == RayTraceResult.Type.BLOCK && var2.getBlockState(var5.getBlockPos()).getBlock() == Blocks.END_PORTAL_FRAME) {
         return new ActionResult(EnumActionResult.PASS, var1);
      } else {
         if (!var2.isRemote) {
            BlockPos var6 = ((WorldServer)var2).getChunkProvider().getStrongholdGen(var2, "Stronghold", new BlockPos(var3));
            if (var6 != null) {
               EntityEnderEye var7 = new EntityEnderEye(var2, var3.posX, var3.posY + (double)(var3.height / 2.0F), var3.posZ);
               var7.moveTowards(var6);
               var2.spawnEntity(var7);
               var2.playSound((EntityPlayer)null, var3.posX, var3.posY, var3.posZ, SoundEvents.ENTITY_ENDEREYE_LAUNCH, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
               var2.playEvent((EntityPlayer)null, 1003, new BlockPos(var3), 0);
               if (!var3.capabilities.isCreativeMode) {
                  --var1.stackSize;
               }

               var3.addStat(StatList.getObjectUseStats(this));
               return new ActionResult(EnumActionResult.SUCCESS, var1);
            }
         }

         return new ActionResult(EnumActionResult.FAIL, var1);
      }
   }
}
