package net.minecraft.command.server;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandPardonIp extends CommandBase {
   public String getName() {
      return "pardon-ip";
   }

   public int getRequiredPermissionLevel() {
      return 3;
   }

   public boolean canUse(MinecraftServer var1, ICommandSender var2) {
      return var1.getPlayerList().getBannedIPs().isLanServer() && super.canUse(var1, var2);
   }

   public String getUsage(ICommandSender var1) {
      return "commands.unbanip.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length == 1 && var3[0].length() > 1) {
         Matcher var4 = CommandBanIp.IP_PATTERN.matcher(var3[0]);
         if (var4.matches()) {
            var1.getPlayerList().getBannedIPs().removeEntry(var3[0]);
            notifyCommandListener(var2, this, "commands.unbanip.success", new Object[]{var3[0]});
         } else {
            throw new SyntaxErrorException("commands.unbanip.invalid", new Object[0]);
         }
      } else {
         throw new WrongUsageException("commands.unbanip.usage", new Object[0]);
      }
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length == 1 ? getListOfStringsMatchingLastWord(var3, var1.getPlayerList().getBannedIPs().getKeys()) : Collections.emptyList();
   }
}
