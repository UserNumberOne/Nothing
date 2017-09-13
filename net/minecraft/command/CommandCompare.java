package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public class CommandCompare extends CommandBase {
   public String getName() {
      return "testforblocks";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.compare.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 9) {
         throw new WrongUsageException("commands.compare.usage", new Object[0]);
      } else {
         var2.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
         BlockPos var4 = parseBlockPos(var2, var3, 0, false);
         BlockPos var5 = parseBlockPos(var2, var3, 3, false);
         BlockPos var6 = parseBlockPos(var2, var3, 6, false);
         StructureBoundingBox var7 = new StructureBoundingBox(var4, var5);
         StructureBoundingBox var8 = new StructureBoundingBox(var6, var6.add(var7.getLength()));
         int var9 = var7.getXSize() * var7.getYSize() * var7.getZSize();
         if (var9 > 524288) {
            throw new CommandException("commands.compare.tooManyBlocks", new Object[]{var9, 524288});
         } else if (var7.minY >= 0 && var7.maxY < 256 && var8.minY >= 0 && var8.maxY < 256) {
            World var10 = var2.getEntityWorld();
            if (var10.isAreaLoaded(var7) && var10.isAreaLoaded(var8)) {
               boolean var11 = false;
               if (var3.length > 9 && "masked".equals(var3[9])) {
                  var11 = true;
               }

               var9 = 0;
               BlockPos var12 = new BlockPos(var8.minX - var7.minX, var8.minY - var7.minY, var8.minZ - var7.minZ);
               BlockPos.MutableBlockPos var13 = new BlockPos.MutableBlockPos();
               BlockPos.MutableBlockPos var14 = new BlockPos.MutableBlockPos();

               for(int var15 = var7.minZ; var15 <= var7.maxZ; ++var15) {
                  for(int var16 = var7.minY; var16 <= var7.maxY; ++var16) {
                     for(int var17 = var7.minX; var17 <= var7.maxX; ++var17) {
                        var13.setPos(var17, var16, var15);
                        var14.setPos(var17 + var12.getX(), var16 + var12.getY(), var15 + var12.getZ());
                        boolean var18 = false;
                        IBlockState var19 = var10.getBlockState(var13);
                        if (!var11 || var19.getBlock() != Blocks.AIR) {
                           if (var19 == var10.getBlockState(var14)) {
                              TileEntity var20 = var10.getTileEntity(var13);
                              TileEntity var21 = var10.getTileEntity(var14);
                              if (var20 != null && var21 != null) {
                                 NBTTagCompound var22 = var20.writeToNBT(new NBTTagCompound());
                                 var22.removeTag("x");
                                 var22.removeTag("y");
                                 var22.removeTag("z");
                                 NBTTagCompound var23 = var21.writeToNBT(new NBTTagCompound());
                                 var23.removeTag("x");
                                 var23.removeTag("y");
                                 var23.removeTag("z");
                                 if (!var22.equals(var23)) {
                                    var18 = true;
                                 }
                              } else if (var20 != null) {
                                 var18 = true;
                              }
                           } else {
                              var18 = true;
                           }

                           ++var9;
                           if (var18) {
                              throw new CommandException("commands.compare.failed", new Object[0]);
                           }
                        }
                     }
                  }
               }

               var2.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, var9);
               notifyCommandListener(var2, this, "commands.compare.success", new Object[]{var9});
            } else {
               throw new CommandException("commands.compare.outOfWorld", new Object[0]);
            }
         } else {
            throw new CommandException("commands.compare.outOfWorld", new Object[0]);
         }
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length > 0 && var3.length <= 3 ? getTabCompletionCoordinate(var3, 0, var4) : (var3.length > 3 && var3.length <= 6 ? getTabCompletionCoordinate(var3, 3, var4) : (var3.length > 6 && var3.length <= 9 ? getTabCompletionCoordinate(var3, 6, var4) : (var3.length == 10 ? getListOfStringsMatchingLastWord(var3, new String[]{"masked", "all"}) : Collections.emptyList())));
   }
}
