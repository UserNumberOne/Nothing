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

public class BlockCommandBlock extends BlockContainer {
   public static final PropertyDirection FACING = BlockDirectional.FACING;
   public static final PropertyBool CONDITIONAL = PropertyBool.create("conditional");

   public BlockCommandBlock(MapColor var1) {
      super(Material.IRON, color);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(CONDITIONAL, Boolean.valueOf(false)));
   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      TileEntityCommandBlock tileentitycommandblock = new TileEntityCommandBlock();
      tileentitycommandblock.setAuto(this == Blocks.CHAIN_COMMAND_BLOCK);
      return tileentitycommandblock;
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!worldIn.isRemote) {
         TileEntity tileentity = worldIn.getTileEntity(pos);
         if (tileentity instanceof TileEntityCommandBlock) {
            TileEntityCommandBlock tileentitycommandblock = (TileEntityCommandBlock)tileentity;
            boolean flag = worldIn.isBlockPowered(pos);
            boolean flag1 = tileentitycommandblock.isPowered();
            boolean flag2 = tileentitycommandblock.isAuto();
            if (flag && !flag1) {
               tileentitycommandblock.setPowered(true);
               if (tileentitycommandblock.getMode() != TileEntityCommandBlock.Mode.SEQUENCE && !flag2) {
                  boolean flag3 = !tileentitycommandblock.isConditional() || this.isNextToSuccessfulCommandBlock(worldIn, pos, state);
                  tileentitycommandblock.setConditionMet(flag3);
                  worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
                  if (flag3) {
                     this.propagateUpdate(worldIn, pos);
                  }
               }
            } else if (!flag && flag1) {
               tileentitycommandblock.setPowered(false);
            }
         }
      }

   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!worldIn.isRemote) {
         TileEntity tileentity = worldIn.getTileEntity(pos);
         if (tileentity instanceof TileEntityCommandBlock) {
            TileEntityCommandBlock tileentitycommandblock = (TileEntityCommandBlock)tileentity;
            CommandBlockBaseLogic commandblockbaselogic = tileentitycommandblock.getCommandBlockLogic();
            boolean flag = !StringUtils.isNullOrEmpty(commandblockbaselogic.getCommand());
            TileEntityCommandBlock.Mode tileentitycommandblock$mode = tileentitycommandblock.getMode();
            boolean flag1 = !tileentitycommandblock.isConditional() || this.isNextToSuccessfulCommandBlock(worldIn, pos, state);
            boolean flag2 = tileentitycommandblock.isConditionMet();
            boolean flag3 = false;
            if (tileentitycommandblock$mode != TileEntityCommandBlock.Mode.SEQUENCE && flag2 && flag) {
               commandblockbaselogic.trigger(worldIn);
               flag3 = true;
            }

            if (tileentitycommandblock.isPowered() || tileentitycommandblock.isAuto()) {
               if (tileentitycommandblock$mode == TileEntityCommandBlock.Mode.SEQUENCE && flag1 && flag) {
                  commandblockbaselogic.trigger(worldIn);
                  flag3 = true;
               }

               if (tileentitycommandblock$mode == TileEntityCommandBlock.Mode.AUTO) {
                  worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
                  if (flag1) {
                     this.propagateUpdate(worldIn, pos);
                  }
               }
            }

            if (!flag3) {
               commandblockbaselogic.setSuccessCount(0);
            }

            tileentitycommandblock.setConditionMet(flag1);
            worldIn.updateComparatorOutputLevel(pos, this);
         }
      }

   }

   public boolean isNextToSuccessfulCommandBlock(World var1, BlockPos var2, IBlockState var3) {
      EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);
      TileEntity tileentity = worldIn.getTileEntity(pos.offset(enumfacing.getOpposite()));
      return tileentity instanceof TileEntityCommandBlock && ((TileEntityCommandBlock)tileentity).getCommandBlockLogic().getSuccessCount() > 0;
   }

   public int tickRate(World var1) {
      return 1;
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if (tileentity instanceof TileEntityCommandBlock && playerIn.canUseCommandBlock()) {
         playerIn.displayGuiCommandBlock((TileEntityCommandBlock)tileentity);
         return true;
      } else {
         return false;
      }
   }

   public boolean hasComparatorInputOverride(IBlockState var1) {
      return true;
   }

   public int getComparatorInputOverride(IBlockState var1, World var2, BlockPos var3) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      return tileentity instanceof TileEntityCommandBlock ? ((TileEntityCommandBlock)tileentity).getCommandBlockLogic().getSuccessCount() : 0;
   }

   public void onBlockPlacedBy(World var1, BlockPos var2, IBlockState var3, EntityLivingBase var4, ItemStack var5) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if (tileentity instanceof TileEntityCommandBlock) {
         TileEntityCommandBlock tileentitycommandblock = (TileEntityCommandBlock)tileentity;
         CommandBlockBaseLogic commandblockbaselogic = tileentitycommandblock.getCommandBlockLogic();
         if (stack.hasDisplayName()) {
            commandblockbaselogic.setName(stack.getDisplayName());
         }

         if (!worldIn.isRemote) {
            NBTTagCompound nbttagcompound = stack.getTagCompound();
            if (nbttagcompound == null || !nbttagcompound.hasKey("BlockEntityTag", 10)) {
               commandblockbaselogic.setTrackOutput(worldIn.getGameRules().getBoolean("sendCommandFeedback"));
               tileentitycommandblock.setAuto(this == Blocks.CHAIN_COMMAND_BLOCK);
            }

            if (tileentitycommandblock.getMode() == TileEntityCommandBlock.Mode.SEQUENCE) {
               boolean flag = worldIn.isBlockPowered(pos);
               tileentitycommandblock.setPowered(flag);
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
      return this.getDefaultState().withProperty(FACING, EnumFacing.getFront(meta & 7)).withProperty(CONDITIONAL, Boolean.valueOf((meta & 8) != 0));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((EnumFacing)state.getValue(FACING)).getIndex() | (((Boolean)state.getValue(CONDITIONAL)).booleanValue() ? 8 : 0);
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, CONDITIONAL});
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return this.getDefaultState().withProperty(FACING, BlockPistonBase.getFacingFromEntity(pos, placer)).withProperty(CONDITIONAL, Boolean.valueOf(false));
   }

   public void propagateUpdate(World var1, BlockPos var2) {
      IBlockState iblockstate = worldIn.getBlockState(pos);
      if (iblockstate.getBlock() == Blocks.COMMAND_BLOCK || iblockstate.getBlock() == Blocks.REPEATING_COMMAND_BLOCK) {
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(pos);
         blockpos$mutableblockpos.move((EnumFacing)iblockstate.getValue(FACING));

         for(TileEntity tileentity = worldIn.getTileEntity(blockpos$mutableblockpos); tileentity instanceof TileEntityCommandBlock; tileentity = worldIn.getTileEntity(blockpos$mutableblockpos)) {
            TileEntityCommandBlock tileentitycommandblock = (TileEntityCommandBlock)tileentity;
            if (tileentitycommandblock.getMode() != TileEntityCommandBlock.Mode.SEQUENCE) {
               break;
            }

            IBlockState iblockstate1 = worldIn.getBlockState(blockpos$mutableblockpos);
            Block block = iblockstate1.getBlock();
            if (block != Blocks.CHAIN_COMMAND_BLOCK || worldIn.isUpdateScheduled(blockpos$mutableblockpos, block)) {
               break;
            }

            worldIn.scheduleUpdate(new BlockPos(blockpos$mutableblockpos), block, this.tickRate(worldIn));
            blockpos$mutableblockpos.move((EnumFacing)iblockstate1.getValue(FACING));
         }
      }

   }
}
