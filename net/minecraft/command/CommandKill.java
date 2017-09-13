package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandKill extends CommandBase {
   public String getName() {
      return "kill";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.kill.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (args.length == 0) {
         EntityPlayer entityplayer = getCommandSenderAsPlayer(sender);
         entityplayer.onKillCommand();
         notifyCommandListener(sender, this, "commands.kill.successful", new Object[]{entityplayer.getDisplayName()});
      } else {
         Entity entity = getEntity(server, sender, args[0]);
         entity.onKillCommand();
         notifyCommandListener(sender, this, "commands.kill.successful", new Object[]{entity.getDisplayName()});
      }

   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return index == 0;
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return args.length == 1 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : Collections.emptyList();
   }
}
