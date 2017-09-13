package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.src.MinecraftServer;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_10_R1.command.ProxiedNativeCommandSender;
import org.bukkit.craftbukkit.v1_10_R1.command.VanillaCommandWrapper;

public class CommandExecuteAt extends CommandBase {
   public String getName() {
      return "execute";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender icommandlistener) {
      return "commands.execute.usage";
   }

   public void execute(final MinecraftServer minecraftserver, final ICommandSender icommandlistener, String[] astring) throws CommandException {
      if (astring.length < 5) {
         throw new WrongUsageException("commands.execute.usage", new Object[0]);
      } else {
         final Entity entity = a(minecraftserver, icommandlistener, astring[0], Entity.class);
         final double d0 = parseDouble(entity.posX, astring[1], false);
         final double d1 = parseDouble(entity.posY, astring[2], false);
         final double d2 = parseDouble(entity.posZ, astring[3], false);
         final BlockPos blockposition = new BlockPos(d0, d1, d2);
         byte b0 = 4;
         if ("detect".equals(astring[4]) && astring.length > 10) {
            World world = entity.getEntityWorld();
            double d3 = parseDouble(d0, astring[5], false);
            double d4 = parseDouble(d1, astring[6], false);
            double d5 = parseDouble(d2, astring[7], false);
            Block block = getBlockByText(icommandlistener, astring[8]);
            int i = parseInt(astring[9], -1, 15);
            BlockPos blockposition1 = new BlockPos(d3, d4, d5);
            if (!world.isBlockLoaded(blockposition1)) {
               throw new CommandException("commands.execute.failed", new Object[]{"detect", entity.getName()});
            }

            IBlockState iblockdata = world.getBlockState(blockposition1);
            if (iblockdata.getBlock() != block || i >= 0 && iblockdata.getBlock().getMetaFromState(iblockdata) != i) {
               throw new CommandException("commands.execute.failed", new Object[]{"detect", entity.getName()});
            }

            b0 = 10;
         }

         String s = buildString(astring, b0);
         class 1ProxyListener implements ICommandSender {
            private final ICommandSender base = icommandlistener;

            public String getName() {
               return entity.getName();
            }

            public ITextComponent getDisplayName() {
               return entity.getDisplayName();
            }

            public void sendMessage(ITextComponent ichatbasecomponent) {
               icommandlistener.sendMessage(ichatbasecomponent);
            }

            public boolean canUseCommand(int i, String s) {
               return icommandlistener.canUseCommand(i, s);
            }

            public BlockPos getPosition() {
               return blockposition;
            }

            public Vec3d getPositionVector() {
               return new Vec3d(d0, d1, d2);
            }

            public World getEntityWorld() {
               return entity.world;
            }

            public Entity getCommandSenderEntity() {
               return entity;
            }

            public boolean sendCommandFeedback() {
               return minecraftserver == null || minecraftserver.worldServer[0].getGameRules().getBoolean("commandBlockOutput");
            }

            public void setCommandStat(CommandResultStats.Type commandobjectiveexecutor_enumcommandresult, int i) {
               entity.setCommandStat(commandobjectiveexecutor_enumcommandresult, i);
            }

            public MinecraftServer h() {
               return entity.h();
            }
         }

         ICommandSender icommandlistener1 = new 1ProxyListener();
         minecraftserver.getCommandHandler();

         try {
            CommandSender sender = null;
            ICommandSender listener = icommandlistener;

            while(sender == null) {
               if (listener instanceof DedicatedServer) {
                  sender = minecraftserver.console;
               } else if (listener instanceof RConConsoleSource) {
                  sender = minecraftserver.remoteConsole;
               } else if (listener instanceof CommandBlockBaseLogic) {
                  sender = ((CommandBlockBaseLogic)listener).sender;
               } else if (listener instanceof 1ProxyListener) {
                  listener = ((1ProxyListener)listener).base;
               } else if (VanillaCommandWrapper.lastSender != null) {
                  sender = VanillaCommandWrapper.lastSender;
               } else {
                  if (listener.getCommandSenderEntity() == null) {
                     throw new CommandException("Unhandled executor " + icommandlistener.getClass().getSimpleName(), new Object[0]);
                  }

                  sender = listener.getCommandSenderEntity().getBukkitEntity();
               }
            }

            int j = CommandBlockBaseLogic.executeCommand(icommandlistener1, new ProxiedNativeCommandSender(icommandlistener1, sender, entity.getBukkitEntity()), s);
            if (j < 1) {
               throw new CommandException("commands.execute.allInvocationsFailed", new Object[]{s});
            }
         } catch (Throwable var28) {
            if (var28 instanceof CommandException) {
               throw (CommandException)var28;
            } else {
               throw new CommandException("commands.execute.failed", new Object[]{s, entity.getName()});
            }
         }
      }
   }

   public List tabComplete(MinecraftServer minecraftserver, ICommandSender icommandlistener, String[] astring, @Nullable BlockPos blockposition) {
      return astring.length == 1 ? getListOfStringsMatchingLastWord(astring, minecraftserver.getPlayers()) : (astring.length > 1 && astring.length <= 4 ? getTabCompletionCoordinate(astring, 1, blockposition) : (astring.length > 5 && astring.length <= 8 && "detect".equals(astring[4]) ? getTabCompletionCoordinate(astring, 5, blockposition) : (astring.length == 9 && "detect".equals(astring[4]) ? getListOfStringsMatchingLastWord(astring, Block.REGISTRY.getKeys()) : Collections.emptyList())));
   }

   public boolean isUsernameIndex(String[] astring, int i) {
      return i == 0;
   }

   public int compareTo(ICommand o) {
      return this.compareTo(o);
   }
}
