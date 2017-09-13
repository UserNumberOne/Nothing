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

   public BlockCommandBlock(MapColor materialmapcolor) {
      super(Material.IRON, materialmapcolor);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(CONDITIONAL, Boolean.valueOf(false)));
   }

   public TileEntity createNewTileEntity(World world, int i) {
      TileEntityCommandBlock tileentitycommand = new TileEntityCommandBlock();
      tileentitycommand.setAuto(this == Blocks.CHAIN_COMMAND_BLOCK);
      return tileentitycommand;
   }

   public void neighborChanged(IBlockState iblockdata, World world, BlockPos blockposition, Block block) {
      if (!world.isRemote) {
         TileEntity tileentity = world.getTileEntity(blockposition);
         if (tileentity instanceof TileEntityCommandBlock) {
            TileEntityCommandBlock tileentitycommand = (TileEntityCommandBlock)tileentity;
            boolean flag = world.isBlockPowered(blockposition);
            boolean flag1 = tileentitycommand.isPowered();
            boolean flag2 = tileentitycommand.isAuto();
            org.bukkit.block.Block bukkitBlock = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
            int old = flag1 ? 15 : 0;
            int current = flag ? 15 : 0;
            BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(bukkitBlock, old, current);
            world.getServer().getPluginManager().callEvent(eventRedstone);
            if (eventRedstone.getNewCurrent() > 0 && eventRedstone.getOldCurrent() <= 0) {
               tileentitycommand.setPowered(true);
               if (tileentitycommand.getMode() != TileEntityCommandBlock.Mode.SEQUENCE && !flag2) {
                  boolean flag3 = !tileentitycommand.isConditional() || this.isNextToSuccessfulCommandBlock(world, blockposition, iblockdata);
                  tileentitycommand.setConditionMet(flag3);
                  world.scheduleUpdate(blockposition, this, this.tickRate(world));
                  if (flag3) {
                     this.propagateUpdate(world, blockposition);
                  }
               }
            } else if (eventRedstone.getNewCurrent() <= 0 && eventRedstone.getOldCurrent() > 0) {
               tileentitycommand.setPowered(false);
            }
         }
      }

   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      if (!world.isRemote) {
         TileEntity tileentity = world.getTileEntity(blockposition);
         if (tileentity instanceof TileEntityCommandBlock) {
            TileEntityCommandBlock tileentitycommand = (TileEntityCommandBlock)tileentity;
            CommandBlockBaseLogic commandblocklistenerabstract = tileentitycommand.getCommandBlockLogic();
            boolean flag = !StringUtils.isNullOrEmpty(commandblocklistenerabstract.getCommand());
            TileEntityCommandBlock.Mode tileentitycommand_type = tileentitycommand.getMode();
            boolean flag1 = !tileentitycommand.isConditional() || this.isNextToSuccessfulCommandBlock(world, blockposition, iblockdata);
            boolean flag2 = tileentitycommand.isConditionMet();
            boolean flag3 = false;
            if (tileentitycommand_type != TileEntityCommandBlock.Mode.SEQUENCE && flag2 && flag) {
               commandblocklistenerabstract.trigger(world);
               flag3 = true;
            }

            if (tileentitycommand.isPowered() || tileentitycommand.isAuto()) {
               if (tileentitycommand_type == TileEntityCommandBlock.Mode.SEQUENCE && flag1 && flag) {
                  commandblocklistenerabstract.trigger(world);
                  flag3 = true;
               }

               if (tileentitycommand_type == TileEntityCommandBlock.Mode.AUTO) {
                  world.scheduleUpdate(blockposition, this, this.tickRate(world));
                  if (flag1) {
                     this.propagateUpdate(world, blockposition);
                  }
               }
            }

            if (!flag3) {
               commandblocklistenerabstract.setSuccessCount(0);
            }

            tileentitycommand.setConditionMet(flag1);
            world.updateComparatorOutputLevel(blockposition, this);
         }
      }

   }

   public boolean isNextToSuccessfulCommandBlock(World world, BlockPos blockposition, IBlockState iblockdata) {
      EnumFacing enumdirection = (EnumFacing)iblockdata.getValue(FACING);
      TileEntity tileentity = world.getTileEntity(blockposition.offset(enumdirection.getOpposite()));
      return tileentity instanceof TileEntityCommandBlock && ((TileEntityCommandBlock)tileentity).getCommandBlockLogic().getSuccessCount() > 0;
   }

   public int tickRate(World world) {
      return 1;
   }

   public boolean onBlockActivated(World world, BlockPos blockposition, IBlockState iblockdata, EntityPlayer entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack, EnumFacing enumdirection, float f, float f1, float f2) {
      TileEntity tileentity = world.getTileEntity(blockposition);
      if (tileentity instanceof TileEntityCommandBlock && entityhuman.canUseCommandBlock()) {
         entityhuman.displayGuiCommandBlock((TileEntityCommandBlock)tileentity);
         return true;
      } else {
         return false;
      }
   }

   public boolean hasComparatorInputOverride(IBlockState iblockdata) {
      return true;
   }

   public int getComparatorInputOverride(IBlockState iblockdata, World world, BlockPos blockposition) {
      TileEntity tileentity = world.getTileEntity(blockposition);
      return tileentity instanceof TileEntityCommandBlock ? ((TileEntityCommandBlock)tileentity).getCommandBlockLogic().getSuccessCount() : 0;
   }

   public void onBlockPlacedBy(World world, BlockPos blockposition, IBlockState iblockdata, EntityLivingBase entityliving, ItemStack itemstack) {
      TileEntity tileentity = world.getTileEntity(blockposition);
      if (tileentity instanceof TileEntityCommandBlock) {
         TileEntityCommandBlock tileentitycommand = (TileEntityCommandBlock)tileentity;
         CommandBlockBaseLogic commandblocklistenerabstract = tileentitycommand.getCommandBlockLogic();
         if (itemstack.hasDisplayName()) {
            commandblocklistenerabstract.setName(itemstack.getDisplayName());
         }

         if (!world.isRemote) {
            NBTTagCompound nbttagcompound = itemstack.getTagCompound();
            if (nbttagcompound == null || !nbttagcompound.hasKey("BlockEntityTag", 10)) {
               commandblocklistenerabstract.setTrackOutput(world.getGameRules().getBoolean("sendCommandFeedback"));
               tileentitycommand.setAuto(this == Blocks.CHAIN_COMMAND_BLOCK);
            }

            if (tileentitycommand.getMode() == TileEntityCommandBlock.Mode.SEQUENCE) {
               boolean flag = world.isBlockPowered(blockposition);
               tileentitycommand.setPowered(flag);
            }
         }
      }

   }

   public int quantityDropped(Random random) {
      return 0;
   }

   public EnumBlockRenderType getRenderType(IBlockState iblockdata) {
      return EnumBlockRenderType.MODEL;
   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(FACING, EnumFacing.getFront(i & 7)).withProperty(CONDITIONAL, Boolean.valueOf((i & 8) != 0));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      return ((EnumFacing)iblockdata.getValue(FACING)).getIndex() | (((Boolean)iblockdata.getValue(CONDITIONAL)).booleanValue() ? 8 : 0);
   }

   public IBlockState withRotation(IBlockState iblockdata, Rotation enumblockrotation) {
      return iblockdata.withProperty(FACING, enumblockrotation.rotate((EnumFacing)iblockdata.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState iblockdata, Mirror enumblockmirror) {
      return iblockdata.withRotation(enumblockmirror.toRotation((EnumFacing)iblockdata.getValue(FACING)));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, CONDITIONAL});
   }

   public IBlockState getStateForPlacement(World world, BlockPos blockposition, EnumFacing enumdirection, float f, float f1, float f2, int i, EntityLivingBase entityliving) {
      return this.getDefaultState().withProperty(FACING, BlockPistonBase.getFacingFromEntity(blockposition, entityliving)).withProperty(CONDITIONAL, Boolean.valueOf(false));
   }

   public void propagateUpdate(World world, BlockPos blockposition) {
      IBlockState iblockdata = world.getBlockState(blockposition);
      if (iblockdata.getBlock() == Blocks.COMMAND_BLOCK || iblockdata.getBlock() == Blocks.REPEATING_COMMAND_BLOCK) {
         BlockPos.MutableBlockPos blockposition_mutableblockposition = new BlockPos.MutableBlockPos(blockposition);
         blockposition_mutableblockposition.move((EnumFacing)iblockdata.getValue(FACING));

         for(TileEntity tileentity = world.getTileEntity(blockposition_mutableblockposition); tileentity instanceof TileEntityCommandBlock; tileentity = world.getTileEntity(blockposition_mutableblockposition)) {
            TileEntityCommandBlock tileentitycommand = (TileEntityCommandBlock)tileentity;
            if (tileentitycommand.getMode() != TileEntityCommandBlock.Mode.SEQUENCE) {
               break;
            }

            IBlockState iblockdata1 = world.getBlockState(blockposition_mutableblockposition);
            Block block = iblockdata1.getBlock();
            if (block != Blocks.CHAIN_COMMAND_BLOCK || world.isUpdateScheduled(blockposition_mutableblockposition, block)) {
               break;
            }

            world.scheduleUpdate(new BlockPos(blockposition_mutableblockposition), block, this.tickRate(world));
            blockposition_mutableblockposition.move((EnumFacing)iblockdata1.getValue(FACING));
         }
      }

   }
}
