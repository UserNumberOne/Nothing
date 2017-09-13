package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;

public class CommandGameMode extends CommandBase {
   public String getName() {
      return "gamemode";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.gamemode.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length <= 0) {
         throw new WrongUsageException("commands.gamemode.usage", new Object[0]);
      } else {
         GameType var4 = this.getGameModeFromCommand(var2, var3[0]);
         EntityPlayerMP var5 = var3.length >= 2 ? getPlayer(var1, var2, var3[1]) : getCommandSenderAsPlayer(var2);
         var5.setGameType(var4);
         TextComponentTranslation var6 = new TextComponentTranslation("gameMode." + var4.getName(), new Object[0]);
         if (var2.getEntityWorld().getGameRules().getBoolean("sendCommandFeedback")) {
            var5.sendMessage(new TextComponentTranslation("gameMode.changed", new Object[]{var6}));
         }

         if (var5 == var2) {
            notifyCommandListener(var2, this, 1, "commands.gamemode.success.self", new Object[]{var6});
         } else {
            notifyCommandListener(var2, this, 1, "commands.gamemode.success.other", new Object[]{var5.getName(), var6});
         }

      }
   }

   protected GameType getGameModeFromCommand(ICommandSender var1, String var2) throws CommandException, NumberInvalidException {
      GameType var3 = GameType.parseGameTypeWithDefault(var2, GameType.NOT_SET);
      return var3 == GameType.NOT_SET ? WorldSettings.getGameTypeById(parseInt(var2, 0, GameType.values().length - 2)) : var3;
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length == 1 ? getListOfStringsMatchingLastWord(var3, new String[]{"survival", "creative", "adventure", "spectator"}) : (var3.length == 2 ? getListOfStringsMatchingLastWord(var3, var1.getOnlinePlayerNames()) : Collections.emptyList());
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var2 == 1;
   }
}
