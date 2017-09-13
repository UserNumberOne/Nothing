package net.minecraft.command;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;

public class CommandDefaultGameMode extends CommandGameMode {
   public String getName() {
      return "defaultgamemode";
   }

   public String getUsage(ICommandSender var1) {
      return "commands.defaultgamemode.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length <= 0) {
         throw new WrongUsageException("commands.defaultgamemode.usage", new Object[0]);
      } else {
         GameType var4 = this.getGameModeFromCommand(var2, var3[0]);
         this.setDefaultGameType(var4, var1);
         notifyCommandListener(var2, this, "commands.defaultgamemode.success", new Object[]{new TextComponentTranslation("gameMode." + var4.getName(), new Object[0])});
      }
   }

   protected void setDefaultGameType(GameType var1, MinecraftServer var2) {
      var2.setGameType(var1);
      if (var2.getForceGamemode()) {
         for(EntityPlayerMP var4 : var2.getPlayerList().getPlayers()) {
            var4.setGameType(var1);
         }
      }

   }
}
