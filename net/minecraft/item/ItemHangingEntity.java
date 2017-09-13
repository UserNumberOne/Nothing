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
      this.hangingEntityClass = entityClass;
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      BlockPos blockpos = pos.offset(facing);
      if (facing != EnumFacing.DOWN && facing != EnumFacing.UP && playerIn.canPlayerEdit(blockpos, facing, stack)) {
         EntityHanging entityhanging = this.createEntity(worldIn, blockpos, facing);
         if (entityhanging != null && entityhanging.onValidSurface()) {
            if (!worldIn.isRemote) {
               entityhanging.playPlaceSound();
               worldIn.spawnEntity(entityhanging);
            }

            --stack.stackSize;
         }

         return EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.FAIL;
      }
   }

   @Nullable
   private EntityHanging createEntity(World var1, BlockPos var2, EnumFacing var3) {
      return (EntityHanging)(this.hangingEntityClass == EntityPainting.class ? new EntityPainting(worldIn, pos, clickedSide) : (this.hangingEntityClass == EntityItemFrame.class ? new EntityItemFrame(worldIn, pos, clickedSide) : null));
   }
}
