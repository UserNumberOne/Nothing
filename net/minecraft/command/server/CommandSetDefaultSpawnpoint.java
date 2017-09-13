package net.minecraft.command.server;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.src.MinecraftServer;
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
      BlockPos var4;
      if (var3.length == 0) {
         var4 = getCommandSenderAsPlayer(var2).getPosition();
      } else {
         if (var3.length != 3 || var2.getEntityWorld() == null) {
            throw new WrongUsageException("commands.setworldspawn.usage", new Object[0]);
         }

         var4 = parseBlockPos(var2, var3, 0, true);
      }

      var2.getEntityWorld().setSpawnPoint(var4);
      var1.getPlayerList().sendPacketToAllPlayers(new SPacketSpawnPosition(var4));
      notifyCommandListener(var2, this, "commands.setworldspawn.success", new Object[]{var4.getX(), var4.getY(), var4.getZ()});
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length > 0 && var3.length <= 3 ? getTabCompletionCoordinate(var3, 0, var4) : Collections.emptyList();
   }
}
