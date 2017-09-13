package net.minecraft.enchantment;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_10_R1.util.CraftMagicNumbers;
import org.bukkit.event.block.EntityBlockFormEvent;

public class EnchantmentFrostWalker extends Enchantment {
   public EnchantmentFrostWalker(Enchantment.Rarity var1, EntityEquipmentSlot... var2) {
      super(var1, EnumEnchantmentType.ARMOR_FEET, var2);
      this.setName("frostWalker");
   }

   public int getMinEnchantability(int var1) {
      return var1 * 10;
   }

   public int getMaxEnchantability(int var1) {
      return this.getMinEnchantability(var1) + 15;
   }

   public boolean isTreasureEnchantment() {
      return true;
   }

   public int getMaxLevel() {
      return 2;
   }

   public static void freezeNearby(EntityLivingBase var0, World var1, BlockPos var2, int var3) {
      if (var0.onGround) {
         float var4 = (float)Math.min(16, 2 + var3);
         BlockPos.MutableBlockPos var5 = new BlockPos.MutableBlockPos(0, 0, 0);

         for(BlockPos.MutableBlockPos var7 : BlockPos.getAllInBoxMutable(var2.add((double)(-var4), -1.0D, (double)(-var4)), var2.add((double)var4, -1.0D, (double)var4))) {
            if (var7.distanceSqToCenter(var0.posX, var0.posY, var0.posZ) <= (double)(var4 * var4)) {
               var5.setPos(var7.getX(), var7.getY() + 1, var7.getZ());
               IBlockState var8 = var1.getBlockState(var5);
               if (var8.getMaterial() == Material.AIR) {
                  IBlockState var9 = var1.getBlockState(var7);
                  if (var9.getMaterial() == Material.WATER && ((Integer)var9.getValue(BlockLiquid.LEVEL)).intValue() == 0 && var1.canBlockBePlaced(Blocks.FROSTED_ICE, var7, false, EnumFacing.DOWN, (Entity)null, (ItemStack)null)) {
                     BlockState var10 = var1.getWorld().getBlockAt(var7.getX(), var7.getY(), var7.getZ()).getState();
                     var10.setType(CraftMagicNumbers.getMaterial(Blocks.FROSTED_ICE));
                     EntityBlockFormEvent var11 = new EntityBlockFormEvent(var0.bukkitEntity, var10.getBlock(), var10);
                     var1.getServer().getPluginManager().callEvent(var11);
                     if (!var11.isCancelled()) {
                        var10.update(true);
                        var1.scheduleUpdate(var7.toImmutable(), Blocks.FROSTED_ICE, MathHelper.getInt(var0.getRNG(), 60, 120));
                     }
                  }
               }
            }
         }
      }

   }

   public boolean canApplyTogether(Enchantment var1) {
      return super.canApplyTogether(var1) && var1 != Enchantments.DEPTH_STRIDER;
   }
}
