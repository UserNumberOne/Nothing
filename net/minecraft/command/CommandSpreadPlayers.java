package net.minecraft.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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
      if (args.length < 6) {
         throw new WrongUsageException("commands.spreadplayers.usage", new Object[0]);
      } else {
         int i = 0;
         BlockPos blockpos = sender.getPosition();
         double d0 = parseDouble((double)blockpos.getX(), args[i++], true);
         double d1 = parseDouble((double)blockpos.getZ(), args[i++], true);
         double d2 = parseDouble(args[i++], 0.0D);
         double d3 = parseDouble(args[i++], d2 + 1.0D);
         boolean flag = parseBoolean(args[i++]);
         List list = Lists.newArrayList();

         while(i < args.length) {
            String s = args[i++];
            if (EntitySelector.hasArguments(s)) {
               List list1 = EntitySelector.matchEntities(sender, s, Entity.class);
               if (list1.isEmpty()) {
                  throw new EntityNotFoundException();
               }

               list.addAll(list1);
            } else {
               EntityPlayer entityplayer = server.getPlayerList().getPlayerByUsername(s);
               if (entityplayer == null) {
                  throw new PlayerNotFoundException();
               }

               list.add(entityplayer);
            }
         }

         sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, list.size());
         if (list.isEmpty()) {
            throw new EntityNotFoundException();
         } else {
            sender.sendMessage(new TextComponentTranslation("commands.spreadplayers.spreading." + (flag ? "teams" : "players"), new Object[]{list.size(), d3, d0, d1, d2}));
            this.spread(sender, list, new CommandSpreadPlayers.Position(d0, d1), d2, d3, ((Entity)list.get(0)).world, flag);
         }
      }
   }

   private void spread(ICommandSender var1, List var2, CommandSpreadPlayers.Position var3, double var4, double var6, World var8, boolean var9) throws CommandException {
      Random random = new Random();
      double d0 = pos.x - maxRange;
      double d1 = pos.z - maxRange;
      double d2 = pos.x + maxRange;
      double d3 = pos.z + maxRange;
      CommandSpreadPlayers.Position[] acommandspreadplayers$position = this.createInitialPositions(random, respectTeams ? this.getNumberOfTeams(p_110669_2_) : p_110669_2_.size(), d0, d1, d2, d3);
      int i = this.spreadPositions(pos, spreadDistance, worldIn, random, d0, d1, d2, d3, acommandspreadplayers$position, respectTeams);
      double d4 = this.setPlayerPositions(p_110669_2_, worldIn, acommandspreadplayers$position, respectTeams);
      notifyCommandListener(sender, this, "commands.spreadplayers.success." + (respectTeams ? "teams" : "players"), new Object[]{acommandspreadplayers$position.length, pos.x, pos.z});
      if (acommandspreadplayers$position.length > 1) {
         sender.sendMessage(new TextComponentTranslation("commands.spreadplayers.info." + (respectTeams ? "teams" : "players"), new Object[]{String.format("%.2f", d4), i}));
      }

   }

   private int getNumberOfTeams(List var1) {
      Set set = Sets.newHashSet();

      for(Entity entity : p_110667_1_) {
         if (entity instanceof EntityPlayer) {
            set.add(((EntityPlayer)entity).getTeam());
         } else {
            set.add((Team)null);
         }
      }

      return set.size();
   }

   private int spreadPositions(CommandSpreadPlayers.Position var1, double var2, World var4, Random var5, double var6, double var8, double var10, double var12, CommandSpreadPlayers.Position[] var14, boolean var15) throws CommandException {
      boolean flag = true;
      double d0 = 3.4028234663852886E38D;

      int i;
      for(i = 0; i < 10000 && flag; ++i) {
         flag = false;
         d0 = 3.4028234663852886E38D;

         for(int j = 0; j < p_110668_14_.length; ++j) {
            CommandSpreadPlayers.Position commandspreadplayers$position = p_110668_14_[j];
            int k = 0;
            CommandSpreadPlayers.Position commandspreadplayers$position1 = new CommandSpreadPlayers.Position();

            for(int l = 0; l < p_110668_14_.length; ++l) {
               if (j != l) {
                  CommandSpreadPlayers.Position commandspreadplayers$position2 = p_110668_14_[l];
                  double d1 = commandspreadplayers$position.dist(commandspreadplayers$position2);
                  d0 = Math.min(d1, d0);
                  if (d1 < p_110668_2_) {
                     ++k;
                     commandspreadplayers$position1.x += commandspreadplayers$position2.x - commandspreadplayers$position.x;
                     commandspreadplayers$position1.z += commandspreadplayers$position2.z - commandspreadplayers$position.z;
                  }
               }
            }

            if (k > 0) {
               commandspreadplayers$position1.x /= (double)k;
               commandspreadplayers$position1.z /= (double)k;
               double d2 = (double)commandspreadplayers$position1.getLength();
               if (d2 > 0.0D) {
                  commandspreadplayers$position1.normalize();
                  commandspreadplayers$position.moveAway(commandspreadplayers$position1);
               } else {
                  commandspreadplayers$position.randomize(random, minX, minZ, maxX, maxZ);
               }

               flag = true;
            }

            if (commandspreadplayers$position.clamp(minX, minZ, maxX, maxZ)) {
               flag = true;
            }
         }

         if (!flag) {
            for(CommandSpreadPlayers.Position commandspreadplayers$position3 : p_110668_14_) {
               if (!commandspreadplayers$position3.isSafe(worldIn)) {
                  commandspreadplayers$position3.randomize(random, minX, minZ, maxX, maxZ);
                  flag = true;
               }
            }
         }
      }

      if (i >= 10000) {
         throw new CommandException("commands.spreadplayers.failure." + (respectTeams ? "teams" : "players"), new Object[]{p_110668_14_.length, p_110668_1_.x, p_110668_1_.z, String.format("%.2f", d0)});
      } else {
         return i;
      }
   }

   private double setPlayerPositions(List var1, World var2, CommandSpreadPlayers.Position[] var3, boolean var4) {
      double d0 = 0.0D;
      int i = 0;
      Map map = Maps.newHashMap();

      for(int j = 0; j < p_110671_1_.size(); ++j) {
         Entity entity = (Entity)p_110671_1_.get(j);
         CommandSpreadPlayers.Position commandspreadplayers$position;
         if (p_110671_4_) {
            Team team = entity instanceof EntityPlayer ? ((EntityPlayer)entity).getTeam() : null;
            if (!map.containsKey(team)) {
               map.put(team, p_110671_3_[i++]);
            }

            commandspreadplayers$position = (CommandSpreadPlayers.Position)map.get(team);
         } else {
            commandspreadplayers$position = p_110671_3_[i++];
         }

         entity.setPositionAndUpdate((double)((float)MathHelper.floor(commandspreadplayers$position.x) + 0.5F), (double)commandspreadplayers$position.getSpawnY(worldIn), (double)MathHelper.floor(commandspreadplayers$position.z) + 0.5D);
         double d2 = Double.MAX_VALUE;

         for(CommandSpreadPlayers.Position commandspreadplayers$position1 : p_110671_3_) {
            if (commandspreadplayers$position != commandspreadplayers$position1) {
               double d1 = commandspreadplayers$position.dist(commandspreadplayers$position1);
               d2 = Math.min(d1, d2);
            }
         }

         d0 += d2;
      }

      d0 = d0 / (double)p_110671_1_.size();
      return d0;
   }

   private CommandSpreadPlayers.Position[] createInitialPositions(Random var1, int var2, double var3, double var5, double var7, double var9) {
      CommandSpreadPlayers.Position[] acommandspreadplayers$position = new CommandSpreadPlayers.Position[p_110670_2_];

      for(int i = 0; i < acommandspreadplayers$position.length; ++i) {
         CommandSpreadPlayers.Position commandspreadplayers$position = new CommandSpreadPlayers.Position();
         commandspreadplayers$position.randomize(p_110670_1_, p_110670_3_, p_110670_5_, p_110670_7_, p_110670_9_);
         acommandspreadplayers$position[i] = commandspreadplayers$position;
      }

      return acommandspreadplayers$position;
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return args.length >= 1 && args.length <= 2 ? getTabCompletionCoordinateXZ(args, 0, pos) : Collections.emptyList();
   }

   static class Position {
      double x;
      double z;

      Position() {
      }

      Position(double var1, double var3) {
         this.x = xIn;
         this.z = zIn;
      }

      double dist(CommandSpreadPlayers.Position var1) {
         double d0 = this.x - pos.x;
         double d1 = this.z - pos.z;
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

      public void moveAway(CommandSpreadPlayers.Position var1) {
         this.x -= pos.x;
         this.z -= pos.z;
      }

      public boolean clamp(double var1, double var3, double var5, double var7) {
         boolean flag = false;
         if (this.x < p_111093_1_) {
            this.x = p_111093_1_;
            flag = true;
         } else if (this.x > p_111093_5_) {
            this.x = p_111093_5_;
            flag = true;
         }

         if (this.z < p_111093_3_) {
            this.z = p_111093_3_;
            flag = true;
         } else if (this.z > p_111093_7_) {
            this.z = p_111093_7_;
            flag = true;
         }

         return flag;
      }

      public int getSpawnY(World var1) {
         BlockPos blockpos = new BlockPos(this.x, 256.0D, this.z);

         while(blockpos.getY() > 0) {
            blockpos = blockpos.down();
            if (worldIn.getBlockState(blockpos).getMaterial() != Material.AIR) {
               return blockpos.getY() + 1;
            }
         }

         return 257;
      }

      public boolean isSafe(World var1) {
         BlockPos blockpos = new BlockPos(this.x, 256.0D, this.z);

         while(blockpos.getY() > 0) {
            blockpos = blockpos.down();
            Material material = worldIn.getBlockState(blockpos).getMaterial();
            if (material != Material.AIR) {
               return !material.isLiquid() && material != Material.FIRE;
            }
         }

         return false;
      }

      public void randomize(Random var1, double var2, double var4, double var6, double var8) {
         this.x = MathHelper.nextDouble(rand, p_111097_2_, p_111097_6_);
         this.z = MathHelper.nextDouble(rand, p_111097_4_, p_111097_8_);
      }
   }
}
