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
      String var5 = this.entitiesID[var3.getTypeID()];
      if (var5 != null) {
         ICommandSender var6 = new ICommandSender() {
            public String getName() {
               return var2.getName();
            }

            public ITextComponent getDisplayName() {
               return var2.getDisplayName();
            }

            public void sendMessage(ITextComponent var1) {
               var2.sendMessage(var1);
            }

            public boolean canUseCommand(int var1, String var2x) {
               return true;
            }

            public BlockPos getPosition() {
               return var2.getPosition();
            }

            public Vec3d getPositionVector() {
               return var2.getPositionVector();
            }

            public World getEntityWorld() {
               return var2.getEntityWorld();
            }

            public Entity getCommandSenderEntity() {
               return var2.getCommandSenderEntity();
            }

            public boolean sendCommandFeedback() {
               return var2.sendCommandFeedback();
            }

            public void setCommandStat(CommandResultStats.Type var1, int var2x) {
               var2.setCommandStat(var1, var2x);
            }

            public MinecraftServer getServer() {
               return var2.getServer();
            }
         };

         String var7;
         try {
            var7 = CommandBase.getEntityName(var1, var6, var5);
         } catch (EntityNotFoundException var12) {
            return;
         }

         String var8 = this.objectives[var3.getTypeID()];
         if (var8 != null) {
            Scoreboard var9 = var2.getEntityWorld().getScoreboard();
            ScoreObjective var10 = var9.getObjective(var8);
            if (var10 != null && var9.entityHasObjective(var7, var10)) {
               Score var11 = var9.getOrCreateScore(var7, var10);
               var11.setScorePoints(var4);
            }
         }
      }

   }

   public void readStatsFromNBT(NBTTagCompound var1) {
      if (var1.hasKey("CommandStats", 10)) {
         NBTTagCompound var2 = var1.getCompoundTag("CommandStats");

         for(CommandResultStats.Type var6 : CommandResultStats.Type.values()) {
            String var7 = var6.getTypeName() + "Name";
            String var8 = var6.getTypeName() + "Objective";
            if (var2.hasKey(var7, 8) && var2.hasKey(var8, 8)) {
               String var9 = var2.getString(var7);
               String var10 = var2.getString(var8);
               setScoreBoardStat(this, var6, var9, var10);
            }
         }
      }

   }

   public void writeStatsToNBT(NBTTagCompound var1) {
      NBTTagCompound var2 = new NBTTagCompound();

      for(CommandResultStats.Type var6 : CommandResultStats.Type.values()) {
         String var7 = this.entitiesID[var6.getTypeID()];
         String var8 = this.objectives[var6.getTypeID()];
         if (var7 != null && var8 != null) {
            var2.setString(var6.getTypeName() + "Name", var7);
            var2.setString(var6.getTypeName() + "Objective", var8);
         }
      }

      if (!var2.hasNoTags()) {
         var1.setTag("CommandStats", var2);
      }

   }

   public static void setScoreBoardStat(CommandResultStats var0, CommandResultStats.Type var1, @Nullable String var2, @Nullable String var3) {
      if (var2 != null && !var2.isEmpty() && var3 != null && !var3.isEmpty()) {
         if (var0.entitiesID == STRING_RESULT_TYPES || var0.objectives == STRING_RESULT_TYPES) {
            var0.entitiesID = new String[NUM_RESULT_TYPES];
            var0.objectives = new String[NUM_RESULT_TYPES];
         }

         var0.entitiesID[var1.getTypeID()] = var2;
         var0.objectives[var1.getTypeID()] = var3;
      } else {
         removeScoreBoardStat(var0, var1);
      }

   }

   private static void removeScoreBoardStat(CommandResultStats var0, CommandResultStats.Type var1) {
      if (var0.entitiesID != STRING_RESULT_TYPES && var0.objectives != STRING_RESULT_TYPES) {
         var0.entitiesID[var1.getTypeID()] = null;
         var0.objectives[var1.getTypeID()] = null;
         boolean var2 = true;

         for(CommandResultStats.Type var6 : CommandResultStats.Type.values()) {
            if (var0.entitiesID[var6.getTypeID()] != null && var0.objectives[var6.getTypeID()] != null) {
               var2 = false;
               break;
            }
         }

         if (var2) {
            var0.entitiesID = STRING_RESULT_TYPES;
            var0.objectives = STRING_RESULT_TYPES;
         }
      }

   }

   public void addAllStats(CommandResultStats var1) {
      for(CommandResultStats.Type var5 : CommandResultStats.Type.values()) {
         setScoreBoardStat(this, var5, var1.entitiesID[var5.getTypeID()], var1.objectives[var5.getTypeID()]);
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
         this.typeID = var3;
         this.typeName = var4;
      }

      public int getTypeID() {
         return this.typeID;
      }

      public String getTypeName() {
         return this.typeName;
      }

      public static String[] getTypeNames() {
         String[] var0 = new String[values().length];
         int var1 = 0;

         for(CommandResultStats.Type var5 : values()) {
            var0[var1++] = var5.getTypeName();
         }

         return var0;
      }

      @Nullable
      public static CommandResultStats.Type getTypeByName(String var0) {
         for(CommandResultStats.Type var4 : values()) {
            if (var4.getTypeName().equals(var0)) {
               return var4;
            }
         }

         return null;
      }
   }
}
