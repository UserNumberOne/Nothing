package net.minecraft.command.server;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.math.BlockPos;

public class CommandAchievement extends CommandBase {
   public String getName() {
      return "achievement";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.achievement.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 2) {
         throw new WrongUsageException("commands.achievement.usage", new Object[0]);
      } else {
         final StatBase var4 = StatList.getOneShotStat(var3[1]);
         if ((var4 != null || "*".equals(var3[1])) && (var4 == null || var4.isAchievement())) {
            final EntityPlayerMP var5 = var3.length >= 3 ? getPlayer(var1, var2, var3[2]) : getCommandSenderAsPlayer(var2);
            boolean var6 = "give".equalsIgnoreCase(var3[0]);
            boolean var7 = "take".equalsIgnoreCase(var3[0]);
            if (var6 || var7) {
               if (var4 == null) {
                  if (var6) {
                     for(Achievement var9 : AchievementList.ACHIEVEMENTS) {
                        var5.addStat(var9);
                     }

                     notifyCommandListener(var2, this, "commands.achievement.give.success.all", new Object[]{var5.getName()});
                  } else if (var7) {
                     for(Achievement var17 : Lists.reverse(AchievementList.ACHIEVEMENTS)) {
                        var5.takeStat(var17);
                     }

                     notifyCommandListener(var2, this, "commands.achievement.take.success.all", new Object[]{var5.getName()});
                  }
               } else {
                  if (var4 instanceof Achievement) {
                     Achievement var16 = (Achievement)var4;
                     if (var6) {
                        if (var5.getStatFile().hasAchievementUnlocked(var16)) {
                           throw new CommandException("commands.achievement.alreadyHave", new Object[]{var5.getName(), var4.createChatComponent()});
                        }

                        ArrayList var19;
                        for(var19 = Lists.newArrayList(); var16.parentAchievement != null && !var5.getStatFile().hasAchievementUnlocked(var16.parentAchievement); var16 = var16.parentAchievement) {
                           var19.add(var16.parentAchievement);
                        }

                        for(Achievement var22 : Lists.reverse(var19)) {
                           var5.addStat(var22);
                        }
                     } else if (var7) {
                        if (!var5.getStatFile().hasAchievementUnlocked(var16)) {
                           throw new CommandException("commands.achievement.dontHave", new Object[]{var5.getName(), var4.createChatComponent()});
                        }

                        ArrayList var18 = Lists.newArrayList(Iterators.filter(AchievementList.ACHIEVEMENTS.iterator(), new Predicate() {
                           public boolean apply(@Nullable Achievement var1) {
                              return var5.getStatFile().hasAchievementUnlocked(var1) && var1 != var4;
                           }
                        }));
                        ArrayList var10 = Lists.newArrayList(var18);

                        for(Achievement var12 : var18) {
                           Achievement var13 = var12;

                           boolean var14;
                           for(var14 = false; var13 != null; var13 = var13.parentAchievement) {
                              if (var13 == var4) {
                                 var14 = true;
                              }
                           }

                           if (!var14) {
                              for(Achievement var24 = var12; var24 != null; var24 = var24.parentAchievement) {
                                 var10.remove(var12);
                              }
                           }
                        }

                        for(Achievement var23 : var10) {
                           var5.takeStat(var23);
                        }
                     }
                  }

                  if (var6) {
                     var5.addStat(var4);
                     notifyCommandListener(var2, this, "commands.achievement.give.success.one", new Object[]{var5.getName(), var4.createChatComponent()});
                  } else if (var7) {
                     var5.takeStat(var4);
                     notifyCommandListener(var2, this, "commands.achievement.take.success.one", new Object[]{var4.createChatComponent(), var5.getName()});
                  }
               }
            }

         } else {
            throw new CommandException("commands.achievement.unknownAchievement", new Object[]{var3[1]});
         }
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (var3.length == 1) {
         return getListOfStringsMatchingLastWord(var3, new String[]{"give", "take"});
      } else if (var3.length != 2) {
         return var3.length == 3 ? getListOfStringsMatchingLastWord(var3, var1.getOnlinePlayerNames()) : Collections.emptyList();
      } else {
         ArrayList var5 = Lists.newArrayList();

         for(StatBase var7 : AchievementList.ACHIEVEMENTS) {
            var5.add(var7.statId);
         }

         return getListOfStringsMatchingLastWord(var3, var5);
      }
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var2 == 2;
   }
}
