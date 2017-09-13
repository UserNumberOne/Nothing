package net.minecraft.command.server;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class CommandMessage extends CommandBase {
   public List getAliases() {
      return Arrays.asList("w", "msg");
   }

   public String getName() {
      return "tell";
   }

   public int getRequiredPermissionLevel() {
      return 0;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.message.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 2) {
         throw new WrongUsageException("commands.message.usage", new Object[0]);
      } else {
         EntityPlayerMP var4 = getPlayer(var1, var2, var3[0]);
         if (var4 == var2) {
            throw new PlayerNotFoundException("commands.message.sameTarget", new Object[0]);
         } else {
            ITextComponent var5 = getChatComponentFromNthArg(var2, var3, 1, !(var2 instanceof EntityPlayer));
            TextComponentTranslation var6 = new TextComponentTranslation("commands.message.display.incoming", new Object[]{var2.getDisplayName(), var5.createCopy()});
            TextComponentTranslation var7 = new TextComponentTranslation("commands.message.display.outgoing", new Object[]{var4.getDisplayName(), var5.createCopy()});
            var6.getStyle().setColor(TextFormatting.GRAY).setItalic(Boolean.valueOf(true));
            var7.getStyle().setColor(TextFormatting.GRAY).setItalic(Boolean.valueOf(true));
            var4.sendMessage(var6);
            var2.sendMessage(var7);
         }
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return getListOfStringsMatchingLastWord(var3, var1.getOnlinePlayerNames());
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var2 == 0;
   }
}
