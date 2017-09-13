package net.minecraft.command.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

public class CommandSaveOff extends CommandBase {
   public String getName() {
      return "save-off";
   }

   public String getUsage(ICommandSender var1) {
      return "commands.save-off.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      boolean var4 = false;

      for(int var5 = 0; var5 < var1.worlds.length; ++var5) {
         if (var1.worlds[var5] != null) {
            WorldServer var6 = var1.worlds[var5];
            if (!var6.disableLevelSaving) {
               var6.disableLevelSaving = true;
               var4 = true;
            }
         }
      }

      if (var4) {
         notifyCommandListener(var2, this, "commands.save.disabled", new Object[0]);
      } else {
         throw new CommandException("commands.save-off.alreadyOff", new Object[0]);
      }
   }
}
