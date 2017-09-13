package net.minecraft.command;

import net.minecraft.server.MinecraftServer;

public class CommandSetPlayerTimeout extends CommandBase {
   public String getName() {
      return "setidletimeout";
   }

   public int getRequiredPermissionLevel() {
      return 3;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.setidletimeout.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (args.length != 1) {
         throw new WrongUsageException("commands.setidletimeout.usage", new Object[0]);
      } else {
         int i = parseInt(args[0], 0);
         server.setPlayerIdleTimeout(i);
         notifyCommandListener(sender, this, "commands.setidletimeout.success", new Object[]{i});
      }
   }
}
