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
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
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

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return !((Boolean)var1.getValue(ATTACHED)).booleanValue() ? TRIP_WRITE_ATTACHED_AABB : AABB;
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return var1.withProperty(NORTH, Boolean.valueOf(isConnectedTo(var2, var3, var1, EnumFacing.NORTH))).withProperty(EAST, Boolean.valueOf(isConnectedTo(var2, var3, var1, EnumFacing.EAST))).withProperty(SOUTH, Boolean.valueOf(isConnectedTo(var2, var3, var1, EnumFacing.SOUTH))).withProperty(WEST, Boolean.valueOf(isConnectedTo(var2, var3, var1, EnumFacing.WEST)));
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return NULL_AABB;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.STRING;
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Items.STRING);
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      var1.setBlockState(var2, var3, 3);
      this.notifyHook(var1, var2, var3);
   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      this.notifyHook(var1, var2, var3.withProperty(POWERED, Boolean.valueOf(true)));
   }

   public void onBlockHarvested(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4) {
      if (!var1.isRemote && var4.getHeldItemMainhand() != null && var4.getHeldItemMainhand().getItem() == Items.SHEARS) {
         var1.setBlockState(var2, var3.withProperty(DISARMED, Boolean.valueOf(true)), 4);
      }

   }

   private void notifyHook(World var1, BlockPos var2, IBlockState var3) {
      for(EnumFacing var7 : new EnumFacing[]{EnumFacing.SOUTH, EnumFacing.WEST}) {
         for(int var8 = 1; var8 < 42; ++var8) {
            BlockPos var9 = var2.offset(var7, var8);
            IBlockState var10 = var1.getBlockState(var9);
            if (var10.getBlock() == Blocks.TRIPWIRE_HOOK) {
               if (var10.getValue(BlockTripWireHook.FACING) == var7.getOpposite()) {
                  Blocks.TRIPWIRE_HOOK.calculateState(var1, var9, var10, false, true, var8, var3);
               }
               break;
            }

            if (var10.getBlock() != Blocks.TRIPWIRE) {
               break;
            }
         }
      }

   }

   public void onEntityCollidedWithBlock(World var1, BlockPos var2, IBlockState var3, Entity var4) {
      if (!var1.isRemote && !((Boolean)var3.getValue(POWERED)).booleanValue()) {
         this.updateState(var1, var2);
      }

   }

   public void randomTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!var1.isRemote && ((Boolean)var1.getBlockState(var2).getValue(POWERED)).booleanValue()) {
         this.updateState(var1, var2);
      }

   }

   private void updateState(World var1, BlockPos var2) {
      IBlockState var3 = var1.getBlockState(var2);
      boolean var4 = ((Boolean)var3.getValue(POWERED)).booleanValue();
      boolean var5 = false;
      List var6 = var1.getEntitiesWithinAABBExcludingEntity((Entity)null, var3.getBoundingBox(var1, var2).offset(var2));
      if (!var6.isEmpty()) {
         for(Entity var8 : var6) {
            if (!var8.doesEntityNotTriggerPressurePlate()) {
               var5 = true;
               break;
            }
         }
      }

      if (var4 != var5 && var5 && ((Boolean)var3.getValue(ATTACHED)).booleanValue()) {
         CraftWorld var15 = var1.getWorld();
         PluginManager var16 = var1.getServer().getPluginManager();
         org.bukkit.block.Block var9 = var15.getBlockAt(var2.getX(), var2.getY(), var2.getZ());
         boolean var10 = false;
         Iterator var11 = var6.iterator();

         label48:
         while(true) {
            Object var13;
            while(true) {
               if (!var11.hasNext()) {
                  break label48;
               }

               Object var12 = var11.next();
               if (var12 != null) {
                  if (var12 instanceof EntityPlayer) {
                     var13 = CraftEventFactory.callPlayerInteractEvent((EntityPlayer)var12, Action.PHYSICAL, var2, (EnumFacing)null, (ItemStack)null, (EnumHand)null);
                     break;
                  }

                  if (var12 instanceof Entity) {
                     var13 = new EntityInteractEvent(((Entity)var12).getBukkitEntity(), var9);
                     var16.callEvent((EntityInteractEvent)var13);
                     break;
                  }
               }
            }

            if (!((Cancellable)var13).isCancelled()) {
               var10 = true;
               break;
            }
         }

         if (!var10) {
            return;
         }
      }

      if (var5 != var4) {
         var3 = var3.withProperty(POWERED, Boolean.valueOf(var5));
         var1.setBlockState(var2, var3, 3);
         this.notifyHook(var1, var2, var3);
      }

      if (var5) {
         var1.scheduleUpdate(new BlockPos(var2), this, this.tickRate(var1));
      }

   }

   public static boolean isConnectedTo(IBlockAccess var0, BlockPos var1, IBlockState var2, EnumFacing var3) {
      BlockPos var4 = var1.offset(var3);
      IBlockState var5 = var0.getBlockState(var4);
      Block var6 = var5.getBlock();
      if (var6 == Blocks.TRIPWIRE_HOOK) {
         EnumFacing var7 = var3.getOpposite();
         return var5.getValue(BlockTripWireHook.FACING) == var7;
      } else {
         return var6 == Blocks.TRIPWIRE;
      }
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(POWERED, Boolean.valueOf((var1 & 1) > 0)).withProperty(ATTACHED, Boolean.valueOf((var1 & 4) > 0)).withProperty(DISARMED, Boolean.valueOf((var1 & 8) > 0));
   }

   public int getMetaFromState(IBlockState var1) {
      int var2 = 0;
      if (((Boolean)var1.getValue(POWERED)).booleanValue()) {
         var2 |= 1;
      }

      if (((Boolean)var1.getValue(ATTACHED)).booleanValue()) {
         var2 |= 4;
      }

      if (((Boolean)var1.getValue(DISARMED)).booleanValue()) {
         var2 |= 8;
      }

      return var2;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      switch(BlockTripWire.SyntheticClass_1.a[var2.ordinal()]) {
      case 1:
         return var1.withProperty(NORTH, (Boolean)var1.getValue(SOUTH)).withProperty(EAST, (Boolean)var1.getValue(WEST)).withProperty(SOUTH, (Boolean)var1.getValue(NORTH)).withProperty(WEST, (Boolean)var1.getValue(EAST));
      case 2:
         return var1.withProperty(NORTH, (Boolean)var1.getValue(EAST)).withProperty(EAST, (Boolean)var1.getValue(SOUTH)).withProperty(SOUTH, (Boolean)var1.getValue(WEST)).withProperty(WEST, (Boolean)var1.getValue(NORTH));
      case 3:
         return var1.withProperty(NORTH, (Boolean)var1.getValue(WEST)).withProperty(EAST, (Boolean)var1.getValue(NORTH)).withProperty(SOUTH, (Boolean)var1.getValue(EAST)).withProperty(WEST, (Boolean)var1.getValue(SOUTH));
      default:
         return var1;
      }
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      switch(BlockTripWire.SyntheticClass_1.b[var2.ordinal()]) {
      case 1:
         return var1.withProperty(NORTH, (Boolean)var1.getValue(SOUTH)).withProperty(SOUTH, (Boolean)var1.getValue(NORTH));
      case 2:
         return var1.withProperty(EAST, (Boolean)var1.getValue(WEST)).withProperty(WEST, (Boolean)var1.getValue(EAST));
      default:
         return super.withMirror(var1, var2);
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
