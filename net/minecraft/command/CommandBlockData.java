package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CommandBlockData extends CommandBase {
   public String getName() {
      return "blockdata";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.blockdata.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 4) {
         throw new WrongUsageException("commands.blockdata.usage", new Object[0]);
      } else {
         var2.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
         BlockPos var4 = parseBlockPos(var2, var3, 0, false);
         World var5 = var2.getEntityWorld();
         if (!var5.isBlockLoaded(var4)) {
            throw new CommandException("commands.blockdata.outOfWorld", new Object[0]);
         } else {
            IBlockState var6 = var5.getBlockState(var4);
            TileEntity var7 = var5.getTileEntity(var4);
            if (var7 == null) {
               throw new CommandException("commands.blockdata.notValid", new Object[0]);
            } else {
               NBTTagCompound var8 = var7.writeToNBT(new NBTTagCompound());
               NBTTagCompound var9 = var8.copy();

               NBTTagCompound var10;
               try {
                  var10 = JsonToNBT.getTagFromJson(getChatComponentFromNthArg(var2, var3, 3).getUnformattedText());
               } catch (NBTException var12) {
                  throw new CommandException("commands.blockdata.tagError", new Object[]{var12.getMessage()});
               }

               var8.merge(var10);
               var8.setInteger("x", var4.getX());
               var8.setInteger("y", var4.getY());
               var8.setInteger("z", var4.getZ());
               if (var8.equals(var9)) {
                  throw new CommandException("commands.blockdata.failed", new Object[]{var8.toString()});
               } else {
                  var7.readFromNBT(var8);
                  var7.markDirty();
                  var5.notifyBlockUpdate(var4, var6, var6, 3);
                  var2.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 1);
                  notifyCommandListener(var2, this, "commands.blockdata.success", new Object[]{var8.toString()});
               }
            }
         }
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length > 0 && var3.length <= 3 ? getTabCompletionCoordinate(var3, 0, var4) : Collections.emptyList();
   }
}
