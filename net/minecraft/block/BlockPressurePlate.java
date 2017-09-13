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
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.plugin.PluginManager;

public class BlockPressurePlate extends BlockBasePressurePlate {
   public static final PropertyBool POWERED = PropertyBool.create("powered");
   private final BlockPressurePlate.Sensitivity sensitivity;

   protected BlockPressurePlate(Material material, BlockPressurePlate.Sensitivity blockpressureplatebinary_enummobtype) {
      super(material);
      this.setDefaultState(this.blockState.getBaseState().withProperty(POWERED, Boolean.valueOf(false)));
      this.sensitivity = blockpressureplatebinary_enummobtype;
   }

   protected int getRedstoneStrength(IBlockState iblockdata) {
      return ((Boolean)iblockdata.getValue(POWERED)).booleanValue() ? 15 : 0;
   }

   protected IBlockState setRedstoneStrength(IBlockState iblockdata, int i) {
      return iblockdata.withProperty(POWERED, Boolean.valueOf(i > 0));
   }

   protected void playClickOnSound(World world, BlockPos blockposition) {
      if (this.blockMaterial == Material.WOOD) {
         world.playSound((EntityPlayer)null, blockposition, SoundEvents.BLOCK_WOOD_PRESSPLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.8F);
      } else {
         world.playSound((EntityPlayer)null, blockposition, SoundEvents.BLOCK_STONE_PRESSPLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.6F);
      }

   }

   protected void playClickOffSound(World world, BlockPos blockposition) {
      if (this.blockMaterial == Material.WOOD) {
         world.playSound((EntityPlayer)null, blockposition, SoundEvents.BLOCK_WOOD_PRESSPLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.7F);
      } else {
         world.playSound((EntityPlayer)null, blockposition, SoundEvents.BLOCK_STONE_PRESSPLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.5F);
      }

   }

   protected int computeRedstoneStrength(World world, BlockPos blockposition) {
      AxisAlignedBB axisalignedbb = PRESSURE_AABB.offset(blockposition);
      List list;
      switch(BlockPressurePlate.SyntheticClass_1.a[this.sensitivity.ordinal()]) {
      case 1:
         list = world.getEntitiesWithinAABBExcludingEntity((Entity)null, axisalignedbb);
         break;
      case 2:
         list = world.getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb);
         break;
      default:
         return 0;
      }

      if (!list.isEmpty()) {
         Iterator iterator = list.iterator();

         while(true) {
            Entity entity;
            while(true) {
               if (!iterator.hasNext()) {
                  return 0;
               }

               entity = (Entity)iterator.next();
               if (this.getRedstoneStrength(world.getBlockState(blockposition)) != 0) {
                  break;
               }

               org.bukkit.World bworld = world.getWorld();
               PluginManager manager = world.getServer().getPluginManager();
               Cancellable cancellable;
               if (entity instanceof EntityPlayer) {
                  cancellable = CraftEventFactory.callPlayerInteractEvent((EntityPlayer)entity, Action.PHYSICAL, blockposition, (EnumFacing)null, (ItemStack)null, (EnumHand)null);
               } else {
                  cancellable = new EntityInteractEvent(entity.getBukkitEntity(), bworld.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()));
                  manager.callEvent((EntityInteractEvent)cancellable);
               }

               if (!cancellable.isCancelled()) {
                  break;
               }
            }

            if (!entity.doesEntityNotTriggerPressurePlate()) {
               break;
            }
         }

         return 15;
      } else {
         return 0;
      }
   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(POWERED, Boolean.valueOf(i == 1));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      return ((Boolean)iblockdata.getValue(POWERED)).booleanValue() ? 1 : 0;
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
