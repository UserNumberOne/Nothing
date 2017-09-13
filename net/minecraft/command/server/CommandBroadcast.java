package net.minecraft.command.server;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandBroadcast extends CommandBase {
   public String getName() {
      return "say";
   }

   public int getRequiredPermissionLevel() {
      return 1;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.say.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length > 0 && var3[0].length() > 0) {
         ITextComponent var4 = getChatComponentFromNthArg(var2, var3, 0, true);
         var1.getPlayerList().sendChatMsg(new TextComponentTranslation("chat.type.announcement", new Object[]{var2.getDisplayName(), var4}));
      } else {
         throw new WrongUsageException("commands.say.usage", new Object[0]);
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length >= 1 ? getListOfStringsMatchingLastWord(var3, var1.getOnlinePlayerNames()) : Collections.emptyList();
   }
}
