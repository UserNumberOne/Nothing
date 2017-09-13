package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemHangingEntity extends Item {
   private final Class hangingEntityClass;

   public ItemHangingEntity(Class var1) {
      this.hangingEntityClass = var1;
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      BlockPos var10 = var4.offset(var6);
      if (var6 != EnumFacing.DOWN && var6 != EnumFacing.UP && var2.canPlayerEdit(var10, var6, var1)) {
         EntityHanging var11 = this.createEntity(var3, var10, var6);
         if (var11 != null && var11.onValidSurface()) {
            if (!var3.isRemote) {
               var11.playPlaceSound();
               var3.spawnEntity(var11);
            }

            --var1.stackSize;
         }

         return EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.FAIL;
      }
   }

   @Nullable
   private EntityHanging createEntity(World var1, BlockPos var2, EnumFacing var3) {
      return (EntityHanging)(this.hangingEntityClass == EntityPainting.class ? new EntityPainting(var1, var2, var3) : (this.hangingEntityClass == EntityItemFrame.class ? new EntityItemFrame(var1, var2, var3) : null));
   }
}
