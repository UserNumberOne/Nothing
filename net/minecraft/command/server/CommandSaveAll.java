package net.minecraft.command.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;

public class CommandSaveAll extends CommandBase {
   public String getName() {
      return "save-all";
   }

   public String getUsage(ICommandSender var1) {
      return "commands.save.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      sender.sendMessage(new TextComponentTranslation("commands.save.start", new Object[0]));
      if (server.getPlayerList() != null) {
         server.getPlayerList().saveAllPlayerData();
      }

      try {
         for(int i = 0; i < server.worlds.length; ++i) {
            if (server.worlds[i] != null) {
               WorldServer worldserver = server.worlds[i];
               boolean flag = worldserver.disableLevelSaving;
               worldserver.disableLevelSaving = false;
               worldserver.saveAllChunks(true, (IProgressUpdate)null);
               worldserver.disableLevelSaving = flag;
            }
         }

         if (args.length > 0 && "flush".equals(args[0])) {
            sender.sendMessage(new TextComponentTranslation("commands.save.flushStart", new Object[0]));

            for(int j = 0; j < server.worlds.length; ++j) {
               if (server.worlds[j] != null) {
                  WorldServer worldserver1 = server.worlds[j];
                  boolean flag1 = worldserver1.disableLevelSaving;
                  worldserver1.disableLevelSaving = false;
                  worldserver1.saveChunkData();
                  worldserver1.disableLevelSaving = flag1;
               }
            }

            sender.sendMessage(new TextComponentTranslation("commands.save.flushEnd", new Object[0]));
         }
      } catch (MinecraftException var7) {
         notifyCommandListener(sender, this, "commands.save.failed", new Object[]{var7.getMessage()});
         return;
      }

      notifyCommandListener(sender, this, "commands.save.success", new Object[0]);
   }
}
