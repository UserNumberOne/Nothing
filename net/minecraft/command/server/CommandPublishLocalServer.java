package net.minecraft.command.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameType;

public class CommandPublishLocalServer extends CommandBase {
   public String getName() {
      return "publish";
   }

   public String getUsage(ICommandSender var1) {
      return "commands.publish.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      String var4 = var1.shareToLAN(GameType.SURVIVAL, false);
      if (var4 != null) {
         notifyCommandListener(var2, this, "commands.publish.started", new Object[]{var4});
      } else {
         notifyCommandListener(var2, this, "commands.publish.failed", new Object[0]);
      }

   }
}
