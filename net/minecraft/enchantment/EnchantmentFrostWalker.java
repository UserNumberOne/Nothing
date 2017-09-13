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
   public EnchantmentFrostWalker(Enchantment.Rarity enchantment_rarity, EntityEquipmentSlot... aenumitemslot) {
      super(enchantment_rarity, EnumEnchantmentType.ARMOR_FEET, aenumitemslot);
      this.setName("frostWalker");
   }

   public int getMinEnchantability(int i) {
      return i * 10;
   }

   public int getMaxEnchantability(int i) {
      return this.getMinEnchantability(i) + 15;
   }

   public boolean isTreasureEnchantment() {
      return true;
   }

   public int getMaxLevel() {
      return 2;
   }

   public static void freezeNearby(EntityLivingBase entityliving, World world, BlockPos blockposition, int i) {
      if (entityliving.onGround) {
         float f = (float)Math.min(16, 2 + i);
         BlockPos.MutableBlockPos blockposition_mutableblockposition = new BlockPos.MutableBlockPos(0, 0, 0);

         for(BlockPos.MutableBlockPos blockposition_mutableblockposition1 : BlockPos.getAllInBoxMutable(blockposition.add((double)(-f), -1.0D, (double)(-f)), blockposition.add((double)f, -1.0D, (double)f))) {
            if (blockposition_mutableblockposition1.distanceSqToCenter(entityliving.posX, entityliving.posY, entityliving.posZ) <= (double)(f * f)) {
               blockposition_mutableblockposition.setPos(blockposition_mutableblockposition1.getX(), blockposition_mutableblockposition1.getY() + 1, blockposition_mutableblockposition1.getZ());
               IBlockState iblockdata = world.getBlockState(blockposition_mutableblockposition);
               if (iblockdata.getMaterial() == Material.AIR) {
                  IBlockState iblockdata1 = world.getBlockState(blockposition_mutableblockposition1);
                  if (iblockdata1.getMaterial() == Material.WATER && ((Integer)iblockdata1.getValue(BlockLiquid.LEVEL)).intValue() == 0 && world.canBlockBePlaced(Blocks.FROSTED_ICE, blockposition_mutableblockposition1, false, EnumFacing.DOWN, (Entity)null, (ItemStack)null)) {
                     BlockState blockState = world.getWorld().getBlockAt(blockposition_mutableblockposition1.getX(), blockposition_mutableblockposition1.getY(), blockposition_mutableblockposition1.getZ()).getState();
                     blockState.setType(CraftMagicNumbers.getMaterial(Blocks.FROSTED_ICE));
                     EntityBlockFormEvent event = new EntityBlockFormEvent(entityliving.bukkitEntity, blockState.getBlock(), blockState);
                     world.getServer().getPluginManager().callEvent(event);
                     if (!event.isCancelled()) {
                        blockState.update(true);
                        world.scheduleUpdate(blockposition_mutableblockposition1.toImmutable(), Blocks.FROSTED_ICE, MathHelper.getInt(entityliving.getRNG(), 60, 120));
                     }
                  }
               }
            }
         }
      }

   }

   public boolean canApplyTogether(Enchantment enchantment) {
      return super.canApplyTogether(enchantment) && enchantment != Enchantments.DEPTH_STRIDER;
   }
}
