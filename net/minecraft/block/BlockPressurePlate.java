package net.minecraft.block;

import java.util.Iterator;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.plugin.PluginManager;

public class BlockPressurePlate extends BlockBasePressurePlate {
   public static final PropertyBool POWERED = PropertyBool.create("powered");
   private final BlockPressurePlate.Sensitivity sensitivity;

   protected BlockPressurePlate(Material var1, BlockPressurePlate.Sensitivity var2) {
      super(var1);
      this.setDefaultState(this.blockState.getBaseState().withProperty(POWERED, Boolean.valueOf(false)));
      this.sensitivity = var2;
   }

   protected int getRedstoneStrength(IBlockState var1) {
      return ((Boolean)var1.getValue(POWERED)).booleanValue() ? 15 : 0;
   }

   protected IBlockState setRedstoneStrength(IBlockState var1, int var2) {
      return var1.withProperty(POWERED, Boolean.valueOf(var2 > 0));
   }

   protected void playClickOnSound(World var1, BlockPos var2) {
      if (this.blockMaterial == Material.WOOD) {
         var1.playSound((EntityPlayer)null, var2, SoundEvents.BLOCK_WOOD_PRESSPLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.8F);
      } else {
         var1.playSound((EntityPlayer)null, var2, SoundEvents.BLOCK_STONE_PRESSPLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.6F);
      }

   }

   protected void playClickOffSound(World var1, BlockPos var2) {
      if (this.blockMaterial == Material.WOOD) {
         var1.playSound((EntityPlayer)null, var2, SoundEvents.BLOCK_WOOD_PRESSPLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.7F);
      } else {
         var1.playSound((EntityPlayer)null, var2, SoundEvents.BLOCK_STONE_PRESSPLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.5F);
      }

   }

   protected int computeRedstoneStrength(World var1, BlockPos var2) {
      AxisAlignedBB var3 = PRESSURE_AABB.offset(var2);
      List var4;
      switch(BlockPressurePlate.SyntheticClass_1.a[this.sensitivity.ordinal()]) {
      case 1:
         var4 = var1.getEntitiesWithinAABBExcludingEntity((Entity)null, var3);
         break;
      case 2:
         var4 = var1.getEntitiesWithinAABB(EntityLivingBase.class, var3);
         break;
      default:
         return 0;
      }

      if (!var4.isEmpty()) {
         Iterator var5 = var4.iterator();

         while(true) {
            Entity var6;
            while(true) {
               if (!var5.hasNext()) {
                  return 0;
               }

               var6 = (Entity)var5.next();
               if (this.getRedstoneStrength(var1.getBlockState(var2)) != 0) {
                  break;
               }

               CraftWorld var7 = var1.getWorld();
               PluginManager var8 = var1.getServer().getPluginManager();
               Object var9;
               if (var6 instanceof EntityPlayer) {
                  var9 = CraftEventFactory.callPlayerInteractEvent((EntityPlayer)var6, Action.PHYSICAL, var2, (EnumFacing)null, (ItemStack)null, (EnumHand)null);
               } else {
                  var9 = new EntityInteractEvent(var6.getBukkitEntity(), var7.getBlockAt(var2.getX(), var2.getY(), var2.getZ()));
                  var8.callEvent((EntityInteractEvent)var9);
               }

               if (!((Cancellable)var9).isCancelled()) {
                  break;
               }
            }

            if (!var6.doesEntityNotTriggerPressurePlate()) {
               break;
            }
         }

         return 15;
      } else {
         return 0;
      }
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(POWERED, Boolean.valueOf(var1 == 1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Boolean)var1.getValue(POWERED)).booleanValue() ? 1 : 0;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{POWERED});
   }

   public static enum Sensitivity {
      EVERYTHING,
      MOBS;
   }

   static class SyntheticClass_1 {
      static final int[] a = new int[BlockPressurePlate.Sensitivity.values().length];

      static {
         try {
            a[BlockPressurePlate.Sensitivity.EVERYTHING.ordinal()] = 1;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[BlockPressurePlate.Sensitivity.MOBS.ordinal()] = 2;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
