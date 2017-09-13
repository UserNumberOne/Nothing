package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameRules;

public class CommandGameRule extends CommandBase {
   public String getName() {
      return "gamerule";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.gamerule.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      GameRules var4 = var2.getEntityWorld().getGameRules();
      String var5 = var3.length > 0 ? var3[0] : "";
      String var6 = var3.length > 1 ? buildString(var3, 1) : "";
      switch(var3.length) {
      case 0:
         var2.sendMessage(new TextComponentString(joinNiceString(var4.getRules())));
         break;
      case 1:
         if (!var4.hasRule(var5)) {
            throw new CommandException("commands.gamerule.norule", new Object[]{var5});
         }

         String var7 = var4.getString(var5);
         var2.sendMessage((new TextComponentString(var5)).appendText(" = ").appendText(var7));
         var2.setCommandStat(CommandResultStats.Type.QUERY_RESULT, var4.getInt(var5));
         break;
      default:
         if (var4.areSameType(var5, GameRules.ValueType.BOOLEAN_VALUE) && !"true".equals(var6) && !"false".equals(var6)) {
            throw new CommandException("commands.generic.boolean.invalid", new Object[]{var6});
         }

         var4.setOrCreateGameRule(var5, var6);
         a(var4, var5, var1);
         notifyCommandListener(var2, this, "commands.gamerule.success", new Object[]{var5, var6});
      }

   }

   public static void a(GameRules var0, String var1, MinecraftServer var2) {
      if ("reducedDebugInfo".equals(var1)) {
         int var3 = var0.getBoolean(var1) ? 22 : 23;

         for(EntityPlayerMP var5 : var2.getPlayerList().getPlayers()) {
            var5.connection.sendPacket(new SPacketEntityStatus(var5, (byte)var3));
         }
      }

   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (var3.length == 1) {
         return getListOfStringsMatchingLastWord(var3, this.a(var1).getRules());
      } else {
         if (var3.length == 2) {
            GameRules var5 = this.a(var1);
            if (var5.areSameType(var3[0], GameRules.ValueType.BOOLEAN_VALUE)) {
               return getListOfStringsMatchingLastWord(var3, new String[]{"true", "false"});
            }
         }

         return Collections.emptyList();
      }
   }

   private GameRules a(MinecraftServer var1) {
      return var1.getWorldServer(0).getGameRules();
   }

   public int compareTo(ICommand var1) {
      return this.compareTo(var1);
   }
}
