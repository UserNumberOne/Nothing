package net.minecraft.command.server;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandListBans extends CommandBase {
   public String getName() {
      return "banlist";
   }

   public int getRequiredPermissionLevel() {
      return 3;
   }

   public boolean checkPermission(MinecraftServer var1, ICommandSender var2) {
      return (var1.getPlayerList().getBannedIPs().isLanServer() || var1.getPlayerList().getBannedPlayers().isLanServer()) && super.checkPermission(var1, var2);
   }

   public String getUsage(ICommandSender var1) {
      return "commands.banlist.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length >= 1 && "ips".equalsIgnoreCase(var3[0])) {
         var2.sendMessage(new TextComponentTranslation("commands.banlist.ips", new Object[]{var1.getPlayerList().getBannedIPs().getKeys().length}));
         var2.sendMessage(new TextComponentString(joinNiceString(var1.getPlayerList().getBannedIPs().getKeys())));
      } else {
         var2.sendMessage(new TextComponentTranslation("commands.banlist.players", new Object[]{var1.getPlayerList().getBannedPlayers().getKeys().length}));
         var2.sendMessage(new TextComponentString(joinNiceString(var1.getPlayerList().getBannedPlayers().getKeys())));
      }

   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length == 1 ? getListOfStringsMatchingLastWord(var3, new String[]{"players", "ips"}) : Collections.emptyList();
   }
}
