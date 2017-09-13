package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.server.MinecraftServer;
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
      GameRules gamerules = this.getOverWorldGameRules(server);
      String s = args.length > 0 ? args[0] : "";
      String s1 = args.length > 1 ? buildString(args, 1) : "";
      switch(args.length) {
      case 0:
         sender.sendMessage(new TextComponentString(joinNiceString(gamerules.getRules())));
         break;
      case 1:
         if (!gamerules.hasRule(s)) {
            throw new CommandException("commands.gamerule.norule", new Object[]{s});
         }

         String s2 = gamerules.getString(s);
         sender.sendMessage((new TextComponentString(s)).appendText(" = ").appendText(s2));
         sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, gamerules.getInt(s));
         break;
      default:
         if (gamerules.areSameType(s, GameRules.ValueType.BOOLEAN_VALUE) && !"true".equals(s1) && !"false".equals(s1)) {
            throw new CommandException("commands.generic.boolean.invalid", new Object[]{s1});
         }

         gamerules.setOrCreateGameRule(s, s1);
         notifyGameRuleChange(gamerules, s, server);
         notifyCommandListener(sender, this, "commands.gamerule.success", new Object[]{s, s1});
      }

   }

   public static void notifyGameRuleChange(GameRules var0, String var1, MinecraftServer var2) {
      if ("reducedDebugInfo".equals(p_184898_1_)) {
         byte b0 = (byte)(rules.getBoolean(p_184898_1_) ? 22 : 23);

         for(EntityPlayerMP entityplayermp : server.getPlayerList().getPlayers()) {
            entityplayermp.connection.sendPacket(new SPacketEntityStatus(entityplayermp, b0));
         }
      }

   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (args.length == 1) {
         return getListOfStringsMatchingLastWord(args, this.getOverWorldGameRules(server).getRules());
      } else {
         if (args.length == 2) {
            GameRules gamerules = this.getOverWorldGameRules(server);
            if (gamerules.areSameType(args[0], GameRules.ValueType.BOOLEAN_VALUE)) {
               return getListOfStringsMatchingLastWord(args, new String[]{"true", "false"});
            }
         }

         return Collections.emptyList();
      }
   }

   private GameRules getOverWorldGameRules(MinecraftServer var1) {
      return server.worldServerForDimension(0).getGameRules();
   }
}
