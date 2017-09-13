package net.minecraft.command.server;

import com.mojang.authlib.GameProfile;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.util.math.BlockPos;

public class CommandBanPlayer extends CommandBase {
   public String getName() {
      return "ban";
   }

   public int getRequiredPermissionLevel() {
      return 3;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.ban.usage";
   }

   public boolean checkPermission(MinecraftServer var1, ICommandSender var2) {
      return var1.getPlayerList().getBannedPlayers().isLanServer() && super.checkPermission(var1, var2);
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length >= 1 && var3[0].length() > 0) {
         GameProfile var4 = var1.getPlayerProfileCache().getGameProfileForUsername(var3[0]);
         if (var4 == null) {
            throw new CommandException("commands.ban.failed", new Object[]{var3[0]});
         } else {
            String var5 = null;
            if (var3.length >= 2) {
               var5 = getChatComponentFromNthArg(var2, var3, 1).getUnformattedText();
            }

            UserListBansEntry var6 = new UserListBansEntry(var4, (Date)null, var2.getName(), (Date)null, var5);
            var1.getPlayerList().getBannedPlayers().addEntry(var6);
            EntityPlayerMP var7 = var1.getPlayerList().getPlayerByUsername(var3[0]);
            if (var7 != null) {
               var7.connection.disconnect("You are banned from this server.");
            }

            notifyCommandListener(var2, this, "commands.ban.success", new Object[]{var3[0]});
         }
      } else {
         throw new WrongUsageException("commands.ban.usage", new Object[0]);
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length >= 1 ? getListOfStringsMatchingLastWord(var3, var1.getOnlinePlayerNames()) : Collections.emptyList();
   }
}
