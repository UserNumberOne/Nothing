package net.minecraft.command;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CommandFill extends CommandBase {
   public String getName() {
      return "fill";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.fill.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 7) {
         throw new WrongUsageException("commands.fill.usage", new Object[0]);
      } else {
         var2.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
         BlockPos var4 = parseBlockPos(var2, var3, 0, false);
         BlockPos var5 = parseBlockPos(var2, var3, 3, false);
         Block var6 = CommandBase.getBlockByText(var2, var3[6]);
         int var7 = 0;
         if (var3.length >= 8) {
            var7 = parseInt(var3[7], 0, 15);
         }

         IBlockState var8 = var6.getStateFromMeta(var7);
         BlockPos var9 = new BlockPos(Math.min(var4.getX(), var5.getX()), Math.min(var4.getY(), var5.getY()), Math.min(var4.getZ(), var5.getZ()));
         BlockPos var10 = new BlockPos(Math.max(var4.getX(), var5.getX()), Math.max(var4.getY(), var5.getY()), Math.max(var4.getZ(), var5.getZ()));
         int var11 = (var10.getX() - var9.getX() + 1) * (var10.getY() - var9.getY() + 1) * (var10.getZ() - var9.getZ() + 1);
         if (var11 > 32768) {
            throw new CommandException("commands.fill.tooManyBlocks", new Object[]{var11, Integer.valueOf(32768)});
         } else if (var9.getY() >= 0 && var10.getY() < 256) {
            World var12 = var2.getEntityWorld();

            for(int var13 = var9.getZ(); var13 <= var10.getZ(); var13 += 16) {
               for(int var14 = var9.getX(); var14 <= var10.getX(); var14 += 16) {
                  if (!var12.isBlockLoaded(new BlockPos(var14, var10.getY() - var9.getY(), var13))) {
                     throw new CommandException("commands.fill.outOfWorld", new Object[0]);
                  }
               }
            }

            NBTTagCompound var25 = new NBTTagCompound();
            boolean var26 = false;
            if (var3.length >= 10 && var6.hasTileEntity(var8)) {
               String var15 = getChatComponentFromNthArg(var2, var3, 9).getUnformattedText();

               try {
                  var25 = JsonToNBT.getTagFromJson(var15);
                  var26 = true;
               } catch (NBTException var23) {
                  throw new CommandException("commands.fill.tagError", new Object[]{var23.getMessage()});
               }
            }

            ArrayList var27 = Lists.newArrayList();
            var11 = 0;

            for(int var16 = var9.getZ(); var16 <= var10.getZ(); ++var16) {
               for(int var17 = var9.getY(); var17 <= var10.getY(); ++var17) {
                  for(int var18 = var9.getX(); var18 <= var10.getX(); ++var18) {
                     BlockPos var19 = new BlockPos(var18, var17, var16);
                     if (var3.length >= 9) {
                        if (!"outline".equals(var3[8]) && !"hollow".equals(var3[8])) {
                           if ("destroy".equals(var3[8])) {
                              var12.destroyBlock(var19, true);
                           } else if ("keep".equals(var3[8])) {
                              if (!var12.isAirBlock(var19)) {
                                 continue;
                              }
                           } else if ("replace".equals(var3[8]) && !var6.hasTileEntity(var8)) {
                              if (var3.length > 9) {
                                 Block var20 = CommandBase.getBlockByText(var2, var3[9]);
                                 if (var12.getBlockState(var19).getBlock() != var20) {
                                    continue;
                                 }
                              }

                              if (var3.length > 10) {
                                 int var31 = CommandBase.parseInt(var3[10]);
                                 IBlockState var21 = var12.getBlockState(var19);
                                 if (var21.getBlock().getMetaFromState(var21) != var31) {
                                    continue;
                                 }
                              }
                           }
                        } else if (var18 != var9.getX() && var18 != var10.getX() && var17 != var9.getY() && var17 != var10.getY() && var16 != var9.getZ() && var16 != var10.getZ()) {
                           if ("hollow".equals(var3[8])) {
                              var12.setBlockState(var19, Blocks.AIR.getDefaultState(), 2);
                              var27.add(var19);
                           }
                           continue;
                        }
                     }

                     TileEntity var32 = var12.getTileEntity(var19);
                     if (var32 != null) {
                        if (var32 instanceof IInventory) {
                           ((IInventory)var32).clear();
                        }

                        var12.setBlockState(var19, Blocks.BARRIER.getDefaultState(), var6 == Blocks.BARRIER ? 2 : 4);
                     }

                     IBlockState var33 = var6.getStateFromMeta(var7);
                     if (var12.setBlockState(var19, var33, 2)) {
                        var27.add(var19);
                        ++var11;
                        if (var26) {
                           TileEntity var22 = var12.getTileEntity(var19);
                           if (var22 != null) {
                              var25.setInteger("x", var19.getX());
                              var25.setInteger("y", var19.getY());
                              var25.setInteger("z", var19.getZ());
                              var22.readFromNBT(var25);
                           }
                        }
                     }
                  }
               }
            }

            for(BlockPos var29 : var27) {
               Block var30 = var12.getBlockState(var29).getBlock();
               var12.notifyNeighborsRespectDebug(var29, var30);
            }

            if (var11 <= 0) {
               throw new CommandException("commands.fill.failed", new Object[0]);
            } else {
               var2.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, var11);
               notifyCommandListener(var2, this, "commands.fill.success", new Object[]{var11});
            }
         } else {
            throw new CommandException("commands.fill.outOfWorld", new Object[0]);
         }
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length > 0 && var3.length <= 3 ? getTabCompletionCoordinate(var3, 0, var4) : (var3.length > 3 && var3.length <= 6 ? getTabCompletionCoordinate(var3, 3, var4) : (var3.length == 7 ? getListOfStringsMatchingLastWord(var3, Block.REGISTRY.getKeys()) : (var3.length == 9 ? getListOfStringsMatchingLastWord(var3, new String[]{"replace", "destroy", "keep", "hollow", "outline"}) : (var3.length == 10 && "replace".equals(var3[8]) ? getListOfStringsMatchingLastWord(var3, Block.REGISTRY.getKeys()) : Collections.emptyList()))));
   }
}
