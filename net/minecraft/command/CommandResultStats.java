package net.minecraft.command;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class CommandResultStats {
   private static final int NUM_RESULT_TYPES = CommandResultStats.Type.values().length;
   private static final String[] STRING_RESULT_TYPES = new String[NUM_RESULT_TYPES];
   private String[] entitiesID;
   private String[] objectives;

   public CommandResultStats() {
      this.entitiesID = STRING_RESULT_TYPES;
      this.objectives = STRING_RESULT_TYPES;
   }

   public void setCommandStatForSender(MinecraftServer var1, final ICommandSender var2, CommandResultStats.Type var3, int var4) {
      String s = this.entitiesID[typeIn.getTypeID()];
      if (s != null) {
         ICommandSender icommandsender = new ICommandSender() {
            public String getName() {
               return sender.getName();
            }

            public ITextComponent getDisplayName() {
               return sender.getDisplayName();
            }

            public void sendMessage(ITextComponent var1) {
               sender.sendMessage(component);
            }

            public boolean canUseCommand(int var1, String var2x) {
               return true;
            }

            public BlockPos getPosition() {
               return sender.getPosition();
            }

            public Vec3d getPositionVector() {
               return sender.getPositionVector();
            }

            public World getEntityWorld() {
               return sender.getEntityWorld();
            }

            public Entity getCommandSenderEntity() {
               return sender.getCommandSenderEntity();
            }

            public boolean sendCommandFeedback() {
               return sender.sendCommandFeedback();
            }

            public void setCommandStat(CommandResultStats.Type var1, int var2x) {
               sender.setCommandStat(type, amount);
            }

            public MinecraftServer getServer() {
               return sender.getServer();
            }
         };

         String s1;
         try {
            s1 = CommandBase.getEntityName(server, icommandsender, s);
         } catch (EntityNotFoundException var12) {
            return;
         }

         String s2 = this.objectives[typeIn.getTypeID()];
         if (s2 != null) {
            Scoreboard scoreboard = sender.getEntityWorld().getScoreboard();
            ScoreObjective scoreobjective = scoreboard.getObjective(s2);
            if (scoreobjective != null && scoreboard.entityHasObjective(s1, scoreobjective)) {
               Score score = scoreboard.getOrCreateScore(s1, scoreobjective);
               score.setScorePoints(p_184932_4_);
            }
         }
      }

   }

   public void readStatsFromNBT(NBTTagCompound var1) {
      if (tagcompound.hasKey("CommandStats", 10)) {
         NBTTagCompound nbttagcompound = tagcompound.getCompoundTag("CommandStats");

         for(CommandResultStats.Type commandresultstats$type : CommandResultStats.Type.values()) {
            String s = commandresultstats$type.getTypeName() + "Name";
            String s1 = commandresultstats$type.getTypeName() + "Objective";
            if (nbttagcompound.hasKey(s, 8) && nbttagcompound.hasKey(s1, 8)) {
               String s2 = nbttagcompound.getString(s);
               String s3 = nbttagcompound.getString(s1);
               setScoreBoardStat(this, commandresultstats$type, s2, s3);
            }
         }
      }

   }

   public void writeStatsToNBT(NBTTagCompound var1) {
      NBTTagCompound nbttagcompound = new NBTTagCompound();

      for(CommandResultStats.Type commandresultstats$type : CommandResultStats.Type.values()) {
         String s = this.entitiesID[commandresultstats$type.getTypeID()];
         String s1 = this.objectives[commandresultstats$type.getTypeID()];
         if (s != null && s1 != null) {
            nbttagcompound.setString(commandresultstats$type.getTypeName() + "Name", s);
            nbttagcompound.setString(commandresultstats$type.getTypeName() + "Objective", s1);
         }
      }

      if (!nbttagcompound.hasNoTags()) {
         tagcompound.setTag("CommandStats", nbttagcompound);
      }

   }

   public static void setScoreBoardStat(CommandResultStats var0, CommandResultStats.Type var1, @Nullable String var2, @Nullable String var3) {
      if (entityID != null && !entityID.isEmpty() && objectiveName != null && !objectiveName.isEmpty()) {
         if (stats.entitiesID == STRING_RESULT_TYPES || stats.objectives == STRING_RESULT_TYPES) {
            stats.entitiesID = new String[NUM_RESULT_TYPES];
            stats.objectives = new String[NUM_RESULT_TYPES];
         }

         stats.entitiesID[resultType.getTypeID()] = entityID;
         stats.objectives[resultType.getTypeID()] = objectiveName;
      } else {
         removeScoreBoardStat(stats, resultType);
      }

   }

   private static void removeScoreBoardStat(CommandResultStats var0, CommandResultStats.Type var1) {
      if (resultStatsIn.entitiesID != STRING_RESULT_TYPES && resultStatsIn.objectives != STRING_RESULT_TYPES) {
         resultStatsIn.entitiesID[resultTypeIn.getTypeID()] = null;
         resultStatsIn.objectives[resultTypeIn.getTypeID()] = null;
         boolean flag = true;

         for(CommandResultStats.Type commandresultstats$type : CommandResultStats.Type.values()) {
            if (resultStatsIn.entitiesID[commandresultstats$type.getTypeID()] != null && resultStatsIn.objectives[commandresultstats$type.getTypeID()] != null) {
               flag = false;
               break;
            }
         }

         if (flag) {
            resultStatsIn.entitiesID = STRING_RESULT_TYPES;
            resultStatsIn.objectives = STRING_RESULT_TYPES;
         }
      }

   }

   public void addAllStats(CommandResultStats var1) {
      for(CommandResultStats.Type commandresultstats$type : CommandResultStats.Type.values()) {
         setScoreBoardStat(this, commandresultstats$type, resultStatsIn.entitiesID[commandresultstats$type.getTypeID()], resultStatsIn.objectives[commandresultstats$type.getTypeID()]);
      }

   }

   public static enum Type {
      SUCCESS_COUNT(0, "SuccessCount"),
      AFFECTED_BLOCKS(1, "AffectedBlocks"),
      AFFECTED_ENTITIES(2, "AffectedEntities"),
      AFFECTED_ITEMS(3, "AffectedItems"),
      QUERY_RESULT(4, "QueryResult");

      final int typeID;
      final String typeName;

      private Type(int var3, String var4) {
         this.typeID = id;
         this.typeName = name;
      }

      public int getTypeID() {
         return this.typeID;
      }

      public String getTypeName() {
         return this.typeName;
      }

      public static String[] getTypeNames() {
         String[] astring = new String[values().length];
         int i = 0;

         for(CommandResultStats.Type commandresultstats$type : values()) {
            astring[i++] = commandresultstats$type.getTypeName();
         }

         return astring;
      }

      @Nullable
      public static CommandResultStats.Type getTypeByName(String var0) {
         for(CommandResultStats.Type commandresultstats$type : values()) {
            if (commandresultstats$type.getTypeName().equals(name)) {
               return commandresultstats$type;
            }
         }

         return null;
      }
   }
}
