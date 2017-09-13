package net.minecraft.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class CommandSpreadPlayers extends CommandBase {
   public String getName() {
      return "spreadplayers";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.spreadplayers.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 6) {
         throw new WrongUsageException("commands.spreadplayers.usage", new Object[0]);
      } else {
         int var4 = 0;
         BlockPos var5 = var2.getPosition();
         double var6 = parseDouble((double)var5.getX(), var3[var4++], true);
         double var8 = parseDouble((double)var5.getZ(), var3[var4++], true);
         double var10 = parseDouble(var3[var4++], 0.0D);
         double var12 = parseDouble(var3[var4++], var10 + 1.0D);
         boolean var14 = parseBoolean(var3[var4++]);
         ArrayList var15 = Lists.newArrayList();

         while(var4 < var3.length) {
            String var16 = var3[var4++];
            if (EntitySelector.hasArguments(var16)) {
               List var17 = EntitySelector.matchEntities(var2, var16, Entity.class);
               if (var17.isEmpty()) {
                  throw new EntityNotFoundException();
               }

               var15.addAll(var17);
            } else {
               EntityPlayerMP var23 = var1.getPlayerList().getPlayerByUsername(var16);
               if (var23 == null) {
                  throw new PlayerNotFoundException();
               }

               var15.add(var23);
            }
         }

         var2.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, var15.size());
         if (var15.isEmpty()) {
            throw new EntityNotFoundException();
         } else {
            var2.sendMessage(new TextComponentTranslation("commands.spreadplayers.spreading." + (var14 ? "teams" : "players"), new Object[]{var15.size(), var12, var6, var8, var10}));
            this.spread(var2, var15, new CommandSpreadPlayers.Position(var6, var8), var10, var12, ((Entity)var15.get(0)).world, var14);
         }
      }
   }

   private void spread(ICommandSender var1, List var2, CommandSpreadPlayers.Position var3, double var4, double var6, World var8, boolean var9) throws CommandException {
      Random var10 = new Random();
      double var11 = var3.x - var6;
      double var13 = var3.z - var6;
      double var15 = var3.x + var6;
      double var17 = var3.z + var6;
      CommandSpreadPlayers.Position[] var19 = this.createInitialPositions(var10, var9 ? this.getNumberOfTeams(var2) : var2.size(), var11, var13, var15, var17);
      int var20 = this.spreadPositions(var3, var4, var8, var10, var11, var13, var15, var17, var19, var9);
      double var21 = this.setPlayerPositions(var2, var8, var19, var9);
      notifyCommandListener(var1, this, "commands.spreadplayers.success." + (var9 ? "teams" : "players"), new Object[]{var19.length, var3.x, var3.z});
      if (var19.length > 1) {
         var1.sendMessage(new TextComponentTranslation("commands.spreadplayers.info." + (var9 ? "teams" : "players"), new Object[]{String.format("%.2f", var21), var20}));
      }

   }

   private int getNumberOfTeams(List var1) {
      HashSet var2 = Sets.newHashSet();

      for(Entity var4 : var1) {
         if (var4 instanceof EntityPlayer) {
            var2.add(((EntityPlayer)var4).getTeam());
         } else {
            var2.add((Team)null);
         }
      }

      return var2.size();
   }

   private int spreadPositions(CommandSpreadPlayers.Position var1, double var2, World var4, Random var5, double var6, double var8, double var10, double var12, CommandSpreadPlayers.Position[] var14, boolean var15) throws CommandException {
      boolean var16 = true;
      double var17 = 3.4028234663852886E38D;

      int var19;
      for(var19 = 0; var19 < 10000 && var16; ++var19) {
         var16 = false;
         var17 = 3.4028234663852886E38D;

         for(int var20 = 0; var20 < var14.length; ++var20) {
            CommandSpreadPlayers.Position var21 = var14[var20];
            int var22 = 0;
            CommandSpreadPlayers.Position var23 = new CommandSpreadPlayers.Position();

            for(int var24 = 0; var24 < var14.length; ++var24) {
               if (var20 != var24) {
                  CommandSpreadPlayers.Position var25 = var14[var24];
                  double var26 = var21.dist(var25);
                  var17 = Math.min(var26, var17);
                  if (var26 < var2) {
                     ++var22;
                     var23.x += var25.x - var21.x;
                     var23.z += var25.z - var21.z;
                  }
               }
            }

            if (var22 > 0) {
               var23.x /= (double)var22;
               var23.z /= (double)var22;
               double var32 = (double)var23.getLength();
               if (var32 > 0.0D) {
                  var23.normalize();
                  var21.moveAway(var23);
               } else {
                  var21.randomize(var5, var6, var8, var10, var12);
               }

               var16 = true;
            }

            if (var21.clamp(var6, var8, var10, var12)) {
               var16 = true;
            }
         }

         if (!var16) {
            for(CommandSpreadPlayers.Position var31 : var14) {
               if (!var31.isSafe(var4)) {
                  var31.randomize(var5, var6, var8, var10, var12);
                  var16 = true;
               }
            }
         }
      }

      if (var19 >= 10000) {
         throw new CommandException("commands.spreadplayers.failure." + (var15 ? "teams" : "players"), new Object[]{var14.length, var1.x, var1.z, String.format("%.2f", var17)});
      } else {
         return var19;
      }
   }

   private double setPlayerPositions(List var1, World var2, CommandSpreadPlayers.Position[] var3, boolean var4) {
      double var5 = 0.0D;
      int var7 = 0;
      HashMap var8 = Maps.newHashMap();

      for(int var9 = 0; var9 < var1.size(); ++var9) {
         Entity var10 = (Entity)var1.get(var9);
         CommandSpreadPlayers.Position var11;
         if (var4) {
            Team var12 = var10 instanceof EntityPlayer ? ((EntityPlayer)var10).getTeam() : null;
            if (!var8.containsKey(var12)) {
               var8.put(var12, var3[var7++]);
            }

            var11 = (CommandSpreadPlayers.Position)var8.get(var12);
         } else {
            var11 = var3[var7++];
         }

         var10.setPositionAndUpdate((double)((float)MathHelper.floor(var11.x) + 0.5F), (double)var11.getSpawnY(var2), (double)MathHelper.floor(var11.z) + 0.5D);
         double var21 = Double.MAX_VALUE;

         for(CommandSpreadPlayers.Position var17 : var3) {
            if (var11 != var17) {
               double var18 = var11.dist(var17);
               var21 = Math.min(var18, var21);
            }
         }

         var5 += var21;
      }

      var5 = var5 / (double)var1.size();
      return var5;
   }

   private CommandSpreadPlayers.Position[] createInitialPositions(Random var1, int var2, double var3, double var5, double var7, double var9) {
      CommandSpreadPlayers.Position[] var11 = new CommandSpreadPlayers.Position[var2];

      for(int var12 = 0; var12 < var11.length; ++var12) {
         CommandSpreadPlayers.Position var13 = new CommandSpreadPlayers.Position();
         var13.randomize(var1, var3, var5, var7, var9);
         var11[var12] = var13;
      }

      return var11;
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length >= 1 && var3.length <= 2 ? getTabCompletionCoordinateXZ(var3, 0, var4) : Collections.emptyList();
   }

   static class Position {
      double x;
      double z;

      Position() {
      }

      Position(double var1, double var3) {
         this.x = var1;
         this.z = var3;
      }

      double dist(CommandSpreadPlayers.Position var1) {
         double var2 = this.x - var1.x;
         double var4 = this.z - var1.z;
         return Math.sqrt(var2 * var2 + var4 * var4);
      }

      void normalize() {
         double var1 = (double)this.getLength();
         this.x /= var1;
         this.z /= var1;
      }

      float getLength() {
         return MathHelper.sqrt(this.x * this.x + this.z * this.z);
      }

      public void moveAway(CommandSpreadPlayers.Position var1) {
         this.x -= var1.x;
         this.z -= var1.z;
      }

      public boolean clamp(double var1, double var3, double var5, double var7) {
         boolean var9 = false;
         if (this.x < var1) {
            this.x = var1;
            var9 = true;
         } else if (this.x > var5) {
            this.x = var5;
            var9 = true;
         }

         if (this.z < var3) {
            this.z = var3;
            var9 = true;
         } else if (this.z > var7) {
            this.z = var7;
            var9 = true;
         }

         return var9;
      }

      public int getSpawnY(World var1) {
         BlockPos var2 = new BlockPos(this.x, 256.0D, this.z);

         while(var2.getY() > 0) {
            var2 = var2.down();
            if (var1.getBlockState(var2).getMaterial() != Material.AIR) {
               return var2.getY() + 1;
            }
         }

         return 257;
      }

      public boolean isSafe(World var1) {
         BlockPos var2 = new BlockPos(this.x, 256.0D, this.z);

         while(var2.getY() > 0) {
            var2 = var2.down();
            Material var3 = var1.getBlockState(var2).getMaterial();
            if (var3 != Material.AIR) {
               return !var3.isLiquid() && var3 != Material.FIRE;
            }
         }

         return false;
      }

      public void randomize(Random var1, double var2, double var4, double var6, double var8) {
         this.x = MathHelper.nextDouble(var1, var2, var6);
         this.z = MathHelper.nextDouble(var1, var4, var8);
      }
   }
}
