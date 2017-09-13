package net.minecraft.command.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.src.MinecraftServer;
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
      var2.sendMessage(new TextComponentTranslation("commands.save.start", new Object[0]));
      if (var1.getPlayerList() != null) {
         var1.getPlayerList().saveAllPlayerData();
      }

      try {
         for(int var4 = 0; var4 < var1.worldServer.length; ++var4) {
            if (var1.worldServer[var4] != null) {
               WorldServer var5 = var1.worldServer[var4];
               boolean var6 = var5.disableLevelSaving;
               var5.disableLevelSaving = false;
               var5.saveAllChunks(true, (IProgressUpdate)null);
               var5.disableLevelSaving = var6;
            }
         }

         if (var3.length > 0 && "flush".equals(var3[0])) {
            var2.sendMessage(new TextComponentTranslation("commands.save.flushStart", new Object[0]));

            for(int var8 = 0; var8 < var1.worldServer.length; ++var8) {
               if (var1.worldServer[var8] != null) {
                  WorldServer var9 = var1.worldServer[var8];
                  boolean var10 = var9.disableLevelSaving;
                  var9.disableLevelSaving = false;
                  var9.saveChunkData();
                  var9.disableLevelSaving = var10;
               }
            }

            var2.sendMessage(new TextComponentTranslation("commands.save.flushEnd", new Object[0]));
         }
      } catch (MinecraftException var7) {
         notifyCommandListener(var2, this, "commands.save.failed", new Object[]{var7.getMessage()});
         return;
      }

      notifyCommandListener(var2, this, "commands.save.success", new Object[0]);
   }
}
