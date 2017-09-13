package net.minecraft.command.server;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
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
      if (args.length < 2) {
         throw new WrongUsageException("commands.achievement.usage", new Object[0]);
      } else {
         final StatBase statbase = StatList.getOneShotStat(args[1]);
         if ((statbase != null || "*".equals(args[1])) && (statbase == null || statbase.isAchievement())) {
            final EntityPlayerMP entityplayermp = args.length >= 3 ? getPlayer(server, sender, args[2]) : getCommandSenderAsPlayer(sender);
            boolean flag = "give".equalsIgnoreCase(args[0]);
            boolean flag1 = "take".equalsIgnoreCase(args[0]);
            if (flag || flag1) {
               if (statbase == null) {
                  if (flag) {
                     for(Achievement achievement4 : AchievementList.ACHIEVEMENTS) {
                        entityplayermp.addStat(achievement4);
                     }

                     notifyCommandListener(sender, this, "commands.achievement.give.success.all", new Object[]{entityplayermp.getName()});
                  } else if (flag1) {
                     for(Achievement achievement5 : Lists.reverse(AchievementList.ACHIEVEMENTS)) {
                        entityplayermp.takeStat(achievement5);
                     }

                     notifyCommandListener(sender, this, "commands.achievement.take.success.all", new Object[]{entityplayermp.getName()});
                  }
               } else {
                  if (statbase instanceof Achievement) {
                     Achievement achievement = (Achievement)statbase;
                     if (flag) {
                        if (entityplayermp.getStatFile().hasAchievementUnlocked(achievement)) {
                           throw new CommandException("commands.achievement.alreadyHave", new Object[]{entityplayermp.getName(), statbase.createChatComponent()});
                        }

                        Object list;
                        for(list = Lists.newArrayList(); achievement.parentAchievement != null && !entityplayermp.getStatFile().hasAchievementUnlocked(achievement.parentAchievement); achievement = achievement.parentAchievement) {
                           list.add(achievement.parentAchievement);
                        }

                        for(Achievement achievement1 : Lists.reverse(list)) {
                           entityplayermp.addStat(achievement1);
                        }
                     } else if (flag1) {
                        if (!entityplayermp.getStatFile().hasAchievementUnlocked(achievement)) {
                           throw new CommandException("commands.achievement.dontHave", new Object[]{entityplayermp.getName(), statbase.createChatComponent()});
                        }

                        List list1 = Lists.newArrayList(Iterators.filter(AchievementList.ACHIEVEMENTS.iterator(), new Predicate() {
                           public boolean apply(@Nullable Achievement var1) {
                              return entityplayermp.getStatFile().hasAchievementUnlocked(p_apply_1_) && p_apply_1_ != statbase;
                           }
                        }));
                        List list2 = Lists.newArrayList(list1);

                        for(Achievement achievement2 : list1) {
                           Achievement achievement3 = achievement2;

                           boolean flag2;
                           for(flag2 = false; achievement3 != null; achievement3 = achievement3.parentAchievement) {
                              if (achievement3 == statbase) {
                                 flag2 = true;
                              }
                           }

                           if (!flag2) {
                              for(Achievement var24 = achievement2; var24 != null; var24 = var24.parentAchievement) {
                                 list2.remove(achievement2);
                              }
                           }
                        }

                        for(Achievement achievement6 : list2) {
                           entityplayermp.takeStat(achievement6);
                        }
                     }
                  }

                  if (flag) {
                     entityplayermp.addStat(statbase);
                     notifyCommandListener(sender, this, "commands.achievement.give.success.one", new Object[]{entityplayermp.getName(), statbase.createChatComponent()});
                  } else if (flag1) {
                     entityplayermp.takeStat(statbase);
                     notifyCommandListener(sender, this, "commands.achievement.take.success.one", new Object[]{statbase.createChatComponent(), entityplayermp.getName()});
                  }
               }
            }

         } else {
            throw new CommandException("commands.achievement.unknownAchievement", new Object[]{args[1]});
         }
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (args.length == 1) {
         return getListOfStringsMatchingLastWord(args, new String[]{"give", "take"});
      } else if (args.length != 2) {
         return args.length == 3 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : Collections.emptyList();
      } else {
         List list = Lists.newArrayList();

         for(StatBase statbase : AchievementList.ACHIEVEMENTS) {
            list.add(statbase.statId);
         }

         return getListOfStringsMatchingLastWord(args, list);
      }
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return index == 2;
   }
}
