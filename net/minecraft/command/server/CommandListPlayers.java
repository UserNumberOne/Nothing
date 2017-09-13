package net.minecraft.command.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandListPlayers extends CommandBase {
   public String getName() {
      return "list";
   }

   public int getRequiredPermissionLevel() {
      return 0;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.players.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      int var4 = var1.getCurrentPlayerCount();
      var2.sendMessage(new TextComponentTranslation("commands.players.list", new Object[]{var4, var1.getMaxPlayers()}));
      var2.sendMessage(new TextComponentString(var1.getPlayerList().getFormattedListOfPlayers(var3.length > 0 && "uuids".equalsIgnoreCase(var3[0]))));
      var2.setCommandStat(CommandResultStats.Type.QUERY_RESULT, var4);
   }
}
