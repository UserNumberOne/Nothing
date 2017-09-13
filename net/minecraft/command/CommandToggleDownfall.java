package net.minecraft.command;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.WorldInfo;

public class CommandToggleDownfall extends CommandBase {
   public String getName() {
      return "toggledownfall";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.downfall.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      this.toggleRainfall(var1);
      notifyCommandListener(var2, this, "commands.downfall.success", new Object[0]);
   }

   protected void toggleRainfall(MinecraftServer var1) {
      WorldInfo var2 = var1.worlds[0].getWorldInfo();
      var2.setRaining(!var2.isRaining());
   }
}
