package net.minecraft.command.server;

import com.mojang.authlib.GameProfile;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandWhitelist extends CommandBase {
   public String getName() {
      return "whitelist";
   }

   public int getRequiredPermissionLevel() {
      return 3;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.whitelist.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 1) {
         throw new WrongUsageException("commands.whitelist.usage", new Object[0]);
      } else {
         if ("on".equals(var3[0])) {
            var1.getPlayerList().setWhiteListEnabled(true);
            notifyCommandListener(var2, this, "commands.whitelist.enabled", new Object[0]);
         } else if ("off".equals(var3[0])) {
            var1.getPlayerList().setWhiteListEnabled(false);
            notifyCommandListener(var2, this, "commands.whitelist.disabled", new Object[0]);
         } else if ("list".equals(var3[0])) {
            var2.sendMessage(new TextComponentTranslation("commands.whitelist.list", new Object[]{var1.getPlayerList().getWhitelistedPlayerNames().length, var1.getPlayerList().getAvailablePlayerDat().length}));
            String[] var4 = var1.getPlayerList().getWhitelistedPlayerNames();
            var2.sendMessage(new TextComponentString(joinNiceString(var4)));
         } else if ("add".equals(var3[0])) {
            if (var3.length < 2) {
               throw new WrongUsageException("commands.whitelist.add.usage", new Object[0]);
            }

            GameProfile var5 = var1.getUserCache().getGameProfileForUsername(var3[1]);
            if (var5 == null) {
               throw new CommandException("commands.whitelist.add.failed", new Object[]{var3[1]});
            }

            var1.getPlayerList().addWhitelistedPlayer(var5);
            notifyCommandListener(var2, this, "commands.whitelist.add.success", new Object[]{var3[1]});
         } else if ("remove".equals(var3[0])) {
            if (var3.length < 2) {
               throw new WrongUsageException("commands.whitelist.remove.usage", new Object[0]);
            }

            GameProfile var6 = var1.getPlayerList().getWhitelistedPlayers().getByName(var3[1]);
            if (var6 == null) {
               throw new CommandException("commands.whitelist.remove.failed", new Object[]{var3[1]});
            }

            var1.getPlayerList().removePlayerFromWhitelist(var6);
            notifyCommandListener(var2, this, "commands.whitelist.remove.success", new Object[]{var3[1]});
         } else if ("reload".equals(var3[0])) {
            var1.getPlayerList().reloadWhitelist();
            notifyCommandListener(var2, this, "commands.whitelist.reloaded", new Object[0]);
         }

      }
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (var3.length == 1) {
         return getListOfStringsMatchingLastWord(var3, new String[]{"on", "off", "list", "add", "remove", "reload"});
      } else {
         if (var3.length == 2) {
            if ("remove".equals(var3[0])) {
               return getListOfStringsMatchingLastWord(var3, var1.getPlayerList().getWhitelistedPlayerNames());
            }

            if ("add".equals(var3[0])) {
               return getListOfStringsMatchingLastWord(var3, var1.getUserCache().getUsernames());
            }
         }

         return Collections.emptyList();
      }
   }
}
