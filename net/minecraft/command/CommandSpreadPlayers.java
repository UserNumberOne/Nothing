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
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Team;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkProviderServer;

public class CommandSpreadPlayers extends CommandBase {
   public String getName() {
      return "spreadplayers";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender icommandlistener) {
      return "commands.spreadplayers.usage";
   }

   public void execute(MinecraftServer minecraftserver, ICommandSender icommandlistener, String[] astring) throws CommandException {
      if (astring.length < 6) {
         throw new WrongUsageException("commands.spreadplayers.usage", new Object[0]);
      } else {
         byte b0 = 0;
         BlockPos blockposition = icommandlistener.getPosition();
         double d0 = (double)blockposition.getX();
         int i = b0 + 1;
         double d1 = parseDouble(d0, astring[b0], true);
         double d2 = parseDouble((double)blockposition.getZ(), astring[i++], true);
         double d3 = parseDouble(astring[i++], 0.0D);
         double d4 = parseDouble(astring[i++], d3 + 1.0D);
         boolean flag = parseBoolean(astring[i++]);
         ArrayList arraylist = Lists.newArrayList();

         while(i < astring.length) {
            String s = astring[i++];
            if (EntitySelector.hasArguments(s)) {
               List list = EntitySelector.matchEntities(icommandlistener, s, Entity.class);
               if (list.isEmpty()) {
                  throw new EntityNotFoundException();
               }

               arraylist.addAll(list);
            } else {
               EntityPlayerMP entityplayer = minecraftserver.getPlayerList().getPlayerByUsername(s);
               if (entityplayer == null) {
                  throw new PlayerNotFoundException();
               }

               arraylist.add(entityplayer);
            }
         }

         icommandlistener.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, arraylist.size());
         if (arraylist.isEmpty()) {
            throw new EntityNotFoundException();
         } else {
            icommandlistener.sendMessage(new TextComponentTranslation("commands.spreadplayers.spreading." + (flag ? "teams" : "players"), new Object[]{arraylist.size(), d4, d1, d2, d3}));
            this.spread(icommandlistener, arraylist, new CommandSpreadPlayers.Position(d1, d2), d3, d4, ((Entity)arraylist.get(0)).world, flag);
         }
      }
   }

   private void spread(ICommandSender icommandlistener, List list, CommandSpreadPlayers.Position commandspreadplayers_location2d, double d0, double d1, World world, boolean flag) throws CommandException {
      Random random = new Random();
      double d2 = commandspreadplayers_location2d.x - d1;
      double d3 = commandspreadplayers_location2d.z - d1;
      double d4 = commandspreadplayers_location2d.x + d1;
      double d5 = commandspreadplayers_location2d.z + d1;
      CommandSpreadPlayers.Position[] acommandspreadplayers_location2d = this.createInitialPositions(random, flag ? this.getNumberOfTeams(list) : list.size(), d2, d3, d4, d5);
      int i = this.spreadPositions(commandspreadplayers_location2d, d0, world, random, d2, d3, d4, d5, acommandspreadplayers_location2d, flag);
      double d6 = this.setPlayerPositions(list, world, acommandspreadplayers_location2d, flag);
      notifyCommandListener(icommandlistener, this, "commands.spreadplayers.success." + (flag ? "teams" : "players"), new Object[]{acommandspreadplayers_location2d.length, commandspreadplayers_location2d.x, commandspreadplayers_location2d.z});
      if (acommandspreadplayers_location2d.length > 1) {
         icommandlistener.sendMessage(new TextComponentTranslation("commands.spreadplayers.info." + (flag ? "teams" : "players"), new Object[]{String.format("%.2f", d6), i}));
      }

   }

   private int getNumberOfTeams(List list) {
      HashSet hashset = Sets.newHashSet();

      for(Entity entity : list) {
         if (entity instanceof EntityPlayer) {
            hashset.add(((EntityPlayer)entity).getTeam());
         } else {
            hashset.add((Object)null);
         }
      }

      return hashset.size();
   }

   private int spreadPositions(CommandSpreadPlayers.Position commandspreadplayers_location2d, double d0, World world, Random random, double d1, double d2, double d3, double d4, CommandSpreadPlayers.Position[] acommandspreadplayers_location2d, boolean flag) throws CommandException {
      boolean flag1 = true;
      double d5 = 3.4028234663852886E38D;

      int i;
      for(i = 0; i < 10000 && flag1; ++i) {
         flag1 = false;
         d5 = 3.4028234663852886E38D;

         for(int k = 0; k < acommandspreadplayers_location2d.length; ++k) {
            CommandSpreadPlayers.Position commandspreadplayers_location2d2 = acommandspreadplayers_location2d[k];
            int j = 0;
            CommandSpreadPlayers.Position commandspreadplayers_location2d1 = new CommandSpreadPlayers.Position();

            for(int l = 0; l < acommandspreadplayers_location2d.length; ++l) {
               if (k != l) {
                  CommandSpreadPlayers.Position commandspreadplayers_location2d3 = acommandspreadplayers_location2d[l];
                  double d6 = commandspreadplayers_location2d2.dist(commandspreadplayers_location2d3);
                  d5 = Math.min(d6, d5);
                  if (d6 < d0) {
                     ++j;
                     commandspreadplayers_location2d1.x += commandspreadplayers_location2d3.x - commandspreadplayers_location2d2.x;
                     commandspreadplayers_location2d1.z += commandspreadplayers_location2d3.z - commandspreadplayers_location2d2.z;
                  }
               }
            }

            if (j > 0) {
               commandspreadplayers_location2d1.x /= (double)j;
               commandspreadplayers_location2d1.z /= (double)j;
               double d7 = (double)commandspreadplayers_location2d1.getLength();
               if (d7 > 0.0D) {
                  commandspreadplayers_location2d1.normalize();
                  commandspreadplayers_location2d2.moveAway(commandspreadplayers_location2d1);
               } else {
                  commandspreadplayers_location2d2.randomize(random, d1, d2, d3, d4);
               }

               flag1 = true;
            }

            if (commandspreadplayers_location2d2.clamp(d1, d2, d3, d4)) {
               flag1 = true;
            }
         }

         if (!flag1) {
            for(CommandSpreadPlayers.Position commandspreadplayers_location2d1 : acommandspreadplayers_location2d) {
               if (!commandspreadplayers_location2d1.isSafe(world)) {
                  commandspreadplayers_location2d1.randomize(random, d1, d2, d3, d4);
                  flag1 = true;
               }
            }
         }
      }

      if (i >= 10000) {
         throw new CommandException("commands.spreadplayers.failure." + (flag ? "teams" : "players"), new Object[]{acommandspreadplayers_location2d.length, commandspreadplayers_location2d.x, commandspreadplayers_location2d.z, String.format("%.2f", d5)});
      } else {
         return i;
      }
   }

   private double setPlayerPositions(List list, World world, CommandSpreadPlayers.Position[] acommandspreadplayers_location2d, boolean flag) {
      double d0 = 0.0D;
      int i = 0;
      HashMap hashmap = Maps.newHashMap();

      for(int j = 0; j < list.size(); ++j) {
         Entity entity = (Entity)list.get(j);
         CommandSpreadPlayers.Position commandspreadplayers_location2d;
         if (flag) {
            Team scoreboardteambase = entity instanceof EntityPlayer ? ((EntityPlayer)entity).getTeam() : null;
            if (!hashmap.containsKey(scoreboardteambase)) {
               hashmap.put(scoreboardteambase, acommandspreadplayers_location2d[i++]);
            }

            commandspreadplayers_location2d = (CommandSpreadPlayers.Position)hashmap.get(scoreboardteambase);
         } else {
            commandspreadplayers_location2d = acommandspreadplayers_location2d[i++];
         }

         entity.setPositionAndUpdate((double)((float)MathHelper.floor(commandspreadplayers_location2d.x) + 0.5F), (double)commandspreadplayers_location2d.getSpawnY(world), (double)MathHelper.floor(commandspreadplayers_location2d.z) + 0.5D);
         double d1 = Double.MAX_VALUE;

         for(CommandSpreadPlayers.Position commandspreadplayers_location2d1 : acommandspreadplayers_location2d) {
            if (commandspreadplayers_location2d != commandspreadplayers_location2d1) {
               double d2 = commandspreadplayers_location2d.dist(commandspreadplayers_location2d1);
               d1 = Math.min(d2, d1);
            }
         }

         d0 += d1;
      }

      d0 = d0 / (double)list.size();
      return d0;
   }

   private CommandSpreadPlayers.Position[] createInitialPositions(Random random, int i, double d0, double d1, double d2, double d3) {
      CommandSpreadPlayers.Position[] acommandspreadplayers_location2d = new CommandSpreadPlayers.Position[i];

      for(int j = 0; j < acommandspreadplayers_location2d.length; ++j) {
         CommandSpreadPlayers.Position commandspreadplayers_location2d = new CommandSpreadPlayers.Position();
         commandspreadplayers_location2d.randomize(random, d0, d1, d2, d3);
         acommandspreadplayers_location2d[j] = commandspreadplayers_location2d;
      }

      return acommandspreadplayers_location2d;
   }

   public List tabComplete(MinecraftServer minecraftserver, ICommandSender icommandlistener, String[] astring, @Nullable BlockPos blockposition) {
      return astring.length >= 1 && astring.length <= 2 ? getTabCompletionCoordinateXZ(astring, 0, blockposition) : Collections.emptyList();
   }

   public int compareTo(ICommand o) {
      return this.compareTo(o);
   }

   static class Position {
      double x;
      double z;

      Position() {
      }

      Position(double d0, double d1) {
         this.x = d0;
         this.z = d1;
      }

      double dist(CommandSpreadPlayers.Position commandspreadplayers_location2d) {
         double d0 = this.x - commandspreadplayers_location2d.x;
         double d1 = this.z - commandspreadplayers_location2d.z;
         return Math.sqrt(d0 * d0 + d1 * d1);
      }

      void normalize() {
         double d0 = (double)this.getLength();
         this.x /= d0;
         this.z /= d0;
      }

      float getLength() {
         return MathHelper.sqrt(this.x * this.x + this.z * this.z);
      }

      public void moveAway(CommandSpreadPlayers.Position commandspreadplayers_location2d) {
         this.x -= commandspreadplayers_location2d.x;
         this.z -= commandspreadplayers_location2d.z;
      }

      public boolean clamp(double d0, double d1, double d2, double d3) {
         boolean flag = false;
         if (this.x < d0) {
            this.x = d0;
            flag = true;
         } else if (this.x > d2) {
            this.x = d2;
            flag = true;
         }

         if (this.z < d1) {
            this.z = d1;
            flag = true;
         } else if (this.z > d3) {
            this.z = d3;
            flag = true;
         }

         return flag;
      }

      public int getSpawnY(World world) {
         BlockPos blockposition = new BlockPos(this.x, 256.0D, this.z);

         while(blockposition.getY() > 0) {
            blockposition = blockposition.down();
            if (getType(world, blockposition).getMaterial() != Material.AIR) {
               return blockposition.getY() + 1;
            }
         }

         return 257;
      }

      public boolean isSafe(World world) {
         BlockPos blockposition = new BlockPos(this.x, 256.0D, this.z);

         while(blockposition.getY() > 0) {
            blockposition = blockposition.down();
            Material material = getType(world, blockposition).getMaterial();
            if (material != Material.AIR) {
               if (!material.isLiquid() && material != Material.FIRE) {
                  return true;
               }

               return false;
            }
         }

         return false;
      }

      public void randomize(Random random, double d0, double d1, double d2, double d3) {
         this.x = MathHelper.nextDouble(random, d0, d2);
         this.z = MathHelper.nextDouble(random, d1, d3);
      }

      private static IBlockState getType(World world, BlockPos position) {
         ((ChunkProviderServer)world.chunkProvider).provideChunk(position.getX() >> 4, position.getZ() >> 4);
         return world.getBlockState(position);
      }
   }
}
