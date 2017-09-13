package net.minecraft.command;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CommandStats extends CommandBase {
   public String getName() {
      return "stats";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.stats.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 1) {
         throw new WrongUsageException("commands.stats.usage", new Object[0]);
      } else {
         boolean var4;
         if ("entity".equals(var3[0])) {
            var4 = false;
         } else {
            if (!"block".equals(var3[0])) {
               throw new WrongUsageException("commands.stats.usage", new Object[0]);
            }

            var4 = true;
         }

         int var5;
         if (var4) {
            if (var3.length < 5) {
               throw new WrongUsageException("commands.stats.block.usage", new Object[0]);
            }

            var5 = 4;
         } else {
            if (var3.length < 3) {
               throw new WrongUsageException("commands.stats.entity.usage", new Object[0]);
            }

            var5 = 2;
         }

         String var6 = var3[var5++];
         if ("set".equals(var6)) {
            if (var3.length < var5 + 3) {
               if (var5 == 5) {
                  throw new WrongUsageException("commands.stats.block.set.usage", new Object[0]);
               }

               throw new WrongUsageException("commands.stats.entity.set.usage", new Object[0]);
            }
         } else {
            if (!"clear".equals(var6)) {
               throw new WrongUsageException("commands.stats.usage", new Object[0]);
            }

            if (var3.length < var5 + 1) {
               if (var5 == 5) {
                  throw new WrongUsageException("commands.stats.block.clear.usage", new Object[0]);
               }

               throw new WrongUsageException("commands.stats.entity.clear.usage", new Object[0]);
            }
         }

         CommandResultStats.Type var7 = CommandResultStats.Type.getTypeByName(var3[var5++]);
         if (var7 == null) {
            throw new CommandException("commands.stats.failed", new Object[0]);
         } else {
            World var8 = var2.getEntityWorld();
            CommandResultStats var9;
            if (var4) {
               BlockPos var10 = parseBlockPos(var2, var3, 1, false);
               TileEntity var11 = var8.getTileEntity(var10);
               if (var11 == null) {
                  throw new CommandException("commands.stats.noCompatibleBlock", new Object[]{var10.getX(), var10.getY(), var10.getZ()});
               }

               if (var11 instanceof TileEntityCommandBlock) {
                  var9 = ((TileEntityCommandBlock)var11).getCommandResultStats();
               } else {
                  if (!(var11 instanceof TileEntitySign)) {
                     throw new CommandException("commands.stats.noCompatibleBlock", new Object[]{var10.getX(), var10.getY(), var10.getZ()});
                  }

                  var9 = ((TileEntitySign)var11).getStats();
               }
            } else {
               Entity var15 = getEntity(var1, var2, var3[1]);
               var9 = var15.getCommandStats();
            }

            if ("set".equals(var6)) {
               String var16 = var3[var5++];
               String var18 = var3[var5];
               if (var16.isEmpty() || var18.isEmpty()) {
                  throw new CommandException("commands.stats.failed", new Object[0]);
               }

               CommandResultStats.setScoreBoardStat(var9, var7, var16, var18);
               notifyCommandListener(var2, this, "commands.stats.success", new Object[]{var7.getTypeName(), var18, var16});
            } else if ("clear".equals(var6)) {
               CommandResultStats.setScoreBoardStat(var9, var7, (String)null, (String)null);
               notifyCommandListener(var2, this, "commands.stats.cleared", new Object[]{var7.getTypeName()});
            }

            if (var4) {
               BlockPos var17 = parseBlockPos(var2, var3, 1, false);
               TileEntity var19 = var8.getTileEntity(var17);
               var19.markDirty();
            }

         }
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length == 1 ? getListOfStringsMatchingLastWord(var3, new String[]{"entity", "block"}) : (var3.length == 2 && "entity".equals(var3[0]) ? getListOfStringsMatchingLastWord(var3, var1.getOnlinePlayerNames()) : (var3.length >= 2 && var3.length <= 4 && "block".equals(var3[0]) ? getTabCompletionCoordinate(var3, 1, var4) : ((var3.length != 3 || !"entity".equals(var3[0])) && (var3.length != 5 || !"block".equals(var3[0])) ? ((var3.length != 4 || !"entity".equals(var3[0])) && (var3.length != 6 || !"block".equals(var3[0])) ? (var3.length == 6 && "entity".equals(var3[0]) || var3.length == 8 && "block".equals(var3[0]) ? getListOfStringsMatchingLastWord(var3, this.getObjectiveNames(var1)) : Collections.emptyList()) : getListOfStringsMatchingLastWord(var3, CommandResultStats.Type.getTypeNames())) : getListOfStringsMatchingLastWord(var3, new String[]{"set", "clear"}))));
   }

   protected List getObjectiveNames(MinecraftServer var1) {
      Collection var2 = var1.worldServerForDimension(0).getScoreboard().getScoreObjectives();
      ArrayList var3 = Lists.newArrayList();

      for(ScoreObjective var5 : var2) {
         if (!var5.getCriteria().isReadOnly()) {
            var3.add(var5.getName());
         }
      }

      return var3;
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var1.length > 0 && "entity".equals(var1[0]) && var2 == 1;
   }
}
