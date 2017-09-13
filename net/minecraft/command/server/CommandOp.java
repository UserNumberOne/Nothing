package net.minecraft.command.server;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandOp extends CommandBase {
   public String getName() {
      return "op";
   }

   public int getRequiredPermissionLevel() {
      return 3;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.op.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length == 1 && var3[0].length() > 0) {
         GameProfile var4 = var1.getPlayerProfileCache().getGameProfileForUsername(var3[0]);
         if (var4 == null) {
            throw new CommandException("commands.op.failed", new Object[]{var3[0]});
         } else {
            var1.getPlayerList().addOp(var4);
            notifyCommandListener(var2, this, "commands.op.success", new Object[]{var3[0]});
         }
      } else {
         throw new WrongUsageException("commands.op.usage", new Object[0]);
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (var3.length == 1) {
         String var5 = var3[var3.length - 1];
         ArrayList var6 = Lists.newArrayList();

         for(GameProfile var10 : var1.getOnlinePlayerProfiles()) {
            if (!var1.getPlayerList().canSendCommands(var10) && doesStringStartWith(var5, var10.getName())) {
               var6.add(var10.getName());
            }
         }

         return var6;
      } else {
         return Collections.emptyList();
      }
   }
}
