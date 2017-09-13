package net.minecraft.command;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandTrigger extends CommandBase {
   public String getName() {
      return "trigger";
   }

   public int getRequiredPermissionLevel() {
      return 0;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.trigger.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 3) {
         throw new WrongUsageException("commands.trigger.usage", new Object[0]);
      } else {
         EntityPlayerMP var4;
         if (var2 instanceof EntityPlayerMP) {
            var4 = (EntityPlayerMP)var2;
         } else {
            Entity var5 = var2.getCommandSenderEntity();
            if (!(var5 instanceof EntityPlayerMP)) {
               throw new CommandException("commands.trigger.invalidPlayer", new Object[0]);
            }

            var4 = (EntityPlayerMP)var5;
         }

         Scoreboard var9 = var1.worldServerForDimension(0).getScoreboard();
         ScoreObjective var6 = var9.getObjective(var3[0]);
         if (var6 != null && var6.getCriteria() == IScoreCriteria.TRIGGER) {
            int var7 = parseInt(var3[2]);
            if (!var9.entityHasObjective(var4.getName(), var6)) {
               throw new CommandException("commands.trigger.invalidObjective", new Object[]{var3[0]});
            } else {
               Score var8 = var9.getOrCreateScore(var4.getName(), var6);
               if (var8.isLocked()) {
                  throw new CommandException("commands.trigger.disabled", new Object[]{var3[0]});
               } else {
                  if ("set".equals(var3[1])) {
                     var8.setScorePoints(var7);
                  } else {
                     if (!"add".equals(var3[1])) {
                        throw new CommandException("commands.trigger.invalidMode", new Object[]{var3[1]});
                     }

                     var8.increaseScore(var7);
                  }

                  var8.setLocked(true);
                  if (var4.interactionManager.isCreative()) {
                     notifyCommandListener(var2, this, "commands.trigger.success", new Object[]{var3[0], var3[1], var3[2]});
                  }

               }
            }
         } else {
            throw new CommandException("commands.trigger.invalidObjective", new Object[]{var3[0]});
         }
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (var3.length == 1) {
         Scoreboard var5 = var1.worldServerForDimension(0).getScoreboard();
         ArrayList var6 = Lists.newArrayList();

         for(ScoreObjective var8 : var5.getScoreObjectives()) {
            if (var8.getCriteria() == IScoreCriteria.TRIGGER) {
               var6.add(var8.getName());
            }
         }

         return getListOfStringsMatchingLastWord(var3, (String[])var6.toArray(new String[var6.size()]));
      } else {
         return var3.length == 2 ? getListOfStringsMatchingLastWord(var3, new String[]{"add", "set"}) : Collections.emptyList();
      }
   }
}
