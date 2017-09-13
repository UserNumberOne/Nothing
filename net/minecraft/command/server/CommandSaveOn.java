package net.minecraft.command.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

public class CommandSaveOn extends CommandBase {
   public String getName() {
      return "save-on";
   }

   public String getUsage(ICommandSender var1) {
      return "commands.save-on.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      boolean var4 = false;

      for(int var5 = 0; var5 < var1.worlds.length; ++var5) {
         if (var1.worlds[var5] != null) {
            WorldServer var6 = var1.worlds[var5];
            if (var6.disableLevelSaving) {
               var6.disableLevelSaving = false;
               var4 = true;
            }
         }
      }

      if (var4) {
         notifyCommandListener(var2, this, "commands.save.enabled", new Object[0]);
      } else {
         throw new CommandException("commands.save-on.alreadyOn", new Object[0]);
      }
   }
}
