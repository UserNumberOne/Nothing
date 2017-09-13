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
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.src.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CommandTestForBlock extends CommandBase {
   public String getName() {
      return "testforblock";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.testforblock.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 4) {
         throw new WrongUsageException("commands.testforblock.usage", new Object[0]);
      } else {
         var2.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
         BlockPos var4 = parseBlockPos(var2, var3, 0, false);
         Block var5 = Block.getBlockFromName(var3[3]);
         if (var5 == null) {
            throw new NumberInvalidException("commands.setblock.notFound", new Object[]{var3[3]});
         } else {
            int var6 = -1;
            if (var3.length >= 5) {
               var6 = parseInt(var3[4], -1, 15);
            }

            World var7 = var2.getEntityWorld();
            if (!var7.isBlockLoaded(var4)) {
               throw new CommandException("commands.testforblock.outOfWorld", new Object[0]);
            } else {
               NBTTagCompound var8 = new NBTTagCompound();
               boolean var9 = false;
               if (var3.length >= 6 && var5.hasTileEntity()) {
                  String var10 = getChatComponentFromNthArg(var2, var3, 5).getUnformattedText();

                  try {
                     var8 = JsonToNBT.getTagFromJson(var10);
                     var9 = true;
                  } catch (NBTException var14) {
                     throw new CommandException("commands.setblock.tagError", new Object[]{var14.getMessage()});
                  }
               }

               IBlockState var15 = var7.getBlockState(var4);
               Block var11 = var15.getBlock();
               if (var11 != var5) {
                  throw new CommandException("commands.testforblock.failed.tile", new Object[]{var4.getX(), var4.getY(), var4.getZ(), var11.getLocalizedName(), var5.getLocalizedName()});
               } else {
                  if (var6 > -1) {
                     int var12 = var15.getBlock().getMetaFromState(var15);
                     if (var12 != var6) {
                        throw new CommandException("commands.testforblock.failed.data", new Object[]{var4.getX(), var4.getY(), var4.getZ(), var12, var6});
                     }
                  }

                  if (var9) {
                     TileEntity var16 = var7.getTileEntity(var4);
                     if (var16 == null) {
                        throw new CommandException("commands.testforblock.failed.tileEntity", new Object[]{var4.getX(), var4.getY(), var4.getZ()});
                     }

                     NBTTagCompound var13 = var16.writeToNBT(new NBTTagCompound());
                     if (!NBTUtil.areNBTEquals(var8, var13, true)) {
                        throw new CommandException("commands.testforblock.failed.nbt", new Object[]{var4.getX(), var4.getY(), var4.getZ()});
                     }
                  }

                  var2.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 1);
                  notifyCommandListener(var2, this, "commands.testforblock.success", new Object[]{var4.getX(), var4.getY(), var4.getZ()});
               }
            }
         }
      }
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (var3.length > 0 && var3.length <= 3) {
         return getTabCompletionCoordinate(var3, 0, var4);
      } else {
         return var3.length == 4 ? getListOfStringsMatchingLastWord(var3, Block.REGISTRY.getKeys()) : Collections.emptyList();
      }
   }
}
