package net.minecraft.command.server;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandSetDefaultSpawnpoint extends CommandBase {
   public String getName() {
      return "setworldspawn";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.setworldspawn.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      BlockPos blockpos;
      if (args.length == 0) {
         blockpos = getCommandSenderAsPlayer(sender).getPosition();
      } else {
         if (args.length != 3 || sender.getEntityWorld() == null) {
            throw new WrongUsageException("commands.setworldspawn.usage", new Object[0]);
         }

         blockpos = parseBlockPos(sender, args, 0, true);
      }

      sender.getEntityWorld().setSpawnPoint(blockpos);
      server.getPlayerList().sendPacketToAllPlayers(new SPacketSpawnPosition(blockpos));
      notifyCommandListener(sender, this, "commands.setworldspawn.success", new Object[]{blockpos.getX(), blockpos.getY(), blockpos.getZ()});
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return args.length > 0 && args.length <= 3 ? getTabCompletionCoordinate(args, 0, pos) : Collections.emptyList();
   }
}
