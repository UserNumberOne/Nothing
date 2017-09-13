package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandXP extends CommandBase {
   public String getName() {
      return "xp";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.xp.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length <= 0) {
         throw new WrongUsageException("commands.xp.usage", new Object[0]);
      } else {
         String var4 = var3[0];
         boolean var5 = var4.endsWith("l") || var4.endsWith("L");
         if (var5 && var4.length() > 1) {
            var4 = var4.substring(0, var4.length() - 1);
         }

         int var6 = parseInt(var4);
         boolean var7 = var6 < 0;
         if (var7) {
            var6 *= -1;
         }

         EntityPlayerMP var8 = var3.length > 1 ? getPlayer(var1, var2, var3[1]) : getCommandSenderAsPlayer(var2);
         if (var5) {
            var2.setCommandStat(CommandResultStats.Type.QUERY_RESULT, var8.experienceLevel);
            if (var7) {
               var8.addExperienceLevel(-var6);
               notifyCommandListener(var2, this, "commands.xp.success.negative.levels", new Object[]{var6, var8.getName()});
            } else {
               var8.addExperienceLevel(var6);
               notifyCommandListener(var2, this, "commands.xp.success.levels", new Object[]{var6, var8.getName()});
            }
         } else {
            var2.setCommandStat(CommandResultStats.Type.QUERY_RESULT, var8.experienceTotal);
            if (var7) {
               throw new CommandException("commands.xp.failure.widthdrawXp", new Object[0]);
            }

            var8.addExperience(var6);
            notifyCommandListener(var2, this, "commands.xp.success", new Object[]{var6, var8.getName()});
         }

      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length == 2 ? getListOfStringsMatchingLastWord(var3, var1.getOnlinePlayerNames()) : Collections.emptyList();
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var2 == 1;
   }
}
