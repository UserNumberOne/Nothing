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

   public String getUsage(ICommandSender icommandlistener) {
      return "commands.gamerule.usage";
   }

   public void execute(MinecraftServer minecraftserver, ICommandSender icommandlistener, String[] astring) throws CommandException {
      GameRules gamerules = icommandlistener.getEntityWorld().getGameRules();
      String s = astring.length > 0 ? astring[0] : "";
      String s1 = astring.length > 1 ? buildString(astring, 1) : "";
      switch(astring.length) {
      case 0:
         icommandlistener.sendMessage(new TextComponentString(joinNiceString(gamerules.getRules())));
         break;
      case 1:
         if (!gamerules.hasRule(s)) {
            throw new CommandException("commands.gamerule.norule", new Object[]{s});
         }

         String s2 = gamerules.getString(s);
         icommandlistener.sendMessage((new TextComponentString(s)).appendText(" = ").appendText(s2));
         icommandlistener.setCommandStat(CommandResultStats.Type.QUERY_RESULT, gamerules.getInt(s));
         break;
      default:
         if (gamerules.areSameType(s, GameRules.ValueType.BOOLEAN_VALUE) && !"true".equals(s1) && !"false".equals(s1)) {
            throw new CommandException("commands.generic.boolean.invalid", new Object[]{s1});
         }

         gamerules.setOrCreateGameRule(s, s1);
         a(gamerules, s, minecraftserver);
         notifyCommandListener(icommandlistener, this, "commands.gamerule.success", new Object[]{s, s1});
      }

   }

   public static void a(GameRules gamerules, String s, MinecraftServer minecraftserver) {
      if ("reducedDebugInfo".equals(s)) {
         int i = gamerules.getBoolean(s) ? 22 : 23;

         for(EntityPlayerMP entityplayer : minecraftserver.getPlayerList().getPlayers()) {
            entityplayer.connection.sendPacket(new SPacketEntityStatus(entityplayer, (byte)i));
         }
      }

   }

   public List tabComplete(MinecraftServer minecraftserver, ICommandSender icommandlistener, String[] astring, @Nullable BlockPos blockposition) {
      if (astring.length == 1) {
         return getListOfStringsMatchingLastWord(astring, this.a(minecraftserver).getRules());
      } else {
         if (astring.length == 2) {
            GameRules gamerules = this.a(minecraftserver);
            if (gamerules.areSameType(astring[0], GameRules.ValueType.BOOLEAN_VALUE)) {
               return getListOfStringsMatchingLastWord(astring, new String[]{"true", "false"});
            }
         }

         return Collections.emptyList();
      }
   }

   private GameRules a(MinecraftServer minecraftserver) {
      return minecraftserver.getWorldServer(0).getGameRules();
   }

   public int compareTo(ICommand o) {
      return this.compareTo(o);
   }
}
