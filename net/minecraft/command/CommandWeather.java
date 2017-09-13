package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;

public class CommandWeather extends CommandBase {
   public String getName() {
      return "weather";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.weather.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length >= 1 && var3.length <= 2) {
         int var4 = (300 + (new Random()).nextInt(600)) * 20;
         if (var3.length >= 2) {
            var4 = parseInt(var3[1], 1, 1000000) * 20;
         }

         WorldServer var5 = var1.worldServer[0];
         WorldInfo var6 = var5.getWorldInfo();
         if ("clear".equalsIgnoreCase(var3[0])) {
            var6.setCleanWeatherTime(var4);
            var6.setRainTime(0);
            var6.setThunderTime(0);
            var6.setRaining(false);
            var6.setThundering(false);
            notifyCommandListener(var2, this, "commands.weather.clear", new Object[0]);
         } else if ("rain".equalsIgnoreCase(var3[0])) {
            var6.setCleanWeatherTime(0);
            var6.setRainTime(var4);
            var6.setThunderTime(var4);
            var6.setRaining(true);
            var6.setThundering(false);
            notifyCommandListener(var2, this, "commands.weather.rain", new Object[0]);
         } else {
            if (!"thunder".equalsIgnoreCase(var3[0])) {
               throw new WrongUsageException("commands.weather.usage", new Object[0]);
            }

            var6.setCleanWeatherTime(0);
            var6.setRainTime(var4);
            var6.setThunderTime(var4);
            var6.setRaining(true);
            var6.setThundering(true);
            notifyCommandListener(var2, this, "commands.weather.thunder", new Object[0]);
         }

      } else {
         throw new WrongUsageException("commands.weather.usage", new Object[0]);
      }
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length == 1 ? getListOfStringsMatchingLastWord(var3, new String[]{"clear", "rain", "thunder"}) : Collections.emptyList();
   }
}
