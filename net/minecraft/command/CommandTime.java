package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

public class CommandTime extends CommandBase {
   public String getName() {
      return "time";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.time.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length > 1) {
         if ("set".equals(var3[0])) {
            int var8;
            if ("day".equals(var3[1])) {
               var8 = 1000;
            } else if ("night".equals(var3[1])) {
               var8 = 13000;
            } else {
               var8 = parseInt(var3[1], 0);
            }

            this.a(var1, var8);
            notifyCommandListener(var2, this, "commands.time.set", new Object[]{var8});
            return;
         }

         if ("add".equals(var3[0])) {
            int var7 = parseInt(var3[1], 0);
            this.b(var1, var7);
            notifyCommandListener(var2, this, "commands.time.added", new Object[]{var7});
            return;
         }

         if ("query".equals(var3[0])) {
            if ("daytime".equals(var3[1])) {
               int var6 = (int)(var2.getEntityWorld().getWorldTime() % 24000L);
               var2.setCommandStat(CommandResultStats.Type.QUERY_RESULT, var6);
               notifyCommandListener(var2, this, "commands.time.query", new Object[]{var6});
               return;
            }

            if ("day".equals(var3[1])) {
               int var5 = (int)(var2.getEntityWorld().getWorldTime() / 24000L % 2147483647L);
               var2.setCommandStat(CommandResultStats.Type.QUERY_RESULT, var5);
               notifyCommandListener(var2, this, "commands.time.query", new Object[]{var5});
               return;
            }

            if ("gametime".equals(var3[1])) {
               int var4 = (int)(var2.getEntityWorld().getTotalWorldTime() % 2147483647L);
               var2.setCommandStat(CommandResultStats.Type.QUERY_RESULT, var4);
               notifyCommandListener(var2, this, "commands.time.query", new Object[]{var4});
               return;
            }
         }
      }

      throw new WrongUsageException("commands.time.usage", new Object[0]);
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (var3.length == 1) {
         return getListOfStringsMatchingLastWord(var3, new String[]{"set", "add", "query"});
      } else if (var3.length == 2 && "set".equals(var3[0])) {
         return getListOfStringsMatchingLastWord(var3, new String[]{"day", "night"});
      } else {
         return var3.length == 2 && "query".equals(var3[0]) ? getListOfStringsMatchingLastWord(var3, new String[]{"daytime", "gametime", "day"}) : Collections.emptyList();
      }
   }

   protected void a(MinecraftServer var1, int var2) {
      for(int var3 = 0; var3 < var1.worldServer.length; ++var3) {
         var1.worldServer[var3].setWorldTime((long)var2);
      }

   }

   protected void b(MinecraftServer var1, int var2) {
      for(int var3 = 0; var3 < var1.worldServer.length; ++var3) {
         WorldServer var4 = var1.worldServer[var3];
         var4.setWorldTime(var4.getWorldTime() + (long)var2);
      }

   }
}
