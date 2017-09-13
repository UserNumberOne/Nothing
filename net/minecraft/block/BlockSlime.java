package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSlime extends BlockBreakable {
   public BlockSlime() {
      super(Material.CLAY, false, MapColor.GRASS);
      this.setCreativeTab(CreativeTabs.DECORATIONS);
      this.slipperiness = 0.8F;
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.TRANSLUCENT;
   }

   public void onFallenUpon(World var1, BlockPos var2, Entity var3, float var4) {
      if (var3.isSneaking()) {
         super.onFallenUpon(var1, var2, var3, var4);
      } else {
         var3.fall(var4, 0.0F);
      }

   }

   public void onLanded(World var1, Entity var2) {
      if (var2.isSneaking()) {
         super.onLanded(var1, var2);
      } else if (var2.motionY < 0.0D) {
         var2.motionY = -var2.motionY;
         if (!(var2 instanceof EntityLivingBase)) {
            var2.motionY *= 0.8D;
         }
      }

   }

   public void onEntityWalk(World var1, BlockPos var2, Entity var3) {
      if (Math.abs(var3.motionY) < 0.1D && !var3.isSneaking()) {
         double var4 = 0.4D + Math.abs(var3.motionY) * 0.2D;
         var3.motionX *= var4;
         var3.motionZ *= var4;
      }

      super.onEntityWalk(var1, var2, var3);
   }
}
