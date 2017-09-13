package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockBed extends BlockHorizontal {
   public static final PropertyEnum PART = PropertyEnum.create("part", BlockBed.EnumPartType.class);
   public static final PropertyBool OCCUPIED = PropertyBool.create("occupied");
   protected static final AxisAlignedBB BED_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5625D, 1.0D);

   public BlockBed() {
      super(Material.CLOTH);
      this.setDefaultState(this.blockState.getBaseState().withProperty(PART, BlockBed.EnumPartType.FOOT).withProperty(OCCUPIED, Boolean.valueOf(false)));
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (var1.isRemote) {
         return true;
      } else {
         if (var3.getValue(PART) != BlockBed.EnumPartType.HEAD) {
            var2 = var2.offset((EnumFacing)var3.getValue(FACING));
            var3 = var1.getBlockState(var2);
            if (var3.getBlock() != this) {
               return true;
            }
         }

         if (var1.provider.canRespawnHere() && var1.getBiome(var2) != Biomes.HELL) {
            if (((Boolean)var3.getValue(OCCUPIED)).booleanValue()) {
               EntityPlayer var13 = this.getPlayerInBed(var1, var2);
               if (var13 != null) {
                  var4.sendStatusMessage(new TextComponentTranslation("tile.bed.occupied", new Object[0]));
                  return true;
               }

               var3 = var3.withProperty(OCCUPIED, Boolean.valueOf(false));
               var1.setBlockState(var2, var3, 4);
            }

            EntityPlayer.SleepResult var14 = var4.trySleep(var2);
            if (var14 == EntityPlayer.SleepResult.OK) {
               var3 = var3.withProperty(OCCUPIED, Boolean.valueOf(true));
               var1.setBlockState(var2, var3, 4);
               return true;
            } else {
               if (var14 == EntityPlayer.SleepResult.NOT_POSSIBLE_NOW) {
                  var4.sendStatusMessage(new TextComponentTranslation("tile.bed.noSleep", new Object[0]));
               } else if (var14 == EntityPlayer.SleepResult.NOT_SAFE) {
                  var4.sendStatusMessage(new TextComponentTranslation("tile.bed.notSafe", new Object[0]));
               }

               return true;
            }
         } else {
            var1.setBlockToAir(var2);
            BlockPos var11 = var2.offset(((EnumFacing)var3.getValue(FACING)).getOpposite());
            if (var1.getBlockState(var11).getBlock() == this) {
               var1.setBlockToAir(var11);
            }

            var1.newExplosion((Entity)null, (double)var2.getX() + 0.5D, (double)var2.getY() + 0.5D, (double)var2.getZ() + 0.5D, 5.0F, true, true);
            return true;
         }
      }
   }

   @Nullable
   private EntityPlayer getPlayerInBed(World var1, BlockPos var2) {
      for(EntityPlayer var4 : var1.playerEntities) {
         if (var4.isPlayerSleeping() && var4.bedLocation.equals(var2)) {
            return var4;
         }
      }

      return null;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      EnumFacing var5 = (EnumFacing)var1.getValue(FACING);
      if (var1.getValue(PART) == BlockBed.EnumPartType.HEAD) {
         if (var2.getBlockState(var3.offset(var5.getOpposite())).getBlock() != this) {
            var2.setBlockToAir(var3);
         }
      } else if (var2.getBlockState(var3.offset(var5)).getBlock() != this) {
         var2.setBlockToAir(var3);
         if (!var2.isRemote) {
            this.dropBlockAsItem(var2, var3, var1, 0);
         }
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return var1.getValue(PART) == BlockBed.EnumPartType.HEAD ? null : Items.BED;
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return BED_AABB;
   }

   @Nullable
   public static BlockPos getSafeExitLocation(World var0, BlockPos var1, int var2) {
      EnumFacing var3 = (EnumFacing)var0.getBlockState(var1).getValue(FACING);
      int var4 = var1.getX();
      int var5 = var1.getY();
      int var6 = var1.getZ();

      for(int var7 = 0; var7 <= 1; ++var7) {
         int var8 = var4 - var3.getFrontOffsetX() * var7 - 1;
         int var9 = var6 - var3.getFrontOffsetZ() * var7 - 1;
         int var10 = var8 + 2;
         int var11 = var9 + 2;

         for(int var12 = var8; var12 <= var10; ++var12) {
            for(int var13 = var9; var13 <= var11; ++var13) {
               BlockPos var14 = new BlockPos(var12, var5, var13);
               if (hasRoomForPlayer(var0, var14)) {
                  if (var2 <= 0) {
                     return var14;
                  }

                  --var2;
               }
            }
         }
      }

      return null;
   }

   protected static boolean hasRoomForPlayer(World var0, BlockPos var1) {
      return var0.getBlockState(var1.down()).isFullyOpaque() && !var0.getBlockState(var1).getMaterial().isSolid() && !var0.getBlockState(var1.up()).getMaterial().isSolid();
   }

   public void dropBlockAsItemWithChance(World var1, BlockPos var2, IBlockState var3, float var4, int var5) {
      if (var3.getValue(PART) == BlockBed.EnumPartType.FOOT) {
         super.dropBlockAsItemWithChance(var1, var2, var3, var4, 0);
      }

   }

   public EnumPushReaction getMobilityFlag(IBlockState var1) {
      return EnumPushReaction.DESTROY;
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Items.BED);
   }

   public void onBlockHarvested(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4) {
      if (var4.capabilities.isCreativeMode && var3.getValue(PART) == BlockBed.EnumPartType.HEAD) {
         BlockPos var5 = var2.offset(((EnumFacing)var3.getValue(FACING)).getOpposite());
         if (var1.getBlockState(var5).getBlock() == this) {
            var1.setBlockToAir(var5);
         }
      }

   }

   public IBlockState getStateFromMeta(int var1) {
      EnumFacing var2 = EnumFacing.getHorizontal(var1);
      return (var1 & 8) > 0 ? this.getDefaultState().withProperty(PART, BlockBed.EnumPartType.HEAD).withProperty(FACING, var2).withProperty(OCCUPIED, Boolean.valueOf((var1 & 4) > 0)) : this.getDefaultState().withProperty(PART, BlockBed.EnumPartType.FOOT).withProperty(FACING, var2);
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      if (var1.getValue(PART) == BlockBed.EnumPartType.FOOT) {
         IBlockState var4 = var2.getBlockState(var3.offset((EnumFacing)var1.getValue(FACING)));
         if (var4.getBlock() == this) {
            var1 = var1.withProperty(OCCUPIED, var4.getValue(OCCUPIED));
         }
      }

      return var1;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return var1.withProperty(FACING, var2.rotate((EnumFacing)var1.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return var1.withRotation(var2.toRotation((EnumFacing)var1.getValue(FACING)));
   }

   public int getMetaFromState(IBlockState var1) {
      int var2 = 0;
      var2 = var2 | ((EnumFacing)var1.getValue(FACING)).getHorizontalIndex();
      if (var1.getValue(PART) == BlockBed.EnumPartType.HEAD) {
         var2 |= 8;
         if (((Boolean)var1.getValue(OCCUPIED)).booleanValue()) {
            var2 |= 4;
         }
      }

      return var2;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, PART, OCCUPIED});
   }

   public static enum EnumPartType implements IStringSerializable {
      HEAD("head"),
      FOOT("foot");

      private final String name;

      private EnumPartType(String var3) {
         this.name = var3;
      }

      public String toString() {
         return this.name;
      }

      public String getName() {
         return this.name;
      }
   }
}
