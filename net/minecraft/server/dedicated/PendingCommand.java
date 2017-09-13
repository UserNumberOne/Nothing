package net.minecraft.server.dedicated;

import net.minecraft.command.ICommandSender;

public class PendingCommand {
   public final String command;
   public final ICommandSender sender;

   public PendingCommand(String var1, ICommandSender var2) {
      this.command = var1;
      this.sender = var2;
   }
}
