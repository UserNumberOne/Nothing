package net.minecraft.item;

import com.google.common.base.Predicate;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ItemGlassBottle extends Item {
   public ItemGlassBottle() {
      this.setCreativeTab(CreativeTabs.BREWING);
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      List var5 = var2.getEntitiesWithinAABB(EntityAreaEffectCloud.class, var3.getEntityBoundingBox().expandXyz(2.0D), new Predicate() {
         public boolean apply(@Nullable EntityAreaEffectCloud var1) {
            return var1 != null && var1.isEntityAlive() && var1.getOwner() instanceof EntityDragon;
         }
      });
      if (!var5.isEmpty()) {
         EntityAreaEffectCloud var8 = (EntityAreaEffectCloud)var5.get(0);
         var8.setRadius(var8.getRadius() - 0.5F);
         var2.playSound((EntityPlayer)null, var3.posX, var3.posY, var3.posZ, SoundEvents.ITEM_BOTTLE_FILL_DRAGONBREATH, SoundCategory.NEUTRAL, 1.0F, 1.0F);
         return new ActionResult(EnumActionResult.SUCCESS, this.turnBottleIntoItem(var1, var3, new ItemStack(Items.DRAGON_BREATH)));
      } else {
         RayTraceResult var6 = this.rayTrace(var2, var3, true);
         if (var6 == null) {
            return new ActionResult(EnumActionResult.PASS, var1);
         } else {
            if (var6.typeOfHit == RayTraceResult.Type.BLOCK) {
               BlockPos var7 = var6.getBlockPos();
               if (!var2.isBlockModifiable(var3, var7) || !var3.canPlayerEdit(var7.offset(var6.sideHit), var6.sideHit, var1)) {
                  return new ActionResult(EnumActionResult.PASS, var1);
               }

               if (var2.getBlockState(var7).getMaterial() == Material.WATER) {
                  var2.playSound(var3, var3.posX, var3.posY, var3.posZ, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                  return new ActionResult(EnumActionResult.SUCCESS, this.turnBottleIntoItem(var1, var3, new ItemStack(Items.POTIONITEM)));
               }
            }

            return new ActionResult(EnumActionResult.PASS, var1);
         }
      }
   }

   protected ItemStack turnBottleIntoItem(ItemStack var1, EntityPlayer var2, ItemStack var3) {
      --var1.stackSize;
      var2.addStat(StatList.getObjectUseStats(this));
      if (var1.stackSize <= 0) {
         return var3;
      } else {
         if (!var2.inventory.addItemStackToInventory(var3)) {
            var2.dropItem(var3, false);
         }

         return var1;
      }
   }
}
