package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandSetSpawnpoint extends CommandBase {
   public String getName() {
      return "spawnpoint";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.spawnpoint.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length > 1 && var3.length < 4) {
         throw new WrongUsageException("commands.spawnpoint.usage", new Object[0]);
      } else {
         EntityPlayerMP var4 = var3.length > 0 ? a(var1, var2, var3[0]) : getCommandSenderAsPlayer(var2);
         BlockPos var5 = var3.length > 3 ? parseBlockPos(var2, var3, 1, true) : var4.getPosition();
         if (var4.world != null) {
            var4.setSpawnPoint(var5, true);
            notifyCommandListener(var2, this, "commands.spawnpoint.success", new Object[]{var4.getName(), var5.getX(), var5.getY(), var5.getZ()});
         }

      }
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (var3.length == 1) {
         return getListOfStringsMatchingLastWord(var3, var1.getPlayers());
      } else {
         return var3.length > 1 && var3.length <= 4 ? getTabCompletionCoordinate(var3, 1, var4) : Collections.emptyList();
      }
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var2 == 0;
   }
}
