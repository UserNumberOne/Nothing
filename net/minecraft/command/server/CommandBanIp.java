package net.minecraft.command.server;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.UserListIPBansEntry;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

public class CommandBanIp extends CommandBase {
   public static final Pattern IP_PATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

   public String getName() {
      return "ban-ip";
   }

   public int getRequiredPermissionLevel() {
      return 3;
   }

   public boolean canUse(MinecraftServer var1, ICommandSender var2) {
      return var1.getPlayerList().getBannedIPs().isLanServer() && super.canUse(var1, var2);
   }

   public String getUsage(ICommandSender var1) {
      return "commands.banip.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length >= 1 && var3[0].length() > 1) {
         ITextComponent var4 = var3.length >= 2 ? getChatComponentFromNthArg(var2, var3, 1) : null;
         Matcher var5 = IP_PATTERN.matcher(var3[0]);
         if (var5.matches()) {
            this.a(var1, var2, var3[0], var4 == null ? null : var4.getUnformattedText());
         } else {
            EntityPlayerMP var6 = var1.getPlayerList().getPlayerByUsername(var3[0]);
            if (var6 == null) {
               throw new PlayerNotFoundException("commands.banip.invalid", new Object[0]);
            }

            this.a(var1, var2, var6.getPlayerIP(), var4 == null ? null : var4.getUnformattedText());
         }

      } else {
         throw new WrongUsageException("commands.banip.usage", new Object[0]);
      }
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length == 1 ? getListOfStringsMatchingLastWord(var3, var1.getPlayers()) : Collections.emptyList();
   }

   protected void a(MinecraftServer var1, ICommandSender var2, String var3, @Nullable String var4) {
      UserListIPBansEntry var5 = new UserListIPBansEntry(var3, (Date)null, var2.getName(), (Date)null, var4);
      var1.getPlayerList().getBannedIPs().addEntry(var5);
      List var6 = var1.getPlayerList().getPlayersMatchingAddress(var3);
      String[] var7 = new String[var6.size()];
      int var8 = 0;

      for(EntityPlayerMP var10 : var6) {
         var10.connection.disconnect("You have been IP banned.");
         var7[var8++] = var10.getName();
      }

      if (var6.isEmpty()) {
         notifyCommandListener(var2, this, "commands.banip.success", new Object[]{var3});
      } else {
         notifyCommandListener(var2, this, "commands.banip.success.players", new Object[]{var3, joinNiceString(var7)});
      }

   }
}
