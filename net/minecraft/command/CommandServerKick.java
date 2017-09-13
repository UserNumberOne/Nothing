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
      if (args.length > 0 && args[0].length() > 1) {
         EntityPlayerMP entityplayermp = server.getPlayerList().getPlayerByUsername(args[0]);
         String s = "Kicked by an operator.";
         boolean flag = false;
         if (entityplayermp == null) {
            throw new PlayerNotFoundException();
         } else {
            if (args.length >= 2) {
               s = getChatComponentFromNthArg(sender, args, 1).getUnformattedText();
               flag = true;
            }

            entityplayermp.connection.disconnect(s);
            if (flag) {
               notifyCommandListener(sender, this, "commands.kick.success.reason", new Object[]{entityplayermp.getName(), s});
            } else {
               notifyCommandListener(sender, this, "commands.kick.success", new Object[]{entityplayermp.getName()});
            }

         }
      } else {
         throw new WrongUsageException("commands.kick.usage", new Object[0]);
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return args.length >= 1 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : Collections.emptyList();
   }
}
