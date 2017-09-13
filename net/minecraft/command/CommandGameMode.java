package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
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
      if (args.length <= 0) {
         throw new WrongUsageException("commands.gamemode.usage", new Object[0]);
      } else {
         GameType gametype = this.getGameModeFromCommand(sender, args[0]);
         EntityPlayer entityplayer = args.length >= 2 ? getPlayer(server, sender, args[1]) : getCommandSenderAsPlayer(sender);
         entityplayer.setGameType(gametype);
         ITextComponent itextcomponent = new TextComponentTranslation("gameMode." + gametype.getName(), new Object[0]);
         if (sender.getEntityWorld().getGameRules().getBoolean("sendCommandFeedback")) {
            entityplayer.sendMessage(new TextComponentTranslation("gameMode.changed", new Object[]{itextcomponent}));
         }

         if (entityplayer == sender) {
            notifyCommandListener(sender, this, 1, "commands.gamemode.success.self", new Object[]{itextcomponent});
         } else {
            notifyCommandListener(sender, this, 1, "commands.gamemode.success.other", new Object[]{entityplayer.getName(), itextcomponent});
         }

      }
   }

   protected GameType getGameModeFromCommand(ICommandSender var1, String var2) throws CommandException, NumberInvalidException {
      GameType gametype = GameType.parseGameTypeWithDefault(gameModeString, GameType.NOT_SET);
      return gametype == GameType.NOT_SET ? WorldSettings.getGameTypeById(parseInt(gameModeString, 0, GameType.values().length - 2)) : gametype;
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return args.length == 1 ? getListOfStringsMatchingLastWord(args, new String[]{"survival", "creative", "adventure", "spectator"}) : (args.length == 2 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : Collections.emptyList());
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return index == 1;
   }
}
