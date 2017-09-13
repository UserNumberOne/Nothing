package net.minecraft.command.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.src.MinecraftServer;

public class CommandStop extends CommandBase {
   public String getName() {
      return "stop";
   }

   public String getUsage(ICommandSender var1) {
      return "commands.stop.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var1.worldServer != null) {
         notifyCommandListener(var2, this, "commands.stop.start", new Object[0]);
      }

      var1.safeShutdown();
   }
}
