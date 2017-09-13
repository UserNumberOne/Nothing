package net.minecraft.command.server;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandEmote extends CommandBase {
   public String getName() {
      return "me";
   }

   public int getRequiredPermissionLevel() {
      return 0;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.me.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (args.length <= 0) {
         throw new WrongUsageException("commands.me.usage", new Object[0]);
      } else {
         ITextComponent itextcomponent = getChatComponentFromNthArg(sender, args, 0, !(sender instanceof EntityPlayer));
         server.getPlayerList().sendChatMsg(new TextComponentTranslation("chat.type.emote", new Object[]{sender.getDisplayName(), itextcomponent}));
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
   }
}
