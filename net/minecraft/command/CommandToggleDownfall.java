package net.minecraft.command;

import net.minecraft.src.MinecraftServer;
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
      this.a(var1);
      notifyCommandListener(var2, this, "commands.downfall.success", new Object[0]);
   }

   protected void a(MinecraftServer var1) {
      WorldInfo var2 = var1.worldServer[0].getWorldInfo();
      var2.setRaining(!var2.isRaining());
   }
}
