package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.src.MinecraftServer;
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
      if (var3.length == 0) {
         EntityPlayerMP var5 = getCommandSenderAsPlayer(var2);
         var5.onKillCommand();
         notifyCommandListener(var2, this, "commands.kill.successful", new Object[]{var5.getDisplayName()});
      } else {
         Entity var4 = b(var1, var2, var3[0]);
         var4.onKillCommand();
         notifyCommandListener(var2, this, "commands.kill.successful", new Object[]{var4.getDisplayName()});
      }
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var2 == 0;
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length == 1 ? getListOfStringsMatchingLastWord(var3, var1.getPlayers()) : Collections.emptyList();
   }
}
