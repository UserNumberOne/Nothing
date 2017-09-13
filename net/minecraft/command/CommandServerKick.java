package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandServerKick extends CommandBase {
   public String getName() {
      return "kick";
   }

   public int getRequiredPermissionLevel() {
      return 3;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.kick.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length > 0 && var3[0].length() > 1) {
         EntityPlayerMP var4 = var1.getPlayerList().getPlayerByUsername(var3[0]);
         String var5 = "Kicked by an operator.";
         boolean var6 = false;
         if (var4 == null) {
            throw new PlayerNotFoundException();
         } else {
            if (var3.length >= 2) {
               var5 = getChatComponentFromNthArg(var2, var3, 1).getUnformattedText();
               var6 = true;
            }

            var4.connection.disconnect(var5);
            if (var6) {
               notifyCommandListener(var2, this, "commands.kick.success.reason", new Object[]{var4.getName(), var5});
            } else {
               notifyCommandListener(var2, this, "commands.kick.success", new Object[]{var4.getName()});
            }

         }
      } else {
         throw new WrongUsageException("commands.kick.usage", new Object[0]);
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length >= 1 ? getListOfStringsMatchingLastWord(var3, var1.getOnlinePlayerNames()) : Collections.emptyList();
   }
}
