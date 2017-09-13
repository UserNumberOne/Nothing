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
import net.minecraft.src.MinecraftServer;
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

         BlockPos var8 = new BlockPos(Math.min(var4.getX(), var5.getX()), Math.min(var4.getY(), var5.getY()), Math.min(var4.getZ(), var5.getZ()));
         BlockPos var9 = new BlockPos(Math.max(var4.getX(), var5.getX()), Math.max(var4.getY(), var5.getY()), Math.max(var4.getZ(), var5.getZ()));
         int var10 = (var9.getX() - var8.getX() + 1) * (var9.getY() - var8.getY() + 1) * (var9.getZ() - var8.getZ() + 1);
         if (var10 > 32768) {
            throw new CommandException("commands.fill.tooManyBlocks", new Object[]{var10, Integer.valueOf(32768)});
         } else if (var8.getY() >= 0 && var9.getY() < 256) {
            World var11 = var2.getEntityWorld();

            for(int var12 = var8.getZ(); var12 <= var9.getZ(); var12 += 16) {
               for(int var13 = var8.getX(); var13 <= var9.getX(); var13 += 16) {
                  if (!var11.isBlockLoaded(new BlockPos(var13, var9.getY() - var8.getY(), var12))) {
                     throw new CommandException("commands.fill.outOfWorld", new Object[0]);
                  }
               }
            }

            NBTTagCompound var24 = new NBTTagCompound();
            boolean var25 = false;
            if (var3.length >= 10 && var6.hasTileEntity()) {
               String var14 = getChatComponentFromNthArg(var2, var3, 9).getUnformattedText();

               try {
                  var24 = JsonToNBT.getTagFromJson(var14);
                  var25 = true;
               } catch (NBTException var22) {
                  throw new CommandException("commands.fill.tagError", new Object[]{var22.getMessage()});
               }
            }

            ArrayList var26 = Lists.newArrayList();
            var10 = 0;

            for(int var15 = var8.getZ(); var15 <= var9.getZ(); ++var15) {
               for(int var16 = var8.getY(); var16 <= var9.getY(); ++var16) {
                  for(int var17 = var8.getX(); var17 <= var9.getX(); ++var17) {
                     BlockPos var18 = new BlockPos(var17, var16, var15);
                     if (var3.length >= 9) {
                        if (!"outline".equals(var3[8]) && !"hollow".equals(var3[8])) {
                           if ("destroy".equals(var3[8])) {
                              var11.destroyBlock(var18, true);
                           } else if ("keep".equals(var3[8])) {
                              if (!var11.isAirBlock(var18)) {
                                 continue;
                              }
                           } else if ("replace".equals(var3[8]) && !var6.hasTileEntity()) {
                              if (var3.length > 9) {
                                 Block var19 = CommandBase.getBlockByText(var2, var3[9]);
                                 if (var11.getBlockState(var18).getBlock() != var19) {
                                    continue;
                                 }
                              }

                              if (var3.length > 10) {
                                 int var30 = CommandBase.parseInt(var3[10]);
                                 IBlockState var20 = var11.getBlockState(var18);
                                 if (var20.getBlock().getMetaFromState(var20) != var30) {
                                    continue;
                                 }
                              }
                           }
                        } else if (var17 != var8.getX() && var17 != var9.getX() && var16 != var8.getY() && var16 != var9.getY() && var15 != var8.getZ() && var15 != var9.getZ()) {
                           if ("hollow".equals(var3[8])) {
                              var11.setBlockState(var18, Blocks.AIR.getDefaultState(), 2);
                              var26.add(var18);
                           }
                           continue;
                        }
                     }

                     TileEntity var31 = var11.getTileEntity(var18);
                     if (var31 != null) {
                        if (var31 instanceof IInventory) {
                           ((IInventory)var31).clear();
                        }

                        var11.setBlockState(var18, Blocks.BARRIER.getDefaultState(), var6 == Blocks.BARRIER ? 2 : 4);
                     }

                     IBlockState var32 = var6.getStateFromMeta(var7);
                     if (var11.setBlockState(var18, var32, 2)) {
                        var26.add(var18);
                        ++var10;
                        if (var25) {
                           TileEntity var21 = var11.getTileEntity(var18);
                           if (var21 != null) {
                              var24.setInteger("x", var18.getX());
                              var24.setInteger("y", var18.getY());
                              var24.setInteger("z", var18.getZ());
                              var21.readFromNBT(var24);
                           }
                        }
                     }
                  }
               }
            }

            for(BlockPos var28 : var26) {
               Block var29 = var11.getBlockState(var28).getBlock();
               var11.notifyNeighborsRespectDebug(var28, var29);
            }

            if (var10 <= 0) {
               throw new CommandException("commands.fill.failed", new Object[0]);
            } else {
               var2.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, var10);
               notifyCommandListener(var2, this, "commands.fill.success", new Object[]{var10});
            }
         } else {
            throw new CommandException("commands.fill.outOfWorld", new Object[0]);
         }
      }
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (var3.length > 0 && var3.length <= 3) {
         return getTabCompletionCoordinate(var3, 0, var4);
      } else if (var3.length > 3 && var3.length <= 6) {
         return getTabCompletionCoordinate(var3, 3, var4);
      } else if (var3.length == 7) {
         return getListOfStringsMatchingLastWord(var3, Block.REGISTRY.getKeys());
      } else if (var3.length == 9) {
         return getListOfStringsMatchingLastWord(var3, new String[]{"replace", "destroy", "keep", "hollow", "outline"});
      } else {
         return var3.length == 10 && "replace".equals(var3[8]) ? getListOfStringsMatchingLastWord(var3, Block.REGISTRY.getKeys()) : Collections.emptyList();
      }
   }
}
