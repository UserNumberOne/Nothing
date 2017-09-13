package net.minecraft.command.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class CommandScoreboard extends CommandBase {
   public String getName() {
      return "scoreboard";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.scoreboard.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (!this.handleUserWildcards(var1, var2, var3)) {
         if (var3.length < 1) {
            throw new WrongUsageException("commands.scoreboard.usage", new Object[0]);
         }

         if ("objectives".equalsIgnoreCase(var3[0])) {
            if (var3.length == 1) {
               throw new WrongUsageException("commands.scoreboard.objectives.usage", new Object[0]);
            }

            if ("list".equalsIgnoreCase(var3[1])) {
               this.listObjectives(var2, var1);
            } else if ("add".equalsIgnoreCase(var3[1])) {
               if (var3.length < 4) {
                  throw new WrongUsageException("commands.scoreboard.objectives.add.usage", new Object[0]);
               }

               this.addObjective(var2, var3, 2, var1);
            } else if ("remove".equalsIgnoreCase(var3[1])) {
               if (var3.length != 3) {
                  throw new WrongUsageException("commands.scoreboard.objectives.remove.usage", new Object[0]);
               }

               this.removeObjective(var2, var3[2], var1);
            } else {
               if (!"setdisplay".equalsIgnoreCase(var3[1])) {
                  throw new WrongUsageException("commands.scoreboard.objectives.usage", new Object[0]);
               }

               if (var3.length != 3 && var3.length != 4) {
                  throw new WrongUsageException("commands.scoreboard.objectives.setdisplay.usage", new Object[0]);
               }

               this.setDisplayObjective(var2, var3, 2, var1);
            }
         } else if ("players".equalsIgnoreCase(var3[0])) {
            if (var3.length == 1) {
               throw new WrongUsageException("commands.scoreboard.players.usage", new Object[0]);
            }

            if ("list".equalsIgnoreCase(var3[1])) {
               if (var3.length > 3) {
                  throw new WrongUsageException("commands.scoreboard.players.list.usage", new Object[0]);
               }

               this.listPlayers(var2, var3, 2, var1);
            } else if ("add".equalsIgnoreCase(var3[1])) {
               if (var3.length < 5) {
                  throw new WrongUsageException("commands.scoreboard.players.add.usage", new Object[0]);
               }

               this.addPlayerScore(var2, var3, 2, var1);
            } else if ("remove".equalsIgnoreCase(var3[1])) {
               if (var3.length < 5) {
                  throw new WrongUsageException("commands.scoreboard.players.remove.usage", new Object[0]);
               }

               this.addPlayerScore(var2, var3, 2, var1);
            } else if ("set".equalsIgnoreCase(var3[1])) {
               if (var3.length < 5) {
                  throw new WrongUsageException("commands.scoreboard.players.set.usage", new Object[0]);
               }

               this.addPlayerScore(var2, var3, 2, var1);
            } else if ("reset".equalsIgnoreCase(var3[1])) {
               if (var3.length != 3 && var3.length != 4) {
                  throw new WrongUsageException("commands.scoreboard.players.reset.usage", new Object[0]);
               }

               this.resetPlayerScore(var2, var3, 2, var1);
            } else if ("enable".equalsIgnoreCase(var3[1])) {
               if (var3.length != 4) {
                  throw new WrongUsageException("commands.scoreboard.players.enable.usage", new Object[0]);
               }

               this.enablePlayerTrigger(var2, var3, 2, var1);
            } else if ("test".equalsIgnoreCase(var3[1])) {
               if (var3.length != 5 && var3.length != 6) {
                  throw new WrongUsageException("commands.scoreboard.players.test.usage", new Object[0]);
               }

               this.testPlayerScore(var2, var3, 2, var1);
            } else if ("operation".equalsIgnoreCase(var3[1])) {
               if (var3.length != 7) {
                  throw new WrongUsageException("commands.scoreboard.players.operation.usage", new Object[0]);
               }

               this.applyPlayerOperation(var2, var3, 2, var1);
            } else {
               if (!"tag".equalsIgnoreCase(var3[1])) {
                  throw new WrongUsageException("commands.scoreboard.players.usage", new Object[0]);
               }

               if (var3.length < 4) {
                  throw new WrongUsageException("commands.scoreboard.players.tag.usage", new Object[0]);
               }

               this.applyPlayerTag(var1, var2, var3, 2);
            }
         } else {
            if (!"teams".equalsIgnoreCase(var3[0])) {
               throw new WrongUsageException("commands.scoreboard.usage", new Object[0]);
            }

            if (var3.length == 1) {
               throw new WrongUsageException("commands.scoreboard.teams.usage", new Object[0]);
            }

            if ("list".equalsIgnoreCase(var3[1])) {
               if (var3.length > 3) {
                  throw new WrongUsageException("commands.scoreboard.teams.list.usage", new Object[0]);
               }

               this.listTeams(var2, var3, 2, var1);
            } else if ("add".equalsIgnoreCase(var3[1])) {
               if (var3.length < 3) {
                  throw new WrongUsageException("commands.scoreboard.teams.add.usage", new Object[0]);
               }

               this.addTeam(var2, var3, 2, var1);
            } else if ("remove".equalsIgnoreCase(var3[1])) {
               if (var3.length != 3) {
                  throw new WrongUsageException("commands.scoreboard.teams.remove.usage", new Object[0]);
               }

               this.removeTeam(var2, var3, 2, var1);
            } else if ("empty".equalsIgnoreCase(var3[1])) {
               if (var3.length != 3) {
                  throw new WrongUsageException("commands.scoreboard.teams.empty.usage", new Object[0]);
               }

               this.emptyTeam(var2, var3, 2, var1);
            } else if ("join".equalsIgnoreCase(var3[1])) {
               if (var3.length < 4 && (var3.length != 3 || !(var2 instanceof EntityPlayer))) {
                  throw new WrongUsageException("commands.scoreboard.teams.join.usage", new Object[0]);
               }

               this.joinTeam(var2, var3, 2, var1);
            } else if ("leave".equalsIgnoreCase(var3[1])) {
               if (var3.length < 3 && !(var2 instanceof EntityPlayer)) {
                  throw new WrongUsageException("commands.scoreboard.teams.leave.usage", new Object[0]);
               }

               this.leaveTeam(var2, var3, 2, var1);
            } else {
               if (!"option".equalsIgnoreCase(var3[1])) {
                  throw new WrongUsageException("commands.scoreboard.teams.usage", new Object[0]);
               }

               if (var3.length != 4 && var3.length != 5) {
                  throw new WrongUsageException("commands.scoreboard.teams.option.usage", new Object[0]);
               }

               this.setTeamOption(var2, var3, 2, var1);
            }
         }
      }

   }

   private boolean handleUserWildcards(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      int var4 = -1;

      for(int var5 = 0; var5 < var3.length; ++var5) {
         if (this.isUsernameIndex(var3, var5) && "*".equals(var3[var5])) {
            if (var4 >= 0) {
               throw new CommandException("commands.scoreboard.noMultiWildcard", new Object[0]);
            }

            var4 = var5;
         }
      }

      if (var4 < 0) {
         return false;
      } else {
         ArrayList var13 = Lists.newArrayList(this.getScoreboard(var1).getObjectiveNames());
         String var6 = var3[var4];
         ArrayList var7 = Lists.newArrayList();

         for(String var9 : var13) {
            var3[var4] = var9;

            try {
               this.execute(var1, var2, var3);
               var7.add(var9);
            } catch (CommandException var12) {
               TextComponentTranslation var11 = new TextComponentTranslation(var12.getMessage(), var12.getErrorObjects());
               var11.getStyle().setColor(TextFormatting.RED);
               var2.sendMessage(var11);
            }
         }

         var3[var4] = var6;
         var2.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, var7.size());
         if (var7.isEmpty()) {
            throw new WrongUsageException("commands.scoreboard.allMatchesFailed", new Object[0]);
         } else {
            return true;
         }
      }
   }

   protected Scoreboard getScoreboard(MinecraftServer var1) {
      return var1.worldServerForDimension(0).getScoreboard();
   }

   protected ScoreObjective convertToObjective(String var1, boolean var2, MinecraftServer var3) throws CommandException {
      Scoreboard var4 = this.getScoreboard(var3);
      ScoreObjective var5 = var4.getObjective(var1);
      if (var5 == null) {
         throw new CommandException("commands.scoreboard.objectiveNotFound", new Object[]{var1});
      } else if (var2 && var5.getCriteria().isReadOnly()) {
         throw new CommandException("commands.scoreboard.objectiveReadOnly", new Object[]{var1});
      } else {
         return var5;
      }
   }

   protected ScorePlayerTeam convertToTeam(String var1, MinecraftServer var2) throws CommandException {
      Scoreboard var3 = this.getScoreboard(var2);
      ScorePlayerTeam var4 = var3.getTeam(var1);
      if (var4 == null) {
         throw new CommandException("commands.scoreboard.teamNotFound", new Object[]{var1});
      } else {
         return var4;
      }
   }

   protected void addObjective(ICommandSender var1, String[] var2, int var3, MinecraftServer var4) throws CommandException {
      String var5 = var2[var3++];
      String var6 = var2[var3++];
      Scoreboard var7 = this.getScoreboard(var4);
      IScoreCriteria var8 = (IScoreCriteria)IScoreCriteria.INSTANCES.get(var6);
      if (var8 == null) {
         throw new WrongUsageException("commands.scoreboard.objectives.add.wrongType", new Object[]{var6});
      } else if (var7.getObjective(var5) != null) {
         throw new CommandException("commands.scoreboard.objectives.add.alreadyExists", new Object[]{var5});
      } else if (var5.length() > 16) {
         throw new SyntaxErrorException("commands.scoreboard.objectives.add.tooLong", new Object[]{var5, Integer.valueOf(16)});
      } else if (var5.isEmpty()) {
         throw new WrongUsageException("commands.scoreboard.objectives.add.usage", new Object[0]);
      } else {
         if (var2.length > var3) {
            String var9 = getChatComponentFromNthArg(var1, var2, var3).getUnformattedText();
            if (var9.length() > 32) {
               throw new SyntaxErrorException("commands.scoreboard.objectives.add.displayTooLong", new Object[]{var9, Integer.valueOf(32)});
            }

            if (var9.isEmpty()) {
               var7.addScoreObjective(var5, var8);
            } else {
               var7.addScoreObjective(var5, var8).setDisplayName(var9);
            }
         } else {
            var7.addScoreObjective(var5, var8);
         }

         notifyCommandListener(var1, this, "commands.scoreboard.objectives.add.success", new Object[]{var5});
      }
   }

   protected void addTeam(ICommandSender var1, String[] var2, int var3, MinecraftServer var4) throws CommandException {
      String var5 = var2[var3++];
      Scoreboard var6 = this.getScoreboard(var4);
      if (var6.getTeam(var5) != null) {
         throw new CommandException("commands.scoreboard.teams.add.alreadyExists", new Object[]{var5});
      } else if (var5.length() > 16) {
         throw new SyntaxErrorException("commands.scoreboard.teams.add.tooLong", new Object[]{var5, Integer.valueOf(16)});
      } else if (var5.isEmpty()) {
         throw new WrongUsageException("commands.scoreboard.teams.add.usage", new Object[0]);
      } else {
         if (var2.length > var3) {
            String var7 = getChatComponentFromNthArg(var1, var2, var3).getUnformattedText();
            if (var7.length() > 32) {
               throw new SyntaxErrorException("commands.scoreboard.teams.add.displayTooLong", new Object[]{var7, Integer.valueOf(32)});
            }

            if (var7.isEmpty()) {
               var6.createTeam(var5);
            } else {
               var6.createTeam(var5).setTeamName(var7);
            }
         } else {
            var6.createTeam(var5);
         }

         notifyCommandListener(var1, this, "commands.scoreboard.teams.add.success", new Object[]{var5});
      }
   }

   protected void setTeamOption(ICommandSender var1, String[] var2, int var3, MinecraftServer var4) throws CommandException {
      ScorePlayerTeam var5 = this.convertToTeam(var2[var3++], var4);
      if (var5 != null) {
         String var6 = var2[var3++].toLowerCase();
         if (!"color".equalsIgnoreCase(var6) && !"friendlyfire".equalsIgnoreCase(var6) && !"seeFriendlyInvisibles".equalsIgnoreCase(var6) && !"nametagVisibility".equalsIgnoreCase(var6) && !"deathMessageVisibility".equalsIgnoreCase(var6) && !"collisionRule".equalsIgnoreCase(var6)) {
            throw new WrongUsageException("commands.scoreboard.teams.option.usage", new Object[0]);
         }

         if (var2.length == 4) {
            if ("color".equalsIgnoreCase(var6)) {
               throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[]{var6, joinNiceStringFromCollection(TextFormatting.getValidValues(true, false))});
            }

            if (!"friendlyfire".equalsIgnoreCase(var6) && !"seeFriendlyInvisibles".equalsIgnoreCase(var6)) {
               if (!"nametagVisibility".equalsIgnoreCase(var6) && !"deathMessageVisibility".equalsIgnoreCase(var6)) {
                  if ("collisionRule".equalsIgnoreCase(var6)) {
                     throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[]{var6, joinNiceString(Team.CollisionRule.getNames())});
                  }

                  throw new WrongUsageException("commands.scoreboard.teams.option.usage", new Object[0]);
               }

               throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[]{var6, joinNiceString(Team.EnumVisible.getNames())});
            }

            throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[]{var6, joinNiceStringFromCollection(Arrays.asList("true", "false"))});
         }

         String var7 = var2[var3];
         if ("color".equalsIgnoreCase(var6)) {
            TextFormatting var8 = TextFormatting.getValueByName(var7);
            if (var8 == null || var8.isFancyStyling()) {
               throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[]{var6, joinNiceStringFromCollection(TextFormatting.getValidValues(true, false))});
            }

            var5.setChatFormat(var8);
            var5.setNamePrefix(var8.toString());
            var5.setNameSuffix(TextFormatting.RESET.toString());
         } else if ("friendlyfire".equalsIgnoreCase(var6)) {
            if (!"true".equalsIgnoreCase(var7) && !"false".equalsIgnoreCase(var7)) {
               throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[]{var6, joinNiceStringFromCollection(Arrays.asList("true", "false"))});
            }

            var5.setAllowFriendlyFire("true".equalsIgnoreCase(var7));
         } else if ("seeFriendlyInvisibles".equalsIgnoreCase(var6)) {
            if (!"true".equalsIgnoreCase(var7) && !"false".equalsIgnoreCase(var7)) {
               throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[]{var6, joinNiceStringFromCollection(Arrays.asList("true", "false"))});
            }

            var5.setSeeFriendlyInvisiblesEnabled("true".equalsIgnoreCase(var7));
         } else if ("nametagVisibility".equalsIgnoreCase(var6)) {
            Team.EnumVisible var11 = Team.EnumVisible.getByName(var7);
            if (var11 == null) {
               throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[]{var6, joinNiceString(Team.EnumVisible.getNames())});
            }

            var5.setNameTagVisibility(var11);
         } else if ("deathMessageVisibility".equalsIgnoreCase(var6)) {
            Team.EnumVisible var12 = Team.EnumVisible.getByName(var7);
            if (var12 == null) {
               throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[]{var6, joinNiceString(Team.EnumVisible.getNames())});
            }

            var5.setDeathMessageVisibility(var12);
         } else if ("collisionRule".equalsIgnoreCase(var6)) {
            Team.CollisionRule var13 = Team.CollisionRule.getByName(var7);
            if (var13 == null) {
               throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[]{var6, joinNiceString(Team.CollisionRule.getNames())});
            }

            var5.setCollisionRule(var13);
         }

         notifyCommandListener(var1, this, "commands.scoreboard.teams.option.success", new Object[]{var6, var5.getRegisteredName(), var7});
      }

   }

   protected void removeTeam(ICommandSender var1, String[] var2, int var3, MinecraftServer var4) throws CommandException {
      Scoreboard var5 = this.getScoreboard(var4);
      ScorePlayerTeam var6 = this.convertToTeam(var2[var3], var4);
      if (var6 != null) {
         var5.removeTeam(var6);
         notifyCommandListener(var1, this, "commands.scoreboard.teams.remove.success", new Object[]{var6.getRegisteredName()});
      }

   }

   protected void listTeams(ICommandSender var1, String[] var2, int var3, MinecraftServer var4) throws CommandException {
      Scoreboard var5 = this.getScoreboard(var4);
      if (var2.length > var3) {
         ScorePlayerTeam var6 = this.convertToTeam(var2[var3], var4);
         if (var6 == null) {
            return;
         }

         Collection var7 = var6.getMembershipCollection();
         var1.setCommandStat(CommandResultStats.Type.QUERY_RESULT, var7.size());
         if (var7.isEmpty()) {
            throw new CommandException("commands.scoreboard.teams.list.player.empty", new Object[]{var6.getRegisteredName()});
         }

         TextComponentTranslation var8 = new TextComponentTranslation("commands.scoreboard.teams.list.player.count", new Object[]{var7.size(), var6.getRegisteredName()});
         var8.getStyle().setColor(TextFormatting.DARK_GREEN);
         var1.sendMessage(var8);
         var1.sendMessage(new TextComponentString(joinNiceString(var7.toArray())));
      } else {
         Collection var10 = var5.getTeams();
         var1.setCommandStat(CommandResultStats.Type.QUERY_RESULT, var10.size());
         if (var10.isEmpty()) {
            throw new CommandException("commands.scoreboard.teams.list.empty", new Object[0]);
         }

         TextComponentTranslation var11 = new TextComponentTranslation("commands.scoreboard.teams.list.count", new Object[]{var10.size()});
         var11.getStyle().setColor(TextFormatting.DARK_GREEN);
         var1.sendMessage(var11);

         for(ScorePlayerTeam var9 : var10) {
            var1.sendMessage(new TextComponentTranslation("commands.scoreboard.teams.list.entry", new Object[]{var9.getRegisteredName(), var9.getTeamName(), var9.getMembershipCollection().size()}));
         }
      }

   }

   protected void joinTeam(ICommandSender var1, String[] var2, int var3, MinecraftServer var4) throws CommandException {
      Scoreboard var5 = this.getScoreboard(var4);
      String var6 = var2[var3++];
      HashSet var7 = Sets.newHashSet();
      HashSet var8 = Sets.newHashSet();
      if (var1 instanceof EntityPlayer && var3 == var2.length) {
         String var14 = getCommandSenderAsPlayer(var1).getName();
         if (var5.addPlayerToTeam(var14, var6)) {
            var7.add(var14);
         } else {
            var8.add(var14);
         }
      } else {
         while(var3 < var2.length) {
            String var9 = var2[var3++];
            if (var9.startsWith("@")) {
               for(Entity var11 : getEntityList(var4, var1, var9)) {
                  String var12 = getEntityName(var4, var1, var11.getCachedUniqueIdString());
                  if (var5.addPlayerToTeam(var12, var6)) {
                     var7.add(var12);
                  } else {
                     var8.add(var12);
                  }
               }
            } else {
               String var10 = getEntityName(var4, var1, var9);
               if (var5.addPlayerToTeam(var10, var6)) {
                  var7.add(var10);
               } else {
                  var8.add(var10);
               }
            }
         }
      }

      if (!var7.isEmpty()) {
         var1.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, var7.size());
         notifyCommandListener(var1, this, "commands.scoreboard.teams.join.success", new Object[]{var7.size(), var6, joinNiceString(var7.toArray(new String[var7.size()]))});
      }

      if (!var8.isEmpty()) {
         throw new CommandException("commands.scoreboard.teams.join.failure", new Object[]{var8.size(), var6, joinNiceString(var8.toArray(new String[var8.size()]))});
      }
   }

   protected void leaveTeam(ICommandSender var1, String[] var2, int var3, MinecraftServer var4) throws CommandException {
      Scoreboard var5 = this.getScoreboard(var4);
      HashSet var6 = Sets.newHashSet();
      HashSet var7 = Sets.newHashSet();
      if (var1 instanceof EntityPlayer && var3 == var2.length) {
         String var12 = getCommandSenderAsPlayer(var1).getName();
         if (var5.removePlayerFromTeams(var12)) {
            var6.add(var12);
         } else {
            var7.add(var12);
         }
      } else {
         while(var3 < var2.length) {
            String var8 = var2[var3++];
            if (var8.startsWith("@")) {
               for(Entity var10 : getEntityList(var4, var1, var8)) {
                  String var11 = getEntityName(var4, var1, var10.getCachedUniqueIdString());
                  if (var5.removePlayerFromTeams(var11)) {
                     var6.add(var11);
                  } else {
                     var7.add(var11);
                  }
               }
            } else {
               String var9 = getEntityName(var4, var1, var8);
               if (var5.removePlayerFromTeams(var9)) {
                  var6.add(var9);
               } else {
                  var7.add(var9);
               }
            }
         }
      }

      if (!var6.isEmpty()) {
         var1.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, var6.size());
         notifyCommandListener(var1, this, "commands.scoreboard.teams.leave.success", new Object[]{var6.size(), joinNiceString(var6.toArray(new String[var6.size()]))});
      }

      if (!var7.isEmpty()) {
         throw new CommandException("commands.scoreboard.teams.leave.failure", new Object[]{var7.size(), joinNiceString(var7.toArray(new String[var7.size()]))});
      }
   }

   protected void emptyTeam(ICommandSender var1, String[] var2, int var3, MinecraftServer var4) throws CommandException {
      Scoreboard var5 = this.getScoreboard(var4);
      ScorePlayerTeam var6 = this.convertToTeam(var2[var3], var4);
      if (var6 != null) {
         ArrayList var7 = Lists.newArrayList(var6.getMembershipCollection());
         var1.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, var7.size());
         if (var7.isEmpty()) {
            throw new CommandException("commands.scoreboard.teams.empty.alreadyEmpty", new Object[]{var6.getRegisteredName()});
         }

         for(String var9 : var7) {
            var5.removePlayerFromTeam(var9, var6);
         }

         notifyCommandListener(var1, this, "commands.scoreboard.teams.empty.success", new Object[]{var7.size(), var6.getRegisteredName()});
      }

   }

   protected void removeObjective(ICommandSender var1, String var2, MinecraftServer var3) throws CommandException {
      Scoreboard var4 = this.getScoreboard(var3);
      ScoreObjective var5 = this.convertToObjective(var2, false, var3);
      var4.removeObjective(var5);
      notifyCommandListener(var1, this, "commands.scoreboard.objectives.remove.success", new Object[]{var2});
   }

   protected void listObjectives(ICommandSender var1, MinecraftServer var2) throws CommandException {
      Scoreboard var3 = this.getScoreboard(var2);
      Collection var4 = var3.getScoreObjectives();
      if (var4.isEmpty()) {
         throw new CommandException("commands.scoreboard.objectives.list.empty", new Object[0]);
      } else {
         TextComponentTranslation var5 = new TextComponentTranslation("commands.scoreboard.objectives.list.count", new Object[]{var4.size()});
         var5.getStyle().setColor(TextFormatting.DARK_GREEN);
         var1.sendMessage(var5);

         for(ScoreObjective var7 : var4) {
            var1.sendMessage(new TextComponentTranslation("commands.scoreboard.objectives.list.entry", new Object[]{var7.getName(), var7.getDisplayName(), var7.getCriteria().getName()}));
         }

      }
   }

   protected void setDisplayObjective(ICommandSender var1, String[] var2, int var3, MinecraftServer var4) throws CommandException {
      Scoreboard var5 = this.getScoreboard(var4);
      String var6 = var2[var3++];
      int var7 = Scoreboard.getObjectiveDisplaySlotNumber(var6);
      ScoreObjective var8 = null;
      if (var2.length == 4) {
         var8 = this.convertToObjective(var2[var3], false, var4);
      }

      if (var7 < 0) {
         throw new CommandException("commands.scoreboard.objectives.setdisplay.invalidSlot", new Object[]{var6});
      } else {
         var5.setObjectiveInDisplaySlot(var7, var8);
         if (var8 != null) {
            notifyCommandListener(var1, this, "commands.scoreboard.objectives.setdisplay.successSet", new Object[]{Scoreboard.getObjectiveDisplaySlot(var7), var8.getName()});
         } else {
            notifyCommandListener(var1, this, "commands.scoreboard.objectives.setdisplay.successCleared", new Object[]{Scoreboard.getObjectiveDisplaySlot(var7)});
         }

      }
   }

   protected void listPlayers(ICommandSender var1, String[] var2, int var3, MinecraftServer var4) throws CommandException {
      Scoreboard var5 = this.getScoreboard(var4);
      if (var2.length > var3) {
         String var6 = getEntityName(var4, var1, var2[var3]);
         Map var7 = var5.getObjectivesForEntity(var6);
         var1.setCommandStat(CommandResultStats.Type.QUERY_RESULT, var7.size());
         if (var7.isEmpty()) {
            throw new CommandException("commands.scoreboard.players.list.player.empty", new Object[]{var6});
         }

         TextComponentTranslation var8 = new TextComponentTranslation("commands.scoreboard.players.list.player.count", new Object[]{var7.size(), var6});
         var8.getStyle().setColor(TextFormatting.DARK_GREEN);
         var1.sendMessage(var8);

         for(Score var10 : var7.values()) {
            var1.sendMessage(new TextComponentTranslation("commands.scoreboard.players.list.player.entry", new Object[]{var10.getScorePoints(), var10.getObjective().getDisplayName(), var10.getObjective().getName()}));
         }
      } else {
         Collection var11 = var5.getObjectiveNames();
         var1.setCommandStat(CommandResultStats.Type.QUERY_RESULT, var11.size());
         if (var11.isEmpty()) {
            throw new CommandException("commands.scoreboard.players.list.empty", new Object[0]);
         }

         TextComponentTranslation var12 = new TextComponentTranslation("commands.scoreboard.players.list.count", new Object[]{var11.size()});
         var12.getStyle().setColor(TextFormatting.DARK_GREEN);
         var1.sendMessage(var12);
         var1.sendMessage(new TextComponentString(joinNiceString(var11.toArray())));
      }

   }

   protected void addPlayerScore(ICommandSender var1, String[] var2, int var3, MinecraftServer var4) throws CommandException {
      String var5 = var2[var3 - 1];
      int var6 = var3;
      String var7 = getEntityName(var4, var1, var2[var3++]);
      if (var7.length() > 40) {
         throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong", new Object[]{var7, Integer.valueOf(40)});
      } else {
         ScoreObjective var8 = this.convertToObjective(var2[var3++], true, var4);
         int var9 = "set".equalsIgnoreCase(var5) ? parseInt(var2[var3++]) : parseInt(var2[var3++], 0);
         if (var2.length > var3) {
            Entity var10 = getEntity(var4, var1, var2[var6]);

            try {
               NBTTagCompound var11 = JsonToNBT.getTagFromJson(buildString(var2, var3));
               NBTTagCompound var12 = entityToNBT(var10);
               if (!NBTUtil.areNBTEquals(var11, var12, true)) {
                  throw new CommandException("commands.scoreboard.players.set.tagMismatch", new Object[]{var7});
               }
            } catch (NBTException var13) {
               throw new CommandException("commands.scoreboard.players.set.tagError", new Object[]{var13.getMessage()});
            }
         }

         Scoreboard var17 = this.getScoreboard(var4);
         Score var18 = var17.getOrCreateScore(var7, var8);
         if ("set".equalsIgnoreCase(var5)) {
            var18.setScorePoints(var9);
         } else if ("add".equalsIgnoreCase(var5)) {
            var18.increaseScore(var9);
         } else {
            var18.decreaseScore(var9);
         }

         notifyCommandListener(var1, this, "commands.scoreboard.players.set.success", new Object[]{var8.getName(), var7, var18.getScorePoints()});
      }
   }

   protected void resetPlayerScore(ICommandSender var1, String[] var2, int var3, MinecraftServer var4) throws CommandException {
      Scoreboard var5 = this.getScoreboard(var4);
      String var6 = getEntityName(var4, var1, var2[var3++]);
      if (var2.length > var3) {
         ScoreObjective var7 = this.convertToObjective(var2[var3++], false, var4);
         var5.removeObjectiveFromEntity(var6, var7);
         notifyCommandListener(var1, this, "commands.scoreboard.players.resetscore.success", new Object[]{var7.getName(), var6});
      } else {
         var5.removeObjectiveFromEntity(var6, (ScoreObjective)null);
         notifyCommandListener(var1, this, "commands.scoreboard.players.reset.success", new Object[]{var6});
      }

   }

   protected void enablePlayerTrigger(ICommandSender var1, String[] var2, int var3, MinecraftServer var4) throws CommandException {
      Scoreboard var5 = this.getScoreboard(var4);
      String var6 = getPlayerName(var4, var1, var2[var3++]);
      if (var6.length() > 40) {
         throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong", new Object[]{var6, Integer.valueOf(40)});
      } else {
         ScoreObjective var7 = this.convertToObjective(var2[var3], false, var4);
         if (var7.getCriteria() != IScoreCriteria.TRIGGER) {
            throw new CommandException("commands.scoreboard.players.enable.noTrigger", new Object[]{var7.getName()});
         } else {
            Score var8 = var5.getOrCreateScore(var6, var7);
            var8.setLocked(false);
            notifyCommandListener(var1, this, "commands.scoreboard.players.enable.success", new Object[]{var7.getName(), var6});
         }
      }
   }

   protected void testPlayerScore(ICommandSender var1, String[] var2, int var3, MinecraftServer var4) throws CommandException {
      Scoreboard var5 = this.getScoreboard(var4);
      String var6 = getEntityName(var4, var1, var2[var3++]);
      if (var6.length() > 40) {
         throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong", new Object[]{var6, Integer.valueOf(40)});
      } else {
         ScoreObjective var7 = this.convertToObjective(var2[var3++], false, var4);
         if (!var5.entityHasObjective(var6, var7)) {
            throw new CommandException("commands.scoreboard.players.test.notFound", new Object[]{var7.getName(), var6});
         } else {
            int var8 = var2[var3].equals("*") ? Integer.MIN_VALUE : parseInt(var2[var3]);
            ++var3;
            int var9 = var3 < var2.length && !var2[var3].equals("*") ? parseInt(var2[var3], var8) : Integer.MAX_VALUE;
            Score var10 = var5.getOrCreateScore(var6, var7);
            if (var10.getScorePoints() >= var8 && var10.getScorePoints() <= var9) {
               notifyCommandListener(var1, this, "commands.scoreboard.players.test.success", new Object[]{var10.getScorePoints(), var8, var9});
            } else {
               throw new CommandException("commands.scoreboard.players.test.failed", new Object[]{var10.getScorePoints(), var8, var9});
            }
         }
      }
   }

   protected void applyPlayerOperation(ICommandSender var1, String[] var2, int var3, MinecraftServer var4) throws CommandException {
      Scoreboard var5 = this.getScoreboard(var4);
      String var6 = getEntityName(var4, var1, var2[var3++]);
      ScoreObjective var7 = this.convertToObjective(var2[var3++], true, var4);
      String var8 = var2[var3++];
      String var9 = getEntityName(var4, var1, var2[var3++]);
      ScoreObjective var10 = this.convertToObjective(var2[var3], false, var4);
      if (var6.length() > 40) {
         throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong", new Object[]{var6, Integer.valueOf(40)});
      } else if (var9.length() > 40) {
         throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong", new Object[]{var9, Integer.valueOf(40)});
      } else {
         Score var11 = var5.getOrCreateScore(var6, var7);
         if (!var5.entityHasObjective(var9, var10)) {
            throw new CommandException("commands.scoreboard.players.operation.notFound", new Object[]{var10.getName(), var9});
         } else {
            Score var12 = var5.getOrCreateScore(var9, var10);
            if ("+=".equals(var8)) {
               var11.setScorePoints(var11.getScorePoints() + var12.getScorePoints());
            } else if ("-=".equals(var8)) {
               var11.setScorePoints(var11.getScorePoints() - var12.getScorePoints());
            } else if ("*=".equals(var8)) {
               var11.setScorePoints(var11.getScorePoints() * var12.getScorePoints());
            } else if ("/=".equals(var8)) {
               if (var12.getScorePoints() != 0) {
                  var11.setScorePoints(var11.getScorePoints() / var12.getScorePoints());
               }
            } else if ("%=".equals(var8)) {
               if (var12.getScorePoints() != 0) {
                  var11.setScorePoints(var11.getScorePoints() % var12.getScorePoints());
               }
            } else if ("=".equals(var8)) {
               var11.setScorePoints(var12.getScorePoints());
            } else if ("<".equals(var8)) {
               var11.setScorePoints(Math.min(var11.getScorePoints(), var12.getScorePoints()));
            } else if (">".equals(var8)) {
               var11.setScorePoints(Math.max(var11.getScorePoints(), var12.getScorePoints()));
            } else {
               if (!"><".equals(var8)) {
                  throw new CommandException("commands.scoreboard.players.operation.invalidOperation", new Object[]{var8});
               }

               int var13 = var11.getScorePoints();
               var11.setScorePoints(var12.getScorePoints());
               var12.setScorePoints(var13);
            }

            notifyCommandListener(var1, this, "commands.scoreboard.players.operation.success", new Object[0]);
         }
      }
   }

   protected void applyPlayerTag(MinecraftServer var1, ICommandSender var2, String[] var3, int var4) throws CommandException {
      String var5 = getEntityName(var1, var2, var3[var4]);
      Entity var6 = getEntity(var1, var2, var3[var4++]);
      String var7 = var3[var4++];
      Set var8 = var6.getTags();
      if ("list".equals(var7)) {
         if (!var8.isEmpty()) {
            TextComponentTranslation var9 = new TextComponentTranslation("commands.scoreboard.players.tag.list", new Object[]{var5});
            var9.getStyle().setColor(TextFormatting.DARK_GREEN);
            var2.sendMessage(var9);
            var2.sendMessage(new TextComponentString(joinNiceString(var8.toArray())));
         }

         var2.setCommandStat(CommandResultStats.Type.QUERY_RESULT, var8.size());
      } else {
         if (var3.length < 5) {
            throw new WrongUsageException("commands.scoreboard.players.tag.usage", new Object[0]);
         }

         String var16 = var3[var4++];
         if (var3.length > var4) {
            try {
               NBTTagCompound var10 = JsonToNBT.getTagFromJson(buildString(var3, var4));
               NBTTagCompound var11 = entityToNBT(var6);
               if (!NBTUtil.areNBTEquals(var10, var11, true)) {
                  throw new CommandException("commands.scoreboard.players.tag.tagMismatch", new Object[]{var5});
               }
            } catch (NBTException var12) {
               throw new CommandException("commands.scoreboard.players.tag.tagError", new Object[]{var12.getMessage()});
            }
         }

         if ("add".equals(var7)) {
            if (!var6.addTag(var16)) {
               throw new CommandException("commands.scoreboard.players.tag.tooMany", new Object[]{Integer.valueOf(1024)});
            }

            notifyCommandListener(var2, this, "commands.scoreboard.players.tag.success.add", new Object[]{var16});
         } else {
            if (!"remove".equals(var7)) {
               throw new WrongUsageException("commands.scoreboard.players.tag.usage", new Object[0]);
            }

            if (!var6.removeTag(var16)) {
               throw new CommandException("commands.scoreboard.players.tag.notFound", new Object[]{var16});
            }

            notifyCommandListener(var2, this, "commands.scoreboard.players.tag.success.remove", new Object[]{var16});
         }
      }

   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (var3.length == 1) {
         return getListOfStringsMatchingLastWord(var3, new String[]{"objectives", "players", "teams"});
      } else {
         if ("objectives".equalsIgnoreCase(var3[0])) {
            if (var3.length == 2) {
               return getListOfStringsMatchingLastWord(var3, new String[]{"list", "add", "remove", "setdisplay"});
            }

            if ("add".equalsIgnoreCase(var3[1])) {
               if (var3.length == 4) {
                  Set var5 = IScoreCriteria.INSTANCES.keySet();
                  return getListOfStringsMatchingLastWord(var3, var5);
               }
            } else if ("remove".equalsIgnoreCase(var3[1])) {
               if (var3.length == 3) {
                  return getListOfStringsMatchingLastWord(var3, this.getObjectiveNames(false, var1));
               }
            } else if ("setdisplay".equalsIgnoreCase(var3[1])) {
               if (var3.length == 3) {
                  return getListOfStringsMatchingLastWord(var3, Scoreboard.getDisplaySlotStrings());
               }

               if (var3.length == 4) {
                  return getListOfStringsMatchingLastWord(var3, this.getObjectiveNames(false, var1));
               }
            }
         } else if ("players".equalsIgnoreCase(var3[0])) {
            if (var3.length == 2) {
               return getListOfStringsMatchingLastWord(var3, new String[]{"set", "add", "remove", "reset", "list", "enable", "test", "operation", "tag"});
            }

            if (!"set".equalsIgnoreCase(var3[1]) && !"add".equalsIgnoreCase(var3[1]) && !"remove".equalsIgnoreCase(var3[1]) && !"reset".equalsIgnoreCase(var3[1])) {
               if ("enable".equalsIgnoreCase(var3[1])) {
                  if (var3.length == 3) {
                     return getListOfStringsMatchingLastWord(var3, var1.getOnlinePlayerNames());
                  }

                  if (var3.length == 4) {
                     return getListOfStringsMatchingLastWord(var3, this.getTriggerNames(var1));
                  }
               } else if (!"list".equalsIgnoreCase(var3[1]) && !"test".equalsIgnoreCase(var3[1])) {
                  if ("operation".equalsIgnoreCase(var3[1])) {
                     if (var3.length == 3) {
                        return getListOfStringsMatchingLastWord(var3, this.getScoreboard(var1).getObjectiveNames());
                     }

                     if (var3.length == 4) {
                        return getListOfStringsMatchingLastWord(var3, this.getObjectiveNames(true, var1));
                     }

                     if (var3.length == 5) {
                        return getListOfStringsMatchingLastWord(var3, new String[]{"+=", "-=", "*=", "/=", "%=", "=", "<", ">", "><"});
                     }

                     if (var3.length == 6) {
                        return getListOfStringsMatchingLastWord(var3, var1.getOnlinePlayerNames());
                     }

                     if (var3.length == 7) {
                        return getListOfStringsMatchingLastWord(var3, this.getObjectiveNames(false, var1));
                     }
                  } else if ("tag".equalsIgnoreCase(var3[1])) {
                     if (var3.length == 3) {
                        return getListOfStringsMatchingLastWord(var3, this.getScoreboard(var1).getObjectiveNames());
                     }

                     if (var3.length == 4) {
                        return getListOfStringsMatchingLastWord(var3, new String[]{"add", "remove", "list"});
                     }
                  }
               } else {
                  if (var3.length == 3) {
                     return getListOfStringsMatchingLastWord(var3, this.getScoreboard(var1).getObjectiveNames());
                  }

                  if (var3.length == 4 && "test".equalsIgnoreCase(var3[1])) {
                     return getListOfStringsMatchingLastWord(var3, this.getObjectiveNames(false, var1));
                  }
               }
            } else {
               if (var3.length == 3) {
                  return getListOfStringsMatchingLastWord(var3, var1.getOnlinePlayerNames());
               }

               if (var3.length == 4) {
                  return getListOfStringsMatchingLastWord(var3, this.getObjectiveNames(true, var1));
               }
            }
         } else if ("teams".equalsIgnoreCase(var3[0])) {
            if (var3.length == 2) {
               return getListOfStringsMatchingLastWord(var3, new String[]{"add", "remove", "join", "leave", "empty", "list", "option"});
            }

            if ("join".equalsIgnoreCase(var3[1])) {
               if (var3.length == 3) {
                  return getListOfStringsMatchingLastWord(var3, this.getScoreboard(var1).getTeamNames());
               }

               if (var3.length >= 4) {
                  return getListOfStringsMatchingLastWord(var3, var1.getOnlinePlayerNames());
               }
            } else {
               if ("leave".equalsIgnoreCase(var3[1])) {
                  return getListOfStringsMatchingLastWord(var3, var1.getOnlinePlayerNames());
               }

               if (!"empty".equalsIgnoreCase(var3[1]) && !"list".equalsIgnoreCase(var3[1]) && !"remove".equalsIgnoreCase(var3[1])) {
                  if ("option".equalsIgnoreCase(var3[1])) {
                     if (var3.length == 3) {
                        return getListOfStringsMatchingLastWord(var3, this.getScoreboard(var1).getTeamNames());
                     }

                     if (var3.length == 4) {
                        return getListOfStringsMatchingLastWord(var3, new String[]{"color", "friendlyfire", "seeFriendlyInvisibles", "nametagVisibility", "deathMessageVisibility", "collisionRule"});
                     }

                     if (var3.length == 5) {
                        if ("color".equalsIgnoreCase(var3[3])) {
                           return getListOfStringsMatchingLastWord(var3, TextFormatting.getValidValues(true, false));
                        }

                        if ("nametagVisibility".equalsIgnoreCase(var3[3]) || "deathMessageVisibility".equalsIgnoreCase(var3[3])) {
                           return getListOfStringsMatchingLastWord(var3, Team.EnumVisible.getNames());
                        }

                        if ("collisionRule".equalsIgnoreCase(var3[3])) {
                           return getListOfStringsMatchingLastWord(var3, Team.CollisionRule.getNames());
                        }

                        if ("friendlyfire".equalsIgnoreCase(var3[3]) || "seeFriendlyInvisibles".equalsIgnoreCase(var3[3])) {
                           return getListOfStringsMatchingLastWord(var3, new String[]{"true", "false"});
                        }
                     }
                  }
               } else if (var3.length == 3) {
                  return getListOfStringsMatchingLastWord(var3, this.getScoreboard(var1).getTeamNames());
               }
            }
         }

         return Collections.emptyList();
      }
   }

   protected List getObjectiveNames(boolean var1, MinecraftServer var2) {
      Collection var3 = this.getScoreboard(var2).getScoreObjectives();
      ArrayList var4 = Lists.newArrayList();

      for(ScoreObjective var6 : var3) {
         if (!var1 || !var6.getCriteria().isReadOnly()) {
            var4.add(var6.getName());
         }
      }

      return var4;
   }

   protected List getTriggerNames(MinecraftServer var1) {
      Collection var2 = this.getScoreboard(var1).getScoreObjectives();
      ArrayList var3 = Lists.newArrayList();

      for(ScoreObjective var5 : var2) {
         if (var5.getCriteria() == IScoreCriteria.TRIGGER) {
            var3.add(var5.getName());
         }
      }

      return var3;
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return !"players".equalsIgnoreCase(var1[0]) ? ("teams".equalsIgnoreCase(var1[0]) ? var2 == 2 : false) : (var1.length > 1 && "operation".equalsIgnoreCase(var1[1]) ? var2 == 2 || var2 == 5 : var2 == 2);
   }
}
