package net.minecraft.item;

import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemFirework extends Item {
   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      if (!var3.isRemote) {
         EntityFireworkRocket var10 = new EntityFireworkRocket(var3, (double)((float)var4.getX() + var7), (double)((float)var4.getY() + var8), (double)((float)var4.getZ() + var9), var1);
         var3.spawnEntity(var10);
         if (!var2.capabilities.isCreativeMode) {
            --var1.stackSize;
         }
      }

      return EnumActionResult.SUCCESS;
   }
}
