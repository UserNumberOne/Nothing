package net.minecraft.command;

import com.google.gson.JsonParseException;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandTitle extends CommandBase {
   private static final Logger LOGGER = LogManager.getLogger();

   public String getName() {
      return "title";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.title.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 2) {
         throw new WrongUsageException("commands.title.usage", new Object[0]);
      } else {
         if (var3.length < 3) {
            if ("title".equals(var3[1]) || "subtitle".equals(var3[1])) {
               throw new WrongUsageException("commands.title.usage.title", new Object[0]);
            }

            if ("times".equals(var3[1])) {
               throw new WrongUsageException("commands.title.usage.times", new Object[0]);
            }
         }

         EntityPlayerMP var4 = getPlayer(var1, var2, var3[0]);
         SPacketTitle.Type var5 = SPacketTitle.Type.byName(var3[1]);
         if (var5 != SPacketTitle.Type.CLEAR && var5 != SPacketTitle.Type.RESET) {
            if (var5 == SPacketTitle.Type.TIMES) {
               if (var3.length != 5) {
                  throw new WrongUsageException("commands.title.usage", new Object[0]);
               }

               int var11 = parseInt(var3[2]);
               int var7 = parseInt(var3[3]);
               int var8 = parseInt(var3[4]);
               SPacketTitle var9 = new SPacketTitle(var11, var7, var8);
               var4.connection.sendPacket(var9);
               notifyCommandListener(var2, this, "commands.title.success", new Object[0]);
            } else {
               if (var3.length < 3) {
                  throw new WrongUsageException("commands.title.usage", new Object[0]);
               }

               String var12 = buildString(var3, 2);

               ITextComponent var13;
               try {
                  var13 = ITextComponent.Serializer.jsonToComponent(var12);
               } catch (JsonParseException var10) {
                  throw toSyntaxException(var10);
               }

               SPacketTitle var14 = new SPacketTitle(var5, TextComponentUtils.processComponent(var2, var13, var4));
               var4.connection.sendPacket(var14);
               notifyCommandListener(var2, this, "commands.title.success", new Object[0]);
            }
         } else {
            if (var3.length != 2) {
               throw new WrongUsageException("commands.title.usage", new Object[0]);
            }

            SPacketTitle var6 = new SPacketTitle(var5, (ITextComponent)null);
            var4.connection.sendPacket(var6);
            notifyCommandListener(var2, this, "commands.title.success", new Object[0]);
         }

      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length == 1 ? getListOfStringsMatchingLastWord(var3, var1.getOnlinePlayerNames()) : (var3.length == 2 ? getListOfStringsMatchingLastWord(var3, SPacketTitle.Type.getNames()) : Collections.emptyList());
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var2 == 0;
   }
}
