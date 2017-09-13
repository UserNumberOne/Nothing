package net.minecraft.tileentity;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandResultStats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityCommandBlock extends TileEntity {
   private boolean powered;
   private boolean auto;
   private boolean conditionMet;
   private boolean sendToClient;
   private final CommandBlockBaseLogic commandBlockLogic = new CommandBlockBaseLogic() {
      public BlockPos getPosition() {
         return TileEntityCommandBlock.this.pos;
      }

      public Vec3d getPositionVector() {
         return new Vec3d((double)TileEntityCommandBlock.this.pos.getX() + 0.5D, (double)TileEntityCommandBlock.this.pos.getY() + 0.5D, (double)TileEntityCommandBlock.this.pos.getZ() + 0.5D);
      }

      public World getEntityWorld() {
         return TileEntityCommandBlock.this.getWorld();
      }

      public void setCommand(String var1) {
         super.setCommand(command);
         TileEntityCommandBlock.this.markDirty();
      }

      public void updateCommand() {
         IBlockState iblockstate = TileEntityCommandBlock.this.world.getBlockState(TileEntityCommandBlock.this.pos);
         TileEntityCommandBlock.this.getWorld().notifyBlockUpdate(TileEntityCommandBlock.this.pos, iblockstate, iblockstate, 3);
      }

      @SideOnly(Side.CLIENT)
      public int getCommandBlockType() {
         return 0;
      }

      @SideOnly(Side.CLIENT)
      public void fillInInfo(ByteBuf var1) {
         buf.writeInt(TileEntityCommandBlock.this.pos.getX());
         buf.writeInt(TileEntityCommandBlock.this.pos.getY());
         buf.writeInt(TileEntityCommandBlock.this.pos.getZ());
      }

      public Entity getCommandSenderEntity() {
         return null;
      }

      public MinecraftServer getServer() {
         return TileEntityCommandBlock.this.world.getMinecraftServer();
      }
   };

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(compound);
      this.commandBlockLogic.writeToNBT(compound);
      compound.setBoolean("powered", this.isPowered());
      compound.setBoolean("conditionMet", this.isConditionMet());
      compound.setBoolean("auto", this.isAuto());
      return compound;
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(compound);
      this.commandBlockLogic.readDataFromNBT(compound);
      this.setPowered(compound.getBoolean("powered"));
      this.setConditionMet(compound.getBoolean("conditionMet"));
      this.setAuto(compound.getBoolean("auto"));
   }

   @Nullable
   public SPacketUpdateTileEntity getUpdatePacket() {
      if (this.isSendToClient()) {
         this.setSendToClient(false);
         NBTTagCompound nbttagcompound = this.writeToNBT(new NBTTagCompound());
         return new SPacketUpdateTileEntity(this.pos, 2, nbttagcompound);
      } else {
         return null;
      }
   }

   public boolean onlyOpsCanSetNbt() {
      return true;
   }

   public CommandBlockBaseLogic getCommandBlockLogic() {
      return this.commandBlockLogic;
   }

   public CommandResultStats getCommandResultStats() {
      return this.commandBlockLogic.getCommandResultStats();
   }

   public void setPowered(boolean var1) {
      this.powered = poweredIn;
   }

   public boolean isPowered() {
      return this.powered;
   }

   public boolean isAuto() {
      return this.auto;
   }

   public void setAuto(boolean var1) {
      boolean flag = this.auto;
      this.auto = autoIn;
      if (!flag && autoIn && !this.powered && this.world != null && this.getMode() != TileEntityCommandBlock.Mode.SEQUENCE) {
         Block block = this.getBlockType();
         if (block instanceof BlockCommandBlock) {
            BlockPos blockpos = this.getPos();
            BlockCommandBlock blockcommandblock = (BlockCommandBlock)block;
            this.conditionMet = !this.isConditional() || blockcommandblock.isNextToSuccessfulCommandBlock(this.world, blockpos, this.world.getBlockState(blockpos));
            this.world.scheduleUpdate(blockpos, block, block.tickRate(this.world));
            if (this.conditionMet) {
               blockcommandblock.propagateUpdate(this.world, blockpos);
            }
         }
      }

   }

   public boolean isConditionMet() {
      return this.conditionMet;
   }

   public void setConditionMet(boolean var1) {
      this.conditionMet = conditionMetIn;
   }

   public boolean isSendToClient() {
      return this.sendToClient;
   }

   public void setSendToClient(boolean var1) {
      this.sendToClient = p_184252_1_;
   }

   public TileEntityCommandBlock.Mode getMode() {
      Block block = this.getBlockType();
      return block == Blocks.COMMAND_BLOCK ? TileEntityCommandBlock.Mode.REDSTONE : (block == Blocks.REPEATING_COMMAND_BLOCK ? TileEntityCommandBlock.Mode.AUTO : (block == Blocks.CHAIN_COMMAND_BLOCK ? TileEntityCommandBlock.Mode.SEQUENCE : TileEntityCommandBlock.Mode.REDSTONE));
   }

   public boolean isConditional() {
      IBlockState iblockstate = this.world.getBlockState(this.getPos());
      return iblockstate.getBlock() instanceof BlockCommandBlock ? ((Boolean)iblockstate.getValue(BlockCommandBlock.CONDITIONAL)).booleanValue() : false;
   }

   public void validate() {
      this.blockType = null;
      super.validate();
   }

   public static enum Mode {
      SEQUENCE,
      AUTO,
      REDSTONE;
   }
}
