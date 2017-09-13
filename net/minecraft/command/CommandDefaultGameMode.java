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
      if (args.length <= 0) {
         throw new WrongUsageException("commands.defaultgamemode.usage", new Object[0]);
      } else {
         GameType gametype = this.getGameModeFromCommand(sender, args[0]);
         this.setDefaultGameType(gametype, server);
         notifyCommandListener(sender, this, "commands.defaultgamemode.success", new Object[]{new TextComponentTranslation("gameMode." + gametype.getName(), new Object[0])});
      }
   }

   protected void setDefaultGameType(GameType var1, MinecraftServer var2) {
      server.setGameType(gameType);
      if (server.getForceGamemode()) {
         for(EntityPlayerMP entityplayermp : server.getPlayerList().getPlayers()) {
            entityplayermp.setGameType(gameType);
         }
      }

   }
}
