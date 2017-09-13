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

public class CommandPardonPlayer extends CommandBase {
   public String getName() {
      return "pardon";
   }

   public int getRequiredPermissionLevel() {
      return 3;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.unban.usage";
   }

   public boolean canUse(MinecraftServer var1, ICommandSender var2) {
      return var1.getPlayerList().getBannedPlayers().isLanServer() && super.canUse(var1, var2);
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length == 1 && var3[0].length() > 0) {
         GameProfile var4 = var1.getPlayerList().getBannedPlayers().getBannedProfile(var3[0]);
         if (var4 == null) {
            throw new CommandException("commands.unban.failed", new Object[]{var3[0]});
         } else {
            var1.getPlayerList().getBannedPlayers().removeEntry(var4);
            notifyCommandListener(var2, this, "commands.unban.success", new Object[]{var3[0]});
         }
      } else {
         throw new WrongUsageException("commands.unban.usage", new Object[0]);
      }
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length == 1 ? getListOfStringsMatchingLastWord(var3, var1.getPlayerList().getBannedPlayers().getKeys()) : Collections.emptyList();
   }
}
