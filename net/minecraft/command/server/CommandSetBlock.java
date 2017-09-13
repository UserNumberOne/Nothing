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
import net.minecraft.server.MinecraftServer;
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

         IBlockState var7 = var5.getStateFromMeta(var6);
         World var8 = var2.getEntityWorld();
         if (!var8.isBlockLoaded(var4)) {
            throw new CommandException("commands.setblock.outOfWorld", new Object[0]);
         } else {
            NBTTagCompound var9 = new NBTTagCompound();
            boolean var10 = false;
            if (var3.length >= 7 && var5.hasTileEntity(var7)) {
               String var11 = getChatComponentFromNthArg(var2, var3, 6).getUnformattedText();

               try {
                  var9 = JsonToNBT.getTagFromJson(var11);
                  var10 = true;
               } catch (NBTException var14) {
                  throw new CommandException("commands.setblock.tagError", new Object[]{var14.getMessage()});
               }
            }

            if (var3.length >= 6) {
               if ("destroy".equals(var3[5])) {
                  var8.destroyBlock(var4, true);
                  if (var5 == Blocks.AIR) {
                     notifyCommandListener(var2, this, "commands.setblock.success", new Object[0]);
                     return;
                  }
               } else if ("keep".equals(var3[5]) && !var8.isAirBlock(var4)) {
                  throw new CommandException("commands.setblock.noChange", new Object[0]);
               }
            }

            TileEntity var15 = var8.getTileEntity(var4);
            if (var15 != null) {
               if (var15 instanceof IInventory) {
                  ((IInventory)var15).clear();
               }

               var8.setBlockState(var4, Blocks.AIR.getDefaultState(), var5 == Blocks.AIR ? 2 : 4);
            }

            IBlockState var12 = var5.getStateFromMeta(var6);
            if (!var8.setBlockState(var4, var12, 2)) {
               throw new CommandException("commands.setblock.noChange", new Object[0]);
            } else {
               if (var10) {
                  TileEntity var13 = var8.getTileEntity(var4);
                  if (var13 != null) {
                     var9.setInteger("x", var4.getX());
                     var9.setInteger("y", var4.getY());
                     var9.setInteger("z", var4.getZ());
                     var13.readFromNBT(var9);
                  }
               }

               var8.notifyNeighborsRespectDebug(var4, var12.getBlock());
               var2.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 1);
               notifyCommandListener(var2, this, "commands.setblock.success", new Object[0]);
            }
         }
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length > 0 && var3.length <= 3 ? getTabCompletionCoordinate(var3, 0, var4) : (var3.length == 4 ? getListOfStringsMatchingLastWord(var3, Block.REGISTRY.getKeys()) : (var3.length == 6 ? getListOfStringsMatchingLastWord(var3, new String[]{"replace", "destroy", "keep"}) : Collections.emptyList()));
   }
}
