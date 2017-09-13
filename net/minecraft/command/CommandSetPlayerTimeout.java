package net.minecraft.command;

import net.minecraft.src.MinecraftServer;

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
      if (var3.length != 1) {
         throw new WrongUsageException("commands.setidletimeout.usage", new Object[0]);
      } else {
         int var4 = parseInt(var3[0], 0);
         var1.setIdleTimeout(var4);
         notifyCommandListener(var2, this, "commands.setidletimeout.success", new Object[]{var4});
      }
   }
}
