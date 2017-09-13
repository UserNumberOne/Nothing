package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.border.WorldBorder;

public class CommandWorldBorder extends CommandBase {
   public String getName() {
      return "worldborder";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.worldborder.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 1) {
         throw new WrongUsageException("commands.worldborder.usage", new Object[0]);
      } else {
         WorldBorder var4 = this.a(var1);
         if ("set".equals(var3[0])) {
            if (var3.length != 2 && var3.length != 3) {
               throw new WrongUsageException("commands.worldborder.set.usage", new Object[0]);
            }

            double var5 = var4.getTargetSize();
            double var7 = parseDouble(var3[1], 1.0D, 6.0E7D);
            long var9 = var3.length > 2 ? parseLong(var3[2], 0L, 9223372036854775L) * 1000L : 0L;
            if (var9 > 0L) {
               var4.setTransition(var5, var7, var9);
               if (var5 > var7) {
                  notifyCommandListener(var2, this, "commands.worldborder.setSlowly.shrink.success", new Object[]{String.format("%.1f", var7), String.format("%.1f", var5), Long.toString(var9 / 1000L)});
               } else {
                  notifyCommandListener(var2, this, "commands.worldborder.setSlowly.grow.success", new Object[]{String.format("%.1f", var7), String.format("%.1f", var5), Long.toString(var9 / 1000L)});
               }
            } else {
               var4.setTransition(var7);
               notifyCommandListener(var2, this, "commands.worldborder.set.success", new Object[]{String.format("%.1f", var7), String.format("%.1f", var5)});
            }
         } else if ("add".equals(var3[0])) {
            if (var3.length != 2 && var3.length != 3) {
               throw new WrongUsageException("commands.worldborder.add.usage", new Object[0]);
            }

            double var17 = var4.getDiameter();
            double var21 = var17 + parseDouble(var3[1], -var17, 6.0E7D - var17);
            long var24 = var4.getTimeUntilTarget() + (var3.length > 2 ? parseLong(var3[2], 0L, 9223372036854775L) * 1000L : 0L);
            if (var24 > 0L) {
               var4.setTransition(var17, var21, var24);
               if (var17 > var21) {
                  notifyCommandListener(var2, this, "commands.worldborder.setSlowly.shrink.success", new Object[]{String.format("%.1f", var21), String.format("%.1f", var17), Long.toString(var24 / 1000L)});
               } else {
                  notifyCommandListener(var2, this, "commands.worldborder.setSlowly.grow.success", new Object[]{String.format("%.1f", var21), String.format("%.1f", var17), Long.toString(var24 / 1000L)});
               }
            } else {
               var4.setTransition(var21);
               notifyCommandListener(var2, this, "commands.worldborder.set.success", new Object[]{String.format("%.1f", var21), String.format("%.1f", var17)});
            }
         } else if ("center".equals(var3[0])) {
            if (var3.length != 3) {
               throw new WrongUsageException("commands.worldborder.center.usage", new Object[0]);
            }

            BlockPos var11 = var2.getPosition();
            double var12 = parseDouble((double)var11.getX() + 0.5D, var3[1], true);
            double var14 = parseDouble((double)var11.getZ() + 0.5D, var3[2], true);
            var4.setCenter(var12, var14);
            notifyCommandListener(var2, this, "commands.worldborder.center.success", new Object[]{var12, var14});
         } else if ("damage".equals(var3[0])) {
            if (var3.length < 2) {
               throw new WrongUsageException("commands.worldborder.damage.usage", new Object[0]);
            }

            if ("buffer".equals(var3[1])) {
               if (var3.length != 3) {
                  throw new WrongUsageException("commands.worldborder.damage.buffer.usage", new Object[0]);
               }

               double var18 = parseDouble(var3[2], 0.0D);
               double var22 = var4.getDamageBuffer();
               var4.setDamageBuffer(var18);
               notifyCommandListener(var2, this, "commands.worldborder.damage.buffer.success", new Object[]{String.format("%.1f", var18), String.format("%.1f", var22)});
            } else if ("amount".equals(var3[1])) {
               if (var3.length != 3) {
                  throw new WrongUsageException("commands.worldborder.damage.amount.usage", new Object[0]);
               }

               double var19 = parseDouble(var3[2], 0.0D);
               double var23 = var4.getDamageAmount();
               var4.setDamageAmount(var19);
               notifyCommandListener(var2, this, "commands.worldborder.damage.amount.success", new Object[]{String.format("%.2f", var19), String.format("%.2f", var23)});
            }
         } else if ("warning".equals(var3[0])) {
            if (var3.length < 2) {
               throw new WrongUsageException("commands.worldborder.warning.usage", new Object[0]);
            }

            if ("time".equals(var3[1])) {
               if (var3.length != 3) {
                  throw new WrongUsageException("commands.worldborder.warning.time.usage", new Object[0]);
               }

               int var25 = parseInt(var3[2], 0);
               int var16 = var4.getWarningTime();
               var4.setWarningTime(var25);
               notifyCommandListener(var2, this, "commands.worldborder.warning.time.success", new Object[]{var25, var16});
            } else if ("distance".equals(var3[1])) {
               if (var3.length != 3) {
                  throw new WrongUsageException("commands.worldborder.warning.distance.usage", new Object[0]);
               }

               int var26 = parseInt(var3[2], 0);
               int var27 = var4.getWarningDistance();
               var4.setWarningDistance(var26);
               notifyCommandListener(var2, this, "commands.worldborder.warning.distance.success", new Object[]{var26, var27});
            }
         } else {
            if (!"get".equals(var3[0])) {
               throw new WrongUsageException("commands.worldborder.usage", new Object[0]);
            }

            double var20 = var4.getDiameter();
            var2.setCommandStat(CommandResultStats.Type.QUERY_RESULT, MathHelper.floor(var20 + 0.5D));
            var2.sendMessage(new TextComponentTranslation("commands.worldborder.get.success", new Object[]{String.format("%.0f", var20)}));
         }

      }
   }

   protected WorldBorder a(MinecraftServer var1) {
      return var1.worldServer[0].getWorldBorder();
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (var3.length == 1) {
         return getListOfStringsMatchingLastWord(var3, new String[]{"set", "center", "damage", "warning", "add", "get"});
      } else if (var3.length == 2 && "damage".equals(var3[0])) {
         return getListOfStringsMatchingLastWord(var3, new String[]{"buffer", "amount"});
      } else if (var3.length >= 2 && var3.length <= 3 && "center".equals(var3[0])) {
         return getTabCompletionCoordinateXZ(var3, 1, var4);
      } else {
         return var3.length == 2 && "warning".equals(var3[0]) ? getListOfStringsMatchingLastWord(var3, new String[]{"time", "distance"}) : Collections.emptyList();
      }
   }
}
