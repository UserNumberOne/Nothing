package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.event.block.BlockRedstoneEvent;

public class BlockCommandBlock extends BlockContainer {
   public static final PropertyDirection FACING = BlockDirectional.FACING;
   public static final PropertyBool CONDITIONAL = PropertyBool.create("conditional");

   public BlockCommandBlock(MapColor var1) {
      super(Material.IRON, var1);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(CONDITIONAL, Boolean.valueOf(false)));
   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      TileEntityCommandBlock var3 = new TileEntityCommandBlock();
      var3.setAuto(this == Blocks.CHAIN_COMMAND_BLOCK);
      return var3;
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!var2.isRemote) {
         TileEntity var5 = var2.getTileEntity(var3);
         if (var5 instanceof TileEntityCommandBlock) {
            TileEntityCommandBlock var6 = (TileEntityCommandBlock)var5;
            boolean var7 = var2.isBlockPowered(var3);
            boolean var8 = var6.isPowered();
            boolean var9 = var6.isAuto();
            org.bukkit.block.Block var10 = var2.getWorld().getBlockAt(var3.getX(), var3.getY(), var3.getZ());
            int var11 = var8 ? 15 : 0;
            int var12 = var7 ? 15 : 0;
            BlockRedstoneEvent var13 = new BlockRedstoneEvent(var10, var11, var12);
            var2.getServer().getPluginManager().callEvent(var13);
            if (var13.getNewCurrent() > 0 && var13.getOldCurrent() <= 0) {
               var6.setPowered(true);
               if (var6.getMode() != TileEntityCommandBlock.Mode.SEQUENCE && !var9) {
                  boolean var14 = !var6.isConditional() || this.isNextToSuccessfulCommandBlock(var2, var3, var1);
                  var6.setConditionMet(var14);
                  var2.scheduleUpdate(var3, this, this.tickRate(var2));
                  if (var14) {
                     this.propagateUpdate(var2, var3);
                  }
               }
            } else if (var13.getNewCurrent() <= 0 && var13.getOldCurrent() > 0) {
               var6.setPowered(false);
            }
         }
      }

   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!var1.isRemote) {
         TileEntity var5 = var1.getTileEntity(var2);
         if (var5 instanceof TileEntityCommandBlock) {
            TileEntityCommandBlock var6 = (TileEntityCommandBlock)var5;
            CommandBlockBaseLogic var7 = var6.getCommandBlockLogic();
            boolean var8 = !StringUtils.isNullOrEmpty(var7.getCommand());
            TileEntityCommandBlock.Mode var9 = var6.getMode();
            boolean var10 = !var6.isConditional() || this.isNextToSuccessfulCommandBlock(var1, var2, var3);
            boolean var11 = var6.isConditionMet();
            boolean var12 = false;
            if (var9 != TileEntityCommandBlock.Mode.SEQUENCE && var11 && var8) {
               var7.trigger(var1);
               var12 = true;
            }

            if (var6.isPowered() || var6.isAuto()) {
               if (var9 == TileEntityCommandBlock.Mode.SEQUENCE && var10 && var8) {
                  var7.trigger(var1);
                  var12 = true;
               }

               if (var9 == TileEntityCommandBlock.Mode.AUTO) {
                  var1.scheduleUpdate(var2, this, this.tickRate(var1));
                  if (var10) {
                     this.propagateUpdate(var1, var2);
                  }
               }
            }

            if (!var12) {
               var7.setSuccessCount(0);
            }

            var6.setConditionMet(var10);
            var1.updateComparatorOutputLevel(var2, this);
         }
      }

   }

   public boolean isNextToSuccessfulCommandBlock(World var1, BlockPos var2, IBlockState var3) {
      EnumFacing var4 = (EnumFacing)var3.getValue(FACING);
      TileEntity var5 = var1.getTileEntity(var2.offset(var4.getOpposite()));
      return var5 instanceof TileEntityCommandBlock && ((TileEntityCommandBlock)var5).getCommandBlockLogic().getSuccessCount() > 0;
   }

   public int tickRate(World var1) {
      return 1;
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      TileEntity var11 = var1.getTileEntity(var2);
      if (var11 instanceof TileEntityCommandBlock && var4.canUseCommandBlock()) {
         var4.displayGuiCommandBlock((TileEntityCommandBlock)var11);
         return true;
      } else {
         return false;
      }
   }

   public boolean hasComparatorInputOverride(IBlockState var1) {
      return true;
   }

   public int getComparatorInputOverride(IBlockState var1, World var2, BlockPos var3) {
      TileEntity var4 = var2.getTileEntity(var3);
      return var4 instanceof TileEntityCommandBlock ? ((TileEntityCommandBlock)var4).getCommandBlockLogic().getSuccessCount() : 0;
   }

   public void onBlockPlacedBy(World var1, BlockPos var2, IBlockState var3, EntityLivingBase var4, ItemStack var5) {
      TileEntity var6 = var1.getTileEntity(var2);
      if (var6 instanceof TileEntityCommandBlock) {
         TileEntityCommandBlock var7 = (TileEntityCommandBlock)var6;
         CommandBlockBaseLogic var8 = var7.getCommandBlockLogic();
         if (var5.hasDisplayName()) {
            var8.setName(var5.getDisplayName());
         }

         if (!var1.isRemote) {
            NBTTagCompound var9 = var5.getTagCompound();
            if (var9 == null || !var9.hasKey("BlockEntityTag", 10)) {
               var8.setTrackOutput(var1.getGameRules().getBoolean("sendCommandFeedback"));
               var7.setAuto(this == Blocks.CHAIN_COMMAND_BLOCK);
            }

            if (var7.getMode() == TileEntityCommandBlock.Mode.SEQUENCE) {
               boolean var10 = var1.isBlockPowered(var2);
               var7.setPowered(var10);
            }
         }
      }

   }

   public int quantityDropped(Random var1) {
      return 0;
   }

   public EnumBlockRenderType getRenderType(IBlockState var1) {
      return EnumBlockRenderType.MODEL;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(FACING, EnumFacing.getFront(var1 & 7)).withProperty(CONDITIONAL, Boolean.valueOf((var1 & 8) != 0));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((EnumFacing)var1.getValue(FACING)).getIndex() | (((Boolean)var1.getValue(CONDITIONAL)).booleanValue() ? 8 : 0);
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return var1.withProperty(FACING, var2.rotate((EnumFacing)var1.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return var1.withRotation(var2.toRotation((EnumFacing)var1.getValue(FACING)));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, CONDITIONAL});
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return this.getDefaultState().withProperty(FACING, BlockPistonBase.getFacingFromEntity(var2, var8)).withProperty(CONDITIONAL, Boolean.valueOf(false));
   }

   public void propagateUpdate(World var1, BlockPos var2) {
      IBlockState var3 = var1.getBlockState(var2);
      if (var3.getBlock() == Blocks.COMMAND_BLOCK || var3.getBlock() == Blocks.REPEATING_COMMAND_BLOCK) {
         BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos(var2);
         var4.move((EnumFacing)var3.getValue(FACING));

         for(TileEntity var5 = var1.getTileEntity(var4); var5 instanceof TileEntityCommandBlock; var5 = var1.getTileEntity(var4)) {
            TileEntityCommandBlock var6 = (TileEntityCommandBlock)var5;
            if (var6.getMode() != TileEntityCommandBlock.Mode.SEQUENCE) {
               break;
            }

            IBlockState var7 = var1.getBlockState(var4);
            Block var8 = var7.getBlock();
            if (var8 != Blocks.CHAIN_COMMAND_BLOCK || var1.isUpdateScheduled(var4, var8)) {
               break;
            }

            var1.scheduleUpdate(new BlockPos(var4), var8, this.tickRate(var1));
            var4.move((EnumFacing)var7.getValue(FACING));
         }
      }

   }
}
