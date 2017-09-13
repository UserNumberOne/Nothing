package net.minecraft.command.server;

import com.mojang.authlib.GameProfile;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandDeOp extends CommandBase {
   public String getName() {
      return "deop";
   }

   public int getRequiredPermissionLevel() {
      return 3;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.deop.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length == 1 && var3[0].length() > 0) {
         GameProfile var4 = var1.getPlayerList().getOppedPlayers().getGameProfileFromName(var3[0]);
         if (var4 == null) {
            throw new CommandException("commands.deop.failed", new Object[]{var3[0]});
         } else {
            var1.getPlayerList().removeOp(var4);
            notifyCommandListener(var2, this, "commands.deop.success", new Object[]{var3[0]});
         }
      } else {
         throw new WrongUsageException("commands.deop.usage", new Object[0]);
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length == 1 ? getListOfStringsMatchingLastWord(var3, var1.getPlayerList().getOppedPlayerNames()) : Collections.emptyList();
   }
}
