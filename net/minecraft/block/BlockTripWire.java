package net.minecraft.block;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.plugin.PluginManager;

public class BlockTripWire extends Block {
   public static final PropertyBool POWERED = PropertyBool.create("powered");
   public static final PropertyBool ATTACHED = PropertyBool.create("attached");
   public static final PropertyBool DISARMED = PropertyBool.create("disarmed");
   public static final PropertyBool NORTH = PropertyBool.create("north");
   public static final PropertyBool EAST = PropertyBool.create("east");
   public static final PropertyBool SOUTH = PropertyBool.create("south");
   public static final PropertyBool WEST = PropertyBool.create("west");
   protected static final AxisAlignedBB AABB = new AxisAlignedBB(0.0D, 0.0625D, 0.0D, 1.0D, 0.15625D, 1.0D);
   protected static final AxisAlignedBB TRIP_WRITE_ATTACHED_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);

   public BlockTripWire() {
      super(Material.CIRCUITS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(POWERED, Boolean.valueOf(false)).withProperty(ATTACHED, Boolean.valueOf(false)).withProperty(DISARMED, Boolean.valueOf(false)).withProperty(NORTH, Boolean.valueOf(false)).withProperty(EAST, Boolean.valueOf(false)).withProperty(SOUTH, Boolean.valueOf(false)).withProperty(WEST, Boolean.valueOf(false)));
      this.setTickRandomly(true);
   }

   public AxisAlignedBB getBoundingBox(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      return !((Boolean)iblockdata.getValue(ATTACHED)).booleanValue() ? TRIP_WRITE_ATTACHED_AABB : AABB;
   }

   public IBlockState getActualState(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      return iblockdata.withProperty(NORTH, Boolean.valueOf(isConnectedTo(iblockaccess, blockposition, iblockdata, EnumFacing.NORTH))).withProperty(EAST, Boolean.valueOf(isConnectedTo(iblockaccess, blockposition, iblockdata, EnumFacing.EAST))).withProperty(SOUTH, Boolean.valueOf(isConnectedTo(iblockaccess, blockposition, iblockdata, EnumFacing.SOUTH))).withProperty(WEST, Boolean.valueOf(isConnectedTo(iblockaccess, blockposition, iblockdata, EnumFacing.WEST)));
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState iblockdata, World world, BlockPos blockposition) {
      return NULL_AABB;
   }

   public boolean isOpaqueCube(IBlockState iblockdata) {
      return false;
   }

   public boolean isFullCube(IBlockState iblockdata) {
      return false;
   }

   @Nullable
   public Item getItemDropped(IBlockState iblockdata, Random random, int i) {
      return Items.STRING;
   }

   public ItemStack getItem(World world, BlockPos blockposition, IBlockState iblockdata) {
      return new ItemStack(Items.STRING);
   }

   public void onBlockAdded(World world, BlockPos blockposition, IBlockState iblockdata) {
      world.setBlockState(blockposition, iblockdata, 3);
      this.notifyHook(world, blockposition, iblockdata);
   }

   public void breakBlock(World world, BlockPos blockposition, IBlockState iblockdata) {
      this.notifyHook(world, blockposition, iblockdata.withProperty(POWERED, Boolean.valueOf(true)));
   }

   public void onBlockHarvested(World world, BlockPos blockposition, IBlockState iblockdata, EntityPlayer entityhuman) {
      if (!world.isRemote && entityhuman.getHeldItemMainhand() != null && entityhuman.getHeldItemMainhand().getItem() == Items.SHEARS) {
         world.setBlockState(blockposition, iblockdata.withProperty(DISARMED, Boolean.valueOf(true)), 4);
      }

   }

   private void notifyHook(World world, BlockPos blockposition, IBlockState iblockdata) {
      for(EnumFacing enumdirection : new EnumFacing[]{EnumFacing.SOUTH, EnumFacing.WEST}) {
         for(int k = 1; k < 42; ++k) {
            BlockPos blockposition1 = blockposition.offset(enumdirection, k);
            IBlockState iblockdata1 = world.getBlockState(blockposition1);
            if (iblockdata1.getBlock() == Blocks.TRIPWIRE_HOOK) {
               if (iblockdata1.getValue(BlockTripWireHook.FACING) == enumdirection.getOpposite()) {
                  Blocks.TRIPWIRE_HOOK.calculateState(world, blockposition1, iblockdata1, false, true, k, iblockdata);
               }
               break;
            }

            if (iblockdata1.getBlock() != Blocks.TRIPWIRE) {
               break;
            }
         }
      }

   }

   public void onEntityCollidedWithBlock(World world, BlockPos blockposition, IBlockState iblockdata, Entity entity) {
      if (!world.isRemote && !((Boolean)iblockdata.getValue(POWERED)).booleanValue()) {
         this.updateState(world, blockposition);
      }

   }

   public void randomTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      if (!world.isRemote && ((Boolean)world.getBlockState(blockposition).getValue(POWERED)).booleanValue()) {
         this.updateState(world, blockposition);
      }

   }

   private void updateState(World world, BlockPos blockposition) {
      IBlockState iblockdata = world.getBlockState(blockposition);
      boolean flag = ((Boolean)iblockdata.getValue(POWERED)).booleanValue();
      boolean flag1 = false;
      List list = world.getEntitiesWithinAABBExcludingEntity((Entity)null, iblockdata.getBoundingBox(world, blockposition).offset(blockposition));
      if (!list.isEmpty()) {
         for(Entity entity : list) {
            if (!entity.doesEntityNotTriggerPressurePlate()) {
               flag1 = true;
               break;
            }
         }
      }

      if (flag != flag1 && flag1 && ((Boolean)iblockdata.getValue(ATTACHED)).booleanValue()) {
         org.bukkit.World bworld = world.getWorld();
         PluginManager manager = world.getServer().getPluginManager();
         org.bukkit.block.Block block = bworld.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
         boolean allowed = false;
         Iterator var11 = list.iterator();

         label48:
         while(true) {
            Cancellable cancellable;
            while(true) {
               if (!var11.hasNext()) {
                  break label48;
               }

               Object object = var11.next();
               if (object != null) {
                  if (object instanceof EntityPlayer) {
                     cancellable = CraftEventFactory.callPlayerInteractEvent((EntityPlayer)object, Action.PHYSICAL, blockposition, (EnumFacing)null, (ItemStack)null, (EnumHand)null);
                     break;
                  }

                  if (object instanceof Entity) {
                     cancellable = new EntityInteractEvent(((Entity)object).getBukkitEntity(), block);
                     manager.callEvent((EntityInteractEvent)cancellable);
                     break;
                  }
               }
            }

            if (!cancellable.isCancelled()) {
               allowed = true;
               break;
            }
         }

         if (!allowed) {
            return;
         }
      }

      if (flag1 != flag) {
         iblockdata = iblockdata.withProperty(POWERED, Boolean.valueOf(flag1));
         world.setBlockState(blockposition, iblockdata, 3);
         this.notifyHook(world, blockposition, iblockdata);
      }

      if (flag1) {
         world.scheduleUpdate(new BlockPos(blockposition), this, this.tickRate(world));
      }

   }

   public static boolean isConnectedTo(IBlockAccess iblockaccess, BlockPos blockposition, IBlockState iblockdata, EnumFacing enumdirection) {
      BlockPos blockposition1 = blockposition.offset(enumdirection);
      IBlockState iblockdata1 = iblockaccess.getBlockState(blockposition1);
      Block block = iblockdata1.getBlock();
      if (block == Blocks.TRIPWIRE_HOOK) {
         EnumFacing enumdirection1 = enumdirection.getOpposite();
         return iblockdata1.getValue(BlockTripWireHook.FACING) == enumdirection1;
      } else {
         return block == Blocks.TRIPWIRE;
      }
   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(POWERED, Boolean.valueOf((i & 1) > 0)).withProperty(ATTACHED, Boolean.valueOf((i & 4) > 0)).withProperty(DISARMED, Boolean.valueOf((i & 8) > 0));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      int i = 0;
      if (((Boolean)iblockdata.getValue(POWERED)).booleanValue()) {
         i |= 1;
      }

      if (((Boolean)iblockdata.getValue(ATTACHED)).booleanValue()) {
         i |= 4;
      }

      if (((Boolean)iblockdata.getValue(DISARMED)).booleanValue()) {
         i |= 8;
      }

      return i;
   }

   public IBlockState withRotation(IBlockState iblockdata, Rotation enumblockrotation) {
      switch(BlockTripWire.SyntheticClass_1.a[enumblockrotation.ordinal()]) {
      case 1:
         return iblockdata.withProperty(NORTH, (Boolean)iblockdata.getValue(SOUTH)).withProperty(EAST, (Boolean)iblockdata.getValue(WEST)).withProperty(SOUTH, (Boolean)iblockdata.getValue(NORTH)).withProperty(WEST, (Boolean)iblockdata.getValue(EAST));
      case 2:
         return iblockdata.withProperty(NORTH, (Boolean)iblockdata.getValue(EAST)).withProperty(EAST, (Boolean)iblockdata.getValue(SOUTH)).withProperty(SOUTH, (Boolean)iblockdata.getValue(WEST)).withProperty(WEST, (Boolean)iblockdata.getValue(NORTH));
      case 3:
         return iblockdata.withProperty(NORTH, (Boolean)iblockdata.getValue(WEST)).withProperty(EAST, (Boolean)iblockdata.getValue(NORTH)).withProperty(SOUTH, (Boolean)iblockdata.getValue(EAST)).withProperty(WEST, (Boolean)iblockdata.getValue(SOUTH));
      default:
         return iblockdata;
      }
   }

   public IBlockState withMirror(IBlockState iblockdata, Mirror enumblockmirror) {
      switch(BlockTripWire.SyntheticClass_1.b[enumblockmirror.ordinal()]) {
      case 1:
         return iblockdata.withProperty(NORTH, (Boolean)iblockdata.getValue(SOUTH)).withProperty(SOUTH, (Boolean)iblockdata.getValue(NORTH));
      case 2:
         return iblockdata.withProperty(EAST, (Boolean)iblockdata.getValue(WEST)).withProperty(WEST, (Boolean)iblockdata.getValue(EAST));
      default:
         return super.withMirror(iblockdata, enumblockmirror);
      }
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{POWERED, ATTACHED, DISARMED, NORTH, EAST, WEST, SOUTH});
   }

   static class SyntheticClass_1 {
      static final int[] a;
      static final int[] b = new int[Mirror.values().length];

      static {
         try {
            b[Mirror.LEFT_RIGHT.ordinal()] = 1;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            b[Mirror.FRONT_BACK.ordinal()] = 2;
         } catch (NoSuchFieldError var3) {
            ;
         }

         a = new int[Rotation.values().length];

         try {
            a[Rotation.CLOCKWISE_180.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[Rotation.COUNTERCLOCKWISE_90.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[Rotation.CLOCKWISE_90.ordinal()] = 3;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
