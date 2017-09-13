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
         super.setCommand(var1);
         TileEntityCommandBlock.this.markDirty();
      }

      public void updateCommand() {
         IBlockState var1 = TileEntityCommandBlock.this.world.getBlockState(TileEntityCommandBlock.this.pos);
         TileEntityCommandBlock.this.getWorld().notifyBlockUpdate(TileEntityCommandBlock.this.pos, var1, var1, 3);
      }

      @SideOnly(Side.CLIENT)
      public int getCommandBlockType() {
         return 0;
      }

      @SideOnly(Side.CLIENT)
      public void fillInInfo(ByteBuf var1) {
         var1.writeInt(TileEntityCommandBlock.this.pos.getX());
         var1.writeInt(TileEntityCommandBlock.this.pos.getY());
         var1.writeInt(TileEntityCommandBlock.this.pos.getZ());
      }

      public Entity getCommandSenderEntity() {
         return null;
      }

      public MinecraftServer getServer() {
         return TileEntityCommandBlock.this.world.getMinecraftServer();
      }
   };

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(var1);
      this.commandBlockLogic.writeToNBT(var1);
      var1.setBoolean("powered", this.isPowered());
      var1.setBoolean("conditionMet", this.isConditionMet());
      var1.setBoolean("auto", this.isAuto());
      return var1;
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(var1);
      this.commandBlockLogic.readDataFromNBT(var1);
      this.setPowered(var1.getBoolean("powered"));
      this.setConditionMet(var1.getBoolean("conditionMet"));
      this.setAuto(var1.getBoolean("auto"));
   }

   @Nullable
   public SPacketUpdateTileEntity getUpdatePacket() {
      if (this.isSendToClient()) {
         this.setSendToClient(false);
         NBTTagCompound var1 = this.writeToNBT(new NBTTagCompound());
         return new SPacketUpdateTileEntity(this.pos, 2, var1);
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
      this.powered = var1;
   }

   public boolean isPowered() {
      return this.powered;
   }

   public boolean isAuto() {
      return this.auto;
   }

   public void setAuto(boolean var1) {
      boolean var2 = this.auto;
      this.auto = var1;
      if (!var2 && var1 && !this.powered && this.world != null && this.getMode() != TileEntityCommandBlock.Mode.SEQUENCE) {
         Block var3 = this.getBlockType();
         if (var3 instanceof BlockCommandBlock) {
            BlockPos var4 = this.getPos();
            BlockCommandBlock var5 = (BlockCommandBlock)var3;
            this.conditionMet = !this.isConditional() || var5.isNextToSuccessfulCommandBlock(this.world, var4, this.world.getBlockState(var4));
            this.world.scheduleUpdate(var4, var3, var3.tickRate(this.world));
            if (this.conditionMet) {
               var5.propagateUpdate(this.world, var4);
            }
         }
      }

   }

   public boolean isConditionMet() {
      return this.conditionMet;
   }

   public void setConditionMet(boolean var1) {
      this.conditionMet = var1;
   }

   public boolean isSendToClient() {
      return this.sendToClient;
   }

   public void setSendToClient(boolean var1) {
      this.sendToClient = var1;
   }

   public TileEntityCommandBlock.Mode getMode() {
      Block var1 = this.getBlockType();
      return var1 == Blocks.COMMAND_BLOCK ? TileEntityCommandBlock.Mode.REDSTONE : (var1 == Blocks.REPEATING_COMMAND_BLOCK ? TileEntityCommandBlock.Mode.AUTO : (var1 == Blocks.CHAIN_COMMAND_BLOCK ? TileEntityCommandBlock.Mode.SEQUENCE : TileEntityCommandBlock.Mode.REDSTONE));
   }

   public boolean isConditional() {
      IBlockState var1 = this.world.getBlockState(this.getPos());
      return var1.getBlock() instanceof BlockCommandBlock ? ((Boolean)var1.getValue(BlockCommandBlock.CONDITIONAL)).booleanValue() : false;
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
