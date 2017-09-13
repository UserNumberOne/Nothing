package net.minecraft.command;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.src.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public class CommandClone extends CommandBase {
   public String getName() {
      return "clone";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.clone.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 9) {
         throw new WrongUsageException("commands.clone.usage", new Object[0]);
      } else {
         var2.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
         BlockPos var4 = parseBlockPos(var2, var3, 0, false);
         BlockPos var5 = parseBlockPos(var2, var3, 3, false);
         BlockPos var6 = parseBlockPos(var2, var3, 6, false);
         StructureBoundingBox var7 = new StructureBoundingBox(var4, var5);
         StructureBoundingBox var8 = new StructureBoundingBox(var6, var6.add(var7.getLength()));
         int var9 = var7.getXSize() * var7.getYSize() * var7.getZSize();
         if (var9 > 32768) {
            throw new CommandException("commands.clone.tooManyBlocks", new Object[]{var9, Integer.valueOf(32768)});
         } else {
            boolean var10 = false;
            Block var11 = null;
            int var12 = -1;
            if ((var3.length < 11 || !"force".equals(var3[10]) && !"move".equals(var3[10])) && var7.intersectsWith(var8)) {
               throw new CommandException("commands.clone.noOverlap", new Object[0]);
            } else {
               if (var3.length >= 11 && "move".equals(var3[10])) {
                  var10 = true;
               }

               if (var7.minY >= 0 && var7.maxY < 256 && var8.minY >= 0 && var8.maxY < 256) {
                  World var13 = var2.getEntityWorld();
                  if (var13.isAreaLoaded(var7) && var13.isAreaLoaded(var8)) {
                     boolean var14 = false;
                     if (var3.length >= 10) {
                        if ("masked".equals(var3[9])) {
                           var14 = true;
                        } else if ("filtered".equals(var3[9])) {
                           if (var3.length < 12) {
                              throw new WrongUsageException("commands.clone.usage", new Object[0]);
                           }

                           var11 = getBlockByText(var2, var3[11]);
                           if (var3.length >= 13) {
                              var12 = parseInt(var3[12], 0, 15);
                           }
                        }
                     }

                     ArrayList var15 = Lists.newArrayList();
                     ArrayList var16 = Lists.newArrayList();
                     ArrayList var17 = Lists.newArrayList();
                     LinkedList var18 = Lists.newLinkedList();
                     BlockPos var19 = new BlockPos(var8.minX - var7.minX, var8.minY - var7.minY, var8.minZ - var7.minZ);

                     for(int var20 = var7.minZ; var20 <= var7.maxZ; ++var20) {
                        for(int var21 = var7.minY; var21 <= var7.maxY; ++var21) {
                           for(int var22 = var7.minX; var22 <= var7.maxX; ++var22) {
                              BlockPos var23 = new BlockPos(var22, var21, var20);
                              BlockPos var24 = var23.add(var19);
                              IBlockState var25 = var13.getBlockState(var23);
                              if ((!var14 || var25.getBlock() != Blocks.AIR) && (var11 == null || var25.getBlock() == var11 && (var12 < 0 || var25.getBlock().getMetaFromState(var25) == var12))) {
                                 TileEntity var26 = var13.getTileEntity(var23);
                                 if (var26 != null) {
                                    NBTTagCompound var27 = var26.writeToNBT(new NBTTagCompound());
                                    var16.add(new CommandClone.StaticCloneData(var24, var25, var27));
                                    var18.addLast(var23);
                                 } else if (!var25.isFullBlock() && !var25.isFullCube()) {
                                    var17.add(new CommandClone.StaticCloneData(var24, var25, (NBTTagCompound)null));
                                    var18.addFirst(var23);
                                 } else {
                                    var15.add(new CommandClone.StaticCloneData(var24, var25, (NBTTagCompound)null));
                                    var18.addLast(var23);
                                 }
                              }
                           }
                        }
                     }

                     if (var10) {
                        for(BlockPos var32 : var18) {
                           TileEntity var35 = var13.getTileEntity(var32);
                           if (var35 instanceof IInventory) {
                              ((IInventory)var35).clear();
                           }

                           var13.setBlockState(var32, Blocks.BARRIER.getDefaultState(), 2);
                        }

                        for(BlockPos var33 : var18) {
                           var13.setBlockState(var33, Blocks.AIR.getDefaultState(), 3);
                        }
                     }

                     ArrayList var31 = Lists.newArrayList();
                     var31.addAll(var15);
                     var31.addAll(var16);
                     var31.addAll(var17);
                     List var34 = Lists.reverse(var31);

                     for(CommandClone.StaticCloneData var41 : var34) {
                        TileEntity var46 = var13.getTileEntity(var41.pos);
                        if (var46 instanceof IInventory) {
                           ((IInventory)var46).clear();
                        }

                        var13.setBlockState(var41.pos, Blocks.BARRIER.getDefaultState(), 2);
                     }

                     var9 = 0;

                     for(CommandClone.StaticCloneData var42 : var31) {
                        if (var13.setBlockState(var42.pos, var42.blockState, 2)) {
                           ++var9;
                        }
                     }

                     for(CommandClone.StaticCloneData var43 : var16) {
                        TileEntity var47 = var13.getTileEntity(var43.pos);
                        if (var43.nbt != null && var47 != null) {
                           var43.nbt.setInteger("x", var43.pos.getX());
                           var43.nbt.setInteger("y", var43.pos.getY());
                           var43.nbt.setInteger("z", var43.pos.getZ());
                           var47.readFromNBT(var43.nbt);
                           var47.markDirty();
                        }

                        var13.setBlockState(var43.pos, var43.blockState, 2);
                     }

                     for(CommandClone.StaticCloneData var44 : var34) {
                        var13.notifyNeighborsRespectDebug(var44.pos, var44.blockState.getBlock());
                     }

                     List var40 = var13.getPendingBlockUpdates(var7, false);
                     if (var40 != null) {
                        for(NextTickListEntry var48 : var40) {
                           if (var7.isVecInside(var48.position)) {
                              BlockPos var49 = var48.position.add(var19);
                              var13.scheduleBlockUpdate(var49, var48.getBlock(), (int)(var48.scheduledTime - var13.getWorldInfo().getWorldTotalTime()), var48.priority);
                           }
                        }
                     }

                     if (var9 <= 0) {
                        throw new CommandException("commands.clone.failed", new Object[0]);
                     } else {
                        var2.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, var9);
                        notifyCommandListener(var2, this, "commands.clone.success", new Object[]{var9});
                     }
                  } else {
                     throw new CommandException("commands.clone.outOfWorld", new Object[0]);
                  }
               } else {
                  throw new CommandException("commands.clone.outOfWorld", new Object[0]);
               }
            }
         }
      }
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (var3.length > 0 && var3.length <= 3) {
         return getTabCompletionCoordinate(var3, 0, var4);
      } else if (var3.length > 3 && var3.length <= 6) {
         return getTabCompletionCoordinate(var3, 3, var4);
      } else if (var3.length > 6 && var3.length <= 9) {
         return getTabCompletionCoordinate(var3, 6, var4);
      } else if (var3.length == 10) {
         return getListOfStringsMatchingLastWord(var3, new String[]{"replace", "masked", "filtered"});
      } else if (var3.length == 11) {
         return getListOfStringsMatchingLastWord(var3, new String[]{"normal", "force", "move"});
      } else {
         return var3.length == 12 && "filtered".equals(var3[9]) ? getListOfStringsMatchingLastWord(var3, Block.REGISTRY.getKeys()) : Collections.emptyList();
      }
   }

   static class StaticCloneData {
      public final BlockPos pos;
      public final IBlockState blockState;
      public final NBTTagCompound nbt;

      public StaticCloneData(BlockPos var1, IBlockState var2, NBTTagCompound var3) {
         this.pos = var1;
         this.blockState = var2;
         this.nbt = var3;
      }
   }
}
