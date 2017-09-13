package net.minecraft.command.server;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.src.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CommandSetBlock extends CommandBase {
   public String getName() {
      return "setblock";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.setblock.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 4) {
         throw new WrongUsageException("commands.setblock.usage", new Object[0]);
      } else {
         var2.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
         BlockPos var4 = parseBlockPos(var2, var3, 0, false);
         Block var5 = CommandBase.getBlockByText(var2, var3[3]);
         int var6 = 0;
         if (var3.length >= 5) {
            var6 = parseInt(var3[4], 0, 15);
         }

         World var7 = var2.getEntityWorld();
         if (!var7.isBlockLoaded(var4)) {
            throw new CommandException("commands.setblock.outOfWorld", new Object[0]);
         } else {
            NBTTagCompound var8 = new NBTTagCompound();
            boolean var9 = false;
            if (var3.length >= 7 && var5.hasTileEntity()) {
               String var10 = getChatComponentFromNthArg(var2, var3, 6).getUnformattedText();

               try {
                  var8 = JsonToNBT.getTagFromJson(var10);
                  var9 = true;
               } catch (NBTException var13) {
                  throw new CommandException("commands.setblock.tagError", new Object[]{var13.getMessage()});
               }
            }

            if (var3.length >= 6) {
               if ("destroy".equals(var3[5])) {
                  var7.destroyBlock(var4, true);
                  if (var5 == Blocks.AIR) {
                     notifyCommandListener(var2, this, "commands.setblock.success", new Object[0]);
                     return;
                  }
               } else if ("keep".equals(var3[5]) && !var7.isAirBlock(var4)) {
                  throw new CommandException("commands.setblock.noChange", new Object[0]);
               }
            }

            TileEntity var14 = var7.getTileEntity(var4);
            if (var14 != null) {
               if (var14 instanceof IInventory) {
                  ((IInventory)var14).clear();
               }

               var7.setBlockState(var4, Blocks.AIR.getDefaultState(), var5 == Blocks.AIR ? 2 : 4);
            }

            IBlockState var11 = var5.getStateFromMeta(var6);
            if (!var7.setBlockState(var4, var11, 2)) {
               throw new CommandException("commands.setblock.noChange", new Object[0]);
            } else {
               if (var9) {
                  TileEntity var12 = var7.getTileEntity(var4);
                  if (var12 != null) {
                     var8.setInteger("x", var4.getX());
                     var8.setInteger("y", var4.getY());
                     var8.setInteger("z", var4.getZ());
                     var12.readFromNBT(var8);
                  }
               }

               var7.notifyNeighborsRespectDebug(var4, var11.getBlock());
               var2.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 1);
               notifyCommandListener(var2, this, "commands.setblock.success", new Object[0]);
            }
         }
      }
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (var3.length > 0 && var3.length <= 3) {
         return getTabCompletionCoordinate(var3, 0, var4);
      } else if (var3.length == 4) {
         return getListOfStringsMatchingLastWord(var3, Block.REGISTRY.getKeys());
      } else {
         return var3.length == 6 ? getListOfStringsMatchingLastWord(var3, new String[]{"replace", "destroy", "keep"}) : Collections.emptyList();
      }
   }
}
